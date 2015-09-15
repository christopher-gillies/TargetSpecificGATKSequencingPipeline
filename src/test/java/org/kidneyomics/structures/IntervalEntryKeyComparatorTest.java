package org.kidneyomics.structures;

import static org.junit.Assert.*;

import java.util.Map.Entry;

import org.junit.Test;

public class IntervalEntryKeyComparatorTest {

	@Test
	public void test() {
		
		
		assertTrue(IntervalEntryKeyComparator.CHR_TO_INT("1") == 1);
		
		
		assertTrue(IntervalEntryKeyComparator.CHR_TO_INT("5") == 5);
		
		assertTrue(IntervalEntryKeyComparator.CHR_TO_INT("X").equals(23));
	}

}
