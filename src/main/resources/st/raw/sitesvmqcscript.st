library(gridExtra)
library(ggplot2)
library(e1071)
library(impute)

#READ SNP TABLE
setwd("<outDir>");
tbl = read.table("<statsFile>",header=TRUE);

snps = subset(tbl,IS_SNP==1);
rownames(snps) = snps$CHR_POS
indels = subset(tbl,IS_SNP==0);
rownames(indels) = indels$CHR_POS


snps$FILTER = factor(snps$FILTER)

snps$IN_DBSNP = factor(snps$IN_DBSNP,levels=c(0,1),labels=c("NOT_IN_DBSNP","IN_DBSNP"))

snps$IN_DBSNP_OR_GOLD_INDEL = factor(snps$IN_DBSNP_OR_GOLD_INDEL,levels=c(0,1),labels=c("NOT_IN_DBSNP","IN_DSNSP"))

snps$CONFIRMED = "UNKNOWN";

indels$FILTER = factor(indels$FILTER)

indels$IN_DBSNP = factor(indels$IN_DBSNP,levels=c(0,1),labels=c("NOT_IN_DBSNP","IN_DBSNP"))

indels$IN_DBSNP_OR_GOLD_INDEL = factor(indels$IN_DBSNP_OR_GOLD_INDEL,levels=c(0,1),labels=c("NOT_IN_DBSNP","IN_DSNSP"))

indels$CONFIRMED = "UNKNOWN";

snps$ALLELE_DOSAGE_TEST_NORMALIZED = snps$ALLELE_DOSAGE_TEST / snps$MEAN_ALT_DEPTH
indels$ALLELE_DOSAGE_TEST_NORMALIZED = indels$ALLELE_DOSAGE_TEST / indels$MEAN_ALT_DEPTH


####
#SNPS
####



inv.norm = function(r) {
	qnorm((rank(r)-.5)/length(r))
}

tmp = as.matrix(snps[,c("RP_RANKSUM","CALLRATE","QD","MEAN_ALLELE_BALANCE","INBREEDING","HAPLOTYPE_SCORE","MQ_RANKSUM","MQ", "ALLELE_DOSAGE_TEST" , "BASEQ_RANKSUM", "MEAN_ALT_DEPTH","ALLELE_DOSAGE_TEST_NORMALIZED")]);

tmp.invn = apply(tmp,MARGIN=2,FUN=inv.norm)

imputed.values = data.frame(impute.knn(tmp.invn)$data)

imputed.values = data.frame(apply(imputed.values,MARGIN=2,FUN=inv.norm))

imputed.values$CONSENSUS = snps$CONSENSUS;

imputed.values.training = subset(imputed.values, CONSENSUS != "UNKNOWN");

imputed.values.training$CONSENSUS = factor(imputed.values.training$CONSENSUS,levels=c("FILTER","PASS"),labels=c("FILTER","PASS"))


svm.model = svm(CONSENSUS ~ CALLRATE + QD + RP_RANKSUM + MEAN_ALLELE_BALANCE  + INBREEDING + ALLELE_DOSAGE_TEST_NORMALIZED + HAPLOTYPE_SCORE + MQ_RANKSUM  + BASEQ_RANKSUM + MEAN_ALT_DEPTH,data=imputed.values.training,kernel="radial",scale=FALSE,probability=TRUE,cost=1)

pred.snps = predict(svm.model,imputed.values,probability=TRUE);
snps$P_PASS = attr(pred.snps,"probabilities")[,"PASS"]

snps$SVM = snps$P_PASS >= 0.5
snps$P_FILTER = attr(pred.snps,"probabilities")[,"FILTER"]	
#snps$SVM = (snps$P_PASS * snps$PRIOR_TRUE) >= (snps$P_FILTER * snps$PRIOR_FALSE) 
snps$POSTERIOR = (snps$P_PASS * snps$PRIOR_TRUE) / (  (snps$P_PASS * snps$PRIOR_TRUE) + (snps$P_FILTER * snps$PRIOR_FALSE));

snps$SVM_ADJ = snps$SVM
snps[rownames(subset(snps, PASS_1KG == "FILTER" | PASS_EXAC == "FILTER")),"SVM_ADJ"] = FALSE



write.table(snps[,c("KEY","P_PASS","POSTERIOR","SVM","CHR_POS","IN_DBSNP","PASS_EXAC","PASS_1KG","CONSENSUS","CONFIRMED")],file="svm.snp.predictions.txt",sep="\t",row.names=FALSE,quote=FALSE);


pdf("svm.dbsnp.snps.pdf")
grid = addmargins(table(snps$IN_DBSNP,snps$SVM),margin=1:2)
T = grid[2,3]
TP = grid[2,2]
SENS = TP/T
N = grid[1,3]
TN = grid[1,1]
SPEC = TN/N
#SPECIFICITY = list("","",SPEC)
SPECIFICITY = c(NA,NA,SPEC)
new.grid = rbind(grid,SPECIFICITY)
#SENSITIVITY = list("","",SENS,"");
SENSITIVITY = c(NA,NA,SENS,NA);
new.grid = cbind(new.grid,SENSITIVITY)
grid.table(new.grid)
dev.off()

