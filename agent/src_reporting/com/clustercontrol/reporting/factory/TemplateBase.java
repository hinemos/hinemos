/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;
import com.clustercontrol.util.Messages;

import net.sf.jasperreports.engine.JasperPrint;

public abstract class TemplateBase {

	protected Integer m_curPage = 0;
	protected boolean m_showPage = true;
	protected Date m_startDate;
	protected Date m_endDate;
	protected List<String[]> m_nodes = new ArrayList<>();
	protected String m_templateId;
	protected String m_jrxmlFilePath;
	protected HashMap<String, String> m_propertiesMap;

	abstract public List<JasperPrint> getReport(Integer pageOffset) throws ReportingPropertyNotFound;

	public void setShowPage(boolean show) {
		m_showPage = show;
	}

	public void setCurPage(int page) {
		m_curPage = page;
	}

	public void setReportPeriod(Date start, Date end) {
		m_startDate = start;
		m_endDate = end;		
	}

	public void setReportPeriodByDays(Date start, int days) {
		m_startDate = start;
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);
		cal.add(Calendar.DATE, days);
		m_endDate = cal.getTime();
	}

	public void setReportPeriodByMonths(Date start, int months) {
		m_startDate = start;
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);
		cal.add(Calendar.MONTH, months);
		m_endDate = cal.getTime();
	}

	public void setNodes(List<String[]> nodeList) {
		m_nodes = nodeList;
	}
	
	public void setTemplateId(String templateId) {
		m_templateId = templateId;
	}
	
	public void setJrxmlFilePath(String path) {
		m_jrxmlFilePath = path;
	}
	
	public void setPropertiesMap(HashMap<String, String> map) {
		m_propertiesMap = map;
		
		this.setJrxmlFilePath(m_propertiesMap.get(ReportingConstant.JRXML_PATH_KEY_VALUE));
	}
	
	public boolean getShowPage() {
		return m_showPage;
	}

	public Integer getCurPage() {
		return m_curPage;
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
	
	protected HashMap<String, String> getCommonProperty() {
		
		HashMap<String, String> retMap = new HashMap<String, String>();
		
		// 重要度表記
		retMap.put(ReportingConstant.CRIT_STR_KEY_VALUE, isDefine(ReportingConstant.CRIT_STR_KEY_VALUE, Messages.getString("COMMON_PRIORITY_CRIT")));
		retMap.put(ReportingConstant.WARN_STR_KEY_VALUE, isDefine(ReportingConstant.WARN_STR_KEY_VALUE, Messages.getString("COMMON_PRIORITY_WARN")));
		retMap.put(ReportingConstant.INFO_STR_KEY_VALUE, isDefine(ReportingConstant.INFO_STR_KEY_VALUE, Messages.getString("COMMON_PRIORITY_INFO")));
		retMap.put(ReportingConstant.UNKNOWN_STR_KEY_VALUE, isDefine(ReportingConstant.UNKNOWN_STR_KEY_VALUE, Messages.getString("COMMON_PRIORITY_UNKNOWN")));
		retMap.put(ReportingConstant.TOTAL_KEY_VALUE, isDefine(ReportingConstant.TOTAL_KEY_VALUE, Messages.getString("MONITORING_TOTAL")));
		
		// ジョブステータス表記
		retMap.put(ReportingConstant.STATUS_NORMAL_KEY_VALUE, isDefine(ReportingConstant.STATUS_NORMAL_KEY_VALUE, Messages.getString("COMMON_STATUS_NORMAL")));
		retMap.put(ReportingConstant.STATUS_WARNING_KEY_VALUE, isDefine(ReportingConstant.STATUS_WARNING_KEY_VALUE, Messages.getString("COMMON_STATUS_WARNING")));
		retMap.put(ReportingConstant.STATUS_ERROR_KEY_VALUE, isDefine(ReportingConstant.STATUS_ERROR_KEY_VALUE, Messages.getString("COMMON_STATUS_ERROR")));
		
		// 共通
		retMap.put(ReportingConstant.ITEM_NAME_MSG, isDefine(ReportingConstant.ITEM_NAME_MSG, Messages.getString("MONITORING_DETAL_ITEM_NAME_MESSAGE")));
		retMap.put(ReportingConstant.ITEM_NAME_OWNROLEID, isDefine(ReportingConstant.ITEM_NAME_OWNROLEID, Messages.getString("MONITORING_DETAL_ITEM_NAME_OWNERROLEID")));
		retMap.put(ReportingConstant.ITEM_NAME_SCPTXT, isDefine(ReportingConstant.ITEM_NAME_SCPTXT, Messages.getString("MONITORING_DETAL_ITEM_NAME_SCOPETEXT")));
		
		// 監視項目表記
		retMap.put(ReportingConstant.ITEM_NAME_APPLI, isDefine(ReportingConstant.ITEM_NAME_APPLI, Messages.getString("MONITORING_DETAL_ITEM_NAME_APPLICATION")));
		retMap.put(ReportingConstant.ITEM_NAME_COMM, isDefine(ReportingConstant.ITEM_NAME_COMM, Messages.getString("MONITORING_DETAL_ITEM_NAME_COMMENT")));
		retMap.put(ReportingConstant.ITEM_NAME_COMMDATE, isDefine(ReportingConstant.ITEM_NAME_COMMDATE, Messages.getString("MONITORING_DETAL_ITEM_NAME_COMMENTDATE")));
		retMap.put(ReportingConstant.ITEM_NAME_COMMUSR, isDefine(ReportingConstant.ITEM_NAME_COMMUSR, Messages.getString("MONITORING_DETAL_ITEM_NAME_COMMENTUSER")));
		retMap.put(ReportingConstant.ITEM_NAME_CONFUSR, isDefine(ReportingConstant.ITEM_NAME_CONFUSR, Messages.getString("MONITORING_DETAL_ITEM_NAME_CONFIRMUSER")));
		retMap.put(ReportingConstant.ITEM_NAME_GENEDATE, isDefine(ReportingConstant.ITEM_NAME_GENEDATE, Messages.getString("MONITORING_DETAL_ITEM_NAME_GENERATIONDATE")));
		retMap.put(ReportingConstant.ITEM_NAME_MSGID, isDefine(ReportingConstant.ITEM_NAME_MSGID, Messages.getString("MONITORING_DETAL_ITEM_NAME_MESSAGEID")));
		retMap.put(ReportingConstant.ITEM_NAME_MONDETID, isDefine(ReportingConstant.ITEM_NAME_MONDETID, Messages.getString("MONITORING_DETAL_ITEM_NAME_MONITORDETAILID")));
		retMap.put(ReportingConstant.ITEM_NAME_MONID, isDefine(ReportingConstant.ITEM_NAME_MONID, Messages.getString("MONITORING_DETAL_ITEM_NAME_MONITORID")));
		retMap.put(ReportingConstant.ITEM_NAME_OUTDATE, isDefine(ReportingConstant.ITEM_NAME_OUTDATE, Messages.getString("MONITORING_DETAL_ITEM_NAME_OUTPUTDATE")));
		retMap.put(ReportingConstant.ITEM_NAME_PLGID, isDefine(ReportingConstant.ITEM_NAME_PLGID, Messages.getString("MONITORING_DETAL_ITEM_NAME_PLUGINID")));
		retMap.put(ReportingConstant.ITEM_NAME_PRI, isDefine(ReportingConstant.ITEM_NAME_PRI, Messages.getString("MONITORING_DETAL_ITEM_NAME_PRIORITY")));
		
		// ジョブ項目表記
		retMap.put(ReportingConstant.ITEM_NAME_ELPSEDTIME, isDefine(ReportingConstant.ITEM_NAME_ELPSEDTIME, Messages.getString("JOB_ITEM_NAME_ELPSEDTIME")));
		retMap.put(ReportingConstant.ITEM_NAME_ENDDATE, isDefine(ReportingConstant.ITEM_NAME_ENDDATE, Messages.getString("JOB_ITEM_NAME_ENDDATE")));
		retMap.put(ReportingConstant.ITEM_NAME_ENDSTATUS, isDefine(ReportingConstant.ITEM_NAME_ENDSTATUS, Messages.getString("JOB_ITEM_NAME_ENDSTATUS")));
		retMap.put(ReportingConstant.ITEM_NAME_ENDTIME, isDefine(ReportingConstant.ITEM_NAME_ENDTIME, Messages.getString("JOB_ITEM_NAME_ENDTIME")));
		retMap.put(ReportingConstant.ITEM_NAME_ENDVALUE, isDefine(ReportingConstant.ITEM_NAME_ENDVALUE, Messages.getString("JOB_ITEM_NAME_ENDVALUE")));
		retMap.put(ReportingConstant.ITEM_NAME_FACILITYID, isDefine(ReportingConstant.ITEM_NAME_FACILITYID, Messages.getString("JOB_ITEM_NAME_FACILITYID")));
		retMap.put(ReportingConstant.ITEM_NAME_JOBID, isDefine(ReportingConstant.ITEM_NAME_JOBID, Messages.getString("JOB_ITEM_NAME_JOBID")));
		retMap.put(ReportingConstant.ITEM_NAME_JOBUNITID, isDefine(ReportingConstant.ITEM_NAME_JOBUNITID, Messages.getString("JOB_ITEM_NAME_JOBUNITID")));
		retMap.put(ReportingConstant.ITEM_NAME_NODENAME, isDefine(ReportingConstant.ITEM_NAME_NODENAME, Messages.getString("JOB_ITEM_NAME_NODENAME")));
		retMap.put(ReportingConstant.ITEM_NAME_SCHEDULEDATE, isDefine(ReportingConstant.ITEM_NAME_SCHEDULEDATE, Messages.getString("JOB_ITEM_NAME_SCHEDULEDATE")));
		retMap.put(ReportingConstant.ITEM_NAME_SESSIONID, isDefine(ReportingConstant.ITEM_NAME_SESSIONID, Messages.getString("JOB_ITEM_NAME_SESSIONID")));
		retMap.put(ReportingConstant.ITEM_NAME_STARTDATE, isDefine(ReportingConstant.ITEM_NAME_STARTDATE, Messages.getString("JOB_ITEM_NAME_STARTDATE")));
		retMap.put(ReportingConstant.ITEM_NAME_STARTJOBID, isDefine(ReportingConstant.ITEM_NAME_STARTJOBID, Messages.getString("JOB_ITEM_NAME_STARTJOBID")));
		retMap.put(ReportingConstant.ITEM_NAME_STARTTIME, isDefine(ReportingConstant.ITEM_NAME_STARTTIME, Messages.getString("JOB_ITEM_NAME_STARTTIME")));
		retMap.put(ReportingConstant.ITEM_NAME_STATUS, isDefine(ReportingConstant.ITEM_NAME_STATUS, Messages.getString("JOB_ITEM_NAME_STATUS")));
		retMap.put(ReportingConstant.ITEM_NAME_TRIGGERINFO, isDefine(ReportingConstant.ITEM_NAME_TRIGGERINFO, Messages.getString("JOB_ITEM_NAME_TRIGGERINFO")));
		retMap.put(ReportingConstant.ITEM_NAME_TRIGGERTYPE, isDefine(ReportingConstant.ITEM_NAME_TRIGGERTYPE, Messages.getString("JOB_ITEM_NAME_TRIGGERTYPE")));
		
		return retMap;
	}
}
