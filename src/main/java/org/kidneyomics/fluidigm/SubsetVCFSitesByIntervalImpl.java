package org.kidneyomics.fluidigm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.kidneyomics.structures.Interval;
import org.kidneyomics.structures.IntervalTreeSet;
import org.kidneyomics.util.ChromosomePositionInterval;
import org.kidneyomics.vcf.VCFFile;
import org.kidneyomics.vcf.VCFLine;
import org.kidneyomics.vcf.VCFFile.VCFLineIterator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("subsetVCFSitesByIntervalImpl")
public class SubsetVCFSitesByIntervalImpl implements SubsetService {

	private String sitesToKeep;
	
	public void subset(String vcf, String out) throws Exception {
		
		
		//List<ChromosomePositionInterval> locationsToKeep = readSites();
		List<String> locationsToKeep = readSitesToStringList();
		IntervalTreeSet its = new IntervalTreeSet(locationsToKeep);
		VCFFile vfile = new VCFFile();
		
		
		VCFLineIterator iter = vfile.iterator(vcf);
		
		
		Writer writer = new BufferedWriter( new FileWriter(new File(out)));
		
		
		writer.write(StringUtils.join(iter.getHeaderLines(), "\n"));
		writer.write("\n");
		
		while(iter.hasNext()) {
			VCFLine vline = iter.next();
			if(inSomeInterval(vline,its)) {
				writer.write(vline.toString());
				writer.write("\n");
			}
			
		}
		writer.close();
	}
	
	
	
	
	private boolean inSomeInterval(VCFLine vline, IntervalTreeSet its) {
		int start = vline.getPos();
		int end =  vline.getPos() + vline.getAlt().length() - 1;
		Interval interval = new Interval(start,end);
		return its.query(vline.getChrom(), interval);
	}
	
	/*
	private boolean inSomeInterval(VCFLine vline, List<ChromosomePositionInterval> locationsToKeep) {
		
		for(ChromosomePositionInterval cpi : locationsToKeep) {
			if(vline.intersectsInterval(cpi)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	private List<ChromosomePositionInterval> readSites() throws Exception {
		List<ChromosomePositionInterval> locationsToKeep = new LinkedList<ChromosomePositionInterval>();
		File f = new File(sitesToKeep);
		List<String> lines = FileUtils.readLines(f);
		
		for(String line : lines) {
			locationsToKeep.add(new ChromosomePositionInterval(line));
		}
		
		return locationsToKeep;
	}
	*/
	
	private List<String> readSitesToStringList() throws Exception {
		File f = new File(sitesToKeep);
		List<String> lines = FileUtils.readLines(f);
		return lines;

	}

	public String getSitesToKeep() {
		return sitesToKeep;
	}

	public void setSitesToKeep(String sitesToKeep) {
		this.sitesToKeep = sitesToKeep;
	}
	
	
}
