package fr.uge.snapList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SnapList<T> {

	private final ArrayList<T> elements;
	private final ArrayList<T> snapShot;
	private final Function<? super List<T>,? extends T> function;
	private int finger = 0;
	private int autoSnap= -1;
	
	public SnapList(Function<? super List<T>,? extends T> function) {
		Objects.requireNonNull(function);
		this.elements = new ArrayList<>();
		this.snapShot = new ArrayList<>();
		this.function = function;
	}
	
	public void add(T value) {
		Objects.requireNonNull(value);
		elements.add(value);
		if(autoSnap==1) {
			snapshot();
		}
		else {
			autoSnap--;
		}
	}
	
	public int elementSize() {
		return elements.size();
	}

	public String toString() {
		return elements.stream().map(String::valueOf).collect(Collectors.joining(" | ","[","]"));
	}
	
	public boolean canSnapshot() {
		return finger != elements.size();
	}
	
	public void snapshot() {
		if(!canSnapshot()) {
			throw new IllegalStateException();
		}
		snapShot.add(function.apply(IntStream
				.range(finger, elementSize())
				.mapToObj(e-> elements.get(e))
				.toList()
				));
		finger = elementSize();
	}
	
	public List<T> snapshotList(){
		return List.copyOf(snapShot);
	}
	
	public void forEach(Consumer<? super T> consumer) {
		Objects.requireNonNull(consumer);
		snapShot.forEach(consumer);
		elements.stream().skip(finger).forEach(consumer);
		//IntStream.range(finger, elements.size()).mapToObj(e -> elements.get(e)).forEach(consumer);
	}
	
	public Iterator<T> iterator(){
		return new Iterator<>() {
			private final Iterator<T> itElement = elements.stream().skip(finger).iterator();
			private final Iterator<T> itSnap = snapShot.iterator();
			@Override
			public boolean hasNext() {
				return itElement.hasNext() || itSnap.hasNext();
			}

			@Override
			public T next() {
				if(!hasNext()) {
					throw new NoSuchElementException();
				}
				if(itSnap.hasNext()) {
					return itSnap.next();
				}
				return itElement.next();
			}
			
		};
	}
	
	public void autoSnapshot(int value) {
		if(value < autoSnap) {
			throw new IllegalStateException();
		}
		if(value < 0) {
			throw new IllegalArgumentException();
		}
		autoSnap = value;
	}
}
