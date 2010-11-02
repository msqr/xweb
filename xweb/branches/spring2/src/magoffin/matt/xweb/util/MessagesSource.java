/* ===================================================================
 * MessagesSource.java
 * 
 * Created Aug 4, 2004 8:10:06 PM
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
 * $Id: MessagesSource.java,v 1.2 2007/09/25 06:23:43 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import java.util.Enumeration;
import java.util.Locale;

import org.springframework.context.MessageSource;

/**
 * Extension of MessageSource to allow for getting all messages.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2007/09/25 06:23:43 $
 */
public interface MessagesSource extends MessageSource {

	/**
	 * Get an enumeration of keys.
	 * @param locale
	 * @return enumeration of message keys
	 */
	public Enumeration<String> getKeys(Locale locale);
	
	/**
	 * Register an additional message resource at runtime.
	 * 
	 * @param resource the resource path to register
	 */
	public void registerMessageResource(String resource);
	
}
