package fr.uge.seq;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

class SeqImpl <T> implements Seq<T>{
	
	private final List<T> selfList;
	private final Function<? super Object,? extends T> selfFunction;
	private final int size;
	
	SeqImpl(List<? extends T> list, Function<? super Object,? extends T> function) {
		Objects.requireNonNull(list);
		Objects.requireNonNull(function);
		selfList = List.copyOf(list);
		selfFunction =function;
		size = selfList.size();
	}

	@Override
	public int size() {
		return this.size;
	}
	
	@Override
	public T get(int index) {
		Objects.checkIndex(index, size());
		return selfList.get(index);
	}
	
	public String toString() {
		return selfList.stream().map(String::valueOf).collect(Collectors.joining(", ","<",">"));
	}
	
	public <E>Seq<T> map(Function<? super Object,? extends T> function){
		Objects.requireNonNull(function);
		return new SeqImpl<T>(selfList, e-> function.apply(function.apply(e)));
	}
}
