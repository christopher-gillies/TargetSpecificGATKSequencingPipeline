package org.kidneyomics.fluidigm;

import static org.junit.Assert.*;

import org.junit.Test;
import org.kidneyomics.vcf.GenotypeField;

public class GT_DS_GPFieldParserTest {

	@Test
	public void test1() {
		GT_DS_GPFieldParser parser = new GT_DS_GPFieldParser();
		
		GenotypeField field = parser.parse("0|0:0.03:0.97,0.03,0");
		
		assertEquals("0|0:0.03:0.97,0.03,0.0", field.toString("GT:DS:GP"));
	}
	
	

}
