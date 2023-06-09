
package uk.ac.cam.cares.jps.dispersion.sensor;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import uk.ac.cam.cares.jps.base.config.AgentLocator;
import uk.ac.cam.cares.jps.base.config.IKeys;
import uk.ac.cam.cares.jps.base.config.KeyValueManager;
import uk.ac.cam.cares.jps.base.query.JenaResultSetFormatter;
import uk.ac.cam.cares.jps.base.query.KnowledgeBaseClient;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
@WebServlet(urlPatterns = {"/AirQualitySensorAgent","/resetAirQualityRepository"})
public class AirQualitySensorAgent extends JPSHttpServlet {

	private static final long serialVersionUID = 1L;
	public static AirSensorConfig config= new AirSensorConfig();
	public static String rdf4jServer = config.getRDF4JLocation();
	public static String repositoryID = "airqualitystation";
	public static Repository repo = new HTTPRepository(rdf4jServer, repositoryID);
	public static final String dataseturl=KeyValueManager.get(IKeys.DATASET_AIRQUALITY_URL);
	protected void setLogger() {
		logger=LoggerFactory.getLogger(AirQualitySensorAgent.class);
	}
	Logger logger= LoggerFactory.getLogger(AirQualitySensorAgent.class);
	
	protected JSONObject processRequestParameters(JSONObject requestParams, HttpServletRequest request) {
		
		JSONObject response= new JSONObject();
		String path = request.getServletPath();
		if(path.contains("/resetAirQualityRepository")) { //used for Both AQMesh and soft sensor use case
			repo = new HTTPRepository(rdf4jServer, repositoryID);
	   		RepositoryConnection con = repo.getConnection();
			String location = requestParams.optString("location", "singapore_AQ");
	   		String cityiri= requestParams.optString("city", "http://dbpedia.org/resource/Singapore");
	   		resetAllAQMesh(location,cityiri);
			response.put("status", "reset endpoint successful");
			
		}else { //used for AQmesh only
			String cityiri= requestParams.optString("city", "http://dbpedia.org/resource/Singapore");
			//right now the input is not connected yet
			String context = uploadData(cityiri);
			response.put("airStationIRI", context);	
		}
	
		return response;
	}
	
	public List<String[]> extractAvailableContext(String cityiri) {
		 String querycontext = "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
					+ "PREFIX j4:<http://www.theworldavatar.com/ontology/ontosensor/OntoSensor.owl#> "
					+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/process_control_equipment/measuring_instrument.owl#> "
					+ "PREFIX j6:<http://www.w3.org/2006/time#> " 
					+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
					+ "SELECT DISTINCT ?graph " 
					+ "{ graph ?graph " 
					+ "{ "
					+ "?graph j2:hasAddress <"+cityiri+"> ."
					//+ "?graph j2:enumerationValue <"+"AQMESH"+"> ."
					
					+ "}"
					+ "}";
		 
		 List<String[]> listmap = queryEndPointDataset(querycontext); //it will give 30 data
		
		 return listmap;
	}
	
	public void resetRepoTrial(RepositoryConnection con, String location) {// unused for the servlet
		int stnnumber=1;
		String index="";
		String midfix="";
		if(location.contains("AQ")) {
			if(location.contains("singapore")) {
				stnnumber=1;
				index="SGAQMesh";
				midfix="sgp/singapore";
			}
			
		}
		else {
			if(location.contains("hague")) {
				stnnumber=1;
				index="NL";
				midfix="nld/thehague";
			}else if(location.contains("berlin")) {
				stnnumber=1;
				index="DE";
				midfix="deu/berlin";
			}else if(location.contains("kong")) {
				stnnumber=8;
				index="HK";
				midfix="hkg/hongkong";
			}else if(location.contains("singapore")) {
				stnnumber=14;
				index="SG";
				midfix="sgp/singapore";
			}
		}
		for (int d = 1; d <= stnnumber; d++) {
			String number = "00" + d;
			if (d > 9&& d<=99) {
				number = "0" + d;
			}
			String[] filenames = { index+"CO2Sensor-" + number + ".owl",
					index+"COSensor-" + number + ".owl", index+"SO2Sensor-" + number + ".owl",
					index+"O3Sensor-" + number + ".owl", index+"NO2Sensor-" + number + ".owl",
					index+"NOSensor-" + number + ".owl", index+"NOxSensor-" + number + ".owl",
					index+"PM1Sensor-" + number + ".owl", index+"PM2.5Sensor-" + number + ".owl",index+"PM10Sensor-" + number + ".owl"};
			String context = "http://www.theworldavatar.com/kb/"+midfix+"/AirQualityStation-" + number
					+ ".owl#AirQualityStation-" + number;
			if (location.contains("AQ")) {
				context = "http://www.theworldavatar.com/kb/"+midfix+"/AirQualityStationAQMesh-" + number
						+ ".owl#AirQualityStationAQMesh-" + number;
				
			}
			System.out.println("upload files for graph");
			for (String el : filenames) {
				new AirQualitySensorAgent().addFiletoRepo(con, el, context,midfix);

			}
		}
	}
	
