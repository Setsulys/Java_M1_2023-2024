package fr.uge.set;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public final class HashTableSet{

	private record Entry(Object value,Entry next) {
	}

	private Entry[] array;
	private static int SIZE;
	private int length;

	public HashTableSet(){
		SIZE = 16;
		length = 0;
		array = new Entry[SIZE];
	}

	private int hackersDelight(Object value) {
		return value.hashCode() & (SIZE-1);
	}
	
//	private Entry[] updateSize(Entry[] array) {
//		if(size()>(SIZE/2)) {
//			SIZE = SIZE*2;
//			Entry[] array2 = new Entry[SIZE];
//			Arrays.asList(array).forEach(e -> array2[Arrays.asList(array).indexOf(e)] = e);
//			array = array2;
//		}
//		return array;
//	}

	public  void add(Object value) {
		Objects.requireNonNull(value);
		var hashvalue=hackersDelight(value);
		for(Entry element=array[hashvalue];element!=null;element = element.next()){
			if(element.value.equals(value)) {
				return;
			}
		}
		length++;
		//array = updateSize(array);
		array[hashvalue] = new Entry(value, array[hashvalue]);
	}


	public int size() {
		return length;
	}

	public void forEach(Consumer<Object> function) {
		Objects.requireNonNull(function);
		for(var i = 0; i < SIZE;i++) {
			for(var element = array[i]; element != null; element = element.next()) {
				function.accept( element.value());
			}
		}
		//Arrays.stream(array).flatMap(element -> Stream.iterate(element,e -> e.next())).map(Entry::value).forEach(function::accept);
	}

	public boolean contains(Object obj) {
		Objects.requireNonNull(obj);
		var hashvalue = hackersDelight(obj);
		for(Entry element = array[hashvalue];element!= null;element = element.next()) {
			if(obj.hashCode() == element.value().hashCode()) {
				return true;
			}
		}
		return false;
	}
}
