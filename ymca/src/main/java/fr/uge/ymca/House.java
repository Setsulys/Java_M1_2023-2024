package fr.uge.ymca;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public final class House {
	private final ArrayList<People> house = new ArrayList<>();
	private final HashMap<Kind, Integer> discount = new HashMap<>();
	
	public void add(People people) {
		Objects.requireNonNull(people);
		house.add(people);
	}

	/*
	@Override
	public String toString() {
		if(!house.isEmpty())
			return "House with " +house.stream().map(VillagePeople::name).collect(Collectors.joining(", ")).toString();
		return "Empty House";
	}
	*/
	@Override
	public String toString() {
		if(!house.isEmpty())
			return "House with " +house.stream().map(People::name).sorted().collect(Collectors.joining(", ")).toString();
		return "Empty House";
	}
	
	public double averagePrice() {
		return house.stream().mapToDouble(e -> switch(e) {
		case VillagePeople (var __,var kind) -> discount.containsKey(kind)?100- discount.get(kind)/**((1-discount.get(kind)/100))*/:100;
		case Minion __ -> 1;
		}).average().orElse(Double.NaN);
	}
	
	public void addDiscount(Kind kind) {
		Objects.requireNonNull(kind);
		addDiscount(kind,20);
	}
	
	public void removeDiscount(Kind kind) {
		Objects.requireNonNull(kind);
		if(!discount.containsKey(kind)) {
			throw new IllegalStateException();
		}
		discount.entrySet().removeIf(e -> e.getKey().equals(kind));
	}
	
	public void addDiscount(Kind kind, int percent) {
		Objects.requireNonNull(kind);
		if(0 > percent || percent > 100) {
			throw new IllegalArgumentException();
		}
		discount.put(kind,percent);
	}
	public  HashMap<Integer,Integer>priceByDiscount() {
		var map = new HashMap<Integer,Integer>();
		for(var d : discount.entrySet()) {
			map.putIfAbsent(d.getValue(), 0);
		}

		return null;
	}
}
