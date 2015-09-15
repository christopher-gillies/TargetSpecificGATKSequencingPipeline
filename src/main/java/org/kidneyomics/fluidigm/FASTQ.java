package org.kidneyomics.fluidigm;

import java.io.File;

import org.springframework.util.StringUtils;


public class FASTQ {

	String file1;
	String file2;
	
	
	public boolean isPairedEnd() {
		return !StringUtils.isEmpty(file1) && !StringUtils.isEmpty(file2);
	}

	public String getFile1() {
		return file1;
	}
	public void setFile1(String file1) {
		this.file1 = file1;
	}
	public String getFile2() {
		return file2;
	}
	public void setFile2(String file2) {
		this.file2 = file2;
	}
	
	public boolean isValid() {
		File f1 = new File(file1);
		if(isPairedEnd()) {
			File f2 = new File(file2);
			return f1.exists() && f2.exists();
		} else if(!StringUtils.isEmpty(file1) && StringUtils.isEmpty(file2)) {
			return f1.exists();
		} else {
			return false;
		}

	}
	
	
}
