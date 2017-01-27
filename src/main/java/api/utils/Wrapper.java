package api.utils;

/**
 * Created by SkyBeast on 18/12/2016.
 */
public interface Wrapper<T>
{
	T get();

	T set(T value);

	default void setIfNull(T value)
	{
		if (get() == null)
			set(value);
	}

	default void reset()
	{
		set(null);
	}
}
