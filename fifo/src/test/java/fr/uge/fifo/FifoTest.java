package fr.uge.fifo;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@SuppressWarnings("static-method")
public class FifoTest {
  @Nested
  public class Q1 {
    @Test
    public void shouldWorkWithIntegers() {
      Fifo<Integer> fifo = new Fifo<Integer>(10);
      fifo.offer(3);
      fifo.offer(14);
      fifo.offer(15);
    }

    @Test
    public void shouldWorkWithStrings() {
      Fifo<String> fifo = new Fifo<String>(10);
      fifo.offer("foo");
      fifo.offer("bar");
    }

    @Test
    public void shouldSizeBeUpdateWhenOfferIsCalled() {
      var fifo = new Fifo<String>(10);
      fifo.offer("foo");
      fifo.offer("bar");
      fifo.offer("baz");
      assertEquals(3, fifo.size());
    }

    @Test
    public void shouldEmptyFifoHasASizeEqualsToZero() {
      var fifo = new Fifo<String>(12);
      assertEquals(0, fifo.size());
    }
  }


  @Nested
  public class Q2 {
    @Test
    public void shouldGetAnErrorWhenCapacityIsNonPositive() {
      assertThrows(IllegalArgumentException.class, () -> new Fifo<>(-3));
    }
    @Test
    public void shouldGetAnErrorWhenCapacityIsZero() {
      assertThrows(IllegalArgumentException.class, () -> new Fifo<>(0));
    }
    @Test
    public void shouldGetAnErrorWhenOfferingNull() {
      var fifo = new Fifo<>(234);
      assertThrows(NullPointerException.class, () -> fifo.offer(null));
    }
  }


  @Nested
  public class Q3 {
    @Test
    public void shouldPollBeCorrectlyTypedUsingIntegers() {
      Fifo<Integer> fifo = new Fifo<Integer>(10);
      fifo.offer(2);
      fifo.offer(21);
      int result = fifo.poll();
      assertEquals(2, result);
    }

    @Test
    public void shouldPollBeCorrectlyTypedUsingStrings() {
      Fifo<String> fifo = new Fifo<String>(10);
      fifo.offer("boo");
      fifo.offer("bah");
      fifo.offer("car");
      String result = fifo.poll();
      assertEquals("boo", result);
    }

    @Test
    public void shouldPollUpdateTheSize() {
      var fifo = new Fifo<Integer>(10);
      fifo.offer(2);
      fifo.offer(21);
      fifo.poll();
      assertEquals(1, fifo.size());
    }

    @Test
    public void shouldPollGetTheValuesInTheRightOrder() {
      var fifo = new Fifo<Integer>(3);
      fifo.offer(1);
      fifo.offer(2);
      fifo.offer(3);
      assertEquals(1, fifo.poll());
      assertEquals(2, fifo.poll());
      assertEquals(3, fifo.poll());
    }

    @Test
    public void shouldGetNullWhenPollingFromAnEmptyFifo() {
      var fifo = new Fifo<>(2);
      assertNull(fifo.poll());
    }

    @Test
    public void shouldGetNullWhenPollingFromAnEmptyFifo2() {
      var fifo = new Fifo<>(1);
      fifo.offer("foo");
      assertEquals("foo", fifo.poll());
      assertNull(fifo.poll());
    }

    @Test
    public void shouldBeAbleToAddToTheCapacityAfterRemoval() {
      var fifo = new Fifo<String>(2);
      fifo.offer("foo");
      fifo.poll();
      fifo.offer("1");
      fifo.offer("2");
    }

    @Test
    public void shouldGetOfferedValueWhenPolling() {
      var fifo = new Fifo<Integer>(2);
      fifo.offer(9);
      assertEquals(9, fifo.poll());
      fifo.offer(2);
      fifo.offer(37);
      assertEquals(2, fifo.poll());
      fifo.offer(12);
      assertEquals(37, fifo.poll());
      assertEquals(12, fifo.poll());
    }

    @Test
    public void shouldBeAbleToMixOfferAndPoll() {
      var fifo = new Fifo<Integer>(2);
      fifo.offer(2);
      fifo.offer(21);
      assertEquals(2, fifo.poll());
      fifo.offer(66);
      assertEquals(21, fifo.poll());
      fifo.offer(134);
      assertEquals(66, fifo.poll());
      assertEquals(134, fifo.poll());
    }

    @Test
    public void shouldGetOfferedValueWhenPollingWithMixedTypes() {
      var fifo = new Fifo<>(40);
      for (var i = 0; i < 20; i++) {
        fifo.offer(i);
      }
      assertEquals(0, fifo.poll());
      fifo.offer("foo");
      for (var i = 1; i < 20; i++) {
        assertEquals(i, fifo.poll());
      }
      assertEquals("foo", fifo.poll());
    }

