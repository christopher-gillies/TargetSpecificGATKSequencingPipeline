package org.kidneyomics.fluidigm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import net.sf.samtools.util.CloseableIterator;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kidneyomics.vcf.GenotypeField;
import org.kidneyomics.vcf.GenotypeFieldParser;
import org.kidneyomics.vcf.RegexStringCaptureService;
import org.kidneyomics.vcf.VCFFile;
import org.kidneyomics.vcf.VCFFile.VCFLineIterator;
import org.kidneyomics.vcf.VCFLine;
import org.kidneyomics.vcf.ValueCaptureService;

public class SiteQualityFilter implements Filter {

	Logger logger = Logger.getLogger(SiteQualityFilter.class);
	
	private String vcf;
	private String snpFilter;
	private String indelFilter;
	
	ApplicationProperties applicationProperties;
	
	@Override
	public void filter(String out) throws Exception {
		
		JexlEngine jexl = new JexlEngine();
		
        Expression snpExpression = jexl.createExpression( snpFilter );
        Expression indelExpression = jexl.createExpression( indelFilter );		
        		
		VCFFile vfile = new VCFFile();
		
		
		VCFLineIterator iter = vfile.iterator(vcf);
		Writer writer = new BufferedWriter( new FileWriter(new File(out)));
		
		
		writer.write(StringUtils.join(iter.getHeaderLines(), "\n"));
		writer.write("\n");
		
		
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

		//logger.info(vfile.getHeaderLines());
		
		int count = 0;
		while(iter.hasNext()) {
			VCFLine vline = iter.next();
			
			//logger.info(vline.getFormat());
			
			//String acS = getValue(vline.getInfo(),acService);
			//String anS = getValue(vline.getInfo(),anService);
			
			
			//boolean inDbsnp = !vline.getId().equals(".");

			
			//String dpS = getValue(vline.getInfo(),dpService);
			String qdS = getValue(vline.getInfo(),qdService);
			double qd;
			if(qdS == null) {
				logger.info(vline.getId(":") + " missing QD assuming it is 10");
				qd = 10.0;
			} else {
				qd = Double.parseDouble(qdS);
			}
			
			//String fsS = getValue(vline.getInfo(),fsService);
			
			
			//String mqS = getValue(vline.getInfo(),mqService);
			//String mqRankSumS = getValue(vline.getInfo(),mqRankSumService);
			String rpRankSumS = getValue(vline.getInfo(),rpRankSumService);
			double rpRankSum;
			if(rpRankSumS == null) {
				/*
				 * use expected value
				 */
				logger.info(vline.getId(":") + " missing ReadPosRankSum assuming it is 0");
				rpRankSum = 0;
			} else {
				rpRankSum = Double.parseDouble(rpRankSumS);
			}
			
			String fsS = getValue(vline.getInfo(),fsService);
			double fs;
			if(fsS == null) {
				/*
				 * use expected value
				 */
				logger.info(vline.getId(":") + " missing FS assuming it is 0");
				fs = 0;
			} else {
				fs = Double.parseDouble(fsS);
			}
			
			
			String mqS = getValue(vline.getInfo(),mqService);
			double mq;
			if(mqS == null) {
				/*
				 * use expected value
				 */
				logger.info(vline.getId(":") + " missing MQ assuming it is 0");
				mq = 60;
			} else {
				mq = Double.parseDouble(mqS);
			}
			
			String mqRankSumS = getValue(vline.getInfo(),mqRankSumService);
			double mqRankSum;
			if(mqRankSumS == null) {
				/*
				 * use expected value
				 */
				logger.info(vline.getId(":") + " missing mqRankSum assuming it is 0");
				mqRankSum = 0;
			} else {
				mqRankSum = Double.parseDouble(mqRankSumS);
			}
			
			
			//Float qualityS = vline.getQual();
			
			//String clipplingRankSumS = getValue(vline.getInfo(),clipplingRankSumService);
			
			//String sorS = getValue(vline.getInfo(),sorService);
			//String inbreedingS = getValue(vline.getInfo(),inbreedingService);
			
			List<GenotypeField> gts = vline.parseGenotypeFieldBySampleIds(gtfp, vline.getSampleIds());
			int nonMissingCount = 0;
			
			for(GenotypeField gt : gts) {
				if(!gt.getIsMissing()) {
					nonMissingCount++;
				}
			}
			
			double callrate = nonMissingCount / ((double) gts.size());
			
			JexlContext context = new MapContext();
			context.set("QD", qd);
			context.set("ReadPosRankSum", rpRankSum);
			context.set("FS", fs);
			context.set("MQ", mq);
			context.set("MQRankSum", mqRankSum);
			context.set("CALLRATE", callrate);
			
			/*
			 * Should we filter the variant?
			 */
			boolean result = false;
			if(vline.isSNP()) {
				result = (Boolean) snpExpression.evaluate(context);
			} else {
				result = (Boolean) indelExpression.evaluate(context);
			}
			
			if(result) {
				logger.info(vline.getId(":") + " FILTER " + "QD: " + qd + " ReadPosRankSum: " + rpRankSum + " CALLRATE: " + callrate + " FS: " + fs + " MQ: " + mq + " MQRankSum: " + mqRankSum);
				vline.setFilter("FILTER");
			} else {
				logger.info(vline.getId(":") + " PASS " + "QD: " + qd + " ReadPosRankSum: " + rpRankSum + " CALLRATE: " + callrate + " FS: " + fs + " MQ: " + mq + " MQRankSum: " + mqRankSum);
				vline.setFilter("PASS");
			}
			
			writer.write(vline.toString());
			writer.write("\n");
			++count;
		}
		
		writer.close();
		
	}

	private String getValue(String info, ValueCaptureService<String> service) {
		String tmp = service.getValue(info);
		
		if(StringUtils.isEmpty(tmp)) {
			return null;
		} else {
			return tmp;
		}
	}
	
	@Override
	public void setVcf(String vcf) {
		this.vcf = vcf;

	}


	@Override
	public void setSnpFilter(String snpFilter) {
		this.snpFilter = snpFilter;
	}


	@Override
	public void setIndelFilter(String indelFilter) {
		this.indelFilter = indelFilter;
	}
	
	public ApplicationProperties getApplicationProperties() {
		return applicationProperties;
	}

	public void setApplicationProperties(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}
	

}
