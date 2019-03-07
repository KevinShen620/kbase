/**
 * 
 */
package kevsn.util.fns;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 通过lambda循环容器时，先调用{@link #handleElement(Object)}，得到返回值，把这个返回值传入
 * {@link #isContinue(Object)}，检查是否继续迭代
 * 
 * @author kevin
 *
 */
public interface ForeachIterator<T, R> {

	R handleElement(T element);

	default boolean isContinue(R result) {
		return result == null;
	}

	public static <T, R> ForeachIterator<T, R> createIter(Consumer<T> consumer) {
		return new ForeachIterator<T, R>() {

			@Override
			public R handleElement(T element) {
				consumer.accept(element);
				return null;
			}

		};
	}

	public static <T, R> ForeachIterator<T, R> createIter(Function<T, R> function) {
		return new ForeachIterator<T, R>() {

			@Override
			public R handleElement(T element) {
				return function.apply(element);
			}
		};
	}

	public static <T> ForeachIterator<T, Boolean> createBoolResultIter(
			Function<T, Boolean> function) {
		return new ForeachIterator<T, Boolean>() {

			@Override
			public Boolean handleElement(T element) {
				Boolean bool = function.apply(element);
				Objects.requireNonNull(bool, "需要返回明确的boolean值");
				return bool;
			}

			@Override
			public boolean isContinue(Boolean result) {
				return result;
			}
		};
	}

	public static <T, R> ForeachIterator<T, R> createIter(Function<T, R> function,
			Predicate<R> predict) {
		return new ForeachIterator<T, R>() {

			@Override
			public R handleElement(T element) {
				return function.apply(element);
			}

			@Override
			public boolean isContinue(R result) {
				return predict.test(result);
			}
		};
	}

}
