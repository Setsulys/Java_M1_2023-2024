package fr.uge.seq;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public interface Seq<T> {

	@SuppressWarnings("unchecked")
	public static <T>Seq<T> from(List<? extends T> list) {
		Objects.requireNonNull(list);
		list.stream().forEach(e -> Objects.requireNonNull(e));
		return new SeqImpl<T>(list, e ->(T) e);
	}
	int size();
	T get(int index);
	<E>Seq<T> map(Function<? super Object,? extends T> function);
}
