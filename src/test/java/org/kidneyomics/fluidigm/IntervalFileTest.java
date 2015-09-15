package org.kidneyomics.fluidigm;

import org.junit.Test;
import static org.junit.Assert.*;

public class IntervalFileTest {

	
	@Test
	public void test() {
		IntervalFile f1 = new IntervalFile();
		f1.setEntry("x");
		
		IntervalFile f2 = new IntervalFile();
		f2.setEntry("y");
		
		
		assertTrue(f1.compareTo(f2) == -1);
		assertTrue(f2.compareTo(f1) == 1);
	}
	
	@Test
	public void test2() {
		IntervalFile f1 = new IntervalFile();
		f1.setEntry("1");
		
		IntervalFile f2 = new IntervalFile();
		f2.setEntry("1");
		
		
		assertTrue(f1.compareTo(f2) == 0);
		assertTrue(f2.compareTo(f1) == 0);
	}
}
