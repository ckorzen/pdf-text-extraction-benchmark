import web
import tempfile
import os
import subprocess
import shutil

FOLDER      = os.path.dirname(os.path.realpath(__file__))
ROOT_FOLDER = os.path.dirname(FOLDER) + "/"

class Extraction:
	"""
	This class does the actual extraction by calling the relevant perl methods
	Errors are caught in the calling class
	"""
	def extractHeaders(self,path):
		"""extract headers from text file"""
		headers = subprocess.check_output([ROOT_FOLDER+"bin/getHeader.pl",path])
		web.debug(headers)
		return headers
	def extractCitations(self,path):
		"""extract citations from text file"""
		citations = subprocess.check_output([ROOT_FOLDER+"bin/getCitations.pl",path])
		web.debug(citations)
		return citations
	def extractBody(self,path):
		"""extract body from text file"""
		body = subprocess.check_output([ROOT_FOLDER+"bin/getBody.pl",path])
		web.debug(body)
		return body
	def extractKeyphrases(self,path):
		"""extract keyphrases from text file"""
		# keyphrases = subprocess.check_output(["pwd"], cwd=ROOT_FOLDER)
		keyphrases = subprocess.check_output(["java", "-cp", "lib/Maui/simseer.jar:lib/Maui/maui-1.1.jar:lib/Maui/weka.jar:lib/Maui/wikipediaminer1.1.jar", "edu.psu.ist.simseerx.keyphrase.KeyPhrases", path], cwd=ROOT_FOLDER)
		kp_string = "<keyphrases>"
		kp_list = keyphrases.split('\n')
		for kp in kp_list:
			kp_string += "<keyphrase>" + kp + "</keyphrase>"
		kp_string += "</keyphrases>"
		web.debug(kp_string)
		return kp_string
