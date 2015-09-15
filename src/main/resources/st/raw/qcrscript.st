
library(ggplot2)
setwd("<outDir>");
tbl = read.table("<statsFile>",header=TRUE);
rownames(tbl) = tbl[,"SAMPLE_ID"];


pdf("qc.merged.eps_mse.eps_cycle_mean.pdf");
ggplot(tbl,aes(x= EPS_MSE,y= EPS_Cycle_Mean,label=SAMPLE_ID)) + geom_text()
dev.off();


pdf("qc.merged.q20.pdf");
ggplot(tbl,aes(x=Q20BasesPct...)) + geom_histogram() + xlab("Q20 Bases %");
dev.off();

pdf("qc.merged.total.reads.pdf");
ggplot(tbl,aes(x=TotalReads.e6.)) + geom_histogram() + xlab("Total Reads in millions");
dev.off();


pdf("qc.merged.median.insert.size.pdf");
ggplot(tbl,aes(x=ISize_medium)) + geom_histogram() + xlab("Median insert size");
dev.off();


pdf("qc.merged.mean.depth.pdf");
ggplot(tbl,aes(x=MeanDepth)) + geom_histogram() + xlab("Mean depth");
dev.off();

pdf("qc.merged.mean.depth.q20.pdf");
ggplot(tbl,aes(y=MeanDepth, x=Q20BasesPct...,label=rownames(tbl))) + geom_text() + ylab("Mean depth") + xlab("Q20 bases %") + geom_abline(intercept = 50,slope=0) + geom_vline(xintercept=90);
dev.off();

pdf("qc.merged.mean.depth.insert.size.pdf");
ggplot(tbl,aes(y=MeanDepth, x=ISize_medium,label=rownames(tbl))) + geom_text() + ylab("Mean depth") + xlab("ISize_medium") + geom_abline(intercept = 50,slope=0);
dev.off();

pdf("qc.merged.mapping.rate.pdf");
ggplot(tbl,aes(x=MappingRate...)) + geom_histogram() + xlab("Mapping Rate %");
dev.off();

#subset = scale(tbl[,c("Q20BasesPct...","TotalReads.e6.","ISize_medium","MeanDepth","MappingRate...")]);

#Remove TotalReads.e6 because MeanDepth is too correlated with it and it may skew the results...
subset = scale(tbl[,c("Q20BasesPct...","ISize_medium","MeanDepth","MappingRate...")],scale=FALSE);

fit = princomp(subset,cor=FALSE);

pdf("qc.variance.explained.pdf");
plot(fit,type="lines");
dev.off();

pdf("qc.pca.analysis.pdf");
biplot(fit);
dev.off();