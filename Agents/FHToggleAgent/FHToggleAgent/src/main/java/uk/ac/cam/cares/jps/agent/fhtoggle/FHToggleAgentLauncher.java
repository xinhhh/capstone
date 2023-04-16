package uk.ac.cam.cares.jps.agent.fhtoggle;

import org.json.JSONObject;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeries;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;
import uk.ac.cam.cares.jps.base.agent.JPSAgent;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.query.RemoteRDBStoreClient;
import uk.ac.cam.cares.jps.base.query.RemoteStoreClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.*;
import java.util.*;
import javax.servlet.annotation.WebServlet;
import javax.ws.rs.BadRequestException;
import javax.xml.crypto.Data;

import org.apache.logging.log4j.LogManager;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;


@WebServlet(urlPatterns = {"/retrieve"})
public class FHToggleAgentLauncher extends JPSAgent{
	/**
     * Logger for reporting info/errors.
     */
    private static final Logger LOGGER = LogManager.getLogger(FHToggleAgentLauncher.class);
    
    //timeSeries Object
    TimeSeries<OffsetDateTime> timeseries;
    
    /**
     * Parameters to set rdf client and rdb client
     */
	String dbUrl;
	String dbUsername;
	String dbPassword;
	String sparqlQueryEndpoint;
	String sparqlUpdateEndpoint;
    
    //latest timeseries value
    double latestTimeSeriesValue;
    
    //keys in the request's JSON Object
    public static final String KEY_TIMESERIES_CLIENTPROPERTIES = "timeSeriesClientProperties";
    public static final String KEY_DATAIRIS = "dataIRIs";

    
    //set kbClient
    RemoteStoreClient kbClient = new RemoteStoreClient();

	//tsClient
	TimeSeriesClient<OffsetDateTime> tsClient;
    
    //JSONObjects
    JSONObject message;
    
    /**
     * Error messages
     */
    private static final String TSCLIENT_ERROR_MSG = "Could not construct the time series client needed by the input agent!";
    private static final String LOADCONFIGS_ERROR_MSG = "Could not load configs from the properties file!";
    private static final String GETLATESTSTATUSANDCHECKTHRESHOLD_ERROR_MSG = "Unable to query for latest data and/or check RFID Status Threshold!";
    private static final String ARGUMENT_MISMATCH_MSG = "Need five arguments in the following order:1) time series client for timeseries data 2)list of data IRIs 3)Number of hours 4) species sparql endpoints 5)whether tagged object contains some chemical (true or false)";
    private static final String AGENT_ERROR_MSG = "The RFID Query Agent could not be constructed!";
	private static final String QUERYBUILDER_ERROR_MSG = "The RFID Query Builder could not be constructed!";

    @Override
    public JSONObject processRequestParameters(JSONObject requestParams, HttpServletRequest request) {
        return processRequestParameters(requestParams);
    } 

    @Override
    public JSONObject processRequestParameters(JSONObject requestParams) {
    	JSONObject jsonMessage = new JSONObject();
		String[] args;
		if (validateInput(requestParams)) {
			LOGGER.info("Passing request to Agent..");
        	String timeseriesDataClientProperties = System.getenv(requestParams.getString(KEY_TIMESERIES_CLIENTPROPERTIES));
        	String DataIRIs =  System.getenv(requestParams.getString(KEY_DATAIRIS));
            args = new String[] {timeseriesDataClientProperties, DataIRIs};
            jsonMessage = initializeAgent(args);
            jsonMessage.accumulate("message","POST request has been sent successfully.");
            requestParams = jsonMessage;
		}
		else {
			LOGGER.info("Unable to validate request sent to the agent!");
			throw new JPSRuntimeException("Unable to validate request sent to the agent!");
		}
		return requestParams;
	}
    
    @Override
    public boolean validateInput(JSONObject requestParams) throws BadRequestException {
      boolean validate = true;

      String timeseriesClientProperties;
	  String dataIRIProperties;

      if (requestParams.isEmpty()) {
    	  validate = false;
    	  LOGGER.info("Empty Request!");  
      }
      else {
		validate = requestParams.has(KEY_TIMESERIES_CLIENTPROPERTIES);
		if (validate == true) {
			dataIRIProperties = (requestParams.getString(KEY_DATAIRIS));
			if (System.getenv(dataIRIProperties) == null) {
				validate = false;
				LOGGER.info("Unable to validate location of iri.properties!");
			}
		}
		
		if (validate == true) {
			timeseriesClientProperties =  (requestParams.getString(KEY_TIMESERIES_CLIENTPROPERTIES));
			if (System.getenv(timeseriesClientProperties) == null) {
				validate = false;
				LOGGER.info("Unable to validate location of client.properties!");
			}
		}
		
	}
	return validate;
    }
    
