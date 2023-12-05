package fr.uge.simd;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("static-method")
public class VectorComputationTest {
  @Nested
  public class Q1 {

    @ParameterizedTest
    @ValueSource(ints = {0, 17, 64, 127, 256, 511})
    public void testSum(int length) {
      var array = new Random(0).ints(length, 0, 1_000).toArray();
      var sum = VectorComputation.sum(array);
      var expected = new Random(0).ints(length, 0, 1_000).sum();
      assertEquals(expected, sum);
    }
  }

  @Nested
  public class Q2 {

    @ParameterizedTest
    @ValueSource(ints = {0, 17, 64, 127, 256, 511})
    public void testSumMask(int length) {
      var array = new Random(0).ints(length, 0, 1_000).toArray();
      var sum = VectorComputation.sumMask(array);
      var expected = new Random(0).ints(length, 0, 1_000).sum();
      assertEquals(expected, sum);
    }
  }

  @Nested
  public class Q3 {

    @ParameterizedTest
    @ValueSource(ints = {17, 64, 127, 256, 511})
    public void testMin(int length) {
      var array = new Random(0).ints(length, 0, 1_000).toArray();
      var min = VectorComputation.min(array);
      var expected = new Random(0).ints(length, 0, 1_000).min().orElseThrow();
      assertEquals(expected, min);
    }
  }

  @Nested
  public class Q4 {
    @ParameterizedTest
    @ValueSource(ints = {17, 64, 127, 256, 511})
    public void testMinMask(int length) {
      var array = new Random(0).ints(length, 0, 1_000).toArray();
      var min = VectorComputation.minMask(array);
      var expected = new Random(0).ints(length, 0, 1_000).min().orElseThrow();
      assertEquals(expected, min);
    }
  }

}