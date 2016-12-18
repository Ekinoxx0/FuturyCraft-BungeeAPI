package api.packets;

/**
 * Created by SkyBeast on 18/12/2016.
 */
public interface Callback<T>
{
	void response(T t);
}
