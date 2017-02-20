#!/usr/bin/perl -CSDA

use strict;
use Encode;
use File::Basename;
use File::Path;

my $FILE = $ARGV[0];
my $importDir = dirname($FILE);

$FILE = basename(without_ext($FILE));

my $relDir = $importDir;
my $repository = "simseer";


if (! -e "$importDir/$FILE.txt") {
#         return 0;
}
my $filePath = $importDir;
if ( -e "$importDir/$FILE.pdf") {
    $filePath .= "/$FILE.pdf";
} elsif ( -e "$importDir/$FILE.PDF") {
    $filePath .= "/$FILE.PDF";
} elsif ( -e "$importDir/$FILE.ps") {
    $filePath .= "/$FILE.ps";
} elsif ( -e "$importDir/$FILE.PS") {
    $filePath .= "/$FILE.PS";
} else {
    print "$FILE: no pdf or ps\n";
}

my $xml = "<document id=\"unset\">\n";
# open(IN, "<:utf8", "$importDir/$FILE.file") or die("No file at $importDir/$FILE.file");
# $xml .= "<fileInfo>\n";
# $xml .= "<repository>$repository</repository>\n";
# $xml .= "<filePath>$filePath</filePath>\n";
# $xml .= "<bodyFile>$relDir/$FILE.body</bodyFile>\n";
# $xml .= "<citeFile>$relDir/$FILE.cite</citeFile>\n";
# 
# while(<IN>) {
#     if (m/xml version/) {
# #             next;
#     }
# 
#     s/checksum/checkSum/g;
#     $xml .= $_;
# 
# };
# 
# close IN;
# $xml .= "</fileInfo>\n";
open(IN, "<:utf8", "$importDir/$FILE.header") or die("No file at $importDir/$FILE.file");

while(<IN>) {
    $xml .= $_;
}
close IN;

open(IN, "<:utf8", "$importDir/$FILE.parscit") or die("No file at $importDir/$FILE.file");
while(<IN>) {
    $xml .= $_;
}
close IN;

$xml .= "</document>\n";
$xml =~ s/<[\/]*algorithm.*>//g;

open (XML, ">:utf8", "$importDir/$FILE.xml") or die "$FILE: could not open xml file for writing";
print XML $xml;
close XML;

print "SUCCESS";

sub without_ext {
    my ($file) = @_;
    return substr($file, 0, rindex($file, '.'));
}