package api.utils;

/**
 * Created by SkyBeast on 18/12/2016.
 */
public class Wrapper<T>
{
	private T instance;

	public T set(T newInstance)
	{
		T old = instance;
		instance = newInstance;
		return old;
	}

	public T get()
	{
		return instance;
	}
}
