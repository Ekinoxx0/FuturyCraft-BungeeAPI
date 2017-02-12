package api.utils.concurrent;

/**
 * This exception is thrown when a Thread is interrupted but not stopping.
 * This exception can only be thrown when a InterruptedException is thrown from a Loop, so only internally.
 */
public class ThreadLoopException extends RuntimeException
{
	/**
	 * {@inheritDoc}
	 */
	public ThreadLoopException() {}

	/**
	 * {@inheritDoc}
	 */
	public ThreadLoopException(String var1) {super(var1);}

	/**
	 * {@inheritDoc}
	 */
	public ThreadLoopException(String var1, Throwable var2) {super(var1, var2);}

	/**
	 * {@inheritDoc}
	 */
	public ThreadLoopException(Throwable var1) {super(var1);}
}
