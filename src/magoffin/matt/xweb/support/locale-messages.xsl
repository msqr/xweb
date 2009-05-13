<?xml version="1.0" encoding="UTF-8"?>
<!--
	Generates JSON encoded i18n message bundle from Xweb messages.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:x="http://msqr.us/xsd/jaxb-web">
	
	<xsl:output method="text"/>
	
	<xsl:template match="x:x-data">
		<xsl:text>({</xsl:text>
		<xsl:apply-templates select="x:x-msg/x:msg"/>
		<xsl:text>})</xsl:text>
	</xsl:template>
	
	<xsl:template match="x:msg">
		<xsl:if test="position() &gt; 1">
			<xsl:text>,&#xA;</xsl:text>
		</xsl:if>
		<xsl:text>"</xsl:text>
		<xsl:value-of select="@key"/>
		<xsl:text>" : "</xsl:text>
		<xsl:call-template name="javascript-string">
			<xsl:with-param name="output-string" select="string(.)"/>
		</xsl:call-template>
		<xsl:text>"</xsl:text>
	</xsl:template>
	
	<!--
		Named Template: javascript-string
		
		Replace occurances of " in a string with \".
		
		Parameters:
			output-string	- the text to seach/replace in
	-->
	<xsl:template name="javascript-string">
		<xsl:param name="output-string"/>
		<xsl:call-template name="global-replace">
			<xsl:with-param name="output-string" select="$output-string"/>
			<xsl:with-param name="target"><xsl:text>"</xsl:text></xsl:with-param>
			<xsl:with-param name="replacement"><xsl:text>\"</xsl:text></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!--
		Named Template: global-replace
		
		Replace occurances of one string with another.
		
		Parameters:
			output-string	- the text to seach/replace in
			target			- the text to search for
			replacement		- the text to replace occurances of 'target' with
	-->
	<xsl:template name="global-replace">
		<xsl:param name="output-string"/>
		<xsl:param name="target"/>
		<xsl:param name="replacement"/>
		<xsl:choose>
			<xsl:when test="contains($output-string,$target)">
				<xsl:value-of select=
					"concat(substring-before($output-string,$target), $replacement)"/>
				<xsl:call-template name="global-replace">
					<xsl:with-param name="output-string" 
						 select="substring-after($output-string,$target)"/>
					<xsl:with-param name="target" select="$target"/>
					<xsl:with-param name="replacement" 
						 select="$replacement"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$output-string"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>
