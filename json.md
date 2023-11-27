# LY STEVEN TP8 JSON


### 1. Avant de se lancer dans l'écriture de la méthode toJSON, on va commencer par écrire une méthode d'aide (helper method) invoke(method, object) qui appelle la méthode method sur l'objet object en utilisant la réflexion. On extrait le code de cette méthode du reste du code, car on veut gérer les exceptions correctement.<br>Quelle est la méthode qui permet d'appeler une méthode (de type java.lang.reflect.Method) sur un objet ?<br>Quelle est l'exception qui peut être levée...
- ... parce que les arguments de la méthode de java.lang.reflect.Method ne sont pas bons ?<br>Comment doit-on la traiter ?
- ... parce que la méthode à appeler n'est pas visible ?<br>Ici on veut lever une Error si c'est le cas, quelle Error doit-on lever, et comment faire ?
- ... parce que la méthode appelée lève elle-même une exception checked, une exception non checked ou une erreur ?<br>Pour chacun de ses 3 cas, que doit-on faire ?
```java
public class JSONPrinter {

	static Object invoke(Method method, Object object) {
		Objects.requireNonNull(method);
		Objects.requireNonNull(object);
		try {
			var m = method.invoke(object);
			return m;
		} catch (IllegalAccessException e) {
			throw new IllegalAccessError();
		} catch(InvocationTargetException e) {
			var cause = e.getCause();
			switch(cause) {
			case RuntimeException rte -> throw rte;
			case Error error -> throw error;
			default -> throw new UndeclaredThrowableException(cause);
			}
		}
	}
}
```
### 2. On souhaite maintenant écrire la méthode toJSON qui prend en paramètre un java.lang.Record, utilise la réflexion pour accéder à l'ensemble des composants d'un record (java.lang.Class.getRecordComponent), sélectionne les accesseurs, puis affiche les couples nom du composant, valeur associée au format JSON.
```java
public class JSONPrinter {
    ...
	@SuppressWarnings("unused")
	private static String escape(Object o) {
		return o instanceof String s ? "\"" + s + "\"": "" + o;
	}

	public static String toJSON(Record rec) {
		Objects.requireNonNull(rec);

		return Arrays.stream(rec.getClass().getRecordComponents())
				.map(e-> "\""+e.getName+"\":" +escape(invoke(e.getAccessor(),rec)))
				.collect(Collectors.joining(",","{","}"));
	}
}
```
### 3. En fait, on peut avoir des noms de clé d'objet JSON qui ne sont pas des noms valides en Java, par exemple "book-title", pour cela on se propose d'utiliser une annotation pour indiquer quel doit être le nom de clé utilisée pour générer le JSON.<br>Déclarez l'annotation JSONProperty visible à l'exécution et permettant d'annoter des composants de record, puis modifiez le code de toJSON pour n'utiliser que les propriétés issues de méthodes marquées par l'annotation JSONProperty.
```java
public class JSONPrinter {
    ...
	public static String toJSON(Record rec) {
		Objects.requireNonNull(rec);

		return Arrays.stream(rec.getClass().getRecordComponents())
				.map(e-> {
					var annote = e.isAnnotationPresent(JSONProperty.class)? e.getAnnotation(JSONProperty.class).value():e.getName();
					return "\""+annote+"\":" +escape(invoke(e.getAccessor(),rec));
					}
				)
				.collect(Collectors.joining(",","{","}"));
	}
}
```
### 4. On souhaite maintenant pouvoir gérer des listes de records, pour cela, nous allons ajouter une surcharge à la méthode toJSON qui prend en paramètre une liste de records.
```java
public class JSONPrinter {
    ...
	public static String toJSON(List<? extends Record> list) {
		Objects.requireNonNull(list);
		list.forEach(Objects::requireNonNull);
		return list.stream().map(e -> toJSON(e)).collect(Collectors.joining(",","[","]"));
	}
}
```
### 5.
Parce que c'est sur un tableau, donc il y a une copie défensive alors c'est lent

### 6. Nous allons donc limiter les appels à getRecordComponents en stockant le résultat de getRecordComponents dans un cache pour éviter de faire l'appel à chaque fois qu'on utilise toJSON(record).<br>Utilisez la classe ClassValue pour mettre en cache le résultat d'un appel à getRecordComponents pour une classe donnée.
```java
public class JSONPrinter {
    private static final ClassValue<RecordComponent[]> CACHE = new ClassValue<>() {

		@Override
		protected RecordComponent[] computeValue(Class<?> type) {
			return type.getRecordComponents();
		}
		
	};
    ...
    public static String toJSON(Record record) {
		Objects.requireNonNull(record);

		return Arrays.stream(CACHE.get(record.getClass()))
				.map(e-> {
					var annote = e.isAnnotationPresent(JSONProperty.class)? e.getAnnotation(JSONProperty.class).value():e.getName();
					return "\""+annote+"\":" +escape(invoke(e.getAccessor(),record));
					}
				)
				.collect(Collectors.joining(",","{","}"));
	}
    ...
}
```