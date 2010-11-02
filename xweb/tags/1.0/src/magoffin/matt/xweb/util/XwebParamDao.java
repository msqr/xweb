/* ===================================================================
 * XwebParamDao.java
 * 
 * Created Aug 14, 2005 9:24:44 PM
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
 * $Id: XwebParamDao.java,v 1.1 2006/07/10 04:22:35 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import java.util.List;

import magoffin.matt.xweb.XwebParameter;

/**
 * DAO for persistent {@link magoffin.matt.xweb.XwebParameter} objects.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/07/10 04:22:35 $
 */
public interface XwebParamDao {

	/**
	 * Get a XwebParameter by its key.
	 * @param key the key
	 * @return the XwebParameter, or <em>null</em> if no parameter exists for the 
	 * given <em>key</em>
	 */
	public XwebParameter getParameter(String key);
	
	/**
	 * Get a List of all XwebParameter objects.
	 * @return List of XwebParameter objects (never <em>null</em>)
	 */
	public List<XwebParameter> getParameters();
	
	/**
	 * Update a XwebParameter.
	 * @param parameter the setting to update
	 * @return the modified parameter
	 */
	public XwebParameter updateParameter(XwebParameter parameter);
	
	/**
	 * Remove a XwebParameter.
	 * @param key the key of the XwebParameter to delete
	 */
	public void removeParameter(String key);
}
