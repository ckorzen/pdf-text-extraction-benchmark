package DocFilter::Filter;
##
##  Isaac Councill, 7/31/07
##
use strict;


sub filter {
    my ($filePath) = @_;

    if (!open (IN, "<$filePath")) {
	return (0, 0, "Could not open file $filePath: $!");
    }
    my $text;
    {
	local $/ = undef;
	$text = <IN>;
    }

    if (hasReferences(\$text) <= 0) {
	return (1, 0, "No reference section is present");
    }
    return (1, 1, "All filters passed");

} # filter


sub hasReferences {
    my $rText = shift;
    if ($$rText =~ /\b(REFERENCES?|References?|BIBLIOGRAPHY|Bibliography|REFERENCES AND NOTES|References and Notes)\:?\s*\n/sg) {
	return 1;
    } else {
	return 0;
    }

} # hasReferences


1;
