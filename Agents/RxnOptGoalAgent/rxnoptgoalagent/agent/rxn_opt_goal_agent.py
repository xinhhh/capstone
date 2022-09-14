from abc import ABC
from flask_apscheduler import APScheduler
from flask import Flask
from flask import request
from flask import jsonify
from flask import render_template
from urllib.parse import unquote
from urllib.parse import urlparse
from rdflib import Graph, URIRef, Literal
from datetime import datetime
import json
import time
import os

import agentlogging

from rxnoptgoalagent.kg_operations import RxnOptGoalIterSparqlClient
from rxnoptgoalagent.data_model import *

# TODO find a better place to put this
AVAILABLE_RXN_OPT_PLAN_LIST = ['http://www.theworldavatar.com/resource/plans/RxnOpt/rxnoptplan']


class FlaskConfig(object):
    """
        This class provides the configuration for flask app object. Each config should be provided as constant. For more information, visit https://flask.palletsprojects.com/en/2.0.x/config/.
    """
    SCHEDULER_API_ENABLED = True


class RxnOptGoalAgent(ABC):
    GOAL_SPECS_RESPONSE_KEY = "Created a RxnOptGoalIter (ROGI) Derivation"

    def __init__(
        self,
        goal_agent_iri: str,
        goal_agent_endpoint: str,
        goal_monitor_time_interval: int,
        goal_iter_agent_iri: str,
        derivation_instance_base_url: str,
        kg_url: str,
        kg_update_url: str = None,
        kg_user: str = None,
        kg_password: str = None,
        fs_url: str = None,
        fs_user: str = None,
        fs_password: str = None,
        app: Flask = Flask(__name__, template_folder="/app/templates"),
        flask_config: FlaskConfig = FlaskConfig(),
        # register_agent: bool = True,
        logger_name: str = "dev"
    ):
        """
            This method initialises the instance of RxnOptGoalAgent.

            Arguments:
                app - flask app object, an example: app = Flask(__name__)
                goal_agent_iri - OntoAgent:Service IRI of the goal agent, an example: "http://www.example.com/triplestore/agents/Service__XXXAgent#Service"
                goal_agent_endpoint - data property OntoAgent:hasHttpUrl of OntoAgent:Operation of the derivation agent, an example: "http://localhost:5000/endpoint"
                goal_monitor_time_interval - time interval between two runs of goal monitoring job (in SECONDS)
                derivation_instance_base_url - namespace to be used when creating derivation instance, an example: "http://www.example.com/triplestore/repository/"
                kg_url - SPARQL query endpoint, an example: "http://localhost:8080/blazegraph/namespace/triplestore/sparql"
                kg_update_url - SPARQL update endpoint, will be set to the same value as kg_url if not provided, an example: "http://localhost:8080/blazegraph/namespace/triplestore/sparql"
                kg_user - username used to access the SPARQL query/update endpoint specified by kg_url/kg_update_url
                kg_password - password that set for the kg_user used to access the SPARQL query/update endpoint specified by kg_url/kg_update_url
                fs_url - file server endpoint, an example: "http://localhost:8080/FileServer/"
                fs_user - username used to access the file server endpoint specified by fs_url
                fs_password - password that set for the fs_user used to access the file server endpoint specified by fs_url
                flask_config - configuration object for flask app, should be an instance of the class FlaskConfig provided as part of this package
                #TODO register_agent - boolean value, whether to register the agent to the knowledge graph
                logger_name - logger names for getting correct loggers from agentlogging package, valid logger names: "dev" and "prod", for more information, visit https://github.com/cambridge-cares/TheWorldAvatar/blob/develop/Agents/utils/python-utils/agentlogging/logging.py
        """

        # initialise flask app with its configuration
        self.app = app
        self.app.config.from_object(flask_config)

        # initialise flask scheduler and assign time interval for monitorDerivations job
        self.scheduler = APScheduler(app=self.app)
        self.goal_monitor_time_interval = goal_monitor_time_interval

        # assign IRI and HTTP URL of the agent
        self.goal_agent_iri = goal_agent_iri
        self.goal_agent_endpoint = goal_agent_endpoint

        # assign IRI of goal iteration agent
        self.goal_iter_agent_iri = goal_iter_agent_iri

        # assign KG related information
        self.kg_url = kg_url
        self.kg_update_url = kg_update_url if kg_update_url is not None else kg_url
        self.kg_user = kg_user
        self.kg_password = kg_password

        # assign file server related information
        self.fs_url = fs_url
        self.fs_user = fs_user
        self.fs_password = fs_password

        # initialise the RxnOptGoalIterSparqlClient
        self.sparql_client = RxnOptGoalIterSparqlClient(
            query_endpoint=self.kg_url, update_endpoint=self.kg_update_url,
            kg_user=self.kg_user, kg_password=self.kg_password,
            fs_url=self.fs_url, fs_user=self.fs_user, fs_pwd=self.fs_password
        )

        # initialise the derivation_client with SPARQL Query and Update endpoint
        self.derivation_instance_base_url = derivation_instance_base_url
        if kg_user is None:
            self.store_client = self.sparql_client.jpsBaseLib_view.RemoteStoreClient(
                self.kg_url, self.kg_update_url)
        else:
            self.store_client = self.sparql_client.jpsBaseLib_view.RemoteStoreClient(
                self.kg_url, self.kg_update_url, self.kg_user, self.kg_password)
        self.derivation_client = self.sparql_client.jpsBaseLib_view.DerivationClient(
            self.store_client, derivation_instance_base_url)

        # initialise the logger
        self.logger = agentlogging.get_logger(logger_name)

        # add the default routes for the flask app
        self.app.add_url_rule('/', 'root', self.default, methods=['GET'])

        # add the route for the rxn opt goal specification
        self.app.add_url_rule('/goal', 'rxn_opt_goal', self.goal_page, methods=['GET'])

        # add the url pattern that handles the goal request
        url_pattern = urlparse(self.goal_agent_endpoint).path
        url_pattern_name = url_pattern.strip('/').replace('/', '_') + '_rxnoptgoal'
        self.app.add_url_rule(url_pattern, url_pattern_name, self.handle_rxn_opt_goal_request, methods=['POST'])
        self.logger.info(f"The endpoint to handle goal request is added as: {url_pattern}")

        # TODO think about if we need the registeration of the goal agent
        # # register the agent to the KG if required
        # self.register_agent = register_agent
        # try:
        #     self.register_agent_in_kg()
        # except Exception as e:
        #     self.logger.error(
        #         "Failed to register the agent <{}> to the KG <{}>. Error: {}".format(self.agentIRI, self.kg_url, e),
        #         stack_info=True, exc_info=True)
        #     raise e

        self.logger.info(f"RxnOptGoalAgent initialised with IRI: {self.goal_agent_iri}")


    def default(self):
        """Instruction for the RxnOptGoalAgent usage."""
        msg = "Welcome to the RxnOptGoalAgent!<BR>"
        msg += "This is a goal agent that capable of persure a reaction optimisation goal.<BR>"
        msg += "For more information, please visit https://github.com/cambridge-cares/TheWorldAvatar/tree/160-dev-rxn-opt-goal-agent/Agents/RxnOptGoalAgent#readme<BR>"    
        return msg


    def goal_page(self):
        return render_template(
            'rxn_opt_goal.html',
            # TODO [nice-to-have] specify the limits of the goal, e.g. yield within 0-100%
            # TODO [nice-to-have] put the unit as symbol in the dropdown list, e.g. %, g/mol, kg, etc.
            goal_spec_from_flask={
                perf_iri: {
                    'iri': perf_iri,
                    'display': getShortName(perf_iri),
                    'units': [{
                        'iri': u, 'display': getShortName(u)
                    } for u in AVAILABLE_PERFORMANCE_INDICATOR_UNIT_DICT[perf_iri]]
                } for perf_iri in AVAILABLE_PERFORMANCE_INDICATOR_LIST
            },
            desires_type=[{'iri': des, 'display': getShortName(des)} for des in [ONTOGOAL_DESIRESGREATERTHAN, ONTOGOAL_DESIRESLESSTHAN]],
            # TODO [next iteration] where do we encode a list of available rxnoptplan?
            # TODO [next iteration] how to specify resource limitations (digital twin of hardware)?
            rxn_opt_goal_plan=[{'iri': plan, 'display': plan} for plan in AVAILABLE_RXN_OPT_PLAN_LIST],
        )


    def handle_rxn_opt_goal_request(self):
        """
        This function is called when a goal request is received.
        """
        if request.method == 'POST':
            parameters = request.form
        else:
            self.logger.error("The method is not supported.")
            return f"The method [{request.method}] is not supported."

        # Varify the parameters
        self.logger.info(f"Received a goal request with parameters: {parameters}")
        all_parameters = ["chem_rxn", "cycleAllowance", "deadline",
                          "first_goal_clz", "first_goal_desires", "first_goal_num_val", "first_goal_unit",
                          "rxn_opt_goal_plan",
                          "second_goal_clz", "second_goal_desires", "second_goal_num_val", "second_goal_unit"]
        if not all([p in parameters and bool(parameters[p]) for p in all_parameters]):
            return f"""The request parameters are incomplete, required parameters: {all_parameters}.
                    Received parameters: {parameters}.
                    Please provided the missing fields: {[p for p in all_parameters if p not in parameters or not bool(parameters[p])]}."""

        # Parse request form parameters to construct goal related instances
        chem_rxn_iri = parameters['chem_rxn']

        rxn_opt_goal_plan = self.sparql_client.get_goal_plan(parameters['rxn_opt_goal_plan'])

        first_goal_desires = parameters['first_goal_desires']
        first_goal_desires_quantity = OM_Quantity(
            instance_iri=INSTANCE_IRI_TO_BE_INITIALISED,
            namespace_for_init=self.derivation_instance_base_url,
            clz=parameters['first_goal_clz'],
            hasValue=OM_Measure(
                instance_iri=INSTANCE_IRI_TO_BE_INITIALISED,
                namespace_for_init=self.derivation_instance_base_url,
                hasUnit=parameters['first_goal_unit'],
                hasNumericalValue=parameters['first_goal_num_val']
            )
        )
        first_goal = Goal(
            instance_iri=INSTANCE_IRI_TO_BE_INITIALISED,
            namespace_for_init=self.derivation_instance_base_url,
            hasPlan=rxn_opt_goal_plan,
            desiresGreaterThan=first_goal_desires_quantity if first_goal_desires == ONTOGOAL_DESIRESGREATERTHAN else None,
            desiresLessThan=first_goal_desires_quantity if first_goal_desires == ONTOGOAL_DESIRESLESSTHAN else None,
        )

        second_goal_desires = parameters['second_goal_desires']
        second_goal_desires_quantity = OM_Quantity(
            instance_iri=INSTANCE_IRI_TO_BE_INITIALISED,
            namespace_for_init=self.derivation_instance_base_url,
            clz=parameters['second_goal_clz'],
            hasValue=OM_Measure(
                instance_iri=INSTANCE_IRI_TO_BE_INITIALISED,
                namespace_for_init=self.derivation_instance_base_url,
                hasUnit=parameters['second_goal_unit'],
                hasNumericalValue=parameters['second_goal_num_val']
            )
        )
        second_goal = Goal(
            instance_iri=INSTANCE_IRI_TO_BE_INITIALISED,
            namespace_for_init=self.derivation_instance_base_url,
            hasPlan=rxn_opt_goal_plan,
            desiresGreaterThan=second_goal_desires_quantity if second_goal_desires == ONTOGOAL_DESIRESGREATERTHAN else None,
            desiresLessThan=second_goal_desires_quantity if second_goal_desires == ONTOGOAL_DESIRESLESSTHAN else None,
        )

        restriction = Restriction(
            instance_iri=INSTANCE_IRI_TO_BE_INITIALISED,
            namespace_for_init=self.derivation_instance_base_url,
            cycleAllowance=parameters['cycleAllowance'],
            deadline=datetime.timestamp(datetime.fromisoformat(parameters['deadline']))
        )

        # TODO doe boundaries? this should be design together with the ROGI agent

        # Now we need to construct a GoalSet object with above information
        goal_list = [first_goal, second_goal]
        goal_set_instance = GoalSet(
            instance_iri=INSTANCE_IRI_TO_BE_INITIALISED,
            namespace_for_init=self.derivation_instance_base_url,
            hasGoal=goal_list,
            hasRestriction=restriction
        )

        # Upload the goal set instance to KG, also add timestamp to the goal set instance
        g = Graph()
        g = goal_set_instance.create_instance_for_kg(g)
        self.sparql_client.uploadGraph(g)
        self.derivation_client.addTimeInstance(goal_set_instance.instance_iri)
        self.derivation_client.updateTimestamp(goal_set_instance.instance_iri)

        # Query the KG to get all previous ReactionExperiment with specific PerformanceIndicator for the requested ChemicalReaction
        lst_rxn_exp = self.sparql_client.get_all_rxn_exp_with_target_perfind_given_chem_rxn(
            chem_rxn_iri,
            [goal.desires().clz for goal in goal_list]
        )

        # Construct the list for derivation inputs
        derivation_inputs = [goal_set_instance.instance_iri] + lst_rxn_exp

        # Create a RxnOptGoalIter (ROGI) derivation for new info
        # NOTE: the ROGI derivations needs to be created with the IRI of the ROGI agent
        # which is DIFFERENT from the IRI of ROG agent (self.goal_agent_iri)
        # TODO in this iteration, we provide the ROGI agent IRI as a parameter (self.goal_iter_agent_iri)
        # TODO but in the future, this information should obtained by ROG agent from the KG
        rogi_derivation = self.derivation_client.createAsyncDerivationForNewInfo(self.goal_iter_agent_iri, derivation_inputs)

        # # TODO
        # # Add a periodical job to monitor the goal iterations for the created ROGI derivation
        # self.scheduler.add_job(
        #     id=f'monitor_goal_{getShortName(rogi_derivation)}',
        #     func=self.monitor_goal_iterations,
        #     trigger='interval', seconds=self.goal_monitor_time_interval
        # )
        # self.logger.info("Monitor goal iteration is scheduled with a time interval of %d seconds." % (self.goal_monitor_time_interval))
        return jsonify({self.GOAL_SPECS_RESPONSE_KEY: rogi_derivation})

    def monitor_goal_iterations(self):
        """
        This function is called by the scheduler to monitor the goal iterations.
        """
        self.logger.info("Monitoring the goal iterations...")
        # TODO implement