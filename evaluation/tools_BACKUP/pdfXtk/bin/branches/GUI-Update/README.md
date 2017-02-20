pdfxtk
======

PDF Extraction Toolkit


= logging
please use commons.logging


= troubleshooting

to install a file into local maven repository:
mvn install:install-file -DgroupId=com.touchgraph -DartifactId=touchgraph-mod -Dversion=1.0 -Dpackaging=jar -Dfile=${your.file}


# touchgraph
TODO: what is it ?



# sonar
you need to install sonar locally: http://www.sonarqube.org/downloads/
after that adopt the property 'sonar.host.url' in parent pom.xml

to run use:
mvn sonar:sonar