package fr.uge.ymca;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public final class House {
	private final ArrayList<People> house = new ArrayList<>();
	
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
		case VillagePeople k -> 100;
		case Minion m -> 1;
		default -> 0;
		}).average().orElse(Double.NaN);
	}
}
