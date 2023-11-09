package fr.uge.table;

import java.lang.reflect.RecordComponent;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class IntTable {
	
	sealed interface Impl permits MapImpl,RecordImpl{
		int size();
		
		void set(String str,int value);
		
		int get(String str, int value);
	}

	static final class MapImpl implements Impl{
		private final LinkedHashMap<String, Integer> map;
		
		public MapImpl(LinkedHashMap<String,Integer> newMap) {
			Objects.requireNonNull(newMap);
			this.map = newMap;
		}
		
		public MapImpl() {
			this.map = new LinkedHashMap<>();
		}
		
		public int size() {
			return map.size();
		}
		
		public void set(String str,int value) {
			Objects.requireNonNull(str);
			map.put(str, value);
		}
		
		public int get(String str,int value) {
			Objects.requireNonNull(str);
			return map.getOrDefault(str, value);
		}
	}
	
	static final class RecordImpl implements Impl{
		
		private final MapImpl mapImpl;
		private final int[] enumateArray;
		
		RecordImpl(LinkedHashMap<String,Integer> map){
			Objects.requireNonNull(map);
			this.mapImpl = new MapImpl(map);
			this.enumateArray = new int[this.mapImpl.size()];
		}
		
		public int size() {
			return mapImpl.size();
		}

		@Override
		public void set(String str, int value) {
			Objects.requireNonNull(str);
			var index = mapImpl.get(str, -1);
			
			if(index==-1) {
				throw new IllegalStateException();
			}
			enumateArray[index] = value;
		}

		@Override
		public int get(String str, int value) {
			Objects.requireNonNull(str);
			var result = mapImpl.get(str, -1);
			return result==-1? value:enumateArray[result];
		}
	}
	
	private final Impl storage;
	
	public IntTable(){
		this.storage = new MapImpl();
	}
	
	private IntTable(Impl impl) {
		Objects.requireNonNull(impl);
		this.storage = impl;
	}
	
	public void set(String str, int value) {
		Objects.requireNonNull(str);
		storage.set(str, value);
	}
	
	public int size() {
		return storage.size();
	}
	
	public int get(String str,int value) {
		Objects.requireNonNull(str);
		return storage.get(str, value);
	}
	
	public IntTable apply(IntUnaryOperator function) {
		Objects.requireNonNull(function);
		switch(storage) {
		case RecordImpl ri -> {
			var newMap = new LinkedHashMap<String,Integer>(ri.mapImpl.map);
			var newRec = new RecordImpl(newMap);
			var newTable = new IntTable(newRec);
			newRec.mapImpl.map.entrySet().stream().forEach(e -> newTable.set(e.getKey(), function.applyAsInt(ri.enumateArray[e.getValue()])));
			return newTable;
			
		}
		case MapImpl mi ->{
			IntTable table2 = new IntTable();
			mi.map.entrySet().stream().forEach(e -> table2.set(e.getKey(), function.applyAsInt(e.getValue())));
			return table2;
		}
		}
	}

	static LinkedHashMap<String,Integer> recordComponentIndexes(RecordComponent[] components) {
		Objects.requireNonNull(components);
		var map = new LinkedHashMap<String,Integer>();
		IntStream.range(0, components.length).forEach(i -> map.put(components[i].getName(), i));
		return map;
	}

	public static IntTable from(Class<?> otherClass) {
		Objects.requireNonNull(otherClass);
		if(!otherClass.isRecord()) {
			throw new IllegalArgumentException();
		}
		var impl = new RecordImpl(recordComponentIndexes(otherClass.getRecordComponents()));
		return new IntTable(impl);
	}
	
	@Override
	public String toString() {
		return switch(storage) {
		case RecordImpl ri ->ri.mapImpl.map.entrySet().stream().map(e -> e.getKey()+"="+ri.enumateArray[e.getValue()]).collect(Collectors.joining(", ","{","}"));
		case MapImpl mi -> mi.map.entrySet().stream().map(e -> e.getKey()+"="+e.getValue()).collect(Collectors.joining(", ","{","}"));
		};
	}
}
