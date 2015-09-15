package org.kidneyomics.fluidigm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.sf.samtools.util.CloseableIterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kidneyomics.util.NotSupportedException;
import org.kidneyomics.vcf.GTFieldParser;
import org.kidneyomics.vcf.GenotypeField;
import org.kidneyomics.vcf.GenotypeFieldParser;
import org.kidneyomics.vcf.GenotypeFieldParserFactory;
import org.kidneyomics.vcf.RegexStringCaptureService;
import org.kidneyomics.vcf.VCFFile;
import org.kidneyomics.vcf.VCFFile.VCFLineIterator;
import org.kidneyomics.vcf.VCFLine;
import org.kidneyomics.vcf.ValueCaptureService;

public class GATKVariantInfoCollector implements InfoCollector {

	Logger logger = Logger.getLogger(GATKVariantInfoCollector.class);
	
	private String vcf;
	
	
	ApplicationProperties applicationProperties;
	
	@Override
	public void collectInfo(String fileOut) throws IOException, NotSupportedException {
		
		HashSet<String> indels = new HashSet<String>();
		/*
		 * Read gold indels
		 */
		int index = 1;
		VCFFile goldIndels = new VCFFile();
		CloseableIterator<VCFLine> indelIter = goldIndels.iterator(applicationProperties.getGoldIndels());
		while(indelIter.hasNext()) {
			VCFLine vline = indelIter.next();
			indels.add(vline.getChrom() + vline.getPos());
			if(index % 10000 == 0) {
				logger.info("Reading indel " + vline.getId(":"));
			}
			index++;
		}
		indelIter.close();
		
		VCFFile vfile = new VCFFile();
		
		
		VCFLineIterator iter = vfile.iterator(vcf);
		
		GenotypeFieldParser gtfp = new GT_AD_DP_GQFieldParser();

		ValueCaptureService<String> acService = new RegexStringCaptureService("AC=([^;]+)");
		ValueCaptureService<String> anService = new RegexStringCaptureService("AN=([^;]+)");
		
		
		//StringBuilder sb = new StringBuilder();
		
		//Write file
		File out = new File(fileOut);
		FileWriter fw = new FileWriter(out);
		BufferedWriter writer = new BufferedWriter(fw);
		
		writer.append("CHR_POS_ID\tCHR_POS\tKEY\tIS_SNP\tIN_DBSNP\tGOLD_INDEL\tIN_DBSNP_OR_GOLD_INDEL\tFILTER\tSAMPLE\tCALLRATE\tAF\tGT\tGQ\tDP\tREF_DEPTH\tALT_DEPTH\tALLELE_BALANCE\tALLELE_DOSAGE_TEST\n");
		while(iter.hasNext()) {
			VCFLine vline = iter.next();
			String acS = acService.getValue(vline.getInfo());
			int ac;
			if(acS.contains(",")) {
				String split[] = acS.split(",");
				if(split.length > 1) {
					ac = Integer.parseInt(split[1]);
				} else {
					ac = Integer.parseInt(split[0]);
				}
			} else {
				ac = Integer.parseInt(acS);
			}
			
			
			String anS = anService.getValue(vline.getInfo());
			int an = Integer.parseInt(anS);
			
			boolean inDbsnp = !vline.getId().equals(".");
			boolean isGoldIndel = indels.contains(vline.getChrom() + vline.getPos());
			boolean inDbsnpOrIsGoldIndel = inDbsnp || isGoldIndel;

			String filter = vline.getFilter();

			List<GenotypeField> gts = null;
			try {
				gts = vline.parseGenotypeFieldBySampleIds(gtfp, vline.getSampleIds());
			} catch(Exception e) {
				logger.info("Switching to GT only parser");
				
				gtfp = new GTFieldParser();
				gts = vline.parseGenotypeFieldBySampleIds(gtfp, vline.getSampleIds());
			}
			/*
			 * CALL RATE
			 */
			int nonMissingCount = 0;
			for(GenotypeField gt : gts) {
				if(!gt.getIsMissing()) {
					nonMissingCount++;
				}
			}
			
			double callrate = nonMissingCount / ((double) gts.size());
			
			for(GenotypeField gt : gts) {
				
				String sample = gt.getSampleId();
				
				writer.append(vline.getChrom() + ":" + vline.getPos() + ":" + sample);
				writer.append("\t");
				
				writer.append(vline.getChrom() + ":" + vline.getPos());
				writer.append("\t");
				
				writer.append(vline.getId("_"));
				writer.append("\t");
				
				writer.append(vline.isSNP() ? "1" : "0");
				writer.append("\t");
				
				writer.append( inDbsnp ? "1" : "0");
				writer.append("\t");
				
				writer.append( isGoldIndel ? "1" : "0");
				writer.append("\t");
				
				writer.append( inDbsnpOrIsGoldIndel ? "1" : "0");
				writer.append("\t");
				
				writer.append( filter);
				writer.append("\t");
				
				writer.append(sample);
				writer.append("\t");
				
				writer.append( Double.toString(callrate));
				writer.append("\t");
				
				writer.append( Double.toString(ac / (double) an));
				writer.append("\t");
				
				/*
				 * genotype
				 */
				if(gt.getIsMissing()) {
					
					//GT
					writer.append( "NA" );
					writer.append("\t");
					
					//GQ
					writer.append( "NA" );
					writer.append("\t");
					
					//DP
					writer.append( "NA" );
					writer.append("\t");
					
					//REF_DEPTH
					writer.append( "NA" );
					writer.append("\t");
					
					//ALT_DEPTH
					writer.append( "NA" );
					writer.append("\t");
					
					//ALLELE BALANCE
					writer.append( "NA" );
					writer.append("\t");
					
					//ALLELE DOSAGE TEST
					writer.append( "NA");
					writer.append("\t");
				} else {
					
					//GT
					int alleles[] = gt.getAlleles();
					if(alleles[0] == alleles[1] && alleles[0] == 0) {
						writer.append( "HOM_REF" );
						writer.append("\t");
					} else if(alleles[0] == alleles[1] && alleles[0] != 0) {
						writer.append( "HOM_ALT" );
						writer.append("\t");
					} else {
						writer.append( "HET" );
						writer.append("\t");
					}
					
					if(gtfp.getClass().equals(GT_AD_DP_GQFieldParser.class)) {
						//GQ
						writer.append( Integer.toString(gt.getGenotypeQuality() ));
						writer.append("\t");
						
						//DP
						writer.append(Integer.toString( gt.getDepth() ));
						writer.append("\t");
						

						//ALLELE BALANCE
						
						
						int refAd = AlleleBalanceAndAltDepthCalculator.refDepth(gt);
						int altAd = AlleleBalanceAndAltDepthCalculator.altDepth(gt);
						
						//REF AD
						writer.append( Integer.toString(refAd ));
						writer.append("\t");
						
						//ALT AD
						writer.append( Integer.toString(altAd ));
						writer.append("\t");
						
						//ALLELE BALANCE
						writer.append( Double.toString( AlleleBalanceAndAltDepthCalculator.alleleBalance(gt) ));
						writer.append("\t");
						
						//ALLELE DOSAGE TEST
						if(altAd + refAd > 0) {
							writer.append( Double.toString( AlleleDosageCalculator.getPhredScaledPvalue(refAd, altAd) ) );
							writer.append("\t");
						} else {
							writer.append( "NA");
							writer.append("\t");
						}
					} else {
						//GQ
						writer.append( "NA");
						writer.append("\t");
						
						//DP
						writer.append( "NA" );
						writer.append("\t");
						
						
						//REF AD
						writer.append( "NA" );
						writer.append("\t");
						
						//ALT AD
						writer.append( "NA" );
						writer.append("\t");
						
						//ALLELE BALANCE
						writer.append( "NA");
						writer.append("\t");
						
						//ALLELE DOSAGE TEST
						writer.append( "NA");
						writer.append("\t");
					}
					
					
				}
				writer.append("\n");


				
			}
			

			
		}
		iter.close();
		
		//FileUtils.write(out,sb.toString());
		writer.close();
	}

	
	@Override
	public void setVcf(String vcf) {
		this.vcf = vcf;	
	}

	public ApplicationProperties getApplicationProperties() {
		return applicationProperties;
	}

	public void setApplicationProperties(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}
	
	
}
