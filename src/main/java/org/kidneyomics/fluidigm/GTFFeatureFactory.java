package org.kidneyomics.fluidigm;

import java.util.List;

import org.biojava.nbio.genome.parsers.gff.Feature;
import org.biojava.nbio.genome.parsers.gff.FeatureList;
import org.biojava.nbio.genome.parsers.gff.Location;

public class GTFFeatureFactory {
	
	public static Feature createFeature(String line) {
		return createFeature(line,0);
	}
	
	public static Feature createFeature(String line, int pad) {
		String[] cols = line.split("\t");
		if(cols.length != 9) {
			throw new IllegalArgumentException("Incorrect number of columns. Found " + cols.length);
		}
		String seqname = cols[0];
		String source = cols[1];
		String type = cols[2];
		char strand = cols[6].charAt(0);
		Location location = Location.fromBio(Integer.parseInt(cols[3]) - pad, Integer.parseInt(cols[4]) + pad, strand);
		
		Double score = null;
		if(cols[5].equalsIgnoreCase(".")) {
			score = Double.MIN_VALUE;
		} else {
			score = Double.parseDouble(cols[5]);
		}
		
		Integer frame = null;
		
		if(cols[7].equals(".")) {
			frame = Integer.MIN_VALUE;
		} else {
			frame = Integer.parseInt(cols[7]);
		}
		
		String attributes = cols[8];
		//return new GTFFeature(seqname, source, type, location, score, strand, frame, attributes);
		return new Feature(seqname, source, type, location, score, frame, attributes);
	}
	
	public static FeatureList createFeatureList(List<String> lines) {
		FeatureList list = new FeatureList();
		for(String line : lines) {
			list.add(createFeature(line));
		}
		return list;
	}
	
}
