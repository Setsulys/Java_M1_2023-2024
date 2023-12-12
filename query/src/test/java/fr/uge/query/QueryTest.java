package fr.uge.query;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueryTest {
  @Nested
  class Q1 {
    @Test
    public void ticketName() {
      record Ticket(Optional<String> name) {}

      List<Ticket> tickets = List.of(
          new Ticket(Optional.of("Bob")),
          new Ticket(Optional.empty()),
          new Ticket(Optional.of("Ana")));
      Query<String> query = Query.fromList(tickets, Ticket::name);
      assertEquals("Bob |> Ana", "" + query);
    }

    @Test
    public void personPet() {
      record Pet(String name) {}
      record Person(String name, Optional<Pet> pet) {}

      List<Person> persons = List.of(
          new Person("Fred", Optional.empty()),
          new Person("Samy", Optional.of(new Pet("Scooby"))));
      Query<Pet> query = Query.fromList(persons, Person::pet);
      assertEquals("Pet[name=Scooby]", "" + query);
    }

    @Test
    public void example() {
      class Helpers {
        static Optional<String> asStringIfEven(int value) {
          return Optional.of(value).filter(v -> v % 2 == 0).map(Object::toString);
        }
      }

      List<Integer> list = List.of(1, 2, 4, 8);
      Query<String> query = Query.fromList(list, Helpers::asStringIfEven);
      assertEquals("2 |> 4 |> 8", "" + query);
    }

    @Test
    public void integers() {
      class Helpers {
        static Optional<Integer> asInt(String name) {
          var scanner = new Scanner(name);
          return Optional.ofNullable(scanner.hasNextInt() ? scanner.nextInt() : null);
        }
      }

      var list = List.of("foo", "42", "3.14", "-17");
      var query = Query.fromList(list, Helpers::asInt);
      assertEquals("42 |> -17", "" + query);
    }

    @Test
    public void empty() {
      var emptyList = List.of();
      var query = Query.fromList(emptyList, Optional::ofNullable);
      assertEquals("", "" + query);
    }

    @Test
    public void arrow() {
      var list = List.of(",", ",");
      var query = Query.fromList(list, Optional::ofNullable);
      assertEquals(", |> ,", "" + query);
    }

    @Test
    public void nullAsInitialValues() {
      var list = Arrays.asList("foo", null, "bar", null);
      var query = Query.fromList(list, Optional::ofNullable);
      assertEquals("foo |> bar", "" + query);
    }

    @Test
    public void notTooManyCallsToTransform() {
      record Info(Optional<String> name) {}

      var counter = new Object() { int count; };
      var list = List.of(new Info(Optional.of("1")), new Info(Optional.of("2")));
      var query = Query.fromList(list, value -> {
        counter.count++;
        return Optional.empty();
      });
      assertEquals(0, counter.count);
      assertEquals("", "" + query);
      assertEquals(2, counter.count);
    }

    @Test
    public void signature() {
      Query<Number> query = Query.fromList(List.<String>of(), (Object o) -> Optional.<Integer>empty());
      assertNotNull(query);
    }

    @Test
    public void signature2() {
      Query<Object> query = Query.fromList(List.of(Optional.of("hello")), UnaryOperator.<Optional<CharSequence>>identity());
      assertNotNull(query);
    }

    @Test
    public void isAnInterface() {
      assertTrue(Query.class.isInterface());
    }

    @Test
    public void onlyOneImplementation() {
      var permittedSubclasses = Query.class.getPermittedSubclasses();
      assertNotNull(permittedSubclasses);
      assertEquals(1, permittedSubclasses.length);
    }

    @Test
    public void implementationHasNoPublicConstructors() {
      record Pet(Optional<String> name) {}
      var query = Query.fromList(List.of(new Pet(Optional.of("Scooby")), new Pet(Optional.empty())), Pet::name);
      assertEquals(0, query.getClass().getConstructors().length);
    }

    @Test
    public void preconditionsFrom() {
      class Helpers {
        static Optional<Object> alwaysEmpty(Object o) {
          return Optional.empty();
        }
      }

      assertAll(
          () -> assertThrows(NullPointerException.class, () -> Query.fromList(null, Helpers::alwaysEmpty)),
          () -> assertThrows(NullPointerException.class, () -> Query.fromList(List.of(), null))
      );
    }

    @Test
    public void preconditionsPrint() {
      class Helpers {
        static Optional<Object> returnNull(Object o) {
          return null;
        }
      }

      assertThrows(NullPointerException.class, () -> System.out.println(Query.fromList(List.of("foo"), Helpers::returnNull)));
    }
  }


  @Nested
  class Q2 {
    @Test
    public void ticketName() {
      record Ticket(Optional<String> name) {}

      var tickets = List.of(
          new Ticket(Optional.of("Bob")),
          new Ticket(Optional.empty()),
          new Ticket(Optional.of("Ana")));
      var query = Query.fromList(tickets, Ticket::name);
      assertEquals(List.of("Bob", "Ana"), query.toList());
    }

    @Test
    public void personPet() {
      record Pet(String name) {}
      record Person(String name, Optional<Pet> pet) {}

      var persons = List.of(
          new Person("Fred", Optional.empty()),
          new Person("Samy", Optional.of(new Pet("Scooby"))));
      var query = Query.fromList(persons, Person::pet);
      assertEquals(List.of(new Pet("Scooby")), query.toList());
    }

    @Test
    public void integers() {
      class Helpers {
        static Optional<Integer> asInt(String name) {
          var scanner = new Scanner(name);
          return Optional.ofNullable(scanner.hasNextInt() ? scanner.nextInt() : null);
        }
      }

      var list = List.of("foo", "42", "3.14", "-17");
      var query = Query.fromList(list, Helpers::asInt);
      assertEquals(List.of(42, -17), query.toList());
    }

    @Test
    public void empty() {
      var emptyList = List.of();
      var query = Query.fromList(emptyList, Optional::ofNullable);
      assertEquals(List.of(), query.toList());
    }

    @Test
    public void nullAsValues() {
      class Helpers {
        static Optional<String> notNull(String text) {
          return Optional.ofNullable(text);
        }
      }

      var list = Arrays.asList("foo", null, "bar", null);
      assertEquals(List.of("foo", "bar"), Query.fromList(list, Helpers::notNull).toList());
    }

    @Test
    public void aLot() {
      class Helpers {
        static Optional<Integer> pair(int value) {
          return Optional.ofNullable(value % 2 == 0 ? value : null);
        }
      }

      var list = range(0, 1_000_000).boxed().toList();
      assertTimeoutPreemptively(Duration.ofMillis(2_000),
          () -> assertEquals(500_000, Query.fromList(list, Helpers::pair).toList().size())
      );
    }

    @Test
    public void aLot2() {
      class Helpers {
        static Optional<Integer> pair(int value) {
          return Optional.ofNullable(value % 2 == 0 ? value : null);
        }
      }

      var list = range(0, 1_000_000).boxed().toList();
      var expected = range(0, 1_000_000).filter(x -> x % 2 == 0).boxed().toList();
      assertTimeoutPreemptively(Duration.ofMillis(2_000),
          () -> assertEquals(expected, Query.fromList(list, Helpers::pair).toList())
      );
    }

    @Test
    public void notTooManyCallsToTransform() {
      record Info(Optional<String> name) {}

      var counter = new Object() { int count; };
      var list = List.of(new Info(Optional.of("1")), new Info(Optional.of("2")));
      var query = Query.fromList(list, value -> {
        counter.count++;
        return Optional.empty();
      });
      assertEquals(0, counter.count);
      assertEquals(List.of(), query.toList());
      assertEquals(2, counter.count);
    }

    @Test
    public void nonModifiableList() {
      record Foo(Optional<Integer> count) {}

      var fooList = range(0, 10).mapToObj(i -> new Foo(Optional.of(i))).toList();
      var result = Query.fromList(fooList, Foo::count).toList();
      assertAll(
          () -> assertEquals(10, result.size()),
          () -> assertThrows(UnsupportedOperationException.class, () -> result.add(3)),
          () -> assertThrows(UnsupportedOperationException.class, () -> result.remove(3))
      );
    }

    @Test
    public void preconditionsToList() {
      class Helpers {
        static Optional<Object> returnNull(Object o) {
          return null;
        }
      }

      assertThrows(NullPointerException.class, () -> Query.fromList(List.of("foo"), Helpers::returnNull).toList());
    }
  }


  @Nested
  class Q3 {
    @Test
    public void ticketName() {
      record Ticket(Optional<String> name) {}

      var tickets = List.of(
          new Ticket(Optional.of("Bob")),
          new Ticket(Optional.empty()),
          new Ticket(Optional.of("Ana")));
      var query = Query.fromList(tickets, Ticket::name);
      assertAll(
          () -> assertEquals(2, query.toStream().count()),
          () -> assertEquals("Bob", query.toStream().findFirst().orElseThrow()),
          () -> assertEquals(List.of("Bob", "Ana"), query.toStream().toList())
      );

    }

    @Test
    public void personPet() {
      record Pet(String name) {}
      record Person(String name, Optional<Pet> pet) {}

      var persons = List.of(
          new Person("Fred", Optional.empty()),
          new Person("Samy", Optional.of(new Pet("Scooby"))));
      var query = Query.fromList(persons, Person::pet);
      assertAll(
          () -> assertEquals(1, query.toStream().count()),
          () -> assertEquals(new Pet("Scooby"), query.toStream().findFirst().orElseThrow()),
          () -> assertEquals(List.of(new Pet("Scooby")), query.toStream().toList())
      );
    }

    @Test
    public void integers() {
      class Helpers {
        static Optional<Integer> hasInt(CharSequence name) {
          var scanner = new Scanner(name.toString());
          return Optional.ofNullable(scanner.hasNextInt() ? scanner.nextInt() : null);
        }
      }

      var list = List.of("foo", "42", "3.14", "-17");
      assertEquals(42, Query.fromList(list, Helpers::hasInt).toStream().findFirst().orElseThrow());

      var emptyList = List.<String>of();
      assertTrue(Query.fromList(emptyList, Helpers::hasInt).toStream().findFirst().isEmpty());
    }

    @Test
    public void empty() {
      var emptyList = List.of();
      var query = Query.fromList(emptyList, Optional::ofNullable);
      assertEquals(0, query.toStream().count());
    }

    @Test
    public void streamDoNotDoAnyComputationByDefault() {
      record Info(Optional<String> name) {}

      var counter = new Object() { int count; };
      var list = List.of(new Info(Optional.of("1")), new Info(Optional.of("2")));
      var query = Query.fromList(list, value -> Optional.of(counter.count++));
      assertEquals(0, counter.count);
    }

    @Test
    public void notTooManyCallsToTransform() {
      record Info(Optional<String> name) {}

      var counter = new Object() { int count; };
      var list = List.of(new Info(Optional.of("1")), new Info(Optional.of("2")));
      var query = Query.fromList(list, value -> Optional.of(counter.count++));
      assertEquals(0, query.toStream().findFirst().orElseThrow());
      assertEquals(1, counter.count);
    }

    @Test
    public void nullAsValues() {
      class Helpers {
        static Optional<String> notNull(String text) {
          return Optional.ofNullable(text);
        }
      }

      var list = Arrays.asList(null, "foo", null, "bar", null);
      assertEquals("foo", Query.fromList(list, Helpers::notNull).toStream().findFirst().orElseThrow());
    }
  }


  @Nested
  class Q4 {
    @Test
    public void ticketName() {
      record Ticket(Optional<String> name) {}

      var tickets = List.of(
          new Ticket(Optional.of("Bob")),
          new Ticket(Optional.empty()),
          new Ticket(Optional.of("Ana")));
      var query = Query.fromList(tickets, Ticket::name);
      assertEquals(List.of("Bob", "Ana"), query.toLazyList());
    }

    @Test
    public void personPet() {
      record Pet(String name) {}
      record Person(String name, Optional<Pet> pet) {}

      var persons = List.of(
          new Person("Fred", Optional.empty()),
          new Person("Samy", Optional.of(new Pet("Scooby"))));
      var query = Query.fromList(persons, Person::pet);
      assertEquals(List.of(new Pet("Scooby")), query.toLazyList());
    }

    @Test
    public void integers() {
      class Helpers {
        static Optional<Integer> hasInt(String name) {
          var scanner = new Scanner(name);
          return Optional.ofNullable(scanner.hasNextInt() ? scanner.nextInt() : null);
        }
      }

      var list = List.of("foo", "42", "3.14", "-17");
      assertEquals(List.of(42, -17), Query.fromList(list, Helpers::hasInt).toLazyList());

      var emptyList = List.<String>of();
      assertEquals(List.of(), Query.fromList(emptyList, Helpers::hasInt).toLazyList());
    }

    @Test
    public void nullAsValues() {
      class Helpers {
        static Optional<String> notNull(String text) {
          return Optional.ofNullable(text);
        }
      }

      var list = Arrays.asList("foo", null, "bar", null);
      assertEquals(List.of("foo", "bar"), Query.fromList(list, Helpers::notNull).toLazyList());
    }

    @Test
    public void lazyListALot() {
      class Helpers {
        static Optional<Integer> even(int value) {
          return Optional.ofNullable(value % 2 == 0 ? value : null);
        }
      }

      var list = range(0, 1_000_000).boxed().toList();
      assertTimeoutPreemptively(Duration.ofMillis(2_000),
          () -> assertEquals(500_000, Query.fromList(list, Helpers::even).toLazyList().size())
      );
    }

    @Test
    public void lazyListALot2() {
      class Helpers {
        static Optional<Integer> even(int value) {
          return Optional.ofNullable(value % 2 == 0 ? value : null);
        }
      }

      var list = range(0, 1_000_000).boxed().toList();
      var expected = range(0, 1_000_000).filter(x -> x % 2 == 0).boxed().toList();
      assertTimeoutPreemptively(Duration.ofMillis(2_000),
          () -> assertEquals(expected, Query.fromList(list, Helpers::even).toLazyList())
      );
    }

    @Test
    public void lazyListComplexity() {
      class Helpers {
        static Optional<Integer> even(int value) {
          return Optional.ofNullable(value % 2 == 0 ? value : null);
        }
      }

      var list = range(0, 1_000_000).boxed().collect(Collectors.toCollection(LinkedList::new));
      var expected = range(0, 1_000_000).filter(x -> x % 2 == 0).boxed().toList();
      assertTimeoutPreemptively(Duration.ofMillis(2_000),
          () -> assertEquals(expected, Query.fromList(list, Helpers::even).toLazyList())
      );
    }

    @Test
    public void notTooManyCallsToTransform() {
      var counter = new Object() { int count; };
      var query = Query.fromList(List.of(1, 2, 3, 4, 5), value -> {
        counter.count++;
        return Optional.of(value);
      });
      var lazyList = query.toLazyList();
      assertEquals(0, counter.count);
      assertEquals(2, lazyList.get(1));
      assertEquals(2, counter.count);
    }

    @Test
    public void notTooManyCallsToTransform2() {
      var counter = new Object() { int count; };
      var query = Query.fromList(List.of(1, 2, 3, 4, 5), value -> {
        counter.count++;
        return Optional.of(value);
      });
      var lazyList = query.toLazyList();
      assertEquals(0, counter.count);
      assertEquals(5, lazyList.size());
      assertEquals(5, counter.count);
    }

    @Test
    public void notTooManyCallsToTransform3() {
      var counter = new Object() { int count; };
      var query = Query.fromList(List.of(1, 2, 3, 4, 5), value -> {
        counter.count++;
        return value % 2 == 0 ? Optional.of(value) : Optional.empty();
      });
      var lazyList = query.toLazyList();
      assertEquals(0, counter.count);
      assertEquals(4, lazyList.get(1));
      assertEquals(4, counter.count);
    }

    @Test
    public void notTooManyCallsToTransform4() {
      var counter = new Object() { int count; };
      var query = Query.fromList(List.of(1, 2, 3, 4, 5), value -> {
        counter.count++;
        return value % 2 == 0 ? Optional.of(value) : Optional.empty();
      });
      var lazyList = query.toLazyList();
      assertEquals(0, counter.count);
      assertEquals(2, lazyList.get(0));
      assertEquals(4, lazyList.get(1));
      assertEquals(4, counter.count);
    }

    @Test
    public void notTooManyCallsToTransform5() {
      var counter = new Object() { int count; };
      var query = Query.fromList(List.of(1, 2, 3, 4, 5), value -> {
        counter.count++;
        return value % 2 == 0 ? Optional.of(value) : Optional.empty();
      });
      var lazyList = query.toLazyList();
      assertEquals(0, counter.count);
      assertEquals(2, lazyList.size());
      assertEquals(5, counter.count);
      assertEquals(2, lazyList.size());
      assertEquals(5, counter.count);
    }

    @Test
    public void getOutOfBounds() {
      record Foo(Optional<Integer> value) {}

      var fooList = range(0, 5).mapToObj(i -> new Foo(Optional.of(i))).toList();
      var lazyList = Query.fromList(fooList, Foo::value).toLazyList();
      assertAll(
          () -> assertEquals(5, lazyList.size()),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> lazyList.get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> lazyList.get(5))
      );
    }

    @Test
    public void nonModifiableLazyList() {
      record Foo(Optional<Integer> count) {}

      var fooList = range(0, 10).mapToObj(i -> new Foo(Optional.of(i))).toList();
      var lazyList = Query.fromList(fooList, Foo::count).toLazyList();
      assertAll(
          () -> assertEquals(10, lazyList.size()),
          () -> assertThrows(UnsupportedOperationException.class, () -> lazyList.add(3)),
          () -> assertThrows(UnsupportedOperationException.class, () -> lazyList.addAll(List.of(3, 4))),
          () -> assertThrows(UnsupportedOperationException.class, () -> lazyList.remove(3)),
          () -> assertThrows(UnsupportedOperationException.class, () -> lazyList.removeAll(List.of(0))),
          () -> assertThrows(UnsupportedOperationException.class, lazyList::clear)
      );
    }
  }


