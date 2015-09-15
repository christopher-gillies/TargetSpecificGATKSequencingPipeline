package org.kidneyomics.fluidigm;

import java.util.Comparator;

public class DescendingDoubleComparator implements Comparator<Double> {

	@Override
	public int compare(Double o1, Double o2) {
		// TODO Auto-generated method stub
		return - o1.compareTo(o2);
	}

}
