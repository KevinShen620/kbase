/*
 * @(#) DBUtils.java 2014-4-9
 *
 * Copyright (c) 2011  VAM Solutions Corp. All rights reserved.
 */
package kevsn.util.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.vamsc.util.exception.VAMSCException;

import kevsn.util.fns.ExceptionalConsumer;
import kevsn.util.fns.ExceptionalFunction;

/**
 * @author Kevin
 * 
 */
public class DBUtils {

	private static final Logger logger = Logger.getLogger(DBUtils.class.getName());

	public static String PROTOCOL_MYSQL = "jdbc:mysql";

	public static String PROTOCOL_POSTGRES = "jdbc:postgresql";

	public static String DRIVER_MYSQL = "com.mysql.jdbc.Driver";

	public static String DRIVER_POSTGRES = "org.postgresql.Driver";

	private DBUtils() {

	}

	public static <T> T queryResultSet(Connection conn,
			ExceptionalFunction<ResultSet, T, SQLException> resultHandler, String sql,
			Object... args) throws SQLException {
		if (args == null || args.length == 0) {
			try (Statement stat = conn.createStatement()) {
				ResultSet result = stat.executeQuery(sql);
				return resultHandler.apply(result);
			}
		}
		try (PreparedStatement stat = conn.prepareStatement(sql)) {
			for (int i = 0; i < args.length; ++i) {
				stat.setObject(i + 1, args[i]);
			}
			ResultSet resultSet = stat.executeQuery();
			return resultHandler.apply(resultSet);
		}
	}

	public static List<Map<String, Object>> buildMap(ResultSet resultset)
			throws SQLException {
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		ResultSetMetaData meta = resultset.getMetaData();
		while (resultset.next()) {
			Map<String, Object> resultMap = new HashMap<String, Object>();
			for (int i = 0; i < meta.getColumnCount(); ++i) {
				String columnName = meta.getColumnLabel(i + 1);
				Object obj = resultset.getObject(columnName);
				resultMap.put(columnName, obj);
			}
			mapList.add(resultMap);
		}
		return mapList;
	}

	/**
	 * 查询数据库，不会把异常转为{@link RuntimeException}
	 * 
	 * @param conn
	 * @param sql
	 * @param args
	 * @return
	 * @throws SQLException
	 */
	public static List<Map<String, Object>> queryExceptional(Connection conn, String sql,
			Object... args) throws SQLException {
		return queryResultSet(conn, resultSet -> buildMap(resultSet), sql, args);
	}

	public static List<Map<String, Object>> query(Connection conn, String sql,
			Object... args) throws QueryException {
		try {
			return queryExceptional(conn, sql, args);
		} catch (SQLException e) {
			throw new QueryException(e);
		}
	}

	/**
	 * 返回所有表名
	 * 
	 * @param con
	 * @param tableNameLike
	 *            表名模式，%匹配多个字符，_匹配单个字符
	 * 
	 * @return
	 */
	public static List<String> getTableNamesLike(Connection con, String tableNameLike) {
		DatabaseMetaData metaData;
		try {
			metaData = con.getMetaData();
			ResultSet resultSet = metaData.getTables(null, null, tableNameLike, null);
			List<String> list = new ArrayList<String>();
			while (resultSet.next()) {
				String tableName = resultSet.getString("TABLE_NAME");
				list.add(tableName);
			}
			return list;
		} catch (Exception e) {
			throw new VAMSCException(e);
		}
	}

	/**
	 * 返回所有有的数据库表
	 * 
	 * @param con
	 * @return
	 */
	public static List<String> getTableNames(Connection con) {
		DatabaseMetaData metaData;
		try {
			metaData = con.getMetaData();
			ResultSet resultSet = metaData.getTables(null, null, null, null);
			List<String> list = new ArrayList<String>();
			while (resultSet.next()) {
				String tableName = resultSet.getString("TABLE_NAME");
				list.add(tableName);
			}
			return list;
		} catch (Exception e) {
			throw new VAMSCException(e);
		}
	}

