<?xml version="1.0" encoding="UTF-8"?>

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
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:o="http://www.niso.org/standards/z39-96/ns/oasis-exchange/table"
  xmlns:m="http://mulberrytech.com/xslt/oasis-html/util"
  exclude-result-prefixes="#all"
  version="2.0">

  <xsl:strip-space elements="o:table o:tgroup o:thead
    o:tbody o:tfoot o:row"/>
  
  <!--<xsl:output method="html"/>-->
  
  <!-- Set $hard-styles to false() or give it a value if you intend to
       call template name="m:table-css" into the top of your HTML
       for more flexible and controllable styling of tables with CSS.
       When in force, $hard-styles will include styling using @style
       at the element level for controlling borders, and suppress
       the appearance of @class attributes.      
  -->
  <xsl:param name="hard-styles" select="true()"/>
  
  <!-- You can also override the default here -->
  <xsl:param name="default-cell-styling">border-color: black; border-width: thin; padding: 5px</xsl:param>
  <!-- Or tweak the m:table-css template itself -->

  <!-- $default-border-style sets the style of borders only when are set to appear -->
  <xsl:param name="default-border-style">solid</xsl:param>

  <!-- the included stylesheet includes key and function declarations required -->
  <xsl:include href="oasis-table-support.xsl"/>

  <!-- For testing:
    <xsl:template match="/">
    <html>
      <head>
        <title>Test table code</title>
        <xsl:call-template name="m:table-css"/>
      </head>
      <body>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>-->
  
  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>
  
  <!-- call m:table-css template to generate an HTML 'style' element in the
       header of HTML output when $hard-styles is false() -->
  <xsl:template name="m:table-css">
    <!-- generates a CSS style element with settings for table borders -->
    <style type="text/css">
      <!-- collapsed borders are going to look better anytime borders are missing -->
      <xsl:text>&#xA;table.tgroup { border-collapse: collapse }</xsl:text>
      
      <!-- consecutive tgroups should have no vertical space -->
      <xsl:text>&#xA;table.tgroup.pgwide { width: 100% }</xsl:text>
      <xsl:text>&#xA;table.tgroup.cont { margin-bottom: 0px }</xsl:text>
      <xsl:text>&#xA;table.tgroup.contd { margin-top: 0px }</xsl:text>
      <xsl:text>&#xA;td, th { </xsl:text>
      <xsl:value-of select="$default-cell-styling"/>
      <xsl:text> }</xsl:text>
      
      <xsl:for-each select="$m:border-specs">
        <xsl:text>&#xA;.</xsl:text>
        <xsl:value-of select="@class"/>
        <xsl:text> { </xsl:text>
        <xsl:value-of select="@style"/>
        <xsl:text> } </xsl:text>
      </xsl:for-each>
      <xsl:text>&#xA;</xsl:text>
    </style>
  </xsl:template>
  
  <!--<xsl:template match="o:table">
    <div>
      <xsl:call-template name="m:assign-class">
        <xsl:with-param name="class" select="string-join(('table',@content-type),' ')"/>
      </xsl:call-template>
      <xsl:apply-templates/>
    </div>
  </xsl:template>-->
  
  <xsl:template match="o:table">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="o:tgroup">
    <!-- NISO JATS does not include @pgwide, but OASIS permits it -->
    <xsl:variable name="pgwide" select="../@pgwide='1'"/>
    <xsl:variable name="continuing" select="exists(following-sibling::o:tgroup)"/>
    <xsl:variable name="continued" select="exists(following-sibling::o:tgroup)"/>
    <xsl:variable name="classes"
      select="'tgroup','pgwide'[$pgwide],'cont'[$continuing],'contd'[$continued]"/>
    <table>
      <xsl:call-template name="m:assign-class">
        <xsl:with-param name="class" select="string-join($classes,' ')"/>
      </xsl:call-template>
      <xsl:if test="$hard-styles">
        <xsl:attribute name="style">
          <xsl:text>border-collapse: collapse</xsl:text>
          <xsl:if test="$pgwide">; width: 100%</xsl:if>
          <xsl:if test="$continuing">; margin-bottom: 0px</xsl:if>
          <xsl:if test="$continued">; marging-top: 0px</xsl:if>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </table>
  </xsl:template>
  
  <xsl:template match="o:thead">
    <thead>
      <xsl:call-template name="m:assign-class"/>
      <xsl:apply-templates/>
    </thead>
  </xsl:template>
  
  <!-- Following the OASIS spec TR 9901:1999, NISO 1.0 has no tfoot,
       but earlier versions of the CALS/OASIS table model do -->
  <xsl:template match="o:tfoot">
    <tfoot>
      <xsl:call-template name="m:assign-class"/>
      <xsl:apply-templates/>
    </tfoot>
  </xsl:template>
  
  <xsl:template match="o:tbody">
    <tbody>
      <xsl:call-template name="m:assign-class"/>
      <xsl:apply-templates/>
    </tbody>
  </xsl:template>
  
  <xsl:template match="o:row">
    <xsl:variable name="rowno" select="m:rowno(.)"/>
    <xsl:variable name="here" select="."/>
    <tr>
      <xsl:call-template name="m:assign-class"/>
      <!--<xsl:for-each select="1 to max(key('entry-by-row',$rowno,$here/ancestor::o:tgroup)/m:across(.))">-->
      <xsl:variable name="colcount" select="(ancestor::o:tgroup/@cols[. castable as xs:integer][number(.) gt 0])"/>
      <xsl:for-each select="1 to $colcount">
        <xsl:variable name="colno" select="."/>
        <xsl:variable name="entry" select="$here/o:entry[m:across(.) = $colno][1]"/>
        <!-- we process an entry unless it occupies an earlier column, in
             which case it spans -->
        <xsl:if test="not($entry/m:across(.) &lt; $colno)">
          <xsl:apply-templates select="$entry"/>
        </xsl:if>
        <xsl:if test="empty(key('entry-by-row',$rowno,$here/ancestor::o:tgroup)[m:across(.)=$colno])">
          <!-- as mandated by the OASIS spec, if a row is missing an entry in a given position,
               we create a "ghost" entry to insert (to maintain the square) --> 
          <xsl:variable name="ghost-entry">
            <!-- it has to be in a ghost branch with the same properties as an actual entry
                 would have -->
            <o:tgroup>
              <xsl:copy-of select="$here/../parent::o:tgroup/@*"/>
              <!-- remember that under certain error conditions, more than one colspec
                   may be given for a certain column; so we only take the first (if any) -->
              <xsl:copy-of select="$here/../parent::o:tgroup/o:colspec[m:colno(.)=$colno][1]"/>
              <xsl:for-each select="$here/..">
                <xsl:copy copy-namespaces="no">
                  <xsl:copy-of select="@*"/>
                  <xsl:for-each select="$here">
                    <xsl:copy copy-namespaces="no">
                      <xsl:copy-of select="@*"/>
                      <o:entry>&#xA0;</o:entry>
                    </xsl:copy>
                  </xsl:for-each>
                </xsl:copy>
              </xsl:for-each>
            </o:tgroup>
          </xsl:variable>
          <!-- now we apply templates to the ghost entry, et voila -->
          <xsl:apply-templates select="$ghost-entry//o:entry"/>
        </xsl:if>
      </xsl:for-each>
    </tr>
  </xsl:template>
  
  <xsl:template match="o:thead/*/o:entry | o:tfoot/*/o:entry">
    <th>
      <xsl:if test="$hard-styles">
        <xsl:attribute name="style" select="$default-border-style"/>
      </xsl:if>
      <xsl:call-template name="m:entry-content"/>
    </th>
  </xsl:template>
  
  <xsl:template match="o:entry">
    <td>
      <xsl:if test="$hard-styles">
        <xsl:attribute name="style" select="$default-border-style"/>
      </xsl:if>
      <xsl:call-template name="m:entry-content"/>
    </td>
  </xsl:template>

  <xsl:template name="m:entry-content">
    <!-- add m:align if it's not 'left' (the default) or 'char' (handled otherwise) -->
    <xsl:if test="not(m:align(.)=('char'))">
      <xsl:attribute name="align" select="m:align(.)"/>
    </xsl:if>
    <!-- get valign from the first available: the entry, its row,
         or its thead, tfoot or tbody ancestor -->
    <xsl:copy-of select="(@valign,parent::o:row/@valign,
      ancestor::*[self::o:thead|self::o:tfoot|self::o:tbody]/@valign)[1]"/>
    <!-- @morerows will determine @rowspan -->
    <xsl:apply-templates select="@morerows"/>
    <!-- m:across(.) determins @colspan -->
    <xsl:if test="count(m:across(.)) > 1">
      <xsl:attribute name="colspan" select="count(m:across(.))"/>
    </xsl:if>
    
    <!-- m:border-spec designates border placement -->
    <xsl:attribute name="class"
      select="string-join(('entry',m:border-spec(.)/@class),' ')"/>
    <xsl:if test="$hard-styles">
      <xsl:attribute name="style"
        select="string-join(($default-cell-styling,m:border-spec(.)/@style),'; ')"/>
    </xsl:if>
    
    <!-- we have to calculate the width based on the widths set on colspecs -->
    <!-- the calculation of m:across takes account of @colname, @namest and @nameend -->
    <xsl:variable name="colspecs"
      select="key('colspec-by-no',m:across(.))"/>
    <!-- $calculated-width adds the widths together -->
    <xsl:variable name="calculated-width"
      select="m:width-sum($colspecs/(@colwidth[matches(.,'\d')]/string(.),'1*')[1])"/>
    <!-- if the result is a star value, we resolve it; otherwise $width is
         the $calculated-width -->
    <xsl:variable name="width" select="if (m:star-value($calculated-width))
        then m:relative-percentage($calculated-width,$colspecs/../o:colspec) else $calculated-width"/>
    
    <!-- only write the width if it's not zero -->
    <xsl:if test="matches($width,'[1-9]')">
      <xsl:attribute name="width" select="$width"/>
    </xsl:if>
    
    <xsl:apply-templates select="." mode="cell-contents"/>
    
    <!--for debugging  -->
    <!--<br class="br"/>
    <xsl:text>(</xsl:text>
    <!-\-<xsl:value-of select="$width"/>
    <xsl:text>; </xsl:text>-\->
    <xsl:value-of select="m:colspec-for-entry(.)/m:colwidth-unit(.)" separator=","/>
    <!-\-<xsl:text> / </xsl:text>
    <xsl:value-of select="m:across(.)" separator=","/>-\->
    <xsl:text>)</xsl:text>-->
  </xsl:template>
  
  <xsl:template match="o:entry" mode="cell-contents">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="o:entry[m:align(.)='char']" mode="cell-contents">
    <!-- treats contents as a string, ignoring any inline markup -->
    <!-- character aligns at @charoff if given, or 50% --> 
    <xsl:variable name="colspec" select="m:colspec-for-entry(.)"/>
    <xsl:variable name="char" select="(@char/string(.),$colspec/@char/string(.),'')[1]"/>
    <xsl:variable name="charoff"
      select="(((@charoff,$colspec/@charoff))[. castable as xs:integer]/xs:integer(.),50)[1]"/>
    <span style="float:left; text-align: right; width:{$charoff}%">
      <xsl:value-of select=".[not(contains(.,$char)) or not($char)]"/>
      <xsl:value-of select="substring-before(.,$char)"/>
      <xsl:value-of select="$char[contains(current(),$char)]"/>
    </span>
    <span style="float: left; text-align: left; width:{100 - $charoff}%">
      <xsl:value-of select="substring-after(.,$char)[$char]"/>
      <xsl:value-of select="'&#xA0;'[not(contains(current(),$char)) or not($char)]"/>
    </span>
  </xsl:template>

  <xsl:template match="o:entry/@morerows">
    <xsl:attribute name="rowspan">
      <xsl:value-of select="xs:integer(.) + 1"/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template name="m:assign-class">
    <xsl:param name="class" select="local-name()"/>
    <xsl:if test="normalize-space($class) and not($hard-styles)">
      <xsl:attribute name="class" select="$class"/>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>