/* ===================================================================
 * ContentTypeResolver.java
 * 
 * Created Dec 3, 2006 1:10:13 PM
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: ContentTypeResolver.java,v 1.1 2006/12/03 03:14:37 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Dynamic resolution of the content type.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/12/03 03:14:37 $
 */
public interface ContentTypeResolver {

	/**
	 * Resolve the content type from the servlet request.
	 * 
	 * @param request the request
	 * @param model the view model
	 * @return the content type to use
	 */
	public String resolveContentType(HttpServletRequest request, Map<String, ?> model);
	
}
