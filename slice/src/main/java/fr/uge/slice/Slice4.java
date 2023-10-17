package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public sealed interface Slice4<E>{

	abstract int size();
	abstract E get(int index);
	abstract Slice4<E> subSlice(int from, int to);

	static <E> Slice4<E> array(E[] array) {
		Objects.requireNonNull(array);
		return array(array, 0, array.length);
	}

	static<E> Slice4<E> array(E[]array,int from,int to){
		Objects.requireNonNull(array);
		Objects.checkFromToIndex(from, to, array.length);
		return new SliceImpl<E>(array,from, to);
	}

}

 final class SliceImpl<V> implements Slice4<V>{
	 
	 private final V[] innerArray;
	 private final int fromArray;
	 private final int toArray;
	 
	 public SliceImpl(V[] array,int from, int to){
		 Objects.requireNonNull(array);
		 Objects.checkFromToIndex(from, to, array.length);
		 innerArray = array;
		 fromArray = from;
		 toArray = to;
		 
	 }

	@Override
	public int size() {
		return toArray - fromArray;
	}

	@Override
	public V get(int index) {
		Objects.checkIndex(index,size());
		return innerArray[fromArray + index];
	}
	
	@Override
	public String toString() {
		return Arrays.stream(innerArray,fromArray,toArray).map(String::valueOf).collect(Collectors.joining(", ","[","]"));
	}

	@Override
	public Slice4<V> subSlice(int from, int to) {
		Objects.checkFromToIndex(from, to,size());
		return new SliceImpl<>(innerArray,fromArray+from,fromArray+to);
	}
}