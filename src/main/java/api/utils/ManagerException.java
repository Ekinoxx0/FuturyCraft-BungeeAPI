package api.utils;

/**
 * An exception that can be thrown when the plugin enables.
 */
public class ManagerException extends RuntimeException
{
	/**
	 * {@inheritDoc}
	 */
	public ManagerException() {}

	/**
	 * {@inheritDoc}
	 */
	public ManagerException(String var1) {super(var1);}

	/**
	 * {@inheritDoc}
	 */
	public ManagerException(String var1, Throwable var2) {super(var1, var2);}

	/**
	 * {@inheritDoc}
	 */
	public ManagerException(Throwable var1) {super(var1);}
}
