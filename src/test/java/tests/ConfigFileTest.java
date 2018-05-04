package tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import factories.LogsGenerator;
import managers.ReadConfigFile;

public class ConfigFileTest extends AbstractTest {

	ReadConfigFile read;
	List<String> fields;

	@Before
	public void setUp() throws Exception {
		read = new ReadConfigFile(testSrcFolder + "/prueba.json", generateLogger("configFileTest"));
		read.processFile();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFields() {
		fields = new ArrayList<>();
		fields.add("SISTEMA");
		fields.add("FECHA");
		fields.add("HORA");
		boolean cond;
		for (String s : fields) {
			cond = false;
			for (String s2 : read.getFields())
				if (s.equals(s2)) {
					cond = true;
					break;
				}
			assertTrue(cond);
			cond = false;
		}
	}

	// @Test
	// public void testNestedFields() {
	// fields = new ArrayList<>();
	// fields.add("prueba1");
	// fields.add("prueba2");
	// fields.add("prueba3");
	// boolean cond;
	// for (String s : fields) {
	// cond = false;
	// for (String s2 : read.getNestedFields())
	// if (s.equals(s2)) {
	// cond = true;
	// break;
	// }
	// assertTrue(cond);
	// cond = false;
	// }
	// }

	// @Test
	// public void testDespisedFields() {
	// fields = new ArrayList<>();
	// fields.add("blablabla");
	// fields.add("pepepepe");
	// boolean cond;
	// for (String s : fields) {
	// cond = false;
	// for (String s2 : read.getDespisedFields())
	// if (s.equals(s2)) {
	// cond = true;
	// break;
	// }
	// assertTrue(cond);
	// cond = false;
	// }
	// }

	@Test
	public void testTransformation() {
		assertEquals("no esta claro", read.getTransformation("HORA"));
	}

	@Test
	public void testFormat() {
		assertEquals("no esta claro", read.getTransformation("HORA"));
	}

	@Test
	public void testDbConfig() {
		assertEquals("localhost", read.getDbIp());
		assertEquals("3306", read.getDbPort());
		assertEquals("stg", read.getDb());
		assertEquals("param_extract", read.getDbTable());
		assertEquals("create", read.getDbOperation());
	}
}
