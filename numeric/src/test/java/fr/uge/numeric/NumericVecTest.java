package fr.uge.numeric;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class NumericVecTest {

  @Nested
  public class Q1 {
    @Test
    public void longs() {
      NumericVec<Long> vec = NumericVec.longs(1, 42, 747);
      assertAll(
          () -> assertEquals(3, vec.size()),
          () -> assertEquals(1L, vec.get(0)),
          () -> assertEquals(42L, vec.get(1)),
          () -> assertEquals(747L, vec.get(2))
      );
    }

    @Test
    public void emptyLongs() {
      var vec = NumericVec.longs();
      assertEquals(0, vec.size());
    }

    @Test
    public void longsSideMutation() {
      var array = new long[] { 12L, 80L, 128L };
      var vec = NumericVec.longs(array);
      array[1] = 64L;
      assertEquals(80L, vec.get(1));
    }

    @Test
    public void getOutOfBounds() {
      var vec = NumericVec.longs(7L, -3L);
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> vec.get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> vec.get(2))
      );
    }

    @Test
    public void noPublicConstructor() {
      assertEquals(0, NumericVec.class.getConstructors().length);
    }
  }


  @Nested
  public class Q2 {
    @Test
    public void longsAdd() {
      var vec = NumericVec.longs(1L, 2L, 3L);
      vec.add(4L);
      assertEquals(4, vec.size());
    }

    @Test
    public void longsResizeable() {
      var vec = NumericVec.longs();
      for(var i = 0L; i < 17L; i++) {
        vec.add(i);
      }
      assertEquals(17, vec.size());
    }

    @Test
    public void longsALotOfValues() {
      var vec = NumericVec.longs();
      LongStream.range(0, 1_000_000).forEach(vec::add);
      assertAll(
          () -> assertEquals(1_000_000, vec.size()),
          () -> IntStream.range(0, 1_000_000).forEach(i -> assertEquals(i, vec.get(i)))
      );
    }

    @Test
    public void addPrecondition() {
      var vec = NumericVec.longs();
      assertThrows(NullPointerException.class, () -> vec.add(null));
    }
  }


  @Nested
  public class Q3 {
    @Test
    public void intsOrLongsOrDoublesWithValuesTyping() {
      NumericVec<Integer> intSeq = NumericVec.ints(42);
      NumericVec<Long> longSeq = NumericVec.longs(42L);
      NumericVec<Double> doubleSeq = NumericVec.doubles(256.);
      assertAll(
          () -> assertNotNull(intSeq),
          () -> assertNotNull(longSeq),
          () -> assertNotNull(doubleSeq)
      );
    }

    @Test
    public void intsWithValues() {
      var vec = NumericVec.ints(1, 42, 747);
      assertAll(
          () -> assertEquals(3, vec.size()),
          () -> assertEquals(1, vec.get(0)),
          () -> assertEquals(42, vec.get(1)),
          () -> assertEquals(747, vec.get(2))
      );
    }

    @Test
    public void longsWithValues() {
      var vec = NumericVec.longs(1L, 42L, 747L);
      assertAll(
          () -> assertEquals(3, vec.size()),
          () -> assertEquals(1L, vec.get(0)),
          () -> assertEquals(42L, vec.get(1)),
          () -> assertEquals(747L, vec.get(2))
      );
    }

    @Test
    public void doublesWithValues() {
      var vec = NumericVec.doubles(2., 256., 16.);
      assertAll(
          () -> assertEquals(3, vec.size()),
          () -> assertEquals(2., vec.get(0)),
          () -> assertEquals(256., vec.get(1)),
          () -> assertEquals(16., vec.get(2))
      );
    }

    @Test
    public void intsWithValuesAdd() {
      var vec = NumericVec.ints(1, 42);
      vec.add(0);
      vec.add(-31);
      assertAll(
          () -> assertEquals(4, vec.size()),
          () -> assertEquals(1, vec.get(0)),
          () -> assertEquals(42, vec.get(1)),
          () -> assertEquals(0, vec.get(2)),
          () -> assertEquals(-31, vec.get(3))
      );
    }

    @Test
    public void longsWithValuesAdd() {
      var vec = NumericVec.longs(1L, 42L);
      vec.add(0L);
      vec.add(-31L);
      assertAll(
          () -> assertEquals(4, vec.size()),
          () -> assertEquals(1L, vec.get(0)),
          () -> assertEquals(42L, vec.get(1)),
          () -> assertEquals(0L, vec.get(2)),
          () -> assertEquals(-31L, vec.get(3))
      );
    }

    @Test
    public void doubleWithValuesAdd() {
      var vec = NumericVec.doubles(2., 16.);
      vec.add(0.);
      vec.add(-32.);
      assertAll(
          () -> assertEquals(4, vec.size()),
          () -> assertEquals(2., vec.get(0)),
          () -> assertEquals(16., vec.get(1)),
          () -> assertEquals(0., vec.get(2)),
          () -> assertEquals(-32., vec.get(3))
      );
    }

    @Test
    public void intsWithValuesBig() {
      var vec = NumericVec.ints(IntStream.range(0, 1_000_000).toArray());
      assertAll(
          () -> assertEquals(1_000_000, vec.size()),
          () -> IntStream.range(0, 1_000_000).forEach(i -> assertEquals(i, vec.get(i)))
      );
    }

    @Test
    public void longsWithValuesBig() {
      var vec = NumericVec.longs(LongStream.range(0, 1_000_000).toArray());
      assertAll(
          () -> assertEquals(1_000_000, vec.size()),
          () -> IntStream.range(0, 1_000_000).forEach(i -> assertEquals((long) i, vec.get(i)))
      );
    }

    @Test
    public void doublesWithValuesBig() {
      var vec = NumericVec.doubles(IntStream.range(0, 1_000_000).mapToDouble(i -> i).toArray());
      assertAll(
          () -> assertEquals(1_000_000, vec.size()),
          () -> IntStream.range(0, 1_000_000).forEach(i -> assertEquals((double) i, vec.get(i)))
      );
    }

    @Test
    public void intsSideMutation() {
      var array = new int[] { 12, 80, 128 };
      var vec = NumericVec.ints(array);
      array[1] = 64;
      assertEquals(80, vec.get(1));
    }

    @Test
    public void longsSideMutation() {
      var array = new long[] { 12L, 80L, 128L };
      var vec = NumericVec.longs(array);
      array[1] = 64L;
      assertEquals(80L, vec.get(1));
    }

    @Test
    public void doublesSideMutation() {
      var array = new double[] { 12, 80, 128 };
      var vec = NumericVec.doubles(array);
      array[1] = 64.;
      assertEquals(80., vec.get(1));
    }

    @Test
    public void onlyOneArray() {
      assertTrue(Arrays.stream(NumericVec.class.getDeclaredFields())
          .noneMatch(field -> field.getType().isArray() && field.getType() != long[].class));
    }

    @Test
    public void intsOrLongsOrDoublesWithValuesPrecondition() {
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> NumericVec.ints(null)),
          () -> assertThrows(NullPointerException.class, () -> NumericVec.longs(null)),
          () -> assertThrows(NullPointerException.class, () -> NumericVec.doubles(null))
      );
    }
  }

  @Nested
  public class Q4 {

    @Test
    public void stream() {
      var vec = NumericVec.longs();
      vec.add(12L);
      vec.add(1L);
      assertEquals(List.of(12L, 1L), vec.stream().toList());
    }

    @Test
    public void streamCount() {
      var vec = NumericVec.ints(2, 3, 4);
      assertEquals(3, vec.stream().map(__ -> fail()).count());
    }

    @Test
    public void streamMutation() {
      var vec = NumericVec.doubles();
      vec.add(32.);
      var stream = vec.stream();
      vec.add(64.);
      assertEquals(List.of(32.), stream.toList());
    }

    @Test
    public void streamDontSplitIfNotEnoughElements() {
      var vec = NumericVec.ints();
      IntStream.range(0, 512).forEach(vec::add);
      assertNull(vec.stream().spliterator().trySplit());
    }

    @Test
    public void streamSplitIfEnoughElements() {
      var vec = NumericVec.ints();
      IntStream.range(0, 2_048).forEach(vec::add);
      assertNotNull(vec.stream().spliterator().trySplit());
    }

    @Test
    public void streamNotParallelByDefault() {
      var stream = NumericVec.longs(200L).stream();
      assertFalse(stream.isParallel());
    }

    @Test
    public void streamCharacteristics() {
      var spliterator = NumericVec.longs().stream().spliterator();
      assertAll(
          () -> assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL)),
          () -> assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED)),
          () -> assertTrue(spliterator.hasCharacteristics(Spliterator.IMMUTABLE))
      );
    }
  }

  @Nested
  public class Q5 {
    @Test
    public void intsIsAList() {
      List<Integer> list = NumericVec.ints(1, 2, 3, 4);
      assertAll(
          () -> assertEquals(4, list.size()),
          () -> assertFalse(list.isEmpty()),
          () -> assertEquals(2, list.get(1)),
          //() -> assertEquals(1, list.getFirst()),
          //() -> assertEquals(4, list.getLast()),
          () -> assertTrue(list.contains(3))
      );
    }

    @Test
    public void longsIsAList() {
      List<Long> list = NumericVec.longs(1L, 2L, 3L, 4L);
      assertAll(
          () -> assertEquals(4, list.size()),
          () -> assertFalse(list.isEmpty()),
          () -> assertEquals(2L, list.get(1)),
          //() -> assertEquals(1L, list.getFirst()),
          //() -> assertEquals(4L, list.getLast()),
          () -> assertTrue(list.contains(3L))
      );
    }

    @Test
    public void doublesIsAList() {
      List<Double> list = NumericVec.doubles(1., 2., 3., 4.);
      assertAll(
          () -> assertEquals(4, list.size()),
          () -> assertFalse(list.isEmpty()),
          () -> assertEquals(2., list.get(1)),
          //() -> assertEquals(1., list.getFirst()),
          //() -> assertEquals(4., list.getLast()),
          () -> assertTrue(list.contains(3.))
      );
    }

    @Test
    public void emptyIntsAsList() {
      List<Integer> list = NumericVec.ints();
      assertAll(
          () -> assertEquals(0, list.size()),
          () -> assertTrue(list.isEmpty()),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> list.get(1)),
          //() -> assertThrows(NoSuchElementException.class,  list::getFirst),
          //() -> assertThrows(NoSuchElementException.class,  list::getLast),
          () -> assertFalse(list.contains(3))
      );
    }

    @Test
    public void emptyLongsAsList() {
      List<Long> list = NumericVec.longs();
      assertAll(
          () -> assertEquals(0, list.size()),
          () -> assertTrue(list.isEmpty()),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> list.get(1)),
          //() -> assertThrows(NoSuchElementException.class,  list::getFirst),
          //() -> assertThrows(NoSuchElementException.class,  list::getLast),
          () -> assertFalse(list.contains(3L))
      );
    }

    @Test
    public void emptyDoublesAsList() {
      List<Double> list = NumericVec.doubles();
      assertAll(
          () -> assertEquals(0, list.size()),
          () -> assertTrue(list.isEmpty()),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> list.get(1)),
          //() -> assertThrows(NoSuchElementException.class,  list::getFirst),
          //() -> assertThrows(NoSuchElementException.class,  list::getLast),
          () -> assertFalse(list.contains(3.))
      );
    }

    @Test
    public void intsToString() {
      List<Integer> list = NumericVec.ints(1, 2, 3, 4);
      assertEquals("[1, 2, 3, 4]", "" + list);
    }

    @Test
    public void longsToString() {
      List<Long> list = NumericVec.longs(1L, 2L, 3L, 4L);
      assertEquals("[1, 2, 3, 4]", "" + list);
    }

    @Test
    public void doublesToString() {
      List<Double> list = NumericVec.doubles(1., 2., 3., 4.);
      assertEquals("[1.0, 2.0, 3.0, 4.0]", "" + list);
    }

    @Test
    public void intsForEachLoop() {
      var vec = NumericVec.ints(1, 2, 3, 4);
      var sum = 0;
      for(var item : vec) {
        sum += item;
      }
      assertEquals(10, sum);
    }

    @Test
    public void longsForEachLoop() {
      var vec = NumericVec.longs(1L, 2L, 3L, 4L);
      var sum = 0L;
      for(var item : vec) {
        sum += item;
      }
      assertEquals(10L, sum);
    }

    @Test
    public void doublesForEachLoop() {
      var vec = NumericVec.doubles(1., 2., 3., 4.);
      var sum = .0;
      for(var item : vec) {
        sum += item;
      }
      assertEquals(10.0, sum);
    }

    @Test
    public void spliteratorCharacteristics() {
      var spliterator = NumericVec.ints().spliterator();
      assertAll(
          () -> assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL)),
          () -> assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED)),
          () -> assertTrue(spliterator.hasCharacteristics(Spliterator.IMMUTABLE))
      );
    }

    @Test
    public void intsVecEquals() {
      var vec = NumericVec.ints(1, 2, 3, 4);
      assertEquals(NumericVec.ints(1, 2, 3, 4), vec);
    }

    @Test
    public void longsVecEquals() {
      var vec = NumericVec.longs(1L, 2L, 3L, 4L);
      assertEquals(NumericVec.longs(1L, 2L, 3L, 4L), vec);
    }

    @Test
    public void doublesVecEquals() {
      var vec = NumericVec.doubles(1., 2., 3., 4.);
      assertEquals(NumericVec.doubles(1., 2., 3., 4.), vec);
    }

    @Test
    public void intsEquals() {
      var vec = NumericVec.ints(1, 2, 3, 4);
      assertEquals(List.of(1, 2, 3, 4), vec);
    }

    @Test
    public void longsEquals() {
      var vec = NumericVec.longs(1L, 2L, 3L, 4L);
      assertEquals(List.of(1L, 2L, 3L, 4L), vec);
    }

    @Test
    public void doublesEquals() {
      var vec = NumericVec.doubles(1., 2., 3., 4.);
      assertEquals(List.of(1., 2., 3., 4.), vec);
    }
  }


