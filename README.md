# TargetSpecificGATKSequencingPipeline
This package takes FASTQ files and produces a filtered VCF file

# Setup 
* This has been tested on Ubuntu 14.04.3
* You will need at least 8gb of ram to run this
* Since this is a pipeline that utilizes many other programs there are many dependencies. 
* I have tried to outline the steps to installing all the necessary dependencies from a fresh default Ubuntu 14.04.04 installation.
* This pipeline should work on most Linux distributions and Mac provided that you install the dependencies

# Install dependencies

### git
```
sudo apt-get install git
```

### java
* GATK does not support openjdk so you should install java from oracle
* http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html
* 
```
cd ~/Downloads/
tar -xvf jdk-7u79-linux-x64.tar.gz
sudo mkdir -p /usr/lib/jvm
sudo mv ./jdk1.7.0_79 /usr/lib/jvm/
sudo update-alternatives --install "/usr/bin/java" "java" "/usr/lib/jvm/jdk1.7.0_79/bin/java" 2000
sudo update-alternatives --install "/usr/bin/javac" "javac" "/usr/lib/jvm/jdk1.7.0_79/bin/javac" 2000
sudo update-alternatives --install "/usr/bin/javaws" "javaws" "/usr/lib/jvm/jdk1.7.0_79/bin/javaws" 2000

java -version
# Result
# java version "1.7.0_79"
# Java(TM) SE Runtime Environment (build 1.7.0_79-b15)
# Java HotSpot(TM) 64-Bit Server VM (build 24.79-b02, mixed mode)
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

* Install tabix
```
sudo apt-get install tabix
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
* Note that there are about 20gb in reference files
```
mkdir ~/sequencing_reference_files
wget -c -r --no-parent --reject "index.html*" \
"http://glom.sph.umich.edu/TargetedSequencingPipelineReferences/" -nH --cut-dirs=1
```
* Note you should reindex the tabix files just incase the tbi file was copied before the vcf. This is recommended because GATK can throw error messages when the tbi file is out of date.
```
cd ~/sequencing_reference_files
tabix -f -pvcf Mills_and_1000G_gold_standard.indels.hg19.sites.relabel.vcf.gz
tabix -f -pvcf 1000G_phase1.indels.hg19.sites.relabel.vcf.gz
tabix -f -pvcf 1000G_omni2.5.hg19.sites.relabel.vcf.gz
tabix -f -pvcf ALL.phase3.combined.sites.unfiltered.vcf.gz
tabix -f -pvcf dbsnp_138.hg19.relabel.vcf.gz
tabix -f -pvcf ExAC.r0.3.sites.vep.vcf.gz
tabix -f -pvcf hapmap_3.3.hg19.sites.relabel.vcf.gz
```


# If you want to use Release version of TargetSpecificGATKSequencingPipeline then follow the steps below
```
cd ~/sequencing_programs
wget https://github.com/christopher-gillies/TargetSpecificGATKSequencingPipeline/raw/master/release/TargetSpecificGATKSequencingPipeline-0.1.jar
wget https://raw.githubusercontent.com/christopher-gillies/TargetSpecificGATKSequencingPipeline/master/example.ubuntu.application.properties
```

* Change the username from cgillies to YOUR_USERNAME in the example.ubuntu.application.properties file

```
cat ~/sequencing_programs/example.ubuntu.application.properties | \
perl -lane '$_ =~ s/cgillies/YOUR_USERNAME/; print $_' > \
~/sequencing_programs/ubuntu.application.properties
```

* Make sure the paths are still valid for the files in the ubuntu.application.properties. They should be hopefully be if you have followed this tutorial exactly.

* Test that the pipeline runs
```
export PIPELINE=~/sequencing_programs/TargetSpecificGATKSequencingPipeline-0.1.jar
export CONF=~/sequencing_programs/ubuntu.application.properties
java -jar $PIPELINE --conf $CONF --help
```
* The program successfully started up if the help menu is displayed


