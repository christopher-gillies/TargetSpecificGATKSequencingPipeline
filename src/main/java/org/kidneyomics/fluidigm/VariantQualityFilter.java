package org.kidneyomics.fluidigm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sf.samtools.util.CloseableIterator;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kidneyomics.util.StringUtil;
import org.kidneyomics.vcf.Allele;
import org.kidneyomics.vcf.AlleleSet;
import org.kidneyomics.vcf.GenotypeField;
import org.kidneyomics.vcf.GenotypeFieldParser;
import org.kidneyomics.vcf.RegexStringCaptureService;
import org.kidneyomics.vcf.VCFFile;
import org.kidneyomics.vcf.VCFFile.VCFLineIterator;
import org.kidneyomics.vcf.VCFLine;
import org.kidneyomics.vcf.ValueCaptureService;

public class VariantQualityFilter implements Filter {

	Logger logger = Logger.getLogger(VariantQualityFilter.class);
	
	private String vcf;
	private String snpFilter;
	private String indelFilter;
	
	ApplicationProperties applicationProperties;
	
	@Override
	public void filter(String out) throws Exception {
		
		
		logger.info("snpFilter:\n" + snpFilter);
		logger.info("indelFilter:\n" + indelFilter);
		JexlEngine jexl = new JexlEngine();
		
        Expression snpExpression = jexl.createExpression( snpFilter );
        Expression indelExpression = jexl.createExpression( indelFilter );		
        		
		VCFFile vfile = new VCFFile();
		
		
		VCFLineIterator iter = vfile.iterator(vcf);
		Writer writer = new BufferedWriter( new FileWriter(new File(out)));
		
		
		writer.write(StringUtils.join(iter.getHeaderLines(), "\n"));
		writer.write("\n");
		
		
		GenotypeFieldParser gtfp = new GT_AD_DP_GQFieldParser();
		
		//ValueCaptureService<String> acService = new RegexStringCaptureService("AC=([^;]+)");
		//ValueCaptureService<String> anService = new RegexStringCaptureService("AN=([^;]+)");
		
		//int count = 0;
		while(iter.hasNext()) {
			VCFLine vline = iter.next();
			
			
			
			boolean isSnp = vline.isSNP();
			//logger.info("**** "+vline.getId(":") + " **** ");
			//logger.info("IS_SNP " + vline.isSNP());
			List<GenotypeField> gts = vline.parseGenotypeFieldBySampleIds(gtfp, vline.getSampleIds());
			for(GenotypeField gt : gts) {
				if(gt.getIsMissing()) {
					continue;
				}
				
				int dp = gt.getDepth();
				int gq = gt.getGenotypeQuality();
				int altAd = AlleleBalanceAndAltDepthCalculator.altDepth(gt);
				int refAd = AlleleBalanceAndAltDepthCalculator.refDepth(gt);
				float ab = AlleleBalanceAndAltDepthCalculator.alleleBalance(gt);
				
				double adtest = 0;
				if(altAd + refAd > 0) {
					adtest = AlleleDosageCalculator.getPhredScaledPvalue(refAd, altAd);
				}
				
				
				int alleles[] = gt.getAlleles();
				boolean isHet = alleles[0] != alleles[1];
				boolean isHomRef = (alleles[0] == alleles[1]) && (alleles[0] == 0);
				boolean isHomAlt = (alleles[0] == alleles[1]) && (alleles[0] != 0);
				
				JexlContext context = new MapContext();
				context.set("IS_HET", isHet);
				context.set("IS_HOM_REF", isHomRef);
				context.set("IS_HOM_ALT", isHomAlt);
				context.set("DP", dp);
				context.set("GQ", gq);
				context.set("AB", ab);
				context.set("ALT_DP", altAd);
				context.set("AD_TEST", adtest);
				
				//logger.info(gt.toString("GT:AD:DP:GQ"));
				boolean result = false;
				if(isSnp) {

					result = (Boolean) snpExpression.evaluate(context);
					//logger.info(result);
					if(result) {
						//logger.info("set missing snp GT:AD:DP:GQ "  +gt.toString("GT:AD:DP:GQ"));
						gt.setMissing();
					}
				} else {
					result = (Boolean) indelExpression.evaluate(context);
					//logger.info(result);
					if(result) {
						//logger.info("set missing indel GT:AD:DP:GQ "  +gt.toString("GT:AD:DP:GQ"));
						gt.setMissing();
					}
				}
				
				/*
				if((isHet || isHomAlt)) {
					logger.info("**Sample: " + gt.getSampleId() + "**");
					logger.info("IS_HET: " + isHet);
					logger.info("IS_HOM_REF: " + isHomRef);
					logger.info("IS_HOM_ALT: " + isHomAlt);
					logger.info("AD " + altAd);
					logger.info("AB " + ab);
					logger.info("Filter? "+  result);
				}
				*/
			}
			//end gt loop
			
			//Update genotypes
			vline.setFormat("GT:AD:DP:GQ");
			vline.updateGenotypeFields(gts, vline.getSampleIds(), "GT:AD:DP:GQ");
			
			//Update VCFLine Statistics
			VCFStatisticUpdater.updateStatisticsAndRecodeMultiallelelicSites(vline, gtfp);
			

			writer.write(vline.toString());
			writer.write("\n");
			//++count;
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
