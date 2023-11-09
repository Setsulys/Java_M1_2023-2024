package fr.umlv.template;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import fr.umlv.template.Document.Template;

public class DocumentTest {
  @Nested
  public class Q1 {

    @Test
    public void newTemplate() {
      Document.Template template = new Document.Template(List.of("hello ", " template"));
      assertEquals(List.of("hello ", " template"), template.fragments());
    }

    @Test
    public void templateOneFragment() {
      var template = new Template(List.of("hello"));
      assertEquals(List.of("hello"), template.fragments());
    }

    @Test
    public void templateConcat() {
      var template = new Template(List.of("hello ", " template"));
      assertEquals("hello @ template", "" + template);
    }

    @Test
    public void templateConcatOnFragment() {
      var template = new Template(List.of("hello"));
      assertEquals("hello", "" + template);
    }

    @Test
    public void templateNotModifiable() {
      var list = new ArrayList<>(List.of("A", "B", "C"));
      var template = new Template(list);
      list.add("D");
      assertAll(
          () -> assertEquals(List.of("A", "B", "C"), template.fragments()),
          () -> assertEquals("A@B@C", "" + template)
      );
    }

    @Test
    public void templateIsPublic() {
      assertTrue(Modifier.isPublic(Template.class.getModifiers()));
    }

