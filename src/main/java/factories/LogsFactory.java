package factories;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;

import ch.qos.logback.classic.LoggerContext;
import managers.LogFile;
import managers.MemoryCpuManager;

public class LogsFactory implements Runnable{
	
	private PathManager pathManager;
	private LoggerContext lc;
	private Logger logger;
	private List<String> routes;
	private List<String> fields;
	private List<String> nestedFields;
	private List<String> despisedFields;
	private List<Map<String, String>> values;
	private MemoryCpuManager mm;
	private int executionPoint;
	private String separator;
	private SimpleDateFormat df, parser;
	
	public LogsFactory (String route, String separator, Logger lc) {
		logger=lc;
		pathManager = new PathManager(route, lc);
		routes = new ArrayList<>();
		values = new ArrayList<>();
		fields = new ArrayList<>();
		mm = new MemoryCpuManager(lc);
		this.separator = separator;
		executionPoint = 0;
	}
	
	public void setFields(List<String> fields, List<String> nFields, List<String> dFields) {
		this.fields = fields;
		nestedFields = nFields;
		despisedFields = dFields;
	}
	
	public void applyDateTransformation(String field, String initialFormat, String finalFormat) throws ParseException {
		List<Map<String, String>> aux = new ArrayList<>();
		String value;
		int cont=0;
		df = new SimpleDateFormat(finalFormat);
		parser = new SimpleDateFormat(initialFormat);
		for (Map<String, String> map : values) {
			
			if (map.containsKey(field)) {
				value = map.get(field).toString();
				// map.remove(field);
				// if (value.length() > 12)
				// value = value.substring(0, value.length() - 2);

				value = df.format(parser.parse(value));

				map.put(field, value);

			}
			value = null;
		}
	}

	public void joinFields(String field1, String field2, String finalField) {
		Map<String, String> auxMap2;
		List<Map<String, String>> vAux = new ArrayList<>();
		for (Map<String, String> auxMap1: values) {
			if (auxMap1.containsKey(field1) && auxMap1.containsKey(field2)) {
				auxMap2 = new HashMap<>();
				for (String key : auxMap1.keySet()) {
					if (!key.equals(field1) && !key.equals(field2)) {
						auxMap2.put(key, auxMap1.get(key));
					}
				}
				auxMap2.put(finalField, auxMap1.get(field1) + auxMap1.get(field2));
				vAux.add(auxMap2);
			}
		}
		values = null;
		values = vAux;
	}
	
	private void processFile(String route) throws IOException {
		logger.info("[Module begin] LogsFactory.processFile, Parameters: route(String)");
		logger.debug(route);
		LogFile log = new LogFile(route, separator, logger);
		log.setFields(new ArrayList<>(fields), new ArrayList<>(nestedFields), new ArrayList<>(despisedFields));
		log.run();
		values.addAll(log.getData());
		log.clear();		
	}

	@Override
	public void run() {
		logger.info("[Module begin] LogsFactory.run, Parameters: N/A");
		if (routes == null || routes.isEmpty()) {
			logger.debug("obtaining file routes");
			routes = new ArrayList<>();
			routes.addAll(pathManager.getRoutes("txt"));
			routes.addAll(pathManager.getRoutes("TXT"));
			pathManager.close();
		}
		int i;
		for (i = executionPoint; (i < routes.size() && mm.canAllocate()); i++){
			if (values != null) {
				logger.warn(values.size() + " values");
				values.clear();
			}
			try {
				processFile(routes.get(i));
			} catch (IOException e) {
				logger.error("Error opening file: " + e.getMessage());
//				e.printStackTrace();
			}
			mm.gc();
		}
		executionPoint = ++i;
		logger.info("[Module end] LogsFactory.run");
	}
	
	public void clear() {
		logger.info("[Module begin] LogsFactory.clean");
//		if (fields != null && !fields.isEmpty())
//			fields.clear();
//		if (values != null && !values.isEmpty())
//			values.clear();
		logger.info("[Module end] LogsFactory.clean");
	}
	
	public boolean hasFinished() {
		return (values != null) && (!values.isEmpty()) && (executionPoint >= routes.size());
	}
	
	public boolean canContinuePerMemory() {
		return !hasFinished() && mm.canAllocate();
	}
	
	public boolean canContinuePerCPU() {
		return !hasFinished() && mm.canProcess();
	}
	
	public List<String> getFields() {
		List<String> result = new ArrayList<>(fields);
		result.addAll(nestedFields);
		return result;
	}
	
	public List<Map<String, String>> getValues(){
		return values;
	}

}
