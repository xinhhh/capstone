package uk.ac.cam.cares.jps.scenario.kg;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cares.jps.base.agent.JPSAgent;
import uk.ac.cam.cares.jps.base.config.JPSConstants;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.interfaces.StoreClientInterface;
import uk.ac.cam.cares.jps.base.query.StoreRouter;
import uk.ac.cam.cares.jps.base.util.InputValidator;
import uk.ac.cam.cares.jps.base.util.MiscUtil;
import uk.ac.cam.cares.jps.scenario.kb.KnowledgeBaseAgent;

//@WebServlet(urlPatterns = {"/kb/*"})
public class AccessAgent extends JPSAgent{

	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.getLogger(KnowledgeBaseAgent.class);

	@Override
	public JSONObject processRequestParameters(JSONObject requestParams) {
		JSONObject result = processRequestParameters(requestParams,null);
		return result;
	}

	@Override
	public JSONObject processRequestParameters(JSONObject requestParams, HttpServletRequest request) {
		System.out.println("JSON PARAMS" + requestParams.toString());
		if (!validateInput(requestParams)) {
			throw new JSONException("AccessAgent: Input parameters not found.\n");
		}
		
		JSONObject JSONresult = new JSONObject();
		String method = MiscUtil.optNullKey(requestParams, JPSConstants.METHOD);
		System.out.println("METHOD: "+ method);
		switch (method) {
			case HttpGet.METHOD_NAME:	
				JSONresult = get(requestParams);
			    break;
			case HttpPost.METHOD_NAME:
				post(requestParams);
				break;
			case HttpPut.METHOD_NAME:
				put(requestParams);
				break;
			}		
	    return JSONresult;
	}
		
	/**
	 * Perform HTTP GET. This will be either a SPARQL query or "get" all triples (from specified graph).
	 * @param requestParams
	 * @return
	 */
	public JSONObject get(JSONObject requestParams) {
		
		String sparqlquery = MiscUtil.optNullKey(requestParams, JPSConstants.QUERY_SPARQL_QUERY);
		String sparqlupdate = MiscUtil.optNullKey(requestParams, JPSConstants.QUERY_SPARQL_UPDATE);
		String graphIRI = null; //TODO 
		String accept = MiscUtil.optNullKey(requestParams, JPSConstants.HEADERS);		
	    String targetIRI = requestParams.getString(JPSConstants.TARGETIRI);
		
	    if(sparqlupdate != null) {
	    	throw new JPSRuntimeException("parameter " + JPSConstants.QUERY_SPARQL_UPDATE + " is not allowed");
	    }
	    
		try {
			logInputParams(requestParams, sparqlquery, false);
			
			StoreClientInterface kbClient = StoreRouter.getStoreClient(getShortIRI(targetIRI), true, false);
			
			JSONObject JSONresult = new JSONObject();
			String result = null;
			if (sparqlquery != null) { 
				//query
				result = kbClient.execute(sparqlquery);
				JSONresult.put("result",result);
			}else {		//TODO: defaulting to this could be dangerous for large triple store
				//get
				result = kbClient.get(graphIRI, accept);
				JSONresult.put("result",result);
			}
			return JSONresult;
		
		} catch (RuntimeException e) {
			logInputParams(requestParams, sparqlquery, true);
			throw new JPSRuntimeException(e);
		}
	}
	
	/**
	 * Perform HTTP PUT. Insert triples into store.
	 * @param requestParams
	 */
	public void put(JSONObject requestParams) {
		
		String sparqlquery = MiscUtil.optNullKey(requestParams, JPSConstants.QUERY_SPARQL_QUERY);
		String sparqlupdate = MiscUtil.optNullKey(requestParams, JPSConstants.QUERY_SPARQL_UPDATE);
		String graphIRI = null; //TODO 
		String body = MiscUtil.optNullKey(requestParams, JPSConstants.CONTENT);
		String contentType = MiscUtil.optNullKey(requestParams, JPSConstants.CONTENTTYPE);	
	    String targetIRI = requestParams.getString(JPSConstants.TARGETIRI);
	    
	    if(sparqlquery!=null && sparqlupdate!=null) {
	    	throw new JPSRuntimeException("parameters " + JPSConstants.QUERY_SPARQL_QUERY + " and " 
	    									+ JPSConstants.QUERY_SPARQL_UPDATE + " are not allowed");
	    }
	    
		try {
			logInputParams(requestParams, null, false);
			
			//TODO check target or datasetUrl for this
			StoreClientInterface kbClient = StoreRouter.getStoreClient(getShortIRI(targetIRI), false, true);
			
			kbClient.insert(graphIRI, body, contentType);
		} catch (RuntimeException e) {
			logInputParams(requestParams, null, true);
			throw new JPSRuntimeException(e);
		}
	}
	
