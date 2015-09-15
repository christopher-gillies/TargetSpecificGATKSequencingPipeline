package org.kidneyomics.fluidigm;

import static org.junit.Assert.*;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

public class PrimerListSplitterTest {

	Logger logger = Logger.getLogger(PrimerListSplitterTest.class);
	
	@Test
	public void testSplitListByChr() {
		List<String> intervals = new LinkedList<String>();
		intervals.add("22:1-2");
		intervals.add("22:4-5");
		intervals.add("1:4-5");
		intervals.add("1:1-2");
		intervals.add("1:3-4");
		intervals.add("3:1");
		
		
		Map<String,List<String>> results = PrimerListSplitter.splitListByChr(intervals);
		
		assertTrue(results.containsKey("22"));
		assertTrue(results.containsKey("1"));
		assertTrue(results.containsKey("3"));
		
		assertTrue(results.size() == 3);
		
		
		
		assertTrue(results.get("22").size() == 2);
		
		
		assertTrue(results.get("1").size() == 3);
		
		assertTrue(results.get("3").size() == 1);
		
		
		assertTrue(results.get("1").get(0).equals("1:4-5"));
	}
	
	
	
	@Test
	public void testWriteMapToFiles() throws Exception {
		List<String> intervals = new LinkedList<String>();
		intervals.add("22:1-2");
		intervals.add("22:4-5");
		intervals.add("1:4-5");
		intervals.add("1:1-2");
		intervals.add("1:3-4");
		intervals.add("3:1");
		
		
		Map<String,List<String>> resultsMap = PrimerListSplitter.splitListByChr(intervals);
		
		List<IntervalFile> files = PrimerListSplitter.writeMapToFiles(resultsMap, "/tmp/", "test.intervals");
		
		for(IntervalFile intervalFile : files) {
			File f = intervalFile.getFile();
			String name = FilenameUtils.getBaseName(f.getAbsolutePath());
			logger.info(f.getAbsolutePath());
			logger.info(name);
			List<String> lines = FileUtils.readLines(f);
			if(intervalFile.getEntry().equals("22")) {
				assertTrue(lines.size() == 2);
			} else if(intervalFile.getEntry().equals("3")) {
				assertTrue(lines.size() == 1);
			} else if(intervalFile.getEntry().equals("1")) {
				assertTrue(lines.size() == 3);
			}
			
			FileUtils.deleteQuietly(f);
		}
		
	}

}
