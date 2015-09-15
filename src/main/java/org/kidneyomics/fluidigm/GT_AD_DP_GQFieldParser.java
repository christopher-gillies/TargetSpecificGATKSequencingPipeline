package org.kidneyomics.fluidigm;

import org.kidneyomics.vcf.GenotypeFieldParser;
import org.kidneyomics.vcf.GenotypeField;
import org.kidneyomics.util.StringUtil;

/**
 * This class parses a genotype field with the following format GT:DP:GQ:PL
 * 
 * @author cgillies
 * 
 */
public class GT_AD_DP_GQFieldParser implements GenotypeFieldParser {

	/**
	 * 
	 * FORMAT GT:AD:DP:GQ Example 0/0:165,0:165:99
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

			String[] alleleDepths = cols[1].split(",");
			for (String ad : alleleDepths) {
				result.getAllelicDepths().add(
						Integer.parseInt(ad));
			}
			
			result.setDepth(Integer.parseInt(cols[2]));

			result.setGenotypeQuality(Integer.parseInt(cols[3]));


			return result;
		} else {
			return result;
		}

	}

}
