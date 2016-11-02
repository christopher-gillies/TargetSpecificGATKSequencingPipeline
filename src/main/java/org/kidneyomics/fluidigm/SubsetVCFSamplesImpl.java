package org.kidneyomics.fluidigm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.kidneyomics.vcf.GTFieldParser;
import org.kidneyomics.vcf.GenotypeField;
import org.kidneyomics.vcf.GenotypeFieldParser;
import org.kidneyomics.vcf.VCFLine;
import org.kidneyomics.vcf.VCFFile;
import org.kidneyomics.vcf.VCFFile.VCFLineIterator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("subsetVCFSamplesImpl")
public class SubsetVCFSamplesImpl implements SubsetService {
	
	private String sampleList;
	
	private List<String> samplesToRemove = new LinkedList<String>();
	
	private boolean keepPassOnly = false;
	
	
	
	public boolean isKeepPassOnly() {
		return keepPassOnly;
	}

	public void setKeepPassOnly(boolean keepPassOnly) {
		this.keepPassOnly = keepPassOnly;
	}

	public void subset(String vcf, String out) throws Exception {
		
		VCFFile vfile = new VCFFile();
		VCFLineIterator iter = vfile.iterator(vcf);
		
		List<String> allSampleIds = iter.getSampleIds();
		
		List<String> samplesToKeep = readSampleIds(allSampleIds);
		
		List<String> headerLines = iter.getHeaderLines();
		
		//Update sample ids in header
		headerLines.set(headerLines.size() - 1, VCFStatisticUpdater.makeNewHeader(samplesToKeep));
		
		
		Writer writer = new BufferedWriter( new FileWriter(new File(out)));
		
		
		writer.write(StringUtils.join(headerLines, "\n"));
		writer.write("\n");
		
		GenotypeFieldParser gtfp = new GT_AD_DP_GQFieldParser();
		
		while(iter.hasNext()) {
			VCFLine vline = iter.next();
			
			//3-7-2016 if this file is a gotcloud file create a new parser
			if(vline.getFormat().equals("GT:DP:GQ:PL")) {
				gtfp = new GT_DP_GQ_PLFieldParser();
			} else if(vline.getFormat().equals("GT")) {
				//11-2-2016
				//Add support for GT only (Exome Chip)
				gtfp = new GTFieldParser();
			}
			
			for(String sample : samplesToRemove) {
				//Set all samples to remove to be missing so that we can calculate statistics more accurately
				vline.getGenotypes().put(sample, "./.");
			}
			VCFStatisticUpdater.updateStatisticsAndRecodeMultiallelelicSites(vline, gtfp);
			
			//give option to remove ac 0 sites
			if(vline.getFilter().equals("PASS") || !keepPassOnly) {
				writer.write(vline.toString(samplesToKeep));
				writer.write("\n");
			}
		}
		
		writer.close();
		
		
		iter.close();
		
	}
	
	private List<String> readSampleIds(List<String> allSampleIds) throws Exception {
		HashMap<String,Integer> allSampleIdsMap = new HashMap<String,Integer>();
		samplesToRemove.clear();
		//Read sample ids into map
		for(String sampleId : allSampleIds) {
			allSampleIdsMap.put(sampleId, 0);
		}
		
		//store samples to subset into a list and mark stored samples
		List<String> samplesToKeep = new LinkedList<String>();
		List<String> lines = FileUtils.readLines(new File(sampleList));
		for(String line : lines) {
			String cols[] = line.split("[ \t,]+");
			if(allSampleIdsMap.containsKey(cols[0])) {
				samplesToKeep.add(cols[0]);
				allSampleIdsMap.put(cols[0], 1);
			} else {
				throw new Exception("sample " + cols[0] + " not found");
			}
		}
		
		for(String sampleId : allSampleIds) {
			int count = allSampleIdsMap.get(sampleId);
			if(count == 0) {
				this.samplesToRemove.add(sampleId);
			}
		}
		
		if( (samplesToKeep.size() + samplesToRemove.size()) != allSampleIds.size()) {
			throw new Exception("sizes don't match error!");
		}

		return samplesToKeep;
	}

	public String getSampleList() {
		return sampleList;
	}

	public void setSampleList(String sampleList) {
		this.sampleList = sampleList;
	}
	
	
}
