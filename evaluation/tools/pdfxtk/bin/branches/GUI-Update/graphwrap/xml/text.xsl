<?xml version="1.0" encoding="UTF-8"?>

<!-- ===================================================================
     Stylesheet for displaying text zones
     =================================================================== -->

<xsl:stylesheet version="1.0"
  xmlns:xmi="http://www-iiuf.unifr.ch/~hitz/xmillum"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:template match="//PDFResult/page[1]">
    <xmi:document>
      <xmi:style name="zones">
	<param name="foreground" value="black"/>
	<param name="background" value="yellow"/>
	<param name="transparency" value="1.0"/>
	<param name="fill" value="false"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
      </xmi:style>

      <xmi:style name="yellow-style">
	<param name="foreground" value="yellow"/>
	<param name="background" value="yellow"/>
	<param name="transparency" value="0.4"/>
	<param name="fill" value="true"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
      </xmi:style>
      
      <xmi:style name="image-style">
	<param name="foreground" value="darkblue1"/>
	<param name="background" value="darkblue1"/>
	<param name="transparency" value="0.4"/>
	<param name="fill" value="true"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
      </xmi:style>
      
      <xmi:style name="textblock-style">
	<param name="foreground" value="darkorange1"/>
	<param name="background" value="darkorange1"/>
	<param name="transparency" value="0.4"/>
	<param name="fill" value="true"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
      </xmi:style>
      
      <xmi:style name="block-style">
	<param name="foreground" value="red"/>
	<param name="background" value="red"/>
	<param name="transparency" value="1.0"/>
	<param name="fill" value="false"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
	<param name="fontweight" value="bold"/>
      </xmi:style>
      
      <xmi:style name="potential-table-style">
	<param name="foreground" value="darkred1"/>
	<param name="background" value="darkorange1"/>
	<param name="transparency" value="0.2"/>
	<param name="fill" value="true"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
      </xmi:style>
      
      <xmi:style name="instance-style">
	<param name="foreground" value="darkpurple1"/>
	<param name="background" value="darkpurple1"/>
	<param name="transparency" value="0.10"/>
	<param name="fill" value="true"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
      </xmi:style>
      
      <xmi:style name="sub-instance-style">
	<param name="foreground" value="darkgreen1"/>
	<param name="background" value="darkgreen1"/>
	<param name="transparency" value="0.20"/>
	<param name="fill" value="true"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
      </xmi:style>
      
      <xmi:style name="table-row-style">
	<param name="foreground" value="darkred2"/>
	<param name="background" value="darkred2"/>
	<param name="transparency" value="0.2"/>
	<param name="fill" value="true"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
      </xmi:style>
      
      <xmi:style name="division-style">
	<param name="foreground" value="darkgreen2"/>
	<param name="background" value="darkgreen2"/>
	<param name="transparency" value="0.2"/>
	<param name="fill" value="true"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
      </xmi:style>
      
      <xmi:style name="edge-style">
	<param name="foreground" value="darkred1"/>
	<param name="background" value="darkred1"/>
	<param name="transparency" value="0.4"/>
	<param name="fill" value="false"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
	<param name="stroke-width" value="0.5"/>
      </xmi:style>
      
      <xmi:style name="selection-style">
	<param name="foreground" value="yellow"/>
	<param name="background" value="yellow"/>
	<param name="transparency" value="0.6"/>
	<param name="fill" value="true"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
      </xmi:style>
      
      <xmi:style name="line-style">
	<param name="foreground" value="darkpurple1"/>
	<param name="background" value="darkpurple1"/>
	<param name="transparency" value="0.9"/>
	<param name="fill" value="false"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
	<param name="stroke-width" value="2.0"/>
      </xmi:style>
      
      <xmi:style name="rect-style">
	<param name="foreground" value="darkred2"/>
	<param name="background" value="darkred2"/>
	<param name="transparency" value="0.8"/>
	<param name="fill" value="false"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
	<param name="stroke-width" value="2.0"/>
      </xmi:style>
      
      <xmi:style name="filled-rect-style">
	<param name="foreground" value="darkgreen2"/>
	<param name="background" value="darkgreen2"/>
	<param name="transparency" value="0.8"/>
	<param name="fill" value="false"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
	<param name="stroke-width" value="2.0"/>
      </xmi:style>
           
      <xmi:style name="table">
	<param name="foreground" value="red"/>
	<param name="background" value="red"/>
	<param name="transparency" value="0.4"/>
	<param name="fill" value="true"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
      </xmi:style>
      
      <xmi:style name="table-block">
	<param name="foreground" value="blue"/>
	<param name="background" value="blue"/>
	<param name="transparency" value="1.0"/>
	<param name="fill" value="true"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>	
      </xmi:style>
      
      <xmi:style name="text-style">
	<param name="foreground" value="darkindigo1"/>
	<param name="background" value="white"/>
	<param name="transparency" value="1.0"/>
	<param name="fill" value="false"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
      </xmi:style>
      
      <xmi:style name="xycut-style">
	<param name="foreground" value="darkcyan2"/>
	<param name="background" value="white"/>
	<param name="transparency" value="0.5"/>
	<param name="fill" value="false"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
	<param name="stroke-width" value="2.0"/>
      </xmi:style>
      
      <xmi:style name="table-cell-style">
	<param name="foreground" value="darkred2"/>
	<param name="background" value="white"/>
	<param name="transparency" value="0.5"/>
	<param name="fill" value="false"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
	<param name="stroke-width" value="2.0"/>
      </xmi:style>
      
      <xmi:style name="table-style">
	<param name="foreground" value="darkred2"/>
	<param name="background" value="white"/>
	<param name="transparency" value="0.3"/>
	<param name="fill" value="true"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
	<param name="stroke-width" value="0.0"/>
      </xmi:style>
      
      <xmi:style name="cluster-style">
	<param name="foreground" value="darkpurple2"/>
	<param name="background" value="white"/>
	<param name="transparency" value="0.5"/>
	<param name="fill" value="false"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
	<param name="stroke-width" value="2.0"/>
      </xmi:style>
      
      <xmi:style name="one-style">
	<param name="foreground" value="darkred1"/>
	<param name="background" value="white"/>
	<param name="transparency" value="0.5"/>
	<param name="fill" value="false"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
	<param name="stroke-width" value="3.0"/>
      </xmi:style>
      
      <xmi:style name="two-style">
	<param name="foreground" value="darkpurple1"/>
	<param name="background" value="white"/>
	<param name="transparency" value="0.5"/>
	<param name="fill" value="false"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
	<param name="stroke-width" value="3.0"/>
      </xmi:style>
      
      <xmi:style name="three-style">
	<param name="foreground" value="orange"/>
	<param name="background" value="white"/>
	<param name="transparency" value="0.5"/>
	<param name="fill" value="false"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
	<param name="stroke-width" value="3.0"/>
      </xmi:style>
      
      <xmi:style name="four-style">
	<param name="foreground" value="darkgreen2"/>
	<param name="background" value="white"/>
	<param name="transparency" value="0.5"/>
	<param name="fill" value="false"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
	<param name="stroke-width" value="3.0"/>
      </xmi:style>
      
      <xmi:style name="five-style">
	<param name="foreground" value="darkcyan1"/>
	<param name="background" value="white"/>
	<param name="transparency" value="0.5"/>
	<param name="fill" value="false"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
	<param name="stroke-width" value="3.0"/>
      </xmi:style>
      
      <xmi:style name="six-style">
	<param name="foreground" value="darkcoral3"/>
	<param name="background" value="white"/>
	<param name="transparency" value="0.5"/>
	<param name="fill" value="false"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
	<param name="stroke-width" value="3.0"/>
      </xmi:style>
      
      <xmi:style name="other-style">
	<param name="foreground" value="black"/>
	<param name="background" value="white"/>
	<param name="transparency" value="0.1"/>
	<param name="fill" value="false"/>
	<param name="resolution" value="10"/>
	<param name="fontfamily" value="arial"/>
	<param name="stroke-width" value="3.0"/>
      </xmi:style>
      
      <!-- commented-out experimental section
      
	<xmi:flag name="type">
	<value name="paragraph" style="cluster-style"/>
	<value name="other-text" style="division-style"/>
	<value name="cell" style="one-style"/>
      </xmi:flag>
	  
	  
      <xmi:handler name="popup" class="iiuf.xmillum.handlers.PopupFlagger">
	<param name="flag" value="type"/>
        <param name="allow-clear" value="true"/>
      </xmi:handler>
      
      <xmi:tool name="flag-output" class="iiuf.xmillum.tool.FlagOutput">
      </xmi:tool>

	<xmi:handler name="info" class="iiuf.xmillum.handlers.Info">
	</xmi:handler>

	<xmi:tool name="default" class="iiuf.xmillum.tool.InfoWindow">
	</xmi:tool>
		-->
		
		
      <xmi:object name="page-image" class="iiuf.xmillum.displayable.Image"/>
      
      <!-- Table display -->
      
      <xmi:object name="segment-group" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="division-style"/>
      </xmi:object>
      
      <!-- END Table display -->
      
      
      <xmi:object name="cluster-text" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.TextArea2">
	<param name="style" value="block-style"/>
      </xmi:object>
      
      <xmi:object name="text-segment" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.TextArea2">
	<param name="style" value="block-style"/>
      </xmi:object>
      
      <xmi:object name="cluster" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="xycut-style"/>
	<!--
	<param name="press1" value="info"/>
	<param name="press3" value="popup"/>
	<param name="press2" value="flag-output"/> -->
      </xmi:object>
      
      <xmi:object name="edge" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="edge-style"/>
      </xmi:object>
      
      <xmi:object name="potential-table-text" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.TextArea2">
	<param name="style" value="block-style"/>
      </xmi:object>
      
      <xmi:object name="table" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="table-style"/>
      </xmi:object>
      
      <xmi:object name="table-cell" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="table-cell-style"/>
      </xmi:object>
      
      <xmi:object name="potential-table" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="potential-table-style"/>
      </xmi:object>
      
      <xmi:object name="heading" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="potential-table-style"/>
      </xmi:object>
      
      <xmi:object name="potential-table-cell" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="xycut-style"/>
      </xmi:object>
      
      <xmi:object name="current-selection" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="textblock-style"/>
      </xmi:object>
      
      <xmi:object name="instance" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="instance-style"/>
      </xmi:object>
      
      <xmi:object name="sub-instance" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="sub-instance-style"/>
      </xmi:object>
      
      <xmi:object name="textblock" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.TextArea2">
	<param name="style" value="block-style"/>
      </xmi:object>
      
      <xmi:object name="text-line" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="yellow-style"/>
      </xmi:object>
      
      <xmi:object name="text" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.TextArea2">
	<param name="style" value="text-style"/>
      </xmi:object>
      
      <xmi:object name="text_fragment" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.TextArea2">
	<param name="style" value="text-style"/>
      </xmi:object>
            
      <xmi:object name="image" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="image-style"/>
      </xmi:object>
      
      <xmi:object name="line" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="line-style"/>
      </xmi:object>
      
      <xmi:object name="rectangle" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="rect-style"/>
      </xmi:object>
      
      <xmi:object name="filled_rect" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="filled-rect-style"/>
      </xmi:object>
      
      <xmi:object name="column_gap" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="five-style"/>
      </xmi:object>
      
      <xmi:object name="page-division" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="image-style"/>
      </xmi:object>
      
      <xmi:object name="table-row" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="table-row-style"/>
      </xmi:object>
      
      <xmi:object name="table-column" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="division-style"/>
      </xmi:object>
      
      <xmi:object name="xycut" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="xycut-style"/>
      </xmi:object>
      
      <xmi:object name="level1" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="one-style"/>
      </xmi:object>
      
      <xmi:object name="level2" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="two-style"/>
      </xmi:object>
      
      <xmi:object name="level3" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="three-style"/>
      </xmi:object>
      
      <xmi:object name="level4" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="four-style"/>
      </xmi:object>
      
      <xmi:object name="level5" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="five-style"/>
      </xmi:object>
      
      <xmi:object name="level6" class="at.ac.tuwien.dbai.pdfwrap.gui.displayable.Block2">
	<param name="style" value="six-style"/>
      </xmi:object>
      
    
    <xmi:layer name="Page Image">
    <!-- <image src="{@image}"/> -->
      <page-image src="output.png"/>
      </xmi:layer>
      
    <xmi:layer name="Clusters">
	<xsl:for-each select="text-block">
	  <cluster x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="{text()}" info="{@info}" type="{@type}" colour="0.0" font-size="{floor(@font-size)}"/>
	  </xsl:for-each>
      </xmi:layer> 
      
    <xmi:layer name="Headings">
	<xsl:for-each select="text-block[@type='heading']">
	  <heading x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="{text()}" info="{@info}" type="{@type}" colour="0.0" font-size="{floor(@font-size)}"/>
	  </xsl:for-each>
      </xmi:layer> 
      
    <xmi:layer name="Edges">
	<xsl:for-each select="edge-segment">
	  <edge x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="{text()}" colour="0.0" font-size="{floor(@font-size)}"/>
	  </xsl:for-each>
      </xmi:layer>  
      
      <xmi:layer name="Lines of text">
	<xsl:for-each select="text-line">
	  <text-line x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="{text()}" colour="0.0" font-size="{floor(@font-size)}"/>
	  </xsl:for-each>
      </xmi:layer> 
      
    <xmi:layer name="Found instances">
	<xsl:for-each select="wrapping-instance">
	  <instance x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="instance" colour="0.0" font-size="{floor(@font-size * 1.0)}"/>
	</xsl:for-each>
      </xmi:layer>
      
      <xmi:layer name="Sub-instances">
	<xsl:for-each select="sub-instance">
	  <sub-instance x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="sub-instance" colour="0.0" font-size="{floor(@font-size * 1.0)}"/>
	</xsl:for-each>
      </xmi:layer>
    
    <!-- Found sub-instances to go here! -->
    
    <xmi:layer name="Images">
	<xsl:for-each select="image-segment">
	  <image x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="Image" colour="0.0" font-size="{floor(@font-size)}"/>
	  </xsl:for-each>
      </xmi:layer> 
      
      <xmi:layer name="Lines">
	<xsl:for-each select="line-segment">
	  <line x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="Line" colour="0.0" font-size="{floor(@font-size)}"/>
	  </xsl:for-each>
      </xmi:layer> 
      
      <xmi:layer name="Rectangles">
	<xsl:for-each select="rect-segment">
	  <rectangle x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="Rectangle" colour="0.0" font-size="{floor(@font-size)}"/>
	  </xsl:for-each>
      </xmi:layer> 
      
      <xmi:layer name="Filled rectangles">
	<xsl:for-each select="filled-rect">
	  <filled_rect x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="Rectangle" colour="0.0" font-size="{floor(@font-size)}"/>
	  </xsl:for-each>
      </xmi:layer> 
      
    <xmi:layer name="Cluster text">
	<xsl:for-each select="text-block">
	  <cluster-text x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="{text()}" colour="0.0" font-size="14.0"/>
	  </xsl:for-each>
      </xmi:layer>  
      
    <!-- Table display -->
      
    
      
	  <xmi:layer name="Potential Tables">
	<xsl:for-each select="candidate-table">
	  <potential-table x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" colour="0.0" font-size="22.0"/>
	</xsl:for-each>
      </xmi:layer>
      
      <xmi:layer name="Tables">
	<xsl:for-each select="table">
	  <table x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="table" colour="0.0" font-size="{floor(@font-size * 1.0)}"/>
	</xsl:for-each>
      </xmi:layer>
      
      <xmi:layer name="Table Cells">
	<xsl:for-each select="table-cell">
	  <table-cell x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="table" colour="0.0" font-size="{floor(@font-size * 1.0)}"/>
	</xsl:for-each>
      </xmi:layer>
    
      
    <!-- END Table display -->
    
    
      <xmi:layer name="Page Divisions">
	<xsl:for-each select="page-division">
	  <page-division x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="seggroup" colour="0.0" font-size="{floor(@font-size * 1.0)}"/>
	</xsl:for-each>
      </xmi:layer>
      
       
      
      
            
      <xmi:layer name="Current selection">
	<xsl:for-each select="current-selection">
	  <current-selection x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="{text()}" colour="0.0" font-size="{floor(@font-size * 0.70)}"/>
	</xsl:for-each>
      </xmi:layer>
      
      
      
      <xmi:layer name="Table Rows">
	<xsl:for-each select="table-row">
	  <table-row x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="seggroup" colour="0.0" font-size="{floor(@font-size * 1.0)}"/>
	</xsl:for-each>
      </xmi:layer>
            
      <xmi:layer name="Table Columns">
	<xsl:for-each select="table-column">
	  <table-column x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="seggroup" colour="0.0" font-size="{floor(@font-size * 1.0)}"/>
	</xsl:for-each>
      </xmi:layer>
           
      
      
    <!--
      <xmi:layer name="Lines of text">
	<xsl:for-each select="text-line">
	  <textline x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="{text()}" colour="0.0" font-size="{floor(@font-size)}"/>
	  </xsl:for-each>
      </xmi:layer>
    -->
    
    <!--     
      <xmi:layer name="level1">
	<xsl:for-each select="level1">
	  <level1 x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="{text()}" colour="0.0" font-size="{floor(@font-size)}"/>
	  </xsl:for-each>
      </xmi:layer>

      <xmi:layer name="level2">
	<xsl:for-each select="level2">
	  <level2 x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="{text()}" colour="0.0" font-size="{floor(@font-size)}"/>
	  </xsl:for-each>
      </xmi:layer>
      
      <xmi:layer name="level3">
	<xsl:for-each select="level3">
	  <level3 x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="{text()}" colour="0.0" font-size="{floor(@font-size)}"/>
	  </xsl:for-each>
      </xmi:layer>
      
      <xmi:layer name="level4">
	<xsl:for-each select="level4">
	  <level4 x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="{text()}" colour="0.0" font-size="{floor(@font-size)}"/>
	  </xsl:for-each>
      </xmi:layer>
      
      <xmi:layer name="level5">
	<xsl:for-each select="level5">
	  <level5 x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="{text()}" colour="0.0" font-size="{floor(@font-size)}"/>
	  </xsl:for-each>
      </xmi:layer>
      
      <xmi:layer name="level6">
	<xsl:for-each select="level6">
	  <level6 x="{floor(@x)}" y="{floor(@y)}" w="{floor(@w)}" h="{floor(@h)}" text="{text()}" colour="0.0" font-size="{floor(@font-size)}"/>
	  </xsl:for-each>
	  </xmi:layer> 
	 -->
    </xmi:document>
  </xsl:template>
</xsl:stylesheet>
