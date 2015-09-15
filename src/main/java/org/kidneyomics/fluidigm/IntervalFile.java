package org.kidneyomics.fluidigm;

import java.io.File;

public class IntervalFile implements Comparable<IntervalFile> {

	private File file;
	private String entry;
	
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public String getEntry() {
		return entry;
	}
	public void setEntry(String entry) {
		this.entry = entry;
	}
	
	
	public int getSortId() {	
		int chr = -1;
		try {
			chr = Integer.parseInt(this.entry);
		} catch(Exception e) {
			if(this.entry.equalsIgnoreCase("x")) {
				chr = 25;
			} else if(this.entry.equals("y")) {
				chr = 26;
			} else if(this.entry.equals("m")) {
				chr = 27;
			} else if(this.entry.equals("mt")) {
				chr = 27;
			} else {
				chr = 28;
			}
		}
		return chr;
	}
	
	
	@Override
	public int compareTo(IntervalFile other) {
		int cmp = this.getSortId() - other.getSortId();
		if(cmp == 0) {
			return 0;
		} else if(cmp < 0) {
			return -1;
		} else {
			return 1;
		}
		 
	}
	
	
}
