package org.kidneyomics.fluidigm;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.stringtemplate.v4.ST;

public class Sample {
	private String sampleId;
	private Collection<FASTQ> fastqFiles;
	private List<BAM> bamFiles;
	
	public Sample(String sampleId) {
		this.sampleId = sampleId;
		this.fastqFiles = new LinkedList<FASTQ>();
		this.bamFiles = new LinkedList<BAM>();
	}
	
	
	public void addFASTQ(FASTQ fastq) {
		this.fastqFiles.add(fastq);
	}
	
	
	public String getSampleId() {
		return sampleId;
	}


	public Collection<FASTQ> getFastqFiles() {
		return fastqFiles;
	}


	public List<BAM> getBamFiles() {
		return bamFiles;
	}


	public String toString() {
		//ST template = new ST("<sample.sampleId>: FASTQs:<\n><sample.fastqFiles:{fastq | <fastq.file1>}>");
		ST template = new ST("SAMPLE: <sample.sampleId>:\nFASTQs:<sample.fastqFiles:{fastq | \t<fastq.file1>\t<fastq.file2>\n}>\nBAMs:<sample.bamFiles:{bam | \t<bam>}> ");
		template.add("sample", this);
		return template.render();
	}
}
