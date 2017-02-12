package api.utils;

/**
 * A simple manager.
 */
public interface SimpleManager
{
	/**
	 * Init the manager.
	 * Should never be called twice.
	 */
	default void init() {}

	/**
	 * Stop the manager.
	 * Should never be called twice.
	 */
	default void stop() {}
}
