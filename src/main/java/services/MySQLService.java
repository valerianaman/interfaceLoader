package services;

import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.text.Normalizer;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.math.IntMath;

import ch.qos.logback.classic.LoggerContext;

public class MySQLService {
	private final String driver = "com.mysql.jdbc.Driver";
	private String url;
	private String username = null;
	private String password = null;
	private Connection conn = null;
	private Statement stmt = null;
	private String db = null;
	private boolean connected;
	private Logger logger;
	private LoggerContext lc;
	private final int OPTIMAL_INSERT_SIZE = 500;
	private final int COMMIT_SIZE = 10000/OPTIMAL_INSERT_SIZE;

	public MySQLService(String host, String port, String db, Logger lc) throws ClassNotFoundException {
		Class.forName(driver);
		this.url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useUnicode=true&characterEncoding=utf8&characterResultSets=utf8";
		this.db = db;
		connected = false;
		logger=lc;
	}

	public void authenticate(String user, String pass) {
		username = user;
		password = pass;
	}

	public boolean connect() throws SQLException {
		connected = false;
		logger.info("[Module begin] MySQLService.connect, Parameters: N/A");
		conn = DriverManager.getConnection(url, username, password);
		stmt = conn.createStatement();

		connected = true;
		logger.info("[Module end] MySQLService.connect");
		return connected;
	}

	private boolean isConnected() {
		return connected;
	}

	public boolean createTableAs(String tableName, Map<String, String> fields) {
		logger.info("[Module begin] MySQLService.createTableAs, Parameters: tableName(String), fields(Map<String, String>)");
		StringBuilder query = new StringBuilder("");
		try {
			if (isConnected()) {
				query.append("CREATE TABLE `").append(normalize(db)).append("`.`"+ normalize(tableName)).append("` (");
				int i = 0;

				for (String s : fields.keySet()) {
					query.append("`").append(normalize(s) ).append(normalize(fields.get(s)));
					i++;
					if (i < fields.size())
						query.append(", ");
					else
						query.append(") engine=innodb default charset=utf8;");
				}
				stmt.executeUpdate(query.toString());
				logger.info("[Module end] MySQLService.createTableAs");
				return true;
			} else
				return false;
		} catch (SQLException e) {
//			e.printStackTrace();
			logger.error("MySQLService.createTableAs error " + e);
			return false;
		}
	}
	
	public boolean createTextTable(String tableName, List<String> fields) {
		logger.info("[Module begin] MySQLService.createTextTable, Parameters: tableName(String), fields(List<String>)");
		StringBuilder query = new StringBuilder("");
		if (isConnected()) {
			try {

				query.append("CREATE TABLE `").append(normalize(db)).append("`.`").append(normalize(tableName)).append("` (");
				int i = 0;

				for (String s : fields) {
					query.append("`" ).append(normalize(s)).append("` text");
					i++;
					if (i < fields.size())
						query.append(", ");
					else
						query.append(") engine=innodb default charset=utf8 ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8;");
				}
				stmt.executeUpdate(query.toString());
				logger.info("[Module end] MySQLService.createTextTable");
				return true;

			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("MySQLService.createTextTable error " + e);
				return false;
			}
		}
		return false;
	}
	
