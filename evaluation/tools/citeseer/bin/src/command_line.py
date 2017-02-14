import sys
import os
import os.path
import subprocess
import traceback

from utilities import Util
from extraction import Extraction

# Root folder is the parent directory of the directory in which this script is 
# located.
FOLDER      = os.path.dirname(os.path.realpath(__file__))
ROOT_FOLDER = os.path.dirname(FOLDER)

global utilities
utilities = Util()

"""
Because CiteSeerX doesn't provide a command line tool, but only an HTTP API, 
we implement it on our own. It takes a PDF file and extracts scholarly 
informations from it, using the methods provided by CiteSeerX. It basically 
does the same routine from API - without any HTTP stuff.
"""

def process_pdf(pdf_path, out_path):    
    pdf_path = os.path.realpath(pdf_path)
    out_path = os.path.realpath(out_path)
        
    # As in the API (service.py, line 105), extract text from PDF using pdfbox.
    txt_file = pdf2text(pdf_path, out_path)
        
    # Extract *ALL* scholarly informations, as in service.py, lines 38-83.
    extract(txt_file, out_path)

def pdf2text(pdf_path, out_path):
    """
    Calls pdfbox to convert a pdf file into text file. 
    Returns the path of the text file
    """
    
    target_path = out_path + ".pre"
    
    cmd = ["java", "-jar",
            os.path.join(ROOT_FOLDER, "pdfbox/pdfbox-app-1.8.1.jar"),
            "ExtractText",
            pdf_path,
            target_path
          ]
    ret = subprocess.call(cmd)
    
    # Raise an error if text extraction failed
    if ret > 0: 
        raise IOError
        
    return target_path

def extract(txt_file, out_path):
    """
    Extracts *ALL* scholarly informations from given txt file and writes it to
    given out path.
    """
        
    extractor = Extraction()
    data = ""
        
    try:
        # Extract the header.    
        # data = data + extractor.extractHeaders(txt_file)
        # Extract the body.
        data = data + extractor.extractBody(txt_file)
        # Extract the references.
        #data = data + extractor.extractCitations(txt_file)
        # Extract the keyphrases
        #data = data + extractor.extractKeyphrases(txt_file)
        
        data = utilities.printXML(data)
    except (IOError, OSError) as er: #Internal error, i.e. during extraction
        traceback.print_exc()
        data = ""
    
    # Write the data to file.    
    with open(out_path, "w+") as f:
        f.write(data)

if __name__ == "__main__":
    pdf = sys.argv[1]
    out = sys.argv[2]

    process_pdf(pdf, out)    

