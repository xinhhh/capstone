################################################
# Authors: Jieyang Xu (jx309@cam.ac.uk) #
# Date: 30/11 2022                            #
################################################

# The purpose of this module is to perform data calculation by calling various calculation agent
# 
# NOTE: pd.DataFrame is used frequently in this module, where for all the dataframe
# without special notice, the column[0] should all be LSOA code used as identifier

import agentlogging
from agent.errorhandling.exceptions import *
from agent.dataretrieval.dataretrival import *

import numpy as np
import requests


# Initialise logger
logger = agentlogging.get_logger("prod")

# ----------------------Call Calculation Agent---------------------------------- #
def call_cop_agent(url: str, temp: np.ndarray, unit:str, t_h: float = None, hp_efficiency: float = None):
    '''
    This module will call the calculation agent(cop) to perform calculation with the parameters and url specified
    
    Arguments:
    url: the endpoint to perform calculation
    temperature: should be an array (np.ndarray) I suppose you are not interested in calculating single value...
    unit: unit with respect to temperature, must be wrapped as a url as per OntoMeasurement. Units below are available:
          DegreeCelsius ℃:    http://www.ontology-of-units-of-measure.org/resource/om-2/degreeCelsius
          DegreeFahrenheit ℉:    http://www.ontology-of-units-of-measure.org/resource/om-2/degreeFahrenheit
          Kelvin K:    http://www.ontology-of-units-of-measure.org/resource/om-2/kelvin
          t_h: hot side temperature (see equation above), if not provided, 318.15 will be used as default value.
          hp_efficiency: heat pump efficiency (see equation above), if not provided, 0.35 will be used as default value.
    '''
    
    # Wrapping the query
    query = {
        'query':{'temperature':temp.tolist(),
                'unit':unit
                }
        }
    
    if t_h:
        query['query']['t_h'] = t_h
    else:
        logger.info('Using default hot side temperature (318.15K) when calculating COP')

    if hp_efficiency:
        query['query']['hp_efficiency'] = hp_efficiency
    else:
        logger.info('Using default heat pump efficiency (0.35) when calculating COP')

    logger.info('Sending data to calculationagent_cop...')
    
    try:
        headers = {'Content-type': 'application/json'}
        response = requests.get(url, headers=headers, json=query)

    except Exception as ex:
        logger.error(f'Fail to connect to calculation agent (cop) -- Response code {response.status_code}')
        raise ConnectionError(f'Fail to connect to calculation agent (cop) -- Response code {response.status_code}') from ex

    try:
        data = response.json()

    except Exception as ex:
        logger.error(f'No valid JSON context from response -- Response code {response.status_code}')
        raise InvalidInput(f'No valid JSON context from response -- Response code {response.status_code}') from ex

    cop = np.array(data['COP'])
    logger.info('COP successfully calculated')

    return cop

def call_fuel_cost_agent(url:str, df_elec_in:pd.DataFrame, df_gas_in:pd.DataFrame, year: str = YEAR, annual: bool = None):
    '''
    To calculate the fuel cost per LSOA, normally on per household basis
    Returns three dataframe which have first column as LSOA code, and the following 12
    columns for each month of df_cost_total, df_cost_elec, df_cost_gas
    Arguments:
    df_elec: two-column data frame which MUST have the electricity data placed at the second column
    (i.e. at position [1])
    df_gas: two-column data frame which MUST have the gas data placed at the second column
    (i.e. at position [1])
    year: choose the year for index: prices of gas & elec, monthly distribution references of gas & elec
    annual: if True, the second column will include the annual value (with monthly data)
            if False, only monthly value will be returned
    '''
    # Make a copy so it won't ruin the original df
    df_elec = copy.deepcopy(df_elec_in)
    df_gas = copy.deepcopy(df_gas_in)

    # Wrapping the query
    query = {
        'query':{'df_electricity':df_elec.to_dict(orient='list'),
                'df_gas':df_gas.to_dict(orient='list'),
                'year':year
                }
        }
    if annual:
        query['query']['annual'] = str(annual)

        logger.info('Sending data to calculationagent_cop...')
    
    try:
        headers = {'Content-type': 'application/json'}
        response = requests.get(url, headers=headers, json=query)

    except Exception as ex:
        logger.error(f'Fail to connect to calculation agent (cop) -- Response code {response.status_code}')
        raise ConnectionError(f'Fail to connect to calculation agent (cop) -- Response code {response.status_code}') from ex

    try:
        data = response.json()

    except Exception as ex:
        logger.error(f'No valid JSON context from response -- Response code {response.status_code}')
        raise InvalidInput(f'No valid JSON context from response -- Response code {response.status_code}') from ex
    
    df_cost_total = pd.DataFrame.from_dict(data['df_cost_total'])
    df_cost_elec = pd.DataFrame.from_dict(data['df_cost_elec'])
    df_cost_gas = pd.DataFrame.from_dict(data['df_cost_gas'])
    
    return df_cost_total, df_cost_elec, df_cost_gas
