package uk.ac.cam.cares.jps.agent.weather;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * set value of apikey and comment @Disabled to run this
 * @author Kok Foong Lee
 *
 */
@Disabled
public class WeatherAPIConnectorTest {
	@Test
    public void testAPI() {
        String apikey = "REPLACE WITH API KEY";
    	WeatherAPIConnector.getWeatherDataFromOpenWeather(0, 0, apikey);
    }
}
