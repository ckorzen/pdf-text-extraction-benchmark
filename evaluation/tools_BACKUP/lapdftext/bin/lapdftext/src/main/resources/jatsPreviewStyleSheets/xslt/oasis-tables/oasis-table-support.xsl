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
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all"
  version="2.0">

  <!-- xmlns:saxon="http://saxon.sf.net/" -->
  
  <!-- The Saxon extension attribute saxon:memo-function speeds up 
       processing significantly by caching rather than recalculating
       the values returned by processing-intensive functions -->
  
  <!-- $default-border-style sets the style of borders only when are set to appear;
       set in the calling stylesheet -->
  <!--<xsl:param name="default-border-style">solid</xsl:param>-->
  
  <xsl:key name="colspec-by-name" match="o:colspec" use="@colname"/>
  
  <xsl:key name="colspec-by-no" match="o:colspec" use="m:colno(.)"/>
  
  <xsl:key name="entry-by-row" match="o:entry" use="m:down(.)"/>
  
  <!--<xsl:key name="entry-by-col" match="o:entry" use="m:across(.)"/>-->
  
  
  <!--<xsl:function name="m:colno" as="xs:integer" saxon:memo-function="yes">-->
  <xsl:function name="m:colno" as="xs:integer">
      <!-- returns a column number for a column -->
    <xsl:param name="col" as="element(o:colspec)"/>
    <!-- we have to account for possible error conditions, falling back
         gracefully if colspecs are assigned explicit numbers -->
    <!-- note that this means if the data is bad, more than one colspec
         can get the same number -->
    <!-- the best way of avoiding this is to validate the input: either
         all colspecs are given with correct colnums, or none are -->
    <xsl:variable name="actual-colno" select="count($col|$col/preceding-sibling::o:colspec)"/>
    <xsl:choose>
      <!--<xsl:when test="false()"/>-->
      <xsl:when test="exists($col/@colnum[. castable as xs:integer][number(.) gt 0])">
        <!-- if a colno is given as a natural number, we use it -->
        <xsl:sequence select="xs:integer($col/@colnum)"/>
      </xsl:when>
      <!-- if a preceding colspec is assigned a number, we count from it -->
      <xsl:when test="exists($col/preceding-sibling::*/@colnum[. castable as xs:integer][number(.) gt 0])">
        <xsl:variable name="numbered-sibling"
          select="$col/preceding-sibling::*[exists(@colnum[. castable as xs:integer][number(.) gt 0])][1]"/>
        <!-- the number is the preceding colspec's number, plus the number of
             colspecs between -->
        <xsl:sequence select="xs:integer($numbered-sibling/@colnum +
          ($actual-colno - count($numbered-sibling/(.|preceding-sibling::*))))"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$actual-colno"/>
      </xsl:otherwise>
    </xsl:choose>    
  </xsl:function>
  
  <xsl:function name="m:colspec-for-entry" as="element(o:colspec)?">
    <xsl:param name="entry" as="element(o:entry)"/>
    <xsl:variable name="t" select="$entry/ancestor::o:tgroup[1]"/>
    <xsl:variable name="nominal-colspec" select="$entry/(@namest,@colname)[1]/key('colspec-by-name',.,$t)"/>
    <xsl:variable name="positioned-colspec" select="$entry/key('colspec-by-no',m:across(.)[1],$t)[1]"/>
    <!-- under certain error conditions there might be more than one of either nominal or
         positioned colspecs, so we only return the first -->
    <xsl:sequence select="($nominal-colspec,$positioned-colspec)[1]"/>
  </xsl:function>
  
  <xsl:function name="m:rowno" as="xs:integer">
    <!-- returns the position of the row among all the rows, taking
         all tgroups and tfoot at the end -->
    <xsl:param name="row" as="element(o:row)"/>
    <xsl:sequence select="count($row/parent::o:tbody/../o:thead/o:row) +
      count($row/parent::o:tfoot/../o:tbody/o:row) +
      count($row/(.|preceding-sibling::o:row))"/>
  </xsl:function>
  
  <!--<xsl:function name="m:down" as="xs:integer+" saxon:memo-function="yes">-->
  <xsl:function name="m:down" as="xs:integer+">
    <!-- given an entry returns a sequence of one or more integers
         indicating its vertical coverage by row number -->
    <xsl:param name="entry" as="element(o:entry)"/>
    <!-- starting at the count of the row plus any thead rows if we're in a tbody
         and tbody rows if we're in a tfoot --> 
    <xsl:variable name="start" select="m:rowno($entry/parent::o:row)"/>
    <xsl:sequence
      select="$start to xs:integer($start + ($entry/@morerows[. castable as xs:integer],0)[1])"/>
  </xsl:function>
  
  <!--<xsl:function name="m:across" as="xs:integer*" saxon:memo-function="yes">-->
  <xsl:function name="m:across" as="xs:integer*">
      <!-- given an entry, returns a sequence of one or more integers
         indicating its horizontal coverage by column number, an
         empty sequence if namest and nameend are out of order. -->
    <xsl:param name="entry" as="element(o:entry)"/>
    <xsl:variable name="t" select="$entry/ancestor::o:tgroup[1]"/>
    <xsl:variable name="nominal-colspec" select="$entry/(@namest,@colname)[1]/key('colspec-by-name',.,$t)[1]"/>
    <xsl:choose>
      <xsl:when test="exists($nominal-colspec)">
        <!-- If there's a colspec there's also a possibility of spanning columns; so
             we return the index of the colspec through the index of the colspec
             for the @nameend (if any) -->
        <xsl:sequence
          select="m:colno($nominal-colspec) to
            ($entry/@nameend/key('colspec-by-name',.,$t),$nominal-colspec)[1]/m:colno(.)"/>        
      </xsl:when>
      <xsl:otherwise>
        <!-- $row1 identifies the first row of this entry -->
        <xsl:variable name="row1" select="m:down($entry)[1]"/>
        <!-- $nominal is the column we'd be in, absent any @morerows above us; this
             is one more than the last position of our preceding sibling-->
        <xsl:variable name="nominal"
          select="($entry/preceding-sibling::o:entry[1]/m:across(.)[last()],1)[1]"/>
        <xsl:sequence select="m:first-open-col($entry,$row1,$nominal)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <xsl:function name="m:actual-cols" as="xs:integer">
    <!-- returns the number of columns actually given in a row -->
    <xsl:param name="row" as="element(o:row)"/>
    <xsl:sequence select="max($row/o:entry/m:across(.))"/>
  </xsl:function>
  
  <xsl:function name="m:first-open-col" as="xs:integer">
    <xsl:param name="entry" as="element(o:entry)"/>
    <xsl:param name="row" as="xs:integer"/>
    <xsl:param name="col" as="xs:integer"/>
    <!-- returns $col if column $col in row $row is not
         occupied by an earlier entry, or if it is, 
         increments up to the first free column -->
    <xsl:variable name="t" select="$entry/ancestor::o:tgroup"/>
    <!-- $closed indicates the row/col is taken; an entry assigned to the same row in
         the table already has this column -->
    <xsl:variable name="taken" select="exists(
      key('entry-by-row',$row,$t)[. &lt;&lt; $entry]
      [m:across(.) = $col])"/>
    <xsl:choose>
      <xsl:when test="$taken">
        <!-- if the spot is taken, we try the next one -->
        <xsl:sequence select="m:first-open-col($entry,$row,$col + xs:integer(1))"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$col"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <xsl:function name="m:align" as="xs:string">
    <!-- returns an alignment value for an entry -->
    <xsl:param name="e" as="element(o:entry)"/>
    <!-- disabling alignment of 'char' specified on tgroup; this is valid,
         but without a @char or @charoff on tgroup it is unclear how it
         should work -->
    <xsl:variable name="t" select="$e/ancestor::o:tgroup[1]"/>
    <xsl:variable name="colspec" select="m:colspec-for-entry($e)"/>
    <!-- taking first available: entry's align, colspec's align, tgroup's align, 'left' -->
    <xsl:sequence select="string(($e/@align,$colspec/@align,$t/@align,'left')[1])"/>
  </xsl:function>
  
  <xsl:function name="m:tgroup-width-unit" as="xs:string?">
    <!-- for a tgroup, if all given colspec widths have the same unit (not *),
         returns the unit -->
    <xsl:param name="tgroup" as="element(o:tgroup)"/>
    <xsl:variable name="units" select="for $c in $tgroup/o:colspec
      return m:colwidth-unit($c)"/>
    <xsl:if test="not($units != $units)">
      <xsl:sequence select="$units[1]"/>
    </xsl:if>
  </xsl:function>
  
  <xsl:function name="m:colwidth-unit" as="xs:string?">
    <!-- for a colspec, returns the unit, defaulting to 'pt' if a value
         is given without a unit, and nothing if the unit is not
         recognized -->
    <xsl:param name="colspec" as="element(o:colspec)"/>
    <!-- the unit is the width stripped of spaces and non-letter characters except * -->
    <xsl:variable name="unit" select="$colspec/@colwidth/replace(.,'[^\p{L}\*]','')[normalize-space(.)]"/>
    <xsl:choose>
      <!-- a colspec with a numeric width gets its unit, 'pt' if no unit
           is given, nothing if an unrecognized unit is given -->
      <xsl:when test="matches($colspec/@colwidth,'\d')">
        <xsl:sequence select="($unit,'pt')[1][matches(.,'^(\*|in|pc|pt|cm|mm|px|%)$')]"/>
      </xsl:when>
      <!-- a colspec whose width is '*', or that has no width given, is assumed to be '*' -->
      <xsl:when test="matches($colspec/@colwidth,'^\s*\*\s*$') or empty($colspec/@colwidth)">
        <xsl:sequence select="'*'"/>
      </xsl:when>
      <!-- otherwise nothing -->
      <xsl:otherwise/>
    </xsl:choose>
  </xsl:function>
  
  <xsl:function name="m:star-value" as="xs:double?">
    <!-- if string input is in "star" notation, returns its value as
         a double; the value "*" is read as "1*" -->
    <xsl:param name="width" as="xs:string?"/>
    <xsl:variable name="w" select="if (normalize-space($width)='*')
      then '1*' else normalize-space($width)"/>
    <xsl:if test="matches($w,'^\d+(\.\d+)?\*$')">
      <xsl:sequence select="xs:double(replace($w,'[\s\*]','')[. castable as xs:double])"/>
    </xsl:if>
  </xsl:function>
  
  <xsl:function name="m:width-sum" as="xs:string?">
    <!-- if provided width values have the same unit of measure,
         they are summed -->
    <xsl:param name="widths" as="xs:string*"/>
    <!-- if no widths are provided, return nothing -->
    <xsl:variable name="units" select="for $w in $widths return
      replace($w,'[^\p{L}\*]','')"/>
    <!-- only return a value if widths are given and all their units are the same -->
    <xsl:if test="exists($widths) and not($units != $units)">
      <!-- an empty string is treated as 0, negative values are treated as positive -->
      <xsl:variable name="values" select="for $w in $widths
        return xs:double((replace($w,'[\s\p{L}\*]','')[. castable as xs:double],0)[1])"/>
      <!-- as per OASIS TR 9901:1999, 'pt' is provided as a default when no unit of measure
           is given -->
      <xsl:sequence select="concat(sum($values),($units[1][matches(.,'^(\*|in|pc|pt|cm|mm|px|%)?$')],'pt')[1])"/>
    </xsl:if>
  </xsl:function>
  
  <xsl:function name="m:relative-percentage" as="xs:string">
    <!-- returns percentage value for colspec using * notation among its siblings
         using * notation
         colspec with no @colwidth are assumed to have @colwidth='1*' -->
    <xsl:param name="col" as="xs:string"/>
    <xsl:param name="family" as="element(o:colspec)+"/>
    <xsl:variable name="this-value"
      select="m:star-value($col)"/>
    <xsl:variable name="total" select="sum($family/m:star-value((@colwidth,'1*')[1]))"/>
    <xsl:variable name="proportion" select="round(($this-value div $total) * 10000) div 100"/>
    <xsl:sequence select="concat(string($proportion),'%')"/>
  </xsl:function>
  
  <xsl:variable name="m:border-specs" as="element(m:border)+">
    <m:border class="xxxx-borders" style="border-top-style: none; border-bottom-style: none; border-left-style: none; border-right-style: none"/>
    <m:border class="txxx-borders" style="border-top-style: {$default-border-style}; border-bottom-style: none; border-left-style: none; border-right-style: none"/>
    <m:border class="xbxx-borders" style="border-top-style: none; border-bottom-style: {$default-border-style}; border-left-style: none; border-right-style: none"/>
    <m:border class="xxlx-borders" style="border-top-style: none; border-bottom-style: none; border-left-style: {$default-border-style}; border-right-style: none"/>
    <m:border class="xxxr-borders" style="border-top-style: none; border-bottom-style: none; border-left-style: none; border-right-style: {$default-border-style}"/>
    <m:border class="tbxx-borders" style="border-top-style: {$default-border-style}; border-bottom-style: {$default-border-style}; border-left-style: none; border-right-style: none"/>
    <m:border class="txlx-borders" style="border-top-style: {$default-border-style}; border-bottom-style: none; border-left-style: {$default-border-style}; border-right-style: none"/>
    <m:border class="txxr-borders" style="border-top-style: {$default-border-style}; border-bottom-style: none; border-left-style: none; border-right-style: {$default-border-style}"/>
    <m:border class="xblx-borders" style="border-top-style: none; border-bottom-style: {$default-border-style}; border-left-style: {$default-border-style}; border-right-style: none"/>
    <m:border class="xbxr-borders" style="border-top-style: none; border-bottom-style: {$default-border-style}; border-left-style: none; border-right-style: {$default-border-style}"/>
    <m:border class="xxlr-borders" style="border-top-style: none; border-bottom-style: none; border-left-style: {$default-border-style}; border-right-style: {$default-border-style}"/>
    <m:border class="tblx-borders" style="border-top-style: {$default-border-style}; border-bottom-style: {$default-border-style}; border-left-style: {$default-border-style}; border-right-style: none"/>
    <m:border class="tbxr-borders" style="border-top-style: {$default-border-style}; border-bottom-style: {$default-border-style}; border-left-style: none; border-right-style: {$default-border-style}"/>
    <m:border class="txlr-borders" style="border-top-style: {$default-border-style}; border-bottom-style: none; border-left-style: {$default-border-style}; border-right-style: {$default-border-style}"/>
    <m:border class="xblr-borders" style="border-top-style: none; border-bottom-style: {$default-border-style}; border-left-style: {$default-border-style}; border-right-style: {$default-border-style}"/>
    <m:border class="tblr-borders" style="border-top-style: {$default-border-style}; border-bottom-style: {$default-border-style}; border-left-style: {$default-border-style}; border-right-style: {$default-border-style}"/>
  </xsl:variable>
  
  <xsl:function name="m:border-spec" as="element(m:border)?">
    <!-- returns an element from inside $m:border-specs for
         applying borders to an entry -->
    <xsl:param name="entry" as="element(o:entry)"/>
    <xsl:variable name="frame-spec" select="$entry/ancestor::o:table/@frame"/>
    <xsl:variable name="t" select="$entry/ancestor::o:tgroup[1]"/>
    <xsl:variable name="top-edge" select="(m:down($entry)) = 1"/>
    <xsl:variable name="left-edge" select="m:across($entry) = 1"/>
    <xsl:variable name="bottom-edge" select="(m:down($entry)) = count($t//o:row)"/>
    <xsl:variable name="right-edge" select="m:across($entry) = $t/@cols"/>
    
    <!-- these will be slow on large tables, and unnecessary on square ones: -->
    <!--<xsl:variable name="bottom-edge" select="(m:down($entry)) = max($t//o:entry/m:down(.))"/>-->
    <!--<xsl:variable name="right-edge" select="m:across($entry) = max($t//o:entry/m:across(.))"/>-->
    
    <xsl:variable name="across-index" select="m:across($entry)[1]"/>
    <xsl:variable name="neighbor-up"
      select="$entry/key('entry-by-row',((m:down[1]) - 1),$t)[m:across(.) = $across-index]"/>
    <xsl:variable name="neighbor-left"
      select="$entry/key('entry-by-row',(m:down[1]),$t)[m:across(.) = ($across-index - 1)]"/>
    
    <!-- $top is set if the closest available @rowsep is 1
         (from among the neighbor entry's @rowsep, its colspec's @rowsep, its row's @rowsep,
          a @rowsep on the entry's tgroup or table) -->
    <xsl:variable name="top" select="(($neighbor-up/@rowsep, $neighbor-up/m:colspec-for-entry(.)/@rowsep,
      $neighbor-up/parent::o:row/@rowsep, $entry/ancestor::o:tgroup[1]/@rowsep, $entry/ancestor::o:table[1]/@rowsep)[1] = 1)
      or ($top-edge and $frame-spec=('top','topbot','all'))"/>
    <!-- checking the entry likewise for $bottom -->
    <xsl:variable name="bottom" select="(($entry/@rowsep, $entry/m:colspec-for-entry(.)/@rowsep,
      $entry/parent::o:row/@rowsep, $entry/ancestor::o:tgroup[1]/@rowsep, $entry/ancestor::o:table[1]/@rowsep)[1] = 1)
      or ($bottom-edge and $frame-spec=('bottom','topbot','all'))"/>
    <!-- $left is set if the closest available @colsep is 1
         (from among the neighbor entry's @colsep, its colspec's @colsep,
          or a @colsep on the entry's tgroup or table) -->
    <xsl:variable name="left" select="(($neighbor-left/@colsep, $neighbor-left/m:colspec-for-entry(.)/@colsep,
      $entry/ancestor::o:tgroup[1]/@colsep, $entry/ancestor::o:table[1]/@colsep)[1] = 1)
      or ($left-edge and $frame-spec=('left','sides','all'))"/>
    <!-- checking the entry likewise for $right -->
    <xsl:variable name="right" select="(($entry/@colsep, $entry/m:colspec-for-entry(.)/@colsep,
      $entry/ancestor::o:tgroup[1]/@colsep, $entry/ancestor::o:table[1]/@colsep)[1] = 1)
      or ($right-edge and $frame-spec=('right','sides','all'))"/>
    
    <!-- border-off strings together any of 'tblr' that will be turned off -->
    <xsl:variable name="border-off" select="string-join(
      ('t'[not($top)],'b'[not($bottom)],'l'[not($left)],'r'[not($right)]),'')"/>

    <xsl:variable name="code" select="translate('tblr',$border-off,'xxxx')"/>
    
    <xsl:sequence select="$m:border-specs[@class=string-join(($code,'borders'),'-')]"/>
  </xsl:function>
</xsl:stylesheet>