    @Test
    public void preconditions() {
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> new Template(null)),
          () -> assertThrows(IllegalArgumentException.class, () -> new Template(List.of())),
          () -> assertThrows(NullPointerException.class, () -> new Template(Arrays.asList("hello", null))),
          () -> assertThrows(NullPointerException.class, () -> new Template(Arrays.asList(null, "template"))),
          () -> assertThrows(UnsupportedOperationException.class, () -> new Template(List.of("foo")).fragments().add("bar"))
      );
    }
  }


  @Nested
  public class Q2 {

    @Test
    public void interpolate() {
      var template = new Template(List.of("| ", " | ", " | ", " |"));
      assertEquals("| 1 | 2 | 3 |", template.interpolate(List.of(1, 2, 3)));
    }

    @Test
    public void interpolateString() {
      var template = new Template(List.of("[", ", ", ", ", "]"));
      assertEquals("[1, 2, 3]", template.interpolate(List.of("1", "2", "3")));
    }

    @Test
    public void interpolateWithMonkeyAt() {
      var template = new Template(List.of("", ">@"));
      assertEquals("@>@", template.interpolate(List.of("@")));
    }

    @Test
    public void valueCanBeNull() {
      var template = new Template(List.of("<", "-", ">"));
      assertAll(
          () -> assertEquals("<null-777>", template.interpolate(Arrays.asList(null, 777))),
          () -> assertEquals("<66-null>", template.interpolate(Arrays.asList(66, null)))
      );
    }

//    @Test
//    @Timeout(2)
//    public void interpolateALot() {
//      var fragments = IntStream.range(0, 100_001).mapToObj(__ -> "").toList();
//      var template = new Template(fragments);
//      var list = IntStream.range(0, 100_000).boxed().collect(toCollection(java.util.LinkedList::new));
//      var text = template.interpolate(list);
//      assertEquals(488_890, text.length());
//    }

    @Test
    public void interpolateIsPublic() throws NoSuchMethodException {
      var interpolate = Template.class.getMethod("interpolate", List.class);
      assertTrue(Modifier.isPublic(interpolate.getModifiers()));
    }

    @Test
    public void preconditions() {
      var template = new Template(List.of("name = ", ""));
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> template.interpolate(null)),
          () -> assertThrows(IllegalArgumentException.class, () -> template.interpolate(List.of(1, 2))),
          () -> assertThrows(IllegalArgumentException.class, () -> template.interpolate(List.of())),
          () -> assertThrows(IllegalArgumentException.class, () -> template.interpolate(List.of(1, 2, 3)))
      );
    }
  }


  @Nested
  public class Q3 {

    @Test
    public void templateOf() {
      var template = Template.of("(@-@)");
      assertAll(
          () -> assertEquals(List.of("(", "-", ")"), template.fragments()),
          () -> assertEquals("(@-@)", "" + template),
          () -> assertEquals("(7-42)", template.interpolate(List.of(7, 42)))
      );
    }

    @Test
    public void templateOf2() {
      var template = Template.of("< @ | @ >");
      assertAll(
          () -> assertEquals(List.of("< ", " | ", " >"), template.fragments()),
          () -> assertEquals("< @ | @ >", "" + template),
          () -> assertEquals("< 1 | 2 >", template.interpolate(List.of(1, 2)))
      );
    }

    @Test
    public void templateOneFragment() {
      var template = Template.of("hello");
      assertEquals(List.of("hello"), template.fragments());
    }

    @Test
    public void templateTwoFragments() {
      var template = Template.of("name = @");
      assertEquals(List.of("name = ", ""), template.fragments());
    }

    @Test
    public void templateThreeFragments() {
      var template = Template.of("@@");
      assertEquals(List.of("", "", ""), template.fragments());
    }

    @Test
    public void templateOfIsPublic() throws NoSuchMethodException {
      var of = Template.class.getMethod("of", String.class);
      assertTrue(Modifier.isPublic(of.getModifiers()));
    }

    @Test
    public void preconditions() {
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> Template.of(null)),
          () -> assertThrows(UnsupportedOperationException.class, () -> new Template(List.of("foo")).fragments().add("bar"))
      );
    }
  }


  @Nested
  public class Q4 {

    @Test
    public void document() {
      record Point(int x, int y) {}

      Document.Template template = new Template(List.of("< ", " | ", " >"));
      Document<Point> document = new Document<Point>(template, List.of(Point::x, Point::y));
      assertNotNull(document);
    }

    @Test
    public void documentPerson() {
      record Person(String name, int age) {}

      var template = new Template(List.of("< ", " | ", " >"));
      var document = new Document<Person>(template, List.of(Person::name, Person::age));
      assertNotNull(document);
    }

    @Test
    public void documentPerson2() {
      record Person(String name, int age) {}

      var template = new Template(List.of("< ", " | ", " >"));
      var document = new Document<Person>(template, List.of(Person::name, Person::age));
      assertNotNull(document);
    }

    @Test
    public void documentToString() {
      record Person(String name, int age) {}

      var template = new Template(List.of("hello ", " !"));
      var document = new Document<Person>(template, List.of(Object::toString));
      assertNotNull(document);
    }

    @Test
    public void documentEmpty() {
      var template = new Template(List.of("text"));
      var document = new Document<>(template, List.of());
      assertNotNull(document);
    }

    @Test
    public void documentSignature() {
      record Person(Person significantOther) {}

      var template = new Template(List.of("significant other ", " !"));
      var functions = List.<UnaryOperator<Person>>of(Person::significantOther);
      var document = new Document<>(template, functions);
      assertNotNull(document);
    }

    @Test
    public void documentSignature2() {
      record Pet(String name) {}

      var template = new Template(List.of("part1 ", " part2"));
      var functions = List.of(UnaryOperator.identity());
      var document = new Document<Pet>(template, functions);
      assertNotNull(document);
    }

    @Test
    public void documentIsPublic() {
      assertTrue(
          Arrays.stream(Document.class.getConstructors())
              .allMatch(c -> Modifier.isPublic(c.getModifiers())));
    }

    @Test
    public void preconditions() {
      record Point(int x, int y, int z) {}

      var template = new Template(List.of("< ", " | ", " >"));
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> new Document<>(template, null)),
          () -> assertThrows(IllegalArgumentException.class, () -> new Document<>(template, List.of())),
          () -> assertThrows(IllegalArgumentException.class, () -> new Document<Point>(template, List.of(Point::x))),
          () -> assertThrows(IllegalArgumentException.class, () -> new Document<Point>(template, List.of(Point::x, Point::y, Point::z)))
      );
    }
  }


  @Nested
  public class Q5 {

    @Test
    public void toDocument() {
      record Point(int x, int y) {}

      Document.Template template = new Template(List.of("< ", " | ", " >"));
      Document<Point> document = template.toDocument(Point::x, Point::y);
      assertNotNull(document);
    }

    @Test
    public void toDocumentPerson() {
      record Person(String name, int age) {}

      var template = new Template(List.of("< ", " | ", " >"));
      var document = template.toDocument(Person::name, Person::age);
      assertNotNull(document);
    }

    @Test
    public void toDocumentPerson2() {
      record Person(String name, int age) {}

      var template = new Template(List.of("< ", " | ", " >"));
      var document = template.<Person>toDocument(Person::name, Person::age);
      assertNotNull(document);
    }

    @Test
    public void toDocumentToString() {
      record Person(String name, int age) {}

      var template = new Template(List.of("hello ", " !"));
      var document = template.<Person>toDocument(Object::toString);
      assertNotNull(document);
    }

    @Test
    public void toDocumentEmpty() {
      var template = new Template(List.of("text"));
      var document = template.toDocument();
      assertNotNull(document);
    }

    @Test
    public void toDocumentSignature() {
      record Person(Person significantOther) {}

      var template = new Template(List.of("significant other ", " !"));
      var function = List.<UnaryOperator<Person>>of(Person::significantOther).get(0);
      var document = template.<Person>toDocument(function);
      assertNotNull(document);
    }

    @Test
    public void toDocumentSignature2() {
      record Pet(String name) {}

      var template = new Template(List.of("part1 ", " part2"));
      var function = List.of(UnaryOperator.identity()).get(0);
      var document = template.toDocument(function);
      assertNotNull(document);
    }

    @Test
    public void toDocumentIsPublic() {
      assertTrue(
          Arrays.stream(Template.class.getMethods())
              .filter(m -> m.getName().equals("toDocument"))
              .allMatch(m -> Modifier.isPublic(m.getModifiers())));
    }

    @Test
    public void preconditions() {
      record Point(int x, int y, int z) {}

      var template = new Template(List.of("< ", " | ", " >"));
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> template.toDocument(null)),
          () -> assertThrows(IllegalArgumentException.class, template::toDocument),
          () -> assertThrows(IllegalArgumentException.class, () -> template.toDocument(Point::x)),
          () -> assertThrows(IllegalArgumentException.class, () -> template.toDocument(Point::x, Point::y, Point::z))
      );
    }
  }


  @Nested
  public class Q6 {

    @Test
    public void applyTemplate() {
      record Point(int x, int y) {}

      Document.Template template = new Template(List.of("<", "-", ">"));
      Document<Point> document = new Document<>(template, List.of(Point::x, Point::y));
      String text = document.applyTemplate(new Point(7, 42));
      assertEquals("<7-42>", text);
    }

    @Test
    public void applyTemplateAllowNull() {
      record Person(String name) {}

      var template = new Template(List.of("<", "-", ">"));
      var document = new Document<Person>(template, List.of(Person::name, Person::name));
      var text = document.applyTemplate(new Person(null));
      assertEquals("<null-null>", text);
    }

    @Test
    public void applyTemplateNotVisible() {
      assertTrue(
          Arrays.stream(Document.class.getMethods())
              .noneMatch(m -> m.getName().equals("applyTemplate")));
    }

    @Test
    public void precondition() {
      record Point(int x, int y, int z) {}

      var template = new Template(List.of("empty"));
      var document = new Document<>(template, List.of());
      assertThrows(NullPointerException.class, () -> document.applyTemplate(null));
    }
  }


