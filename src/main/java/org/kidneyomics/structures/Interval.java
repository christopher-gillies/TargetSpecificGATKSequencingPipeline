package org.kidneyomics.structures;

public class Interval implements Comparable<Interval> {

	private int start;
	private int end;
	
	public Interval(int start, int end) {
		this.start = start;
		this.end = end;
		if(start > end) {
			throw new IllegalArgumentException("start must be less than or equal to the end");
		}
	}
	
	public boolean overlapsWith(Interval interval) {
		
		if(interval.getStart() >= this.start && interval.getStart() <= this.end) {
			/*
			 * INTERNAL OR PARTIAL INTERSECTION GREATER END POINT
			 * |-----------|
			 *    |--|
			 * 
			 * |-------------|
			 *               |--------|
			 */
			return true;
		} else if(interval.getStart() <= this.start && interval.getEnd() >= this.start) {
			/*
			 * 
			 * 
			 * A        |----------------|
			 * B |-------|
			 * 
			 * A  |-------|
			 * B |---------------|
			 */
			return true;
		} else {
			/*
			 * 
			 * A |--------|
			 * B           |---------|
			 * 
			 * A                |---------------------------|
			 * B |-------------|
			 */
			return false;
		}
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	
	/**
	 * 
	 * @param Interval a
	 * @param Interval b
	 * @return new "superset" Interval if the intervals overlap or null
	 * 
	 */
	public static Interval merge(Interval a, Interval b) {
		if(a.overlapsWith(b)) {
			int start = Math.min(a.getStart(), b.getStart());
			int end = Math.max(a.getEnd(), b.getEnd());
			return new Interval(start,end);
		} else {
			return null;
		}
	}

	@Override
	public int compareTo(Interval b) {
		int cmp = this.getStart() - b.getStart();
		if(cmp == 0) {
			return 0;
		} else if(cmp < 0) {
			return -1;
		} else {
			return 1;
		}
	}
	
	public String toString() {
		return "[ " + this.start + "-" + this.end + " ]";
	}
	
	public String toString(String chr) {
		return chr + ":" + this.start + "-" + this.end;
	}
	
	public int size() {
		return this.end - this.start + 1;
	}
	
}
