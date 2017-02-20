<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================================= -->
<!--  MODULE:    Saxon shell (pipelining) utility stylesheet       -->
<!--  DATE:      January 2009                                      -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--  SYSTEM:    NCBI Archiving and Interchange Journal Articles   -->
<!--                                                               -->
<!--  PURPOSE:   Provide support for pipelining stylesheets        -->
<!--             directly, using Saxon extensions, in XSLT 2.0     -->
<!--                                                               -->
<!--  PROCESSOR DEPENDENCIES:                                      -->
<!--             Saxon, from Saxonica (www.saxonica.com)           -->
<!--             Tested using Saxon 9.1.0.3 (B and SA)             -->
<!--                                                               -->
<!--  COMPONENTS REQUIRED:                                         -->
<!--             This stylesheet does not stand alone; it is a     -->
<!--             code module for inclusion into another stylesheet -->
<!--             that specifies the steps of the pipeline.         -->
<!--                                                               -->
<!--  INPUT:     Any                                               -->
<!--                                                               -->
<!--  OUTPUT:    Any                                               -->
<!--                                                               -->
<!--  CREATED FOR:                                                 -->
<!--             Digital Archive of Journal Articles               -->
<!--             National Center for Biotechnology Information (NCBI)     -->
<!--             National Library of Medicine (NLM)                -->
<!--                                                               -->
<!--  CREATED BY:                                                  -->
<!--             Wendell Piez, Mulberry Technologies, Inc.         -->
<!--                                                               -->
<!-- ============================================================= -->

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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:saxon="http://saxon.sf.net/"
  version="2.0"
  extension-element-prefixes="saxon">

  <!-- This stylesheet does not stand alone! It is a component
       to be called into XSLT 2.0 shell stylesheets. -->
  
  <xsl:variable name="document" select="/" saxon:assignable="yes"/>
  
  <xsl:param name="runtime-params">
    <base-dir>
      <xsl:value-of
        select="replace(base-uri(/), '/[^/]+$','')"/>     
    </base-dir>
  </xsl:param>

  <xsl:template match="/">
    <xsl:for-each select="$processes/step">
      <xsl:message>
        <xsl:text>&#xA;... Applying </xsl:text>
        <xsl:value-of select="."/>
      </xsl:message>
      <saxon:assign name="document"
        select="saxon:transform(
                  saxon:compile-stylesheet(doc(.)),
                  $document,
                  $runtime-params/* )"/>
      <!-- A third argument to saxon:transform could specify
           runtime parameters for any (or all) steps -->
    </xsl:for-each>
    <xsl:sequence select="$document"/>
    <xsl:message>&#xA;... Done</xsl:message>
  </xsl:template>
  
</xsl:stylesheet>