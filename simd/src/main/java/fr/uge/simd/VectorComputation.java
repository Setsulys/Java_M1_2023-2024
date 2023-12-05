package fr.uge.simd;

import org.apache.commons.math3.optim.PointVectorValuePair;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.*;

public class VectorComputation {

	public static int sum(int [] array) {
		var length = array.length;
		var species = IntVector.SPECIES_PREFERRED;
		var loopBound = length - length % species.length();
		var vector = IntVector.zero(species);
		for(var i=0; i  < loopBound;i+= species.length()) {
			vector = vector.add(IntVector.fromArray(species, array, i));	
		}
		var sumVector = vector.reduceLanes(VectorOperators.ADD);
		for(var i=loopBound; i < length;i++) {
			sumVector+=array[i];
		}
		return sumVector;
	}
	
	public static int sumMask(int [] array) {
		var length = array.length;
		var species = IntVector.SPECIES_PREFERRED;
		var loopBound = species.loopBound(length);
		var vector =IntVector.zero(species);
		var i=0;
		for(;i <loopBound; i+= species.length()) {
			vector = vector.add(IntVector.fromArray(species, array, i));
		}
		var mask = species.indexInRange(i, length);
		vector = vector.add(IntVector.fromArray(species, array, i,mask));
		return vector.reduceLanes(VectorOperators.ADD);
	}

	public static int min(int [] array) {
		var length = array.length;
		if(length < 1) {
			throw new IllegalArgumentException();
		}
		var species = IntVector.SPECIES_PREFERRED;
		var loopBound = species.loopBound(length);
		var min = array[0];
		var vector = IntVector.broadcast(species, array[0]);
		for(var i=0; i  < loopBound;i+= species.length()) {
			vector =vector.min(IntVector.fromArray(species, array, i));
		}
		min =vector.reduceLanes(VectorOperators.MIN);
		for(var i=loopBound; i < length;i++) {
			min = min < array[i]? min: array[i];
		}
		return min;
	}
	
	public static int minMask(int [] array) {
		var length = array.length;
		if(length < 1) {
			throw new IllegalArgumentException();
		}
		var species = IntVector.SPECIES_PREFERRED;
		var loopBound = species.loopBound(length);
		var vector = IntVector.broadcast(species, array[0]);
		var i=0;
		for(; i  < loopBound;i+= species.length()) {
			vector =vector.min(IntVector.fromArray(species, array, i));
		}
		var mask = species.indexInRange(i, length);
		var vector2 = IntVector.fromArray(species, array, i,mask);
		vector = vector.lanewise(VectorOperators.MIN,vector2,mask);
		return vector.reduceLanes(VectorOperators.MIN);
	}
}
