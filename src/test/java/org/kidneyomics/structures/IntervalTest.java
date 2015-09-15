package org.kidneyomics.structures;

import static org.junit.Assert.*;

import org.junit.Test;

public class IntervalTest {

	
	@Test
	public void testSize1() {
		
		Interval a = new Interval(0, 10);
		
		assertTrue(a.size() == 11);
		
	}
	
	@Test
	public void testOverlap1() {
		
		Interval a = new Interval(0, 10);
		Interval b = new Interval(5,6);
		
		assertTrue(a.overlapsWith(b));
		
	}
	
	
	
	@Test
	public void testOverlap2() {
		
		Interval a = new Interval(0, 10);
		Interval b = new Interval(-10,0);
		
		assertTrue(a.overlapsWith(b));
		
	}
	
	
	@Test
	public void testOverlap3() {
		
		Interval a = new Interval(0, 10);
		Interval b = new Interval(-10,5);
		
		assertTrue(a.overlapsWith(b));
		
	}
	
	
	@Test
	public void testOverlap4() {
		
		Interval a = new Interval(0, 10);
		Interval b = new Interval(10,11);
		
		assertTrue(a.overlapsWith(b));
		
	}
	
	
	
	@Test
	public void testOverlap5() {
		
		Interval a = new Interval(0, 10);
		Interval b = new Interval(-10,100);
		
		assertTrue(a.overlapsWith(b));
		
	}
	
	
	@Test
	public void testOverlap6() {
		
		Interval a = new Interval(0, 10);
		Interval b = new Interval(10,100);
		
		assertTrue(a.overlapsWith(b));
		
	}
	
	@Test
	public void testOverlap7() {
		
		Interval a = new Interval(0, 10);
		Interval b = new Interval(11,100);
		
		assertFalse(a.overlapsWith(b));
		
	}
	
	
	@Test
	public void testOverlap8() {
		
		Interval a = new Interval(0, 10);
		Interval b = new Interval(-10,-1);
		
		assertFalse(a.overlapsWith(b));
		
	}
	
	
	@Test
	public void testMerge1() {
		
		Interval a = new Interval(0, 10);
		Interval b = new Interval(-10,-1);
		
		assertNull(Interval.merge(a, b));
		
	}
	
	
	@Test
	public void testMerge2() {
		
		Interval a = new Interval(0, 10);
		Interval b = new Interval(-10,0);
		
		Interval merge = Interval.merge(a, b);
		assertTrue(merge.getStart() == -10);
		assertTrue(merge.getEnd() == 10);
		
	}
	
	
	@Test
	public void testMerge3() {
		
		Interval a = new Interval(0, 10);
		Interval b = new Interval(-10,100);
		
		Interval merge = Interval.merge(a, b);
		assertTrue(merge.getStart() == -10);
		assertTrue(merge.getEnd() == 100);
		
	}

}
