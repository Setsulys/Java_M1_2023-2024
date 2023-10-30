package fr.uge.fifo;

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

public class Fifo<E> extends AbstractQueue<E> implements Iterable<E>{

	private static final int SIZEMAX =16;
	private int head;
	private int tail;
	private E[] array;
	private int size;

	@SuppressWarnings("unchecked")
	public Fifo(int maxElement) {
		if(maxElement <= 0) {
			throw new IllegalArgumentException();
		}
		head=0;
		tail=0;
		size=0;
		array = (E[]) new Object[SIZEMAX];
	}

	public Fifo() {
		this(16);
	}

	public boolean offer(E value) {
		Objects.requireNonNull(value);
		if(size()==array.length) {
			resize();
		}
		array[tail]=value;
		size++;
		tail = (tail+1)%array.length;
		return true;
	}

	public int size() {
		return size;
	}

	public E poll(){
		if(head==array.length) {
			throw new IllegalStateException();
		}
		E element = array[head];
		array[head]=null;
		head = (head+1)%array.length;
		size--;
		return element;
	}

	public E peek() {
		if (size()<=0) {
			return null;
		}
		return array[head];
	}

	@SuppressWarnings("unchecked")
	public void resize() {
		E[] array2 = array;

		array = (E[]) new Object[array.length <<1];

		if(head ==0) {
			array = Arrays.copyOf(array2, array.length <<1);
			tail = size();
			return;
		}
		System.arraycopy(array2, head, array, 0, array2.length-head);
		System.arraycopy(array2, 0, array, array2.length-head, tail);
		tail =size();
		head=0;
	}

	@Override
	public String toString() {
		String s;
		if(size()==0) {
			return s = "[]";
		}
		else if(head < tail) { 
			s = Arrays.stream(array).filter(e -> e != null).map(String::valueOf).collect(Collectors.joining(", ","[","]"));
		}
		else{
			s = Arrays.stream(array,head,array.length).filter(e -> e != null).map(String::valueOf).collect(Collectors.joining(", ","[",""));
			s+= Arrays.stream(array,0,tail).filter(e -> e != null).map(String::valueOf).collect(Collectors.joining(", ",", ","]"));
		}
		return s;
	}

	public Iterator<E> iterator() {
		return new Iterator<E>(){
			private int cursor = head;
			private int selfsize;

			@Override
			public boolean hasNext() {
				return selfsize < size();
			}

			@Override
			public E next() {
				if(!hasNext()) {
					throw new NoSuchElementException();
				}
				var element = array[cursor];
				cursor = (cursor+1)%array.length;
				selfsize++;
				return element;
			}

		};
	}

	public void clear() {
		var s = size();
		for(var i =0; i < s;i++) {
			array[i]=null;
			size--;
		}
		head = 0;
		tail = 0;
	}
}