	public List<List<String>> select(List<String> select, String tableName) {
		logger.info("[Module begin] MySQLService.select, Parameters: select(List<String>), tableName(String)");
		if (isConnected()) {
			try {
				String query = "select ";
				int i = 0;
				for (String str : select) {
					i++;
					query += str;
					if (i < select.size())
						query += ", ";
				}
				List<String> fields = (select.size() == 1 & select.get(0).equals("*")) ? describeTable(tableName)
						: select;
				query += " from " + normalize(tableName);
				List<List<String>> result = new ArrayList<>();
				ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					List<String> l = new ArrayList<>();
					for (String s : fields)
						l.add(rs.getString(s));
					result.add(l);
				}
				logger.info("[Module end] MySQLService.select");
				return result;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("MySQLService.select error " + e );
				e.printStackTrace();
			}
			return null;
		}
		return null;
	}
	
	public List<List<String>> selectWhere(List<String> select, Map<String, String> where, String tableName){
		logger.info("[Module begin] MySQLService.selectWhere, Parameters: select(List<String>), where (Map<String, String>),tableName(String)");
		if(isConnected()){
			try {
				String query = "select ";
				int i = 0;
				for (String str : select) {
					i++;
					query += str;
					if (i < select.size())
						query += ", ";
				}
				List<String> fields;
				if (select.size() == 1 & select.get(0).equals("*"))
					fields = describeTable(tableName);
				else
					fields = select;
				query += " from " + normalize(tableName);
				if (where != null) {
					query += " where";
					for(String s: where.keySet()) {
						query += " "+normalize(s)+"=\""+where.get(s)+"\" and";
					}
					query = query.substring(0, query.length()-4);
				}
				List<List<String>> result = new ArrayList<>();
				ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					List<String> l = new ArrayList<>();
					for (String s : fields)
						l.add(rs.getString(s));
					result.add(l);
				}
				logger.info("[Module end] MySQLService.selectWhere");
				return result;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("MySQLService.selectWhere error" + e);
				e.printStackTrace();
			}
			return null;
		}
		return null;
	}
	
	public List<Map<String, String>> selectWhereAsMap(List<String> select, Map<String, String> where, String tableName){
		logger.info("[Module begin] MySQLService.selectWhereAsMap, Parameters: select(List<String>), where (Map<String, String>),tableName(String)");
		if(isConnected()){
			try {
				String query = "select ";
				int i = 0;
				for (String str : select) {
					i++;
					query += str;
					if (i < select.size())
						query += ", ";
				}
				List<String> fields;
				if (select.size() == 1 & select.get(0).equals("*"))
					fields = describeTable(tableName);
				else
					fields = select;
				query += " from " + normalize(tableName);
				if (where != null) {
					query += " where";
					for(String s: where.keySet()) {
						query += " "+normalize(s)+"=\""+where.get(s)+"\" and";
					}
					query = query.substring(0, query.length()-4);
				}
				List<Map<String, String>> result = new ArrayList<>();
//				System.out.println(query);
				ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					Map<String, String> mAux = new HashMap<>();
					for (String s : fields)
						mAux.put(s, rs.getString(s));
					result.add(mAux);
				}
				logger.info("[Module end] MySQLService.selectWhereAsMap");
				return result;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("MySQLService.selectWhereAsMap error" + e);
				e.printStackTrace();
			}
			return null;
		}
		return null;
	}
	
	private Map<String, String> normalizeMap(Map<String, String> map) {
		//logger.info("[Module begin] MySQLService.normalizeMap, Parameters: map(Map<String, String>)");
		Map<String, String> result = new HashMap<>();
		for(String s: map.keySet())
			result.put(normalize(s), normalizeData(map.get(s)));
		//logger.info("[Module end] normalizeMap");
		return result;
	}
	
	private String createInsertQuery(String tableName, List<Map<String, String>> lMaps, List<String> fields) {
//		logger.info("[Module begin] MySQLService.createInsertQuery, Parameters: tableName(String), map(Map<String, String>), fields(List<String>)");
		StringBuilder query = new StringBuilder("INSERT INTO `").append(normalize(db)).append("`.`").append(normalize(tableName)).append("` VALUES");
		int i, j = 0;
		for (Map<String, String> map : lMaps) {
			Map<String, String> hmnormalized = normalizeMap(map); //////////////////////////////////////////////// BVOY POTR AQUI
			query.append(" (");
			i = 0;
			j++;
			for (String s : fields) {
				i++;
				Set<String> set = hmnormalized.keySet();
				if (set.contains(s))
					if (hmnormalized.get(s).equals("null"))
					query.append(hmnormalized.get(s));
					else 
						query.append("'").append(limitToMaxTextSize(hmnormalized.get(s).replaceAll("\"", "").replaceAll("'", "\'").replaceAll("\"", ""))).append("'");
				else
					query.append("null");
				if (i < fields.size())
					query.append(", ");
			}
			query.append(")");
			if (j < lMaps.size())
				query.append(", ");
			else
				query.append(";");
		}
//		logger.info("[Module end] MySQLService.createInsertQuery");
		return query.toString();
	}
	
	public boolean insert(List<Map<String, String>> data, String tableName) {
		logger.info("[Module begin] MySQLService.insert, Parameters: data(List<Map<String, String>>), tableName(String)");
		String query = "";
		boolean cond = true;
		List<String> fields = describeTable(tableName);
		try {
			setAutoCommitFalse();
		} catch (SQLException e1) {
			logger.error("MySQLService.insert error: cant set autocommit to false, this will make inserts more slowly\n" + e1);
//			e1.printStackTrace();
			cond = false;
		}
		try {
//			List<Map<String, String>> subList = data.subList(fromIndex, toIndex)
			List<List<Map<String, String>>> lSublists = Lists.partition(data, (data.size()/OPTIMAL_INSERT_SIZE)+1);
			logger.info("Hay un total de "+lSublists.size()+" filas a insertar");
			for (List<Map<String, String>> hm : lSublists) {
				query = createInsertQuery(tableName, hm, fields);
				stmt.executeUpdate(query);
			}
			logger.info("[Module end] MySQLService.insert");
			if (cond)
				commit();
			return true;
		} catch (SQLException e) {
//			System.err.println("Insert query explodes. Nice try, but no");
//			System.err.println(query);
//			e.printStackTrace();
			logger.error("MySQLService.insert error: " + query + "\n" + e);
			return false;
		}
	}
	
	public boolean insert(List<Map<String, String>> data, String tableName, Map<String, String> additionalFields) {
		logger.info("[Module begin] MySQLService.insert, Parameters: data(List<Map<String, String>>), tableName(String), additionalFields(Map<String, String>)");
		String query = "";
		boolean cond = true;
		int commitCount = 0;
		Map<String, String> hmnormalized;
		List<String> fields = describeTable(tableName);
		try {
			setAutoCommitFalse();
		} catch (SQLException e1) {
			logger.error("MySQLService.insert error: cant set autocommit to false, this will make inserts more slowly\n" + e1);
//			e1.printStackTrace();
			cond = false;
		}
		int errors = 0;
		int j = 1;
		logger.info(data.size() + " row(s) will be inserted");
		for(Map<String, String> map: data)
			map.putAll(additionalFields);
		List<List<Map<String, String>>> lSublists;
		if (data.size() > OPTIMAL_INSERT_SIZE)
			lSublists = Lists.partition(data,  IntMath.divide(data.size(), (data.size()/OPTIMAL_INSERT_SIZE), RoundingMode.UP));
		else {
			lSublists = new ArrayList<>();
			lSublists.add(data);
		}
		logger.info("Hay un total de "+lSublists.size()+" listas de "+lSublists.get(0).size()+" a insertar");
		for (List<Map<String, String>> hm : lSublists) {
			logger.debug("MySQLService.insert: creating query");
			query = createInsertQuery(tableName, hm, fields);
			logger.debug("MySQLService.insert: inserting "+hm.size()+" lines");
			try {
				stmt.executeUpdate(query);
				commitCount++;
			} catch (SQLException e) {
				errors++;
				// TODO Auto-generated catch block
//				System.err.println(query);
				logger.error("MySQLService.insert error: " + query + "\n" + e);
//				e.printStackTrace();
			}
			if (commitCount >= COMMIT_SIZE) {
				commitCount = 0;
				try {
					commit();
				} catch (SQLException e) {
					logger.error("MySQLService error on COMMIT: "+e);
				}
			}
		}
		try {
			commit();
		} catch (SQLException e) {
			logger.error("MySQLService error on COMMIT: "+e);
		}
		logger.debug("Insertadas " + j + "filas, han fallado " + errors + " inserciones");
		logger.info(j + " row(s) inserted, " + "Errors: " + errors );
		logger.info("[Module end] MySQLService.insert");
		return true;
	}
	
	/**
	 * @param tableName
	 * @return list of colums in the table
	 */
	public List<String> describeTable(String tableName) {
		logger.info("[Module begin] MySQLService.describeTable, Parameters: tableName(String)");
		List<String> fields = new ArrayList<>();
		try {
			ResultSet rs = stmt.executeQuery("describe "+ normalize(tableName));
			while(rs.next())
				fields.add(rs.getString("Field"));
			logger.info("[Module end] MySQLService.describeTable");
			return fields;	
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("MySQLService.describleTable error " + e);
			return null;
		}
	}
	
	public boolean existTable(String tablename) {
		String query = "show tables";
		try {
			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getTables(null, null, "%", null);
			while (rs.next()) {
			  if(rs.getString(3).equals(tablename)) {
				  return true;
			  }
			}
			return false;
		} catch (SQLException e) {
//			System.err.println("Drop query explodes. Nice try, but no");
			e.printStackTrace();
			return false;
		}
	}

	public boolean dropTable(String tableName) {
		logger.info("[Module begin] MySQLService.dropTable, Parameters: tableName(String)");
		String query = "DROP TABLE IF EXISTS `" +normalize(db)+"`.`"+ normalize(tableName) + "`;";
		try {
			stmt.executeUpdate(query);
			logger.info("[Module end] MySQLService.dropTable");
			return true;
		} catch (SQLException e) {
//			System.err.println("Drop query explodes. Nice try, but no");
			logger.error("MySQLService.dropTable error" + e);
			e.printStackTrace();
			return false;
		}
	}
	
	
	
	
	/**
	 * checks if string contains japanese chars
	 * 
	 * @param input
	 */

	private Boolean atLeastOneCharJapanese(String input) {
		Boolean result = false;

		for (int i = 0; i < input.length(); i++) {
			char character;
			character = input.charAt(i);
			String block;
			block = Character.UnicodeBlock.of(character).toString();

			boolean condition = (block == "CJK_UNIFIED_IDEOGRAPHS" || block == "HIRAGANA" || block == "KATAKANA"
					|| block == "CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A") || block == "HALFWIDTH_AND_FULLWIDTH_FORMS";
			result = result || condition;
		}
		return result;

	}

	/**
	 * removes all accents from input string & replaces spaces for _ & all to
	 * lowercase
	 * 
	 * @param input
	 * @return
	 */

	private String normalize(String input) {
		String result;
		// logger.info("[Module begin] MySQLService.normalize, Parameters:
		// input(String)");
		// margen de mejora, mirar tamaño máximo de campo y eso
		if (input.length() > 50)
			input = input.substring(0, 50);

		if (atLeastOneCharJapanese(input)) {
			result = input;
		} else {
			result = Pattern.compile("\\P{ASCII}+").matcher(Normalizer.normalize(input, Normalizer.Form.NFD))
					.replaceAll("");
		}

		// logger.info("[Module end] MySQLService.normalize");
		return result.replaceAll("[().]", " ").replaceAll("-", "").replace("\\]", " ").replace("\\[", " ").trim()
				.replaceAll("  ", " ").replaceAll(" ", "_").replaceAll("\"", "\\\"").replaceAll("\n", " ")
				.replaceAll("\r", "").toLowerCase();
	}


	private String normalizeData(String input) {
		String result;
		// logger.info("[Module begin] MySQLService.normalize, Parameters:
		// input(String)");
		// margen de mejora, mirar tamaño máximo de campo y eso
		if (input.length() > 4000)
			input = input.substring(0, 4000);
		if (atLeastOneCharJapanese(input)) {
			result = input;
		} else {
			result = Pattern.compile("\\P{ASCII}+").matcher(Normalizer.normalize(input, Normalizer.Form.NFD))
					.replaceAll("");
		}

		// logger.info("[Module end] MySQLService.normalize");
		return result.trim().replaceAll("\n", " ").replaceAll("\r", "").replaceAll("  ", " ").replaceAll("\"", "\\\"")
				.replaceAll("'", " ").toLowerCase() + " ";
	}

	
	

