package org.kidneyomics.fluidigm;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CoverageCalculatorImpl implements CoverageCalculator {

	
	private Map<String,Integer> chrSizes = null;
	private Map<String,ArrayWrapper> chrs = null;
	
	@Override
	public CoverageCalculator readReference(File reference) throws IOException {
		
		chrSizes = ReferenceFASTAScanner.getSequenceSizes(reference);
		
		for(Map.Entry<String, Integer> entry : chrSizes.entrySet()) {
			String key = entry.getKey();
			int val = entry.getValue();
			ArrayWrapper aw = new ArrayWrapper(val);
			chrs.put(key, aw);
		}
		
		return this;
	}

	@Override
	public CoverageCalculator add(String chr, int start, int end) {
		ArrayWrapper aw = chrs.get(chr);
		if(aw != null) {
			aw.add(start, end);
		} else {
			throw new IllegalArgumentException(chr + " was not found!");
		}
		return this;
	}

	@Override
	public int[] getCounts(String chr, int start, int end) {
		ArrayWrapper aw = chrs.get(chr);
		
		if(aw != null) {
			aw.add(start, end);
		} else {
			throw new IllegalArgumentException(chr + " was not found!");
		}
		return aw.getCounts(start, end);
	}

	@Override
	public double meanCoverage(String chr, int start, int end) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private class ArrayWrapper {
		
		/**
		 * language specification ensures the default value is 0
		 */
		public int[] array;
		
		public ArrayWrapper(int n) {
			array = new int[n];
		}
		
		public void add(int start, int end) {
			for(int i = start; i <= end; i++) {
				array[i]++;
			}
		}
		
		public int[] getCounts(int start, int end) {
			int size = end - start + 1;
			int[] result = new int[size];
			int count = 0;
			for(int i = start; i <= end; i++) {
				result[count++] = array[i];
			}
			return result;
		}
	}

}