	/**
	 * 根据表名的正则表达式获取表名列表
	 * 
	 * @param con
	 * @param tableNameRegex
	 * @return
	 */
	public static List<String> getTableNamesByRegex(Connection con,
			String tableNameRegex) {
		Pattern pattern = Pattern.compile(tableNameRegex);
		DatabaseMetaData metaData;
		try {
			metaData = con.getMetaData();
			ResultSet resultSet = metaData.getTables(null, null, null, null);
			List<String> list = new ArrayList<String>();
			while (resultSet.next()) {
				String tableName = resultSet.getString("TABLE_NAME");
				Matcher m = pattern.matcher(tableName);
				if (m.matches()) {
					list.add(tableName);
				}
			}
			return list;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param conn
	 * @param schemaName
	 * @param tableName
	 * @return 对应表各个列的数据类型,key为列名，value为数据类型，对应{@link Types}
	 */
	public static Map<String, ColumnDataType> getTableColumnDataTypes(Connection conn,
			String schemaName, String tableName) {
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("select * from ");
			if (StringUtils.isEmpty(schemaName)) {
				sql.append(schemaName).append(".");
			}
			sql.append(tableName).append(" where 1=0");
			Statement state = conn.createStatement();
			try (ResultSet result = state.executeQuery(sql.toString())) {
				ResultSetMetaData meta = result.getMetaData();
				Map<String, ColumnDataType> map = new HashMap<String, ColumnDataType>();
				for (int i = 0; i < meta.getColumnCount(); ++i) {
					String colName = meta.getColumnLabel(i + 1);
					int dataType = meta.getColumnType(i + 1);
					String typeName = meta.getColumnTypeName(i + 1);
					map.put(colName, new ColumnDataType(dataType, typeName));
				}
				result.close();
				return map;
			}
		} catch (Exception e) {
			throw new VAMSCException(e);
		}
	}

	public static List<Map<String, ColumnData>> queryColumnDatas(Connection conn,
			String sql,
			Object... args) {
		try {
			PreparedStatement state = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; ++i) {
				state.setObject(i + 1, args[i]);
			}
			try (ResultSet resultset = state.executeQuery()) {
				List<Map<String, ColumnData>> mapList = new ArrayList<Map<String, ColumnData>>();
				ResultSetMetaData meta = resultset.getMetaData();
				while (resultset.next()) {
					Map<String, ColumnData> map = new HashMap<String, ColumnData>();
					for (int i = 0; i < meta.getColumnCount(); ++i) {
						String columnName = meta.getColumnLabel(i + 1);
						ColumnData cdata = new ColumnData();
						Object obj = resultset.getObject(columnName);
						cdata.setValue(obj);
						cdata.setDataType(meta.getColumnType(i + 1));
						cdata.setDataTypeName(meta.getColumnTypeName(i + 1));
						map.put(columnName, cdata);
					}
					mapList.add(map);
				}
				resultset.close();
				return mapList;
			}
		} catch (Exception e) {
			throw new VAMSCException(e);
		}
	}

	public static int executeUpdateExceptional(Connection conn, String sql,
			Object... args)
			throws SQLException {
		try (PreparedStatement state = conn.prepareStatement(sql)) {
			for (int i = 0; i < args.length; ++i) {
				state.setObject(i + 1, args[i]);
			}
			return state.executeUpdate();
		}
	}

	public static int executeUpdate(Connection conn, String sql, Object... args) {
		try {
			return executeUpdateExceptional(conn, sql, args);
		} catch (SQLException e) {
			throw new VAMSCException(e);
		}
	}

	public static int executeUpdateAndClose(Connection conn, String sql, Object... args) {
		try (Connection _con = conn;
				PreparedStatement state = _con.prepareStatement(sql)) {
			for (int i = 0; i < args.length; ++i) {
				state.setObject(i + 1, args[i]);
			}
			return state.executeUpdate();
		} catch (Exception e) {
			throw new VAMSCException(e);
		}
	}

	public static int[] executeUpdatesAndClose(Connection conn, String... sqls) {
		try (Connection _con = conn; Statement stat = _con.createStatement()) {
			for (String sql : sqls) {
				stat.addBatch(sql);
			}
			return stat.executeBatch();
		} catch (SQLException e) {
			throw new VAMSCException(e);
		}
	}

	public static void doInConnection(Connection conn, boolean closeAfterFinish,
			ExceptionalConsumer<Connection, ?> consumer) {
		try {
			consumer.accept(conn);
		} catch (Throwable e) {
			throw new VAMSCException(e);
		} finally {
			if (closeAfterFinish) {
				closeQuietly(conn);
			}
		}
	}

