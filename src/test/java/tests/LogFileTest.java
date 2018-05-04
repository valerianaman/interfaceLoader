package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import ch.qos.logback.core.util.SystemInfo;
import managers.LogFile;

public class LogFileTest extends AbstractTest {

	LogFile file;
	LogFile file2;
	List<String> fields;
	List<String> nestedFields;
	List<String> despisedFields;
	Logger logger;

	@Before
	public void setUp() throws Exception {
		String[] nf = { "EQUIPO", "NUM. SERIE", "INTERF. TCPIP RED EXT.", "ESTADO", "PORCENTAJE" };
		String[] df = { "ROLE MAQUINA", "SOURCE", "TOTAL DISCO", "USOCPU", "RECURSOS DEL HARDWARE",
				"PROBLEMAS DEL SISTEMA", "NO HAY NINGUN PROBLEMA", "SOFTWARE INSTALADO", "SOFTWARE CORRECTO",
				"CONTROLADOR", "TODOS LOS CONTROLAD. OK", "XCOM ACTIVOS", "GESTORES MQSERIES", "TODOS LOS GESTORES OK",
				"CANALES MQSERIES", "TODOS LOS CANALES OK", "COLAS MQSERIES", "TODOS LAS COLAS OK", "PUERTOS XCOM O MQ",
				"NO HAY NINGUN PROBLEMA" };
		fields = new ArrayList<>();
		fields.add("SISTEMA");
		fields.add("FECHA");
		fields.add("HORA");
		nestedFields = new ArrayList<>();
		nestedFields.addAll(Arrays.asList(nf));
		despisedFields = new ArrayList<>();
		despisedFields.addAll(Arrays.asList(df));

		logger = generateLogger("logFileTest");
		file = new LogFile(testSrcFolder + "/prueba2.txt", ";", logger);

		file.setFields(fields, nestedFields, despisedFields);
		logger.info("starting pueba2 run");
		file.run();
	}

	@After
	public void tearDown() throws Exception {
		file.clear();
		file = null;
	}

	@Test
	public void testMainFields() throws IOException {
		logger.warn("Empezando test de mainfields");
		List<Map<String, String>> l = file.getData();
		assertFalse(l.isEmpty());
		int size = l.get(0).keySet().size();
		for (int i = 0; i < l.size(); i++) {
			assertTrue(l.get(i).containsKey("SISTEMA"));
			assertEquals("C12186X", l.get(i).get("SISTEMA"));
			assertTrue(l.get(i).containsKey("FECHA"));
			assertEquals("151229", l.get(i).get("FECHA"));
			assertTrue(l.get(i).containsKey("HORA"));
			assertEquals("00034381", l.get(i).get("HORA"));

			assertEquals(size, l.get(i).keySet().size());
		}

	}

	@Test
	public void testNestedFields() {
		logger.warn("Empezando test de nestedfields");
		List<Map<String, String>> l = file.getData();
		assertFalse(l.isEmpty());
		for (int i = 0; i < 3; i++) {
			assertTrue(l.get(i).containsKey("EQUIPO"));
			assertEquals("C12186X", l.get(i).get("EQUIPO"));
			assertTrue(l.get(i).containsKey("NUM. SERIE"));
			assertEquals("6546C40", l.get(i).get("NUM. SERIE"));
			assertTrue(l.get(i).containsKey("ESTADO"));
			assertEquals("OK", l.get(i).get("ESTADO"));
			assertTrue(l.get(i).containsKey("PORCENTAJE"));
			assertEquals("0000000100", l.get(i).get("PORCENTAJE"));
		}

	}
}
