package uk.ac.cam.cares.jps.agent.fhtoggle;

import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.query.RemoteRDBStoreClient;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeries;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;

import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.TimeZone;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.jena.atlas.json.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;


/**
 * Class to query for the latest RFID tag status from the knowledge graph and subsequently the chemical species information.
 * @author  */
public class FHToggleAgent{

	/**
     * Logger for reporting info/errors.
     */
    private static final Logger LOGGER = LogManager.getLogger(FHToggleAgentLauncher.class);

    /**
     * The time series client to interact with the knowledge graph and data storage
     */
    private TimeSeriesClient<OffsetDateTime> tsClient;

    ArrayList<String> dataIRIList = new ArrayList<String>();

    /**
     * Log messages
     */
    private static final String GETLATESTDATA_ERROR_MSG = "Unable to query for latest data!" ;

    /**
     * List to store timeseries string values
     */
    private List<String> dataValuesAsString;

    /**
     * timeSeries Object
     */
    TimeSeries<OffsetDateTime> timeseries;
    
    /**
     * RDB Client Object
     */
    private RemoteRDBStoreClient RDBClient;

    /**
     * The Zone offset of the timestamp (https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/ZoneOffset.html)
     */
    public static final ZoneOffset ZONE_OFFSET = ZoneOffset.UTC;

    /**
     * Standard constructor which receives and parses DataIRIs from the AgentLauncher class.
     * @param dataIRIs The data IRIs to query latest data for
     */
    public FHToggleAgent(String propertiesFile) throws IOException {
        try (InputStream input = new FileInputStream(propertiesFile)) {
            // Load properties file from specified path
            Properties prop = new Properties();
            prop.load(input);
            
            // Read the mappings folder from the properties file
            for(String key : prop.stringPropertyNames()) {
                try {
                    dataIRIList.add(prop.getProperty(key));
                }
                catch (NullPointerException e) {
                    throw new IOException ("The IRI for " + key + "cannot be found in the properties file.");
                }
             }
        }
        LOGGER.info("The first element in this list is " + dataIRIList.get(0));
    }

    /**
     * Setter for the time series client.
     * @param tsClient The time series client to use.
     */
    public void setTsClient(TimeSeriesClient<OffsetDateTime> tsClient) {
        this.tsClient = tsClient;
    }

    /**
     * Setter for the remote rdb store client.
     * @param RDBClient The remote rdb store client to use.
     */
    public void setRDBClient(RemoteRDBStoreClient RDBClient) {
        this.RDBClient = RDBClient;
    }

    /**
     * Query the database for the latest RFID tag status and timestamp.
     * @param dataIRI the data IRI to query values from
     */
    public TimeSeries<OffsetDateTime> queryLatestStatus(String dataIRI)throws IllegalArgumentException {
        try (Connection conn = RDBClient.getConnection()){
            timeseries = tsClient.getLatestData(dataIRI, conn);
        } catch (Exception e) {
            throw new JPSRuntimeException(GETLATESTDATA_ERROR_MSG, e);
        }
        return timeseries;
    }

    /**
     * Queries for latest data and check whether latest timestamps exceed threshold set by user
     * @return results
     * A Json Object with the following format {iri_0: {exceedThreshold: true/false, timestamp: timestamp value, dataIRI: data IRI 1}, 
     *                                          iri_1: {exceedThreshold: true/false, timestamp: timestamp value, dataIRI: data IRI 2} }
     */
    public JSONObject queriesStatusAndCheckTimeStamps() {
        JSONObject results = new JSONObject();

        for (int i = 0; i <= dataIRIList.size() - 1; i++) {
            JSONObject values = new JSONObject();
            TimeSeries<OffsetDateTime> LatestTimeSeries = queryLatestStatus(dataIRIList.get(i));
            values = getLatestSetpoint(LatestTimeSeries, dataIRIList.get(i));
            results.put("iri_"+i, values);
            LOGGER.info(results);
        }

        LOGGER.info("The final result is " + results);
        return results;
    }

    public JSONObject getLatestSetpoint(TimeSeries<OffsetDateTime> timeSeriesObject, String dataIRI) {
        JSONObject result = new JSONObject();
        String latestTimeSeriesValue;
        OffsetDateTime latestTimeStamp;
        Double setpointVal = 0.;
        try {
            //process timeseries object and convert to a suitable form, retrieve values only
            dataValuesAsString = timeSeriesObject.getValuesAsString(dataIRI);
            latestTimeSeriesValue = dataValuesAsString.get(dataValuesAsString.size() - 1);
            latestTimeStamp = timeSeriesObject.getTimes().get(timeSeriesObject.getTimes().size() - 1);
        } catch (Exception e){
            throw new JPSRuntimeException("Unable to retrieve latest value and timestamp from timeseries object.");
        }

        if(latestTimeSeriesValue.contains("1")) {
            //TODO change setpoiint value to in-use capacity once available
            setpointVal = 1.;
        }

        result.put("dataIRI", dataIRI);
        result.put("ts", latestTimeStamp);
        result.put("value", setpointVal);

        return result;
    }


    public void sendSetpoint (JSONObject result) {
        //TODO contact the BACnet(?) API here
    }

    
}

