package org.kidneyomics.fluidigm;

import java.util.Collection;

public interface CommandWriter {

	String writePreprocessingCommands(Collection<Sample> samples, AlignArgs args) throws Exception;
	
	String writeTrimCommands(Collection<Sample> samples, TrimArgs args) throws Exception;
	
	String writeVariantCallCommands(Collection<Sample> samples, CallArgs args) throws Exception;
	
	String applyVQSR(String vcf, VQSRArgs args) throws Exception;
	
	void setOutput(String output);

	String writeVariantCallCommandsUnifiedGenotyper(Collection<Sample> samples,
			CallArgs args) throws Exception;
}
