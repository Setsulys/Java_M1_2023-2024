package fr.uge.graph;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;

final class MatrixGraph<T> implements Graph<T> {

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
		Objects.checkIndex(src, nodeCount);
		Objects.checkIndex(dst, nodeCount);
		this.array[src*nodeCount+dst]=weight;
	}
	
	public Optional<T> getWeight(int src, int dst){
		Objects.checkIndex(src, nodeCount);
		Objects.checkIndex(dst, nodeCount);
		return Optional.ofNullable(array[src*nodeCount+dst]);
	}
	
	public void mergeAll(Graph<? extends T> graph, BinaryOperator<T> merger) {
		Objects.requireNonNull(graph);
		Objects.requireNonNull(merger);
		if(graph.nodeCount() != nodeCount()) {
			throw new IllegalArgumentException();
		}
		for(var src=0; src < nodeCount; src++) {
			for(var dst=0; dst < nodeCount; dst++) {
				Optional<T> weight1 = getWeight(src,dst);
				//Optional<T> weight2 = graph.getWeight(src, dst);
				
			}
		}
		
	}
}
