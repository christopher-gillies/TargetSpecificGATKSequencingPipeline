package org.kidneyomics.fluidigm;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestFASTQ {

	@Test
	public void test1() {
		FASTQ a = new FASTQ();
		a.setFile1("f1");
		assertFalse(a.isPairedEnd());
	}
	
	@Test
	public void test2() {
		FASTQ a = new FASTQ();
		a.setFile1("f1");
		a.setFile2("f2");
		assertTrue(a.isPairedEnd());
	}
	
	//@Test
	public void test3() {
		FASTQ a = new FASTQ();
		a.setFile1("/Users/cgillies/Documents/workspace-sts-3.6.1.RELEASE/FluidigmAlignmentVariantCalling/pairedEndTestData/446840_S1_L001_R1_001.fastq.gz");
		a.setFile2("/Users/cgillies/Documents/workspace-sts-3.6.1.RELEASE/FluidigmAlignmentVariantCalling/pairedEndTestData/446840_S1_L001_R2_001.fastq.gz");
		assertTrue(a.isPairedEnd());
		assertTrue(a.isValid());
	}
	
	//@Test
	public void test4() {
		FASTQ a = new FASTQ();
		a.setFile1("/Users/cgillies/Documents/workspace-sts-3.6.1.RELEASE/FluidigmAlignmentVariantCalling/pairedEndTestData/446840_S1_L001_R1_001.fastq.gz");
		assertFalse(a.isPairedEnd());
		assertTrue(a.isValid());
	}

}
