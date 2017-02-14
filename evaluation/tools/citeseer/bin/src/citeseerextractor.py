#!/usr/bin/python2.7

"""
    Copyright (C) 2013  Kyle Williams <kwilliams@psu.edu>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
"""

"""
	This is a client for the CiteSeerExtractor. 
	It can easily be used to retrieve various parts of a PDF without needing to worry about HTTP and XML handling.
	
	Generally the methods return 2 values: a Boolean value detailing whether the method succeeded and a message
	When the method succeeded, the message will contain the requested data and when the method fails it will return a messag explaining why (or should at least)
	Example: pass, value = citeseerextractor.getSomething('token')
	Check to make sure pass is true before proceeding
"""

import cgi, cgitb
import sys
import requests
import xml.etree.ElementTree as ET
import re
import subprocess
from django.utils.encoding import smart_str, smart_unicode

class CiteSeerExtractor:
	
	def __init__(self, url):	
		self.url = url
	
	def postPDF(self, pdf):
		with open (pdf, 'rb') as f:
			r = requests.post(str(self.url + '/file'), data=f)
		if r.status_code == 201:
			root = ET.fromstring(smart_str(r.content))
			token = root.find('token').text
			return True, token
		else:
			return False, r.content
	
	def getXMLTag(self, token, resource, tag):
		r = requests.get(self.url + '/' + token + '/' + resource)
		if r.status_code == 200:
			root = ET.fromstring(smart_str(r.content))
			if tag is '/':
				return True, root
			tagContents = root.findall(tag)
			return True, tagContents
		else:
			return False, r.content
	
	def getHeaderAsXMLString(self, token):
		passed, header = self.getXMLTag(token, 'header', '/')
		if passed is False:
			return False, header
		return True, ET.tostring(header)
		
	def getCitationsAsXMLString(self, token):
		passed, header = self.getXMLTag(token, 'citations', '/')
		if passed is False:
			return False, header
		return True, ET.tostring(header)
		
	def getAuthorNames(self, token):
		print token
		passed, authorsXML = self.getXMLTag(token, 'header', 'algorithm/authors/author/name')
		if passed is False:
			return False, authorsXML
		authorList = ''
		authNo = 0
		for node in authorsXML:
			authNo += 1
			authorList += node.text
			if authNo < len(authorsXML)-1:
				authorList += ", "
			elif authNo == len(authorsXML)-1:
				authorList += " and "
		return True, authorList
		
	def getTitle(self, token):
		passed, titleXML = self.getXMLTag(token, 'header', 'algorithm/title')
		if passed is False:
			return False, titleXML
		return True, titleXML[0].text
		
	def getBodyText(self, token):
		passed, bodyXML = self.getXMLTag(token, 'body', 'body')
		if passed is False:
			return False, bodyXML
		return True, bodyXML[0].text

if  __name__ =='__main__':
	
	#"""
	#Here are some examples of how the code can be used
	#"""
	
	## To post a PDF use the postPDF method with a string containing the location of the PDF on the filesystem
	## A 'token' will be returned that can then be used to retrieve various aspects of the PDF, i.e. authors, title, body, etc.
	
	#Create a CiteSeerExtractor object - sys.argv[1] is the URL
	csex = CiteSeerExtractor(sys.argv[1]) 
	
	#Post the PDF
	passed, message = csex.postPDF(sys.argv[2])
	if passed is True:
		print "File successfully created, your token is " + str(message)
	else:
		print "Post failed"
		print message
		sys.exit(0)
	token = message	
	
	#Get authors
	passed, message = csex.getAuthorNames(token)
	if passed is True:
		print message
	else:
		print "Failed to get authors"
		print message
		sys.exit(0)
		
	#Get title
	passed, message = csex.getTitle(token)
	if passed is True:
		print message
	else:
		print "Failed to get title"
		print message
		sys.exit(0)
	
	#Get header as XML
	passed, message = csex.getHeaderAsXMLString(token)
	if passed is True:
		print message
	else:
		print "Failed to get header metadata"
		print message
		sys.exit(0)
		
