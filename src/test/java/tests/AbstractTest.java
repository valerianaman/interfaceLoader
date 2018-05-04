package tests;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public abstract class AbstractTest {

	protected static final String testSrcFolder = "src/test/resources";
	protected static final String testLogFolder = "src/test/resources/logs";

	public static Logger generateLogger(String applicationName) {

		System.setProperty("application-name", applicationName);
		System.setProperty("message_level", "debug");
		System.setProperty("DEV_HOME", testLogFolder);

		List<String> applicationNames;
		applicationNames = new LinkedList<>(Arrays.asList("configFileTest", "LogFileTest", "loggerFactory.test", "test",
				"logManagerPruebaCompleta"));
		Logger log1 = null;
		for (String s : applicationNames) {
			log1 = (Logger) LoggerFactory.getLogger(s);

			if (log1 != null) {

				break;
			}

		}

		// log1.getLoggerContext().reset();
		LoggerContext lc;
		if (log1 == null) {

			lc = (LoggerContext) LoggerFactory.getILoggerFactory();

		} else {

			lc = log1.getLoggerContext();
			JoranConfigurator jc = new JoranConfigurator();
			jc.setContext(lc);
			lc.reset();
			lc.putProperty("application-name", applicationName);
			lc.putProperty("message_level", "debug");
			lc.putProperty("DEV_HOME", testLogFolder);

			try {
				jc.doConfigure(testSrcFolder + "/logback.xml");
			} catch (JoranException e1) { // TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

		// System.out.println("===");
		// StatusPrinter.print(lc);

		/*
		 * try { jc.doConfigure(testSrcFolder + "/logback.xml"); } catch
		 * (JoranException e1) { // TODO Auto-generated catch block
		 * e1.printStackTrace(); }
		 */
		Logger log = lc.getLogger(applicationName);
		return log;
	}

}
