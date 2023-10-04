# LY STEVEN TP2 SED

### 1. On va dans un premier temps définir une interface Rule qui va représenter une règle. Une règle prend en entrée une ligne (une String) et renvoie soit une nouvelle ligne soit rien (on peut supprimer une ligne). <br>Rappeler comment on indique, en Java, qu'une méthode peut renvoyer quelque chose ou rien ?

```java
public class StreamEditor {

	@FunctionalInterface
	public interface Rule{
		Optional<String> rewrite(String str);
	}
}
```
En Java, on indique qu'une méthode peut renvoyer quelque chose ou rien en utilisant un ``Optional<>``

Je créé une interface fonctionnelle ``Rule`` ayant une méthode ``rewrite`` prennant en parametre un ``String`` et renvoyant un ``Optional<String>``.

#### 2. Avant de créer, dans StreamEditor, la méthode rewrite qui prend deux fichiers, on va créer une méthode rewrite intermédiaire qui travaille sur des flux de caractères. On souhaite écrire une méthode rewrite(reader, writer) qui prend en paramètre un BufferedReader (qui possède une méthode readLine()) ainsi qu'un Writer qui possède la méthode write(String).<br>Comment doit-on gérer l'IOException ?


```java
public final class StreamEditor {
    ...
    public void rewrite(BufferedReader reader, Writer writer) throws IOException{
		Objects.requireNonNull(reader);
		Objects.requireNonNull(writer);
		String line;
		while((line = reader.readLine())!= null) {
			var rew = rule.rewrite(line);
			if(!rew.isEmpty()) {
				writer.write(rew.get());
				writer.write("\n");
			}
		}
	}
}
```
On ne gère pas vraiment l'IOException, on la throw

#### 3. On souhaite créer la méthode rewrite(input, output) qui prend deux fichiers (pour être exact, deux chemins vers les fichiers) en paramètre et applique la règle sur les lignes du fichier input et écrit le résultat dans le fichier output.<br>Comment faire en sorte que les fichiers ouverts soit correctement fermés ?<br>Comment doit-on gérer l'IOException ?

```java
public final class StreamEditor {
    ...
	public void rewrite(Path input, Path output) throws IOException {
		Objects.requireNonNull(input);
		Objects.requireNonNull(output);
		try(var reader =Files.newBufferedReader(input)){
			try(var writer = Files.newBufferedWriter(output)){
				rewrite(reader,writer);
			}
		}	
	}
}
```
Pour faire en sorte que les fichiers ouverts soient bien correctement fermé, il faut utiliser un ``try()`` qui fermera directement le fichier, en effet le try parenthèse considère qu'il y a un finally où l'on ferme le fichier.

On throw les IOExceptions

#### 4. On va écrire la méthode createRules qui prend en paramètre une chaîne de caractères et qui construit la règle correspondante.<br>Pour l'instant, on va considérer qu'une règle est spécifiée par un seul caractère :
- "s" veut dire strip (supprimer les espaces),
- "u" veut dire uppercase (mettre en majuscules),
- "l" veut dire lowercase (mettre en minuscules) et
- "d" veut dire delete (supprimer).

```java
public final class StreamEditor {
    ...
	public static Rule createRules(String string) {
		Objects.requireNonNull(string);
		return switch(string) {
		case "s" ->line -> Optional.of(line.strip()); 
		case "u" ->line -> Optional.of(line.toUpperCase(Locale.FRENCH));
		case "l" ->line -> Optional.of(line.toLowerCase(Locale.FRENCH));
		case "d" ->line ->Optional.empty();
		default -> {throw new IllegalArgumentException();}
		};
	}
}
```
On doit passer par un ``switch`` qui crééra la règle selon le caractère spécifié. Le ``default`` envera un ``IllegalArgumentException()``
<br>Pour que les majuscules/minuscules fonctionnent de la même façon que la configuration de l'OS sur lequel tourne l'application, il faut faire ``toUpperCase(Locale.FRENCH)`` au lieu de ``toUpperCase()`` et pareil pour ``toLowerCase()`` il faudra écrire ``toLowerCase(Local.FRENCH)``

#### 5. On veut pouvoir composer les règles, par exemple, on veut que "sl" strip les espaces puis mette le résultat en minuscules. Pour cela, dans un premier temps, on va écrire une méthode statique andThen dans Rule, qui prend en paramètre deux règles et renvoie une nouvelle règle qui applique la première règle puis applique la seconde règle sur le résultat de la première.

