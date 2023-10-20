package fr.uge.set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.RecordComponent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("static-method")
public class HashTableSetTest {
  @Nested
  public class Q1 {

    @Test
    public void entryIsNotDeclaredInThePackage() {
      assertThrows(ClassNotFoundException.class, () -> Class.forName(HashTableSet.class.getPackageName() + ".Entry"));
    }

    @Test
    public void entryIsDeclaredInsideIntHashSet() {
      assertTrue(Arrays.stream(HashTableSet.class.getDeclaredClasses())
          .anyMatch(type -> type.getName().equals(HashTableSet.class.getName() + "$Entry")));
    }

    @Test
    public void entryIsARecord() throws ClassNotFoundException {
      var type = Class.forName(HashTableSet.class.getName() + "$Entry");
      assertTrue(type.isRecord());
    }

    @Test
    public void entryIsStaticAndPrivateAndFinal() throws ClassNotFoundException {
      var type = Class.forName(HashTableSet.class.getName() + "$Entry");
      var accessFlags = type.accessFlags();
      assertAll(
          () -> assertTrue(accessFlags.contains(AccessFlag.PRIVATE)),
          () -> assertTrue(accessFlags.contains(AccessFlag.STATIC)),
          () -> assertTrue(accessFlags.contains(AccessFlag.FINAL))
      );
    }

    @Test
    public void entryHasTwoComponents() throws ClassNotFoundException {
      var type = Class.forName(HashTableSet.class.getName() + "$Entry");
      var components = type.getRecordComponents();
      assertNotNull(components);
      assertEquals(2, components.length);
      assertEquals(java.util.Set.of(Object.class, type),
          Arrays.stream(components).map(RecordComponent::getType).collect(toSet()));
    }
  }


  @Nested
  public class Q2 {

    @Test
    public void shouldAddOne() {
      var set = new HashTableSet();
      set.add(1);
      assertEquals(1, set.size());
    }

    @Test
    public void shouldAnIntegerOnlyOnce() {
      var set = new HashTableSet();
      set.add(1_000);
      set.add(1_000);
      assertEquals(1, set.size());
    }

    @Test
    public void shouldAddAString() {
      var set = new HashTableSet();
      set.add("foo");
      assertEquals(1, set.size());
    }

    @Test
    public void shouldAddAStringOnlyOnce() {
      var set = new HashTableSet();
      set.add("foo");
      set.add("foo");
      assertEquals(1, set.size());
    }

    @Test
    public void shouldAddADurationOnlyOnce() {
      var set = new HashTableSet();
      set.add(Duration.ofMillis(500));
      set.add(Duration.ofMillis(500));
      assertEquals(1, set.size());
    }

    @Test
    public void shouldAddWithoutErrors() {
      var set = new HashTableSet();
      assertTimeoutPreemptively(Duration.ofMillis(5_000),
          () -> IntStream.range(0, 100).forEach(set::add));
      assertEquals(100, set.size());
    }

    @Test
    public void shouldGetAnErrorWhenAddingNull() {
      var set = new HashTableSet();
      assertThrows(NullPointerException.class, () -> set.add(null));
    }

    @Test
    public void shouldTheClassBePublicFinal() {
      var accessFlags = HashTableSet.class.accessFlags();
      assertAll(
          () -> assertTrue(accessFlags.contains(AccessFlag.PUBLIC)),
          () -> assertTrue(accessFlags.contains(AccessFlag.FINAL))
      );
    }

    @Test
    public void shouldTheArrayDefaultCapacityBe16() throws IllegalAccessException {
      var set = new HashTableSet();
      var field = Arrays.stream(HashTableSet.class.getDeclaredFields())
          .filter(f -> Object[].class.isAssignableFrom(f.getType()))
          .findFirst()
          .orElseThrow();
      field.setAccessible(true);
      assertEquals(16, ((Object[]) field.get(set)).length);
    }

