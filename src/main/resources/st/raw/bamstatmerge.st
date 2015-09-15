use strict;
use warnings;

my $bamList = "<bamList>";
my $outFile = "<outFile>";


my $samples = {};
my @keys;

open(IN,$bamList) or die($bamList + " does not exist\n");
while(\<IN>) {
	chomp;
	my ($sample,$bam,$index,$statFile) = split(/\t/,$_);
	
	$samples->{$sample} = {};
	
	open(IN_2, $statFile) or die($statFile + " does not exist\n");
	while(\<IN_2>) {
		chomp;
		my ($key,$value) = split(/\t/);
		$samples->{$sample}->{$key} = $value;
	}
	close(IN_2);
	@keys = keys(%{$samples->{$sample}});
}
close(IN);

open(OUT, "> $outFile") or die("Can't open $outFile\n");
print OUT join("\t","SAMPLE_ID",@keys)."\n";
for my $sample (keys(%{$samples})) {
	my @vals = map { $samples->{$sample}->{$_} } @keys;
	print OUT join("\t",$sample,@vals)."\n";
}
close(OUT);


