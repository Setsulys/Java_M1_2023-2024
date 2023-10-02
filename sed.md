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
			return (String str) -> rule2.rewrite(rule1.rewrite(str).get());
		}
	}
    ...


}
```