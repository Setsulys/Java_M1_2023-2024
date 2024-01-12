package unionset;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.AccessFlag;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.IntStream;

import static java.util.Collections.emptyIterator;
import static java.util.Spliterators.emptySpliterator;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class UnionSetTest {

  @Nested
  public class Q1 {
    @Test
    public void unionSetContainsTheValuesOfBothSets() {
      UnionSet<String> unionSet = UnionSet.of(Set.of("foo"), Set.of("bar"));
      assertAll(
          () -> assertTrue(unionSet.contains("foo")),
          () -> assertTrue(unionSet.contains("bar"))
      );
    }

    @Test
    public void unionSetContainsTheValuesOfBothSets2() {
      UnionSet<Integer> unionSet = UnionSet.of(Set.of(1, 2, 3), Set.of(4, 5));
      assertAll(
          () -> assertTrue(unionSet.contains(1)),
          () -> assertTrue(unionSet.contains(2)),
          () -> assertTrue(unionSet.contains(3)),
          () -> assertTrue(unionSet.contains(4)),
          () -> assertTrue(unionSet.contains(5))
      );
    }

    @Test
    public void unionSetIsImmutable() {
      var hashSet = new HashSet<String>();
      hashSet.add("foo");
      var unionSet = UnionSet.of(hashSet, Set.of("bar"));
      hashSet.remove("foo");
      assertAll(
          () -> assertTrue(unionSet.contains("foo")),
          () -> assertTrue(unionSet.contains("bar"))
      );
    }

    @Test
    public void unionSetIsImmutable2() {
      var hashSet = new HashSet<String>();
      hashSet.add("bar");
      var unionSet = UnionSet.of(Set.of("foo"), hashSet);
      hashSet.remove("bar");
      assertAll(
          () -> assertTrue(unionSet.contains("foo")),
          () -> assertTrue(unionSet.contains("bar"))
      );
    }

    @Test
    public void unionSetHasNoPublicConstructor() {
      assertEquals(0, UnionSet.class.getConstructors().length);
    }

    @Test
    public void qualityOfImplementation() {
      assertAll(
          () -> assertTrue(UnionSet.class.accessFlags().contains(AccessFlag.PUBLIC)),
          () -> assertTrue(UnionSet.class.accessFlags().contains(AccessFlag.FINAL)),
          () -> assertTrue(Set.class.isAssignableFrom(UnionSet.class.getDeclaredField("set1").getType())),
          () -> assertTrue(Set.class.isAssignableFrom(UnionSet.class.getDeclaredField("set2").getType())),
          () -> assertTrue(Arrays.stream(UnionSet.class.getDeclaredFields())
              .allMatch(field -> field.accessFlags().contains(AccessFlag.PRIVATE)))
      );
    }

    @Test
    public void unionSetDoesNotComputeThenUnion() {
      assertAll(
          () -> assertTrue(UnionSet.class.accessFlags().contains(AccessFlag.PUBLIC)),
          () -> assertTrue(UnionSet.class.accessFlags().contains(AccessFlag.FINAL))
      );
    }

    @Test
    public void unionSetAlsoContainsTheIntersection() {
      var unionSet = UnionSet.of(Set.of(1, 2), Set.of(1, 4));
      assertTrue(unionSet.contains(1));
    }

    @Test
    public void unionSetAllowToMixSetOfDifferentType() {
      var unionSet = UnionSet.of(Set.of(1, 2), Set.of("foo", "bar"));
      assertAll(
          () -> assertTrue(unionSet.contains(1)),
          () -> assertTrue(unionSet.contains(1)),
          () -> assertTrue(unionSet.contains(2)),
          () -> assertTrue(unionSet.contains("foo")),
          () -> assertTrue(unionSet.contains("bar"))
      );
    }

    @Test
    public void unionSetOfIsCorrectlyTyped() {
      UnionSet<Comparable<?>> unionSet = UnionSet.of(Set.of(1, 2), Set.of("foo", "bar"));
      assertNotNull(unionSet);
    }

    @Test
    public void unionSetContainsIsCorrectlyTyped() {
      var unionSet = UnionSet.of(Set.of("foo"), Set.of("bar", "baz"));
      assertFalse(unionSet.contains(42));
    }

    @Test
    public void unionSetPreconditions() {
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> UnionSet.of(null, Set.of())),
          () -> assertThrows(NullPointerException.class, () -> UnionSet.of(Set.of(), null)),
          () -> assertThrows(NullPointerException.class, () -> UnionSet.of(Set.of(), new HashSet<>() {{ add(null); }})),
          () -> assertThrows(NullPointerException.class, () -> UnionSet.of(new HashSet<>() {{ add(null); }}, Set.of()))
      );
    }

    @Test
    public void unionSetContainsPrecondition() {
      var unionSet = UnionSet.of(Set.of(), Set.of());
      assertThrows(NullPointerException.class, () -> unionSet.contains(null));
    }
  }


  @Nested
  public class Q2 {
    @Test
    public void unionSetPrintBothValues() {
      var unionSet = UnionSet.of(Set.of("foo"), Set.of("bar"));
      assertEquals("[foo, bar]", "" + unionSet);
    }

    @Test
    public void unionSetDoesNotPrintTheSameValueTwice() {
      var unionSet = UnionSet.of(Set.of(42), Set.of(42));
      assertEquals("[42]", "" + unionSet);
    }

    @Test
    public void unionSetDoesNotPrintTheSameValueTwice2() {
      var unionSet = UnionSet.of(Set.of(42), Set.of(3, 42));
      assertEquals("[42, 3]", "" + unionSet);
    }

    @Test
    public void unionSetDoesNotPrintTheSameValueTwice3() {
      var unionSet = UnionSet.of(Set.of(42), Set.of(42, 3));
      assertEquals("[42, 3]", "" + unionSet);
    }

    @Test
    public void unionSetPrintWorksWithEmptySets() {
      assertAll(
          () -> assertEquals("[]", "" + UnionSet.of(Set.of(), Set.of())),
          () -> assertEquals("[foo]", "" + UnionSet.of(Set.of("foo"), Set.of())),
          () -> assertEquals("[bar]", "" + UnionSet.of(Set.of(), Set.of("bar")))
      );
    }

    @Test
    public void unionSetPrintTheIntersectionOnlyOnce() {
      var unionSet = UnionSet.of(Set.of(1, 2), Set.of(1, 3));
      var text = "" + unionSet;
      assertTrue("[1, 2, 3]".equals(text) || "[2, 1, 3]".equals(text));
    }
  }


  @Nested
  public class Q3 {
    @Test
    public void concatIteratorOfIntegers() {
      var concatIterator = UnionSet.concat(List.of(1, 2).iterator(), List.of(3, 4).iterator());
      assertEquals(1, concatIterator.next());
      assertEquals(2, concatIterator.next());
      assertEquals(3, concatIterator.next());
      assertEquals(4, concatIterator.next());
      assertThrows(NoSuchElementException.class, concatIterator::next);
    }

    @Test
    public void concatIteratorOfString() {
      var concatIterator = UnionSet.concat(List.of("foo").iterator(), List.of("bar", "baz").iterator());
      assertTrue(concatIterator.hasNext());
      assertEquals("foo", concatIterator.next());
      assertTrue(concatIterator.hasNext());
      assertEquals("bar", concatIterator.next());
      assertTrue(concatIterator.hasNext());
      assertEquals("baz", concatIterator.next());
      assertFalse(concatIterator.hasNext());
    }

    @Test
    public void concatIteratorWorksWithAnEmptyIterator() {
      var concatIterator = UnionSet.concat(emptyIterator(), List.of("foo", "bar").iterator());
      assertEquals("foo", concatIterator.next());
      assertEquals("bar", concatIterator.next());
      assertThrows(NoSuchElementException.class, concatIterator::next);
    }

    @Test
    public void concatIteratorWorksWithAnEmptyIterator2() {
      var concatIterator = UnionSet.concat(List.of(3, 14).iterator(), emptyIterator());
      assertEquals(3, concatIterator.next());
      assertEquals(14, concatIterator.next());
      assertThrows(NoSuchElementException.class, concatIterator::next);
    }

    @Test
    public void concatALotOfIntegers() {
      var concatIterator = UnionSet.concat(
          IntStream.range(0, 1_000_000).iterator(),
          IntStream.range(1_000_000, 2_000_000).iterator());
      var list = new ArrayList<Integer>();
      concatIterator.forEachRemaining(list::add);
      assertEquals(IntStream.range(0, 2_000_000).boxed().toList(), list);
    }

    @Test
    public void concatRemove() {
      var concatIterator = UnionSet.concat(List.of(1, 2).iterator(), List.of(3).iterator());
      assertEquals(1, concatIterator.next());
      assertThrows(UnsupportedOperationException.class, concatIterator::remove);
    }

    @Test
    public void concatIteratorIsLazy() {
      var list = IntStream.range(0, 1_000_000).mapToObj(i -> "" + i).toList();
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
        for(var i = 0; i < 1_000_000; i++) {
          var filterOutIterator = UnionSet.concat(list.iterator(), list.iterator());
          assertEquals("0", filterOutIterator.next());
        }
      });
    }

    @Test
    public void concatIteratorIsCorrectlyTyped() {
      Iterator<Serializable> concatIterator = UnionSet.concat(
          List.of("foo").iterator(),
          List.of(42).iterator());
      assertNotNull(concatIterator);
    }

    @Test
    public void concatIteratorPreconditions() {
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> UnionSet.concat(null, emptyIterator())),
          () -> assertThrows(NullPointerException.class, () -> UnionSet.concat(emptyIterator(), null))
      );
    }
  }


  @Nested
  public class Q4 {
    @Test
    public void filterOutIteratorOfStrings() {
      var filterOutIterator = UnionSet.filterOut(List.of(" ", "foo", " ").iterator(), String::isBlank);
      assertEquals("foo", filterOutIterator.next());
      assertThrows(NoSuchElementException.class, filterOutIterator::next);
    }

    @Test
    public void filterOutIteratorOfStrings2() {
      var filterOutIterator = UnionSet.filterOut(List.of("", "foo", "bar", "", "baz").iterator(), String::isEmpty);
      assertTrue(filterOutIterator.hasNext());
      assertEquals("foo", filterOutIterator.next());
      assertTrue(filterOutIterator.hasNext());
      assertEquals("bar", filterOutIterator.next());
      assertTrue(filterOutIterator.hasNext());
      assertEquals("baz", filterOutIterator.next());
      assertFalse(filterOutIterator.hasNext());
      assertThrows(NoSuchElementException.class, filterOutIterator::next);
    }

    @Test
    public void filterOutIteratorOfStrings3() {
      var filterOutIterator = UnionSet.filterOut(List.of("", "foo", "bar", "", "baz").iterator(), String::isEmpty);
      assertTrue(filterOutIterator.hasNext());
      assertTrue(filterOutIterator.hasNext());
      assertEquals("foo", filterOutIterator.next());
      assertTrue(filterOutIterator.hasNext());
      assertTrue(filterOutIterator.hasNext());
      assertEquals("bar", filterOutIterator.next());
      assertTrue(filterOutIterator.hasNext());
      assertTrue(filterOutIterator.hasNext());
      assertEquals("baz", filterOutIterator.next());
      assertFalse(filterOutIterator.hasNext());
      assertFalse(filterOutIterator.hasNext());
      assertThrows(NoSuchElementException.class, filterOutIterator::next);
      assertFalse(filterOutIterator.hasNext());
    }

    @Test
    public void filterOutIteratorOfPersons() {
      record Person(String name) {
        boolean isNameEmpty() {
          return name.isEmpty();
        }
      }

      var filterOutIterator = UnionSet.filterOut(List.of(new Person("Bob"), new Person("")).iterator(), Person::isNameEmpty);
      assertEquals(new Person("Bob"), filterOutIterator.next());
      assertFalse(filterOutIterator.hasNext());
    }

    @Test
    public void filterOutRemove() {
      var filterOutIterator = UnionSet.filterOut(List.of("", "1").iterator(), String::isEmpty);
      assertEquals("1", filterOutIterator.next());
      assertThrows(UnsupportedOperationException.class, filterOutIterator::remove);
    }

    @Test
    public void filterOutIteratorIsLazy() {
      var list = IntStream.range(0, 1_000_000).mapToObj(i -> "" + i).toList();
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
        for(var i = 0; i < 1_000_000; i++) {
          var filterOutIterator = UnionSet.filterOut(list.iterator(), String::isEmpty);
          assertEquals("0", filterOutIterator.next());
        }
      });
    }

    @Test
    public void filterOutIteratorWithAnEmptyIteratorDoesNotCallTheFunction() {
      var list = new ArrayList<>();
      Iterator<Object> filterOutIterator = UnionSet.filterOut(emptyIterator(), list::add);
      assertNotNull(filterOutIterator);
      assertEquals(List.of(), list);
    }

    @Test
    public void filterOutIteratorCorrectlyTyped() {
      Iterator<CharSequence> filterOutIterator = UnionSet.filterOut(List.of("foo", "bar").iterator(), Thread::holdsLock);
      assertEquals("foo", filterOutIterator.next());
      assertEquals("bar", filterOutIterator.next());
      assertFalse(filterOutIterator.hasNext());
    }
  }


  @Nested
  public class Q5 {
    @Test
    public void forLoopOfIntegers() {
      var unionSet = UnionSet.of(Set.of(1), Set.of(2, 1));
      var list = new ArrayList<Integer>();
      for(var element: unionSet) {
        list.add(element);
      }
      assertEquals(List.of(1, 2), list);
    }

    @Test
    public void forLoopOfStrings() {
      var unionSet = UnionSet.of(Set.of("foo", "bar", "baz"), Set.of("bar", "foo", "whizz"));
      var set = new HashSet<String>();
      for(var element: unionSet) {
        assertTrue(set.add(element));
      }
      assertEquals(Set.of("foo", "bar", "baz", "whizz"), set);
    }

    @Test
    public void forLoopOneElement() {
      var unionSet = UnionSet.of(Set.of(42), Set.of(42));
      var counter = 0;
      for(var element: unionSet) {
        assertEquals(42, element);
        counter++;
      }
      assertEquals(1, counter);
    }

    @Test
    public void forLoopEmpty() {
      var unionSet = UnionSet.of(Set.of(), Set.of());
      for(var element: unionSet) {
        fail();
      }
    }

    @Test
    public void forLoopWithALotOfDuplicates() {
      var unionSet = UnionSet.of(
          IntStream.range(0, 1_000_000).boxed().collect(toSet()),
          IntStream.range(0, 1_000_000).boxed().collect(toSet())
      );
      var set = new HashSet<Integer>();
      for(var element: unionSet) {
        assertTrue(set.add(element));
      }
      assertEquals(IntStream.range(0, 1_000_000).boxed().collect(toSet()), set);
    }
  }

  @Nested
  public class Q6 {
    @Test
    public void sizeDoesNotCountTheDuplicates() {
      var unionSet = UnionSet.of(Set.of(1, 2, 3), Set.of(1, 4));
      assertEquals(4, unionSet.size());
    }

    @Test
    public void sizeWorksWithNoDuplicate() {
      var unionSet = UnionSet.of(Set.of(1, 2), Set.of(3, 4));
      assertEquals(4, unionSet.size());
    }

    @Test
    public void sizeOfAnEmptyUnionSet() {
      var unionSet = UnionSet.of(Set.of(), Set.of());
      assertEquals(0, unionSet.size());
    }

    @Test
    public void sizeIsOnlyComputedOnce() {
      var unionSet = UnionSet.of(
          IntStream.range(0, 1_000_000).boxed().collect(toSet()),
          IntStream.range(0, 1_000_000).boxed().collect(toSet())
      );
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
        for (var i = 0; i < 1_000_000; i++) {
          assertEquals(1_000_000, unionSet.size());
        }
      });
    }

    @Test
    public void unionSetIsARealSetOfIntegers() {
      Set<Integer> unionSet = UnionSet.of(Set.of(1, 2, 3), Set.of(3, 4));
      assertEquals(Set.of(1, 2, 3, 4), unionSet);
    }

    @Test
    public void unionSetIsARealSetString() {
      Set<String> unionSet = UnionSet.of(Set.of("foo", "bar"), Set.of("foo", "bar", "baz", "whizz"));
      assertEquals(Set.of("foo", "bar", "baz", "whizz"), unionSet);
    }

    @Test
    public void unionSetIsARealSetWithNoDuplicate() {
      var unionSet = UnionSet.of(Set.of(1, 2, 3), Set.of(4, 5));
      assertEquals(Set.of(1, 2, 3, 4, 5), unionSet);
    }

    @Test
    public void unionSetEqualsAlsoWorks() {
      var unionSet = UnionSet.of(Set.of(3, 14, 15), Set.of(4, 15));
      assertTrue(unionSet.equals(Set.of(3, 14, 15, 4)));
    }

    @Test
    public void unionSetCanHaveALotOfIntegers() {
      var unionSet = UnionSet.of(
          IntStream.range(0, 1_000_000).boxed().collect(toSet()),
          IntStream.range(0, 1_000_000).boxed().collect(toSet())
      );
      assertEquals(IntStream.range(0, 1_000_000).boxed().collect(toSet()), unionSet);
    }
  }


  @Nested
  public class Q7 {
    @Test
    public void concatFilterOutSpliteratorCanJustConcatenate() {
      var spliterator1 = List.of("foo", "bar").spliterator();
      var spliterator2 = List.of("baz", "whizz").spliterator();
      var concatFilterOutSpliterator = UnionSet.concatFilterOutSpliterator(spliterator1, spliterator2, String::isEmpty);
      var list = new ArrayList<String>();
      concatFilterOutSpliterator.forEachRemaining(list::add);
      assertEquals(List.of("foo", "bar", "baz", "whizz"), list);
    }

    @Test
    public void concatFilterOutSpliteratorCanJustConcatenateWithPersons() {
      record Person(String name) {
        boolean isNameEmpty() {
          return Person.this.name.isEmpty();
        }
      }
      var spliterator1 = List.of(new Person("Bob"), new Person("Ana")).spliterator();
      var spliterator2 = List.of(new Person("John")).spliterator();
      var concatFilterOutSpliterator = UnionSet.concatFilterOutSpliterator(spliterator1, spliterator2, Person::isNameEmpty);
      var list = new ArrayList<Person>();
      concatFilterOutSpliterator.forEachRemaining(list::add);
      assertEquals(List.of(new Person("Bob"), new Person("Ana"), new Person("John")), list);
    }

    @Test
    public void concatFilterOutSpliteratorDoNotShowEmptyStringsOfTheSecondSpliterator() {
      var spliterator1 = List.of("foo", "bar").spliterator();
      var spliterator2 = List.of("", "baz", "").spliterator();
      var concatFilterOutSpliterator = UnionSet.concatFilterOutSpliterator(spliterator1, spliterator2, String::isEmpty);
      var list = new ArrayList<String>();
      concatFilterOutSpliterator.forEachRemaining(list::add);
      assertEquals(List.of("foo", "bar", "baz"), list);
    }

    @Test
    public void concatFilterOutSpliteratorDoNotShowEmptyPersonsOfTheSecondSpliterator() {
      record Person(String name) {
        boolean isNameEmpty() {
          return Person.this.name.isEmpty();
        }
      }
      var spliterator1 = List.of(new Person("Bob"), new Person("Ana")).spliterator();
      var spliterator2 = List.of(new Person(""), new Person("John")).spliterator();
      var concatFilterOutSpliterator = UnionSet.concatFilterOutSpliterator(spliterator1, spliterator2, Person::isNameEmpty);
      var list = new ArrayList<Person>();
      concatFilterOutSpliterator.forEachRemaining(list::add);
      assertEquals(List.of(new Person("Bob"), new Person("Ana"), new Person("John")), list);
    }

    @Test
    public void concatFilterOutSpliteratorDoNotShowEmptyStringsOfTheSecondSpliterator2() {
      var spliterator1 = List.of("", "foo", "").spliterator();
      var spliterator2 = List.of("", "bar").spliterator();
      var concatFilterOutSpliterator = UnionSet.concatFilterOutSpliterator(spliterator1, spliterator2, String::isEmpty);
      var list = new ArrayList<String>();
      concatFilterOutSpliterator.forEachRemaining(list::add);
      assertEquals(List.of("", "foo", "", "bar"), list);
    }

    @Test
    public void concatFilterOutSpliteratorWorksWithEmptySpliterator() {
      var spliterator = List.of("foo", "", "bar").spliterator();
      var concatFilterOutSpliterator = UnionSet.concatFilterOutSpliterator(emptySpliterator(), spliterator, String::isEmpty);
      var list = new ArrayList<String>();
      concatFilterOutSpliterator.forEachRemaining(list::add);
      assertEquals(List.of("foo", "bar"), list);
    }

    @Test
    public void concatFilterOutSpliteratorWorksWithEmptySpliterator2() {
      var concatFilterOutSpliterator = UnionSet.concatFilterOutSpliterator(emptySpliterator(), emptySpliterator(), String::isEmpty);
      var list = new ArrayList<String>();
      concatFilterOutSpliterator.forEachRemaining(list::add);
      assertEquals(List.of(), list);
    }

    @Test
    public void concatFilterOutSpliteratorIsLazy() {
      var list = IntStream.range(0, 1_000_000).mapToObj(i -> "" + i).toList();
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
        for(var i = 0; i < 1_000_000; i++) {
          var concatFilterOutSpliterator = UnionSet.concatFilterOutSpliterator(list.spliterator(), list.spliterator(), String::isEmpty);
          assertTrue(concatFilterOutSpliterator.tryAdvance(value -> assertEquals("0", value)));
        }
      });
    }

    @Test
    public void concatFilterOutSpliteratorOfListCharacteristics() {
      var list1 = List.of("1", "2");
      var list2 = List.of("2", "3");
      var concatFilterOutSpliterator = UnionSet.concatFilterOutSpliterator(list1.spliterator(), list2.spliterator(), String::isEmpty);
      assertTrue(concatFilterOutSpliterator.hasCharacteristics(Spliterator.ORDERED));
    }

    @Test
    public void concatFilterOutSpliteratorOfArraysAsListCharacteristics() {
      var list1 = Arrays.asList(null, "1");
      var list2 = Arrays.asList("1", null, "2");
      var concatFilterOutSpliterator = UnionSet.concatFilterOutSpliterator(list1.spliterator(), list2.spliterator(), Objects::nonNull);
      assertTrue(concatFilterOutSpliterator.hasCharacteristics(Spliterator.ORDERED));
      assertFalse(concatFilterOutSpliterator.hasCharacteristics(Spliterator.NONNULL));
    }

    @Test
    public void concatFilterOutSpliteratorOfDequeCharacteristics() {
      var queue1 = new ArrayDeque<>(List.of("1", "2"));
      var queue2 = new ArrayDeque<>(List.of("2", "3"));
      var concatFilterOutSpliterator = UnionSet.concatFilterOutSpliterator(queue1.spliterator(), queue2.spliterator(), String::isEmpty);
      assertTrue(concatFilterOutSpliterator.hasCharacteristics(Spliterator.ORDERED));
      assertTrue(concatFilterOutSpliterator.hasCharacteristics(Spliterator.NONNULL));
    }

    @Test
    public void concatFilterOutSpliteratorOfSetCharacteristics() {
      var set1 = Set.of("1", "2");
      var set2 = Set.of("2", "3");
      var concatFilterOutSpliterator = UnionSet.concatFilterOutSpliterator(set1.spliterator(), set2.spliterator(), String::isEmpty);
      assertFalse(concatFilterOutSpliterator.hasCharacteristics(Spliterator.ORDERED));
      assertFalse(concatFilterOutSpliterator.hasCharacteristics(Spliterator.DISTINCT));
    }

    @Test
    public void concatFilterOutSpliteratorPreconditions() {
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> UnionSet.concatFilterOutSpliterator(null, emptySpliterator(), String::isEmpty)),
          () -> assertThrows(NullPointerException.class, () -> UnionSet.concatFilterOutSpliterator(emptySpliterator(), null, String::isEmpty)),
          () -> assertThrows(NullPointerException.class, () -> UnionSet.concatFilterOutSpliterator(emptySpliterator(), emptySpliterator(), null))
          );
    }
  }


  @Nested
  public class Q8 {
    @Test
    public void streamOfStrings() {
      var unionSet = UnionSet.of(Set.of("foo", "bar"), Set.of("bar", "whizz"));
      assertEquals(Set.of("f", "b", "w"), unionSet.stream().map(s -> "" + s.charAt(0)).collect(toSet()));
    }

    @Test
    public void streamOfIntegers() {
      var unionSet = UnionSet.of(Set.of(1, 2, 3, 4), Set.of(0, 1, 4, 5));
      assertEquals(Set.of(0, 2, 4, 6, 8, 10), unionSet.stream().map(v -> v * 2).collect(toSet()));
    }

    @Test
    public void streamWithASplitNumberOfIntegers() {
      var unionSet = UnionSet.of(
          IntStream.range(0, 10).boxed().collect(toSet()),
          IntStream.range(10, 20).boxed().collect(toSet())
      );

      var set = unionSet.stream().collect(toSet());
      assertEquals(IntStream.range(0, 20).boxed().collect(toSet()), set);
    }

    @Test
    public void streamOfALotOfIntegers() {
      var unionSet = UnionSet.of(
          Set.of(),
          IntStream.range(0, 1_000_000).boxed().collect(toSet())
      );

      var set = unionSet.stream().collect(toSet());
      assertEquals(IntStream.range(0, 1_000_000).boxed().collect(toSet()), set);
    }

    @Test
    public void streamOfALotOfIntegers2() {
      var unionSet = UnionSet.of(
          IntStream.range(0, 1_000_000).boxed().collect(toSet()),
          Set.of()
      );

      var set = unionSet.stream().collect(toSet());
      assertEquals(IntStream.range(0, 1_000_000).boxed().collect(toSet()), set);
    }

    @Test
    public void streamKeepTheOrder() {
      var unionSet = UnionSet.of(Set.of("foo"), Set.of("foo", "bar"));
      assertEquals(List.of("foo", "bar"), unionSet.stream().toList());
    }

    @Test
    public void streamCountWorks() {
      var unionSet = UnionSet.of(Set.of("foo", "bar", "baz"), Set.of("foo", "whizz"));
      assertEquals(3, unionSet.stream().skip(1).count());
    }

    @Test
    public void streamPeekIsCalled() {
      var unionSet = UnionSet.of(Set.of(1, 2, 3), Set.of(1, 2));
      var object = new Object() { boolean flag; };
      assertEquals(3, unionSet.stream()
          .peek(__ -> object.flag = true)
          .count());
      assertTrue(object.flag);
    }

    // Yes, there is an extra difficulty here! You have to think a little more :)
    @Test
    public void streamCharacteristicsOfTheUnionSetIsALeastDistinct() {
      var unionSet = UnionSet.of(Set.of("Mickael", "Ana"), Set.of("Bob", "Mickael"));
      assertTrue(unionSet.stream().spliterator().hasCharacteristics(Spliterator.DISTINCT));
    }
  }


  @Nested
  public class Q9 {
    @Test
    public void parallelStreamOfIntegers() {
      var unionSet = UnionSet.of(
          IntStream.range(0, 10).boxed().collect(toSet()),
          IntStream.range(0, 20).boxed().collect(toSet())
      );

      var set = unionSet.stream()
          .parallel()
          .collect(toSet());
      assertEquals(IntStream.range(0, 20).boxed().collect(toSet()), set);
    }

    @Test
    public void parallelStreamWithASplitNumberOfIntegers() {
      var unionSet = UnionSet.of(
          IntStream.range(0, 10).boxed().collect(toSet()),
          IntStream.range(10, 20).boxed().collect(toSet())
      );

      var set = unionSet.stream()
          .parallel()
          .collect(toSet());
      assertEquals(IntStream.range(0, 20).boxed().collect(toSet()), set);
    }

    @Test
    public void parallelStreamOfALotOfIntegers() {
      var unionSet = UnionSet.of(
          Set.of(),
          IntStream.range(0, 1_000_000).boxed().collect(toSet())
      );

      var set = unionSet.stream()
          .parallel()
          .collect(toSet());
      assertEquals(IntStream.range(0, 1_000_000).boxed().collect(toSet()), set);
    }

    @Test
    public void parallelStreamOfALotOfIntegers2() {
      var unionSet = UnionSet.of(
          IntStream.range(0, 1_000_000).boxed().collect(toSet()),
          Set.of()
      );

      var set = unionSet.stream()
          .parallel()
          .collect(toSet());
      assertEquals(IntStream.range(0, 1_000_000).boxed().collect(toSet()), set);
    }

    @Test
    public void parallelStreamOfIntegersUsesSeveralThreads() {
      var unionSet = UnionSet.of(
          IntStream.range(0, 1_000_000).boxed().collect(toSet()),
          IntStream.range(0, 1_000_000).boxed().collect(toSet())
      );
      var threadSet = new HashSet<Thread>();
      var set = unionSet.stream()
          .parallel()
          .peek(__ -> threadSet.add(Thread.currentThread()))
          .collect(toSet());
      assertEquals(IntStream.range(0, 1_000_000).boxed().collect(toSet()), set);
      assertTrue(threadSet.size() > 1);
    }

    @Test
    public void parallelStreamIsOnlyParallelWhenAsked() {
      var unionSet = UnionSet.of(Set.of("foo", "bar"), Set.of("baz", "whizz"));
      assertAll(
          () -> assertFalse(unionSet.stream().isParallel()),
          () -> assertTrue(unionSet.stream().parallel().isParallel())
      );
    }
  }
}