package org.kidneyomics.fluidigm;

import org.stringtemplate.v4.ST;

public class BAM {
	
	private String bamFile;
	private String bamIndex;
	private String statFile;
	
	public String getBamFile() {
		return bamFile;
	}
	public BAM setBamFile(String bamFile) {
		this.bamFile = bamFile;
		return this;
	}
	public String getBamIndex() {
		return bamIndex;
	}
	public BAM setBamIndex(String bamIndex) {
		this.bamIndex = bamIndex;
		return this;
	}
	public String getStatFile() {
		return statFile;
	}
	public void setStatFile(String statFile) {
		this.statFile = statFile;
	}
	
	@Override
	public String toString() {
		ST temp = new ST("BAM: <bam>, INDEX: <index>, STATS: <stats>");
		temp.add("bam", bamFile)
		.add("index", bamIndex)
		.add("stats", statFile);
		return temp.render();
	}
	
}
