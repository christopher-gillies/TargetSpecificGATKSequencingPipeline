package org.kidneyomics.fluidigm;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.kidneyomics.util.StringUtil;
import org.kidneyomics.vcf.AlleleSet;
import org.kidneyomics.vcf.GenotypeField;
import org.kidneyomics.vcf.GenotypeFieldParser;
import org.kidneyomics.vcf.VCFLine;

public class VCFStatisticUpdater {

	public static Logger logger = Logger.getLogger(VCFStatisticUpdater.class);
	
	public static void updateStatisticsAndRecodeMultiallelelicSites(VCFLine vline, GenotypeFieldParser gtfp) throws Exception {
		//Update allele frequencies
		vline.calculateStatistics(gtfp);
		AlleleSet as = vline.getAlleleSet();
		
		/*
		 * remove multiallelic sites that are no longer present...
		 */
		if(vline.isMultiAllelic()) {
			logger.info("Multiallele modification...");
			logger.info(vline.getId(":"));
			String altAlleles[] = vline.getAlt().split(",");
			
			//find all remaining alt alleles
			HashMap<String,Integer> found = new HashMap<String,Integer>();
			//subtract 1 because of reference allele
			for(int allele : as.getAlleleCodes()) {
				if(allele != 0) {
					found.put(altAlleles[allele - 1],allele);
				}
			}
			//sort them
			List<Integer> sortedRemainingNonRefAlleles = new LinkedList<Integer>();
			sortedRemainingNonRefAlleles.addAll(found.values());
			Collections.sort(sortedRemainingNonRefAlleles);
			logger.info(sortedRemainingNonRefAlleles);
			
			
			List<String> newAltAlleles = new LinkedList<String>();
			//save remaining alt alleles
			for(int a : sortedRemainingNonRefAlleles) {
				newAltAlleles.add(altAlleles[a-1]);
			}
			//update alt field
			
			logger.info("old alt: " + vline.getAlt());
			if(newAltAlleles.size() > 0) {
				vline.setAlt(StringUtil.join(",", (List) newAltAlleles));
			} else {
				vline.setAlt(".");
			}
			logger.info("new alt: " + vline.getAlt());
			
			
			//Recode
			HashMap<Integer,Integer> recodingMap = new HashMap<Integer,Integer>();
			recodingMap.put(0, 0);
			int currentCode = 1;
			//Assign new codes
			for(int a : sortedRemainingNonRefAlleles) {
				recodingMap.put(a, currentCode++);
			}
			logger.info(recodingMap);
			//Update Codes
			List<GenotypeField> newGts = vline.parseGenotypeFieldBySampleIds(gtfp, vline.getSampleIds());
			for(GenotypeField gt : newGts) {
				if(!gt.getIsMissing()) {
					int alleles[] = gt.getAlleles();
					
					List<Integer> oldAlleleDepths = gt.getAllelicDepths();
					List<Integer> newAllelDepths = new LinkedList<Integer>();
					
					//add reference allele depth
					newAllelDepths.add( oldAlleleDepths.get(0));
					//add all other allele depths
					for(int a : sortedRemainingNonRefAlleles) {
						newAllelDepths.add( oldAlleleDepths.get(a));
					}
					//update allele depths
					gt.setAllelicDepths(newAllelDepths);
					
					if(recodingMap.containsKey(alleles[0])) {
						alleles[0] = recodingMap.get(alleles[0]);
					} else {
						throw new Exception("gt map invalid!");
					}
					
					if(recodingMap.containsKey(alleles[1])) {
						alleles[1] = recodingMap.get(alleles[1]);
					} else {
						throw new Exception("gt map invalid!");
					}
				}
			}
			//update genotype fields to reflect new state
			vline.updateGenotypeFields(newGts, vline.getSampleIds(), "GT:AD:DP:GQ");
			
		}
		
		
		as.updateAf();
		int an = as.getTotalNumberOfAlleles();
		Set<Integer> alleleCodes = as.getAlleleCodes();
		List<Integer> acs = new LinkedList<Integer>();
		List<Float> afs = new LinkedList<Float>();
		for(int allele : alleleCodes) {
			if(allele != 0) {
				acs.add(as.getAllele(allele).getCount());
				afs.add(as.getAllele(allele).getFreq());
			}
		}
		vline.updateAN(an);
		
		//logger.info(vline.getId(":"));
		//logger.info(acService.getValue(vline.getInfo()));
		vline.updateAC(acs);
		//logger.info(acService.getValue(vline.getInfo()));
		vline.updateAF(afs);
		
		//filter variants that have ac 0
		boolean hasAcGreaterThan0 = false;
		for(int ac : acs) {
			if(ac > 0) {
				hasAcGreaterThan0 = true;
			}
		}
		if(!hasAcGreaterThan0) {
			vline.setFilter("FILTER");
			logger.info("********set ac 0 " + vline.getId(":"));
			vline.setInfo(vline.getInfo() + ";AC_0_FILTER");
		}
	}
	
	private static void printField(StringBuilder sb, String delimiter, Object value) {
		if(value == null) {
			sb.append(".");
		} else {
			sb.append(value.toString());
		}
		sb.append(delimiter);
	}
	
	public static String makeNewHeader(List<String> sampleIds) {
		StringBuilder sb = new StringBuilder();
		printField(sb, "\t", "#CHROM");
		printField(sb, "\t", "POS");
		printField(sb, "\t", "ID");
		printField(sb, "\t", "REF");
		printField(sb, "\t",  "ALT");
		printField(sb, "\t", "QUAL");
		printField(sb, "\t", "FILTER");
		printField(sb, "\t", "INFO");
		if(sampleIds.size() > 0) {
			printField(sb, "\t", "FORMAT");
			for (String sample : sampleIds) {
				printField(sb, "\t", sample);
			}
		}
		// Remove Last Tab
		sb.delete(sb.length() - 1, sb.length());
		return sb.toString();
	}
}
