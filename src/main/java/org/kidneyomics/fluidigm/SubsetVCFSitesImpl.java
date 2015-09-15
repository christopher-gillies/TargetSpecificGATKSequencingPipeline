package org.kidneyomics.fluidigm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.kidneyomics.vcf.VCFFile;
import org.kidneyomics.vcf.VCFLine;
import org.kidneyomics.vcf.VCFFile.VCFLineIterator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("subsetVCFSitesImpl")
public class SubsetVCFSitesImpl implements SubsetService {

	private String sitesToKeep;
	
	public void subset(String vcf, String out) throws Exception {
		
		
		HashSet<String> sitesToKeepSet = readSites();
		
		VCFFile vfile = new VCFFile();
		
		
		VCFLineIterator iter = vfile.iterator(vcf);
		
		
		Writer writer = new BufferedWriter( new FileWriter(new File(out)));
		
		
		writer.write(StringUtils.join(iter.getHeaderLines(), "\n"));
		writer.write("\n");
		
		while(iter.hasNext()) {
			VCFLine vline = iter.next();
			if(sitesToKeepSet.contains(vline.getId(":"))) {
				writer.write(vline.toString());
				writer.write("\n");
			}
			
		}
		writer.close();
	}
	
	
	private HashSet<String> readSites() throws Exception {
		HashSet<String> sitesToKeepSet = new HashSet<String>();
		File f = new File(sitesToKeep);
		List<String> lines = FileUtils.readLines(f);
		
		for(String line : lines) {
			String newLine = line.replaceAll("\t", ":");
			sitesToKeepSet.add(newLine);
		}
		
		return sitesToKeepSet;
	}

	public String getSitesToKeep() {
		return sitesToKeep;
	}

	public void setSitesToKeep(String sitesToKeep) {
		this.sitesToKeep = sitesToKeep;
	}
	
	
}
