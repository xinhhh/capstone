package uk.ac.cam.cares.jps.powsys.coordination;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.query.JenaHelper;
import uk.ac.cam.cares.jps.base.query.JenaResultSetFormatter;
import uk.ac.cam.cares.jps.base.query.QueryBroker;
import uk.ac.cam.cares.jps.base.query.sparql.JenaModelWrapper;
import uk.ac.cam.cares.jps.base.query.sparql.Paths;
import uk.ac.cam.cares.jps.base.query.sparql.PrefixToUrlMap;
import uk.ac.cam.cares.jps.base.query.sparql.Prefixes;
import uk.ac.cam.cares.jps.base.query.sparql.QueryBuilder;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
import uk.ac.cam.cares.jps.base.scenario.ScenarioClient;
import uk.ac.cam.cares.jps.base.util.FileUtil;
import uk.ac.cam.cares.jps.powsys.electricalnetwork.ENAgent;
import uk.ac.cam.cares.jps.powsys.util.Util;

@WebServlet(urlPatterns = { "/startcombinedsimulationPF", "/startcombinedsimulationOPF" })
public class CoordinationAgent extends JPSHttpServlet implements Prefixes, Paths {

	private static final long serialVersionUID = 6859324316966357379L;
	private Logger logger = LoggerFactory.getLogger(CoordinationAgent.class);

	@Override
	protected void doGetJPS(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	
		JSONObject jo = AgentCaller.readJsonParameter(request);
		//String electricalNetwork = jo.getString("electricalnetwork");
		String mergeScenarioUrl = jo.getString("mergescenariourl");		
		
		String path = request.getServletPath();

		String modeltype = null;
		if ("/startcombinedsimulationPF".equals(path)) {
			modeltype = "PF";
		} else if ("/startcombinedsimulationOPF".equals(path)) {
			modeltype = "OPF";
		}
		
		coordinate(mergeScenarioUrl, jo, modeltype);
	}
	
	/**
	 * @param scenarioUrlOfMockedAgent scenario url with the result of the nuclear power plant simulation (including OWL files for nuclear power plants and generators)
	 * @param input
	 * @param modeltype "PF" (power flow) or "OPF" (optimal power flow)
	 */
	public void coordinate(String scenarioUrlOfMockedAgent, JSONObject input, String modeltype) {
		
		String electricalNetwork = input.getString("electricalnetwork");
		
		// the hasSubsystem triples of the electrical network top node itself are not part of the model
		OntModel model = ENAgent.readModelGreedy(electricalNetwork);
		
		List<BusInfo> buses = queryBuses(model);
		
		BusInfo slackBus = findFirstSlackBus(buses);		
				
		deletePowerGeneratorsFromElectricalNetwork(model, electricalNetwork, slackBus);
		
		List<String> plants = getNuclearPowerPlantsFromMockedScenarioAgent(scenarioUrlOfMockedAgent);
		
		completeNuclearPowerGenerators(scenarioUrlOfMockedAgent, plants);
		
		List<GeneratorInfo> newGenerators = queryGenerators(scenarioUrlOfMockedAgent, plants);

		addNuclearPowerGeneratorsToElectricalNetwork(electricalNetwork, newGenerators);
		
		initVoltageMagnitudeInPUForBuses(buses);
		
		connectNuclearPowerGeneratorsToOptimalBus(buses, newGenerators, slackBus);
			
		startENSimulation(input, modeltype);
	}
	
	private void startENSimulation(JSONObject input, String modeltype) {
		
		// direct call of EN agent for debugging on local computer
//		try {
//		    String baseUrl = QueryBroker.getLocalDataPath() + "/JPS_POWSYS_EN";
//			new ENAgent().startSimulation(electricalNetwork, baseUrl, modeltype);
//			return "";
//		} catch (IOException e) {
//			throw new JPSRuntimeException(e.getMessage(), e);
//		}
		
		String path = "JPS_POWSYS/ENAgent/startsimulationPF";
		if ("OPF".equals(modeltype)) {
			path = "JPS_POWSYS/ENAgent/startsimulationOPF";
		}
		AgentCaller.executeGetWithJsonParameter(path, input.toString());
	}
	
