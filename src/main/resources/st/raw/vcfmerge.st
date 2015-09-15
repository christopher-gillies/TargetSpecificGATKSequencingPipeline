use strict;
use warnings;


my $fileList = "<fileList>";
my $outDir = "<outDir>";
my $outPrefix = "<outPrefix>";


my $finalOut = $outDir.$outPrefix.".vcf";


################################################################################
#	GET FILE NAMES
################################################################################
my @vcfs = ();
open(IN,$fileList) or die("Cannot open $fileList");
while(\<IN>) {
	chomp;
	push(@vcfs,$_);
}


################################################################################
#	READ HEADER
################################################################################
my $cmdHeader = "cat $vcfs[0] | ";
open(HEADER,$cmdHeader) or die("Cannot open $cmdHeader\n");
my @headerLines = ();
while(\<HEADER>) {
	chomp;
	if($_ =~ /^#/) {
		push(@headerLines,$_);
	} else {
		last;
	}
}
close(HEADER);
my $vcfHeader = join("\n",@headerLines)."\n";

################################################################################
#	MERGE VCFs AND COMPRESS 
################################################################################
print STDERR "WRITING FINAL File $finalOut...\n";
open(OUT, " > $finalOut") or die("Cannot open $finalOut\n");
print OUT $vcfHeader;
foreach my $vcf (@vcfs) {
	my $cmdIn = "cat $vcf |";
	open(IN, $cmdIn) or die('Cannot open $vcf\n');
	print STDERR "WRITING $vcf..\n";
	while(\<IN>) {
		next if $_ =~ /^#/;
		print OUT $_;
	}
	close(IN);
}
close(OUT);
	