package fr.uge.embed;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class EmbedList<T> extends AbstractList<T> implements Iterable<T>{
	

	private final UnaryOperator<T> getNext;
	private final BiConsumer<T,T> setNext;
	private T head;
	private T tail;
	private int size;
	private boolean isUnmodifiable;
	
	public EmbedList(UnaryOperator<T> getNext,BiConsumer<T,T> setNext) {
		Objects.requireNonNull(getNext);
		Objects.requireNonNull(setNext);
		this.getNext = getNext;
		this.setNext = setNext;
	}
	
	public int size() {
		return size;
	}
	
	public void addFirst(T node) {
		Objects.requireNonNull(node);
		if(isUnmodifiable) {
			throw new UnsupportedOperationException();
		}
		if(size ==0) {
			tail = node;
		}
		size++;
		setNext.accept(node, head);
		head = node;
	}
	
	public void forEach(Consumer<? super T> consumer) {
		Objects.requireNonNull(consumer);
//		if(isUnmodifiable) {
//			throw new UnsupportedOperationException();
//		}
//		var pointer = head;
//		while(pointer!=null) {
//			consumer.accept(pointer);
//			pointer = getNext.apply(pointer);
//		}
		Stream.iterate(head, getNext).limit(size).forEach(consumer);
	}
	
	@Override
	public Iterator<T> iterator(){
		return new Iterator<T>(){
			private T it = head;
			@Override
			public boolean hasNext() {
				return it!=null;
			}

			@Override
			public T next() {
				if(!hasNext()) {
					throw new NoSuchElementException();
				}
				var value= it;
				it = getNext.apply(it);
				return value;
			}	
		};
	}
	
	public T get(int index) {
		Objects.checkIndex(index, size);
		var pointer = head;
		for(;index!=0; index--) {
			pointer = getNext.apply(pointer);
		}
		return pointer;
	}
	
	@Override
	public int indexOf(Object o) {
		Objects.requireNonNull(o);
		var pointer = head;
		for(var i = 0; i < size; i++) {
			if(o.equals(pointer)) {
				return i;
			}
			pointer = getNext.apply(pointer);
		}
		return -1;
	}
	
	@Override
	public int lastIndexOf(Object o) {
		Objects.requireNonNull(o);
		var pointer = head;
		var lastIndexOf = -1;
		for(var i =0; i < size;i++) {
			if(o.equals(pointer)) {
				lastIndexOf = i;
			}
			pointer = getNext.apply(pointer);
		}
		return lastIndexOf;
	}
	
	public EmbedList<T> unmodifiable(){
		if(isUnmodifiable) {
			return this;
		}
		var list = new EmbedList<>(getNext, setNext);
		list.isUnmodifiable=true;
		list.size=size();
		list.head = head;
		list.tail = tail;
		return list;
	}
	
	@Override
	public boolean add(T node) {
		Objects.requireNonNull(node);
		if(isUnmodifiable) {
			throw new UnsupportedOperationException();
		}
		if(size==0) {
			head = node;
			tail = node;
		}
		else {
			setNext.accept(tail, node);
			tail=node;
		}
		size++;
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c) {
		Objects.requireNonNull(c);
		if(isUnmodifiable) {
			throw new UnsupportedOperationException();
		}
		if(c.isEmpty()) {
			return false;
		}
		c.stream().forEach(this::add);
		return true;
	}
	
	public<E> Stream<E> valueStream(Function<? super T,? extends E> function){
		Objects.requireNonNull(function);
		return StreamSupport.stream(fromIterator(iterator(),function), false);
	}
	
	public <E>Spliterator<E> fromIterator(Iterator<? extends T> it, Function<? super T,? extends E> function){
		return new Spliterator<>() {
			private int i;
			@Override
			public boolean tryAdvance(Consumer<? super E> action) {
				if(it.hasNext()) {
					action.accept(function.apply(it.next()));
					i++;
					return true;
				}
				return false;
			}

			@Override
			public Spliterator<E> trySplit() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public long estimateSize() {
				// TODO Auto-generated method stub
				return size()-i;
			}

			@Override
			public int characteristics() {
				if(isUnmodifiable) {
					return IMMUTABLE | ORDERED | NONNULL | SIZED;
				}
				return ORDERED | NONNULL | SIZED;
			}
			
		};
	}
}
