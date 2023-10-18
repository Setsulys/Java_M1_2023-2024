# LY STEVEN TP4 STEVEN

## Exercice 2 - HashTableSet

### 1. Quels doivent être les champs de la classe Entry correspondant à une case d'une des listes chaînées utilisées par table de hachage<br>Rappeler quelle est l'intérêt de déclarer Entry comme membre de la classe HashTableSet plutôt que comme une classe à coté dans le même package que HashTableSet ?<br>Ne pourrait-on pas utiliser un record plutôt qu'une classe, ici ? Si oui, pourquoi ? Si non, pourquoi ?<br>Écrire la classe HashTableSet dans le package fr.uge.set et ajouter Entry en tant que classe interne.
```java
public class HashTableSet<T>{
	private record Entry<T>(T value,Entry next) {
	}
}
```
Les champs de la classe Entry doivent etre, le type voulus ``<T>`` et ``Entry`` pour pouvoir chainer 
<br> L'interet de déclarer ``Entry`` comme un membre de la classe ``HashTableSet`` est qu'on peut directement l'utiliser
<br> ``Entry`` doit etre un record car on y stocke des données.

### 2. On souhaite maintenant ajouter un constructeur sans paramètre, une méthode add qui permet d'ajouter un élément non null et une méthode size qui renvoie le nombre d'éléments insérés (avec une complexité en O(1)).<br>Pour l'instant, on va dire que la taille du tableau est toujours 16, on fera en sorte que la table de hachage s'agrandisse toute seule plus tard.<br>Dans la classe HashTableSet, implanter le constructeur et les méthodes add et size.

```java

```
`````````````````