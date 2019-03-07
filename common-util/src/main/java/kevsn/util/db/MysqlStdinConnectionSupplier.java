/*
 * @(#) StdinConnectionSupplier.java 2014-8-22
 *
 * Copyright (c) 2011  VAM Solutions Corp. All rights reserved.
 */
package kevsn.util.db;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

/**
 * 从终端获取连接信息
 * 
 * @author Kevin
 * 
 */
public class MysqlStdinConnectionSupplier {

	private static final Logger logger = Logger
			.getLogger(MysqlStdinConnectionSupplier.class.getName());

	private MysqlStdinConnectionSupplier() {

	}

	public static ConnectionInfo getConnectionInfo() {
		ConnectionInfo con = null;
		try (Scanner scanner = new Scanner(System.in)) {
			System.out.print("host:");
			String url = scanner.nextLine();
			System.out.print("port:");
			String sport = scanner.nextLine();
			sport = StringUtils.isEmpty(sport) ? "3306" : sport;
			System.out.print("default db:");
			String defaultDb = scanner.nextLine();
			System.out.print("userName:");
			String userName = scanner.nextLine();
			System.out.print("password:");
			String password = scanner.nextLine();
			con = new ConnectionInfo();
			con.setDriverClass(DBUtils.DRIVER_MYSQL);
			con.setProtocol(DBUtils.PROTOCOL_MYSQL);
			con.setUserName(userName);
			con.setPassword(password);
			con.setHost(url);
			con.setPort(Integer.valueOf(sport));
			con.setDefaultDb(defaultDb);
			if (logger.isLoggable(Level.INFO)) {
				logger.info("connection:" + con);
			}
		}
		return con;
	}

}
