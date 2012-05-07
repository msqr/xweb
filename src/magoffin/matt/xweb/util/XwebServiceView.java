/* ===================================================================
 * XwebServiceView.java
 * 
 * Created Feb 6, 2005 5:11:39 PM
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
 * $Id: XwebServiceView.java,v 1.1 2006/07/10 04:22:34 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import magoffin.matt.xweb.Xweb;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Extension of XwebJaxbView to output simple XML service responses.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/07/10 04:22:34 $
 */
public class XwebServiceView extends XwebJaxbView {
	
	private boolean includeXwebMessages = false;
	private boolean includeXwebSession = false;
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Xweb buildXweb(Map model, String rootName,
			HttpServletRequest request) throws Exception {
		// create Xweb data container now
		Xweb xData = new Xweb();
		
		String modelKey = model.containsKey(XwebConstants.DEFALUT_MODEL_OBJECT) 
			? XwebConstants.DEFALUT_MODEL_OBJECT 
			: rootName;
		
		processModelObject(xData,model,modelKey);
		processNonModelObjects(xData, model, modelKey);
		
		setupContext(request,xData);

		Locale requestLocale = RequestContextUtils.getLocale(request);
		processErrors(request,xData,model,rootName,modelKey,requestLocale);
		processMessages(request,xData,model,rootName,modelKey,requestLocale);
		
		if ( includeXwebSession ) {
			processSession(request,xData);
		}
		
		processRequestData(request,xData);
		
		if ( includeXwebMessages ) {
			processMessagesSource(xData, requestLocale);
		}

		debugXweb(xData);
		
	    return xData;
	}

	@Override
	protected boolean isContextRequired() {
		return false;
	}
	
	/**
	 * @return the includeXwebMessages
	 */
	public boolean isIncludeXwebMessages() {
		return includeXwebMessages;
	}
	
	/**
	 * @param includeXwebMessages the includeXwebMessages to set
	 */
	public void setIncludeXwebMessages(boolean includeXwebMessages) {
		this.includeXwebMessages = includeXwebMessages;
	}

	/**
	 * @return Returns the includeXwebSession.
	 */
	public boolean isIncludeXwebSession() {
		return includeXwebSession;
	}

	/**
	 * @param includeXwebSession The includeXwebSession to set.
	 */
	public void setIncludeXwebSession(boolean includeXwebSession) {
		this.includeXwebSession = includeXwebSession;
	}
	
}
