package uk.ac.cam.cares.jps.agent.status.process;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONObject;
import uk.ac.cam.cares.jps.agent.status.TestHandler;
import uk.ac.cam.cares.jps.agent.status.TestRegistry;
import uk.ac.cam.cares.jps.agent.status.TestUtils;
import uk.ac.cam.cares.jps.agent.status.define.TestDefinition;
import uk.ac.cam.cares.jps.agent.status.define.TestType;
import uk.ac.cam.cares.jps.agent.status.execute.QueryStore;
import uk.ac.cam.cares.jps.agent.status.record.TestRecord;

/**
 * Handles generation of response for requests to view the main dashboard.
 *
 * @author Michael Hillman
 */
public class DashboardRequest extends StatusRequest {

    /**
     * Initialise a new DashboardRequest instance.
     *
     * @param request HTTP request.
     * @param response HTTP response.
     * @param handler Test handler instance.
     */
    public DashboardRequest(HttpServletRequest request, HttpServletResponse response, TestHandler handler) {
        super(request, response, handler);
    }

    /**
     * Act on the request, setting attributes and forwarding to the dashboard.jsp page.
     */
    @Override
    public void processRequest() throws ServletException, IOException {
        JSONObject parameters = super.parseRequestJSON();

        if (parameters != null && parameters.has("QUERY_FILE")) {
            LOGGER.info("Returning content of stored query file...");
            processQueryRequest(parameters);

        } else if (parameters != null && parameters.has("LOG")) {
            LOGGER.info("Returning log for a single test result...");
            processLogRequest(parameters);

        } else if (parameters != null && parameters.has("NAME") && parameters.has("TYPE")) {
            LOGGER.info("Showing page for single test...");
            processForSingleTest(parameters);
        } else {
            LOGGER.info("Showing main dashboard page...");
            processForAllTests();
        }
    }

    /**
     * Show the dashboard with a summary of all tests.
     *
     * @throws ServletException
     * @throws IOException
     */
    private void processForAllTests() throws ServletException, IOException {
        // A map all defined tests keyed by their type
        Map<TestType, Set<TestDefinition>> testDefinitions = new LinkedHashMap<>();
        for (TestType testType : TestRegistry.getDefinedTypes()) {
            testDefinitions.put(testType, TestRegistry.getDefinedTests(testType));
        }
        request.setAttribute("test-definitions", testDefinitions);

        // Latest results for each test definition
        Map<TestDefinition, TestRecord> latestRecords = new LinkedHashMap<>();

        Map<TestType, Integer> failureCounts = new LinkedHashMap<>();
        Map<TestType, Integer> successCounts = new LinkedHashMap<>();

        for (TestDefinition definition : TestRegistry.getDefinedTests()) {

            // Find latest record for this test.
            TestRecord latestRecord = handler.getRecordStore().getLatestRecord(definition);
            if (latestRecord != null) {

                // Store the record
                latestRecords.put(definition, latestRecord);

                // Count failure (if applicable)
                if (!failureCounts.containsKey(definition.getType())) {
                    failureCounts.put(definition.getType(), 0);
                }
                if (!latestRecord.getResult()) {
                    failureCounts.put(
                            definition.getType(),
                            failureCounts.get(definition.getType()) + 1
                    );
                }

                // Count success (if applicable)
                if (!successCounts.containsKey(definition.getType())) {
                    successCounts.put(definition.getType(), 0);
                }
                if (latestRecord.getResult()) {
                    successCounts.put(
                            definition.getType(),
                            successCounts.get(definition.getType()) + 1
                    );
                }
            } else {
                LOGGER.warn("Could not find a test record for the '" + definition.getName() + "' test.");
            }
        }
        request.setAttribute("test-results", latestRecords);
        request.setAttribute("test-failures", failureCounts);
        request.setAttribute("test-successes", successCounts);

        // Forward to the overall dashboard page
        RequestDispatcher rd = request.getRequestDispatcher("dashboard.jsp");
        response.setContentType("text/html;charset=UTF-8");
        rd.forward(request, response);
    }

