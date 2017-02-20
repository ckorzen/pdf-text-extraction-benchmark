#!/usr/bin/perl -CSDA

use strict;
use FindBin;
use lib "$FindBin::Bin/../lib";
use ParsCit::Controller;

my $textFile = $ARGV[0];

my $rXML = ParsCit::Controller::extractCitations($textFile);
print $$rXML;
