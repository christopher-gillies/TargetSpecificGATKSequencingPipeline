package org.kidneyomics.fluidigm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.samtools.util.CloseableIterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kidneyomics.util.NotSupportedException;
import org.kidneyomics.vcf.GTFieldParser;
import org.kidneyomics.vcf.GenotypeField;
import org.kidneyomics.vcf.GenotypeFieldParser;
import org.kidneyomics.vcf.GenotypeFieldParserFactory;
import org.kidneyomics.vcf.RegexStringCaptureService;
import org.kidneyomics.vcf.TabixQuery;
import org.kidneyomics.vcf.VCFFile;
import org.kidneyomics.vcf.VCFFile.VCFLineIterator;
import org.kidneyomics.vcf.VCFLine;
import org.kidneyomics.vcf.ValueCaptureService;

public class GATKSiteInfoCollector implements InfoCollector {

	Logger logger = Logger.getLogger(GATKSiteInfoCollector.class);
	
	private String vcf;
	
	private double minCallRate = 0.5;
	
	private int altAlleleDepth = 10;
	
	
	
	public int getAltAlleleDepth() {
		return altAlleleDepth;
	}

	public void setAltAlleleDepth(int altAlleleDepth) {
		this.altAlleleDepth = altAlleleDepth;
	}



	private boolean collectExacAnd1000G = false;
	
	
	public boolean isCollectExacAnd1000G() {
		return collectExacAnd1000G;
	}

	public void setCollectExacAnd1000G(boolean collectExacAnd1000G) {
		this.collectExacAnd1000G = collectExacAnd1000G;
	}
	
	

	public double getMinCallRate() {
		return minCallRate;
	}

	public void setMinCallRate(double minCallRate) {
		this.minCallRate = minCallRate;
	}



	ApplicationProperties applicationProperties;
	
