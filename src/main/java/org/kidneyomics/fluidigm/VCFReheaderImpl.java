package org.kidneyomics.fluidigm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.sf.samtools.util.StringUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kidneyomics.vcf.VCFFile;
import org.kidneyomics.vcf.VCFFile.VCFLineIterator;
import org.kidneyomics.vcf.VCFLine;
import org.springframework.stereotype.Component;

@Component("vcfReheader")
public class VCFReheaderImpl implements VCFReheader {

	Logger logger = Logger.getLogger(VCFReheaderImpl.class);
	@Override
	public void reheader(String inVcf, String outVcf, String idMap) throws Exception {
		
		HashMap<String,String> map = readMap(idMap);
		
		VCFFile in = new VCFFile();
		VCFLineIterator iter = in.iterator(inVcf);
		
		logger.info(inVcf);
		Writer writer = new BufferedWriter( new FileWriter(new File(outVcf)));
		
		List<String> headerLines = iter.getHeaderLines();
		
		String finalLine = null;
		int lastIndex = -1;
		if(headerLines.size() > 0) {
			lastIndex = headerLines.size() - 1;
			finalLine = headerLines.get(lastIndex);
			
		} else {
			writer.close();
			throw new Exception("Incorrectly formated vcf");
		}
		
		String updatedFinalLine = mapHeaderLine(finalLine, map);
		headerLines.set(lastIndex, updatedFinalLine);
		
		
		
		writer.write(StringUtils.join(headerLines, "\n"));
		writer.write("\n");
		while(iter.hasNext()) {
			VCFLine line = iter.next();
			//logger.info(line.toString());
			writer.write(line.toString());
			writer.write("\n");
		}
		
		iter.close();
		writer.flush();
		writer.close();
		
	}
	
	private String mapHeaderLine(String line, HashMap<String,String> map) {
		String result = null;
		
		/*
		 * Automatically increment duplicate samples
		 */
		HashMap<String,Integer> alreadyFound = new HashMap<String,Integer>();
		
		String cols[] = line.split("\t");
		for(int i = 9; i < cols.length; i++) {
			if(map.containsKey(cols[i])) {
				cols[i] = map.get(cols[i]);
				if(alreadyFound.containsKey(cols[i])) {
					int count = alreadyFound.get(cols[i]);
					alreadyFound.put(cols[i], ++count);
					cols[i] = cols[i] + "_" + count;
				} else {
					alreadyFound.put(cols[i], 1);
				}
			}
		}
		
		result = StringUtil.join("\t", cols);
		
		
		return result;
	}
	
	private HashMap<String,String> readMap(String idMap) throws Exception {
		HashMap<String,String> map = new HashMap<String,String>();
		
		List<String> lines = FileUtils.readLines(new File(idMap));
		for(String line : lines) {
			if(line.startsWith("#")) {
				continue;
			}
			
			String cols[] = line.split("\t");
			if(cols.length < 2) {
				throw new Exception("idMap not formatted correctly");
			}
			
			// insert into map
			map.put(cols[1], cols[0]);
			
		}
		
		return map;
		
	}

}
