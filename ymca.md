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

### 6. En fait, cette implantation n'est pas satisfaisante car elle ajoute une méthode publique dans VillagePeople et Minion alors que c'est un détail d'implantation. Au lieu d'utiliser la POO (programmation orienté objet), on va utiliser la POD (programmation orienté data) qui consiste à utiliser le pattern matching pour connaître le prix par nuit d'un VillagePeople ou un Minion.

```java
public final class House {
    ...
	public double averagePrice() {
		return house.stream().mapToDouble(e -> switch(e) {
		case VillagePeople __ -> 100;
		case Minion __ -> 1;
		default -> 0;
		}).average().orElse(Double.NaN);
	}
}
```
Je remplace les variables ``VillagePeople k`` et ``Minion m`` par des wildcards.

### 7. L'implantation précédente pose problème : il est possible d'ajouter une autre personne qu'un VillagePeople ou un Minion, mais celle-ci ne sera pas prise en compte par le pattern matching. Pour cela, on va interdire qu'une personne soit autre chose qu'un VillagePeople ou un Minion en scellant le super type commun.

```java
public sealed interface People permits VillagePeople,Minion {
	...
}
```
On doit donc ajouter le ``sealed`` et permetre les ``VillagePeoples`` et les ``Minions`` ce qui nous permet d'enlever le ``case _ -> ...`` du pattern matching

### 8. On veut périodiquement faire un geste commercial pour une maison envers une catégorie/sorte de VillagePeople en appliquant une réduction de 80% pour tous les VillagePeople ayant la même sorte (par exemple, pour tous les BIKERs). Pour cela, on se propose d'ajouter une méthode addDiscount qui prend une sorte en paramètre et offre un discount pour tous les VillagePeople de cette sorte. Si l'on appelle deux fois addDiscount avec la même sorte, le discount n'est appliqué qu'une fois.

```java
public final class House {
	...
	private final HashSet<Kind> discount = new HashSet<>();
	...
	public double averagePrice() {
		return house.stream().mapToDouble(e -> switch(e) {
		case VillagePeople (var __,var kind) -> discount.contains(kind)?20:100;
		case Minion __ -> 1;
		}).average().orElse(Double.NaN);
	}
	
	public void addDiscount(Kind kind) {
		Objects.requireNonNull(kind);
		discount.add(kind);
	}
}
```
On ajoute la methode ``addDiscount`` qui ajoute les kinds dans un``HashSet`` et on doit modifier le cas ``VillagePeople`` où on fait un record Pattern pour travailler sur le ``kind`` de ``VillagePeople``

### 9. Enfin, on souhaite pouvoir supprimer l'offre commerciale (discount) en ajoutant la méthode removeDiscount qui supprime le discount si celui-ci a été ajouté précédemment ou plante s'il n'y a pas de discount pour la sorte prise en paramètre.
```java
public final class House {
	...
	public void removeDiscount(Kind kind) {
		Objects.requireNonNull(kind);
		if(!discount.contains(kind)) {
			throw new IllegalStateException();
		}
		discount.removeIf(e -> e.equals(kind));
	}
}
```
On ajoute la méthode ``removeDiscount`` et j'utilise un ``removeIf`` pour ne retirer que ``Kind`` qu'on a besoin


### 10. **Optionnellement**, faire en sorte que l'on puisse ajouter un discount suivi d'un pourcentage de réduction, c'est à dire un entier entre 0 et 100, en implantant une méthode addDiscount(kind, percent). Ajouter également une méthode priceByDiscount qui renvoie une table associative qui a un pourcentage renvoie la somme des prix par nuit auxquels on a appliqué ce pourcentage (la somme est aussi un entier). La somme totale doit être la même que la somme de tous les prix par nuit (donc ne m'oubliez pas les minions). Comme précédemment, les pourcentages ne se cumulent pas si on appelle addDiscount plusieurs fois.