library(gridExtra)
library(ggplot2)

#READ SNP TABLE
setwd("<outDir>");
tbl = read.table("<statsFile>",header=TRUE);
snps = subset(tbl,IS_SNP==1);
rownames(snps) = snps$CHR_POS
indels = subset(tbl,IS_SNP==0);
rownames(indels) = indels$CHR_POS


snps$FILTER = factor(snps$FILTER)

snps$IN_DBSNP = factor(snps$IN_DBSNP,levels=c(0,1),labels=c("NOT_IN_DBSNP","IN_DSNSP"))

snps$IN_DBSNP_OR_GOLD_INDEL = factor(snps$IN_DBSNP_OR_GOLD_INDEL,levels=c(0,1),labels=c("NOT_IN_DBSNP","IN_DSNSP"))



indels$FILTER = factor(indels$FILTER)

indels$IN_DBSNP = factor(indels$IN_DBSNP,levels=c(0,1),labels=c("NOT_IN_DBSNP","IN_DSNSP"))

indels$IN_DBSNP_OR_GOLD_INDEL = factor(indels$IN_DBSNP_OR_GOLD_INDEL,levels=c(0,1),labels=c("NOT_IN_DBSNP","IN_DSNSP"))




#CALLRATE

pdf("callrate.snps.dbsnp.pdf")
ggplot(snps, aes(x=CALLRATE,fill=IN_DBSNP)) + geom_bar(position="dodge")
dev.off()

pdf("callrate.snps.filter.pdf")
ggplot(snps, aes(x=CALLRATE,fill=FILTER)) + geom_bar(position="dodge")
dev.off()


pdf("callrate.indels.dbsnp.pdf")
ggplot(indels, aes(x=CALLRATE,fill=IN_DBSNP_OR_GOLD_INDEL)) + geom_bar(position="dodge")
dev.off()

pdf("callrate.indels.filter.pdf")
ggplot(indels, aes(x=CALLRATE,fill=FILTER)) + geom_bar(position="dodge")
dev.off()


#QD

pdf("qd.snps.dbsnp.pdf")
ggplot(snps, aes(x=QD,fill=IN_DBSNP)) + geom_bar(position="dodge")
dev.off()

pdf("qd.snps.filter.pdf")
ggplot(snps, aes(x=QD,fill=FILTER)) + geom_bar(position="dodge")
dev.off()


pdf("qd.indels.dbsnp.pdf")
ggplot(indels, aes(x=QD,fill=IN_DBSNP_OR_GOLD_INDEL)) + geom_bar(position="dodge")
dev.off()

pdf("qd.indels.filter.pdf")
ggplot(indels, aes(x=QD,fill=FILTER)) + geom_bar(position="dodge")
dev.off()


#RP_RANKSUM

pdf("rp.snps.dbsnp.pdf")
ggplot(snps, aes(x=RP_RANKSUM,fill=IN_DBSNP)) + geom_bar(position="dodge")
dev.off()

pdf("rp.snps.filter.pdf")
ggplot(snps, aes(x=RP_RANKSUM,fill=FILTER)) + geom_bar(position="dodge")
dev.off()


pdf("rp.indels.dbsnp.pdf")
ggplot(indels, aes(x=RP_RANKSUM,fill=IN_DBSNP_OR_GOLD_INDEL)) + geom_bar(position="dodge")
dev.off()

pdf("rp.indels.filter.pdf")
ggplot(indels, aes(x=RP_RANKSUM,fill=FILTER)) + geom_bar(position="dodge")
dev.off()



#NORMALIZED_ALLELE_DOSAGE_TEST

pdf("nad.snps.dbsnp.pdf")
ggplot(snps, aes(x=NORMALIZED_ALLELE_DOSAGE_TEST,fill=IN_DBSNP)) + geom_bar(position="dodge")
dev.off()

pdf("nad.snps.filter.pdf")
ggplot(snps, aes(x=NORMALIZED_ALLELE_DOSAGE_TEST,fill=FILTER)) + geom_bar(position="dodge")
dev.off()


pdf("nad.indels.dbsnp.pdf")
ggplot(indels, aes(x=NORMALIZED_ALLELE_DOSAGE_TEST,fill=IN_DBSNP_OR_GOLD_INDEL)) + geom_bar(position="dodge")
dev.off()