//  @Nested
//  public class Q7 {
//
//    @Test
//    public void generate() {
//      record Point(int x, int y) { }
//
//      List<Point> points =  List.of(new Point(7, 42), new Point(13, 752));
//      Document.Template template = new Template(List.of("(", "-", ")"));
//      Document<Point> document = new Document<>(template, List.of(Point::x, Point::y));
//      String text = document.generate(points, "|");
//      assertEquals("(7-42)|(13-752)", text);
//    }
//
//    @Test
//    public void generateEmployee() {
//      record Employee(String name, int age, boolean manager) { }
//
//      List<Employee> employees =  List.of(
//          new Employee("Ana", 35, true),
//          new Employee("Bob", 31, false),
//          new Employee("Kate", 23, false));
//      var template = new Template(List.of("| ", " | ", " |"));
//      var document = new Document<Employee>(template, List.of(Employee::name, Employee::manager));
//      var text = document.generate(employees, "\n");
//      assertEquals("""
//          | Ana | true |
//          | Bob | false |
//          | Kate | false |\
//          """, text);
//    }
//
//    @Test
//    public void generateEmpty() {
//      record Point(int x, int y) { }
//
//      var template = new Template(List.of("<", "-", ">"));
//      var document = new Document<Point>(template, List.of(Point::x, Point::y));
//      var text = document.generate(List.of(), "");
//      assertEquals("", text);
//    }
//
//    @Test
//    @Timeout(2)
//    public void generateALot() {
//      record Id(int id) { }
//
//      var template = new Template(List.of("[", "]"));
//      var document = new Document<Id>(template, List.of(Id::id));
//      var list = IntStream.range(0, 100_000).mapToObj(Id::new).collect(toCollection(java.util.LinkedList::new));
//      var text = document.generate(list, "");
//      assertEquals(688_890, text.length());
//    }
//
//    @Test
//    public void generateSignature() {
//      record Dog(String name) {
//        @Override
//        public String toString() {
//          return name;
//        }
//      }
//
//      var template = new Template(List.of("(", ")"));
//      var document = new Document<Record>(template, List.of(Object::toString));
//      var instances = List.of(new Dog("Iggy Pop"));
//      var text = document.generate(instances, "");
//      assertEquals("(Iggy Pop)", text);
//    }
//
//    @Test
//    public void generateIsPublic() {
//      assertTrue(
//          Arrays.stream(Document.class.getMethods())
//              .filter(m -> m.getName().equals("generate"))
//              .allMatch(m -> Modifier.isPublic(m.getModifiers())));
//    }
//
//    @Test
//    public void preconditions() {
//      record Empty() {}
//
//      var template = new Template(List.of("empty"));
//      var document = new Document<Empty>(template, List.of());
//      assertAll(
//          () -> assertThrows(NullPointerException.class, () -> document.generate(null, "")),
//          () -> assertThrows(NullPointerException.class, () -> document.generate(List.of(), null)),
//          () -> assertThrows(NullPointerException.class, () -> document.generate(Arrays.asList(null, new Empty()), "")),
//          () -> assertThrows(NullPointerException.class, () -> document.generate(Arrays.asList(new Empty(), null), ""))
//      );
//    }
//  }
//
//  @Nested
//  public class Q8 {
//
//    @Test
//    public void bind() {
//      Document.Template template = new Template(List.of("Hello, ", ". ", "."));
//      Document.Template template2 = template.bind("Mr");
//      assertEquals("Hello, Mr. Swackhammer.", template2.interpolate(List.of("Swackhammer")));
//    }
//
//    @Test
//    public void bindWithAnInteger() {
//     var template = new Template(List.of("a", "b", "c"));
//     var template2 = template.bind(1);
//     assertAll(
//         () -> assertEquals(List.of("a1b", "c"), template2.fragments()),
//         () -> assertEquals("a1b2c", template2.interpolate(List.of(2)))
//     );
//    }
//
//    @Test
//    public void bindWithMonkeyAt() {
//      var template = new Template(List.of("a", "b", "c"));
//      var template2 = template.bind('@');
//      assertAll(
//          () -> assertEquals(List.of("a@b", "c"), template2.fragments()),
//          () -> assertEquals("a@b@c", template2.interpolate(List.of("@")))
//      );
//    }
//
//    @Test
//    public void bindAllowNull() {
//      var nullValue = (Object) null;
//      var template = new Template(List.of("<", "-", ">"));
//      var template2 = template.bind(nullValue);
//      assertAll(
//          () -> assertEquals(List.of("<null-", ">"), template2.fragments()),
//          () -> assertEquals("<null-null>", template2.interpolate(singletonList(nullValue)))
//      );
//    }
//
//    @Test
//    public void bindTwice() {
//      var template = new Template(List.of("<", "-", ">"));
//      var template2 = template.bind(747).bind(5.2);
//      assertAll(
//          () -> assertEquals(List.of("<747-5.2>"), template2.fragments()),
//          () -> assertEquals("<747-5.2>", template2.interpolate(List.of()))
//      );
//    }
//
//    @Test
//    public void bindIsPublic() {
//      assertTrue(
//          Arrays.stream(Template.class.getMethods())
//              .filter(m -> m.getName().equals("bind"))
//              .allMatch(m -> Modifier.isPublic(m.getModifiers())));
//    }
//  }
//
//
//  @Nested
//  public class Q9 {
//    @Test
//    public void bindSeveralValues() {
//      Document.Template template = new Template(List.of("Hello ", " ", ""));
//      Document.Template template2 = template.bind("Mr", "Wolff");
//      assertEquals("Hello Mr Wolff", template2.interpolate(List.of()));
//    }
//
//    @Test
//    public void bindSeveralValuesNullAllowed() {
//      var template = new Template(List.of("Hello ", " ", ""));
//      var template2 = template.bind(null, "Wolff");
//      assertEquals("Hello null Wolff", template2.interpolate(List.of()));
//    }
//
//    @Test
//    public void precondition() {
//      var arrayNull = (Object[]) null;
//      var template = new Template(List.of("empty"));
//      assertThrows(NullPointerException.class, () -> template.bind(arrayNull));
//    }
//  }
}