# TargetSpecificGATKSequencingPipeline
This package takes FASTQ files and produces a filtered VCF file

# Setup 
* This has been tested on Ubuntu 14.04.3
* Since this is a pipeline that utilizes many other programs there are many dependencies. 
* I have tried to outline the steps to installing all the necessary dependencies from a fresh default Ubuntu 14.04.04 installation.

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

### If you want to use the svm filter
You will need to install R. Unfortunately, Ubuntu 14.04 has an old version of R so we have to install it a non-standard way
```
cd ~/
sudo apt-get install liblapack3
sudo apt-get install libgfortran3
sudo apt-get install libblas3
sudo apt-get install gfortran
wget http://cran.es.r-project.org/bin/linux/ubuntu/trusty/r-base-core_3.2.2-1trusty0_amd64.deb
sudo dpkg -i r-base-core_3.2.2-1trusty0_amd64.deb
sudp Rscript -e 'install.packages("ggplot2",repos="http://cran.wustl.edu/")' 
sudo Rscript -e 'install.packages("gridExtra",repos="http://cran.wustl.edu/")' 
sudo Rscript -e 'install.packages("e1071",repos="http://cran.wustl.edu/")' 
sudo Rscript -e 'source("https://bioconductor.org/biocLite.R"); biocLite("impute")' 
```

# Download Reference files
* note that there are about 20gb in reference files
```
mkdir ~/sequencing_reference_files
wget -c -r --no-parent --reject "index.html*" \
"http://glom.sph.umich.edu/TargetedSequencingPipelineReferences/" -nH --cut-dirs=1
```

# Use Release version of TargetSpecificGATKSequencingPipeline



# Use Source version of TargetSpecificGATKSequencingPipeline

###To build you will need to install VCFAnalysisTools jar into your local maven repository
export LIB=./lib/VCFAnalysisTools-1.03.jar
mvn install:install-file -Dfile=$LIB -DgroupId=org.kidneyomics \
    -DartifactId=VCFAnalysisTools -Dversion=1.03 -Dpackaging=jar
