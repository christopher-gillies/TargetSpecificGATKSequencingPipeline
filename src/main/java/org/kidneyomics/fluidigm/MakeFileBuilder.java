package org.kidneyomics.fluidigm;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kidneyomics.structures.Interval;
import org.kidneyomics.structures.IntervalTreeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.stringtemplate.v4.ST;

public class MakeFileBuilder implements CommandWriter {
	
	//private String makefileTemplateBase;
	
	
	@Autowired
	private ApplicationProperties properties;
	
	@Autowired private ApplicationContext applicationContext;
	
	private String output;
	
	

	Logger logger = Logger.getLogger(MakeFileBuilder.class);
	
	
	@Override
	public String writePreprocessingCommands(Collection<Sample> samples, AlignArgs alignArgs) throws Exception {
		MakeFile makefile = applicationContext.getBean(MakeFile.class);
		
		
		Collection<MakeEntry> alignmentDependencies = new LinkedList<MakeEntry>();
		
		
		/*
		 * 1. Generate the BWA index
		 */
		
		String bwa = properties.getBwa();
		String reference = properties.getReference();
		ST bwaIndex = new ST("<bwa> index -a bwtsw <reference>");
		bwaIndex.add("bwa", bwa);
		bwaIndex.add("reference", reference);
		
		
		MakeEntry bwaIndexEntry = new MakeEntry();
		bwaIndexEntry.addDependency(new MakeEntry().setTarget(reference));
		bwaIndexEntry.setComment("Index the reference sequence using bwa");
		bwaIndexEntry.addCommand(bwaIndex.render());
		//bwaIndexEntry.setTarget(output + "/" + "BWA_INDEX.OK");
		bwaIndexEntry.setTarget(reference + ".sa");
		
		
		// makefile.addMakeEntry(bwaIndexEntry);
		
		MakeEntry bwaIndexComplete = new MakeEntry();
		bwaIndexComplete.setComment("BWA indexing complete");
		bwaIndexComplete.setTarget(output + "/" + "BWA_INDEX.OK");
		bwaIndexComplete.addCommand("touch $@");
		bwaIndexComplete.addDependency(new MakeEntry().setTarget(reference + ".sa"));
		bwaIndexComplete.addDependency(new MakeEntry().setTarget(reference + ".amb"));
		bwaIndexComplete.addDependency(new MakeEntry().setTarget(reference + ".ann"));
		bwaIndexComplete.addDependency(new MakeEntry().setTarget(reference + ".bwt"));
		bwaIndexComplete.addDependency(new MakeEntry().setTarget(reference + ".pac"));
		
		makefile.addMakeEntry(bwaIndexComplete);
		alignmentDependencies.add(bwaIndexComplete);
		
		/*
		 * 2. Generate the fasta file index for samtools
		 */
		
		String samtools = properties.getSamtools();
		ST fastaIndex = new ST("<samtools> faidx <reference>");
		fastaIndex.add("samtools", samtools);
		fastaIndex.add("reference", reference);
		
		MakeEntry fastaIndexEntry = new MakeEntry();
		fastaIndexEntry.setComment("Index the reference sequence using samtools");
		fastaIndexEntry.setTarget(reference + ".fai");
		fastaIndexEntry.addCommand(fastaIndex.render()).addCommand("touch $@");
		fastaIndexEntry.addDependency(new MakeEntry().setTarget(reference));
		
		//makefile.addMakeEntry(fastaIndexEntry);
		
		MakeEntry fastaIndexComplete = new MakeEntry();
		fastaIndexComplete.setComment("samtools indexing complete");
		fastaIndexComplete.setTarget(output + "/" + "SAMTOOLS_INDEX.OK");
		fastaIndexComplete.addCommand("touch $@");
		fastaIndexComplete.addDependency(fastaIndexEntry);
		
		makefile.addMakeEntry(fastaIndexComplete);
		alignmentDependencies.add(fastaIndexComplete);
		
		/*
		 * 3. Generate the fasta file index for picard
		 */
		
		String picard = properties.getPicard();
		String jvmSize = properties.getJvmSize();
		String referenceDict = FilenameUtils.getFullPath(reference) + "/" + FilenameUtils.getBaseName(reference) + ".dict";
		
		//Add output
		ST fastaDict = new ST("java -Xmx<jvmSize> -jar <picard> CreateSequenceDictionary reference=<reference> output=" + referenceDict);
		fastaDict.add("picard", picard);
		fastaDict.add("reference", reference);
		fastaDict.add("jvmSize", jvmSize);
		
		MakeEntry fastaDictEntry = new MakeEntry();
		fastaDictEntry.setComment("Index the reference sequence using picard");
		fastaDictEntry.addDependency(new MakeEntry().setTarget(reference));
		//fastaDictEntry.setTarget(output + "/" + "PICARD_DICT.OK");
		fastaDictEntry.setTarget(referenceDict);
		fastaDictEntry.addCommand(fastaDict.render());
		
		//makefile.addMakeEntry(fastaDictEntry);
		
		
		MakeEntry fastaDictComplete = new MakeEntry();
		fastaDictComplete.setComment("Index the reference sequence using picard completed");
		fastaDictComplete.addDependency(fastaDictEntry);
		fastaDictComplete.setTarget(output + "/" + "PICARD_DICT.OK");
		fastaDictComplete.addCommand("touch $@");
		
		alignmentDependencies.add(fastaDictComplete);
		makefile.addMakeEntry(fastaDictComplete);
		
		
		/*
		 * Write BWA MEM Commands 
		 */
		writeAlignmentCommands(samples,makefile,alignmentDependencies,alignArgs);
		
		return makefile.writeTemplateToString();
	}
	
