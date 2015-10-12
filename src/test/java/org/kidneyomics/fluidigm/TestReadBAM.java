package org.kidneyomics.fluidigm;

import static org.junit.Assert.*;

import java.io.File;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

import org.apache.log4j.Logger;
import org.biojava.nbio.core.sequence.io.FastaReader;
import org.junit.Test;

public class TestReadBAM {

	Logger logger = Logger.getLogger(TestReadBAM.class);
	//@Test
	public void test() {
		File file = new File("/Users/cgillies/Documents/workspace-sts-3.6.1.RELEASE/FluidigmReferences/bams/458204.realinged.recalibrated.bam");
		SAMFileReader reader = new SAMFileReader(file);
		SAMRecordIterator iter = reader.iterator();
		int index = 1;
		while(iter.hasNext()) {
			SAMRecord record = iter.next();
			if(++index % 1000 == 0 ) {
				logger.info(record.getReferenceName() + ":" + record.getAlignmentStart() + " - " + record.getAlignmentEnd());
			}
		}
		iter.close();
		reader.close();
	}
	
	
	public void test2() {
		
	}

}
