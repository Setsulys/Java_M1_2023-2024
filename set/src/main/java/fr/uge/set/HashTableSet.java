package fr.uge.set;

import java.util.Objects;

public final class HashTableSet<T>{

	private record Entry<T>(T value,Entry next) {
	}
	
	private final Entry[] array;
	private static int SIZE;
	private int length;
	
	public HashTableSet(){
		SIZE = 16;
		length =0;
		array = new Entry[SIZE];
	}
	
	private int position(T value) {
		return value.hashCode() & (SIZE-1);
	}
	
	public void add(T value) {
		Objects.requireNonNull(value);
		var hashvalue=position(value);
		Entry element;
		for(element=array[hashvalue];element!=null;element.next()){
			if(element.value.equals(value)) {
				return;
			}
		}
		length++;
		array[hashvalue] = new Entry<T>(value, array[hashvalue]);
	}

	public int size() {
		return length;
	}
	
	public static void main(String[] args) {
		System.out.println(Integer.hashCode(-777)%4);
	}
}
