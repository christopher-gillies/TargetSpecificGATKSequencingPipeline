package org.kidneyomics.fluidigm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class ReferenceFASTAScanner {

	static Logger logger = Logger.getLogger(ReferenceFASTAScanner.class);
	
	public static Map<String,Integer> getSequenceSizes(File fin) throws IOException {
		HashMap<String,Integer> sequenceSizes = new HashMap<String,Integer>();
		
		
		FileInputStream fis = new FileInputStream(fin);
		 
		//Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	 
		String line = null;
		int currentLength = 0;
		String currentSequence = null;
		while ((line = br.readLine()) != null) {
			if(line.startsWith(">")) {
				
				if(currentSequence != null) {
					sequenceSizes.put(currentSequence, currentLength);
					logger.info(currentSequence + " size " + currentLength);
				}
				currentLength = 0;
				String[] fields = line.replace(">", "").split(" ");
				currentSequence = fields[0];
				
			} else {
				currentLength += line.length();
			}
		}
		
		if(currentSequence != null) {
			sequenceSizes.put(currentSequence, currentLength);
			logger.info(currentSequence + " size " + currentLength);
		}
		
	 
		br.close();
		
		return sequenceSizes;
	}
	
}
