#!/usr/bin/perl -CSDA

use strict;
use FindBin;
use lib "$FindBin::Bin/../lib";
use HeaderParse::API::Parser;
use HeaderParse::Config::API_Config;

my $textFile = $ARGV[0];

my ($status, $msg, $rXML) =  HeaderParse::API::Parser::_parseHeader($textFile, 0);

if ($status <= 0) {
    print "$status  $msg";
}
else{
    print $$rXML;
}

