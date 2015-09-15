# TargetSpecificGATKSequencingPipeline
This package takes FASTQ files and produces a filtered VCF file

# Setup 
* this has been tested on Ubuntu 14.04.3

# Install dependencies

### git
```
sudo apt-get install git
```

### java
```
sudo apt-get install openjdk-6-jre-headless
```

### maven if you want to build source
```
sudo apt-get install maven
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

* htslib and samtools
```
sudo apt-get install libncurses5-dev
sudo apt-get install zlib1g-dev
cd ~/sequencing_programs
git clone https://github.com/samtools/htslib.git
cd htslib
make
cd ~/sequencing_programs
git clone git://github.com/samtools/samtools.git  
cd samtools
```

* bwa
```
cd ~/sequencing_programs
git clone https://github.com/lh3/bwa.git
make
```

* picard tools
```
cd ~/sequencing_programs
wget https://github.com/broadinstitute/picard/releases/download/1.139/picard-tools-1.139.zip
unzip picard-tools-1.139.zip
```

* qplot
```
cd ~/sequencing_programs
git clone https://github.com/statgen/libStatGen
cd libStatGen
make
cd ~/sequencing_programs
wget http://www.sph.umich.edu/csg/zhanxw/software/qplot/qplot-source.20130627.tar.gz
tar -xvf qplot-source.20130627.tar.gz
cd qplot
make
```

# Download Reference files
* note that there are about 20gb in reference files
```
mkdir ~/sequencing_reference_files
wget -r --no-parent --reject "index.html*" http://glom.sph.umich.edu/TargetedSequencingPipelineReferences/
```

# Use Release version of TargetSpecificGATKSequencingPipeline



# Use Source version of TargetSpecificGATKSequencingPipeline

###To build you will need to install VCFAnalysisTools jar into your local maven repository
export LIB=./lib/VCFAnalysisTools-1.03.jar
mvn install:install-file -Dfile=$LIB -DgroupId=org.kidneyomics \
    -DartifactId=VCFAnalysisTools -Dversion=1.03 -Dpackaging=jar
