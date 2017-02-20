<?xml version="1.0" encoding="UTF-8"?>
<!--                                                                -->
<!-- =============================================================  -->
<!--  MODULE:    XProc pipeline                                     -->
<!--  DATE:      June 2012                                          -->
<!--                                                                -->
<!-- =============================================================  -->
<!--                                                                -->
<!-- =============================================================  -->
<!--  SYSTEM:    NISO 1.0 JATS (Journal Article Tag Set)            -->
<!--                                                                -->
<!--  PURPOSE:   Pipelines stylesheets to convert                   -->
<!--             NISO JATS XML for preview                          -->
<!--                                                                -->
<!--  PROCESSOR DEPENDENCIES:                                       -->
<!--             An XProc processor supporting XSLT 2.0             -->
<!--             Tested using Calabash 0.9.37                       -->
<!--                                                                -->
<!--  COMPONENTS REQUIRED:                                          -->
<!--             XSLT stylesheets named in input ports              -->
<!--                                                                -->
<!--  INPUT:     NISO JATS 3.0 XML                                  -->
<!--             Also supports NLM 3.0                              -->
<!--             and NLM 2.3 (with some limitations)                -->
<!--                                                                -->
<!--  OUTPUT:    HTML, XHTML or XSL-FO, as indicated in the         -->
<!--             final step                                         -->
<!--                                                                -->
<!--  CREATED FOR:                                                  -->
<!--             Digital Archive of Journal Articles                -->
<!--             National Center for Biotechnology Information  (NCBI)     -->
<!--             National Library of Medicine (NLM)                 -->
<!--                                                                -->
<!--  CREATED BY:                                                   -->
<!--             Wendell Piez, Mulberry Technologies, Inc.          -->
<!--                                                                -->
<!-- =============================================================  -->
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
<!-- =============================================================  -->

<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                version="1.0">

   <p:input port="source"/>

   <p:input port="parameters" kind="parameter"/>

   <p:output port="result"/>

   <p:serialization port="result" method="html" encoding="us-ascii"/>

   <p:xslt name="format-NLM-citations" version="2.0">
      <!-- format citations in NLM/PMC format -->
      <p:input port="stylesheet">
         <p:document href="../../xslt/citations-prep/jats-PMCcit.xsl"/>
      </p:input>
   </p:xslt>

   <p:xslt name="exclude-print-only" version="2.0">
      <!-- exclude elements with @specific-use='print-only' -->
      <p:input port="stylesheet">
         <p:document href="../../xslt/prep/jats-webfilter.xsl"/>
      </p:input>
   </p:xslt>

   <p:xslt name="display-html" version="2.0">
      <!-- convert into HTML for display -->
      <p:with-param name="transform" select="'jats-PMCcit-web-html.xpl'"/>
      <p:input port="stylesheet">
         <p:document href="../../xslt/main/jats-html.xsl"/>
      </p:input>
   </p:xslt>

</p:declare-step>
