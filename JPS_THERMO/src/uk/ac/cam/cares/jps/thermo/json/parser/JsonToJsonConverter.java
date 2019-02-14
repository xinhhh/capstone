package uk.ac.cam.cares.jps.thermo.json.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.query.resultio.QueryResultParseException;
import org.eclipse.rdf4j.query.resultio.helpers.QueryResultCollector;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONParser;

import org.json.JSONObject;



/**
 * 
 * @author NK510 Updates generated JSON file with features 'species IRI' and
 *         'quantum calculation IRI'.
 *
 */
public class JsonToJsonConverter {

	/** The Constant logger. */
	final static Logger logger = Logger.getLogger(JsonToJsonConverter.class.getName());

	/**
	 * 
	 * @author NK510
	 * @param filePath
	 * @throws QueryResultParseException
	 * @throws QueryResultHandlerException
	 * @throws IOException
	 * 
	 *                                     Method reads generated json file as a
	 *                                     result of SPARQL query. Parses that file
	 *                                     and extracts species IRI and quantum
	 *                                     calculation IRI. Both features are stored
	 *                                     in Java List data structure.
	 */
	public Set<String> getListIRI(String filePath)
			throws QueryResultParseException, QueryResultHandlerException, IOException {

		Set<String> iriSet = new HashSet<String>();

		SPARQLResultsJSONParser parser = new SPARQLResultsJSONParser();// SimpleValueFactory.getInstance()

		QueryResultCollector handler = new QueryResultCollector();

		String massUnit = "";
		String speciesIRI = "";
		String qunatumCalculationIRI = "";

		/**
		 * 
		 * @author NK510 RDF4J JSON parser is used to parse input JSON file.
		 * 
		 */
		parser.setQueryResultHandler(handler);

		parser.parseQueryResult(new FileInputStream(filePath));

		List<BindingSet> bindingSetList = handler.getBindingSets();

		for (BindingSet b : bindingSetList) {

			if ((b.getValue("speciesIRI") != null)) {
				speciesIRI = b.getValue("speciesIRI").stringValue();
				iriSet.add(speciesIRI);
				logger.info("speciesIRI: " + speciesIRI + ",  species  name: " + b.getBinding("speciesIRI").getName());
			}
			;

			if ((b.getValue("g09") != null)) {
				qunatumCalculationIRI = b.getValue("g09").stringValue();
				iriSet.add(qunatumCalculationIRI);
				logger.info("qunatumCalculationIRI: " + qunatumCalculationIRI + ", g09 name: "
						+ b.getBinding("g09").getName());
			}

			if ((b.getValue("massUnit") != null)) {
				massUnit = b.getBinding("massUnit").getValue().stringValue();
				iriSet.add(massUnit);
				logger.info("massUnit: " + massUnit + ", mass unit name: " + b.getBinding("massUnit").getName());
			}

		}

		return iriSet;

	}

	/**
	 * 
	 * @author NK510
	 * @param filePath
	 * @param iriList
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * 
	 *                                      Updates existing JSON file that is
	 *                                      generated by Python script. Adds quantum
	 *                                      calculation IRI and species IRI into
	 *                                      generated JSON file.
	 */
	public String updateJsonContent(String filePath, String speciesIRI, String quantumCalculationIRI, String massUnit)
			throws UnsupportedEncodingException, IOException {
		
		/**
		 * @author NK510 Adds the content of JSON file into Json object.
		 */
		
		JSONObject  jsonObject = new JSONObject(IOUtils.toString(new FileInputStream (new File(filePath)),"UTF-8"));
		
		/**
		 * 
		 * @author NK510 Updates generated Json Object with quantum calculation IRI, 
		 *         species IRI, mass unit IRI, thermo agent IRI.
		 * 
		 */

		jsonObject.append("massUnit", massUnit);

		jsonObject.append("speciesIRI", speciesIRI);

		jsonObject.append("quantumCalculationIRI", quantumCalculationIRI);

		jsonObject.append("ThermoAgentIRI", "Thermo agent IRI will be added later.");
		
		String updatedJsonContent = jsonObject.toString();

		logger.info("updated json string: " + updatedJsonContent);

		return updatedJsonContent;

	}

	public void writeUpdatedJsonToFile(String updatedJsonContent, String updatedJsonOutputFilePath,
			HttpServletResponse response) throws IOException {

		/**
		 * @author NK510 Stores updated json content into  json file.
		 */

		FileWriter fileWriter = new FileWriter(new File(updatedJsonOutputFilePath));

		fileWriter.write(updatedJsonContent);

		fileWriter.flush();
		fileWriter.close();
		
		/**
		 * @author NK510 Prints updated json content on brower's page
		 */
		PrintWriter printerWriter = response.getWriter();

		printerWriter.println(updatedJsonContent);

		printerWriter.close();

		
	}

}