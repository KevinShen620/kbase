/*
 * @(#)VAMSCCheckedException.java 2011-9-15
 *
 * Copyright (c) 2011  VAM Solutions Corp. All rights reserved.
 */
package com.vamsc.util.exception;

/**
 * 系统的受控异常，为了方便做全局控制，建议所有系统的受控异常都从它继承
 * 
 * @author Kevin
 * 
 */
public class VAMSCCheckedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3577601644402000777L;

	public VAMSCCheckedException() {
		super();
	}

	public VAMSCCheckedException(String message) {
		super(message);
	}

	public VAMSCCheckedException(String message, Throwable cause) {
		super(message, cause);
	}

	public VAMSCCheckedException(Throwable cause) {
		super(cause);
	}
}
