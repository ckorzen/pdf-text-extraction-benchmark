import web
import tempfile
import os
import subprocess
import xmltodict
import json
import cgi
import shutil
import magic

FOLDER      = os.path.dirname(os.path.realpath(__file__))
ROOT_FOLDER = os.path.dirname(FOLDER) + "/" # there must be a trailing /
TMP_FOLDER=tempfile.gettempdir()+"/citeseerextractor/" #Specifies temp folder - useful for cleaning up afterwards

class Util:
	"""
	Some utility methods for handling uploads and printing output
	Errors are caught in the calling classes
	"""
	def handleUpload(self, inObject):
		"""
		Handles upload coming from web.input, write it to a temp file, and return the path to that temp file
		"""
		web.debug(inObject['myfile'].filename) # This is the filename
                if not os.path.isdir(TMP_FOLDER): # Make sure the tmp folder exists
                    web.debug("Temp folder " + TMP_FOLDER + " is missing. Recreating it.")
                    os.mkdir(TMP_FOLDER, 0o700)
		handler, path = tempfile.mkstemp(dir=TMP_FOLDER)
		f = open(path,'w')
		f.write(inObject['myfile'].file.read())
		f.close()
		web.debug(path)
		return path
	
	def pdf2text(self, path):
		#"""
		#calls pdfbox to convert a pdf file into text file. 
		#returns the path of the text file
		#"""
		ret = subprocess.call(["java", "-jar", ROOT_FOLDER+"pdfbox/pdfbox-app-1.8.1.jar", "ExtractText", path, path+".txt"])
		""" Raise an error if text extraction failed """
		if ret > 0: 
			raise IOError
		return path+".txt"
		
	def ps2text(self, path):
		#"""
		#calls pdfbox to convert a pdf file into text file. 
		#returns the path of the text file
		#"""
		ret = subprocess.call(["ps2txt", path+".ps", path+".txt"])
		""" Raise an error if text extraction failed """
		if ret > 0:
			raise IOError
		return path+".txt"
			
	def typeFilter(self, path): 
		"""
		Pass in the pdfpath here, returns the uploaded file's MIME type
		"""	
		fileTypeString = magic.from_file(path, mime=True) # Stores the MIME string that describes the file type
		web.debug(fileTypeString)
		return fileTypeString

	def academicFilter(self, path): 
		"""
		Pass in txtpath here, only tells if document is academic or not
		"""
		acaFilter = 0
		acaFilter = subprocess.check_output([ROOT_FOLDER+"bin/doFilter.pl",path]) # This is typically either 0 or 1
		web.debug(acaFilter)
		return acaFilter
		
	def printXML(self, xml):
		"""Returns XMl with the proper headers"""
		response = """<?xml version="1.0" encoding="UTF-8"?>\n"""
		response = response + "<CSXAPIMetadata>\n"
		response = response + xml
		response = response + "</CSXAPIMetadata>\n"
		return response
	
	def printXMLLocations(self, fileid):
		"""Returns the URIs for different types of metadata"""
		response = '<token>' + fileid + '</token>'
		response = response + '<file>' + web.ctx.homedomain + '/extractor/' + fileid + '/file</file>\n'
		response = response + '<header>' + web.ctx.homedomain + '/extractor/' + fileid + '/header</header>\n'
		response = response + '<citations>' + web.ctx.homedomain + '/extractor/' + fileid + '/citations</citations>\n'
		response = response + '<keyphrases>' + web.ctx.homedomain + '/extractor/' + fileid + '/keyphrases</keyphrases>\n'
		response = response + '<body>' + web.ctx.homedomain + '/extractor/' + fileid + '/body</body>\n'
		response = response + '<text>' + web.ctx.homedomain + '/extractor/' + fileid + '/text</text>\n'
		return self.printXML(response)

