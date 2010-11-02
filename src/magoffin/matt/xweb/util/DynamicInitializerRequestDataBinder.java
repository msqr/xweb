/* ===================================================================
 * DynamicInitializerRequestDataBinder.java
 * 
 * Copyright (c) 2004 Matt Magoffin. Created Aug 15, 2004 8:10:46 PM.
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
 * $Id: DynamicInitializerRequestDataBinder.java,v 1.2 2007/07/12 09:09:54 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;

import org.springframework.validation.DataBinder;
import org.springframework.web.bind.ServletRequestDataBinder;

/**
 * Extension of ServletRequestDataBinder to support collection properties
 * and other dynamic properties.
 * 
 * <p>For efficiency, one instance of this binder can be created via the 
 * {@link #DynamicInitializerRequestDataBinder(Map)} method, then use the 
 * {@link #DynamicInitializerRequestDataBinder(Object, String, DataBinder)}
 * constructor to create new instances for individual requests.</p>
 * 
 * <p>This DataBinder inspects each property to see if the property name matches 
 * either an array pattern generated from each configured property mapping, or 
 * if the property name simply starts with the property name. For example, 
 * if a command object has a List property of other objects, a request property 
 * might look like <code>recipe.ingredient[0].ingredientId</code>. Here the 
 * command object is assumed to have a method like <code>List getIngredient()</code>.
 * If this binder has a property mapping <code>recipe.ingredient</code> that maps
 * to a DynamicInitializer, the {@link magoffin.matt.xweb.util.DynamicInitializer#newInstance(Object, String)}
 * method will be invoked, passing in the commnad object and the property name
 * (<code>recipe.ingredient</code>). The DynamicInitializer must return an appropriate 
 * object for that property name, in this case one that supports a <code>setIngredientId(x)</code>
 * method. In this fashion, collection properties can be dynamically constructed
 * at request time, without having to pre-populate them with an arbitrary numer
 * of objects.</p>
 * 
 * <p>In addition, if a request property name does not match the array pattern
 * described above, this class checks if that property name starts with any 
 * property mapping. If it does, and the command object's value for that property 
 * is <em>null</em>, the DynamicInitializer associated with that property mapping
 * will be invoked and the returned object set to that property on the command 
 * object. For example, for a request property like <code>index.displaySection</code>
 * if a mapping for <code>index</code> exists, that DynamicInitializer will be invoked
 * (passing the command object and <code>index.displaySection</code>) and the 
 * returned object will be set on the command object via a <code>setDisplaySection(x)</code>
 * method. In this way nested command objects can be created at request time.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2007/07/12 09:09:54 $
 */
