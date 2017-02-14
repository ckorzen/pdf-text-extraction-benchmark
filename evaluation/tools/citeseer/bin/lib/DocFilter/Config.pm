package DocFilter::Config;


## Global

$algorithmName = "BasicDocFilter";
$algorithmVersion = "1.0";


## Repository Mappings

%repositories = ('example1' => '/',
		 'example2' => '/home',
		 );


## WS Settings

$serverURL = '127.0.0.1';
$serverPort = 10666;
$URI = 'http://citeseerx.org/algorithms/docfilter/wsdl';

1;
