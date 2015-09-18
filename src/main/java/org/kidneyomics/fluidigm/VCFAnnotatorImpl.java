package org.kidneyomics.fluidigm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kidneyomics.vcf.GTFieldParser;
import org.kidneyomics.vcf.GenotypeField;
import org.kidneyomics.vcf.GenotypeFieldParser;
import org.kidneyomics.vcf.VCFFile;
import org.kidneyomics.vcf.VCFLine;
import org.kidneyomics.vcf.VCFFile.VCFLineIterator;
import org.springframework.stereotype.Component;

@Component("vcfAnnotatorImpl")
public class VCFAnnotatorImpl implements VCFAnnotator {

	Logger logger = Logger.getLogger(VCFAnnotatorImpl.class);
	
	@Override
	public void annotate(String in, String out) throws IOException {
		
		VCFFile vfile = new VCFFile();
		VCFLineIterator iter = vfile.iterator(in);
		Writer writer = new BufferedWriter( new FileWriter(new File(out)));
		
		//CALLRATE + QD + RP_RANKSUM + MEAN_ALLELE_BALANCE  + INBREEDING + NORMALIZED_ALLELE_DOSAGE_TEST  + MQ_RANKSUM  + BASEQ_RANKSUM + MEAN_ALT_DEPTH
		List<String> headerLines = iter.getHeaderLines();
		headerLines.add(1, "##INFO=<ID=CALLRATE,Number=1,Type=Float,Description=\"The proportion of samples called at this site\">");
		headerLines.add(1, "##INFO=<ID=ALLELE_DOSAGE_TEST,Number=1,Type=Float,Description=\"The probability that this site does not follow the allele balance of 50% across heterozygote genotypes. This is PHRED scaled\">");
		headerLines.add(1, "##INFO=<ID=NORMALIZED_ALLELE_DOSAGE_TEST,Number=1,Type=Float,Description=\"The probability that this site does not follow the allele balance of 50% across heterozygote genotypes. This is PHRED scaled but normalized by the mean alternative allele depth\">");
		headerLines.add(1, "##INFO=<ID=MEAN_ALT_DEPTH,Number=1,Type=Float,Description=\"The mean number of alternative allele read depth across heterozygotes\">");
		headerLines.add(1, "##INFO=<ID=MEAN_ALLELE_BALANCE,Number=1,Type=Float,Description=\"The mean allele balance across heterozygote sites\">");
		
		
		writer.write(StringUtils.join(headerLines, "\n"));
		writer.write("\n");
		
		
		GenotypeFieldParser gtfp = new GT_AD_DP_GQFieldParser();
		
		while(iter.hasNext()) {
			VCFLine vline = iter.next();
			
			List<GenotypeField> gts = null;
			try {
				gts = vline.parseGenotypeFieldBySampleIds(gtfp, vline.getSampleIds());
			} catch(Exception e) {
				logger.info("Switching to GT only parser");
				
				gtfp = new GTFieldParser();
				gts = vline.parseGenotypeFieldBySampleIds(gtfp, vline.getSampleIds());
			}
			
			FeatureCalculator featureCalculator = new SVMFeatureCalculator();
			featureCalculator.calculate(gts, gtfp.getClass().equals(GT_AD_DP_GQFieldParser.class));
			String alleleBalance = featureCalculator.getMeanAlleleBalance();
			String meanAltDepth = featureCalculator.getMeanAltDepth();
			String alleleDosagePhred = featureCalculator.getAlleleDosageTest();
			String normalizedAlleleDosagePhred = featureCalculator.getNormalizedAlleleDosageTest();
			double callrate = featureCalculator.getCallRate();
			
			StringBuilder sb = new StringBuilder();
			sb.append(vline.getInfo());
			
			sb.append(";");
			sb.append("CALLRATE=");
			sb.append(callrate);
			
			if(!alleleDosagePhred.equals("NA")) {
				sb.append(";");
				sb.append("ALLELE_DOSAGE_TEST=");
				sb.append(alleleDosagePhred);
			}
			
			if(!normalizedAlleleDosagePhred.equals("NA")) {
				sb.append(";");
				sb.append("NORMALIZED_ALLELE_DOSAGE_TEST=");
				sb.append(normalizedAlleleDosagePhred);
			}
			
			if(!meanAltDepth.equals("NA")) {
				sb.append(";");
				sb.append("MEAN_ALT_DEPTH=");
				sb.append(meanAltDepth);
			}
			
			if(!alleleBalance.equals("NA")) {
				sb.append(";");
				sb.append("MEAN_ALLELE_BALANCE=");
				sb.append(alleleBalance);
			}
			


			
			vline.setInfo(sb.toString());
			writer.write(vline.toString());
			writer.write("\n");
		
		}
		
		writer.close();
		
	}

}