public class DynamicInitializerRequestDataBinder extends ServletRequestDataBinder 
implements ServletRequestDataBinderTemplate {
	
	/**
	 * Comparator that sorts by the number of <code>.</code> characters
	 * in a String.
	 * 
	 * <p>If two strings have the same number of <code>.</code> characters, 
	 * the strings are compared as normal strings for sorting purposes. This
	 * class is intented to sort Java class names by the number of packages 
	 * a class is in, where classes with fewer packages are sorted before
	 * classes with more packages.</p>
	 */
	private static class SortBySteps implements Comparator<String> {

		public int compare(String s1, String s2) {
			int steps1 = getNumSteps(s1);
			int steps2 = getNumSteps(s2);
			return  steps1 < steps2 ? -1 : (steps1 > steps2 ? 1 : s1.compareTo(s2));
		}
		
		private int getNumSteps(String s) {
			int start = s.indexOf('.');
			if ( start < 0 ) return 1;
			int end = s.lastIndexOf('.');
			if ( start == end ) return 2;
			int cnt = 2;
			for ( start = s.indexOf('.',start+1); start < end; cnt++ ) {
				// nothing to do
			}
			return cnt;
		}
	}
	
	private static final Comparator<String> SORT_BY_STEPS = new SortBySteps();
	
	private SortedMap<String, DynamicInitializer> initializerMapping;
	private Map<String, Pattern> patternMap;
	
	/**
	 * Dummy constructor, so that copy constructor can be used later.
	 * @param mapping the pattern mapping
	 */
	public DynamicInitializerRequestDataBinder(Map<String, DynamicInitializer> mapping) {
		this(new Object(),"no-name", mapping);
	}
	
	/**
	 * Copy constructor.
	 * 
	 * @param target the target bean
	 * @param objectName the bean name
	 * @param binder the binder to copy
	 */
	public DynamicInitializerRequestDataBinder(Object target, String objectName, 
			DataBinder binder) {
		super(target,objectName);
		if ( binder instanceof DynamicInitializerRequestDataBinder ) {
			DynamicInitializerRequestDataBinder dBinder = 
				(DynamicInitializerRequestDataBinder)binder;
			this.initializerMapping = dBinder.initializerMapping;
			this.patternMap = dBinder.patternMap;
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param target the command target
	 * @param objectName the command target's bind name
	 * @param m dynamic binding path map
	 */
	public DynamicInitializerRequestDataBinder(Object target, String objectName, 
			Map<String, DynamicInitializer> m) {
		super(target, objectName);
		
		for ( Iterator<String> itr = m.keySet().iterator(); itr.hasNext(); ) {
			String key = itr.next();
			Object o = m.get(key);
			if ( !(o instanceof DynamicInitializer) ) {
				throw new IllegalArgumentException(
						"The DataBinderInitializerRequestDataBinder class only accepts objects that implement "
						+DynamicInitializer.class +"; key '" +key +"' is a " 
						+(o == null ? "(null)" : o.getClass().getName()));
			}
		}

		// we initialize our mapping classes by number of steps (packages)
		// because we want to handle shorter packages first, e.g. handle
		// bean.property before bean.bean.property
		
		SortedMap<String, DynamicInitializer> iMapping 
			= new TreeMap<String, DynamicInitializer>(SORT_BY_STEPS);
		iMapping.putAll(m);
		Map<String, Pattern> pMap = new LinkedHashMap<String, Pattern>(m.size()*2);
		for ( Iterator<String> itr = iMapping.keySet().iterator(); itr.hasNext(); ) {
			String propName = itr.next();
			String regexPat = "^(" +propName +")" +"\\[(\\d+)\\].*";
			Pattern pat = Pattern.compile(regexPat);
			pMap.put(propName,pat);
		}
		
		// make maps unmodifiable
		this.initializerMapping = Collections.unmodifiableSortedMap(iMapping);
		this.patternMap = Collections.unmodifiableMap(pMap);
	}
	
	@Override
	public void bind(ServletRequest request) {
		inspectCollectionProperties(request);
		super.bind(request);
	}
	
	/**
	 * Inspect a request for dynamic binding parameters.
	 * 
	 * <p>This method will inspect all request parameter keys for any that 
	 * match any of the dynamic bind names configured in this instance.</p>
	 * 
	 * @param request the request to inspect
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void inspectCollectionProperties(ServletRequest request) {
		Map<String, String> requestMap = request.getParameterMap();
		// need to first clear out any mapped list objects
		Map<String, Boolean> clearedLists = new HashMap<String, Boolean>();
		for ( Iterator itr = patternMap.keySet().iterator(); itr.hasNext(); ) {
			String propName = (String)itr.next();
			Pattern pat = patternMap.get(propName);
			for ( Iterator paramItr = requestMap.keySet().iterator(); paramItr.hasNext(); ) {
				String param = (String)paramItr.next();
				Matcher matcher = pat.matcher(param);
				if ( matcher.matches() ) {
					String objPropName = matcher.group(1);
					String collectionIdx = matcher.group(2);
					List<Object> l = (List<Object>)
						getPropertyAccessor().getPropertyValue(objPropName);
					
					// clear out list first time around, which lets us eliminate items 
					// from session form
					if ( !clearedLists.containsKey(objPropName) ) {
						l.clear();
						clearedLists.put(objPropName,Boolean.TRUE);
					}
					
					int idx = Integer.parseInt(collectionIdx);
					if ( l.size() <= idx ) {
						DynamicInitializer initializer = initializerMapping.get(propName);
						try {
							for ( int i = l.size(); i <= idx; i++ ) {
								l.add(initializer.newInstance(getTarget(),objPropName));
							}
						} catch ( Exception e ) {
							throw new RuntimeException("Unable to instantiate list object "
									+"for property " +propName +" on bean "
									+getTarget().getClass());
						}
					}
				} else if ( param.startsWith(propName) ) {
					Object o = getPropertyAccessor().getPropertyValue(propName);
					if ( o == null ) {
						DynamicInitializer initializer = initializerMapping.get(propName);
						Object newObj = initializer.newInstance(
								getTarget(),propName);
						getPropertyAccessor().setPropertyValue(propName,newObj);
					}
				}
			}
		}
	}
	
}