	 /**
     * @param args A String[] {timeseriesDataClientProperties, DataIRIs, numOfHours, speciesProperties, containSpecies}
     */
    public JSONObject initializeAgent(String[] args) {
    	 // Ensure that there are five arguments provided
        if (args.length != 5) {
            LOGGER.error(ARGUMENT_MISMATCH_MSG);
            throw new JPSRuntimeException(ARGUMENT_MISMATCH_MSG);
        }
        LOGGER.debug("Launcher called with the following files: " + String.join(" ", args));
    	
         // Create the agent
		 FHToggleAgent agent;

		 try {
			 agent = new FHToggleAgent(args[1]);
		 } catch (IOException e) {
			 LOGGER.error(AGENT_ERROR_MSG, e);
			 throw new JPSRuntimeException(AGENT_ERROR_MSG, e);
		 }
		 LOGGER.info("Input agent object initialized.");

		 JSONObject jsonMessage = new JSONObject();
		 jsonMessage.accumulate("Result", "Input agent object initialized.");
		 
		 try {
			 loadTSClientConfigs(args[0]);
		 } catch (IOException e) {
			 throw new JPSRuntimeException(LOADCONFIGS_ERROR_MSG, e);
		 }
		 
		 RemoteStoreClient kbClient = new RemoteStoreClient();
		 kbClient.setQueryEndpoint(sparqlQueryEndpoint);
		 kbClient.setUpdateEndpoint(sparqlUpdateEndpoint);
		 
		 RemoteRDBStoreClient rdbStoreClient = new RemoteRDBStoreClient(dbUrl, dbUsername, dbPassword);
		 agent.setRDBClient(rdbStoreClient);
		 
		 // Create and set the time series client
		 try {
			 tsClient = new TimeSeriesClient<>(kbClient ,OffsetDateTime.class);
			 agent.setTsClient(tsClient);
		 } catch (JPSRuntimeException e) {
			 LOGGER.error(TSCLIENT_ERROR_MSG, e);
			 throw new JPSRuntimeException(TSCLIENT_ERROR_MSG, e);
		 }

		 LOGGER.info("Time series client object initialized.");
		 jsonMessage.accumulate("Result", "Time series client object initialized.");

		 JSONObject overallResults = new JSONObject();

		 try {
			overallResults = agent.queriesStatusAndCheckTimeStamps();
			LOGGER.info("Queried for latest RFID tag status and checked timestamp threshold.");
			jsonMessage.accumulate("Result", "Queried for latest RFID tag status and checked timestamp threshold.");
			LOGGER.info(overallResults.toString());
		}
		catch (Exception e) {
			LOGGER.error(GETLATESTSTATUSANDCHECKTHRESHOLD_ERROR_MSG, e);
			throw new JPSRuntimeException(GETLATESTSTATUSANDCHECKTHRESHOLD_ERROR_MSG ,e);
		}

		
		//loop through results to get data IRI, exceedThreshold, timestamp
		JSONObject result = new JSONObject();
		for (int i = 0; i <= overallResults.length() - 1; i++){
			try{
				result = overallResults.getJSONObject("iri_" + i);
				agent.sendSetpoint(result);
			}
			catch(Exception e){
				throw new JPSRuntimeException("Cannot change VAV setpoint value for " + result.getString("dataIRI"), e);
			}
			
		}
		jsonMessage.accumulate("Result", "Changed setpoint via BACnet.");
		 return jsonMessage;
	}


	/**
     * Reads the username, password, sparql endpoint from a properties file and saves it in fields.
     * @param filepath Path to the properties file from which to read the username, password, sparql endpoints
     */
    public void loadTSClientConfigs(String filepath) throws IOException {
        // Check whether properties file exists at specified location
        File file = new File(filepath);
        if (!file.exists()) {
            throw new FileNotFoundException("No properties file found at specified filepath: " + filepath);
        }
		
        try (InputStream input = new FileInputStream(file)) {

            // Load properties file from specified path
            Properties prop = new Properties();
            prop.load(input);
			if (prop.containsKey("db.url")) {
                this.dbUrl = prop.getProperty("db.url");
            } else {
                throw new IOException("Properties file is missing \"db.url=<db_url>\"");
            }
            if (prop.containsKey("db.user")) {
                this.dbUsername = prop.getProperty("db.user");
            } else {
                throw new IOException("Properties file is missing \"db.user=<db_user>\"");
            }
            if (prop.containsKey("db.password")) {
                this.dbPassword = prop.getProperty("db.password");
            } else {
                throw new IOException("Properties file is missing \"db.password=<db_password>\"");
            }
            if (prop.containsKey("sparql.query.endpoint")) {
	        	kbClient.setQueryEndpoint(prop.getProperty("sparql.query.endpoint"));
	        } else {
	        	throw new IOException("Properties file is missing \"sparql.query.endpoint=<sparql_query_endpoint>\"");
	        }
	        if (prop.containsKey("sparql.update.endpoint")) {
	        	kbClient.setUpdateEndpoint(prop.getProperty("sparql.update.endpoint"));
	        } else {
	        	throw new IOException("Properties file is missing \"sparql.update.endpoint=<sparql_update_endpoint>\"");
	        }
        }
        }
    }

