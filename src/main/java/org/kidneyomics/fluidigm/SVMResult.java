package org.kidneyomics.fluidigm;

public class SVMResult {
	private String key;
	private float probabilityOfCorrect;
	private float posterior;
	private boolean keep;
	private String chrPos;
	private boolean inDbSnp;
	private String passExac;
	private String pass1kg;
	private String consensus;
	private String confirmed;
	
	public SVMResult() {

	}
	
	public static SVMResult getSVMResultFromLine(String line) throws Exception{
		
		SVMResult result = new SVMResult();
		if(line == null) {
			throw new Exception("line is null");
		}
		
		String cols[] = line.split("\t");
		
		if(cols.length != 10) {
			throw new Exception(line + "\nline is not formated correctly");
		}
		
		result.setKey(cols[0]);
		result.setProbabilityOfCorrect(Float.parseFloat(cols[1]));
		result.setPosterior(Float.parseFloat(cols[2]));
		result.setKeep(cols[3].equals("TRUE"));
		result.setChrPos(cols[4]);
		result.setInDbSnp(cols[5].equals("IN_DBSNP"));
		result.setPassExac(cols[6]);
		result.setPass1kg(cols[7]);
		result.setConsensus(cols[8]);
		result.setConfirmed(cols[9]);
		
		return result;
	}
	
	
	@Override
	public boolean equals(Object o) {
		SVMResult other =  (SVMResult) o;
		return this.getKey().equals(other.getKey());
	}
	
	@Override
	public int hashCode() {
		return 31 * this.getKey().hashCode() + 17;
	}
	
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public float getProbabilityOfCorrect() {
		return probabilityOfCorrect;
	}
	public void setProbabilityOfCorrect(float probabilityOfCorrect) {
		this.probabilityOfCorrect = probabilityOfCorrect;
	}
	public boolean isKeep() {
		return keep;
	}
	public void setKeep(boolean keep) {
		this.keep = keep;
	}
	public String getChrPos() {
		return chrPos;
	}
	public void setChrPos(String chrPos) {
		this.chrPos = chrPos;
	}
	public boolean isInDbSnp() {
		return inDbSnp;
	}
	public void setInDbSnp(boolean inDbSnp) {
		this.inDbSnp = inDbSnp;
	}
	public String getPassExac() {
		return passExac;
	}
	public void setPassExac(String passExac) {
		this.passExac = passExac;
	}
	public String getPass1kg() {
		return pass1kg;
	}
	public void setPass1kg(String pass1kg) {
		this.pass1kg = pass1kg;
	}
	public String getConsensus() {
		return consensus;
	}
	public void setConsensus(String consensus) {
		this.consensus = consensus;
	}
	public String getConfirmed() {
		return confirmed;
	}
	public void setConfirmed(String confirmed) {
		this.confirmed = confirmed;
	}

	public float getPosterior() {
		return posterior;
	}

	public void setPosterior(float posterior) {
		this.posterior = posterior;
	}
	
	
	
	
}
