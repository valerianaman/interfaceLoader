package tests;

import static org.junit.Assert.*;

import java.text.ParseException;
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
import factories.LogsFactory;

public class LogsFactoryTest extends AbstractTest {

	LogsFactory factory;
	String[] f = { "SISTEMA", "FECHA", "HORA" };
	String[] nf = { "EQUIPO", "NUM. SERIE", "INTERF. TCPIP RED EXT.", "ESTADO", "PORCENTAJE" };
	String[] df = { "ROLE MAQUINA", "SOURCE", "TOTAL DISCO", "USOCPU", "RECURSOS DEL HARDWARE", "PROBLEMAS DEL SISTEMA",
			"NO HAY NINGUN PROBLEMA", "SOFTWARE INSTALADO", "SOFTWARE CORRECTO", "CONTROLADOR",
			"TODOS LOS CONTROLAD. OK", "XCOM ACTIVOS", "GESTORES MQSERIES", "TODOS LOS GESTORES OK", "CANALES MQSERIES",
			"TODOS LOS CANALES OK", "COLAS MQSERIES", "TODOS LAS COLAS OK", "PUERTOS XCOM O MQ",
			"NO HAY NINGUN PROBLEMA" };

	@Before
	public void setUp() throws Exception {
		factory = new LogsFactory(testSrcFolder + "/txt", ";", generateLogger("loggerFactory.test"));
		factory.setFields(Arrays.asList(f), Arrays.asList(nf), Arrays.asList(df));
		factory.run();
	}

	@After
	public void tearDown() throws Exception {
		factory.clear();
		factory = null;
	}

	@Test
	public void testFormatDate() {
		factory.joinFields("FECHA", "HORA", "DATE");
		int size = factory.getValues().get(0).keySet().size();
		System.out.println(size);
		try {
			factory.applyDateTransformation("DATE", "yyMMddhhmmss", "yyyy-MM-dd hh:mm:ss");
			assertEquals(size, factory.getValues().get(0).keySet().size());
			for (String s : factory.getValues().get(0).keySet())
				System.out.println(s);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
