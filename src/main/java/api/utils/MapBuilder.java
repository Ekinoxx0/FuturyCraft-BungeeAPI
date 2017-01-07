package api.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by SkyBeast on 07/01/2017.
 */
public class MapBuilder<K, V>
{
	private final Map<K, V> map;
	private final Class<K> kClass;
	private final Class<V> vClass;
	private boolean built = false;

	public MapBuilder(Class<K> kClass, Class<V> vClass)
	{
		this.map = new HashMap<>();
		this.kClass = kClass;
		this.vClass = vClass;
	}

	public MapBuilder(Map<K, V> map, Class<K> kClass, Class<V> vClass)
	{
		this.map = map;
		this.kClass = kClass;
		this.vClass = vClass;
	}

	public MapBuilder(MapBuilder<K, V> clone)
	{
		this.map = new HashMap<>(clone.map);
		this.kClass = clone.kClass;
		this.vClass = clone.vClass;
	}

	public static <K, V> MapBuilder<K, V> of(Class<K> kClass, Class<V> vClass, Object... pairs)
	{
		return new MapBuilder<>(kClass, vClass).appendAll(pairs);
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> mapOf(Class<K> kClass, Class<V> vClass, Object... pairs)
	{
		if (pairs.length % 2 == 1) throw new IllegalArgumentException("Invalid key/value pairs");

		Map<K, V> map = new HashMap<>();
		for (int i = 0; i < pairs.length; i += 2)
		{
			Object k = pairs[i];
			Object v = pairs[i + 1];

			if (k == null || !kClass.isInstance(k) || !vClass.isInstance(v))
				throw new IllegalStateException("Invalid key/value pairs");

			map.put((K) k, (V) v);

		}
		return map;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> immutableOf(Class<K> kClass, Class<V> vClass, Object... pairs)
	{
		return Collections.unmodifiableMap(mapOf(kClass, vClass, pairs));
	}

	public MapBuilder<K, V> append(K key, V value)
	{
		if (built) throw new IllegalStateException("Map already built!");
		map.put(key, value);
		return this;
	}

	@SuppressWarnings("unchecked")
	public MapBuilder<K, V> appendAll(Object... pairs)
	{
		if (built) throw new IllegalStateException("Map already built!");
		if (pairs.length % 2 == 1) throw new IllegalArgumentException("Invalid key/value pairs");

		for (int i = 0; i < pairs.length; i += 2)
		{
			Object k = pairs[i];
			Object v = pairs[i + 1];

			if (k == null || !kClass.isInstance(k) || !vClass.isInstance(v))
				throw new IllegalStateException("Invalid key/value pairs");

			append((K) k, (V) v);

		}
		return this;
	}

	public MapBuilder<K, V> addAll(MapBuilder<K, V> builder)
	{
		if (built) throw new IllegalStateException("Map already built!");
		map.putAll(builder.map);
		return this;
	}

	public MapBuilder<K, V> addAll(Map<K, V> map)
	{
		if (built) throw new IllegalStateException("Map already built!");
		this.map.putAll(map);
		return this;
	}

	public Map<K, V> getMap()
	{
		return map;
	}

	public Map<K, V> build()
	{
		if (built) throw new IllegalStateException("Map already built!");
		built = true;
		return map;
	}

	public Map<K, V> immutable()
	{
		if (built) throw new IllegalStateException("Map already built!");
		built = true;
		return Collections.unmodifiableMap(map);
	}

	@Override
	public String toString()
	{
		return "MapBuilder{" +
				"map=" + map +
				", kClass=" + kClass +
				", vClass=" + vClass +
				", built=" + built +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MapBuilder<?, ?> that = (MapBuilder<?, ?>) o;

		return built == that.built && (map != null ? map.equals(that.map) : that.map == null && (kClass != null ?
				kClass.equals(that.kClass) : that.kClass == null && (vClass != null ? vClass.equals(that.vClass) :
				that.vClass == null)));

	}

	@Override
	public int hashCode()
	{
		int result = map != null ? map.hashCode() : 0;
		result = 31 * result + (kClass != null ? kClass.hashCode() : 0);
		result = 31 * result + (vClass != null ? vClass.hashCode() : 0);
		result = 31 * result + (built ? 1 : 0);
		return result;
	}
}
