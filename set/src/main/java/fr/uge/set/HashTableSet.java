package fr.uge.set;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public final class HashTableSet<T>{

	private record Entry<T>(T value,Entry<T> next) {
	}

	private Entry<T>[] array;
	private static int SIZE=16;
	private int length;

	@SuppressWarnings("unchecked")
	public HashTableSet(){
		length = 0;
		array = new Entry[SIZE];
	}

	private int hackersDelight(Object value) {
		return value.hashCode() & (SIZE-1);
	}

	@SuppressWarnings("unchecked")
	private Entry<T>[] updateSize(Entry<T>[] array) {
		if(size()>(SIZE/2)) {
			SIZE = SIZE*2;
			Entry<T>[] array2 = new Entry[SIZE];
			for(var i=0; i < SIZE/2	;i++) {
				for (var entry = array[i];entry!=null;entry = entry.next()) {
					array2[hackersDelight(entry.value())] = new Entry<T>(entry.value(), array2[hackersDelight(entry.value())]);
				}
			}
			array = array2;
		}
		return array;
	}

	public void add(T value) {
		Objects.requireNonNull(value);
		if(contains(value)) {
			return;
		}
		array = updateSize(array);
		length++;
		array[hackersDelight(value)] = new Entry<T>(value, array[hackersDelight(value)]);
	}


	public int size() {
		return length;
	}

	public void forEach(Consumer<? super T> function) {
		Objects.requireNonNull(function);
		for(var i = 0; i < SIZE;i++) {
			for(var entry = array[i]; entry != null; entry = entry.next()) {
				function.accept(entry.value());
			}
		}
		//Arrays.stream(array).flatMap(element -> Stream.iterate(element,e -> e.next())).map(Entry::value).forEach(function::accept);
	}

	public boolean contains(T value) {
		Objects.requireNonNull(value);
		var hashvalue = hackersDelight(value);
		for(Entry<T> entry = array[hashvalue];entry!= null;entry = entry.next()) {
			if(value.equals(entry.value())) {
				return true;
			}
		}
		return false;
	}
	
	public void addAll(HashTableSet<? extends T> table) {
		Objects.requireNonNull(table);
		table.forEach(t -> add(t));
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof HashTableSet<?> hashTableSet 
				&& this==hashTableSet
				&& this.array ==hashTableSet.array;
	}
}
