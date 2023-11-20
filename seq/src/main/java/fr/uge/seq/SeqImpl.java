package fr.uge.seq;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class SeqImpl <T,R> implements Seq<T>{
	
	private final List<R> selfList;
	private final Function<? super R,? extends T> selfFunction;
	private final int size;
	
	SeqImpl(List<R> list, Function<? super R,? extends T> function) {
		Objects.requireNonNull(list);
		Objects.requireNonNull(function);
		selfList = list;
		selfFunction = function;
		size = selfList.size();
	}

	@Override
	public int size() {
		return this.size;
	}
	
	@Override
	public T get(int index) {
		Objects.checkIndex(index, size());
		return selfFunction.apply(selfList.get(index));
	}
	
	public String toString() {
		return selfList.stream().map(e -> selfFunction.apply(e).toString()).collect(Collectors.joining(", ","<",">"));
	}

	@SuppressWarnings("hiding")
	@Override
	public <R>Seq<R> map(Function<? super T, ? extends R> function) {
		Objects.requireNonNull(function);
		return new SeqImpl<>(selfList, e -> function.apply(selfFunction.apply(e)));
	}
	
	@SuppressWarnings("unchecked")
	public Optional<R> findFirst() {
		return selfList.stream().findFirst().isEmpty()?Optional.empty(): selfList.stream().findAny();
	}

	public static <T> Spliterator<T> fromIterator(Iterator<? extends T> it){
		return new Spliterator<>() {

			@Override
			public boolean tryAdvance(Consumer<? super T> action) {
				if(!it.hasNext()) {
					return false;
				}
				action.accept(it.next());
				return true;
			}

			@Override
			public Spliterator<T> trySplit() {
				return null;
			}

			@Override
			public long estimateSize() {
				// TODO Auto-generated method stub
				return Long.MAX_VALUE;
			}

			@Override
			public int characteristics() {
				// TODO Auto-generated method stub
				return 0;
			}
			
		};
	}
	@Override
	public Stream<T> stream() {
		Spliterator<T> spliterator = fromIterator();
		return StreamSupport.stream(spliterator, true);
	}
}
