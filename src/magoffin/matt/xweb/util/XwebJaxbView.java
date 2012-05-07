/* ===================================================================
 * XwebJaxbView.java
 * 
 * Created Feb 18, 2005 10:37:37 PM
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
 * $Id: XwebJaxbView.java,v 1.8 2007/10/02 09:03:52 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import java.io.BufferedOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
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
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.xslt.AbstractXsltView;
import org.w3c.dom.Document;

/**
 * View implementation for JAXB XSLT transformed outupt using the Xweb model
 * view.
 * 
 * <p>
 * Note, much code borrowed from Spring's AbstractXsltView class, but could not
 * override necessary methods as they are declared final.
 * </p>
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
 * <dt>defaultIgnoreMarshallErorrs</dt>
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
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.8 $ $Date: 2007/10/02 09:03:52 $
 */
@SuppressWarnings("deprecation")
public class XwebJaxbView extends AbstractXsltView implements InitializingBean {

	/**
	 * The XSL parameter for the HTTP user agent, set automatically:
	 * <code>user-agent</code>.
	 */
	public final static String XSL_PARAM_USER_AGENT = "user-agent";

	/**
	 * The XSL parameter for the HTTP web context path, set automatically:
	 * <code>web-context</code>.
	 */
	public final static String XSL_PARAM_WEB_CONTEXT = "web-context";

	/**
	 * The XSL parameter for the HTTP server name, set automatically:
	 * <code>server-name</code>
	 */
	public final static String XSL_PARAM_SERVER_NAME = "server-name";

	/**
	 * The XSL parameter for the HTTP server port, set automatically:
	 * <code>server-port</code>
	 */
	public final static String XSL_PARAM_SERVER_PORT = "server-port";

	/**
	 * The XSL parameter for the user's locale, set automatically:
	 * <code>user-locale</code>
	 */
	public final static String XSL_PARAM_USER_LOCALE = "user-locale";

	/**
	 * The XSL parameter for the user's locale, set automatically:
	 * <code>user-locale</code>
	 */
	public final static String XSL_PARAM_REQUEST_PATH = "path";

	/**
	 * The XWeb JAXB Context.
	 */
	public static final String XWEB_JAXB_CONTEXT = "magoffin.matt.xweb";

	/* Injected properties. */

	private String jaxbContext = null;
	private boolean defaultIgnoreMarshallErrors = true;
	private boolean debugMessageResource = false;
	private Map<String, Object> marshallerProperties = new HashMap<String, Object>();
	private MessagesSource messagesSource = null;
	private Cache appSettingsCache = null;
	private XwebParamDao parameterDao = null;
	private XwebHelper webHelper = null;
	private ContentTypeResolver contentTypeResolver = null;
	private boolean includeMessages = true;
	private boolean enableXmlSourceResponse = true;

	/** The JAXP TransformerFactory to use for getting transformers (used for debugging DOM). */
	private TransformerFactory transformerFactory = null;

	/* Internal properties. */

	private static final DocumentBuilderFactory DOC_BUILDER_FACTORY = 
			DocumentBuilderFactory.newInstance();
	private static final Map<String, JAXBContext> JAXBCONTEXT_CACHE 
		= new HashMap<String, JAXBContext>();

	static {
		DOC_BUILDER_FACTORY.setNamespaceAware(true);
		DOC_BUILDER_FACTORY.setValidating(true);
	}

