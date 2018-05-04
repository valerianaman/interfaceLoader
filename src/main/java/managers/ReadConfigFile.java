package managers;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.slf4j.Logger;

public class ReadConfigFile {

	/**
	 * this class is made just for parseing fields from json to
	 * something treatable
	 * @author rcidsanc
	 *
	 */
	class ConfigField {
		private String name;
		private String field;
		private String format = null;
		private String transform = null;

		public ConfigField(JSONObject obj) {
			name = (String) obj.get("name");
			field = (String) obj.get("field");
			if (obj.containsKey("format")) {
				format = (String) obj.get("format");
			}
			if (obj.containsKey("transform")) {
				transform = (String) obj.get("transform");
			}
		}

		public String getName() {
			return name;
		}

		public String getField() {
			return field;
		}
		
		public String getFormat() {
			return format;
		}
		
		public String getTransformation() {
			return transform;
		}
	}
	
	private Logger logger;
	private JSONObject jsonFile;
	private boolean canContinue = false;
	private String name;
	private String filesRoute;
	private String separator;
	private List<ConfigField> fields;
	private List<ConfigField> nestedFields;
	private List<ConfigField> despisedFields;
	private String dbIp;
	private String dbPort;
	private String db;
	private String dbTable;
	private String dbOperation;
	private String dbUser;
	private String dbPass;

	public ReadConfigFile(String path, Logger lg) {
		logger = lg;
		logger.info("[ReadConfigFile module] Started execution.");
		FileReader file = null;
		JSONParser parser = new JSONParser();
		try {
			file = new FileReader(path);
			jsonFile = (JSONObject) parser.parse(file);
			file.close();
			canContinue = true;
			logger.debug("Json config file opened correctly");
		} catch (FileNotFoundException e) {
			logger.error("Config file not found at the specifyed route:\n\t" + e.getMessage());
			// e.printStackTrace();
		} catch (IOException e) {
			logger.error("Input/output error reading file. Maybe corrupt or damaged:\n\t" + e.getMessage());
			// e.printStackTrace();
		} catch (ParseException e) {
			logger.error(
					"Error parseing config file. Check if its in json format, and sintax errors:\n\t" + e.getMessage());
			// e.printStackTrace();
		}
	}
	
	/**
	 * process a jsonarray returning a configfield list
	 * @param jsonarray arr
	 * @return configfield list
	 */
	private List<ConfigField> processJsonArray(JSONArray arr){
		List<ConfigField> result = new ArrayList<>();
		for(Object o: arr)
			result.add(new ConfigField((JSONObject) o));
		return result;
	}
	
	public void processFile() {
		name = (String) jsonFile.get("name");
		filesRoute = (String) jsonFile.get("files_route");
		separator = (String) jsonFile.get("separator");
		if (jsonFile.containsKey("db_config")) {
			JSONObject obj = (JSONObject) jsonFile.get("db_config");
			dbIp = (String) obj.get("db_ip");
			dbPort = (String) obj.get("db_port");
			db = (String) obj.get("db");
			dbTable = (String) obj.get("db_table");
			dbOperation = (String) obj.get("db_operation");
			dbUser = (String) obj.get("db_user");
			dbPass = (String) obj.get("db_pass");
		}
		fields = processJsonArray((JSONArray) jsonFile.get("fields"));
		if (jsonFile.containsKey("nested_fields"))
			nestedFields = processJsonArray((JSONArray) jsonFile.get("nested_fields"));
		if (jsonFile.containsKey("despised_fields"))
			despisedFields = processJsonArray((JSONArray) jsonFile.get("despised_fields"));
	}
	
	// TODO return fields & despisedFields
	// mejor que devuelva por un lado names por otro field, format, y tal
	public List<String> getFields() {
		List<String> l = new ArrayList<>();
		for (ConfigField f: fields) {
			l.add(f.getField());
		}
		return l;
	}
	
	public List<String> getNestedFields() {
		List<String> l = new ArrayList<>();
		for (ConfigField f: nestedFields) {
			l.add(f.getField());
		}
		return l;
	}
	
	public List<String> getDespisedFields() {
		List<String> l = new ArrayList<>();
		for (ConfigField f: despisedFields) {
			l.add(f.getField());
		}
		return l;
	}
	
	public String getTransformation(String field) {
		String result = null;
		for(ConfigField f: fields)
			if(f.getField().equals(field))
				result = f.getTransformation();
		if (result == null)
			for(ConfigField f: nestedFields)
				if(f.getField().equals(field))
					result = f.getTransformation();
		if (result == null)
			for(ConfigField f: despisedFields)
				if(f.getField().equals(field))
					result = f.getTransformation();
		return result;
	}
	
	public String getFormat(String field) {
		String result = null;
		for(ConfigField f: fields)
			if(f.getField().equals(field)) {
				result = f.getFormat();
				logger.debug("esta en fields");
			}
		if (result == null)
			for(ConfigField f: nestedFields)
				if(f.getField().equals(field)) {
					
				}
		if (result == null)
			for(ConfigField f: despisedFields)
				if(f.getField().equals(field)) {
					result = f.getFormat();
					logger.debug("esta en despisedFields");
				}
		return result;
	}
	
	public String getDbIp(){
		return dbIp;
	}
	
	public String getDbPort() {
		return dbPort;
	}

	public String getDb() {
		return db;
	}

	public String getDbTable() {
		return dbTable;
	}

	public String getDbOperation() {
		return dbOperation;
	}

	public boolean canContinue() {
		return canContinue;
	}

	public String getDbUser() {
		return dbUser;
	}

	public String getDbPass() {
		return dbPass;
	}

	public String getFilesRoute() {
		return filesRoute;
	}

	public String getSeparator() {
		return separator;
	}
	
}
