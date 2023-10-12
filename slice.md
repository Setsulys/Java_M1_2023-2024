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