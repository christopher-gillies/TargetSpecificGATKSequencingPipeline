# TargetSpecificGATKSequencingPipeline
This package takes FASTQ files and produces a filtered VCF file

# Setup 
* this has been tested on 

# Install dependencies

### git
```
sudo apt-get install git
```

### If you are interested in read trimming
* cutadapt https://cutadapt.readthedocs.org/en/stable/installation.html
* install pip and cutadapt
 
 ```
sudo apt-get install build-essential
sudo apt-get install python-dev
sudo apt-get install python-dev
sudo apt-get install python-pip
pip install --user --upgrade cutadapt
or
sudo pip install --upgrade cutadapt
```

### If you are interested in aligning and variant calling

* GATK 
* https://www.broadinstitute.org/gatk/download/
* You will need an account to download it
```
# the name sequencing_programs does not matter
mkdir ~/sequencing_programs
cd ~/sequencing_programs
cp ~/Downloads/GenomeAnalysisTK-3.4-46.tar.bz2 .
tar -xvf GenomeAnalysisTK-3.4-46.tar.bz2
```

* samtools
```
cd ~/sequencing_programs
git clone git://github.com/samtools/samtools.git  
cd samtools
```






###To build you will need to install VCFAnalysisTools jar into your local maven repository
export LIB=./lib/VCFAnalysisTools-1.03.jar
mvn install:install-file -Dfile=$LIB -DgroupId=org.kidneyomics \
    -DartifactId=VCFAnalysisTools -Dversion=1.03 -Dpackaging=jar
