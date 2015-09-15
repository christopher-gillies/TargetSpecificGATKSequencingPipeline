package org.kidneyomics.fluidigm;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class PrimerListSplitter {

	
	
	/**
	 * 
	 * @param intervals
	 * @return map separating the intervals by chromosome
	 */
	public static Map<String,List<String>> splitListByChr(List<String> intervals) {
		Map<String,List<String>> chrIntervals = new HashMap<String,List<String>>();
		
		for( String interval : intervals) {
			
			String split[] = interval.split(":");
			if(split.length == 0) {
				continue;
			}
			String chr = split[0];
			if(chrIntervals.containsKey(chr)) {
				chrIntervals.get(chr).add(interval);
			} else {
				chrIntervals.put(chr, new LinkedList<String>());
				chrIntervals.get(chr).add(interval);
			}
			
		}
		
		
		return chrIntervals;
	}
	
	
	public static List<IntervalFile> writeMapToFiles(Map<String,List<String>> intervalsMap, String dir, String suffix) throws Exception {
		
		List<IntervalFile> filesList = new LinkedList<IntervalFile>();
		
		for(Map.Entry<String, List<String>> entry : intervalsMap.entrySet()) {
			String key = entry.getKey();
			List<String> lines = entry.getValue();
			
			File f = new File(dir + "/" + key + "." + suffix);
			
			FileUtils.writeLines(f, lines);
			
			if(!f.exists()) {
				throw new Exception(f.getAbsolutePath() + " failed to write");
			} else {
				IntervalFile intervalFile = new IntervalFile();
				intervalFile.setFile(f);
				intervalFile.setEntry(key);
				filesList.add(intervalFile);
			}
			
		}
		
		return filesList;
		
	}
}
