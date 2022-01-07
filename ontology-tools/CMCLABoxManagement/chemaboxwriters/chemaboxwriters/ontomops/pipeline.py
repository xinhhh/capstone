from chemaboxwriters.common.base import get_pipeline, Pipeline
import chemaboxwriters.common.handlers as handlers
import chemaboxwriters.common.stageenums as stge
from chemaboxwriters.ontomops.jsonwriter import om_jsonwriter
from chemaboxwriters.ontomops.csvwriter import om_csvwriter
from typing import Optional
import logging

logger = logging.getLogger(__name__)

def assemble_omops_pipeline(
        name: Optional[str] = None,
        outStage: Optional[str] = None
    )->Pipeline:

    if name is None: name = 'ontomops'

    logger.info(f"Assembling {name} pipeline.")

    pipeline = get_pipeline(
                    name = name,
                    outStage = outStage)

    pipeline.add_handler(handler = handlers.get_qc_log_to_qc_json_handler()) \
            .add_handler(handler = handlers.get_json_to_json_handler(
                                                inStageTag = stge.ONTO_MOPS_INP_TAG,
                                                outStageTag = stge.ONTO_MOPS_TAG,
                                                handlerFunc=om_jsonwriter)) \
            .add_handler(handler = handlers.get_json_to_csv_handler(
                                                inStageTag = stge.ONTO_MOPS_TAG,
                                                outStageTag = stge.ONTO_MOPS_TAG,
                                                handlerFunc=om_csvwriter)) \
            .add_handler(handler = handlers.get_csv_to_owl_handler(
                                                inStageTag = stge.ONTO_MOPS_TAG,
                                                outStageTag = stge.ONTO_MOPS_TAG))
    return pipeline