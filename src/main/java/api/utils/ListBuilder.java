package api.utils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by SkyBeast on 07/01/2017.
 */
@ToString
@EqualsAndHashCode
@Getter
public class ListBuilder<E>
{
	private final List<E> list;
	private static final Collector COLLECTOR = Collector.of(ListBuilder::new, ListBuilder::append,
			ListBuilder::addAll);
	private boolean built;

	public ListBuilder(List<E> list)
	{
		this.list = list;
	}

	public ListBuilder()
	{
		list = new ArrayList<>();
	}

	public ListBuilder(int size)
	{
		list = new ArrayList<>(size);
	}

	public ListBuilder(ListBuilder<E> clone)
	{
		list = new ArrayList<>(clone.list);
	}

	@SuppressWarnings("unchecked")
	public static <E> Collector<E, ListBuilder<E>, ListBuilder<E>> collector()
	{
		return COLLECTOR;
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <E> List<E> immutableOf(E... elements)
	{
		if (elements.length == 0)
			return Collections.emptyList();
		else if (elements.length == 1)
			return Collections.singletonList(elements[0]);

		return Arrays.asList(elements);
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <E> List<E> listOf(E... elements)
	{
		return Stream.of(elements).collect(Collectors.toList());
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <E> ListBuilder<E> of(E... elements)
	{
		return Stream.of(elements).collect(collector());
	}

	public static <E> ListBuilder<E> size(int size)
	{
		return new ListBuilder<>(size);
	}

	public ListBuilder<E> append(E element)
	{
		if (built) throw new IllegalStateException("List already built!");
		list.add(element);
		return this;
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	public final ListBuilder<E> appendAll(E... elements)
	{
		if (built) throw new IllegalStateException("List already built!");
		Stream.of(elements).forEach(list::add);
		return this;
	}

	public ListBuilder<E> set(int index, E element)
	{
		if (built) throw new IllegalStateException("List already built!");
		list.set(index, element);
		return this;
	}

	public ListBuilder<E> addAll(ListBuilder<E> builder)
	{
		if (built) throw new IllegalStateException("List already built!");
		list.addAll(builder.list);
		return this;
	}

	public ListBuilder<E> addAll(Collection<E> coll)
	{
		if (built) throw new IllegalStateException("List already built!");
		list.addAll(coll);
		return this;
	}

	public List<E> build()
	{
		if (built) throw new IllegalStateException("List already built!");
		built = true;
		return list;
	}

	public List<E> immutable()
	{
		if (built) throw new IllegalStateException("List already built!");
		built = true;
		if (list.isEmpty())
			return Collections.emptyList();
		else if (list.size() == 1)
			return Collections.singletonList(list.get(0));

		return Collections.unmodifiableList(list);
	}
}
