/*
 * @(#)VAMSCException.java 2011-9-15
 *
 * Copyright (c) 2011  VAM Solutions Corp. All rights reserved.
 */
package com.vamsc.util.exception;

/**
 * 自定义的异常类
 * <p>
 * <p>
 * 在调用各类库函数的时候，总会碰上一些checkedException，然而这里面很多异常都是由于程序编码有误引起的，并非不可避免，
 * 而java编译器又强制程序员捕捉这类异常。碰上这种时候，api的调用者通常不知道该如何处理这类异常。
 * <p>
 * 本异常类的作用是封装普通不想处理的checkedExcption，代码如下
 * <p>
 * 
 * <pre>
 * try{
 *     obj.callMethod()
 *    }catch(Exception e){
 *    	throw new VAMSCException(e);
 *    }
 * </pre>
 * 
 * 这么处理既没有压制异常，也可以避免声明抛出一大堆的受控异常
 * 
 * @author Kevin
 * 
 */
public class VAMSCException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5802457311651425182L;

	public VAMSCException() {
		super();
	}

	public VAMSCException(String msg) {
		super(msg);
	}

	public VAMSCException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public VAMSCException(Throwable throwable) {
		super(throwable.getMessage(), throwable);
	}

	public static void throwException(String error, Throwable throwable) {
		VAMSCException e = new VAMSCException(error, throwable);
		throw e;
	}

	/**
	 * 尝试把一个checked exception转换为一个{@link RuntimeException},如果原异常本身就为
	 * {@link RuntimeException}，直接抛出,否则转换为{@link VAMSCException}
	 * 
	 * @param throwable
	 * @return
	 */
	public static RuntimeException tryConvertToRuntimeException(
			Throwable throwable) {
		if (throwable instanceof RuntimeException) {
			return (RuntimeException) throwable;
		}
		return new VAMSCException(throwable.getMessage(), throwable);
	}

	public static VAMSCException tryConvertToVamscException(Throwable throwable) {
		if (throwable instanceof VAMSCException) {
			return (VAMSCException) throwable;
		}
		return new VAMSCException(throwable);
	}

	public static void throwException(Throwable e) {
		if (e instanceof RuntimeException) {
			RuntimeException e0 = (RuntimeException) e;
			throw e0;
		}
		throw new VAMSCException(e.getMessage(), e);
	}

	public static void throwCheckException(Throwable e) {
		throw new VAMSCException(e);
	}
}
