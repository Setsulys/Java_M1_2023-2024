package fr.uge.graph;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

final class MatrixGraph<T> implements Graph<T> {
	
	public record Edge<T>(int src,int dst,T weight){
		
	}

	private final T[] array;
	private int nodeCount;
	@SuppressWarnings("unchecked")
	public MatrixGraph(int nodeNb) {
		if(nodeNb < 0) {
			throw new IllegalArgumentException();
		}
		this.nodeCount = nodeNb;
		this.array = (T[]) new Object[nodeCount * nodeCount];

	}

	public int nodeCount() {
		return nodeCount;
	}

	public void addEdge(int src, int dst, T weight) {
		Objects.requireNonNull(weight);
		Objects.checkIndex(src, nodeCount());
		Objects.checkIndex(dst, nodeCount());
		this.array[src*nodeCount+dst]=weight;
	}

	public Optional<T> getWeight(int src, int dst){
		Objects.checkIndex(src, nodeCount());
		Objects.checkIndex(dst, nodeCount());
		return Optional.ofNullable(array[src*nodeCount+dst]);
	}

	public Iterator<Integer> neighborIterator(int src){
		Objects.checkIndex(src, nodeCount());
		return new Iterator<>(){

			private Optional<Integer> it=isValid(0);
			private Optional<Integer> previous= Optional.empty();

			private Optional<Integer> isValid(int value) {
				for(var dst=value; dst < nodeCount();dst++) {
					if(array[src*nodeCount+dst]!=null) {
						return Optional.of(dst);
					}
				}
				return Optional.empty();
			}

			@Override
			public boolean hasNext() {
				return it.isPresent();
			}

			@Override
			public Integer next() {
				if(!hasNext()) {
					throw new NoSuchElementException();
				}
				previous = it;
				it = isValid(it.orElseThrow()+1);
				return previous.orElseThrow();
			}

			public void remove() {
				if(previous.isEmpty()) {
					throw new IllegalStateException();
				}
				array[src*nodeCount+previous.orElseThrow()]=null;
				previous=it;
			}

		};
	}
}
