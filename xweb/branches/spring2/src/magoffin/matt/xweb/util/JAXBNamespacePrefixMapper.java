/* ===================================================================
 * JAXBNamespacePrefixMapper.java
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
 * $Id: JAXBNamespacePrefixMapper.java,v 1.1 2006/07/10 04:22:35 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * JAXB RI utility to map XML namespaces in a more human-readable way.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl>
 *   <dt>namespaceMapping</dt>
 *   <dd>A mapping of namespace URIs to prefix values.</dd>
 *   
 *   <dt>predeclareUriList</dt>
 *   <dd>An array of namespace URIs to predeclare.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/07/10 04:22:35 $
 */
public class JAXBNamespacePrefixMapper extends NamespacePrefixMapper {
	
	private Map<String,String> namespaceMapping = 
		new LinkedHashMap<String,String>();
	private String[] predeclareUriList = new String[0];
	
	@Override
	public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
		if ( namespaceMapping.containsKey(namespaceUri) ) {
			return namespaceMapping.get(namespaceUri);
		}
		return suggestion;
	}

	@Override
	public String[] getPreDeclaredNamespaceUris() {
		return predeclareUriList;
	}

	/**
	 * @return Returns the namespaceMapping.
	 */
	public Map<String, String> getNamespaceMapping() {
		return namespaceMapping;
	}

	/**
	 * @param namespaceMapping The namespaceMapping to set.
	 */
	public void setNamespaceMapping(Map<String, String> namespaceMapping) {
		this.namespaceMapping = namespaceMapping;
	}

	/**
	 * @return Returns the predeclareUriList.
	 */
	public String[] getPredeclareUriList() {
		return predeclareUriList;
	}

	/**
	 * @param predeclareUriList The predeclareUriList to set.
	 */
	public void setPredeclareUriList(String[] predeclareUriList) {
		this.predeclareUriList = predeclareUriList;
	}
	
}
