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
import java.beans.PropertyEditor;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.PropertyEditorRegistrar;
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

	/**
	 * The default value for the <code>javaBeanIgnoreProperties</code> property.
	 */
	public static final String[] DEFAULT_JAVA_BEAN_IGNORE_PROPERTIES = new String[] { "class", };
	/**
	 * The default value for the <code>javaBeanTreatAsStringValues</code>
	 * property.
	 */
	public static final Class<?>[] DEFAULT_JAVA_BEAN_STRING_VALUES = new Class<?>[] { Class.class, };

	private static final Class<?>[] DEFAULT_MODEL_OBJECT_IGNORE_TYPES = new Class<?>[] {};
	private static final Pattern CHARSET_PATTERN = Pattern.compile("charset\\s*=\\s*([^\\s]+)",
			Pattern.CASE_INSENSITIVE);

	private Set<Class<?>> modelObjectIgnoreTypes = new LinkedHashSet<Class<?>>(
			Arrays.asList(DEFAULT_MODEL_OBJECT_IGNORE_TYPES));
	private Set<String> javaBeanIgnoreProperties = new LinkedHashSet<String>(
			Arrays.asList(DEFAULT_JAVA_BEAN_IGNORE_PROPERTIES));
	private Set<Class<?>> javaBeanTreatAsStringValues = new LinkedHashSet<Class<?>>(
			Arrays.asList(DEFAULT_JAVA_BEAN_STRING_VALUES));

	/** The default content type: application/json;charset=UTF-8. */
	public static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";
	
	/** The default character encoding used: UTF-8. */
	public static final String UTF8_CHAR_ENCODING = "UTF-8";
	
	private int indentAmount = 0;
	private boolean includeParentheses = false;
	private PropertyEditorRegistrar propertyEditorRegistrar = null;

	/**
	 * Default constructor.
	 */
	public JSONView() {
		setContentType(JSON_CONTENT_TYPE);
	}

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		PropertyEditorRegistrar registrar = this.propertyEditorRegistrar;
		@SuppressWarnings("unchecked")
		Enumeration<String> attrEnum = request.getAttributeNames();
		while ( attrEnum.hasMoreElements() ) {
			String key = attrEnum.nextElement();
			Object val = request.getAttribute(key);
			if ( val instanceof PropertyEditorRegistrar ) {
				registrar = (PropertyEditorRegistrar) val;
				break;
			}
		}

		response.setCharacterEncoding(UTF8_CHAR_ENCODING);
		response.setContentType(getContentType());
		Writer writer = response.getWriter();
		if ( this.includeParentheses ) {
			writer.write('(');
		}
		JsonGenerator json = new JsonFactory().createJsonGenerator(writer);
		json.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		if ( indentAmount > 0 ) {
			json.useDefaultPrettyPrinter();
		}
		json.writeStartObject();
		for ( String key : model.keySet() ) {
			Object val = model.get(key);
			writeJsonValue(json, key, val, registrar);
		}
		json.writeEndObject();
		json.close();
		if ( this.includeParentheses ) {
			writer.write(')');
		}
	}
	
	private Collection<?> getPrimitiveCollection(Object array) {
		int len = Array.getLength(array);
		List<Object> result = new ArrayList<Object>(len);
		for ( int i = 0; i < len; i++ ) {
			result.add(Array.get(array, i));
		}
		return result;
	}

	private void writeJsonValue(JsonGenerator json, String key, Object val,
			PropertyEditorRegistrar registrar) throws JsonGenerationException, IOException {
		if ( val instanceof Collection<?> || (val != null && val.getClass().isArray()) ) {
			Collection<?> col;
			if ( val instanceof Collection<?> ) {
				col = (Collection<?>) val;
			} else if ( !val.getClass().getComponentType().isPrimitive() ) {
				col = Arrays.asList((Object[]) val);
			} else {
				// damn you, primitives
				col = getPrimitiveCollection(val);
			}
			if ( key != null ) {
				json.writeFieldName(key);
			}
			json.writeStartArray();
			for ( Object colObj : col ) {
				writeJsonValue(json, null, colObj, registrar);
			}

			json.writeEndArray();
		} else if ( val instanceof Map<?, ?> ) {
			if ( key != null ) {
				json.writeFieldName(key);
			}
			json.writeStartObject();
			for ( Map.Entry<?, ?> me : ((Map<?, ?>) val).entrySet() ) {
				Object propName = me.getKey();
				if ( propName == null ) {
					continue;
				}
				writeJsonValue(json, propName.toString(), me.getValue(), registrar);
			}
			json.writeEndObject();
		} else if ( val instanceof Double ) {
			if ( key == null ) {
				json.writeNumber((Double) val);
			} else {
				json.writeNumberField(key, (Double) val);
			}
		} else if ( val instanceof Integer ) {
			if ( key == null ) {
				json.writeNumber((Integer) val);
			} else {
				json.writeNumberField(key, (Integer) val);
			}
		} else if ( val instanceof Short ) {
			if ( key == null ) {
				json.writeNumber(((Short) val).intValue());
			} else {
				json.writeNumberField(key, ((Short) val).intValue());
			}
		} else if ( val instanceof Float ) {
			if ( key == null ) {
				json.writeNumber((Float) val);
			} else {
				json.writeNumberField(key, (Float) val);
			}
		} else if ( val instanceof Long ) {
			if ( key == null ) {
				json.writeNumber((Long) val);
			} else {
				json.writeNumberField(key, (Long) val);
			}
		} else if ( val instanceof Boolean ) {
			if ( key == null ) {
				json.writeBoolean((Boolean) val);
			} else {
				json.writeBooleanField(key, (Boolean) val);
			}
		} else if ( val instanceof String ) {
			if ( key == null ) {
				json.writeString((String) val);
			} else {
				json.writeStringField(key, (String) val);
			}
		} else {
			// create a JSON object from bean properties
			generateJavaBeanObject(json, key, val, registrar);
		}
	}
	
	private void generateJavaBeanObject(JsonGenerator json, String key, Object bean,
			PropertyEditorRegistrar registrar) throws JsonGenerationException, IOException {
		if ( key != null ) {
			json.writeFieldName(key);
		}
		if ( bean == null ) {
			json.writeNull();
			return;
		}
		BeanWrapper wrapper = getPropertyAccessor(bean, registrar);
		PropertyDescriptor[] props = wrapper.getPropertyDescriptors();
		json.writeStartObject();
		for ( PropertyDescriptor prop : props ) {
			String name = prop.getName();
			if ( this.getJavaBeanIgnoreProperties() != null
					&& this.getJavaBeanIgnoreProperties().contains(name) ) {
				continue;
			}
			if ( wrapper.isReadableProperty(name) ) {
				Object propVal = wrapper.getPropertyValue(name);
				if ( propVal != null ) {

					// Spring does not apply PropertyEditors on read methods, so manually handle
					PropertyEditor editor = wrapper.findCustomEditor(null, name);
					if ( editor != null ) {
						editor.setValue(propVal);
						propVal = editor.getAsText();
					}
					if ( propVal instanceof Enum<?> || getJavaBeanTreatAsStringValues() != null
							&& getJavaBeanTreatAsStringValues().contains(propVal.getClass()) ) {
						propVal = propVal.toString();
					}
					writeJsonValue(json, name, propVal, registrar);
				}
			}
		}
		json.writeEndObject();
	}
	
	private BeanWrapper getPropertyAccessor(Object obj, PropertyEditorRegistrar registrar) {
		BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(obj);
		if ( registrar != null ) {
			registrar.registerCustomEditors(bean);
		}
		return bean;
	}

	/**
	 * This method performs the same functions as
	 * {@link AbstractView#render(Map, HttpServletRequest, HttpServletResponse)}
	 * except that it uses a LinkedHashMap to preserve model order rather than a
	 * HashMap.
	 */
	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("Rendering view with name '" + getBeanName() + "' with model " + model
					+ " and static attributes " + getStaticAttributes());
		}

		// Consolidate static and dynamic model attributes.
		Map<String, Object> mergedModel = new LinkedHashMap<String, Object>(getStaticAttributes().size()
				+ (model != null ? model.size() : 0));
		mergedModel.putAll(getStaticAttributes());
		if ( model != null ) {
			mergedModel.putAll(model);
		}

		// Expose RequestContext?
		if ( getRequestContextAttribute() != null ) {
			mergedModel.put(getRequestContextAttribute(),
					createRequestContext(request, response, mergedModel));
		}

		// remove objects that should be ignored
		if ( modelObjectIgnoreTypes != null && modelObjectIgnoreTypes.size() > 0 ) {
			Iterator<Object> objects = mergedModel.values().iterator();
			while ( objects.hasNext() ) {
				Object o = objects.next();
				if ( o == null ) {
					objects.remove();
					continue;
				}
				for ( Class<?> clazz : modelObjectIgnoreTypes ) {
					if ( clazz.isAssignableFrom(o.getClass()) ) {
						if ( logger.isTraceEnabled() ) {
							logger.trace("Ignoring model type [" + o.getClass() + ']');
						}
						objects.remove();
						break;
					}
				}
			}
		}

		prepareResponse(request, response);
		renderMergedOutputModel(mergedModel, request, response);
	}

	/**
	 * Get the configured character encoding to use for the response.
	 * 
	 * <p>
	 * This method will extract the character encoding specified in
	 * {@link #getContentType()}, or default to {@code UTF-8} if none available.
	 * </p>
	 * 
	 * @return character encoding name
	 */
	protected String getResponseCharacterEncoding() {
		String charset = "UTF-8";
		Matcher m = CHARSET_PATTERN.matcher(getContentType());
		if ( m.find() ) {
			charset = m.group(1);
		}
		return charset;
	}

	public Set<Class<?>> getModelObjectIgnoreTypes() {
		return modelObjectIgnoreTypes;
	}

	public void setModelObjectIgnoreTypes(Set<Class<?>> modelObjectIgnoreTypes) {
		this.modelObjectIgnoreTypes = modelObjectIgnoreTypes;
	}

	public Set<String> getJavaBeanIgnoreProperties() {
		return javaBeanIgnoreProperties;
	}

	public void setJavaBeanIgnoreProperties(Set<String> javaBeanIgnoreProperties) {
		this.javaBeanIgnoreProperties = javaBeanIgnoreProperties;
	}

	public Set<Class<?>> getJavaBeanTreatAsStringValues() {
		return javaBeanTreatAsStringValues;
	}

	public void setJavaBeanTreatAsStringValues(Set<Class<?>> javaBeanTreatAsStringValues) {
		this.javaBeanTreatAsStringValues = javaBeanTreatAsStringValues;
	}

	public int getIndentAmount() {
		return indentAmount;
	}
	public void setIndentAmount(int indentAmount) {
		this.indentAmount = indentAmount;
	}
	public boolean isIncludeParentheses() {
		return includeParentheses;
	}
	public void setIncludeParentheses(boolean includeParentheses) {
		this.includeParentheses = includeParentheses;
	}

	public PropertyEditorRegistrar getPropertyEditorRegistrar() {
		return propertyEditorRegistrar;
	}

	public void setPropertyEditorRegistrar(PropertyEditorRegistrar propertyEditorRegistrar) {
		this.propertyEditorRegistrar = propertyEditorRegistrar;
	}
	
}
