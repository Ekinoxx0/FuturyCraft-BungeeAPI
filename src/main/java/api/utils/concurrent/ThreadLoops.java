package api.utils.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Created by SkyBeast on 27/01/17.
 */
public final class ThreadLoops
{
	private ThreadLoops()
	{
		throw new InstantiationError("You cannot instantiate me! :p");
	}

	/**
	 * Create a new InfiniteThreadLoop.
	 * <p>
	 * This ThreadLoop will call the loop until stopped.
	 *
	 * @param loop the Loop
	 * @return the new ThreadLoop
	 */
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
									runLoop();
							}
					);
		}

		void runLoop()
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

	/**
	 * Create a new ConditionThreadLoop.
	 * <p>
	 * This ThreadLoop will call the loop until stopped or the condition returned false.
	 *
	 * @param condition the condition which can stop the ThreadLoop
	 * @param loop      the Loop
	 * @return the new ThreadLoop
	 */
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
									runLoop();
							}
					);
		}
	}

	/**
	 * Create a new ScheduledThreadLoop.
	 * <p>
	 * This ThreadLoop will call the loop with a certain period until stopped.
	 *
	 * @param loop   the Loop
	 * @param period the period
	 * @param unit   the TimeUnit of the period
	 * @return the new ThreadLoop
	 */
	public static ThreadLoop newScheduledThreadLoop(Loop loop, long period, TimeUnit unit)
	{
		return new ScheduledThreadLoop(loop, period, period, unit);
	}

	/**
	 * Create a new ScheduledThreadLoop.
	 * <p>
	 * This ThreadLoop will call the loop with a certain period until stopped, once the initialDelay is passed.
	 *
	 * @param loop         the Loop
	 * @param initialDelay the initial delay
	 * @param period       the period
	 * @param unit         the TimeUnit of the period
	 * @return the new ThreadLoop
	 */
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
		boolean started;

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
			if (started)
				throw new IllegalStateException("Looper thread already started");
			started = true;

			executorService.scheduleWithFixedDelay
					(
							this::runLoop,
							initialDelay,
							period,
							unit
					);
		}

		void runLoop()
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

		@Override
		public void stop()
		{
			started = false;
			executorService.shutdown();
		}

		@Override
		public boolean isAlive()
		{
			return !executorService.isShutdown();
		}
	}


	/**
	 * Create a new ScheduledConditionThreadLoop.
	 * <p>
	 * This ThreadLoop will call the loop with a certain period until stopped or the condition returned false.
	 *
	 * @param condition the condition which can stop the ThreadLoop
	 * @param loop      the Loop
	 * @param period    the period
	 * @param unit      the TimeUnit of the period
	 * @return the new ThreadLoop
	 */
	public static ThreadLoop newScheduledConditionThreadLoop(BooleanSupplier condition, Loop loop, long period,
	                                                         TimeUnit unit)
	{
		return new ScheduledConditionThreadLoop(condition, loop, 0, period, unit);
	}


	/**
	 * Create a new ScheduledConditionThreadLoop.
	 * <p>
	 * This ThreadLoop will call the loop with a certain period until stopped or the condition returned false, once the
	 * initialDelay is passed.
	 *
	 * @param condition    the condition which can stop the ThreadLoop
	 * @param loop         the Loop
	 * @param initialDelay the initial delay
	 * @param period       the period
	 * @param unit         the TimeUnit of the period
	 * @return the new ThreadLoop
	 */
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
			if (started)
				throw new IllegalStateException("Looper thread already started");
			started = true;

			executorService.scheduleWithFixedDelay
					(
							() ->
							{
								if (condition.getAsBoolean())
									runLoop();
							},
							initialDelay,
							period,
							unit
					);
		}
	}
}
