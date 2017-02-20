<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--  MODULE:    HTML to XHTML conversion                          -->
<!--  DATE:      January 2009                                      -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--  SYSTEM:    NCBI Archiving and Interchange Journal Articles   -->
<!--                                                               -->
<!--  PURPOSE:   Converts HTML to XHTML, with support for MathML   -->
<!--                                                               -->
<!--  PROCESSOR DEPENDENCIES:                                      -->
<!--             XSLT 1.0. Tested using Saxon 6.5.5 and            -->
<!--             Saxon 9.1.0.3 (B and SA)                          -->
<!--                                                               -->
<!--  COMPONENTS REQUIRED:                                         -->
<!--             none (this stylesheet will run standalone)        -->
<!--                                                               -->
<!--  INPUT:     Any (though HTML tagging is expected)             -->
<!--                                                               -->
<!--  OUTPUT:    XHTML                                             -->
<!--                                                               -->
<!--  CREATED FOR:                                                 -->
<!--             Digital Archive of Journal Articles               -->
<!--             National Center for Biotechnology Information  (NCBI)    -->
<!--             National Library of Medicine (NLM)                -->
<!--                                                               -->
<!--  CREATED BY:                                                  -->
<!--             Wendell Piez, Mulberry Technologies, Inc.         -->
<!--                                                               -->
<!-- ============================================================= -->
<!-- 
  This work is in the public domain and may be reproduced, published or 
  otherwise used without the permission of the National Library of Medicine (NLM).
  
  We request only that the NLM is cited as the source of the work.
  
  Although all reasonable efforts have been taken to ensure the accuracy and 
  reliability of the software and data, the NLM and the U.S. Government  do 
  not and cannot warrant the performance or results that may be obtained  by
  using this software or data. The NLM and the U.S. Government disclaim all 
  warranties, express or implied, including warranties of performance, 
  merchantability or fitness for any particular purpose.
-->
<!-- ============================================================= -->
<!--  Function of this stylesheet:

       This stylesheet accepts arbitrary XML input and casts it
       into the XHTML namespace, with the exception of MathML.
       MathML elements are renamed without a prefix, while they
       remain otherwise unchanged.

       In addition, a processing instruction is added at the top
       of the document to call in a MathML display stylesheet
       from W3C. This will enable MathML display in browsers that
       support MathML using this mechanism.                        -->
      
      

<xsl:stylesheet version="1.0"
  xmlns:mml="http://www.w3.org/1998/Math/MathML"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


  <xsl:output method="xml" indent="no" encoding="UTF-8"/>

  <xsl:template match="/">
    <xsl:processing-instruction name="xml-stylesheet">type="text/xsl"
      href="http://www.w3.org/Math/XSL/mathml.xsl"</xsl:processing-instruction>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="*">
    <xsl:element name="{local-name()}" namespace="http://www.w3.org/1999/xhtml">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="mml:*">
    <xsl:element name="{local-name()}" namespace="http://www.w3.org/1998/Math/MathML">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>


  <xsl:template match="comment() | processing-instruction()">
    <xsl:copy-of select="."/>
  </xsl:template>

</xsl:stylesheet>
