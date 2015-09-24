package org.kidneyomics.fluidigm;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STRawGroupDir;

public class SVMScriptWriter implements TemplateStringWriter {

	private String outDir;
	private String templateDirectory;
	private String statsFile;
	
	private String confirmedSites = null;
	
	private boolean useBayes = false;
	
	
	
	public boolean isUseBayes() {
		return useBayes;
	}


	public void setUseBayes(boolean useBayes) {
		this.useBayes = useBayes;
	}


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
		if(this.confirmedSites == null) {
			st = group.getInstanceOf("sitesvmqcscript");
		} else {
			st = group.getInstanceOf("sitesvmqcscriptconfirmed");
			st.add("confirmedSites", confirmedSites);
		}
		
		if(useBayes) {
			st.add("useBayes", "TRUE");
		} else {
			st.add("useBayes", "FALSE");
		}
		
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
	
	public void setConfirmedSites(String confirmedSites) {
		this.confirmedSites = confirmedSites;
	}
	
	
}