```java
public final class StreamEditor {

	@FunctionalInterface
	public interface Rule{
		Optional<String> rewrite(String s);

		static Rule andThen(Rule rule1, Rule rule2) {
			Objects.requireNonNull(rule1);
			Objects.requireNonNull(rule2);
			return (String line) -> rule1.rewrite(line).flatMap(rule2::rewrite);
		}
	}
    ...
		public static Rule createRules(String string) {
		Objects.requireNonNull(string);
		Rule rule = line ->Optional.of(line);
		for(var c=0; c<string.length();c++) {
			Rule newRule = switch(String.valueOf(string.charAt(c))) {
			case "s" ->line -> Optional.of(line.strip());
			case "u" ->line -> Optional.of(line.toUpperCase(Locale.FRENCH));
			case "l" ->line -> Optional.of(line.toLowerCase(Locale.FRENCH));
			case "d" ->line -> Optional.empty();
			case "" -> line -> Optional.of(line);
			default -> throw new IllegalArgumentException();
			};
			rule = Rule.andThen(rule,newRule);
		}
		return rule;
	}

}
```
On doit modifier la méthode ``createRules`` pour qu'elle puisse prendre plusieurs options a la suite, pour ca on utilise un for qui décompose la string et on fait le switch pour récuperer la nouvelle règle et on applique celle ci a l'ancienne règle avec ``andThen`

#### 6. En fait, déclarer andThen en tant que méthode statique n'est pas très "objet" ... En orienté objet, on préfèrerait écrire rule1.andThen(rule2) plutôt que Rule.andThen(rule1, rule2). On va donc implanter une nouvelle méthode andThen dans Rule, cette fois-ci comme une méthode d'instance.
```java
public final class StreamEditor {

	@FunctionalInterface
	public interface Rule{
		...
		default Rule andThen(Rule rule) {
			Objects.requireNonNull(rule);
			return (String line) -> rule.rewrite(this.rewrite(line).get());
		}
	}
	...
}
```
#### 7. On souhaite implanter la règle qui correspond au if, par exemple, "i=foo;u", qui veut dire si la ligne courante est égal à foo (le texte entre le '=' et le ';') alors, on met en majuscules sinon on recopie la ligne.<br>Avant de modifier createRules(), on va créer, dans Rule, une méthode statique guard(function, rule) qui prend en paramètre une fonction et une règle et crée une règle qui est appliquée à la ligne courante si la fonction renvoie vrai pour cette ligne. Autrement dit, on veut pouvoir créer une règle qui s'applique uniquement aux lignes pour lesquelles la fonction renvoie vrai.<br>Quelle interface fonctionnelle correspond à une fonction qui prend une String et renvoie un boolean ?

```java
public final class StreamEditor {

	@FunctionalInterface
	public interface Rule{
		...
		static Rule guard(Predicate<String> function,Rule rule) {
			Objects.requireNonNull(function);
			Objects.requireNonNull(rule);
			return (String line) ->{
				if(function.test(line)) {
					return rule.rewrite(line);
				}
				else {
					return Optional.of(line);
				}
			};
		}
	}
	...
	private static Rule switchOnRule(String string) {
		Objects.requireNonNull(string);
		Rule rule = line ->Optional.of(line);
		for(var c=0; c<string.length();c++) {
			Rule newRule = switch(String.valueOf(string.charAt(c))) {
			case "s" ->line -> Optional.of(line.strip());
			case "u" ->line -> Optional.of(line.toUpperCase(Locale.FRENCH));
			case "l" ->line -> Optional.of(line.toLowerCase(Locale.FRENCH));
			case "d" ->line -> Optional.empty();
			case "" -> line -> Optional.of(line);
			default -> throw new IllegalArgumentException();
			};
			rule = Rule.andThen(rule,newRule);
		}
		return rule;
	}

	public static Rule createRules(String string) {
		Objects.requireNonNull(string);
		Rule rule = line ->Optional.of(line);
		if(Pattern.compile(".*i=.*").matcher(string).matches()) {
			System.out.println(string.split("i=")[1].split(";")[1]);
			String strPred =string.split("i=")[1].split(";")[0];
			Predicate<String> pred = s -> s.matches(strPred);
			string = string.split("i=")[1].split(";")[1];
			rule= switchOnRule(string);
			Rule.guard(pred, rule);
		}
		else {
			rule = switchOnRule(string);
		}
		return rule;
	}
}

```
L'interface fonctionnel qui prend une String et renvoie un boolean est un ``Predicate<String>``

Malheureusement après grande reflexion je n'ai pas pu finir la question