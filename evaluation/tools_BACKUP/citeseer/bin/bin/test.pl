#!/usr/bin/perl -CSD
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

my $DIR = $ARGV[0] || $$;

my $shuyiBase = "/export/dataksu";
my $fileList = "/export/dataksu/ingest/ingest_list";
my $importDir = "/data/exports/set$DIR";
# http://louise.ist.psu.edu/api/setdocs.xml?ids=2935216,2935215,2935214&state=0&key=abcd
# #my $get_ptr = 'http://louise.ist.psu.edu/apisub/getdocs.xml';
my $status_ptr = "http://louise.ist.psu.edu/apisub/setdocs.xml";
my $ptr_key = "csxb0t";
my %mapfs = ();
my $maxCount = 3; 

$mapfs{"/data/csxcrawl/repository/"} = '/export/dataksu/repository';



my $xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

my $bOnlyPS = 0;

my $FAIL = -1;
my $PASS = 1;

my $counter = 0;


#open (LOG, ">>prep.log");
#open (ERR, ">>prep.err");

mkpath($importDir) unless(-e $importDir);
my ($filesref) = (&get_files());
print "Processing ", $#{$filesref} ," Links\n";
foreach my $file (@{$filesref}) {
    #print "FILE: ",$file,"\n";
    $counter++;
    if ($file =~ m/^\s*$/) {
        next;
    }
    if ($bOnlyPS > 0 && $file !~ m/\.ps(\.g?z)*$/i) {
        next;
    }
    my $filePath = "$file";
}

close FILES;
#close LOG;
#close ERR;

exit;

sub get_files() {
        my $ua = LWP::UserAgent->new;
        my $uri = "http://louise.ist.psu.edu/api/getdocs.xml?key=$ptr_key&n=3"; # changed api-> apisub
        my $r = $ua->get($uri);
        my @fileList = ();
        if($r->is_success) {
            my $n = new XML::Bare( text => $r->content );
            my $xmldoc = $n->parse();
            my $base = $mapfs{$xmldoc->{'response'}->{'location'}->{'value'}};
            foreach my $file_ptr (@{$xmldoc->{'response'}->{'doc'}}) {
                my $fpfragment = $file_ptr->{'value'};
                my $file = "$base/$fpfragment";
                push @fileList,$file;
            }
            return \@fileList;
        }
        else {
            return \@fileList;
        }
}