	private void writeAlignmentCommands(Collection<Sample> samples, MakeFile makefile, Collection<MakeEntry> alignmentDependencies, AlignArgs alignArgs) throws Exception {
		
		StringBuilder bamlist = new StringBuilder();
		Collection<MakeEntry> mergeRealignRecalEntries = new LinkedList<MakeEntry>();
		for(Sample sample : samples) {
			
			
			MakeEntry cleanUpEntry = new MakeEntry();
			cleanUpEntry.setTarget(output + "/" + sample.getSampleId() + ".cleanup.OK");
			cleanUpEntry.setComment("Cleanup for Sample "+ sample.getSampleId());
			Collection<MakeEntry> realignRecaldependencies = new LinkedList<MakeEntry>();
			for(FASTQ fastq : sample.getFastqFiles()) {
				
				/*
				 * 
				 */
				String groupName = FilenameUtils.getBaseName(fastq.getFile1());
				/*
				 * DO NOT LET THE \t get expanded b/c bwa will not work correctly with it!
				 * http://gatkforums.broadinstitute.org/discussion/4502/bwa-mem-and-gatk-error
				 */
				ST readGroup = new ST("@RG\\tID:<group>\\tSM:<sampleId>\\tPL:<platform>\\tLB:<library>\\tPU:<unit>");
				readGroup.add("group", groupName)
				.add("sampleId", sample.getSampleId())
				.add("platform", "illumina")
				.add("library", sample.getSampleId())
				.add("unit",  "flowcell-barcode.lane");
				
				/*
				 * (1) align and convert to bam
				 */
				MakeEntry entry = new MakeEntry();
				String alignedBam = null;
				if(fastq.isPairedEnd()) {
	
					ST bwaCommand = new ST("<bwa> mem -M -R '<readGroup>' <reference> <fastq1> <fastq2> | <samtools> view -bS - > <out>");
					String inFile1Name = FilenameUtils.getBaseName(fastq.getFile1());
					String inFile2Name = FilenameUtils.getBaseName(fastq.getFile2());
					alignedBam = output + "/" + inFile1Name + "." + inFile2Name + ".bam"; 
					bwaCommand.add("bwa", properties.getBwa());
					bwaCommand.add("samtools", properties.getSamtools());
					bwaCommand.add("readGroup", readGroup.render());
					bwaCommand.add("reference", properties.getReference());
					bwaCommand.add("fastq1", fastq.getFile1());
					bwaCommand.add("fastq2", fastq.getFile2());
					bwaCommand.add("out", alignedBam);
					
					entry.addDependencies(alignmentDependencies);
					entry.addDependency(new MakeEntry().setTarget(fastq.getFile1()))
					.addDependency(new MakeEntry().setTarget(fastq.getFile2()));
					entry.setComment("Align/Index/Sort Fastq files " + fastq.getFile1() + "\t" + fastq.getFile2());
					
					entry.addCommand(bwaCommand.render());

				} else {
					
					ST bwaCommand = new ST("<bwa> mem -M -R '<readGroup>' <reference> <fastq1> | <samtools> view -bS - > <out>");
					String inFile1Name = FilenameUtils.getBaseName(fastq.getFile1());
					alignedBam = output + "/" + inFile1Name + ".bam"; 
					bwaCommand.add("bwa", properties.getBwa());
					bwaCommand.add("samtools", properties.getSamtools());
					bwaCommand.add("readGroup", readGroup.render());
					bwaCommand.add("reference", properties.getReference());
					bwaCommand.add("fastq1", fastq.getFile1());
					bwaCommand.add("out", alignedBam);
					
					entry.addDependencies(alignmentDependencies);
					entry.addDependency(new MakeEntry().setTarget(fastq.getFile1()))
					.addDependency(new MakeEntry().setTarget(fastq.getFile2()));
					entry.setComment("Align/Index/Sort Fastq files " + fastq.getFile1());
					
					entry.addCommand(bwaCommand.render());
					
				}
				
				/*
				 * Sort Bam 
				 */
				
				String bamName = FilenameUtils.getBaseName(alignedBam);
				String sortedBam = output + "/" + bamName + ".sorted.bam";
				String bamIndex = output + "/" + bamName + ".sorted.bai";
				
				ST sortCommand = new ST("java -Xmx<jvmSize>  -jar <picard> SortSam INPUT=<input> OUTPUT=<output> SORT_ORDER=coordinate");
				sortCommand.add("picard", properties.getPicard());
				sortCommand.add("input", alignedBam);
				sortCommand.add("output", sortedBam);
				sortCommand.add("jvmSize", properties.getJvmSize());
				entry.addCommand(sortCommand.render());
				
				
				/*
				 * Index Bam
				 */
				
				ST indexCommand = new ST("java -Xmx<jvmSize>  -jar <picard> BuildBamIndex INPUT=<input>");
				indexCommand.add("picard", properties.getPicard());
				indexCommand.add("input", sortedBam);
				indexCommand.add("jvmSize", properties.getJvmSize());
				entry.addCommand(indexCommand.render());
				
				/*
				 * Remove unsorted bam
				 */
				//entry.addCommand("rm " + alignedBam);
				cleanUpEntry.addCommand("rm " + alignedBam);
				/*
				 * Mark this step complete
				 */
				entry.addCommand("touch $@");
				
				/*
				 * Set target
				 */
				entry.setTarget(bamIndex + ".OK");
				
				/*
				 * Add to make file
				 */
				makefile.addMakeEntry(entry);
				
				/*
				 * Save dependency 
				 */
				realignRecaldependencies.add(entry);
				
				/*
				 * add to bam list
				 */
				sample.getBamFiles().add(new BAM().setBamFile(sortedBam).setBamIndex(bamIndex));
			}
			
			/*
			 * Merge BAMS
			 */
			MakeEntry mergeRealignRecalEntry = new MakeEntry();
			
			//Store command as dependency for qc
			mergeRealignRecalEntries.add(mergeRealignRecalEntry);
			
			mergeRealignRecalEntry.setComment("Merge/Realign/Recal for sample " + sample.getSampleId());
			
			/*
			 * add dependencies
			 */
			mergeRealignRecalEntry.addDependencies(realignRecaldependencies);
			
			BAM mergedBam = new BAM();
			mergedBam.setBamFile(output + "/" + sample.getSampleId() + ".bam");
			mergedBam.setBamIndex(output + "/" + sample.getSampleId() + ".bai");
			
			
			if(sample.getBamFiles().size() == 0) {
				throw new Exception("Error no bams for this sample! " + sample.getSampleId());
			} else if(sample.getBamFiles().size() == 1) {
				BAM bam = sample.getBamFiles().get(0);
				/*
				 * bam file
				 */
				ST mergeCommand = new ST("cp <from> <to>");
				
				
				mergeCommand.add("from", bam.getBamFile());
				mergeCommand.add("to", mergedBam.getBamFile());
				mergeRealignRecalEntry.addCommand(mergeCommand.render());
				
				/*
				 * index file
				 */
				

				mergeCommand.remove("from");
				mergeCommand.remove("to");
				mergeCommand.add("from", bam.getBamIndex());
				mergeCommand.add("to", mergedBam.getBamIndex());
				mergeRealignRecalEntry.addCommand(mergeCommand.render());
				
				
				cleanUpEntry.addCommand("rm " + bam.getBamFile());
				cleanUpEntry.addCommand("rm " + bam.getBamIndex());

			} else {
				
				/*
				 * Merge Files
				 */
				ST mergeCommand = new ST("java -Xmx<jvmSize> -jar <picard> MergeSamFiles <bamFiles:{bam | INPUT=<bam.bamFile> }> OUTPUT=<output> SORT_ORDER=coordinate ASSUME_SORTED=true");
				mergeCommand.add("picard", properties.getPicard());
				mergeCommand.add("bamFiles", sample.getBamFiles());
				mergeCommand.add("output", mergedBam.getBamFile());
				mergeCommand.add("jvmSize", properties.getJvmSize());
				mergeRealignRecalEntry.addCommand(mergeCommand.render());
				
				/*
				 * Index new Bam
				 */
				ST indexCommand = new ST("java -Xmx<jvmSize>  -jar <picard> BuildBamIndex INPUT=<input>");
				indexCommand.add("picard", properties.getPicard());
				indexCommand.add("input", mergedBam.getBamFile());
				indexCommand.add("jvmSize", properties.getJvmSize());
				mergeRealignRecalEntry.addCommand(indexCommand.render());
				
				/*
				 * clean up by removing unneeded bams
				 */
				for(BAM bam : sample.getBamFiles()) {
					//mergeRealignRecalEntry.addCommand("rm " + bam.getBamFile());
					//mergeRealignRecalEntry.addCommand("rm " + bam.getBamIndex());
					
					cleanUpEntry.addCommand("rm " + bam.getBamFile());
					cleanUpEntry.addCommand("rm " + bam.getBamIndex());
				}
			}
			
			/*
			 * NOTE: At this point the bam that is available is the mergedBam
			 */
			
			/*
			 * Indel Realignment
			 */
			
			String sampleIndelIntervals = output + "/" + sample.getSampleId() + ".interval_list";
			ST createTargetListCommand = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T RealignerTargetCreator -nt <numThreads> -R <reference> -L <intervals> -I <bam> --known <gold> --known <1000GIndels> -o <outIntervals>");
			createTargetListCommand.add("gatk", properties.getGatk())
			.add("java", properties.getJava())
			.add("jvmSize", properties.getJvmSize())
			.add("numThreads", alignArgs.getNumGatkThreads())
			.add("reference", properties.getReference())
			.add("bam", mergedBam.getBamFile())
			.add("gold", properties.getGoldIndels())
			.add("1000GIndels", properties.get_1000GIndels())
			.add("intervals", alignArgs.getPrimerList())
			.add("outIntervals", sampleIndelIntervals);
	
			mergeRealignRecalEntry.addCommand(createTargetListCommand.render());
			
			String realignedBam = output + "/" + sample.getSampleId() + ".realinged.bam";
			String realignedBamIndex = output + "/" + sample.getSampleId() + ".realinged.bai";
			ST realignmentCommand = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T IndelRealigner -R <reference> -I <bam> -known <gold> -known <1000GIndels> -L <intervals> -targetIntervals <inIntervals> -o <outbam>");
			realignmentCommand.add("gatk", properties.getGatk())
			.add("java", properties.getJava())
			.add("jvmSize", properties.getJvmSize())
			.add("reference", properties.getReference())
			.add("bam", mergedBam.getBamFile())
			.add("gold", properties.getGoldIndels())
			.add("1000GIndels", properties.get_1000GIndels())
			.add("inIntervals", sampleIndelIntervals)
			.add("intervals", alignArgs.getPrimerList())
			.add("outbam", realignedBam);
			
			mergeRealignRecalEntry.addCommand(realignmentCommand.render());
			
			/*
			 * Base Recalibration
			 */
			
			String recalibratedBam = output + "/" + sample.getSampleId() + ".realinged.recalibrated.bam";
			String recalibratedBamIndex = output + "/" + sample.getSampleId() + ".realinged.recalibrated.bai";
			String recalTablePass1 = output + "/" + sample.getSampleId() + ".grp";
			ST recalPass1Command = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T BaseRecalibrator -nct <numThreads> -R <reference> -I <bam> --knownSites <dbsnp> --knownSites <gold> --knownSites <1000GIndels> -L <intervals> -o <bqsr>");
			recalPass1Command.add("gatk", properties.getGatk())
			.add("java", properties.getJava())
			.add("jvmSize", properties.getJvmSize())
			.add("numThreads", alignArgs.getNumGatkThreads())
			.add("reference", properties.getReference())
			.add("bam", realignedBam)
			.add("dbsnp", properties.getDbsnp())
			.add("gold", properties.getGoldIndels())
			.add("1000GIndels", properties.get_1000GIndels())
			.add("intervals", alignArgs.getPrimerList())
			.add("bqsr", recalTablePass1);
			
			mergeRealignRecalEntry.addCommand(recalPass1Command.render());
			
			
			String recalTablePass2= output + "/" + sample.getSampleId() + ".post.grp";
			ST recalPass2Command = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T BaseRecalibrator -nct <numThreads> -R <reference> -I <bam> --knownSites <dbsnp> --knownSites <gold> --knownSites <1000GIndels> --BQSR <bqsr_pre> -L <intervals> -o <bqsr_post>");
			recalPass2Command.add("gatk", properties.getGatk())
			.add("java", properties.getJava())
			.add("jvmSize", properties.getJvmSize())
			.add("numThreads", alignArgs.getNumGatkThreads())
			.add("reference", properties.getReference())
			.add("bam", realignedBam)
			.add("dbsnp", properties.getDbsnp())
			.add("gold", properties.getGoldIndels())
			.add("1000GIndels", properties.get_1000GIndels())
			.add("bqsr_pre", recalTablePass1)
			.add("intervals", alignArgs.getPrimerList())
			.add("bqsr_post", recalTablePass2);
			
			mergeRealignRecalEntry.addCommand(recalPass2Command.render());
			
			/*
			ST analyzeCovariates = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T AnalyzeCovariates -R <reference> -before <before> -after <after> -plots <plots>");
			String recalPlots = output + "/" + sample.getSampleId() + ".recal.plots.pdf";
			analyzeCovariates.add("gatk", properties.getGatk())
			.add("java", properties.getJava())
			.add("jvmSize", properties.getJvmSize())
			.add("reference", properties.getReference())
			.add("before", recalTablePass1)
			.add("after", recalTablePass2)
			.add("plots",recalPlots);
			
			mergeRealignRecalEntry.addCommand(analyzeCovariates.render());
			*/
			
			ST writeRecalBam = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T PrintReads -R <reference> -I <bam> --BQSR <bqsr_post> -o <bamout>");
			writeRecalBam.add("gatk", properties.getGatk())
			.add("java", properties.getJava())
			.add("jvmSize", properties.getJvmSize())
			.add("reference", properties.getReference())
			.add("bam", realignedBam)
			.add("bamout", recalibratedBam)
			.add("bqsr_post", recalTablePass2);
			
			mergeRealignRecalEntry.addCommand(writeRecalBam.render());
			
			/*
			 * Index Recalibrated BAM (Already performed by PrintReads!)
			 */
			
			/*
			ST indexCommand = new ST("java -Xmx<jvmSize>  -jar <picard> BuildBamIndex INPUT=<input>");
			indexCommand.add("picard", properties.getPicard());
			indexCommand.add("input", recalibratedBam);
			indexCommand.add("jvmSize", properties.getJvmSize());
			
			mergeRealignRecalEntry.addCommand(indexCommand.render());
			
			
			*/
			
			/*
			 * Statistics (DepthOfCoverage)
			 */
			
			/*
			ST depthCommand = new ST("<java> -Xmx<jvmSize>  -jar <gatk> -T DepthOfCoverage -R <reference> -I <bam> -o <basename>");
			depthCommand.add("gatk", properties.getGatk());
			depthCommand.add("java", properties.getJava());
			depthCommand.add("bam", recalibratedBam);
			depthCommand.add("reference", properties.getReference());
			depthCommand.add("basename", output + "/" + sample.getSampleId() + ".stats");
			depthCommand.add("jvmSize", properties.getJvmSize());
			
			mergeRealignRecalEntry.addCommand(depthCommand.render());
			*/
			
			// Statistics (CollectMultipleMetrics)
			ST metricsCommand = new ST("java -Xmx<jvmSize>  -jar <picard> CollectMultipleMetrics INPUT=<input> ASSUME_SORTED=true OUTPUT=<basename>");
			metricsCommand.add("picard", properties.getPicard());
			metricsCommand.add("input", recalibratedBam);
			metricsCommand.add("basename", output + "/" + sample.getSampleId() + ".stats");
			metricsCommand.add("jvmSize", properties.getJvmSize());
			
			mergeRealignRecalEntry.addCommand(metricsCommand.render());
			
			/*
			 * qplot
			 */
		
			ST qplotCommand = new ST("<qplot> --reference <reference> --dbsnp <dbsnp> --plot <plot> --stats <stats> --Rcode <rcode> <bam>");
			qplotCommand.add("qplot", properties.getQplot());
			qplotCommand.add("reference", properties.getReference());
			qplotCommand.add("dbsnp", properties.getDbsnp() + ".tbl");
			qplotCommand.add("plot", output + "/" + sample.getSampleId() + ".qplot.pdf");
			String statsFile = output + "/" +  sample.getSampleId() + ".qplot.stats";
			qplotCommand.add("stats",statsFile);
			qplotCommand.add("rcode", output + "/" + sample.getSampleId() + ".qplot.R");
			qplotCommand.add("bam", recalibratedBam);
			mergeRealignRecalEntry.addCommand(qplotCommand.render());
			
			
			
			
			
			/*
			 * Remove intermediate bams
			 */
			
			//mergeRealignRecalEntry.addCommand("rm " + mergedBam.getBamFile());
			//mergeRealignRecalEntry.addCommand("rm " + mergedBam.getBamIndex());
			
			//mergeRealignRecalEntry.addCommand("rm " + realignedBam);
			//mergeRealignRecalEntry.addCommand("rm " + realignedBamIndex);
			
			

			cleanUpEntry.addCommand("rm " + mergedBam.getBamFile());
			cleanUpEntry.addCommand("rm " + mergedBam.getBamIndex());
			
			cleanUpEntry.addCommand("rm " + realignedBam);
			cleanUpEntry.addCommand("rm " + realignedBamIndex);
			
			/*
			 * Set sample align complete
			 */
			
			mergeRealignRecalEntry.setTarget(recalibratedBam + ".OK");
			mergeRealignRecalEntry.addCommand("touch $@");
			

			
			/*
			 * Add to makefile
			 */
			
			makefile.addMakeEntry(mergeRealignRecalEntry);
			
			/*
			 * add final command to cleanUpEntry and add dependency for mergeRealignRecalEntry
			 */
			cleanUpEntry.addCommand("touch $@");
			cleanUpEntry.addDependency(mergeRealignRecalEntry);
			
			makefile.addMakeEntry(cleanUpEntry);
			
			
			/*
			 * Add to bamlist
			 */
			bamlist.append(sample.getSampleId());
			bamlist.append("\t");
			bamlist.append(recalibratedBam);
			bamlist.append("\t");
			bamlist.append(recalibratedBamIndex);
			bamlist.append("\t");
			bamlist.append(statsFile);
			bamlist.append("\n");
		}
		
		FileUtils.write(new File(output + "/bam.list.txt" ), bamlist.toString());
		
		BamStatMergeWriter mergeWriter = applicationContext.getBean(BamStatMergeWriter.class);
		mergeWriter.setBamList(output + "/bam.list.txt");
		mergeWriter.setOutFile(output + "/merged.stats.txt");
		FileUtils.write(new File(output + "/mergeBams.pl"), mergeWriter.writeTemplateToString());
		
		
		BamStatQCWriter qcWriter = applicationContext.getBean(BamStatQCWriter.class);
		qcWriter.setOutDir(output);
		qcWriter.setStatsFile(output + "/merged.stats.txt");
		FileUtils.write(new File(output + "/qcScript.R"), qcWriter.writeTemplateToString());
		
		
		MakeEntry qcEntry = new MakeEntry();
		
		qcEntry.setComment("Merge stat files and generte qc plots");
		qcEntry.addDependencies(mergeRealignRecalEntries);
		qcEntry.setTarget(output + "/QC.OK");
		qcEntry.addCommand("perl -w " + output + "/mergeBams.pl");
		qcEntry.addCommand("Rscript " + output + "/qcScript.R");
		qcEntry.addCommand("touch $@");
				
		makefile.addMakeEntry(qcEntry);
	}
/*
	public String getMakefileTemplateBase() {
		return makefileTemplateBase;
	}
	
	public String getMakefileTemplatePath() {
		URL template = ClassLoader.getSystemResource(makefileTemplateBase);
		return template.getPath();
	}
	
	public void setMakefileTemplateBase(String makefileTemplateBase) {
		this.makefileTemplateBase = makefileTemplateBase;
	}
*/
	public String getOutput() {
		return output;
	}

