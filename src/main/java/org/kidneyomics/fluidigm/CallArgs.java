package org.kidneyomics.fluidigm;

public class CallArgs {
	private String primerList;

	private int cohortSize;
	
	public String getPrimerList() {
		return primerList;
	}

	public void setPrimerList(String primerList) {
		this.primerList = primerList;
	}

	public int getCohortSize() {
		return cohortSize;
	}

	public void setCohortSize(int cohortSize) {
		this.cohortSize = cohortSize;
	}
	
	
	private int numGatkThreads;

	public int getNumGatkThreads() {
		return numGatkThreads;
	}

	public void setNumGatkThreads(int numGatkThreads) {
		this.numGatkThreads = numGatkThreads;
	}
	
	
}
