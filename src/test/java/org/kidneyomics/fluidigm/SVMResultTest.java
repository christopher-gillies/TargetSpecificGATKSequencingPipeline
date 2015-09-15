package org.kidneyomics.fluidigm;

import static org.junit.Assert.*;

import org.junit.Test;

public class SVMResultTest {

	@Test
	public void testHashCodeIdentity() {
		
		SVMResult a = new SVMResult();
		
		a.setKey("1_179520257_G_C");
		
		assertEquals(a.hashCode(),a.hashCode());
		assertEquals(a.equals(a),true);
	}
	
	@Test
	public void testHashCodeIdentity2() {
		
		SVMResult a = new SVMResult();
		
		a.setKey("1_179520257_G_C");
		
		SVMResult b = new SVMResult();
		
		b.setKey("1_179520257_G_C");
		
		assertEquals(a.hashCode(),b.hashCode());
		assertEquals(a.equals(b),true);
	}
	
	
	@Test
	public void testHashCodeDifferent() {
		
		SVMResult a = new SVMResult();
		
		a.setKey("1_179520257_G_C");
		
		SVMResult b = new SVMResult();
		
		b.setKey("1_179520257_C_C");
		
		assertNotEquals(a.hashCode(),b.hashCode());
		assertNotEquals(a.equals(b),true);
	}
	
	
	@Test
	public void testParse() throws Exception {
		
		SVMResult a = SVMResult.getSVMResultFromLine("19_39220183_C_A	0.151688281916183	0.127628549830933	FALSE	19:39220183	NOT_IN_DBSNP	NA	NA	FILTER	UNKNOWN");
		assertEquals(a.getKey(),"19_39220183_C_A");
	}


	@Test
	public void testParse2() throws Exception {
		
		SVMResult a = SVMResult.getSVMResultFromLine("1_179520257_G_C	0.137329161385947	0.11523758306905	FALSE	1:179520257	NOT_IN_DBSNP	NA	NA	FILTER	UNKNOWN");
		assertEquals(a.getKey(),"1_179520257_G_C");
	}
}
