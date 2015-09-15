package org.kidneyomics.fluidigm;

import java.io.FileNotFoundException;


public interface ApplicationProperties {

	void readProperties(String file) throws Exception;
	
	String getGatk();

	void setGatk(String gatk);

	String getBwa();

	void setBwa(String bwa);

	String getSamtools();
	
	void setSamtools(String samtools);
	
	String getReference();
	
	void setReference(String reference);
	
	String getPicard();
	
	void setPicard(String picard);
	
	String getMake();
	 
	void setMake(String make);

	void setJvmSize(String jvmSize);

	String getJvmSize();

	String getCutadapt();

	void setCutadapt(String cutadapt);

	String get_1000GIndels();

	void set_1000GIndels(String _1000gIndels);

	String getGoldIndels();

	void setGoldIndels(String goldIndels);

	String getJava();

	void setJava(String java);

	String getDbsnp();

	void setDbsnp(String dbsnp);

	String getQplot();

	void setQplot(String qplot);

	String getGencode();

	void setGencode(String gencode);

	String getOmniSites();

	void setOmniSites(String omniSites);

	String get_1000GHighConfSnps();

	void set_1000GHighConfSnps(String _1000gHighConfSnps);

	String getHapmap33();

	void setHapmap33(String hapmap33);

	String getExacSites();

	void setExacSites(String exacSites);


	String get_1000GSites();

	void set_1000GSites(String _1000gSites);

	
}
