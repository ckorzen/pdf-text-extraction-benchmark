package FileConverter::Config;

use FindBin;

## Conversion utilities

# valid options are TET or PDFBOX
$PDFTOTEXT = "TET";

# valid options are TEXT or PDF
$PSConversion = "TEXT";

$TETPath = "$FindBin::Bin/../converters/TET-3.0-Linux/bin/tet";

$TETLicensePath =
    "$FindBin::Bin/../converters/TET-3.0-Linux/licensekeys.txt";

$PDFBoxLocation = "$FindBin::Bin/../converters/PDFBox/PDFBox-0.7.3.jar";

$JODConverterPath = 
    "$FindBin::Bin/../converters/jodconverter-2.2.0/jodconverter-cli-2.2.0.jar";

$PrescriptPath = "/usr/local/bin/prescript";

## Compression utilities

$gunzip = "/usr/bin/gunzip";
$uncompress = "/usr/bin/uncompress";
$unzip = "/usr/bin/unzip";


## Repository Mappings

%repositories = ('example1' => '/',
        		 'example2' => '/home',
                );


## WS settings

$serverURL = '127.0.0.1';
$serverPort = 10888;
$URI = 'http://citeseerx.org/fileConversion/wsdl';

1;
