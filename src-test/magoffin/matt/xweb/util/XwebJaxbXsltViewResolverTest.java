/* ===================================================================
 * XwebJaxbXsltViewResolverTest.java
 * 
 * Created May 9, 2012 4:55:05 PM
 * 
 * Copyright (c) 2012 Matt Magoffin.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ===================================================================
 * $Id$
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * Unit test for the {@link XwebJaxbXsltViewResolver}.
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public class XwebJaxbXsltViewResolverTest {

	private XwebJaxbXsltViewResolver getResolverInstance() throws Exception {
		XwebJaxbXsltViewResolver resolver = new XwebJaxbXsltViewResolver();
		resolver.setJaxbContext("magoffin.matt.xwebtest");
		resolver.setCacheTemplates(false);
		resolver.setIgnoreMarshallErrors(false);
		resolver.setApplicationContext(new StaticApplicationContext());
		resolver.setUseAbsolutePaths(true);
		resolver.setPrefix("file:"
				+ new File(getClass().getResource("identity.xsl").toURI()).getParentFile()
						.getAbsolutePath() + '/');
		resolver.setSuffix(".xsl");

		Map<String, Object> marshallerProps = new HashMap<String, Object>();
		JAXBNamespacePrefixMapper mapper = new JAXBNamespacePrefixMapper();
		mapper.getNamespaceMapping().put("http://msqr.us/xsd/jaxb-web/test", "t");
		mapper.setPredeclareUriList(new String[] { "http://msqr.us/xsd/jaxb-web/test" });
		marshallerProps.put("com.sun.xml.bind.namespacePrefixMapper", mapper);
		resolver.setMarshallerProperties(marshallerProps);

		return resolver;
	}

	@Test
	public void resolveNotFound() throws Exception {
		ViewResolver resolver = getResolverInstance();
		View results = resolver.resolveViewName("nadda", Locale.getDefault());
		assertNull(results);
	}

	@Test
	public void resolve() throws Exception {
		ViewResolver resolver = getResolverInstance();
		View results = resolver.resolveViewName("identity", Locale.getDefault());
		assertNotNull(results);
	}
}
