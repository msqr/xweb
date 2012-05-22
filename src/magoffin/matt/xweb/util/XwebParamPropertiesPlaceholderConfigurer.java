/* ===================================================================
 * XwebParamPropertiesPlaceholderConfigurer.java
 * 
 * Created Aug 21, 2005 10:15:07 AM
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
 * $Id: XwebParamPropertiesPlaceholderConfigurer.java,v 1.2 2006/08/26 06:12:07 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import java.util.List;
import java.util.Properties;
import magoffin.matt.xweb.XwebParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.dao.DataAccessException;
import org.springframework.util.StringUtils;

/**
 * Extension of PropertyPlaceholderConfigurer to add support for reading
 * properties from a JDBC source.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2006/08/26 06:12:07 $
 */
public class XwebParamPropertiesPlaceholderConfigurer extends
		PropertyPlaceholderConfigurer {

	@Autowired
	private XwebParamDao settingDao;
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Override
	protected void processProperties(
			ConfigurableListableBeanFactory beanFactoryToProcess,
			final Properties props) throws BeansException {
		
		List<XwebParameter> settings;
		try {
			settings = settingDao.getParameters();
			for ( XwebParameter param : settings ) {
				String key = param.getKey();
				if ( StringUtils.hasText(param.getValue()) ) {
					String value = param.getValue();
					if ( log.isDebugEnabled() ) {
						if ( props.containsKey(key) ) {
							if ( value.equals(props.getProperty(key)) ) {
								log.debug("Read property [" + key + "] from DB; value [" + value + "]");
							} else {
								log.debug("Overriding property [" + key + "] value ["
										+ props.getProperty(key) + "] with DB value [" + value + "]");
							}
						} else {
							log.debug("Adding DB property [" + key + "]: " + value);
						}
					} else if ( log.isInfoEnabled() ) {
						// don't log values at INFO level, because contains passwords and such
						if ( props.containsKey(key) ) {
							if ( value.equals(props.getProperty(key)) ) {
								log.info("Read property [" + key + "] from DB.");
							} else {
								log.info("Overriding property [" + key + "] with DB value.");
							}
						} else {
							log.info("Adding DB property [" + key + "]");
						}
					}
					props.put(key, value);
				}
			}
		} catch ( DataAccessException e ) {
			log.warn("Unable to process property placeholder XwebParameters: {}", e.getMessage());
		}
		
		super.processProperties(beanFactoryToProcess, props);
	}
	
	/**
	 * @return the settingDao
	 */
	public XwebParamDao getSettingDao() {
		return settingDao;
	}
	
	/**
	 * @param settingDao the settingDao to set
	 */
	public void setSettingDao(XwebParamDao settingDao) {
		this.settingDao = settingDao;
	}
	
}
