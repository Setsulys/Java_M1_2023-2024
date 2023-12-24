package fr.uge.numeric;

import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NumericVec<T> {

	private long[] numericArray;
	private int sizeOf; 
	private final ToLongFunction<? super T> into;
	private final LongFunction<? extends T> from;

	private NumericVec(long[] array, ToLongFunction<? super T> into,LongFunction<? extends T> from){
		this.numericArray =array;
		this.sizeOf =array.length;
		this.into=into;
		this.from=from;
	}

	@SafeVarargs
	public static NumericVec<Long> longs(long ... args){
		return new NumericVec<>(Arrays.copyOf(args, args.length),e->e,e->e);
	}

	public int size() {
		return sizeOf;
	}

	public T get(int index) {
		Objects.checkIndex(index, size());
		return from.apply(numericArray[index]);
	}

	public void add(T value) {
		Objects.requireNonNull(value);
		if(sizeOf>=numericArray.length) {
			numericArray = Arrays.copyOf(numericArray,(sizeOf+1)*2);
		}
		numericArray[sizeOf]=into.applyAsLong(value);
		sizeOf++;
	}

	@SafeVarargs
	public static NumericVec<Integer> ints(int ...args){
		var longs = new long[args.length];
		for(var i = 0; i < args.length;i++) {
			longs[i] = args[i];
		}
		return new NumericVec<Integer>(Arrays.copyOf(longs, longs.length),e -> e,e->(int)e);
	}

	@SafeVarargs
	public static NumericVec<Double> doubles(double ...args){
		var longs = new long[args.length];
		for(var i = 0; i < args.length;i++) {
			longs[i] = Double.doubleToLongBits(args[i]);
		}
		return new NumericVec<Double>(Arrays.copyOf(longs, longs.length), e-> Double.doubleToRawLongBits(e),e -> Double.longBitsToDouble(e));
	}

	@SuppressWarnings("unchecked")
	public Stream<T> stream(){
		var array = (T[])new Object[size()];
		for(var i =0;i < size();i++) {
			array[i] = from.apply(numericArray[i]);
		}
		return StreamSupport.stream(fromArray(0, sizeOf, array), false);
	}

	@SafeVarargs
	public static <T> Spliterator<T> fromArray(int start,int end,T... array){
		return new Spliterator<>() {
			private int i =start;

			@Override
			public boolean tryAdvance(Consumer<? super T> action) {
				if (i < end) { 
					action.accept(array[i++]);
					return true; 
				}
				return false;
			}

			@Override
			public Spliterator<T> trySplit() {
				if(end <1024) {
					return null;
				}
				var middle = (i + end) >>> 1;
				if (middle == i) {
					return null;
				}
				var spliterator = fromArray(i, middle, array);
				i = middle;
				return spliterator;
			}

			@Override
			public long estimateSize() {
				// TODO Auto-generated method stub
				return end -i;
			}

			@Override
			public int characteristics() {
				// TODO Auto-generated method stub
				return NONNULL | ORDERED | IMMUTABLE | SIZED;
			}
		};
	}
}
