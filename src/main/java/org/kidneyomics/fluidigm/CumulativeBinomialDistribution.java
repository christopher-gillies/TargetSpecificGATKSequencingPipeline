package org.kidneyomics.fluidigm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math3.distribution.BinomialDistribution;

public class CumulativeBinomialDistribution {

	private BinomialDistribution distribution;
	
	
	public CumulativeBinomialDistribution(int trials, double p) {
		if(trials == 0) {
			throw new IllegalArgumentException("trials cannot be 0");
		}
		distribution = new BinomialDistribution(trials, p);
	}
	
	
	public double lowerTail(int x) {
		return Math.exp(lowerTailLog(x));
	}
	
	public double lowerTailLog(int x) {
		List<Double> a = new ArrayList<Double>(distribution.getNumberOfTrials() - x);
		for(int i = 0; i <= x; i++) {
			a.add(distribution.logProbability(i));
		}
		return addLogPValues(a);
	}
	
	public double upperTailLog(int x) {
		List<Double> a = new ArrayList<Double>(distribution.getNumberOfTrials() - x);
		for(int i = x + 1; i <= distribution.getNumberOfTrials(); i++) {
			a.add(distribution.logProbability(i));
		}
		return addLogPValues(a);
	}
	
	private double addLogPValues(List<Double> a) {
		Collections.sort(a, new DescendingDoubleComparator());
		double a_sum = 0;
		for(int i = 1; i < a.size(); i++) {
			a_sum += Math.exp(a.get(i) - a.get(0));
		}
		return a.get(0) + Math.log(1 + a_sum);
	}
	
	public double binomialTestGreaterPvalue(int x) {
		return upperTail(x - 1);
	}
	
	public double binomialTestTwoSidedLogPvalue(int x) {
		/*
		 
		  binom.test2 = function(x,n,p=0.5) {
		  	if(x < n / 2) {
		  		x = n - x
		  	}
		  	pbinom(n - x,n,p) + pbinom(x-1,n,p,lower.tail=FALSE)
		  }
		 
		 */
		int xupper = -1;
		int xlower = -1;
		/*
		 * fixed bug when n = 3 by changing < to <=
		 * x = 1
		 * 1 < 3/2 == 1 < 1 b/c of integer
		 * this is false so
		 * upper would equal x and lower would be 3 - x
		 * upper = 1 and lower would be 2
		 */
		if(x <= distribution.getNumberOfTrials() / 2) {
			xupper = distribution.getNumberOfTrials() - x;
			xlower = x;
		} else {
			xupper = x;
			xlower = distribution.getNumberOfTrials() - x;
		}
		
		if(xlower == xupper) {
			xlower = xlower - 1;
		}
		
		List<Double> logPvals = new ArrayList<Double>(2);
		logPvals.add(lowerTailLog(xlower));
		logPvals.add(upperTailLog(xupper - 1));
		return  addLogPValues(logPvals);
	}
	
	public double binomialTestTwoSidedPvalue(int x) {
		return  Math.exp(binomialTestTwoSidedLogPvalue(x));
	}
	
	
	public double binomialTestTwoSidedPHRED(int x) {
		return  -10 * binomialTestTwoSidedLogPvalue(x) / Math.log(10);
	}
	
	
	public double binomialTestGreaterPHRED(int x) {
		return -10 * upperTailLog(x - 1) / Math.log(10);
	}
	
	public double upperTail(int x) {
		return Math.exp(upperTailLog(x));
	}
}
