package org.kidneyomics.fluidigm;

public interface InfoCollector {
	public void setVcf(String vcf);
	public void collectInfo(String fileOut) throws Exception;
}
