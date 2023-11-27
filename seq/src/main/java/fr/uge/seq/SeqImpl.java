package fr.uge.seq;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
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
	public Optional<T> findFirst() {
		return selfList.stream().findFirst().isEmpty()? Optional.empty():Optional.of(selfFunction.apply(selfList.stream().findFirst().get()));		
	}


	public Spliterator<T> spliterator(int start, int end){
		return new Spliterator<>() {
			private int cur = start;
			@Override
			public boolean tryAdvance(Consumer<? super T> action) {
				Objects.requireNonNull(action);
				if(cur < end) {
					action.accept(selfFunction.apply(selfList.get(cur++)));
					return true;
				}
				return false;
			}

			@Override
			public Spliterator<T> trySplit() {
				var middle = (cur+end) >>>1;
				if(middle == cur) {
					return null;
				}
				var split = spliterator(cur,middle);
				cur = middle;
				return split;
				
			}

			@Override
			public long estimateSize() {
				return end - cur;
			}

			@Override
			public int characteristics() {
				return IMMUTABLE | ORDERED;
			}
			
		};
	}
	
	@Override
	public Stream<T> stream() {
		Spliterator<T> spliterator = spliterator(0,selfList.size());
		return StreamSupport.stream(spliterator, false);
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private int index = 0;
			@Override
			public boolean hasNext() {
				return index < size();
			}

			@Override
			public T next() {
				if(!hasNext()) {
					throw new NoSuchElementException();
				}
				var indexNow = index;
				index++;
				return get(indexNow);
			}
			
		};
	}
}
