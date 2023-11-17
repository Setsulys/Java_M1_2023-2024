# LY STEVEN TP7 GRAPH

### 1. On souhaite créer la classe paramétrée (par le type des valeurs des arcs) MatrixGraph comme seule implantation possible de l'interface Graph définie par le fichier Graph.java<br>La classe MatrixGraph contient
- Un champ array, un tableau des valeurs des arcs comme expliqué ci-dessus,
- Un constructeur qui prend en paramètre le nombre de nœuds du graphe,
- Une méthode nodeCount qui renvoie le nombre de nœuds du graphe.

### Pour l'implantation du constructeur, rappeler pourquoi, en Java, il n'est pas possible de créer des tableaux de variables de type.<br>Écrire la classe MatrixGraph, ses champs, son constructeur et la méthode nodeCount.
```java
final class MatrixGraph<T> implements Graph<T> {

	private final T[] array;
	private int nodeCount;
	@SuppressWarnings("unchecked")
	public MatrixGraph(int nodeNb) {
		if(nodeNb < 0) {
			throw new IllegalArgumentException();
		}
		this.nodeCount = nodeNb;
		this.array = (T[]) new Object[nodeCount * nodeCount];
	}
	
	public int nodeCount() {
		return nodeCount;
	}
}
```
```java
public sealed interface Graph<T> permits MatrixGraph{
  int nodeCount();
}
```
### Note : Pouvez-vous supprimer le warning à la construction ? Pourquoi ?
A la construction on peut supprimer le warning, car le warning se fait sur un cast et c'est un cast sûr.

### 2. On peut remarquer que la classe MatrixGraph n'apporte pas de nouvelles méthodes par rapport aux méthodes de l'interface Graph donc il n'est pas nécessaire que la classe MatrixGraph soit publique.<br>Ajouter une méthode factory nommée createMatrixGraph dans l'interface Graph et déclarer la classe MatrixGraph non publique.

```java
public sealed interface Graph<T> permits MatrixGraph{
    ...
    public static <T>Graph<T> createMatrixGraph(int nodeCount){
        return new MatrixGraph<T>(nodeCount);
    }
}
```

### 3. Indiquer comment trouver la case (i, j) dans un tableau à une seule dimension de taille nodeCount * nodeCount.<br>Si vous n'y arrivez pas, faites un dessin ! Afin d'implanter correctement la méthode getWeight, rappeler à quoi sert la classe java.util.Optional en Java.<br>Implanter la méthode addEdge en utilisant la javadoc pour savoir quelle est la sémantique exacte.<br>Implanter la méthode getWeight en utilisant la javadoc pour savoir quelle est la sémantique exacte.

```java
final class MatrixGraph<T> implements Graph<T> {
    ...
	public void addEdge(int src, int dst, T weight) {
		Objects.requireNonNull(weight);
		Objects.checkIndex(src, nodeCount());
		Objects.checkIndex(dst, nodeCount());
		this.array[src*nodeCount+dst]=weight;
	}
	
	public Optional<T> getWeight(int src, int dst){
		Objects.checkIndex(src, nodeCount());
		Objects.checkIndex(dst, nodeCount());
		return Optional.ofNullable(array[src*nodeCount+dst]);
	}
}
```
```java
public sealed interface Graph<T> permits MatrixGraph{
    ...
    void addEdge(int src, int dst, T weight);
    ...
    Optional<T> getWeight(int src, int dst);
}
```
Pour trouver une case ``(i,j)`` dans un tableau à une dimension, il faut faire : ``i*nodeCount +j``
La classe ``java.util.Optional`` nous permet de renvoyer une valeur ou rien

### 4. On souhaite maintenant implanter une méthode mergeAll qui permet d'ajouter les valeurs des arcs d'un graphe au graphe courant.<br>Dans le cas où on souhaite ajouter une valeur à un arc qui possède déjà une valeur, on utilise une fonction prise en second paramètre qui prend deux valeurs et renvoie la nouvelle valeur.<br>Implanter la méthode mergeAll en utilisant la javadoc pour savoir quelle est la sémantique exacte.
```java
final class MatrixGraph<T> implements Graph<T> {
    ...
    public void mergeAll(Graph<? extends T> graph, BinaryOperator<T> merger) {
		Objects.requireNonNull(graph);
		Objects.requireNonNull(merger);
		if(graph.nodeCount() != nodeCount()) {
			throw new IllegalArgumentException();
		}
		for(var src=0; src < nodeCount; src++) {
			for(var dst=0; dst < nodeCount(); dst++) {
				var weight1 = getWeight(src,dst);
				var weight2 = graph.getWeight(src, dst);
				if(weight2.isEmpty()) {
					continue;
				}
				if(weight1.isPresent()) {	
					addEdge(src, dst, merger.apply(weight1.orElseThrow(), weight2.orElseThrow()));	
				}
				else {
					addEdge(src,dst,weight2.orElseThrow());
				}
			}
		}
	}
}
```
```java
public sealed interface Graph<T> permits MatrixGraph{
    ...
    void mergeAll(Graph<? extends T> graph, BinaryOperator<T> merger);
}
```
Pour le merge all on doit utiliser un ``BinaryOperator<T>`` car on nous demande ``(T,T) -> T``

