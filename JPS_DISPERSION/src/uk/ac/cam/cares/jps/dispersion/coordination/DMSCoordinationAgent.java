package uk.ac.cam.cares.jps.dispersion.coordination;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.servlet.annotation.WebServlet;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cares.jps.base.config.IKeys;
import uk.ac.cam.cares.jps.base.config.KeyValueManager;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;

@WebServlet("/DMSCoordinationAgent")
public class DMSCoordinationAgent extends JPSHttpServlet {

	Logger logger = LoggerFactory.getLogger(DMSCoordinationAgent.class);
	private static final String PARAM_KEY_SHIP = "ship";

	private JSONArray getNewWasteAsync(String reactionMechanism, JSONObject jsonShip) {
		JSONArray newwaste = new JSONArray();
		ArrayList<CompletableFuture> wastes = new ArrayList<>();
		JSONArray ships = jsonShip.getJSONObject("collection").getJSONArray("items");
		int sizeofshipselected = ships.length();

		for (int i = 0; i < sizeofshipselected; i++) {
			logger.info("Ship AGENT called: " + i);
			JSONObject jsonReactionShip = new JSONObject();
			jsonReactionShip.put("reactionmechanism", reactionMechanism);
			jsonReactionShip.put("ship", ships.getJSONObject(i));

			CompletableFuture<String> getAsync = CompletableFuture
					.supplyAsync(() -> execute("/JPS_SHIP/ShipAgent", jsonReactionShip.toString()));

			CompletableFuture<String> processAsync = getAsync
					.thenApply(wasteResult -> new JSONObject(wasteResult).getString("waste"));
			wastes.add(processAsync);
		}

		for (CompletableFuture waste : wastes) {
			try {
				newwaste.put(waste.get());
			} catch (InterruptedException | ExecutionException e) {
				throw new JPSRuntimeException(e.getMessage());
			}
		}

		return newwaste;
	}

	@Override
	protected JSONObject processRequestParameters(JSONObject requestParams) {

		String regionToCityResult = execute("/JPS/RegionToCity", requestParams.toString());
		String city = new JSONObject(regionToCityResult).getString("city");
		requestParams.put("city", city);
		logger.info("city FROM COORDINATION AGENT: " + city);
		logger.info("overall json= " + requestParams.toString());

		String result = execute("/JPS/GetBuildingListFromRegion", requestParams.toString());
		JSONArray building = new JSONObject(result).getJSONArray("building");
		requestParams.put("building", building);
		logger.info("building FROM COORDINATION AGENT: " + building.toString());

		result = execute("/JPS_DISPERSION/SensorWeatherAgent", requestParams.toString());
		JSONArray stationiri = new JSONObject(result).getJSONArray("stationiri");
		requestParams.put("stationiri", stationiri);
		
		if (city.toLowerCase().contains("kong") || city.toLowerCase().contains("singapore")) {

			//=======================================================================
			logger.info("calling postgres= " + requestParams.toString());
			String url = KeyValueManager.get(IKeys.URL_POSITIONQUERY);
			url += "/getEntitiesWithinRegion";
			String resultship = AgentCaller.executeGetWithURLAndJSON(url, requestParams.toString());

			JSONObject jsonShip = new JSONObject(resultship);
			requestParams.put(PARAM_KEY_SHIP, jsonShip);

			if (((JSONArray) ((JSONObject) jsonShip.get("collection")).get("items")).length() != 0) {
				String reactionMechanism = requestParams.optString("reactionmechanism");
				JSONArray newwaste;

				newwaste = getNewWasteAsync(reactionMechanism, jsonShip);

				requestParams.put("waste", newwaste);
			}
			
		} else {
			//=======================================================================
			String wasteresult = execute("/JPS/PowerPlant", requestParams.toString());
			String waste = new JSONObject(wasteresult).getString("waste");
			requestParams.put("waste", waste);
		}

        result = execute("/JPS_DISPERSION/DispersionModellingAgent", requestParams.toString(), HttpPost.METHOD_NAME);
        String folder = new JSONObject(result).getString("folder");
        requestParams.put("folder", folder);
//----------------------------------------------------------------------------       
        
		return requestParams;

	}
}
