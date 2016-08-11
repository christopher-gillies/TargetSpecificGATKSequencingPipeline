package org.kidneyomics.fluidigm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.biojava.nbio.genome.parsers.gff.FeatureI;
import org.biojava.nbio.genome.parsers.gff.FeatureList;
import org.biojava.nbio.genome.parsers.gff.GFF3Reader;
import org.kidneyomics.structures.IntervalTreeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("createLocationsFile")
public class CreateLocationsFileImpl {
	
	@Autowired
	ApplicationProperties applicationProperties;
	
	private boolean outBed = false;
	
	Logger logger = Logger.getLogger(CreateLocationsFileImpl.class);
	
	/*
	 * if no genes are input then just query all genes
	 */
	public void writeLocations(Collection<String> genes, String output, String field) throws Exception {
		writeLocations(genes,50,output, field);
	}
	
	public void writeLocations(Collection<String> genes, int pad, String output, String field) throws Exception {
		
		logger.info("Number of genes: " + genes.size());
		
		logger.info("Field to use: " + field);
		HashSet<String> genesSet = new HashSet<String>();
		HashSet<String> visitedGenes = new HashSet<String>();
		
		HashSet<String> locations = new HashSet<String>(400000);
		
		//StringBuilder sb = new StringBuilder();
		for(String gene : genes) {
			genesSet.add(gene);
		}
		//logger.info("Number of genes: " + genesSet.size());
		
		String filename = applicationProperties.getGencode();
		
		//FeatureList list = new FeatureList();
		
		
		// Open the file
		//8-11-2016
		//Allow gtf file to be in gzip format or not
		FileInputStream fstream = new FileInputStream(filename);
		InputStream is = fstream;
		if(filename.endsWith(".gz")) {
			GZIPInputStream gzipin = new GZIPInputStream(fstream);
			is = gzipin;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String line;
		int count = 0;
		try {
			//Read File Line By Line
			while ((line = br.readLine()) != null)   {
				if(line.startsWith("#")) {
					continue;
				}
				
				if(line.startsWith("chr")) {
					line = line.replaceFirst("chr", "");
				}
				
				FeatureI feature = GTFFeatureFactory.createFeature(line);
				FeatureI featurePadded = GTFFeatureFactory.createFeature(line,pad);
				
				if(!feature.type().equals(field)) {
					continue;
				}
				
				if(featurePadded.location().bioEnd() - feature.location().bioEnd() != pad) {
					throw new Exception("Padding failed!");
				}
				
				if(feature.location().bioStart() - featurePadded.location().bioStart() != pad) {
					throw new Exception("Padding failed!");
				}
				
				
				if(feature.hasAttribute("gene_name")) {
					String gene = feature.getAttribute("gene_name");
					
					if(genesSet.contains(gene) || genesSet.size() == 0) {
						visitedGenes.add(gene);
						if(!outBed) {
							String location = (featurePadded.seqname() + ":" + featurePadded.location().bioStart() + "-" + featurePadded.location().bioEnd());
							locations.add(location);
						} else {
							String location = (featurePadded.seqname() + "\t" + (featurePadded.location().bioStart() - 1) + "\t" + (featurePadded.location().bioEnd() + 1));
							locations.add(location);
						}
						//sb.append("\n");
						//logger.info(line);
						//logger.info(feature.seqname());
						//logger.info(feature.location().bioStart());
						//logger.info(feature.location().bioEnd());
						//logger.info(featurePadded.seqname());
						//logger.info(featurePadded.location().bioStart());
						//logger.info(featurePadded.location().bioEnd());
						
						//list.add(featurePadded);
					}
					
					if(count % 50000 == 0) {
						logger.info(count + " entries scanned");
					}
					
				} else {
					throw new Exception(line + " missing gene!");
				}
				count++;
				
			}
			
			
			if(visitedGenes.size() < genesSet.size()) {
				logger.info("Found Genes: " + visitedGenes.toString());
				logger.info("Expected Genes: " + genesSet.toString());
				for(String gene : genesSet) {
					if(!visitedGenes.contains(gene)) {
						logger.info(gene + " not found!");
					}
				}
				throw new Exception("Not all genes found!");
			}
			
		} catch(Exception e) {
			throw e;
		} finally {
			//Close the input stream
			br.close();
		}
		
		logger.info("Input " + genesSet.size());
		logger.info("Input " + genesSet);
		
		logger.info("Found " + visitedGenes.size());
		logger.info("Found " + visitedGenes);
		
		logger.info("Found Locations: " + locations.size());
		
		IntervalTreeSet treeSet = IntervalTreeSet.IntervalTreeSetFromCollection(locations);
		logger.info("Total coverage: " + treeSet.computeTotalIntervalCoverage());

		List<String> newLocations = treeSet.getIntervals();
		
		logger.info("Merged Locations: " + newLocations.size());
		
		File out = new File(output);
		FileUtils.writeLines(out, newLocations);
		

	}

	public boolean isOutBed() {
		return outBed;
	}

	public void setOutBed(boolean outBed) {
		this.outBed = outBed;
	}
	
	
	
}
