package uk.ac.cam.cares.jps.servicespool;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import uk.ac.cam.cares.jps.base.config.AgentLocator;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.util.CommandHelper;
import uk.ac.cam.cares.jps.building.BuildingQueryPerformer;
import uk.ac.cam.cares.jps.building.CRSTransformer;
import uk.ac.cam.cares.jps.building.SimpleBuildingData;


@WebServlet("/ADMSAgent")
public class ADMSAgent extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Logger logger = LoggerFactory.getLogger(ADMSAgent.class);

    public ADMSAgent() {
        super();
    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		/*
		 * This agent takes: region, plantIRI, city, weatherstate and later emission stream 
		 * Then writes input files for adms : apl + met
		 * Then starts ADMS and generates output file test.levels.gst
		 * Later it should returns data in the form of Tabular JSON
		 */
		
 
		String myHost = request.getServerName();
		int myPort = request.getServerPort(); // Define the server name and port number without any hardcoding
		
 		String value = request.getParameter("value");
		try {
			JSONObject input = new JSONObject(value);
			input = new JSONObject(value);
			JSONObject region = input.getJSONObject("region");
		 	String plantIRI = input.getString("plant");
		 	String cityIRI = input.getString("city");
			JSONObject weather = input.getJSONObject("weatherstate");
			
			//================== request agent GetBuildingDataForSimulation ===============
			// It was previously an independent agent, currently it is merged with ADMSAgent
			JSONObject bundle = new JSONObject();
			bundle.put("city", cityIRI);
			bundle.put("plant", plantIRI);
			bundle.put("region", region);

			URIBuilder builder = new URIBuilder().setScheme("http").setHost(myHost).setPort(myPort)
					.setPath("/JPS/GetBuildingDataForSimulation")
					.setParameter("value", bundle.toString());
			String buildingsInString = executeGet(builder);	 	
			System.out.println("=========================== buildingsInString ===========================");
			System.out.println(buildingsInString);
			System.out.println("=============================================================");
			
			//==============================================================================
			
			
			double[] plantXY = getPlantXY(plantIRI);
			
			//String srsname = region.getString("srsname");
			double upperx = Double.parseDouble(region.getJSONObject("uppercorner").getString("upperx"));
			double uppery = Double.parseDouble(region.getJSONObject("uppercorner").getString("uppery"));
			double lowerx = Double.parseDouble(region.getJSONObject("lowercorner").getString("lowerx"));
			double lowery = Double.parseDouble(region.getJSONObject("lowercorner").getString("lowery"));
			
			
			String newBuildingData = retrieveBuildingDataInJSON(cityIRI, plantXY[0], plantXY[1], lowerx, lowery, upperx, uppery);
			newBuildingData = newBuildingData.replace('\"', '\'');
			
			
			System.out.println("MY NEW BUILDING DATA = " + newBuildingData);
			writeAPLFile(newBuildingData,plantIRI, region);
			
			
			//writeAPLFile(buildingsInString,plantIRI, region);
			
			writeMetFile(weather);
			
			// =================== Start ADMS when input files are written =======================
			String path = "/JPS/ADMSStarter";
			
			//String targetFolder =  AgentLocator.getPathToWorkingDir(this) + "/" + "ADMS";
			String targetFolder = AgentLocator.getPathToJpsWorkingDir() + "/JPS/ADMS";
			
			URIBuilder builder2 = new URIBuilder().setScheme("http").setHost(myHost).setPort(myPort)
					.setPath(path)
					.setParameter("targetFolder", targetFolder);
			String finalResult = executeGet(builder2);
			response.getWriter().write(finalResult.toString()); // TODO: ZXC Read the output file and then return JSON
			// ====================================================================================
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}


	public void writeMetFile(JSONObject weatherInJSON) {
		
			//String fullPath = AgentLocator.getPathToWorkingDir(this) + "/" + "ADMS";
			String fullPath = AgentLocator.getPathToJpsWorkingDir() + "/JPS/ADMS";
			String targetFolder = AgentLocator.getNewPathToPythonScript("caresjpsadmsinputs", this);
			
			ArrayList<String> args = new ArrayList<String>();
			args.add("python");
			args.add("admsMetWriter.py"); 
			args.add(fullPath);
			// TODO-AE replacing " by $, maybe better by ' as is done in method writeAPLFile
			args.add(weatherInJSON.toString().replace("\"", "$"));
			
			// TODO-AE URGENT URGENT
			//String result = CommandHelper.executeCommands(targetFolder, args);
	}
	
	public String writeAPLFile(String buildingInString, String plantIRI, JSONObject regionInJSON) {
		//String fullPath = AgentLocator.getPathToWorkingDir(this) + "/" + "ADMS";
		String fullPath = AgentLocator.getPathToJpsWorkingDir() + "/JPS/ADMS";
		String targetFolder = AgentLocator.getNewPathToPythonScript("caresjpsadmsinputs", this);
		ArrayList<String> args = new ArrayList<String>();
		args.add("python");
		args.add("admsTest.py"); 
  		args.add(buildingInString.replace("\"", "'"));
 		args.add(regionInJSON.toString().replace("\"", "'")); //TODO ZXC: We should solve the encoding problem once for all 
 		args.add(plantIRI.replace("\"", "'"));
 		args.add(fullPath);
 		// TODO-AE use PythonHelper instead of CommandHelper
  		String result = CommandHelper.executeCommands(targetFolder, args);
		return result;		
	}

	public String executeGet(URIBuilder builder) { // TODO: ZXC: Put this function in utility
		try {
			URI uri = builder.build();
			HttpGet request = new HttpGet(uri);
			request.setHeader(HttpHeaders.ACCEPT, "application/json");
			HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				throw new JPSRuntimeException("HTTP response with error = " + httpResponse.getStatusLine());
			}
			return EntityUtils.toString(httpResponse.getEntity());
		} catch (Exception e) {
			throw new JPSRuntimeException(e.getMessage(), e);
		} 
	}
	
	private String retrieveBuildingDataInJSON(String city, double plantx, double planty, double lowerx, double lowery, double upperx, double uppery) {
		
		logger.info("retrieveBuildingDataInJSON, city=" + city + ", plantx=" + plantx + ", planty=" + planty
				+ ", lowerx=" + lowerx + ", lowery=" + lowery + ", upperx=" + upperx + ", uppery=" + uppery);
		
		List<String> buildingIRIs = new BuildingQueryPerformer().performQueryClosestBuildingsFromRegion(city, plantx, planty, 25, lowerx, lowery, upperx, uppery);
		SimpleBuildingData result = new BuildingQueryPerformer().performQuerySimpleBuildingData(city, buildingIRIs);
		String argument = new Gson().toJson(result);
		return argument;
	}
	
	private double[] getPlantXY(String plant) {
		// TODO-AE URGENT change hard-coded coordinates
		
		// "http://www.theworldavatar.com/kb/nld/thehague/powerplants/Plant-001.owl";
		double plantx = 79831;
		double planty = 454766;
		
		if("http://www.theworldavatar.com/kb/deu/berlin/powerplants/Heizkraftwerk_Mitte.owl#Plant-002".equals(plant)) {
			
			String sourceCRS = "";
			String targetCRS = "";
			
			double[] sourceCenter = new double[2];
			double[] targetCenter = new double[2];
			
			sourceCRS = CRSTransformer.EPSG_25833; // Berlin
			sourceCenter = new double[]{392825, 5819122};
			targetCRS = CRSTransformer.EPSG_28992; // The Hague
			targetCenter = CRSTransformer.transform(sourceCRS, targetCRS, sourceCenter);
			plantx = targetCenter[0];
			planty = targetCenter[1];
		} 
		
		return new double[] {plantx, planty};
	}
}
