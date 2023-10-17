# LY STEVEN TP3 SLICE


## Exercice 2 - The Slice and The furious

### 1. On va dans un premier temps créer une interface Slice avec une méthode array qui permet de créer un slice à partir d'un tableau en Java. <br>L'interface Slice est paramétrée par le type des éléments du tableau et permet que les éléments soient null.<br>L'interface Slice possède deux méthodes d'instance, size qui renvoie le nombre d'éléments et get(index) qui renvoie le index-ième (à partir de zéro).<br>En termes d'implantation, on va créer une classe interne à l'interface Slice nommée ArraySlice implantant l'interface Slice. L'implantation ne doit pas recopier les valeurs du tableau donc un changement d'une des cases du tableau doit aussi être visible si on utilise la méthode get(index).<br>Écrire l'interface Slice puis implanter la classe SliceArray et ses méthodes array, size et get(index).

```java
public sealed interface Slice<E>{
    
    public int size();
	public E get(int index);
    
    public static <E> ArraySlice<E> array(E[] array) {
		Objects.requireNonNull(array);
		return new ArraySlice<E>(array);
	}

	public final class ArraySlice<T> implements Slice<T>{

		private final T[] arrayList;
		
		private ArraySlice(T[] array){
            Objects.requireNonNull(array);
			arrayList = array;
		}
		
        @Override
		public int size() {
			return arrayList.length;
		}
		
        @Override
		public T get(int index) {
			Objects.checkIndex(index, size());
			return arrayList[index];
		}
	}

}
```

### 2. On souhaite que l'affichage d'un slice affiche les valeurs séparées par des virgules avec un '[' et un ']' comme préfixe et suffixe.
```java
public sealed interface Slice<E>{
    ...
	public final class ArraySlice<T> implements Slice<T>{
        ...
        @Override
		public String toString() {
			return Arrays.stream(arrayList).map(String::valueOf).collect(Collectors.joining(", ","[","]"));
		}
    }
}
```

### 3. On souhaite ajouter une surcharge à la méthode array qui, en plus de prendre le tableau en paramètre, prend deux indices from et to et montre les éléments du tableau entre from inclus et to exclus.
```java
public sealed interface Slice<E>{
    ...       
	public static<E> SubArraySlice<E> array(E[]array,int from,int to){
        Objects.requireNonNull(array);
		Objects.checkFromToIndex(from, to, array.length);
		return new SubArraySlice<E>(array,from,to);
    }

	public final class SubArraySlice<T> implements Slice<T>{
		
		private final T[] arrayList;
		private int fromList;
		private int toList;
		
		private SubArraySlice(T[]array,int from, int to) {
			Objects.requireNonNull(array);
			Objects.checkFromToIndex(from, to, array.length);
			arrayList=array;
			fromList = from;
			toList = to;
		}
		
        @Override
		public int size() {
			return toList-fromList;
		}
		
        @Override
		public T get(int index) {
			Objects.checkIndex(index, size());
			return arrayList[fromList +index];
		}
		
		@Override
		public String toString() {
			return Arrays.stream(arrayList,fromList,toList).map(String::valueOf).collect(Collectors.joining(", ","[","]"));
		}
	}
}
	
```

### 4. On souhaite enfin ajouter une méthode subSlice(from, to) à l'interface Slice qui renvoie un sous-slice restreint aux valeurs entre from inclus et to exclu.
```java
public sealed interface Slice<E>{
	...
    public Slice<E> subSlice(int from, int to);

	public final class ArraySlice<T> implements Slice<T>{
        ...

        @Override
		public Slice<T> subSlice(int from,int to) {
			Objects.checkFromToIndex(from, to, size());
			return new SubArraySlice<T>(arrayList,from,to);
		}
		
	}
	
	public final class SubArraySlice<T> implements Slice<T>{
        ...      

        @Override
		public Slice<T> subSlice(int from,int to) {
			Objects.checkFromToIndex(from, to, size());
			return new SubArraySlice<T>(arrayList,fromList +from,fromList +to);
		}
	}
	

}

```

## Exercice 3 - 2 Slice 2 Furious

### 1. Recopier l'interface Slice de l'exercice précédent dans une interface Slice2. Vous pouvez faire un copier-coller de Slice dans même package, votre IDE devrait vous proposer de renommer la copie. Puis supprimer la classe interne SubArraySlice ainsi que la méthode array(array, from, to) car nous allons les réimplanter et commenter la méthode subSlice(from, to) de l'interface, car nous allons la ré-implanter aussi, mais plus tard.

