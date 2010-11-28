/* ===================================================================
 * DynamicInitializer.java
 * 
 * Created Aug 16, 2004 2:54:21 PM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: DynamicInitializer.java,v 1.1 2006/09/06 00:43:35 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

/**
 * Interface for dynamic initialization of an object.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/09/06 00:43:35 $
 */
public interface DynamicInitializer {
	
	/**
	 * Create a new instance of an object.
	 * 
	 * @param bean the bean to create the object for
	 * @param property the property to create the object for
	 * @return the newly created object
	 */
	public Object newInstance(Object bean, String property);

}
