package main;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import managers.LogFileManager;

public class Application {

	private String dbHost, dbPort, db, paramTable, dbUser, dbPass, logLevel, logRoute, configFileRoute, configFIleRoute;

	public Application(String[] args) {
		try {
			parseArgs(args);
			LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
			JoranConfigurator jc = new JoranConfigurator();
			jc.setContext(lc);
			lc.reset();
			lc.putProperty("application-name", "com.everis.PingFileManager");
			lc.putProperty("message_level", logLevel);
			lc.putProperty("DEV_HOME", logRoute);
			try {
				jc.doConfigure(logRoute + "/logback.xml");
			} catch (JoranException e1) {
				// TODO Auto-generated catch block
				System.out.println("Logs file configurator is not placed at the specified path:\n\t"+e1.getMessage());
				e1.printStackTrace();
			}
			Logger log = lc.getLogger("com.everis.PingFileManager");
			LogFileManager em = new LogFileManager(configFIleRoute, log);
			em.run();
		} catch (Exception e) {
			// meter al log
			e.printStackTrace();
		}
	}

	private void parseArgs(String[] args) throws Exception {
		if (args.length > 18 || args.length < 17)
			throw new Exception(
					"\nParams errors: Incorrect number of params, app shoud be launched as:\n\tjava -jar pingFileManager.jar "
							+ "--host db_ip --port db_port --db db_name --table param_table --user db_user --pass db_pass --log_level log_level"
							+ " --log_config_file route_to_logback_config_file --config_file_route route_to_app_config_file");
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "--host":
				dbHost = args[++i];
				break;
			case "--port":
				dbPort = args[++i];
				break;
			case "--db":
				db = args[++i];
				break;
			case "--table":
				paramTable = args[++i];
				break;
			case "--user":
				dbUser = args[++i];
				break;
			case "--pass":
				dbPass = args[++i];
				break;
			case "--log_level":
				logLevel = args[++i];
				break;
			case "--log_config_file":
				logRoute = args[++i];
				break;
			case "--config_file_route":
				configFIleRoute = args[++i];
				break;
			default:
				throw new Exception(
						"\nParams error: you have mistaken several params formulation, app shoud be launched as:\n\tjava -jar pingFileManager.jar "
								+ "--host db_ip --port db_port --db db_name --table param_table --user db_user --pass db_pass --log_level log_level"
								+ " --log_config_file route_to_logback_config_file --config_file_route route_to_app_config_file");
			}
		}
	}

	public static void main(String[] args) {
		Application app = new Application(args);
	}

}
