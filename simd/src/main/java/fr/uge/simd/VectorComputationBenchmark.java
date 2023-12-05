package fr.uge.simd;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = { "--add-modules", "jdk.incubator.vector" })
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@SuppressWarnings("static-method")
public class VectorComputationBenchmark {
	private final int[] array = new Random(0).ints(100_000, 0, 1_000).toArray();

	@Benchmark
	public int sum_novector_array(Blackhole blackhole) {
		var sum = 0;
		for (var value : array) {
			sum += value;
		}
		return sum;
	}

	//@Benchmark
	public int sum_vector_array(Blackhole blackhole) {
		return VectorComputation.sum(array);
	}

	//@Benchmark
	public int sum_vector_mask_array(Blackhole blackhole) {
		return VectorComputation.sumMask(array);
	}

	//@Benchmark
	public int min_loop_array(Blackhole blackhole) {
		int min = array[0];
		for (var i = 1; i < array.length; i++) {
			min = Math.min(min, array[i]);
		}
		return min;
	}

	//@Benchmark
	public int min_vector_array(Blackhole blackhole) {
		return VectorComputation.min(array);
	}

	//@Benchmark
	public int min_vector_mask_array(Blackhole blackhole) {
		return VectorComputation.minMask(array);
	}
}
