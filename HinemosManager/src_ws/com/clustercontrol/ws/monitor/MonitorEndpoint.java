/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.monitor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.EventLogNotFound;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.bean.EventBatchConfirmInfo;
import com.clustercontrol.monitor.bean.EventCustomCommandInfoData;
import com.clustercontrol.monitor.bean.EventCustomCommandResultRoot;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.monitor.bean.EventDisplaySettingInfo;
import com.clustercontrol.monitor.bean.EventFilterInfo;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.bean.ScopeDataInfo;
import com.clustercontrol.monitor.bean.StatusDataInfo;
import com.clustercontrol.monitor.bean.StatusFilterInfo;
import com.clustercontrol.monitor.bean.ViewListInfo;
import com.clustercontrol.monitor.run.util.EventUtil;
import com.clustercontrol.monitor.session.EventCustomCommandBean;
import com.clustercontrol.monitor.session.MonitorControllerBean;
import com.clustercontrol.monitor.util.EventHinemosPropertyUtil;
import com.clustercontrol.repository.bean.FacilityTargetConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * 監視用のWebAPIエンドポイント
 */
@MTOM
@javax.jws.WebService(targetNamespace = "http://monitor.ws.clustercontrol.com")
@XmlSeeAlso({
	EventDataInfo.class
})
public class MonitorEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( MonitorEndpoint.class );
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
	 * 引数で指定された条件に一致するイベント一覧情報を取得します。(クライアントview用)<BR><BR>
	 * 
	 * 各イベント情報は、EventDataInfoインスタンスとして保持されます。<BR>
	 * 戻り値のViewListInfoは、クライアントにて表示用の形式に変換されます。
	 * 
	 * MonitorResultRead権限が必要
	 * 
	 * @param facilityId 取得対象の親ファシリティID
	 * @param filter 検索条件
	 * @param messages 表示イベント数
	 * @return ビュー一覧情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 * @see com.clustercontrol.monitor.bean.EventDataInfo
	 * @see com.clustercontrol.monitor.factory.SelectEvent#getEventList(String, EventFilterInfo, int)
	 */
	public ViewListInfo getEventList(String facilityId, EventFilterInfo filter, int messages) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getEventList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_RESULT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// 認証済み操作ログ
		SimpleDateFormat sdf = getSdf();
		StringBuilder msg = new StringBuilder();
		msg.append(", FacilityID=");
		msg.append(facilityId);
		if (filter != null) {
			setEventFilterMessage(msg, filter, sdf);
		}
		msg.append(", Count=");
		msg.append(messages);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Get, Method=getEventList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new MonitorControllerBean().getEventList(facilityId, filter, messages);
	}

	/**
	 * スコープ情報一覧を取得します。<BR><BR>
	 * 引数で指定されたファシリティの配下全てのファシリティのスコープ情報一覧を返します。<BR>
	 * 各スコープ情報は、ScopeDataInfoのインスタンスとして保持されます。<BR>
	 * 
	 * MonitorResultRead権限が必要
	 * 
	 * @param facilityId 取得対象の親ファシリティID
	 * @param statusFlag
	 * @param eventFlag
	 * @param orderFlg
	 * @return スコープ情報一覧（ScopeDataInfoが格納されたArrayList）
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws FacilityNotFound
	 * @throws MonitorNotFound
	 * 
	 */
	public ArrayList<ScopeDataInfo> getScopeList(String facilityId, boolean statusFlag, boolean eventFlag, boolean orderFlg) throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound, FacilityNotFound {
		m_log.debug("getScopeList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_RESULT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityID=");
		msg.append(facilityId);
		msg.append(", StatusFlag=");
		msg.append(statusFlag);
		msg.append(", EventFlag=");
		msg.append(eventFlag);
		msg.append(", OrderFlag=");
		msg.append(orderFlg);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Get, Method=getScopeList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new MonitorControllerBean().getScopeList(facilityId, statusFlag, eventFlag, orderFlg);
	}

	/**
	 * 引数で指定された条件に一致するステータス情報一覧を取得します。<BR>
	 * 各ステータス情報は、StatusDataInfoのインスタンスとして保持されます。<BR>
	 * 
	 * MonitorResultRead権限が必要
	 * 
	 * @param facilityId 取得対象の親ファシリティID
	 * @param property 検索条件
	 * @return ステータス情報一覧（StatusDataInfoが格納されたArrayList）
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 * @see com.clustercontrol.monitor.bean.StatusDataInfo
	 * @see com.clustercontrol.monitor.factory.SelectStatus#getStatusList(String, StatusFilterInfo)
	 */
	public ArrayList<StatusDataInfo> getStatusList(String facilityId, StatusFilterInfo filter) throws InvalidUserPass, InvalidRole, HinemosUnknown
	{
		m_log.debug("getStatusList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_RESULT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityID=");
		msg.append(facilityId);
		if (filter != null) {
			// 重要度リストの文字列化
			StringBuilder priorityMsg = new StringBuilder();
			if(filter.getPriorityList() != null) {
				for(int i = 0; i<filter.getPriorityList().length; i++) {
					priorityMsg.append(Messages.getString(PriorityConstant.typeToMessageCode(filter.getPriorityList()[i]), Locale.ENGLISH)).append(" ");
				}
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			sdf.setTimeZone(HinemosTime.getTimeZone());
			msg.append(", Priority=");
			msg.append(priorityMsg);
			msg.append(", OutputDateFrom=");
			msg.append(filter.getOutputDateFrom()==null?null:sdf.format(new Date(filter.getOutputDateFrom())));
			msg.append(", OutputDateTo=");
			msg.append(filter.getOutputDateTo()==null?null:sdf.format(new Date(filter.getOutputDateTo())));
			msg.append(", GenerationDateFrom=");
			msg.append(filter.getGenerationDateFrom()==null?null:sdf.format(new Date(filter.getGenerationDateFrom())));
			msg.append(", GenerationDateTo=");
			msg.append(filter.getGenerationDateTo()==null?null:sdf.format(new Date(filter.getGenerationDateTo())));
			msg.append(", FacilityType=");
			msg.append(filter.getFacilityType());
			msg.append(", Application=");
			msg.append(filter.getApplication());
			msg.append(", Message=");
			msg.append(filter.getMessage());
			msg.append(", OutputDate=");
			msg.append(filter.getOutputDate()==null?null:sdf.format(new Date(filter.getOutputDate())));
			msg.append(", GenerationDate=");
			msg.append(filter.getGenerationDate()==null?null:sdf.format(new Date(filter.getGenerationDate())));
			msg.append(", OwnerRoleId=");
			msg.append(filter.getOwnerRoleId());
		}
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Get, Method=getStatusList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new MonitorControllerBean().getStatusList(facilityId, filter);
	}

	/**
	 * 引数で指定されたステータス情報を削除します。<BR>
	 * 引数のlistは、StatusDataInfoが格納されたListとして渡されます。<BR>
	 * 
	 * MonitorResultWrite権限が必要
	 * 
	 * @param list 削除対象のステータス情報一覧（StatusDataInfoが格納されたList）
	 * @return 削除に成功した場合、</code> true </code>
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 * 
	 * @see com.clustercontrol.monitor.bean.StatusDataInfo
	 * @see com.clustercontrol.monitor.factory.DeleteStatus#delete(List)
	 */
	public boolean deleteStatus(ArrayList<StatusDataInfo> list) throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("deleteStatus");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_RESULT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;

		// 認証済み操作ログ
		if(list != null && list.size() > 0){
			StringBuffer msg = new StringBuffer();
			for (int i=0; i<list.size(); i++) {
				msg.append(", " + (i + 1) + "=(");
				msg.append("PluginID=");
				msg.append(list.get(i).getPluginId());
				msg.append(", MonitorID=");
				msg.append(list.get(i).getMonitorId());
				msg.append(", FacilityID=");
				msg.append(list.get(i).getFacilityId());
				msg.append(")");
			}

			try {
				ret = new MonitorControllerBean().deleteStatus(list);
			} catch (Exception e) {
				m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Delete Status Failed, Method=deleteStatus, User="
						+ HttpAuthenticator.getUserAccountString(wsctx)
						+ msg.toString());
				throw e;
			}
			m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Delete Status, Method=deleteStatus, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
		}

		return ret;
	}

	/**
	 * 引数で指定された条件に一致する帳票出力用イベント情報一覧を返します。<BR><BR>
	 * 
	 * MonitorResultRead権限が必要
	 * 	 *
	 * @param facilityId 取得対象の親ファシリティID
	 * @param filter 検索条件
	 * @param filename 出力ファイル名
	 * @param language ロケール
	 * @return 帳票のデータハンドラ
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 * @since 2.1.0
	 * 
	 * @see com.clustercontrol.monitor.factory.SelectEvent#getEventListForReport(String, EventFilterInfo)
	 */
	@XmlMimeType("application/octet-stream")
	public DataHandler downloadEventFile(String facilityId, EventFilterInfo filter, String filename, String language) throws HinemosUnknown, InvalidUserPass, InvalidRole
	{
		m_log.debug("downloadEventFile");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_RESULT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// 認証済み操作ログ
		SimpleDateFormat sdf = getSdf();
		StringBuilder msg = new StringBuilder();
		msg.append(", FacilityID=");
		msg.append(facilityId);
		msg.append(", FileName=");
		msg.append(filename);
		msg.append(", Locale=");
		msg.append(language);
		
		if (filter != null) {
			setEventFilterMessage(msg, filter, sdf);
		}
		DataHandler ret = null;

		try {
			ret = new MonitorControllerBean().downloadEventFile(facilityId, filter, filename, new Locale(language));
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Download Failed, Method=downloadEventFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Download, Method=downloadEventFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	public void deleteEventFile(String filename) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("deleteEventFile");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_RESULT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FileName=");
		msg.append(filename);

		try {
			new MonitorControllerBean().deleteEventFile(filename);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Delete Failed, Method=deleteEventFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Delete, Method=deleteEventFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

	}

	/**
	 * イベント詳細情報を取得します。<BR><BR>
	 * 
	 * MonitorResultRead権限が必要
	 * 
	 * @param monitorId 取得対象の監視項目ID
	 * @param monitorDetailId 取得対象の監視詳細
	 * @param pluginId 取得対象のプラグインID
	 * @param facilityId 取得対象のファシリティID
	 * @param outputDate 取得対象の受信日時
	 * @return イベント詳細情報
	 * @throws HinemosUnknown
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 */
	public EventDataInfo getEventInfo(String monitorId, String monitorDetailId, String pluginId, String facilityId, Long outputDate) throws MonitorNotFound, HinemosUnknown, InvalidUserPass, InvalidRole
	{
		m_log.debug("getEventInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_RESULT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		SimpleDateFormat sdf = getSdf();
		
		msg.append(", MonitorID=");
		msg.append(monitorId);
		msg.append(", MonitorDetailID=");
		msg.append(monitorDetailId);
		msg.append(", PluginID=");
		msg.append(pluginId);
		msg.append(", FacilityID=");
		msg.append(facilityId);
		msg.append(", OutputDate=");
		msg.append(longToDateString(outputDate, sdf));
		
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Get, Method=getEventInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new MonitorControllerBean().getEventInfo(monitorId, monitorDetailId, pluginId, facilityId, outputDate);
	}

	/**
	 * 引数で指定されたイベント情報のコメントを更新します。<BR><BR>
	 * コメント追記ユーザとして、コメントユーザを設定します。
	 * 
	 * MonitorResultWrite権限が必要
	 * 
	 * @param monitorId 更新対象の監視項目ID
	 * @param monitorDetailId 更新対象の監視詳細
	 * @param pluginId 更新対象のプラグインID
	 * @param facilityId 更新対象のファシリティID
	 * @param outputDate 更新対象の受信日時
	 * @param comment コメント
	 * @param commentDate コメント変更日時
	 * @param commentUser コメント変更ユーザ
	 * @throws EventLogNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * 
	 * @see com.clustercontrol.bean.ConfirmConstant
	 * @see com.clustercontrol.monitor.factory.ModifyEventComment#modifyComment(String, String, String, Long, String, Long, String)
	 */
	public void modifyComment(String monitorId, String monitorDetailId, String pluginId, String facilityId, Long outputDate, String comment, Long commentDate, String commentUser)
			throws HinemosUnknown, EventLogNotFound, InvalidUserPass, InvalidSetting, InvalidRole
			{
		m_log.debug("modifyComment");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_RESULT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		SimpleDateFormat sdf = getSdf();
		StringBuffer msg = new StringBuffer();
		msg.append(", MonitorID=");
		msg.append(monitorId);
		msg.append(", MonitorDetailID=");
		msg.append(monitorDetailId);
		msg.append(", PluginID=");
		msg.append(pluginId);
		msg.append(", FacilityID=");
		msg.append(facilityId);
		msg.append(", OutputDate=");
		msg.append(longToDateString(outputDate, sdf));
		msg.append(", Comment=");
		msg.append(comment);
		msg.append(", CommentDate=");
		msg.append(longToDateString(commentDate, sdf));
		msg.append(", CommentUser=");
		msg.append(commentUser);

		try {
			new MonitorControllerBean().modifyComment(monitorId, monitorDetailId, pluginId, facilityId, outputDate, comment, commentDate, commentUser);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Change, Method=modifyComment, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Change, Method=modifyComment, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
			}


	/**
	 * 引数で指定されたイベント情報一覧の確認を更新します。<BR><BR>
	 * 確認ユーザとして、操作を実施したユーザを設定します。<BR>
	 * 複数のイベント情報を更新します。
	 * 
	 * MonitorResultWrite権限が必要
	 * 
	 * @param list 更新対象のイベント情報一覧（EventDataInfoが格納されたArrayList）
	 * @param confirmType 確認タイプ（未確認／確認中／確認済）（更新値）
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 * 
	 * @see com.clustercontrol.bean.ConfirmConstant
	 * @see com.clustercontrol.monitor.factory.ModifyEventConfirm#modifyConfirm(List, int)
	 */
	public void modifyConfirm(ArrayList<EventDataInfo> list, int confirmType) throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("modifyConfirm");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_RESULT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		SimpleDateFormat sdf = getSdf();
		
		if (list != null && list.size() > 0){
			for (int i = 0; i < list.size(); i++) {
				EventDataInfo event = list.get(i);
				msg.append(", " + (i + 1) + "=(");
				setEventDataInfoMessage(msg, event, sdf);
				msg.append(", Comfirm=");
				msg.append(confirmToString(confirmType));
				msg.append(")");
			}
		}

		try {
			new MonitorControllerBean().modifyConfirm(list, confirmType);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Confirm Failed, Method=modifyConfirm, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Confirm, Method=modifyConfirm, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * 引数で指定された条件に一致するイベント情報の確認を一括更新します。<BR><BR>
	 * 確認ユーザとして、操作を実施したユーザを設定します。<BR>
	 * 
	 * MonitorResultWrite権限が必要
	 * 
	 * @param confirmType 確認タイプ（未／済）（更新値）
	 * @param facilityId 更新対象の親ファシリティID
	 * @param property 更新条件
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 * @see com.clustercontrol.bean.ConfirmConstant
	 * @see com.clustercontrol.monitor.factory.ModifyEventConfirm#modifyBatchConfirm(int, String, EventBatchConfirmInfo)
	 */
	public void modifyBatchConfirm(int confirmType, String facilityId, EventBatchConfirmInfo info) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("modifyBatchConfirm");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_RESULT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if (info != null) {
			msg.append(", FacilityID=");
			msg.append(facilityId);
			msg.append(", Priority=");
			msg.append(priorityListToString(info.getPriorityList()));
			msg.append(", Target Facility=");
			if(info.getFacilityType() != null && FacilityTargetConstant.TYPE_BENEATH == info.getFacilityType()) {
				msg.append("Sub-scope Facilities Only");
			} else {
				msg.append("ALL Facilities");
			}
			msg.append(", Application=");
			msg.append(info.getApplication());
			msg.append(", Message=");
			msg.append(info.getMessage());
		}

		try {
			new MonitorControllerBean().modifyBatchConfirm(confirmType, facilityId, info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Confirm All Failed, Method=modifyBatchConfirm, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Confirm All, Method=modifyBatchConfirm, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * 引数で指定されたイベント情報一覧の性能グラフ用フラグを更新します。<BR><BR>
	 * 複数のイベント情報を更新します。
	 * 
	 * MonitorResultWrite権限が必要
	 * 
	 * @param list 更新対象のイベント情報一覧（EventDataInfoが格納されたArrayList）
	 * @param collectGraphFlg 性能グラフ用フラグ（ON:true、OFF:false）（更新値）
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws MonitorNotFound
	 */
	public void modifyCollectGraphFlg(ArrayList<EventDataInfo> list, Boolean collectGraphFlg) throws InvalidUserPass, InvalidRole, HinemosUnknown, MonitorNotFound {
		m_log.debug("modifyCollectGraphFlg");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_RESULT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		SimpleDateFormat sdf = getSdf();
		
		if (list != null && list.size() > 0){
			for (int i = 0; i < list.size(); i++) {
				EventDataInfo event = list.get(i);
				msg.append(", " + (i + 1) + "=(");
				setEventDataInfoMessage(msg, event, sdf);
				msg.append(", CollectGraphFlg=");
				msg.append(collectGraphFlg);
				msg.append(")");
			}
		}

		try {
			new MonitorControllerBean().modifyCollectGraphFlg(list, collectGraphFlg);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MONITOR + " CollectGraphFlg update Failed, Method=modifyCollectGraphFlg, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MONITOR + " CollectGraphFlg update, Method=modifyCollectGraphFlg, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	
	/**
	 * 引数で指定されたイベント情報のコメント、確認、性能グラフ用フラグ、ユーザ拡張イベント項目を更新します。<BR><BR>
	 * 上記以外の項目は更新されません。
	 * 監視項目ID、監視詳細、プラグインID、ファシリティID、受信日時は必須項目です。
	 * 
	 * MonitorResultWrite権限が必要
	 * 
	 * @param info 更新対象のイベント情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * @throws MonitorNotFound
	 */
	public void modifyEventInfo(EventDataInfo info) throws InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown, MonitorNotFound {
		m_log.debug("modifyEventInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_RESULT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		setEventDataInfoMessage2(msg, info, getSdf());
		
		try {
			new MonitorControllerBean().modifyEventInfo(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MONITOR + " EventInfo update Failed, Method=modifyEventInfo, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MONITOR + " EventInfo update, Method=modifyEventInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * イベントの画面表示設定を取得します。<BR><BR>
	 * 
	 * @return イベント画面表示設定
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 */
	public EventDisplaySettingInfo getEventDisplaySettingInfo() throws InvalidRole, HinemosUnknown, InvalidUserPass{
		m_log.debug("getEventDisplaySettingInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_RESULT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// 認証済み操作ログ
		StringBuilder msg = new StringBuilder();
		
		EventDisplaySettingInfo retVal = null;
		
		try {
			retVal = new MonitorControllerBean().getEventDisplaySettingInfo();
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MONITOR + " EventDisplaySettingInfo get Failed, Method=getEventDisplaySettingInfo, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MONITOR + " EventDisplaySettingInfo get, Method=getEventDisplaySettingInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		
		return retVal;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * イベントカスタムコマンドの設定を取得します。<BR><BR>
	 * 
	 * @return イベントカスタムコマンド設定
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 */
	public EventCustomCommandInfoData getEventCustomCommandSettingInfo() throws InvalidRole, HinemosUnknown, InvalidUserPass{
		m_log.debug("getEventCustomCommandSettingInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_RESULT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		
		EventCustomCommandInfoData retVal = null;
		
		try {
			retVal = new EventCustomCommandBean().getEventCustomCommandSettingInfo();
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MONITOR + " EventCustomCommandSettingInfo get Failed, Method=getEventCustomCommandSettingInfo, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MONITOR + " EventCustomCommandSettingInfo get, Method=getEventCustomCommandSettingInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		
		return retVal;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * 引数で指定されたイベントNoのコマンドを指定されたイベント情報に対して実行します。<BR><BR>
	 * 
	 * MonitorResultWrite権限が必要
	 * 
	 * @param commandNo 実行するコマンド番号
	 * @param eventList コマンド実行対象のイベント
	 * @return 実行結果を確認するためのコマンド結果ID
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public String execEventCustomCommand(int commandNo, List<EventDataInfo> eventList) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("execEventCustomCommand");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_RESULT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		SimpleDateFormat sdf = getSdf();
		
		msg.append("  CommandNo=" + commandNo);
		msg.append(", EventList=");
		if (eventList == null) {
			msg.append("null");
		} else {
			for (int i = 0; i < eventList.size(); i++) {
				msg.append(", " + (i + 1) + "=(");
				setEventDataInfoMessage2(msg, eventList.get(i), sdf);
				msg.append(")");
			}
		}
		
		String retVal = null;
		
		try {
			retVal = new EventCustomCommandBean().execEventCustomCommand(commandNo, eventList);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MONITOR + " EventCustomCommand execute Failed, Method=modifyEventInfo, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MONITOR + " EventCustomCommand execute, Method=modifyEventInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		
		return retVal;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * イベントカスタムコマンドの実行結果を取得します。<BR><BR>
	 * 
	 * MonitorResultWrite権限が必要
	 * 
	 * @param commandResultID イベントカスタムコマンド実行時に出力されたコマンド結果ID
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public EventCustomCommandResultRoot getEventCustomCommandResult(String commandResultID) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getEventCustomCommandResult");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_RESULT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// 認証済み操作ログ
		StringBuilder msg = new StringBuilder();
		
		msg.append("  commandResultID=" + commandResultID);
		
		EventCustomCommandResultRoot retVal = null;
		
		try {
			retVal = new EventCustomCommandBean().getEventCustomCommandResult(commandResultID);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MONITOR + " EventCustomCommand getResult Failed, Method=getEventCustomCommandResult, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MONITOR + " EventCustomCommand getResult, Method=getEventCustomCommandResult, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		
		return retVal;
	}
	
	private void setEventFilterMessage(StringBuilder msg, EventFilterInfo filter, SimpleDateFormat sdf) {
		
		msg.append(", Priority=");
		msg.append(priorityListToString(filter.getPriorityList()));
		msg.append(", OutputDateFrom=");
		msg.append(longToDateString(filter.getOutputDateFrom(), sdf));
		msg.append(", OutputDateTo=");
		msg.append(longToDateString(filter.getOutputDateTo(), sdf));
		msg.append(", GenerationDateFrom=");
		msg.append(longToDateString(filter.getGenerationDateFrom(), sdf));
		msg.append(", GenerationDateTo=");
		msg.append(longToDateString(filter.getGenerationDateTo(), sdf));
		msg.append(", MonitorId=");
		msg.append(filter.getMonitorId());
		msg.append(", MonitorDetailId=");
		msg.append(filter.getMonitorDetailId());
		msg.append(", FacilityType=");
		msg.append(filter.getFacilityType());
		msg.append(", Application=");
		msg.append(filter.getApplication());
		msg.append(", Message=");
		msg.append(filter.getMessage());
		msg.append(", getConfirmFlgTypeList=");
		msg.append(confirmListToString(filter.getConfirmFlgTypeList()));
		msg.append(", OutputDate=");
		msg.append(longToDateString(filter.getOutputDate(), sdf));
		msg.append(", GenerationDate=");
		msg.append(longToDateString(filter.getGenerationDate(), sdf));
		msg.append(", ConfirmedUser=");
		msg.append(filter.getConfirmedUser());
		msg.append(", Comment=");
		msg.append(filter.getComment());
		msg.append(", CommentDate=");
		msg.append(longToDateString(filter.getCommentDate(), sdf));
		msg.append(", CommentUser=");
		msg.append(filter.getCommentUser());
		msg.append(", CollectGraphFlg=");
		msg.append(filter.getCollectGraphFlg());
		msg.append(", OwnerRoleId=");
		msg.append(filter.getOwnerRoleId());
		msg.append(", PositionFrom=");
		msg.append(filter.getPositionFrom());
		msg.append(", PositionTo=");
		msg.append(filter.getPositionTo());
		String format = ", " + EventHinemosPropertyUtil.DEFAULT_NAME_PATTERN + " =";
		for (int i = 1; i < EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			msg.append(String.format(format, i));
			msg.append(EventUtil.getUserItemValue(filter, i));
		}

	}
	
	private void setEventDataInfoMessage(StringBuffer msg, EventDataInfo info, SimpleDateFormat sdf) {
		
		if (info == null) {
			msg.append("  null");
			return;
		}
		
		msg.append("PluginID=");
		msg.append(info.getPluginId());
		msg.append(", FacilityID=");
		msg.append(info.getFacilityId());
		msg.append(", Priority=");
		msg.append(priorityToString(info.getPriority()));
		msg.append(", Time Received=");
		msg.append(longToDateString(info.getOutputDate(), sdf));
		msg.append(", Time Created=");
		msg.append(longToDateString(info.getGenerationDate(), sdf));
	}
	
	private void setEventDataInfoMessage2(StringBuffer msg, EventDataInfo info, SimpleDateFormat sdf) {
		
		if (info == null) {
			msg.append("  null");
			return;
		}
		
		msg.append("  MonitorID=");
		msg.append(info.getMonitorId());
		msg.append(", MonitorDetailID=");
		msg.append(info.getMonitorDetailId());
		msg.append(", PluginID=");
		msg.append(info.getPluginId());
		msg.append(", OutputDate=");
		msg.append(longToDateString(info.getOutputDate(), sdf));
		msg.append(", FacilityID=");
		msg.append(info.getFacilityId());
	}

	private static String longToDateString(Long value, SimpleDateFormat sdf) {
		if (value == null) {
			return "null";
		}
		return sdf.format(new Date(value));
	}
	
	private static SimpleDateFormat getSdf() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		sdf.setTimeZone(HinemosTime.getTimeZone());
		return sdf;
	}
	
	private static String priorityListToString(Integer[] priorityList) {
		if (priorityList == null) {
			return "null";
		}
		StringBuilder msg = new StringBuilder();
		
		for (int i = 0; i < priorityList.length; i++) {
			msg.append(priorityToString(priorityList[i]));
		}
		return msg.toString();
	}
	
	private static String priorityToString(Integer priority) {
		if (priority == null) {
			return "null";
		}
		return Messages.getString(PriorityConstant.typeToMessageCode(priority), Locale.ENGLISH) + " ";
	}
	
	private static String confirmListToString(Integer[] confirmList) {
		if (confirmList == null) {
			return "null";
		}
		StringBuilder msg = new StringBuilder();
		
		for(int i = 0; i < confirmList.length; i++) {
			msg.append(confirmToString(confirmList[i]));
		}
		return msg.toString();
	}
	
	private static String confirmToString(Integer confirm) {
		if (confirm == null) {
			return "null";
		}
		return Messages.getString(ConfirmConstant.typeToMessageCode(confirm), Locale.ENGLISH) + " ";
	}
}
