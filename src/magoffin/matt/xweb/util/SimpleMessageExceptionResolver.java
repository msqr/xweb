/* ===================================================================
 * SimpleMessageExceptionResolver.java
 * 
 * Created Mar 18, 2005 5:12:20 PM
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
 * $Id: SimpleMessageExceptionResolver.java,v 1.2 2007/07/12 09:09:55 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * Simple exception handler to display an error message..
 * 
 * <p>Saves the request URL and error message, then returns 
 * the <code>redirectView</code> view, which is assumed to be 
 * a redirect view.</p>
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl>
 *   <dt>redirectView</dt>
 *   <dd>The name of the view, assumed to be a redirect, to return.</dd>
 *   
 *   <dt>exceptionClass</dt>
 *   <dd>The Class of exception to resolve this handler to.</dd>
 * 
 *   <dt>errorMessageKey</dt>
 *   <dd>The key for the message resource for the error message to add to session.</dd>
 * 
 *   <dt>defaultErrorMessage</dt>
 *   <dd>The default error message. Defaults to 'An error has occured.'</dd>
 * 
 *   <dt>saveRequestUrl</dt>
 *   <dd>If <em>true</em> then the {@link XwebHelper#saveRequestURL(HttpServletRequest)}
 *   method will be called, saving the request URL to session. Defaults to 
 *   <em>false</em>.</dd>
 * 
 *   <dt>order</dt>
 *   <dd>An ordering, as per the {@link org.springframework.core.Ordered} 
 *   interface. Defaults to <code>0</code>.</dd>
 *   
 *   <dt>xwebHelper</dt>
 *   <dd>The {@link XwebHelper} instance to use.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2007/07/12 09:09:55 $
 */
public class SimpleMessageExceptionResolver implements HandlerExceptionResolver, Ordered {
	
	private String redirectView;
	private Class<? extends Exception> exceptionClass;
	private String errorMessageKey;
	private String defaultErrorMessage = "An error has occured.";
	private boolean saveRequestUrl = false;
	private int order = 0;
	private XwebHelper xwebHelper;
	
	@Override
	public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex) {
		if ( exceptionClass.isAssignableFrom(ex.getClass()) ) {
			if ( saveRequestUrl ) {
				xwebHelper.saveRequestURL(request);
			}
			ObjectError error = new ObjectError(ex.getClass().getSimpleName(),
					new String[] {errorMessageKey},null,defaultErrorMessage);
			xwebHelper.saveMessage(request, error);
			return new ModelAndView(redirectView);
		}
		return null;
	}

	@Override
	public int getOrder() {
		return order;
	}
	
	/**
	 * @return the defaultErrorMessage
	 */
	public String getDefaultErrorMessage() {
		return defaultErrorMessage;
	}
	
	/**
	 * @param defaultErrorMessage the defaultErrorMessage to set
	 */
	public void setDefaultErrorMessage(String defaultErrorMessage) {
		this.defaultErrorMessage = defaultErrorMessage;
	}
	
	/**
	 * @return the errorMessageKey
	 */
	public String getErrorMessageKey() {
		return errorMessageKey;
	}
	
	/**
	 * @param errorMessageKey the errorMessageKey to set
	 */
	public void setErrorMessageKey(String errorMessageKey) {
		this.errorMessageKey = errorMessageKey;
	}
	
	/**
	 * @return the exceptionClass
	 */
	public Class<? extends Exception> getExceptionClass() {
		return exceptionClass;
	}
	
	/**
	 * @param exceptionClass the exceptionClass to set
	 */
	public void setExceptionClass(Class<? extends Exception> exceptionClass) {
		this.exceptionClass = exceptionClass;
	}
	
	/**
	 * @return the redirectView
	 */
	public String getRedirectView() {
		return redirectView;
	}
	
	/**
	 * @param redirectView the redirectView to set
	 */
	public void setRedirectView(String redirectView) {
		this.redirectView = redirectView;
	}
	
	/**
	 * @return the saveRequestUrl
	 */
	public boolean isSaveRequestUrl() {
		return saveRequestUrl;
	}
	
	/**
	 * @param saveRequestUrl the saveRequestUrl to set
	 */
	public void setSaveRequestUrl(boolean saveRequestUrl) {
		this.saveRequestUrl = saveRequestUrl;
	}
	
	/**
	 * @return the xWebHelper
	 */
	public XwebHelper getXwebHelper() {
		return xwebHelper;
	}
	
	/**
	 * @param webHelper the xWebHelper to set
	 */
	public void setXwebHelper(XwebHelper webHelper) {
		xwebHelper = webHelper;
	}
	
	/**
	 * @param order the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}

}
