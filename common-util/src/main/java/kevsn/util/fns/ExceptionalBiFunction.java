/*
 * @(#) ExceptionalBiFunction.java 2015年5月14日
 *
 */
package kevsn.util.fns;

import java.util.function.BiFunction;


/**
 * @author Kevin
 *
 */
@FunctionalInterface
public interface ExceptionalBiFunction<T1, T2, R, E extends Throwable> {

	R apply(T1 t1, T2 t2) throws E;

	// static <T1, T2, R> ExceptionalBiFunction<T1, T2, R, ? extends Throwable>
	// wrap(
	// BiFunction<T1, T2, R> function) {
	// return (t1, t2) -> function.apply(t1, t2);
	// }

	static <T1, T2, R> BiFunction<T1, T2, R> wrap(
			ExceptionalBiFunction<T1, T2, R, ? extends Throwable> function) {
		return (t1, t2) -> {
			try {
				return function.apply(t1, t2);
			} catch (Throwable e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		};
	}
}
