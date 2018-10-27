/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.util.Date;
import java.util.HashMap;

import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;

public abstract class DatasourceBase {
	
	protected String m_templateId;
	protected String m_facilityId;
	protected Date m_startDate;
	protected Date m_endDate;
	protected HashMap<String, String> m_propertiesMap;
	
	protected static final String SUFFIX_KEY_VALUE = "suffix";
	
	abstract public HashMap<String, Object> createDataSource(int num) throws ReportingPropertyNotFound;

	public void setFacilityId(String facilityId) {
		m_facilityId = facilityId;
	}

	public void setTemplateId(String templateId) {
		m_templateId = templateId;
	}

	public void setPropertiesMap(HashMap<String, String> map) {
		m_propertiesMap = map;
	}
	
	public void setStartDate(Date date) {
		m_startDate = date;
	}
	
	public void setEndDate(Date date) {
		m_endDate = date;
	}
	
	/**
	 * keyに対応するプロパティがあるかを確認し、ない場合はdefaultValueを返す
	 * @param key
	 * @param defaultValue
	 * @return String
	 */
	public String isDefine(String key, String defaultValue) {
		
		if(m_propertiesMap.get(key) == null || m_propertiesMap.get(key).isEmpty()) {
			return defaultValue;
		} else {
			return m_propertiesMap.get(key);
		}
	}
}
