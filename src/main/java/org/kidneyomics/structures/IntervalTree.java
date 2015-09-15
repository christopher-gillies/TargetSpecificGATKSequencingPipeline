package org.kidneyomics.structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;

public class IntervalTree {

	
	private IntervalNode root;
	/**
	 * Create a list of non-overlapping intervals for the tree
	 * O(log n) query
	 * @param intervals MUST BE FROM THE SAME CHROMOSOME OR THIS WILL NOT WORK !!!
	 */
	public IntervalTree (List<Interval> intervals) {
		if(intervals.size() == 0) {
			throw new IllegalArgumentException("intervals size must be greater than 0");
		} else {
			List<Interval> nonOverlappingIntervals = IntervalTree.createNonOverlapping(intervals);
			this.root = new IntervalNode();
			addNode(this.root, nonOverlappingIntervals);
		}
	}
	
	/**
	 * 
	 * @param interval
	 * @return true if the input interval intersects some interval in the tree and false otherwise
	 */
	public boolean query(Interval interval) {
		return this.root.query(interval);
	}
	
	public IntervalNode getRoot() {
		return root;
	}


	public void setRoot(IntervalNode root) {
		this.root = root;
	}



	public String toString() {
		return root.toString();
	}
	
	public List<Interval> getIntervalsLowestToHighest() {
		return this.root.getIntervalsLowestToHighest();
	}

	public int computeIntervalCoverage() {
		return this.root.computeIntervalCoverage();
	}
	
	private IntervalNode addNode(IntervalNode node, List<Interval> intervals) {
		if(intervals == null || intervals.size() == 0) {
			return null;
		} else if(intervals.size() == 1) {
			node.setInterval(intervals.get(0));
			node.setLeft(null);
			node.setRight(null);
			return node;
		} else {
			Collections.sort(intervals);
			int centerIndex = -1;
			if(intervals.size() % 2 == 0) {
				//even
				int change = RandomUtils.nextInt(0, 2);
				centerIndex = intervals.size() / 2 - change;
				
			} else {
				//odd
				centerIndex = intervals.size() / 2;
			}
			
			//Set interval
			Interval center = intervals.get(centerIndex);
			node.setInterval(center);
			
			//Left
			List<Interval> leftIntervals = intervals.subList(0, centerIndex);
			IntervalNode left = addNode(new IntervalNode(),leftIntervals);
			node.setLeft(left);
			
			//Right
			List<Interval> rightIntervals = intervals.subList(centerIndex + 1, intervals.size());
			IntervalNode right = addNode(new IntervalNode(),rightIntervals);
			node.setRight(right);

			
			return node;
		}
	}
	
	/**
	 * 
	 * @param intervals
	 * @return return intervals that are chained together if they overlap
	 */
	public static List<Interval> createNonOverlapping(List<Interval> intervals) {
		
		/*
		 * Sort by start position
		 */
		Collections.sort(intervals);
		
		List<Interval> nonOverlappingIntervals = new ArrayList<Interval>(intervals.size());
		Iterator<Interval> iterA = intervals.iterator();
		
		Interval current = iterA.next();
		while(iterA.hasNext()) {
			Interval next = iterA.next();
			if(current.overlapsWith(next)) {
				/*
				 * if they overlap then set the current interval to be the merging of the two intervals under investigation
				 */
				current = Interval.merge(current, next);
			} else {
				/*
				 * else they do not overlap so save the current interval and then set the current interval to be the next interval
				 * the next loop iteration will update next
				 */
				nonOverlappingIntervals.add(current);
				current = next;
			}
		}
		/*
		 * add last interval
		 */
		nonOverlappingIntervals.add(current);
		
		return nonOverlappingIntervals;
	}
	
	/**
	 * 
	 * @param intervals - a list CHR:POS-POS
	 * @return map of lists for each CHR in interval form
	 * @throws Exception
	 */
	public static Map<String,List<Interval>> makeIntervalMapByChr(List<String> intervals) throws Exception {
		Map<String,List<Interval>> chrIntervals = new HashMap<String,List<Interval>>();
		
		for( String stInterval : intervals) {
			
			String split[] = stInterval.split(":");
			if(split.length != 2) {
				throw new Exception("Error interval not formatted correctly. Expected CHR:POS-POS but found: " + stInterval);
			}
			
			String ends[] = split[1].split("-");
			if(ends.length != 2) {
				throw new Exception("Error interval not formatted correctly. Expected CHR:POS-POS but found: " + stInterval);
			}
			
			int start = Integer.parseInt(ends[0]);
			int end = Integer.parseInt(ends[1]);
			String chr = split[0];
			if(chrIntervals.containsKey(chr)) {
				chrIntervals.get(chr).add(new Interval(start,end));
			} else {
				chrIntervals.put(chr, new LinkedList<Interval>());
				chrIntervals.get(chr).add(new Interval(start,end));
			}
			
		}
		
		
		return chrIntervals;
		
	}

}