/*	private String normalize(String input) {
		//logger.info("[Module begin] MySQLService.normalize, Parameters: input(String)");
		// margen de mejora, mirar tamaño máximo de campo y eso
		if (input.length() > 50)
			input = input.substring(0, 50);
		//logger.info("[Module end] MySQLService.normalize");
		return Pattern.compile("\\P{ASCII}+").matcher(Normalizer.normalize(input, Normalizer.Form.NFD))
				.replaceAll("").replaceAll("[().]", " ").replaceAll("-", "").replace("\\]", " ").replace("\\[", " ").trim()
				.replaceAll("  ", " ").replaceAll(" ", "_").replaceAll("\"", "\\\"").replaceAll("\n", " ").replaceAll("\r", "").toLowerCase();
	}
	
	private String normalizeData(String input) {
		//logger.info("[Module begin] MySQLService.normalize, Parameters: input(String)");
		// margen de mejora, mirar tamaño máximo de campo y eso
		if (input.length() > 4000)
			input = input.substring(0, 4000);
		//logger.info("[Module end] MySQLService.normalize");
		return Pattern.compile("\\P{ASCII}+").matcher(Normalizer.normalize(input, Normalizer.Form.NFD)).replaceAll("")
				.trim().replaceAll("\n", " ").replaceAll("\r", "").replaceAll("  ", " ").replaceAll("\"", "\\\"").replaceAll("'", " ")
				.toLowerCase() + " ";
	}
	*/
	private String limitToMaxTextSize(String input) {
		return (input.length() >= 4000) ? input.substring(0, 4000) : input;
	}
	
	private void setAutoCommitFalse() throws SQLException {
		conn.setAutoCommit(false);
		stmt = conn.createStatement();
	}
	
	private void commit() throws SQLException {
		conn.commit();
		stmt = conn.createStatement();
	}

	public boolean close() {
		logger.info("[Module begin] MySQLService.close, Parameters: N/A");
		try {
			stmt.close();
			conn.close();
			logger.info("[Module end] MySQLService.close");
			return true;
		} catch (SQLException e) {
			logger.error("MySQLService.close error: " + e);
			return false;
		}
	}
}
