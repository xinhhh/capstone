package uk.ac.cam.cares.jps.powsys.electricalnetwork.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.jena.atlas.json.JsonException;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.ontology.OntModel;
import org.json.JSONObject;

import junit.framework.TestCase;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.scenario.BucketHelper;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
import uk.ac.cam.cares.jps.powsys.electricalnetwork.ENAgent;
import uk.ac.cam.cares.jps.powsys.envisualization.ENVisualization;
import uk.ac.cam.cares.jps.powsys.envisualization.ENVisualization.StaticobjectgenClass;
import uk.ac.cam.cares.jps.powsys.envisualization.MapPoint;

public class TestENVisualization extends TestCase {

	public void testcreateKML() throws TransformerException {
		ENVisualization a=new ENVisualization();
		OntModel model = ENAgent.readModelGreedy("http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork");
		List<String[]> generators=a.queryElementCoordinate(model, "PowerGenerator");
	
		//-----------------------------
		ArrayList<ENVisualization.StaticobjectgenClass> gensmerged = new ArrayList<ENVisualization.StaticobjectgenClass>();
		ArrayList<String> coorddata = new ArrayList<String>();
		for (int e = 0; e < generators.size(); e++) {
			StaticobjectgenClass gh = a.new StaticobjectgenClass();
			gh.setnamegen("/" + generators.get(e)[0].split("#")[1] + ".owl");
			gh.setx(generators.get(e)[1]);
			gh.sety(generators.get(e)[2]);
			System.out.println("/" + generators.get(e)[0].split("#")[1] + ".owl");

			if (coorddata.contains(gh.getx()) && coorddata.contains(gh.gety())) {
				int index = coorddata.indexOf(gh.getx()) / 2;
				gensmerged.get(index).setnamegen(gensmerged.get(index).getnamegen() + gh.getnamegen());
			} else {
				gensmerged.add(gh);
				coorddata.add(generators.get(e)[1]);
				coorddata.add(generators.get(e)[2]);
			}

		}		
		
		for(int g=0;g<gensmerged.size();g++) {
			MapPoint c= new MapPoint(Double.valueOf(gensmerged.get(g).gety()),Double.valueOf(gensmerged.get(g).getx()),0.0,gensmerged.get(g).getnamegen());
			a.addMark(c,"generator");
		}
		
		//--------------------------------
		
		List<String[]> bus=a.queryElementCoordinate(model, "BusNode");
		int size2=bus.size();
		for(int g=0;g<size2;g++) {
		MapPoint c= new MapPoint(Double.valueOf(bus.get(g)[2]),Double.valueOf(bus.get(g)[1]),0.0,"/"+bus.get(g)[0].split("#")[1]+".owl");
		a.addMark(c,"bus");
		}
		
		String res=a.writeFiletoString();
		
	}
	
	public void testcreateLineJS() throws IOException {
		ENVisualization a=new ENVisualization();
		OntModel model = ENAgent.readModelGreedy("http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork");
		String res=a.createLineJS(model);
		System.out.println("resultjs= "+res);
	}
	public void testcreateMarkers() throws IOException {
		ENVisualization a=new ENVisualization();
		OntModel model = ENAgent.readModelGreedy("http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork");
		String res=a.createMarkers("BASE",model);
		List<String> myList = new ArrayList<String>(Arrays.asList(res.split(",")));
		System.out.println(myList.get(2));//vemission
		System.out.println(myList.get(3));//fueltype
		System.out.println(myList.get(4));//name
		String flag = "testPOWSYSNuclearStartSimulationAndProcessResultAgentCallForTestScenario";
		String scenarioUrl = BucketHelper.getScenarioUrl(flag); 
		JPSHttpServlet.enableScenario(scenarioUrl);	
		model = ENAgent.readModelGreedy("http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork");
		
		res= a.createMarkers(flag,model);
		myList = new ArrayList<String>(Arrays.asList(res.split(",")));
		System.out.println(myList.get(2));//vemission
		System.out.println(myList.get(3));//fueltype
		System.out.println(myList.get(4));//name
		
	}
	public void testcallVisualizationLineJS() throws IOException  {

		JSONObject jo = new JSONObject();
		
		jo.put("electricalnetwork", "http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork");
		jo.put("flag", "testPOWSYSNuclearStartSimulationAndProcessResultAgentCallForTestScenario");
		String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_POWSYS/ENVisualization/createLineJS", jo.toString());
		jo = new JSONObject();
		jo.put("electricalnetwork", "http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork");
		jo.put("flag", "BASE");
		resultStart = AgentCaller.executeGetWithJsonParameter("JPS_POWSYS/ENVisualization/createLineJS", jo.toString());
		System.out.println("resultStart= "+resultStart);
	}
	public static void testcallVisualizationMarker() throws IOException  {

		JSONObject jo = new JSONObject();
		jo.put("electricalnetwork","http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork");
//		jo.put("flag", "testPOWSYSNuclearStartSimulationAndProcessResultAgentCallForTestScenario");
		jo.put("flag","BASE");
//		JSONObject jo2 = new JSONObject();
//		jo2.put("jpscontext", jo);
		String iriofnetwork = jo.getString("electricalnetwork");
		String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_POWSYS/ENVisualization/createMarkers", jo.toString());
		System.out.println("resultStart= "+resultStart);
	}
	public void testcallVisualizationKML() throws IOException  {

		JSONObject jo = new JSONObject();
		
		jo.put("electricalnetwork", "http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork");
		jo.put("n", "1505");
		jo.put("flag", "testPOWSYSNuclearStartSimulationAndProcessResultAgentCallForTestScenario");
		
		String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_POWSYS/ENVisualization/createKMLFile", jo.toString());
		System.out.println("resultStart= "+resultStart);
	}
	
	
	public void testKMLFile() {
		
		String url = "http://www.theworldavatar.com/OntoEN/test2.kml";
		
		String result = AgentCaller.executeGetWithURL(url);
		
		System.out.println(result);
		
	}
	
	
	
}
