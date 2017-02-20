#!/usr/bin/perl -CSDA

use strict;
use FindBin;
use lib "$FindBin::Bin/../lib";
use File::Copy;
use File::Basename;
use File::Path;
use FileConverter::Controller;
use DocFilter::Filter;
use ParsCit::Controller;
use HeaderParse::API::Parser;
use HeaderParse::Config::API_Config;
use LWP::UserAgent;
use XML::Bare;


$SIG{'ALRM'} = \&timeout;

sub timeout() {
  print "Exiting on timeout\n"; 
  die "ERROR::EOT";
}

my $FILE = $ARGV[0];
my $importDir = "../../www/php/upload/files/extracted";
my $xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

my $bOnlyPS = 0;

my $FAIL = -1;
my $PASS = 1;

my $counter = 0;

open (LOG, ">>prep.log");
open (ERR, ">>prep.err");


mkpath($importDir) unless(-e $importDir);
my ($filesref) = (&get_files());
#print "Processing $FILE\n";
import($FILE, $counter);
close FILES;
close LOG;
close ERR;

exit;

sub import {
    
    my ($filePath, $id) = @_;
    
    my ($status, $msg) = prep($filePath, $id);
    #open(STATUSOUT,">>status$$.out");
    #print STATUSOUT "$filePath $id $status\n";
    #close(STATUSOUT);
    if ($status == 0) {
        print "FAIL";
    }
    if ($status == 1) {
        print "SUCCESS";
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

    #my $metPath = $filePath.".met";
    $filePath =~ m/^.*(\.(ps|pdf)(\.g?z)?)$/i;
    my $ext = $1;
    my $fileid = &getfileID($filePath);
    my $targetPath = "$importDir/" . without_ext(basename($filePath)) . "$ext";
    #my $targetMET = "$importDir/" . without_ext(basename($filePath)) . ".met";
    unless(copy($filePath, $targetPath)) {
        return (0, "unable to copy: $!");
    }
    #print "COPY [OK] ";
    #unless(copy($metPath, $targetMET)) {
        #return (0, "unable to copy met file: $!");
    #}
    #print "COPY MET [OK] ";
    
    my $textFile;
    my $conversionSuccess = 0;


    eval { # External call going too slow ?
        alarm(600);
        my ($status, $msg, $textPath) = extractText($targetPath, $id);
        if ($status > 0) {
        $textFile = $textPath;
        my ($status, $msg) = filter($textFile);
            if ($status > 0) {
            $conversionSuccess = 1;
            }
        }else {
            post_message($fileid,$FAIL);
            unlink($targetPath); 
            #unlink($targetMET);
            return ($FAIL, "Conversion Timeout");
       }
        alarm(0);
    };
    
    if($@=~/ERROR::/) { 
        return ($FAIL,"Conversion Timeout");
    }
    
    #print "CONVERT [OK] ";
    my ($status, $msg) = filter($textFile);
    if ($status <= 0) {
        unlink($targetPath); 
        #unlink($targetMET);
        unlink($textFile);
        post_message($fileid,$FAIL);
        return ($status, $msg);
    }
    
    #print "FILTER [OK] ";
    my ($status, $msg) = extractCitations($textFile, $id);
    if ($status <= 0) {
        unlink($targetPath); 
        #unlink($targetMET);
        unlink($textFile);
        post_message($fileid,$FAIL);
        return ($status, $msg);
    }
    
    #print "CITATIONS [OK] ";
    my ($status, $msg) = extractHeader($textFile, $id);
    if ($status <= 0) {
        unlink($targetPath); 
        #unlink($targetMET);
        unlink($textFile);
        post_message($fileid,$FAIL);
        return ($status, $msg);
    }
    
    #print "HEADER [OK]";
    print "\n";
    post_message($fileid, $PASS);
    return (1, "");
    
}

sub post_message($$) {
    #my ($id,$state) = @_;
    #my $ua = LWP::UserAgent->new();
    #my $uri = "$status_ptr?key=$ptr_key&ids=$id&state=$state";
    #
    #eval { 
        #alarm(300);
        #my $ms = $ua->get($uri);
        #if($ms->is_success) {
            #return $PASS;
        #}
        #else{
            #print STDERR "Failed to update status \n";
            #return $FAIL;
        #}
        #alarm(0);
    #};

    #if($@=~/ERROR::/) {
        #return $FAIL;
    #}
}

sub get_files() {

    #my $ua = LWP::UserAgent->new;
    #my $uri = "http://louise.ist.psu.edu/api/getdocs.xml?key=$ptr_key&n=$maxCount"; 
    #my $r = $ua->get($uri);
    #my @fileList = ();
    #if($r->is_success) {
        #my $n = new XML::Bare( text => $r->content );
        #my $xmldoc = $n->parse();
        #my $base = $mapfs{$xmldoc->{'response'}->{'location'}->{'value'}};
        #foreach my $file_ptr (@{$xmldoc->{'response'}->{'doc'}}) {
            #my $fpfragment = $file_ptr->{'value'};
            #my $file = "$base/$fpfragment";
            #push @fileList,$file;
        #}
        #return \@fileList;
    #}
    #else {
        #return \@fileList;
    #}
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
    my ($status, $msg, $textFile, $rTrace, $rCheckSums) =  FileConverter::Controller::extractText($filePath);
    if ($status <= 0) {
        return ($status, $msg);
    } 
    else {
        unless(open(FINFO, ">$importDir/" . without_ext(basename($filePath)) . ".file")) {
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
    my ($sysStatus, $filterStatus, $msg) = DocFilter::Filter::filter($textFile);
    
    if ($sysStatus > 0) {
        if ($filterStatus > 0) {
            return (1);
        }
        else {
            return (0, "document failed filtration");
        }
    } 
    else {
        return (0, "An error occurred during filtration: $msg");
    }
}


sub extractCitations {
    
    my ($textFile, $id) = @_;
    my $rXML = ParsCit::Controller::extractCitations($textFile);

    unless(open(CITE, ">:utf8", "$importDir/" . without_ext(basename($textFile)) . ".parscit")) {
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

    my ($status, $msg, $rXML) =  HeaderParse::API::Parser::_parseHeader($textFile, $jobID);

    if ($status <= 0) {
        return ($status, $msg);
    }

    unless(open(HEAD, ">:utf8", "$importDir/" . without_ext(basename($textFile)) . ".header")) {
        return (0, "Unable to open header file: $!");
    }

    print HEAD $$rXML;
    close HEAD;
    return (1);

}

sub without_ext {
    my ($file) = @_;
    return substr($file, 0, rindex($file, '.'));
}
