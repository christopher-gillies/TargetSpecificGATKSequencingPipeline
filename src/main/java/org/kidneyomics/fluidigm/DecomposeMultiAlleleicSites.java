package org.kidneyomics.fluidigm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kidneyomics.vcf.GenotypeField;
import org.kidneyomics.vcf.GenotypeFieldParser;
import org.kidneyomics.vcf.VCFFile;
import org.kidneyomics.vcf.VCFLine;
import org.kidneyomics.vcf.VCFFile.VCFLineIterator;
import org.springframework.stereotype.Component;

@Component("decomposeMultiAlleleicSites")
public class DecomposeMultiAlleleicSites {
	
	
	public void decompose(String vcf, String out) throws Exception {
		
		VCFFile vfile = new VCFFile();
		VCFLineIterator iter = vfile.iterator(vcf);
		
		
		GenotypeFieldParser gtfp = new GT_AD_DP_GQFieldParser();
		
		Writer writer = new BufferedWriter( new FileWriter(new File(out)));
		
		List<String> headerLines = iter.getHeaderLines();
		headerLines.add(1, "##INFO=<ID=OLD_MULTIALLELIC,Number=1,Type=String,Description=\"The old multiallelic site\">");
		
		
		writer.write(StringUtils.join(headerLines, "\n"));
		writer.write("\n");
		
		while(iter.hasNext()) {
			VCFLine vline = iter.next();
			
			if(!vline.isMultiAllelic()) {
				writer.write(vline.toString());
				writer.write("\n");
			} else {
				List<VCFLine> newSites = createMultialleleSites(vline, gtfp);
				for(VCFLine newVline : newSites) {
					writer.write(newVline.toString());
					writer.write("\n");
				}
			}

		}
		iter.close();
		writer.close();
	}
	
	public List<VCFLine> createMultialleleSites(VCFLine vline, GenotypeFieldParser gtfp) throws Exception {
		List<VCFLine> list = new LinkedList<VCFLine>();
		String alts[] = vline.getAlt().split(",");
		//For each alt code
		for(int i = 0; i < alts.length; i++) {
			int altCode = i + 1;
			VCFLine newVline = new VCFLine(vline.toString(),vline.getSampleIds());
			//Set new alt
			newVline.setAlt(alts[i]);
			//recode genotypes
			List<GenotypeField> gts = newVline.parseGenotypeFieldBySampleIds(gtfp, vline.getSampleIds());
			for(GenotypeField gt : gts) {
				//make sure it isnt missing
				if(!gt.getIsMissing()) {
					int alleles[] = gt.getAlleles();
					/*
					 * if it matches the new alt code then set it to be one
					 * otherwise set it to be reference
					 */
					if(alleles[0] == altCode) {
						alleles[0] = 1;
					} else {
						alleles[0] = 0;
					}
					
					if(alleles[1] == altCode) {
						alleles[1] = 1;
					} else {
						alleles[1] = 0;
					}
				}
			}
			//update genotype fields
			newVline.updateGenotypeFields(gts, vline.getSampleIds(), "GT:AD:DP:GQ");
			//update statistics
			VCFStatisticUpdater.updateStatisticsAndRecodeMultiallelelicSites(newVline, gtfp);
			
			//update info=
			newVline.setInfo(newVline.getInfo() + ";OLD_MULTIALLELIC=" + vline.getId("_"));
			list.add(newVline);
		}
		return list;
	}
}