    @Test
    public void shouldNotTakeTooLongToAddTheSameNumberMultipleTimes() {
      var set = new HashTableSet();
      assertTimeoutPreemptively(Duration.ofMillis(5_000),
          () -> IntStream.range(0, 1_000_000).map(i -> 42).forEach(set::add));
      assertEquals(1, set.size());
    }

    @Test
    public void shouldAnswerZeroWhenAskingForSizeOfEmptySet() {
      var set = new HashTableSet();
      assertEquals(0, set.size());
    }

    @Test
    public void shouldNotAddTwiceTheSameAndComputeSizeAccordingly() {
      var set = new HashTableSet();
      set.add(3);
      assertEquals(1, set.size());
      set.add(-777);
      assertEquals(2, set.size());
      set.add(3);
      assertEquals(2, set.size());
      set.add(-777);
      assertEquals(2, set.size());
      set.add(11);
      assertEquals(3, set.size());
      set.add(3);
      assertEquals(3, set.size());
    }
  }


  @Nested
  public class Q3 {

    @Test
    public void shouldDoNoThingWhenForEachCalledOnEmptySet() {
      var set = new HashTableSet();
      set.forEach(__ -> fail("should not be called"));
    }

    @Test
    public void shouldComputeTheSumOfAllTheElementsInASetUsingForEachAngGetTheSameAsTheFormula() {
      var length = 100;
      var set = new HashTableSet();
      IntStream.range(0, length).forEach(set::add);
      var box = new Object() { int sum; };
      set.forEach(element -> box.sum += (int) element);
      assertEquals(length * (length - 1) / 2, box.sum);
    }

    @Test
    public void shouldNotCountTheSameValueTwice() {
      var set = new HashTableSet();
      IntStream.range(0, 100).map(i -> i / 2).forEach(set::add);
      assertEquals(50, set.size());
    }

    @Test
    public void shouldAddAllTheElementsOfASetToAListUsingForEach() {
      var set = new HashTableSet();
      IntStream.range(0, 100).forEach(set::add);
      var list = new ArrayList<>();
      set.forEach(list::add);
      list.sort(null);
      IntStream.range(0, 100).forEach(i -> assertEquals(i, list.get(i)));
    }

    @Test
    public void shouldNotUseNullAsAParameterForForEach() {
      var set = new HashTableSet();
      assertThrows(NullPointerException.class, () -> set.forEach(null));
    }
  }


  @Nested
  class Q4 {

    @Test
    public void shouldFindAPreviouslyInsertedValue() {
      var set = new HashTableSet();
      set.add("foo");
      assertAll(
          () -> assertTrue(set.contains("foo")),
          () -> assertFalse(set.contains("bar"))
      );
    }

    @Test
    public void shouldNotFindAnythingContainedInAnEmptySet() {
      var set = new HashTableSet();
      assertFalse(set.contains(42));
      assertFalse(set.contains("foo"));
    }

    @Test
    public void shouldNotFindSomethingWithTheWrongType() {
      var set = new HashTableSet();
      set.add(42);
      assertFalse(set.contains("foo"));
    }

    @Test
    public void shouldNotFindAnIntegerBeforeAddingItButShouldFindItAfter() {
      var set = new HashTableSet();
      for (int i = 0; i < 100; i++) {
        assertFalse(set.contains(i));
        set.add(i);
        assertTrue(set.contains(i));
      }
    }

    @Test
    public void shouldAddAndTestContainsForAnExtremalValue() {
      var set = new HashTableSet();
      assertFalse(set.contains(Integer.MIN_VALUE));
      set.add(Integer.MIN_VALUE);
      assertTrue(set.contains(Integer.MIN_VALUE));
      set.add(Integer.MAX_VALUE);
      assertTrue(set.contains(Integer.MAX_VALUE));
      assertEquals(2, set.size());
    }

    @Test
    public void shouldContainsThrowAnErrorWithNull() {
      var set = new HashTableSet();
      assertThrows(NullPointerException.class, () -> set.contains(null));
    }
  }


  @Nested
  class Q5 {