	public static void doInConnection(Connection conn,
			ExceptionalConsumer<Connection, ?> consumer) {
		doInConnection(conn, false, consumer);
	}

	public static void doInConnectionAndClose(Connection conn,
			ExceptionalConsumer<Connection, ?> consumer) {
		doInConnection(conn, true, consumer);
	}

	public static void doInTransactionAndClose(Connection conn,
			ExceptionalConsumer<Connection, ?> consumer) throws VAMSCException {
		doInTransaction(conn, true, consumer);
	}

	public static <R> R doInTransactionAndCloseGet(Connection conn,
			ExceptionalFunction<Connection, R, ?> supplier) throws VAMSCException {
		return doInTransactionGet(conn, true, supplier);
	}

	public static void doInTransaction(Connection conn, boolean closeAfterFinish,
			ExceptionalConsumer<Connection, ?> consumer) throws VAMSCException {
		try {
			conn.setAutoCommit(false);
			consumer.accept(conn);
			conn.commit();
		} catch (Throwable e) {
			throw new VAMSCException(e);
		} finally {
			if (closeAfterFinish) {
				closeQuietly(conn);
			}
		}
	}

	public static <R> R doInTransactionGet(Connection conn, boolean closeAfterFinish,
			ExceptionalFunction<Connection, R, ?> function) throws VAMSCException {
		try {
			conn.setAutoCommit(false);
			R rst = function.apply(conn);
			conn.commit();
			return rst;
		} catch (Throwable e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				throw new VAMSCException(e1);
			}
			throw new VAMSCException(e);
		} finally {
			if (closeAfterFinish) {
				closeQuietly(conn);
			}
		}
	}

	public static Connection getConnection(ConnectionInfo cinfo) {
		return getConnection(cinfo.getDriverClass(), cinfo.getProtocol(), cinfo.getHost(),
				cinfo.getPort(), cinfo.getDefaultDb(), cinfo.getUserName(),
				cinfo.getPassword(), cinfo.getParamString());
	}

	private static Connection getConnection(String driver, String protocol, String host,
			Integer port, String defaultDb, String userName, String password,
			String paramString) {
		try {
			Class.forName(driver);
			StringBuilder url = new StringBuilder();
			url.append(protocol).append("://");
			if (StringUtils.isEmpty(host)) {
				url.append("localhost");
			} else {
				url.append(host);
			}
			String _port = (port == null || port < 0) ? null : String.valueOf(port);
			if (StringUtils.isEmpty(_port)) {
				if (StringUtils.equals(protocol, PROTOCOL_MYSQL)) {
					_port = "3306";
				} else if (StringUtils.equals(protocol, PROTOCOL_POSTGRES)) {
					_port = "5432";
				} else {
					throw new RuntimeException("null port");
				}
			}
			url.append(":").append(_port).append("/");
			if (StringUtils.isNotEmpty(defaultDb)) {
				url.append(defaultDb);
			}
			if (StringUtils.isNotEmpty(paramString)) {
				url.append("?").append(paramString);
			}
			return getConnection(driver, url.toString(), userName, password);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static Connection getConnection(String driver, String url, String userName,
			String password) {
		try {
			Class.forName(driver);
			return DriverManager.getConnection(url, userName, password);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static void closeQuietly(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				throw new VAMSCException(e);
			}
		}
	}

	/**
	 * 
	 * @param connection
	 * @param table
	 * @param checkExists
	 *            删除前是否需要检查
	 * @return
	 */
	public static void dropTable(Connection connection, String table) {
		try {
			Statement stat = connection.createStatement();
			stat.executeUpdate("drop table if exists " + table);
			if (logger.isLoggable(Level.INFO)) {
				logger.info("drop table " + table);
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 删除数据
	 * 
	 * @param connection
	 * @param table
	 */
	public static int clearTable(Connection connection, String table) {
		try {
			Statement stat = connection.createStatement();
			int i = stat.executeUpdate("delete from " + table);
			if (logger.isLoggable(Level.INFO)) {
				logger.info("delete table " + table + ",count:" + i);
			}
			return i;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static void rollBack(Connection connection) {
		try {
			connection.rollback();
		} catch (SQLException e) {
			throw new VAMSCException(e);
		}
	}

}
