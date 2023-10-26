package fr.uge.set;

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
				for (var element = array[i];element!=null;element = element.next()) {
					array2[hackersDelight(element)] = new Entry<T>(element.value(), array2[hackersDelight(element)]);
				}
			}
			//			Consumer<Entry> cons = element -> array2[hackersDelight(element)] = new Entry(element.value(), array2[hackersDelight(element)]);
			//			forEach(cons);
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
			for(var element = array[i]; element != null; element = element.next()) {
				function.accept(element.value());
			}
		}
		//Arrays.stream(array).flatMap(element -> Stream.iterate(element,e -> e.next())).map(Entry::value).forEach(function::accept);
	}

	public boolean contains(Object value) {
		Objects.requireNonNull(value);
		var hashvalue = hackersDelight(value);
		for(Entry<T> element = array[hashvalue];element!= null;element = element.next()) {
			if(value.equals(element.value())) {
				return true;
			}
		}
		return false;
	}
	
	public void addAll(HashTableSet<? extends T> table) {
		Objects.requireNonNull(table);
		for(var i=0; i< table.length;i++) {
			for(var element = table.array[hackersDelight(i)]; element!=null;element.next()) {
				this.add(element.value());
			}
		}
	}
}
