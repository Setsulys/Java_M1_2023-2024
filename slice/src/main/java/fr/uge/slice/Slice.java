package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public sealed interface Slice<E>{

	public int size();
	public E get(int index);
	public Slice<E> subSlice(int from, int to);

	public static <E> ArraySlice<E> array(E[] array) {
		Objects.requireNonNull(array);
		return new ArraySlice<E>(array);
	}

	public static<E> SubArraySlice<E> array(E[]array,int from,int to){
		Objects.requireNonNull(array);
		Objects.checkFromToIndex(from, to, array.length);
		return new SubArraySlice<E>(array,from,to);
	}

	public final class ArraySlice<T> implements Slice<T>{

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
		public Slice<T> subSlice(int from,int to) {
			Objects.checkFromToIndex(from, to, size());
			return new SubArraySlice<T>(arrayList,from,to);
		}

	}

	public final class SubArraySlice<T> implements Slice<T>{

		private final T[] arrayList;
		private int fromList;
		private int toList;

		private SubArraySlice(T[]array,int from, int to) {
			Objects.requireNonNull(array);
			Objects.checkFromToIndex(from, to, array.length);
			arrayList=array;
			fromList = from;
			toList = to;
		}

		@Override
		public int size() {
			return toList-fromList;
		}

		@Override
		public T get(int index) {
			Objects.checkIndex(index, size());
			return arrayList[fromList +index];
		}

		@Override
		public String toString() {
			return Arrays.stream(arrayList,fromList,toList).map(String::valueOf).collect(Collectors.joining(", ","[","]"));
		}

		@Override
		public Slice<T> subSlice(int from,int to) {
			Objects.checkFromToIndex(from, to, size());
			return new SubArraySlice<T>(arrayList,fromList +from,fromList +to);
		}
	}
}
