package org.kidneyomics.fluidigm;

public interface Filter {
	public void filter(String out) throws Exception;
	
	public void setVcf(String vcf);
	
	public void setSnpFilter(String snpFilter);
	
	public void setIndelFilter(String indelFilter);
}