    @Test
    public void shouldBeAbleToAdd16Elements() {
      var set = new HashTableSet();
      IntStream.range(0, 16).forEach(set::add);
      var list = new ArrayList<>();
      set.forEach(list::add);
      list.sort(null);
      assertAll(
          () -> assertEquals(16, set.size()),
          () -> assertEquals(IntStream.range(0, 16).mapToObj(x -> x).toList(), list)
      );
    }

    @Test
    public void shouldBeAbleToAdd17Elements() {
      var set = new HashTableSet();
      IntStream.range(0, 17).forEach(set::add);
      var list = new ArrayList<>();
      set.forEach(list::add);
      list.sort(null);
      assertAll(
          () -> assertEquals(17, set.size()),
          () -> assertEquals(IntStream.range(0, 17).mapToObj(x -> x).toList(), list)
      );
    }

    @Test
    public void shouldRehashWorkProperlyWithContains() {
      var set = new HashTableSet();
      IntStream.range(21, 30).forEach(set::add);
      assertTrue(set.contains(26));
      assertTrue(set.contains(27));
    }
    
    @Test
    public void shouldRehashWorkProperlyWithgoutDuplicates() {
      var set = new HashTableSet();
      IntStream.range(0, 9).forEach(i -> set.add(i * 16));
      var list  = new ArrayList();
      set.forEach(list::add);
      assertEquals(9, list.size());
    }

    @Test
    public void shouldFindItAfterAddingMany() {
      var set = new HashTableSet();
      for (int i = 0; i < 100; i++) {
        set.add(100 - i);
        assertTrue(set.contains(100 - i));
      }
      for (int i = 0; i < 100; i++) {
        assertTrue(set.contains(100 - i));
      }
    }
    
    @Test
    public void shouldNotFindAnIntegerBeforeAddingItButShouldFindItAfterALot() {
      var set = new HashTableSet();
      for (int i = 0; i < 1_000_000; i++) {
        assertFalse(set.contains(i));
        set.add(i);
        assertTrue(set.contains(i));
      }
    }

    @Test
    public void shouldBeAbleToALotOfString() {
      var set = new HashTableSet();
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> IntStream.range(0, 1_000_000).mapToObj(String::valueOf).forEach(set::add));
      assertEquals(1_000_000, set.size());
      IntStream.range(0, 1_000_000).forEach(set::contains);
    }

    @Test
    public void shouldNotTakeTooLongToAddDifferentElementsMultipleTimes() {
      var set = new HashTableSet();
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> IntStream.range(0, 1_000_000).map(i -> i / 2).forEach(set::add));
      assertEquals(1_000_000 / 2, set.size());
      IntStream.range(0, 1_000_000 / 2).forEach(set::contains);
    }
  }


