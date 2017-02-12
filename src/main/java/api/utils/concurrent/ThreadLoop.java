package api.utils.concurrent;

/**
 * This interface aims at generalizing ThreadLoops.
 * To create a ThreadLoop, use ThreadLoops.newXXXThreadLoop().
 * <p>
 * You may create custom implementations.
 */
public interface ThreadLoop
{
	/**
	 * Start the ThreadLoop.
	 * <p>
	 * Note: Do not start the ThreadLoop if the ThreadLoop is alive.
	 */
	void start();

	/**
	 * Stop the ThreadLoop.
	 * <p>
	 * Note: Do not stop the ThreadLoop if the ThreadLoop is not alive.
	 */
	void stop();

	/**
	 * Check whether or not the ThreadLoop is alive.
	 *
	 * @return whether or not the ThreadLoop is alive
	 */
	boolean isAlive();
}
