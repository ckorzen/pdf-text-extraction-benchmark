#!/opt/ActivePerl-5.8/bin/perl -CSD
use strict;
use FindBin;
use lib "$FindBin::Bin/../lib";
use File::Copy;
use File::Basename;
use FileConverter::Controller;
use DocFilter::Filter;
use ParsCit::Controller;
use HeaderParse::API::Parser;
use HeaderParse::Config::API_Config;
use LWP::UserAgent;



my $shuyiBase = "/export/dataksu";
my $fileList = "/export/dataksu/ingest/ingest_list";
my $importDir = "/data/exports/set9";
# http://louise.ist.psu.edu/api/setdocs.xml?ids=2935216,2935215,2935214&state=0&key=abcd
my $status_ptr = "http://louise.ist.psu.edu/api/setdocs.xml";
my $ptr_key = "csxb0t";


my $xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

my $start = $ARGV[0];
my $end = $ARGV[1];

my $bOnlyPS = 0;

if (!$start || !$end) {
    print "Usage: $0 start end\n";
    exit;
}

my $FAIL = -1;
my $PASS = 1;

my $counter = 0;


open (LOG, ">>prep.log");
open (ERR, ">>prep.err");

open(FILES, "<$fileList") or die $!;
while(<FILES>) {
    $counter++;
    if ($counter<$start || $counter>$end) {
	next;
    }
    chomp();
    my $file = $_;
    if ($file =~ m/^\s*$/) {
	next;
    }
    if ($bOnlyPS > 0 && $file !~ m/\.ps(\.g?z)*$/i) {
	next;
    }
   # my $filePath = "$shuyiBase/$file";
    my $filePath = "$file";
#    if (! -e $filePath) {
#	print "not found: $filePath\n";
#	next;
#    }
#    print "$filePath\n";
    import($filePath, $counter);

    if (($counter%1000)==0) {
	print "finished $counter\n";
    }
}
close FILES;
close LOG;
close ERR;

exit;

sub import {
    my ($filePath, $id) = @_;
    
    my ($status, $msg) = prep($filePath, $id);
    open(STATUSOUT,">>status$$.out");
    print STATUSOUT "$filePath $id $status\n";
    close(STATUSOUT);
    if ($status == 0) {
	print ERR "$id: $msg\n";
    }
    if ($status == 1) {
	print LOG "$id\n";
    }
}


sub getfileID($) {
	my $filepath = shift;
	my @pathE = fileparse($filepath);
	my $filename = $pathE[0];
	$filename =~s/\.(pdf|ps)(\.gz)?$//g;
	$filename =~s/\.//g;
	$filename=~s/^[0]+//g;
	return $filename;
}


sub prep {
    my ($filePath, $id) = @_;

    my $metPath = $filePath.".met";

#    $filePath =~ m/^.*\/(.*)$/;
#    my $fn = $1;
    $filePath =~ m/^.*(\.(ps|pdf)(\.g?z)?)$/i;
    my $ext = $1;

    my $fileid = &getfileID($filePath);
    my $targetPath = "$importDir/$id$ext";
    my $targetMET = "$importDir/$id.met";

    unless(copy($filePath, $targetPath)) {
	return (0, "unable to copy: $!");
    }
    print "COPY [OK] ";
    unless(copy($metPath, $targetMET)) {
	return (0, "unable to copy met file: $!");
    }
    print "COPY MET [OK] ";
    my $textFile;
    my $conversionSuccess = 0;

    my ($status, $msg, $textPath) = extractText($targetPath, $id);
    if ($status > 0) {
	$textFile = $textPath;
	my ($status, $msg) = filter($textFile);
	if ($status > 0) {
	    $conversionSuccess = 1;
	}
    } else {
	post_message($fileid,$FAIL);
	return ($status, $msg);
    }
    print "CONVERT [OK] ";
    my ($status, $msg) = filter($textFile);
    if ($status <= 0) {
	post_message($fileid,$FAIL);
	return ($status, $msg);
    }
    print "FILTER [OK] ";
    my ($status, $msg) = extractCitations($textFile, $id);
    if ($status <= 0) {
	post_message($fileid,$FAIL);
	return ($status, $msg);
    }
    print "CITATIONS [OK] ";
    my ($status, $msg) = extractHeader($textFile, $id);
    if ($status <= 0) {
	post_message($fileid,$FAIL);
	return ($status, $msg);
    }    
    print "HEADER [OK]";
    print "\n";
    post_message($fileid, $PASS);
    return (1, "");
}

sub post_message($$) {
	my ($id,$state) = @_;
	my $ua = LWP::UserAgent->new();
	my $ms = $ua->post($status_ptr, 'key' => $ptr_key, 'ids' => $id, 'state' => $state);
	if($ms->is_success) {
		return $PASS;
	}
	else {
		return $FAIL;
	}
}



sub checkPDF {
    my $url = shift;
    if ($url =~ m/pdf(\.g?z)?$/i) {
	return 1;
    } else {
	return 0;
    }
}


sub extractText {
    my ($filePath, $id) = @_;
    my ($status, $msg, $textFile, $rTrace, $rCheckSums) =
	FileConverter::Controller::extractText($filePath);
    if ($status <= 0) {
	return ($status, $msg);
    } else {
	unless(open(FINFO, ">$importDir/$id.file")) {
	    return (0, "unable to write finfo: $!");
	}
	print FINFO $xmlHeader;
	print FINFO "<conversionTrace>";
	print FINFO join ",", @$rTrace;
	print FINFO "</conversionTrace>\n";
	print FINFO "<checksums>\n";
	foreach my $checkSum(@$rCheckSums) {
	    print FINFO "<checksum>\n";
	    print FINFO "<fileType>".$checkSum->getFileType()."</fileType>\n";
	    print FINFO "<sha1>".$checkSum->getSHA1()."</sha1>\n";
	    print FINFO "</checksum>\n";
	}
	print FINFO "</checkSums>\n";
	close FINFO;
    }
    return (1, "", $textFile);
}


sub filter {
    my $textFile = shift;
    my ($sysStatus, $filterStatus, $msg) =
	DocFilter::Filter::filter($textFile);
    if ($sysStatus > 0) {
	if ($filterStatus > 0) {
	    return (1);
	} else {
	    return (0, "document failed filtration");
	}
    } else {
	return (0, "An error occurred during filtration: $msg");
    }
}


sub extractCitations {
    my ($textFile, $id) = @_;

    my $rXML = ParsCit::Controller::extractCitations($textFile);

    unless(open(CITE, ">:utf8", "$importDir/$id.parscit")) {
	return (0, "Unable to open parscit file: $!");
    }

    print CITE $$rXML;
    close CITE;
    return (1);
}


sub extractHeader {
    my ($textFile, $id) = @_;

    my $jobID;
    while($jobID = rand(time)) {
	unless(-f $offlineD."$jobID") {
	    last;
	}
    }

    my ($status, $msg, $rXML) =
	HeaderParse::API::Parser::_parseHeader($textFile, $jobID);

    if ($status <= 0) {
	return ($status, $msg);
    }

    unless(open(HEAD, ">:utf8", "$importDir/$id.header")) {
	return (0, "Unable to open header file: $!");
    }

    print HEAD $$rXML;
    close HEAD;
    return (1);

}