### 5. En fait, on peut remarquer que l'on peut écrire le code de mergeAll pour qu'il soit indépendant de l'implantation et donc écrire l'implantation de mergeAll directement dans l'interface.<br>Déplacer l'implantation de mergeAll dans l'interface et si nécessaire modifier le code pour qu'il soit indépendant de l'implantation.

```java
public sealed interface Graph<T> permits MatrixGraph{
	...
	default void mergeAll(Graph<? extends T> graph, BinaryOperator<T> merger) {
		Objects.requireNonNull(graph);
		Objects.requireNonNull(merger);
		if(graph.nodeCount() != nodeCount()) {
			throw new IllegalArgumentException();
		}
		for(var src=0; src < nodeCount(); src++) {
			for(var dst=0; dst < nodeCount(); dst++) {
				var weight1 = getWeight(src,dst);
				var weight2 = graph.getWeight(src, dst);
				if(weight2.isEmpty()) {
					continue;
				}
				if(weight1.isPresent()) {	
					addEdge(src, dst, merger.apply(weight1.orElseThrow(), weight2.orElseThrow()));	
				}
				else {
					addEdge(src,dst,weight2.orElseThrow());
				}
			}
		}
	}
}
```
### 6. Rappeler le fonctionnement d'un itérateur et de ses méthodes hasNext et next.<br>Que renvoie next si hasNext retourne false ?<br>Expliquer pourquoi il n'est pas nécessaire, dans un premier temps, d'implanter la méthode remove qui fait pourtant partie de l'interface.<br>Implanter la méthode neighborsIterator(src) qui renvoie un itérateur sur tous les nœuds ayant un arc dont la source est src.

```java
final class MatrixGraph<T> implements Graph<T> {
	...
	public Iterator<Integer> neighborIterator(int src){
		Objects.checkIndex(src, nodeCount());
		return new Iterator<>(){
			
			private Optional<Integer> it=isValid(0);
			
			private Optional<Integer> isValid(int value) {
				for(var dst=value; dst < nodeCount();dst++) {
					if(array[src*nodeCount+dst]!=null) {
						return Optional.of(dst);
					}
				}
				return Optional.empty();
			}
			
			@Override
			public boolean hasNext() {
				return it.isPresent();
			}

			@Override
			public Integer next() {
				if(!hasNext()) {
					throw new NoSuchElementException();
				}
				var value = it.orElseThrow();
				it = isValid(it.orElseThrow()+1);
				return value;
			}
			
		};
	}
}
```
```java
public sealed interface Graph<T> permits MatrixGraph{
	...
	Iterator<Integer> neighborIterator(int src);
}
```
- Un iterateur permet de parcourir notre array de notre classe avec une boucle forEach
- La methode ``hasNext()`` permet de savoir si l'élément suivant existe
- La methode ``next()`` renvoie l'élément suivant, si ``hasNext()`` renvoie vrai, sinon on renvoi un ``NoSuchElementException``

### 7. Expliquer le fonctionnement précis de la méthode remove de l'interface Iterator.<br>Implanter la méthode remove de l'itérateur.
```java
final class MatrixGraph<T> implements Graph<T> {
	...
	public Iterator<Integer> neighborIterator(int src){
		Objects.checkIndex(src, nodeCount());
		return new Iterator<>(){
			private Optional<Integer> it=isValid(0);
			private Optional<Integer> previous= Optional.empty();
			...
			@Override
			public Integer next() {
				if(!hasNext()) {
					throw new NoSuchElementException();
				}
				previous = it;
				it = isValid(it.orElseThrow()+1);
				return previous.orElseThrow();
			}
			
			public void remove() {
				if(previous.isEmpty()) {
					throw new IllegalStateException();
				}
				array[src*nodeCount+previous.orElseThrow()]=null;
				previous=it;
			}
			
		};
	}
}
```

### 8. On souhaite ajouter une méthode forEachEdge qui prend en paramètre un index d'un nœud et une fonction qui est appel cette fonction avec chaque arc sortant de ce nœud. <br>Pour cela, nous allons, dans un premier temps, définir le type Graph.Edge à l'intérieur de l'interface Graph. Un Graph.Edge est définie par un entier src, un entier dst et un poids weight.
```java
final class MatrixGraph<T> implements Graph<T> {
	
	public record Edge<T>(int src,int dst,T weight){
		
	}
	...
}
```
```java
public sealed interface Graph<T> permits MatrixGraph{
	...
	default void forEachEdge(int src,Consumer<? super Edge<T>> function) {
		Objects.requireNonNull(function);
		Objects.checkIndex(src, nodeCount());
		for(var dst=0; dst < nodeCount();dst++) {
			if(!getWeight(src, dst).isEmpty()) {
				var value = new Edge<T>(src,dst,getWeight(src, dst).get());
				function.accept(value);
			}
		}
	}
}
```

### 9. Enfin, on souhaite écrire une méthode edges qui renvoie tous les arcs du graphe sous forme d'un stream.<br>L'idée ici n'est pas de réimplanter son propre stream (c'est prévu dans la suite du cours) mais de créer un stream sur tous les nœuds (sous forme d'entier) puis pour chaque nœud de renvoyer tous les arcs en réutilisant la méthode forEachEdge que l'on vient d'écrire.

```java

```