	private BusInfo findFirstSlackBus(List<BusInfo> buses) {
		BusInfo slackBus = null;
		for (BusInfo current : buses) {
			String busType = current.busType;
			if ("3".equals(busType)) {
				logger.info("slack bus was found: busnumber = " + current.busNumber + ", busiri = " + current.busIri);
				slackBus = current;
				break;
			}
		}
		if (slackBus == null) {
			throw new JPSRuntimeException("No slack bus was found in the electrical network. The optimization was aborted.");
		}
		
		return slackBus;
	}
	
	public List<String> completeNuclearPowerGenerators(String scenarioUrlOfMockedAgent, List<String> nuclearPowerPlants) {
		
		List<String> result = new ArrayList<String>();
		
		String sparqlQuery = getQueryForGeneratorsInNuclearPlant();
		
		for (String current : nuclearPowerPlants) {
			String queryResult = new ScenarioClient().query(scenarioUrlOfMockedAgent, current, sparqlQuery);		
			List<String[]> list = JenaResultSetFormatter.convertToListofStringArrays(queryResult, "generator");
			
			for (String[] currentRow : list) {
				String generator = currentRow[0];
				result.add(generator);
				
				URI uri = new ScenarioClient().getReadUrl(scenarioUrlOfMockedAgent, generator);
				OntModel model = JenaHelper.createModel();
				try {
					URL url = uri.toURL();
					JenaHelper.readFromUrl(url, model);
				} catch (MalformedURLException e) {
					new JPSRuntimeException(e.getMessage(), e);
				}
				
				completePowerGenerator(model, generator);
			}
		}		
		
		return result;
	}
	
	public String getQueryForGeneratorsInNuclearPlant() {
		QueryBuilder builder = new QueryBuilder();
		builder.select("?generator");
		builder.a("?entity", OPSREAL, "NuclearPlant");
		builder.prop("?entity", "?generator", OCPSYST, "hasSubsystem");
		return builder.build().toString();
	}
	
	public List<String> getNuclearPowerPlantsFromMockedScenarioAgent(String scenarioUrlOfMockedAgent) {
		
		List<String> plantList = new ArrayList<String>();
		JSONObject jo = new JSONObject();
		String result = new ScenarioClient().mockedOperation(scenarioUrlOfMockedAgent, "/processresult", jo.toString());
		logger.info("result from mocked scenario agent = " + result);
		JSONArray jaResult = new JSONObject(result).getJSONArray("plantirilist");
		for (int i=0; i<jaResult.length(); i++) {
			plantList.add(jaResult.getString(i));
		}
		
		return plantList;
	}
	
	public void completePowerGenerator(OntModel model, String powerGenerator) {
		
		logger.info("adding additional attributes to nuclear power generator = " + powerGenerator);

		JenaModelWrapper w = new JenaModelWrapper(model, null);
		
		// read some values from the original OWL file
		RDFNode o = w.getPropertyValue(powerGenerator, PGISCOORDX);
		double x = o.asLiteral().getDouble();
		o = w.getPropertyValue(powerGenerator, PGISCOORDY);
		double y = o.asLiteral().getDouble();
		
		String pgIri = PrefixToUrlMap.getPrefixUrl(OPSMODE) + "Pg";
		String[] pathPg = new String[] {OCPSYST, "isModeledBy", OCPMATH, "hasModelVariable", pgIri, OCPSYST, "hasValue", OCPSYST, "numericalValue"};
		o = w.getPropertyValue(powerGenerator, pathPg);
		double pgValue = o.asLiteral().getDouble();
		
		// replace the template IRI by the IRI of the power generator
		String path = Util.getResourceDir(this) + "/EGen-001_template.owl";
		String content = FileUtil.readFileLocally(path);
		String templateIRI = "http://www.theworldavatar.com/EGen-001_template.owl";
		content = content.replace(templateIRI + "#EGen-001", powerGenerator);
		String powerGeneratorWithoutFragment = powerGenerator;
		int i = powerGenerator.indexOf("#");
		if (i > 0) {
			powerGeneratorWithoutFragment = powerGenerator.substring(0, i);
		}
		content = content.replace(templateIRI, powerGeneratorWithoutFragment);
		
		// read the template into a model and update its values with those from the original OWL file
		model = JenaHelper.createModel();
		JenaHelper.readFromString(content, model);
		w = new JenaModelWrapper(model, null);
				
		w.setPropertyValue(powerGenerator, x, PGISCOORDX);
		w.setPropertyValue(powerGenerator, y, PGISCOORDY);
		w.setPropertyValue(powerGenerator, pgValue, pathPg);
			
		// overwrite the original OWL file
		content = JenaHelper.writeToString(model);
		new QueryBroker().put(powerGenerator, content);
	}
	
