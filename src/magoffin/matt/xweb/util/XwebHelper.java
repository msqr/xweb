/* ===================================================================
 * XwebHelper.java
 * 
 * Created Jul 2, 2006 9:20:01 PM
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
 * $Id: XwebHelper.java,v 1.2 2006/09/04 06:34:56 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.MessageSourceResolvable;

/**
 * Helper API for Xweb.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.2 $ $Date: 2006/09/04 06:34:56 $
 */
public interface XwebHelper {

	/**
	 * Get an AppContextSupport instance for the current request.
	 * @param request the request
	 * @return the app context support
	 */
	AppContextSupport getAppContextSupport(HttpServletRequest request);
	
	/**
	 * Save a message to session.
	 * 
	 * @param request the current request
	 * @param message the error to save
	 */
	public void saveMessage(HttpServletRequest request, 
			MessageSourceResolvable message);
	
	/**
	 * Get the saved message from session.
	 * 
	 * @param request the current request
	 * @return the message, or <em>null</em> if not available
	 */
	public MessageSourceResolvable getSavedMessage(HttpServletRequest request);
	
	/**
	 * Remove the saved session URL.
	 * 
	 * @param request the current request
	 */
	public void clearSavedMessage(HttpServletRequest request);

	/**
	 * Save the current URL to session.
	 * 
	 * <p>The saved URL will contain any query string passed along with the request.</p>
	 * 
	 * @param request the current request
	 * @see #getSavedRequestURL(HttpServletRequest)
	 */
	public void saveRequestURL(HttpServletRequest request);
	
	/**
	 * Get the session-saved URL.
	 * 
	 * @param request the current request
	 * @return the saved URL, or <em>null</em> if none previously saved
	 * @see #saveRequestURL(HttpServletRequest)
	 */
	public String getSavedRequestURL(HttpServletRequest request);

}
