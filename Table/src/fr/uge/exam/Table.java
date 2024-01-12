package fr.uge.exam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Table<T> {

	public class Group<K>{
		private final List<Integer> list;
		public Group(List<Integer> list) {
			Objects.requireNonNull(list);
			this.list=list;
		}
		public int keySize() {
			return list.size();
		}
	}
	private final List<T> table;
	private Table(List<T> list) {
		Objects.requireNonNull(list);
		table = list;
	}
	
	@SafeVarargs
	public static<T> Table<T> of(T ... elements){
		Arrays.stream(elements).forEach(Objects::requireNonNull);
		return new Table<T>(Arrays.stream(elements).toList());
		
	}
	
	public int size() {
		return table.size();
	}
	
	public <E>Group<T> groupBy(Function<? super T,? extends E> function,Comparator<? super E> compare) {
		Objects.requireNonNull(function);
		Objects.requireNonNull(compare);
		var l=IntStream.range(0, size()).filter(t-> compare.equals(function.apply(table.get(t)))).boxed().collect(Collectors.toList());
		return new Group<T>(l);
	}
}
