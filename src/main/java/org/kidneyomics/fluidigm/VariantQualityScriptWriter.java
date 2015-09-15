package org.kidneyomics.fluidigm;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STRawGroupDir;

public class VariantQualityScriptWriter implements TemplateStringWriter {

	private String outDir;
	private String templateDirectory;
	private String statsFile;
	
	
	public String getOutDir() {
		return outDir;
	}


	public void setOutDir(String outDir) {
		this.outDir = outDir;
	}


	public String getTemplateDirectory() {
		return templateDirectory;
	}


	public void setTemplateDirectory(String templateDirectory) {
		this.templateDirectory = templateDirectory;
	}


	@Override
	public String writeTemplateToString() {
		STGroup group = new STRawGroupDir(templateDirectory);
		
		ST st =  null;
		st = group.getInstanceOf("variantqcscript");
		
		st.add("statsFile", statsFile);
		st.add("outDir", outDir);
		
		return st.render();
	}

	
	@Override
	public String toString() {
		return writeTemplateToString();
	}


	public String getStatsFile() {
		return statsFile;
	}


	public void setStatsFile(String statsFile) {
		this.statsFile = statsFile;
	}
	
	
	
}
