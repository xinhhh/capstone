from datetime import datetime
import json
import uuid

import tests.conftest as cf


# ----------------------------------------------------------------------------------
# Test cases for the function RxnOptGoalAgent::handle_rxn_opt_goal_request
# upon receiving a goal request, the rog agent should:
# 1. correctly create the rogi derivation
# TODO 2. start the goal iteration monitoring job
# ----------------------------------------------------------------------------------

def test_handle_rxn_opt_goal_request(
    initialise_test_triples,
    create_rog_agent
):
    endpoint = initialise_test_triples

    rog_agent = create_rog_agent(
        goal_iter_agent_iri=f'http://{str(uuid.uuid4())}',
        kg_url=endpoint,
        kg_update_url=endpoint,
    )

    # 1. correctly create the rogi derivation
    sample_goal_request = cf.sample_goal_request
    with rog_agent.app.test_client() as test_client:
        r = test_client.post(rog_agent.goal_agent_endpoint, data=sample_goal_request)
        j = json.loads(r.text)
        rogi_derivation = j[rog_agent.GOAL_SPECS_RESPONSE_KEY]
        assert rogi_derivation is not None
        assert rog_agent.sparql_client.check_if_triple_exist(
            rogi_derivation, cf.ONTODERIVATION_ISDERIVEDUSING, rog_agent.goal_iter_agent_iri
        )

        # Check that the experiment beliefs are correct
        rxn_exp_beliefs_res = rog_agent.sparql_client.performQuery(
            f"""SELECT DISTINCT ?rxn_exp WHERE {{
                    <{rogi_derivation}> <{cf.ONTODERIVATION_ISDERIVEDFROM}> ?rxn_exp .
                    VALUES ?rxn_exp_type {{ <{'> <'.join([cf.ONTOREACTION_REACTIONEXPERIMENT, cf.ONTOREACTION_REACTIONVARIATION])}> }}
                    ?rxn_exp a ?rxn_exp_type .
                }}"""
        )
        rxn_exp_beliefs = [x['rxn_exp'] for x in rxn_exp_beliefs_res]
        assert cf.dal.check_if_two_lists_equal(rxn_exp_beliefs, [
            cf.rogi_cf.IRIs.EXP_1_BASE_IRI.value,
            cf.rogi_cf.IRIs.EXP_2_BASE_IRI.value,
            cf.rogi_cf.IRIs.EXP_3_BASE_IRI.value,
            cf.rogi_cf.IRIs.EXP_4_BASE_IRI.value,
            cf.rogi_cf.IRIs.EXP_5_BASE_IRI.value,
        ])

        # Check that the GoalSet is valid
        goal_set_query_res = rog_agent.sparql_client.performQuery(
            f"""SELECT ?goal_set_iri WHERE {{<{rogi_derivation}> <{cf.ONTODERIVATION_ISDERIVEDFROM}> ?goal_set_iri. ?goal_set_iri a <{cf.ONTOGOAL_GOALSET}>.}}"""
        )
        assert len(goal_set_query_res) == 1
        goal_set_iri = goal_set_query_res[0]['goal_set_iri']

        goal_set_instance = rog_agent.sparql_client.get_goal_set_instance(goal_set_iri)

        # restriction
        assert goal_set_instance.hasRestriction.instance_iri is not None
        assert goal_set_instance.hasRestriction.cycleAllowance == sample_goal_request["cycleAllowance"]
        assert goal_set_instance.hasRestriction.deadline == datetime.timestamp(datetime.fromisoformat(sample_goal_request["deadline"]))

        # goal
        assert len(goal_set_instance.hasGoal) == 2
        if goal_set_instance.hasGoal[0].desires().clz == sample_goal_request["first_goal_clz"]:
            assert goal_set_instance.hasGoal[1].desires().clz == sample_goal_request["second_goal_clz"]
            goal_1 = goal_set_instance.hasGoal[0]
            goal_2 = goal_set_instance.hasGoal[1]
        else:
            assert goal_set_instance.hasGoal[0].desires().clz == sample_goal_request["second_goal_clz"]
            assert goal_set_instance.hasGoal[1].desires().clz == sample_goal_request["first_goal_clz"]
            goal_1 = goal_set_instance.hasGoal[1]
            goal_2 = goal_set_instance.hasGoal[0]

        # desired quantity
        assert goal_1.desiresGreaterThan.instance_iri is not None
        assert goal_1.desiresGreaterThan.hasValue.instance_iri is not None
        assert goal_1.desiresGreaterThan.hasValue.hasUnit == sample_goal_request["first_goal_unit"]
        assert goal_1.desiresGreaterThan.hasValue.hasNumericalValue == sample_goal_request["first_goal_num_val"]
        assert goal_1.desiresLessThan is None
        assert goal_2.desiresGreaterThan is None
        assert goal_2.desiresLessThan.instance_iri is not None
        assert goal_2.desiresLessThan.hasValue.instance_iri is not None
        assert goal_2.desiresLessThan.hasValue.hasUnit == sample_goal_request["second_goal_unit"]
        assert goal_2.desiresLessThan.hasValue.hasNumericalValue == sample_goal_request["second_goal_num_val"]

        # plan
        assert len(goal_1.hasPlan) == 1
        assert goal_1.hasPlan[0].instance_iri == cf.rogi_cf.IRIs.RXN_OPT_PLAN.value
        assert len(goal_2.hasPlan) == 1
        assert goal_2.hasPlan[0].instance_iri == cf.rogi_cf.IRIs.RXN_OPT_PLAN.value

        # step
        for goal in [goal_1, goal_2]:
            assert len(goal.hasPlan[0].hasStep) == 3

            # step 1 - doe
            doe_step = goal.hasPlan[0].get_step(cf.ONTOGOAL_DESIGNOFEXPERIMENT)
            assert doe_step.instance_iri == cf.rogi_cf.IRIs.STEP_DOE.value
            assert len(doe_step.canBePerformedBy) == 1
            assert doe_step.canBePerformedBy[0] == cf.rogi_cf.IRIs.STEP_DOE_AGENT.value
            assert len(doe_step.hasNextStep) == 1
            assert doe_step.hasNextStep[0].instance_iri == cf.rogi_cf.IRIs.STEP_EXE.value
            assert len(doe_step.hasNextStep[0].canBePerformedBy) == 1
            assert doe_step.hasNextStep[0].canBePerformedBy[0] == cf.rogi_cf.IRIs.STEP_EXE_AGENT.value
            assert len(doe_step.hasNextStep[0].hasNextStep) == 1
            assert doe_step.hasNextStep[0].hasNextStep[0].instance_iri == cf.rogi_cf.IRIs.STEP_POSTPRO.value
            assert len(doe_step.hasNextStep[0].hasNextStep[0].canBePerformedBy) == 1
            assert doe_step.hasNextStep[0].hasNextStep[0].canBePerformedBy[0] == cf.rogi_cf.IRIs.STEP_POSTPRO_AGENT.value
            assert doe_step.hasNextStep[0].hasNextStep[0].hasNextStep is None

            # step 2 - exe
            exe_step = goal.hasPlan[0].get_step(cf.ONTOGOAL_RXNEXPEXECUTION)
            assert exe_step.instance_iri == cf.rogi_cf.IRIs.STEP_EXE.value
            assert len(exe_step.canBePerformedBy) == 1
            assert exe_step.canBePerformedBy[0] == cf.rogi_cf.IRIs.STEP_EXE_AGENT.value
            assert len(exe_step.hasNextStep) == 1
            assert exe_step.hasNextStep[0].instance_iri == cf.rogi_cf.IRIs.STEP_POSTPRO.value
            assert len(exe_step.hasNextStep[0].canBePerformedBy) == 1
            assert exe_step.hasNextStep[0].canBePerformedBy[0] == cf.rogi_cf.IRIs.STEP_POSTPRO_AGENT.value
            assert exe_step.hasNextStep[0].hasNextStep is None

            # step 3 - postpro
            postpro_step = goal.hasPlan[0].get_step(cf.ONTOGOAL_POSTPROCESSING)
            assert postpro_step.instance_iri == cf.rogi_cf.IRIs.STEP_POSTPRO.value
            assert len(postpro_step.canBePerformedBy) == 1
            assert postpro_step.canBePerformedBy[0] == cf.rogi_cf.IRIs.STEP_POSTPRO_AGENT.value
            assert postpro_step.hasNextStep is None

        # TODO 2. start the goal iteration monitoring job
        # TODO test if the periodical goal monitoring job is started