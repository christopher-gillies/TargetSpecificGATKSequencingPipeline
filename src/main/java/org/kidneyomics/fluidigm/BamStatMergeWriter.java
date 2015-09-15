package org.kidneyomics.fluidigm;

import java.net.URL;

import org.apache.log4j.Logger;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STRawGroupDir;

public class BamStatMergeWriter implements TemplateStringWriter {
	
	
	private String templateDirectory;
	
	private String bamList;
	private String outFile;
	
	Logger logger = Logger.getLogger(BamStatMergeWriter.class);
	
	
	
	public String getTemplateDirectory() {
		return templateDirectory;
	}




	public void setTemplateDirectory(String templateDirectory) {
		this.templateDirectory = templateDirectory;
	}




	@Override
	public String toString() {
		return writeTemplateToString();
	}




	@Override
	public String writeTemplateToString() {
		logger.info(templateDirectory);
		STGroup group = new STRawGroupDir(templateDirectory);
		
		ST st = group.getInstanceOf("bamstatmerge");
		
		st.add("outFile", outFile);
		st.add("bamList", bamList);
		
		return st.render();
	}




	public String getBamList() {
		return bamList;
	}




	public void setBamList(String bamList) {
		this.bamList = bamList;
	}




	public String getOutFile() {
		return outFile;
	}




	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	
	
	
}
