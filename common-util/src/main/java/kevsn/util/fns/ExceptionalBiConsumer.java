/*
 * @(#) ExceptionalBiConsumer.java 2015年5月14日
 *
 */
package kevsn.util.fns;

import java.util.function.BiConsumer;

/**
 * @author Kevin
 *
 */
@FunctionalInterface
public interface ExceptionalBiConsumer<T1, T2, E extends Throwable> {

	void accept(T1 t1, T2 t2) throws E;

	static <T1, T2> BiConsumer<T1, T2> wrap(
			ExceptionalBiConsumer<T1, T2, ? extends Throwable> consumer, T1 t1,
			T2 t2) {
		return new BiConsumer<T1, T2>() {

			@Override
			public void accept(T1 t, T2 u) {
				try {
					consumer.accept(t1, t2);
				} catch (Throwable e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		};
	}
}
