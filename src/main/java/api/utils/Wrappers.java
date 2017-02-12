package api.utils;

/**
 * Created by SkyBeast on 27/01/17.
 */
public final class Wrappers
{
	private static final Wrapper EMPTY_IMMUTABLE = new Wrapper()
	{
		@Override
		public Object get()
		{
			return null;
		}

		@Override
		public Object set(Object value)
		{
			throw new UnsupportedOperationException("Value already initialized");
		}
	};

	private Wrappers()
	{
		throw new InstantiationError("You cannot instantiate me! :p");
	}

	/**
	 * Create a new MutableWrapper with no value.
	 *
	 * @param <T> the type of the value which can be stored
	 * @return the new Wrapper
	 */
	public static <T> Wrapper<T> newMutableWrapper()
	{
		return new MutableWrapper<>(null);
	}

	/**
	 * Create a new MutableWrapper with a base value.
	 *
	 * @param baseValue the base value
	 * @param <T>       the type of the value which can be stored
	 * @return the new Wrapper
	 */
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

	/**
	 * Create a new MutableWrapper with a base value.
	 * <p>
	 * If the base value is null, emptyImmutable() is returned.
	 *
	 * @param baseValue the base value
	 * @param <T>       the type of the value which can be stored
	 * @return the new Wrapper
	 */
	public static <T> Wrapper<T> newImmutableWrapper(T baseValue)
	{
		if (baseValue == null)
			return emptyImmutable();
		return new ImmutableWrapper<>(baseValue);
	}

	/**
	 * Return the cached empty immutable instance.
	 *
	 * @param <T> the type of the value which can be stored
	 * @return the cached empty immutable instance
	 */
	@SuppressWarnings("unchecked")
	public static <T> Wrapper<T> emptyImmutable()
	{
		return EMPTY_IMMUTABLE;
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

	/**
	 * Create a new SingletonWrapper.
	 * <p>
	 * The value of the wrapper cannot be modified when set.
	 *
	 * @param <T> the type of the value which can be stored
	 * @return the new Wrapper
	 */
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
