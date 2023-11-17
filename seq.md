# LY STEVEN TP7 SEQ

### 1. Dans un premier temps, on va définir une classe SeqImpl qui est une implantation de l'interface Seq dans le même package que Seq.<br>Écrire le constructeur dans la classe SeqImpl ainsi que la méthode from(list) dans l'interface Seq sachant que, comme indiqué ci-dessus, SeqImpl contient une liste non mutable.<br>Expliquer pourquoi le constructeur ne doit pas être public ?<br>Puis déclarer les méthodes size et get() dans l'interface Seq et implanter ces méthodes dans la classe SeqImpl.
```java
public interface Seq<T> {

	public static <T>Seq<T> from(List<? extends T> list) {
		Objects.requireNonNull(list);
		list.stream().forEach(e -> Objects.requireNonNull(e));
		return new SeqImpl<T>(list);
	}
	int size();
	T get(int index);
}
```

```java
class SeqImpl <T> implements Seq<T>{
	
	private final List<T> selfList;
	private final int size;
	
	SeqImpl(List<? extends T> list) {
		Objects.requireNonNull(list);
		selfList = List.copyOf(list);
		size = selfList.size();
	}

	public int size() {
		return this.size;
	}
	
	public T get(int index) {
		Objects.checkIndex(index, size());
		return selfList.get(index);
	}
}
```
Le constructeur ne doit pas etre publique, car on y accède via l'interface ``Seq`` mais il ne doit pas etre non plus private, car l'interface ne le verait pas


### 2. On souhaite écrire une méthode d'affichage permettant d'afficher les valeurs d'un Seq séparées par des virgules (suivies d'un espace), l'ensemble des valeurs étant encadré par des chevrons ('<' et '>')
```java
class SeqImpl <T> implements Seq<T>{
	...
	public String toString() {
		return selfList.stream().map(String::valueOf).collect(Collectors.joining(", ","<",">"));
	}
}
```

### 3. On souhaite écrire une méthode map qui prend en paramètre une fonction à appliquer à chaque élément d'un Seq pour créer un nouveau Seq. On souhaite avoir une implantation paresseuse, c'est-à-dire une implantation qui ne fait pas de calcul si ce n'est pas nécessaire. Par exemple, tant que personne n'accède à un élément du nouveau Seq il n'est pas nécessaire d'appliquer la fonction. L'idée est de stoker les anciens éléments ainsi que la fonction et de l'appliquer seulement si c'est nécessaire.<br>Bien sûr, cela va nous obliger à changer l'implantation déjà existante de SeqImpl car maintenant tous les Seq vont stocker une liste d'éléments ainsi qu'une fonction de transformation (de mapping).
#### Avant de se lancer dans l'implantation de map, quelle doit être sa signature ? Quel doit être le type des éléments de la liste ? Et le type de la fonction stockée ? Dans SeqImpl, ajouter un champ correspondant à la fonction prise en paramètre par map, sans pour l'instant écrire la méthode map. On appelle cela faire un refactoring, c'est-à-dire préparer la classe pour une fonctionnalité future.
```java

``````````````````````````````````````````