	public void deletePowerGeneratorsFromElectricalNetwork(OntModel model, String electricalNetwork, BusInfo slackBus) {
				
		String query = getQueryForPowerGenerators();
		ResultSet resultSet = JenaHelper.query(model, query);
		String result = JenaResultSetFormatter.convertToJSONW3CStandard(resultSet);
		List<String[]> resultList = JenaResultSetFormatter.convertToListofStringArrays(result,  "entity", "busnumber", "busnumbervalue");
		
		StringBuffer delete = new StringBuffer("PREFIX OCPSYST:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> \r\n");
		delete.append("DELETE DATA { \r\n");
		for (String[] current : resultList) {
			String generator = current[0];
			String busNumberValue = current[2];
			if (slackBus.busNumber.equals(busNumberValue)) {
				logger.info("generator is connected to the slack bus and won't be deleted, generator = " + generator);	
			} else {
				delete.append("<" + electricalNetwork + "> OCPSYST:hasSubsystem <" + generator + "> . \r\n");
			}
		}
		delete.append("} \r\n");		
		logger.info("deleting current power generators from electrical network top node\n" + delete.toString());

		new QueryBroker().updateFile(electricalNetwork, delete.toString());
	}
	
	public String getQueryForPowerGenerators() {
		QueryBuilder builder = new QueryBuilder();
		builder.select("?entity", "?busnumber", "?busnumbervalue");
		builder.a("?entity", OPSREAL, "PowerGenerator");
		builder.a("?busnumber", OPSMODE, "BusNumber");
		builder.prop("?entity", "?busnumber", OCPSYST, "isModeledBy", OCPMATH, "hasModelVariable");
		builder.prop("?busnumber", "?busnumbervalue", PVALNUMVAL);
		
		return builder.build().toString();
	}
	
	public void addNuclearPowerGeneratorsToElectricalNetwork(String electricalNetwork, List<GeneratorInfo> generators) {
		
		StringBuffer insert = new StringBuffer("PREFIX OCPSYST:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> \r\n");
		insert.append("INSERT DATA { \r\n");
		for (GeneratorInfo current : generators) {
			insert.append("<" + electricalNetwork + "> OCPSYST:hasSubsystem <" + current.generatorIri + "> . \r\n");
		}
		insert.append("} \r\n");		
		logger.info("adding nuclear power generators to electrical network top node\n" + insert.toString());

		new QueryBroker().updateFile(electricalNetwork, insert.toString());
	}
	
