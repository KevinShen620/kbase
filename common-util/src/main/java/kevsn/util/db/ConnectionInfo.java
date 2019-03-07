/*
 * @(#) ConnectionInfo.java 2014-8-22
 *
 * Copyright (c) 2011  VAM Solutions Corp. All rights reserved.
 */
package kevsn.util.db;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Kevin
 * 
 */
public class ConnectionInfo {

	private Map<String, String> params;

	public static ConnectionInfo getLocalMysqlConnectionInfo(String userName,
			String password, String database) {
		ConnectionInfo conInfo = new ConnectionInfo();
		conInfo.setDefaultDb(database);
		conInfo.setHost("localhost");
		conInfo.setPassword(password);
		conInfo.setUserName(userName);
		assignMysqlInfo(conInfo);
		return conInfo;
	}

	public static ConnectionInfo getMysqlConnectionInfo(String host, String userName,
			String password, String defaultDb) {
		ConnectionInfo cinfo = new ConnectionInfo();
		cinfo.setDefaultDb(defaultDb);
		cinfo.setUserName(userName);
		cinfo.setPassword(password);
		cinfo.setHost(host);
		assignMysqlInfo(cinfo);
		return cinfo;
	}

	private static void assignMysqlInfo(ConnectionInfo cinfo) {
		cinfo.setProtocol(DBUtils.PROTOCOL_MYSQL);
		cinfo.setDriverClass(DBUtils.DRIVER_MYSQL);
		cinfo.setPort(3306);
	}

	private String userName;

	private String password;

	private String protocol;

	private String driverClass;

	private String defaultDb;

	private String host;

	private int port;

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the driverClass
	 */
	public String getDriverClass() {
		return StringUtils.isEmpty(driverClass) ? DBUtils.DRIVER_MYSQL : driverClass;
	}

	/**
	 * @param driverClass
	 *            the driverClass to set
	 */
	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	/**
	 * @return the url
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the url to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return StringUtils.isEmpty(protocol) ? DBUtils.PROTOCOL_MYSQL : protocol;
	}

	/**
	 * @param protocol
	 *            the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @return the defaultDb
	 */
	public String getDefaultDb() {
		return defaultDb;
	}

	/**
	 * @param defaultDb
	 *            the defaultDb to set
	 */
	public void setDefaultDb(String defaultDb) {
		this.defaultDb = defaultDb;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port <= 0 ? 3306 : port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	public String getParam(String key) {
		if (this.params == null) {
			return null;
		}
		return params.get(key);
	}

	public String getParamString() {
		if (this.params == null || this.params.isEmpty()) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		this.params.forEach((k, v) -> {
			builder.append(k).append("=").append(v).append(";");
		});
		if (builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}

	public void setParam(String key, String value) {
		if (this.params == null) {
			this.params = new HashMap<>();
		}
		this.params.put(key, value);
	}

	@Override
	public String toString() {
		return "ConnectionInfo [userName=" + userName + ", password=" + password
				+ ", protocol=" + protocol + ", driverClass=" + driverClass
				+ ", defaultDb="
				+ defaultDb + ", host=" + host + ", port=" + port + "]";
	}

}
