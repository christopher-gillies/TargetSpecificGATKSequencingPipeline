package org.kidneyomics.fluidigm;

public interface VCFReheader {
	void reheader(String inVcf, String outVcf, String idMap) throws Exception;
}
