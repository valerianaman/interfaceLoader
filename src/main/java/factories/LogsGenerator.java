package factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class LogsGenerator {

	LoggerContext context = null;
	JoranConfigurator configurator = null;
	Logger logger = null;
	
	public LogsGenerator(String logbackConfigFile, String logsDestinationFolder, String appName, String messageLevel){
		context = (LoggerContext) LoggerFactory.getILoggerFactory();
		configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.putProperty("DEV_HOME", logsDestinationFolder);
		context.putProperty("application-name", appName);
		context.putProperty("message_level", messageLevel);
		try {
			configurator.doConfigure(logbackConfigFile);
		} catch (JoranException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger = context.getLogger(appName);
	}
	
	public void debug(String message) {
		logger.debug(message);
	}
	
	public void info(String message) {
		logger.info(message);
	}
	
	public void warn(String message) {
		logger.warn(message);
	}
	
	public void error(String message) {
		logger.error(message);
	}
}
