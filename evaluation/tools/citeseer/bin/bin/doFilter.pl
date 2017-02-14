#!/usr/bin/perl -CSDA

use strict;
use FindBin;
use lib "$FindBin::Bin/../lib";
use DocFilter::Filter;

my $textFile = $ARGV[0];

my ($sysStatus, $filterStatus, $msg) = DocFilter::Filter::filter($textFile);
    
if ($sysStatus > 0) {
	if ($filterStatus > 0) {
		print 1;
  }
  else {
		print 0;
  }
} 
else {
	print -1;
}
    