/*
 * @(#) Functions.java 2015年8月29日
 *
 */
package kevsn.util.fns;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Kevin
 *
 */
public class Lambdas {

	private static final Predicate<?> TRUE_PREDICT = obj -> true;

	private Lambdas() {

	}

	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> truePredict() {
		return (Predicate<T>) TRUE_PREDICT;
	}

	public static <T> Predicate<T> notPredict(Predicate<T> predict) {
		Predicate<T> p = new Predicate<T>() {

			@Override
			public boolean test(T t) {
				return !predict.test(t);
			}
		};
		return p;
	}

	public static <T> Supplier<T> convertToSupplier(T value) {
		return new ValueSupplier<>(value);
	}

	private static class ValueSupplier<T> implements Supplier<T> {

		private T value;

		public ValueSupplier(T value) {
			this.value = value;
		}

		@Override
		public T get() {
			return value;
		}

	}

}
