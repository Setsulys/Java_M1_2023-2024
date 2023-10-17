package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface Slice3<E> {

	public int size();

	public E get(int index);

	public default Slice3<E> subSlice(int from, int to) {
		Objects.checkFromToIndex(from, to,Slice3.this.size());
		return new Slice3<E>() {

			@Override
			public int size() {
				return to - from;
			}

			@Override
			public E get(int index) {
				Objects.checkIndex(index, size());
				return Slice3.this.get(from +index);
			}
			@Override
			public String toString() {
				return IntStream.range(0, size()).mapToObj(e -> get(e)).map(String::valueOf).collect(Collectors.joining(", ","[","]"));
			}
		};
	}

	public static <E> Slice3<E> array(E[] array) {
		Objects.requireNonNull(array);
		return new Slice3<E>() {

			private final E[] arrayList = array;

			@Override
			public int size() {
				return arrayList.length;
			}

			@Override
			public E get(int index) {
				Objects.checkIndex(index, size());
				return arrayList[index];
			}

			@Override
			public String toString() {
				return Arrays.stream(arrayList).map(String::valueOf).collect(Collectors.joining(", ", "[", "]"));
			}
		};
	}
	
	public static <E> Slice3<E> array(E[] array, int from,int to){
		Objects.requireNonNull(array);
		Objects.checkFromToIndex(from, to, array.length);
		return array(array).subSlice(from, to);
	}
}
