package org.kidneyomics.fluidigm;

import org.kidneyomics.util.StringUtil;
import org.kidneyomics.vcf.GenotypeField;
import org.kidneyomics.vcf.GenotypeFieldParser;

public class GT_DP_GQ_PLFieldParser implements GenotypeFieldParser {
	

	/**
	 * 
	 * FORMAT GT:DP:GQ:PL Example 0/0:334:255:0,255,255
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

		if (cols.length < 4) {
			throw new IllegalArgumentException("Input not formatted correctly");
		}

		if (cols[0].substring(0, 1) != ".") {
			result.getAlleles()[0] = Integer.parseInt(cols[0].substring(0, 1));
			result.setPhased((cols[0].charAt(1) == '|') ? true : false);
			result.getAlleles()[1] = Integer.parseInt(cols[0].substring(2, 3));
			
			result.setDepth(Integer.parseInt(cols[1]));

			result.setGenotypeQuality(Integer.parseInt(cols[2]));

			String[] phredScores = cols[3].split(",");
			for (String pl : phredScores) {
				result.getPhredScaledGenotypeLikelihoods().add(Integer.parseInt(pl));
			}
			
			return result;
		} else {
			return result;
		}

	}

}
