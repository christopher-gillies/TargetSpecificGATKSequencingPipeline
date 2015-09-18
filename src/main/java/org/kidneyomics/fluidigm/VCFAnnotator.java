package org.kidneyomics.fluidigm;

import java.io.IOException;

public interface VCFAnnotator {
	void annotate(String in, String out) throws IOException;
}
