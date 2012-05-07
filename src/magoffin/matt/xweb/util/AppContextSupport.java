/* ===================================================================
 * AppContextSupport.java
 * 
 * Created Feb 26, 2005 4:56:06 PM
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
 * $Id: AppContextSupport.java,v 1.2 2007/07/12 09:09:55 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import magoffin.matt.xweb.XwebParameter;
import magoffin.matt.xweb.XwebParameters;

/**
 * Class to support the AppContext configuration.
 * 
 * <p>This class is designed to be stored in the application's ServletContext, 
 * for holding the AppContext data which does not change (at least much) 
 * over the life of the running application.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2007/07/12 09:09:55 $
 */

public class AppContextSupport implements Serializable {

	private static final long serialVersionUID = 8988748274955015805L;

	private XwebParameters appContext;
	private Map<String,String> attributes;
	
	/**
	 * Construct a new AppContextSupport object and initialize with an 
	 * AppContext object.
	 * 
	 * @param appContext the AppContext to initialize with
	 */
	public AppContextSupport(XwebParameters appContext) {
		setAppContext(appContext);
	}
	
	/**
	 * Initialize this instance.
	 */
	private void init() {
		if ( this.appContext == null ) {
			throw new IllegalArgumentException("appContext can not be null");
		}
		attributes = new HashMap<String,String>(this.appContext.getParam().size());
		for ( Iterator<?> itr = this.appContext.getParam().iterator(); itr.hasNext(); ) {
			XwebParameter param = (XwebParameter)itr.next();
			attributes.put(param.getKey(),param.getValue());
		}
	}
	
	/**
	 * Return <em>true</em> if the specifiec parameter is defined as true.
	 * 
	 * <p>The values of <code>1</code>, <code>t</code>, <code>true</code>, 
	 * <code>y</code>, and <code>yes</code> are considered <em>true</em>.</p>
	 * 
	 * @param paramName the AppContext meta parameter to test for truth
	 * @return boolean
	 */
	public boolean isParameterTrue(String paramName) {
		String s = attributes.get(paramName);
		if (s == null) {
			return false;
		}
		boolean result = false;
		s = s.toLowerCase();
		if (s.equals("true") || s.equals("yes") || s.equals("y")
				|| s.equals("t") || s.equals("1")) {
			result = true;
		}
		return result;
	}
	
	/**
	 * Returns a debug-friendly string representing the meta parameters in this 
	 * instance's AppContext object.
	 */
	@Override
	public String toString() {
		return "AppContextSupport"+attributes;
	}

	/**
	 * @return Returns the appContext.
	 */
	public XwebParameters getAppContext() {
		return appContext;
	}

	/**
	 * @param appContext The appContext to set.
	 */
	public void setAppContext(XwebParameters appContext) {
		this.appContext = appContext;
		init();
	}
	
}