	@Override
	public void setOutput(String output) {
		this.output = output;
	}

	@Override
	public String writeTrimCommands(Collection<Sample> samples, TrimArgs args) throws Exception {
		
		MakeFile makefile = applicationContext.getBean(MakeFile.class);
		
		StringBuilder fastqFileList = new StringBuilder();
		
		
		
		String targetSpecificAdapter = StringUtils.repeat("N", args.getTspl());
		String adapter1 = targetSpecificAdapter + args.getAdapter1();
		String adapter2 = targetSpecificAdapter + args.getAdapter2();
		
		logger.info("Number of samples: " + samples.size());
		for(Sample sample : samples) {
			Collection<FASTQ> fastqs = sample.getFastqFiles();
			logger.info("Number of fastqs for sample: " + sample.getSampleId() + "\t" + fastqs.size());
			for(FASTQ fastq : fastqs) {
				if(fastq.isPairedEnd()) {
					
					MakeEntry entry = new MakeEntry();
					entry
					.addDependency(new MakeEntry().setTarget(fastq.getFile1()))
					.addDependency(new MakeEntry().setTarget(fastq.getFile2()));
					entry.setComment("Trim files " + fastq.getFile1() + "\t" + fastq.getFile2());
					/*
					 * Trim beginning
					 */
					
					//$cutadapt -e 0 -g ^$trimFront -o $out1F $file1
					
					String inFile1Name = FilenameUtils.getBaseName(fastq.getFile1());
					String out1_5p = output + "/" + inFile1Name + ".5p.trimmed.fastq.gz"; 

					String inFile2Name = FilenameUtils.getBaseName(fastq.getFile2());
					String out2_5p = output + "/" + inFile2Name + ".5p.trimmed.fastq.gz"; 
					
					entry.setTarget(output + "/" + sample.getSampleId() + "." + inFile1Name + "." + inFile2Name + ".TRIMMED.OK");
					
					ST trim5p = new ST("<cutadapt> --minimum-length 20 -e 0 -g ^<targetSpecificAdapter> -o <out> <infile>");
					trim5p.add("cutadapt", properties.getCutadapt())
					.add("targetSpecificAdapter", targetSpecificAdapter);
					
					//File 1
					trim5p.add("out", out1_5p)
					.add("infile", fastq.getFile1());
					
					//add 5p command
					entry.addCommand(trim5p.render());
					
					//File 2
					trim5p.remove("out");
					trim5p.remove("infile");
					trim5p.add("out", out2_5p)
					.add("infile", fastq.getFile2());
					
					//add 5p command
					entry.addCommand(trim5p.render());
					
					/*
					 * Trim end paired-end
					 */
					//"$cutadapt -e 0 --overlap 27 -a $adapterF1 -A $adapterF2 -o $out1 -p $out2 $file1 $file2";
					ST trim3p = new ST("<cutadapt> --minimum-length 20 -e <err> --overlap <overlap> -a <adapter1> -A <adapter2> -o <out1> -p <out2> <fastq1> <fastq2> > <sampleId>.<file1>.<file2>.stats.txt");
					trim3p.add("cutadapt", properties.getCutadapt());
					trim3p.add("err", args.getErr());
					
					/*
					 * We are trimming the NNNNNNNNNADAPTER from every read so that we can remove the target specific adapter from the sequence.
					 */
					trim3p.add("overlap", args.getTspl() + args.getMinAdapterOverlap());
					trim3p.add("adapter1", adapter1);
					trim3p.add("adapter2", adapter2);
					trim3p.add("sampleId", output + "/" + sample.getSampleId());
					trim3p.add("file1", inFile1Name);
					trim3p.add("file2", inFile2Name);
					
					String out1_3p =  output + "/" + inFile1Name + ".5p.3p.trimmed.fastq.gz";
					trim3p.add("out1", out1_3p);
					trim3p.add("fastq1", fastq.getFile1());
					String out2_3p =  output + "/" + inFile2Name + ".5p.3p.trimmed.fastq.gz";
					trim3p.add("out2", out2_3p);
					trim3p.add("fastq2", fastq.getFile2());
					
					//add 3p command
					entry.addCommand(trim3p.render());
					
					
					//clean up
					entry.addCommand("rm " + out1_5p);
					entry.addCommand("rm " + out2_5p);
					
					//Creation complete command
					entry.addCommand("touch $@");
					
					//add to makefile
					makefile.addMakeEntry(entry);
					
					//Add to file
					fastqFileList.append(sample.getSampleId());
					fastqFileList.append("\t");
					fastqFileList.append(out1_3p);
					fastqFileList.append("\t");
					fastqFileList.append(out2_3p);
					fastqFileList.append("\n");
				} else {
					MakeEntry entry = new MakeEntry();
					entry.setComment("Trim file " + fastq.getFile1());
					entry
					.addDependency(new MakeEntry().setTarget(fastq.getFile1()));
					
					String inFile1Name = FilenameUtils.getBaseName(fastq.getFile1());
					String out1_5p = output + "/" + inFile1Name + ".5p.trimmed.fastq.gz";
					
					entry.setTarget(output + "/" + sample.getSampleId() + "." + inFile1Name  + ".TRIMMED.OK");
					
					ST trim5p = new ST("<cutadapt> --minimum-length 20 -e 0 -g ^<targetSpecificAdapter> -o <out> <infile>");
					trim5p.add("cutadapt", properties.getCutadapt())
					.add("targetSpecificAdapter", targetSpecificAdapter);
					
					//File 1
					trim5p.add("out", out1_5p)
					.add("infile", fastq.getFile1());
					//add 5p command
					entry.addCommand(trim5p.render());
					
					if(!StringUtils.isEmpty(adapter1) && !StringUtils.isEmpty(adapter2)) {
						ST trim3p = new ST("<cutadapt> --minimum-length 20 -e <err> --overlap <overlap> -a <adapter1> -a <adapter2> -o <out> <infile> > <sampleId>.<file1>.stats.txt");
						trim3p.add("cutadapt", properties.getCutadapt())
						.add("file1", inFile1Name)
						.add("adapter1", adapter1)
						.add("adapter2", adapter2)
						.add("sampleId", output + "/" + sample.getSampleId())
						.add("err", args.getErr())
						.add("infile", out1_5p);
						trim3p.add("overlap", args.getTspl() + args.getMinAdapterOverlap());
						String out1_3p = output + "/" + inFile1Name + ".5p.3p.trimmed.fastq.gz";
						trim3p.add("out",out1_3p);
						entry.addCommand(trim3p.render());
						
						//Add to file
						fastqFileList.append(sample.getSampleId());
						fastqFileList.append("\t");
						fastqFileList.append(out1_3p);
						fastqFileList.append("\n");
						
					} else {
						ST trim3p = new ST("<cutadapt> --minimum-length 20 -e <err> --overlap <overlap> -a <adapter1> -o <out> <infile> > <sampleId>.<file1>.stats.txt");
						trim3p.add("cutadapt", properties.getCutadapt())
						.add("file1", inFile1Name)
						.add("adapter1", adapter1)
						.add("sampleId", output + "/" + sample.getSampleId())
						.add("err", args.getErr())
						.add("infile", out1_5p);
						trim3p.add("overlap", args.getTspl() + args.getMinAdapterOverlap());
						String out1_3p = output + "/" + inFile1Name + ".5p.3p.trimmed.fastq.gz";
						trim3p.add("out",out1_3p);
						entry.addCommand(trim3p.render());
						
						//Add to file
						fastqFileList.append(sample.getSampleId());
						fastqFileList.append("\t");
						fastqFileList.append(out1_3p);
						fastqFileList.append("\n");
					}
					
					//clean up
					entry.addCommand("rm " + out1_5p);
					
					//Creation complete command
					entry.addCommand("touch $@");
					
					//add to makefile
					makefile.addMakeEntry(entry);
					
				}
			}
		}
		
		FileUtils.write(new File(output + "/fastq.list.txt"), fastqFileList.toString());
		return makefile.writeTemplateToString();
	}

