from pathlib import Path
from rdflib import Graph
from rdflib import RDF
import pytest
import time
import copy

import agent.datamodel as dm

from . import conftest as cf


def test_example_triples():
    """
    This test checks that the example triples are correct in syntax.

    Raises:
        e: If the example triples are not valid RDF.
    """
    g = Graph()
    pathlist = Path(cf.TEST_TRIPLES_DIR).glob('*.ttl')
    for path in pathlist:
        try:
            g.parse(str(path))
        except Exception as e:
            raise e


def test_example_data_instantiation(initialise_clients):
    """
        This test checks that all example data gets correctly instantiated,
        including associated time series data in PostgreSQL.
    """
    # Get SPARQL client from fixture
    sparql_client, _, rdb_url = initialise_clients

    ### TRIPPLE STORE ###
    # Verify that KG is empty
    assert sparql_client.getAmountOfTriples() == 0

    # Upload example test triples (ABox & TBox)
    cf.initialise_triples(sparql_client)

    # Verify instantiation of expected number of triples
    assert sparql_client.getAmountOfTriples() == (cf.TBOX_TRIPLES + cf.ABOX_TRIPLES)

    ### POSTGRESQL ###
    # Verify that Postgres database is empty
    assert cf.get_number_of_rdb_tables(rdb_url) == 0

    # Initialise and Upload time series
    cf.initialise_timeseries(kgclient=sparql_client, rdb_url=rdb_url, 
                             rdb_user=cf.DB_USER, rdb_password=cf.DB_PASSWORD,
                             dataIRI=cf.PRICE_INDEX_INSTANCE_IRI,
                             dates=cf.DATES, values=cf.VALUES)

    # Verify that expected tables and triples are created (i.e. dbTable + 1 ts table)
    assert cf.get_number_of_rdb_tables(rdb_url) == 2
    assert sparql_client.getAmountOfTriples() == (cf.TBOX_TRIPLES + cf.ABOX_TRIPLES + cf.TS_TRIPLES)

    # Verify correct retrieval of time series data
    dates, values = cf.retrieve_timeseries(kgclient=sparql_client, rdb_url=rdb_url, 
                             rdb_user=cf.DB_USER, rdb_password=cf.DB_PASSWORD,
                             dataIRI=cf.PRICE_INDEX_INSTANCE_IRI)
    assert dates == cf.DATES
    # Account for rounding errors
    assert pytest.approx(values, rel=1e-5) == cf.VALUES

    # Verify that dropping all tables works as expected
    cf.initialise_database(rdb_url)
    assert cf.get_number_of_rdb_tables(rdb_url) == 0


# def test_get_all_metoffice_stations_exceptions(center, radius, expected_msg):

#     with pytest.raises(InvalidInput) as excinfo:
#     # Check correct exception type
#         get_all_metoffice_stations(circle_center=center, circle_radius=radius)
#         pytest.fail
#     # Check correct exception message
#     assert expected_msg in str(excinfo.value)