# If you wan to use source version of TargetSpecificGATKSequencingPipeline then follow the steps below
* Clone repository
```
export JAVA_HOME=/usr/lib/jvm/jdk1.7.0_79/
cd ~/sequencing_programs/
git clone https://github.com/christopher-gillies/TargetSpecificGATKSequencingPipeline.git
```
* Install maven dependencies in lib folder
```
mvn install:install-file -Dfile=./lib/picard-1.107.jar -DgroupId=net.sf.picard -DartifactId=picard -Dversion=1.107 -Dpackaging=jar

mvn install:install-file -Dfile=./lib/sam-1.107.jar -DgroupId=net.sf.samtools -DartifactId=sam -Dversion=1.107 -Dpackaging=jar

mvn install:install-file -Dfile=./lib/tribble-1.107.jar -DgroupId=org.broad.tribble \
    -DartifactId=tribble -Dversion=1.107 -Dpackaging=jar

mvn install:install-file -Dfile=./lib/VCFAnalysisTools-1.03.jar -DgroupId=org.kidneyomics \
    -DartifactId=VCFAnalysisTools -Dversion=1.03 -Dpackaging=jar
```
* Build the package
```
cd ~/sequencing_programs/TargetSpecificGATKSequencingPipeline/
mvn package
```
* Setup configureation
```
cat ~/sequencing_programs/TargetSpecificGATKSequencingPipeline/example.ubuntu.application.properties | \
perl -lane '$_ =~ s/cgillies/YOUR_USERNAME/; print $_' > \
~/sequencing_programs/TargetSpecificGATKSequencingPipeline/ubuntu.application.properties
```

* Test that the pipeline runs
```
export PIPELINE=~/sequencing_programs/TargetSpecificGATKSequencingPipeline/target/TargetSpecificGATKSequencingPipeline-0.1.jar
export CONF=~/sequencing_programs/TargetSpecificGATKSequencingPipeline/ubuntu.application.properties
java -jar $PIPELINE --conf $CONF --help
```
* The program successfully started up if the help menu is displayed

# Download test data about 2GB
```
cd ~/
git clone https://github.com/christopher-gillies/FluidigmTestData.git
cd FluidigmTestData
ls *.gz | perl -F"_" -lane 'use Cwd; my $dir = Cwd::getcwd(); print "$F[0]\t$dir/$_"' > fastq.list.txt
```

# How do I use the adapter trimming pipeline?
```
export OUT_DIR=~/FluidigmTestData/trim
export FASTQ_FILE_LIST=~/FluidigmTestData/fastq.list.txt
export PIPELINE=~/sequencing_programs/TargetSpecificGATKSequencingPipeline-0.1.jar
export CONF=~/sequencing_programs/ubuntu.application.properties
mkdir $OUT_DIR
java -jar $PIPELINE --conf $CONF --command trim --tspl 20 --minAdapterOverlap 7 \
--adapter1 AGACCAAGTCTCTGCTACCGTA --adapter2 TGTAGAACCATGTCGTCAGTGT --maxErr 0.05 \
--output $OUT_DIR --fastqFiles $FASTQ_FILE_LIST
cd $OUT_DIR
# 2 is the number of jobs to run
make -j 2
```

# How do I use alignment pipeline?
## Create gene list
```
export OUT_DIR=~/FluidigmTestData/align
export PIPELINE=~/sequencing_programs/TargetSpecificGATKSequencingPipeline-0.1.jar
export CONF=~/sequencing_programs/ubuntu.application.properties
mkdir $OUT_DIR
export GENES="INF2
NPHS2
NPHS1
WT1
"
export GENES_STR=$(echo $GENES | perl -lane 'print join(",",@F)')
java -jar $PIPELINE --command makeLocations --conf $CONF \
--output $OUT_DIR/genes.intervals \
--genes "$GENES_STR"
```
* This may take a couple minutes because it is going to scan through all elements in genecode

```
export OUT_DIR=~/FluidigmTestData/align
export FASTQ_FILE_LIST=~/FluidigmTestData/trim/fastq.list.txt
export PIPELINE=~/sequencing_programs/TargetSpecificGATKSequencingPipeline-0.1.jar
export CONF=~/sequencing_programs/ubuntu.application.properties
java -jar $PIPELINE --command align --output $OUT_DIR --fastqFiles $FASTQ_FILE_LIST --primerLocations $OUT_DIR/genes.intervals --conf $CONF
cd $OUT_DIR
# 1 is the number of jobs to run
make -j 1
```