pdf("nad.indels.filter.pdf")
ggplot(indels, aes(x=NORMALIZED_ALLELE_DOSAGE_TEST,fill=FILTER)) + geom_bar(position="dodge")
dev.off()

#MEAN_ALLELE_BALANCE

pdf("mean.allele.balance.snps.dbsnp.pdf")
ggplot(snps, aes(x=MEAN_ALLELE_BALANCE,fill=IN_DBSNP)) + geom_bar(position="dodge")
dev.off()

pdf("mean.allele.balance.snps.filter.pdf")
ggplot(snps, aes(x=MEAN_ALLELE_BALANCE,fill=FILTER)) + geom_bar(position="dodge")
dev.off()



pdf("mean.allele.balance.indels.dbsnp.pdf")
ggplot(indels, aes(x=MEAN_ALLELE_BALANCE,fill=IN_DBSNP_OR_GOLD_INDEL)) + geom_bar(position="dodge")
dev.off()

pdf("mean.allele.balance.indels.filter.pdf")
ggplot(indels, aes(x=MEAN_ALLELE_BALANCE,fill=FILTER)) + geom_bar(position="dodge")
dev.off()

#QD x CALLRATE

pdf("qd.callrate.snps.dbsnp.pdf")
ggplot(snps, aes(x=QD,y=CALLRATE,colour=IN_DBSNP)) + geom_point(position="dodge")
dev.off()

pdf("qd.callrate.snps.filter.pdf")
ggplot(snps, aes(x=QD,y=CALLRATE,colour=FILTER)) + geom_point(position="dodge")
dev.off()

pdf("qd.callrate.indels.dbsnp.pdf")
ggplot(indels, aes(x=QD,y=CALLRATE,colour=IN_DBSNP)) + geom_point(position="dodge")
dev.off()

pdf("qd.callrate.indels.filter.pdf")
ggplot(indels, aes(x=QD,y=CALLRATE,colour=FILTER)) + geom_point(position="dodge")
dev.off()

#QD x RP_RANKSUM

pdf("qd.rp.snps.dbsnp.pdf")
ggplot(snps, aes(x=QD,y=RP_RANKSUM,colour=IN_DBSNP)) + geom_point(position="dodge")
dev.off()

pdf("qd.rp.snps.filter.pdf")
ggplot(snps, aes(x=QD,y=RP_RANKSUM,colour=FILTER)) + geom_point(position="dodge")
dev.off()


pdf("qd.rp.indels.dbsnp.pdf")
ggplot(indels, aes(x=QD,y=RP_RANKSUM,colour=IN_DBSNP)) + geom_point(position="dodge")
dev.off()

pdf("qd.rp.indels.filter.pdf")
ggplot(indels, aes(x=QD,y=RP_RANKSUM,colour=FILTER)) + geom_point(position="dodge")
dev.off()


#CALLRATE x RP_RANKSUM

pdf("qd.rp.snps.dbsnp.pdf")
ggplot(snps, aes(x=CALLRATE,y=RP_RANKSUM,colour=IN_DBSNP)) + geom_point(position="dodge")
dev.off()

pdf("qd.rp.snps.filter.pdf")
ggplot(snps, aes(x=CALLRATE,y=RP_RANKSUM,colour=FILTER)) + geom_point(position="dodge")
dev.off()



pdf("qd.rp.indels.dbsnp.pdf")
ggplot(indels, aes(x=CALLRATE,y=RP_RANKSUM,colour=IN_DBSNP)) + geom_point(position="dodge")
dev.off()

pdf("qd.rp.indels.filter.pdf")
ggplot(indels, aes(x=CALLRATE,y=RP_RANKSUM,colour=FILTER)) + geom_point(position="dodge")
dev.off()



pdf("filter.dbsnp.snps.pdf")
grid = addmargins(table(snps$IN_DBSNP,snps$FILTER),margin=1:2)
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

pdf("filter.hapmap.snps.pdf")
grid = addmargins(table(snps$IN_HAPMAP,snps$FILTER),margin=1:2)
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

pdf("filter.dbsnp.indels.pdf")
grid = addmargins(table(indels$IN_DBSNP,indels$FILTER),margin=1:2)
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


