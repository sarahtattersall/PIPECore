package utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class AbstractTestLog4J2 {

    /**
     * Change logging level for the given class, or for the root logger
     * Intended to enable logging to be limited to a particular class, but logger hierarchy may cause 
     * logging to be affected for more than the target class
     * @param clazz    only needed for a given class; may be null if testClassOnly is false
     * @param level:   e.g., Level.DEBUG
     * @param testClassOnly:  true: only for given class; false:  root logger
     */

    public static void setUpLog4J2(Class clazz, Level level, boolean testClassOnly) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig rootConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        if ((testClassOnly) && (clazz != null)) {
            LoggerConfig testClassConfig = config.getLoggerConfig(clazz.getName());
            testClassConfig.setLevel(level);
        } else {
            rootConfig.setLevel(level);
        }
        ctx.updateLoggers(); // This causes all Loggers to refetch information from their LoggerConfig.
    }

    /**
     * Change the root logging level, affecting logging for all classes.
     * Default is Level.ERROR
     * @param level
     */
    public static void setUpLog4J2ForRoot(Level level) {
        setUpLog4J2(null, level, false);
    }

}
