package org.kidneyomics.structures;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

public class IntervalTreeTest {

	
	Logger logger = Logger.getLogger(IntervalTreeTest.class);
	
	
	@Test
	public void testCreateNonOverlappingList1() {
		List<Interval> intervals = new LinkedList<Interval>();
		intervals.add(new Interval(-10,5));
		intervals.add(new Interval(25,30));
		intervals.add(new Interval(2,20));
		intervals.add(new Interval(0,10));
		
		
		
		//List<Interval>
		List<Interval> nonOverlapping = IntervalTree.createNonOverlapping(intervals);
		
		assertTrue(nonOverlapping.size() == 2);
		
		
		assertTrue(nonOverlapping.get(0).getStart() == -10);
		assertTrue(nonOverlapping.get(0).getEnd() == 20);
		
		
		assertTrue(nonOverlapping.get(1).getStart() == 25);
		assertTrue(nonOverlapping.get(1).getEnd() == 30);
	}
	
	
	@Test
	public void testCreateNonOverlappingList2() {
		List<Interval> intervals = new LinkedList<Interval>();
		intervals.add(new Interval(-10,5));
		intervals.add(new Interval(25,30));
		intervals.add(new Interval(2,20));
		intervals.add(new Interval(0,10));
		intervals.add(new Interval(-100,100));
		
		
		//List<Interval>
		List<Interval> nonOverlapping = IntervalTree.createNonOverlapping(intervals);
		
		assertTrue(nonOverlapping.size() == 1);
		
		
		assertTrue(nonOverlapping.get(0).getStart() == -100);
		assertTrue(nonOverlapping.get(0).getEnd() == 100);
		
	}
	
	
	
	@Test
	public void testCreateNonOverlappingList3() {
		List<Interval> intervals = new LinkedList<Interval>();
		intervals.add(new Interval(1,1));
		intervals.add(new Interval(2,2));
		intervals.add(new Interval(3,3));
		intervals.add(new Interval(4,4));
		intervals.add(new Interval(5,5));
		
		
		//List<Interval>
		List<Interval> nonOverlapping = IntervalTree.createNonOverlapping(intervals);
		
		assertTrue(nonOverlapping.size() == 5);
		
		
	}
	
	
	@Test
	public void testPrintIntervalTree() {
		List<Interval> intervals = new LinkedList<Interval>();
		intervals.add(new Interval(3,3));
		intervals.add(new Interval(5,5));
		intervals.add(new Interval(1,1));
		intervals.add(new Interval(2,2));
		
		intervals.add(new Interval(4,4));
		
		IntervalTree t = new IntervalTree(intervals);
		logger.info(t.toString());
		
	}
	
	
	@Test
	public void checkIntervalTree() {
		List<Interval> intervals = new LinkedList<Interval>();
		intervals.add(new Interval(3,3));
		intervals.add(new Interval(5,5));
		intervals.add(new Interval(1,1));
		intervals.add(new Interval(2,2));
		
		intervals.add(new Interval(4,4));
		
		
				
		for(int i = 0; i < 20; i++) {
			IntervalTree t = new IntervalTree(intervals);
			//logger.info(t.toString());
			IntervalNode root = t.getRoot();
			assertTrue(root.getInterval().getStart() == 3);
			
			IntervalNode rootLeft = root.getLeft();
			
			assertTrue(rootLeft.getInterval().getStart() == 2 || rootLeft.getInterval().getStart() == 1);
			
			if(rootLeft.getInterval().getStart() == 2) {
				assertTrue(rootLeft.getRight() == null);
				assertTrue(rootLeft.getLeft().getInterval().getStart() == 1);
			} else {
				assertTrue(rootLeft.getLeft() == null);
				assertTrue(rootLeft.getRight().getInterval().getStart() == 2);
			}
			
			
			IntervalNode rootRight = root.getRight();
			
			assertTrue(rootRight.getInterval().getStart() == 4 || rootRight.getInterval().getStart() == 5);
			
			if(rootRight.getInterval().getStart() == 5) {
				assertTrue(rootRight.getRight() == null);
				assertTrue(rootRight.getLeft().getInterval().getStart() == 4);
			} else {
				assertTrue(rootRight.getLeft() == null);
				assertTrue(rootRight.getRight().getInterval().getStart() == 5);
			}
		}
		
	}
	
	
	@Test
	public void testQuery() {
		List<Interval> intervals = new LinkedList<Interval>();
		intervals.add(new Interval(300,400));
		intervals.add(new Interval(500,600));
		intervals.add(new Interval(0,100));
		intervals.add(new Interval(200,300));
		
		intervals.add(new Interval(101,199));
		
		IntervalTree t = new IntervalTree(intervals);
		logger.info(t.toString());
		
		assertTrue(t.query(new Interval(550,550)));
		
		
		assertTrue(t.query(new Interval(100,100)));
		
		assertFalse(t.query(new Interval(-1,-1)));
	}
	
	
	@Test
	public void testComputeTotalCoverage() {
		List<Interval> intervals = new LinkedList<Interval>();
		intervals.add(new Interval(300,400));
		intervals.add(new Interval(500,600));
		intervals.add(new Interval(0,100));
		intervals.add(new Interval(200,300));
		
		intervals.add(new Interval(101,199));
		
		IntervalTree t = new IntervalTree(intervals);
		assertTrue(t.computeIntervalCoverage() == 502);
	}
	
	
	@Test
	public void testGetIntervalsLowestToHighest() {
		List<Interval> intervals = new LinkedList<Interval>();
		intervals.add(new Interval(3,3));
		intervals.add(new Interval(5,5));
		intervals.add(new Interval(1,1));
		intervals.add(new Interval(2,2));
		
		intervals.add(new Interval(4,4));
		
		IntervalTree t = new IntervalTree(intervals);
		List<Interval> ordered = t.getIntervalsLowestToHighest();
		assertTrue(ordered.size() == 5);
		assertTrue(ordered.get(0).getStart() == 1);
		assertTrue(ordered.get(0).getEnd() == 1);
		assertTrue(ordered.get(1).getStart() == 2);
		assertTrue(ordered.get(1).getEnd() == 2);
		assertTrue(ordered.get(2).getStart() == 3);
		assertTrue(ordered.get(2).getEnd() == 3);
		assertTrue(ordered.get(3).getStart() == 4);
		assertTrue(ordered.get(3).getEnd() == 4);
		assertTrue(ordered.get(4).getStart() == 5);
		assertTrue(ordered.get(4).getEnd() == 5);
		
	}
	
	
}
