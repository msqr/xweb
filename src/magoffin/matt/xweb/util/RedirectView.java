/* ===================================================================
 * RedirectView.java
 * 
 * Created Feb 5, 2007 3:04:26 PM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: RedirectView.java,v 1.3 2007/08/13 09:27:15 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

/**
 * Refactoring of {@link org.springframework.web.servlet.view.RedirectView}
 * to allow saving of alert message into session for later retrieval.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.3 $ $Date: 2007/08/13 09:27:15 $
 */
public class RedirectView extends AbstractUrlBasedView {
	
	/** The default encoding schema. */
	public static final String DEFAULT_ENCODING_SCHEME = "UTF-8";


	private XwebHelper webHelper = null;

	private boolean contextRelative = false;

	private boolean http10Compatible = true;

	private String encodingScheme = DEFAULT_ENCODING_SCHEME;
	
	private Set<String> modelQueryParameters = null;


	/**
	 * Constructor for use as a bean.
	 */
	public RedirectView() {
		// nothing to do
	}

	/**
	 * Create a new RedirectView with the given URL.
	 * <p>The given URL will be considered as relative to the web server,
	 * not as relative to the current ServletContext.
	 * @param url the URL to redirect to
	 * @see #RedirectView(String, boolean)
	 */
	public RedirectView(String url) {
		setUrl(url);
	}

	/**
	 * Create a new RedirectView with the given URL.
	 * @param url the URL to redirect to
	 * @param contextRelative whether to interpret the given URL as
	 * relative to the current ServletContext
	 */
	public RedirectView(String url, boolean contextRelative) {
		setUrl(url);
		this.contextRelative = contextRelative;
	}

	/**
	 * Create a new RedirectView with the given URL.
	 * @param url the URL to redirect to
	 * @param contextRelative whether to interpret the given URL as
	 * relative to the current ServletContext
	 * @param http10Compatible whether to stay compatible with HTTP 1.0 clients
	 */
	public RedirectView(String url, boolean contextRelative, boolean http10Compatible) {
		setUrl(url);
		this.contextRelative = contextRelative;
		this.http10Compatible = http10Compatible;
	}

	/**
	 * Set whether to interpret a given URL that starts with a slash ("/")
	 * as relative to the current ServletContext, i.e. as relative to the
	 * web application root.
	 * <p>Default is "false": A URL that starts with a slash will be interpreted
	 * as absolute, i.e. taken as-is. If true, the context path will be
	 * prepended to the URL in such a case.
	 * @see javax.servlet.http.HttpServletRequest#getContextPath
	 * @param contextRelative whether to interpret the given URL as
	 * relative to the current ServletContext
	 */
	public void setContextRelative(boolean contextRelative) {
		this.contextRelative = contextRelative;
	}

	/**
	 * Set whether to stay compatible with HTTP 1.0 clients.
	 * <p>In the default implementation, this will enforce HTTP status code 302
	 * in any case, i.e. delegate to <code>HttpServletResponse.sendRedirect</code>.
	 * Turning this off will send HTTP status code 303, which is the correct
	 * code for HTTP 1.1 clients, but not understood by HTTP 1.0 clients.
	 * <p>Many HTTP 1.1 clients treat 302 just like 303, not making any
	 * difference. However, some clients depend on 303 when redirecting
	 * after a POST request; turn this flag off in such a scenario.
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect
	 * @param http10Compatible true to use HTTP 1.0 style redirect
	 */
	public void setHttp10Compatible(boolean http10Compatible) {
		this.http10Compatible = http10Compatible;
	}

	/**
	 * Set the encoding scheme for this view.
	 * @param encodingScheme the encoding scheme
	 */
	public void setEncodingScheme(String encodingScheme) {
		this.encodingScheme = encodingScheme;
	}


	/**
	 * Convert model to request parameters and redirect to the given URL.
	 * @see #sendRedirect
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected final void renderMergedOutputModel(
			Map model, HttpServletRequest request, HttpServletResponse response) throws IOException {

		// Prepare target URL.
		StringBuilder targetUrl = new StringBuilder();
		if (this.contextRelative && getUrl().startsWith("/")) {
			// Do not apply context path to relative URLs.
			targetUrl.append(request.getContextPath());
		}
		targetUrl.append(getUrl());
		
		if ( model != null ) {
			MessageSourceResolvable message = null;
			Object o = model.get(XwebConstants.ALERT_MESSAGES_OBJECT);
			if ( o instanceof MessageSourceResolvable ) {
				message = (MessageSourceResolvable)o;
			}
			if ( message != null ) {
				webHelper.saveMessage(request, message);
			}
			if ( !CollectionUtils.isEmpty(this.modelQueryParameters) ) {
				if ( targetUrl.indexOf("?") < 0 ) {
					targetUrl.append("?");
				}
				for ( String key : this.modelQueryParameters ) {
					Object val = model.get(key);
					if ( val != null ) {
						if ( targetUrl.charAt(targetUrl.length()-1) != '?' ) {
							targetUrl.append('&');
						}
						targetUrl.append(key).append('=').append(val.toString());
					}
				}
				if ( targetUrl.charAt(targetUrl.length()-1) == '?' ) {
					targetUrl.setLength(targetUrl.length()-1);
				}
			}
		}

		sendRedirect(response, targetUrl.toString());
	}

	/**
	 * URL-encode the given input String with the given encoding scheme.
	 * <p>Default implementation uses <code>URLEncoder.encode(input, enc)</code>
	 * on JDK 1.4+, falling back to <code>URLEncoder.encode(input)</code>
	 * (which uses the platform default encoding) on JDK 1.3.
	 * @param input the unencoded input String
	 * @return the encoded output String
	 * @throws UnsupportedEncodingException if thrown by the JDK URLEncoder
	 * @see java.net.URLEncoder#encode(String, String)
	 * @see java.net.URLEncoder#encode(String)
	 */
	protected String urlEncode(String input) throws UnsupportedEncodingException {
		return URLEncoder.encode(input, encodingScheme);
	}

	/**
	 * Send a redirect back to the HTTP client
	 * @param response current HTTP response (for sending response headers)
	 * @param targetUrl the target URL to redirect to
	 * @throws IOException if thrown by response methods
	 */
	protected void sendRedirect(HttpServletResponse response, String targetUrl)
			throws IOException {
		
		if (http10Compatible) {
			// Always send status code 302.
			response.sendRedirect(response.encodeRedirectURL(targetUrl));
		}
		else {
			// Correct HTTP status code is 303, in particular for POST requests.
			response.setStatus(303);
			response.setHeader("Location", response.encodeRedirectURL(targetUrl));
		}
	}
	
	/**
	 * @return the webHelper
	 */
	public XwebHelper getWebHelper() {
		return webHelper;
	}
	
	/**
	 * @param webHelper the webHelper to set
	 */
	public void setWebHelper(XwebHelper webHelper) {
		this.webHelper = webHelper;
	}

	/**
	 * @return the modelQueryParameters
	 */
	public Set<String> getModelQueryParameters() {
		return modelQueryParameters;
	}

	/**
	 * @param modelQueryParameters the modelQueryParameters to set
	 */
	public void setModelQueryParameters(Set<String> modelQueryParameters) {
		this.modelQueryParameters = modelQueryParameters;
	}
	
}