### 2. Déclarer une classe SubArraySlice à l'intérieur de la classe ArraySlice comme une inner class donc pas comme une classe statique et implanter cette classe et la méthode array(array, from, to).
```java
public sealed interface Slice2<E>{
	...
	public static <E>ArraySlice<E>.SubArraySlice array(E[] array, int from, int to) {
		Objects.requireNonNull(array);
		Objects.checkFromToIndex(from, to, array.length);
		return new ArraySlice<E>(array).new SubArraySlice(from,to);
	}

	public final class ArraySlice<T> implements Slice2<T>{
        ...
		public final class SubArraySlice implements Slice2<T>{
			private int fromList;
			private int toList;
		
			private SubArraySlice(int  from, int to) {
				fromList =from;
				toList = to;
			}
			
            @Override
			public int size() {
				return toList - fromList;
			}
			
            @Override
			public T get(int index) {
				Objects.checkIndex(index, size());
				return arrayList[fromList + index];
			}
			
			@Override
			public String toString() {
				return Arrays.stream(arrayList,fromList,toList).map(String::valueOf).collect(Collectors.joining(", ","[","]"));
			}
			
		}
	}
}
```

### 3. On souhaite enfin ajouter une méthode subSlice(from, to) à l'interface Slice qui renvoie un sous-slice restreint aux valeurs entre from inclus et to exclu.
```java

public sealed interface Slice2<E>{
	...
	public Slice2<E> subSlice(int from,int to);
	...

	public final class ArraySlice<T> implements Slice2<T>{
        ...
		@Override
		public  ArraySlice<T>.SubArraySlice subSlice(int from,int to){
			Objects.checkFromToIndex(from, to,size( ));
			return new ArraySlice<T>(arrayList).new SubArraySlice(from,to);
		}
		
		public final class SubArraySlice implements Slice2<T>{
            ...
			@Override
			public ArraySlice<T>.SubArraySlice subSlice(int from, int to) {
				Objects.requireNonNull(arrayList);
				Objects.checkFromToIndex(from, to, size());
				return new ArraySlice<T>(arrayList).new SubArraySlice(fromList + from,fromList +to);
			}
			
		}
	}
}
```

### 4. Dans quel cas va-t-on utiliser une inner class plutôt qu'une classe interne ?
On va utiliser une inner class au lieu d'une classe interne car ...

## Exercice 4 - The Slice and The Furious: Tokyo Drift

### 1. Recopier l'interface Slice du premier exercice dans une interface Slice3. Supprimer la classe interne SubArraySlice ainsi que la méthode array(array, from, to) car nous allons les réimplanter et commenter la méthode subSlice(from, to) de l'interface, car nous allons la réimplanter plus tard.<br>Puis déplacer la classe ArraySlice à l'intérieur de la méthode array(array) et transformer celle-ci en classe anonyme.
```java

public interface Slice3<E>{
	public int size();
	public E get(int index);
//	public Slice3<E> subSlice(int from, int to);

	public static <E> Slice3<E> array(E[] array) {
		Objects.requireNonNull(array);

		return new Slice3<E>(){
			private final E[] arrayList=array;

			@Override
			public int size() {
				return arrayList.length;
			}

			@Override
			public E get(int index) {
				Objects.checkIndex(index, size());
				return arrayList[index];
			}

			@Override
			public String toString() {
				return Arrays.stream(arrayList).map(String::valueOf).collect(Collectors.joining(", ","[","]"));
			}

//			@Override
//			public Slice3<T> subSlice(int from,int to) {
//				Objects.checkFromToIndex(from, to, size());
//				return new SubArraySlice<T>(arrayList,from,to);
//			}
		};
	}
}

```

### 2. On va maintenant chercher à implanter la méthode subSlice(from, to) directement dans l'interface Slice3. Ainsi, l'implantation sera partagée.<br>Écrire la méthode subSlice(from, to) en utilisant là encore une classe anonyme.<br>Comme l'implantation est dans l'interface, on n'a pas accès au tableau qui n'existe que dans l'implantation donnée dans la méthode array(array)... mais ce n'est pas grave, car on peut utiliser les méthodes de l'interface.<br>Puis fournissez une implantation à la méthode array(array, from, to).
```java
public interface Slice3<E> {
	...
	public default Slice3<E> subSlice(int from, int to) {
		Objects.checkFromToIndex(from, to,Slice3.this.size());
		return new Slice3<E>() {

			@Override
			public int size() {
				return to - from;
			}

			@Override
			public E get(int index) {
				Objects.checkIndex(index, size());
				return Slice3.this.get(from +index);
			}
			@Override
			public String toString() {
				return IntStream.range(0, size()).mapToObj(e -> get(e)).map(String::valueOf).collect(Collectors.joining(", ","[","]"));
			}
		};
	}
	...	
	public static <E> Slice3<E> array(E[] array, int from,int to){
		Objects.requireNonNull(array);
		Objects.checkFromToIndex(from, to, array.length);
		return array(array).subSlice(from, to);
	}
}

```
### 3. Dans quel cas va-t-on utiliser une classe anonyme plutôt qu'une classe interne ?
On va utiliser une classe anonyme plutôt qu'une innerclass quand on a pas besoin de faire appel a la classe

