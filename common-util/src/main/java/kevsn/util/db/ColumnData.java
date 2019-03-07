/*
 * @(#) ColumnData.java 2014-4-10
 *
 * Copyright (c) 2011  VAM Solutions Corp. All rights reserved.
 */
package kevsn.util.db;

import java.sql.Types;

/**
 * @author Kevin
 * 
 */
public class ColumnData {

	private ColumnDataType type;

	private Object value;

	/**
	 * @see Types
	 * @return the dataType
	 */
	public int getDataType() {
		return type.getDataType();
	}

	public ColumnDataType getType() {
		return type;
	}

	/**
	 * @param dataType
	 *            the dataType to set
	 */
	public void setDataType(int dataType) {
		ensureType().setDataType(dataType);
	}

	private ColumnDataType ensureType() {
		if (type == null) {
			type = new ColumnDataType();
		}
		return type;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * @return the dataTypeName
	 */
	public String getDataTypeName() {
		return ensureType().getDataTypeName();
	}

	/**
	 * @param dataTypeName
	 *            the dataTypeName to set
	 */
	public void setDataTypeName(String dataTypeName) {
		ensureType().setDataTypeName(dataTypeName);
	}

	@Override
	public String toString() {
		return "ColumnData [type=" + type + ", value=" + value + "]";
	}

}