	@Override
	public String writeVariantCallCommandsUnifiedGenotyper(Collection<Sample> samples,
			CallArgs args) throws Exception {
		MakeFile makefile = applicationContext.getBean(MakeFile.class);
		
		String[] keys = {
				"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","X","Y","M"
		};
		

		List<String> bams = new LinkedList<String>();
		for(Sample s : samples) {
			bams.add(s.getBamFiles().get(0).getBamFile());
		}
		
		List<String> intervals = FileUtils.readLines(new File(args.getPrimerList()));
		
		//Map<String,List<String>> intervalsMap = PrimerListSplitter.splitListByChr(intervals);
		
		//List<IntervalFile> intervalFiles = PrimerListSplitter.writeMapToFiles(intervalsMap, this.output, "intervals");
		
		//Collections.sort(intervalFiles);
		
		
		IntervalTreeSet treeSet = new IntervalTreeSet(intervals);
		
		Map<String,List<Interval>> intervalMap = treeSet.getIntervalsLowestToHighest();
		
		List<String> vcfs = new LinkedList<String>();
		
		for(String key : keys) {
			
			List<Interval> intervalsForKey = intervalMap.get(key);
			if(intervalsForKey == null) {
				continue;
			} else {
				for(Interval interval : intervalsForKey) {
					ST unifiedGenotyperCommand = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T UnifiedGenotyper -R <reference> <bams:{bam | -I <bam> }> --dbsnp <dbsnp> -L <intervals> -o <out> -dt NONE -glm BOTH -stand_call_conf 30 -stand_emit_conf 10 -nct <nct> -nt 1");
					String outVcf = "chr"+ key + "_" + interval.getStart() + "_" + interval.getEnd() + ".vcf";
					vcfs.add(this.output + "/" + outVcf);
					unifiedGenotyperCommand.add("java", properties.getJava())
					.add("jvmSize", properties.getJvmSize())
					.add("gatk", properties.getGatk())
					.add("reference", properties.getReference())
					.add("bams", bams)
					.add("out",this.output + "/" + outVcf)
					.add("intervals", key + ":" + interval.getStart() + "-" + interval.getEnd())
					.add("nct", args.getNumGatkThreads())
					.add("dbsnp", properties.getDbsnp());
					
					MakeEntry entry = new MakeEntry();
					entry.setComment("Genotype using UnifiedGenotyper");
					entry.setTarget(this.output + "/" + outVcf + ".OK");
					entry.addCommand(unifiedGenotyperCommand.render());
					entry.addCommand("touch $@");
					makefile.addMakeEntry(entry);
				}
			}
		}
		
		/*
		 * Merge script
		 */
		
		//create vcf files to merge
		String vcfFileList = this.output + "/" + "vcf.list.txt";
		FileUtils.writeLines(new File(vcfFileList), vcfs);
		
		//write perl script to merge files
		VCFMergeWriter vcfMergeWriter = (VCFMergeWriter) applicationContext.getBean("vcfMergeWriter");
		vcfMergeWriter.setFileList(vcfFileList);
		vcfMergeWriter.setOutDir(this.output);
		vcfMergeWriter.setOutPrefix("merged");
		String mergeScriptFile = this.output + "/mergeVcf.pl"; 
		FileUtils.write(new File(mergeScriptFile), vcfMergeWriter.toString());
		
		//write merge command
		MakeEntry entry = new MakeEntry();
		entry.setComment("Genotype using UnifiedGenotyper");
		entry.setTarget(this.output + "/merged.vcf.OK");
		entry.addCommand("perl -w " + mergeScriptFile);
		entry.addCommand("touch $@");
		entry.addDependencies(makefile.getMakeEntries());
		makefile.addMakeEntry(entry);
		
		return makefile.toString();
	}
	
