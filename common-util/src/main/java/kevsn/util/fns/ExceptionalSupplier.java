/*
 * @(#) ExceptionalSupplier.java 2015年5月14日
 *
 */
package kevsn.util.fns;

import java.util.function.Supplier;

/**
 * @author Kevin
 *
 */
@FunctionalInterface
public interface ExceptionalSupplier<T, E extends Throwable> {

	T get() throws E;

	static <T> Supplier<T> wrap(
			ExceptionalSupplier<T, ? extends Throwable> supplier) {
		return new Supplier<T>() {

			@Override
			public T get() {
				try {
					return supplier.get();
				} catch (Throwable e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		};
	}
}
