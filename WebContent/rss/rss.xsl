<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
  <html>
  <body>
 <xsl:apply-templates select="documentcollection/document/rss/channel"/>
  </body>
  </html>
</xsl:template>

<xsl:template match="/documentcollection/document/rss/channel">
  <p><h3>
  Channel title: <a href="{link}"><xsl:value-of select="title"/></a><br/></h3>
  <xsl:apply-templates select="item"/>
  </p>
</xsl:template>

<xsl:template match="/documentcollection/document/rss/channel/item">
	<xsl:if test="contains(title, 'war') or contains(title, 'peace') or contains(description, 'war') or contains(description, 'peace')">
   <p>
  title: <xsl:value-of select="title"/><br/>
  description: <xsl:value-of select="description"/><br/>
  link: <xsl:value-of select="link"/>
  </p>
</xsl:if>
</xsl:template>


</xsl:stylesheet>


