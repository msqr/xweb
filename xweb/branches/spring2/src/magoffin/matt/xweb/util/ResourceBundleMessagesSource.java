/* ===================================================================
 * ResourceBundleMessagesSource.java
 * 
 * Created Aug 4, 2004 8:09:50 PM
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
 * $Id: ResourceBundleMessagesSource.java,v 1.4 2007/09/25 06:23:43 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Extension of {@link ResourceBundleMessagesSource} to allow finding 
 * all keys for all messages.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.4 $ $Date: 2007/09/25 06:23:43 $
 */
public class ResourceBundleMessagesSource extends ResourceBundleMessageSource 
implements MessagesSource
{
	/** Private copy of basenames, as parent class does not provide a way to access this. */
	private String[] basenames;
	private MessagesSource parent;

	@Override
	public void setBasenames(String[] basenames)  {
		super.setBasenames(basenames);
		this.basenames = basenames;
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.xweb.util.MessagesSource#registerMessageResource(java.lang.String)
	 */
	public void registerMessageResource(String resource) {
		String [] newBasenames = new String[basenames.length+1];
		System.arraycopy(basenames, 0, newBasenames, 0, basenames.length);
		newBasenames[newBasenames.length-1] = resource;
		super.setBasenames(newBasenames);
		this.basenames = newBasenames;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.xweb.util.MessagesSource#getKeys(java.util.Locale)
	 */
	public Enumeration<String> getKeys(Locale locale) {
		if ( basenames.length == 1 && parent == null ) {
			// handle simple case
			try {
				ResourceBundle bundle = ResourceBundle.getBundle(basenames[0], locale,
						getBundleClassLoader());
				return bundle.getKeys();
			} catch (MissingResourceException ex) {
				logger.warn("ResourceBundle [" + basenames[0] 
						+ "] not found for MessageSource: " + ex.getMessage());
			}
			List<String> emptyList = Collections.emptyList();
			return Collections.enumeration(emptyList);
		}
		// return a combined list of all bundles' keys
		Set<String> combinedKeys = new LinkedHashSet<String>();
		if ( parent != null ) {
			Enumeration<String> parentKeys = parent.getKeys(locale);
			while ( parentKeys.hasMoreElements() ) {
				combinedKeys.add(parentKeys.nextElement());
			}
		}
		for (int i = 0; i < this.basenames.length; i++) {
			try {
				ResourceBundle bundle = ResourceBundle.getBundle(basenames[i], 
						locale, getBundleClassLoader());
				Enumeration<String> enumeration = bundle.getKeys();
				while ( enumeration.hasMoreElements() ) {
					combinedKeys.add(enumeration.nextElement());
				}
			} catch (MissingResourceException ex) {
				logger.warn("ResourceBundle [" + basenames[i] 
						+ "] not found for MessageSource: " + ex.getMessage());
			}
		}
		return Collections.enumeration(combinedKeys);
	}

	
	@Override
	protected String getMessageInternal(String code, Object[] args,
			Locale locale) {
		String msg = super.getMessageInternal(code, args, locale);
		if ( msg != null ) {
			return msg;
		}
		if ( parent != null ) {
			return parent.getMessage(code, args, locale);
		}
		return null;
	}

	/**
	 * @return the parent
	 */
	public MessagesSource getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(MessagesSource parent) {
		this.parent = parent;
	}

}
