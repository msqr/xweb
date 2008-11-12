/* ===================================================================
 * JSONView.java
 * 
 * Created Jan 3, 2007 12:20:21 PM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: JSONView.java,v 1.4 2007/07/12 09:09:54 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import java.beans.PropertyDescriptor;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.web.servlet.view.AbstractView;

/**
 * View to return JSON encoded data.
 * 
 * <p>The view model is turned into a complete JSON object. The model keys become
 * JSON object keys, and the model values the corresponding JSON object values. 
 * Array and Collection object values will be rendered as JSON array values. 
 * Primiative types will render as JSON primitive values (numbers, strings). 
 * Objects will be treated as JavaBeans and the bean properties will be used 
 * to render nested JSON objects.</p>
 * 
 * <p>All object values are handled in a recursive fashion, so array, collection, 
 * and bean property values will be rendered accordingly.</p>
 * 
 * <p>The entire JSON obejct is constrcuted in memory, so this may not be suitable
 * for large JSON objects.</p>
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl>
 *   <dt>indentAmount</dt>
 *   <dd>The number of spaces to indent (pretty print) the JSON output with.
 *   If set to zero no indentation will be added (this is the default).</dd>
 *   
 *   <dt>javaBeanIgnoreProperties</dt>
 *   <dd>A set of JavaBean properties to ignore for all JavaBeans. This is useful
 *   for omitting things like <code>class</code> which is not usually desired. 
 *   Defaults to {@link #DEFAULT_JAVA_BEAN_IGNORE_PROPERTIES}.</dd>
 *   
 *   <dt>javaBeanTreatAsStringValues</dt>
 *   <dd>A set of JavaBean property object types to treat as Strings for all JavaBeans.
 *   This is useful for omitting object values such as Class objects, which is
 *   not usually desired. Defaults to {@link #DEFAULT_JAVA_BEAN_STRING_VALUES}.</dd>
 *   
 *   <dt>includeParentheses</dt>
 *   <dd>If true, the entire response will be enclosed in parentheses,
 *   required for JSON evaluation support in certain browsers.</dd>
 *   
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.4 $ $Date: 2007/07/12 09:09:54 $
 */
public class JSONView extends AbstractView {
	
	/** The default value for the <code>javaBeanIgnoreProperties</code> property. */
	public static final String[] DEFAULT_JAVA_BEAN_IGNORE_PROPERTIES = new String[] {
		"class",
	};
	
	/** The default value for the <code>javaBeanTreatAsStringValues</code> property. */
	public static final Class<?>[] DEFAULT_JAVA_BEAN_STRING_VALUES = new Class<?>[] {
		Class.class,
	};
	
	private int indentAmount = 0;
	private Set<String> javaBeanIgnoreProperties = new LinkedHashSet<String>(
			Arrays.asList(DEFAULT_JAVA_BEAN_IGNORE_PROPERTIES));
	private Set<Class<?>> javaBeanTreatAsStringValues = new LinkedHashSet<Class<?>>(
			Arrays.asList(DEFAULT_JAVA_BEAN_STRING_VALUES));
	private boolean includeParentheses = true;

