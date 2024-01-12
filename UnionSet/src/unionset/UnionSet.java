package unionset;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class UnionSet<T> implements Iterable<T>, Set<T>{

	private final Set<T> set1;
	private final Set<T> set2;
	private Optional<Integer> size = Optional.empty();
	
	private UnionSet(Set<T> firstSet,Set<T> secondSet) {
		this.set1=firstSet;
		this.set2=secondSet;
	}
	
	public static<T> UnionSet<T> of(Set<T> firstSet,Set<T> secondSet){
		Objects.requireNonNull(firstSet);
		Objects.requireNonNull(secondSet);
		firstSet.forEach(Objects::requireNonNull);
		secondSet.forEach(Objects::requireNonNull);
		return new UnionSet<>(Set.copyOf(firstSet),Set.copyOf(secondSet));
	}
	
	public boolean contains(Object value) {
		return (set1.contains(value) || set2.contains(value));
	}
	
	@Override
	public String toString() {
		return Stream.concat(set1.stream(), set2.stream())
				.distinct()
					.map(String::valueOf)
				.collect(Collectors.joining(", ","[","]"));
	}
	
	public static<T> Iterator<T> concat(Iterator<? extends T> iterator1, Iterator<? extends T> iterator2) {
		Objects.requireNonNull(iterator1);
		Objects.requireNonNull(iterator2);
		return new Iterator<>() {
			@Override
			public boolean hasNext() {
				return(iterator1.hasNext() || iterator2.hasNext());
			}

			@Override
			public T next() {
				if(!hasNext()) {
					throw new NoSuchElementException();
				}
				return iterator1.hasNext()?iterator1.next():iterator2.next();
			}
		};
	}
	
	
	public static<T> Iterator<T> filterOut(Iterator<? extends T> iterator,Predicate<? super T> predicate) {
		return new Iterator<>() {
			private T nextValid= computeNext();
			
			private T computeNext(){
				while(iterator.hasNext()) {
					var element = iterator.next();
					if(!predicate.test(element)) {
						return element;
					}
				}
				return null;
			}
			
			@Override
			public boolean hasNext() {
				return nextValid != null;
			}

			@Override
			public T next() {
				if(!hasNext()) {
					throw new NoSuchElementException();
				}
				var element = nextValid;
				nextValid= computeNext();
				return element;
			}
		};
	}

	@Override
	public Iterator<T> iterator() {
		return concat(set1.iterator(), filterOut(set2.iterator(), set1::contains));
	}
	
	public int size() {
		if(size.isEmpty()) {
			size = Optional.of(Stream.concat(set1.stream(), set2.stream()).distinct().mapToInt(v->1).sum());
		}
		return size.orElseThrow();
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof Set)) {
			return false;
		}
		Set other = (Set) obj;
		return containsAll(other);
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(T e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		Objects.requireNonNull(c);
		return c.stream().allMatch(this::contains);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	
	
	public static<T> Spliterator<T> concatFilterOutSpliterator(Spliterator<T> sp1,Spliterator<T>sp2, Predicate<T> predicate) {
		Objects.requireNonNull(sp1);
		Objects.requireNonNull(sp2);
		Objects.requireNonNull(predicate);
		return new Spliterator<T>() {

			@Override
			public boolean tryAdvance(Consumer<? super T> action) {
				var advSp1 =sp1.tryAdvance(action);
				if(advSp1) {
					return advSp1;
				}
				while(sp2.tryAdvance(e ->{
					if(!predicate.test(e)) {
						action.accept(e);
					}
				})) {
					return true;
				}
				return false;
			}

			@Override
			public Spliterator<T> trySplit() {
				return null;
			}

			@Override
			public long estimateSize() {
				return sp1.estimateSize() + sp2.estimateSize();
			}

			@Override
			public int characteristics() {
				return sp1.characteristics() & sp2.characteristics();
			}	
		};
	}
	
	public Stream<T> stream(){
		return StreamSupport.stream(concatFilterOutSpliterator(set1.spliterator(), set2.spliterator(), set1::contains), true);
	}
}
