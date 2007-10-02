/* ===================================================================
 * IgnoreValidation.java
 * 
 * Created Feb 24, 2005 12:23:57 PM
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
 * $Id: IgnoreValidation.java,v 1.1 2006/07/10 04:22:34 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ValidationEventHandler implementation that simply ignores all 
 * validation events.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/07/10 04:22:34 $
 */
public final class IgnoreValidation implements ValidationEventHandler
{
	/**
	 * Static instance of this class.
	 */
	public static final IgnoreValidation IGNORE_VALIDATION = new IgnoreValidation();
	
	private final Log logger = LogFactory.getLog(IgnoreValidation.class);
	
	/* (non-Javadoc)
	 * @see javax.xml.bind.ValidationEventHandler#handleEvent(javax.xml.bind.ValidationEvent)
	 */
	public boolean handleEvent(ValidationEvent ve) {
	    if ( logger.isDebugEnabled() ) {
	        logger.debug("ValidationEvent: " +ve.getMessage());
	    }
		return true; // ignore errors, ha ha!
	}
}