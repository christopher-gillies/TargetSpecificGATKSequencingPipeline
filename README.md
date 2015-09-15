# TargetSpecificGATKSequencingPipeline
This package takes FASTQ files and produces a filtered VCF file

###To build you will need to install VCFAnalysisTools jar into your local maven repository
export LIB=./lib/VCFAnalysisTools-1.03.jar
mvn install:install-file -Dfile=$LIB -DgroupId=org.kidneyomics \
    -DartifactId=VCFAnalysisTools -Dversion=1.03 -Dpackaging=jar