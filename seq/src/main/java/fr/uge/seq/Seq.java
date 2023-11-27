package fr.uge.seq;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Seq<T> extends Iterable<T>{

	@SuppressWarnings("unchecked")
	public static <T> Seq<T> from(List<? extends T> list) {
		Objects.requireNonNull(list);
		return new SeqImpl<>(List.copyOf(list), e ->(T) e);
	}
	
	int size();
	
	T get(int index);
	
	<R>Seq<R> map(Function<? super T,? extends R>function);

	<R>Optional<R> findFirst();

	@SafeVarargs
	public static <T>Seq<T> of(T... values) {
		Objects.requireNonNull(values);
		return from(List.of(values));
	}
	
	Stream<T> stream();
}
