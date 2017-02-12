package api.utils;

/**
 * This interface aims at generalizing Wrappers.
 * To create a ThreadLoop, use Wrappers.newXXXWrapper().
 * <p>
 * You may create custom implementations.
 *
 * @param <T> the type of the value which can be stored.
 */
public interface Wrapper<T>
{
	/**
	 * Get the wrapped object, or null if none.
	 *
	 * @return the wrapped object or null if none
	 */
	T get();

	/**
	 * Set the wrapped object.
	 *
	 * @param value the new value which can be null
	 * @return the old value or null if none
	 * @throws UnsupportedOperationException if the operation is not supported by the implementation
	 */
	T set(T value);

	/**
	 * Set the value only if the value is null.
	 *
	 * @param value the new value
	 */
	default void setIfNull(T value)
	{
		if (get() == null)
			set(value);
	}

	/**
	 * Reset the wrapped value.
	 */
	default void reset()
	{
		set(null);
	}
}
