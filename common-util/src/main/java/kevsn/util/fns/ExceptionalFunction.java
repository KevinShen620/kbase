/*
 * @(#) ExceptionAbleFunction.java 2015年5月14日
 *
 */
package kevsn.util.fns;

import java.util.function.Function;



/**
 * 
 * @author Kevin
 *
 */
@FunctionalInterface
public interface ExceptionalFunction<T, R, E extends Throwable> {

	R apply(T t) throws E;

	/**
	 * wrap a ExceptionalFunction to a common function,if fun.apply throws
	 * exception,use {@link VAMSCException} instead
	 * 
	 * @param fun
	 * @return a function that doesn't declared exception throws
	 */
	static <T, R, E extends Throwable> Function<T, R> wrap(
			ExceptionalFunction<T, R, E> fun) {
		return t -> {
			try {
				return fun.apply(t);
			} catch (Throwable ex) {
				throw new RuntimeException(ex);
			}
		};
	}
}
