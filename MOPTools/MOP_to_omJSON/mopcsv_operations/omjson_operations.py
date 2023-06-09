from MOP_to_omJSON.mopcsv_operations.pathwriter import io_dirs
from MOP_to_omJSON.mopcsv_operations.omjson_converter import cbuCSVtoJSON
from MOP_to_omJSON.mopcsv_operations.omjson_converter import mopCSVtoJSON

def mopCSVOperations(cbuIRICSVFilePath, mopCSVFilePath):
    args = io_dirs(cbuIRICSVFilePath, mopCSVFilePath)
    cbuJSONFilePath = args[0]
    print(cbuJSONFilePath)
    mopJSONFilePath = args[1]
    print(mopJSONFilePath)
    cbuCSVtoJSON(cbuIRICSVFilePath, cbuJSONFilePath)
    mopCSVtoJSON(mopCSVFilePath, cbuJSONFilePath, mopJSONFilePath)
    return mopJSONFilePath