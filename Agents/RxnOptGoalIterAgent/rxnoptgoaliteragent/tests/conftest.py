from testcontainers.core.container import DockerContainer
from random import randint
from pathlib import Path
from rdflib import Graph
from flask import Flask
from enum import Enum
import logging
import pkgutil
import pytest
import shutil
import time
import uuid
import os

from pyderivationagent.conf import config_derivation_agent

from rxnoptgoaliteragent.kg_operations import RxnOptGoalIterSparqlClient
from rxnoptgoaliteragent.agent import RxnOptGoalIterAgent
from rxnoptgoaliteragent.data_model import *

from chemistry_and_robots.tests.conftest import TargetIRIs

logging.getLogger("py4j").setLevel(logging.INFO)

from pyderivationagent.kg_operations import TIME_HASTIME
from pyderivationagent.kg_operations import TIME_INTIMEPOSITION
from pyderivationagent.kg_operations import TIME_NUMERICPOSITION


# ----------------------------------------------------------------------------------
# Constant and configuration
# ----------------------------------------------------------------------------------

THIS_DIR = os.path.dirname(os.path.abspath(__file__))
SECRETS_PATH = os.path.join(THIS_DIR,'dummy_services_secrets')
SECRETS_FILE_PATH = os.path.join(THIS_DIR,'dummy_services_secrets', 'dummy_test_auth')
URL_FILE_PATH = os.path.join(THIS_DIR,'dummy_services_secrets', 'dummy_test_url')
TEST_TRIPLES_DIR = os.path.join(THIS_DIR, 'test_triples')
DOWNLOADED_DIR = os.path.join(THIS_DIR,'downloaded_files_for_test')

KG_SERVICE = "blazegraph"
KG_ROUTE = "blazegraph/namespace/kb/sparql"
FS_SERVICE = "fileserver"
FS_ROUTE = "FileServer/"

# Configuration env files
# NOTE the triple store URL provided in the agent.*.env files are the URL to access blazegraph container WITHIN the docker stack
ROGI_AGENT_ENV = os.path.join(THIS_DIR,'agent.goal.iter.env.test')

DERIVATION_INSTANCE_BASE_URL = config_derivation_agent(ROGI_AGENT_ENV).DERIVATION_INSTANCE_BASE_URL

# NOTE we use a new rxn exp instance as the basis for create_dummy_triples_for_performance_indicator
PLACEHOLDER_NEW_EXPERIMENT_IRI = TargetIRIs.NEW_RXN_EXP_1_IRI.value


# ----------------------------------------------------------------------------------
# Pytest session related functions
# ----------------------------------------------------------------------------------

def pytest_sessionstart(session):
    """ This will run before all the tests"""
    if os.path.exists(SECRETS_FILE_PATH):
        os.remove(SECRETS_FILE_PATH)
    if os.path.exists(URL_FILE_PATH):
        os.remove(URL_FILE_PATH)
    if os.path.exists(DOWNLOADED_DIR):
        shutil.rmtree(DOWNLOADED_DIR)

def pytest_sessionfinish(session):
    """ This will run after all the tests"""
    if os.path.exists(SECRETS_FILE_PATH):
        os.remove(SECRETS_FILE_PATH)
    if os.path.exists(URL_FILE_PATH):
        os.remove(URL_FILE_PATH)
    if os.path.exists(DOWNLOADED_DIR):
        shutil.rmtree(DOWNLOADED_DIR)


# ----------------------------------------------------------------------------------
# Session-scoped test fixtures
# ----------------------------------------------------------------------------------

@pytest.fixture(scope="session")
def get_service_url(session_scoped_container_getter):
    def _get_service_url(service_name, url_route):
        service = session_scoped_container_getter.get(service_name).network_info[0]
        service_url = f"http://localhost:{service.host_port}/{url_route}"
        return service_url

    # this will run only once per entire test session and ensures that all the services
    # in docker containers are ready. Increase the sleep value in case services need a bit
    # more time to run on your machine.
    time.sleep(8)
    return _get_service_url

