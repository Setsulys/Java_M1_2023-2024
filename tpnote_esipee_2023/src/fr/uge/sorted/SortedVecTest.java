package fr.uge.sorted;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class SortedVecTest {

  @Nested
  public class Q1 {
    @Test
    public void checkSortedStrings() {
      var strings = new String[] { "bar", "baz", "foo" };
      SortedVec.checkSortedStrings(strings);
    }

    @Test
    public void checkSortedStringsEmpty() {
      SortedVec.checkSortedStrings(new String[0]);
    }

    @Test
    public void checkSortedStringsOneElement() {
      var strings = new String[] { "hello" };
      SortedVec.checkSortedStrings(strings);
    }

    @Test
    public void checkSortedStringsDuplicateElements() {
      var strings = new String[] { "Albus", "Bar", "Bar", "Foo" };
      SortedVec.checkSortedStrings(strings);
    }

    @Test
    public void checkSortedStringsNotSorted() {
      var strings = new String[] { "Potter", "Harry" };
      assertThrows(IllegalArgumentException.class, () -> SortedVec.checkSortedStrings(strings));
    }

    @Test
    public void checkSortedStringsNotSorted2() {
      var strings = new String[] { "a", "b", "c", "a" };
      assertThrows(IllegalArgumentException.class, () -> SortedVec.checkSortedStrings(strings));
    }

    @Test
    public void checkSortedStringsIsAnHelperMethod() {
      var checkSortedStrings = Arrays.stream(SortedVec.class.getDeclaredMethods())
          .filter(m -> m.getName().equals("checkSortedStrings"))
          .findFirst()
          .orElseThrow();
      assertFalse(checkSortedStrings.accessFlags().contains(AccessFlag.PUBLIC));
    }

    @Test
    public void checkSortedStringsFastEnough() {
      var strings = IntStream.range(0, 100_000)
          .mapToObj(i -> String.format("%05d", i))
          .toArray(String[]::new);
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> SortedVec.checkSortedStrings(strings));
    }
  }


  @Nested
  public class Q2 {
    @Test
    public void ofSortedStrings() {
      List<String> list = List.of("bar", "baz", "foo");
      SortedVec<String> sortedVec = SortedVec.ofSortedStrings(list);
      assertAll(
          () -> assertEquals(3, sortedVec.size()),
          () -> assertEquals("bar", sortedVec.get(0)),
          () -> assertEquals("baz", sortedVec.get(1)),
          () -> assertEquals("foo", sortedVec.get(2))
      );
    }

    @Test
    public void ofSortedStringsEmpty() {
      var sortedVec = SortedVec.ofSortedStrings(List.of());
      assertEquals(0, sortedVec.size());
    }

    @Test
    public void ofSortedStringsOneElement() {
      var sortedVec = SortedVec.ofSortedStrings(List.of(""));
      assertAll(
          () -> assertEquals(1, sortedVec.size()),
          () -> assertEquals("", sortedVec.get(0))
      );
    }

    @Test
    public void ofSortedStringsMutableAdd() {
      var list = new ArrayList<>(List.of("a", "b", "c", "d"));
      var sortedVec = SortedVec.ofSortedStrings(list);
      list.add("wtf");
      assertAll(
          () -> assertEquals(4, sortedVec.size()),
          () -> assertEquals("a", sortedVec.get(0)),
          () -> assertEquals("b", sortedVec.get(1)),
          () -> assertEquals("c", sortedVec.get(2)),
          () -> assertEquals("d", sortedVec.get(3))
      );
    }

    @Test
    public void ofSortedStringsMutableSet() {
      var list = new ArrayList<>(List.of("a", "b", "c", "d"));
      var sortedVec = SortedVec.ofSortedStrings(list);
      list.set(0, "wtf");
      assertAll(
          () -> assertEquals(4, sortedVec.size()),
          () -> assertEquals("a", sortedVec.get(0)),
          () -> assertEquals("b", sortedVec.get(1)),
          () -> assertEquals("c", sortedVec.get(2)),
          () -> assertEquals("d", sortedVec.get(3))
      );
    }

    @Test
    public void ofSortedStringsMutableAddFirst() {
      var list = new ArrayList<>(List.of("a", "b", "c", "d"));
      var sortedVec = SortedVec.ofSortedStrings(list);
      list.addFirst("wtf");
      assertAll(
          () -> assertEquals(4, sortedVec.size()),
          () -> assertEquals("a", sortedVec.get(0)),
          () -> assertEquals("b", sortedVec.get(1)),
          () -> assertEquals("c", sortedVec.get(2)),
          () -> assertEquals("d", sortedVec.get(3))
      );
    }

    @Test
    public void ofSortedStringsMutableRemoveLast() {
      var list = new ArrayList<>(List.of("a", "b", "c", "d"));
      var sortedVec = SortedVec.ofSortedStrings(list);
      list.removeLast();
      assertAll(
          () -> assertEquals(4, sortedVec.size()),
          () -> assertEquals("a", sortedVec.get(0)),
          () -> assertEquals("b", sortedVec.get(1)),
          () -> assertEquals("c", sortedVec.get(2)),
          () -> assertEquals("d", sortedVec.get(3))
      );
    }

    @Test
    public void ofSortedStringsFastEnough() {
      var strings = IntStream.range(0, 100_000)
          .mapToObj(i -> String.format("%05d", i))
          .collect(Collectors.toCollection(LinkedList::new));
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> SortedVec.ofSortedStrings(strings));
    }

    @Test
    public void qualityOfImplementation() {
      assertTrue(SortedVec.class.accessFlags().contains(AccessFlag.FINAL));
      assertEquals(0, SortedVec.class.getConstructors().length);
      assertTrue(Arrays.stream(SortedVec.class.getDeclaredFields())
          .allMatch(f -> f.accessFlags().contains(AccessFlag.FINAL)));
      assertTrue(Arrays.stream(SortedVec.class.getDeclaredFields())
          .anyMatch(f -> f.getType().isArray()));
      assertTrue(SortedVec.class.getDeclaredFields().length <= 2);
    }

    @Test
    public void ofSortedStringsPreconditions() {
      var list = new ArrayList<String>() {{ add(null); }};
      var list2 = new ArrayList<String>() {{ add("foo"); add(null); }};
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> SortedVec.ofSortedStrings(null)),
          () -> assertThrows(NullPointerException.class, () -> SortedVec.ofSortedStrings(list)),
          () -> assertThrows(NullPointerException.class, () -> SortedVec.ofSortedStrings(list2))
      );
    }

    @Test
    public void ofSortedStringsNotSorted() {
      assertAll(
          () -> assertThrows(IllegalArgumentException.class, () -> SortedVec.ofSortedStrings(List.of("P", "H"))),
          () -> assertThrows(IllegalArgumentException.class, () -> SortedVec.ofSortedStrings(List.of("a", "b", "c", "a")))
      );
    }

    @Test
    public void ofSortedStringsUseUnicodeOrder() {
      var sortedVec = SortedVec.ofSortedStrings(List.of("B", "a"));
      assertNotNull(sortedVec);
    }

    @Test
    public void getOutOfBounds() {
      var sortedVec = SortedVec.ofSortedStrings(List.of("1", "2"));
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class,
              () -> sortedVec.get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class,
              () -> sortedVec.get(2))
      );
    }
  }


  @Nested
  public class Q3 {
    @Test
    public void checkSorted() {
      var strings = new String[] { "bar", "baz", "foo" };
      SortedVec.checkSorted(strings, String::compareToIgnoreCase);
    }

    @Test
    public void checkSorted2() {
      var strings = new String[] { "bar", "Baz", "foo" };
      SortedVec.checkSorted(strings, String::compareToIgnoreCase);
    }

    @Test
    public void checkSortedEmpty() {
      record Person(String name) {
        int compare(Person p) {
          return fail();
        }
      }
      SortedVec.checkSorted(new Person[0], Person::compare);
    }

    @Test
    public void checkSortedOneElement() {
      record Dog(int age) {
        int compare(Dog d) {
          return fail();
        }
      }
      var scooby = new Dog(42);
      SortedVec.checkSorted(new Dog[] { scooby }, Dog::compare);
    }

    @Test
    public void checkSortedDuplicateElements() {
      var strings = new String[] { "Albus", "Bar", "Bar", "Foo" };
      SortedVec.checkSorted(strings, String::compareToIgnoreCase);
    }

    @Test
    public void checkSortedNotSorted() {
      var strings = new String[] { "Potter", "Harry" };
      assertThrows(IllegalArgumentException.class,
          () -> SortedVec.checkSorted(strings, String::compareToIgnoreCase));
    }

    @Test
    public void checkSortedNotSorted2() {
      var strings = new String[] { "a", "b", "c", "a" };
      assertThrows(IllegalArgumentException.class,
          () -> SortedVec.checkSorted(strings, String::compareToIgnoreCase));
    }

    @Test
    public void checkSortedFastEnough() {
      var strings = IntStream.range(0, 100_000)
          .mapToObj(i -> String.format("%05d", i))
          .toArray(String[]::new);
      assertTimeoutPreemptively(Duration.ofMillis(1_000),
          () -> SortedVec.checkSorted(strings, String::compareToIgnoreCase));
    }

    @Test
    public void checkSortedIsAnHelperMethod() {
      var checkSorted = Arrays.stream(SortedVec.class.getDeclaredMethods())
          .filter(m -> m.getName().equals("checkSorted"))
          .findFirst()
          .orElseThrow();
      assertFalse(checkSorted.accessFlags().contains(AccessFlag.PUBLIC));
    }

    @Test
    public void ofSorted() {
      List<String> list = List.of("bar", "baz", "foo");
      SortedVec<String> sortedVec = SortedVec.ofSorted(list, String::compareToIgnoreCase);
      assertAll(
          () -> assertEquals(3, sortedVec.size()),
          () -> assertEquals("bar", sortedVec.get(0)),
          () -> assertEquals("baz", sortedVec.get(1)),
          () -> assertEquals("foo", sortedVec.get(2))
      );
    }

    @Test
    public void ofSortedEmpty() {
      record Person(String name) {
        int compare(Person p) {
          return fail();
        }
      }
      var sortedVec = SortedVec.ofSorted(List.of(), Person::compare);
      assertEquals(0, sortedVec.size());
    }

    @Test
    public void ofSortedOneElement() {
      record Dog(int age) {
        int compare(Dog d) {
          return fail();
        }
      }
      var scooby = new Dog(42);
      var sortedVec = SortedVec.ofSorted(List.of(scooby), Dog::compare);
      assertAll(
          () -> assertEquals(1, sortedVec.size()),
          () -> assertEquals(scooby, sortedVec.get(0))
      );
    }

    @Test
    public void ofSortedMutableAdd() {
      var list = new ArrayList<>(List.of("a", "b", "c", "d"));
      var sortedVec = SortedVec.ofSorted(list, String::compareToIgnoreCase);
      list.add("wtf");
      assertAll(
          () -> assertEquals(4, sortedVec.size()),
          () -> assertEquals("a", sortedVec.get(0)),
          () -> assertEquals("b", sortedVec.get(1)),
          () -> assertEquals("c", sortedVec.get(2)),
          () -> assertEquals("d", sortedVec.get(3))
      );
    }

    @Test
    public void ofSortedMutableSet() {
      var list = new ArrayList<>(List.of("a", "b", "c", "d"));
      var sortedVec = SortedVec.ofSorted(list, String::compareToIgnoreCase);
      list.set(0, "wtf");
      assertAll(
          () -> assertEquals(4, sortedVec.size()),
          () -> assertEquals("a", sortedVec.get(0)),
          () -> assertEquals("b", sortedVec.get(1)),
          () -> assertEquals("c", sortedVec.get(2)),
          () -> assertEquals("d", sortedVec.get(3))
      );
    }

    @Test
    public void ofSortedMutableAddFirst() {
      var list = new ArrayList<>(List.of("a", "b", "c", "d"));
      var sortedVec = SortedVec.ofSorted(list, String::compareToIgnoreCase);
      list.addFirst("wtf");
      assertAll(
          () -> assertEquals(4, sortedVec.size()),
          () -> assertEquals("a", sortedVec.get(0)),
          () -> assertEquals("b", sortedVec.get(1)),
          () -> assertEquals("c", sortedVec.get(2)),
          () -> assertEquals("d", sortedVec.get(3))
      );
    }

    @Test
    public void ofSorteMutableRemoveLast() {
      var list = new ArrayList<>(List.of("a", "b", "c", "d"));
      var sortedVec = SortedVec.ofSorted(list, String::compareToIgnoreCase);
      list.removeLast();
      assertAll(
          () -> assertEquals(4, sortedVec.size()),
          () -> assertEquals("a", sortedVec.get(0)),
          () -> assertEquals("b", sortedVec.get(1)),
          () -> assertEquals("c", sortedVec.get(2)),
          () -> assertEquals("d", sortedVec.get(3))
      );
    }

    @Test
    public void ofSortedFastEnough() {
      var integers = IntStream.range(0, 100_000)
          .boxed()
          .collect(Collectors.toCollection(LinkedList::new));
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> SortedVec.ofSorted(integers, Integer::compare));
    }

    @Test
    public void ofSortedPreconditions() {
      var list = new ArrayList<String>() {{ add("foo"); add(null); }};
      assertAll(
          () -> assertThrows(NullPointerException.class,
              () -> SortedVec.ofSorted(null, String::compareToIgnoreCase)),
          () -> assertThrows(NullPointerException.class,
              () -> SortedVec.ofSorted(List.of(), null)),
          () -> assertThrows(NullPointerException.class,
              () -> SortedVec. ofSorted(list, String::compareToIgnoreCase))
      );
    }

    @Test
    public void ofSortedNotSorted() {
      assertAll(
          () -> assertThrows(IllegalArgumentException.class, () -> SortedVec.ofSorted(List.of("P", "H"), String::compareToIgnoreCase)),
          () -> assertThrows(IllegalArgumentException.class, () -> SortedVec.ofSorted(List.of("a", "b", "c", "a"), String::compareToIgnoreCase))
      );
    }
  }


  @Nested
  public class Q4 {
    @Test
    public void ofSortedSignature() {
      class A {
        int compare(A a) { return fail(); }
      }
      class B extends A {}

      var list = List.of(new B());
      var sortedVec = SortedVec.<B>ofSorted(list, A::compare);
      assertNotNull(sortedVec);
    }

    @Test
    public void ofSortedSignature2() {
      class A {
        int compare(A a) { return fail(); }
      }
      class B extends A {}

      var list = List.of(new B());
      var sortedVec = SortedVec.<A>ofSorted(list, A::compare);
      assertNotNull(sortedVec);
    }

    @Test
    public void ofSortedSignature3() {
      var list = List.of("foo");
      var sortedVec = SortedVec.<CharSequence>ofSorted(list,
          (Object o1, Object o2) -> o1.toString().compareTo(o2.toString()));
      assertNotNull(sortedVec);
    }
  }


  @Nested
  public class Q5 {
    @Test
    public void isInOfSortedStrings() {
      var sortedVec = SortedVec.ofSortedStrings(List.of("a", "c", "d"));
      assertAll(
          () -> assertTrue(sortedVec.isIn("a")),
          () -> assertTrue(sortedVec.isIn("c")),
          () -> assertTrue(sortedVec.isIn("d")),
          () -> assertFalse(sortedVec.isIn("b"))
      );
    }

    @Test
    public void isInOfSortedStringsEmpty() {
      var sortedVec = SortedVec.ofSortedStrings(List.of());
      assertAll(
          () -> assertFalse(sortedVec.isIn("a")),
          () -> assertFalse(sortedVec.isIn("b")),
          () -> assertFalse(sortedVec.isIn("A"))
      );
    }

    @Test
    public void isInOfSorted() {
      var sortedVec = SortedVec.ofSorted(List.of("a", "c", "d"), String::compareToIgnoreCase);
      assertAll(
          () -> assertTrue(sortedVec.isIn("a")),
          () -> assertTrue(sortedVec.isIn("c")),
          () -> assertTrue(sortedVec.isIn("d")),
          () -> assertFalse(sortedVec.isIn("b")),
          () -> assertTrue(sortedVec.isIn("A")),
          () -> assertTrue(sortedVec.isIn("C")),
          () -> assertTrue(sortedVec.isIn("D")),
          () -> assertFalse(sortedVec.isIn("B"))
      );
    }

    @Test
    public void isInOfSortedStringsFastEnough() {
      var strings = IntStream.range(0, 100_000)
          .mapToObj(i -> String.format("%05d", i))
          .toList();
      var sortedVec = SortedVec.ofSortedStrings(strings);
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
        for(var s : strings) {
          assertTrue(sortedVec.isIn(s));
        }
      });
    }

    @Test
    public void isInOfSortedFastEnough() {
      var strings = IntStream.range(0, 100_000)
          .mapToObj(i -> String.format("%05d", i))
          .toList();
      var sortedVec = SortedVec.ofSorted(strings, String::compareToIgnoreCase);
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
        for(var s : strings) {
          assertTrue(sortedVec.isIn(s));
        }
      });
    }

    @Test
    public void qualityOfImplementation() {
      assertEquals(2, SortedVec.class.getDeclaredFields().length);
    }

    @Test
    public void isInPrecondition() {
      var sortedVec1 = SortedVec.ofSortedStrings(List.of());
      var sortedVec2 = SortedVec.ofSorted(List.of(), String::compareToIgnoreCase);
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> sortedVec1.isIn(null)),
          () -> assertThrows(NullPointerException.class, () -> sortedVec2.isIn(null))
      );
    }
  }


  @Nested
  public class Q6 {
    @Test
    public void displayOfSortedStrings() {
      var sortedVec = SortedVec.ofSortedStrings(List.of("a", "b", "c"));
      assertEquals("a <= b <= c", "" + sortedVec);
    }

    @Test
    public void displayOfSortedStringsEmpty() {
      var sortedVec = SortedVec.ofSortedStrings(List.of());
      assertEquals("", "" + sortedVec);
    }

    @Test
    public void displayOfSortedCat() {
      record Cat(String name) {
        int compare(Cat cat) {
          return fail();
        }
      }
      var garfield = new Cat("Garfield");
      var sortedVec = SortedVec.ofSorted(List.of(garfield), Cat::compare);
      assertEquals("Cat[name=Garfield]", "" + sortedVec);
    }

    @Test
    public void displayOfSorted() {
      var sortedVec = SortedVec.ofSorted(List.of("a", "b", "c", "d"), String::compareToIgnoreCase);
      assertEquals("a <= b <= c <= d", "" + sortedVec);
    }
  }


  @Nested
  public class Q7 {
    @Test
    public void appendOfSortedStrings() {
      var sortedVec1 = SortedVec.ofSortedStrings(List.of("bar", "foo"));
      var sortedVec2 = SortedVec.ofSortedStrings(List.of("baz", "whizz"));
      var sortedVec3 = sortedVec1.append(sortedVec2);
      assertAll(
          () -> assertNotSame(sortedVec1, sortedVec3),
          () -> assertNotSame(sortedVec2, sortedVec3),
          () -> assertEquals(4, sortedVec3.size()),
          () -> assertEquals("bar", sortedVec3.get(0)),
          () -> assertEquals("baz", sortedVec3.get(1)),
          () -> assertEquals("foo", sortedVec3.get(2)),
          () -> assertEquals("whizz", sortedVec3.get(3))
      );
    }

    @Test
    public void appendOfSortedStringsDuplicate() {
      var sortedVec1 = SortedVec.ofSortedStrings(List.of("bar", "foo"));
      var sortedVec2 = SortedVec.ofSortedStrings(List.of("foo", "whizz"));
      var sortedVec3 = sortedVec1.append(sortedVec2);
      assertAll(
          () -> assertNotSame(sortedVec1, sortedVec3),
          () -> assertNotSame(sortedVec2, sortedVec3),
          () -> assertEquals(4, sortedVec3.size()),
          () -> assertEquals("bar", sortedVec3.get(0)),
          () -> assertEquals("foo", sortedVec3.get(1)),
          () -> assertEquals("foo", sortedVec3.get(2)),
          () -> assertEquals("whizz", sortedVec3.get(3))
      );
    }

    @Test
    public void appendOfSortedStringsEmpty() {
      var sortedVec1 = SortedVec.ofSortedStrings(List.of("bar", "foo"));
      var sortedVec2 = SortedVec.ofSortedStrings(List.of());
      var sortedVec3 = sortedVec1.append(sortedVec2);
      assertAll(
          () -> assertNotSame(sortedVec2, sortedVec3),
          () -> assertEquals(2, sortedVec3.size()),
          () -> assertEquals("bar", sortedVec3.get(0)),
          () -> assertEquals("foo", sortedVec3.get(1))
      );
    }

    @Test
    public void appendOfSortedStringsEmpty2() {
      var sortedVec1 = SortedVec.ofSortedStrings(List.of());
      var sortedVec2 = SortedVec.ofSortedStrings(List.of("bar", "foo"));
      var sortedVec3 = sortedVec1.append(sortedVec2);
      assertAll(
          () -> assertNotSame(sortedVec1, sortedVec3),
          () -> assertEquals(2, sortedVec3.size()),
          () -> assertEquals("bar", sortedVec3.get(0)),
          () -> assertEquals("foo", sortedVec3.get(1))
      );
    }

    @Test
    public void appendOfSortedFails() {
      var sortedVec1 = SortedVec.ofSortedStrings(List.of("bar", "foo"));
      var sortedVec2 = SortedVec.ofSorted(List.of("baz", "whizz"), String::compareToIgnoreCase);
      assertThrows(IllegalArgumentException.class, () -> sortedVec1.append(sortedVec2));
    }

    @Test
    public void appendCustomClass() {
      class A {}
      var compare = java.util.Comparator.comparingInt(A::hashCode);
      var sortedVec1 = SortedVec.ofSorted(List.of(new A()), compare);
      var sortedVec2 = SortedVec.ofSorted(List.of(new A()), compare);
      var sortedVec3 = sortedVec1.append(sortedVec2);
      assertEquals(2, sortedVec3.size());
    }

    @Test
    public void appendSignature() {
      assertTrue(Arrays.stream(SortedVec.class.getDeclaredMethods())
          .filter(m -> m.getName().equals("append"))
          .allMatch(m -> m.getGenericParameterTypes()[0] instanceof ParameterizedType parameterizedType &&
              !(parameterizedType.getActualTypeArguments()[0] instanceof TypeVariable<?>)));
    }

    @Test
    public void appendPrecondition() {
      var sortedVec = SortedVec.ofSortedStrings(List.of("bar", "foo"));
      assertThrows(NullPointerException.class, () -> sortedVec.append(null));
    }
  }


  @Nested
  public class Q8 {
    @Test
    public void ofSortedIntegers() {
      List<Integer> list = List.of(1, 2, 4);
      SortedVec<Integer> sortedVec = SortedVec.ofSorted(list);
      assertAll(
          () -> assertEquals(3, sortedVec.size()),
          () -> assertEquals(1, sortedVec.get(0)),
          () -> assertEquals(2, sortedVec.get(1)),
          () -> assertEquals(4, sortedVec.get(2))
      );
    }

    @Test
    public void ofSortedTimestamp() {
      var list = List.of(new Timestamp(12));
      var sortedVec = SortedVec.ofSorted(list);
      assertAll(
          () -> assertEquals(1, sortedVec.size()),
          () -> assertEquals(new Timestamp(12), sortedVec.get(0))
      );
    }

    @Test
    public void ofSortedOneElementInteger() {
      var sortedVec = SortedVec.ofSorted(List.of(42));
      assertAll(
          () -> assertEquals(1, sortedVec.size()),
          () -> assertEquals(42, sortedVec.get(0))
      );
    }

    @Test
    public void ofSortedMutableAddInteger() {
      var list = new ArrayList<>(List.of(1, 2, 3, 4));
      var sortedVec = SortedVec.ofSorted(list);
      list.add(42);
      assertAll(
          () -> assertEquals(4, sortedVec.size()),
          () -> assertEquals(1, sortedVec.get(0)),
          () -> assertEquals(2, sortedVec.get(1)),
          () -> assertEquals(3, sortedVec.get(2)),
          () -> assertEquals(4, sortedVec.get(3))
      );
    }

    @Test
    public void ofSortedMutableSetInteger() {
      var list = new ArrayList<>(List.of(1, 2, 3, 4));
      var sortedVec = SortedVec.ofSorted(list);
      list.set(0, 42);
      assertAll(
          () -> assertEquals(4, sortedVec.size()),
          () -> assertEquals(1, sortedVec.get(0)),
          () -> assertEquals(2, sortedVec.get(1)),
          () -> assertEquals(3, sortedVec.get(2)),
          () -> assertEquals(4, sortedVec.get(3))
      );
    }

    @Test
    public void ofSortedMutableAddFirst() {
      var list = new ArrayList<>(List.of(1, 2, 3, 4));
      var sortedVec = SortedVec.ofSorted(list);
      list.addFirst(42);
      assertAll(
          () -> assertEquals(4, sortedVec.size()),
          () -> assertEquals(1, sortedVec.get(0)),
          () -> assertEquals(2, sortedVec.get(1)),
          () -> assertEquals(3, sortedVec.get(2)),
          () -> assertEquals(4, sortedVec.get(3))
      );
    }

    @Test
    public void ofSortedMutableRemoveLast() {
      var list = new ArrayList<>(List.of(1, 2, 3, 4));
      var sortedVec = SortedVec.ofSorted(list);
      list.removeLast();
      assertAll(
          () -> assertEquals(4, sortedVec.size()),
          () -> assertEquals(1, sortedVec.get(0)),
          () -> assertEquals(2, sortedVec.get(1)),
          () -> assertEquals(3, sortedVec.get(2)),
          () -> assertEquals(4, sortedVec.get(3))
      );
    }

    @Test
    public void ofSortedAppend() {
      var sortedVec1 = SortedVec.ofSorted(List.of(new Date(42)));
      var sortedVec2 = SortedVec.ofSorted(List.of(new Timestamp(13)));
      var sortedVec3 = sortedVec1.append(sortedVec2);
      assertAll(
          () -> assertEquals(2, sortedVec3.size()),
          () -> assertEquals(new Timestamp(13), sortedVec3.get(0)),
          () -> assertEquals(new Date(42), sortedVec3.get(1))
      );
    }

    @Test
    public void ofSortedALotOfIntegers() {
      var list = IntStream.range(0, 100_000).boxed().toList();
      var sortedVec = SortedVec.ofSorted(list);
      for(var i = 0; i < sortedVec.size(); i++) {
        assertEquals(i, sortedVec.get(i));
      }
    }

    @Test
    public void ofSortedALotOfStrings() {
      var list = IntStream.range(0, 100_000).mapToObj(i -> String.format("%05d", i)).toList();
      var sortedVec = SortedVec.ofSorted(list);
      for(var i = 0; i < sortedVec.size(); i++) {
        assertEquals(list.get(i), sortedVec.get(i));
      }
    }

    @Test
    public void ofSortedSignature() {
      class A implements Comparable<Object> {
        @Override
        public int compareTo(Object o) {
          return toString().compareTo(o.toString());
        }
      }
      class B extends A {}
      List<B> list = List.of(new B());
      SortedVec<A> sortedVec = SortedVec.ofSorted(list);
      assertNotNull(sortedVec);
    }

    @Test
    public void ofSortedPreconditions() {
      var list = new ArrayList<String>() {{ add("foo"); add(null); }};
      assertAll(
          () -> assertThrows(NullPointerException.class,
              () -> SortedVec.ofSorted(null)),
          () -> assertThrows(NullPointerException.class,
              () -> SortedVec.ofSorted(list))
      );
    }

    @Test
    public void getOutOfBounds() {
      var sortedVec = SortedVec.ofSorted(List.of(1, 2));
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class,
              () -> sortedVec.get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class,
              () -> sortedVec.get(2))
      );
    }
  }
}