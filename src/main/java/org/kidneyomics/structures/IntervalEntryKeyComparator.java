package org.kidneyomics.structures;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

public class IntervalEntryKeyComparator implements Comparator<Map.Entry<String, IntervalTree>> {

	@Override
	public int compare(Entry<String, IntervalTree> o1,
			Entry<String, IntervalTree> o2) {
		
		String key1 = o1.getKey();
		String key2 = o2.getKey();
	
		return CHR_TO_INT(key1).compareTo(CHR_TO_INT(key2));
	}
	
	
	public static Integer CHR_TO_INT(String chr) {
		if(chr.equalsIgnoreCase("x")) {
			return 23;
		} else if(chr.equalsIgnoreCase("y")) {
			return 24;
		} else if(chr.equalsIgnoreCase("m")) {
			return 25;
		} else if(chr.equalsIgnoreCase("mt")) {
			return 25;
		} else {
			return Integer.parseInt(chr);
		}
	}

}
