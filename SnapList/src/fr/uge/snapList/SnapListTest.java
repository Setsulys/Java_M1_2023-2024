package fr.uge.snapList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class SnapListTest {

  @Nested
  class Q1 {
    @Test
    public void simple() {
      var snapList = new SnapList<Integer>(__ -> 0);
      snapList.add(10);
      snapList.add(20);
      snapList.add(30);
      assertEquals(3, snapList.elementSize());
      assertEquals("[10 | 20 | 30]", snapList.toString());
    }

    @Test
    public void listOfString() {
      var snapList = new SnapList<String>(__ -> "");
      snapList.add("1");
      snapList.add("2");
      snapList.add("3");
      assertEquals(3, snapList.elementSize());
      assertEquals("[1 | 2 | 3]", snapList.toString());
    }

    @Test
    public void listEmpty() {
      var snapList = new SnapList<>(__ -> fail());
      assertEquals(0, snapList.elementSize());
      assertEquals("[]", snapList.toString());
    }

    @Test
    public void listOneElement() {
      var snapList = new SnapList<Double>(__ -> 0.0);
      snapList.add(42.0);
      assertEquals(1, snapList.elementSize());
      assertEquals("[42.0]", snapList.toString());
    }

    @Test
    public void listSummaryFunction() {
      var snapList = new SnapList<String>((List<String> list) -> "");
      assertNotNull(snapList);
      var snapList2 = new SnapList<CharSequence>((List<CharSequence> list) -> "");
      assertNotNull(snapList2);
    }

    @Test
    public void preconditions() {
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> new SnapList<String>(null)),
          () -> assertThrows(NullPointerException.class, () -> new SnapList<String>(__ -> "").add(null))
      );
    }
  }

  @Nested
  class Q2 {
    @Test
    public void simple() {
      var snapList = new SnapList<Integer>(__ -> 0);
      snapList.add(10);
      snapList.add(20);
      snapList.snapshot();
      snapList.add(30);
      snapList.snapshot();
      assertFalse(snapList.canSnapshot());
      assertEquals(3, snapList.elementSize());
      assertEquals("[10 | 20 | 30]", snapList.toString());
    }

    @Test
    public void mixContent() {
      var snapList = new SnapList<Double>(__ -> 0.0);
      snapList.add(10.0);
      snapList.add(20.0);
      assertTrue(snapList.canSnapshot());
      snapList.snapshot();
      assertFalse(snapList.canSnapshot());
      snapList.add(30.0);
      snapList.add(40.0);
      assertEquals(4, snapList.elementSize());
      assertEquals("[10.0 | 20.0 | 30.0 | 40.0]", snapList.toString());
    }

    @Test
    public void snapshotFunction() {
      var snapshotSubLists = new ArrayList<List<Integer>>();
      var snapList = new SnapList<Integer>(list -> {
        snapshotSubLists.add(list);
        return list.stream().mapToInt(v -> v).sum();
      });
      snapList.add(10);
      snapList.add(20);
      snapList.snapshot();
      snapList.add(30);
      snapList.snapshot();
      assertEquals(List.of(List.of(10, 20), List.of(30)), snapshotSubLists);
    }

    @Test
    public void empty() {
      var snapList = new SnapList<>(__ -> fail());
      assertFalse(snapList.canSnapshot());
      assertEquals(0, snapList.elementSize());
    }

    @Test
    public void snapshotNonEmpty() {
      assertThrows(IllegalStateException.class, () -> new SnapList<String>(__ -> "").snapshot());
    }

    @Test
    public void snapshotNonEmpty2() {
      var snapList = new SnapList<>(__ -> "0");
      snapList.add("314");
      assertTrue(snapList.canSnapshot());
      snapList.snapshot();
      assertFalse(snapList.canSnapshot());
      assertThrows(IllegalStateException.class, snapList::snapshot);
    }
  }

  @Nested
  class Q3 {
    @Test
    public void example() {
//      var snapList = new SnapList<>(List::size);
//      snapList.add(10);
//      snapList.add(20);
//      snapList.add(30);
//      System.out.println(snapList.elementSize());  // 3
//      snapList.snapshot();
//      snapList.add(40);
//      snapList.snapshot();
//      System.out.println(snapList.elementSize());  // 4
//      System.out.println(snapList);   // [10 | 20 | 30 | 40]
//      System.out.println(snapList.snapshotList());  //  [3, 1]

      var snapList = new SnapList<>(List::size);
      snapList.add(10);
      snapList.add(20);
      snapList.add(30);
      assertEquals(3, snapList.elementSize());
      snapList.snapshot();
      assertEquals(3, snapList.elementSize());
      snapList.add(40);
      snapList.snapshot();
      assertEquals(4, snapList.elementSize());
      assertEquals("[10 | 20 | 30 | 40]", snapList.toString());
      assertEquals(List.of(3, 1), snapList.snapshotList());
    }

    @Test
    public void snapshotList() {
      var snapList = new SnapList<Integer>(list -> list.stream().mapToInt(v -> v).sum());
      snapList.add(3);
      snapList.add(4);
      snapList.snapshot();
      snapList.add(10);
      assertEquals(List.of(7), snapList.snapshotList());
    }

    @Test
    public void empty() {
      var snapList = new SnapList<>(__ -> fail());
      assertEquals(List.of(), snapList.snapshotList());
    }

    @Test
    public void noSnapshot() {
      var snapList = new SnapList<String>(__ -> fail());
      snapList.add("foo");
      snapList.add("bar");
      snapList.add("baz");
      assertEquals(List.of(), snapList.snapshotList());
    }

    @Test
    public void snapshotFunctionListIsNonModifiable() {
      var snapList = new SnapList<Integer>(list -> {
        var list2 = (List<Integer>) list;
        assertThrows(UnsupportedOperationException.class, () -> list2.add(1));
        assertThrows(UnsupportedOperationException.class, () -> list2.set(0, 1));
        return 42;
      });
      snapList.add(100);
      snapList.snapshot();
      snapList.add(200);
      snapList.snapshot();
      snapList.add(300);
      snapList.snapshot();
      assertEquals(List.of(42, 42, 42), snapList.snapshotList());
    }

    @Test
    public void snapshotListNonModifiable() {
      var snapList = new SnapList<Boolean>(list -> list.stream().allMatch(Boolean.TRUE::equals));
      snapList.add(true);
      snapList.add(true);
      snapList.snapshot();
      var snapshotList = snapList.snapshotList();
      snapList.add(true);
      snapList.snapshot();
      assertEquals(List.of(true), snapshotList);
    }

    @Test
    public void snapshotListOutOfBounds() {
      var snapList = new SnapList<Integer>(List::size);
      snapList.add(13);
      snapList.add(56);
      snapList.snapshot();
      var snapshotList = snapList.snapshotList();
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> snapshotList.get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> snapshotList.get(2))
      );
    }

    @Test
    @Timeout(500)
    public void snapshotALot() {
      var snapList = new SnapList<Integer>(List::size);
      IntStream.range(0, 100_000).forEach(snapList::add);
      snapList.snapshot();
      IntStream.range(0, 700_000).forEach(snapList::add);
      snapList.snapshot();
      assertEquals(List.of(100_000, 700_000), snapList.snapshotList());
    }

    @Test
    @Timeout(500)
    public void snapshotALot2() {
      var snapList = new SnapList<Integer>(List::size);
      IntStream.range(0, 100_000).forEach(i -> {
        snapList.add(i);
        snapList.snapshot();
      });
      assertEquals(Collections.nCopies(100_000, 1), snapList.snapshotList());
    }
  }

  @Nested
  class Q5 {
    @Test
    public void forEach() {
      var snapList = new SnapList<Integer>(l -> l.stream().mapToInt(v -> v).sum());
      snapList.add(36);
      snapList.add(4);
      snapList.snapshot();
      snapList.add(24);
      var box = new Object() { int sum; };
      snapList.forEach(v -> box.sum += v);
      assertEquals(64, box.sum);
    }

    @Test
    public void forEachEmpty() {
      var snapList = new SnapList<String>(__ -> fail());
      snapList.forEach((CharSequence __) -> fail());
    }

    @Test
    public void forEachValuesNoSummary() {
      var snapList = new SnapList<String>(__ -> fail());
      snapList.add("foo");
      snapList.add("bar");
      snapList.add("baz");
      var list = new ArrayList<String>();
      snapList.forEach(list::add);
      assertEquals(List.of("foo", "bar", "baz"), list);
    }

    @Test
    @Timeout(500)
    public void forEachValuesALot() {
      var snapList = new SnapList<Integer>(__ -> fail());
      IntStream.range(0, 1_000_000).forEach(snapList::add);
      var list = new ArrayList<Integer>();
      snapList.forEach(list::add);
      assertEquals(IntStream.range(0, 1_000_000).boxed().toList(), list);
    }

    @Test
    public void forEachSummaries() {
      var snapList = new SnapList<String>(list -> list.get(0));
      snapList.add("foo");
      snapList.snapshot();
      snapList.add("bar");
      snapList.snapshot();
      snapList.add("baz");
      snapList.snapshot();
      var list = new ArrayList<String>();
      snapList.forEach(list::add);
      assertEquals(List.of("foo", "bar", "baz"), list);
    }

    @Test
    @Timeout(500)
    public void forEachSummariesALot() {
      var snapList = new SnapList<Integer>(list -> list.get(0));
      IntStream.range(0, 1_000_000).forEach(v -> {
        snapList.add(v);
        snapList.snapshot();
      });
      var list = new ArrayList<Integer>();
      snapList.forEach(list::add);
      assertEquals(IntStream.range(0, 1_000_000).boxed().toList(), list);
    }

    @Test
    public void forEachMix() {
      var snapList = new SnapList<String>(list -> list.stream().reduce("", String::concat));
      snapList.add("foo");
      snapList.add("bar");
      snapList.snapshot();
      snapList.add("baz");
      snapList.add("whizz");
      var list = new ArrayList<String>();
      snapList.forEach(list::add);
      assertEquals(List.of("foobar", "baz", "whizz"), list);
    }

    @Test
    public void precondition() {
      var snapList = new SnapList<>(__ -> fail());
      assertThrows(NullPointerException.class, () -> snapList.forEach(null));
    }
  }

  @Nested
  class Q6 {
    @Test
    public void iterator() {
      var snapList = new SnapList<Integer>(l -> l.stream().mapToInt(v -> v).sum());
      snapList.add(36);
      snapList.add(4);
      snapList.snapshot();
      snapList.add(24);
      var box = new Object() { int sum; };
      snapList.iterator().forEachRemaining(v -> box.sum += v);
      assertEquals(64, box.sum);
    }

    @Test
    public void iterator2() {
      var snapList = new SnapList<Integer>(List::size);
      snapList.add(10);
      snapList.add(20);
      snapList.snapshot();
      snapList.add(30);
      snapList.add(40);
      snapList.add(50);
      var list = new ArrayList<Integer>();
      snapList.iterator().forEachRemaining(list::add);
      assertEquals(List.of(2, 30, 40, 50), list);
    }

    @Test
    public void iteratorEmpty() {
      var snapList = new SnapList<String>(__ -> fail());
      var it = snapList.iterator();
      assertFalse(it.hasNext());
      assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    public void iteratorValuesNoSummary() {
      var snapList = new SnapList<String>(__ -> fail());
      snapList.add("foo");
      snapList.add("bar");
      snapList.add("baz");
      var list = new ArrayList<String>();
      snapList.iterator().forEachRemaining(list::add);
      assertEquals(List.of("foo", "bar", "baz"), list);
    }

    @Test
    @Timeout(500)
    public void iteratorValuesALot() {
      var snapList = new SnapList<Integer>(__ -> fail());
      IntStream.range(0, 1_000_000).forEach(snapList::add);
      var list = new ArrayList<Integer>();
      snapList.iterator().forEachRemaining(list::add);
      assertEquals(IntStream.range(0, 1_000_000).boxed().toList(), list);
    }

    @Test
    public void iteratorSummaries() {
      var snapList = new SnapList<String>(list -> list.get(0));
      snapList.add("foo");
      snapList.snapshot();
      snapList.add("bar");
      snapList.snapshot();
      snapList.add("baz");
      snapList.snapshot();
      var list = new ArrayList<String>();
      snapList.iterator().forEachRemaining(list::add);
      assertEquals(List.of("foo", "bar", "baz"), list);
    }

    @Test
    @Timeout(500)
    public void iteratorSummariesALot() {
      var snapList = new SnapList<Integer>(list -> list.get(0));
      IntStream.range(0, 1_000_000).forEach(v -> {
        snapList.add(v);
        snapList.snapshot();
      });
      var list = new ArrayList<Integer>();
      snapList.iterator().forEachRemaining(list::add);
      assertEquals(IntStream.range(0, 1_000_000).boxed().toList(), list);
    }

    @Test
    public void iteratorMix() {
      var snapList = new SnapList<String>(list -> list.stream().reduce("", String::concat));
      snapList.add("foo");
      snapList.add("bar");
      snapList.snapshot();
      snapList.add("baz");
      snapList.add("whizz");
      var list = new ArrayList<String>();
      snapList.iterator().forEachRemaining(list::add);
      assertEquals(List.of("foobar", "baz", "whizz"), list);
    }

    @Test
    public void iteratorOnlyNext() {
      var snapList = new SnapList<Integer>(List::size);
      snapList.add(10);
      snapList.add(20);
      var iterator = snapList.iterator();
      assertEquals(10, iterator.next());
      assertEquals(20, iterator.next());
      assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void iteratorRemove() {
      var snapList = new SnapList<Integer>(List::size);
      snapList.add(10);
      var iterator = snapList.iterator();
      assertThrows(UnsupportedOperationException.class, iterator::remove);
    }
  }

  @Nested
  class Q7 {
//    @Test
//    public void iteratorAddCME() {
//      var snapList = new SnapList<Integer>(List::size);
//      snapList.add(10);
//      var iterator = snapList.iterator();
//      snapList.add(20);
//      assertThrows(ConcurrentModificationException.class, iterator::next);
//    }

    @Test
    public void iteratorSummaryCME() {
      var snapList = new SnapList<Integer>(List::size);
      snapList.add(10);
      snapList.add(20);
      var iterator = snapList.iterator();
      snapList.snapshot();
      assertThrows(ConcurrentModificationException.class, iterator::next);
    }

    @Test
    public void iteratorSummaryList() {
      var snapList = new SnapList<Integer>(List::size);
      snapList.add(10);
      snapList.add(20);
      var iterator = snapList.iterator();
      snapList.snapshotList();
      assertEquals(10, iterator.next());
    }

    @Test
    public void iteratorForEach() {
      var snapList = new SnapList<Integer>(List::size);
      snapList.add(10);
      snapList.add(20);
      var iterator = snapList.iterator();
      snapList.forEach(__ -> {});
      assertEquals(10, iterator.next());
    }

    @Test
    public void iteratorAndASubsequentIterator() {
      var snapList = new SnapList<Integer>(List::size);
      snapList.add(10);
      snapList.add(20);
      var iterator = snapList.iterator();
      var iterator2 = snapList.iterator();
      assertNotNull(iterator2);
      assertEquals(10, iterator.next());
    }
  }

  @Nested
  class Q8 {
    @Test
    public void autoSnapshot() {
      var snapList = new SnapList<Integer>(List::size);
      snapList.autoSnapshot(2);
      snapList.add(10);
      snapList.add(20);
      assertEquals(List.of(2), snapList.snapshotList());
    }

    @Test
    @Timeout(500)
    public void autoSnapshotALot() {
      var snapList = new SnapList<Integer>(list -> list.get(0));
      snapList.autoSnapshot(1);
      IntStream.range(0, 1_000_000).forEach(snapList::add);
      assertEquals(IntStream.range(0, 1_000_000).boxed().toList(), snapList.snapshotList());
    }

    @Test
    public void preconditions() {
      var snapList = new SnapList<Integer>(List::size);
      assertThrows(IllegalArgumentException.class, () -> snapList.autoSnapshot(-1));

      snapList.autoSnapshot(10);
      assertThrows(IllegalStateException.class, () -> snapList.autoSnapshot(3));
    }
  }
}