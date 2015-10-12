package org.kidneyomics.fluidigm;

import java.util.List;

import org.kidneyomics.vcf.GenotypeField;

public interface FeatureCalculator {

	FeatureCalculator calculate(List<GenotypeField> gts, boolean parseAlleleDepths);
	
	double getCallRate();
	
	String getAlleleDosageTest();
	
	String getNormalizedAlleleDosageTest();
	
	String getMeanAlleleBalance();
	
	String getMeanAltDepth();
	
	void setQD(double qd);
	
}
