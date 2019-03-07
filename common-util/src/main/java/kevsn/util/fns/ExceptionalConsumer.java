/*
 * @(#) ExceptionalConsumer.java 2015年5月14日
 *
 */
package kevsn.util.fns;

import java.util.function.Consumer;



/**
 * @author Kevin
 *
 */
@FunctionalInterface
public interface ExceptionalConsumer<T, E extends Throwable> {

	void accept(T t) throws E;

	/**
	 * 
	 * wrap a {@link ExceptionalConsumer} common consumer,if the origin
	 * consumer.accept throws exception,use {@link VAMSCException} instead
	 * 
	 * @param consumer
	 * @return a consumer that doesn't declared exception throws
	 */
	static <T> Consumer<T> wrap(ExceptionalConsumer<T, ?> consumer) {
		return t -> {
			try {
				consumer.accept(t);
			} catch (Throwable e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		};
	}
}