	public void addFiletoRepo(RepositoryConnection con,String filename,String contextiri, String midfix) {
		String root=AgentLocator.getProperty("absdir.root");
		String fileprefix=root+"/kb/"+midfix+"/";
		String iriprefix="http://www.theworldavatar.com/kb/"+midfix+"/";
		File file =new File(fileprefix+filename);
		String baseURI=iriprefix+filename;
		try {
			
			try {
	    		rdf4jServer = "http://localhost/rdf4j-server"; //for claudius
	    		 repo = new HTTPRepository(rdf4jServer, repositoryID);
//				con.add(file, baseURI, RDFFormat.RDFXML);
				//BELOW IS FOR ADDING THE NAMED GRAPH/CONTEXT :
				ValueFactory f=repo.getValueFactory();
				IRI context= f.createIRI(contextiri);
				con.add(file, baseURI, RDFFormat.RDFXML,context);
				System.out.println("success");
			}
			finally {
				con.close();				
			}
			
		}
		catch(RDF4JException e) {
			System.out.println("fail 1");
			logger.error(e.getMessage());
		}
		catch (java.io.IOException e) {
			System.out.println("fail 2");
			logger.error(e.getMessage());
		}
	}
	
	public void insertDataRepoContext(List<String>info,String context) {

		String sparqlupdate2 = "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
					+ "PREFIX j4:<http://www.theworldavatar.com/ontology/ontosensor/OntoSensor.owl#> "
					+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/process_control_equipment/measuring_instrument.owl#> "
					+ "PREFIX j6:<http://www.w3.org/2006/time#> " 
					+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
					+ "WITH <" + context + ">"
					//+ "DELETE { ?valueemission j2:numericalValue ?vemission .} "
					+ "INSERT {"
					+ "<" + context+ "> j2:hasAddress <"+info.get(0)+"> ." 
					+ "<" + context+ "> j2:enumerationValue \""+info.get(1)+"\" ." 
					+ "<" + context+ "> j4:hasOverallPSI \""+info.get(2)+"\"^^xsd:integer ." 
					+ "<" + context+ "> j4:podSerialNumber \""+info.get(3)+"\" ."  //for aqmesh
					+ "<" + context+ "> j4:locationName \""+info.get(4)+"\" ." //for aqmesh
					+ "} "
					+ "WHERE { " 
					+ "}";
			
		KnowledgeBaseClient.update(dataseturl, null, sparqlupdate2);	

	}
	
