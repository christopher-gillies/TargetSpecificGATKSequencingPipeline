package org.kidneyomics.fluidigm;

import org.junit.Assert;

import java.io.File;
import java.net.URL;



import org.apache.log4j.Logger;
import org.junit.Test;

public class TestMakeFile {

	Logger logger = Logger.getLogger(TestMakeFile.class);
	
	@Test
	public void testWriteCommands1() {
		URL templateDirectory = ClassLoader.getSystemResource("st/makefile.stg");
		String fullpath = templateDirectory.getPath();
		File f = new File(fullpath);
		logger.info(fullpath);

		
		MakeFile m = new MakeFile(fullpath);
		
		MakeEntry e1 = new MakeEntry();
		e1.setTarget("HELLO1.OK").setComment("ENTRY1");;
		e1.addDependency(new MakeEntry().setTarget("DEP1")).addDependency(new MakeEntry().setTarget("DEP2"));
		e1.addCommand("COMMAND1");
		e1.addCommand("COMMAND2");
		m.addMakeEntry(e1);
		
		
		MakeEntry e2 = new MakeEntry();
		e2.setTarget("HELLO2.OK");
		e2.addDependency(new MakeEntry().setTarget("DEP3")).addDependency(new MakeEntry().setTarget("DEP4")).addDependency(new MakeEntry().setTarget("DEP5"));
		e2.addCommand("COMMAND3");
		e2.addCommand("COMMAND4");
		e2.addCommand("COMMAND5");
		m.addMakeEntry(e2);
		
		logger.info("\n" + m.writeTemplateToString());
		
		String expected = "all:\tHELLO1.OK\tHELLO2.OK\t\n\ttouch $@.OK\n\n#ENTRY1\nHELLO1.OK:\tDEP1\tDEP2\t\n\tCOMMAND1\n\tCOMMAND2\n\nHELLO2.OK:\tDEP3\tDEP4\tDEP5\t\n\tCOMMAND3\n\tCOMMAND4\n\tCOMMAND5\n\n";
		logger.info("\n" + expected);
		
		//logger.info("\n" + m.writeCommands().substring(0, 70));
		//logger.info("\n" + expected.substring(0, 70));
		//Assert.assertEquals(expected.substring(0, 80), m.writeCommands().substring(0, 80));
		Assert.assertEquals(expected, m.writeTemplateToString());
	}
	
	
}
