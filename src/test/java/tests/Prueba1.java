package tests;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class Prueba1 extends AbstractTest {

	public static void main(String[] args) {
		try {

			generateLogger("test");

		} catch (Exception e) {
			// meter al log
			e.printStackTrace();
		}
	}
}