@pytest.fixture(scope="session")
def get_service_auth():
    def _get_service_auth(service_name):
        password_file = os.path.join(SECRETS_PATH,service_name+'_passwd.txt')
        user_file = os.path.join(SECRETS_PATH,service_name+'_user.txt')

        # read service auth from files
        username = ''
        password = ''
        if os.path.exists(user_file):
            with open(user_file) as f:
                username = f.read().strip()
        if os.path.exists(password_file):
            with open(password_file) as f:
                password = f.read().strip()

        return username, password

    return _get_service_auth

@pytest.fixture(scope="session")
def generate_random_download_path():
    def _generate_random_download_path(filename_extension):
        return os.path.join(DOWNLOADED_DIR,f'{str(uuid.uuid4())}.'+filename_extension)
    return _generate_random_download_path

@pytest.fixture(scope="session")
def initialise_clients(get_service_url, get_service_auth):
    # Retrieve endpoint and auth for triple store
    sparql_endpoint = get_service_url(KG_SERVICE, url_route=KG_ROUTE)
    sparql_user, sparql_pwd = get_service_auth(KG_SERVICE)

    # Retrieve endpoint and auth for file server
    fs_url = get_service_url(FS_SERVICE, url_route=FS_ROUTE)
    fs_user, fs_pwd = get_service_auth(FS_SERVICE)

    # Create SparqlClient for testing
    sparql_client = RxnOptGoalIterSparqlClient(
        sparql_endpoint, sparql_endpoint,
        kg_user=sparql_user, kg_password=sparql_pwd,
        fs_url=fs_url, fs_user=fs_user, fs_pwd=fs_pwd
    )

    # Clear triple store before any usage
    sparql_client.performUpdate("DELETE WHERE {?s ?p ?o.}")

    # Create DerivationClient for creating derivation instances
    derivation_client = sparql_client.jpsBaseLib_view.DerivationClient(
        sparql_client.kg_client,
        DERIVATION_INSTANCE_BASE_URL
    )

    yield sparql_client, derivation_client

    # Clear logger at the end of the test
    clear_loggers()


# ----------------------------------------------------------------------------------
# Module-scoped test fixtures
# ----------------------------------------------------------------------------------

@pytest.fixture(scope="module")
def create_rogi_agent():
    def _create_rogi_agent(
        register_agent:bool=False,
        random_agent_iri:bool=False,
    ):
        rogi_agent_config = config_derivation_agent(ROGI_AGENT_ENV)
        rogi_agent = RxnOptGoalIterAgent(
            register_agent=rogi_agent_config.REGISTER_AGENT if not register_agent else register_agent,
            agent_iri=rogi_agent_config.ONTOAGENT_SERVICE_IRI if not random_agent_iri else 'http://agent_' + str(uuid.uuid4()),
            time_interval=rogi_agent_config.DERIVATION_PERIODIC_TIMESCALE,
            derivation_instance_base_url=rogi_agent_config.DERIVATION_INSTANCE_BASE_URL,
            kg_url=rogi_agent_config.SPARQL_QUERY_ENDPOINT,
            kg_update_url=rogi_agent_config.SPARQL_UPDATE_ENDPOINT,
            kg_user=rogi_agent_config.KG_USERNAME,
            kg_password=rogi_agent_config.KG_PASSWORD,
            fs_url=rogi_agent_config.FILE_SERVER_ENDPOINT,
            fs_user=rogi_agent_config.FILE_SERVER_USERNAME,
            fs_password=rogi_agent_config.FILE_SERVER_PASSWORD,
            agent_endpoint=rogi_agent_config.ONTOAGENT_OPERATION_HTTP_URL,
            app=Flask(__name__)
        )
        return rogi_agent
    return _create_rogi_agent