//  @Nested
//  public class Q6 {
//    @Test
//    public void addAllIntsList() {
//      var vec = NumericVec.ints(1, 2);
//      var result = vec.addAll(List.of(3, 4));
//      assertTrue(result);
//      assertEquals(List.of(1, 2, 3, 4), vec);
//    }
//
//    @Test
//    public void addAllLongsList() {
//      var vec = NumericVec.longs(1L, 2L);
//      var result = vec.addAll(List.of(3L, 4L));
//      assertTrue(result);
//      assertEquals(List.of(1L, 2L, 3L, 4L), vec);
//    }
//
//    @Test
//    public void addAllDoubleList() {
//      var vec = NumericVec.doubles(1., 2.);
//      var result = vec.addAll(List.of(3., 4.));
//      assertTrue(result);
//      assertEquals(List.of(1., 2., 3., 4.), vec);
//    }
//
//    @Test
//    public void addAllIntsSet() {
//      var vec = NumericVec.ints(1, 2);
//      var result = vec.addAll(new LinkedHashSet<>(List.of(3, 4)));
//      assertTrue(result);
//      assertEquals(List.of(1, 2, 3, 4), vec);
//    }
//
//    @Test
//    public void addAllLongsSet() {
//      var vec = NumericVec.longs(1L, 2L);
//      var result = vec.addAll(new LinkedHashSet<>(List.of(3L, 4L)));
//      assertTrue(result);
//      assertEquals(List.of(1L, 2L, 3L, 4L), vec);
//    }
//
//    @Test
//    public void addAllDoubleSet() {
//      var vec = NumericVec.doubles(1., 2.);
//      var result = vec.addAll(new LinkedHashSet<>(List.of(3., 4.)));
//      assertTrue(result);
//      assertEquals(List.of(1., 2., 3., 4.), vec);
//    }
//
//    @Test
//    public void addAllVecInts() {
//      var vec = NumericVec.ints(44, 666);
//      var vec2 = NumericVec.ints(77, 888);
//      var result = vec.addAll(vec2);
//      assertTrue(result);
//      assertEquals(NumericVec.ints(44, 666, 77, 888), vec);
//    }
//
//    @Test
//    public void addAllVecLongs() {
//      var vec = NumericVec.longs(44, 666);
//      var vec2 = NumericVec.longs(77, 888);
//      var result = vec.addAll(vec2);
//      assertTrue(result);
//      assertEquals(NumericVec.longs(44, 666, 77, 888), vec);
//    }
//
//    @Test
//    public void addAllVecDoubles() {
//      var vec = NumericVec.doubles(44, 666);
//      var vec2 = NumericVec.doubles(77, 888);
//      var result = vec.addAll(vec2);
//      assertTrue(result);
//      assertEquals(NumericVec.doubles(44, 666, 77, 888), vec);
//    }
//
//    @Test
//    @SuppressWarnings({"rawtypes", "unchecked"})
//    public void addAllShouldThrowAnExceptionInCaseOfTypePollution() {
//      NumericVec<Integer> vec = NumericVec.ints(1, 2);
//      NumericVec vec2 = NumericVec.longs(3);
//      assertThrows(ClassCastException.class, () -> vec.addAll(vec2));
//    }
//
//    @Test
//    public void addAllIntsVecEmpty() {
//      var vec = NumericVec.ints(32);
//      var vec2 = NumericVec.ints();
//      var result = vec.addAll(vec2);
//      assertFalse(result);
//      assertAll(
//          () -> assertEquals(1, vec.size()),
//          () -> assertEquals(32, vec.get(0))
//      );
//    }
//
//    @Test
//    public void addAllLongsVecEmpty() {
//      var vec = NumericVec.longs(32L);
//      var vec2 = NumericVec.longs();
//      var result = vec.addAll(vec2);
//      assertFalse(result);
//      assertAll(
//          () -> assertEquals(1, vec.size()),
//          () -> assertEquals(32L, vec.get(0))
//      );
//    }
//
//    @Test
//    public void addAllDoublesVecEmpty() {
//      var vec = NumericVec.doubles(32.);
//      var vec2 = NumericVec.doubles();
//      var result = vec.addAll(vec2);
//      assertFalse(result);
//      assertAll(
//          () -> assertEquals(1, vec.size()),
//          () -> assertEquals(32., vec.get(0))
//      );
//    }
//
//    @Test
//    public void addAllIntsBig() {
//      var vec = NumericVec.ints();
//      IntStream.range(0, 1_000_000).forEach(vec::add);
//      var vec2 = NumericVec.ints();
//      IntStream.range(1_000_000, 2_000_000).forEach(vec2::add);
//      var result = vec.addAll(vec2);
//      assertTrue(result);
//      assertAll(
//          () -> assertEquals(2_000_000, vec.size()),
//          () -> IntStream.range(0, 2_000_000).forEach(i -> assertEquals(i, vec.get(i)))
//      );
//    }
//
//    @Test
//    public void addAllLongsBig() {
//      var vec = NumericVec.longs();
//      LongStream.range(0, 1_000_000).forEach(vec::add);
//      var vec2 = NumericVec.longs();
//      LongStream.range(1_000_000, 2_000_000).forEach(vec2::add);
//      var result = vec.addAll(vec2);
//      assertTrue(result);
//      assertAll(
//          () -> assertEquals(2_000_000, vec.size()),
//          () -> IntStream.range(0, 2_000_000).forEach(i -> assertEquals((long) i, vec.get(i)))
//      );
//    }
//
//    @Test
//    public void addAllDoublesBig() {
//      var vec = NumericVec.doubles();
//      IntStream.range(0, 1_000_000).mapToDouble(i -> i).forEach(vec::add);
//      var vec2 = NumericVec.doubles();
//      IntStream.range(1_000_000, 2_000_000).mapToDouble(i -> i).forEach(vec2::add);
//      var result = vec.addAll(vec2);
//      assertTrue(result);
//      assertAll(
//          () -> assertEquals(2_000_000, vec.size()),
//          () -> IntStream.range(0, 2_000_000).forEach(i -> assertEquals((double) i, vec.get(i)))
//      );
//    }
//
//    @Test
//    public void addAllPrecondition() {
//      assertAll(
//          () -> assertThrows(NullPointerException.class, () -> NumericVec.ints().addAll(null)),
//          () -> assertThrows(NullPointerException.class, () -> NumericVec.longs().addAll(null)),
//          () -> assertThrows(NullPointerException.class, () -> NumericVec.doubles().addAll(null))
//      );
//    }
//  }
//
//
//  @Nested
//  public class Q7 {
//
//    @Test
//    public void toNumericVec() {
//      var vec = IntStream.range(0, 10).boxed().collect(NumericVec.toNumericVec(NumericVec::ints));
//      assertAll(
//          () -> assertEquals(10, vec.size()),
//          () -> IntStream.range(0, 10).forEach(i -> assertEquals(i, vec.get(i)))
//      );
//    }
//
//    @Test
//    public void toNumericVecStreamParallel() {
//      var vec = IntStream.range(0, 1_000_000).boxed().collect(NumericVec.toNumericVec(NumericVec::ints));
//      var thread = Thread.currentThread();
//      var otherThreadCount = vec.stream().parallel().mapToInt(__ -> thread != Thread.currentThread()? 1: 0).sum();
//      assertNotEquals(0, otherThreadCount);
//    }
//
//    @Test
//    public void toNumericVecMutable() {
//      var vec = Stream.of(12L, 45L).collect(NumericVec.toNumericVec(NumericVec::longs));
//      vec.add(99L);
//      assertAll(
//          () -> assertEquals(3, vec.size()),
//          () -> assertEquals(12L, vec.get(0)),
//          () -> assertEquals(45L, vec.get(1)),
//          () -> assertEquals(99L, vec.get(2))
//      );
//    }
//
//    @Test
//    public void toNumericVecParallelInts() {
//      var vec = IntStream.range(0, 1_000_000).parallel()
//          .boxed()
//          .collect(NumericVec.toNumericVec(NumericVec::ints));
//      assertAll(
//          () -> assertEquals(1_000_000, vec.size()),
//          () -> IntStream.range(0, 1_000_000).forEach(i -> assertEquals(i, vec.get(i)))
//      );
//    }
//
//    @Test
//    public void toNumericVecParallelLongs() {
//      var vec = LongStream.range(0, 1_000_000).parallel()
//          .boxed()
//          .collect(NumericVec.toNumericVec(NumericVec::longs));
//      assertAll(
//          () -> assertEquals(1_000_000, vec.size()),
//          () -> IntStream.range(0, 1_000_000).forEach(i -> assertEquals((long) i, vec.get(i)))
//      );
//    }
//
//    @Test
//    public void toNumericVecParallelDoubles() {
//      var vec = IntStream.range(0, 1_000_000).parallel()
//          .mapToObj(i -> (double) i)
//          .collect(NumericVec.toNumericVec(NumericVec::doubles));
//      assertAll(
//          () -> assertEquals(1_000_000, vec.size()),
//          () -> IntStream.range(0, 1_000_000).forEach(i -> assertEquals((double) i, vec.get(i)))
//      );
//    }
//
//    @Test
//    public void toNumericVecPreconditions() {
//      assertThrows(NullPointerException.class, () -> NumericVec.toNumericVec(null));
//    }
//
//    @Test
//    public void toNumericVersusNull() {
//      assertAll(
//          () -> assertThrows(NullPointerException.class, () -> Stream.of(12, null).collect(NumericVec.toNumericVec(NumericVec::ints))),
//          () -> assertThrows(NullPointerException.class, () -> Stream.of(12L, null).collect(NumericVec.toNumericVec(NumericVec::longs))),
//          () -> assertThrows(NullPointerException.class, () -> Stream.of(12., null).collect(NumericVec.toNumericVec(NumericVec::doubles)))
//      );
//    }
//  }
//
//
//  @Nested
//  public class Q8 {
//    @Test
//    public void freezeInts() {
//      NumericVec<Integer> vec = NumericVec.ints(17, 21, 42);
//      NumericVec<Integer> frozen = vec.freeze();
//      assertAll(
//          () -> assertTrue(frozen.isFrozen()),
//          () -> assertThrows(UnsupportedOperationException.class, () -> frozen.add(3))
//      );
//
//    }
//
//    @Test
//    public void freezeLongs() {
//      NumericVec<Long> vec = NumericVec.longs(17, 21, 42);
//      NumericVec<Long> frozen = vec.freeze();
//      assertAll(
//          () -> assertTrue(frozen.isFrozen()),
//          () -> assertThrows(UnsupportedOperationException.class, () -> frozen.add(3L))
//      );
//    }
//
//    @Test
//    public void freezeDoubles() {
//      NumericVec<Double> vec = NumericVec.doubles(17, 21, 42);
//      NumericVec<Double> frozen = vec.freeze();
//      assertAll(
//          () -> assertTrue(frozen.isFrozen()),
//          () -> assertThrows(UnsupportedOperationException.class, () -> frozen.add(3.))
//      );
//    }
//
//    @Test
//    public void freezeAddAll() {
//      var vec = NumericVec.ints(17, 21, 42);
//      var frozen = vec.freeze();
//      assertThrows(UnsupportedOperationException.class, () -> frozen.addAll(List.of(3, 4)));
//    }
//
//    @Test
//    public void addAllFrozenIntsIsOk() {
//      var vec = NumericVec.ints(17, 21, 42);
//      var frozen = NumericVec.ints(1, 2).freeze();
//      vec.addAll(frozen);
//      assertEquals(NumericVec.ints(17, 21, 42, 1, 2), vec);
//    }
//
//    @Test
//    public void addAllFrozenLongsIsOk() {
//      var vec = NumericVec.longs(17, 21, 42);
//      assertFalse(vec.isFrozen());
//      var frozen = NumericVec.longs(1, 2).freeze();
//      vec.addAll(frozen);
//      assertEquals(NumericVec.longs(17, 21, 42, 1, 2), vec);
//    }
//
//    @Test
//    public void addAllFrozenDoublesIsOk() {
//      var vec = NumericVec.doubles(17, 21, 42);
//      assertFalse(vec.isFrozen());
//      var frozen = NumericVec.doubles(1, 2).freeze();
//      vec.addAll(frozen);
//      assertEquals(NumericVec.doubles(17, 21, 42, 1, 2), vec);
//    }
//
//    @Test
//    public void freezeNoSideEffect() {
//      var vec = NumericVec.ints(17, 21, 42);
//      assertFalse(vec.isFrozen());
//      var frozen = vec.freeze();
//      vec.add(101);
//      assertAll(
//          () -> assertEquals(3, frozen.size()),
//          () -> assertEquals(4, vec.size())
//      );
//    }
//
//    @Test
//    public void implementationNoSupplementaryFields() {
//      var declaredTypes = Arrays.stream(NumericVec.class.getDeclaredFields())
//          .map(Field::getType)
//          .filter(type -> !type.getPackageName().equals("java.util.function"))
//          .collect(toSet());
//
//      assertEquals(Set.of(long[].class, int.class), declaredTypes);
//    }
//
//    @Test
//    public void implementationNoArrayDuplication() throws IllegalAccessException {
//      var arrayField = Arrays.stream(NumericVec.class.getDeclaredFields())
//          .filter(f -> f.getType().isArray())
//          .findFirst()
//          .orElseThrow();
//      arrayField.setAccessible(true);
//
//      var vec = NumericVec.ints(1, 2, 3);
//      var frozen = vec.freeze();
//      assertSame(arrayField.get(vec), arrayField.get(frozen));
//    }
//  }
//
//
//  @Nested
//  public class Q9 {
//    @Test
//    public void toUnmodifiableNumericVec() {
//      var vec = IntStream.range(0, 10).boxed().collect(NumericVec.toUnmodifiableNumericVec(NumericVec::ints));
//      assertAll(
//          () -> assertEquals(10, vec.size()),
//          () -> IntStream.range(0, 10).forEach(i -> assertEquals(i, vec.get(i)))
//      );
//    }
//
//    @Test
//    public void toUnmodifiableNumericVecNotModifiable() {
//      var vec = Stream.of(12, 45).collect(NumericVec.toUnmodifiableNumericVec(NumericVec::ints));
//      assertAll(
//          () -> assertThrows(UnsupportedOperationException.class, () -> vec.add(99)),
//          () -> assertThrows(UnsupportedOperationException.class, () -> vec.addAll(List.of(66, 33)))
//      );
//    }
//
//    @Test
//    public void toUnmodifiableNumericVecNotModifiableNoSideEffect() {
//      var vec = NumericVec.ints();
//      var frozen = Stream.of(1, 2, 3).collect(NumericVec.toUnmodifiableNumericVec(() -> vec));
//      vec.add(4);
//      assertEquals(NumericVec.ints(1, 2, 3), frozen);
//    }
//
//    @Test
//    public void toUnmodifiableNumericVecParallelInts() {
//      var vec = IntStream.range(0, 1_000_000).parallel()
//          .boxed()
//          .collect(NumericVec.toUnmodifiableNumericVec(NumericVec::ints));
//      assertAll(
//          () -> assertEquals(1_000_000, vec.size()),
//          () -> IntStream.range(0, 1_000_000).forEach(i -> assertEquals(i, vec.get(i)))
//      );
//    }
//
//    @Test
//    public void toUnmodifiableNumericVecParallelLongs() {
//      var vec = LongStream.range(0, 1_000_000).parallel()
//          .boxed()
//          .collect(NumericVec.toUnmodifiableNumericVec(NumericVec::longs));
//      assertAll(
//          () -> assertEquals(1_000_000, vec.size()),
//          () -> IntStream.range(0, 1_000_000).forEach(i -> assertEquals((long) i, vec.get(i)))
//      );
//    }
//
//    @Test
//    public void toUnmodifiableNumericVecParallelDoubles() {
//      var vec = IntStream.range(0, 1_000_000).parallel()
//          .mapToObj(i -> (double) i)
//          .collect(NumericVec.toUnmodifiableNumericVec(NumericVec::doubles));
//      assertAll(
//          () -> assertEquals(1_000_000, vec.size()),
//          () -> IntStream.range(0, 1_000_000).forEach(i -> assertEquals((double) i, vec.get(i)))
//      );
//    }
//
//    @Test
//    public void toUnmodifiableNumericVecPreconditions() {
//      assertThrows(NullPointerException.class, () -> NumericVec.toUnmodifiableNumericVec(null));
//    }
//
//    @Test
//    public void toUnmodifiableNumericVecVersusNull() {
//      assertAll(
//          () -> assertThrows(NullPointerException.class, () -> Stream.of(12, null).collect(NumericVec.toUnmodifiableNumericVec(NumericVec::ints))),
//          () -> assertThrows(NullPointerException.class, () -> Stream.of(12L, null).collect(NumericVec.toUnmodifiableNumericVec(NumericVec::longs))),
//          () -> assertThrows(NullPointerException.class, () -> Stream.of(12., null).collect(NumericVec.toUnmodifiableNumericVec(NumericVec::doubles)))
//      );
//    }
//  }
}