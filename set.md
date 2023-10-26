# LY STEVEN TP4 STEVEN

## Exercice 2 - HashTableSet

### 1. Quels doivent être les champs de la classe Entry correspondant à une case d'une des listes chaînées utilisées par table de hachage<br>Rappeler quelle est l'intérêt de déclarer Entry comme membre de la classe HashTableSet plutôt que comme une classe à coté dans le même package que HashTableSet ?<br>Ne pourrait-on pas utiliser un record plutôt qu'une classe, ici ? Si oui, pourquoi ? Si non, pourquoi ?<br>Écrire la classe HashTableSet dans le package fr.uge.set et ajouter Entry en tant que classe interne.
```java
public class HashTableSet{
	private record Entry(Object value,Entry next) {
	}
}
```
Les champs de la classe Entry doivent etre, le type voulus ``<T>`` et ``Entry`` pour pouvoir chainer 
<br> L'interet de déclarer ``Entry`` comme un membre de la classe ``HashTableSet`` est qu'on peut directement l'utiliser
<br> ``Entry`` doit etre un record car on y stocke des données.

### 2. On souhaite maintenant ajouter un constructeur sans paramètre, une méthode add qui permet d'ajouter un élément non null et une méthode size qui renvoie le nombre d'éléments insérés (avec une complexité en O(1)).<br>Pour l'instant, on va dire que la taille du tableau est toujours 16, on fera en sorte que la table de hachage s'agrandisse toute seule plus tard.<br>Dans la classe HashTableSet, implanter le constructeur et les méthodes add et size.

```java
public class HashTableSet{
	private final Entry[] array;
	private static int SIZE=16;
	private int length;
	
	public HashTableSet(){
		length = 0;
		array = new Entry[SIZE];
	}
	
	private int hackersDelight(Object value) {
		return value.hashCode() & (SIZE-1);
	}
	
	public  void add(Object value) {
		Objects.requireNonNull(value);
		var hashvalue=hackersDelight(value);
		for(Entry element=array[hashvalue];element!=null;element = element.next()){
			if(element.value.equals(value)) {
				return;
			}
		}
		length++;
		array[hashvalue] = new Entry(value, array[hashvalue]);
	}


	public int size() {
		return length;
	}
}
```

### 3. On cherche maintenant à implanter une méthode forEach qui prend en paramètre une fonction. La méthode forEach parcourt tous les éléments insérés et pour chaque élément, appelle la fonction prise en paramètre avec l'élément courant.<br>Quelle doit être la signature de la functional interface prise en paramètre de la méthode forEach ?<br>Quel est le nom de la classe du package java.util.function qui a une méthode ayant la même signature ?<br>Écrire la méthode forEach.

```java
public class HashTableSet{
	public void forEach(Consumer<Object> function) {
		Objects.requireNonNull(function);
		for(var i = 0; i < SIZE;i++) {
			for(var element = array[i]; element != null; element = element.next()) {
				function.accept( element.value());
			}
		}
	}
}
```
La signature de la functional interface prise en paramètre est ``Consumer<T>``,  le nom du package du quel provient cette signature est : ``java.util.function.Consumer``

### 4. On souhaite maintenant ajouter une méthode contains qui renvoie si un objet pris en paramètre est un élément de l'ensemble ou pas, sous forme d'un booléen.<br>Expliquer pourquoi nous n'allons pas utiliser forEach pour implanter contains (Il y a deux raisons, une algorithmique et une spécifique à Java).<br>Écrire la méthode contains.
```java
public final class HashTableSet{

	public void add(Object value) {
		Objects.requireNonNull(value);
		var hashvalue=hackersDelight(value);
//		for(Entry element=array[hashvalue];element!=null;element = element.next()){
//			if(element.value.equals(value)) {
//				return;
//			}
//		}
		if(contains(value)) {
			return;
		}
		length++;
		array[hashvalue] = new Entry(value, array[hashvalue]);
	}

	public boolean contains(Object value) {
		Objects.requireNonNull(value);
		var hashvalue = hackersDelight(value);
		for(Entry element = array[hashvalue];element!= null;element = element.next()) {
			if(value.equals(element.value())) {
				return true;
			}
		}
		return false;
	}
}
```
Nous n'allons pas utiliser ``forEach`` car on ne va pas verifier pour toute les valeurs et on veut renvoyer directement si une valeur est trouvé

