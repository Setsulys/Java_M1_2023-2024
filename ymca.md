# LY STEVEN TP1 YMCA


### 1. Écrire le code de VillagePeople tel que l'on puisse créer des VillagePeople avec leur nom et leur sorte. Par exemple,

        var lee = new VillagePeople("Lee", Kind.BIKER);
        System.out.println(lee);  // Lee (BIKER)

```java
public record VillagePeople (String name,Kind kind){

	public VillagePeople{
		Objects.requireNonNull(name);
		Objects.requireNonNull(kind);
	}
	
	@Override
	public String toString() {
		return name +" ("+ kind+")";
	}
}
```

### 2. On veut maintenant introduire une maison House qui va contenir des VillagePeople. Une maison possède une méthode add qui permet d'ajouter un VillagePeople dans la maison (Il est possible d'ajouter plusieurs fois le même). L'affichage d'une maison doit renvoyer le texte "House with" suivi des noms des VillagePeople ajoutés à la maison, séparés par une virgule. Dans le cas où une maison est vide, le texte est "Empty House".

        var house = new House();
        System.out.println(house);  // Empty House
        var david = new VillagePeople("David", Kind.COWBOY);
        var victor = new VillagePeople("Victor", Kind.COP);
        house.add(david);
        house.add(victor);
        System.out.println(house);  // House with David, Victor

```java
public final class House {
	private final ArrayList<VillagePeople> house = new ArrayList<>();
	
	public void add(VillagePeople people) {
		Objects.requireNonNull(people);
		house.add(people);
	}
	
	@Override
	public String toString() {
		if(!house.isEmpty())
			return "House with " +house.stream().map(VillagePeople::name).collect(Collectors.joining(", ")).toString();
		return "Empty House";
	}
}
```
J'ai décidé de réaliser un tostring à l'aide d'un stream, car je trouve que c'est plus propre qu'un stringbuilder
J'ai juste eu du mal à me souvenir qu'il fallait que j'ajoute ``collect(Collectors.joining(","))`` ce qui m'a pris un peu de temps

### 3. En fait, on veut que l'affichage affiche les noms des VillagePeople dans l'ordre alphabétique, il va donc falloir trier les noms avant de les afficher. On pourrait créer une liste intermédiaire des noms puis les trier avec un appel à list.sort(null) mais cela commence à faire beaucoup de code pour un truc assez simple. Heureusement, il y a plus simple, on va utiliser un Stream pour faire l'affichage.

```java
public final class House {
	
    ...

	@Override
	public String toString() {
		if(!house.isEmpty())
			return "House with " +house.stream().map(VillagePeople::name).sorted().collect(Collectors.joining(", ")).toString();
		return "Empty House";
	}
}

```
En ajoutant le sorted au stream que j'ai fait précédement j'ai pu répondre à la question

### 4. En fait, avoir une maison qui ne peut accepter que des VillagePeople n'est pas une bonne décision en termes de business, ils ne sont pas assez nombreux. YMCA décide donc qu'en plus des VillagePeople ses maisons permettent maintenant d'accueillir aussi des Minions, une autre population sans logement.
```java
public record Minion(String name) implements People{
	
	public Minion{
		Objects.requireNonNull(name);
	}
	
	@Override
	public String toString() {
		return name + " (MINION)";
	}
}
```
```java
public record VillagePeople (String name,Kind kind) implements People{
    ...
}
```

```java
public interface People {

	public String name();
}

```

```java
public final class House {
	private final ArrayList<People> house = new ArrayList<>();
	
	public void add(People people) {
		...
	}

	@Override
	public String toString() {
		if(!house.isEmpty())
			return "House with " +house.stream().map(People::name).sorted().collect(Collectors.joining(", ")).toString();
		return "Empty House";
	}
}
```
Pour résoudre cet exercice, il nous faut créer une interface que j'ai appelé ``People`` que je n'ai pas sellé. En effet, ça à été dit qu'il pouvait y avoir d'autre personnes.\
Il me faut aussi changer l'arraylist de ``House`` pour remplacer ``VillagePeople`` par ``People`` et aussi faire la modification sur ``add``

### 5. On cherche à ajouter une méthode averagePrice à House qui renvoie le prix moyen pour une nuit sachant que le prix pour une nuit pour un VillagePeople est 100 et le prix pour une nuit pour un Minion est 1 (il vaut mieux être du bon côté du pistolet à prouts). Le prix moyen (renvoyé par averagePrice) est la moyenne des prix des VillagePeople et Minion présent dans la maison.

```java
public final class House {
    ...
	public double averagePrice() {
		return house.stream().mapToDouble(e -> switch(e) {
		case VillagePeople k -> 100;
		case Minion m -> 1;
		default -> 0;
		}).average().orElse(Double.NaN);
	}
}
```
On doit faire le prix moyen dans la maison, sachant qu'une maison peut contenir des ``VillagePeople`` ou des ``Minions`` on doit les differencier donc on doit faire un switch sur l'interface pour chaque élément de la liste. Comme j'avais une erreur sans j'ai du mettre le cas ``default`` on doit utiliser des doubles pour les NaN
