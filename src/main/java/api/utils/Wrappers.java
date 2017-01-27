package api.utils;

/**
 * Created by SkyBeast on 27/01/17.
 */
public enum Wrappers
{
	;

	public static <T> Wrapper<T> newMutableWrapper()
	{
		return new MutableWrapper<>(null);
	}

	public static <T> Wrapper<T> newMutableWrapper(T baseValue)
	{
		return new MutableWrapper<>(baseValue);
	}

	private static class MutableWrapper<T> implements Wrapper<T>
	{
		T value;

		MutableWrapper(T value) {this.value = value;}

		@Override
		public T set(T value)
		{
			T old = this.value;
			this.value = value;
			return old;
		}

		@Override
		public T get()
		{
			return value;
		}
	}

	public static <T> Wrapper<T> newImmutableWrapper(T baseValue)
	{
		return new ImmutableWrapper<>(baseValue);
	}

	private static class ImmutableWrapper<T> extends MutableWrapper<T>
	{
		ImmutableWrapper(T value) {super(value);}

		@Override
		public T set(T value)
		{
			throw new UnsupportedOperationException("Cannot set a value to an ImmutableWrapper");
		}
	}

	public static <T> Wrapper<T> newSingletonWrapper()
	{
		return new SingletonWrapper<>();
	}

	private static class SingletonWrapper<T> extends MutableWrapper<T>
	{
		boolean set;

		SingletonWrapper() {super(null);}

		@Override
		public T set(T value)
		{
			if (set)
				throw new UnsupportedOperationException("Value already initialized");

			this.value = value;
			set = true;

			return null;
		}
	}
}