	@SuppressWarnings("unchecked")
	@Override
	public void render(Map model, HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		// This is a copy of AbstractView#render except a LinkedHashMap is used to 
		// preserve key ordering.

		if (logger.isDebugEnabled()) {
			logger.debug("Rendering view with name '" + getBeanName() + "' with model " + model 
					+ " and static attributes " + getStaticAttributes());
		}

		// consolidate static and dynamic model attributes
		Map<String, Object> mergedModel = new LinkedHashMap<String, Object>(
				getStaticAttributes().size() + (model != null ? model.size() : 0));
		mergedModel.putAll(getStaticAttributes());
		if (model != null) {
			mergedModel.putAll(model);
		}

		// expose RequestContext?
		if (getRequestContextAttribute() != null) {
			mergedModel.put(getRequestContextAttribute(), 
					createRequestContext(request, mergedModel));
		}

		renderMergedOutputModel(mergedModel, request, response);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void renderMergedOutputModel(Map model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		JSONObject json = new JSONObject();
		for ( String key : ((Map<String, Object>)model).keySet() ) {
			Object val = model.get(key);
			setJsonValue(json, key, val);
		}
		response.setContentType(getContentType());
		Writer writer = response.getWriter();
		if ( this.includeParentheses ) {
			writer.write('(');
		}
		writer.write(indentAmount > 0 
				? json.toString(indentAmount) 
				: json.toString());
		if ( this.includeParentheses ) {
			writer.write(')');
		}
	}
	
	private void setJsonValue(JSONObject json, String key, Object val) throws JSONException {
		if ( val instanceof Collection || val.getClass().isArray() ) {
			Collection<?> col = val instanceof Collection
				? (Collection<?>)val : Arrays.asList((Object[])val);
			JSONArray arrayObj = new JSONArray();
			for ( Object colObj : col ) {
				setJsonValue(arrayObj, colObj);
			}
			json.put(key, arrayObj);
		} else if ( val instanceof Number
				|| val instanceof Boolean || val instanceof String 
				|| val instanceof JSONObject || val instanceof JSONArray ) {
			json.put(key, val);
		} else {
			// create a JSONObject from bean properties
			JSONObject beanObj = generateJavaBeanObject(val);
			json.put(key, beanObj);
		}
	}
	
	private JSONObject generateJavaBeanObject(Object bean) throws JSONException {
		BeanWrapper wrapper = new BeanWrapperImpl(bean);
		PropertyDescriptor[] props = wrapper.getPropertyDescriptors();
		JSONObject beanObj = new JSONObject();
		for ( PropertyDescriptor prop : props ) {
			String name = prop.getName();
			if ( this.javaBeanIgnoreProperties != null 
					&& this.javaBeanIgnoreProperties.contains(name) ) {
				continue;
			}
			if ( wrapper.isReadableProperty(name) ) {
				Object propVal = wrapper.getPropertyValue(name);
				if ( propVal != null ) {
					if ( this.javaBeanTreatAsStringValues != null 
							&& this.javaBeanTreatAsStringValues.contains(propVal.getClass())) {
						propVal = propVal.toString();
					}
					setJsonValue(beanObj, name, propVal);
				}
			}
		}
		return beanObj;
	}
	
	private void setJsonValue(JSONArray json, Object val) throws JSONException {
		if ( val instanceof Collection || val.getClass().isArray() ) {
			Collection<?> col = val instanceof Collection
				? (Collection<?>)val : Arrays.asList((Object[])val);
			JSONArray arrayObj = new JSONArray();
			for ( Object colObj : col ) {
				setJsonValue(arrayObj, colObj);
			}
			json.put(arrayObj);
		} else if ( val instanceof Number
				|| val instanceof Boolean || val instanceof String 
				|| val instanceof JSONObject || val instanceof JSONArray ) {
			json.put(val);
		} else {
			// create a JSONObject from bean properties
			JSONObject beanObj = generateJavaBeanObject(val);
			json.put(beanObj);
		}
	}

	/**
	 * @return the indentAmount
	 */
	public int getIndentAmount() {
		return indentAmount;
	}
	
	/**
	 * @param indentAmount the indentAmount to set
	 */
	public void setIndentAmount(int indentAmount) {
		this.indentAmount = indentAmount;
	}
	
	/**
	 * @return the javaBeanIgnoreProperties
	 */
	public Set<String> getJavaBeanIgnoreProperties() {
		return javaBeanIgnoreProperties;
	}
	
	/**
	 * @param javaBeanIgnoreProperties the javaBeanIgnoreProperties to set
	 */
	public void setJavaBeanIgnoreProperties(Set<String> javaBeanIgnoreProperties) {
		this.javaBeanIgnoreProperties = javaBeanIgnoreProperties;
	}
	
	/**
	 * @return the includeParentheses
	 */
	public boolean isIncludeParentheses() {
		return includeParentheses;
	}
	
	/**
	 * @param includeParentheses the includeParentheses to set
	 */
	public void setIncludeParentheses(boolean includeParentheses) {
		this.includeParentheses = includeParentheses;
	}
	
	/**
	 * @return the javaBeanTreatAsStringValues
	 */
	public Set<Class<?>> getJavaBeanTreatAsStringValues() {
		return javaBeanTreatAsStringValues;
	}
	
	/**
	 * @param javaBeanTreatAsStringValues the javaBeanTreatAsStringValues to set
	 */
	public void setJavaBeanTreatAsStringValues(
			Set<Class<?>> javaBeanTreatAsStringValues) {
		this.javaBeanTreatAsStringValues = javaBeanTreatAsStringValues;
	}
	
}
