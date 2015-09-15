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



indels$FILTER = factor(indels$FILTER)

indels$IN_DBSNP = factor(indels$IN_DBSNP,levels=c(0,1),labels=c("NOT_IN_DBSNP","IN_DBSNP"))

indels$IN_DBSNP_OR_GOLD_INDEL = factor(indels$IN_DBSNP_OR_GOLD_INDEL,levels=c(0,1),labels=c("NOT_IN_DBSNP","IN_DSNSP"))



#READ CONFIRMED

confirmed = read.table("<confirmedSites>",header=TRUE);
confirmed = confirmed[order(confirmed$CHR_POS,-confirmed$CONFIRMED),]
confirmed = confirmed[!duplicated(confirmed$CHR_POS),]
confirmed_snps = subset(confirmed, INDEL == 0)
confirmed_indels = subset(confirmed, INDEL == 1)
rownames(confirmed_snps) = confirmed_snps$CHR_POS
rownames(confirmed_indels) = confirmed_indels$CHR_POS

additional_true_negative_snps = length(setdiff(rownames(subset(confirmed_snps, CONFIRMED==0 & INDEL == 0)),rownames(snps)))
additional_false_negative_snps = length(setdiff(rownames(subset(confirmed_snps, CONFIRMED==1 & INDEL == 0)),rownames(snps)))

additional_true_negative_indels = length(setdiff(rownames(subset(confirmed_indels, CONFIRMED==0 & INDEL == 1)),rownames(indels)))
additional_false_negative_indels = length(setdiff(rownames(subset(confirmed_indels, CONFIRMED==1 & INDEL == 1)),rownames(indels)))


#INTERSECT
common_snps = intersect(rownames(confirmed_snps),rownames(snps))
common_indels = intersect(rownames(confirmed_indels),rownames(indels))
confirmed_yes_snps = subset(confirmed_snps, CONFIRMED == 1 & INDEL == 0 & rownames(confirmed_snps) %in% common_snps)
confirmed_yes_indels = subset(confirmed_indels, CONFIRMED == 1 & INDEL == 1 & rownames(confirmed_indels) %in% common_indels)
total_confirmed_calls = dim(confirmed_yes_snps)[1]
confirmed_no_snps = subset(confirmed_snps, CONFIRMED == 0 & INDEL == 0 & rownames(confirmed_snps) %in% common_snps)
confirmed_no_indels = subset(confirmed_indels, CONFIRMED == 0 & INDEL == 1 & rownames(confirmed_indels) %in% common_indels)
total_not_confirmed_calls = dim(confirmed_no_snps)[1]


#confirmed[ !(rownames(confirmed) %in% common), ]

snps$CONFIRMED = "UNKNOWN";
snps[snps$IN_DBSNP == "IN_DBSNP", "CONFIRMED"] = "IN_DBSNP";
snps[rownames(confirmed_yes_snps), "CONFIRMED"] = "YES";
snps[rownames(confirmed_no_snps), "CONFIRMED"] = "NO";

indels$CONFIRMED = "UNKNOWN";
indels[indels$IN_DBSNP == "IN_DBSNP", "CONFIRMED"] = "IN_DBSNP";
indels[rownames(confirmed_yes_indels), "CONFIRMED"] = "YES";
indels[rownames(confirmed_no_indels), "CONFIRMED"] = "NO";

snps$CONFIRMED = factor(snps$CONFIRMED);
indels$CONFIRMED = factor(indels$CONFIRMED);

snps$ALLELE_DOSAGE_TEST_NORMALIZED = snps$ALLELE_DOSAGE_TEST / snps$MEAN_ALT_DEPTH
indels$ALLELE_DOSAGE_TEST_NORMALIZED = indels$ALLELE_DOSAGE_TEST / indels$MEAN_ALT_DEPTH


#CALLED CONFIRMED
confirmed_sites = rownames(subset(tbl,rownames(tbl) %in% rownames(confirmed)));


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

subset(snps[,c("P_PASS","SVM","CONFIRMED","CONSENSUS")],CONFIRMED=="NO")


subset(snps,CONFIRMED=="NO" & SVM == TRUE)

subset(snps,CONFIRMED=="YES" & SVM == FALSE)

#cat merged.unifiedGenotyper.vcf | grep 49166017 | perl -lane 'print join("\n",split(/\t/))' | grep "0/1\|1/1\|1/0"

subset(snps[,c("P_PASS","SVM","CONFIRMED")],CONFIRMED=="YES")


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

pdf("svm.confirmed.snps.pdf")
confirmed_results = table(snps$CONFIRMED,snps$SVM);
confirmed_results["NO","FALSE"] = confirmed_results["NO","FALSE"] + additional_true_negative_snps;
confirmed_results["YES","FALSE"] = confirmed_results["YES","FALSE"] + additional_false_negative_snps;
grid = addmargins(confirmed_results,margin=1:2)
grid.table(grid)
FP =  grid[2,2];
TP = grid[4,2];
FDR = FP / (TP + FP);
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

subset(indels[,c("P_PASS","SVM","CONFIRMED","CONSENSUS")],CONFIRMED=="NO")


subset(indels,CONFIRMED=="NO" & SVM == TRUE)
subset(indels,CONFIRMED=="YES" & SVM == FALSE)

subset(indels[,c("P_PASS","SVM","CONFIRMED")],CONFIRMED=="YES")


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
new.grid = cbind(new.grid,SENSITIVITY)
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
new.grid = cbind(new.grid,SENSITIVITY)
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

pdf("svm.confirmed.indels.pdf")
confirmed_results = table(indels$CONFIRMED,indels$SVM);
confirmed_results["NO","FALSE"] = confirmed_results["NO","FALSE"] + additional_true_negative_indels;
confirmed_results["YES","FALSE"] = confirmed_results["YES","FALSE"] + additional_false_negative_indels;
grid = addmargins(confirmed_results,margin=1:2)
grid.table(grid)
FP =  grid[2,2];
TP = grid[4,2];
FDR = FP / (TP + FP);
dev.off()
