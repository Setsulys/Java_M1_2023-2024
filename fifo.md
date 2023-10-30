# LY STEVEN TP5

## Exercice 2 - Fifo

### 1. On souhaite écrire une classe Fifo générique (avec une variable de type E) dans le package fr.uge.fifo prenant en paramètre une capacité (un entier), le nombre d’éléments maximal que peut stocker la structure de données.<br>On souhaite de plus, écrire la méthode offer qui permet d'ajouter des éléments à la fin (tail) du tableau circulaire sachant que pour l'instant on ne se préoccupera pas du cas où le tableau est plein. Et une méthode size qui renvoie le nombre d'éléments ajoutés.
```java
public class Fifo<E> {

	private static final int SIZEMAX;
	private int head;
	private int tail;
	private final E[] array;
	private int size;
	
	@SuppressWarnings("unchecked")
	public Fifo(int maxElement) {
		if(maxElement <= 0) {
			throw new IllegalArgumentException();
		}
		head=0;
		tail=0;
		size=0;
		array = (E[]) new Object[SIZEMAX];
	}

    public void offer(E value) {
		Objects.requireNonNull(value);
		array[tail]=value;
		size++;
		tail = (tail+1)%array.length;	
	}
	
	public int size() {
		return size;
	}
	
}
```
### 2. Avez-vous pensé aux préconditions ?

oui

### 3. On souhaite écrire une méthode poll qui retire un élément du tableau circulaire et une méthode peek qui récupère cet élement sans le retirer. Que faire si la file est vide ?
```java
public class Fifo<E> {
    ...
	public E poll(){
		if(head==array.length) {
			throw new IllegalStateException();
		}
		E element = array[head];
		head = (head+1)%array.length;
		size--;
		return element;
	}
	
	public int size() {
		return size;
	}


	public E peek() {
		if (size()<=0) {
			return null;
		}
		return array[head];
	}
}
```
Si la file est vide on renvoi null

### 4. Rappelez ce qu'est un memory leak en Java et assurez-vous que votre implantation n'a pas ce comportement indésirable.
```java

public class Fifo<E> {
    ...
	public E poll(){
		if(head==array.length) {
			throw new IllegalStateException();
		}
		E element = array[head];
		array[head]=null;
		head = (head+1)%array.length;
		size--;
		return element;
	}
}
```
### 5. On souhaite agrandir le tableau circulaire dynamiquement en doublant sa taille quand le tableau est plein. Attention, il faut penser au cas où le début de la liste (head) a un indice qui est supérieur à l'indice indiquant la fin de la file (tail).
De plus, on va ajouter un nouveau constructeur sans paramètre qui, par défaut, crée un tableau circulaire de taille 16.
```java
public class Fifo<E> {
	private E[] array;
	...
	public Fifo() {
		this(16);
	}
	
	public void offer(E value) {
		Objects.requireNonNull(value);
		if(size()==array.length) {
			resize();
		}
		array[tail]=value;
		size++;
		tail = (tail+1)%array.length;
		
	}
	...
	@SuppressWarnings("unchecked")
	public void resize() {
		E[] array2 = array;
		array = (E[]) new Object[array.length << 1];
		if(head ==0) {
			array = Arrays.copyOf(array2, array.length<<1);
			tail = size();
			head=0;
			return;
		}
		System.arraycopy(array2, head, array, 0, array2.length-head);
		System.arraycopy(array2, 0, array, array2.length-head, tail);
		tail =size();
		head=0;
	}
}
```

### 6. On souhaite ajouter une méthode d'affichage qui affiche les éléments dans l'ordre dans lequel ils seraient sortis en utilisant poll. L'ensemble des éléments devra être affiché entre crochets ('[' et ']') avec les éléments séparés par des virgules (suivies d'un espace).
```java
public class Fifo<E> {
	...
	@Override
	public String toString() {
		var stringJoiner= new StringJoiner(", ","[","]");
		var j= head;
		for(var i= 0; i< size();i++) {
			stringJoiner.add(array[j].toString());
			j=(j+1)%array.length;
		}
		return stringJoiner.toString();
	}
}

```
### 7. En fait, le code que vous avez écrit est peut-être faux (oui, les testent passent, et alors ?)... Le calcul sur les entiers n'est pas sûr/sécurisé en Java (ou en C, car Java à copié le modèle de calcul du C). En effet, une opération '+' sur deux nombres suffisamment grand devient négatif.
Le test fonctionne

### 8. Rappelez quel est le principe d'un itérateur.<br>Quel doit être le type de retour de la méthode iterator() ? Implanter la méthode iterator()
```java
public class Fifo<E> {
	...
	public Iterator<E> iterator() {
		return new Iterator<E>(){
			private int cursor = head;
			private int selfsize;
			
			@Override
			public boolean hasNext() {
				return selfsize < size();
			}

			@Override
			public E next() {
				if(!hasNext()) {
					throw new NoSuchElementException();
				}
				var element = array[cursor];
				cursor = (cursor+1)%array.length;
				selfsize++;
				return element;
			}
			
		};
	}
}
```
un iterateur permet aux fonctions de pouvoir parcourir le tableau de la classe, le type de retour de la méthode ``iterator()`` doit etre un ``Iterator<E>``

### 9. On souhaite que le tableau circulaire soit parcourable en utilisant une boucle for-each-in, comme ceci :
>```java 
>	var fifo = ...
>    for(var value: fifo) {
>      ...
>    }
>```
### Quelle interface doit implanter la classe Fifo ?
```java
public class Fifo<E> implements Iterable<E>{
	...
}
```
### 10. Enfin, il existe déjà en Java une interface pour les files d'éléments, java.util.Queue, on souhaite maintenant que notre implantation de tableau circulaire Fifo implante cette interface.<br>Pour nous aider, il existe une classe abstraite java.util.AbstractQueue, qui implante déjà un certain nombre de méthodes de l'interface Queue, il ne vous reste plus qu'à implanter les méthodes manquantes.
```java
public class Fifo<E> extends AbstractQueue<E> implements Iterable<E>{
	...
	public void clear() {
		var s = size();
		for(var i =0; i < s;i++) {
			array[i]=null;
			size--;
		}
		head = 0;
		tail = 0;
	}
}
```