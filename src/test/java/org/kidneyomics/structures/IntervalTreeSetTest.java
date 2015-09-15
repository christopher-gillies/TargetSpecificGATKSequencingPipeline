package org.kidneyomics.structures;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

public class IntervalTreeSetTest {

	private Logger logger = Logger.getLogger(IntervalTreeSetTest.class);
	@Test
	public void testQuery() throws Exception {
		List<String> intervals = new LinkedList<String>();
		intervals.add("1:100-200");
		intervals.add("2:100-200");
		intervals.add("1:150-300");
		intervals.add("2:50-99");
		
		IntervalTreeSet its = new IntervalTreeSet(intervals);
		
		assertTrue(its.query("2", new Interval(51,60)));
		assertTrue(its.query("1", new Interval(150,150)));
		
		assertTrue(its.query("1", new Interval(290,290)));
		assertFalse(its.query("2", new Interval(290,290)));
	}
	
	
	@Test
	public void testComputeTotalCoverage() throws Exception {
		List<String> intervals = new LinkedList<String>();
		intervals.add("1:100-200"); 
		intervals.add("2:100-200");
		intervals.add("1:150-300"); //201
		intervals.add("2:50-99");
		
		IntervalTreeSet its = new IntervalTreeSet(intervals);
		
		assertTrue(its.computeTotalIntervalCoverage() == 352);
		
		
		
	}
	
	
	@Test
	public void testGetIntervalsLowestToHighest() throws Exception {
		List<String> intervals = new LinkedList<String>();
		intervals.add("1:100-200"); 
		intervals.add("2:100-200");
		intervals.add("1:150-300"); //201
		intervals.add("2:50-99");
		
		IntervalTreeSet its = new IntervalTreeSet(intervals);
		
		Map<String,List<Interval>> intervalMap = its.getIntervalsLowestToHighest();
		assertTrue(intervalMap.size() == 2);
		
		
	}
	
	
	@Test
	public void testGetIntervals() throws Exception {
		List<String> intervals = new LinkedList<String>();
		intervals.add("2:50-100");
		intervals.add("X:100-200"); 
		intervals.add("1:100-200"); 
		intervals.add("2:100-200");
		intervals.add("1:150-300"); //201
		
		intervals.add("3:50-99");
		
		IntervalTreeSet its = new IntervalTreeSet(intervals);
		
		List<String> intervalsOut = its.getIntervals();
		for(String s : intervalsOut) {
			logger.info(s);
		}
		assertTrue(intervalsOut.size() == 4);
		assertTrue(intervalsOut.get(0).equals("1:100-300"));
		
		
	}

}