# fixture used in the test_kg_sparql_client.py
@pytest.fixture(scope="module")
def initialise_triple_store():
    # NOTE: requires access to the docker.cmclinnovations.com registry from the machine the test is run on.
    # For more information regarding the registry, see: https://github.com/cambridge-cares/TheWorldAvatar/wiki/Docker%3A-Image-registry
    blazegraph = DockerContainer('docker.cmclinnovations.com/blazegraph_for_tests:1.0.0')
    # the port is set as 9999 to match with the value set in the docker image
    blazegraph.with_exposed_ports(9999)
    yield blazegraph

# fixture used in the test_kg_sparql_client.py
@pytest.fixture(scope="module")
def initialise_test_triples(initialise_triple_store):
    with initialise_triple_store as container:
        # Wait some arbitrary time until container is reachable
        time.sleep(8)

        # Retrieve SPARQL endpoint
        endpoint = get_endpoint(container)
        print(f"SPARQL endpoint: {endpoint}")

        # Create SparqlClient for testing
        sparql_client = RxnOptGoalIterSparqlClient(endpoint, endpoint)

        # Clear triple store before any usage
        sparql_client.performUpdate("DELETE WHERE {?s ?p ?o.}")

        # Create DerivationClient for creating derivation instances
        derivation_client = sparql_client.jpsBaseLib_view.DerivationClient(
            sparql_client.kg_client,
            DERIVATION_INSTANCE_BASE_URL
        )

        initialise_triples(sparql_client, derivation_client)

        yield sparql_client, derivation_client

        # Clear logger at the end of the test
        clear_loggers()


# ----------------------------------------------------------------------------------
# Helper functions
# ----------------------------------------------------------------------------------

def get_endpoint(docker_container):
    # Retrieve SPARQL endpoint for temporary testcontainer
    # endpoint acts as both Query and Update endpoint
    endpoint = 'http://' + docker_container.get_container_host_ip().replace('localnpipe', 'localhost') + ':' \
               + docker_container.get_exposed_port(9999)
    # 'kb' is default namespace in Blazegraph
    endpoint += '/blazegraph/namespace/kb/sparql'
    return endpoint


# method adopted from https://github.com/pytest-dev/pytest/issues/5502#issuecomment-647157873
def clear_loggers():
    """Remove handlers from all loggers"""
    import logging
    loggers = [logging.getLogger()] + list(logging.Logger.manager.loggerDict.values())
    for logger in loggers:
        handlers = getattr(logger, 'handlers', [])
        for handler in handlers:
            logger.removeHandler(handler)


# ----------------------------------------------------------------------------------
# Utility functions and classes
# ----------------------------------------------------------------------------------