	public void connectNuclearPowerGeneratorsToOptimalBus(List<BusInfo> buses, List<GeneratorInfo> generators, BusInfo slackBus) {
		
		logger.info("connecting generators to optimal buses, number of buses = " + buses.size() + ", number of generators = " + generators.size());
						
		// find the closest bus under the following constraints:
		// 1. The closest bus must not be the slack bus
		// 2. The closest bus must have baseKV around 230 kV (say plus minus 1%)
		double baseKVmin = 230 * 0.99;
		double baseKVmax = 230 * 1.01;
		for (GeneratorInfo current : generators) {
			
			//System.out.println("searching optimal bus for generator = " + current.generatorIri + ", x = " + current.x + ", y = " + current.y + ", current bus number instance = " + current.busNumberIri);
					
			// calculate the distance of the current generator to the closest bus
			double distanceToClosestBus = -1;
			BusInfo closestBus = null;
			for (BusInfo currentBus : buses) {
				double dist = distance(current.x, current.y, currentBus.x, currentBus.y);
				boolean isSlackBus = currentBus.busIri.equals(slackBus.busIri);
				boolean isWithin230KVrange = ((currentBus.baseKV >= baseKVmin) && (currentBus.baseKV <= baseKVmax));
				if ( (!isSlackBus) && isWithin230KVrange && (closestBus == null || (dist < distanceToClosestBus))) {
					distanceToClosestBus = dist;
					closestBus = currentBus;
				}
			} 
			
			if (closestBus == null) {
				throw new JPSRuntimeException("no optimal bus was found for generator = " + current.generatorIri);
			}
			
			current.closestBusNumber = closestBus.busNumber;
			current.distanceToClosestBus = distanceToClosestBus;
		}
				
		// collect all generators that belong to the same power plant
		Map<String, List<GeneratorInfo>> mapFromPlantToGenerators = new HashMap<String, List<GeneratorInfo>>();
		for (GeneratorInfo current : generators) {
			List<GeneratorInfo> list = mapFromPlantToGenerators.get(current.plantIri);
			if (list == null) {
				list = new ArrayList<GeneratorInfo>();
				mapFromPlantToGenerators.put(current.plantIri, list);
			}
			list.add(current);
		}
		
		// connect all generators which belong to the same power plant to the same bus
		for (String currentPlant : mapFromPlantToGenerators.keySet()) {
			
			List<GeneratorInfo> list = mapFromPlantToGenerators.get(currentPlant);
			
			// find the closest bus among all generators from the same plant
			String busNumber = null;
			double minimalDistance = -1;
			for (GeneratorInfo currentGenerator : list) {
				if ((busNumber == null) || (currentGenerator.distanceToClosestBus < minimalDistance)) {
					busNumber = currentGenerator.closestBusNumber;
					minimalDistance = currentGenerator.distanceToClosestBus;
				}
			}
			
			// finally, connect the generators to the buses in the OWL files
			for (GeneratorInfo currentGenerator : list) {
				logger.info("connecting generator to bus, generator = " + currentGenerator.generatorIri + ", bus number = " + busNumber);
				connectGeneratorToBus(currentGenerator.generatorIri, currentGenerator.busNumberIri, busNumber);
			}	
		}
	}
	
	private void connectGeneratorToBus(String generatorIri, String busNumberIri, String busNumber) {
		
		int busNumberValue = Integer.valueOf(busNumber);
		
		OntModel modelGen = JenaHelper.createModel(generatorIri);
		JenaModelWrapper w = new JenaModelWrapper(modelGen, null);
		w.setPropertyValue(busNumberIri, busNumberValue, PVALNUMVAL);
			
		// overwrite the original OWL file
		String content = JenaHelper.writeToString(modelGen);
		new QueryBroker().put(generatorIri, content);
	}
	
	private double distance(double x1, double y1, double x2, double y2) {
		// TODO-AE SC URGENT 20190429 distance according to WSG84 degree instead of meters?
		 double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
		 return distance;
	}
	
	private List<GeneratorInfo> queryGenerators(String scenarioUrlOfMockedAgent, List<String> nuclearPowerPlants) {
		
		List<GeneratorInfo> result = new ArrayList<GeneratorInfo>();
		
		String queryNuclearPowerPlant = getQueryForGeneratorsInNuclearPlant();
		String queryGenerator = getQueryForGenerator();
		QueryBroker broker = new QueryBroker();
		
		for (String currentPlant : nuclearPowerPlants) {
			String queryResult = new ScenarioClient().query(scenarioUrlOfMockedAgent, currentPlant, queryNuclearPowerPlant);		
			List<String[]> generators = JenaResultSetFormatter.convertToListofStringArrays(queryResult, "generator");
			
			for (String[] currentGen : generators) {
				String generatorIri = currentGen[0];
				GeneratorInfo info = new GeneratorInfo();
				info.generatorIri = generatorIri;
				info.plantIri = currentPlant;
				
				String resultGen = broker.queryFile(generatorIri, queryGenerator);
				List<String[]> resultGenAsList = JenaResultSetFormatter.convertToListofStringArrays(resultGen, "entity", "x", "y", "busnumber");
				info.x = Double.valueOf(resultGenAsList.get(0)[1]);
				info.y = Double.valueOf(resultGenAsList.get(0)[2]);
				info.busNumberIri = resultGenAsList.get(0)[3];
				
				result.add(info);
			}
		}		
		
		return result;
	}
	
