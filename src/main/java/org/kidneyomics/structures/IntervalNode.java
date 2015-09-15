package org.kidneyomics.structures;

import java.util.LinkedList;
import java.util.List;

public class IntervalNode {
	private Interval interval;
	private IntervalNode left;
	private IntervalNode right;
	
	public Interval getInterval() {
		return interval;
	}
	public void setInterval(Interval interval) {
		this.interval = interval;
	}
	public IntervalNode getLeft() {
		return left;
	}
	public void setLeft(IntervalNode left) {
		this.left = left;
	}
	public IntervalNode getRight() {
		return right;
	}
	public void setRight(IntervalNode right) {
		this.right = right;
	}
	
	public int computeIntervalCoverage() {
		int coverage = this.getInterval().size();
		
		if(this.getLeft() != null) {
			coverage = coverage + this.getLeft().computeIntervalCoverage();
		}
		
		if(this.getRight() != null) {
			coverage = coverage + this.getRight().computeIntervalCoverage();
		}
		
		return coverage;
	}
	
	/**
	 * 
	 * @return a list of the intervals store in the tree from lowest to highest
	 */
	public List<Interval> getIntervalsLowestToHighest() {
		List<Interval> intervals = new LinkedList<Interval>();
		return getIntervalsLowestToHighest(intervals);
	}
	
	private List<Interval> getIntervalsLowestToHighest(List<Interval> intervals) {

		if(this.getLeft() != null) {
			this.getLeft().getIntervalsLowestToHighest(intervals);
		}
		
		intervals.add(this.getInterval());
		
		if(this.getRight() != null) {
			this.getRight().getIntervalsLowestToHighest(intervals);
		}
		
		return intervals;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toStringBuilder(sb);
		return sb.toString();
	}
	
	public void toStringBuilder(StringBuilder sb) {
	
		if(this.getInterval() == null) {
			return;
		}
		
		sb.append(this.getInterval().toString());
		if(this.getLeft() != null) {
			sb.append("\nLeft For ");
			sb.append(this.getInterval().toString());
			sb.append("\n");
			this.getLeft().toStringBuilder(sb);
		}
		
		if(this.getRight() != null) {
			sb.append("\nRight For ");
			sb.append(this.getInterval().toString());
			sb.append("\n");
			this.getRight().toStringBuilder(sb);
		}
	}
	
	public boolean query(Interval interval) {
		if(this.getInterval().overlapsWith(interval)) {
			return true;
		} else {
			int cmp = interval.compareTo(this.getInterval());
			if(cmp == -1) {
				//The interval is less than the node's interval
				if(this.getLeft() == null) {
					return false;
				} else {
					return this.getLeft().query(interval);
				}
			} else {
				if(this.getRight() == null) {
					return false;
				} else {
					return this.getRight().query(interval);
				}
			}
		}
	}
}
