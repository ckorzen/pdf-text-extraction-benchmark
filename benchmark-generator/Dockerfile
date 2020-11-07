FROM maven:3.6.3-jdk-8

RUN apt-get update && apt-get install -y make

# Download JavaCC (needed to build the TeX parser).
RUN wget https://repo1.maven.org/maven2/net/java/dev/javacc/javacc/7.0.4/javacc-7.0.4.jar

# Copy "commons" and install it.
COPY ./commons /commons
RUN cd commons && mvn -DskipTests install

# Copy "tex-parser" and install it.
COPY ./tex-parser /tex-parser
RUN cd /tex-parser/src/main/java/parse/ && java -classpath /javacc-7.0.4.jar javacc TeXParser.jj
RUN cd tex-parser && mvn -DskipTests install

# Copy "tex-paragraph-parser" and install it.
COPY ./tex-paragraph-parser /tex-paragraph-parser
RUN cd tex-paragraph-parser && mvn -DskipTests install

COPY Makefile Makefile

CMD ["/bin/bash"]

# Build the Docker container:
# docker build -t arxiv-benchmark-generator .

# Run the Docker container:
# docker run -it -v <input-dir-with-tex-files>:/input -v <target-dir>:/output arxiv-benchmark-generator