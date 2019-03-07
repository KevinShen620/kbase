/*
 * @(#) ExceptionalProcedure.java 2015年7月28日
 *
 */
package kevsn.util.fns;

/**
 * @author Kevin
 *
 */
public interface ExceptionalProcedure<T extends Throwable> {

	void execute() throws T;
}
