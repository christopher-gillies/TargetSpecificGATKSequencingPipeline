package org.kidneyomics.fluidigm;

import org.kidneyomics.util.StringUtil;
import org.kidneyomics.vcf.GenotypeField;
import org.kidneyomics.vcf.GenotypeFieldParser;

public class GT_DS_GPFieldParser implements GenotypeFieldParser {
	

	/**
	 * 
	 * FORMAT GT:DS:GP Example 0/0:334:255:0,255,255
	 * 
	 * Example 0/1:6:25:28,0,62
	 * 
	 * */


	@Override
	public GenotypeField parse(String data) {
		GenotypeField result = new GenotypeField();
		if (StringUtil.isNullOrEmpty(data)) {
			throw new IllegalArgumentException(StringUtil.isNullOrEmptyMessage);
		}
		
		if(data.startsWith("./.") || data.startsWith(".\\.")) {
			result.setMissing();
			return result;
		}
		
		String[] cols = data.split(":");

		if (cols.length < 3) {
			throw new IllegalArgumentException("Input not formatted correctly");
		}

		if (cols[0].substring(0, 1) != ".") {
			result.getAlleles()[0] = Integer.parseInt(cols[0].substring(0, 1));
			result.setPhased((cols[0].charAt(1) == '|') ? true : false);
			result.getAlleles()[1] = Integer.parseInt(cols[0].substring(2, 3));
			
			result.setGenotypeDosage(Float.parseFloat(cols[1]));


			String[] genotypeLikelihoods = cols[2].split(",");
			for (String gp : genotypeLikelihoods) {
				result.getGenotypeLikelihoods().add(Float.parseFloat(gp));
			}
			
			return result;
		} else {
			return result;
		}

	}

}
