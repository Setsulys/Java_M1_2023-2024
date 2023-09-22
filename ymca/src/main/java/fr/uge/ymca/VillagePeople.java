package fr.uge.ymca;

import java.util.Objects;

public record VillagePeople (String name,Kind kind) implements People{

	public VillagePeople{
		Objects.requireNonNull(name);
		Objects.requireNonNull(kind);
	}
	
	@Override
	public String toString() {
		return name +" ("+ kind+")";
	}
}
