package org.kidneyomics.fluidigm;

import java.util.List;

import org.apache.log4j.Logger;
import org.kidneyomics.vcf.GenotypeField;
import org.slf4j.LoggerFactory;

public class SVMFeatureCalculator implements FeatureCalculator {

	private double callRate;
	private String alleleDosageTest = "NA";
	private String normalizedAlleleDosageTest = "NA";
	private String meanAlleleBalance = "NA";
	private String meanAltDepth = "NA";
	private Double qd;
	
	private static Logger logger = Logger.getLogger(SVMFeatureCalculator.class);
	
	@Override
	public FeatureCalculator calculate(List<GenotypeField> gts, boolean parseAlleleDepths) {
		
		
		int nonMissingCount = 0;
		int refDepth = 0;
		int altDepth = 0;
		int hetCount = 0;
		int homAltCount = 0;
		int homAltRefDepth = 0;
		int homAltAltDepth = 0;
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
					
					if(gt.getAlleles()[0] == gt.getAlleles()[1] && (gt.getAlleles()[0] != 0) && gt.getGenotypeQuality() > 20) {
						homAltRefDepth += AlleleBalanceAndAltDepthCalculator.refDepth(gt);
						homAltAltDepth += AlleleBalanceAndAltDepthCalculator.altDepth(gt);
						homAltCount++;
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
		
		
		if(qd != null && hetCount == 0 && homAltCount > 0 & callRate > 0.5) {
			
			logger.info("NO HETs Found: imputing...");
			logger.info("QD = " + qd);
			//Cases where all sites are homozygous
			double meanAb = qdToMeanAlleleBalance(qd);
			meanAlleleBalance = Double.toString(meanAb);
			
			logger.info("meanAlleleBalance = " + meanAlleleBalance);
			
			meanAltDepth = Float.toString(homAltAltDepth / ((float) homAltCount));
			logger.info("meanAltDepth = " + meanAltDepth);
			logger.info("homAltDepth = " + homAltAltDepth);
			double refdepth = (homAltAltDepth / meanAb) - homAltAltDepth;
			logger.info("refAltDepth = " + refdepth);
			double res = AlleleDosageCalculator.getPhredScaledPvalue((int) Math.round(refdepth / (float) homAltCount), Math.round(homAltAltDepth / (float) homAltCount));
			
			alleleDosageTest = Double.toString(res);
			double normalizedRes = res / Math.round(homAltAltDepth / (float) homAltCount);
			normalizedAlleleDosageTest = Double.toString(normalizedRes);
			
			
			logger.info("meanAltDepth = " + meanAltDepth);
			logger.info("alleleDosageTest = " + alleleDosageTest);
			logger.info("normalizedAlleleDosageTest = " + normalizedAlleleDosageTest);
		}
		
		return this;
	}

	/**
	 * calculate mean allele balance from qd when we have no information
	 * max out at 0.5
	 * @param qd
	 * @return
	 */
	private double qdToMeanAlleleBalance(double qd) {
		double res = 0.13219 + 0.03394 * qd;
		if(res > 0.5) {
			res = 0.5;
		}
		return res;
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

	@Override
	public void setQD(double qd) {
		this.qd = qd;
	}

}
