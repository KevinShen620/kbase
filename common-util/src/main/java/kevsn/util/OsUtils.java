/*
 * @(#)OsUtils.java 2012-10-22
 *
 * Copyright (c) 2011  VAM Solutions Corp. All rights reserved.
 */
package kevsn.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vamsc.util.exception.VAMSCException;

import kevsn.util.OsProcessUtil.ResultInfo;

/**
 * @author Kevin
 * 
 */
public class OsUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(OsUtils.class);

	public static final boolean isWindows;

	public static final String LINE_SEP;

	public static final String OS_NAME;

	private OsUtils() {

	}

	static {
		OS_NAME = System.getProperty("os.name");
		isWindows = OS_NAME.toLowerCase().indexOf("windows") >= 0;
		LINE_SEP = System.getProperty("line.separator");

	}

	/**
	 * 判断当前是否为windows操作系统
	 * 
	 * @return
	 */
	public static final boolean isWindowsOs() {
		return isWindows;
	}

	/**
	 * 
	 * @return 系统的进程信息
	 */
	public static List<ProcessInfo> getProcessList() {
		assetsNotWindows();
		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {
			process = runtime.exec("ps auxc");
		} catch (IOException e) {
			throw new VAMSCException(e.getMessage(), e);
		}
		List<ProcessInfo> plist = new LinkedList<ProcessInfo>();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream()))) {
			reader.readLine();
			for (String line = reader.readLine(); StringUtils
					.isNotEmpty(line); line = reader.readLine()) {
				ProcessInfo p = createProcessInstance(line);
				plist.add(p);
			}
		} catch (Exception e) {
			throw new VAMSCException(e.getMessage(), e);

		}
		return plist;
	}

	/**
	 * 
	 * 根据进程名获取对应的进程信息，如果需要调查多个进程，进程名中间用都好隔开
	 * 
	 * @param names
	 * @return
	 */
	
	public static List<ProcessInfo> getProcessByName(String names) {
		String[] _names = StringUtils.split(names, ',');
		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {
			StringBuilder command = new StringBuilder();
			command.append("ps auxc|egrep ");
			if (_names.length > 1) {
				for (String n : _names) {
					command.append("\\(").append(n).append("\\)\\|");
				}
				command.delete(command.length() - 2, command.length());
			} else {
				command.append("ps aux|grep ").append(_names[0]);
			}
			process = runtime.exec(command.toString());
		} catch (Exception e) {
			throw new VAMSCException(e.getMessage(), e);
		}
		List<ProcessInfo> plist = new LinkedList<ProcessInfo>();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream()))) {
			boolean firstLine = true;
			for (String line = reader.readLine(); StringUtils
					.isNotEmpty(line); line = reader.readLine()) {
				boolean ignore = firstLine && line.matches("USER\\s+PID");
				if (!ignore) {
					ProcessInfo p = createProcessInstance(line);
					if (match(p.getName(), _names)) {
						plist.add(p);
					}
				}
				firstLine = false;

			}
		} catch (Exception e) {
			throw new VAMSCException(e.getMessage(), e);
		}
		return plist;
	}

	/**
	 * 根据pid返回进程
	 * 
	 * @param pids
	 *            进程号，如果有多个进程，中间用逗号隔开
	 * @return
	 */
	
	public static List<ProcessInfo> getProcessByPids(String pids) {
		assetsNotWindows();
		String[] _pids = StringUtils.split(pids, ',');
		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {
			StringBuilder command = new StringBuilder();
			if (_pids.length > 1) {
				command.append("ps aux|egrep ");
				command.append("\\(");
				for (String n : _pids) {
					command.append(n).append("\\|");
					// command.append("\\(").append(n).append("\\)\\|");
				}
				command.delete(command.length() - 2, command.length());
				command.append("\\)");
			} else {
				command.append("ps aux|grep ").append(_pids[0]);
			}
			String cmd = command.toString();
			LOGGER.info(cmd);
			String[] c = new String[] { "/bin/sh", "-c", cmd };
			process = runtime.exec(c);
		} catch (Exception e) {
			throw new VAMSCException(e.getMessage(), e);
		}
		List<ProcessInfo> plist = new LinkedList<ProcessInfo>();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream()))) {
			boolean firstLine = true;
			for (String line = reader.readLine(); StringUtils
					.isNotEmpty(line); line = reader.readLine()) {
				boolean ignore = firstLine && line.matches("USER\\s+PID");
				if (!ignore) {
					ProcessInfo p = createProcessInstance(line);
					if (match(String.valueOf(p.getPid()), _pids)) {
						plist.add(p);
					}
				}
				firstLine = false;

			}
		} catch (Exception e) {
			throw new VAMSCException(e.getMessage(), e);
		}
		return plist;

	}

	private static void assetsNotWindows() {
		if (isWindows) {
			LOGGER.error("暂时不支持windows");
			throw new VAMSCException("暂时不支持windows操作系统");
		}
	}

	private static ProcessInfo createProcessInstance(String line) {
		String[] splits = StringUtils.split(line);
		String user = splits[0];
		String pid = splits[1];
		String cpu = splits[2];
		String mem = splits[3];
		String name = splits[10];
		ProcessInfo p = new ProcessInfo();
		p.setPid(Integer.valueOf(pid));
		p.setUser(user);
		p.setCpuPercent(Double.valueOf(cpu));
		p.setMemeryPercent(Double.valueOf(mem));
		p.setName(name);
		return p;
	}

	private static boolean match(String value, String[] array) {
		for (String each : array) {
			if (StringUtils.equals(each, value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>
	 * 经测试，{@link InetAddress#isReachable(int)}这个方法不太靠谱，只能直接调用ping命令，进行网络联通性测试
	 * <p>
	 * 
	 * @param address
	 * @param timeout
	 *            timeout seconds
	 * @return
	 */
		public static boolean ping(String address, int timeout) {
		try {
			String command = "ping -W" + timeout + " -c1 " + address;
			LOGGER.info(command);
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
			return p.exitValue() == 0;
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * @see #ping(String, int)
	 * @param address
	 * @return
	 */
	
	public static boolean ping(String address) {
		assetsNotWindows();
		try {
			String command = "ping -c1 " + address;
			LOGGER.info(command);
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
			return p.exitValue() == 0;
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * 
	 * @return 0:成功重启,1:成功关闭服务，但是没有启动,2:关闭服务失败
	 */
	
	public static ServiceRestartResult restartService(String serviceName) {
		return _restartService(serviceName, "service " + serviceName + " stop",
				"service " + serviceName + " start");
	}

	
	public static boolean stopService(String serviceName) {
		Runtime runtime = Runtime.getRuntime();
		try {
			return _stopService(runtime, serviceName, "service " + serviceName + " stop");
		} catch (IOException | InterruptedException e) {
			LOGGER.error("停止服务{}出错", serviceName, e);
			return false;
		}

	}

	
	public static boolean startService(String serviceName) {
		Runtime runtime = Runtime.getRuntime();
		try {
			return _startService(runtime, serviceName,
					"service " + serviceName + " start");
		} catch (IOException | InterruptedException e) {
			LOGGER.error("停止服务{}出错", serviceName, e);
			return false;
		}
	}

	/**
	 * 重启服务
	 * 
	 * @param serviceName
	 * @param useInitd
	 *            是否采用init.d重启服务，如果为<code>true</code> ，调用重启命令/etc/init.d/serviceName
	 *            operation
	 * @return
	 */
	
	public static ServiceRestartResult restartService(String serviceName,
			boolean useInitd) {
		if (useInitd) {
			return _restartService(serviceName, "/etc/init.d/" + serviceName + " stop",
					"/etc/init.d/" + serviceName + " start");
		}
		return _restartService(serviceName, "service " + serviceName + " stop",
				"service " + serviceName + " start");
	}

	private static boolean _stopService(Runtime runtime, String serviceName,
			String stopCommand) throws IOException, InterruptedException {
		LOGGER.info("关闭服务:" + serviceName);
		Process process = runtime.exec(stopCommand);
		process.waitFor();
		int value = process.exitValue();
		if (value != 0) {
			if (LOGGER.isDebugEnabled()) {
				ResultInfo rst = OsProcessUtil.getExecutedProcessResultInfo(process, true,
						true);
				LOGGER.debug("关闭服务{}日志信息:{}", serviceName, rst);
			}
			LOGGER.error("关闭服务" + serviceName + "失败，返回值为" + value);
			return false;
		}
		return true;
	}

	private static boolean _startService(Runtime runtime, String serviceName,
			String startCommand) throws IOException, InterruptedException {
		LOGGER.info("启动服务{}", serviceName);
		Process p = runtime.exec(startCommand);
		int value = p.waitFor();
		if (value != 0) {
			if (LOGGER.isDebugEnabled()) {
				ResultInfo rst = OsProcessUtil.getExecutedProcessResultInfo(p, true,
						true);
				LOGGER.debug("启动服务{}日志信息：{}", serviceName, rst);
			}
			return false;
		}
		return true;
	}

	private static ServiceRestartResult _restartService(String serviceName,
			String stopCommand, String startCommand) {
		assetsNotWindows();
		Runtime runtime = Runtime.getRuntime();

		try {
			boolean ok = _stopService(runtime, serviceName, stopCommand);
			if (!ok) {
				return ServiceRestartResult.FAILED_STOP;
			}
		} catch (IOException | InterruptedException e) {
			LOGGER.error("关闭服务{}的时候发生错误", serviceName, e);
			return ServiceRestartResult.EXCEPTION;
		}

		try {
			boolean ok = _startService(runtime, serviceName, startCommand);
			if (!ok) {
				return ServiceRestartResult.FAILED_STOP;
			}
		} catch (IOException | InterruptedException e) {
			LOGGER.error("启动服务{}的时候发生错误", serviceName, e);
			return ServiceRestartResult.EXCEPTION;
		}
		return ServiceRestartResult.OK;
	}

	public static String getTmpDir() {
		return System.getProperty("java.io.tmpdir");
	}

	public static String getUserHomeDir() {
		return System.getProperty("user.home");
	}

	/**
	 * 
	 * @return 用户当前目录
	 */
	public static String getCurrentDir() {
		return System.getProperty("user.dir");
	}

	public static String getOsName() {
		return OS_NAME;
	}

	public static enum ServiceRestartResult {
		/**
		 * 成功重启
		 */
		OK,
		/**
		 * 关闭服务失败
		 */
		FAILED_STOP,
		/**
		 * 成功关闭服务，但是没有成功启动
		 */
		FAILED_START,
		/**
		 * 未知的例外
		 */
		EXCEPTION,
	}

	public static void main(String[] args) {
		System.out.println(getCurrentDir());
	}
}
