#!/usr/bin/perl -w
package HeaderParse::API::Parser;
use strict;
use utf8;
use HeaderParse::API::ParserMethods;
use IO::Handle;
use HeaderParse::Config::API_Config;
use vars qw($ServerURL $repositoryLocation $algVersion);


sub _parseHeader{
    my ($fileID, $jobID) = @_;
    my ($header, $faultMessage, $rResponse, $success, $papertext);

    my $status = 1;
    my $msg = "";

#    my $file = "$repositoryLocation/$fileID";
#    print "file: $file\n";
    my $file = $fileID;

    if (! -e $file) {
	return fatal("File does not exist: $file");
    }

    open(IN, "<:utf8", $file) or
	return fatal("Could not open file: $file");
    {
	local $/ = undef;
	$papertext = <IN>;
    }
    close IN;

    ($faultMessage, $header) =
        &HeaderParse::API::ParserMethods::ExtractHeaderInformation(\$papertext);
    if(!length($faultMessage)){
        $rResponse =
	    &HeaderParse::API::ParserMethods::Parse($header, $jobID);
	if ($rResponse eq "0") {
	    return fatal("Timeout while parsing");
	}
	return ($status, $msg, $rResponse);

    } else{
        #error occured while extracing the header
	return fatal("file $file: $faultMessage");
    }

}

sub fatal {
    my ($msg) = @_;
    return (0, $msg, undef);
}

1;
