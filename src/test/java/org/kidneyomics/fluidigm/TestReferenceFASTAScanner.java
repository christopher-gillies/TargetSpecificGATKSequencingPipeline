package org.kidneyomics.fluidigm;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestReferenceFASTAScanner {

	static Logger logger = Logger.getLogger(TestReferenceFASTAScanner.class);
	static File tmpFile = null; 
	@BeforeClass
	public static void before() throws IOException {
		String data = ">1 dna:chromosome chromosome:GRCh37:1:1:249250621:1\n" +
"NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN\n" +
"NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN\n" +
"NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN\n" + 
"NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN\n" + 
">2 dna:chromosome chromosome:GRCh37:1:1:249250621:1\n" +
"NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN\n" +
"NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN\n" + 
"NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN\n";
		
		String tmp = FileUtils.getTempDirectoryPath();
		tmpFile = new File(tmp + "/test.fasta");
		FileUtils.write(tmpFile, data);
	}
	
	@AfterClass
	public static void after() {
		FileUtils.deleteQuietly(tmpFile);
	}
	
	
	
	
	@Test
	public void test() throws IOException {
		Map<String,Integer> map = ReferenceFASTAScanner.getSequenceSizes(tmpFile);
		
		int len = "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN".length();
		
		
		assertTrue(map.get("1").equals(len * 4));
		logger.info(len * 4);
		assertTrue(map.get("2").equals(len * 3));
		logger.info(len * 3);
	}
}
