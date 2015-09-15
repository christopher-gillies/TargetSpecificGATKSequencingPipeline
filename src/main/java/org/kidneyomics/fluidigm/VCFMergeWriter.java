package org.kidneyomics.fluidigm;

import java.net.URL;

import org.apache.log4j.Logger;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STRawGroupDir;

public class VCFMergeWriter implements TemplateStringWriter {
	
	
	private String templateDirectory;

	
	Logger logger = Logger.getLogger(VCFMergeWriter.class);
	
	
	private String outPrefix;
	private String outDir;
	private String fileList;
	
	
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
		
		ST st = group.getInstanceOf("vcfmerge");
		st.add("fileList", fileList);
		st.add("outPrefix", outPrefix);
		st.add("outDir", outDir);
		
		return st.render();
	}

	public String getOutPrefix() {
		return outPrefix;
	}

	public void setOutPrefix(String outPrefix) {
		this.outPrefix = outPrefix;
	}

	public String getOutDir() {
		return outDir;
	}

	public void setOutDir(String outDir) {
		this.outDir = outDir;
	}

	public String getFileList() {
		return fileList;
	}

	public void setFileList(String fileList) {
		this.fileList = fileList;
	}


	

	
	
}