	private final Map<String, XwebMessages> msgMap = new HashMap<String, XwebMessages>();
	private final ObjectFactory objectFactory = new ObjectFactory();
	private List<XDataPostProcessor> postProcessors = null;

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(webHelper, "The webHelper property is required.");
		synchronized ( JAXBCONTEXT_CACHE ) {
			if (!JAXBCONTEXT_CACHE.containsKey(this.jaxbContext)) {
				String myContext = XWEB_JAXB_CONTEXT + ":" + jaxbContext;
				JAXBContext context = null;
				try {
					context = JAXBContext.newInstance(myContext);
					JAXBCONTEXT_CACHE.put(this.jaxbContext, context);
				} catch (JAXBException e) {
					throw new RuntimeException(
							"Unable to create JAXBContext for context '"
									+ myContext + "'", e);
				}
				if (logger.isInfoEnabled()) {
					logger.info("Initialized JAXB context '" + myContext + "' ["
							+ context + "]");
				}
			} else if (logger.isDebugEnabled()) {
				logger.debug("Using cached JAXB context ["
						+ JAXBCONTEXT_CACHE.get(this.jaxbContext) + "] for context '"
						+ this.jaxbContext + "'");
			}
		}
	}

	@Override
	protected boolean isContextRequired() {
		return false;
	}

	@Override
	public void setStylesheetLocation(Resource stylesheetLocation) {
		super.setStylesheetLocation(stylesheetLocation);

		if (getApplicationContext() == null)
			return;

		// look for XDataPostProcessor
		Map<String, XDataPostProcessor> postProcessorsMap = BeanFactoryUtils
				.beansOfTypeIncludingAncestors(getApplicationContext(), XDataPostProcessor.class, false,
						false);
		List<XDataPostProcessor> postProcessorList = new LinkedList<XDataPostProcessor>();
		for ( XDataPostProcessor processor : postProcessorsMap.values() ) {
			if (processor.supportsView(getBeanName())) {
				postProcessorList.add(processor);
			}
		}
		if (postProcessorList.size() > 0) {
			if (logger.isInfoEnabled()) {
				logger.info("Found " + postProcessorList.size()
						+ " post processors: " + postProcessorList);
			}
			this.postProcessors = postProcessorList;
		}
	}

	/**
	 * Get the cached JAXBContext.
	 * 
	 * @return JAXBContext
	 * @throws RuntimeException
	 *             if the JAXBContext for <code>jaxbContext</code> has not
	 *             been initialized already (via the {@link #init()} method)
	 */
	private JAXBContext getContext() {
		JAXBContext context = JAXBCONTEXT_CACHE.get(this.jaxbContext);
		if (context == null) {
			throw new RuntimeException("JAXBContext for [" + jaxbContext
					+ "] not defined.");
		}
		return context;
	}

	/**
	 * Get a JAXB Marshaller, configured for our context and set to ignore
	 * marshalling errors if specified.
	 * 
	 * @return a Marshaller
	 * @throws Exception
	 *             if unable to create the marshaller
	 */
	protected Marshaller getMarshaller() throws Exception {
		Marshaller marshaller = getContext().createMarshaller();
		boolean ignoreMarshallerrors = defaultIgnoreMarshallErrors;
		if (ignoreMarshallerrors) {
			marshaller.setEventHandler(IgnoreValidation.IGNORE_VALIDATION);
		}
		if (marshallerProperties != null) {
			for (Iterator<Map.Entry<String, Object>> itr = marshallerProperties.entrySet().iterator(); itr
					.hasNext();) {
				Map.Entry<String, Object> me = itr.next();
				marshaller.setProperty(me.getKey().toString(), me.getValue());
			}
		}
		return marshaller;
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
	protected void processModelObject(Xweb xData, Map<String, ?> model, String modelKey)
			throws JAXBException {
		Object o = model.get(modelKey);
		if (o != null) {
			if (o.getClass().getName().startsWith(jaxbContext)) {
				XwebModel webModel = this.objectFactory.createXwebModel();
				webModel.setAny(o);
				xData.setXModel(webModel);
			} else if (logger.isDebugEnabled()) {
				logger.debug("Model object class '" + o.getClass().getName()
						+ "' not in JAXB context '" + jaxbContext
						+ "' so ignored");
			}
		} else if (logger.isDebugEnabled()) {
			logger.debug("No XML model object found at '" + modelKey + "'");
		}
		if (xData.getXModel() == null) {
			XwebParameter noModelParam = objectFactory.createXwebParameter();
			noModelParam.setKey("no.model");
			noModelParam.setValue("no model");
			XwebModel webModel = this.objectFactory.createXwebModel();
			webModel.setAny(noModelParam);
			xData.setXModel(webModel);
		}
	}

	/**
	 * Get a new Document instance.
	 * 
	 * @return Document instance
	 * @throws ParserConfigurationException  if unable to get Document
	 */
	protected final Document getNewDocument()
			throws ParserConfigurationException {
		return DOC_BUILDER_FACTORY.newDocumentBuilder().newDocument();
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
	@SuppressWarnings( { "unchecked", "rawtypes" })
	protected void processRequestData(HttpServletRequest request, Xweb xData)
			throws JAXBException {
		// set up request (required)
		XwebParameters xRequest = objectFactory.createXwebParameters();
		xData.setXRequest(xRequest);
		Map paramMap = request.getParameterMap();
		for (Iterator itr = paramMap.keySet().iterator(); itr.hasNext();) {
			String key = itr.next().toString();
			String[] paramVals = (String[]) paramMap.get(key);
			for (int i = 0; i < paramVals.length; i++) {
				if (logger.isDebugEnabled()) {
					logger.debug("Setting XSL x-request param " + key + ": "
							+ paramVals[i]);
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
				if (logger.isDebugEnabled()) {
					logger.debug("Setting XSL x-request-header param " + key + ": "
							+ headerVal);
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
	protected void processMessages(HttpServletRequest request, Xweb xData, Map model,
			String rootName, String modelKey, Locale locale) throws JAXBException {
		
		Object alert = model.get(XwebConstants.ALERT_MESSAGES_OBJECT);
		if ( alert == null ) {
			alert = webHelper.getSavedMessage(request);
			if ( alert != null ) {
				webHelper.clearSavedMessage(request);
			}
		}

		if (alert != null) {
			XwebMessages xMessages = objectFactory.createXwebMessages();
			xData.setXMessages(xMessages);
			MessageSourceAccessor msgs = getMessageSourceAccessor();
			if (alert instanceof MessageSourceResolvable) {
				XwebMessage aMessage = objectFactory.createXwebMessage();
				aMessage.setValue(msgs.getMessage((MessageSourceResolvable)alert,
						locale));
				xMessages.getMsg().add(aMessage);
			} else if (alert instanceof MessageSourceResolvable[]) {
				for (MessageSourceResolvable msr : (MessageSourceResolvable[])alert) {
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
	protected void processMessagesSource(Xweb xData, Locale locale)
	throws JAXBException {
		// set up message resources
		if (messagesSource != null && includeMessages) {
			XwebMessages xMsgs = getMessages(locale, messagesSource);
			xData.setXMsg(xMsgs);
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
	@SuppressWarnings("rawtypes")
	protected void processErrors(HttpServletRequest request, Xweb xData,
			Map model, String rootName, String modelKey, Locale locale)
			throws JAXBException {
		// insert errors, if any
		RequestContext ctx = (RequestContext) model
				.get(XwebConstants.REQUEST_CONTEXT_OBJECT);
		if (ctx == null) {
			if ( logger.isDebugEnabled() ) {
				logger.debug("RequestContext is null, unable to add errors to model");
			}
			return;
		}
		Errors errors = ctx.getErrors(modelKey);
		if (webHelper.getSavedMessage(request) != null
				|| (errors != null && errors.hasErrors())
				|| ctx.getErrors(rootName) != null) {
			MessageSourceAccessor msgs = getMessageSourceAccessor();
			XwebErrors xErrors = objectFactory.createXwebErrors();
			xData.setXErrors(xErrors);
			if (errors != null) {
				List<ObjectError> l = errors.getAllErrors();
				for (Iterator<ObjectError> itr = l.iterator(); itr.hasNext();) {
					ObjectError error = itr.next();
					handleMessage(xErrors, locale, msgs, error);
				}
			} else if (ctx.getErrors(rootName) != null) {
				// handle errors associated with root name command object
				for (Iterator<ObjectError> itr = ctx.getErrors(rootName).getAllErrors()
						.iterator(); itr.hasNext();) {
					ObjectError error = itr.next();
					handleMessage(xErrors, locale, msgs, error);
				}
			}

			if (webHelper.getSavedMessage(request) != null) {
				handleMessage(xErrors, locale, msgs, webHelper
						.getSavedMessage(request));
				webHelper.clearSavedMessage(request);
			}

			if (model.get(XwebConstants.ALERT_MESSAGES_OBJECT) instanceof MessageSourceResolvable) {
				handleMessage(xErrors, locale, msgs,
						(MessageSourceResolvable) model
								.get(XwebConstants.ALERT_MESSAGES_OBJECT));
			}
		}
	}

	private void handleMessage(XwebErrors xErrors, Locale locale, MessageSourceAccessor msgs,
			MessageSourceResolvable obj) {
		XwebError xError = objectFactory.createXwebError();
		if (logger.isTraceEnabled()) {
			logger.trace("Got error: " + obj);
		}
		String msg = null;
		if (obj instanceof FieldError) {
			// add field name as attribute
			FieldError fe = (FieldError) obj;
			String field = fe.getField();
			xError.setField(field);
			String code = fe.getCode();
			String defaultMsg = obj.getDefaultMessage();
			Object[] args = null;
			if (code.equals("error.required")) {
				// look for display name
				String name = msgs.getMessage(defaultMsg, locale);
				args = new Object[] { name };
			} else if (code.equals("typeMismatch")) {
				// ignore?
				code = null;
			} else {
				args = fe.getArguments();
			}
			if (code != null) {
				msg = msgs.getMessage(code, args, locale);
			}
		} else {
			msg = msgs.getMessage(obj, locale);
		}

		if (msg != null) {
			// set error message as text
			xError.setValue(msg);
			xErrors.getError().add(xError);
		}
	}

	/**
	 * Setup the XContext data.
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
	 * @throws JAXBException
	 *         if a JAXB error occurs
	 */
	protected void setupContext(HttpServletRequest request, Xweb xData)
			throws JAXBException {
		XwebContext context = objectFactory.createXwebContext();
		xData.setXContext(context);
		context.setServerName(request.getServerName());
		context.setServerPort(request.getServerPort());
		context.setUserAgent(request.getHeader("user-agent"));
		context.setUserLocale(request.getLocale().toString());
		context.setWebContext(request.getContextPath());
		context.setPath(request.getServletPath());
	}

	private XwebMessages getMessages(Locale locale, MessagesSource msgSrc) {
		String key = locale.toString();
		if (msgMap.containsKey(key)) {
			return msgMap.get(key);
		}

		synchronized (msgMap) {
			if (msgMap.containsKey(key)) {
				return msgMap.get(key);
			}

			XwebMessages xMsgs = objectFactory.createXwebMessages();
			/*
			 * if ( useLang ) { Attr lang =
			 * dom.createAttributeNS("http://www.w3.org/XML/1998/namespace","xml:lang");
			 * lang.setValue(locale.getLanguage());
			 * msgElem.setAttributeNodeNS(lang); }
			 */

			Enumeration<String> enumeration = msgSrc.getKeys(locale);
			while (enumeration.hasMoreElements()) {
				String msgKey = enumeration.nextElement();
				XwebMessage xMsg = objectFactory.createXwebMessage();
				xMsgs.getMsg().add(xMsg);
				xMsg.setKey(msgKey);
				Object val = msgSrc.getMessage(msgKey, null, locale);
				if (val != null) {
					xMsg.setValue(val.toString());
				}
			}
			msgMap.put(key, xMsgs);
			return xMsgs;
		}
	}

	private XwebParameters getAppSettings() {
		if (appSettingsCache != null) {
			try {
				Element cachedResult = appSettingsCache.get("app.settings");
				if (cachedResult != null) {
					XwebParameters result = (XwebParameters) cachedResult
							.getValue();
					return result;
				}
			} catch (CacheException e) {
				if (logger.isWarnEnabled()) {
					logger
							.warn(
									"Error using app settings cache, proceeding without cache",
									e);
				}
			}
		}

		try {
			XwebParameters result = objectFactory.createXwebParameters();
			List<XwebParameter> settings = parameterDao.getParameters();
			result.getParam().addAll(settings);

			if (appSettingsCache != null) {
				Element cachedElement = new Element("app.settings",
						result);
				appSettingsCache.put(cachedElement);
			}

			return result;
		} catch (ClassCastException e) {
			throw new RuntimeException(
					"XwebParameters cannot be cast to Serializable", e);
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
	@SuppressWarnings( { "unchecked", "rawtypes" })
	protected Xweb buildXweb(Map model, String rootName,
			HttpServletRequest request) throws Exception {
		// create Xweb data container now
		Xweb xData = objectFactory.createXweb();

		String modelKey = model.containsKey(XwebConstants.DEFALUT_MODEL_OBJECT) ? XwebConstants.DEFALUT_MODEL_OBJECT
				: rootName;

		processModelObject(xData, model, modelKey);

		// add request context data
		setupContext(request, xData);

		// insert non-model, non-message DOM objects as necessary
		processNonModelObjects(xData, model, modelKey);

		Locale locale = null;
		try {
			locale = RequestContextUtils.getLocale(request);
		} catch (IllegalStateException e) {
			logger.warn("Unable to get Locale from request, using default ["
					+ Locale.getDefault() + "]");
			locale = Locale.getDefault();
		}

		processMessages(request, xData, model, rootName, modelKey, locale);
		processErrors(request, xData, model, rootName, modelKey, locale);
		
		processSession(request, xData);
		processRequestData(request, xData);

		// add Settings values
		if (parameterDao != null) {
			XwebParameters params = getAppSettings();
			xData.getXContext().setSettings(params);
		}

		if (this.postProcessors != null) {
			for ( XDataPostProcessor processor : this.postProcessors ) {
				processor.process(xData, request);
			}
		}
		// postProcessXweb(model, rootName, request, xData);

		if (!debugMessageResource) {
			debugXweb(xData);
		}

		// add add AppContext if available
		AppContextSupport appCtxSupport = webHelper
				.getAppContextSupport(request);
		if (appCtxSupport != null) {
			XwebAuxillary xAux = xData.getXAuxillary();
			if (xAux == null) {
				xAux = objectFactory.createXwebAuxillary();
				xData.setXAuxillary(xAux);
			}
			xAux.getAny().add(appCtxSupport.getAppContext());
		}

		// set up message resources
		processMessagesSource(xData, locale);

		if (debugMessageResource) {
			debugXweb(xData);
		}

		return xData;
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
	@SuppressWarnings("rawtypes")
	protected void processSession(HttpServletRequest request, Xweb xData) throws JAXBException {
		if (request.getSession(false) != null) {
			XwebSession xSession = objectFactory.createXwebSession();
			xSession.setSessionId(request.getSession().getId());
			xData.setXSession(xSession);
			HttpSession session = request.getSession();
			for (Enumeration enumeration = session.getAttributeNames(); enumeration
					.hasMoreElements();) {
				String key = (String) enumeration.nextElement();
				if (key == null)
					continue;
				Object val = session.getAttribute(key);
				if (val == null)
					continue;
				if (val.getClass().getName().startsWith(jaxbContext)) {
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
	@SuppressWarnings("rawtypes")
	protected void processNonModelObjects(Xweb xData, Map model, String modelKey)
	throws JAXBException {
		if (model.size() > 2) {
			XwebAuxillary xAux = objectFactory.createXwebAuxillary();
			xData.setXAuxillary(xAux);
			for (Iterator itr = model.keySet().iterator(); itr.hasNext();) {
				String key = (String) itr.next();
				// skip any Spring-specific or ma2-internal data...
				if (key != null
						&& !XwebConstants.DEFALUT_REFERENCE_DATA_OBJECT.equals(key)
						&& (key.startsWith("magoffin.matt.ma2")
								|| key.startsWith("org.springframework") 
								|| key.equals(modelKey))) {
					continue;
				}
				Object auxObj = model.get(key);
				if (auxObj == null) {
					continue;
				}

				if (auxObj.getClass().getName().startsWith(jaxbContext)) {
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
	 * Log a debug log statement of the serialized version of an Xweb instance
	 * to the class logger.
	 * 
	 * @param xData
	 *        the Xweb to serialize to the logger
	 */
	protected void debugXweb(Xweb xData) {
		if (logger.isDebugEnabled()) {
			try {
				debugSource(new JAXBSource(getMarshaller(),xData), 
						"---- START DOM -----\n",
						"----- END DOM ------\n", logger);
			} catch ( Exception e ) {
				logger.warn("Unable to debug Xweb", e);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Source createXsltSource(Map model, String root,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Xweb xData = buildXweb(model, root, request);
		return new JAXBSource(getMarshaller(), xData);
	}

	/**
	 * Debug an XML source to a Logger object.
	 * 
	 * @param source the XML Node to debug
	 * @param header header to prepend to debug output
	 * @param footer footer to append to debug output
	 * @param log the Log to write debug output to
	 */
	protected void debugSource(Source source, String header, String footer, Log log) {
		StringWriter writer = new StringWriter();
		writer.append(header);
		try {
			transformXml(source, new StreamResult(writer),
					transformerFactory.newTransformer(), null);
		} catch ( TransformerConfigurationException e ) {
			e.printStackTrace(new PrintWriter(writer));
		}
		writer.append(footer);
		log.debug(writer.toString());
	}

	/**
	 * Internal method for transforming so that indentation is set consistently.
	 * @param source the XML source
	 * @param result the XML result
	 * @param transformer the XSLT transformer to use
	 */
	private void transformXml(Source source, Result result, 
			Transformer transformer, String encoding) {
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void doTransform(Map model, Source source, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// check for ;xml=true flag
		if ( this.enableXmlSourceResponse && "true".equals(request.getParameter("xml")) ) {
			response.setContentType("text/xml");
			transformXml(source, new StreamResult(new BufferedOutputStream(
					response.getOutputStream())), 
					transformerFactory.newTransformer(), 
					response.getCharacterEncoding());
			return;
		}
		
		// allow dynamic content-type resolution here
		if ( this.contentTypeResolver != null ) {
			response.setContentType(this.contentTypeResolver.resolveContentType(request, model));
		}
		super.doTransform(model, source, request, response);
	}

	@Override
	protected TransformerFactory newTransformerFactory(@SuppressWarnings("rawtypes") Class clazz) {
		if ( this.transformerFactory != null ) {
			return this.transformerFactory;
		}
		return super.newTransformerFactory(clazz);
	}

	/**
	 * @return Returns the appSettingsCache.
	 */
	public Cache getAppSettingsCache() {
		return appSettingsCache;
	}

	/**
	 * @param appSettingsCache
	 *            The appSettingsCache to set.
	 */
	public void setAppSettingsCache(Cache appSettingsCache) {
		this.appSettingsCache = appSettingsCache;
	}

	/**
	 * @return Returns the debugMessageResource.
	 */
	public boolean isDebugMessageResource() {
		return debugMessageResource;
	}

	/**
	 * @param debugMessageResource
	 *            The debugMessageResource to set.
	 */
	public void setDebugMessageResource(boolean debugMessageResource) {
		this.debugMessageResource = debugMessageResource;
	}

	/**
	 * @return Returns the defaultIgnoreMarshallErrors.
	 */
	public boolean isDefaultIgnoreMarshallErrors() {
		return defaultIgnoreMarshallErrors;
	}

	/**
	 * @param defaultIgnoreMarshallErrors
	 *            The defaultIgnoreMarshallErrors to set.
	 */
	public void setDefaultIgnoreMarshallErrors(
			boolean defaultIgnoreMarshallErrors) {
		this.defaultIgnoreMarshallErrors = defaultIgnoreMarshallErrors;
	}

	/**
	 * @return Returns the jaxbContext.
	 */
	public String getJaxbContext() {
		return jaxbContext;
	}

	/**
	 * @param jaxbContext
	 *            The jaxbContext to set.
	 */
	public void setJaxbContext(String jaxbContext) {
		this.jaxbContext = jaxbContext;
	}

	/**
	 * @return Returns the marshallerProperties.
	 */
	public Map<String, Object> getMarshallerProperties() {
		return marshallerProperties;
	}

	/**
	 * @param marshallerProperties
	 *            The marshallerProperties to set.
	 */
	public void setMarshallerProperties(Map<String, Object> marshallerProperties) {
		this.marshallerProperties = marshallerProperties;
	}

	/**
	 * @return Returns the messagesSource.
	 */
	public MessagesSource getMessagesSource() {
		return messagesSource;
	}

	/**
	 * @param messagesSource
	 *            The messagesSource to set.
	 */
	public void setMessagesSource(MessagesSource messagesSource) {
		this.messagesSource = messagesSource;
	}

	/**
	 * @return Returns the settingDao.
	 */
	public XwebParamDao getParameterDao() {
		return parameterDao;
	}

	/**
	 * @param settingDao
	 *            The settingDao to set.
	 */
	public void setParameterDao(XwebParamDao settingDao) {
		this.parameterDao = settingDao;
	}

	/**
	 * @return Returns the webHelper.
	 */
	public XwebHelper getWebHelper() {
		return webHelper;
	}

	/**
	 * @param webHelper
	 *            The webHelper to set.
	 */
	public void setWebHelper(XwebHelper webHelper) {
		this.webHelper = webHelper;
	}
	
	/**
	 * @param transformerFactory the transformerFactory to set
	 */
	public void setTransformerFactory(TransformerFactory transformerFactory) {
		this.transformerFactory = transformerFactory;
	}
	
	/**
	 * @return the contentTypeResolver
	 */
	public ContentTypeResolver getContentTypeResolver() {
		return contentTypeResolver;
	}

	/**
	 * @param contentTypeResolver the contentTypeResolver to set
	 */
	public void setContentTypeResolver(ContentTypeResolver contentTypeResolver) {
		this.contentTypeResolver = contentTypeResolver;
	}

	/**
	 * @return the enableXmlSourceResponse
	 */
	public boolean isEnableXmlSourceResponse() {
		return enableXmlSourceResponse;
	}

	/**
	 * @param enableXmlSourceResponse the enableXmlSourceResponse to set
	 */
	public void setEnableXmlSourceResponse(boolean enableXmlSourceResponse) {
		this.enableXmlSourceResponse = enableXmlSourceResponse;
	}

	/**
	 * @return the includeMessages
	 */
	public boolean isIncludeMessages() {
		return includeMessages;
	}

	/**
	 * @param includeMessages the includeMessages to set
	 */
	public void setIncludeMessages(boolean includeMessages) {
		this.includeMessages = includeMessages;
	}

}
