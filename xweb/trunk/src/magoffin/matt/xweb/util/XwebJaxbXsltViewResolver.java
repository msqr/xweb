/* ===================================================================
 * XwebJaxbXsltViewResolver.java
 * 
 * Created May 9, 2012 9:21:08 AM
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

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.sf.ehcache.Ehcache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.xslt.XsltViewResolver;

/**
 * View resolver for {@link XwebJaxbXsltView}.
 * 
 * <p>
 * The configurable properties of this class are:
 * </p>
 * 
 * <dl>
 * <dt>jaxbContext</dt>
 * <dd>The classpath-style JAXB context to use, for example
 * <code>com.supercool.domain</code>.</dd>
 * 
 * <dt>ignoreMarshallErrors</dt>
 * <dd>Boolean value; if <em>true</em> then ignore marshall errors. If
 * <em>false</em> then fail on marshall errors. Defaults to <em>true</em>.
 * Useful for debugging.</dd>
 * 
 * <dt>debugMessageResource</dt>
 * <dd>Boolean value; if <em>true</em> then if <code>DEBUG</code> level logging
 * is enabled for this class then the entire <code>XwebMessages</code> data
 * object will be output with the <code>Xweb</code> XML object to the log. If
 * <em>false</em> then the Xweb XML output will not include it. Since the
 * <code>XwebMessages</code> object will contain a lot of data, it is useful to
 * not output that while debugging Xweb output.</dd>
 * 
 * <dt>marshallerProperties</dt>
 * <dd>A Map of properties to set on the JAXB Marshaller. This is useful for
 * passing such properties as
 * <code>com.sun.xml.bind.namespacePrefixMapper</code> property to output valid
 * XHTML.</dd>
 * 
 * <dt>messagesSource</dt>
 * <dd>The <code>MessagesSource</code> object which will be turned into a
 * <code>XwebMessages</code> and added to the output Xweb XML object. This makes
 * internationalized messages available to the view XSLT.</dd>
 * 
 * <dt>useAbsolutePaths</dt>
 * <dd>If <em>true</em> then absolute file paths are assumed for the URLs
 * configured by this resolver. This supports running outside of a web-based
 * application context. Defaults to <em>false</em>.</dd>
 * </dl>
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public class XwebJaxbXsltViewResolver extends XsltViewResolver implements Ordered {

	private String jaxbContext;
	private Map<String, Object> marshallerProperties = new HashMap<String, Object>();
	private Ehcache appSettingsCache = null;
	private boolean ignoreMarshallErrors = true;
	private boolean useAbsolutePaths = false;
	private boolean debugMessageResource = false;

	@Autowired
	private MessagesSource messagesSource;

	@Autowired
	private XwebHelper webHelper;

	/**
	 * Default constructor.
	 */
	public XwebJaxbXsltViewResolver() {
		super();
		setViewClass(XwebJaxbXsltView.class);
	}

	@Override
	protected AbstractUrlBasedView buildView(String viewName) throws Exception {
		XwebJaxbXsltView view = (XwebJaxbXsltView) super.buildView(viewName);
		view.setJaxbContext(jaxbContext);
		view.setMessagesSource(messagesSource);
		view.setWebHelper(webHelper);
		view.setMarshallerProperties(marshallerProperties);
		view.setAppSettingsCache(appSettingsCache);
		view.setIgnoreMarshallErrors(ignoreMarshallErrors);
		view.setUseAbsolutePaths(useAbsolutePaths);
		view.setDebugMessageResource(debugMessageResource);
		return view;
	}

	@Override
	protected View loadView(String viewName, Locale locale) throws Exception {
		try {
			return super.loadView(viewName, locale);
		} catch ( RuntimeException e ) {
			Throwable root = e;
			while ( root.getCause() != null ) {
				root = root.getCause();
			}
			if ( root instanceof FileNotFoundException ) {
				// ignore this, and simply return null (happens when cacheTemplates == true)
				return null;
			}
			throw e;
		}
	}

	public String getJaxbContext() {
		return jaxbContext;
	}

	public void setJaxbContext(String jaxbContext) {
		this.jaxbContext = jaxbContext;
	}

	public Map<String, Object> getMarshallerProperties() {
		return marshallerProperties;
	}

	public void setMarshallerProperties(Map<String, Object> marshallerProperties) {
		this.marshallerProperties = marshallerProperties;
	}

	public Ehcache getAppSettingsCache() {
		return appSettingsCache;
	}

	public void setAppSettingsCache(Ehcache appSettingsCache) {
		this.appSettingsCache = appSettingsCache;
	}

	public MessagesSource getMessagesSource() {
		return messagesSource;
	}

	public void setMessagesSource(MessagesSource messagesSource) {
		this.messagesSource = messagesSource;
	}

	public XwebHelper getWebHelper() {
		return webHelper;
	}

	public void setWebHelper(XwebHelper webHelper) {
		this.webHelper = webHelper;
	}

	public boolean isIgnoreMarshallErrors() {
		return ignoreMarshallErrors;
	}

	public void setIgnoreMarshallErrors(boolean ignoreMarshallErrors) {
		this.ignoreMarshallErrors = ignoreMarshallErrors;
	}

	public boolean isUseAbsolutePaths() {
		return useAbsolutePaths;
	}

	public void setUseAbsolutePaths(boolean useAbsolutePaths) {
		this.useAbsolutePaths = useAbsolutePaths;
	}

	public boolean isDebugMessageResource() {
		return debugMessageResource;
	}

	public void setDebugMessageResource(boolean debugMessageResource) {
		this.debugMessageResource = debugMessageResource;
	}

}