class IRIs(Enum):
    GOAL_ITER_BASE_IRI = 'http://www.example.com/triplestore/ontogoal/rxnopt/'
    GOALSET_1 = GOAL_ITER_BASE_IRI + 'GoalSet_1'

    GOAL_1 = GOAL_ITER_BASE_IRI + 'Goal_1'
    DESIRED_QUANTITY_1 = GOAL_ITER_BASE_IRI + 'Desired_Quantity_1'
    DESIRED_QUANTITY_1_TYPE = ONTOREACTION_YIELD
    DESIRED_QUANTITY_MEASURE_1 = GOAL_ITER_BASE_IRI + 'Desired_Quantity_Measure_1'
    DESIRED_QUANTITY_MEASURE_1_UNIT = OM_PERCENT
    DESIRED_QUANTITY_MEASURE_1_NUMVAL = 99.9
    RESULT_QUANTITY_1 = GOAL_ITER_BASE_IRI + 'Result_Quantity_1'
    RESULT_QUANTITY_1_TYPE = ONTOREACTION_YIELD
    RESULT_QUANTITY_MEASURE_1 = GOAL_ITER_BASE_IRI + 'Result_Quantity_Measure_1'
    RESULT_QUANTITY_MEASURE_1_UNIT = OM_PERCENT
    RESULT_QUANTITY_MEASURE_1_NUMVAL = 50.0

    GOAL_2 = GOAL_ITER_BASE_IRI + 'Goal_2'
    DESIRED_QUANTITY_2 = GOAL_ITER_BASE_IRI + 'Desired_Quantity_2'
    DESIRED_QUANTITY_2_TYPE = ONTOREACTION_RUNMATERIALCOST
    DESIRED_QUANTITY_MEASURE_2 = GOAL_ITER_BASE_IRI + 'Desired_Quantity_Measure_2'
    DESIRED_QUANTITY_MEASURE_2_UNIT = ONTOUOM_POUNDSTERLINGPERLITRE
    DESIRED_QUANTITY_MEASURE_2_NUMVAL = 0.01
    RESTRICTION_1 = GOAL_ITER_BASE_IRI + 'Restriction_1'
    RESTRICTION_1_CYCLEALLOWANCE = 5
    RESTRICTION_1_DEADLINE = 4102444800.0

    PLAN_STEP_AGENT_BASE_IRI = 'http://www.theworldavatar.com/resource/plans/RxnOpt/'
    RXN_OPT_PLAN = PLAN_STEP_AGENT_BASE_IRI + 'rxnoptplan'
    STEP_DOE = PLAN_STEP_AGENT_BASE_IRI + 'doe'
    STEP_DOE_AGENT = 'http://www.theworldavatar.com/resource/agents/Service__DoE/Service'
    STEP_EXE = PLAN_STEP_AGENT_BASE_IRI + 'exe'
    STEP_EXE_AGENT = 'http://www.theworldavatar.com/resource/agents/Service__Execution/Service'
    STEP_POSTPRO = PLAN_STEP_AGENT_BASE_IRI + 'postpro'
    STEP_POSTPRO_AGENT = 'http://www.theworldavatar.com/resource/agents/Service__HPLC_PostPro/Service'

    EXP_1_BASE_IRI = 'https://www.example.com/triplestore/ontorxn/ReactionExperiment_1/'
    EXP_2_BASE_IRI = 'https://www.example.com/triplestore/ontorxn/ReactionExperiment_2/'
    EXP_3_BASE_IRI = 'https://www.example.com/triplestore/ontorxn/ReactionExperiment_3/'
    EXP_4_BASE_IRI = 'https://www.example.com/triplestore/ontorxn/ReactionExperiment_4/'
    EXP_5_BASE_IRI = 'https://www.example.com/triplestore/ontorxn/ReactionExperiment_5/'
    EXAMPLE_RXN_EXP_1_IRI = EXP_1_BASE_IRI + 'RxnExp_1'
    EXAMPLE_RXN_EXP_2_IRI = EXP_2_BASE_IRI + 'RxnExp_1'
    EXAMPLE_RXN_EXP_3_IRI = EXP_3_BASE_IRI + 'RxnExp_1'
    EXAMPLE_RXN_EXP_4_IRI = EXP_4_BASE_IRI + 'RxnExp_1'
    EXAMPLE_RXN_EXP_5_IRI = EXP_5_BASE_IRI + 'RxnExp_1'

    DERIVATION_INPUTS = [GOALSET_1, EXAMPLE_RXN_EXP_1_IRI, EXAMPLE_RXN_EXP_2_IRI,
        EXAMPLE_RXN_EXP_3_IRI, EXAMPLE_RXN_EXP_4_IRI, EXAMPLE_RXN_EXP_5_IRI]


def initialise_triples(sparql_client, derivation_client):
    # Delete all triples before initialising prepared triples
    sparql_client.performUpdate("""DELETE WHERE {?s ?p ?o.}""")

	# Upload all relevant example triples provided in the resources folder of 'chemistry_and_robots' package to triple store
    for f in ['sample_data/rxn_data.ttl', 'sample_data/new_exp_data.ttl', 'sample_data/dummy_lab.ttl']:
        data = pkgutil.get_data('chemistry_and_robots', 'resources/'+f).decode("utf-8")
        g = Graph().parse(data=data)
        sparql_client.uploadGraph(g)

    # Upload all relevant example triples provided in the test_triples folder
    pathlist = Path(TEST_TRIPLES_DIR).glob('*.ttl') # goal_iter.ttl and plan_step_agent.ttl
    for path in pathlist:
        sparql_client.uploadOntology(str(path))

    # Add timestamp to pure inputs
    for input in IRIs.DERIVATION_INPUTS.value:
        derivation_client.addTimeInstance(input)
        derivation_client.updateTimestamp(input)


