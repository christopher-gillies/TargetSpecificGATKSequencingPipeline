package org.kidneyomics.fluidigm;

public class AlleleDosageCalculator {


	public static double getPValue(int refDepth, int altDepth) {
		return new CumulativeBinomialDistribution(refDepth + altDepth, 0.5).binomialTestTwoSidedPvalue(refDepth);
	}
	
	public static double getPhredScaledPvalue(int refDepth, int altDepth) {
		return new CumulativeBinomialDistribution(refDepth + altDepth, 0.5).binomialTestTwoSidedPHRED(refDepth);
	}
}
