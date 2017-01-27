package api.utils.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Created by SkyBeast on 27/01/17.
 */
public enum ThreadLoops
{
	;

	public static ThreadLoop newInfiniteThreadLoop(Loop loop)
	{
		return new InfiniteThreadLoop(loop);
	}

	private static class InfiniteThreadLoop implements ThreadLoop
	{
		final Loop loop;
		volatile boolean end;
		final Thread looper = looper();

		InfiniteThreadLoop(Loop loop)
		{
			this.loop = loop;
		}

		Thread looper()
		{
			return new Thread
					(
							() ->
							{
								while (!end)
									runLoop(loop, end);
							}
					);
		}

		@Override
		public void start()
		{
			if (looper.isAlive())
				throw new IllegalStateException("Looper thread already started");

			looper.start();
		}

		@Override
		public void stop()
		{
			end = true;
			looper.interrupt();
		}

		@Override
		public boolean isAlive()
		{
			return looper.isAlive();
		}
	}

	public static ThreadLoop newConditionThreadLoop(BooleanSupplier condition, Loop loop)
	{
		return new ConditionThreadLoop(condition, loop);
	}

	private static class ConditionThreadLoop extends InfiniteThreadLoop
	{
		final BooleanSupplier condition;

		ConditionThreadLoop(BooleanSupplier condition, Loop loop)
		{
			super(loop);
			this.condition = condition;
		}

		Thread looper()
		{
			return new Thread
					(
							() ->
							{
								while (!end && condition.getAsBoolean())
									runLoop(loop, end);
							}
					);
		}
	}

	public static ThreadLoop newScheduledThreadLoop(Loop loop, long period, TimeUnit unit)
	{
		return new ScheduledThreadLoop(loop, 0, period, unit);
	}

	public static ThreadLoop newScheduledThreadLoop(Loop loop, long initialDelay, long period, TimeUnit unit)
	{
		return new ScheduledThreadLoop(loop, initialDelay, period, unit);
	}

	private static class ScheduledThreadLoop implements ThreadLoop
	{
		final Loop loop;
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		long initialDelay;
		long period;
		TimeUnit unit;

		ScheduledThreadLoop(Loop loop, long initialDelay, long period, TimeUnit unit)
		{
			this.loop = loop;
			this.initialDelay = initialDelay;
			this.period = period;
			this.unit = unit;
		}

		@Override
		public void start()
		{
			if (!executorService.isShutdown())
				throw new IllegalStateException("Looper thread already started");

			executorService.scheduleWithFixedDelay
					(
							() -> runLoop(loop),
							initialDelay,
							period,
							unit
					);
		}

		@Override
		public void stop()
		{
			executorService.shutdown();
		}

		@Override
		public boolean isAlive()
		{
			return !executorService.isShutdown();
		}
	}

	public static ThreadLoop newScheduledConditionThreadLoop(BooleanSupplier condition, Loop loop, long period,
	                                                         TimeUnit unit)
	{
		return new ScheduledConditionThreadLoop(condition, loop, 0, period, unit);
	}

	public static ThreadLoop newScheduledConditionThreadLoop(BooleanSupplier condition, Loop loop, long initialDelay,
	                                                         long period, TimeUnit unit)
	{
		return new ScheduledConditionThreadLoop(condition, loop, initialDelay, period, unit);
	}

	private static class ScheduledConditionThreadLoop extends ScheduledThreadLoop
	{
		final BooleanSupplier condition;

		ScheduledConditionThreadLoop(BooleanSupplier condition, Loop loop, long initialDelay, long period, TimeUnit
				unit)
		{
			super(loop, initialDelay, period, unit);
			this.condition = condition;
		}

		@Override
		public void start()
		{
			executorService.scheduleWithFixedDelay
					(
							() ->
							{
								if (condition.getAsBoolean())
									runLoop(loop);
							},
							initialDelay,
							period,
							unit
					);
		}
	}

	private static void runLoop(Loop loop, boolean end)
	{
		try
		{
			loop.run();
		}
		catch (InterruptedException e)
		{
			if (end)
				return;
			throw new ThreadLoopException(e);
		}
		catch (Exception e)
		{
			throw new ThreadLoopException(e);
		}
	}

	private static void runLoop(Loop loop)
	{
		try
		{
			loop.run();
		}
		catch (Exception e)
		{
			throw new ThreadLoopException(e);
		}
	}
}
