<?xml version="1.0" encoding="utf-8"?>
<!-- ============================================================= -->
<!--  MODULE:    'specific-use' filtering for web output           -->
<!--  DATE:      January 2009                                      -->
<!--                                                               -->
<!-- ============================================================= -->

<!-- ============================================================= -->
<!--  SYSTEM:    NCBI Archiving and Interchange Journal Articles   -->
<!--                                                               -->
<!--  PURPOSE:   Processes Journal Publishing 3.0 input to filter  -->
<!--             elements based on 'specific-use' attribute values -->
<!--                                                               -->
<!--  PROCESSOR DEPENDENCIES:                                      -->
<!--             XSLT 1.0. Tested using Saxon 6.5.5 and            -->
<!--             Saxon 9.1.0.3 (B and SA)                          -->
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

       This stylesheet filters Journal Publishing data based
       on settings of the @specific-use attribute. In this version,
       elements with @specific-use='print-only' are excluded from
       the results, while all others are passed through.           -->

<xsl:transform version="1.0"
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml" indent="no"/>


<xsl:template match="*[@specific-use='print-only']"/>
<!-- match and drop these elements -->


<xsl:template match="@*|node()">
  <!-- match and copy these nodes -->
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>


</xsl:transform>

