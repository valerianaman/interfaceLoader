package managers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;

import ch.qos.logback.classic.LoggerContext;
import factories.LogsFactory;
import services.MySQLService;

public class LogFileManager {
	
	private String dbHost, dbPort, db, paramTable, dbUser, dbPass;
	private LoggerContext lc;
	private Logger logger;
	private ReadConfigFile configFile;
	
	public LogFileManager(String path, Logger lc) {
		logger = lc;
		logger.info("Log created");
		configFile = new ReadConfigFile(path, lc);
		configFile.processFile();
		this.dbHost = configFile.getDbIp();
		this.dbPort = configFile.getDbPort();
		this.db = configFile.getDb();
		this.paramTable = configFile.getDbTable();
		this.dbUser = configFile.getDbUser();
		this.dbPass = configFile.getDbPass();
		
	}
	
	private List<Map<String, String>> getParamValues(String[] fields) throws SQLException, ClassNotFoundException{
		logger.info("[Module begin] LogFile.getParamValues, Parameters: fields(String[])");
		try {
			System.out.println(dbHost+" "+ dbPort+" "+db);
			MySQLService mysql = new MySQLService(dbHost, dbPort, db, logger);
			mysql.authenticate(dbUser, dbPass);
			List<Map<String, String>> result = new ArrayList<>();
			if (mysql.connect()) {
				Map<String, String> where = new HashMap<String, String>();
				where.put("extract_type", "PING");
				where.put("state", "1");
				result = mysql.selectWhereAsMap(Arrays.asList(fields), where, paramTable);
//				System.out.println(result);
				mysql.close();
			}
			logger.info("[Module end] pingFileManager.getParamValues -> "+ !result.isEmpty());
			return result;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error("pingFileManager.getParamValues error: " + e);
//			e.printStackTrace();
			return null;
		}
	}
	
	private boolean createTable(String tableName, List<String> fields) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException  {
		logger.info("[Module begin] pingFileManager.createTable, Parameters: tableName(String), fields(List<String>)");
		try {
			MySQLService mysql = new MySQLService(dbHost, dbPort, db, logger);
			mysql.authenticate(dbUser, dbPass);
			mysql.connect();
			mysql.dropTable(tableName);
			boolean result = mysql.createTextTable(tableName, fields);
			mysql.close();
			logger.info("[Module end] pingFileManager.createTable");
			return result;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("pingFileManager.createTable error: " + e);
			return false;
			// e.printStackTrace();
		}
	}

	private boolean insertIntoTable(List<Map<String, String>> data, String tableName,
			Map<String, String> additionalFields) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException{
		logger.info("[Module begin] pingFileManager.insertIntoTable, Parameters: data(List<Map<String, String>>), tableName(String), additionalFields(Map<String, String>)");
		MySQLService mysql = new MySQLService(dbHost, dbPort, db, logger);
		mysql.authenticate(dbUser, dbPass);
		mysql.connect();
		boolean result = mysql.insert(data, tableName, additionalFields);
		mysql.close();
		logger.info("[Module end] pingFileManager.insertIntoTable");
		return result;
	}
	
	public void run() throws IOException, ParseException, NumberFormatException, URISyntaxException,
			ClassNotFoundException, InstantiationException, IllegalAccessException {
		logger.info("[Module begin] pingFileManager.run, Parameters: N/A");
		String[] fields = { "*" };
		String directory = configFile.getFilesRoute();
		List<String> excelFields = null;
		Map<String, String> additionalFields = new HashMap<>();
		int tries = 0;
		String tableName = null;
		List<Map<String, String>> param;
		try {
			param = getParamValues(fields);

			for (Map<String, String> mAux : param) {
				tableName = mAux.get("name_table_dummy");
				additionalFields.put("measure_id", mAux.get("measure_id"));
				additionalFields.put("import_type", mAux.get("import_type"));
				additionalFields.put("data_import_id", mAux.get("data_import_id"));
				logger.debug("tabla de destino -> " + tableName);

				LogsFactory logs = new LogsFactory(directory, configFile.getSeparator(), logger);
				// System.out.println("campos del fichero de configuracion");
				// for(String s: configFile.getFields())
				// System.out.println(s);
				logs.setFields(configFile.getFields(), configFile.getNestedFields(), configFile.getDespisedFields());
				logs.run();
				// tiene que ser fecha & hora, no al reves que peta
				// logs.joinFields("FECHA", "HORA", "DATE");
				// try {
				// logs.applyDateTransformation("DATE", "yyMMddhhmmss",
				// "yyyy-MM-dd hh:mm:ss");
				// } catch (ParseException e1) {
				// // TODO Auto-generated catch block
				// e1.printStackTrace();
				// }

				try {
					logs.applyDateTransformation("FECHA", "yyMMdd", "yyyy-MM-dd");
					logs.applyDateTransformation("HORA", "HHmmssSS", "HH:mm:ss");
					// logs.joinFields("HORA", "FECHA", "HORA/FECHA");
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				List<String> tableFields = logs.getFields();
				tableFields.addAll(additionalFields.keySet());
				logger.debug("campos a insertar " + tableFields.size() + "\n\n\n");
				for (String s : tableFields) {
					logger.debug(s);
				}
				if (createTable(tableName, tableFields)) {
					try {
						insertIntoTable(logs.getValues(), tableName, additionalFields);
						while (!logs.hasFinished()) {
							System.out.println("\n\tnueva extraccionn de datos: hasta aqui \n\n");
							logs.clear();
							if (!logs.canContinuePerMemory())
								throw new Exception(
										"Cannot continue, to much files, heap overflow.\n\tTry launching program with more heap size like: 'java -Xmx2G -Xms512m -jar appName.jar params...'");
							while (!logs.canContinuePerCPU() && tries < 5) {
								tries++;
								Thread.sleep(500);
							}
							if (tries == 5)
								throw new Exception("Cannot continue, CPU usageat tops.");
							logs.run();
							logs.applyDateTransformation("FECHA", "yyMMdd", "yyyy-MM-dd");
							logs.applyDateTransformation("HORA", "HHmmssSS", "HH:mm:ss");
							// logs.joinFields("HORA", "FECHA", "HORA/FECHA");
							insertIntoTable(logs.getValues(), tableName, additionalFields);
						}
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						logger.error("pingFileManager.run error: " + e);
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						logger.error("pingFileManager.run error: " + e);
						e.printStackTrace();
					}
				}
			}
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		logger.info("[Module end] pingFileManager.run");
	}

}
