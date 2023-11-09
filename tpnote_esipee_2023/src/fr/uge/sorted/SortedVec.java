package fr.uge.sorted;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class SortedVec<T>{

	private final T[] selfArray;
	private final Comparator<? super T> comparator;
	
	private SortedVec(T[] newArray,Comparator<? super T> newComparator){
		Objects.requireNonNull(newArray);
		this.selfArray = newArray;
		this.comparator = newComparator;
	}
	
	static void checkSortedStrings(String[] array) {
		if(!IntStream.range(1, array.length).allMatch(i-> array[i].compareTo(array[i-1])>=0)) {
			throw new IllegalArgumentException();
		}
	}

	public static SortedVec<String> ofSortedStrings(List<String> list) {
		list.stream().forEach(e -> Objects.requireNonNull(e));
		var listToArray = list.toArray(new String[list.size()]);
		checkSortedStrings(listToArray);
		return new SortedVec<>(listToArray,String::compareTo);
	}
	
	public int size() {
		return selfArray.length;
	}
	
	public T get(int value) {
		Objects.checkIndex(value, selfArray.length);
		return selfArray[value];
	}

	static <E> void checkSorted(E[] newArray, Comparator<? super E> comparator) {
		Objects.requireNonNull(newArray);
		Objects.requireNonNull(comparator);
		if(!IntStream.range(1, newArray.length).allMatch(i -> comparator.compare(newArray[i], newArray[i-1])>=0)) {
			throw new IllegalArgumentException();
		}
	}

	public static <E> SortedVec<E> ofSorted(List<? extends E> list, Comparator<? super E>comparator) {
		Objects.requireNonNull(list);
		Objects.requireNonNull(comparator);
		list.stream().forEach(e -> Objects.requireNonNull(e));
		@SuppressWarnings("unchecked")
		var listToArray = (E[]) list.toArray(new Object[list.size()]);
		checkSorted(listToArray, comparator);
		return new SortedVec<>(listToArray,comparator);
	}

	public Boolean isIn(T checker) {
		Objects.requireNonNull(checker);
		return Arrays.binarySearch(selfArray, checker, comparator)>=0;
	}
	
	@Override
	public String toString() {
		return Arrays.stream(selfArray).map(e-> e.toString()).collect(Collectors.joining(" <= "));
	}

	public SortedVec<? extends T> append(SortedVec<? extends T> vec) {
		Objects.requireNonNull(vec);
		if(!this.comparator.equals(vec.comparator)) {
			throw new IllegalArgumentException();
		}
		@SuppressWarnings("unchecked")
		var newArray =Stream.concat(Arrays.stream(selfArray), Arrays.stream(vec.selfArray)).sorted(comparator).toList();
		return ofSorted(newArray, this.comparator);
	}

	public static <E> SortedVec<E> ofSorted(List<? extends E> list) {
		Objects.requireNonNull(list);
		list.stream().forEach(Objects::requireNonNull);
		return ofSorted(list, (a,b) -> ((Comparable<E>) a).compareTo(b) );
	}
	
}
