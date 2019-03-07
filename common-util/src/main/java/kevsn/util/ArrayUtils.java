/*
 * @(#) ArrayIter.java 2015年5月1日
 *
 */
package kevsn.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

import kevsn.util.fns.ExceptionalFunction;

/**
 * @author Kevin
 *
 */
public class ArrayUtils {

	private ArrayUtils() {

	}

	/**
	 * for each array
	 * 
	 * @param array
	 *            the array to foreach
	 * @param consumer
	 *            the first param is array index,the second is the element
	 */
	public static <T> void foreachElement(T[] array, BiConsumer<Integer, T> consumer) {
		for (int i = 0; i < array.length; ++i) {
			T obj = array[i];
			consumer.accept(i, obj);
		}
	}

	/**
	 * convert oriArray using function
	 * 
	 * @param oriArray
	 *            oriArray
	 * @param function
	 *            convert array's element
	 * @return new Array
	 */
	@SuppressWarnings("unchecked")
	public static <T, U> U[] mapArray(Class<U> elementType, T[] oriArray,
			Function<T, U> function) {
		U[] newArray = (U[]) Array.newInstance(elementType, oriArray.length);
		for (int i = 0; i < oriArray.length; ++i) {
			newArray[i] = function.apply(oriArray[i]);
		}
		return newArray;
	}

	@SuppressWarnings({ "unchecked", "cast" })
	public static <T, U, E extends Throwable> U[] mapArrayExceptional(
			Class<U> elementType,
			T[] oriArray, ExceptionalFunction<T, U, E> function) throws E {
		U[] newArray = (U[]) Array.newInstance(elementType, oriArray.length);
		for (int i = 0; i < oriArray.length; ++i) {
			newArray[i] = function.apply(oriArray[i]);
		}
		return (U[]) newArray;
	}

	public static <T> T[] asArray(Class<T> elementType, Collection<T> collection) {
		@SuppressWarnings("unchecked")
		T[] array = (T[]) Array.newInstance(elementType, collection.size());
		int i = 0;
		for (T t : collection) {
			array[i++] = t;
		}
		return array;
	}
}
