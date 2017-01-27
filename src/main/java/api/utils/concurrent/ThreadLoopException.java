package api.utils.concurrent;

/**
 * Created by SkyBeast on 27/01/17.
 */
public class ThreadLoopException extends RuntimeException
{
	public ThreadLoopException() {}

	public ThreadLoopException(String var1) {super(var1);}

	public ThreadLoopException(String var1, Throwable var2) {super(var1, var2);}

	public ThreadLoopException(Throwable var1) {super(var1);}
}
