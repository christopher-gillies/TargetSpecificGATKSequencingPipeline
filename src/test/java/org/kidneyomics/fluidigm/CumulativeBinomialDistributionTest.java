package org.kidneyomics.fluidigm;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;

public class CumulativeBinomialDistributionTest {

	Logger logger = Logger.getLogger(CumulativeBinomialDistributionTest.class);
	
	@Test
	public void test() {
		CumulativeBinomialDistribution dist = new CumulativeBinomialDistribution(1000, 0.5);
		double res = dist.upperTailLog(900);
		logger.info(res);
		assertEquals(res,-373.316,0.01);
	}
	
	
	@Test
	public void testBinomialTestPhred() {
		CumulativeBinomialDistribution dist = new CumulativeBinomialDistribution(1000, 0.5);
		double res = dist.binomialTestGreaterPHRED(900);
		logger.info(res);
		assertEquals(res,1611.738,0.01);
	}
	
	@Test
	public void testBinomialTestTwoSidedPvalue() {
		CumulativeBinomialDistribution dist = new CumulativeBinomialDistribution(10, 0.5);
		double res = dist.binomialTestTwoSidedPvalue(3);
		logger.info(res);
		assertEquals(res, 0.3438,0.001);
	}
	
	@Test
	public void testBinomialTestTwoSidedPhred() {
		CumulativeBinomialDistribution dist = new CumulativeBinomialDistribution(100, 0.5);
		double res = dist.binomialTestTwoSidedPHRED(30);
		logger.info(res);
		assertEquals(res,41.05123,0.01);
	}
	
	
	@Test
	public void testBinomialTestTwoSidedPhred2() {
		CumulativeBinomialDistribution dist = new CumulativeBinomialDistribution(2, 0.5);
		double res = dist.binomialTestTwoSidedPHRED(1);
		logger.info(res);
		double res2 = dist.binomialTestTwoSidedPvalue(1);
		logger.info(res2);
	}
	
	
	@Test
	public void testBinomialTestTwoSided2() {
		Exception e = null;
		try {
			CumulativeBinomialDistribution dist = new CumulativeBinomialDistribution(0, 0.5);
		} catch(Exception e2) {
			e = e2;
		}
		assertNotNull(e);
		
	
	}
	

}
