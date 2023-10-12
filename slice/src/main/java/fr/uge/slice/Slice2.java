package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import fr.uge.slice.Slice.SubArraySlice;

public sealed interface Slice2<E>{
	
	public int size();
	public E get(int value);
	public Slice2<E> subSlice(int from,int to);
	
	public static <E> ArraySlice<E> array(E[] array) {
		Objects.requireNonNull(array);
		return new ArraySlice<E>(array);
	}
	
	public static <E>ArraySlice<E>.SubArraySlice array(E[] array, int from, int to) {
		Objects.requireNonNull(array);
		Objects.checkFromToIndex(from, to, array.length);
		return new ArraySlice<E>(array).new SubArraySlice(from,to);
	}

	public final class ArraySlice<T> implements Slice2<T>{

		private final T[] arrayList;
		
		private ArraySlice(T[] array){
			Objects.requireNonNull(array);
			arrayList = array;
		}
		
		@Override
		public int size() {
			return arrayList.length;
		}
		
		@Override
		public T get(int index) {
			Objects.checkIndex(index, size());
			return arrayList[index];
		}
		
		@Override
		public String toString() {
			return Arrays.stream(arrayList).map(String::valueOf).collect(Collectors.joining(", ","[","]"));
		}
		
		@Override
		public  ArraySlice<T>.SubArraySlice subSlice(int from,int to){
			Objects.checkFromToIndex(from, to,size());
			return new ArraySlice<T>(arrayList).new SubArraySlice(from,to);
		}
		
		public final class SubArraySlice implements Slice2<T>{
			private int fromList;
			private int toList;
		
			private SubArraySlice(int  from, int to) {
				fromList =from;
				toList = to;
			}
			
			public int size() {
				return toList - fromList;
			}
			
			public T get(int index) {
				Objects.checkIndex(index, size());
				return arrayList[fromList + index];
			}
			
			@Override
			public String toString() {
				return Arrays.stream(arrayList,fromList,toList).map(String::valueOf).collect(Collectors.joining(", ","[","]"));
			}

			@Override
			public ArraySlice<T>.SubArraySlice subSlice(int from, int to) {
				Objects.requireNonNull(arrayList);
				Objects.checkFromToIndex(from, to, size());
				return new ArraySlice<T>(arrayList).new SubArraySlice(fromList + from,fromList +to);
			}
			
		}
	}
}
