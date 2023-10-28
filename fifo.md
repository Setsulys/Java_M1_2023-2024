# LY STEVEN TP5

## Exercice 2 - Fifo

### 1. On souhaite écrire une classe Fifo générique (avec une variable de type E) dans le package fr.uge.fifo prenant en paramètre une capacité (un entier), le nombre d’éléments maximal que peut stocker la structure de données.<br>On souhaite de plus, écrire la méthode offer qui permet d'ajouter des éléments à la fin (tail) du tableau circulaire sachant que pour l'instant on ne se préoccupera pas du cas où le tableau est plein. Et une méthode size qui renvoie le nombre d'éléments ajoutés.
```java
public class Fifo<E> {

	private static int SIZEMAX;
	private int head;
	private int tail;
	private E[] array;
	private int size;
	
	@SuppressWarnings("unchecked")
	public Fifo(int maxElement) {
		if(maxElement <= 0) {
			throw new IllegalArgumentException();
		}
		SIZEMAX = maxElement;
		head=0;
		tail=0;
		size=0;
		array = (E[]) new Object[SIZEMAX];
	}

	
	@SuppressWarnings("unchecked")
	public Fifo() {
		SIZEMAX=2;
		head=0;
		tail=0;
		size=0;
		array = (E[]) new Object[SIZEMAX];
	}

    public void offer(E value) {
		Objects.requireNonNull(value);
		array[tail]=value;
		size++;
		tail = (tail+1)%SIZEMAX;
		
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
		if(size()<=0) {
			return null;
		}
		E element = array[head];
		head = (head+1)%SIZEMAX;
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

### 5. On souhaite agrandir le tableau circulaire dynamiquement en doublant sa taille quand le tableau est plein. Attention, il faut penser au cas où le début de la liste (head) a un indice qui est supérieur à l'indice indiquant la fin de la file (tail).
De plus, on va ajouter un nouveau constructeur sans paramètre qui, par défaut, crée un tableau circulaire de taille 16.
``````````````````````````````````````````````````````