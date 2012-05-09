/* ===================================================================
 * XwebJaxbXsltView.java
 * 
 * Created May 9, 2012 9:29:00 AM
 * 
 * Copyright (c) 2012 Matt Magoffin.
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;
import magoffin.matt.xweb.ObjectFactory;
import magoffin.matt.xweb.Xweb;
import magoffin.matt.xweb.XwebAuxillary;
import magoffin.matt.xweb.XwebContext;
import magoffin.matt.xweb.XwebError;
import magoffin.matt.xweb.XwebErrors;
import magoffin.matt.xweb.XwebMessage;
import magoffin.matt.xweb.XwebMessages;
import magoffin.matt.xweb.XwebModel;
import magoffin.matt.xweb.XwebParameter;
import magoffin.matt.xweb.XwebParameters;
import magoffin.matt.xweb.XwebSession;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.io.UrlResource;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.xslt.XsltView;

/**
 * View implementation for JAXB XSLT transformed outupt using the Xweb model
 * view.
 * 
 * <p>
 * The configurable properties of this class are:
 * </p>
 * 
 * <dl>
 * <dt>jaxbContext</dt>
 * <dd>The classpath-style JAXB context to use, for example
 * <code>magoffin.matt.ma2.domain</code>.</dd>
 * 
 * <dt>ignoreMarshallErrors</dt>
 * <dd>Boolean value; if <em>true</em> then ignore marshall errors. If
 * <em>false</em> then fail on marshall errors. Defaults to <em>true</em>.
 * Useful for debugging.</dd>
 * 
 * <dt>debugMessageResource</dt>
 * <dd>Boolean value; if <em>true</em> then if <code>DEBUG</code> level logging
 * is enabled for this class then the entire <code>XwebMessages</code> data
 * object will be output with the <code>Xweb</code> XML object to the log. If
 * <em>false</em> then the Xweb XML output will not include it. Since the
 * <code>XwebMessages</code> object will contain a lot of data, it is useful to
 * not output that while debugging Xweb output.</dd>
 * 
 * <dt>marshallerProperties</dt>
 * <dd>A Map of properties to set on the JAXB Marshaller. This is useful for
 * passing such properties as
 * <code>com.sun.xml.bind.namespacePrefixMapper</code> property to output valid
 * XHTML.</dd>
 * 
 * <dt>messagesSource</dt>
 * <dd>The <code>MessagesSource</code> object which will be turned into a
 * <code>XwebMessages</code> and added to the output Xweb XML object. This makes
 * internationalized messages available to the view XSLT.</dd>
 * 
 * <dt>appSettings</dt>
 * <dd>A Map of application settings to set on the &lt;x-settings&gt; element
 * for each transformation. This allows application properties to be passed to
 * the stylesheets.</dd>
 * 
 * <dt>appSettingsCache</dt>
 * <dd>An optional cache to use for the <code>appSettings</code> content, so
 * that the application does not need to generate this data for each request.</dd>
 * 
 * <dt>enableXmlSourceResponse</dt>
 * <dd>If <em>true</em> then if a URL parameter named "xml" is submitted with a
 * value of "true" then the original XML source for the view will be returned,
 * rather than the transformed result. Defaults to <em>true</em>.</dd>
 * </dl>
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public class XwebJaxbXsltView extends XsltView implements InitializingBean {

	/**
	 * The XWeb JAXB Context.
	 */
	public static final String XWEB_JAXB_CONTEXT = "magoffin.matt.xweb";

	// a cache of JAXBContext instances, which are thread-safe and best shared across all views
	private static final Map<String, JAXBContext> JAXBCONTEXT_CACHE = new HashMap<String, JAXBContext>(
			2);
	private static final DocumentBuilderFactory DOC_BUILDER_FACTORY = DocumentBuilderFactory
			.newInstance();
	static {
		DOC_BUILDER_FACTORY.setNamespaceAware(true);
		DOC_BUILDER_FACTORY.setValidating(true);
	}

	// super XsltView doesn't give us easy access to HttpServletRequest, so use ThreadLocal
	private static final ThreadLocal<HttpServletRequest> REQUEST = new ThreadLocal<HttpServletRequest>();

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Map<String, XwebMessages> msgMap = new HashMap<String, XwebMessages>();
	private final ObjectFactory objectFactory = new ObjectFactory();

	private String jaxbContext = null;
	private boolean useAbsolutePaths = false;
	private boolean ignoreMarshallErrors = true;
	private boolean debugMessageResource = false;
	private Map<String, Object> marshallerProperties = new HashMap<String, Object>();
	private MessagesSource messagesSource = null;
	private XwebParamDao parameterDao = null;
	private XwebHelper webHelper = null;
	private ContentTypeResolver contentTypeResolver = null;
	private boolean includeMessages = true;
	private boolean enableXmlSourceResponse = true;
	private Ehcache appSettingsCache = null;

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		synchronized ( JAXBCONTEXT_CACHE ) {
			if ( !JAXBCONTEXT_CACHE.containsKey(this.jaxbContext) ) {
				String myContext = XWEB_JAXB_CONTEXT + ":" + jaxbContext;
				JAXBContext context = null;
				try {
					context = JAXBContext.newInstance(myContext);
					JAXBCONTEXT_CACHE.put(this.jaxbContext, context);
				} catch ( JAXBException e ) {
					throw new RuntimeException("Unable to create JAXBContext for context '" + myContext
							+ "'", e);
				}
				log.info("Initialized JAXB context '{}", myContext);
			} else if ( log.isDebugEnabled() ) {
				log.debug("Using cached JAXB context [{}] for context '{}'",
						JAXBCONTEXT_CACHE.get(this.jaxbContext), this.jaxbContext);
			}
		}
	}

	@Override
	public boolean checkResource(Locale locale) throws Exception {
		URL xslt = null;
		if ( useAbsolutePaths ) {
			xslt = new URL(getUrl());
		} else {
			xslt = getServletContext().getResource(getUrl());
		}
		try {
			if ( !new UrlResource(xslt).getFile().exists() ) {
				return false;
			}
		} catch ( IOException e ) {
			log.debug("Unable to check existence of XSLT resource [{}]: {}", getUrl(), e.getMessage());
		}
		return true;
	}

	@Override
	protected boolean isContextRequired() {
		return false;
	}

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		REQUEST.set(request);
		super.renderMergedOutputModel(model, request, response);
		REQUEST.remove();
	}

	@Override
	protected Source locateSource(Map<String, Object> model) throws Exception {
		HttpServletRequest request = REQUEST.get();
		String root = XwebConstants.DEFALUT_MODEL_OBJECT;
		for ( Map.Entry<String, ?> me : model.entrySet() ) {
			if ( me.getValue() instanceof JAXBElement<?> ) {
				root = me.getKey();
				break;
			}
		}
		Xweb xweb = buildXweb(model, root, request);
		return new JAXBSource(getMarshaller(), objectFactory.createXData(xweb));
	}

	/**
	 * Get the cached JAXBContext.
	 * 
	 * @return JAXBContext
	 * @throws RuntimeException
	 *         if the JAXBContext for <code>jaxbContext</code> has not been
	 *         initialized already (via the {@link #init()} method)
	 */
	private JAXBContext getContext() {
		JAXBContext context = JAXBCONTEXT_CACHE.get(this.jaxbContext);
		if ( context == null ) {
			throw new RuntimeException("JAXBContext for [" + jaxbContext + "] not defined.");
		}
		return context;
	}

	/**
	 * Get a JAXB Marshaller, configured for our context and set to ignore
	 * marshalling errors if specified.
	 * 
	 * @return a Marshaller
	 * @throws Exception
	 *         if unable to create the marshaller
	 */
	private Marshaller getMarshaller() throws Exception {
		Marshaller marshaller = getContext().createMarshaller();
		if ( this.ignoreMarshallErrors ) {
			marshaller.setEventHandler(IgnoreValidation.IGNORE_VALIDATION);
		}
		if ( marshallerProperties != null ) {
			for ( Map.Entry<String, Object> me : marshallerProperties.entrySet() ) {
				marshaller.setProperty(me.getKey().toString(), me.getValue());
			}
		}
		return marshaller;
	}

	private XwebParameters getAppSettings() {
		if ( appSettingsCache != null ) {
			try {
				Element cachedResult = appSettingsCache.get("app.settings");
				if ( cachedResult != null ) {
					XwebParameters result = (XwebParameters) cachedResult.getValue();
					return result;
				}
			} catch ( CacheException e ) {
				if ( logger.isWarnEnabled() ) {
					log.warn("Error using app settings cache, proceeding without cache", e);
				}
			}
		}

		try {
			XwebParameters result = objectFactory.createXwebParameters();
			List<XwebParameter> settings = parameterDao.getParameters();
			result.getParam().addAll(settings);

			if ( appSettingsCache != null ) {
				Element cachedElement = new Element("app.settings", result);
				appSettingsCache.put(cachedElement);
			}

			return result;
		} catch ( ClassCastException e ) {
			throw new RuntimeException("XwebParameters cannot be cast to Serializable", e);
		}
	}

	private XwebMessages getMessages(Locale locale, MessagesSource msgSrc) {
		String key = locale.toString();
		if ( msgMap.containsKey(key) ) {
			return msgMap.get(key);
		}

		synchronized ( msgMap ) {
			if ( msgMap.containsKey(key) ) {
				return msgMap.get(key);
			}

			XwebMessages xMsgs = objectFactory.createXwebMessages();
			Enumeration<String> enumeration = msgSrc.getKeys(locale);
			while ( enumeration.hasMoreElements() ) {
				String msgKey = enumeration.nextElement();
				XwebMessage xMsg = objectFactory.createXwebMessage();
				xMsgs.getMsg().add(xMsg);
				xMsg.setKey(msgKey);
				Object val = msgSrc.getMessage(msgKey, null, locale);
				if ( val != null ) {
					xMsg.setValue(val.toString());
				}
			}
			msgMap.put(key, xMsgs);
			return xMsgs;
		}
	}

	/**
	 * Build the output Xweb view object.
	 * 
	 * @param model
	 *        the model
	 * @param rootName
	 *        the root model name
	 * @param request
	 *        the current request
	 * @return the Xweb
	 * @throws Exception
	 *         if an error occurs
	 */
	protected Xweb buildXweb(Map<String, ?> model, String rootName, HttpServletRequest request)
			throws Exception {
		// create Xweb data container now
		Xweb xData = objectFactory.createXweb();

		String modelKey = model.containsKey(XwebConstants.DEFALUT_MODEL_OBJECT) ? XwebConstants.DEFALUT_MODEL_OBJECT
				: rootName;

		processModelObject(xData, model, modelKey);

		// add request context data
		processContext(request, xData);

		// insert non-model, non-message DOM objects as necessary
		processNonModelObjects(xData, model, modelKey);

		Locale locale = null;
		try {
			locale = RequestContextUtils.getLocale(request);
		} catch ( IllegalStateException e ) {
			logger.warn("Unable to get Locale from request, using default [" + Locale.getDefault() + "]");
			locale = Locale.getDefault();
		}

		processMessages(request, xData, model, rootName, modelKey, locale);
		processErrors(request, xData, model, rootName, modelKey, locale);

		processSession(request, xData);
		processRequestData(request, xData);

		// add Settings values
		if ( parameterDao != null ) {
			XwebParameters params = getAppSettings();
			xData.getXContext().setSettings(params);
		}

		/*
		 * if ( this.postProcessors != null ) { for ( XDataPostProcessor
		 * processor : this.postProcessors ) { processor.process(xData,
		 * request); } } // postProcessXweb(model, rootName, request, xData);
		 */

		if ( !debugMessageResource ) {
			debugXweb(xData);
		}

		// add add AppContext if available
		if ( webHelper != null ) {
			AppContextSupport appCtxSupport = webHelper.getAppContextSupport(request);
			if ( appCtxSupport != null ) {
				XwebAuxillary xAux = xData.getXAuxillary();
				if ( xAux == null ) {
					xAux = objectFactory.createXwebAuxillary();
					xData.setXAuxillary(xAux);
				}
				xAux.getAny().add(appCtxSupport.getAppContext());
			}
		}

		// set up message resources
		processMessagesSource(xData, locale);

		if ( debugMessageResource ) {
			debugXweb(xData);
		}

		return xData;
	}

	/**
	 * Process the XContext data.
	 * 
	 * <p>
	 * Extending views can add more context to the DOM by overriding this method
	 * and using the Node returned from this implementation.
	 * </p>
	 * 
	 * @param request
	 *        the request
	 * @param xData
	 *        the Xweb object to populate
	 */
	private void processContext(HttpServletRequest request, Xweb xData) {
		XwebContext context = objectFactory.createXwebContext();
		xData.setXContext(context);
		context.setServerName(request.getServerName());
		context.setServerPort(request.getServerPort());
		context.setUserAgent(request.getHeader("user-agent"));
		context.setUserLocale(request.getLocale().toString());
		context.setWebContext(request.getContextPath());
		context.setPath(request.getServletPath());
	}

	/**
	 * Add XwebSession data to Xweb if available.
	 * 
	 * @param request
	 *        the request
	 * @param xData
	 *        the Xweb
	 * @throws JAXBException
	 *         if an error occurs
	 */
	private void processSession(HttpServletRequest request, Xweb xData) {
		if ( request.getSession(false) != null ) {
			XwebSession xSession = objectFactory.createXwebSession();
			xSession.setSessionId(request.getSession().getId());
			xData.setXSession(xSession);
			HttpSession session = request.getSession();
			for ( @SuppressWarnings("rawtypes")
			Enumeration enumeration = session.getAttributeNames(); enumeration.hasMoreElements(); ) {
				String key = (String) enumeration.nextElement();
				if ( key == null )
					continue;
				Object val = session.getAttribute(key);
				if ( val == null )
					continue;
				if ( val.getClass().getName().startsWith(jaxbContext) ) {
					xSession.getAny().add(val);
				} else {
					XwebParameter xParam = objectFactory.createXwebParameter();
					xParam.setKey(key);
					xParam.setValue(val.toString());
					xSession.getAny().add(xParam);
				}
			}
		}
	}

	/**
	 * Add non-model objects to the Xweb.
	 * 
	 * @param xData
	 *        the Xweb
	 * @param model
	 *        the model
	 * @param modelKey
	 *        the model key
	 * @throws JAXBException
	 *         if an error occurs
	 */
	private void processNonModelObjects(Xweb xData, Map<String, ?> model, String modelKey)
			throws JAXBException {
		if ( model.size() > 2 ) {
			XwebAuxillary xAux = objectFactory.createXwebAuxillary();
			xData.setXAuxillary(xAux);
			for ( Map.Entry<String, ?> me : model.entrySet() ) {
				String key = me.getKey();
				// skip any Spring-specific or internal data...
				if ( key != null
						&& !XwebConstants.DEFALUT_REFERENCE_DATA_OBJECT.equals(key)
						&& (key.startsWith("magoffin.matt") || key.startsWith("org.springframework") || key
								.equals(modelKey)) ) {
					continue;
				}
				Object auxObj = me.getValue();
				if ( auxObj == null ) {
					continue;
				}

				if ( auxObj.getClass().getName().startsWith(jaxbContext) ) {
					// if it's a domain object, simply add that so it will
					// render to XML
					xAux.getAny().add(auxObj);
				} else {
					// otherwise, create an XParam object to hold the data
					XwebParameter xParam = objectFactory.createXwebParameter();
					xParam.setKey(key);
					xParam.setValue(auxObj.toString());
					xAux.getAny().add(xParam);
				}
			}
		}
	}

	/**
	 * Process the request and add request data to the Xweb.
	 * 
	 * @param request
	 *        the current request
	 * @param xData
	 *        the current Xweb
	 * @throws JAXBException
	 *         if a JAXB error occurs
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void processRequestData(HttpServletRequest request, Xweb xData) throws JAXBException {
		// set up request (required)
		XwebParameters xRequest = objectFactory.createXwebParameters();
		xData.setXRequest(xRequest);
		Map paramMap = request.getParameterMap();
		for ( Iterator itr = paramMap.keySet().iterator(); itr.hasNext(); ) {
			String key = itr.next().toString();
			String[] paramVals = (String[]) paramMap.get(key);
			for ( int i = 0; i < paramVals.length; i++ ) {
				if ( logger.isDebugEnabled() ) {
					logger.debug("Setting XSL x-request param " + key + ": " + paramVals[i]);
				}
				XwebParameter xParam = objectFactory.createXwebParameter();
				xRequest.getParam().add(xParam);
				xParam.setKey(key);
				xParam.setValue(paramVals[i]);
			}
		}

		// set up request headers (required)
		XwebParameters xRequestHeaders = objectFactory.createXwebParameters();
		xData.setXRequestHeaders(xRequestHeaders);
		Enumeration<String> headerNames = request.getHeaderNames();
		while ( headerNames.hasMoreElements() ) {
			String key = headerNames.nextElement();
			Enumeration<String> headerVals = request.getHeaders(key);
			while ( headerVals.hasMoreElements() ) {
				String headerVal = headerVals.nextElement();
				if ( logger.isDebugEnabled() ) {
					logger.debug("Setting XSL x-request-header param " + key + ": " + headerVal);
				}
				XwebParameter xParam = objectFactory.createXwebParameter();
				xRequestHeaders.getParam().add(xParam);
				xParam.setKey(key);
				xParam.setValue(headerVal);
			}
		}
	}

	/**
	 * Process any global messages.
	 * 
	 * @param request
	 *        the current request
	 * @param xData
	 *        the current Xweb
	 * @param model
	 *        the model Map
	 * @param rootName
	 *        the root name
	 * @param modelKey
	 *        the model object key
	 * @param locale
	 *        the current locale
	 * @throws JAXBException
	 *         if a JAXB error occurs
	 */
	@SuppressWarnings("rawtypes")
	protected void processMessages(HttpServletRequest request, Xweb xData, Map model, String rootName,
			String modelKey, Locale locale) throws JAXBException {

		Object alert = model.get(XwebConstants.ALERT_MESSAGES_OBJECT);
		if ( alert == null && webHelper != null ) {
			alert = webHelper.getSavedMessage(request);
			if ( alert != null ) {
				webHelper.clearSavedMessage(request);
			}
		}

		if ( alert != null ) {
			XwebMessages xMessages = objectFactory.createXwebMessages();
			xData.setXMessages(xMessages);
			MessageSourceAccessor msgs = getMessageSourceAccessor();
			if ( alert instanceof MessageSourceResolvable ) {
				XwebMessage aMessage = objectFactory.createXwebMessage();
				aMessage.setValue(msgs.getMessage((MessageSourceResolvable) alert, locale));
				xMessages.getMsg().add(aMessage);
			} else if ( alert instanceof MessageSourceResolvable[] ) {
				for ( MessageSourceResolvable msr : (MessageSourceResolvable[]) alert ) {
					XwebMessage aMessage = objectFactory.createXwebMessage();
					aMessage.setValue(msgs.getMessage(msr, locale));
					xMessages.getMsg().add(aMessage);
				}
			}
		}
	}

	/**
	 * Set up the messages source messages as an XwebMessages instance.
	 * 
	 * @param xData
	 *        the Xweb to add the messages to
	 * @param locale
	 *        the current locale
	 * @throws JAXBException
	 *         if an error occurs
	 */
	private void processMessagesSource(Xweb xData, Locale locale) {
		// set up message resources
		if ( messagesSource != null && includeMessages ) {
			XwebMessages xMsgs = getMessages(locale, messagesSource);
			xData.setXMsg(xMsgs);
		}
	}

	private void handleMessage(XwebErrors xErrors, Locale locale, MessageSourceAccessor msgs,
			MessageSourceResolvable obj) {
		XwebError xError = objectFactory.createXwebError();
		if ( logger.isTraceEnabled() ) {
			logger.trace("Got error: " + obj);
		}
		String msg = null;
		if ( obj instanceof FieldError ) {
			// add field name as attribute
			FieldError fe = (FieldError) obj;
			String field = fe.getField();
			xError.setField(field);
			String code = fe.getCode();
			String defaultMsg = obj.getDefaultMessage();
			Object[] args = null;
			if ( code.equals("error.required") ) {
				// look for display name
				String name = msgs.getMessage(defaultMsg, locale);
				args = new Object[] { name };
			} else if ( code.equals("typeMismatch") ) {
				// ignore?
				code = null;
			} else {
				args = fe.getArguments();
			}
			if ( code != null ) {
				msg = msgs.getMessage(code, args, locale);
			}
		} else {
			msg = msgs.getMessage(obj, locale);
		}

		if ( msg != null ) {
			// set error message as text
			xError.setValue(msg);
			xErrors.getError().add(xError);
		}
	}

	/**
	 * Process any errors from this request and add to Xweb.
	 * 
	 * @param request
	 *        the current request
	 * @param xData
	 *        the current Xweb
	 * @param model
	 *        the model Map
	 * @param rootName
	 *        the root name
	 * @param modelKey
	 *        the model object key
	 * @param locale
	 *        the current locale
	 * @throws JAXBException
	 *         if a JAXB error occurs
	 */
	private void processErrors(HttpServletRequest request, Xweb xData, Map<String, ?> model,
			String rootName, String modelKey, Locale locale) throws JAXBException {
		// insert errors, if any
		RequestContext ctx = (RequestContext) model.get(XwebConstants.REQUEST_CONTEXT_OBJECT);
		if ( ctx == null ) {
			if ( logger.isDebugEnabled() ) {
				logger.debug("RequestContext is null, unable to add errors to model");
			}
			return;
		}
		Errors errors = ctx.getErrors(modelKey);
		if ( webHelper.getSavedMessage(request) != null || (errors != null && errors.hasErrors())
				|| ctx.getErrors(rootName) != null ) {
			MessageSourceAccessor msgs = getMessageSourceAccessor();
			XwebErrors xErrors = objectFactory.createXwebErrors();
			xData.setXErrors(xErrors);
			if ( errors != null ) {
				List<ObjectError> l = errors.getAllErrors();
				for ( Iterator<ObjectError> itr = l.iterator(); itr.hasNext(); ) {
					ObjectError error = itr.next();
					handleMessage(xErrors, locale, msgs, error);
				}
			} else if ( ctx.getErrors(rootName) != null ) {
				// handle errors associated with root name command object
				for ( Iterator<ObjectError> itr = ctx.getErrors(rootName).getAllErrors().iterator(); itr
						.hasNext(); ) {
					ObjectError error = itr.next();
					handleMessage(xErrors, locale, msgs, error);
				}
			}

			if ( webHelper.getSavedMessage(request) != null ) {
				handleMessage(xErrors, locale, msgs, webHelper.getSavedMessage(request));
				webHelper.clearSavedMessage(request);
			}

			if ( model.get(XwebConstants.ALERT_MESSAGES_OBJECT) instanceof MessageSourceResolvable ) {
				handleMessage(xErrors, locale, msgs,
						(MessageSourceResolvable) model.get(XwebConstants.ALERT_MESSAGES_OBJECT));
			}
		}
	}

	/**
	 * Process the result model object.
	 * 
	 * @param xData
	 *        the current Xweb
	 * @param model
	 *        the model Map
	 * @param modelKey
	 *        the model object key
	 * @throws JAXBException
	 *         if a JAXB error occurs
	 */
	private void processModelObject(Xweb xData, Map<String, ?> model, String modelKey) {
		Object o = model.get(modelKey);
		if ( o != null ) {
			if ( o instanceof JAXBElement<?> ) {
				XwebModel webModel = this.objectFactory.createXwebModel();
				webModel.setAny(o);
				xData.setXModel(webModel);
			} else if ( logger.isDebugEnabled() ) {
				logger.debug("Model object class '" + o.getClass().getName() + "' not in JAXB context '"
						+ jaxbContext + "' so ignored");
			}
		} else if ( logger.isDebugEnabled() ) {
			logger.debug("No XML model object found at '" + modelKey + "'");
		}
		if ( xData.getXModel() == null ) {
			XwebParameter noModelParam = objectFactory.createXwebParameter();
			noModelParam.setKey("no.model");
			noModelParam.setValue("no model");
			XwebModel webModel = this.objectFactory.createXwebModel();
			webModel.setAny(noModelParam);
			xData.setXModel(webModel);
		}
	}

	/**
	 * Log a debug log statement of the serialized version of an Xweb instance
	 * to the class logger.
	 * 
	 * @param xData
	 *        the Xweb to serialize to the logger
	 */
	private void debugXweb(Xweb xData) {
		if ( logger.isDebugEnabled() ) {
			try {
				debugSource(new JAXBSource(getMarshaller(), objectFactory.createXData(xData)),
						"---- START DOM -----\n", "----- END DOM ------\n", logger);
			} catch ( Exception e ) {
				logger.warn("Unable to debug Xweb", e);
			}
		}
	}

	/**
	 * Debug an XML source to a Logger object.
	 * 
	 * @param source
	 *        the XML Node to debug
	 * @param header
	 *        header to prepend to debug output
	 * @param footer
	 *        footer to append to debug output
	 * @param debugLog
	 *        the Log to write debug output to
	 */
	private void debugSource(Source source, String header, String footer, Log debugLog) {
		StringWriter writer = new StringWriter();
		writer.append(header);
		try {
			transformXml(source, new StreamResult(writer), getTransformerFactory().newTransformer(),
					null);
		} catch ( TransformerConfigurationException e ) {
			e.printStackTrace(new PrintWriter(writer));
		}
		writer.append(footer);
		debugLog.debug(writer.toString());
	}

	/**
	 * Internal method for transforming so that indentation is set consistently.
	 * 
	 * @param source
	 *        the XML source
	 * @param result
	 *        the XML result
	 * @param transformer
	 *        the XSLT transformer to use
	 */
	private void transformXml(Source source, Result result, Transformer transformer, String encoding) {
		try {
			if ( encoding != null ) {
				transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
			}
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
			transformer.transform(source, result);
		} catch ( Exception e ) {
			throw new RuntimeException("Unable to transform Document to XML String", e);
		}
	}

	public String getJaxbContext() {
		return jaxbContext;
	}

	public void setJaxbContext(String jaxbContext) {
		this.jaxbContext = jaxbContext;
	}

	public boolean isDebugMessageResource() {
		return debugMessageResource;
	}

	public void setDebugMessageResource(boolean debugMessageResource) {
		this.debugMessageResource = debugMessageResource;
	}

	public Map<String, Object> getMarshallerProperties() {
		return marshallerProperties;
	}

	public void setMarshallerProperties(Map<String, Object> marshallerProperties) {
		this.marshallerProperties = marshallerProperties;
	}

	public MessagesSource getMessagesSource() {
		return messagesSource;
	}

	public void setMessagesSource(MessagesSource messagesSource) {
		this.messagesSource = messagesSource;
	}

	public Map<String, XwebMessages> getMsgMap() {
		return msgMap;
	}

	public boolean isIgnoreMarshallErrors() {
		return ignoreMarshallErrors;
	}

	public void setIgnoreMarshallErrors(boolean ignoreMarshallErrors) {
		this.ignoreMarshallErrors = ignoreMarshallErrors;
	}

	public XwebParamDao getParameterDao() {
		return parameterDao;
	}

	public void setParameterDao(XwebParamDao parameterDao) {
		this.parameterDao = parameterDao;
	}

	public XwebHelper getWebHelper() {
		return webHelper;
	}

	public void setWebHelper(XwebHelper webHelper) {
		this.webHelper = webHelper;
	}

	public ContentTypeResolver getContentTypeResolver() {
		return contentTypeResolver;
	}

	public void setContentTypeResolver(ContentTypeResolver contentTypeResolver) {
		this.contentTypeResolver = contentTypeResolver;
	}

	public boolean isIncludeMessages() {
		return includeMessages;
	}

	public void setIncludeMessages(boolean includeMessages) {
		this.includeMessages = includeMessages;
	}

	public boolean isEnableXmlSourceResponse() {
		return enableXmlSourceResponse;
	}

	public void setEnableXmlSourceResponse(boolean enableXmlSourceResponse) {
		this.enableXmlSourceResponse = enableXmlSourceResponse;
	}

	public ObjectFactory getObjectFactory() {
		return objectFactory;
	}

	public Ehcache getAppSettingsCache() {
		return appSettingsCache;
	}

	public void setAppSettingsCache(Ehcache appSettingsCache) {
		this.appSettingsCache = appSettingsCache;
	}

	public boolean isUseAbsolutePaths() {
		return useAbsolutePaths;
	}

	public void setUseAbsolutePaths(boolean useAbsolutePaths) {
		this.useAbsolutePaths = useAbsolutePaths;
	}

}
