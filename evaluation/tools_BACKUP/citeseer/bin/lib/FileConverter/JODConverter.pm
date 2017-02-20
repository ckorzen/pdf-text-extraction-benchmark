package FileConverter::JODConverter;
#
# Wrapper to execute the JODConverter command-line tool for converting
# doc, rtf to PDF files.
#
# Juan Pablo Fernandez Ramirez, 10/05/07
#
use strict;
use FileConverter::Config;
use FileConverter::Utils;

my $JODConverterLoc = $FileConverter::Config::JODConverterPath;

##
# Execute the JODConverter utility.
##
sub convertFile {
    my ($filePath, $rTrace, $rCheckSums) = @_;
    my ($status, $msg) = (1, "");
    
    if (FileConverter::Utils::checkProcess("soffice") == 0) {
    	return (0, "Open Office Service is not running");
    }
    
    my $pdfFilePath = FileConverter::Utils::changeExtension($filePath, "pdf");
    my @commandArgs = ("java", "-jar", $JODConverterLoc, $filePath, 
        $pdfFilePath);
    system(@commandArgs);
    
    if ($? == -1) {
        return (0, "Failed to execute JODConverter: $!");
    } elsif ($? & 127) {
        return (0, "Java died with signal ".($? & 127));
    }

    my $code = $?>>8;
    if ($code == 0) {
        push @$rTrace, "JODConverter";

	my $sha1 = FileConverter::CheckSum->new();
	$sha1->digest($filePath);
	push @$rCheckSums, $sha1;

        return ($status, $msg, $pdfFilePath, $rTrace, $rCheckSums);
    } else {
        return (0, "Error executing JODConverter (code $code): $!");
    }
} # convertFile
1;