	@Override
	public String writeVariantCallCommands(Collection<Sample> samples,
			CallArgs args) throws Exception {
		
		MakeFile makefile = applicationContext.getBean(MakeFile.class);
		
		
		
		/*
		 * (1) Run Haplotype Caller for each sample
		 */
		
		Collection<MakeEntry> mergeDependencies = new LinkedList<MakeEntry>();
		for(Sample sample : samples) {
			
			MakeEntry entry = new MakeEntry();
			entry.addDependency(new MakeEntry().setTarget(sample.getBamFiles().get(0).getBamFile()));
			entry.addDependency(new MakeEntry().setTarget(sample.getBamFiles().get(0).getBamIndex()));
			entry.setTarget(output + "/" + sample.getSampleId() + ".OK");
			entry.setComment("Call sample " + sample.getSampleId() + ".OK");
			String bam = sample.getBamFiles().get(0).getBamFile();
			
			String outfile =  output + "/" + sample.getSampleId() + ".snps.indels.g.vcf";
			ST haplotypeCallerCommand = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T HaplotypeCaller -R <reference> -I <bam> --dbsnp <dbsnp> --emitRefConfidence GVCF --variant_index_type LINEAR --variant_index_parameter 128000 -L <intervals> -o <out> --dontUseSoftClippedBases --kmerSize 12 -dt NONE");
			haplotypeCallerCommand.add("gatk", properties.getGatk())
			.add("java", properties.getJava())
			.add("jvmSize", properties.getJvmSize())
			.add("reference", properties.getReference())
			.add("bam", bam)
			.add("out",outfile)
			.add("intervals", args.getPrimerList())
			.add("dbsnp", properties.getDbsnp());
			
			entry.addCommand(haplotypeCallerCommand.render());
			
			entry.addCommand("touch $@");
			makefile.addMakeEntry(entry);
			
			mergeDependencies.add(entry);
		}
		
		/*
		 * (2) Merge sample batches
		 */
		
		Iterator<Sample> iter = samples.iterator();
		int cohortSize = args.getCohortSize();
		
		int currentBatch = 0;
		
		Collection<String> mergedVcfs = new LinkedList<String>();
		Collection<MakeEntry> jointCallingDependencies = new LinkedList<MakeEntry>();
		
		while(iter.hasNext()) {
			
			MakeEntry mergeEntry = new MakeEntry();
			mergeEntry.addDependencies(mergeDependencies);
			
			Collection<String> vcfs = new LinkedList<String>();
			StringBuilder sampleIds = new StringBuilder();
			for(int i = 0; i < cohortSize && iter.hasNext(); i++) {
				Sample sample = iter.next();
				String vcf = output + "/" + sample.getSampleId() + ".snps.indels.g.vcf";
				vcfs.add(vcf);
				sampleIds.append(sample.getSampleId());
				sampleIds.append("_");
			}
			
			String outfile = output + "/merged.batch." + currentBatch++  + ".snps.indels.g.vcf";
			
			mergedVcfs.add(outfile);
			
			ST mergeCommand = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T CombineGVCFs -R <reference> -L <intervals> -o <out> <vcfs:{vcf | --variant <vcf> }>");
			mergeCommand.add("gatk", properties.getGatk())
			.add("java", properties.getJava())
			.add("jvmSize", properties.getJvmSize())
			.add("reference", properties.getReference())
			.add("out",outfile)
			.add("intervals", args.getPrimerList())
			.add("vcfs", vcfs);
			
			mergeEntry.setComment("Make " + sampleIds.toString() + "\t" + outfile);
			mergeEntry.setTarget(outfile + ".OK");
			
			mergeEntry.addCommand(mergeCommand.render());
			
			for(String vcf : vcfs) {
				mergeEntry.addCommand("rm "+ vcf);
				mergeEntry.addCommand("rm "+ vcf + ".idx");
			}
			
			mergeEntry.addCommand("touch $@");
			
			makefile.addMakeEntry(mergeEntry);
			
			jointCallingDependencies.add(mergeEntry);
		}
		
		/*
		 * (3) joint calling
		 */
		
		MakeEntry jointCallEntry = new MakeEntry();
		jointCallEntry.setComment("Jointly call merged vcfs");
		
		String outfile = output + "/merged.vcf";
		
		jointCallEntry.setTarget(outfile + ".OK");
		jointCallEntry.addDependencies(jointCallingDependencies);
		
		ST jointCallCommand = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T GenotypeGVCFs -R <reference> -L <intervals> -o <out> <vcfs:{vcf | --variant <vcf> }> --dbsnp <dbsnp>");
		jointCallCommand.add("gatk", properties.getGatk())
		.add("java", properties.getJava())
		.add("jvmSize", properties.getJvmSize())
		.add("reference", properties.getReference())
		.add("out",outfile)
		.add("intervals", args.getPrimerList())
		.add("dbsnp", properties.getDbsnp())
		.add("vcfs", mergedVcfs);
		
		jointCallEntry.addCommand(jointCallCommand.render());
		for(String vcf : mergedVcfs) {
			jointCallEntry.addCommand("rm " + vcf);
			jointCallEntry.addCommand("rm " + vcf + ".idx");
		}
		jointCallEntry.addCommand("touch $@");
		
		makefile.addMakeEntry(jointCallEntry);
		
		return makefile.writeTemplateToString();
	}

