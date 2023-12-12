package fr.uge.query;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public sealed interface Query<E>{
	public final class QueryImpl<T,E> implements Query<T>{
		
		private final List<E> selfList;
		private final Function<? super E,? extends Optional<? extends T>> function;

		
		
		QueryImpl(List<E> list,Function<? super E,? extends Optional<? extends T>> function) {
			Objects.requireNonNull(list);
			Objects.requireNonNull(function);
			this.selfList = list;
			this.function = function;
			
		}
		
		@Override
		public String toString() {
			return selfList.stream()
					.map(e -> function.apply(e))
					.filter(f -> f.isPresent())
					.map(s -> s.orElseThrow().toString())
					.collect(Collectors.joining(" |> "));
		}
		
		public List<T> toList(){
			return selfList.stream()
					.map(e -> function.apply(e))
					.filter(f -> f.isPresent())
					.map(l -> l.orElseThrow())
					.collect(Collectors.toUnmodifiableList());
		}
		
		public Stream<T> toStream(){
			return selfList.stream()
					.map(e -> function.apply(e))
					.filter(f -> f.isPresent())
					.map(l -> l.orElseThrow());
		}
		
		public List<T> toLazyList(){
			return new AbstractList<T>() {
				private Iterator<E> iterator = selfList.iterator();
				private List<T> cache = new ArrayList<T>();;
				@Override
				public int size() {
					while(iterator.hasNext()) {
						function.apply(iterator.next()).ifPresent(cache::add);
					}
					return cache.size();
				}

				@Override
				public T get(int index) {
					while(index >= cache.size() && iterator.hasNext()) {
						function.apply(iterator.next()).ifPresent(cache::add);
					}
					return cache.get(index);
				}
			};
		}
	}
	
	
	
	
	
	
	public static <T,E>Query<T> fromList(List<E> recordsList, Function<? super E,? extends Optional<? extends T>> function) {
		Objects.requireNonNull(recordsList);
		Objects.requireNonNull(function);
		return new QueryImpl<>(recordsList, function);
	}
	
	List<E> toList();
	Stream<E> toStream();
	List<E> toLazyList();
}
