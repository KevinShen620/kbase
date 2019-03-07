package kevsn.util.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vamsc.util.exception.RuntimeIoException;
import com.vamsc.util.exception.VAMSCException;

import kevsn.util.CommonStringUtils;
import kevsn.util.OsProcessUtil;
import kevsn.util.OsProcessUtil.ResultInfo;
import kevsn.util.OsUtils;

public class MysqlManageUtil {

	private static final Logger logger = LoggerFactory.getLogger(MysqlManageUtil.class);

	private MysqlManageUtil() {

	}

	public static void importSqlFile(ConnectionInfo conInfo, String mysqlCommand,
			String dbName, Path sqlFile, boolean dropIfExists) {
		if (!Files.exists(sqlFile)) {
			throw new VAMSCException("文件" + sqlFile + "不存在");
		}
		StringBuilder prepareCmd = new StringBuilder();
		if (dropIfExists) {
			_appendBasicCommand(prepareCmd, mysqlCommand, conInfo);
			prepareCmd.append(" --execute='drop database if exists `").append(dbName)
					.append("`;create database `").append(dbName).append("`;'");
		} else {
			_appendBasicCommand(prepareCmd, mysqlCommand, conInfo);
			prepareCmd.append(" --execute='create database if not exists `")
					.append(dbName).append("`;'");
		}
		String[] cmds = new String[] { "sh", "-c", prepareCmd.toString() };
		if (logger.isDebugEnabled()) {
			logger.debug("执行命令{},", "sh -c " + prepareCmd);
		}
		boolean ok = executeCommand(cmds);
		if (!ok) {
			logger.error("创建database{}失败", dbName);
			throw new VAMSCException("创建database " + dbName + "失败");
		}
		StringBuilder importCmd = new StringBuilder(50);
		_appendBasicCommand(importCmd, mysqlCommand, conInfo);
		importCmd.append(" ").append(dbName).append(" --execute='source ")
				.append(sqlFile.toAbsolutePath()).append("'");
		if (logger.isDebugEnabled()) {
			logger.debug("执行命令{}", importCmd);
		}
		ok = executeCommand(new String[] { "sh", "-c", importCmd.toString() });
		if (!ok) {
			logger.error("往{}导入sql文件出错", dbName);
			throw new VAMSCException("往" + dbName + "导入sql文件出错");
		}
	}

