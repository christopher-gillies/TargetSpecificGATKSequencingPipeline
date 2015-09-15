library(ggplot2)
setwd("<outDir>");
source("http://glom.sph.umich.edu/wiki/images/0/04/Multiplot.R")

tab5rows = read.table("<statsFile>", header = TRUE, nrows = 1000)
classes = sapply(tab5rows, class)
tbl = read.table("<statsFile>", header = TRUE, colClasses = classes)
tbl = tbl[!duplicated(tbl$CHR_POS_ID),]
rownames(tbl) = tbl$CHR_POS_ID

tbl$FILTER = factor(tbl$FILTER)

tbl$IN_DBSNP = factor(tbl$IN_DBSNP,levels=c(0,1),labels=c("NOT_IN_DBSNP","IN_DBSNP"))

tbl$IN_DBSNP_OR_GOLD_INDEL = factor(tbl$IN_DBSNP_OR_GOLD_INDEL,levels=c(0,1),labels=c("NOT_IN_DBSNP","IN_DSNSP"))


all_snps_het = subset(tbl, IS_SNP==1 & GT == "HET")

pass_snps = subset(tbl, FILTER == "PASS" & IS_SNP == 1)
not_pass_snps = subset(tbl, FILTER != "PASS" & IS_SNP == 1)


all_snps_hom_alt = subset(tbl, IS_SNP==1 & GT == "HOM_ALT")
pass_snps_hets = subset(tbl, FILTER == "PASS" & GT == "HET" & IS_SNP == 1)

pass_snps_hom_alt = subset(tbl, FILTER == "PASS" & GT == "HOM_ALT" & IS_SNP == 1)


g1 = ggplot(all_snps_het,aes(x=GQ,fill=IN_DBSNP)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
g2 = ggplot(subset(pass_snps_hom_alt,ALT_DEPTH \< 100),aes(x=ALT_DEPTH,fill=IN_DBSNP)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
g3 = ggplot(all_snps_het,aes(x=ALLELE_BALANCE,fill=IN_DBSNP)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
g4 = ggplot(subset(all_snps_het,DP \< 100),aes(x=DP,fill=IN_DBSNP)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
pdf("all.het.snps.dbsnp.pdf")
multiplot(g1,g2,g3,g4,cols=2)
dev.off();

pdf("all.het.snps.alt.depth.dbsnp.pdf")
print(g2)
dev.off();

pdf("all.het.snps.allele.balance.dbsnp.pdf")
print(g3)
dev.off();

g1 = ggplot(all_snps_het,aes(x=GQ,fill=FILTER)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
g2 = ggplot(subset(all_snps_het,ALT_DEPTH \< 100),aes(x=ALT_DEPTH,fill=FILTER)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
g3 = ggplot(all_snps_het,aes(x=ALLELE_BALANCE,fill=FILTER)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
g4 = ggplot(subset(all_snps_het,DP \< 100),aes(x=DP,fill=FILTER)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
pdf("all.het.snps.filter.pdf")
multiplot(g1,g2,g3,g4,cols=2)
dev.off();


g1 = ggplot(subset(pass_snps_hets,DP \< 100),aes(x=DP,fill=IN_DBSNP)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
g2 = ggplot(pass_snps_hets,aes(x=GQ,fill=IN_DBSNP)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
g3 = ggplot(subset(pass_snps_hets,ALT_DEPTH \< 100),aes(x=ALT_DEPTH,fill=IN_DBSNP)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
g4 = ggplot(pass_snps_hets,aes(x=ALLELE_BALANCE,fill=IN_DBSNP)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
pdf("pass.het.snps.dbsnp.pdf")
multiplot(g1,g2,g3,g4,cols=2)
dev.off();

g1 = ggplot(all_snps_hom_alt,aes(x=GQ,fill=IN_DBSNP)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
g2 = ggplot(subset(pass_snps_hom_alt,ALT_DEPTH \< 100),aes(x=ALT_DEPTH,fill=IN_DBSNP)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
g3 = ggplot(all_snps_hom_alt,aes(x=ALLELE_BALANCE,fill=IN_DBSNP)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
g4 = ggplot(subset(all_snps_hom_alt, DP \< 100),aes(x=DP,fill=IN_DBSNP)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
pdf("all.hom.alt.snps.dbsnp.pdf")
multiplot(g1,g2,g3,g4,cols=2)
dev.off();


g1 = ggplot(all_snps_hom_alt,aes(x=GQ,fill=FILTER)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
g2 = ggplot(subset(pass_snps_hom_alt,ALT_DEPTH \< 100),aes(x=ALT_DEPTH,fill=FILTER)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
g3 = ggplot(all_snps_hom_alt,aes(x=ALLELE_BALANCE,fill=FILTER)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
g4 = ggplot(subset(all_snps_hom_alt, DP \< 100),aes(x=DP,fill=FILTER)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
pdf("all.hom.alt.snps.filter.pdf")
multiplot(g1,g2,g3,g4,cols=2)
dev.off();

g1 = ggplot(subset(pass_snps_hom_alt,DP \< 100),aes(x=DP,fill=IN_DBSNP)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
g2 = ggplot(pass_snps_hom_alt,aes(x=GQ,fill=IN_DBSNP)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
g3 = ggplot(subset(pass_snps_hom_alt,ALT_DEPTH \< 100),aes(x=ALT_DEPTH,fill=IN_DBSNP)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
g4 = ggplot(pass_snps_hom_alt,aes(x=ALLELE_BALANCE,fill=IN_DBSNP)) + geom_histogram(position="dodge") + theme(legend.position="bottom")
pdf("pass.hom.alt.snps.dbsnp.pdf")
multiplot(g1,g2,g3,g4,cols=2)
dev.off();
ls

