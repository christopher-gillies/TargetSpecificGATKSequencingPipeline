package org.kidneyomics.fluidigm;

import java.util.List;

import org.kidneyomics.vcf.GenotypeField;

public class SVMFeatureCalculator implements FeatureCalculator {

	private double callRate;
	private String alleleDosageTest = "NA";
	private String normalizedAlleleDosageTest = "NA";
	private String meanAlleleBalance = "NA";
	private String meanAltDepth = "NA";
	
	@Override
	public FeatureCalculator calculate(List<GenotypeField> gts, boolean parseAlleleDepths) {
		
		
		int nonMissingCount = 0;
		int refDepth = 0;
		int altDepth = 0;
		int hetCount = 0;
		for(GenotypeField gt : gts) {
			if(!gt.getIsMissing()) {
				nonMissingCount++;
				
				//Only use allele depth if we have a parser that can get it!
				if(parseAlleleDepths) {
					if(gt.getAlleles()[0] != gt.getAlleles()[1] && (gt.getAlleles()[0] == 0 || gt.getAlleles()[1] == 0) && gt.getGenotypeQuality() > 20) {
						refDepth += AlleleBalanceAndAltDepthCalculator.refDepth(gt);
						altDepth += AlleleBalanceAndAltDepthCalculator.altDepth(gt);
						hetCount++;
					}
					
				}
			}
			
		}
		

		if(hetCount > 0) {
			meanAlleleBalance = Float.toString(altDepth / (float) (altDepth + refDepth));
			meanAltDepth = Float.toString(altDepth / ((float) hetCount));
			
			double res = AlleleDosageCalculator.getPhredScaledPvalue(Math.round(refDepth / (float) hetCount), Math.round(altDepth / (float) hetCount));
			alleleDosageTest = Double.toString(res);
			
			double normalizedRes = res / Math.round(altDepth / (float) hetCount);
			normalizedAlleleDosageTest = Double.toString(normalizedRes);

		}
		
		callRate = nonMissingCount / ((double) gts.size());
		
		return this;
	}

	@Override
	public double getCallRate() {
		
		return callRate;
	}

	@Override
	public String getAlleleDosageTest() {
		
		return alleleDosageTest;
	}

	@Override
	public String getNormalizedAlleleDosageTest() {
		
		return normalizedAlleleDosageTest;
	}

	@Override
	public String getMeanAlleleBalance() {
		
		return meanAlleleBalance;
	}

	@Override
	public String getMeanAltDepth() {
		
		return meanAltDepth;
	}

}
