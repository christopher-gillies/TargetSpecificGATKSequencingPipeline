package org.kidneyomics.structures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class IntervalTreeSet {

	private Map<String,IntervalTree> treeSet;
	
	public IntervalTreeSet(List<String> intervals) throws Exception {
		
		treeSet = new HashMap<String,IntervalTree>();
		
		/*
		 * Get intervals mapped by chromosome
		 */
		Map<String,List<Interval>> mapOfIntervals = IntervalTree.makeIntervalMapByChr(intervals);

		/*
		 * Tree for each chromosome's intervals
		 */
		for(Map.Entry<String, List<Interval>> entry : mapOfIntervals.entrySet()) {
			treeSet.put(entry.getKey(), new IntervalTree(entry.getValue()));
		}
		
	}
	
	public static IntervalTreeSet IntervalTreeSetFromCollection(Collection<String> intervals) throws Exception {
		List<String> newIntervals = new ArrayList<String>(intervals.size());
		newIntervals.addAll(intervals);
		return new IntervalTreeSet(newIntervals);
	}
	
	/**
	 * 
	 * @param chr - chromosome to search
	 * @param interval - interval to search for
	 * @return true if the interval overlaps some interval for the specified chr. Otherwise false
	 */
	public boolean query(String chr, Interval interval) {
		IntervalTree t = treeSet.get(chr);
		if(t != null) {
			return t.query(interval);
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @return total coverage across all chromosomes and intervals
	 */
	public int computeTotalIntervalCoverage() {
		int total = 0;
		for(IntervalTree t : treeSet.values()) {
			total += t.computeIntervalCoverage();
		}
		return total;
	}
	
	/**
	 * 
	 * @return a map that contains an entry for each chromosome and each entry contains a list of intervals
	 */
	public Map<String,List<Interval>> getIntervalsLowestToHighest() {
		Map<String,List<Interval>> intervalSet = new HashMap<String, List<Interval>>();
		for(Map.Entry<String, IntervalTree> entry : this.treeSet.entrySet()) {
			intervalSet.put(entry.getKey(), entry.getValue().getIntervalsLowestToHighest());
		}
		return intervalSet;
	}
	
	/**
	 * 
	 * @return a list of intervals with all overlapping intervals linked together
	 */
	public List<String> getIntervals() {
		List<String> intervals = new LinkedList<String>();
		
		List<Map.Entry<String, IntervalTree>> entriesToSort =  new ArrayList<Map.Entry<String, IntervalTree>>();
		
		entriesToSort.addAll(this.treeSet.entrySet());
		Collections.sort(entriesToSort, new IntervalEntryKeyComparator());
		
		for(Map.Entry<String, IntervalTree> entry : entriesToSort) {
			String chr = entry.getKey();
			List<Interval> chrIntervals = entry.getValue().getIntervalsLowestToHighest();
			for(Interval interval : chrIntervals) {
				intervals.add(interval.toString(chr));
			}
		}
		return intervals;
	}
	
	
 	
}
