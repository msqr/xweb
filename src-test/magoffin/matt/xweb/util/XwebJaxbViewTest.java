/* ===================================================================
 * XwebJaxbViewTest.java
 * 
 * Created May 7, 2012 12:59:52 PM
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import magoffin.matt.xweb.BaseTest;
import magoffin.matt.xwebtest.ObjectFactory;
import magoffin.matt.xwebtest.TestParam;
import magoffin.matt.xwebtest.XwebTest;
import magoffin.matt.xwebtest.XwebTest.Params;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit test for XwebjaxbView class.
 *
 * @author matt
 * @version $Revision$ $Date$
 */
public class XwebJaxbViewTest extends BaseTest {

	@SuppressWarnings("deprecation")
	private XwebJaxbView getViewInstance() throws Exception {
		XwebJaxbView view = new XwebJaxbView();
		view.setJaxbContext("magoffin.matt.xwebtest");
		view.setCache(false);
		view.setTransformerFactory(javax.xml.transform.TransformerFactory.newInstance());
		view.setDefaultIgnoreMarshallErrors(false);
		view.setApplicationContext(new StaticApplicationContext());

		Map<String, Object> marshallerProps = new HashMap<String, Object>();
		JAXBNamespacePrefixMapper mapper = new JAXBNamespacePrefixMapper();
		mapper.getNamespaceMapping().put("http://msqr.us/xsd/jaxb-web/test", "t");
		mapper.setPredeclareUriList(new String[] { "http://msqr.us/xsd/jaxb-web/test" });
		marshallerProps.put("com.sun.xml.bind.namespacePrefixMapper", mapper);
		view.setMarshallerProperties(marshallerProps);
		view.afterPropertiesSet();
		return view;
	}

	@Test
	public void simpleRender() throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest("GET", "/test.html");
		req.setContextPath("/context");
		req.setServletPath("/path");
		req.setParameter("p1", "p1v1");
		req.setParameter("p2", new String[] { "p2v1", "p2v2" });
		req.addHeader("h1", "h1v1");
		MockHttpServletResponse res = new MockHttpServletResponse();
		XwebJaxbView view = getViewInstance();

		XwebTest obj = new XwebTest();
		obj.setString("string");

		Params p = new Params();
		TestParam tp = new TestParam();
		tp.setKey("key");
		tp.setValue("value");
		p.getParam().add(tp);

		Map<String, Object> model = new LinkedHashMap<String, Object>();
		model.put(XwebConstants.DEFALUT_MODEL_OBJECT, new ObjectFactory().createTest(obj));
		view.render(model, req, res);

		String result = res.getContentAsString();
		assertNotNull(result);

		Transformer t = TransformerFactory.newInstance()
				.newTransformer(
						new StreamSource(new ClassPathResource("simple-render.xsl", getClass())
								.getInputStream()));
		ByteArrayOutputStream byos = new ByteArrayOutputStream();
		t.transform(new StreamSource(new StringReader(result)), new StreamResult(byos));
		String verify = byos.toString();
		assertEquals("x-data{" + DEFAULT_X_CONTEXT
				+ "x-request{param{@key{p1}p1v1}param{@key{p2}p2v1}param{@key{p2}p2v2}}"
				+ DEFAULT_X_REQ_HEADERS + "x-model{test{string{string}}}" + "}",
				verify);
	}
	
	private static final String DEFAULT_X_CONTEXT = "x-context{server-name{localhost}server-port{80}user-locale{en}web-context{/context}path{/path}}";
	private static final String DEFAULT_X_REQ_HEADERS = "x-request-headers{param{@key{h1}h1v1}}";

}
