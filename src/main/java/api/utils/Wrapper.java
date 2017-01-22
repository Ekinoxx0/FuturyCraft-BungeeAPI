package api.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by SkyBeast on 18/12/2016.
 */
@Data
@NoArgsConstructor
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
