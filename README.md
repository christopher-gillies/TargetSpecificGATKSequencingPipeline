# TargetSpecificGATKSequencingPipeline
This package takes FASTQ files and produces a filtered VCF file

# Setup 
* this has been tested on 

# Install dependencies

### If you are interested in read trimming then install cutadapt
* https://cutadapt.readthedocs.org/en/stable/installation.html
* install pip and cutadapt
 
 ```
sudo apt-get install build-essential
sudo apt-get install python-dev
sudo apt-get install python-dev
sudo apt-get install python-pip
pip install --user --upgrade cutadapt
```

### If you are interested in aligning and variant calling






###To build you will need to install VCFAnalysisTools jar into your local maven repository
export LIB=./lib/VCFAnalysisTools-1.03.jar
mvn install:install-file -Dfile=$LIB -DgroupId=org.kidneyomics \
    -DartifactId=VCFAnalysisTools -Dversion=1.03 -Dpackaging=jar
