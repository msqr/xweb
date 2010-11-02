/* ===================================================================
 * XDataPostProcessor.java
 * 
 * Created Mar 9, 2006 8:48:39 PM
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
 * $Id: XDataPostProcessor.java,v 1.1 2006/07/10 04:22:35 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import javax.servlet.http.HttpServletRequest;

import magoffin.matt.xweb.XData;

/**
 * API for allowing for XWeb XData post processing.
 * 
 * <p>This API allows for views that depend on specific data being 
 * present to add or update the current model data without needing
 * to send a re-direct to the client so the needed controller can 
 * be run to generate that data. For example a "home" view might 
 * need a specific set of data always, and that view is often the 
 * result view for many controllers. Since the "home" controller 
 * knows best how to populate the data needed by the "home" view, 
 * an XDataPostProcessor can be configured in the other views that 
 * knows how to populate this data, in many cases by calling methods
 * on the "home" controller itself.</p>
 * 
 * @author matt.magoffin
 * @version $Revision: 1.1 $ $Date: 2006/07/10 04:22:35 $
 */
public interface XDataPostProcessor {
	
	/**
	 * Return <em>true</em> if this implementation supports the current view.
	 * @param viewName the view name
	 * @return boolean
	 */
	boolean supportsView(String viewName);
	
	/**
	 * Process the view.
	 * @param xData the xData to process
	 * @param request the current request
	 */
	void process(XData xData, HttpServletRequest request);
	
}
