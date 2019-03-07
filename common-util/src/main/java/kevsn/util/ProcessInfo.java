/*
 * @(#) ProcessInfo.java 2013-3-19
 *
 * Copyright (c) 2011  VAM Solutions Corp. All rights reserved.
 */
package kevsn.util;

import java.io.Serializable;

/**
 * 进程信息
 * 
 * @author Kevin
 * 
 */
public class ProcessInfo implements Serializable, Comparable<ProcessInfo> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8960352837417672848L;

	private int pid;
	/**
	 * 运行进程的用户
	 */
	private String user;
	/**
	 * 进程名称
	 */
	private String processName;
	/**
	 * cpu 占用率
	 */
	private double cpuPercent;
	/**
	 * 内存占用率
	 */
	private double memeryPercent;

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getName() {
		return processName;
	}

	public void setName(String processName) {
		this.processName = processName;
	}

	public double getCpuPercent() {
		return cpuPercent;
	}

	public void setCpuPercent(double cpuPercent) {
		this.cpuPercent = cpuPercent;
	}

	public double getMemeryPercent() {
		return memeryPercent;
	}

	public void setMemeryPercent(double memeryPercent) {
		this.memeryPercent = memeryPercent;
	}

	@Override
	public int compareTo(ProcessInfo o) {
		if (o == null) {
			return 1;
		}
		return pid - o.pid;
	}

	@Override
	public String toString() {
		return "ProcessInfo [pid=" + pid + ", user=" + user + ", processName="
				+ processName + ", cpuPercent=" + cpuPercent
				+ ", memeryPercent=" + memeryPercent + "]";
	}

}
