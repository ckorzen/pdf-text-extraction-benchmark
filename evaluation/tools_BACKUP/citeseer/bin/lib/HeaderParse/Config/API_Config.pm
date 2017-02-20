package HeaderParse::Config::API_Config;

use FindBin qw($Bin);
require Exporter;
use vars qw($VERSION @ISA @EXPORT @EXPORT_OK %EXPORT_TAGS);

@ISA = qw(Exporter);
@EXPORT =  qw($SVM_Dir $offlineD $Database_Dir $Data_Dir $Tmp_Dir $nMinHeaderLength $nMaxHeaderLength $ServerURL $ServerPort $algName $algVersion);

#$SVM_Dir = "$FindBin::Bin/../svm-light/";
#$Database_Dir = "$FindBin::Bin/../lib/HeaderParse/database";
#$Data_Dir = "$FindBin::Bin/../lib/HeaderParse/data/";
#$offlineD = "$FindBin::Bin/../lib/HeaderParse/OfflineFiles/";

$HeaderParseHome = "$FindBin::Bin/..";

$SVM_Dir = "$HeaderParseHome/svm-light/";
$Database_Dir = "$HeaderParseHome/resources/database/";
$Data_Dir = "$HeaderParseHome/resources/data/";
$offlineD = "$HeaderParseHome/resources/models/";
$Tmp_Dir = "$HeaderParseHome/tmp";

$nMinHeaderLength = 50;
$nMaxHeaderLength = 2500;

%repositories = ('example1' => '/',
		 'example2' => '/home',
		 );

$algName = "SVM HeaderParse";
$algVersion = 0.2;

$ServerPort = 40000;
$ServerURL = "130.203.152.158";