	private List<String[]> queryEndPointDataset(String querycontext) {
		String resultfromrdf4j = KnowledgeBaseClient.query(dataseturl, null, querycontext);
		String[] keys = JenaResultSetFormatter.getKeys(resultfromrdf4j);
		List<String[]> listmap = JenaResultSetFormatter.convertToListofStringArrays(resultfromrdf4j, keys);
		return listmap;
	}
	/** calls on data via REST POST request to api aqmesh
	 * Handshake: HTTP POST request is sent first with the username and password. 
	 * User gets back a token that lasts 120 minutes 
	 * DO NOT RUN THIS MORE THAN ONCE A MINUTE OR WE'LL GET INTO TROUBLE! 14/5/20
	 * User sends the token and requests for information via GET. 
	 * First get request gets the gas concentration, 
	 * Second get request gets particle concentration
	 * @return ArrayList<JSONObject> that contains concentrations
	 */
	public ArrayList<JSONObject> getDataFromAPI() {
		//Get token information by password. Only valid for 120 min
		HttpResponse<String> response = Unirest.post("https://api.aqmeshdata.net/api/Authenticate")
				.header("Content-Type", "application/json")
				.body("{\"username\":\"Cares1\",\"password\":\"Cares1Pa55word#\"}\r\n").asString();
		String tokenPhrase = response.getBody();

		JSONObject jsonToken = new JSONObject(tokenPhrase);
		String currenttoken = jsonToken.getString("token");
		currenttoken = "Bearer "+ currenttoken; //Bearer is needed in the keyword. 
		
		//Get pod information using the token
		String responsepod = Unirest.get("https://api.aqmeshdata.net/api/Pods/Assets")
				.header("Authorization", currenttoken).asString().getBody();
		
		//Get Gas and temperature measurement data using the token
		String responseGas = Unirest.get("https://api.aqmeshdata.net/api/LocationData/Repeat/1740/1/01")
					      .header("Accept", "application/json")
					      .header("Authorization", currenttoken).asString().getBody();
		JSONArray jArr = new JSONArray(responseGas);
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<JSONObject> arrJo = new ArrayList<JSONObject>();
		if (jArr != null) {
			for (int i = 1; i< jArr.length(); i++) {
				JSONObject joGas = new JSONObject(jArr.get(i).toString());//{}
				JSONObject jo = new JSONObject();			
				jo.put("CO", joGas.getDouble("co_prescaled"));
				jo.put("NO", joGas.getDouble("no_prescaled"));
				jo.put("NO2", joGas.getDouble("no2_prescaled"));
				jo.put("NOx", Double.valueOf( joGas.getDouble("no2_prescaled")+joGas.getDouble("no_prescaled")));
				jo.put("SO2", joGas.getDouble("so2_prescaled"));
				jo.put("O3", joGas.getDouble("o3_prescaled"));
				arrJo.add(jo);
			}
		}
		//Get PM measurement data using the token
		String responsePM = Unirest.get("https://api.aqmeshdata.net/api/LocationData/Repeat/1740/2/01/1")
			      .header("Accept", "application/json")
			      .header("Authorization", currenttoken).asString().getBody();
		Unirest.shutDown();
		jArr = new JSONArray(responsePM);
		for (int i = 0; i< jArr.length(); i++) {
			JSONObject joPM = new JSONObject(jArr.get(i).toString());//{}
			JSONObject jo = new JSONObject();			
			jo.put("PM1", joPM.getDouble("pm1_prescale"));
			jo.put("PM2.5", joPM.getDouble("pm2_5_prescale"));
			jo.put("PM10", joPM.getDouble("pm10_prescale"));
			//gather the json from here. 
			jo.put("Timestamp", convertTime(joPM.getString("reading_datestamp")));
			arrJo.add(jo);
		}
		return arrJo;
	}
	/** runs getDataFromAPI() and process the output
	 * 
	 * @param stationiri: the name of the station / graph/context where the owl files are stored under
	 */
	public void executePeriodicUpdate(String stationiri) {
		ArrayList<JSONObject> result=getDataFromAPI();
		int len = result.size()/2;
		for (int x = 0; x <10; x++) { //assuming same frequency of these two.
			

			JSONObject jGas = result.get(x);
			JSONObject jPM = result.get(x+len);
			double concpm10=0.0;
			double concpm25=0.0;
			double concpm1=0.0;
			String directorytime = (String) jPM.get("Timestamp");
			concpm1 = concpm1 + jPM.getDouble("PM1");
			concpm25 = concpm1 +concpm25 +jPM.getDouble("PM2.5");
			concpm10 = concpm25 +concpm10 + jPM.getDouble("PM10");
			updateRepoNewMethod(stationiri, "OutsidePM1Concentration",""+concpm1,""+concpm1,directorytime);
			updateRepoNewMethod(stationiri, "OutsidePM25Concentration",""+(concpm1+concpm25),""+(concpm1+concpm25),directorytime);
			updateRepoNewMethod(stationiri, "OutsidePM10Concentration",""+(concpm1+concpm25+concpm10),""+(concpm1+concpm25+concpm10),directorytime);
			
			 
			Iterator<String> keys = jGas.keys();
			while(keys.hasNext()) {
			    String key = keys.next();
		    	String classname = String.format("Outside%sConcentration", key);
		    	String value = jGas.get(key).toString();
		    	updateRepoNewMethod(stationiri, classname, value, value, directorytime);
			}
		}
		 logger.info("updates finished");
		//processed the input to have suitable format
		
		
		
	}
	/** converts anytime into Singapore timezone. 
	 * 
	 * @param current current time in any time zone
	 * @return time in "yyyy-MM-dd'T'HH:mm:ss" in GMT+8 format
	 */
	public static String convertTime(String current) {
		DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		   utcFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		   String result=current;
		try {
			  Date date = utcFormat.parse(current);
			   DateFormat pstFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			   pstFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));

