package api.utils.concurrent;

/**
 * Created by SkyBeast on 18/12/2016.
 */
@FunctionalInterface
public interface Callback<T>
{
	void response(T t);
}
