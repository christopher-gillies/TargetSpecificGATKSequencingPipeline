package org.kidneyomics.fluidigm;

import static org.junit.Assert.*;

import org.junit.Test;
import org.kidneyomics.vcf.GenotypeField;

public class GT_DP_GQ_PLFieldParserTest {

	@Test
	public void test1() {
		GT_DP_GQ_PLFieldParser parser = new GT_DP_GQ_PLFieldParser();
		
		GenotypeField field = parser.parse("0/0:80:160:0,215,101");
		
		assertEquals("0/0:80:160:0,215,101", field.toString("GT:DP:GQ:PL"));
	}
	
	
	@Test
	public void test2() {
		GT_DP_GQ_PLFieldParser parser = new GT_DP_GQ_PLFieldParser();
		
		GenotypeField field = parser.parse("0/0:393:255:0,255,255");
		
		assertEquals("0/0:393:255:0,255,255", field.toString("GT:DP:GQ:PL"));
	}
	
	@Test
	public void test3() {
		GT_DP_GQ_PLFieldParser parser = new GT_DP_GQ_PLFieldParser();
		
		GenotypeField field = parser.parse("0/1:0:3:0,0,0");
		
		assertEquals("0/1:0:3:0,0,0", field.toString("GT:DP:GQ:PL"));
	}

}