			   System.out.println(pstFormat.format(date));
			   result=pstFormat.format(date)+"+08:00";
			   return result;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
		
	}
	
	
	public void updateRepoNewMethod(String context,String propnameclass, String scaledvalue,String prescaledvalue, String newtimestamp) {
		String sensorinfo = "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/ontosensor/OntoSensor.owl#> "
				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/process_control_equipment/measuring_instrument.owl#> "
				+ "PREFIX j6:<http://www.w3.org/2006/time#> " 
				+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
				+ "SELECT ?vprop ?proptime "
				+ "{graph "+"<"+context+">"
				+ "{ "
				+ " ?prop a j4:"+propnameclass+" ."
				+ " ?prop   j2:hasValue ?vprop ." 
				+ " ?vprop   j6:hasTime ?proptime ."
				+ " ?proptime   j6:inXSDDateTimeStamp ?proptimeval ."
				+ "}" 
				+ "}" 
				+ "ORDER BY ASC(?proptimeval)LIMIT1";
		
		List<String[]> keyvaluemapold =queryEndPointDataset(sensorinfo);
		
		String sparqlupdate = "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/ontosensor/OntoSensor.owl#> "
				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/process_control_equipment/measuring_instrument.owl#> "
				+ "PREFIX j6:<http://www.w3.org/2006/time#> " 
				+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
				+ "WITH <" + context + ">"
				+ "DELETE { "
				+ "<" + keyvaluemapold.get(0)[0]+ "> j4:scaledNumValue ?oldpropertydata ."
				+ "<" + keyvaluemapold.get(0)[0]+ "> j4:prescaledNumValue ?oldpropertydata2 ."
				+ "<" + keyvaluemapold.get(0)[1]+ "> j6:inXSDDateTimeStamp ?olddatatime ."
				+ "} "
				+ "INSERT {"
				+ "<" + keyvaluemapold.get(0)[0]+ "> j4:scaledNumValue \""+scaledvalue+"\"^^xsd:double ."
				+ "<" + keyvaluemapold.get(0)[0]+ "> j4:prescaledNumValue \""+prescaledvalue+"\"^^xsd:double ."
				+ "<" + keyvaluemapold.get(0)[1]+ "> j6:inXSDDateTimeStamp \""+newtimestamp+"\" ."  
				+ "} "
				+ "WHERE { "
				+ "<" + keyvaluemapold.get(0)[0]+ "> j4:scaledNumValue ?oldpropertydata ."	
				+ "<" + keyvaluemapold.get(0)[0]+ "> j4:prescaledNumValue ?oldpropertydata2 ."	
				+ "<" + keyvaluemapold.get(0)[1]+ "> j6:inXSDDateTimeStamp ?olddatatime ."
				+ "}";
		
			
			KnowledgeBaseClient.update(dataseturl, null, sparqlupdate); //update the dataset

		
		
	}
	/** Things to do when reseting. 
	 * 1. create repository named airqualitystation on rdf4j
	 *  Ensure that you have "Copy of aqmesh-location-data-Cares1-20200420020751.csv" and 
	 *   "SensorTemp.owl" (aka the blank copy)in your workingdir folder
	 * 2. create owl files by uncomment Line 180 of AirSensorKBCreator.java, and commenting Line 179 . 
	 * 3. Uncomment Line 9 - 12 and comment line 4 - 7 of AirSensorConfig.java
	 * 4. Run main function in AirSensorKBCreator
	 * 5. Ensure that your KB files are created. 
	 * 6. Run this reset function. 
	 */
	public static void resetAllAQMesh(String location, String cityiri) {
		RepositoryConnection con = repo.getConnection();
		AirQualitySensorAgent a=new AirQualitySensorAgent();
		//Uploads the owl files onto your rdf4j dataset
		a.resetRepoTrial(con,location); //currently the context is not used
		int numbersensor=1; 
		//there's only one sensor so far. 
		//
		for(int x=1;x<=numbersensor;x++) {
			String index="0"+x;
			if(x<10) {
				index="00"+x;
			}
			String name="AQMeshSensor-001";
			String context="http://www.theworldavatar.com/kb/sgp/singapore/AirQualityStationAQMesh-"+index+".owl#AirQualityStationAQMesh-"+index;
			List<String>info= new ArrayList<String>();
			info.add(cityiri);
			info.add(name);
			info.add("0"); //overallpsi
			String locationname="Location 2450495"; //(for AQMESH)
			String serialnumber="2450495";
			info.add(locationname);// (for AQMESH)
			info.add(serialnumber);
			a.insertDataRepoContext(info,context);
		
		}
	}
	/** run this ONLY after you run resetAll
	 * In TESTING mode: 
	 * 1. run uploadData
	 * 2. Ensure that extractAvailableContext works by going to localhost:8080/rdf4j-workbench
	 * and checking airqualitystation (no s!) is available
	 * 3. run testAPIClear() in AirQualitySensorAgentTest to check that the API is working. Contact Yichen if otherwise
	 * 4. run testCallAPI() if (3) is green but you don't get any data. 
	 * In Running mode:
	 * 1. swap getDataFromAPI()'s two API get requests from "Repeat" to "Next"
	 * 
	 */
	public static String uploadData(String cityiri) {
		AirQualitySensorAgent a=new AirQualitySensorAgent();
		List<String[]> contextlist=a.extractAvailableContext( cityiri);
		String context=contextlist.get(0)[0];
		a.executePeriodicUpdate(context);
		System.out.println("update is done");
		return context;
	}
	public static void main(String[]args) { //used for upload all content locally
//		resetAllAQMesh("singapore_AQ", "http://dbpedia.org/resource/Singapore");
		uploadData("http://dbpedia.org/resource/Singapore");
		
	}
	
}



