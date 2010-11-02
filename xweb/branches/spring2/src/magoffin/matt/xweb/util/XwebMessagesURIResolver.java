/* ===================================================================
 * XwebMessagesURIResolver.java
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
 * $Id: XwebMessagesURIResolver.java,v 1.1 2007/10/02 09:03:52 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import magoffin.matt.xweb.ObjectFactory;
import magoffin.matt.xweb.XMessages;
import magoffin.matt.xweb.XwebMessage;

/**
 * URIResolver that can resolve a special xweb:messages URI.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.1 $ $Date: 2007/10/02 09:03:52 $
 */
public class XwebMessagesURIResolver implements URIResolver {

	/** The URI for XwebMessages. */
	public static final String XWEB_MESSAGES_URI_PREFIX = "xmsg://";
	
	private MessagesSource messagesSource;
	private ObjectFactory objectFactory = new ObjectFactory();
	private Map<String, XMessages> cache = new HashMap<String, XMessages>();
	private JAXBContext context;
	
	/**
	 * Default constructor;
	 */
	public XwebMessagesURIResolver() {
		super();
		try {
			context = JAXBContext.newInstance("magoffin.matt.xweb");
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Source resolve(String href, String base) throws TransformerException {
		if ( !href.startsWith(XWEB_MESSAGES_URI_PREFIX) ) {
			return null;
		}
		Locale locale = Locale.getDefault();
		if ( href.length() > XWEB_MESSAGES_URI_PREFIX.length() ) {
			String[] data = href.substring(XWEB_MESSAGES_URI_PREFIX.length())
				.split("_");
			if ( data.length > 1 ) {
				locale = new Locale(data[0], data[1]);
			} else {
				locale = new Locale(data[0]);
			}
		}
		if ( locale == null ) {
			locale = Locale.getDefault();
		}
		
		String key = locale.toString();
		try {
			if (cache.containsKey(key)) {
				return new JAXBSource(context, cache.get(key));
			}
	
			synchronized (cache) {
				if (cache.containsKey(key)) {
					return new JAXBSource(context, cache.get(key));
				}
	
				XMessages xMsgs = objectFactory.createXMessages();
	
				Enumeration<String> enumeration = messagesSource.getKeys(locale);
				while (enumeration.hasMoreElements()) {
					String msgKey = enumeration.nextElement();
					XwebMessage xMsg = objectFactory.createXwebMessage();
					xMsgs.getMsg().add(xMsg);
					xMsg.setKey(msgKey);
					Object val = messagesSource.getMessage(msgKey, null, locale);
					if (val != null) {
						xMsg.setValue(val.toString());
					}
				}
				cache.put(key, xMsgs);
				return new JAXBSource(context, xMsgs);
			}
		} catch ( JAXBException e ) {
			throw new TransformerException("JAXB exception", e);
		}
	}

	/**
	 * @return the msgSrc
	 */
	public MessagesSource getMessagesSource() {
		return messagesSource;
	}

	/**
	 * @param msgSrc the msgSrc to set
	 */
	public void setMessagesSource(MessagesSource msgSrc) {
		this.messagesSource = msgSrc;
	}

	/**
	 * @return the objectFactory
	 */
	public ObjectFactory getObjectFactory() {
		return objectFactory;
	}

	/**
	 * @param objectFactory the objectFactory to set
	 */
	public void setObjectFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	/**
	 * @return the context
	 */
	public JAXBContext getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(JAXBContext context) {
		this.context = context;
	}

}
