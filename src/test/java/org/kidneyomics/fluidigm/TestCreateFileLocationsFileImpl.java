package org.kidneyomics.fluidigm;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestCreateFileLocationsFileImpl {

	private static ApplicationContext context;
	private static CreateLocationsFileImpl service;
	@BeforeClass
	public static void start() {
		context = new ClassPathXmlApplicationContext("spring/applicationContext.xml");
		service = (CreateLocationsFileImpl) context.getBean("createLocationsFile",CreateLocationsFileImpl.class);
		
	}
	
	//@Test
	public void test() throws Exception {
		
		Collection<String> genes = new LinkedList<String>();
		
		genes.add("CD2AP");
		genes.add("ACTN4");
		genes.add("TRPC6");
		genes.add("LMX1B");
		genes.add("CFH");
		genes.add("INF2");
		genes.add("CUBN");
		genes.add("SMARCAL1");
		genes.add("PTPRO");
		genes.add("ITGA3");
		genes.add("PLCE1");
		genes.add("NPHS1");
		genes.add("SCARB2");
		genes.add("MYO1E");
		genes.add("COQ6");
		genes.add("PDSS2");
		genes.add("COQ2");
		genes.add("LAMB2");
		genes.add("WT1");
		genes.add("NPHS2");
		genes.add("NEIL1");
		
		service.writeLocations(genes, "/tmp/ns.intervals","exon");
		
	}
	
	
	//@Test
	public void test2() throws Exception {
		
		Collection<String> genes = new LinkedList<String>();
		
		genes.add("CD2AP");
		genes.add("ACTN4");
		genes.add("TRPC6");
		genes.add("LMX1B");
		genes.add("CFH");
		genes.add("INF2");
		genes.add("CUBN");
		genes.add("SMARCAL1");
		genes.add("PTPRO");
		genes.add("ITGA3");
		genes.add("PLCE1");
		genes.add("NPHS1");
		genes.add("SCARB2");
		genes.add("MYO1E");
		genes.add("COQ6");
		genes.add("PDSS2");
		genes.add("COQ2");
		genes.add("LAMB2");
		genes.add("WT1");
		genes.add("NPHS2");
		genes.add("NEIL1");
		
		service.setOutBed(true);
		service.writeLocations(genes, "/tmp/ns.intervals.bed","exon");
		
	}
	
	
	//@Test
	public void test3() throws Exception {
		
		Collection<String> genes = new LinkedList<String>();
		

		service.setOutBed(false);
		service.writeLocations(genes, "/tmp/all.intervals","exon");
		
	}

}
