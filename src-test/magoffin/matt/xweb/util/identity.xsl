<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	
	<xsl:output method="xml" omit-xml-declaration="no"/>
	
	<xsl:template match="/">
		<xsl:copy>
			<xsl:copy-of select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>
