/* ===================================================================
 * BasicNamespaceContext.java
 * 
 * Created Dec 4, 2007 8:23:53 AM
 * 
 * Copyright (c) 2007 Matt Magoffin.
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * A configurable {@link NamespaceContext} to use with XPath.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl class="class-properties">
 *   <dt>namesapcePrefixMap</dt>
 *   <dd>A mapping of prefix values to namespace URI values.</dd>
 * </dl>
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public class BasicNamespaceContext implements NamespaceContext {

	private Map<String, String> namespacePrefixMap = Collections.emptyMap();	
	private Map<String, String> reverseMap = null;
	
	/**
	 * Initialize this class for use, after configuring the class properties.
	 */
	public void init() {
		Map<String, String> rMap = new LinkedHashMap<String, String>();
		for ( Map.Entry<String, String> me : namespacePrefixMap.entrySet() ) {
			rMap.put(me.getValue(), me.getKey());
		}
		this.reverseMap = rMap;
	}

	/* (non-Javadoc)
	 * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
	 */
	public String getNamespaceURI(String prefix) {
		if ( prefix.equals(XMLConstants.XML_NS_PREFIX) ) {
            return XMLConstants.XML_NS_URI;
        } else if ( prefix.equals(XMLConstants.XMLNS_ATTRIBUTE) ) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }
		if ( namespacePrefixMap.containsKey(prefix) ) {
			return namespacePrefixMap.get(prefix);
		}
		return XMLConstants.NULL_NS_URI;
	}

	/* (non-Javadoc)
	 * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
	 */
	public String getPrefix(String namespaceURI) {
		return reverseMap.get(namespaceURI);
	}

	/* (non-Javadoc)
	 * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
	 */
	@SuppressWarnings("rawtypes")
	public Iterator getPrefixes(String namespaceURI) {
		return namespacePrefixMap.keySet().iterator();
	}

	/**
	 * @return the namespacePrefixMap
	 */
	public Map<String, String> getNamespacePrefixMap() {
		return namespacePrefixMap;
	}
	
	/**
	 * @param namespacePrefixMap the namespacePrefixMap to set
	 */
	public void setNamespacePrefixMap(Map<String, String> namespacePrefixMap) {
		this.namespacePrefixMap = namespacePrefixMap;
	}

}
