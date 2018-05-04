package tests;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import managers.LogFileManager;

public class pruebaCompleta extends AbstractTest {

	public static void main(String[] args) {

		try {

			LoggerContext lc = generateLogger("logManagerPruebaCompleta").getLoggerContext();
			LogFileManager manager = new LogFileManager(testSrcFolder + "/pruebaCompleta.json",
					lc.getLogger("logManagerPruebaCompleta"));
			manager.run();
		} catch (Exception e) {
			// meter al log
			e.printStackTrace();
		}

	}

}
