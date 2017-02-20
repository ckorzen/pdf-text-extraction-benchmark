<?xml version="1.0" encoding="utf-8"?>
<!-- ============================================================= -->
<!--  MODULE:    OASIS tables to HTML                              -->
<!--  DATE:      June 2012                                         -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--  SYSTEM:    NCBI Archiving and Interchange Journal Articles   -->
<!--                                                               -->
<!--  PURPOSE:   Processes Journal Publishing 3.0 input to filter  -->
<!--             elements based on 'specific-use' attribute values -->
<!--                                                               -->
<!--  PROCESSOR DEPENDENCIES:                                      -->
<!--             XSLT 2.0. Tested using                            -->
<!--             Saxon 9.3.0.5 (Oxygen build)                      -->
<!--                                                               -->
<!--  COMPONENTS REQUIRED:                                         -->
<!--             none (this stylesheet will run standalone)        -->
<!--                                                               -->
<!--  INPUT:     Journal Publishing 3.0 XML                        -->
<!--                                                               -->
<!--  OUTPUT:    The same                                          -->
<!--                                                               -->
<!--  CREATED FOR:                                                 -->
<!--             Digital Archive of Journal Articles               -->
<!--             National Center for Biotechnology Information   (NCBI)   -->
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

       This stylesheet provides an identity transformation shell
       for the OASIS table conversion module, which turns OASIS
       tables into HTML. This stylesheet will make only this
       modification; run it as a pre-process on documents with
       OASIS tables for processes expecting HTML tables. -->

<xsl:transform version="2.0"
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:import href="../main/oasis-exchange-html.xsl"/>

<xsl:output method="xml" indent="no"/>

<xsl:template match="@*|node()">
  <!-- match and copy these nodes -->
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>


</xsl:transform>

