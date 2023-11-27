package fr.uge.json;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("static-method")
public class JSONPrinterTest {
  // Use jackson to parse a JSON Object as a java.util.Map (keeping the order)
  private static Map<String, Object> parseMap(String input) {
    return parse(input, new com.fasterxml.jackson.core.type.TypeReference<LinkedHashMap<String, Object>>() {});
  }

  // Use jackson to parse a JSON object/list to the type of the type reference
  private static <T> T parse(String input, com.fasterxml.jackson.core.type.TypeReference<T> typeReference) {
    var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
    try {
      return mapper.readValue(input, typeReference);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new IllegalStateException(e.getMessage() + "\n while parsing " + input, e);
    }
  }

  @Nested
  public class Q1 {
    @Test
    public void testInvoke() throws NoSuchMethodException {
      class Foo {
        public int x() {
          return 3;
        }
      }

      var getter = Foo.class.getMethod("x");
      assertEquals(3, JSONPrinter.invoke(getter, new Foo()));
    }

    @Test
    public void testInvokeShouldPropagateRuntimeException() throws NoSuchMethodException {
      class Foo {
        String s;

        public int x() {
          s.toString();  // should throw a NPE
          return 3;
        }
      }

      var getter = Foo.class.getMethod("x");
      assertThrows(NullPointerException.class, () -> JSONPrinter.invoke(getter, new Foo()));
    }

    @Test
    public void testInvokeShouldPropagateError() throws NoSuchMethodException {
      class Foo {
        String s;

        public int x() {
          return x();  // call itself
        }
      }

      var getter = Foo.class.getMethod("x");
      assertThrows(StackOverflowError.class, () -> JSONPrinter.invoke(getter, new Foo()));
    }

    @Test
    public void testInvokeShouldWrapException() throws NoSuchMethodException {
      class Foo {
        String s;

        public int x() throws IOException {
          throw new IOException();
        }
      }

      var getter = Foo.class.getMethod("x");
      assertThrows(UndeclaredThrowableException.class, () -> JSONPrinter.invoke(getter, new Foo()));
    }

    @Test
    public void testInvokeIllegalAccessShouldPropagateAnError() {
      var privateMethod = Arrays.stream(String.class.getDeclaredMethods())
          .filter(m -> m.getParameterCount() == 0)
          .filter(m -> !m.accessFlags().contains(AccessFlag.STATIC))
          .filter(m -> m.accessFlags().contains(AccessFlag.PRIVATE))
          .findFirst()
          .orElseThrow();

      assertThrows(IllegalAccessError.class, () -> JSONPrinter.invoke(privateMethod, "hello"));
    }

    @Test
    public void testInvokeQualityOfImplementation() throws NoSuchMethodException {
      var methods = Arrays.stream(JSONPrinter.class.getDeclaredMethods())
          .filter(m -> m.getName().equals("invoke"))
          .toList();
      assertEquals(1, methods.size());
      var method = methods.getFirst();
      assertFalse(method.accessFlags().contains(AccessFlag.PUBLIC));
    }
  }


  @Nested
  public class Q2 {
    @Test
    public void testToJSONPersonPartial() {
      var person = new Person("John", "Doe");
      var personJSON = JSONPrinter.toJSON(person);
      var map = parseMap(personJSON);
      assertEquals("John", map.get("firstName"));
      assertEquals("Doe", map.get("lastName"));
    }
    @Test
    public void testToJSONPerson() {
      var person = new Person("John", "Doe");
      var personJSON = JSONPrinter.toJSON(person);
      assertEquals(
          Map.of("firstName", "John", "lastName", "Doe"), parseMap(personJSON));
    }
    @Test
    public void testToJSONPersonOrder() {
      var person = new Person("John", "Doe");
      var personJSON = JSONPrinter.toJSON(person);
      assertEquals(
          new LinkedHashMap<>() {{ put("firstName", "John"); put("lastName", "Doe"); }},
          parseMap(personJSON));
    }

    @Test
    public void testToJSONAlienPartial() {
      var alien = new Alien(100, "Jupiter");
      var alienJSON = JSONPrinter.toJSON(alien);
      var map = parseMap(alienJSON);
      assertEquals("Jupiter", map.get("planet"));
      assertEquals(100, map.get("age"));
    }
    @Test
    public void testToJSONAlien() {
      var alien = new Alien(80, "Mars");
      var alienJSON = JSONPrinter.toJSON(alien);
      assertEquals(Map.of("age", 80, "planet", "Mars"), parseMap(alienJSON));
    }
    @Test
    public void testToJSONAlienOrder() {
      var alien = new Alien(112, "Saturn");
      var alienJSON = JSONPrinter.toJSON(alien);
      assertEquals(
          new LinkedHashMap<>() {{
            put("age", 112);
            put("planet", "Saturn");
          }},
          parseMap(alienJSON));
    }

