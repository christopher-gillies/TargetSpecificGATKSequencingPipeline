package org.kidneyomics.fluidigm;

import java.io.File;
import java.io.IOException;

public interface CoverageCalculator {

	/**
	 * 
	 * @param reference
	 * @return CoverageCalculator setup for adding coverage
	 * @throws IOException 
	 */
	CoverageCalculator readReference(File reference) throws IOException;
	
	/**
	 * 
	 * @param chr
	 * @param start
	 * @param end
	 * @return CoverageCalculator with coverage added to a specific region
	 */
	CoverageCalculator add(String chr, int start, int end);
	
	/**
	 * 
	 * @param chr
	 * @param start
	 * @param end
	 * @return an array containing the counts for each base pair across the region
	 */
	int[] getCounts(String chr, int start, int end);
	
	/**
	 * 
	 * @param chr
	 * @param start
	 * @param end
	 * @return the total coverage normalized by the number of base pairs in region
	 */
	double meanCoverage(String chr, int start, int end);
	
	
}
