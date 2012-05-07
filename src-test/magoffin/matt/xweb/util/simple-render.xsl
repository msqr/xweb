<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xweb="http://msqr.us/xsd/jaxb-web"
	xmlns:xwebtest="http://msqr.us/xsd/jaxb-web/test"
	version="1.0">
	
	<xsl:output method="text"/>
	
	<xsl:template match="/xweb:x-data">
		<xsl:text>x-data{</xsl:text>
		<xsl:apply-templates select="*"/>
		<xsl:text>}</xsl:text>
	</xsl:template>
	
	<xsl:template match="*">
		<xsl:value-of select="local-name(.)"/>
		<xsl:text>{</xsl:text>
		<xsl:apply-templates select="attribute::*"/>
		<xsl:if test="count(*) = 0">
			<xsl:value-of select="normalize-space(.)"/>
		</xsl:if>
		<xsl:apply-templates select="*"/>
		<xsl:text>}</xsl:text>
	</xsl:template>
	
	<xsl:template match="attribute::*">
		<xsl:text>@</xsl:text>
		<xsl:value-of select="local-name(.)"/>
		<xsl:text>{</xsl:text>
		<xsl:value-of select="."/>
		<xsl:text>}</xsl:text>
	</xsl:template>
	
</xsl:stylesheet>