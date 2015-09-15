package org.kidneyomics.fluidigm;

public class TrimArgs {

	private String adapter1 = "";
	private String adapter2 = "";
	private int tspl = 20;
	private int minAdapterOverlap = 7;
	private double err = 0.05;
	
	public String getAdapter1() {
		return adapter1;
	}
	public TrimArgs setAdapter1(String adapter1) {
		this.adapter1 = adapter1;
		return this;
	}
	public String getAdapter2() {
		return adapter2;
	}
	public TrimArgs setAdapter2(String adapter2) {
		this.adapter2 = adapter2;
		return this;
	}
	
	
	/*
	 * target specific length
	 */
	public int getTspl() {
		return tspl;
	}
	public TrimArgs setTspl(int tspl) {
		this.tspl = tspl;
		return this;
	}
	public int getMinAdapterOverlap() {
		return minAdapterOverlap;
	}
	public TrimArgs setMinAdapterOverlap(int minAdapterOverlap) {
		this.minAdapterOverlap = minAdapterOverlap;
		return this;
	}
	public double getErr() {
		return err;
	}
	public TrimArgs setErr(double err) {
		this.err = err;
		return this;
	}
	
	
	
	
}