@pytest.mark.parametrize(
    "derivation_input_set, expect_exception, expected_estimate, local_agent_test",
    [
        (cf.DERIVATION_INPUTS_1, True, cf.MARKET_VALUE_1, True),   # local agent instance test
    ],
)
def test_monitor_derivations(
    initialise_clients, create_example_agent, derivation_input_set, expect_exception,
    expected_estimate, local_agent_test, mocker    
):
    """
        Test if derivation agent performs derivation update as expected, the `local_agent_test` 
        parameter controls if the agent performing the update is instantiating in memory (for quick
        debugging) or deployed in docker container (to mimic the production environment)
    """

    # Get required clients from fixtures
    sparql_client, derivation_client, rdb_url = initialise_clients

    # Initialise all triples in test_triples + initialise time series in RDB
    # It first DELETES ALL DATA in the specified SPARQL/RDB endpoints
    # It then SPARQL updates all triples stated in test_triples folder to SPARQL endpoint +
    cf.initialise_triples(sparql_client)
    cf.initialise_database(rdb_url)
    # Initialises PropertyPriceIndex time series and uploads test data to RDB
    cf.initialise_timeseries(kgclient=sparql_client, rdb_url=rdb_url, 
                             rdb_user=cf.DB_USER, rdb_password=cf.DB_PASSWORD,
                             dataIRI=cf.PRICE_INDEX_INSTANCE_IRI,
                             dates=cf.DATES, values=cf.VALUES)

    # Verify correct number of triples (not marked up with timestamp yet)
    assert sparql_client.getAmountOfTriples() == (cf.TBOX_TRIPLES + cf.ABOX_TRIPLES + cf.TS_TRIPLES)

    # Create agent instance and register agent in KG
    # EXPLANATION: 
    # 1) Test Docker stack spins up Blazegraph, Postgres and Agent container, where agent
    #    endpoints (loaded from mocked `stack_configs_mock.py`) contain `docker.host.internal`
    #    to ensure intra-container communication
    # 2) However, successful agent registration within the KG cannot be guaranteed as both are within 
    #    the same Stack and sequence of startup (i.e. agent registration only after KG is available)
    #    cannot be guaranteed; however, this is required to properly pick up derivations
    # 3) Hence, the Dockerised agent is started without initial registration within the Stack and
    #    registration is done within the test to guarantee that Blazegraph will be ready
    # 4) The "belated" registration of the Dockerised agent can be achieved by registering "another local"
    #    agent instance with the same ONTOAGENT_SERVICE_IRI, while registering a "new" agent with a 
    #    different ONTOAGENT_SERVICE_IRI will actually register a local agent instance in the KG
    # TODO: (Potentially) to be reworked for testing when building docker image
    agent = create_example_agent(register_agent=True, random_agent_iri=local_agent_test)

    # Assert that there's currently no instance having rdf:type of the output signature in the KG
    assert not sparql_client.check_if_triple_exist(None, RDF.type.toPython(), dm.OM_AMOUNT_MONEY)

    # Create derivation instance for new information
    # As of pyderivationagent>=1.3.0 this also initialises all timestamps for pure inputs
    derivation_iri = derivation_client.createAsyncDerivationForNewInfo(agent.agentIRI, derivation_input_set)
    print(f"Initialised successfully, created asynchronous derivation instance: {derivation_iri}")
    
    # Expected number of triples after derivation registration
    triples = (cf.TBOX_TRIPLES + cf.ABOX_TRIPLES + cf.TS_TRIPLES)
    triples += cf.TIME_TRIPLES_PER_PURE_INPUT * len(derivation_input_set) # timestamps for pure inputs
    triples += cf.TIME_TRIPLES_PER_PURE_INPUT                             # timestamps for derivation instance
    triples += len(derivation_input_set) + 1    # number of inputs + derivation instance type
    triples += cf.AGENT_SERVICE_TRIPLES
    triples += cf.DERIV_STATUS_TRIPLES
    triples += cf.DERIV_INPUT_TRIPLES
    triples += cf.DERIV_OUTPUT_TRIPLES

    # Verify correct number of triples (incl. timestamp & agent triples)
    assert sparql_client.getAmountOfTriples() == triples    

    if local_agent_test:
        # Patch RDB URL for TimeSeriesClient to ensure that Postgres is not accessed from inside the Container
        # If patching an object, you have to always patch the object as used in the module, e.g. 
        # if you have imported it in the form: from x import y in your module module, you have to patch 
        # module.y instead of x.y. Documentation: https://docs.python.org/3/library/unittest.mock.html#id6
        mocker.patch('agent.kg_operations.tsclient.DB_URL', rdb_url)
        # Start the scheduler to monitor derivations if it's local agent test
        agent._start_monitoring_derivations()

    # Query timestamp of the derivation for every 20 seconds until it's updated
    currentTimestamp_derivation = 0
    while currentTimestamp_derivation == 0:
        time.sleep(10)
        currentTimestamp_derivation = cf.get_timestamp(derivation_iri, sparql_client)

    # Assert that there's now an instance having rdf:type of the output signature in the KG
    assert sparql_client.check_if_triple_exist(None, RDF.type.toPython(), dm.OM_AMOUNT_MONEY)

    # Query the output of the derivation instance
    derivation_outputs = cf.get_derivation_outputs(derivation_iri, sparql_client)
    print(f"Generated derivation outputs that belongsTo the derivation instance: {', '.join(derivation_outputs)}")
    
    # Verify that there are 2 derivation outputs (i.e. AveragePrice and Measure IRIs)
    assert len(derivation_outputs) == 2
    assert dm.OM_AMOUNT_MONEY in derivation_outputs
    assert len(derivation_outputs[dm.OM_AMOUNT_MONEY]) == 1
    assert dm.OM_MEASURE in derivation_outputs
    assert len(derivation_outputs[dm.OM_MEASURE]) == 1
    
    # Verify the values of the derivation output
    market_value_iri = derivation_outputs[dm.OM_AMOUNT_MONEY][0]
    inputs, market_value = cf.get_marketvalue_details(sparql_client, market_value_iri)
    # Verify market value
    assert len(market_value) == 1
    assert pytest.approx(market_value[0], rel=1e-5) == expected_estimate

    # Verify inputs (i.e. derived from)
    # Create deeepcopy to avoid modifying original cf.DERIVATION_INPUTS_... between tests
    derivation_input_set_copy = copy.deepcopy(derivation_input_set)
    for i in inputs:
        for j in inputs[i]:
            assert j in derivation_input_set_copy
            derivation_input_set_copy.remove(j)
    assert len(derivation_input_set_copy) == 0

    print("All check passed.")

    # Shutdown the scheduler to clean up if it's local agent test
    if local_agent_test:
        agent.scheduler.shutdown()
