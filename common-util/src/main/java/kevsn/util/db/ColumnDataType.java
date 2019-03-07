/*
 * @(#) ColumnDataType.java 2014-4-10
 *
 * Copyright (c) 2011  VAM Solutions Corp. All rights reserved.
 */
package kevsn.util.db;

/**
 * @author Kevin
 * 
 */
public class ColumnDataType {

	private int dataType;

	private String dataTypeName;

	public ColumnDataType() {

	}

	public ColumnDataType(int dataType, String dataTypeName) {
		this.dataType = dataType;
		this.dataTypeName = dataTypeName;
	}

	/**
	 * @return the dataType
	 */
	public int getDataType() {
		return dataType;
	}

	/**
	 * @param dataType
	 *            the dataType to set
	 */
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the dataTypeName
	 */
	public String getDataTypeName() {
		return dataTypeName;
	}

	/**
	 * @param dataTypeName
	 *            the dataTypeName to set
	 */
	public void setDataTypeName(String dataTypeName) {
		this.dataTypeName = dataTypeName;
	}

	@Override
	public String toString() {
		return "ColumnDataType [dataType=" + dataType + ", dataTypeName="
				+ dataTypeName + "]";
	}

}
