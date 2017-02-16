package api.utils;

import net.md_5.bungee.api.ProxyServer;

/**
 * A simple manager.
 */
public interface SimpleManager
{
	/**
	 * Start the manager - this method calls init and rethrows exceptions.
	 */
	default void start()
	{
		try
		{
			init();
		}
		catch (Exception e)
		{
			ProxyServer.getInstance().stop(); //Server unstable now :(
			throw new ManagerException("Error while loading manager " + getClass().getSimpleName() + '.', e);
		}
	}

	/**
	 * Init the manager.
	 * Should never be called twice.
	 */
	@SuppressWarnings("ProhibitedExceptionDeclared")
	default void init() throws Exception {}

	/**
	 * Stop the manager.
	 * Should never be called twice.
	 */
	default void stop() {}
}