	/**
	 * Perform HTTP POST. This will perform a SPARQL update on the store. 
	 * @param requestParams
	 */
	public void post(JSONObject requestParams) {	
		
		String sparqlquery = MiscUtil.optNullKey(requestParams, JPSConstants.QUERY_SPARQL_QUERY);
		String sparqlupdate = MiscUtil.optNullKey(requestParams, JPSConstants.QUERY_SPARQL_UPDATE);
		String targetIRI = requestParams.getString(JPSConstants.TARGETIRI);
		
		if(sparqlquery != null) {
			throw new JPSRuntimeException("parameter " + JPSConstants.QUERY_SPARQL_QUERY + " is not allowed");
		}
		
		try {
			logInputParams(requestParams, sparqlupdate, false);
			
			StoreClientInterface kbClient = StoreRouter.getStoreClient(getShortIRI(targetIRI), false, true);
			
			if (sparqlupdate!=null) {
				kbClient.executeUpdate(sparqlupdate);
			}else {
				throw new JPSRuntimeException("parameter " + JPSConstants.QUERY_SPARQL_UPDATE + " is missing");
			}
		} catch (RuntimeException e) {
			logInputParams(requestParams, sparqlupdate, true);
			throw new JPSRuntimeException(e);
		}
	}
	
	//TODO
	@Override
	public boolean validateInput(JSONObject requestParams) throws BadRequestException {	    
		
	    if (requestParams.isEmpty()) {
	        throw new BadRequestException();
	    }
	    try {
	    	
	    	String method = MiscUtil.optNullKey(requestParams,JPSConstants.METHOD);
	        if (method == null) {
	        	return false;
	        }
	    	
	        boolean q = InputValidator.checkIfURLpattern(requestParams.getString(JPSConstants.REQUESTURL));
	        if(!q) {return false;};
	    	
	        String sparqlquery = MiscUtil.optNullKey(requestParams, JPSConstants.QUERY_SPARQL_QUERY);
			String sparqlupdate = MiscUtil.optNullKey(requestParams,  JPSConstants.QUERY_SPARQL_UPDATE);
			if (sparqlquery != null && sparqlupdate != null) { //both query and update are filled. 
				return false;
			}else {
				if (sparqlquery != null) { 
					if (InputValidator.checkIfValidQuery(sparqlquery)!= true){
						return false;
					}
				}
				if (sparqlupdate != null) {
					if (InputValidator.checkIfValidUpdate(sparqlupdate)!= true){
						return false;
					}
				}
			}
			
			return true;
	        
	    }catch (JSONException ex) {
	    	return false;
	    }
	}
	
	protected void logInputParams(JSONObject requestParams, String sparql, boolean hasErrorOccured) {
		
		String method = MiscUtil.optNullKey(requestParams, JPSConstants.METHOD);
		String path = MiscUtil.optNullKey(requestParams, JPSConstants.PATH);		
		String requestUrl = MiscUtil.optNullKey(requestParams, JPSConstants.REQUESTURL);
		String resourceUrl= MiscUtil.optNullKey(requestParams,JPSConstants.SCENARIO_RESOURCE);
		String datasetUrl = MiscUtil.optNullKey(requestParams, JPSConstants.SCENARIO_DATASET);	
		String contentType = MiscUtil.optNullKey(requestParams, JPSConstants.CONTENTTYPE);
		
		StringBuffer b = new StringBuffer(method);
		b.append(" with requestedUrl=").append(requestUrl);
		b.append(", path=").append(path);
		b.append(", datasetUrl=").append(datasetUrl);
		b.append(", resourceUrl=").append(resourceUrl);
		b.append(", contentType=").append(contentType);
		if (hasErrorOccured) {
			b.append(", sparql=" + sparql);
			logger.error(b.toString());
		} else {
			if (sparql != null) {
				int i = sparql.toLowerCase().indexOf("select");
				if (i > 0) {
					sparql = sparql.substring(i);
				}
				if (sparql.length() > 150) {
					sparql = sparql.substring(0, 150);
				}
			}
			b.append(", sparql (short)=" + sparql);
			logger.info(b.toString());
		}
	}
	
	/**
	 * Create short iri required by the StoreRouter. Remove host from uri.
	 * @param url
	 * @return
	 */
	public static String getShortIRI(String url) {
		URI uri = null;
		try {
			uri = new URI(URLDecoder.decode(url,"UTF-8"));
			String authority = uri.getAuthority();
			// A host should contain either a "." or be the "localhost", otherwise this is already a short iri
			if(authority.contains(".") || authority.contains("localhost")) { 
				final String host = authority;
				return Arrays.stream(uri.toString().split("/"))
						.filter(a -> !(host.equals(a)))
						.collect(Collectors.joining("/"));
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
	}
}