## Exercice 5 - Slice & Furious (optionnel)

### 1. Déclarer l'interface Slice4 avec les méthodes size, get(index) et subSlice(from, to) abstraites. De plus, la méthode array(array) peut déléguer son implantation à la méthode array(array, from, to).<br>Pour l'instant, commenter la méthode subSlice(from, to) que l'on implantera plus tard.<br>À la suite du fichier, déclarer une classe non publique SliceImpl implantant l'interface Slice4 et implanter la méthode array(array, from, to).
```java
public sealed interface Slice4<E>{

	abstract int size();
	abstract E get(int index);
	//public Slice4<E> subSlice(int from, int to);

	static <E> Slice4<E> array(E[] array) {
		Objects.requireNonNull(array);
		return array(array, 0, array.length);
	}

	static<E> Slice4<E> array(E[]array,int from,int to){
		Objects.requireNonNull(array);
		Objects.checkFromToIndex(from, to, array.length);
		return new SliceImpl<E>(array,from, to);
	}

}

 final class SliceImpl<V> implements Slice4<V>{
	 
	 private final V[] innerArray;
	 private final int fromArray;
	 private final int toArray;
	 
	 public SliceImpl(V[] array,int from, int to){
		 Objects.requireNonNull(array);
		 Objects.checkFromIndexSize(from, from, array.length);
		 innerArray = array;
		 fromArray = from;
		 toArray = to;
		 
	 }

	@Override
	public int size() {
		return toArray - fromArray;
	}

	@Override
	public V get(int index) {
		Objects.checkIndex(index,size());
		return innerArray[fromArray + index];
	}
	
	@Override
	public String toString() {
		return IntStream.range(0, size()).mapToObj(e -> get(e)).map(String::valueOf).collect(Collectors.joining(", ","[","]"));
	}
}
```

### 2. Dé-commenter la méthode subSlice(from, to) et fournissez une implantation de cette méthode.

```java
public sealed interface Slice4<E>{

	abstract int size();
	abstract E get(int index);
	abstract Slice4<E> subSlice(int from, int to);

	static <E> Slice4<E> array(E[] array) {
		Objects.requireNonNull(array);
		return array(array, 0, array.length);
	}

	static<E> Slice4<E> array(E[]array,int from,int to){
		Objects.requireNonNull(array);
		Objects.checkFromToIndex(from, to, array.length);
		return new SliceImpl<E>(array,from, to);
	}

}

 final class SliceImpl<V> implements Slice4<V>{
	 
	 private final V[] innerArray;
	 private final int fromArray;
	 private final int toArray;
	 
	 public SliceImpl(V[] array,int from, int to){
		 Objects.requireNonNull(array);
		 Objects.checkFromToIndex(from, to, array.length);
		 innerArray = array;
		 fromArray = from;
		 toArray = to;
		 
	 }

	@Override
	public int size() {
		return toArray - fromArray;
	}

	@Override
	public V get(int index) {
		Objects.checkIndex(index,size());
		return innerArray[fromArray + index];
	}
	
	@Override
	public String toString() {
		return Arrays.stream(innerArray,fromArray,toArray).map(String::valueOf).collect(Collectors.joining(", ","[","]"));
	}

	@Override
	public Slice4<V> subSlice(int from, int to) {
		Objects.checkFromToIndex(from, to,size());
		return new SliceImpl<>(innerArray,fromArray+from,fromArray+to);
	}
}
```

### 3. On peut remarquer qu'en programmation objet il y a une toujours une tension entre avoir une seule classe et donc avoir des champs qui ne servent pas vraiment pour certaines instances et avoir plusieurs classes ayant des codes très similaires, mais avec un nombre de champs différents.<br>L'orthodoxie de la POO voudrait que l'on ait juste le nombre de champs qu'il faut, en pratique, on a tendance à ne pas créer trop de classes, car plus on a de code plus c'est difficile de le faire évoluer<br>À votre avis, pour cet exemple, est-il préférable d'avoir deux classes une pour les tableaux et une pour les tableaux avec des bornes ou une seule classe gérant les deux cas ?