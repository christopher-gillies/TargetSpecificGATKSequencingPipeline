package org.kidneyomics.fluidigm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.sf.samtools.util.CloseableIterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kidneyomics.vcf.GenotypeField;
import org.kidneyomics.vcf.GenotypeFieldParser;
import org.kidneyomics.vcf.RegexStringCaptureService;
import org.kidneyomics.vcf.VCFFile;
import org.kidneyomics.vcf.VCFFile.VCFLineIterator;
import org.kidneyomics.vcf.VCFLine;
import org.kidneyomics.vcf.ValueCaptureService;

public class SiteSVMFilter implements Filter {

	Logger logger = Logger.getLogger(SiteSVMFilter.class);
	
	private String vcf;
	private String snpFilter;
	private String indelFilter;
	
	ApplicationProperties applicationProperties;
	
	@Override
	public void filter(String out) throws Exception {
			
		VCFFile vfile = new VCFFile();
		
		HashMap<String,SVMResult> snpResults = parseResults(snpFilter);
		HashMap<String,SVMResult> indelResults = parseResults(indelFilter);
		
		
		VCFLineIterator iter = vfile.iterator(vcf);
		Writer writer = new BufferedWriter( new FileWriter(new File(out)));
		
		List<String> headerLines = iter.getHeaderLines();
		headerLines.add(1, "##INFO=<ID=SVM_PROBABILITY,Number=1,Type=Float,Description=\"The probability that the site is real from a support vector machine\">");
		headerLines.add(1, "##INFO=<ID=SVM_POSTERIOR,Number=1,Type=Float,Description=\"The probability that the site is real using the SVM_PROBABILITY as the likelihood and a prior based on whether the variant exists elsewhere\">");
		headerLines.add(1, "##INFO=<ID=PASS_EXAC,Number=1,Type=String,Description=\"Did this pass the ExAC filter?\">");
		headerLines.add(1, "##INFO=<ID=PASS_1KG,Number=1,Type=String,Description=\"Did this pass the 1000G Phase 3 filter?\">");
		headerLines.add(1, "##INFO=<ID=CONSENSUS_CALL,Number=1,Type=String,Description=\"Did this pass the 1000G Phase 3 filter, ExAC Filter or in dbSNP b138?\">");
		headerLines.add(1, "##FILTER=<ID=FILTER,Description=\"Filtered by SVM classifier trained using PASS/FAIL sites from ExAC v0.3, 1000G Phase 3 and dbSNP b138.\">");
		
		writer.write(StringUtils.join(headerLines, "\n"));
		writer.write("\n");
		
		
		
		
		
		while(iter.hasNext()) {
			VCFLine vline = iter.next();
			
			/*
			 * Should we filter the variant?
			 */
			SVMResult svmRes = null;
			boolean keep = false;
			if(vline.isSNP()) {
				svmRes = snpResults.get(vline.getId("_"));
				if(svmRes == null) {
					throw new Exception(vline.getId("_") + " not found in results!");
				}
				keep = svmRes.isKeep();
			} else {
				svmRes = indelResults.get(vline.getId("_"));
				if(svmRes == null) {
					throw new Exception(vline.getId("_") + " not found in results!");
				}
				keep = svmRes.isKeep();
			}
			
			if(!keep) {
				
				vline.setFilter("FILTER");
			} else {
				
				vline.setFilter("PASS");
			}
			vline.setInfo(vline.getInfo() + ";SVM_PROBABILITY=" + svmRes.getProbabilityOfCorrect() + ";SVM_POSTERIOR=" + svmRes.getPosterior() + ";PASS_EXAC=" + svmRes.getPassExac() + ";PASS_1KG="+svmRes.getPass1kg() + ";CONSENSUS_CALL=" + svmRes.getConsensus());
			writer.write(vline.toString());
			writer.write("\n");
		
		}
		
		writer.close();
		
	}
	
	@Override
	public void setVcf(String vcf) {
		this.vcf = vcf;

	}


	@Override
	public void setSnpFilter(String snpFilter) {
		this.snpFilter = snpFilter;
	}
	

	private HashMap<String,SVMResult> parseResults(String file) throws Exception {
		HashMap<String,SVMResult> result = new HashMap<String,SVMResult>();
		
		List<String> lines = FileUtils.readLines(new File(file));
		for(String line : lines) {
			if(line.startsWith("KEY")) {
				//its a header
				continue;
			}
			logger.info(line);
			SVMResult svmRes = SVMResult.getSVMResultFromLine(line);
			result.put(svmRes.getKey(), svmRes);
		}
		
		return result;
	}

	@Override
	public void setIndelFilter(String indelFilter) {
		this.indelFilter = indelFilter;
	}
	
	public ApplicationProperties getApplicationProperties() {
		return applicationProperties;
	}

	public void setApplicationProperties(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}
	

}
