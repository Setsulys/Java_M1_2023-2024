package fr.uge.ymca;

public sealed interface People permits VillagePeople,Minion {

	public String name();
}