    @Test
    public void shouldPeekBeCorrectlyTypedUsingIntegers() {
      Fifo<String> fifo = new Fifo<String>(10);
      fifo.offer("foo");
      fifo.offer("whizz");
      String result = fifo.peek();
      assertEquals("foo", result);
    }

    @Test
    public void shouldPeekNotUpdateTheSize() {
      var fifo = new Fifo<Integer>(10);
      fifo.offer(101);
      fifo.offer(21);
      var result = fifo.peek();
      assertEquals(101, result);
      assertEquals(2, fifo.size());
    }

    @Test
    public void shouldGetNullWhenPeekingFromAnEmptyFifo() {
      var fifo = new Fifo<>(2);
      assertNull(fifo.peek());
    }

    @Test
    public void shouldGetNullWhenPeekingFromAnEmptyFifo2() {
      var fifo = new Fifo<>(2);
      fifo.offer("hello");
      assertEquals("hello", fifo.poll());
      assertNull(fifo.peek());
    }
  }


  @Nested
  public class Q4 {
    @Test
    public void shouldNotHaveAMemoryLeak() throws InterruptedException {
      var object = new Object();
      var queue = new ReferenceQueue<>();
      var ref = new WeakReference<>(object, queue);
      var fifo = new Fifo<>(16);
      fifo.offer(object);
      fifo.poll();
      object = null;

      for(var  i = 0; i < 3; i++) {
        System.gc();
        Thread.sleep(100);
      }

      assertSame(ref, queue.remove(1_000));
    }
  }


  @Nested
  public class Q5 {
    @Test
    public void shouldGetACorrectSize() {
      var fifo = new Fifo<String>();
      assertEquals(0, fifo.size());
      fifo.offer("foo");
      assertEquals(1, fifo.size());
      fifo.offer("bar");
      assertEquals(2, fifo.size());
      fifo.poll();
      assertEquals(1, fifo.size());
      fifo.poll();
      assertEquals(0, fifo.size());
    }

    @Test
    public void shouldResize() {
      var fifo = new Fifo<Integer>();
      for(var i = 0; i < 100; i++) {
        fifo.offer(i);
      }
      assertEquals(100, fifo.size());
    }

    @Test
    public void shouldResizeWhenAddingMoreThanCapacityElements() {
      var fifo = new Fifo<String>(1);
      fifo.offer("foo");
      fifo.offer("bar");
      assertEquals(2, fifo.size());
    }

    @Test
    public void shouldKeepElementsInOrderWhenResizing() {
      var fifo = new Fifo<String>(2);
      fifo.offer("foo");
      fifo.poll();
      fifo.offer("bar");
      fifo.offer("baz");
      fifo.offer("bat");
      assertEquals(3, fifo.size());
      assertEquals("bar", fifo.poll());
      assertEquals("baz", fifo.poll());
      assertEquals("bat", fifo.poll());
      assertEquals(0, fifo.size());
    }

    @Test
    @Timeout(200)
    public void shouldResizeALot() {
      var fifo = new Fifo<Integer>(1);
      fifo.offer(-1);
      fifo.poll();
      for (int i = 0; i < 1_000_000; i++) {
        fifo.offer(i);
      }
      for (int i = 0; i < 1_000_000; i++) {
        assertEquals(i, fifo.poll());
      }
    }
  }


  @Nested
  public class Q6 {
    @Test
    public void shouldPrintEmptyFifo() {
      var fifo = new Fifo<>();
      assertEquals("[]", "" + fifo);
    }

    @Test
    public void shouldPrintFifoWithOneElement() {
      var fifo = new Fifo<String>();
      fifo.offer("joe");
      assertEquals("[joe]", "" + fifo);
    }

    @Test
    public void shouldPrintFifoWithTwoElements() {
      var fifo = new Fifo<Integer>();
      fifo.offer(1456);
      fifo.offer(8390);
      assertEquals("[1456, 8390]", "" + fifo);
    }

    @Test
    public void shouldPrintFifoInTheSameWayAsAList() {
      var fifo = new Fifo<Integer>();
      var list = new ArrayList<Integer>();
      for (var i = 0; i < 99; i++) {
        fifo.offer(i);
        list.add(i);
      }
      assertEquals(list.toString(), "" + fifo);
    }

    @Test
    public void shouldNotAffectFifoWhenPrinting() {
      var fifo = new Fifo<Integer>();
      for (var i = 0; i < 100; i++) {
        fifo.offer(i);
      }
      assertNotNull("" + fifo);
      for (var i = 0; i < 100; i++) {
        assertEquals(i, (int) fifo.poll());
      }
    }

