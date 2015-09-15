package org.kidneyomics.fluidigm;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.kidneyomics.vcf.GenotypeField;

public class TestAlleleBalanceAndAltDepthCalculator {

	@Test
	public void testAlleleBalance1() {
		
		
		GenotypeField gt = new GenotypeField();
		int alleles[] = { 0, 1};
		List<Integer> ads = new LinkedList<Integer>();
		ads.add(38);
		ads.add(4);
		ads.add(0);
		gt.setAlleles(alleles);
		gt.setAllelicDepths(ads);
		
		assertEquals( 4.0f / (38.0f + 4.0f), AlleleBalanceAndAltDepthCalculator.alleleBalance(gt),0.0001);
	}
	
	
	@Test
	public void testAlleleBalance2() {
		
		
		GenotypeField gt = new GenotypeField();
		int alleles[] = { 2, 0};
		List<Integer> ads = new LinkedList<Integer>();
		ads.add(10);
		ads.add(0);
		ads.add(2);
		gt.setAlleles(alleles);
		gt.setAllelicDepths(ads);
		
		assertEquals( 2.0f / (10.0f + 2.0f), AlleleBalanceAndAltDepthCalculator.alleleBalance(gt),0.0001);
	}
	
	@Test
	public void testAlleleBalance3() {
		
		
		GenotypeField gt = new GenotypeField();
		int alleles[] = { 2, 2};
		List<Integer> ads = new LinkedList<Integer>();
		ads.add(0);
		ads.add(0);
		ads.add(10);
		gt.setAlleles(alleles);
		gt.setAllelicDepths(ads);
		
		assertEquals( 1.0f, AlleleBalanceAndAltDepthCalculator.alleleBalance(gt),0.0001);
	}

	@Test
	public void testAlleleBalance4() {
		
		
		GenotypeField gt = new GenotypeField();
		int alleles[] = { 0, 0};
		List<Integer> ads = new LinkedList<Integer>();
		ads.add(10);
		ads.add(0);
		ads.add(0);
		gt.setAlleles(alleles);
		gt.setAllelicDepths(ads);
		
		assertEquals( 0.0f, AlleleBalanceAndAltDepthCalculator.alleleBalance(gt),0.0001);
	}
	
	@Test
	public void testAlleleBalance5() {
		
		
		GenotypeField gt = new GenotypeField();
		int alleles[] = { 1, 2};
		List<Integer> ads = new LinkedList<Integer>();
		ads.add(0);
		ads.add(10);
		ads.add(20);
		gt.setAlleles(alleles);
		gt.setAllelicDepths(ads);
		
		assertEquals( 20.0f / (10.0f + 20.0f), AlleleBalanceAndAltDepthCalculator.alleleBalance(gt),0.0001);
	}
	
	@Test
	public void testAltDepth1() {
		
		
		GenotypeField gt = new GenotypeField();
		int alleles[] = { 0, 1};
		List<Integer> ads = new LinkedList<Integer>();
		ads.add(38);
		ads.add(4);
		ads.add(0);
		gt.setAlleles(alleles);
		gt.setAllelicDepths(ads);
		
		assertEquals( 4, AlleleBalanceAndAltDepthCalculator.altDepth(gt));
	}
	
	
	@Test
	public void testAltDepth2() {
		
		
		GenotypeField gt = new GenotypeField();
		int alleles[] = { 2, 0};
		List<Integer> ads = new LinkedList<Integer>();
		ads.add(10);
		ads.add(0);
		ads.add(2);
		gt.setAlleles(alleles);
		gt.setAllelicDepths(ads);
		
		assertEquals( 2, AlleleBalanceAndAltDepthCalculator.altDepth(gt));
	}
	
	
	@Test
	public void testAltDepth4() {
		
		
		GenotypeField gt = new GenotypeField();
		int alleles[] = { 0, 0};
		List<Integer> ads = new LinkedList<Integer>();
		ads.add(10);
		ads.add(0);
		ads.add(0);
		gt.setAlleles(alleles);
		gt.setAllelicDepths(ads);
		
		assertEquals( 0, AlleleBalanceAndAltDepthCalculator.altDepth(gt));
	}
	
	@Test
	public void testAltDepth5() {
		
		
		GenotypeField gt = new GenotypeField();
		int alleles[] = { 1, 2};
		List<Integer> ads = new LinkedList<Integer>();
		ads.add(0);
		ads.add(10);
		ads.add(20);
		gt.setAlleles(alleles);
		gt.setAllelicDepths(ads);
		
		assertEquals( 20, AlleleBalanceAndAltDepthCalculator.altDepth(gt));
	}
	
	
	@Test
	public void testRefDepth1() {
		
		
		GenotypeField gt = new GenotypeField();
		int alleles[] = { 0, 1};
		List<Integer> ads = new LinkedList<Integer>();
		ads.add(38);
		ads.add(4);
		ads.add(0);
		gt.setAlleles(alleles);
		gt.setAllelicDepths(ads);
		
		assertEquals( 38, AlleleBalanceAndAltDepthCalculator.refDepth(gt));
	}
	

}
