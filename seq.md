# LY STEVEN TP7 SEQ

### 1. Dans un premier temps, on va définir une classe SeqImpl qui est une implantation de l'interface Seq dans le même package que Seq.<br>Écrire le constructeur dans la classe SeqImpl ainsi que la méthode from(list) dans l'interface Seq sachant que, comme indiqué ci-dessus, SeqImpl contient une liste non mutable.<br>Expliquer pourquoi le constructeur ne doit pas être public ?<br>Puis déclarer les méthodes size et get() dans l'interface Seq et implanter ces méthodes dans la classe SeqImpl.
```java
public interface Seq<T> {

	public static <T>Seq<T> from(List<? extends T> list) {
		Objects.requireNonNull(list);
		return new SeqImpl<T>(List.copyOf(list));
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
public interface Seq<T> {
	@SuppressWarnings("unchecked")
	public static <T> Seq<T> from(List<? extends T> list) {
		Objects.requireNonNull(list);
		return new SeqImpl<>(List.copyOf(list), e ->(T) e);
	}
	...
	<R>Seq<R> map(Function<? super T,? extends R>function);
}
```
```java
class SeqImpl <T,R> implements Seq<T>{
	
	private final List<R> selfList;
	private final Function<? super R,? extends T> selfFunction;
	private final int size;
	
	SeqImpl(List<R> list, Function<? super R,? extends T> function) {
		Objects.requireNonNull(list);
		Objects.requireNonNull(function);
		selfList = list;
		selfFunction = function;
		size = selfList.size();
	}

	...
	
	@Override
	public T get(int index) {
		Objects.checkIndex(index, size());
		return selfFunction.apply(selfList.get(index));
	}
	
	public String toString() {
		return selfList.stream().map(e -> selfFunction.apply(e).toString()).collect(Collectors.joining(", ","<",">"));
	}

	@SuppressWarnings("hiding")
	@Override
	public <R>Seq<R> map(Function<? super T, ? extends R> function) {
		Objects.requireNonNull(function);
		return new SeqImpl<>(selfList, e -> function.apply(selfFunction.apply(e)));
	}
}
```

### 4. On souhaite avoir une méthode findFirst qui renvoie le premier élément du Seq si celui-ci existe.<br>Quel doit être le type de retour ?<br>Déclarer la méthode findFirst dans l'interface et implanter celle-ci dans la classe SeqImpl

```java
public interface Seq<T> {
	...
	Optional<R> findFirst();
}
```
```java
class SeqImpl <T,R> implements Seq<T>{
	...
	@SuppressWarnings("unchecked")
	public Optional<R> findFirst() {
		return selfList.stream().findFirst().isEmpty()?Optional.empty(): selfList.stream().findAny();
	}
}
```
Le type de retour doit etre ``Optional<T>``

### 5. On souhaite implanter la méthode stream() qui renvoie un Stream des éléments du Seq. Pour cela, on va commencer par implanter un Spliterator. Ici, on a deux façon d'implanter le Spliterator : soit on utilise le Spliterator de la liste sous-jacente, soit on utilise des indices. Expliquer dans quel cas on utilise l'un ou l'autre, sachant que nos données sont stockées dans une List.<br>Ensuite, on peut créer la classe correspondant au Spliterator à deux endroits : soit comme une classe interne de la classe SeqImpl, soit comme une classe anonyme d'une méthode spliterator(start, end), quelle est à votre avis le meilleur endroit ?
````````````````````````