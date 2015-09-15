package org.kidneyomics.fluidigm;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.kidneyomics.vcf.GenotypeFieldParser;
import org.kidneyomics.vcf.VCFLine;

public class TestDecomposeMultiAlleleicSites {

	
	Logger logger = Logger.getLogger(TestDecomposeMultiAlleleicSites.class);
	@Test
	public void test() throws Exception {
		DecomposeMultiAlleleicSites decomp = new DecomposeMultiAlleleicSites();
		List<String> sampleIds = new LinkedList<String>();
		sampleIds.add("A1");
		sampleIds.add("A2");
		sampleIds.add("A3");
		sampleIds.add("A4");
		sampleIds.add("A5");
		VCFLine vline = new VCFLine("17	48153905	.	TGGG	TGGGG,T,TGG	952590.0	PASS	AC=3,106,4;AF=0.00543,0.19203,0.00725;AN=552	GT:AD:DP:GQ	0/0:30,2,0,3:35:13	0/2:16,2,294,1:314:45	1/2:16,2,294,1:314:45	2/2:16,2,294,1:314:45	1/3:16,2,294,1:314:45",sampleIds);
		
		GenotypeFieldParser gtfp = new GT_AD_DP_GQFieldParser();
		
		List<VCFLine> decomposed = decomp.createMultialleleSites(vline, gtfp);
		
		assertTrue(decomposed.size() == 3);
		
		for(VCFLine newVline : decomposed) {
			logger.info(newVline.toString());	
		}
		
		VCFLine a1 = decomposed.get(0);
		VCFLine a2 = decomposed.get(1);
		VCFLine a3 = decomposed.get(2);
		
		assertEquals(a1.getGenotypes().get("A1"),"0/0:30,2,0,3:35:13");
		assertEquals(a1.getGenotypes().get("A2"),"0/0:16,2,294,1:314:45");
		assertEquals(a1.getGenotypes().get("A3"),"1/0:16,2,294,1:314:45");
		assertEquals(a1.getGenotypes().get("A4"),"0/0:16,2,294,1:314:45");
		assertEquals(a1.getGenotypes().get("A5"),"1/0:16,2,294,1:314:45");
		
		assertEquals(a2.getGenotypes().get("A1"),"0/0:30,2,0,3:35:13");
		assertEquals(a2.getGenotypes().get("A2"),"0/1:16,2,294,1:314:45");
		assertEquals(a2.getGenotypes().get("A3"),"0/1:16,2,294,1:314:45");
		assertEquals(a2.getGenotypes().get("A4"),"1/1:16,2,294,1:314:45");
		assertEquals(a2.getGenotypes().get("A5"),"0/0:16,2,294,1:314:45");
		
		assertEquals(a3.getGenotypes().get("A1"),"0/0:30,2,0,3:35:13");
		assertEquals(a3.getGenotypes().get("A2"),"0/0:16,2,294,1:314:45");
		assertEquals(a3.getGenotypes().get("A3"),"0/0:16,2,294,1:314:45");
		assertEquals(a3.getGenotypes().get("A4"),"0/0:16,2,294,1:314:45");
		assertEquals(a3.getGenotypes().get("A5"),"0/1:16,2,294,1:314:45");
	}

}
