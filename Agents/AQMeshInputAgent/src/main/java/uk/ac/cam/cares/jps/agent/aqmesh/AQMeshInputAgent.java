package uk.ac.cam.cares.jps.agent.aqmesh;

import org.json.JSONArray;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeries;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Class to retrieve data from the AQMesh API and storing it with connection to The World Avatar (Knowledge Base).
 * @author Niklas Kasenburg
 */
public class AQMeshInputAgent {

    private TimeSeriesClient<LocalDateTime> tsClient;

    public AQMeshInputAgent(String propertiesFile) throws IOException {
        // Initialize time series client
        tsClient = new TimeSeriesClient<>(LocalDateTime.class, propertiesFile);
    }

    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            throw new JPSRuntimeException("No properties file provided as command line argument.");
        }
        String propertiesFile = args[0];

        AQMeshInputAgent agent = new AQMeshInputAgent(propertiesFile);
        // Check whether time series already exists
        if (!agent.timeSeriesExists()) {
            agent.initializeTimeSeries();
        }

        AQMeshAPIConnector connector = new AQMeshAPIConnector(propertiesFile);
        connector.connect();
        agent.updateTimeSeries(connector);
    }

    private void updateTimeSeries(AQMeshAPIConnector connector) {
        try {
            updateParticleReadings(connector);
            updateGasReadings(connector);
        }
        catch (Exception e) {
        }
    }

    private void updateGasReadings(AQMeshAPIConnector connector) {
        JSONArray gasReadings = connector.getGasReadings();
    }

    private void updateParticleReadings(AQMeshAPIConnector connector) {
        JSONArray particleReadings = connector.getParticleReadings();
    }

    private boolean timeSeriesExists() {
        return true;
    }

    private void initializeTimeSeries() {
    }

}
