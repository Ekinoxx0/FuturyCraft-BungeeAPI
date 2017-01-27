package api.utils.concurrent;

/**
 * Created by SkyBeast on 27/01/17.
 */
public interface ThreadLoop
{
	void start();

	void stop();

	boolean isAlive();
}
