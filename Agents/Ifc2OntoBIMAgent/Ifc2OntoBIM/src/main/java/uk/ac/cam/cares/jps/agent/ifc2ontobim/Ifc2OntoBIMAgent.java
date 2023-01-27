package uk.ac.cam.cares.jps.agent.ifc2ontobim;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import uk.ac.cam.cares.jps.base.agent.JPSAgent;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import java.nio.file.Paths;
import java.util.Set;

/**
 * This class acts as the entry point of the compiled war, and coordinates the two components (IfcOwlConverterAgent and
 * OntoBimAgent) to produce a TTL file with ontoBIM instances converted from an IFC model input.
 *
 * @author qhouyee
 */
@WebServlet(urlPatterns = {"/retrieve"})
public class Ifc2OntoBIMAgent extends JPSAgent {
    private static String endpoint;
    private static final Logger LOGGER = LogManager.getLogger(Ifc2OntoBIMAgent.class);
    private static final String IFCOWL_CONVERSION_ERROR_MSG = "Failed to convert to IfcOwl schema. ";
    private static final String KEY_BASEURI = "uri";
    private static final String KEY_ENDPOINT = "endpoint";
    private static final String ttlDir = Paths.get(System.getProperty("user.dir"), "data").toString();
    private static String IFCOWL_API_URl ="http://ifcowlconverter:8080/ifcowlconverter/";

    @Override
    public JSONObject processRequestParameters(JSONObject requestParams, HttpServletRequest request) {
        return processRequestParameters(requestParams);
    }

    @Override
    public JSONObject processRequestParameters(JSONObject requestParams) {
        JSONObject jsonMessage = new JSONObject();
        if (validateInput(requestParams)) {
            LOGGER.info("Passing request to Ifc2OntoBIM Agent..");

            // Process request parameters
            String baseURI = requestParams.getString(KEY_BASEURI);
            endpoint = requestParams.has(KEY_ENDPOINT) ? requestParams.getString(KEY_ENDPOINT) : "";

            String[] args = (!baseURI.equals("default")) ? new String[]{baseURI} : new String[]{"default"};
            jsonMessage = this.runAgent(args);
            LOGGER.info("All ttl files have been generated in OntoBIM. Please check the directory for the files at :");
            jsonMessage.accumulate("Result", "All ttl files have been generated in OntoBIM. Please check the directory.");
        } else {
            LOGGER.fatal("Request parameters are not defined correctly.");
            jsonMessage.put("Result", "Request parameters are not defined correctly.");
        }
        return jsonMessage;
    }

    @Override
    public boolean validateInput(JSONObject requestParams) throws BadRequestException {
        boolean validate;
        if (requestParams.isEmpty()) {
            validate = false;
        } else {
            validate = requestParams.has(KEY_BASEURI);
            if (validate) {
                String baseURI = requestParams.getString(KEY_BASEURI);
                // Base URI passed must either be default or a valid URL (starts with http/https and ends with / or #)
                validate = baseURI.equals("default") ||
                        (baseURI.startsWith("http://www.") || baseURI.startsWith("https://www.")) && (baseURI.endsWith("/") || baseURI.endsWith("#"));
                // When there is an optional endpoint specified, verify it is correct
                if (requestParams.has(KEY_ENDPOINT)) {
                    String endpoint = requestParams.getString(KEY_ENDPOINT);
                    validate = endpoint.startsWith("http") && endpoint.contains("/blazegraph/namespace") && endpoint.endsWith("/sparql");
                }
            }
        }
        return validate;
    }

    // Args can be used to set flags following the IFC2RDF options
    public JSONObject runAgent(String[] args) {
        JSONObject jsonMessage = new JSONObject();

        // Convert the IFC files in the target directory to TTL using IfcOwl Schema
        LOGGER.info("Sending POST request to IfcOwlConverterAgent...");
        try {
            String inputJson = "{\""+KEY_BASEURI+"\":\"" + args[0] + "\"}";
            AccessClient.sendPostRequest(IFCOWL_API_URl, inputJson);
        } catch (Exception e) {
            LOGGER.fatal(IFCOWL_CONVERSION_ERROR_MSG + e.getMessage());
            throw new JPSRuntimeException(IFCOWL_CONVERSION_ERROR_MSG + e.getMessage());
        }
        LOGGER.info("All IFC files have been successfully converted to IfcOwl instances.");
        OntoBimConverter bimConverter = new OntoBimConverter();
        // Generate a set of ttl files  in target directory
        Set<String> ttlFileList = bimConverter.listTTLFiles(ttlDir);

        if (ttlFileList.size()==0){
            LOGGER.info("No TTL file detected! Please place at least 1 IFC file input.");
            jsonMessage.put("Result", "No TTL file detected! Please place at least 1 IFC file input.");
        } else if (ttlFileList.size()>1 && !endpoint.isEmpty()){
            LOGGER.info("More than one TTL file detected! Files will not be uploaded to " + endpoint);
            jsonMessage.put("Result", "More than one TTL file detected! Files will not be uploaded to " + endpoint);
        }

        if (ttlFileList.size()!=0) {
            // Convert each TTL file with IFCOwl instances to ontoBIM instances
            for (String ttlFile : ttlFileList) {
                LOGGER.info("Preparing to convert IFCOwl to OntoBIM schema for TTL file: " + ttlFile);
                bimConverter.convertOntoBIM(ttlFile);
                LOGGER.info(ttlFile + " has been successfully converted!");
                jsonMessage.accumulate("Result", ttlFile + " has been successfully converted!");

                // Load to an endpoint if specified
                if (!endpoint.isEmpty()) {
                    if (ttlFileList.size() == 1) {
                        AccessClient.uploadTTL(endpoint, ttlFile);
                        LOGGER.info(ttlFile + " has been uploaded to " + endpoint);
                        jsonMessage.accumulate("Result", ttlFile + " has been uploaded to " + endpoint);
                    }
                }
            }
        }
        return jsonMessage;
    }
}