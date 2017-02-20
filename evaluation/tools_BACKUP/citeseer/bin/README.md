# CiteSeerExtractor

This code provides a RESTful API to the extraction code that is currently in use in the [CiteSeerX academic digital library](http://citeseerx.ist.psu.edu).

*The code is still in development and is not feature complete yet and does not fail gracefully*

The code is runnable as a stand-alone Web server.

### Dependencies
* Python 2.7
* Java 6
* web.py python module
* String::Approx perl module
* xmltodict (for xml to json conversion)
* python-magic (magic.py) python module

### Installation
1. Get the code
2. Install web.py `pip install web.py`
3. Install String::Approx  `cpan String::Approx`
4. Install xmltodict `pip install xmltodict`
5. Install python-magic `pip install python-magic`

#### 64-bit Architectures
On 64-bit systems you'll need support for 32-bit applications. Please install the appropriate package for your distribution.

Ubuntu: `sudo apt-get install ia32-libs-multiarch`

RHEL/CentOS: `sudo yum install glibc.i686 libstdc++.i686`

### Run
`python service.py [port]` and navigate to http://localhost:port/extractor and follow the instructions for different types of extraction

