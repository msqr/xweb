/* ===================================================================
 * DefaultXsltViewResolver.java
 * 
 * Created Feb 16, 2005 5:51:46 PM
 * 
 * Copyright (c) 2005 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: DefaultXsltViewResolver.java,v 1.4 2007/02/28 09:02:24 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import java.io.File;
import java.net.URL;
import java.util.Locale;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.Ordered;
import org.springframework.core.io.UrlResource;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractCachingViewResolver;

/**
 * ViewResolver implementation to simplify XSLT view resolution.
 * 
 * <p>This view resolver can eliminate having to create individual view defintions for 
 * individual XSLT views. Using the view name the resolver will look to see if 
 * a web resource exists named &gt;xsltPathPrefix&gt;&lt;viewName&gt;&lt;xsltPathSuffix&gt;. 
 * If so, then the AbstractXsltView bean defined by <code>xsltViewTemplateName</code> 
 * will be obtained,  and the bean property <code>stylesheetLocation</code>
 * will be set with the resolved XSLT resource, and the view returned. Obviously the 
 * <code>xsltViewTemplateName</code> bean definition should have the 
 * <code>singleton="false"</code> property set so that new instances are created  for 
 * each view.</p>
 * 
 * <p>If an XSLT resource can not be found, then the resolver will return <em>null</em>, which
 * allows another view resolver to handle the view (assuming another view resolver is 
 * configured in the application).</p>
 * 
 * <p>The following properties are configurable:</p>
 * 
 * <dl>
 *   <dt>order</dt>
 *   <dd>An ordering for the {@link org.springframework.core.Ordered} interface.</dd>
 *   
 *   <dt>useAbsolutePaths</dt>
 *   <dd>If <em>true</em> then treat the constructed resource path as an absolute URL.
 *   Otherwise treat the resource path as a servlet-relative resource, i.e.
 *   <code>getServletContext().getResource()</code>. Defaults to <em>false</em>
 *   so servlet-relative paths are used.</dd>
 *   
 *   <dt>xsltViewTemplateName</dt>
 *   <dd>The name of a non-singleton Spring bean to use as the template for 
 *   XSLT views resolved by this class. The bean is assumed to have a writable-
 *   property named <code>stylesheetLocation</code> which will be set to the
 *   value of the resolved XSLT resource.</dd>
 *   
 *   <dt>xsltPathPrefix</dt>
 *   <dd>An application-relative prefix to add to XSLT resource paths.
 *   Deftults to <code>/WEB-INF/xsl/</code></dd>
 * 
 *   <dt>xsltPathSuffix</dt>
 *   <dd>A suffix to append to XSLT resource paths. Defaults to 
 *   <code>.xsl</code></dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.4 $ $Date: 2007/02/28 09:02:24 $
 */
public class DefaultXsltViewResolver extends AbstractCachingViewResolver implements ViewResolver, Ordered {
    
	/** The name of the Spring to act as a template for default resolved view instances. */
	private String xsltViewTemplateName;
	
	private String xsltPathPrefix = "/WEB-INF/xsl/";
	private String xsltPathSuffix = ".xsl";
	private boolean useAbsolutePaths = false;
	
	private int order = Integer.MAX_VALUE;
    
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.view.AbstractCachingViewResolver#loadView(java.lang.String, java.util.Locale)
	 */
	@Override
	protected View loadView(String viewName, Locale locale) throws Exception {
		// see if file exists
		String defaultXsltResource = xsltPathPrefix+viewName+xsltPathSuffix;
		URL defaultXslt = null;
		if ( useAbsolutePaths ) {
			defaultXslt = new URL(defaultXsltResource);
			if ( !new File(defaultXslt.getPath()).exists() ) {
				return null;
			}
		} else {
			defaultXslt = getServletContext().getResource(defaultXsltResource);
		}
		if ( defaultXslt == null ) {
		    return null; // allow chaining to another view resolver
		}
		
		// got default, so create new view now
		View view = (View)getApplicationContext().getBean(
		        xsltViewTemplateName,View.class);
		
		// set the bean name to the view name, in place of the template name
		if ( view instanceof BeanNameAware ) {
			((BeanNameAware)view).setBeanName(viewName);
		}
		
		// set the stylesheet location
		BeanWrapper wrapper = new BeanWrapperImpl(view);
		wrapper.setPropertyValue("stylesheetLocation",new UrlResource(defaultXslt));
		
		return view;
    }

	/**
	 * @return Returns the order.
	 */
	public int getOrder() {
		return order;
	}
	
	/**
	 * @param order The order to set.
	 */
	public void setOrder(int order) {
		this.order = order;
	}
	
	/**
	 * @return Returns the xsltPathPrefix.
	 */
	public String getXsltPathPrefix() {
		return xsltPathPrefix;
	}
	
	/**
	 * @param xsltPathPrefix The xsltPathPrefix to set.
	 */
	public void setXsltPathPrefix(String xsltPathPrefix) {
		this.xsltPathPrefix = xsltPathPrefix;
	}
	
	/**
	 * @return Returns the xsltPathSuffix.
	 */
	public String getXsltPathSuffix() {
		return xsltPathSuffix;
	}
	
	/**
	 * @param xsltPathSuffix The xsltPathSuffix to set.
	 */
	public void setXsltPathSuffix(String xsltPathSuffix) {
		this.xsltPathSuffix = xsltPathSuffix;
	}
	
	/**
	 * @return Returns the xsltViewTemplateName.
	 */
	public String getXsltViewTemplateName() {
		return xsltViewTemplateName;
	}
	
	/**
	 * @param xsltViewTemplateName The xsltViewTemplateName to set.
	 */
	public void setXsltViewTemplateName(String xsltViewTemplateName) {
		this.xsltViewTemplateName = xsltViewTemplateName;
	}
	
	/**
	 * @return the useAbsolutePaths
	 */
	public boolean isUseAbsolutePaths() {
		return useAbsolutePaths;
	}
	
	/**
	 * @param useAbsolutePaths the useAbsolutePaths to set
	 */
	public void setUseAbsolutePaths(boolean useAbsolutePaths) {
		this.useAbsolutePaths = useAbsolutePaths;
	}

 }
