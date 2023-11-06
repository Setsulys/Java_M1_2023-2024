package fr.uge.graph;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.AccessFlag;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("static-method")
public class GraphTest {
  @Nested
  public class Q1 {
    @Test
    public void createGraphOfIntegers() {
      MatrixGraph<Integer> graph = new MatrixGraph<Integer>(50);
      assertEquals(50, graph.nodeCount());
    }

    @Test
    public void createGraphOfStrings() {
      Graph<String> graph = new MatrixGraph<String>(25);
      assertEquals(25, graph.nodeCount());
    }

    @Test
    public void createEmptyGraph() {
      var graph = new MatrixGraph<>(0);
      assertEquals(0, graph.nodeCount());
    }

    @Test
    public void graphIsNotAnOpenInterface() {
      assertAll(
          () -> assertTrue(Graph.class.isInterface()),
          () -> assertNotNull(Graph.class.getPermittedSubclasses())
      );
    }

    @Test
    public void invalidNodeCount() {
      assertThrows(IllegalArgumentException.class, () -> new MatrixGraph<>(-17));
    }
  }


  public interface GraphFactory {
    <T> Graph<T> createGraph(int nodeCount);
  }
  static Stream<GraphFactory> graphFactoryProvider() {
    return Stream.of(Graph::createMatrixGraph /*, Graph::createNodeMapGraph*/);
  }
  static Stream<Arguments> graphFactoryTwoProviders() {
    return graphFactoryProvider().flatMap(p1 -> graphFactoryProvider().map(p2 -> Arguments.of(p1, p2)));
  }

