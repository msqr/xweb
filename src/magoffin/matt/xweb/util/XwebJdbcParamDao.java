/* ===================================================================
 * XwebJdbcParamDao.java
 * 
 * Created Jul 22, 2006 5:15:02 PM
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: XwebJdbcParamDao.java,v 1.2 2006/08/26 06:11:34 matt Exp $
 * ===================================================================
 */

package magoffin.matt.xweb.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import magoffin.matt.xweb.ObjectFactory;
import magoffin.matt.xweb.XwebParameter;

/**
 * Implementation of {@link XwebParamDao} using Spring's JdbcTemplate.
 * 
 * <p>This implementation relies on a simple 2-column table structure
 * where the paramter key is the primary key column.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2006/08/26 06:11:34 $
 */
public class XwebJdbcParamDao implements XwebParamDao, InitializingBean {
	
	private JdbcTemplate jdbcTemplate;
	private String keyColumnName = "skey";
	private String valueColumnName = "svalue";
	private String tableName = "settings";
	private String keyPrefix = "";
	
	private ObjectFactory objectFactory = new ObjectFactory();
	private String getByKeySql = "select " +keyColumnName +", " +valueColumnName
		+" from " +tableName +" where " +keyColumnName +" = ?";
	private String getAllSql = null;
	private String deleteByKeySql = "delete from " +tableName +" where " +keyColumnName
		+" = ?";
	private String updateByKeySql = "update " +tableName +" set " +valueColumnName
		+" = ? where " +keyColumnName + " = ?";
	private String insertSql = "insert into " +tableName +" (" +keyColumnName
		+", " +valueColumnName +") values (?, ?)";
		
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(jdbcTemplate);
		if ( !StringUtils.hasText(getAllSql) ) {
			getAllSql = "select " +keyColumnName +", " +valueColumnName
			+" from " +tableName 
			+(StringUtils.hasText(keyPrefix) 
					? " where " +keyColumnName +" like '" +keyPrefix +"%'" 
					: "")
			+" order by " +keyColumnName;
		}
	}
	
	private XwebParameter buildParameter(ResultSet rs) throws SQLException {
		try {
			XwebParameter param = objectFactory.createXwebParameter();
			String key = rs.getString(keyColumnName);
			if ( StringUtils.hasLength(keyPrefix) ) {
				param.setKey(key.substring(keyPrefix.length()));
			} else {
				param.setKey(key);
			}
			param.setValue(rs.getString(valueColumnName));
			return param;
		} catch ( JAXBException e ) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.xweb.util.XwebParamDao#getParameter(java.lang.String)
	 */
	public XwebParameter getParameter(String key) {
		return (XwebParameter)jdbcTemplate.query(getByKeySql, 
				new Object[]{keyPrefix+key}, 
				new ResultSetExtractor() {
					public Object extractData(ResultSet rs) 
					throws SQLException, DataAccessException {
						if ( !rs.next() ) {
							return null;
						}
						return buildParameter(rs);
					}
		});
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.xweb.util.XwebParamDao#getParameters()
	 */
	@SuppressWarnings("unchecked")
	public List<XwebParameter> getParameters() {
		return jdbcTemplate.query(getAllSql, new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				return buildParameter(rs);
			}
		});
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.xweb.util.XwebParamDao#removeParameter(java.lang.String)
	 */
	public void removeParameter(String key) {
		jdbcTemplate.update(deleteByKeySql, new Object[] {keyPrefix+key});
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.xweb.util.XwebParamDao#updateParameter(magoffin.matt.xweb.XwebParameter)
	 */
	public XwebParameter updateParameter(XwebParameter parameter) {
		// see if exists...
		XwebParameter storedParam = getParameter(parameter.getKey());
		if ( storedParam != null ) {
			jdbcTemplate.update(updateByKeySql, new Object[] {
					parameter.getValue(), keyPrefix+storedParam.getKey()});
			storedParam.setValue(parameter.getValue());
			return storedParam;
		}
		jdbcTemplate.update(insertSql, new Object[] {
				keyPrefix+parameter.getKey(), parameter.getValue()});
		return parameter;
	}
	
	/**
	 * @return the deleteByKeySql
	 */
	public String getDeleteByKeySql() {
		return deleteByKeySql;
	}
	
	/**
	 * @param deleteByKeySql the deleteByKeySql to set
	 */
	public void setDeleteByKeySql(String deleteByKeySql) {
		this.deleteByKeySql = deleteByKeySql;
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
	 * @return the getByKeySql
	 */
	public String getGetByKeySql() {
		return getByKeySql;
	}
	
	/**
	 * @param getByKeySql the getByKeySql to set
	 */
	public void setGetByKeySql(String getByKeySql) {
		this.getByKeySql = getByKeySql;
	}
	
	/**
	 * @return the insertSql
	 */
	public String getInsertSql() {
		return insertSql;
	}
	
	/**
	 * @param insertSql the insertSql to set
	 */
	public void setInsertSql(String insertSql) {
		this.insertSql = insertSql;
	}
	
	/**
	 * @return the jdbcTemplate
	 */
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	
	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
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
	 * @return the updateByKeySql
	 */
	public String getUpdateByKeySql() {
		return updateByKeySql;
	}
	
	/**
	 * @param updateByKeySql the updateByKeySql to set
	 */
	public void setUpdateByKeySql(String updateByKeySql) {
		this.updateByKeySql = updateByKeySql;
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

	/**
	 * @return the prefix
	 */
	public String getKeyPrefix() {
		return keyPrefix;
	}
	
	/**
	 * @param prefix the prefix to set
	 */
	public void setKeyPrefix(String prefix) {
		this.keyPrefix = prefix;
	}

}