//  @Nested
//  class Q6 {
//
//    @Test
//    public void shouldAddOne() {
//      var set = new HashTableSet<Integer>();
//      set.add(1);
//      assertEquals(1, set.size());
//    }
//
//    @Test
//    public void shouldAddAString() {
//      var set = new HashTableSet<String>();
//      set.add("foo");
//      assertEquals(1, set.size());
//    }
//
//    @Test
//    public void shouldForEachWithTheRightType() {
//      var set = new HashTableSet<Integer>();
//      set.add(1);
//      set.forEach(element -> assertEquals(1, Math.min(element, 10)));
//    }
//
//    @Test
//    public void shouldForEachWithTheRightType2() {
//      var set = new HashTableSet<String>();
//      set.add("foo");
//      set.add("five");
//      set.add("fallout");
//      set.forEach(element -> assertTrue(element.startsWith("f")));
//    }
//  }
//
//  @Nested
//  class Q7 {
//
//    @Test
//    public void shouldForEachWorkWithASupertype() {
//      var set = new HashTableSet<Integer>();
//      set.add(1);
//      set.forEach((Object element) -> {
//        assertEquals(1, element);
//      });
//    }
//
//    @Test
//    public void shouldForEachWorkWithASupertype2() {
//      var set = new HashTableSet<String>();
//      set.add("foo");
//      set.forEach((CharSequence element) -> {
//        assertEquals("foo", element);
//      });
//    }
//  }
//
//  @Nested
//  class Q8 {
//
//    @Test
//    public void shouldAddAllTheElementsWithNoDuplicate() {
//      var set = new HashTableSet<Integer>();
//      set.add(1_000);
//      set.add(2_000);
//      var set2 = new HashTableSet<Integer>();
//      set2.add(2_000);
//      set2.add(4_000);
//
//      set.addAll(set2);
//
//      var list = new ArrayList<Integer>();
//      set.forEach(list::add);
//      list.sort(null);
//      assertEquals(List.of(1_000, 2_000, 4_000), list);
//    }
//
//    @Test
//    public void shouldAddAllTheElementsWithNoDuplicate2() {
//      var set = new HashTableSet<Object>();
//      set.add("foo");
//      set.add(42);
//      var set2 = new HashTableSet<Integer>();
//      set2.add(201);
//      set2.add(42);
//
//      set.addAll(set2);
//
//      var hashSet = new java.util.HashSet<>();
//      set.forEach(hashSet::add);
//      assertEquals(java.util.Set.of(42, 201, "foo"), hashSet);
//    }
//
//    @Test
//    public void shouldNotSupportAddAllWithNull() {
//      var set = new HashTableSet<>();
//      assertThrows(NullPointerException.class, () -> set.addAll(null));
//    }
//  }
//
//
//  @Nested
//  class Q9 {
//
//    @Test
//    public void shouldBeEqualsWhateverOfTheOrder() {
//      var set = new HashTableSet<Integer>();
//      set.add(1);
//      set.add(17);
//      var set2 = new HashTableSet<Integer>();
//      set2.add(17);
//      set2.add(1);
//
//      assertAll(
//          () -> assertTrue(set.equals(set2)),
//          () -> assertTrue(set2.equals(set))
//      );
//    }
//
//    @Test
//    public void shouldBeDifferentEvenIfThereAreNotDisjoint() {
//      var set = new HashTableSet<String>();
//      set.add("foo");
//      set.add("bar");
//      set.add("baz");
//      var set2 = new HashTableSet<String>();
//      set2.add("foo");
//      set2.add("bar");
//      set2.add("whizz");
//
//      assertAll(
//          () -> assertFalse(set.equals(set2)),
//          () -> assertFalse(set2.equals(set))
//      );
//    }
//
//    @Test
//    public void shouldWorkEvenWithSetNotOfTheSameType() {
//      var set = new HashTableSet<Object>();
//      set.add("foo");
//      var set2 = new HashTableSet<String>();
//      set2.add("foo");
//
//      assertAll(
//          () -> assertTrue(set.equals(set2)),
//          () -> assertTrue(set2.equals(set))
//      );
//    }
//
//    @Test
//    public void shouldNotWorkIfSetDoNotHaveTheSameSize() {
//      var set = new HashTableSet<Integer>();
//      IntStream.range(0, 1_000_000).forEach(set::add);
//      var set2 = new HashTableSet<Integer>();
//      set2.add(0);
//
//      assertAll(
//          () -> assertFalse(set.equals(set2)),
//          () -> assertFalse(set2.equals(set))
//      );
//    }
//
//    @Test
//    public void shouldEqualsHasTheCorrectPreconditions() {
//      var set = new HashTableSet<String>();
//      assertAll(
//          () -> assertFalse(set.equals(null)),
//          () -> assertFalse(set.equals("what"))
//      );
//    }
//
//    @Test
//    public void shouldWorksWithSetOfSet() {
//      var set = new HashTableSet<String>();
//      set.add("hello");
//      var setOfSet = new HashTableSet<HashTableSet<String>>();
//      setOfSet.add(set);
//
//      var set2 = new HashTableSet<String>();
//      set2.add("hello");
//      var setOfSet2 = new HashTableSet<HashTableSet<String>>();
//      setOfSet2.add(set2);
//
//      assertAll(
//          () -> assertTrue(setOfSet.equals(setOfSet2)),
//          () -> assertTrue(setOfSet2.equals(setOfSet))
//      );
//    }
//  }
}