    /**
     * Show the dashboard for a single test.
     *
     * @throws ServletException
     * @throws IOException
     */
    private void processForSingleTest(JSONObject parameters) throws ServletException, IOException {
        String testName = parameters.getString("NAME");
        request.setAttribute("test-name", testName);
        String testType = parameters.getString("TYPE");
        request.setAttribute("test-type", testType);

        int recordLimit = 14;
        if (parameters.has("LIMIT")) {
            recordLimit = parameters.getInt("LIMIT");
        }

        // Find the test definition
        TestDefinition definition = TestRegistry.getDefinedTest(testName, testType);
        if (definition == null) {
            LOGGER.error("Could not find TestDefinition instance for '" + testName + "' in '" + testType + "' type.");
            throw new ServletException("Could not find matching TestDefinition instance!");
        }

        // Set description for this test type
        request.setAttribute("test-description", TestUtils.getDescription(definition.getType()));
        
        StringBuilder testDetails = new StringBuilder();
        testDetails.append("name: ");
        testDetails.append(definition.getName());
        testDetails.append("<br>");
        testDetails.append("type: ");
        testDetails.append(definition.getType().toString());
        testDetails.append("<br>");

        for (Entry<String, String> entry : definition.getInputs().entrySet()) {
            testDetails.append(entry.getKey());
            testDetails.append(": ");
            testDetails.append(entry.getValue());
            testDetails.append("<br>");

            request.setAttribute(entry.getKey(), entry.getValue());
        }
        request.setAttribute("test-details", testDetails.toString());

        // Get the last recordLimit executions of this test
        List<TestRecord> records = handler.getRecordStore().getRecordsForTest(definition);
        LOGGER.info("Found " + records.size() + " records for this test.");
                
        if (records.size() > recordLimit) records = records.subList(0, recordLimit);
        request.setAttribute("test-records", records);

        // Forward to the individual results page
        RequestDispatcher rd = request.getRequestDispatcher("test-summary.jsp");
        response.setContentType("text/html;charset=UTF-8");
        rd.forward(request, response);
    }

    /**
     *
     * @param parameters
     * @throws ServletException
     * @throws IOException
     */
    private void processQueryRequest(JSONObject parameters) throws ServletException, IOException {
        String queryFile = parameters.getString("QUERY_FILE");
        String query = QueryStore.readQuery(queryFile);
        query = StringEscapeUtils.escapeHtml(query);
        
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");

        if (query != null && !query.isEmpty()) {
            query = query.replaceAll("\\n", "<br>");
            
            out.println("Showing content from stored query located at:");
            out.println("<br>");
            out.println("<span style='padding-left: 15px;'>~/.jps/queries/" + queryFile + "</span>");
            out.println("<br><br><hr><br>");
            out.print(query);
        } else {
            out.print("Error, could not find/read query file, contact administrators.");
        }
        out.close();
    }

    /**
     *
     * @param parameters
     * @throws ServletException
     * @throws IOException
     */
    private void processLogRequest(JSONObject parameters) throws ServletException, IOException {
        String testName = parameters.getString("NAME");
        String testType = parameters.getString("TYPE");
        String testTime = parameters.getString("TIME");

        if (!testTime.contains("_")) {
            testTime = testTime.replaceAll(" ", "_");
        }

        // Get the location of the log file
        Path logFile = Paths.get(
                System.getProperty("user.home"),
                ".jps",
                "logs",
                testType.toString(),
                testName,
                testTime + ".log"
        );
        LOGGER.info("Looking for log file at: " + logFile);

        // Read the log file and write back HTML
        PrintWriter out = response.getWriter();

        if (Files.exists(logFile)) {
            out.println("Showing content from log file located at:");
            out.println("<br>");
            out.println("<span style='padding-left: 15px;'>" + logFile.toString() + "</span>");
            out.println("<br><br><hr><br>");

            try (BufferedReader reader = new BufferedReader(new FileReader(logFile.toFile()))) {
                String line = null;

                while ((line = reader.readLine()) != null) {
                    out.println(highlightLine(line));
                    out.println("<br>");
                }
            } catch (IOException ioException) {
                LOGGER.error("Exception when reading log file!", ioException);
                out.println("Could not read log file due to server error, contact administrators.");
            }
        } else {
            out.println("Could not find corresponding log file for this execution, contact administrators.");
        }

        out.close();
        response.setContentType("text/html");
    }

    /**
     * @param line
     * @return
     */
    private String highlightLine(String line) {
        if (line.toLowerCase().contains("warning")) {
            return "<span style='background-color: #edba4d32;'>" + line + "</span>";
        }
        if (line.toLowerCase().contains("error")) {
            return "<span style='background-color: #ED4D6E32;'>" + line + "</span>";
        }
        if (line.toLowerCase().contains("success")) {
            return "<span style='background-color: #519E8A32;'>" + line + "</span>";
        }
        return line;
    }

    /**
     * Validate the request.
     *
     * @throws BadRequestException
     */
    @Override
    public void validateRequest() throws BadRequestException {
        // All requests are valid here.
    }

}
// End of class.
