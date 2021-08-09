package uk.ac.cam.cares.jps.agent.email;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.cam.cares.jps.base.util.SysStreamHandler;

/**
 * Test logging functionality.
 *
 * @author Michael Hillman
 */
public class LoggingTest {

    /**
     * Logger for error output.
     */
    private static final Logger LOGGER = LogManager.getLogger(LoggingTest.class);

    /**
     * Checks to see that the root Log4J logger is configured as the properties files denotes.
     */
    @Test
    public void checkLogging() {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext();
        Assertions.assertNotNull(loggerContext, "Current LoggerContext is null.");
        
        Configuration loggerConfig = loggerContext.getConfiguration();
        Assertions.assertNotNull(loggerConfig, "Current configuration is null.");

        // Check for file appender
        Appender fileAppender = loggerConfig.getAppender("RollingFile");
        Assertions.assertNotNull(fileAppender, "Expected to find an appender named 'RollingFile'.");
        Assertions.assertTrue(fileAppender instanceof RollingFileAppender, "Expecting 'RollingFile' appender to be an instance of RollingFileAppender class.");

        // Check for console appender
        Appender consoleAppender = loggerConfig.getAppender("Console");
        Assertions.assertNotNull(consoleAppender, "Expected to find an appender named 'Console'.");
        Assertions.assertTrue(consoleAppender instanceof ConsoleAppender, "Expecting 'Console' appender to be an instance of ConsoleAppender class.");

        // Redirect the system streams
        SysStreamHandler.redirectToLoggers();
        
        // Output some logging statements
        LOGGER.debug("THIS IS A DEBUG LEVEL MESSAGE");
        LOGGER.info("THIS IS A INFO LEVEL MESSAGE");
        LOGGER.warn("THIS IS A WARN LEVEL MESSAGE");
        LOGGER.error("THIS IS A ERROR LEVEL MESSAGE");
        LOGGER.fatal("THIS IS A FATAL LEVEL MESSAGE");
        System.out.println("THIS IS A SYSTEM.OUT MESSAGE");
        System.err.println("THIS IS A SYSTEM.ERR MESSAGE");
    }

}
// End of class.