    @Test
    public void shouldBeAbleToAPrintEvenIfFull() {
      var fifo = new Fifo<String>(2);
      fifo.offer("foo");
      fifo.poll();
      fifo.offer("1");
      fifo.offer("2");
      assertEquals("[1, 2]", "" + fifo);
    }
  }


  @Nested
  public class Q7 {
    // This test needs a lot of memory (more than 8 gigs) and is slow,
    // so it is disabled by default
    // use the option -Xmx9g when running the VM
//    @Test
//    public void shouldNotGetAnOverflowErrorWhenCallingToStringOverAnAlmostMaximalCapacityFifo() {
//      var length = Integer.MAX_VALUE - 12;
//      var length2 = 1024;
//      var fifo = new Fifo<String>(Integer.MAX_VALUE - 8);
//
//      for(var i = 0; i < length; i++) {
//        fifo.offer("A");
//        fifo.poll();
//      }
//
//      for(var i = 0; i < length2; i++) {
//        fifo.offer("B");
//      }
//      var text = fifo.toString();
//      assertNotNull(text);
//    }
  }


  @Nested
  public class Q8 {

    @Test
    public void shouldGetTheRightTypeOfIterator() {
      var fifo = new Fifo<String>();
      Iterator<String> it = fifo.iterator();
      assertNotNull(it);
    }

    @Test
    public void shouldGetAnErrorWhenAskingNextWhenDoesNotHaveNext() {
      var fifo = new Fifo<String>();
      fifo.offer("bar");
      fifo.poll();
      var it = fifo.iterator();
      assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    public void shouldNotGetSideEffectsWhenUsingIteratorHasNext() {
      var fifo = new Fifo<Integer>();
      fifo.offer(117);
      fifo.offer(440);
      var it = fifo.iterator();
      assertTrue(it.hasNext());
      assertTrue(it.hasNext());
      assertEquals(117, (int) it.next());
      assertTrue(it.hasNext());
      assertTrue(it.hasNext());
      assertEquals(440, (int) it.next());
      assertFalse(it.hasNext());
      assertFalse(it.hasNext());
    }

    @Test
    public void shouldIterateProperlyWhenTheNumberofOffersOvertakesOriginalCapacity() {
      var fifo = new Fifo<Integer>();
      fifo.offer(42);
      fifo.poll();
      fifo.offer(55);
      fifo.offer(333);
      var it = fifo.iterator();
      assertTrue(it.hasNext());
      assertTrue(it.hasNext());
      assertEquals(55, (int) it.next());
      assertTrue(it.hasNext());
      assertTrue(it.hasNext());
      assertEquals(333, (int) it.next());
      assertFalse(it.hasNext());
      assertFalse(it.hasNext());
    }

    @Test
    public void shouldBeAbleToIterateTwice() {
      var fifo = new Fifo<Integer>();
      fifo.offer(898);

      var it = fifo.iterator();
      assertTrue(it.hasNext());
      assertTrue(it.hasNext());
      assertEquals(898, (int) it.next());
      assertFalse(it.hasNext());
      assertFalse(it.hasNext());
      var it2 = fifo.iterator();
      assertTrue(it2.hasNext());
      assertTrue(it2.hasNext());
      assertEquals(898, (int) it2.next());
      assertFalse(it2.hasNext());
      assertFalse(it2.hasNext());
    }

    @Test
    public void shouldGetConsistentAnswersFromHasNextWhenEmpty() {
      var fifo = new Fifo<>();
      var it = fifo.iterator();
      assertFalse(it.hasNext());
      assertFalse(it.hasNext());
      assertFalse(it.hasNext());
    }

    @Test
    public void shouldGetConsistentAnswersFromHasNextWhenNotEmpty() {
      var fifo = new Fifo<>();
      fifo.offer("one");
      var it = fifo.iterator();
      assertTrue(it.hasNext());
      assertTrue(it.hasNext());
      assertTrue(it.hasNext());
    }

    @Test
    public void shouldIterateOverALargeNumberOfElements() {
      var fifo = new Fifo<Integer>();
      for (int i = 0; i < 10_000; i++) {
        fifo.offer(i);
      }
      var i = 0;
      var it = fifo.iterator();
      while (it.hasNext()) {
        assertEquals(i++, (int) it.next());
      }
      assertEquals(10_000, fifo.size());
    }

    @Test
    public void shouldGetAnErrorWhenTryingToUseIteratorRemove() {
      var fifo = new Fifo<String>();
      fifo.offer("foooo");
      assertThrows(UnsupportedOperationException.class, () -> fifo.iterator().remove());
    }
  }