	private static boolean executeCommand(String[] commands) {
		Process process;
		try {
			process = Runtime.getRuntime().exec(commands);
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
		int exitCode;
		try {
			exitCode = process.waitFor();
		} catch (InterruptedException e) {
			throw new VAMSCException(e);
		}
		if (exitCode == 0) {
			logger.info("执行命令成功");
			if (logger.isDebugEnabled()) {
				ResultInfo info = OsProcessUtil.getExecutedProcessResultInfo(process,
						true, true);
				logger.debug("命令执行信息:{}", info);
			}
			return true;
		}
		if (logger.isErrorEnabled()) {
			ResultInfo rst = OsProcessUtil.getExecutedProcessResultInfo(process, true,
					true);
			logger.error("执行命令失败");
			logger.warn("命令执行信息{}", rst);
		}
		return false;
	}

	private static void _appendBasicCommand(StringBuilder builder, String mysqlCmd,
			ConnectionInfo cninfo) {
		builder.append(mysqlCmd).append(" ");
		String username = cninfo.getUserName();
		String password = cninfo.getPassword();
		String host = cninfo.getHost();
		if (StringUtils.isEmpty(host)) {
			host = "localhost";
		}
		int port = cninfo.getPort();
		// 第一步，获取登录命令语句
		builder.append(" -u").append(username);
		if (StringUtils.isNotEmpty(password)) {
			builder.append(" -p").append(password);
		}
		builder.append(" -h").append(host).append(" -P").append(port);
	}

	public static void changeRootPassword(Connection con, String rootPassword)
			throws SQLException {
		logger.info("修改root用户密码");
		logger.debug("root密码为{}", rootPassword);
		try (Statement state = con.createStatement()) {
			String passValue = "";
			if (StringUtils.isNotEmpty(rootPassword)) {
				passValue = "password('" + rootPassword + "')";
			}
			String sql1 = "set password for 'root'@'localhost'=" + passValue;
			state.addBatch(sql1);
			String sql2 = "set password for 'root'@'127.0.0.1'=" + passValue;
			state.addBatch(sql2);
			logger.debug(sql1);
			logger.debug(sql2);
			state.executeBatch();
		} catch (SQLException e) {
			logger.error("修改root密码出错", e);
			throw e;
		}
		logger.info("修改密码成功过");
	}

	public static void createUser(Connection con, String db, String userName,
			String password, String... hosts) throws SQLException {
		if (logger.isInfoEnabled()) {
			logger.info("创建用户{},并授权{}", userName,
					CommonStringUtils.joinString(hosts, ",", ""));
		}
		try (Statement stat = con.createStatement()) {
			for (String host : hosts) {
				StringBuilder sqlbuilder = new StringBuilder();
				sqlbuilder.append("grant all privileges on ").append(db).append(".* to '")
						.append(userName).append("'@'").append(host)
						.append("' identified by '").append(password).append("'");
				if (logger.isDebugEnabled()) {
					logger.debug(sqlbuilder.toString());
				}
				stat.addBatch(sqlbuilder.toString());
			}
			stat.executeBatch();
		} catch (SQLException e) {
			logger.error("创建用户{}出错", userName);
			throw e;
		}
	}

	/**
	 * 配置数据库备份
	 * 
	 * @param con
	 * @param syncConn
	 * @param dbName
	 * @throws SQLException
	 */
	public static void configBackUpMasterNode(Connection con, ConnectionInfo syncConn,
			String dbName) throws SQLException {
		logger.info("进行master节点的数据库备份配置，数据备份到{}", syncConn.getHost());
		String password = syncConn.getPassword();
		String syncUserName = syncConn.getUserName();
		String host = syncConn.getHost();
		String dname = StringUtils.isEmpty(dbName) ? "*" : dbName;
		String sql1 = "grant all privileges on " + dname + ".* to '" + syncUserName
				+ "'@'" + host + "' identified by '" + password + "'";
		String sql2 = "grant replication slave on *.* to '" + syncUserName + "'@'" + host
				+ "' identified by '" + password + "'";
		executeStats(con, "stop slave", sql1, sql2);
		logger.info("数据同步配置结束");
	}

	public static void configBackupSlaveNode(Connection con, ConnectionInfo syncConn,
			boolean startSlave) throws SQLException {
		logger.info("运行slave节点的数据库备份配置，master节点为", syncConn.getHost());
		String password = syncConn.getPassword();
		String syncUserName = syncConn.getUserName();
		String host = syncConn.getHost();
		String sql0 = "stop slave;";
		String sql3 = "change master to MASTER_HOST='" + host + "'," + " MASTER_USER='"
				+ syncUserName + "', MASTER_PASSWORD = '" + password + "',"
				+ "MASTER_PORT = 3306;";
		if (startSlave) {
			executeStats(con, sql0, sql3, "start slave");
		} else {
			executeStats(con, sql0, sql3);
		}
	}

	private static void executeStats(Connection con, String... sqls) throws SQLException {
		try (Statement stat = con.createStatement()) {
			for (String sql : sqls) {
				logger.debug(sql);
				stat.addBatch(sql);
			}
			stat.executeBatch();
		}
	}

	public static void configSync(Connection con, ConnectionInfo syncConn,
			boolean startSlave, String dbName) throws SQLException {
		logger.info("配置与{}的数据同步", syncConn.getHost());
		String password = syncConn.getPassword();
		String syncUserName = syncConn.getUserName();
		String host = syncConn.getHost();
		String sql0 = "stop slave;";
		String dname = StringUtils.isEmpty(dbName) ? "*" : dbName;
		String sql1 = "grant all privileges on " + dname + ".* to '" + syncUserName
				+ "'@'" + host + "' identified by '" + password + "'";
		String sql2 = "grant replication slave on *.* to '" + syncUserName + "'@'" + host
				+ "' identified by '" + password + "'";
		String sql3 = "change master to MASTER_HOST='" + host + "'," + " MASTER_USER='"
				+ syncUserName + "', MASTER_PASSWORD = '" + password + "',"
				+ "MASTER_PORT = 3306;";
		if (startSlave) {
			executeStats(con, sql0, sql1, sql2, sql3, "start slave;");
		} else {
			executeStats(con, sql0, sql1, sql2, sql3);
		}
		logger.debug("数据库同步配置结束");
	}

	public static void dropTables(Connection connection, String tablePattern) {
		List<String> tableNames = DBUtils.getTableNamesByRegex(connection, tablePattern);
		for (String table : tableNames) {
			DBUtils.dropTable(connection, table);
		}
	}

	public static int clearTables(Connection connection, String tablePattern) {
		List<String> tableNames = DBUtils.getTableNamesByRegex(connection, tablePattern);
		int total = 0;
		for (String table : tableNames) {
			int i = DBUtils.clearTable(connection, table);
			total += i;
		}
		return total;
	}

	/**
	 * @see #export(String, ExportType, ConnectionInfo, Path)
	 * @see #export(String, ExportType, ConnectionInfo, List, Path)
	 * @param mysqlDumpCmd
	 *            mysqlDumpCmd命令的路径
	 * @param connInfo
	 * @param tablePattern
	 * @param exportPath
	 */
	public static void exportDDL(ConnectionInfo connInfo, String mysqlDumpCmd,
			String tablePattern, Path exportPath) {
		Connection connection = null;
		try {
			connection = DBUtils.getConnection(connInfo);
			List<String> tableNames = null;
			if (StringUtils.isNotEmpty(tablePattern)) {
				tableNames = DBUtils.getTableNamesByRegex(connection, tablePattern);
			}
			_export(mysqlDumpCmd, ExportType.DDL, connInfo, tableNames, exportPath);
		} catch (Exception e) {
			throw new VAMSCException(e);
		} finally {
			if (connection != null) {
				DBUtils.closeQuietly(connection);
			}
		}
	}

	public static void exportData(ConnectionInfo connInfo, String mysqlDumpCmd,
			String tablePattern, Path exportPath) {
		try (Connection connection = DBUtils.getConnection(connInfo)) {
			List<String> tableNames = DBUtils.getTableNamesByRegex(connection,
					tablePattern);
			_export(mysqlDumpCmd, ExportType.DATA, connInfo, tableNames, exportPath);
		} catch (IOException | InterruptedException | SQLException e) {
			throw new VAMSCException(e);
		}
	}

	/**
	 * 
	 * @param conInfo
	 * @param tableNames
	 * @param exportPath
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void _export(String mysqlDumpCmd, ExportType type,
			ConnectionInfo conInfo, List<String> tableNames, Path exportPath)
			throws IOException, InterruptedException {
		String defaultDb = conInfo.getDefaultDb();
		if (StringUtils.isEmpty(defaultDb)) {
			throw new VAMSCException("Empty Db");
		}
		StringBuilder command = new StringBuilder();
		command.append(mysqlDumpCmd);
		String userName = conInfo.getUserName();
		if (StringUtils.isNotEmpty(userName)) {
			command.append(" -u").append(userName);
		}
		String password = conInfo.getPassword();
		if (StringUtils.isNotEmpty(password)) {
			command.append(" -p").append(password);
		}
		if (StringUtils.isNotEmpty(type.param)) {
			command.append(" ").append(type.param).append(" ");
		}
		command.append(defaultDb);
		if (tableNames != null) {
			for (String table : tableNames) {
				command.append(" ").append(table);
			}
		}
		command.append(">").append(exportPath);
		logger.debug("execute command {}", command);
		executeCommand(new String[] { "sh", "-c", command.toString() });
	}

	public static void export(String mysqlDumpCmd, ExportType type,
			ConnectionInfo conInfo, List<String> tableNames, Path exportPath) {
		try {
			_export(mysqlDumpCmd, type, conInfo, tableNames, exportPath);
		} catch (IOException | InterruptedException e) {
			throw new VAMSCException(e);
		}
	}

	public static void export(String mysqlDumpCmd, ExportType type,
			ConnectionInfo conInfo, Path exportPath) {
		try {
			_export(mysqlDumpCmd, type, conInfo, null, exportPath);
		} catch (IOException | InterruptedException e) {
			throw new VAMSCException(e);
		}
	}

	public static enum ExportType {

		DDL("-d"), DATA("-t"), BOTH("");

		private String param;

		private ExportType(String param) {
			this.param = param;
		}

	}

	public static void main(String[] args) {
		Path homePath = Paths.get(OsUtils.getUserHomeDir());
		ConnectionInfo con = ConnectionInfo.getLocalMysqlConnectionInfo("root", "root11",
				"base3");
		String cmd = "/usr/local/mysql/bin/mysqldump";
		export(cmd, ExportType.DDL, con, homePath.resolve("base_ddl.sql"));
		export(cmd, ExportType.DATA, con, homePath.resolve("base_data.sql"));
	}
}
