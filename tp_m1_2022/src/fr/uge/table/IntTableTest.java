package fr.uge.table;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class IntTableTest {
  @Nested
  public class Q1 {
    @Test
    public void createEmpty() {
      IntTable table = new IntTable();
      assertEquals(0, table.size());
    }

    @Test
    public void createAndSet() {
      var table = new IntTable();
      table.set("foo", 1);
      table.set("bar", 2);
      table.set("baz", 42);
      assertEquals(3, table.size());
    }

    @Test
    public void setWithSameName() {
      var table = new IntTable();
      table.set("foo", 1);
      table.set("bar", 2);
      table.set("foo", 10);
      assertEquals(2, table.size());
    }

    @Test
    public void storage() throws NoSuchFieldException, IllegalAccessException {
      var table = new IntTable();
      for(var field: IntTable.class.getDeclaredFields()) {
        field.setAccessible(true);
        var value = field.get(table);
        if (value.getClass().getName().equals("fr.uge.table.IntTable$MapImpl")) {
          return;
        }
      }
      fail("no storage field");
    }

    @Test
    public void fieldEncapsulation() {
      for(var field: IntTable.class.getDeclaredFields()) {
        var modifiers = field.getModifiers();
        assertTrue(Modifier.isPrivate(modifiers));
        assertTrue(Modifier.isFinal(modifiers));
      }
    }
    @Test
    public void classMemberEncapsulation() {
      for(var clazz: IntTable.class.getDeclaredClasses()) {
        var modifiers = clazz.getModifiers();
        assertFalse(Modifier.isPublic(modifiers));
        assertTrue(Modifier.isAbstract(modifiers) || Modifier.isFinal(modifiers));
        assertTrue(Modifier.isStatic(modifiers));
      }
    }
    @Test
    public void ensureNoInheritance() {
      var modifiers = IntTable.class.getModifiers();
      assertTrue(Modifier.isFinal(modifiers));
    }
    @Test
    public void ensureReusable() {
      var modifiers = IntTable.class.getModifiers();
      assertTrue(Modifier.isPublic(modifiers));
    }

    @Test
    public void precondition() {
      var table = new IntTable();
      assertThrows(NullPointerException.class, () -> table.set(null, 3));
    }
  }


  @Nested
  public class Q2 {

    @Test
    public void createSetAndGet() {
      var table = new IntTable();
      table.set("foo", 1);
      table.set("bar", 2);
      assertAll(
          () -> assertEquals(1, table.get("foo", 0)),
          () -> assertEquals(-1, table.get("jhksdfkjhsffhjksqk", -1))
      );
    }

    @Test
    public void setAndGetALot() {
      var table = new IntTable();
      new Random(0)
          .ints(1_000_000, 0, 1_000)
          .forEach(i -> table.set(i < 500 ? "x": "y", i));
      assertAll(
          () -> assertEquals(80, table.get("x", 0)),
          () -> assertEquals(685, table.get("y", 0))
      );
    }

    @Test
    public void setAndGetALot2() {
      var table = new IntTable();
      IntStream.range(0, 1_000_000).forEach(i -> table.set("" + i, i));
      assertEquals(1_000_000, table.size());
      for(var i = 0; i < table.size(); i++) {
        assertEquals(i, table.get("" + i, -1));
      }
    }

    @Test
    public void precondition() {
      var table = new IntTable();
      assertThrows(NullPointerException.class, () -> table.get(null, 0));
    }
  }


  @Nested
  public class Q3 {

    @Test
    public void apply() {
      var table = new IntTable();
      table.set("foo", 1);
      table.set("bar", 2);
      IntTable table2 = table.apply(x -> 5 * x);
      assertAll(
          () -> assertEquals(5, table2.get("foo", 0)),
          () -> assertEquals(10, table2.get("bar", 0)),
          () -> assertEquals(0, table2.get("baz", 0)),

          () -> assertNotSame(table, table2),
          () -> assertEquals(1, table.get("foo", 0)),
          () -> assertEquals(2, table.get("bar", 0)),
          () -> assertEquals(0, table.get("baz", 0))
      );
    }

    @Test
    public void ensureDoNotReinventTheWheel() {
      assertTrue(Arrays.stream(IntTable.class.getMethods())
          .filter(m -> m.getName().equals("apply"))
          .map(m -> m.getParameterTypes()[0])
          .peek(t -> assertEquals("java.util.function", t.getPackageName()))
          .flatMap(t -> Arrays.stream(t.getMethods()))
          .filter(m -> Modifier.isAbstract(m.getModifiers()))
          .peek(m -> assertTrue(m.getReturnType().isPrimitive()))
          .flatMap(m -> Arrays.stream(m.getParameterTypes()))
          .allMatch(Class::isPrimitive));
    }

    @Test
    public void precondition() {
      var table = new IntTable();
      assertThrows(NullPointerException.class, () -> table.apply(null));
    }
  }


  @Nested
  public class Q4 {
    @Test
    public void recordComponentIndexes() throws IllegalAccessException {
      record Point(int x, int y) { }

      var components = Point.class.getRecordComponents();
      var map = IntTable.recordComponentIndexes(components);
      assertEquals(Map.of("x", 0, "y", 1), map);
    }

    @Test
    public void recordComponentIndexes2() throws IllegalAccessException {
      record Person(String name, int age, boolean manager) {}

      var components = Person.class.getRecordComponents();
      var map = IntTable.recordComponentIndexes(components);
      assertEquals(Map.of("name", 0, "age", 1, "manager", 2), map);
    }

    @Test
    public void recordComponentIndexesEmpty() throws IllegalAccessException {
      record Empty() {}

      var components = Empty.class.getRecordComponents();
      var map = IntTable.recordComponentIndexes(components);
      assertEquals(Map.of(), map);
    }

    @Test
    public void encapsulation() {
      assertTrue(Arrays.stream(IntTable.class.getMethods())
          .filter(m -> m.getName().equals("recordComponentIndexes"))
          .findFirst().isEmpty());
    }
  }


  @Nested
  public class Q5 {
    @Test
    public void from() {
      record Point(int x, int y) {}

      IntTable table = IntTable.from(Point.class);
      assertEquals(2, table.size());
    }

    @Test
    public void from2() {
      record Foo(String s) {}

      IntTable table = IntTable.from(Foo.class);
      assertEquals(1, table.size());
    }

    @Test
    public void fromEmpty() {
      record Empty() {}

      IntTable table = IntTable.from(Empty.class);
      assertEquals(0, table.size());
    }

    @Test
    public void fromNotARecord() {
      assertAll(
          () -> assertThrows(IllegalArgumentException.class, () -> IntTable.from(Map.Entry.class)),
          () -> assertThrows(IllegalArgumentException.class, () -> IntTable.from(Record.class))
      );
    }

    @Test
    public void storage() throws NoSuchFieldException, IllegalAccessException {
      record Foo(String s) {}

      var table = IntTable.from(Foo.class);
      for(var field: IntTable.class.getDeclaredFields()) {
        field.setAccessible(true);
        var value = field.get(table);
        if (value.getClass().getName().equals("fr.uge.table.IntTable$RecordImpl")) {
          return;
        }
      }
      fail("no storage field");
    }

    @Test
    public void preconditions() {
      assertThrows(NullPointerException.class, () -> IntTable.from(null));
    }
  }


  @Nested
  public class Q6 {
    @Test
    public void fromEmptyGet() {
      record Point(int x, int y) {}

      IntTable table = IntTable.from(Point.class);
      assertAll(
          () -> assertEquals(2, table.size()),
          () -> assertEquals(0, table.get("x", -1)),
          () -> assertEquals(0, table.get("y", -1)),
          () -> assertEquals(-1, table.get("z", -1))
      );
    }

    @Test
    public void fromGetAndSet() {
      record Person(String name, int age, boolean manager) {}

      var table = IntTable.from(Person.class);
      table.set("name", 5);
      table.set("age", 42);
      table.set("manager", 777);
      assertAll(
          () -> assertEquals(3, table.size()),
          () -> assertEquals(5, table.get("name", 0)),
          () -> assertEquals(42, table.get("age", 0)),
          () -> assertEquals(777, table.get("manager", 0))
      );
    }

    @Test
    public void fromSetUnknown() {
      record Foo(String s) {}

      var table = IntTable.from(Foo.class);
      assertThrows(IllegalStateException.class, () -> table.set("jhdfkfhfdhjk", 42));
    }

    @Test
    public void fromSetTwice() {
      record Point(int x, int y) {}

      var table = IntTable.from(Point.class);
      table.set("x", 5);
      table.set("x", 15);
      assertAll(
          () -> assertEquals(2, table.size()),
          () -> assertEquals(15, table.get("x", -1)),
          () -> assertEquals(0, table.get("y", -1)),
          () -> assertEquals(-1, table.get("z", -1))
      );
    }

    @Test
    public void fromSetAndGetALot() {
      record Point(int x, int y) {}

      var table = IntTable.from(Point.class);
      new Random(0)
          .ints(1_000_000, 0, 1_000)
          .forEach(i -> table.set(i < 500 ? "x": "y", i));
      assertAll(
          () -> assertEquals(2, table.size()),
          () -> assertEquals(80, table.get("x", 0)),
          () -> assertEquals(685, table.get("y", 0))
      );
    }

    @Test
    public void preconditions() {
      record Point(int x, int y) {}

      var table = IntTable.from(Point.class);
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> table.set(null, 42)),
          () -> assertThrows(NullPointerException.class, () -> table.get(null, 0))
      );
    }
  }


  @Nested
  public class Q7 {
    @Test
    public void apply() {
      record Foo(String foo, String bar) {}

      IntTable table = IntTable.from(Foo.class);
      table.set("foo", 1);
      table.set("bar", 2);
      IntTable table2 = table.apply(x -> 5 * x);
      assertAll(
          () -> assertEquals(5, table2.get("foo", -1)),
          () -> assertEquals(10, table2.get("bar", -1)),
          () -> assertEquals(-1, table2.get("baz", -1)),

          () -> assertNotSame(table, table2),
          () -> assertEquals(1, table.get("foo", -1)),
          () -> assertEquals(2, table.get("bar", -1)),
          () -> assertEquals(-1, table.get("baz", -1))
      );
    }

    @Test
    public void applyTableAlsoARecordImplementation() {
      record Foo(String foo) {}

      var table = IntTable.from(Foo.class);
      var table2 = table.apply(x -> 2 * x);
      assertThrows(IllegalStateException.class, () -> table2.set("fjdfhjf", 42));
    }

    @Test
    public void precondition() {
      record Point(int x, int y) {}

      var table = IntTable.from(Point.class);
      assertThrows(NullPointerException.class, () -> table.apply(null));
    }
  }


  @Nested
  public class Q8 {
    @Test
    public void concatOne() {
      var table = new IntTable();
      table.set("foo", 1);
      assertEquals("{foo=1}", "" + table);
    }

    @Test
    public void concatOne2() {
      record Foo(String foo) {}

      var table = IntTable.from(Foo.class);
      table.set("foo", 1);
      assertEquals("{foo=1}", "" + table);
    }

    @Test
    public void concat() {
      var table = new IntTable();
      table.set("foo", 1);
      table.set("bar", 2);
      table.set("whizz", 3);
      assertEquals("{foo=1, bar=2, whizz=3}", "" + table);
    }

    @Test
    public void concat2() {
      record Foo(String foo, String bar, String baz, String whizz) {}

      var table = IntTable.from(Foo.class);
      table.set("bar", 1);
      table.set("foo", 2);
      table.set("whizz", 3);
      assertEquals("{foo=2, bar=1, baz=0, whizz=3}", "" + table);
    }

    @Test
    public void concatAfterApply() {
      var table = new IntTable();
      table.set("foo", -1);
      table.set("bar", 2);
      table.set("whizz", -3);
      var table2 = table.apply(Math::abs);
      assertEquals("{foo=1, bar=2, whizz=3}", "" + table2);
    }

    @Test
    public void concatAfterApply2() {
      record Foo(String foo, String bar, String baz, String whizz) {}

      var table = IntTable.from(Foo.class);
      table.set("bar", -1);
      table.set("foo", 2);
      table.set("whizz", -3);
      var table2 = table.apply(Math::abs);
      assertEquals("{foo=2, bar=1, baz=0, whizz=3}", "" + table2);
    }

    @Test
    public void concatEmpty() {
      record Empty() {}

      assertAll(
          () -> assertEquals("{}", "" + new IntTable()),
          () -> assertEquals("{}", "" + IntTable.from(Empty.class))
      );
    }
  }
}