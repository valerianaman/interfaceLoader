package managers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

public class LogFile {

	private class LinesProcesser {

		private Map<String, String> mainFields = null;
		private List<Map<String, String>> nestedFields;

		public LinesProcesser() {
			nestedFields = new ArrayList<>();
			mainFields = new HashMap<>();
		}

		public void addMainFields(List<String> fields, String[] values) throws Exception {
			for (int i = 0; i < values.length; i++)
				values[i] = values[i].trim();
			logger.debug("keyset size "+mainFields.keySet().size());
			if (!mainFields.isEmpty())
				throw new Exception("Error on file, already added main fields and trying to add it again.");
//			mainFields = new HashMap<>();
			for (int i = 0; i < fields.size(); i++)
				mainFields.put(fields.get(i), values[i]);
		}

		public void addNestedFields(List<String> fields, String[] values) throws Exception {
			if (mainFields == null)
				throw new Exception("Error on file, trying to add nested fields before adding main ones.");
			for (int i = 0; i < values.length; i++)
				values[i] = values[i].trim();
			Map<String, String> aux = new HashMap<>();
			aux.putAll(mainFields);
			int j=0;
			for (int i = 0; i < fields.size(); i++) {
				while (values[j].equals(""))
					j++;
				aux.put(fields.get(i), values[j]);
				j++;
			}
			nestedFields.add(aux);
		}

		/**
		 * a copy of nested fields (wich also contains main fields) its
		 * returned, because this class will be reused and cleared
		 * 
		 * @return
		 */
		public List<Map<String, String>> getResults() {
			List<Map<String, String>> result = new ArrayList<>();
			result.addAll(nestedFields);
			return result;
		}

		public void clear() {
			mainFields = new HashMap<>();
			nestedFields = new ArrayList<>();
		}
	}

	private List<String> fields;
	private List<String> nestedFields = null;
	private List<String> despisedFields = null;
	private List<Map<String, String>> result;
	private Logger logger;
	private int state=0;
	private String route;
	private String separator;
	private BufferedReader buffer;
	private LinesProcesser linesProcesser;

	public LogFile(String route, String separator, Logger lc) {
		logger = lc;
		this.route = route;
		this.separator = separator;
		try {
			buffer = new BufferedReader(new FileReader(normalizeRoute(route)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error("Error reading file '" + route + "', may be corrupted or damaged: " + e.getMessage());
			// e.printStackTrace();
		}
		result = new ArrayList<>();
		linesProcesser = new LinesProcesser();
	}

	public void setFields(List<String> f, List<String> nf, List<String> df) {
		fields = f;
		nestedFields = nf;
		despisedFields = df;
	}

	private boolean matchFields(String[] line, List<String> l) {
		boolean cond = true;
		if (line.length == l.size()) {
			for (int i = 0; i < l.size() && cond; i++) {
				cond = line[i].equals(l.get(i));
			}
			return cond;
		} else
			return false;

	}
	
	private boolean matchAnyField(String[] line, List<String> l) {
		boolean cond = false;
		for (String lPart : line) {
			for (String field : l) {
				cond = lPart.equals(field);
				// not sure if break needed, i think that not really
				// kinda nasty, better while loop, but no time
				if (cond) break;
			}
			if(cond)
				break;
		}
		return cond;
	}

	/**
	 * this method has been designed in a way that in one line you have or main
	 * fields, or nested fields, otherwise would be data or despised fields.
	 * 
	 * @param line
	 * @throws Exception
	 */
	private void processLine(String line) {
		logger.debug("line -> "+line);
		String[] values = line.split(separator);
		String aux;
		List<String> finalValues = new ArrayList<>();
		// below code removes all "blank fields"
		for (int i = 0; i < values.length; i++) {
			aux = values[i].trim();
			while(aux.startsWith(" "))
				aux = aux.replaceFirst(" ", "");
			if (!aux.equals(""))
				finalValues.add(aux);
		}
		logger.debug("line has "+finalValues.size()+" fields");
		for(String s: finalValues)
			logger.debug(s);
		if (matchFields(finalValues.toArray(new String[finalValues.size()]), fields)) {
			logger.info("matchs main fields");
			// if matches a main field that we need, state=1, what means it will be processed
			result.addAll(linesProcesser.getResults());
			linesProcesser.clear();
			state = 1;
		} else if (matchFields(finalValues.toArray(new String[finalValues.size()]), nestedFields) && state != -1) {
			logger.info("match nested fields");
			// if matches a nested field that we need, state=2, what means it will be processed as nested field corresponds
			state = 2;
		} else if (matchAnyField(finalValues.toArray(new String[finalValues.size()]), despisedFields)) {
			logger.info("match any of despised fields");
			// if matches any of the despised fields, it will not process any line till it founds a field needed
			state = 0;
		} else if(state != 0 && state != -1){
			try {
				switch (state) {
				case 1:
					linesProcesser.addMainFields(fields, values);
					break;
				case 2:
					linesProcesser.addNestedFields(nestedFields, values);
					break;
				default:
					break;
				}
			} catch (Exception e) {
				state = -1;
				logger.error("Error processing line: " + line + "\n\t" + e.getMessage());
			}
		}
	}

	public void run() throws IOException {
		String line = buffer.readLine();
		while (line != null) {
			processLine(line);
			line = buffer.readLine();
		}
	}

	private String normalizeRoute(String route) {
		return route.replaceAll("\\\\", "/");
	}

	public List<Map<String, String>> getData() {
		result.addAll(linesProcesser.getResults());
		return new ArrayList<>(result);
	}

	public void clear() throws IOException {
		if (fields != null)
			fields.clear();
		if (nestedFields != null)
			nestedFields.clear();
		if (despisedFields != null)
			despisedFields.clear();
		if (result != null)
			result.clear();
		buffer.close();
		linesProcesser.clear();
	}
}
