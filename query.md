# LY STEVEN TP10 QUERY

### 1. On souhaite écrire une interface Query ainsi qu'une classe QueryImpl qui est une classe interne de l'interface Query et qui va contenir l'implantation de l'interface. Cette classe doit être la seule implantation possible.
Ce n'est pas très beau comme design, mais cela fait un seul fichier ce qui est plus pratique pour la correction.
L'interface Query doit posséder une méthode fromList qui permet de créer une Query comme expliqué ci-dessus. De plus, il doit être possible d'afficher les éléments d'une Query avec la méthode toString() qui effectue le calcul des éléments et les affiche. L'affichage contient tous les éléments présents (ceux pour qui la fonction prise en second paramètre renvoie un élément présent) séparés par le symbole " |> ".
```java
public sealed interface Query<E>{
	public final class QueryImpl<T,E> implements Query<T>{

		private final List<E> selfList;
		private final Function<? super E,? extends Optional<? extends T>> function;
		
		QueryImpl(List<E> list,Function<? super E,? extends Optional<? extends T>> function) {
			Objects.requireNonNull(list);
			Objects.requireNonNull(function);
			this.selfList = list;
			this.function = function;
			
		}
		
		@Override
		public String toString() {
			return selfList.stream()
					.map(e -> function.apply(e))
					.filter(f -> f.isPresent())
					.map(s -> s.get().toString())
					.collect(Collectors.joining(" |> "));
		}
	}

	public static <T,E>Query<T> fromList(List<E> recordsList, Function<? super E,? extends Optional<? extends T>> function) {
		Objects.requireNonNull(recordsList);
		Objects.requireNonNull(function);
		return new QueryImpl<>(recordsList, function);
	}
}
```
### 2. On souhaite ajouter une méthode toList à l'interface Query dont le but est de renvoyer dans une liste non-modifiable les éléments présents.
```java
public sealed interface Query<E>{
	public final class QueryImpl<T,E> implements Query<T>{
		...
		public List<T> toList(){
			return selfList.stream()
					.map(e -> function.apply(e))
					.filter(f -> f.isPresent())
					.map(l -> l.get())
					.collect(Collectors.toUnmodifiableList());
		}
		
	}
	...
	List<E> toList();
}
```

### 3. On souhaite maintenant ajouter une méthode toStream qui renvoie un Stream des éléments présents dans une Query.
```java
public sealed interface Query<E>{
	public final class QueryImpl<T,E> implements Query<T>{
		...
		public Stream<T> toStream(){
			return selfList.stream()
					.map(e -> function.apply(e))
					.filter(f -> f.isPresent())
					.map(l -> l.get());
		}
		
	}
	...
	Stream<E> toStream();
}
```
### 4. On souhaite ajouter une méthode toLazyList qui renvoie une liste non-modifiable dont les éléments sont calculés et mis dans un cache (une liste modifiable) lorsque l'on a besoin de les connaître. Attention, on ne doit remplir le cache qu'avec les éléments dont on a besoin.
```java
public sealed interface Query<E>{
	public final class QueryImpl<T,E> implements Query<T>{
	    ...
		public List<T> toLazyList(){
			return new AbstractList<T>() {
				private Iterator<E> iterator = selfList.iterator();
				private List<T> cache = new ArrayList<T>();;
				@Override
				public int size() {
					while(iterator.hasNext()) {
						function.apply(iterator.next()).ifPresent(cache::add);
					}
					return cache.size();
				}

				@Override
				public T get(int index) {
					while(index >= cache.size() && iterator.hasNext()) {
						function.apply(iterator.next()).ifPresent(cache::add);
					}
					return cache.get(index);
				}
			};
		}
	}
	...
	List<E> toLazyList();
}
````````````````````````````````````````````````````````````````````````````````