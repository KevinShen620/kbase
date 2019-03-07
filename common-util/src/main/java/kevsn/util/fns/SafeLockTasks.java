/*
 * @(#) SafeLockTask.java 2015年6月9日
 *
 */
package kevsn.util.fns;

import java.util.concurrent.locks.Lock;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Kevin
 *
 */
public class SafeLockTasks {

	public static void executeTask(Lock lock, Procedure task) {
		try {
			_lock(lock);
			task.execute();
		} finally {
			_unlock(lock);
		}
	}

	public static <T, R> R executeFunction(Lock lock, Function<T, R> function, T param) {
		try {
			_lock(lock);
			return function.apply(param);
		} finally {
			_unlock(lock);
		}
	}

	public static <T> void executeConsumer(Lock lock, Consumer<T> consumer, T param) {
		try {
			_lock(lock);
			consumer.accept(param);
		} finally {
			_unlock(lock);
		}
	}

	public static <T> T executeSupplier(Lock lock, Supplier<T> supplier) {
		try {
			_lock(lock);
			return supplier.get();
		} finally {
			_unlock(lock);
		}
	}

	private static void _lock(Lock lock) {
		lock.lock();
	}

	private static void _unlock(Lock lock) {
		lock.unlock();

	}
}
