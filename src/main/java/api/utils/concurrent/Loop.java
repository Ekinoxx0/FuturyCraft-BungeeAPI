package api.utils.concurrent;

/**
 * Loops are code which run multiple time, with the same context.
 * This class should only be used in ThreadLoops.
 * <p>
 * Use lambda expressions if you want.
 */
@FunctionalInterface
public interface Loop
{
	/**
	 * Run the loop once.
	 *
	 * @throws InterruptedException may be thrown when <code>ThreadLoop.stop()</code> is called from another thread
	 */
	void run() throws InterruptedException;

	/**
	 * Create a Loop from a Runnable.
	 *
	 * @param runnable the wrapped runnable
	 * @return the new Loop
	 */
	static Loop of(Runnable runnable)
	{
		return runnable::run;
	}
}
