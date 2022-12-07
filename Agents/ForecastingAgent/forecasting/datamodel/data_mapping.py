################################################
# Authors: Magnus Mueller (mm2692@cam.ac.uk)   #
# Date: 30 Nov 2022                            #
################################################
# purpose of this file is provide model configurations for the agent which can be specified via 'use_model_configuration' in the http request 
# The configurations are used to load the model and the respective data
# You can add your own model configurations to 'MODEL_MAPPING' here by simple add a new dictionary like for 'TFT_HEAT_SUPPLY' case
""" KEYS in the dict:
Required:
'ts_data_type': Java data type for time series values , e.g. jpsBaseLibView.java.lang.Double.TYPE
'frequency': The frequency of the time series data, as a datetime timedelta object which represents a duration, the difference between two dates
'data_length': The maximum length of the time series data, which is loaded before the forecast_start_data.
    The loaded data is used to:
        1. Train the model if `train_again` is True or
        2. Scale the data if `scale` is True
        3. Create the forecast as Input to the model. 
        Neural Methods have a fixed input length (same as during training), therefore they use the last values of the time series data with respect to their input length to create the forecast.
'fc_model': The forecasting model which is used to create the forecast.
    It is a dict with the following keys:
    Required:
    'name': The name of the forecasting model
    'scale': If True, the data is scaled before the forecast is created
    'train_again': If True, the model is trained again before the forecast is created
    
    Optional:
    If 'model_path_ckpt_link' and 'model_path_pth_link' are give, the model is loaded from the given paths.
    Else Prophet is used. 
    'model_path_ckpt_link': The link to the darts checkpoint file of the model
    'model_path_pth_link': The link to the darts pth file of the model
    
Optional:
'load_covariates_func': The function which is used to load the covariates. If not provided, no covariates are loaded.
    Be aware, that the returned covariates must be available for the whole 'horizon'. 
    If the covariates are not long enough, Prophet is used, which does not require covariates.
    The function must return parameters:
    'covariates_iris': A list if iris, which are used.
    'covariates': A darts series object, which can be passed into model.predict()
    
    The function will receive the following parameters:
    'kgClient': The kgClient
    'tsClient': The tsClient
    'lowerbound': The lower bound of the time series data (can be None)
    'upperbound': The upper bound of the time series data (can be None)

    
"""

from forecasting.datamodel.iris import *
from forecasting.kgutils.javagateway import jpsBaseLibGW
from forecasting.utils.tools import get_covs_heat_supply
import datetime as dt


#NOTE: @mh807: Streamline py4jps gateway usages to avoid clashes
# PostgreSQL supported data types: https://www.jooq.org/javadoc/dev/org.jooq/org/jooq/impl/SQLDataType.html
jpsBaseLibView = jpsBaseLibGW.createModuleView()
Instant = jpsBaseLibView.java.time.Instant
TIMECLASS = Instant.now().getClass()

# Create data class for all time series data (i.e. all data as double)
jpsBaseLibView = jpsBaseLibGW.createModuleView()
DATACLASS = jpsBaseLibView.java.lang.Double.TYPE
DOUBLE = jpsBaseLibView.java.lang.Double.TYPE
INTEGER = jpsBaseLibView.java.lang.Integer.TYPE
BOOLEAN = jpsBaseLibView.java.lang.Boolean.TYPE

TIME_FORMAT_TS = "YYYY-MM-DDThh:mm:ssZ"
TIME_FORMAT = "%Y-%m-%dT%H:%M:%SZ"

MODEL_MAPPING = {}

# Default mapping which uses Prophet and loads just iri without covariates
MODEL_MAPPING['DEFAULT'] = {
    'fc_model': {
        'train_again': True,
        'name': 'prophet',
        'scale_data': False,
    },
    'frequency': dt.timedelta(hours=1),
    'data_length': 1000,  # 365 * 24 * 2, # 300
    'ts_data_type': DOUBLE,
}

MODEL_MAPPING['TFT_HEAT_SUPPLY'] = {
    'load_covariates_func': get_covs_heat_supply,
    'fc_model': {
        "model_path_ckpt_link":  "https://www.dropbox.com/s/fxt3iztbimvm47s/best.ckpt?dl=1",
        "model_path_pth_link":  "https://www.dropbox.com/s/ntg8lgvh01x09wr/_model.pth.tar?dl=1",
        'train_again': False,
        'name': 'tft',
        'scale_data': True,
    },
    'frequency': MODEL_MAPPING['DEFAULT']['frequency'],
    'data_length': MODEL_MAPPING['DEFAULT']['data_length'],
    'ts_data_type': MODEL_MAPPING['DEFAULT']['ts_data_type'],
}
