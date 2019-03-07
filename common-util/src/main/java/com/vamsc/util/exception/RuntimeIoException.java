/*
 * @(#) RuntimeIoException.java 2015年6月19日
 *
 */
package com.vamsc.util.exception;

import java.io.IOException;

/**
 * {@link IOException}出现场景比较多，而大多数情况，捕捉这个异常后，也不知道该做什么，这个时候，可以考虑包装成
 * {@link RuntimeIoException}后抛出，这样，就简化了API的调用
 * <p>
 * 
 * @author Kevin
 *
 */
public class RuntimeIoException extends VAMSCException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7001526273404684687L;

	public RuntimeIoException(IOException io) {
		super(io.getMessage(), io);
	}

	public IOException getIoException() {
		return (IOException) getCause();
	}
}
