/*
Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.
 */
package com.clustercontrol.ws.monitor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.bean.MonitorFilterInfo;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.performance.bean.CollectorItemInfo;
import com.clustercontrol.performance.session.PerformanceControllerBean;
import com.clustercontrol.sql.bean.JdbcDriverInfo;
import com.clustercontrol.sql.session.MonitorSqlControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.ws.util.HashMapInfo;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * 監視設定用のWebAPIエンドポイント
 */
@javax.jws.WebService(targetNamespace = "http://monitor.ws.clustercontrol.com")
public class MonitorSettingEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( MonitorSettingEndpoint.class );
	private static Log m_opelog = LogFactory.getLog("HinemosOperation");

	/**
	 * echo(WebサービスAPI疎通用)
	 *
	 * 権限必要なし（ユーザ名チェックのみ実施）
	 *
	 * @param str
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public String echo(String str) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return str + ", " + str;
	}

	/**
	 * 監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @return 監視設定一覧
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<MonitorInfo> getMonitorList() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getMonitorList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Get"
				+ ", Method=getMonitorList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MonitorSettingControllerBean().getMonitorListWithoutCheckInfo(null);
	}

	/**
	 * 監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @param condition フィルタ条件
	 * @return 監視設定一覧
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<MonitorInfo> getMonitorListByCondition(MonitorFilterInfo condition) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getMonitorList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		if(condition != null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			sdf.setTimeZone(HinemosTime.getTimeZone());
			StringBuffer msg = new StringBuffer();
			msg.append(", MonitorID=");
			msg.append(condition.getMonitorId());
			msg.append(", MonitorTypeID=");
			msg.append(condition.getMonitorTypeId());
			msg.append(", Description=");
			msg.append(condition.getDescription());
			msg.append(", FacilityID=");
			msg.append(condition.getFacilityId());
			msg.append(", CalendarID=");
			msg.append(condition.getCalendarId());
			msg.append(", RegUser=");
			msg.append(condition.getRegUser());
			msg.append(", RegFromDate=");
			msg.append(condition.getRegFromDate()==null?null:sdf.format(new Date(condition.getRegFromDate())));
			msg.append(", RegToDate=");
			msg.append(condition.getRegToDate()==null?null:sdf.format(new Date(condition.getRegToDate())));
			msg.append(", UpdateUser=");
			msg.append(condition.getUpdateUser());
			msg.append(", UpdateFromDate=");
			msg.append(condition.getUpdateFromDate()==null?null:sdf.format(new Date(condition.getUpdateFromDate())));
			msg.append(", UpdateToDate=");
			msg.append(condition.getUpdateToDate()==null?null:sdf.format(new Date(condition.getUpdateToDate())));
			msg.append(", MonitorFlg=");
			msg.append(condition.getMonitorFlg());
			msg.append(", CollectorFlg=");
			msg.append(condition.getCollectorFlg());
			msg.append(", OwnerRoleId=");
			msg.append(condition.getOwnerRoleId());
			m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Get"
					+ ", Method=getMonitorListByCondition, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
		}

		return new MonitorSettingControllerBean().getMonitorListWithoutCheckInfo(condition);
	}

	/**
	 * 監視設定情報をマネージャに登録します。<BR>
	 *
	 * MonitorSettingAdd権限が必要
	 *
	 * @param info 通知情報
	 * @return 登録に成功した場合、true
	 * @throws HinemosUnknown
	 * @throws MonitorDuplicate
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * @see  com.clustercontrol.monitor.run.bean.MonitorInfo
	 */
	public boolean addMonitor(MonitorInfo info) throws MonitorDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		if (info == null)
			throw new HinemosUnknown("MonitorInfo is null");

		String id = info.getMonitorId();
		m_log.debug("addMonitor : monitorId=" + id + ", monitorInfo=" + info);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();

		msg.append(", MonitorID=");
		msg.append(id);
		if(MonitorTypeConstant.TYPE_STRING == info.getMonitorType()){
			// 文字列監視
			List<MonitorStringValueInfo> list = info.getStringValueInfo();
			if (list != null && list.size() > 0) {
				for (int i=0; i<list.size(); i++) {
					msg.append(", " + (i + 1) + "=(");
					msg.append("OrderNo=");
					msg.append(i);
					msg.append(", Description=");
					msg.append(list.get(i).getDescription());
					msg.append(", ProcessType=");
					msg.append(list.get(i).getProcessType());
					msg.append(", Pattern=");
					msg.append(list.get(i).getPattern());
					msg.append(", CaseSensitivityFlg=");
					msg.append(list.get(i).getCaseSensitivityFlg());
					msg.append(", ValidFlg=");
					msg.append(list.get(i).getValidFlg());
					msg.append(")");
				}
			}
		}
		if (info.getWinEventCheckInfo() != null) {
			info.getWinEventCheckInfo().reflect();
		}
		try {
			ret = new MonitorSettingControllerBean().addMonitor(info);
		} catch (Exception e) {
			m_opelog.warn(monitorTypeToTitle(info.getMonitorTypeId()) + " Add Failed"
					+ ", Method=add" + monitorTypeToMethod(info.getMonitorTypeId()) + ", User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(monitorTypeToTitle(info.getMonitorTypeId()) + " Add"
				+ ", Method=add" + monitorTypeToMethod(info.getMonitorTypeId()) + ", User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * 監視設定情報を更新します。<BR>
	 *
	 * MonitorSettingWrite権限が必要
	 *
	 * @param info 通知情報
	 * @return 変更に成功した場合、true
	 * @throws HinemosUnknown
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * @see  com.clustercontrol.monitor.run.bean.MonitorInfo
	 */
	public boolean modifyMonitor(MonitorInfo info) throws MonitorNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		if (info == null)
			throw new HinemosUnknown("MonitorInfo is null");
		
		String id = info.getMonitorId();
		
		m_log.debug("modifyMonitor : monitorId=" + id + ", monitorInfo=" + info);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", MonitorID=");
		msg.append(id);
		if(MonitorTypeConstant.TYPE_STRING == info.getMonitorType()){
			// 文字列監視
			List<MonitorStringValueInfo> list = info.getStringValueInfo();
			if (list != null && list.size() > 0) {
				for (int i=0; i<list.size(); i++) {
					msg.append(", " + (i + 1) + "=(");
					msg.append("OrderNo=");
					msg.append(i);
					msg.append(", Description=");
					msg.append(list.get(i).getDescription());
					msg.append(", ProcessType=");
					msg.append(list.get(i).getProcessType());
					msg.append(", Pattern=");
					msg.append(list.get(i).getPattern());
					msg.append(", CaseSensitivityFlg=");
					msg.append(list.get(i).getCaseSensitivityFlg());
					msg.append(", ValidFlg=");
					msg.append(list.get(i).getValidFlg());
					msg.append(")");
				}
			}
		}

		if (info.getWinEventCheckInfo() != null) {
			info.getWinEventCheckInfo().reflect();
		}
		try {
			ret = new MonitorSettingControllerBean().modifyMonitor(info);
		} catch (Exception e) {
			m_opelog.warn(monitorTypeToTitle(id) + " Change Failed"
					+ ", Method=modify" + monitorTypeToMethod(id) + ", User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(monitorTypeToTitle(id) + " Change"
				+ ", Method=modify" + monitorTypeToMethod(id) + ", User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * 監視設定情報をマネージャから削除します。<BR>
	 *
	 * MonitorSettingWrite権限が必要
	 *
	 * @param monitorIdList 監視項目ID
	 * @return 削除に成功した場合、true
	 * @throws HinemosUnknown
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 */
	public boolean deleteMonitor(List<String> monitorIdList, String monitorTypeId)
			throws MonitorNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.debug("deleteMonitor : monitorId=" + monitorIdList);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", MonitorID=");
		msg.append(monitorIdList);

		try {
			ret = new MonitorSettingControllerBean().deleteMonitor(monitorIdList, monitorTypeId);
		} catch (Exception e) {
			m_opelog.warn(monitorTypeToTitle(monitorTypeId) + " Delete Failed"
					+ ", Method=delete" + monitorTypeToMethod(monitorTypeId) + ", User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(monitorTypeToTitle(monitorTypeId) + " Delete"
				+ ", Method=delete" + monitorTypeToMethod(monitorTypeId) + ", User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * 監視設定情報を取得します。<BR>
	 * 引数のmonitorId,monitorTypeIdに対応する監視設定情報を取得します。
	 *
	 * MonitorSettingRead権限が必要
	 * 	 *
	 * @param monitorId 監視項目ID
	 * @param monitorTypeId 監視種別ID
	 * @return 監視情報
	 * @throws HinemosUnknown
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @see  com.clustercontrol.monitor.run.bean.MonitorInfo
	 */
	public MonitorInfo getMonitor(String monitorId) throws MonitorNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getMonitor : monitorId=" + monitorId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", MonitorID=");
		msg.append(monitorId);
		m_opelog.debug(" Get, Method=getMonitor, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new MonitorSettingControllerBean().getMonitor(monitorId);
	}

	/**
	 * 監視設定の監視を有効化/無効化します。
	 *
	 * MonitorSettingWrite権限が必要
	 *
	 * @param monitorId
	 * @param monitorTypeId
	 * @throws HinemosUnknown
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public void setStatusMonitor(String monitorId, String monitorTypeId, boolean validFlag) throws HinemosUnknown, MonitorNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("enableMonitor : monitorId=" + monitorId +
				", monitorTypeId = " + monitorTypeId + ", validFlag=" + validFlag);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", MonitorID=");
		msg.append(monitorId);
		msg.append(", ValidFlag=");
		msg.append(validFlag);

		try {
			new MonitorSettingControllerBean().setStatusMonitor(monitorId, monitorTypeId, validFlag);
		} catch (Exception e) {
			m_opelog.warn(monitorTypeToTitle(monitorTypeId) + " Change Valid Failed"
					+ ", Method=setStatusMonitor" + monitorTypeToMethod(monitorTypeId) + ", User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(monitorTypeToTitle(monitorTypeId) + " Change Valid"
				+ ", Method=setStatusMonitor" + monitorTypeToMethod(monitorTypeId) + ", User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

	}

	/**
	 * 監視設定の収集を有効化/無効化します。
	 *
	 * MonitorSettingWrite権限が必要
	 *
	 * @param monitorId
	 * @param monitorTypeId
	 * @throws HinemosUnknown
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public void setStatusCollector(String monitorId, String monitorTypeId, boolean validFlag) throws HinemosUnknown, MonitorNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("enableCollector : monitorId=" + monitorId +
				", monitorTypeId = " + monitorTypeId + ", validFlag=" + validFlag);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", MonitorID=");
		msg.append(monitorId);
		msg.append(", ValidFlag=");
		msg.append(validFlag);

		try {
			new MonitorSettingControllerBean().setStatusCollector(monitorId, monitorTypeId, validFlag);
		} catch (Exception e) {
			m_opelog.warn(monitorTypeToTitle(monitorTypeId) + " Change Valid Failed"
					+ ", Method=setStatusCollector" + monitorTypeToMethod(monitorTypeId) + ", User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(monitorTypeToTitle(monitorTypeId) + " Change Valid"
				+ ", Method=setStatusCollector" + monitorTypeToMethod(monitorTypeId) + ", User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//
	// 各監視機能用のメソッド
	//
	//////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * JDBC定義一覧をリストで返却します。<BR>
	 *
	 * @return JDBC定義のリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<JdbcDriverInfo> getJdbcDriverList() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getJdbcDriverList : ");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		return new MonitorSqlControllerBean().getJdbcDriverList();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//
	// 各監視機能用のリスト取得メソッド
	//
	//////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Hinemosエージェント監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getAgentList() throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("getAgentList()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR_AGENT + " Get"
				+ ", Method=getAgentList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MonitorSettingControllerBean().getAgentList();
	}

	/**
	 * HTTP監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getHttpList() throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("getHttpList()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR_HTTP + " Get"
				+ ", Method=getHttpList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MonitorSettingControllerBean().getHttpList();
	}

	/**
	 * JMX監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getJmxList() throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("getJmxList()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR_JMX + " Get"
				+ ", Method=getJmxList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MonitorSettingControllerBean().getJmxList();
	}

	/**
	 * ログファイル監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getLogfileList() throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("getLogfileList()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR_LOGFILE + " Get"
				+ ", Method=getLogfileList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MonitorSettingControllerBean().getLogfileList();
	}

	/**
	 * リソース監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getPerformanceList() throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("getPerformanceList()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR_PERFORMANCE + " Get"
				+ ", Method=getPerformanceList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MonitorSettingControllerBean().getPerformanceList();
	}

	/**
	 * Ping監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getPingList() throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("getPingList()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR_PING + " Get"
				+ ", Method=getPingList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MonitorSettingControllerBean().getPingList();
	}

	/**
	 * ポート監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getPortList() throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("getPortList()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR_PORT + " Get"
				+ ", Method=getPortList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MonitorSettingControllerBean().getPortList();
	}

	/**
	 * プロセス監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getProcessList() throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("getProcessList()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR_PROCESS + " Get"
				+ ", Method=getProcessList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MonitorSettingControllerBean().getProcessList();
	}

	/**
	 * SNMPトラップ監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getTrapList() throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("getTrapList()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR_SNMPTRAP + " Get"
				+ ", Method=getTrapList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MonitorSettingControllerBean().getTrapList();
	}

	/**
	 * SNMP監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getSnmpList() throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("getSnmpList()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR_SNMP + " Get"
				+ ", Method=getSnmpList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MonitorSettingControllerBean().getSnmpList();
	}

	/**
	 * SQL監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getSqlList() throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("getSqlList()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR_SQL + " Get"
				+ ", Method=getSqlList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MonitorSettingControllerBean().getSqlList();
	}

	/**
	 * システムログ監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getSystemlogList() throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("getSystemlogList()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR_SYSTEMLOG + " Get"
				+ ", Method=getSystemlogList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MonitorSettingControllerBean().getSystemlogList();
	}

	/**
	 * コマンド監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getCustomList() throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("getCustomList()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR_CUSTOM + " Get"
				+ ", Method=getCustomList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MonitorSettingControllerBean().getCustomList();
	}

	/**
	 * Windowsサービス監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @param condition フィルタ条件
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getWinServiceList() throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("getWinServiceList()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR_WINSERVICE + " Get"
				+ ", Method=getWinServiceList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MonitorSettingControllerBean().getWinServiceList();
	}

	/**
	 * Windowsイベント監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @param condition フィルタ条件
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getWinEventList() throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("getWinEventList()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR_WINEVENT + " Get"
				+ ", Method=getWinEventList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MonitorSettingControllerBean().getWinEventList();
	}

	/**
	 * カスタムトラップ監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public ArrayList<MonitorInfo> getCustomTrapList() throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("getCustomList()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR_CUSTOMTRAP + " Get"
				+ ", Method=getCustomList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MonitorSettingControllerBean().getCustomTrapList();
	}

	/**
	 * 収集項目コードの一覧を取得します
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @return 収集項目IDをキーとしCollectorItemTreeItemが格納されているHashMap
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public HashMapInfo getItemCodeMap() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getItemCodeMap");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		HashMapInfo info = new HashMapInfo();
		info.getMap2().putAll(new PerformanceControllerBean().getItemCodeMap());
		return info;
	}
	
	/**
	 * 指定のファシリティで収集可能な項目のリストを返します
	 * デバイス別の収集項目があり、ノード毎に登録されているデバイス情報が異なるため、
	 * 取得可能な収集項目はファシリティ毎に異なる。
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @param facilityId ファシリティID
	 * @return 指定のファシリティで収集可能な項目のリスト
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	public List<CollectorItemInfo> getAvailableCollectorItemList(String facilityId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getAvailableCollectorItemList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		return new PerformanceControllerBean().getAvailableCollectorItemList(facilityId);
	}
	
	/**
	 * 監視種別からタイトルを取得する
	 *
	 * @param type 監視種別
	 * @return タイトル
	 */
	private String monitorTypeToTitle(String type){
		if (HinemosModuleConstant.MONITOR_AGENT.equals(type)) {
			// Hinemosエージェント監視
			return HinemosModuleConstant.LOG_PREFIX_MONITOR_AGENT;
		} else if (HinemosModuleConstant.MONITOR_HTTP_N.equals(type)
				|| HinemosModuleConstant.MONITOR_HTTP_S.equals(type)) {
			// HTTP監視
			return HinemosModuleConstant.LOG_PREFIX_MONITOR_HTTP;
		} else if (HinemosModuleConstant.MONITOR_PING.equals(type)) {
			// PING監視
			return HinemosModuleConstant.LOG_PREFIX_MONITOR_PING;
		} else if (HinemosModuleConstant.MONITOR_PROCESS.equals(type)) {
			// プロセス監視
			return HinemosModuleConstant.LOG_PREFIX_MONITOR_PROCESS;
		} else if (HinemosModuleConstant.MONITOR_SNMP_N.equals(type)
				|| HinemosModuleConstant.MONITOR_SNMP_S.equals(type)) {
			// SNMP監視
			return HinemosModuleConstant.LOG_PREFIX_MONITOR_SNMP;
		} else if (HinemosModuleConstant.MONITOR_SNMPTRAP.equals(type)) {
			// SNMPTRAP監視
			return HinemosModuleConstant.LOG_PREFIX_MONITOR_SNMPTRAP;
		} else if (HinemosModuleConstant.MONITOR_SQL_N.equals(type)
				|| HinemosModuleConstant.MONITOR_SQL_S.equals(type)) {
			// SQL監視
			return HinemosModuleConstant.LOG_PREFIX_MONITOR_SQL;
		} else if (HinemosModuleConstant.MONITOR_PERFORMANCE.equals(type)) {
			// リソース監視
			return HinemosModuleConstant.LOG_PREFIX_MONITOR_PERFORMANCE;
		} else if (HinemosModuleConstant.MONITOR_PORT.equals(type)) {
			// サービス・ポート監視
			return HinemosModuleConstant.LOG_PREFIX_MONITOR_PORT;
		} else if (HinemosModuleConstant.MONITOR_CUSTOM_N.equals(type)
				|| HinemosModuleConstant.MONITOR_CUSTOM_S.equals(type)) {
			// コマンド監視
			return HinemosModuleConstant.LOG_PREFIX_MONITOR_CUSTOM;
		} else if (HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(type)) {
			// システムログ監視
			return HinemosModuleConstant.LOG_PREFIX_MONITOR_SYSTEMLOG;
		} else if (HinemosModuleConstant.MONITOR_LOGFILE.equals(type)) {
			// ログファイル監視 */
			return HinemosModuleConstant.LOG_PREFIX_MONITOR_LOGFILE;
		} else if (HinemosModuleConstant.MONITOR_WINSERVICE.equals(type)) {
			// Windowsサービス監視
			return HinemosModuleConstant.LOG_PREFIX_MONITOR_WINSERVICE;
		} else if (HinemosModuleConstant.MONITOR_CUSTOMTRAP_N.equals(type)
			|| HinemosModuleConstant.MONITOR_CUSTOMTRAP_S.equals(type)) {
			// カスタムトラップ監視
			return HinemosModuleConstant.LOG_PREFIX_MONITOR_CUSTOMTRAP;
		}
		return "";
	}

	/**
	 * 監視種別からメソッド名を取得する
	 *
	 * @param type 監視種別
	 * @return タイトル
	 */
	private String monitorTypeToMethod(String type){
		if (HinemosModuleConstant.MONITOR_AGENT.equals(type)) {
			// Hinemosエージェント監視
			return "Agent";
		} else if (HinemosModuleConstant.MONITOR_HTTP_N.equals(type)
				|| HinemosModuleConstant.MONITOR_HTTP_S.equals(type)) {
			// HTTP監視
			return "Http";
		} else if (HinemosModuleConstant.MONITOR_PING.equals(type)) {
			// PING監視
			return "Ping";
		} else if (HinemosModuleConstant.MONITOR_PROCESS.equals(type)) {
			// プロセス監視
			return "Process";
		} else if (HinemosModuleConstant.MONITOR_SNMP_N.equals(type)
				|| HinemosModuleConstant.MONITOR_SNMP_S.equals(type)) {
			// SNMP監視
			return "Snmp";
		} else if (HinemosModuleConstant.MONITOR_SNMPTRAP.equals(type)) {
			// SNMPTRAP監視
			return "Snmptrap";
		} else if (HinemosModuleConstant.MONITOR_SQL_N.equals(type)
				|| HinemosModuleConstant.MONITOR_SQL_S.equals(type)) {
			// SQL監視
			return "Sql";
		} else if (HinemosModuleConstant.MONITOR_PERFORMANCE.equals(type)) {
			// リソース監視
			return "Performance";
		} else if (HinemosModuleConstant.MONITOR_PORT.equals(type)) {
			// サービス・ポート監視
			return "Port";
		} else if (HinemosModuleConstant.MONITOR_CUSTOM_N.equals(type)
				|| HinemosModuleConstant.MONITOR_CUSTOM_S.equals(type)) {
			// コマンド監視
			return "Custom";
		} else if (HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(type)) {
			// システムログ監視
			return "Systemlog";
		} else if (HinemosModuleConstant.MONITOR_LOGFILE.equals(type)) {
			// ログファイル監視
			return "Logfile";
		} else if (HinemosModuleConstant.MONITOR_WINSERVICE.equals(type)) {
			// Windowsサービス監視
			return "WinService";
		}
		return "";
	}
}
