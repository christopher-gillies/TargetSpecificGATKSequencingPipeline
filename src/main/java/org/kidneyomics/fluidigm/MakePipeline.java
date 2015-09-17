package org.kidneyomics.fluidigm;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.kidneyomics.util.StringUtil;
import org.kidneyomics.vcf.VCFFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class MakePipeline {

	public enum COMMAND {
		ADAPTER_TRIM,
		ALIGN,
		VARIANT_CALL,
		MAKE_LOCATION_LIST,
		VQSR,
		SVM_FILTER,
		VARIANT_QC,
		HARD_FILTER,
		HARD_GENOTYPE_FILTER,
		NORMALIZE,
		REHEADER_VCF,
		SELECT_SITES,
		SELECT_SITES_BY_INTERVAL,
		SELECT_SAMPLES,
		SAMPLE_CALL_RATE,
		UNKNOWN
	}
	
	@Autowired
	ApplicationProperties properties;
	
	private String fieldToSelect = "exon";
	
	private String conf = null;
	
	private String commandWriterBeanName;
	private String output;
	private String bamFiles;
	private String fastqFiles;
	
	private boolean runVariantQC;
	
	private String samplesToKeep;
	
	private Collection<String> genes = new LinkedList<String>();
	
	private Options options;
	
	private String adapter1;
	private String adapter2;
	/*
	 * target specific length
	 */
	private int tspl = 20;
	private int minAdapterOverlap = 7;
	private double err = 0.05;
	
	private int numGatkThreads = 1;
	
	private String primerList;
	
	private int cohortSize = 200;
	
	private String vcf;
	
	private String snpFilter;
	private String indelFilter;
	private String confirmedFile = null;
	
	private String idMap = null;
	
	private boolean outMatrix = false;
	
	private boolean snpsOnly = false;
	
	private boolean unifiedGenotyper = false;
	
	private String sitesToKeep;
	
	private boolean keepPassOnly = false;
	
	private String xmx = null;
	
	private int padding = -1;
	
	private boolean bed = false;
	
	private double minCallRate = 0.5;
	
	
	public boolean isRunVariantQC() {
		return runVariantQC;
	}

	public void setRunVariantQC(boolean runVariantQC) {
		this.runVariantQC = runVariantQC;
	}

	public boolean isBed() {
		return bed;
	}

	public void setBed(boolean bed) {
		this.bed = bed;
	}

	public boolean isUnifiedGenotyper() {
		return unifiedGenotyper;
	}

	public void setUnifiedGenotyper(boolean unifiedGenotyper) {
		this.unifiedGenotyper = unifiedGenotyper;
	}

	public String getIdMap() {
		return idMap;
	}

	public void setIdMap(String idMap) {
		this.idMap = idMap;
	}

	public String getVcf() {
		return vcf;
	}

	public void setVcf(String vcf) {
		this.vcf = vcf;
	}

	public Options getOptions() {
		return options;
	}

	public void setOptions(Options options) {
		this.options = options;
	}

	
	
	public double getMinCallRate() {
		return minCallRate;
	}

	public void setMinCallRate(double minCallRate) {
		this.minCallRate = minCallRate;
	}

	


	public String getConf() {
		return conf;
	}

	public void setConf(String conf) {
		this.conf = conf;
	}

	



	public boolean isSnpsOnly() {
		return snpsOnly;
	}

	public void setSnpsOnly(boolean snpsOnly) {
		this.snpsOnly = snpsOnly;
	}





	private COMMAND command = COMMAND.UNKNOWN;
	
	static Logger logger = Logger.getLogger(MakePipeline.class);
	
	
	public static final File CURRENT_DIR = new File(".");
	
	public static final File DEFAULT_PROPERTY_FILE = new File(CURRENT_DIR.getAbsolutePath() + "/application.properties");
	
	public static void main(String[] args) throws Exception {
		
		logger.info("Pipeline Maker");
		ApplicationContext context = new ClassPathXmlApplicationContext("spring/applicationContext.xml");
		MakePipeline mp = context.getBean(MakePipeline.class); 
		getOptions(args, mp);
		
		
		/*
		 * Read application properties
		 */
		if(mp.getConf() == null) {
			if(DEFAULT_PROPERTY_FILE.exists()) {
				mp.properties.readProperties(DEFAULT_PROPERTY_FILE.getAbsolutePath());
			} else {
				throw new Exception("Please either specifiy a --conf file or create a default properties file in the current working directory");
			}
		} else {
			mp.properties.readProperties(mp.getConf());
		}
		
		
		//Override xmx
		if(mp.getXmx() != null) {
			mp.properties.setJvmSize(mp.getXmx());
		}
		
		CommandWriter commandWriter = (CommandWriter) context.getBean(mp.getCommandWriterBeanName());
		commandWriter.setOutput(mp.getOutput());
		mp.validate();
		logger.info("Parameters set correctly...");
		
		String makefileText = null;
		
		logger.info("COMMAND " + mp.getCommand());
		switch(mp.getCommand()) {
		case ALIGN:
		{
			Collection<Sample> samples = mp.getFastqFileList();
			AlignArgs alignArgs = new AlignArgs();
			alignArgs.setPrimerList(mp.getPrimerList());
			alignArgs.setNumGatkThreads(mp.getNumGatkThreads());
			makefileText = commandWriter.writePreprocessingCommands(samples,alignArgs);
			break;
		}
		case ADAPTER_TRIM:
		{
			Collection<Sample> samples = mp.getFastqFileList();
			TrimArgs trimArgs = new TrimArgs();
			trimArgs.setAdapter1(mp.getAdapter1())
			.setAdapter2(mp.getAdapter2())
			.setMinAdapterOverlap(mp.getMinAdapterOverlap())
			.setErr(mp.getErr())
			.setTspl(mp.getTspl());
			
			makefileText = commandWriter.writeTrimCommands(samples,trimArgs);
			break;
		}
		case VARIANT_CALL:
		{
			Collection<Sample> samples = mp.getBamList();
			CallArgs callArgs = new CallArgs();
			callArgs.setPrimerList(mp.getPrimerList());
			callArgs.setCohortSize(mp.getCohortSize());
			callArgs.setNumGatkThreads(mp.getNumGatkThreads());
			if(!mp.isUnifiedGenotyper()) {
				makefileText = commandWriter.writeVariantCallCommands(samples, callArgs);
			} else {
				makefileText = commandWriter.writeVariantCallCommandsUnifiedGenotyper(samples, callArgs);
			}
			break;
		}
		case MAKE_LOCATION_LIST:
		{
			CreateLocationsFileImpl createLocationsService = (CreateLocationsFileImpl) context.getBean("createLocationsFile",CreateLocationsFileImpl.class);
			String field = mp.getFieldToSelect();
			if(mp.isBed()) {
				createLocationsService.setOutBed(true);
			} else {
				createLocationsService.setOutBed(false);
			}
			
			if(mp.getPadding() >= 0) {
				logger.info("padding: " + mp.getPadding());
				createLocationsService.writeLocations(mp.getGenes(), mp.getPadding(), mp.getOutput(), field);
			} else {
				logger.info("using default padding");
				createLocationsService.writeLocations(mp.getGenes(), mp.getOutput(), field);
			}
			break;
		}
		case HARD_FILTER:
		{
			Filter siteFilter = (SiteQualityFilter) context.getBean("siteQualityFilter");
			siteFilter.setVcf(mp.getVcf());
			siteFilter.setIndelFilter(mp.getIndelFilter());
			siteFilter.setSnpFilter(mp.getSnpFilter());
			siteFilter.filter(mp.getOutput());
			break;
		}
		case HARD_GENOTYPE_FILTER:
		{
			Filter variantFilter = (VariantQualityFilter) context.getBean("variantQualityFilter");
			variantFilter.setVcf(mp.getVcf());
			variantFilter.setIndelFilter(mp.getIndelFilter());
			variantFilter.setSnpFilter(mp.getSnpFilter());
			variantFilter.filter(mp.getOutput());
			break;
		}
		case VARIANT_QC:
		{
			
			/*
			 * Write site stats
			 */
			InfoCollector siteInfoCollector = (InfoCollector) context.getBean("siteInfoCollector");
			siteInfoCollector.setVcf(mp.getVcf());
			
			String statsFile = mp.getOutput() + "/sites.stats.txt";
			siteInfoCollector.collectInfo(statsFile);
			
			SiteQualityScriptWriter siteQualityScriptWriter = (SiteQualityScriptWriter) context.getBean("siteQualityScriptWriter");
			siteQualityScriptWriter.setStatsFile(statsFile);
			siteQualityScriptWriter.setOutDir( mp.getOutput() );
			siteQualityScriptWriter.setConfirmedSites(mp.getConfirmedFile());
			
			String sitesScript = siteQualityScriptWriter.toString();
			String sitesScriptFile = mp.getOutput() + "/sites.script.R";
			FileUtils.write(new File(sitesScriptFile), sitesScript);
			logger.info("Rscript '" + sitesScriptFile + "'");
			//Process p = Runtime.getRuntime().exec(new String[] {"Rscript", sitesScriptFile } );
		    //p.waitFor();
			ProcessBuilder builder = new ProcessBuilder("/bin/bash","-c", "Rscript " + "\"" + sitesScriptFile + "\"" + "> /dev/null 2>&1");
			Process p = builder.start();
			p.waitFor();
		    
		    if(mp.isRunVariantQC()) {
		    
			    /*
			     * Write variant stats
			     */
			    InfoCollector variantInfoCollector = (InfoCollector) context.getBean("variantInfoCollector");
			    variantInfoCollector.setVcf(mp.getVcf());
			    String variantStatsFile = mp.getOutput() + "/variant.stats.txt";
			    variantInfoCollector.collectInfo(variantStatsFile);
				
			    
			    VariantQualityScriptWriter variantQualityScriptWriter = (VariantQualityScriptWriter) context.getBean("variantQualityScriptWriter");
			    variantQualityScriptWriter.setStatsFile(variantStatsFile);
			    variantQualityScriptWriter.setOutDir( mp.getOutput() );
				
				String variantScript = variantQualityScriptWriter.toString();
				String variantScriptFile = mp.getOutput() + "/variant.script.R";
				FileUtils.write(new File(variantScriptFile), variantScript);
				logger.info("Rscript '" + variantScriptFile + "'");
				//Process p2 = Runtime.getRuntime().exec(new String[] {"Rscript", variantScriptFile } );
			    //p2.waitFor();
				ProcessBuilder builder2 = new ProcessBuilder("/bin/bash","-c", "Rscript " + "\"" + variantScriptFile + "\"" + "> /dev/null 2>&1");
				Process p2 = builder2.start();
				p2.waitFor();
		    }
			break;
		}
		case SVM_FILTER:
		{
			//1 collect qc site data
			//2 run svm code
			//3 filter vcf
			
			//1
			GATKSiteInfoCollector siteInfoCollector = (GATKSiteInfoCollector) context.getBean("siteInfoCollector");
			siteInfoCollector.setMinCallRate(mp.getMinCallRate());
			siteInfoCollector.setVcf(mp.getVcf());
			siteInfoCollector.setCollectExacAnd1000G(true);
			String statsFile = mp.getOutput() + "/svm.sites.stats.txt";
			siteInfoCollector.collectInfo(statsFile);
			
			//2
			SVMScriptWriter svmScriptWriter = (SVMScriptWriter) context.getBean("svmScriptWriter");
			svmScriptWriter.setOutDir(mp.getOutput());
			svmScriptWriter.setStatsFile(statsFile);
			svmScriptWriter.setConfirmedSites(mp.getConfirmedFile());
			String script = svmScriptWriter.writeTemplateToString();
			String svmScriptFile = mp.getOutput() + "/svm.script.R";
			FileUtils.write(new File(svmScriptFile), script);
			//Process p = Runtime.getRuntime().exec("Rscript " + svmScriptFile);
			//Process p = Runtime.getRuntime().exec(new String[] {"Rscript", svmScriptFile } );
		    //p.waitFor();
		    
			ProcessBuilder builder = new ProcessBuilder("/bin/bash","-c", "Rscript " + "\"" + svmScriptFile + "\"" + "> /dev/null 2>&1");
			
			Process p = builder.start();
			p.waitFor();
			
		    //3
		    SiteSVMFilter siteSvmFilter = (SiteSVMFilter) context.getBean("siteSvmFilter");
		    siteSvmFilter.setSnpFilter(mp.getOutput() + "/svm.snp.predictions.txt");
		    siteSvmFilter.setIndelFilter(mp.getOutput() + "/svm.indel.predictions.txt");
		    siteSvmFilter.setVcf(mp.getVcf());
		    siteSvmFilter.setSnpsOnly(mp.isSnpsOnly());
		    siteSvmFilter.filter(mp.getOutput() + "/svm.filtered.vcf");
			break;
		}
		case VQSR:
		{
			VQSRArgs vqsrArgs = new VQSRArgs();
			vqsrArgs.setNumGatkThreads(mp.getNumGatkThreads());
			makefileText = commandWriter.applyVQSR(mp.getVcf(), vqsrArgs);
			break;
		}
		case NORMALIZE:
		{
			/*
			//ST cmd = new ST("<vt> decompose <vcf> | <vt> normalize - -r <ref>");
			ST decompose = new ST("<vt> decompose <vcf> | <vt> normalize - -r <ref> > <out>");
			decompose.add("vcf", mp.getVcf());
			decompose.add("vt", mp.getProperties().getVt());
			decompose.add("ref", mp.getProperties().getReference());
			decompose.add("out", mp.getOutput());
			logger.info(decompose.render());
			MakeFile mk = (MakeFile) context.getBean("makeFileWriter");
			MakeEntry entry = new MakeEntry();
			entry.addCommand(decompose.render());
			entry.setTarget(mp.getOutput());
			mk.addMakeEntry(entry);
			String dir = FileUtils.getTempDirectory().getAbsolutePath();
			File out = new File(dir + "/Makefile");
			FileUtils.write(out, mk.toString());
			String cmd = "make -f " + dir + "/Makefile";
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			logger.info(cmd);
			*/
			
			DecomposeMultiAlleleicSites decomposeMultiAlleleicSites = (DecomposeMultiAlleleicSites) context.getBean("decomposeMultiAlleleicSites");
			decomposeMultiAlleleicSites.decompose(mp.getVcf(), mp.getOutput());
			
			break;
		}
		case REHEADER_VCF:
		{
			VCFReheader reheader = (VCFReheader) context.getBean("vcfReheader");
			reheader.reheader(mp.getVcf(), mp.getOutput(), mp.getIdMap());
			break;
		}
		case SELECT_SAMPLES:
		{
			SubsetVCFSamplesImpl subsetVCFSamplesImpl = (SubsetVCFSamplesImpl) context.getBean("subsetVCFSamplesImpl");
			subsetVCFSamplesImpl.setSampleList(mp.getSamplesToKeep());
			subsetVCFSamplesImpl.setKeepPassOnly(mp.isKeepPassOnly());
			subsetVCFSamplesImpl.subset(mp.getVcf(), mp.getOutput());
			
			break;
		}
		case SELECT_SITES:
		{
			SubsetVCFSitesImpl subsetVCFSitesImpl = (SubsetVCFSitesImpl) context.getBean("subsetVCFSitesImpl");
			subsetVCFSitesImpl.setSitesToKeep(mp.getSitesToKeep());
			if(mp.isOutMatrix()) {
				logger.info("Subsetting vcf: " + mp.getVcf());
				String tmpDir = FileUtils.getTempDirectoryPath();
				String tmpVcf = tmpDir + "/tmp.vcf";
				subsetVCFSitesImpl.subset(mp.getVcf(), tmpVcf);
				VCFFile vcf = new VCFFile();
				logger.info("Writing matrix out: " + mp.getOutput());
				vcf.writeMatrix(tmpVcf,mp.getOutput());
			} else {
				logger.info("Subsetting vcf: " + mp.getVcf());
				logger.info("Writing vcf: " + mp.getOutput());
				subsetVCFSitesImpl.subset(mp.getVcf(), mp.getOutput());
			}
			break;
		}
		case SELECT_SITES_BY_INTERVAL:
		{
			SubsetVCFSitesByIntervalImpl subsetVCFSitesByIntervalImpl = (SubsetVCFSitesByIntervalImpl) context.getBean("subsetVCFSitesByIntervalImpl");
			subsetVCFSitesByIntervalImpl.setSitesToKeep(mp.getSitesToKeep());
			if(mp.isOutMatrix()) {
				logger.info("Subsetting vcf: " + mp.getVcf());
				String tmpDir = FileUtils.getTempDirectoryPath();
				String tmpVcf = tmpDir + "/tmp.vcf";
				subsetVCFSitesByIntervalImpl.subset(mp.getVcf(), tmpVcf);
				VCFFile vcf = new VCFFile();
				logger.info("Writing matrix out: " + mp.getOutput());
				vcf.writeMatrix(tmpVcf,mp.getOutput());
			} else {
				logger.info("Subsetting vcf: " + mp.getVcf());
				logger.info("Writing vcf: " + mp.getOutput());
				subsetVCFSitesByIntervalImpl.subset(mp.getVcf(), mp.getOutput());
			}
			break;
		}
		case SAMPLE_CALL_RATE:
		{
			VCFFile vfile = new VCFFile();
			HashMap<String,Float> results = vfile.calculateSampleCallRate(mp.getVcf());
			StringBuilder sb = new StringBuilder();
			List<String> sampleIds = new LinkedList<String>();
			sampleIds.addAll(results.keySet());
			Collections.sort(sampleIds);
			for(String sampleId : sampleIds) {
				sb.append(sampleId);
				sb.append("\t");
				sb.append(results.get(sampleId));
				sb.append("\n");
			}
			FileUtils.write(new File(mp.getOutput()), sb.toString());
			break;
		}
		case UNKNOWN:
		{
			break;
		}
		}
		
		if(makefileText != null) {
			//logger.info("\n" + makefileText);
			File makefileOut = new File(mp.getOutput() + "/Makefile");
			FileUtils.write(makefileOut, makefileText);
		}
		
	}
	
	private Collection<Sample> getBamList() throws Exception {
		Collection<Sample> samples = new LinkedList<Sample>();
		
		File file = new File(this.getBamFiles());
		if(!file.exists()) {
			throw new Exception("bamFiles list does not exist!");
		}
		
		List<String> lines = FileUtils.readLines(file);
		for(String line : lines) {
			String[] cols = line.split("\t");
			
			if(cols.length < 2) {
				throw new Exception(lines + " not formated correctly!");
			}
			
			Sample sample = new Sample(cols[0]);
			BAM bam = new BAM();
			sample.getBamFiles().add(bam);
			if(cols.length == 2) {
				bam.setBamFile(cols[1]);
				
				File bamFile = new File(cols[1]);
				if(!bamFile.exists()) {
					throw new Exception(cols[1] + " does not exist!");
				}
				
			} else if(cols.length == 3) {
				
				bam.setBamFile(cols[1]);
				File bamFile = new File(cols[1]);
				if(!bamFile.exists()) {
					throw new Exception(cols[1] + " does not exist!");
				}
				
				bam.setBamIndex(cols[2]);
				File bamIndex = new File(cols[2]);
				if(!bamIndex.exists()) {
					throw new Exception(cols[2] + " does not exist!");
				}
				
			} else if(cols.length == 4) {
				bam.setBamFile(cols[1]);
				File bamFile = new File(cols[1]);
				if(!bamFile.exists()) {
					throw new Exception(cols[1] + " does not exist!");
				}
				
				bam.setBamIndex(cols[2]);
				File bamIndex = new File(cols[2]);
				if(!bamIndex.exists()) {
					throw new Exception(cols[2] + " does not exist!");
				}
				
				bam.setStatFile(cols[3]);
				File bamStats = new File(cols[3]);
				if(!bamStats.exists()) {
					throw new Exception(cols[3] + " does not exist!");
				}
				
			}
			logger.info(sample);
			samples.add(sample);
			
			
			
		
		}
		
		return samples;
	}
	
	private Collection<Sample> getFastqFileList() throws Exception {
		HashMap<String,Sample> samples = new HashMap<String,Sample>();
		File file = new File(this.getFastqFiles());
		if(!file.exists()) {
			throw new Exception("fastqFiles list does not exist!");
		}
		List<String> lines = FileUtils.readLines(file);
		for(String line : lines) {
			String[] cols = line.split("\t");
			if(cols.length < 2) {
				throw new Exception(line + "\n not formatted correctly");
			}
			
			String sampleId = cols[0];
			Sample sample = null;
			if(samples.containsKey(sampleId)) {
				sample = samples.get(sampleId);
			} else {
				sample = new Sample(sampleId);
				samples.put(sampleId, sample);
			}
			
			if(cols.length == 2) {
				FASTQ fastq1 = new FASTQ();
				fastq1.setFile1(cols[1]);
				
				if(fastq1.isValid()) {
					sample.addFASTQ(fastq1);
				} else {
					throw new Exception(fastq1.getFile1() + " not found!");
				}
				
			} else if(cols.length == 3) {
				
				FASTQ fastq1 = new FASTQ();
				fastq1.setFile1(cols[1]);
				fastq1.setFile2(cols[2]);
				
				if(fastq1.isValid()) {
					sample.addFASTQ(fastq1);
				} else {
					throw new Exception(fastq1.getFile1() + " or " + fastq1.getFile2() + " not found!");
				}
				
			} 
			
			logger.info("READ SAMPLE: "+sample.toString());
		}
		return samples.values();
	}
	
	public static void getOptions(String[] args, MakePipeline mp) throws ParseException {
		Options options = new Options();
		options.addOption("help",false,"Print the help message");
		options.addOption("keepPassSitesOnly",false,"Only keep pass sites");
		options.addOption("runVariantLevelQC",false,"Perform QC at the variant level and the site level");
		options.addOption("bed",false,"output a bed file instead of an interval file for makeLocations");
		options.addOption("unifiedGenotyper",false,"Use UnifiedGenotyper instead of HaplotypeCaller");
		options.addOption("snpsOnly",false,"Only apply svm filter to snps allow all indels to pass");
		options.addOption("outMatrix",false,"output a matrix instead of VCF for site subset");
		options.addOption("command",true,"align, call, trim, makeLocations, variantQc, hardFilter, hardGenotypeFilter, normalize, reheader, selectSites, selectSitesByInterval, selectSamples, svmFilter, sampleCallRate [REQUIRED]");
		options.addOption("conf",true,"the configuration file or properties file");
		options.addOption("output",true,"the target or directory to write to");
		options.addOption("fastqFiles",true,"name of file containing FASTQ files and sample names");
		options.addOption("bamFiles",true,"name of file containing BAM files and sample names");
		options.addOption("outputType",true,"The output type: currently only makefile is supported");
		options.addOption("xmx",true,"The size of the jvm. This overides the application property setting");
		options.addOption("adapter1",true,"The adapter to trim off of the 3' end of the sequences for the first paired end file (R1)");
		options.addOption("adapter2",true,"The adapter to trim off of the 3' end of the sequences for the second paired end file (R2)");
		
		options.addOption("snpFilter",true,"the hard snp filter (Default: QD < 4.5 | MQ < 40 | MQ_RANKSUM < -12.5 | RP_RANKSUM < -6.0 | CALLRATE < 0.7)");
		
		options.addOption("svmSnpFilter",true,"specify snp hard filters e.g. QD < 2. These filters are used to help identify negative training examples for the svm (NOT IMPLEMENTED)");
		
		options.addOption("svmIndelFilter",true,"specify indel hard filters e.g. QD < 2. These filters are used to help identify negative training examples for the svm (NOT IMPLEMENTED)");
		options.addOption("minCallRate",true,"The minimum callrate required for a variant to be considered a positive training example. The default is 0.5.");
		
		options.addOption("indelFilter",true,"the hard indel filter (Default: QD < 2.0 || FS > 200.0 || ReadPosRankSum < -20.0");
		
		options.addOption("confirmed",true,"the list of confirmed variants. this is optional for the hard filter");
		
		
		options.addOption("tspl",true,"targeted sequence specific primer length (Default: 20)");
		
		options.addOption("minAdapterOverlap",true,"the minimum adapter sequence required to match in a read (Default: 7)");
		
		options.addOption("maxErr",true,"the maximum percent errors allowed when trimming adapter sequences (Default: 0.05)");
		
		options.addOption("numGatkThreads",true,"the number of GATK threads to use (Default: 1)");
		
		options.addOption("primerLocations",true,"the locations of the primers in a file e.g. 10:16866974-16867081 each with a new line");
		
		options.addOption("sitesToKeep",true,"the sites to keep");
		options.addOption("samplesToKeep",true,"the samples to keep");
		
		options.addOption("idMap",true,"a map of the ids, there can be multiple possible mappings. This should be a file [NEW_ID]\t[OLD_ID]");
		
		
		options.addOption("genes",true,"Comma separated list of genes");
		options.addOption("fieldToUse",true,"The field to select out of the genecode file [Default: exon] ");
		
		options.addOption("padding",true,"the size of the padding for the makeLocations command");
		
		options.addOption("cohortSize",true,"The size of the cohort to merge in the CombineGVCFs step (Default: 20)");
		
		
		options.addOption("vcf",true,"The vcf file that you want to apply filtering to");
		
		mp.setOptions(options);
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse( options, args);
		
		//set default bean name
		mp.setCommandWriterBeanName("makeFileBuilder");
		
		
		if(cmd.hasOption("help")) {
			printHelp(options);
		}
		
		if(cmd.hasOption("runVariantLevelQC")) {
			mp.setRunVariantQC(true);
		} else {
			mp.setRunVariantQC(false);
		}
		
		if(cmd.hasOption("keepPassSitesOnly")) {
			mp.setKeepPassOnly(true);
		}
		
		if(cmd.hasOption("snpsOnly")) {
			mp.setSnpsOnly(true);
		} else {
			mp.setSnpsOnly(false);
		}
		
		if(cmd.hasOption("unifiedGenotyper")) {
			mp.setUnifiedGenotyper(true);
		}
		
		if(cmd.hasOption("bed")) {
			mp.setBed(true);
		} else {
			mp.setBed(false);
		}
		
		
		
		if(cmd.hasOption("outputType")) {
			String outputType = cmd.getOptionValue("outputType");
			
			if(outputType.equals("makefile")) {
				mp.setCommandWriterBeanName("makeFileBuilder");
			}
			//More options here
		}
		
		if(cmd.hasOption("command")) {
			String value = cmd.getOptionValue("command");
			
			if(value.equals("align")) {
				mp.setCommand(COMMAND.ALIGN);
			} else if(value.equals("call")) {
				mp.setCommand(COMMAND.VARIANT_CALL);
			} else if(value.equals("trim")) {
				mp.setCommand(COMMAND.ADAPTER_TRIM);
			} else if(value.equals("makeLocations")) {
				mp.setCommand(COMMAND.MAKE_LOCATION_LIST);
			} else if(value.equals("vqsr")) {
				mp.setCommand(COMMAND.VQSR);
			} else if(value.equals("variantQc")) {
				mp.setCommand(COMMAND.VARIANT_QC);
			} else if(value.equals("hardFilter")) {
				mp.setCommand(COMMAND.HARD_FILTER);
			} else if(value.equals("hardGenotypeFilter")) {
				mp.setCommand(COMMAND.HARD_GENOTYPE_FILTER);
			} else if(value.equals("normalize")) {
				mp.setCommand(COMMAND.NORMALIZE);
			} else if(value.equals("reheader")) {
				mp.setCommand(COMMAND.REHEADER_VCF);
			} else if(value.equals("selectSites")) {
				mp.setCommand(COMMAND.SELECT_SITES);
			} else if(value.equalsIgnoreCase("selectSitesByInterval")) { 
				mp.setCommand(COMMAND.SELECT_SITES_BY_INTERVAL);
			} else if(value.equals("selectSamples")) {
				mp.setCommand(COMMAND.SELECT_SAMPLES);
			} else if(value.equals("svmFilter")) {
				mp.setCommand(COMMAND.SVM_FILTER);
			} else if(value.equals("sampleCallRate")) {
				mp.setCommand(COMMAND.SAMPLE_CALL_RATE);
			} else {
				mp.setCommand(COMMAND.UNKNOWN);
			}
		}
		
		if(cmd.hasOption("output")) {
			mp.setOutput(cmd.getOptionValue("output"));
		}
		
		if(cmd.hasOption("conf")) {
			mp.setConf(cmd.getOptionValue("conf"));
		}
		
		if(cmd.hasOption("minCallRate")) {
			mp.setMinCallRate( Double.parseDouble(cmd.getOptionValue("minCallRate")));
		}
		
		if(cmd.hasOption("padding")) {
			mp.setPadding(Integer.parseInt(cmd.getOptionValue("padding")));
		}
		
		if(cmd.hasOption("sitesToKeep")) {
			mp.setSitesToKeep(cmd.getOptionValue("sitesToKeep"));
		}
		
		if(cmd.hasOption("samplesToKeep")) {
			mp.setSamplesToKeep(cmd.getOptionValue("samplesToKeep"));
		}
		
		if(cmd.hasOption("genes")) {
			String genesStr = cmd.getOptionValue("genes");
			logger.info("Genes:-" + genesStr+"-");
			if(!StringUtil.isNullOrEmpty(genesStr) && !genesStr.equals(" ")) {
				String genes[] = genesStr.split(",");
				for(int i = 0; i < genes.length; i++) {
					mp.getGenes().add(genes[i]);
				}
			}
		}
		
		if(cmd.hasOption("xmx")) {
			mp.setXmx(cmd.getOptionValue("xmx"));
		}
		
		if(cmd.hasOption("idMap")) {
			mp.setIdMap(cmd.getOptionValue("idMap"));
		}
		
		if(cmd.hasOption("bamFiles")) {
			mp.setBamFiles(cmd.getOptionValue("bamFiles"));
		}
		
		if(cmd.hasOption("fastqFiles")) {
			mp.setFastqFiles(cmd.getOptionValue("fastqFiles"));
		}
		
		if(cmd.hasOption("adapter1")) {
			mp.setAdapter1(cmd.getOptionValue("adapter1"));
		}
		
		if(cmd.hasOption("adapter2")) {
			mp.setAdapter2(cmd.getOptionValue("adapter2"));
		}
		
		if(cmd.hasOption("snpFilter")) {
			mp.setSnpFilter(cmd.getOptionValue("snpFilter"));
		}
		
		if(cmd.hasOption("svmSnpFilter")) {
			String[] filters = cmd.getOptionValues("svmSnpFilter");
			for(String filter : filters) {
				logger.info(filter);
			}
		}
		
		if(cmd.hasOption("svmIndelFilter")) {
			String[] filters = cmd.getOptionValues("svmSnpFilter");
			for(String filter : filters) {
				logger.info(filter);
			}
		}
		
		if(cmd.hasOption("indelFilter")) {
			mp.setIndelFilter(cmd.getOptionValue("indelFilter"));
		}
		
		if(cmd.hasOption("tspl")) {
			mp.setTspl(Integer.parseInt(cmd.getOptionValue("tspl")));
		}
		
		if(cmd.hasOption("minAdapterOverlap")) {
			mp.setMinAdapterOverlap(Integer.parseInt(cmd.getOptionValue("minAdapterOverlap")));
		}
		
		if(cmd.hasOption("cohortSize")) {
			mp.setCohortSize(Integer.parseInt(cmd.getOptionValue("cohortSize")));
		}
		
		if(cmd.hasOption("maxErr")) {
			mp.setErr(Double.parseDouble(cmd.getOptionValue("maxErr")));
			//logger.info("maxErr: " + mp.getErr());
		}
		
		if(cmd.hasOption("numGatkThreads")) {
			mp.setNumGatkThreads(Integer.parseInt(cmd.getOptionValue("numGatkThreads")));
		}
		
		if(cmd.hasOption("primerLocations")) {
			mp.setPrimerList((cmd.getOptionValue("primerLocations")));
		}
		
		if(cmd.hasOption("vcf")) {
			mp.setVcf(cmd.getOptionValue("vcf"));
		}
		
		if(cmd.hasOption("fieldToSelect")) {
			mp.setFieldToSelect(cmd.getOptionValue("fieldToSelect"));
		}
		
		if(cmd.hasOption("outMatrix")) {
			mp.setOutMatrix(true);
		}
		
		if(cmd.hasOption("confirmed")) {
			mp.setConfirmedFile(cmd.getOptionValue("confirmed"));
		}
	}
	
	public static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "Target Specific GATK Sequencing Pipeline", options );
		System.exit(0);
	}
	
	public ApplicationProperties getProperties() {
		return this.properties;
	}
	
	public void setCommandWriterBeanName(String bean) {
		this.commandWriterBeanName = bean;
	}
	
	
	
	public String getFieldToSelect() {
		return fieldToSelect;
	}

	public void setFieldToSelect(String fieldToSelect) {
		this.fieldToSelect = fieldToSelect;
	}

	public String getCommandWriterBeanName() {
		return this.commandWriterBeanName;
	}

	public COMMAND getCommand() {
		return command;
	}

	public void setCommand(COMMAND command) {
		this.command = command;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getBamFiles() {
		return bamFiles;
	}

	public void setBamFiles(String bamFiles) {
		this.bamFiles = bamFiles;
	}

	public String getFastqFiles() {
		return fastqFiles;
	}

	public void setFastqFiles(String fastqFiles) {
		this.fastqFiles = fastqFiles;
	}
	
	
	
	public int getPadding() {
		return padding;
	}

	public void setPadding(int padding) {
		this.padding = padding;
	}

	public int getCohortSize() {
		return cohortSize;
	}

	public void setCohortSize(int cohortSize) {
		this.cohortSize = cohortSize;
	}

	public String getAdapter1() {
		return adapter1;
	}

	public void setAdapter1(String adapter1) {
		this.adapter1 = adapter1;
	}

	public String getAdapter2() {
		return adapter2;
	}

	public void setAdapter2(String adapter2) {
		this.adapter2 = adapter2;
	}

	public int getTspl() {
		return tspl;
	}

	public void setTspl(int tspl) {
		this.tspl = tspl;
	}
	

	public int getMinAdapterOverlap() {
		return minAdapterOverlap;
	}

	public void setMinAdapterOverlap(int minAdapterOverlap) {
		this.minAdapterOverlap = minAdapterOverlap;
	}

	
	
	public boolean isOutMatrix() {
		return outMatrix;
	}

	public void setOutMatrix(boolean outMatrix) {
		this.outMatrix = outMatrix;
	}

	public double getErr() {
		return err;
	}

	public void setErr(double err) {
		this.err = err;
	}

	
	
	public int getNumGatkThreads() {
		return numGatkThreads;
	}

	public void setNumGatkThreads(int numGatkThreads) {
		this.numGatkThreads = numGatkThreads;
	}
	
	

	public String getPrimerList() {
		return primerList;
	}

	public void setPrimerList(String primerList) {
		this.primerList = primerList;
	}

	
	
	public String getSnpFilter() {
		return snpFilter;
	}

	public void setSnpFilter(String snpFilter) {
		this.snpFilter = snpFilter;
	}

	public String getIndelFilter() {
		return indelFilter;
	}

	public void setIndelFilter(String indelFilter) {
		this.indelFilter = indelFilter;
	}

	public String getConfirmedFile() {
		return confirmedFile;
	}

	public void setConfirmedFile(String confirmedFile) {
		this.confirmedFile = confirmedFile;
	}

	public Collection<String> getGenes() {
		return genes;
	}

	public void setGenes(Collection<String> genes) {
		this.genes = genes;
	}

	
	
	public boolean isKeepPassOnly() {
		return keepPassOnly;
	}

	public void setKeepPassOnly(boolean keepPassOnly) {
		this.keepPassOnly = keepPassOnly;
	}

	public String getSamplesToKeep() {
		return samplesToKeep;
	}

	public void setSamplesToKeep(String samplesToKeep) {
		this.samplesToKeep = samplesToKeep;
	}

	public String getSitesToKeep() {
		return sitesToKeep;
	}

	public void setSitesToKeep(String sitesToKeep) {
		this.sitesToKeep = sitesToKeep;
	}
	
	

	public String getXmx() {
		return xmx;
	}

	public void setXmx(String xmx) {
		this.xmx = xmx;
	}

	public void validate() throws Exception {
		
		if(StringUtils.isEmpty(this.getOutput())) {
			throw new Exception("Please set output option");
		}
		
		
		if(StringUtils.isEmpty(this.getConf()) && !DEFAULT_PROPERTY_FILE.exists()) {
			throw new Exception("Please either specifiy a --conf file or create a default properties file in the current working directory");
		}
		
		switch(this.getCommand()) {
		case ALIGN:
			if(StringUtils.isEmpty(this.getFastqFiles())) {
				throw new Exception("Please set fastqFiles option");
			} else {
				File f = new File(this.getFastqFiles());
				if(!f.exists()) {
					throw new Exception("fastqFiles does not exist");
				}
			}
			
			if(StringUtils.isEmpty(this.getPrimerList())) {
				throw new Exception("Please set primerLocations option");
			} else {
				File f = new File(this.getPrimerList());
				if(!f.exists()) {
					throw new Exception("primerLocations file does not exist");
				}
				
				if(!this.getPrimerList().endsWith("intervals")) {
					throw new Exception("primerLocations file should have extension 'intervals' this is required for GATK");
				}
			}
			
			break;
		case ADAPTER_TRIM:
			if(StringUtils.isEmpty(this.getFastqFiles())) {
				throw new Exception("Please set fastqFiles option");
			} else {
				File f = new File(this.getFastqFiles());
				if(!f.exists()) {
					throw new Exception("fastqFiles does not exist");
				}
			}
			
			if(StringUtils.isEmpty(this.getAdapter1())) {
				throw new Exception("Please set adapter1 option");
			}
			break;
		case VARIANT_CALL:
			if(StringUtils.isEmpty(this.getBamFiles())) {
				throw new Exception("Please set bamFiles option");
			} else {
				File f = new File(this.getBamFiles());
				if(!f.exists()) {
					throw new Exception("bamFiles does not exist");
				}
			}
			
			if(StringUtils.isEmpty(this.getPrimerList())) {
				throw new Exception("Please set primerLocations option");
			} else {
				File f = new File(this.getPrimerList());
				if(!f.exists()) {
					throw new Exception("primerLocations file does not exist");
				}
				
				if(!this.getPrimerList().endsWith("intervals")) {
					throw new Exception("primerLocations file should have extension 'intervals' this is required for GATK");
				}
			}
			
			break;
		case MAKE_LOCATION_LIST:
			//if(this.getGenes().size() == 0) {
			//	throw new Exception("please specify some genes!");
			//}
			break;
		case HARD_GENOTYPE_FILTER:
		case HARD_FILTER:
			if(StringUtils.isEmpty(this.getSnpFilter())) {
				throw new Exception("Please set the snp filter");
			}
			
			if(StringUtils.isEmpty(this.getIndelFilter())) {
				throw new Exception("Please set the indel filter");
			}
			if(StringUtils.isEmpty(this.getVcf())) {
				throw new Exception("Please set vcf option");
			} else {
				File f = new File(this.getVcf());
				if(!f.exists()) {
					throw new Exception("vcf file does not exist");
				}
			}
			break;
		case REHEADER_VCF:
			if(StringUtils.isEmpty(this.getIdMap())) {
				throw new Exception("Please set idMap option");
			} else {
				File f = new File(this.getIdMap());
				if(!f.exists()) {
					throw new Exception("idMap file does not exist");
				}
			}
			if(StringUtils.isEmpty(this.getVcf())) {
				throw new Exception("Please set vcf option");
			} else {
				File f = new File(this.getVcf());
				if(!f.exists()) {
					throw new Exception("vcf file does not exist");
				}
			}
			break;
		case SELECT_SITES_BY_INTERVAL:
		case SELECT_SITES: {
			if(StringUtils.isEmpty(this.getSitesToKeep())) {
				throw new Exception("Please set sitesToKeep option");
			} else {
				File f = new File(this.getSitesToKeep());
				if(!f.exists()) {
					throw new Exception("sitesToKeep file does not exist");
				}
			}
			if(StringUtils.isEmpty(this.getVcf())) {
				throw new Exception("Please set vcf option");
			} else {
				File f = new File(this.getVcf());
				if(!f.exists()) {
					throw new Exception("vcf file does not exist");
				}
			}
			break;
		}
		case SELECT_SAMPLES: {
			if(StringUtils.isEmpty(this.getSamplesToKeep())) {
				throw new Exception("Please set samplesToKeep option");
			} else {
				File f = new File(this.getSamplesToKeep());
				if(!f.exists()) {
					throw new Exception("samplesToKeep file does not exist");
				}
			}
			if(StringUtils.isEmpty(this.getVcf())) {
				throw new Exception("Please set vcf option");
			} else {
				File f = new File(this.getVcf());
				if(!f.exists()) {
					throw new Exception("vcf file does not exist");
				}
			}
			break;
		}
		case SAMPLE_CALL_RATE:
		case SVM_FILTER:
		case NORMALIZE:
		case VARIANT_QC:
		case VQSR:
			if(StringUtils.isEmpty(this.getVcf())) {
				throw new Exception("Please set vcf option");
			} else {
				File f = new File(this.getVcf());
				if(!f.exists()) {
					throw new Exception("vcf file does not exist");
				}
			}
			break;
		case UNKNOWN:
			
			printHelp(this.getOptions());
			break;
			default:
				throw new Exception("Unsupported command!");
		}
		
	}
	
	
}