	@Override
	public String applyVQSR(String vcf, VQSRArgs args) throws Exception {
		MakeFile makefile = applicationContext.getBean(MakeFile.class);
		
		/*
		 * Recommendations
		 * https://www.broadinstitute.org/gatk/guide/article?id=39
		 * https://www.broadinstitute.org/gatk/guide/article?id=1259
		 * https://www.broadinstitute.org/gatk/guide/article?id=2805
		 * https://www.broadinstitute.org/gatk/guide/tooldocs/org_broadinstitute_gatk_tools_walkers_variantrecalibration_VariantRecalibrator.php
		 */
		
		/*
		 * (1) Build the SNP recalibration model (exclude depth) and MQ b/c it causes VQSR to crash...
		 */
		
		String vcfNameNormalized = null;
		if(vcf.endsWith(".gz")) {
			vcfNameNormalized = vcf.replaceFirst(".gz$", "");
		} else {
			vcfNameNormalized = vcf;
		}
		
		String basename = FilenameUtils.getBaseName(vcfNameNormalized);
		
		MakeEntry snpRecalEntry = new MakeEntry();
		
		snpRecalEntry.setComment("snp recalibration for " + vcf);
		snpRecalEntry.setTarget(output + "/SNP_RECAL.OK");
		snpRecalEntry.addDependency(new MakeEntry().setTarget(vcf));
		
		
		String snpRecalFile = output + "/recalibrate_SNP.recal";
		String snpTranchesFile = output + "/recalibrate_SNP.tranches";
		ST recalibrationCommand = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T VariantRecalibrator -R <reference> -nt <threads> -input <vcf> -resource:hapmap,known=false,training=true,truth=true,prior=15.0 <hapmap> -resource:omni,known=false,training=true,truth=true,prior=12.0 <omni> -resource:1000G,known=false,training=true,truth=false,prior=10.0 <1000g> -resource:dbsnp,known=true,training=false,truth=false,prior=2.0 <dbsnp> -an QD -an FS -an SOR  -an MQRankSum -an ReadPosRankSum -an InbreedingCoeff -mode SNP -tranche 100.0 -tranche 99.9 -tranche 99.0 -tranche 90.0 -recalFile <recalFile> -tranchesFile <tranchesFile> -rscriptFile <rscriptFile>  --maxGaussians 4");
		recalibrationCommand.add("gatk", properties.getGatk())
		.add("java", properties.getJava())
		.add("jvmSize", properties.getJvmSize())
		.add("reference", properties.getReference())
		.add("threads", args.getNumGatkThreads())
		.add("vcf", vcf)
		.add("recalFile", snpRecalFile)
		.add("tranchesFile", snpTranchesFile)
		.add("rscriptFile", output + "/recalibrate_SNP_plots.R")
		.add("1000g", properties.get_1000GHighConfSnps())
		.add("hapmap", properties.getHapmap33())
		.add("omni", properties.getOmniSites())
		.add("dbsnp", properties.getDbsnp());
		
		snpRecalEntry.addCommand(recalibrationCommand.render());
		
		/*
		 * (2) Apply Filter criteria
		 */
		
		String vcfSnpRecalOut = output + "/" + basename + ".snp.recal.vcf";
		ST applySnpRecalCommand = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T ApplyRecalibration -R <reference> -input <vcf> --ts_filter_level 99.0 -recalFile <recalFile> -tranchesFile <tranchesFile> -o <outVcf> -mode <mode>");
		applySnpRecalCommand.add("gatk", properties.getGatk())
		.add("java", properties.getJava())
		.add("jvmSize", properties.getJvmSize())
		.add("reference", properties.getReference())
		.add("vcf", vcf)
		.add("mode", "SNP")
		.add("outVcf", vcfSnpRecalOut)
		.add("recalFile", snpRecalFile)
		.add("tranchesFile", snpTranchesFile);
		
		snpRecalEntry.addCommand(applySnpRecalCommand.render());
		snpRecalEntry.addCommand("touch $@");
		
	
		makefile.addMakeEntry(snpRecalEntry);
		
		/*
		 * 3 Build indel recal model
		 */
		
		MakeEntry indelRecalEntry = new MakeEntry();
		
		indelRecalEntry.addDependency(snpRecalEntry);
		indelRecalEntry.setTarget(output + "/INDEL_RECAL.OK");
		String vcfSnpIndelRecalOut = output + "/" + basename + ".indel.snp.recal.vcf";
		
		String indelRecalFile = output + "/recalibrate_INDEL.recal";
		String indelTranchesFile = output + "/recalibrate_INDEL.tranches";
		ST recalibrationIndelCommand = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T VariantRecalibrator -R <reference> -nt <threads> -input <vcf>  -an QD -an FS -an SOR  -an MQRankSum -an ReadPosRankSum -an InbreedingCoeff -mode INDEL -resource:mills,known=true,training=true,truth=true,prior=12.0 <mills> -resource:dbsnp,known=true,training=false,truth=false,prior=2.0 <dbsnp> -tranche 100.0 -tranche 99.9 -tranche 99.0 -tranche 90.0 -recalFile <recalFile> -tranchesFile <tranchesFile> -rscriptFile <rscriptFile>  --maxGaussians 4");
		recalibrationIndelCommand.add("gatk", properties.getGatk())
		.add("java", properties.getJava())
		.add("jvmSize", properties.getJvmSize())
		.add("reference", properties.getReference())
		.add("threads", args.getNumGatkThreads())
		.add("vcf", vcfSnpRecalOut)
		.add("recalFile", indelRecalFile)
		.add("tranchesFile", indelTranchesFile)
		.add("rscriptFile", output + "/recalibrate_INDEL_plots.R")
		.add("dbsnp", properties.getDbsnp())
		.add("mills", properties.getGoldIndels());
		
		indelRecalEntry.addCommand(recalibrationIndelCommand.render());
		
		/*
		 * (4) apply recal to indels
		 */
		ST applyIndelRecalCommand = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T ApplyRecalibration -R <reference> -input <vcf> --ts_filter_level 99.0 -recalFile <recalFile> -tranchesFile <tranchesFile> -o <outVcf> -mode <mode>");
		applyIndelRecalCommand.add("gatk", properties.getGatk())
		.add("java", properties.getJava())
		.add("jvmSize", properties.getJvmSize())
		.add("reference", properties.getReference())
		.add("vcf", vcfSnpRecalOut)
		.add("mode", "INDEL")
		.add("outVcf", vcfSnpIndelRecalOut)
		.add("recalFile", indelRecalFile)
		.add("tranchesFile", indelTranchesFile);
		
		indelRecalEntry.addCommand(applyIndelRecalCommand.render());
		
		indelRecalEntry.addCommand("rm " + vcfSnpRecalOut);
		indelRecalEntry.addCommand("rm " + vcfSnpRecalOut + ".idx");
		
		indelRecalEntry.addCommand("touch $@");
		
		makefile.addMakeEntry(indelRecalEntry);
		
		return makefile.writeTemplateToString();
	}

