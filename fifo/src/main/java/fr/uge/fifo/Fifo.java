package fr.uge.fifo;

import java.util.Objects;

public class Fifo<E> {

	private static int SIZEMAX;
	private int head;
	private int tail;
	private E[] array;
	private int size;
	
	@SuppressWarnings("unchecked")
	public Fifo(int maxElement) {
		if(maxElement <= 0) {
			throw new IllegalArgumentException();
		}
		SIZEMAX = maxElement;
		head=0;
		tail=0;
		size=0;
		array = (E[]) new Object[SIZEMAX];
	}

	
	@SuppressWarnings("unchecked")
	public Fifo() {
		SIZEMAX=2;
		head=0;
		tail=0;
		size=0;
		array = (E[]) new Object[SIZEMAX];
	}
	
	public void offer(E value) {
		Objects.requireNonNull(value);
		array[tail]=value;
		size++;
		tail = (tail+1)%SIZEMAX;
		
	}
	
	public int size() {
		return size;
	}
	
	public E poll(){
		if(size()<=0) {
			return null;
		}
		E element = array[head];
		head = (head+1)%SIZEMAX;
		size--;
		return element;
	}
	
	public E peek() {
		if (size()<=0) {
			return null;
		}
		return array[head];
	}
}
