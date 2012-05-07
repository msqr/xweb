/* ===================================================================
 * HttpStatusExceptionResolver.java
 * 
 * Created Apr 1, 2007 8:04:05 PM
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
 * $Id: HttpStatusExceptionResolver.java,v 1.1 2007/04/01 08:32:52 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * HandlerExceptionResolver for resolving exceptions to HTTP status codes.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl>
 *   <dt>exceptionClass</dt>
 *   <dd>The Class of exception to resolve this handler to.</dd>
 *   
 *   <dt>includeErrorMessage</dt>
 *   <dd>If <em>true</em> and the exception contains a message, then
 *   use the {@link HttpServletResponse#sendError(int, String)} method
 *   to return an error page with a message. Otherwise, use the 
 *   {@link HttpServletResponse#sendError(int)} to just send the error
 *   status code response.</dd>
 *   
 *   <dt>order</dt>
 *   <dd>An ordering, as per the {@link org.springframework.core.Ordered} 
 *   interface. Defaults to <code>0</code>.</dd>
 *   
 *   <dt>responseStatus</dt>
 *   <dd>The {@link HttpServletResponse} status code to return.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2007/04/01 08:32:52 $
 */
public class HttpStatusExceptionResolver implements HandlerExceptionResolver,
		Ordered {

	private int order = 0;
	private Class<? extends Exception> exceptionClass;
	private int responseStatus;
	private boolean includeErrorMessage = false;
	
	private final Logger log = Logger.getLogger(getClass());

	@Override
	public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex) {
		if ( exceptionClass.isAssignableFrom(ex.getClass()) ) {
			try {
				if ( includeErrorMessage && StringUtils.hasText(ex.getMessage())) {
					response.sendError(this.responseStatus, ex.getMessage());
				} else {
					response.sendError(this.responseStatus);
				}
			} catch ( IOException e ) {
				log.warn("IOException sending error response [" +this.responseStatus
						+"]", e);
			}
		}
		return null;
	}

	@Override
	public int getOrder() {
		return order;
	}
	
	/**
	 * @param order the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
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
	 * @return the includeErrorMessage
	 */
	public boolean isIncludeErrorMessage() {
		return includeErrorMessage;
	}
	
	/**
	 * @param includeErrorMessage the includeErrorMessage to set
	 */
	public void setIncludeErrorMessage(boolean includeErrorMessage) {
		this.includeErrorMessage = includeErrorMessage;
	}
	
	/**
	 * @return the responseStatus
	 */
	public int getResponseStatus() {
		return responseStatus;
	}

	/**
	 * @param responseStatus the responseStatus to set
	 */
	public void setResponseStatus(int responseStatus) {
		this.responseStatus = responseStatus;
	}

}
