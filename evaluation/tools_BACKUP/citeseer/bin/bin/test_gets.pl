use LWP::UserAgent;
use Data::Dumper;
use XML::Bare;


my $uri = 'http://louise.ist.psu.edu/api/getdocs.xml?key=csxb0t';


my $ua = LWP::UserAgent->new;

my $r = $ua->get($uri);

if($r->is_success) {
	$n = new XML::Bare ( text => $r->content );
	my $rlist = $n->parse();
	$mapfs{'/data/csxcrawl/repository/'}='/export/dataksu/repository';
	$base = $rlist->{'response'}->{'location'}->{'value'};
	foreach $crawl (@{$rlist->{'response'}->{'doc'}}) {
		$fpfragment = $crawl->{'value'};
		$file = "$mapfs{$base}/$fpfragment";
		if(-e $file) { print "Yes\n"; }
	}
}
else {
	print "Can't download";

}