	@Override
	public void collectInfo(String fileOut) throws Exception {
		
		logger.info("Reading VCF: "  + vcf);
		HashSet<String> indels = new HashSet<String>();
		/*
		 * Read gold indels
		 */
		int index = 1;
		VCFFile goldIndels = new VCFFile();
		VCFLineIterator indelIter = goldIndels.iterator(applicationProperties.getGoldIndels());
		while(indelIter.hasNext()) {
			VCFLine vline = indelIter.next();
			indels.add(vline.getChrom() + vline.getPos());
			if(index % 10000 == 0) {
				logger.info("Reading indel " + vline.getId(":"));
			}
			index++;
		}
		indelIter.close();
		
		/*
		 * Read Hapmap
		 */
		HashSet<String> hapmapSites = new HashSet<String>();
		index = 1;
		VCFFile hapmap = new VCFFile();
		VCFLineIterator hapmapIter = hapmap.iterator(applicationProperties.getHapmap33());
		while(hapmapIter.hasNext()) {
			VCFLine vline = hapmapIter.next();
			hapmapSites.add(vline.getId(":"));
			if(index % 10000 == 0) {
				logger.info("Reading hapmap site " + vline.getId(":"));
			}
			index++;
		}
		hapmapIter.close();
		
		
		/*
		 * Read exact site list
		 */
		
		String exacFile = applicationProperties.getExacSites();
		
		TabixQuery exacQuery = new TabixQuery(exacFile,true);
		
		String _1000GFile = applicationProperties.get_1000GSites();
		
		TabixQuery _1kgQuery = new TabixQuery(_1000GFile,true);
		
		VCFFile vfile = new VCFFile();
		
		
		VCFLineIterator iter = vfile.iterator(vcf);
		
		GenotypeFieldParser gtfp = new GT_AD_DP_GQFieldParser();
		ValueCaptureService<String> dpService = new RegexStringCaptureService("DP=([^;]+)");
		ValueCaptureService<String> qdService = new RegexStringCaptureService("QD=([^;]+)");
		ValueCaptureService<String> fsService = new RegexStringCaptureService("FS=([^;]+)");
		ValueCaptureService<String> mqService = new RegexStringCaptureService("MQ=([^;]+)");
		ValueCaptureService<String> mqRankSumService = new RegexStringCaptureService("MQRankSum=([^;]+)");
		ValueCaptureService<String> rpRankSumService = new RegexStringCaptureService("ReadPosRankSum=([^;]+)");
		ValueCaptureService<String> acService = new RegexStringCaptureService("AC=([^;]+)");
		ValueCaptureService<String> anService = new RegexStringCaptureService("AN=([^;]+)");
		ValueCaptureService<String> sorService = new RegexStringCaptureService("SOR=([^;]+)");
		ValueCaptureService<String> inbreedingService = new RegexStringCaptureService("InbreedingCoeff=([^;]+)");
		ValueCaptureService<String> clipplingRankSumService = new RegexStringCaptureService("ClippingRankSum=([^;]+)");
		ValueCaptureService<String> baseQRankSumService = new RegexStringCaptureService("BaseQRankSum=([^;]+)");
		ValueCaptureService<String> haplotypeScoreService = new RegexStringCaptureService("HaplotypeScore=([^;]+)");
		ValueCaptureService<String> svm1kgService = new RegexStringCaptureService("SVM=([^;]+)");
		ValueCaptureService<String> vqsrLodService = new RegexStringCaptureService("VQSLOD=([^;]+)");
		
		//Write file
		File out = new File(fileOut);
		FileWriter fw = new FileWriter(out);
		BufferedWriter sb = new BufferedWriter(fw);
		
		sb.append("CHR_POS\tKEY\tIS_SNP\tIN_DBSNP\tIN_HAPMAP\tGOLD_INDEL\tIN_DBSNP_OR_GOLD_INDEL\tPASS_EXAC\tVQSR_LOD\tPASS_1KG\tSVM_SCORE\tCONSENSUS\tPRIOR_TRUE\tPRIOR_FALSE\tFILTER\tAC\tAN\tDEPTH\tMEAN_ALT_DEPTH\tMEAN_ALLELE_BALANCE\tALLELE_DOSAGE_TEST\tNORMALIZED_ALLELE_DOSAGE_TEST\tQD\tFS\tMQ\tMQ_RANKSUM\tCLIPPING_RANKSUM\tRP_RANKSUM\tBASEQ_RANKSUM\tHAPLOTYPE_SCORE\tQUALITY\tCALLRATE\tSOR\tINBREEDING\n");
		while(iter.hasNext()) {
			VCFLine vline = iter.next();
			logger.info(vline.getId(":"));
			
			//Exac
			String passExac = "NA";
			double vqsrLod = 0.0;
			if(collectExacAnd1000G) {
				List<VCFLine> eLines = null;
				try {
					eLines = exacQuery.query(vline.toTabixQuery());
				} catch(Exception e) {
					eLines = new LinkedList<VCFLine>();
				}
				for(VCFLine eLine : eLines) {
					
					if(eLine != null) {
						if(vline.isSNP() && eLine.isSNP()) {
							vqsrLod = Double.parseDouble(vqsrLodService.getValue(eLine.getInfo()));
							if(vqsrLod >= 0) {
								passExac = "PASS";
							} else {
								passExac = "FILTER";
							}
							//if(eLine.getFilter().equals("PASS")) {
							//	passExac = "PASS";
							//} else {
							//	passExac = "FILTER";
							//}
						} else if(!vline.isSNP() && !eLine.isSNP()) {
							vqsrLod = Double.parseDouble(vqsrLodService.getValue(eLine.getInfo()));
							if(vqsrLod >= 0) {
								passExac = "PASS";
							} else {
								passExac = "FILTER";
							}
							//if(eLine.getFilter().equals("PASS")) {
							//	passExac = "PASS";
							//} else {
							//	passExac = "FILTER";
							//}
						}
						
						
						
					}
				}
			}
			
			//1000KG
			String pass1kg = "NA";
			double svmScore = 0.0;
			if(collectExacAnd1000G) {
				List<VCFLine> _1kgLines = null;
				//added try catch just in case chr not found in 1000G
				try {
					_1kgLines = _1kgQuery.query(vline.toTabixQuery());
				} catch(Exception e) {
					_1kgLines = new LinkedList<VCFLine>();
				}
				for(VCFLine _1kgLine : _1kgLines) {
					if(_1kgLine != null) {
						if(vline.isSNP() && _1kgLine.isSNP()) {
							if(_1kgLine.getFilter().equals("PASS")) {
								pass1kg = "PASS";
							} else {
								pass1kg = "FILTER";
							}
						} else if(!vline.isSNP() && !_1kgLine.isSNP()) {
							if(_1kgLine.getFilter().equals("PASS")) {
								pass1kg = "PASS";
							} else {
								pass1kg = "FILTER";
							}
						}
					}
					
					svmScore = Double.parseDouble(svm1kgService.getValue(_1kgLine.getInfo()));
				}
			}
		
			//logger.info(vline.getFormat());
			
			String ac = getValue(vline.getInfo(),acService);
			String an = getValue(vline.getInfo(),anService);
			boolean inHapmap = hapmapSites.contains(vline.getId(":"));
			boolean inDbsnp = !vline.getId().equals(".");
			boolean isGoldIndel = indels.contains(vline.getChrom() + vline.getPos());
			boolean inDbsnpOrIsGoldIndel = inDbsnp || isGoldIndel;
			String dp = getValue(vline.getInfo(),dpService);
			String qd = getValue(vline.getInfo(),qdService);
			String fs = getValue(vline.getInfo(),fsService);
			String mq = getValue(vline.getInfo(),mqService);
			String mqRankSum = getValue(vline.getInfo(),mqRankSumService);
			String rpRankSum = getValue(vline.getInfo(),rpRankSumService);
			Float quality = vline.getQual();
			
			String filter = vline.getFilter();
			
			String clipplingRankSum = getValue(vline.getInfo(),clipplingRankSumService);
			
			String baseQRankSum = getValue(vline.getInfo(),baseQRankSumService);
			//logger.info(vline.getInfo());
			//logger.info(clipplingRankSum);
			
			String haplotypeScore = getValue(vline.getInfo(),haplotypeScoreService);
			
			String sor = getValue(vline.getInfo(),sorService);
			String inbreeding = getValue(vline.getInfo(),inbreedingService);
			
			List<GenotypeField> gts = null;
			try {
				gts = vline.parseGenotypeFieldBySampleIds(gtfp, vline.getSampleIds());
			} catch(Exception e) {
				logger.info("Switching to GT only parser");
				
				gtfp = new GTFieldParser();
				gts = vline.parseGenotypeFieldBySampleIds(gtfp, vline.getSampleIds());
			}
			
			FeatureCalculator featureCalculator = new SVMFeatureCalculator();
			
			//Set QD to impute allele balance if there are no heterozygous sites
			if(!qd.equals("NA")) {
				featureCalculator.setQD(Double.parseDouble(qd));
			}
			
			featureCalculator.calculate(gts, gtfp.getClass().equals(GT_AD_DP_GQFieldParser.class));
			String alleleBalance = featureCalculator.getMeanAlleleBalance();
			String meanAltDepth = featureCalculator.getMeanAltDepth();
			String alleleDosagePhred = featureCalculator.getAlleleDosageTest();
			String normalizedAlleleDosagePhred = featureCalculator.getNormalizedAlleleDosageTest();
			double callrate = featureCalculator.getCallRate();
			
			String chrPos = vline.getChrom() + ":" + vline.getPos();
			
			String consensus = "NA";
			
			int failCount = 0;
			if(vline.isSNP()) {
				if(!qd.equals("NA") && Double.parseDouble(qd) < 3) {
					failCount++;
				}
				
				//if(!fs.equals("NA") && Double.parseDouble(fs) > 60) {
				//	failCount++;
				//}
				
				if(!mq.equals("NA") && Double.parseDouble(mq) < 50) {
					failCount++;
				}
				
				if(!mqRankSum.equals("NA") && Double.parseDouble(mqRankSum) < -3) {
					failCount++;
				}
				
				if(!mqRankSum.equals("NA") && Double.parseDouble(mqRankSum) > 10) {
					failCount++;
				}
				
				if(!rpRankSum.equals("NA") && Double.parseDouble(rpRankSum) < -20) {
					failCount++;
				}
				
				if(!alleleBalance.equals("NA") && Double.parseDouble(alleleBalance) < 0.2) {
					failCount++;
				}
				
				/*
				 * 3-7-2016 I do not think this makes sense to filter if the balance is > 80%
				 */
				
				//if(!alleleBalance.equals("NA") && Double.parseDouble(alleleBalance) > 0.8) {
				//	failCount++;
				//}
				
				if(!meanAltDepth.equals("NA") && Double.parseDouble(meanAltDepth) < altAlleleDepth) {
					failCount++;
				}

				if(!normalizedAlleleDosagePhred.equals("NA") && Double.parseDouble(normalizedAlleleDosagePhred) > 5) {
					failCount++;
				}
				
				if(!baseQRankSum.equals("NA") && Double.parseDouble(baseQRankSum) < -40) {
					failCount++;
				}
				
				if(callrate < 0.8) {
					failCount++;
				}
			} else {
				if(!qd.equals("NA") && Double.parseDouble(qd) < 3) {
					failCount++;
				}
				
				if(!fs.equals("NA") && Double.parseDouble(fs) > 200) {
					failCount++;
				}
				
				if(!rpRankSum.equals("NA") && Double.parseDouble(rpRankSum) < -20) {
					failCount++;
				}
				
				if(!alleleBalance.equals("NA") && Double.parseDouble(alleleBalance) < 0.2) {
					failCount++;
				}
				
				if(!meanAltDepth.equals("NA") && Double.parseDouble(meanAltDepth) < altAlleleDepth) {
					failCount++;
				}
				
				if(callrate < 0.7) {
					failCount++;
				}
			}
			
			/*
			 * If the site fails multiple filters then mark it as a fail site
			 */
			
			/*
			 * Added .20 callrate criteria b/c most sites with less than 20% call rate move missing values so they cannot have other information collected correctly
			 */
			if(failCount < 3 && callrate >= minCallRate) {
				if(failCount == 2) {
					if(pass1kg.equals("NA") && passExac.equals("NA")) {
						if(inDbsnp) {
							consensus = "UNKNOWN";
						} else {
							consensus = "FILTER";
						}
					} else if(!pass1kg.equals("NA")) {
						//in 1000G
						if(pass1kg.equals("PASS")) {
							consensus = "UNKNOWN";
						} else {
							consensus = "FILTER";
						}
					} else {
						//in ExAc
						if(passExac.equals("PASS")) {
							consensus = "UNKNOWN";
						} else {
							consensus = "FILTER";
						}
					}
				} else if(failCount == 1) {
					if(pass1kg.equals("NA") && passExac.equals("NA")) {
						if(inDbsnp) {
							consensus = "UNKNOWN";
						} else {
							consensus = "UNKNOWN";
						}
					} else if(!pass1kg.equals("NA")) {
						//in 1000G
						if(pass1kg.equals("PASS")) {
							consensus = "PASS";
						} else {
							consensus = "FILTER";
						}
					} else {
						//in ExAc
						if(passExac.equals("PASS")) {
							consensus = "PASS";
						} else {
							consensus = "FILTER";
						}
					}
				} else {
					if(pass1kg.equals("NA") && passExac.equals("NA")) {
						if(inDbsnp) {
							consensus = "PASS";
						} else {
							consensus = "UNKNOWN";
						}
					} else if(!pass1kg.equals("NA")) {
						//in 1000G
						if(pass1kg.equals("PASS")) {
							consensus = "PASS";
						} else {
							consensus = "FILTER";
						}
					} else {
						//in ExAc
						if(passExac.equals("PASS")) {
							consensus = "PASS";
						} else {
							consensus = "FILTER";
						}
					}
				}
				
			} else {
				logger.info("fail sites " + vline.getId("_") + " QD: " + qd + " FS: " + fs + " ReadPositionRankSum: " + rpRankSum + " CallRate: " + callrate + " MQ: " + mq + " MQRankSum: " + mqRankSum
						+ " Mean allele balance: " + alleleBalance + " Mean alt depth: " + meanAltDepth + " Allele Dosage Test Phred:" + alleleDosagePhred + " Normalized Allele Dosage Test Phred:" + normalizedAlleleDosagePhred + " BaseQRankSum: " + baseQRankSum);
				consensus = "FILTER";
			}
			
			//set prior
			//int matchCount = 0;
			double priorTrue = 0.4;
			if(inDbsnp == true) {
				priorTrue += 0.1;
			}
			
			if(passExac.equals("PASS")) {
				priorTrue += 0.2;
			} else if(passExac.equals("FILTER")) {
				priorTrue -= 0.1;
			}
			
			if(pass1kg.equals("PASS")) {
				priorTrue += 0.20;
			} else if(pass1kg.equals("FILTER")) {
				priorTrue -= 0.20;
			}
			
			double priorFalse = 1 - priorTrue;
			
			if(priorTrue + priorFalse != 1) {
				throw new Exception("priorTrue + priorFalse is not 1!");
			}
			
			//boolean inExacOrDbSnp = exacSet.contains(chrPos) || inDbsnp;
			
			sb.append(chrPos);
			sb.append("\t");
			
			sb.append(vline.getId("_"));
			sb.append("\t");
			
			sb.append(vline.isSNP() ? "1" : "0");
			sb.append("\t");
			
			sb.append( inDbsnp ? "1" : "0");
			sb.append("\t");
			
			sb.append( inHapmap ? "1" : "0");
			sb.append("\t");
			
			sb.append( isGoldIndel ? "1" : "0");
			sb.append("\t");
			
			sb.append( inDbsnpOrIsGoldIndel ? "1" : "0");
			sb.append("\t");
			
			sb.append( passExac );
			sb.append("\t");
			
			sb.append( Double.toString(vqsrLod) );
			sb.append("\t");
			
			sb.append( pass1kg );
			sb.append("\t");
			
			sb.append( Double.toString(svmScore) );
			sb.append("\t");
			
			sb.append( consensus );
			sb.append("\t");
			
			sb.append( Double.toString(priorTrue) );
			sb.append("\t");
			
			sb.append( Double.toString( priorFalse ) );
			sb.append("\t");
			
			sb.append( filter);
			sb.append("\t");
			
			sb.append(ac);
			sb.append("\t");
			
			sb.append(an);
			sb.append("\t");
			
			sb.append(dp);
			sb.append("\t");
			
			sb.append(meanAltDepth);
			sb.append("\t");
			
			sb.append(alleleBalance);
			sb.append("\t");
			
			sb.append(alleleDosagePhred);
			sb.append("\t");
			
			sb.append(normalizedAlleleDosagePhred);
			sb.append("\t");
			
			sb.append(qd);
			sb.append("\t");
			
			sb.append(fs);
			sb.append("\t");
			
			sb.append(mq);
			sb.append("\t");
			
			sb.append(mqRankSum);
			sb.append("\t");
			
			sb.append(clipplingRankSum);
			sb.append("\t");
			
			sb.append(rpRankSum);
			sb.append("\t");
			
			sb.append(baseQRankSum);
			sb.append("\t");
			
			sb.append(haplotypeScore);
			sb.append("\t");
			
			sb.append( Double.toString(quality));
			sb.append("\t");
			
			sb.append(Double.toString(callrate));
			sb.append("\t");
			
			sb.append(sor);
			sb.append("\t");
			
			sb.append(inbreeding);
			sb.append("\n");
			
		}
		exacQuery.close();
		iter.close();
		sb.close();
	}

	private String getValue(String info, ValueCaptureService<String> service) {
		String tmp = service.getValue(info);
		
		if(StringUtils.isEmpty(tmp)) {
			return "NA";
		} else {
			return tmp;
		}
	}
	
	@Override
	public void setVcf(String vcf) {
		this.vcf = vcf;	
	}

	public ApplicationProperties getApplicationProperties() {
		return applicationProperties;
	}

	public void setApplicationProperties(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}
	
	
}