//  @Nested
//  class Q5 {
//    @Test
//    public void queryFromIterableOfIntegers() {
//      var list = List.of(1, 2, 4, 8);
//      var query = Query.fromIterable(list);
//
//      assertAll(
//          () -> assertEquals(List.of(1, 2, 4, 8), query.toList()),
//          () -> assertEquals(1, query.toStream().findFirst().orElseThrow())
//      );
//    }
//
//    @Test
//    public void queryFromIterableOfStrings() {
//      List<String> list = List.of("foo", "bar", "baz");
//      Query<String> query = Query.fromIterable(list);
//
//      assertAll(
//          () -> assertEquals(List.of("foo", "bar", "baz"), query.toList()),
//          () -> assertEquals("foo", query.toStream().findFirst().orElseThrow())
//      );
//    }
//
//    @Test
//    public void queryFromSetOfStrings() {
//      var set = Set.of("foo", "bar", "baz");
//      var query = Query.fromIterable(set);
//
//      assertAll(
//          () -> assertEquals(Set.of("foo", "bar", "baz"), new HashSet<>(query.toList())),
//          () -> assertTrue(query.toStream().findFirst().isPresent())
//      );
//    }
//
//    @Test
//    public void iterable() {
//      Iterable<Integer> iterable = List.of(12, 34)::iterator;
//      Query<Object> query = Query.fromIterable(iterable);
//      assertEquals(List.of(12, 34), query.toList());
//    }
//
//    @Test
//    public void queryFromOfPath() {
//      assertNotNull(Query.fromIterable(Path.of(".")));
//    }
//
//    @Test
//    public void signature() {
//      assertEquals(List.of(3), Query.<Object>fromIterable(List.of(3)).toList());
//    }
//
//    @Test
//    public void preconditions() {
//      assertAll(
//          () -> assertThrows(NullPointerException.class, () -> Query.fromIterable(null)),
//          () -> assertThrows(UnsupportedOperationException.class, () -> Query.fromIterable(List.of(1)).toList().add(2)),
//          () -> assertThrows(UnsupportedOperationException.class, () -> Query.fromIterable(List.of(1)).toList().remove(3))
//      );
//    }
//  }
//
//
//  @Nested
//  class Q6 {
//    @Test
//    public void queryFilterInfo() {
//      record Info(Optional<String> name) {}
//
//      var list = List.of(
//          new Info(Optional.of("Ana")),
//          new Info(Optional.empty()),
//          new Info(Optional.of("Bob")),
//          new Info(Optional.of("Bernard")));
//      var query = Query.fromList(list, Info::name).filter(name -> name.startsWith("B"));
//
//      assertAll(
//          () -> assertEquals(List.of("Bob", "Bernard"), query.toList()),
//          () -> assertEquals("Bob", query.toStream().findFirst().orElseThrow())
//      );
//    }
//
//    @Test
//    public void queryFilterIntegers() {
//      var query = Query.fromIterable(List.of(1, 2, 4, 8)).filter(x -> x % 2 == 0);
//
//      assertAll(
//          () -> assertEquals(List.of(2, 4, 8), query.toList()),
//          () -> assertEquals(2, query.toStream().findFirst().orElseThrow())
//      );
//    }
//
//    @Test
//    public void queryFilterStrings() {
//      var query = Query.fromIterable(List.of("foo", "bar", "baz")).filter(s -> s.endsWith("oo"));
//
//      assertAll(
//          () -> assertEquals(List.of("foo"), query.toList()),
//          () -> assertEquals("foo", query.toStream().findFirst().orElseThrow())
//      );
//    }
//
//    @Test
//    public void queryFilterFilterIntegers() {
//      var query = Query.fromIterable(List.of(1, 3, 6, 8))
//          .filter(x -> x % 3 == 0)
//          .filter(x -> x % 2 == 0);
//
//      assertAll(
//          () -> assertEquals(List.of(6), query.toList()),
//          () -> assertEquals(6, query.toStream().findFirst().orElseThrow())
//      );
//    }
//
//    @Test
//    public void notTooManyCallsToTransform() {
//      var counter = new Object() { int count; };
//      var query = Query.fromList(List.of(1, 2, 3, 4, 5), value -> {
//        counter.count++;
//        return Optional.of(value);
//      }).filter(x -> x % 2 == 0);
//      assertEquals(0, counter.count);
//      assertEquals(List.of(2, 4), query.toList());
//      assertEquals(5, counter.count);
//    }
//
//    @Test
//    public void notTooManyCallsToFilter() {
//      var counter = new Object() { int count; };
//      var query =
//          Query.fromList(List.of(1, 2, 3, 4, 5), x -> Optional.of(x).filter(v -> v % 2 == 0))
//               .filter(x -> {
//                 counter.count++;
//                 return true;
//               });
//      assertEquals(0, counter.count);
//      assertEquals(List.of(2, 4), query.toList());
//      assertEquals(2, counter.count);
//    }
//
//    @Test
//    public void signature() {
//      assertEquals(List.of(3), Query.fromIterable(List.of(3)).filter((Object o) -> true).toList());
//    }
//
//    @Test
//    public void preconditions() {
//      assertThrows(NullPointerException.class, () -> Query.fromIterable(List.of(1, 2, 3)).filter(null));
//    }
//  }
//
//
//  @Nested
//  class Q7 {
//    @Test
//    public void queryMapInfo() {
//      record Info(Optional<String> name) {}
//
//      var list = List.of(
//          new Info(Optional.of("Ana")),
//          new Info(Optional.of("Bob")),
//          new Info(Optional.empty()),
//          new Info(Optional.of("Bernard")));
//      var query = Query.fromList(list, Info::name).map(name -> "*" + name + "*");
//
//      assertAll(
//          () -> assertEquals(List.of("*Ana*", "*Bob*", "*Bernard*"), query.toList()),
//          () -> assertEquals("*Ana*", query.toStream().findFirst().orElseThrow())
//      );
//    }
//
//    @Test
//    public void queryMapIntegers() {
//      var query = Query.fromIterable(List.of(1, 2, 4, 8)).map(x -> 2 * x);
//
//      assertAll(
//          () -> assertEquals(List.of(2, 4, 8, 16), query.toList()),
//          () -> assertEquals(2, query.toStream().findFirst().orElseThrow())
//      );
//    }
//
//    @Test
//    public void queryMapStringsToIntegers() {
//      var query = Query.fromIterable(List.of("23", "-12", "42")).map(Integer::parseInt);
//
//      assertAll(
//          () -> assertEquals(List.of(23, -12, 42), query.toList()),
//          () -> assertEquals(23, query.toStream().findFirst().orElseThrow())
//      );
//    }
//
//    @Test
//    public void queryMapMap() {
//      var query = Query.fromIterable(List.of(1, 2, 4, 8))
//          .map(x -> 2 * x)
//          .map(Object::toString);
//
//      assertAll(
//          () -> assertEquals(List.of("2", "4", "8", "16"), query.toList()),
//          () -> assertEquals("2", query.toStream().findFirst().orElseThrow())
//      );
//    }
//
//    @Test
//    public void notTooManyCallsToTransform() {
//      var counter = new Object() { int count; };
//      var query = Query.fromList(List.of(1, 2, 3, 4, 5), value -> {
//        counter.count++;
//        return Optional.of(value);
//      }).map(x -> x * 2);
//      assertEquals(0, counter.count);
//      assertEquals(List.of(2, 4, 6, 8, 10), query.toList());
//      assertEquals(5, counter.count);
//    }
//
//    @Test
//    public void notTooManyCallsToMap() {
//      var counter = new Object() { int count; };
//      var query =
//          Query.fromList(List.of(1, 2, 3, 4, 5), x -> Optional.of(x).filter(v -> v % 2 == 0))
//              .map(x -> {
//                counter.count++;
//                return x * 2;
//              });
//      assertEquals(0, counter.count);
//      assertEquals(List.of(4, 8), query.toList());
//      assertEquals(2, counter.count);
//    }
//
//    @Test
//    public void signature() {
//      assertEquals(List.of("3"), Query.fromIterable(List.of(3)).map((Object o) -> "" + o).toList());
//    }
//
//    @Test
//    public void preconditions() {
//      assertThrows(NullPointerException.class, () -> Query.fromIterable(List.of(1, 2, 3)).map(null));
//    }
//  }
//
//  @Nested
//  class Q8 {
//
//    @Test
//    public void queryReduceIntegerSum() {
//      var sum = Query.fromIterable(List.of(1, 2, 4, 8))
//          .reduce(0, Integer::sum);
//
//      assertEquals(15, sum);
//    }
//
//    @Test
//    public void queryReduceStringConcat() {
//      var text = Query.fromIterable(List.of("foo", "bar", "baz"))
//          .reduce("", String::concat);
//
//      assertEquals("foobarbaz", text);
//    }
//
//    @Test
//    public void queryEmpty() {
//      var result = Query.fromIterable(List.<String>of())
//          .reduce(null, String::concat);
//
//      assertNull(result);
//    }
//
//    @Test
//    public void queryReduceInfoLength() {
//      record Info(Optional<String> name) {}
//
//      var list = List.of(
//          new Info(Optional.empty()),
//          new Info(Optional.of("Ana")),
//          new Info(Optional.of("Bob")),
//          new Info(Optional.of("Bernard")),
//          new Info(Optional.empty()));
//      var result = Query.fromList(list, Info::name)
//          .reduce(0, (acc, s) -> acc + s.length());
//
//      assertEquals(13, result);
//    }
//
//    @Test
//    public void signature() {
//      var result = Query.fromIterable(List.of("foo", "bar", "baz"))
//          .reduce(0, (Object acc, CharSequence value) -> value.length() + (int) acc);
//
//      assertEquals(9, result);
//    }
//
//    @Test
//    public void preconditions() {
//      var query = Query.fromIterable(List.<Integer>of());
//      assertThrows(NullPointerException.class, () -> query.reduce(0, null));
//    }
//  }
}