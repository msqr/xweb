/* ===================================================================
 * BasicXwebHelper.java
 * 
 * Created Jul 2, 2006 9:33:29 PM
 * 
 * Copyright (c) 2006 Matt Magoffin.
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
 * $Id: BasicXwebHelper.java,v 1.4 2007/08/20 01:25:30 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.MessageSourceResolvable;

/**
 * Basic implementation of XwebHelper.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.4 $ $Date: 2007/08/20 01:25:30 $
 */
public class BasicXwebHelper implements XwebHelper {
	
	private AppContextSupport appContextSupport = null;

	/**
	 * Default constsructor.
	 */
	public BasicXwebHelper() {
		// nothing
	}
	
	/**
	 * Construct with an AppContextSupport.
	 * @param appContextSupport the AppContextSupport
	 */
	public BasicXwebHelper(AppContextSupport appContextSupport) {
		this.appContextSupport = appContextSupport;
	}
	
	/**
	 * Get the application's AppContextSupport instance.
	 * 
	 * <p>If this object was constructed with an AppContextSupport
	 * via {@link #BasicXwebHelper(AppContextSupport)} that 
	 * instance will be returned. Otherwise, this method looks for the 
	 * AppContextSupport on the servlet context attribute key 
	 * {@link XwebConstants#APP_KEY_APP_CONTEXT}.</p>
	 * 
	 * @param request the current request
	 */
	public AppContextSupport getAppContextSupport(HttpServletRequest request) {
		if ( this.appContextSupport != null ) {
			return this.appContextSupport;
		}
		Object o = request.getSession().getServletContext().getAttribute(
				XwebConstants.APP_KEY_APP_CONTEXT);
		if ( o instanceof AppContextSupport ) {
			return (AppContextSupport)o;
		}
		return null;
	}

	/**
	 * Save a message into session.
	 * 
	 * <p>This saves the message to session at the 
	 * {@link XwebConstants#SES_KEY_SAVED_MESSAGE} key.</p>
	 * 
	 * @param request the current request
	 * @param message the message to save
	 * @see #getSavedMessage(HttpServletRequest)
	 */
	public final void saveMessage(HttpServletRequest request, MessageSourceResolvable message) {
		request.getSession().setAttribute(XwebConstants.SES_KEY_SAVED_MESSAGE,message);
	}
	
	/**
	 * Get a message previously saved with {@link #saveMessage}.
	 * 
	 * @param request the current request
	 * @return the saved message, or <em>null</em> if not available
	 */
	public final MessageSourceResolvable getSavedMessage(HttpServletRequest request) {
		Object o = request.getSession().getAttribute(XwebConstants.SES_KEY_SAVED_MESSAGE);
		if ( o instanceof MessageSourceResolvable ) {
			return (MessageSourceResolvable)o;
		}
		return null;
	}
	
	/**
	 * Remove a message saved via {@link #saveMessage} from session.
	 * 
	 * @param request the current request
	 */
	public final void clearSavedMessage(HttpServletRequest request) {
		request.getSession().removeAttribute(XwebConstants.SES_KEY_SAVED_MESSAGE);
	}

	/**
	 * Save the current request URL to session.
	 * 
	 * <p>The URL will be saved to the 
	 * {@link XwebConstants#SES_KEY_SAVED_URL} key.</p>
	 * 
	 * @param request the current request
	 * @see #getSavedRequestURL(HttpServletRequest)
	 */
	public void saveRequestURL(HttpServletRequest request) {
		StringBuffer buf = request.getRequestURL();
		String queryString = request.getQueryString();
		if ( queryString != null ) {
			buf.append('?');
			buf.append(queryString);
		}
		request.getSession().setAttribute(XwebConstants.SES_KEY_SAVED_URL,buf.toString());
	}

	/**
	 * Get the session-saved URL.
	 * 
	 * @param request the current request
	 * @return the saved URL, or <em>null</em> if none previously saved
	 * @see #saveRequestURL(HttpServletRequest)
	 */
	public String getSavedRequestURL(HttpServletRequest request) {
		return (String)request.getSession().getAttribute(XwebConstants.SES_KEY_SAVED_URL);
	}
	
}
