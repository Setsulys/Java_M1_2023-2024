# LY STEVEN TP9 SIMD

### 1. On cherche à écrire une fonction sum qui calcule la somme des entiers d'un tableau passé en paramètre. Pour cela, nous allons utiliser l'API de vectorisation pour calculer la somme sur des vecteurs.
- Quelle est la classe qui représente des vecteurs d'entiers ?
    <br>``IntVector``
- Qu'est ce qu'un VectorSpecies et quelle est la valeur de VectorSpecies que nous allons utiliser dans notre cas ?
    <br>``IntVector.SPECIES_PREFERRED``
- Comment créer un vecteur contenant des zéros et ayant un nombre préféré de lanes ?
    <br>``IntVector.zero(species)``
- Comment calculer la taille de la boucle sur les vecteurs (loopBound) ?
    <br>``array.length - array.length % species.length();``
- Comment faire la somme de deux vecteurs d'entiers ?
    <br>``vector.add(IntVector.fromArray(species, array, i));`` i etant le début de la lane
- Comment faire la somme de toutes les lanes d'un vecteur d'entiers ?
    <br> ``vector.reduceLanes(VectorOperators.ADD)``
Si la longueur du tableau n'est pas un multiple du nombre de lanes, on va utiliser une post-loop, quel doit être le code de la post-loop ?
    <br>``for(var i=loopBound; i < length;i++) {sumVector+=array[i];}``
```java
public class VectorComputation {

	public static int sum(int [] array) {
		var length = array.length;
		var species = IntVector.SPECIES_PREFERRED;
		var loopBound = length - length % species.length();
		var vector = IntVector.zero(species);
		for(var i=0; i  < loopBound;i+= species.length()) {
			vector = vector.add(IntVector.fromArray(species, array, i));	
		}
		var sumVector = vector.reduceLanes(VectorOperators.ADD);
		for(var i=loopBound; i < length;i++) {
			sumVector+=array[i];
		}
		return sumVector;
	}
}
```
### 2. On souhaite écrire une méthode sumMask qui évite d'utiliser une post-loop et utilise un mask à la place.
 - Comment peut-on faire une addition de deux vecteurs avec un mask ?
 <br> ``vector.add(IntVector.fromArray(species,array,i,mask))``
 - Comment faire pour créer un mask qui allume les bits entre i la variable de boucle et length la longueur du tableau ?
 <br> ``var mask = species.indexInRange(i,array.length)``
```java

public class VectorComputation {
    ...
	public static int sumMask(int [] array) {
		var length = array.length;
		var species = IntVector.SPECIES_PREFERRED;
		var loopBound = species.loopBound(length);
		var vector =IntVector.zero(species);
		var i=0;
		for(;i <loopBound; i+= species.length()) {
			vector = vector.add(IntVector.fromArray(species, array, i));
		}
		var mask = species.indexInRange(i, length);
		vector = vector.add(IntVector.fromArray(species, array, i,mask));
		return vector.reduceLanes(VectorOperators.ADD);
	}
}
```

### 3. On souhaite maintenant écrire une méthode min qui calcule le minimum des valeurs d'un tableau en utilisant des vecteurs et une post-loop.<br>Contrairement à la somme qui a 0 comme élément nul, le minimum n'a pas d'élément nul... 
- Quelle doit être la valeur utilisée pour initialiser toutes les lanes du vecteur avant la boucle principale ?
<br> ``IntVector.broadcast(species,array[0])``
```java

public class VectorComputation {
    ...
		public static int min(int [] array) {
		var length = array.length;
		if(length < 1) {
			throw new IllegalArgumentException();
		}
		var species = IntVector.SPECIES_PREFERRED;
		var loopBound = species.loopBound(length);
		var min = array[0];
		var vector = IntVector.broadcast(species, array[0]);
		for(var i=0; i  < loopBound;i+= species.length()) {
			vector =vector.min(IntVector.fromArray(species, array, i));
		}
		min =vector.reduceLanes(VectorOperators.MIN);
		for(var i=loopBound; i < length;i++) {
			min = min < array[i]? min: array[i];
		}
		return min;
	}
}
```
### 4. On souhaite enfin écrire une méthode minMask qui au lieu d'utiliser une post-loop comme dans le code précédent, utilise un mask à la place.
Attention, le minimum n'a pas d’élément nul (non, toujours pas !), donc on ne peut pas laisser des zéros "traîner" dans les lanes lorsque l'on fait un minimum sur deux vecteurs.
Écrire le code de la méthode minMask et vérifier que le test nommé "testMinMask" passe.
```java

public class VectorComputation {
    ...
	public static int minMask(int [] array) {
		var length = array.length;
		if(length < 1) {
			throw new IllegalArgumentException();
		}
		var species = IntVector.SPECIES_PREFERRED;
		var loopBound = species.loopBound(length);
		var vector = IntVector.broadcast(species, array[0]);
		var i=0;
		for(; i  < loopBound;i+= species.length()) {
			vector =vector.min(IntVector.fromArray(species, array, i));
		}
		var mask = species.indexInRange(i, length);
		var vector2 = IntVector.fromArray(species, array, i,mask);
		vector = vector.lanewise(VectorOperators.MIN,vector2,mask);
		return vector.reduceLanes(VectorOperators.MIN);
	}
}
```