	private String getQueryForGenerator() {
		QueryBuilder builder = new QueryBuilder();
		builder.select("?entity", "?x", "?y" , "?busnumber", "?busnumbervalue");
		builder.prop("?entity", "?x", PGISCOORDX);
		builder.prop("?entity", "?y", PGISCOORDY);
		builder.a("?busnumber", OPSMODE, "BusNumber");
		builder.prop("?entity", "?busnumber", OCPSYST, "isModeledBy", OCPMATH, "hasModelVariable");
		builder.prop("?busnumber", "?busnumbervalue", PVALNUMVAL);
		
		return builder.build().toString();
	}
	
	public String getQueryForBuses() {
		
		QueryBuilder builder = new QueryBuilder();
		builder.select("?entity", "?x", "?y", "?busnumber", "?busnumbervalue", "?bustypevalue", "?vm", "?vmvalue", "?baseKVvalue");
		builder.a("?entity", OPSREAL, "BusNode");
		builder.prop("?entity", "?x", PGISCOORDX);
		builder.prop("?entity", "?y", PGISCOORDY);
		builder.a("?busnumber", OPSMODE, "BusNumber");
		builder.prop("?entity", "?busnumber", OCPSYST, "isModeledBy", OCPMATH, "hasModelVariable");
		builder.prop("?busnumber", "?busnumbervalue", PVALNUMVAL);
		builder.a("?bustype", OPSMODE, "BusType");
		builder.prop("?entity", "?bustype", OCPSYST, "isModeledBy", OCPMATH, "hasModelVariable");
		builder.prop("?bustype", "?bustypevalue", PVALNUMVAL);
		builder.a("?vm", OPSMODE, "Vm");
		builder.prop("?entity", "?vm", OCPSYST, "isModeledBy", OCPMATH, "hasModelVariable");
		builder.prop("?vm", "?vmvalue", PVALNUMVAL);
		builder.a("?baseKV", OPSMODE, "baseKV");
		builder.prop("?entity", "?baseKV", OCPSYST, "isModeledBy", OCPMATH, "hasModelVariable");
		builder.prop("?baseKV", "?baseKVvalue", PVALNUMVAL);
		
		return builder.build().toString();
	}
	
	public List<BusInfo> queryBuses(OntModel model) {
		
		List<BusInfo> result = new ArrayList<BusInfo>();
		
		String queryBus = getQueryForBuses();
		ResultSet resultSet = JenaHelper.query(model, queryBus);
		String json = JenaResultSetFormatter.convertToJSONW3CStandard(resultSet);
		List<String[]> buses = JenaResultSetFormatter.convertToListofStringArrays(json,"entity", "x", "y", "busnumber", "busnumbervalue", "bustypevalue", "vm", "vmvalue", "baseKVvalue");
				
		for (String[] current : buses) {
			
			BusInfo info = new BusInfo();
			info.busIri = current[0];
			
			info.x = Double.valueOf(current[1]);
			info.y = Double.valueOf(current[2]);
			info.busNumberIri = current[3];
			info.busNumber = current[4];
			info.busType = current[5];
			info.voltageMagnitudeIri = current[6];
			info.voltageMagnitude = Double.valueOf(current[7]);
			info.baseKV = Double.valueOf(current[8]);
			
			result.add(info);
		}
		
		return result;
	}
	
	private void initVoltageMagnitudeInPUForBuses(List<BusInfo> buses) {
			
		logger.info("setting VM (Voltage Magnitude) of all busses to 1 PU, number of buses = " + buses.size());
		for (BusInfo current : buses) {
			
			OntModel modelGen = JenaHelper.createModel(current.busIri);
			JenaModelWrapper w = new JenaModelWrapper(modelGen, null);
			w.setPropertyValue(current.voltageMagnitudeIri, 1.0, PVALNUMVAL);
				
			// overwrite the original OWL file
			String content = JenaHelper.writeToString(modelGen);
			new QueryBroker().put(current.busIri, content);
		}	
	}
}
