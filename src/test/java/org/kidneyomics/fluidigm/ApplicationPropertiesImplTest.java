package org.kidneyomics.fluidigm;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class ApplicationPropertiesImplTest {

	@Test
	public void testReadFile() throws Exception {
		ApplicationProperties prop = new ApplicationPropertiesImpl();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("java=java");
		sb.append("\n");
		sb.append("gatk=gatk");
		sb.append("\n");
		sb.append("bwa=bwa");
		
		String path = FileUtils.getTempDirectoryPath();
		
		File f = new File(path + "/test.txt");
		
		FileUtils.write(f, sb.toString());
		
		prop.readProperties(f.getAbsolutePath());
		
		assertEquals(prop.getJava(),"java");
		assertEquals(prop.getGatk(),"gatk");
		
		assertEquals(prop.getBwa(),"bwa");
		f.delete();
	}

}
