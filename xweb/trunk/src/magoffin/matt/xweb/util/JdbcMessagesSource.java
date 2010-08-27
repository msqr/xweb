/* ===================================================================
 * JdbcMessagesSource.java
 * 
 * Created Sep 6, 2005 9:19:36 PM
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
 * $Id: JdbcMessagesSource.java,v 1.3 2007/09/25 06:23:43 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import magoffin.matt.xweb.util.MessagesSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.util.StringUtils;

/**
 * MessagesSource implementation that derives messages from a database table.
 * 
 * <p>This MessagesSource supports the <code>parentMessageSource</code> property
 * so that this can be used to provide database values that "override" the 
 * parent's message values.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.3 $ $Date: 2007/09/25 06:23:43 $
 */
public class JdbcMessagesSource extends AbstractMessageSource 
implements MessagesSource, InitializingBean {

	private JdbcTemplate jdbcTemplate;
	private String keyColumnName = "skey";
	private String valueColumnName = "svalue";
	private String tableName = "settings";

	private String getAllSql = null;
	private String keyPrefix = "msg:";
	
	private final Logger log = Logger.getLogger(getClass());
	private Map<String, Map<String, String>> jdbcCache 
		= new HashMap<String, Map<String,String>>();
	
	public void afterPropertiesSet() throws Exception {
		if ( !StringUtils.hasText(getAllSql) ) {
			getAllSql = "select " +keyColumnName +", " +valueColumnName
			+" from " +tableName 
			+(StringUtils.hasText(keyPrefix) 
					? " where " +keyColumnName +" like '" +keyPrefix +"%'" 
					: "")
			+" order by " +keyColumnName;
		}
	}

	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {
		Map<String, String> data = getMessageMap(locale);
		if ( data.containsKey(code) ) {
			return new MessageFormat(data.get(code).toString(),locale);
		}
		return null;
	}
	
	@Override
	protected String resolveCodeWithoutArguments(String code, Locale locale) {
		Map<String, String> data = getMessageMap(locale);
		if ( data.containsKey(code) ) {
			return data.get(code).toString();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.xweb.util.MessagesSource#registerMessageResource(java.lang.String)
	 */
	public void registerMessageResource(String resource) {
		throw new UnsupportedOperationException();
	}

	private Map<String, String> getMessageMap(Locale locale) {
		if ( jdbcCache.containsKey(locale.getLanguage()) ) {
			return jdbcCache.get(locale.getLanguage());
		}
		final Map<String, String> data = new LinkedHashMap<String, String>();
		final String prefix = keyPrefix +locale.getLanguage() +':';
		jdbcTemplate.query(getAllSql,new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				String key = rs.getString(keyColumnName);
				String value = rs.getString(valueColumnName);
				if ( StringUtils.hasText(key) && key.startsWith(prefix) 
						&& StringUtils.hasText(value) ) {
					key = key.substring(prefix.length());
					if ( log.isDebugEnabled() ) {
						log.debug("Got DB message [" +key +"] value [" 
								+value +"]");
					}
					data.put(key,value);
				}
			}
		});

		jdbcCache.put(locale.getLanguage(),data);
		return data;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.xweb.util.MessagesSource#getKeys(java.util.Locale)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Enumeration getKeys(Locale locale) {
		final Set data = new LinkedHashSet();
		Map<String, String> jdbcMap = getMessageMap(locale);
		data.addAll(jdbcMap.keySet());
		if ( getParentMessageSource() instanceof MessagesSource ) {
			MessagesSource parent = (MessagesSource)getParentMessageSource();
			Enumeration keys = parent.getKeys(locale);
			while ( keys.hasMoreElements() ) {
				String key = (String)keys.nextElement();
				if ( !data.contains(key) ) {
					data.add(key);
				}
			}
		}
		return Collections.enumeration(data);
	}

	/**
	 * @return Returns the jdbcTemplate.
	 */
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	
	/**
	 * @param jdbcTemplate The jdbcTemplate to set.
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	/**
	 * @return Returns the keyPrefix.
	 */
	public String getKeyPrefix() {
		return keyPrefix;
	}
	
	/**
	 * @param keyPrefix The keyPrefix to set.
	 */
	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}
	
	/**
	 * @return the getAllSql
	 */
	public String getGetAllSql() {
		return getAllSql;
	}
	
	/**
	 * @param getAllSql the getAllSql to set
	 */
	public void setGetAllSql(String getAllSql) {
		this.getAllSql = getAllSql;
	}
	
	/**
	 * @return the jdbcCache
	 */
	public Map<String, Map<String, String>> getJdbcCache() {
		return jdbcCache;
	}
	
	/**
	 * @param jdbcCache the jdbcCache to set
	 */
	public void setJdbcCache(Map<String, Map<String, String>> jdbcCache) {
		this.jdbcCache = jdbcCache;
	}
	
	/**
	 * @return the keyColumnName
	 */
	public String getKeyColumnName() {
		return keyColumnName;
	}
	
	/**
	 * @param keyColumnName the keyColumnName to set
	 */
	public void setKeyColumnName(String keyColumnName) {
		this.keyColumnName = keyColumnName;
	}
	
	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}
	
	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	/**
	 * @return the valueColumnName
	 */
	public String getValueColumnName() {
		return valueColumnName;
	}
	
	/**
	 * @param valueColumnName the valueColumnName to set
	 */
	public void setValueColumnName(String valueColumnName) {
		this.valueColumnName = valueColumnName;
	}

}
