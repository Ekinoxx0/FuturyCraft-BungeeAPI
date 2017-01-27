package api.utils.concurrent;

/**
 * Created by SkyBeast on 27/01/17.
 */
@FunctionalInterface
public interface Loop
{
	void run() throws InterruptedException;

	static Loop of(Runnable runnable)
	{
		return runnable::run;
	}
}