	@Override
	public String writeIndelRealignRecalCommands(Collection<Sample> samples, AlignArgs alignArgs) throws Exception {
		MakeFile makefile = applicationContext.getBean(MakeFile.class);
		
		List<String> bams = new LinkedList<String>();
		StringBuilder bamlist = new StringBuilder();
		
		for(Sample s : samples) {
			bams.add(s.getBamFiles().get(0).getBamFile());
		}

		for(Sample sample : samples) {

			MakeEntry realignRecal = new MakeEntry();
			/*
			 * Indel Realignment
			 */
			
			if(sample.getBamFiles().size() != 1) {
				throw new Exception("Error only 1 bam allowed for each sample. " + sample.getSampleId());
			}
			
			String sampleIndelIntervals = output + "/" + sample.getSampleId() + ".interval_list";
			ST createTargetListCommand = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T RealignerTargetCreator -nt <numThreads> -R <reference> -L <intervals> -I <bam> --known <gold> --known <1000GIndels> -o <outIntervals>");
			createTargetListCommand.add("gatk", properties.getGatk())
			.add("java", properties.getJava())
			.add("jvmSize", properties.getJvmSize())
			.add("numThreads", alignArgs.getNumGatkThreads())
			.add("reference", properties.getReference())
			.add("bam", sample.getBamFiles().get(0).getBamFile())
			.add("gold", properties.getGoldIndels())
			.add("1000GIndels", properties.get_1000GIndels())
			.add("intervals", alignArgs.getPrimerList())
			.add("outIntervals", sampleIndelIntervals);
	
			realignRecal.addCommand(createTargetListCommand.render());
			
			String realignedBam = output + "/" + sample.getSampleId() + ".realinged.bam";
			String realignedBamIndex = output + "/" + sample.getSampleId() + ".realinged.bai";
			ST realignmentCommand = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T IndelRealigner -R <reference> -I <bam> -known <gold> -known <1000GIndels> -L <intervals> -targetIntervals <inIntervals> -o <outbam>");
			realignmentCommand.add("gatk", properties.getGatk())
			.add("java", properties.getJava())
			.add("jvmSize", properties.getJvmSize())
			.add("reference", properties.getReference())
			.add("bam", sample.getBamFiles().get(0).getBamFile())
			.add("gold", properties.getGoldIndels())
			.add("1000GIndels", properties.get_1000GIndels())
			.add("inIntervals", sampleIndelIntervals)
			.add("intervals", alignArgs.getPrimerList())
			.add("outbam", realignedBam);
			
			realignRecal.addCommand(realignmentCommand.render());
			
			/*
			 * Base Recalibration
			 */
			
			String recalibratedBam = output + "/" + sample.getSampleId() + ".realinged.recalibrated.bam";
			String recalibratedBamIndex = output + "/" + sample.getSampleId() + ".realinged.recalibrated.bai";
			String recalTablePass1 = output + "/" + sample.getSampleId() + ".grp";
			ST recalPass1Command = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T BaseRecalibrator -nct <numThreads> -R <reference> -I <bam> --knownSites <dbsnp> --knownSites <gold> --knownSites <1000GIndels> -L <intervals> -o <bqsr>");
			recalPass1Command.add("gatk", properties.getGatk())
			.add("java", properties.getJava())
			.add("jvmSize", properties.getJvmSize())
			.add("numThreads", alignArgs.getNumGatkThreads())
			.add("reference", properties.getReference())
			.add("bam", realignedBam)
			.add("dbsnp", properties.getDbsnp())
			.add("gold", properties.getGoldIndels())
			.add("1000GIndels", properties.get_1000GIndels())
			.add("intervals", alignArgs.getPrimerList())
			.add("bqsr", recalTablePass1);
			
			realignRecal.addCommand(recalPass1Command.render());
			
			String recalTablePass2= output + "/" + sample.getSampleId() + ".post.grp";
			ST recalPass2Command = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T BaseRecalibrator -nct <numThreads> -R <reference> -I <bam> --knownSites <dbsnp> --knownSites <gold> --knownSites <1000GIndels> --BQSR <bqsr_pre> -L <intervals> -o <bqsr_post>");
			recalPass2Command.add("gatk", properties.getGatk())
			.add("java", properties.getJava())
			.add("jvmSize", properties.getJvmSize())
			.add("numThreads", alignArgs.getNumGatkThreads())
			.add("reference", properties.getReference())
			.add("bam", realignedBam)
			.add("dbsnp", properties.getDbsnp())
			.add("gold", properties.getGoldIndels())
			.add("1000GIndels", properties.get_1000GIndels())
			.add("bqsr_pre", recalTablePass1)
			.add("intervals", alignArgs.getPrimerList())
			.add("bqsr_post", recalTablePass2);
			
			realignRecal.addCommand(recalPass2Command.render());
			
			
			ST writeRecalBam = new ST("<java> -Xmx<jvmSize> -jar <gatk> -T PrintReads -R <reference> -I <bam> --BQSR <bqsr_post> -o <bamout>");
			writeRecalBam.add("gatk", properties.getGatk())
			.add("java", properties.getJava())
			.add("jvmSize", properties.getJvmSize())
			.add("reference", properties.getReference())
			.add("bam", realignedBam)
			.add("bamout", recalibratedBam)
			.add("bqsr_post", recalTablePass2);
			
			realignRecal.addCommand(writeRecalBam.render());
			
			//Finish off makeentry
			realignRecal.addCommand("touch $@");
			realignRecal.setTarget(output + "/" + sample.getSampleId() + ".OK");
			realignRecal.setComment("Realign and Recal for " + sample.getSampleId());
		
			makefile.addMakeEntry(realignRecal);
			
			/*
			 * Add to bamlist
			 */
			bamlist.append(sample.getSampleId());
			bamlist.append("\t");
			bamlist.append(recalibratedBam);
			bamlist.append("\t");
			bamlist.append(recalibratedBamIndex);
			bamlist.append("\t");
			bamlist.append(".");
			bamlist.append("\n");
		}
		
		FileUtils.write(new File(output + "/bam.list.txt" ), bamlist.toString());
		
		return makefile.writeTemplateToString();
	}

	
	

}