pdf("svm.consensus.snps.pdf")
grid = addmargins(table(snps$CONSENSUS,snps$SVM),margin=1:2)
T = grid[2,3]
TP = grid[2,2]
SENS = TP/T
N = grid[1,3]
TN = grid[1,1]
SPEC = TN/N
SPECIFICITY = c(NA,NA,SPEC)
new.grid = rbind(grid,SPECIFICITY)
SENSITIVITY = c(NA,NA,NA,SENS,NA);
new.grid = cbind(new.grid,SENSITIVITY)
grid.table(new.grid)
dev.off()




####
#INDELS
####


inv.norm = function(r) {
	qnorm((rank(r)-.5)/length(r))
}

tmp = as.matrix(indels[,c("RP_RANKSUM","CALLRATE","QD","MEAN_ALLELE_BALANCE","INBREEDING","HAPLOTYPE_SCORE","MQ_RANKSUM","MQ", "ALLELE_DOSAGE_TEST" , "BASEQ_RANKSUM", "MEAN_ALT_DEPTH","ALLELE_DOSAGE_TEST_NORMALIZED")]);

tmp.invn = apply(tmp,MARGIN=2,FUN=inv.norm)

imputed.values = data.frame(impute.knn(tmp.invn)$data)

imputed.values = data.frame(apply(imputed.values,MARGIN=2,FUN=inv.norm))

imputed.values$CONSENSUS = indels$CONSENSUS;

imputed.values.training = subset(imputed.values, CONSENSUS != "UNKNOWN");

imputed.values.training$CONSENSUS = factor(imputed.values.training$CONSENSUS,levels=c("FILTER","PASS"),labels=c("FILTER","PASS"))

svm.model = svm(CONSENSUS ~ CALLRATE + QD + RP_RANKSUM + MEAN_ALLELE_BALANCE  + INBREEDING + ALLELE_DOSAGE_TEST_NORMALIZED + HAPLOTYPE_SCORE + MQ_RANKSUM  + BASEQ_RANKSUM + MEAN_ALT_DEPTH,data=imputed.values.training,kernel="radial",scale=FALSE,probability=TRUE,cost=1)
pred.indels = predict(svm.model,imputed.values,probability=TRUE);
indels$P_PASS = attr(pred.indels,"probabilities")[,"PASS"]

indels$SVM = indels$P_PASS >= 0.5
indels$P_FILTER = attr(pred.indels,"probabilities")[,"FILTER"]	
#indels$SVM = (indels$P_PASS * indels$PRIOR_TRUE) >= (indels$P_FILTER * indels$PRIOR_FALSE) 
indels$POSTERIOR = (indels$P_PASS * indels$PRIOR_TRUE) / (  (indels$P_PASS * indels$PRIOR_TRUE) + (indels$P_FILTER * indels$PRIOR_FALSE));

write.table(indels[,c("KEY","P_PASS","POSTERIOR","SVM","CHR_POS","IN_DBSNP","PASS_EXAC","PASS_1KG","CONSENSUS","CONFIRMED")],file="svm.indel.predictions.txt",sep="\t",row.names=FALSE,quote=FALSE);

pdf("svm.dbsnp.indels.pdf")
grid = addmargins(table(indels$IN_DBSNP,indels$SVM),margin=1:2)
T = grid[2,3]
TP = grid[2,2]
SENS = TP/T
N = grid[1,3]
TN = grid[1,1]
SPEC = TN/N
#SPECIFICITY = list("","",SPEC)
SPECIFICITY = c(NA,NA,SPEC)
new.grid = rbind(grid,SPECIFICITY)
#SENSITIVITY = list("","",SENS,"");
SENSITIVITY = c(NA,NA,SENS,NA);
new.grid = cbind(new.grid,SENSITIVITY);
grid.table(new.grid)
dev.off()

pdf("svm.hapmap.snps.pdf")
grid = addmargins(table(snps$IN_HAPMAP,snps$SVM),margin=1:2)
T = grid[2,3]
TP = grid[2,2]
SENS = TP/T
N = grid[1,3]
TN = grid[1,1]
SPEC = TN/N
#SPECIFICITY = list("","",SPEC)
SPECIFICITY = c(NA,NA,SPEC)
new.grid = rbind(grid,SPECIFICITY)
#SENSITIVITY = list("","",SENS,"");
SENSITIVITY = c(NA,NA,SENS,NA);
new.grid = cbind(new.grid,SENSITIVITY);
grid.table(new.grid)
dev.off()

pdf("svm.consensus.indels.pdf")
grid = addmargins(table(indels$CONSENSUS,indels$SVM),margin=1:2)
T = grid[2,3]
TP = grid[2,2]
SENS = TP/T
N = grid[1,3]
TN = grid[1,1]
SPEC = TN/N
#SPECIFICITY = list("","",SPEC)
SPECIFICITY = c(NA,NA,SPEC)
new.grid = rbind(grid,SPECIFICITY)
#SENSITIVITY = list("","",SENS,"");
SENSITIVITY = c(NA,NA,NA,SENS,NA);
new.grid = cbind(new.grid,SENSITIVITY)
grid.table(new.grid)
dev.off()

