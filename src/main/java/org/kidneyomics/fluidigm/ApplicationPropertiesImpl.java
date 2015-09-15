package org.kidneyomics.fluidigm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

import org.apache.log4j.Logger;


public class ApplicationPropertiesImpl implements ApplicationProperties {
	
	private Logger logger = Logger.getLogger(ApplicationPropertiesImpl.class);
	
	private String gatk;
	private String bwa;
	private String samtools;
	private String reference;
	private String picard;
	private String make;
	private String jvmSize;
	private String cutadapt;
	
	private String _1000GIndels;
	private String goldIndels;
	
	private String java;
	
	private String dbsnp;
	
	private String qplot;
	
	private String gencode;
	
	private String omniSites;
	private String _1000GHighConfSnps;
	private String hapmap33;
	
	private String exacSites;
	
	private String _1000GSites;
	
	@Override
	public String getGatk() {
		return gatk;
	}
	
	@Override
	public void setGatk(String gatk) {
		this.gatk = gatk;
	}
	
	@Override
	public String getBwa() {
		return bwa;
	}
	
	@Override
	public void setBwa(String bwa) {
		this.bwa = bwa;
	}
	
	@Override
	public String getSamtools() {
		return samtools;
	}
	
	@Override
	public void setSamtools(String samtools) {
		this.samtools = samtools;
	}
	
	@Override
	public String getReference() {
		return reference;
	}
	
	@Override
	public void setReference(String reference) {
		this.reference = reference;
	}
	
	@Override
	public String getPicard() {
		return picard;
	}
	
	@Override
	public void setPicard(String picard) {
		this.picard = picard;
	}
	
	@Override
	public String getMake() {
		return make;
	}
	
	@Override
	public void setMake(String make) {
		this.make = make;
	}

	@Override
	public String getJvmSize() {
		return this.jvmSize;
	}
	
	@Override
	public void setJvmSize(String jvmSize) {
		this.jvmSize = jvmSize;
	}

	@Override
	public String getCutadapt() {
		return cutadapt;
	}

	@Override
	public void setCutadapt(String cutadapt) {
		this.cutadapt = cutadapt;
	}

	@Override
	public String get_1000GIndels() {
		return _1000GIndels;
	}
	
	@Override
	public void set_1000GIndels(String _1000gIndels) {
		_1000GIndels = _1000gIndels;
	}

	@Override
	public String getGoldIndels() {
		return goldIndels;
	}

	@Override
	public void setGoldIndels(String goldIndels) {
		this.goldIndels = goldIndels;
	}

	@Override
	public String getJava() {
		return java;
	}

	@Override
	public void setJava(String java) {
		this.java = java;
	}

	@Override
	public String getDbsnp() {
		return dbsnp;
	}

	@Override
	public void setDbsnp(String dbsnp) {
		this.dbsnp = dbsnp;
	}

	@Override
	public String getQplot() {
		return qplot;
	}

	@Override
	public void setQplot(String qplot) {
		this.qplot = qplot;
	}

	@Override
	public String getGencode() {
		return gencode;
	}

	@Override
	public void setGencode(String gencode) {
		this.gencode = gencode;
	}

	@Override
	public String getOmniSites() {
		return omniSites;
	}

	@Override
	public void setOmniSites(String omniSites) {
		this.omniSites = omniSites;
	}

	@Override
	public String get_1000GHighConfSnps() {
		return _1000GHighConfSnps;
	}

	@Override
	public void set_1000GHighConfSnps(String _1000gHighConfSnps) {
		_1000GHighConfSnps = _1000gHighConfSnps;
	}

	@Override
	public String getHapmap33() {
		return hapmap33;
	}

	@Override
	public void setHapmap33(String hapmap33) {
		this.hapmap33 = hapmap33;
	}

	@Override
	public String getExacSites() {
		return exacSites;
	}

	@Override
	public void setExacSites(String exacSites) {
		this.exacSites = exacSites;
	}

	@Override
	public String get_1000GSites() {
		return _1000GSites;
	}

	@Override
	public void set_1000GSites(String _1000gSites) {
		_1000GSites = _1000gSites;
	}

	@Override
	public void readProperties(String file) throws Exception {
		File f = new File(file);
		Properties prop = new Properties();
		FileReader reader = new FileReader(f);
		prop.load(reader);
		
		Field[] allFields = this.getClass().getDeclaredFields();
		for (Field field : allFields) {
			String name = field.getName();
			if(name.equals("logger")) {
				continue;
			}
			
			String value = prop.getProperty(name);
			if(value != null) {
				field.set(this, value);
			} else {
				logger.info(name + " is not present... please set a value for this property");
			}
		}
	}

	
	
	
	
	
}
