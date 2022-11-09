################################################
# Authors: Markus Hofmeister (mh807@cam.ac.uk) #    
# Date: 12 Oct 2022                            #
################################################

from flask import Blueprint, request, jsonify

#import agentlogging

from forecasting.errorhandling.exceptions import InvalidInput
from forecasting.forecasting_agent.create_forecast import forecast


# Initialise logger
#logger = agentlogging.get_logger("prod")


inputtasks_bp = Blueprint(
    'inputtasks_bp', __name__
)


# Define route for API request to update transaction record for single property
# or list of provided properties
@inputtasks_bp.route("/api/forecastingAgent/forecast", methods=["POST"])
def api_forecast():
    # Get received 'query' JSON object which holds all HTTP parameters
    try:
        query = request.json["query"]
    except Exception as ex:
        #logger.('No JSON "query" object could be identified.')
        raise InvalidInput('No JSON "query" object could be identified.') from ex
    
    # Retrieve data IRI to be updated
    try:
        dataIRI = str(query['dataIRI'])
        # remove < and > from dataIRI
        if dataIRI.startswith('<'):
            dataIRI = dataIRI[1:]
        if dataIRI.endswith('>'):
            dataIRI = dataIRI[:-1]
    except Exception as ex:
        #logger.('Invalid "dataIRI" provided.')
        raise InvalidInput('Invalid "dataIRI" provided.') from ex
    
    # Retrieve horizon 
    try:
        horizon = int(query['horizon'])
    except Exception as ex:
        #logger.info('No horizon, using default.')
        raise InvalidInput('Invalid "horizon" provided.') from ex

    if horizon <= 0:
        #logger.('Invalid "horizon" provided. Must be higher than 0.')
        raise InvalidInput('Invalid "horizon" provided. Must be higher than 0.')
    
    # Retrieve forecast_start_date 
    try:
        forecast_start_date = query['forecast_start_date']
    except Exception as ex:
        #logger.info('No forecast_start_date, using most recent date.')
        forecast_start_date = None
    
    # Retrieve model_path_ckpt_link 
    try:
        model_path_ckpt_link = query['model_path_ckpt_link']
    except Exception as ex:
        #logger.info('No model_path_ckpt_link, using Prophet.')
        model_path_ckpt_link = ''
    
    
    # Retrieve model_path_pth_link 
    try:
        model_path_pth_link = query['model_path_pth_link']
    except Exception as ex:
        #logger.info('No model_path_pth_link, using Prophet.')
        model_path_pth_link = ''
    
    # retrieve data_length
    try:
        # data length is the length of the time series which is loaded as input to the model
        # prophet will use all of it, but TFT will use it just to scale the data and then uses 'input_chunk_length' to predict

        data_length = int(query['data_length'])
    except Exception as ex:
        #logger.info('No data_length, using default.')
        data_length = 365*24
    try:
        # Forecast dataIRI
        res = forecast(dataIRI, horizon, forecast_start_date, model_path_ckpt_link, model_path_pth_link, data_length)
        res['status'] = '200'
        return jsonify(res)
    except Exception as ex:
        #logger.("Unable to forecast.", ex)
        return jsonify({'status': '500', 'msg': 'Forecast failed. \n' + str(ex)})

