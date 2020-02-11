package uk.ac.cam.cares.jps.des;

import java.io.FileNotFoundException;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.query.QueryBroker;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
import uk.ac.cam.cares.jps.base.util.MatrixConverter;
@WebServlet(urlPatterns = {"/GetForecastData" })
public class ForecastAgent extends JPSHttpServlet{
	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(WeatherIrradiationRetriever.class);
	protected void doGetJPS(HttpServletRequest request, HttpServletResponse res) {
		JSONObject jo = AgentCaller.readJsonParameter(request);

		String baseUrl = jo.optString("baseUrl",  QueryBroker.getLocalDataPath()+"/JPS_DES");
		
		JSONObject result=new JSONObject();
		try {
			result = forecastNextDay(baseUrl);
			readandwriteToFile(baseUrl);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AgentCaller.printToResponse(result, res);
 
		logger.info("return the result from forecast agent");		
	}
	private JSONObject forecastNextDay(String folder) throws InterruptedException, FileNotFoundException{
		new DistributedEnergySystem().copyFromPython(folder, "runpyforecast.bat");
		new DistributedEnergySystem().copyFromPython(folder,"scrapy.py");
		String startbatCommand =folder+"/runpyforecast.bat";
		System.out.println(startbatCommand);
		String resultpy= new DistributedEnergySystem().executeSingleCommand(folder,startbatCommand);
		String jsonres=new QueryBroker().readFileLocal(folder+"/WeatherForecast.json");
		//WeatherForecast.json may not be created if we run more than 10 times a day. 
		JSONObject current= new JSONObject(jsonres);
		return current;
	}
	private static void readandwriteToFile(String folder) throws Exception { //initializing the forecast sensors. 
		String csv=new QueryBroker().readFileLocal(folder+"/WeatherForecast.csv");
		List<String[]> readingFromCSV = MatrixConverter.fromCsvToArray(csv);
		//String baseURL2 = AgentLocator.getCurrentJpsAppDirectory(this) + "/workingdir/";
		WeatherTimeStampKB converter = new WeatherTimeStampKB();
		converter.startConversionForecast(readingFromCSV,"temperature");
		converter.startConversionForecast(readingFromCSV,"irradiation");
		converter.startConversionForecast(readingFromCSV,"windspeed");
	}
	public static void main (String[] args) throws Exception {
		readandwriteToFile("C:\\Users\\LONG01\\JParkSimulator-git\\JPS_DES\\python");
	}
	
	
	
}