### 5. On veut maintenant faire en sorte que la table de hachage se redimensionne toute seule. Pour cela, lors de l'ajout d'un élément, on peut avoir à agrandir la table pour garder comme invariant que la taille du tableau est au moins 2 fois plus grande que le nombre d'éléments.<br>Pour agrandir la table, on va créer un nouveau tableau deux fois plus grand et recopier touts les éléments dans ce nouveau tableau à la bonne place. Ensuite, il suffit de remplacer l'ancien tableau par le nouveau.<br>Expliquer pourquoi, en plus d'être plus lisible, en termes de performance, l'agrandissement doit se faire dans sa propre méthode.<br>Modifier votre implantation pour que la table s'agrandisse dynamiquement.
```java
public final class HashTableSet{
	...
	private Entry[] updateSize(Entry[] array) {
		if(size()>(SIZE/2)) {
			SIZE = SIZE*2;
			Entry[] array2 = new Entry[SIZE];
			for(var i=0; i < SIZE/2	;i++) {
				for (var element = array[i];element!=null;element = element.next()) {
					array2[hackersDelight(element)] = new Entry(element.value(), array2[hackersDelight(element)]);
				}
			}
			array = array2;
		}
		return array;
	}

	public void add(Object value) {
		Objects.requireNonNull(value);
		if(contains(value)) {
			return;
		}
		array = updateSize(array);
		length++;
		array[hackersDelight(value)] = new Entry(value, array[hackersDelight(value)]);
	}
...
}


### Pour que ce code fonctionne, il faut dire que le type des éléments que l'on ajoute avec add et le type des éléments que l'on reçoit dans la lambda du forEach est le même. Pour cela, il faut déclarer HashTableSet comme un type paramétré.<br>Rappeler pourquoi en Java, il n'est pas possible de créer un tableau de type paramétré ? Quel est le work around ? Pourquoi celui-ci génère-t-il un warning ? Et dans quel cas et comment peut on supprimer ce warning ?
```java
public final class HashTableSet<T>{

	private record Entry<T>(T value,Entry<T> next) {
	}

	private Entry<T>[] array;
	private static int SIZE=16;
	private int length;

	@SuppressWarnings("unchecked")
	public HashTableSet(){
		length = 0;
		array = new Entry[SIZE];
	}

	private int hackersDelight(Object value) {
		return value.hashCode() & (SIZE-1);
	}

	@SuppressWarnings("unchecked")
	private Entry<T>[] updateSize(Entry<T>[] array) {
		if(size()>(SIZE/2)) {
			SIZE = SIZE*2;
			Entry<T>[] array2 = new Entry[SIZE];
			for(var i=0; i < SIZE/2	;i++) {
				for (var element = array[i];element!=null;element = element.next()) {
					array2[hackersDelight(element)] = new Entry<T>(element.value(), array2[hackersDelight(element)]);
				}
			}
			//			Consumer<Entry> cons = element -> array2[hackersDelight(element)] = new Entry(element.value(), array2[hackersDelight(element)]);
			//			forEach(cons);
			array = array2;
		}
		return array;
	}

	public void add(T value) {
		Objects.requireNonNull(value);
		if(contains(value)) {
			return;
		}
		array = updateSize(array);
		length++;
		array[hackersDelight(value)] = new Entry<T>(value, array[hackersDelight(value)]);
	}


	public int size() {
		return length;
	}

	public void forEach(Consumer<E> function) {
		Objects.requireNonNull(function);
		for(var i = 0; i < SIZE;i++) {
			for(var element = array[i]; element != null; element = element.next()) {
				function.accept(element.value());
			}
		}
		//Arrays.stream(array).flatMap(element -> Stream.iterate(element,e -> e.next())).map(Entry::value).forEach(function::accept);
	}

	public boolean contains(Object value) {
		Objects.requireNonNull(value);
		var hashvalue = hackersDelight(value);
		for(Entry<T> element = array[hashvalue];element!= null;element = element.next()) {
			if(value.equals(element.value())) {
				return true;
			}
		}
		return false;
	}
}

```
Cela genère un warning car le compilateur n'est pas sur si c'est le bon type, on peut le supprimer quand on est sur du type

### 7. En fait, la signature de la méthode forEach que vous avez écrite n'est pas la bonne. En effet, forEach appelle la lambda avec des éléments de type E, donc la fonction peut prendre en paramètre des valeurs qui sont des super-types de E.<br>Regarder comme on déclare un super-type dans un type paramétré en regardant la javadoc de la méthode forEach de ArrayList.
```java
public final class HashTableSet<T>{
	...
	public void forEach(Consumer<? super T> function) {
		Objects.requireNonNull(function);
		for(var i = 0; i < SIZE;i++) {
			for(var element = array[i]; element != null; element = element.next()) {
				function.accept(element.value());
			}
		}
		//Arrays.stream(array).flatMap(element -> Stream.iterate(element,e -> e.next())).map(Entry::value).forEach(function::accept);
	}
	...
}
```
### 8. On souhaite maintenant écrire une méthode addAll qui permet d'ajouter tous les éléments d'un HashTableSet dans le HashTableSet courant.<br>Note : les éléments du HashTableSet pris en paramètre peuvent être des sous-types du type du HashTableSet courant. Si vous ne savez pas comment déclarer un sous-type dans un type paramétré, vous pouvez regarder la méthode Collection.addAll() des API du JDK.
```java

```