  @Nested
  public class Q2 {
    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void createGraph(GraphFactory graphFactory) {
      var graph = graphFactory.createGraph(50);
      assertNotNull(graph);
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void createGraphSignature(GraphFactory graphFactory) {
      Graph<String> graph = graphFactory.<String>createGraph(50);
      assertNotNull(graph);
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void createEmptyGraph(GraphFactory graphFactory) {
      var graph = graphFactory.createGraph(0);
      assertNotNull(graph);
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void invalidNodeCount(GraphFactory graphFactory) {
      assertThrows(IllegalArgumentException.class, () -> graphFactory.createGraph(-17));
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void implementationHidden(GraphFactory factory) {
      var graph = factory.createGraph(10);
      var accessFlags = graph.getClass().accessFlags();
      assertFalse(accessFlags.contains(AccessFlag.PUBLIC));
      assertTrue(accessFlags.contains(AccessFlag.FINAL));
    }

    @Test
    public void implementationHidden2() throws NoSuchMethodException {
      var method = Graph.class.getMethod("createMatrixGraph", int.class);
      var returnType = method.getReturnType();
      assertTrue(returnType.accessFlags().contains(AccessFlag.INTERFACE));
    }
  }


  @Nested
  public class Q3 {

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void getWeightEmpty(GraphFactory factory) {
      var nodeCount = 20;
      var graph = factory.createGraph(nodeCount);
      for (var i = 0; i < nodeCount; i++) {
        for (var j = 0; j < nodeCount; j++) {
          assertTrue(graph.getWeight(i, j).isEmpty());
        }
      }
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void hasEdgeValid(GraphFactory factory) {
      var graph = factory.createGraph(5);
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> graph.getWeight(-1, 3)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> graph.getWeight(2, -1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> graph.getWeight(5, 2)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> graph.getWeight(3, 5))
      );
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void addEdge(GraphFactory factory) {
      var graph = factory.<Integer>createGraph(7);
      graph.addEdge(3, 4, 2);
      assertEquals(2, (int) graph.getWeight(3, 4).orElseThrow());
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void addEdgeWithAString(GraphFactory factory) {
      var graph = factory.<String>createGraph(10);
      graph.addEdge(7, 8, "hello");
      assertAll(
          () -> assertEquals("hello", graph.getWeight(7, 8).orElseThrow()),
          () -> assertFalse(graph.getWeight(4, 3).isPresent())
      );
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void addEdgeNullWeight(GraphFactory factory) {
      var graph = factory.<Integer>createGraph(7);
      assertThrows(NullPointerException.class, () -> graph.addEdge(3, 4, null));
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void addEdgeTwice(GraphFactory factory) {
      var graph = factory.<String>createGraph(7);
      graph.addEdge(3, 4, "foo");
      graph.addEdge(3, 4, "bar");
      assertEquals("bar", graph.getWeight(3, 4).orElseThrow());
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void addEdgeValid(GraphFactory factory) {
      var graph = factory.createGraph(5);
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> graph.addEdge(-1, 3, 7)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> graph.addEdge(2, -1, 8)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> graph.addEdge(5, 2, 9)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> graph.addEdge(3, 5, 10)));
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void addEdgeALot(GraphFactory factory) {
      var graph = factory.createGraph(17);
      var random = RandomGeneratorFactory.getDefault().create(0);
      for (var index = 0; index < 1_000; index++) {
        var i = random.nextInt(17);
        var j = random.nextInt(17);
        var value = random.nextInt(10_000) - 5_000;
        graph.addEdge(i, j, value);
        assertEquals(value, (int) graph.getWeight(i, j).orElseThrow());
      }
    }
  }


  @Nested
  public class Q4 {

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void disjointMergeAllIntegers(GraphFactory factory) {
      var graph = factory.<Integer>createGraph(2);
      graph.addEdge(1, 0, 2);
      var graph2 = factory.<Integer>createGraph(2);
      graph2.addEdge(0, 1, 42);
      graph.mergeAll(graph2, Integer::sum);

      assertAll(
          () -> assertTrue(graph.getWeight(0, 0).isEmpty()),
          () -> assertTrue(graph.getWeight(1, 1).isEmpty()),
          () -> assertEquals(2, graph.getWeight(1, 0).orElseThrow()),
          () -> assertEquals(42, graph.getWeight(0, 1).orElseThrow())
      );
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void mergeAllIntegers(GraphFactory factory) {
      var graph = factory.<Integer>createGraph(2);
      graph.addEdge(0, 0, 17);
      var graph2 = factory.<Integer>createGraph(2);
      graph2.addEdge(0, 0, 7);
      graph.mergeAll(graph2, Integer::sum);

      assertAll(
          () -> assertEquals(24, graph.getWeight(0, 0).orElseThrow()),
          () -> assertTrue(graph.getWeight(0, 1).isEmpty()),
          () -> assertTrue(graph.getWeight(1, 0).isEmpty()),
          () -> assertTrue(graph.getWeight(1, 1).isEmpty())
      );
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void mergeAllStrings(GraphFactory factory) {
      var graph = factory.<String>createGraph(2);
      graph.addEdge(0, 0, "a");
      graph.addEdge(1, 0, "b");
      var graph2 = factory.<String>createGraph(2);
      graph2.addEdge(0, 0, "c");
      graph2.addEdge(0, 1, "d");
      graph.mergeAll(graph2, String::concat);

      assertAll(
          () -> assertEquals("ac", graph.getWeight(0, 0).orElseThrow()),
          () -> assertEquals("b", graph.getWeight(1, 0).orElseThrow()),
          () -> assertEquals("d", graph.getWeight(0, 1).orElseThrow()),
          () -> assertTrue(graph.getWeight(1, 1).isEmpty())
      );
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryTwoProviders")
    public void mergeAllStrings(GraphFactory factory1, GraphFactory factory2) {
      var graph = factory1.<String>createGraph(2);
      graph.addEdge(0, 0, "a");
      graph.addEdge(1, 0, "b");
      var graph2 = factory2.<String>createGraph(2);
      graph2.addEdge(0, 0, "c");
      graph2.addEdge(0, 1, "d");
      graph.mergeAll(graph2, String::concat);

      assertAll(
          () -> assertEquals("ac", graph.getWeight(0, 0).orElseThrow()),
          () -> assertEquals("b", graph.getWeight(1, 0).orElseThrow()),
          () -> assertEquals("d", graph.getWeight(0, 1).orElseThrow()),
          () -> assertTrue(graph.getWeight(1, 1).isEmpty())
      );
    }


    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void mergeAllALot(GraphFactory factory) {
      var nodeCount = 1_000;
      var graph1 = factory.<Integer>createGraph(nodeCount);
      var graph2 = factory.<Integer>createGraph(nodeCount);

      for(var j = 0; j < nodeCount; j++) {
        for(var i = 0; i < nodeCount; i++) {
          graph1.addEdge(i, j, i);
          graph2.addEdge(i, j, j);
        }
      }

      graph1.mergeAll(graph2, Integer::sum);

      for(var j = 0; j < nodeCount; j++) {
        for(var i = 0; i < nodeCount; i++) {
          assertEquals(i + j, graph1.getWeight(i, j).orElseThrow());
        }
      }
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryTwoProviders")
    public void mergeAllSignature(GraphFactory factory1, GraphFactory factory2) {
      var graph = factory1.createGraph(0);
      var graph2 = factory2.<String>createGraph(0);
      graph.mergeAll(graph2, (o1, o2) -> fail());
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void mergeAllPreconditions(GraphFactory factory) {
      var graph1 = factory.<Integer>createGraph(10);
      var graph2 = factory.<Integer>createGraph(3);
      assertAll(
          () -> assertThrows(IllegalArgumentException.class, () -> graph1.mergeAll(graph2, Integer::sum)),
          () -> assertThrows(NullPointerException.class, () -> graph1.mergeAll(null, Integer::sum)),
          () -> assertThrows(NullPointerException.class, () -> graph1.mergeAll(graph1, null))
      );
    }
  }


  @Nested
  public class Q5 {
    @Test
    public void mergeAllIsNotAbstract() {
      assertTrue(Arrays.stream(Graph.class.getMethods())
          .filter(m -> m.getName().equals("mergeAll"))
          .noneMatch(m -> m.accessFlags().contains(AccessFlag.ABSTRACT)));
    }
  }

  @Nested
  public class Q6 {

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void neighborsSimple(GraphFactory factory) {
      var graph = factory.<Integer>createGraph(6);
      graph.addEdge(1, 2, 222);
      graph.addEdge(1, 5, 555);

      var iterator = graph.neighborIterator(1);
      assertEquals(2, (int) iterator.next());
      assertEquals(5, (int) iterator.next());
      assertThrows(NoSuchElementException.class, iterator::next);
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void neighborsSignature(GraphFactory factory) {
      Graph<Double> graph1 = factory.<Double>createGraph(6);
      graph1.addEdge(1, 2, 3.0);
      Iterator<Integer> iterator1 = graph1.neighborIterator(1);
      assertEquals(2, iterator1.next());

      Graph<String> graph2 = factory.<String>createGraph(6);
      graph2.addEdge(1, 3, "graph");
      Iterator<Integer> iterator2 = graph2.neighborIterator(1);
      assertEquals(3, iterator2.next());
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void neighborsEmptyHasNext(GraphFactory factory) {
      var graph = factory.createGraph(6);
      var iterator = graph.neighborIterator(0);
      assertFalse(iterator.hasNext());
      assertFalse(iterator.hasNext());
      assertFalse(iterator.hasNext());
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void neighborsOutOfRange(GraphFactory factory) {
      var graph = factory.<Integer>createGraph(6);
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> graph.neighborIterator(10)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> graph.neighborIterator(-2))
      );
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void neighborsEmptyNext(GraphFactory factory) {
      var graph = factory.createGraph(6);
      assertThrows(NoSuchElementException.class, () -> graph.neighborIterator(0).next());
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void neighborsOneEdge(GraphFactory factory) {
      var graph = factory.<String>createGraph(6);
      graph.addEdge(1, 2, "hello");
      var iterator = graph.neighborIterator(1);
      assertTrue(iterator.hasNext());
      assertEquals(2, (int) iterator.next());
      assertFalse(iterator.hasNext());
      assertThrows(NoSuchElementException.class, iterator::next);
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void neighborsNoHasNext(GraphFactory factory) {
      var graph = factory.createGraph(10);
      for (int i = 0; i < 10; i++) {
        graph.addEdge(5, i, -1);
      }

      var result = new HashSet<Integer>();
      var expected = new HashSet<Integer>();
      var iterator = graph.neighborIterator(5);
      for (int i = 0; i < 10; i++) {
        expected.add(i);
        result.add(iterator.next());
      }
      assertEquals(expected, result);

      assertFalse(iterator.hasNext());
      assertThrows(NoSuchElementException.class, iterator::next);
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void neighborsNonDestructive(GraphFactory factory) {
      var graph = factory.createGraph(12);
      for (int i = 0; i < 12; i++) {
        graph.addEdge(5, i, 67);
      }
      assertTimeout(Duration.ofMillis(1_000), () -> {
        var neighbors = graph.neighborIterator(5);
        while (neighbors.hasNext()) {
          assertNotNull(neighbors.next());
        }
      });
      for (int i = 0; i < 12; i++) {
        assertEquals(67, (int) graph.getWeight(5, i).orElseThrow());
      }
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void neighborSeveralHasNext(GraphFactory factory) {
      var graph = factory.createGraph(14);
      graph.addEdge(3, 7, 2);
      graph.addEdge(3, 5, 3);
      graph.addEdge(7, 3, 4);

      assertTimeout(Duration.ofMillis(1_000), () -> {
        var neighbors = graph.neighborIterator(3);
        assertTrue(neighbors.hasNext());
        var node1 = neighbors.next();
        for (var i = 0; i < 5; i++) {
          assertTrue(neighbors.hasNext());
        }
        var node2 = neighbors.next();
        assertFalse(neighbors.hasNext());
        assertTrue((node1 == 5 && node2 == 7) || (node1 == 7 && node2 == 5));
      });
    }

    @Test
    public void neighborImplementationIsLazy() {
      var graph = new MatrixGraph<>(3);
      var iterator = graph.neighborIterator(0);
      var packageName = iterator.getClass().getPackageName();
      assertAll(
          () -> assertNotEquals("java.util", packageName),
          () -> assertNotEquals("java.util.stream", packageName)
      );
    }
  }


  @Nested
  public class Q7 {

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void iteratorRemove(GraphFactory factory) {
      var graph = factory.createGraph(11);
      graph.addEdge(3, 10, 13);
      var neighbors = graph.neighborIterator(3);
      assertEquals(10, (int) neighbors.next());
      neighbors.remove();
      assertFalse(graph.getWeight(3, 10).isPresent());
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void iteratorRemoveInvalid(GraphFactory factory) {
      var graph = factory.createGraph(21);
      graph.addEdge(20, 19, 20);
      var neighbors = graph.neighborIterator(20);
      assertThrows(IllegalStateException.class, neighbors::remove);
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void iteratorRemoveTwiceInvalid(GraphFactory factory) {
      var graph = factory.createGraph(21);
      graph.addEdge(20, 19, 20);
      var neighbors = graph.neighborIterator(20);
      neighbors.next();
      neighbors.remove();
      assertFalse(graph.getWeight(20, 19).isPresent());
      assertThrows(IllegalStateException.class, neighbors::remove);
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void iteratorRemoveALot(GraphFactory factory) {
      var graph = factory.createGraph(50);
      for (var i = 0; i < 50; i++) {
        for (var j = 0; j < i; j++) {
          graph.addEdge(i, j, i + j);
        }
      }

      for (int i = 0; i < 50; i++) {
        var neighbors = graph.neighborIterator(i);
        for (var j = 0; j < i; j++) {
          assertTrue(neighbors.hasNext());
          neighbors.next();
          neighbors.remove();
        }
        assertFalse(neighbors.hasNext());
      }

      for (var i = 0; i < 50; i++) {
        for (var j = 0; j < 50; j++) {
          assertTrue(graph.getWeight(i, j).isEmpty());
        }
      }
    }
  }


  @Nested
  public class Q8 {
    @Test
    public void edgeOfInteger() {
      Graph.Edge<Integer> edge = new Graph.Edge<Integer>(1, 2, 42);
      assertAll(
          () -> assertEquals(1, edge.src()),
          () -> assertEquals(2, edge.dst()),
          () -> assertEquals(42, edge.weight())
      );
    }

    @Test
    public void edgeOfString() {
      Graph.Edge<String> edge = new Graph.Edge<String>(7, 4, "foo");
      assertAll(
          () -> assertEquals(7, edge.src()),
          () -> assertEquals(4, edge.dst()),
          () -> assertEquals("foo", edge.weight())
      );
    }

    @Test
    public void edgePreconditions() {
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> new Graph.Edge<>(1, 2, null)),
          () -> assertThrows(IllegalArgumentException.class, () -> new Graph.Edge<>(-1, 2, 10)),
          () -> assertThrows(IllegalArgumentException.class, () -> new Graph.Edge<>(1, -2, 10))
      );
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void forEachEdgeStrings(GraphFactory factory) {
      var graph = factory.<String>createGraph(12);
      graph.addEdge(2, 5, "foo");
      graph.addEdge(2, 9, "bar");

      var edges = new ArrayList<Graph.Edge<String>>();
      graph.forEachEdge(2, edges::add);
      assertEquals(List.of(
          new Graph.Edge<>(2, 5, "foo"),
          new Graph.Edge<>(2, 9, "bar")
      ), edges);
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void forEachEdgeIntegers(GraphFactory factory) {
      var graph = factory.<Integer>createGraph(12);
      graph.addEdge(4, 1, 2);
      graph.addEdge(4, 7, 3);
      graph.addEdge(4, 10, 4);

      var edges = new ArrayList<Graph.Edge<Integer>>();
      graph.forEachEdge(4, edges::add);
      assertEquals(List.of(
          new Graph.Edge<>(4, 1, 2),
          new Graph.Edge<>(4, 7, 3),
          new Graph.Edge<>(4, 10, 4)
      ), edges);
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void forEachEdgeEmpty(GraphFactory factory) {
      var graph = factory.createGraph(3);
      graph.forEachEdge(0, edge -> fail());
    }

    @Test
    public void forEachEdgeIsNotAbstract() {
      assertTrue(Arrays.stream(Graph.class.getMethods())
          .filter(m -> m.getName().equals("forEachEdge"))
          .noneMatch(m -> m.accessFlags().contains(AccessFlag.ABSTRACT)));
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void forEachEdgeSignature(GraphFactory factory) {
      var graph = factory.createGraph(1);
      graph.forEachEdge(0, (Record edge) -> fail());
      graph.forEachEdge(0, (Object edge) -> fail());
    }

    @ParameterizedTest
    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
    public void forEachEdgePreconditions(GraphFactory factory) {
      var graph = factory.createGraph(1);
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> graph.forEachEdge(0, null)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> graph.forEachEdge(-1, edge -> {})),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> graph.forEachEdge(1, edge -> {}))
      );
    }
  }


//  @Nested
//  public class Q9 {
//    @ParameterizedTest
//    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
//    public void edgesIntegers(GraphFactory factory) {
//      var graph = factory.<Integer>createGraph(2);
//      graph.addEdge(0, 0, 0);
//      graph.addEdge(1, 0, 10);
//      graph.addEdge(1, 1, 11);
//
//      var set = graph.edges().collect(toSet());
//      assertEquals(Set.of(
//          new Graph.Edge<>(0, 0, 0),
//          new Graph.Edge<>(1, 0, 10),
//          new Graph.Edge<>(1, 1, 11)
//      ), set);
//    }
//
//    @ParameterizedTest
//    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
//    public void edgesString(GraphFactory factory) {
//      var graph = factory.<String>createGraph(4);
//      graph.addEdge(0, 0, "foo");
//      graph.addEdge(1, 3, "bar");
//      graph.addEdge(2, 2, "whizz");
//      graph.addEdge(3, 1, "baz");
//
//      var set = Set.copyOf(graph.edges().toList());
//      assertEquals(Set.of(
//          new Graph.Edge<>(0, 0, "foo"),
//          new Graph.Edge<>(1, 3, "bar"),
//          new Graph.Edge<>(2, 2, "whizz"),
//          new Graph.Edge<>(3, 1, "baz")
//      ), set);
//    }
//
//    @ParameterizedTest
//    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
//    public void edgesEmpty(GraphFactory factory) {
//      var graph = factory.createGraph(8);
//      assertTrue(graph.edges().findFirst().isEmpty());
//    }
//
//    @ParameterizedTest
//    @MethodSource("fr.uge.graph.GraphTest#graphFactoryProvider")
//    public void edgesALot(GraphFactory factory) {
//      var graph = factory.createGraph(1_000);
//      for(var j = 0; j < graph.nodeCount(); j++) {
//        for(var i = 0; i < graph.nodeCount(); i++) {
//          graph.addEdge(i, j, i + j);
//        }
//      }
//
//      var edges = graph.edges().toList();
//      assertEquals(1_000_000, edges.size());
//      for(var edge: edges) {
//        assertEquals(edge.src() + edge.dst(), edge.weight());
//      }
//    }
//
//    @Test
//    public void edgesIsNotAbstract() {
//      assertTrue(Arrays.stream(Graph.class.getMethods())
//          .filter(m -> m.getName().equals("edges"))
//          .noneMatch(m -> m.accessFlags().contains(AccessFlag.ABSTRACT)));
//    }
//  }
}
