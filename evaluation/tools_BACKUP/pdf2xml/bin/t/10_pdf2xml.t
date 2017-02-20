#!/usr/bin/perl
#-*-perl-*-

use utf8;
use FindBin qw( $Bin );

use Test::More;
use File::Compare;

system("$Bin/../pdf2xml -r -x $Bin/french.pdf > output.xml 2>/dev/null");
is( my_compare( "output.xml", "$Bin/data/french.pdfxtk.xml" ),0, "pdf2xml (pdfxtk)" );

system("$Bin/../pdf2xml -r -x -T $Bin/french.pdf > output.xml 2>/dev/null");
is( my_compare( "output.xml", "$Bin/data/french.tika.xml" ),0, "pdf2xml (Apache Tika)" );

system("$Bin/../pdf2xml -r -x -m -T -l $Bin/word-list.txt $Bin/french.pdf > output.xml 2>/dev/null");
is( my_compare( "output.xml", "$Bin/data/french.voc.xml" ),0, "pdf2xml (wordlist)" );

system("$Bin/../pdf2xml -r -x -m -T $Bin/french.pdf > output.xml 2>/dev/null");
is( my_compare( "output.xml", "$Bin/data/french.dehyphenated.xml" ),0, "pdf2xml (skip merge)" );

system("$Bin/../pdf2xml -r -x -m -h -T $Bin/french.pdf > output.xml 2>/dev/null");
is( my_compare( "output.xml", "$Bin/data/french.raw.xml" ),0, "pdf2xml (raw)" );


# cleanup ....

unlink('output.xml');
done_testing;



# there is one line that destroys the tests! take it away!
# meta includes localized time! --> remove

sub my_compare{
    my ($file1,$file2) = @_;
    system("grep -v '(U ο υ a vu Q' $file1 | grep -v '<meta' > $file1.tmp");
    system("grep -v '(U ο υ a vu Q' $file2 | grep -v '<meta' > $file2.tmp");
    my $ret = compare("$file1.tmp","$file2.tmp");
    unlink("$file1.tmp");
    unlink("$file2.tmp");
    return $ret;
}
