package api.utils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by SkyBeast on 07/01/2017.
 */
@ToString
@EqualsAndHashCode
@Getter
public class MapBuilder<K, V>
{
	private final Map<K, V> map;
	private final Class<K> kClass;
	private final Class<V> vClass;
	private boolean built;

	public MapBuilder(Class<K> kClass, Class<V> vClass)
	{
		map = new HashMap<>();
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
		map = new HashMap<>(clone.map);
		kClass = clone.kClass;
		vClass = clone.vClass;
	}

	public static <K, V> MapBuilder<K, V> of(Class<K> kClass, Class<V> vClass, Object... pairs)
	{
		return new MapBuilder<>(kClass, vClass).appendAll(pairs);
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> mapOf(Class<K> kClass, Class<V> vClass, Object... pairs)
	{
		if ((pairs.length & 1) == 1) throw new IllegalArgumentException("Invalid key/value pairs");

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
		if ((pairs.length & 1) == 1) throw new IllegalArgumentException("Invalid key/value pairs");

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
		if (map.isEmpty())
			return Collections.emptyMap();
		return Collections.unmodifiableMap(map);
	}
}