    @Test
    public void testJsonQualityOfImplementation() throws NoSuchMethodException {
      var method = JSONPrinter.class.getMethod("toJSON", Record.class);
      assertTrue(method.accessFlags().contains(AccessFlag.PUBLIC));
    }

    @Test
    public void testToJSONPrecondition() {
      assertThrows(NullPointerException.class, () -> JSONPrinter.toJSON((Record) null));
    }
  }


  @Nested
  public class Q3 {

    @Test
    public void testToJSONPropertyAnnotation() {
      record Person(@JSONProperty("firstName") String name, int age) { }

      var ana = new Person("Ana", 24);
      var bob = new Person("Bob", 21);
      assertAll(
          () -> assertEquals(
              Map.of("firstName", "Ana", "age", 24),
              parseMap(JSONPrinter.toJSON(ana))),
          () -> assertEquals(
              Map.of("firstName", "Bob", "age", 21),
              parseMap(JSONPrinter.toJSON(bob)))
      );
    }

    @Test
    public void testToJSONPropertyAnnotationOrder() {
      record Person(@JSONProperty("firstName") String name, int age) { }

      var john = new Person("John", 42);
      var jane = new Person("Jane", 41);
      assertAll(
          () -> assertEquals(
              new LinkedHashMap<>() {{ put("firstName", "John"); put( "age", 42); }},
              parseMap(JSONPrinter.toJSON(john))),
          () -> assertEquals(
              new LinkedHashMap<>() {{ put("firstName", "Jane"); put( "age", 41); }},
              parseMap(JSONPrinter.toJSON(jane)))
      );
    }

    @Test
    public void testToJSONPropertyWithAName() {
      record Book(@JSONProperty("book-title") String title,
                  @JSONProperty("book-author") String author,
                  @JSONProperty("book-price") int price) { }

      var book = new Book("The Girl with The Dragon Tattoo", "Stieg Larsson", 100);
      var bookJSON = JSONPrinter.toJSON(book);
      assertEquals(
          Map.of("book-title", "The Girl with The Dragon Tattoo", "book-author", "Stieg Larsson", "book-price", 100),
          parseMap(bookJSON));
    }

    @Test
    public void testToJSONPropertyWithANameOrder() {
      record Book(@JSONProperty("book-title") String title,
                  @JSONProperty("book-author") String author,
                  @JSONProperty("book-price") int price) { }

      var book = new Book("Dune", "Frank Herbert", 20);
      var bookJSON = JSONPrinter.toJSON(book);
      assertEquals(
          new LinkedHashMap<>() {{ put("book-title", "Dune"); put("book-author", "Frank Herbert"); put("book-price", 20); }},
          parseMap(bookJSON));
    }
  }


  @Nested
  public class Q4 {

    @Test
    public void testToJSONPointList() {
      record Point(int x, int y) { }

      var points = IntStream.range(0, 1_000)
          .mapToObj(i -> new Point(i, i))
          .toList();
      var json = JSONPrinter.toJSON(points);

      assertEquals(points,
          parse(json, new com.fasterxml.jackson.core.type.TypeReference<List<Point>>() {}));
    }

    @Test
    public void testToJSONPointListPropertyAnnotation() {
      record Point(@JSONProperty("coord-x") @com.fasterxml.jackson.annotation.JsonProperty("coord-x") int x,
                   @JSONProperty("coord-y") @com.fasterxml.jackson.annotation.JsonProperty("coord-y") int y) { }

      var points = IntStream.range(0, 1_000)
          .mapToObj(i -> new Point(i, i))
          .toList();
      var json = JSONPrinter.toJSON(points);

      assertEquals(points,
          parse(json, new com.fasterxml.jackson.core.type.TypeReference<List<Point>>() {}));
    }

    @Test
    public void testJsonListQualityOfImplementation() throws NoSuchMethodException {
      var method = JSONPrinter.class.getMethod("toJSON", List.class);
      assertTrue(method.accessFlags().contains(AccessFlag.PUBLIC));
    }
  }
}
