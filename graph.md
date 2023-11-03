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
		Objects.checkIndex(src, nodeCount);
		Objects.checkIndex(dst, nodeCount);
		this.array[src*nodeCount+dst]=weight;
	}
	
	public Optional<T> getWeight(int src, int dst){
		Objects.checkIndex(src, nodeCount);
		Objects.checkIndex(dst, nodeCount);
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
    public void mergeAll(Graph<? extends T> graph, BinaryOperator<T> merger){
        Objects.requireNonNull(graph);
        Objects.requireNonNull(merger);
        if(graph.nodeCount()!=nodeCount()){
            throw new IllegalArgumentException();
        }
        .
        .
        .
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
```
```java
```
```java
```
```java
```
```java
```
```java
```