  @Nested
  public class Q9 {
    @Test
    public void shouldBeAbleToUseImplicitForEachLoopWithIntegers() {
      var fifo = new Fifo<Integer>();
      fifo.offer(42);
      for (int value : fifo) {
        assertEquals(42, value);
      }
    }

    @Test
    public void shouldBeAbleToUseImplicitForEachLoopWithStrings() {
      var fifo = new Fifo<String>();
      fifo.offer("loop");
      fifo.offer("loop");
      for (String value : fifo) {
        assertEquals("loop", value);
      }
    }

    @Test
    public void shouldBeAbleToUseImplicitForEachLoopEvenIfEmpty() {
      var fifo = new Fifo<Integer>();
      for (var value : fifo) {
        fail();
      }
    }

    @Test
    public void shouldBeAbleToUseImplicitForEachLoop() {
      var fifo = new Fifo<Integer>();
      fifo.offer(222);
      fifo.poll();

      for (var i = 0; i < 100; i++) {
        fifo.offer(i);
      }
      var i = 0;
      for (int value : fifo) {
        assertEquals(i++, value);
      }
      assertEquals(100, fifo.size());
    }
  }


  @Nested
  public class Q10 {
    @Test
    public void shouldBeAQueue() {
      Queue<Integer> fifo = new Fifo<Integer>();
      for (var i = 0; i < 5; i++) {
        assertTrue(fifo.offer(i));
      }
      assertEquals(5, fifo.size());
      for (var i = 0; i < 5; i++) {
        assertEquals(i, fifo.poll());
      }
      assertEquals(0, fifo.size());
    }

    @Test
    public void shouldSupportIsEmpty() {
      Queue<String> fifo = new Fifo<>();
      assertTrue(fifo.isEmpty());
      fifo.add("foo");
      assertFalse(fifo.isEmpty());
    }

    @Test
    public void shouldSupportAdd() {
      Queue<Integer> fifo = new Fifo<>();
      for (var i = 0; i < 5; i++) {
        assertTrue(fifo.add(i));
      }
      assertEquals(5, fifo.size());
    }

    @Test
    public void shouldSupportRemove() {
      Queue<String> fifo = new Fifo<>();
      fifo.add("foo");
      assertEquals("foo", fifo.remove());
    }

    @Test
    public void shouldSupportRemoveWhenEmpty() {
      Queue<Integer> fifo = new Fifo<>();
      assertThrows(NoSuchElementException.class, fifo::remove);
    }

    @Test
    public void shouldSupportContains() {
      Queue<String> fifo = new Fifo<>();
      fifo.add("foo");
      assertTrue(fifo.contains("foo"));
      assertFalse(fifo.contains(3.14));
    }

    @Test
    public void shouldSupportClear() {
      Queue<Integer> fifo = new Fifo<>();
      for(var i = 0; i < 5; i++) {
        fifo.add(i);
      }
      fifo.clear();
      assertEquals(0, fifo.size());
    }

    @Test
    public void shouldSupportToArrayObject() {
      Queue<String> fifo = new Fifo<>();
      fifo.add("foo");
      fifo.add("bar");
      fifo.add("baz");
      Object[] array = fifo.toArray();
      assertArrayEquals(new Object[] {"foo", "bar", "baz"}, array);
    }

    @Test
    public void shouldSupportToArrayWithAnArray() {
      Queue<String> fifo = new Fifo<>();
      fifo.add("foo");
      fifo.add("bar");
      fifo.add("baz");
      String[] array = fifo.toArray(new String[0]);
      assertArrayEquals(new String[] {"foo", "bar", "baz"}, array);
    }

    @Test
    public void shouldSupportToArrayWithAGenerator() {
      Queue<String> fifo = new Fifo<>();
      fifo.add("foo");
      fifo.add("bar");
      fifo.add("baz");
      String[] array = fifo.toArray(String[]::new);
      assertArrayEquals(new String[] {"foo", "bar", "baz"}, array);
    }

    // This test needs a lot of memory (more than 8 gigs) and is slow,
    // so it is disabled by default
    // use the option -Xmx9g when running the VM
//    @Test
//    public void shouldNotGetAnOverflowErrorWhenIteratingOverAnAlmostMaximalCapacityFifo() {
//      var length = Integer.MAX_VALUE - 12;
//      var length2 = 1024;
//      var fifo = new Fifo<Integer>(Integer.MAX_VALUE - 8);
//      for(var i = 0; i < length; i++) {
//        fifo.offer(17);
//        fifo.poll();
//      }
//      for(var i = 0; i < length2; i++) {
//        fifo.offer(42);
//      }
//      var counter = 0;
//      for(var it = fifo.iterator(); it.hasNext();) {
//        assertEquals(42, it.next());
//        counter++;
//      }
//      assertEquals(fifo.size(), counter);
//    }
  }
}
