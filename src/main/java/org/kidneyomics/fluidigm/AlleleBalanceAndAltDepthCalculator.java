package org.kidneyomics.fluidigm;

import java.util.List;

import org.kidneyomics.vcf.GenotypeField;

public class AlleleBalanceAndAltDepthCalculator {

	public static float alleleBalance(GenotypeField gt) {
		
		int[] alleles = gt.getAlleles();
		List<Integer> alleleDepths = gt.getAllelicDepths();
		
		float depthA = (float) alleleDepths.get(alleles[0]);
		float depthB = (float) alleleDepths.get(alleles[1]);
		
		if(alleles[0] == alleles[1] && alleles[0] == 0) {
			return 0.0f;
		} else if(alleles[0] == alleles[1]) {
			return 1.0f;
		} else {
			/*
			 * altDepth / (altDepth + refDepth)
			 */
			
			/*
			 * Easy cases: one of the alleles is the reference
			 */
			if(alleles[0] == 0) {
				return depthB / (depthA + depthB);
			} else if(alleles[1] == 0) {
				return depthA / (depthA + depthB);
			} else {
				//Both alleles are alt and not the same
				//return the maximum allele depth 
				return Math.max(depthB / (depthA + depthB), depthA / (depthA + depthB));
			}
		}
	}
	
	public static int altDepth(GenotypeField gt) {
		int[] alleles = gt.getAlleles();
		List<Integer> alleleDepths = gt.getAllelicDepths();
		
		int depthA = alleleDepths.get(alleles[0]);
		int depthB = alleleDepths.get(alleles[1]);
		
		if(alleles[0] == alleles[1] && alleles[0] == 0) {
			return 0;
		} else if(alleles[0] == alleles[1]) {
			/*
			 * depthA must equal depthB
			 */
			return depthA;
		} else {
			if(alleles[0] == 0) {
				return depthB;
			} else if(alleles[1] == 0) {
				return depthA;
			} else {
				//Both alleles are alt and not the same
				//return the maximum allele depth 
				return Math.max(depthA,depthB);
			}
		}
	}
	
	public static int refDepth(GenotypeField gt) {
		List<Integer> alleleDepths = gt.getAllelicDepths();
		return alleleDepths.get(0);
	}
	
}
