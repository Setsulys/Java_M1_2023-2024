package fr.uge.slice;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Slice3Test {
  @Nested
  public class Q1 {
    @Test
    public void sliceArray() {
      String[] array = new String[] { "foo", "bar" };
      Slice3<String> slice = Slice3.array(array);
      assertAll(
          () -> assertEquals(2, slice.size()),
          () -> assertEquals("foo", slice.get(0)),
          () -> assertEquals("bar", slice.get(1))
      );
    }

    @Test
    public void sliceArrayAllowNull() {
      Integer[] array = new Integer[] { 6, 7, null };
      Slice3<Integer> slice = Slice3.array(array);
      assertAll(
          () -> assertEquals(3, slice.size()),
          () -> assertEquals(6, slice.get(0)),
          () -> assertEquals(7, slice.get(1)),
          () -> assertNull(slice.get(2))
      );
    }

    @Test
    public void sliceArrayEmpty() {
      var array = new Object[] { };
      var slice = Slice3.array(array);
      assertEquals(0, slice.size());
    }

    @Test
    public void sliceArrayIsAView() {
      var array = new String[] { "foo", "bar", "baz" };
      var slice = Slice3.array(array);
      array[0] = "zorg";
      assertAll(
          () -> assertEquals(3, slice.size()),
          () -> assertEquals("zorg", slice.get(0)),
          () -> assertEquals("bar", slice.get(1)),
          () -> assertEquals("baz", slice.get(2))
      );
    }

    @Test
    public void sliceArrayPrecondition() {
      assertThrows(NullPointerException.class, () -> Slice3.array(null));
    }

    @Test
    public void sliceArrayGetPreconditions() {
      var array = new Double[] { 42.5 };
      var slice = Slice3.array(array);
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.get(1))
      );
    }

    @Test
    public void sliceArrayNoImplementationVisible() {
      assertEquals(0, Slice3.class.getDeclaredClasses().length);
    }
  }


  @Nested
  public class Q2 {
    @Test
    public void sliceArrayPrinted() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice3.array(array);
      assertEquals("[foo, bar]", "" + slice);
    }

    @Test
    public void sliceArrayEmptyPrinted() {
      var array = new LocalDate[] {};
      var slice = Slice3.array(array);
      assertEquals("[]", "" + slice);
    }

    @Test
    public void sliceArrayPrintedAllowsNull() {
      var array = new Double[] { null, 42.2};
      var slice = Slice3.array(array);
      assertEquals("[null, 42.2]", "" + slice);
    }
  }


  @Nested
  public class Q3 {
    @Test
    public void sliceArrayFromTo() {
      String[] array = new String[] { "foo", "bar", "baz", "whizz" };
      Slice3<String> slice = Slice3.array(array, 1, 3);
      assertAll(
          () -> assertEquals(2, slice.size()),
          () -> assertEquals("bar", slice.get(0)),
          () -> assertEquals("baz", slice.get(1))
      );
    }

    @Test
    public void sliceArrayFromToAllowNull() {
      Integer[] array = new Integer[] { 6, null, 7, 9, 11 };
      Slice3<Integer> slice = Slice3.array(array, 1, 4);
      assertAll(
          () -> assertEquals(3, slice.size()),
          () -> assertNull(slice.get(0)),
          () -> assertEquals(7, slice.get(1)),
          () -> assertEquals(9, slice.get(2))
      );
    }

    @Test
    public void sliceArrayFromToEmpty() {
      var array = new Object[] { };
      var slice = Slice3.array(array, 0, 0);
      assertEquals(0, slice.size());
    }

    @Test
    public void sliceArrayFromToIsAView() {
      var array = new String[] { "foo", "bar", "baz" };
      var slice = Slice3.array(array, 1, 3);
      array[1] = "zorg";
      assertAll(
          () -> assertEquals(2, slice.size()),
          () -> assertEquals("zorg", slice.get(0)),
          () -> assertEquals("baz", slice.get(1))
      );
    }

    @Test
    public void sliceArrayFromToPreconditions() {
      var array = new String[] { "foo", "bar", "baz" };
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> Slice3.array(null, 0, 0)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> Slice3.array(array, -1, 1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> Slice3.array(array, 1, -1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> Slice3.array(array, 0, 4)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> Slice3.array(array, 2, 1))
      );
    }

    @Test
    public void sliceArrayFromToGetPreconditions() {
      var array = new Double[] { 1.0, 2.0, 3.0, 4.0 };
      var slice = Slice3.array(array, 1, 4);
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.get(3))
      );
    }

    @Test
    public void sliceArrayFromToPrinted() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice3.array(array, 0, 1);
      assertEquals("[foo]", "" + slice);
    }

    @Test
    public void sliceArrayFromToEmptyPrinted() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice3.array(array, 1, 1);
      assertEquals("[]", "" + slice);
    }

    @Test
    public void sliceArrayFromToPrintedAllowsNull() {
      var array = new Double[] { 8.5, 9.2, null, 42.2};
      var slice = Slice3.array(array, 1, 4);
      assertEquals("[9.2, null, 42.2]", "" + slice);
    }
  }


  @Nested
  public class Q4 {
    @Test
    public void subArraySlice() {
      String[] array = new String[] { "foo", "bar", "baz", "whizz" };
      var slice = Slice3.array(array);
      Slice3<String> slice2 = slice.subSlice(1, 3);
      assertAll(
          () -> assertEquals(2, slice2.size()),
          () -> assertEquals("bar", slice2.get(0)),
          () -> assertEquals("baz", slice2.get(1))
      );
    }

    @Test
    public void subArraySliceAllowNull() {
      Integer[] array = new Integer[] { 6, null, 7, 9, 11 };
      var slice = Slice3.array(array);
      Slice3<Integer> slice2 = slice.subSlice(1, 4);
      assertAll(
          () -> assertEquals(3, slice2.size()),
          () -> assertNull(slice2.get(0)),
          () -> assertEquals(7, slice2.get(1)),
          () -> assertEquals(9, slice2.get(2))
      );
    }

    @Test
    public void subArraySliceEmpty() {
      var array = new Object[] { };
      var slice = Slice3.array(array).subSlice(0, 0);
      assertEquals(0, slice.size());
    }

    @Test
    public void subArraySliceIsAView() {
      var array = new String[] { "foo", "bar", "baz" };
      var slice = Slice3.array(array).subSlice(1, 3);
      array[1] = "zorg";
      assertAll(
          () -> assertEquals(2, slice.size()),
          () -> assertEquals("zorg", slice.get(0)),
          () -> assertEquals("baz", slice.get(1))
      );
    }

    @Test
    public void subArraySliceSubSlice() {
      String[] array = new String[] { "foo", "bar", "baz", "whizz" };
      var slice = Slice3.array(array);
      Slice3<String> slice2 = slice.subSlice(1, 3).subSlice(1, 2);
      assertAll(
          () -> assertEquals(1, slice2.size()),
          () -> assertEquals("baz", slice2.get(0))
      );
    }

    @Test
    public void subArraySliceSubSliceAllowNull() {
      Integer[] array = new Integer[] { 6, null, 7, 9, 11 };
      var slice = Slice3.array(array);
      Slice3<Integer> slice2 = slice.subSlice(1, 4).subSlice(0, 2);
      assertAll(
          () -> assertEquals(2, slice2.size()),
          () -> assertNull(slice2.get(0)),
          () -> assertEquals(7, slice2.get(1))
      );
    }

    @Test
    public void subArraySliceSubSliceEmptyEmpty() {
      var array = new Object[] { };
      var slice = Slice3.array(array).subSlice(0, 0).subSlice(0, 0);
      assertEquals(0, slice.size());
    }

    @Test
    public void subArraySliceSubSliceIsAView() {
      var array = new String[] { "foo", "bar", "baz" };
      var slice = Slice3.array(array).subSlice(1, 3).subSlice(0, 2);
      array[1] = "zorg";
      assertAll(
          () -> assertEquals(2, slice.size()),
          () -> assertEquals("zorg", slice.get(0)),
          () -> assertEquals("baz", slice.get(1))
      );
    }

    @Test
    public void subArraySlicePreconditions() {
      var array = new String[] { "foo", "bar", "baz" };
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> Slice3.array(array).subSlice( -1, 1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> Slice3.array(array).subSlice(1, -1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> Slice3.array(array).subSlice(0, 4)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> Slice3.array(array).subSlice(2, 1))
      );
    }

    @Test
    public void subArraySliceSubSlicePreconditions() {
      var array = new Double[] { 10.0, 20.0, 30.0, 40.0 };
      var slice = Slice3.array(array);
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> Slice3.array(array).subSlice(1, 3).subSlice( -1, 1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> Slice3.array(array).subSlice(1, 3).subSlice(1, -1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> Slice3.array(array).subSlice(1, 3).subSlice(0, 3)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> Slice3.array(array).subSlice(1, 3).subSlice(2, 1))
      );
    }

    @Test
    public void subArraySliceGetPreconditions() {
      var array = new Double[] { 1.0, 2.0, 3.0, 4.0 };
      var slice = Slice3.array(array);
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.subSlice(1, 4).get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.subSlice(1, 4).get(3))
      );
    }

    @Test
    public void subArraySliceSubSliceGetPreconditions() {
      var array = new Double[] { 10.0, 20.0, 30.0, 40.0 };
      var slice = Slice3.array(array);
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> Slice3.array(array).subSlice(1, 3).subSlice(0, 2).get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> Slice3.array(array).subSlice(1, 3).subSlice(0, 1).get(1))
      );
    }

    @Test
    public void subArraySlicePrinted() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice3.array(array).subSlice(0, 1);
      assertEquals("[foo]", "" + slice);
    }

    @Test
    public void subArraySliceEmptyPrinted() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice3.array(array).subSlice(1, 1);
      assertEquals("[]", "" + slice);
    }

    @Test
    public void subArraySlicePrintedAllowsNull() {
      var array = new Double[] { 8.5, 9.2, null, 42.2};
      var slice = Slice3.array(array).subSlice(1, 4);
      assertEquals("[9.2, null, 42.2]", "" + slice);
    }

    @Test
    public void subArraySliceSubSlicePrinted() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice3.array(array).subSlice(0, 2).subSlice(1, 2);
      assertEquals("[bar]", "" + slice);
    }

    @Test
    public void subArraySliceSubSliceEmptyPrinted() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice3.array(array).subSlice(1, 2).subSlice(1, 1);
      assertEquals("[]", "" + slice);
    }

    @Test
    public void subArraySliceSubSliceEmptyEmptyPrinted() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice3.array(array).subSlice(1, 1).subSlice(0, 0);
      assertEquals("[]", "" + slice);
    }

    @Test
    public void subArraySliceSubSlicePrintedAllowsNull() {
      var array = new Double[] { 8.5, 9.2, null, 42.2};
      var slice = Slice3.array(array).subSlice(1, 4).subSlice(1, 2);
      assertEquals("[null]", "" + slice);
    }
  }
}