def get_timestamp(derivation_iri: str, sparql_client):
    query_timestamp = """SELECT ?time WHERE { <%s> <%s>/<%s>/<%s> ?time .}""" % (
        derivation_iri, TIME_HASTIME, TIME_INTIMEPOSITION, TIME_NUMERICPOSITION)
    # the queried results must be converted to int, otherwise it will not be comparable
    return int(sparql_client.performQuery(query_timestamp)[0]['time'])

def get_postpro_derivation(goal_set_iri: str, sparql_client):
    query = f"""SELECT DISTINCT ?postpro_derivation
                WHERE {{
                    <{trimIRI(goal_set_iri)}> <{ONTOGOAL_HASGOAL}>/<{ONTOGOAL_HASPLAN}>/<{ONTOGOAL_HASSTEP}> ?step.
                    ?step a <{ONTOGOAL_POSTPROCESSING}>; <{ONTOGOAL_CANBEPERFORMEDBY}> ?agent.
                    ?postpro_derivation <{ONTODERIVATION_ISDERIVEDUSING}> ?agent.
                }}"""
    response = sparql_client.performQuery(query)
    if len(response) == 0:
        return None
    elif len(response) > 1:
        raise Exception(f'More than one postpro derivation found for unit test: {response}')
    else:
        return response[0]['postpro_derivation']

def create_dummy_triples_for_performance_indicator(goal_set_instance: GoalSet, postpro_derivation_iri: str, sparql_client):
    g = Graph()
    # create placeholder performance indicator instance as output for postpro derivation
    for goal in goal_set_instance.hasGoal:
        perf_ind = PerformanceIndicator(
            instance_iri=INSTANCE_IRI_TO_BE_INITIALISED,
            clz=goal.desires().clz,
            namespace_for_init=getNameSpace(goal_set_instance.instance_iri),
            rxn_exp_iri=PLACEHOLDER_NEW_EXPERIMENT_IRI,
            objPropWithExp=[ONTOREACTION_HASPERFORMANCEINDICATOR],
            hasValue=OM_Measure(
                instance_iri=INSTANCE_IRI_TO_BE_INITIALISED,
                namespace_for_init=getNameSpace(goal_set_instance.instance_iri),
                hasUnit=f"http://{str(uuid.uuid4())}",
                hasNumericalValue=randint(1, 100)
            ),
            yieldLimitingSpecies=None
        )
        g = perf_ind.create_instance_for_kg(g)
        g.add((URIRef(perf_ind.instance_iri), URIRef(ONTODERIVATION_BELONGSTO), URIRef(postpro_derivation_iri)))
    sparql_client.uploadGraph(g)
    return g

def get_result_from_rogi_derivation(rogi_derivation_iri: str, sparql_client) -> List[Result]:
    rogi_derivation_iri = trimIRI(rogi_derivation_iri)
    query = f"""SELECT DISTINCT ?result ?quantity
                WHERE {{
                    ?result <{ONTODERIVATION_BELONGSTO}> <{rogi_derivation_iri}>; a <{ONTOGOAL_RESULT}>; <{ONTOGOAL_REFERSTO}> ?quantity.
                }}"""
    response = sparql_client.performQuery(query)
    if len(response) == 0:
        print(f'No result found for RxnOptGoalIter derivation <{rogi_derivation_iri}> yet')
        return None
    else:
        return [Result(instance_iri=res['result'], refersTo=res['quantity']) for res in response]