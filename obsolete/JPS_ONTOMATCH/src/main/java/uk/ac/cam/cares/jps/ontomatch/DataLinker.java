package uk.ac.cam.cares.jps.ontomatch;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ResultSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cares.jps.base.agent.JPSAgent;
import uk.ac.cam.cares.jps.base.config.AgentLocator;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.query.JenaHelper;
import uk.ac.cam.cares.jps.base.query.JenaResultSetFormatter;
import uk.ac.cam.cares.jps.base.query.QueryBroker;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
import uk.ac.cam.cares.jps.paramsValidator.ParamsValidateHelper;
import uk.ac.cam.cares.jps.paramsValidator.ParamsValidateHelper.CUSTOMVALUETYPE;

/**
 * Agent that read alignment result file, then write linkage to KG Input from
 * KG: alignment Ontology of one matching process Output to KG: owl:sameAs
 * triples
 * 
 * @author shaocong zhang
 * @version 1.0
 * @since 2020-11-25
 */
@WebServlet(urlPatterns = { "/dataLinker" })
public class DataLinker extends JPSAgent {

	private static final long serialVersionUID = -7950910519843708707L;

	@Override
	public JSONObject processRequestParameters(JSONObject requestParams, HttpServletRequest request) {
		logger.info("Datalinker agen");
		JSONObject result = new JSONObject();
		JSONObject jo = requestParams;
		String afileIRI = "";
		double threshold = 0.0;

		if (validateInput(requestParams)) {

		try {
			afileIRI = jo.getString("alignmentIRI");
			threshold = jo.getDouble("threshold");

		} catch (JSONException e) {
			throw new JPSRuntimeException(e);
		}

		logger.info("reading alignment from  " + afileIRI);
		// get a list of matched IRIs
		List<String[]> instances2Equal = provideAlignedInstanceList(afileIRI, threshold);
		// add owl:sameAs triple to KG
		addEqual(instances2Equal);
		try {
			result.put("success", 1);
		} catch (JSONException e) {
			throw new JPSRuntimeException(e);
		}
	}
		return result;
	}

	/***
	 * function that reads the alignment list from an alignment ontology file and
	 * return it as a list of string array
	 * 
	 * @param iriOfAlignmentFile
	 * @param threshold          threshold of measure to filter aligned entities
	 *                           that get returned by query
	 * @return alignment result as list of string array <{entity1, entity2,
	 *         measure}...>
	 */
	public List<String[]> provideAlignedInstanceList(String iriOfAlignmentFile, double threshold) {
		String queryStr = "PREFIX alignment: <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#> "
				+ "SELECT ?entity1 ?entity2 "
				// + "WHERE {?a ?p ?o."

				+ "WHERE {?cell alignment:entity1 ?entity1." + "?cell  alignment:entity2 ?entity2 ."
				+ "?cell alignment:measure ?measure."

				+ "FILTER (?measure >= " + Double.toString(threshold) + " ) " // filtering gen 001 as it is slackbus
				+ "}";
		System.out.println(queryStr);
		List<String[]> resultListfromquery = null;
		try {
			OntModel model = JenaHelper.createModel(iriOfAlignmentFile);
			ResultSet resultSet = JenaHelper.query(model, queryStr);
			String result = JenaResultSetFormatter.convertToJSONW3CStandard(resultSet);
			String[] keys = JenaResultSetFormatter.getKeys(result);
			resultListfromquery = JenaResultSetFormatter.convertToListofStringArrays(result, keys);

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			logger.error(exceptionAsString);
		}
		return resultListfromquery;

	}

	/**
	 * sparql update owl:sameAs triple to KG
	 * 
	 * @param alignedInstances
	 */
	public void addEqual(List<String[]> alignedInstances) {
		QueryBroker broker = new QueryBroker();
		// read file, then rewrite whole file and save
		for (String[] paras : alignedInstances) {
			String iri1 = paras[0];
			String iri2 = paras[1];
			String updateStr = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " + //
					"INSERT DATA\r\n" + "{<" + iri1 + "> owl:sameAs <" + iri2 + ">.\r\n" + "}";
			try {
				broker.updateFile(iri1, updateStr);

			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				String exceptionAsString = sw.toString();
				logger.error(exceptionAsString);
			}
		}
	}

	@Override
	public boolean validateInput(JSONObject requestParams) throws BadRequestException {
		if (requestParams.isEmpty() || !requestParams.has("alignmentIRI")) {
			throw new BadRequestException();
		}
		Map<String, CUSTOMVALUETYPE> paramTypes = new HashMap<String, CUSTOMVALUETYPE>();
		paramTypes.put("alignmentIRI", CUSTOMVALUETYPE.URL);
		paramTypes.put("threshold", CUSTOMVALUETYPE.THRESHOLD);
		if (!ParamsValidateHelper.validateALLParams(requestParams, paramTypes)) {
			throw new BadRequestException();
		}
		return true;

	}

}
