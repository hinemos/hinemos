/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.ws.hub;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.LogFormatDuplicate;
import com.clustercontrol.fault.LogFormatKeyPatternDuplicate;
import com.clustercontrol.fault.LogFormatNotFound;
import com.clustercontrol.fault.LogFormatUsed;
import com.clustercontrol.fault.LogTransferDuplicate;
import com.clustercontrol.fault.LogTransferNotFound;
import com.clustercontrol.hub.bean.StringQueryInfo;
import com.clustercontrol.hub.bean.StringQueryResult;
import com.clustercontrol.hub.bean.TransferInfoDestTypeMst;
import com.clustercontrol.hub.model.LogFormat;
import com.clustercontrol.hub.model.TransferInfo;
import com.clustercontrol.hub.session.HubControllerBean;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * 収集蓄積機能用のWebAPIエンドポイント
 */
@javax.jws.WebService(targetNamespace = "http://hub.ws.clustercontrol.com")
public class HubEndpoint {
	@Resource
	WebServiceContext wsctx;
	
	private static Logger m_log = Logger.getLogger( HubEndpoint.class );
	private static Logger m_opelog = Logger.getLogger("HinemosOperation");

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
	 * 引数で指定したログフォーマットIDに対応するログフォーマットを取得します。<BR>
	 * @param id
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public LogFormat getLogFormat(String id) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getLogFormat");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FormatID=");
		msg.append(id);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_HUB
				+ " Get LogFormat, Method=getLogFormat, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new HubControllerBean().getLogFormat(id);
	}
	
	/**
	 * オーナーロールIDを条件としてログフォーマットの ID 一覧を取得します。<BR> 
	 * @param ownerRoleId
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<String> getLogFormatIdList(String ownerRoleId) throws InvalidUserPass, InvalidRole, HinemosUnknown{

		m_log.debug("getLogFormatIdList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_HUB
				+ " Get LogFormat, Method=getLogFormatList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new HubControllerBean().getLogFormatIdList(ownerRoleId);
	}
	
	public List<LogFormat> getLogFormatList() throws InvalidUserPass, InvalidRole, HinemosUnknown{

		m_log.debug("getLogFormatList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_HUB
				+ " Get LogFormat, Method=getLogFormatList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new HubControllerBean().getLogFormatList();
	}
	
	/**
	 * オーナーロールIDを条件としてログフォーマット一覧を取得します。<BR> 
	 * 
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public List<LogFormat> getLogFormatListByOwnerRole(String ownerRoleId) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getLogFormatListByOwnerRole");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_HUB
				+ " Get LogFormat, Method=getLogFormatListByOwnerRole, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		return new HubControllerBean().getLogFormatListByOwnerRole(ownerRoleId);
	}
	
	/**
	 * ログ[フォーマット]情報を登録します。<BR>
	 * 引数のDTOの内容をマネージャに登録します。
	 * 
	 * @param format
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void addLogFormat(LogFormat format) throws LogFormatDuplicate, LogFormatKeyPatternDuplicate, InvalidSetting, InvalidUserPass, InvalidRole, HinemosUnknown{
		m_log.debug("addLogFormat " + format);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(format != null){
			msg.append(", FormatID=");
			msg.append(format.getLogFormatId());
		}
		try {
			new HubControllerBean().addLogFormat(format);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_HUB
					+ " Add LogFormat Failed, Method=addLogFormat, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_HUB
				+ " Add LogFormat, Method=addLogFormat, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	
	/**
	 * ログ[フォーマット]情報を変更します。<BR>
	 * 引数のプロパティの内容で更新します。
	 * 
	 * @param format
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void modifyLogFormat(LogFormat format) throws LogFormatNotFound, LogFormatKeyPatternDuplicate, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown{
		m_log.debug("modifyLogFormat " + format);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(format != null){
			msg.append(", FormatID=");
			msg.append(format.getLogFormatId());
		}

		try {
			new HubControllerBean().modifyLogFormat(format);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_HUB
					+ " Change LogFormat Failed, Method=modifyLogFormat, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_HUB
				+ " Change LogFormat, Method=modifyLogFormat, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	
	/**
	 * ログフォーマット情報を 削除します。<BR>
	 * 引数のIDに対応するログフォーマットを削除します。
	 * 
	 * @param formatIdList
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void deleteLogFormat(List<String> formatIdList) throws LogFormatNotFound, LogFormatUsed, InvalidUserPass, InvalidRole, HinemosUnknown{
		m_log.debug("deleteLogFormat " + formatIdList);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", LogFormatID=");
		msg.append(formatIdList);

		try {
			new HubControllerBean().deleteLogFormat(formatIdList);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_HUB
					+ " Delete LogFormat Failed, Method=deleteLogFormat, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_HUB
				+ " Delete LogFormat, Method=deleteLogFormat, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	
	/**
	 * 引数で指定した受け渡し設定IDに対応する受け渡し設定を取得します。<BR>
	 * @param id
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public TransferInfo getTransferInfo(String id) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getLogTransfer");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HUB, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", TransferID=");
		msg.append(id);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_HUB
				+ " Get LogTransfer, Method=getLogTransfer, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new HubControllerBean().getTransferInfo(id);
	}
	
	/**
	 * オーナーロールIDを条件として受け渡し設定 ID 一覧を取得します。<BR> 
	 * @param ownerRoleId
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<String> getTransferInfoIdList(String ownerRoleId) throws InvalidUserPass, InvalidRole, HinemosUnknown{

		m_log.debug("getLogTransferIdList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HUB, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_HUB
				+ " Get LogTransfer, Method=getLogTransferIdList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new HubControllerBean().getTransferInfoIdList(ownerRoleId);
	}
	/**
	 * 受け渡し設定 ID 一覧を取得します。<BR> 
	 * @return 
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<TransferInfo> getTransferInfoList() throws InvalidUserPass, InvalidRole, HinemosUnknown{

		m_log.debug("getLogTransferList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HUB, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_HUB
				+ " Get LogTransfer, Method=getLogTransferList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new HubControllerBean().getTransferInfoList();
	}
	
	/**
	 * オーナーロールIDを条件として受け渡し設定一覧を取得します。<BR> 
	 * 
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public List<TransferInfo> getTransferInfoListByOwnerRole(String ownerRoleId) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getLogTransferListByOwnerRole");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HUB, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_HUB
				+ " Get LogTransfer, Method=getLogTransferListByOwnerRole, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		return new HubControllerBean().getTransferInfoListByOwnerRole(ownerRoleId);
	}
	/**
	 * 収集蓄積[転送]情報を登録します。<BR>
	 * 引数のDTOの内容をマネージャに登録します。
	 * 
	 * @param transferInfo
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void addTransferInfo(TransferInfo transferInfo) throws LogTransferDuplicate, InvalidSetting, InvalidUserPass, InvalidRole, HinemosUnknown{
		m_log.debug("addLogTransfer " + transferInfo);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HUB, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(transferInfo != null){
			msg.append(", TransferID=");
			msg.append(transferInfo.getTransferId());
		}
		try {
			new HubControllerBean().addTransferInfo(transferInfo);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_HUB
					+ " Add LogTransfer Failed, Method=addLogTransfer, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_HUB
				+ " Add LogTransfer, Method=addLogTransfer, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	
	/**
	 * 収集蓄積[転送]情報を変更します。<BR>
	 * 引数のプロパティの内容で更新します。
	 * 
	 * @param transferInfo
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void modifyTransferInfo(TransferInfo transferInfo) throws LogTransferNotFound, InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown{
		m_log.debug("modifyLogTransfer " + transferInfo);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HUB, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(transferInfo != null){
			msg.append(", TransferID=");
			msg.append(transferInfo.getTransferId());
		}

		try {
			new HubControllerBean().modifyTransferInfo(transferInfo);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_HUB
					+ " Change LogTransfer Failed, Method=modifyLogTransfer, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_HUB
				+ " Change LogTransfer, Method=modifyLogTransfer, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	
	/**
	 * 収集蓄積[転送]情報を 削除します。<BR>
	 * 引数のIDに対応するログフォーマットを削除します。
	 * 
	 * @param transferIdList
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void deleteTransferInfo(List<String> transferIdList) throws LogTransferNotFound, InvalidUserPass, InvalidRole, HinemosUnknown{
		m_log.debug("deleteLogTransfer " + transferIdList);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HUB, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", TransferID=");
		msg.append(transferIdList);

		try {
			new HubControllerBean().deleteTransferInfo(transferIdList);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_HUB
					+ " Delete LogTransfer Failed, Method=deleteLogTransfer, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_HUB
				+ " Delete LogTransfer, Method=deleteLogTransfer, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * 転送先種別リストを取得する
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<TransferInfoDestTypeMst> getTransferInfoDestTypeMstList() throws InvalidUserPass, InvalidRole, HinemosUnknown{
		m_log.debug("getLogTransferDestTypeMstList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HUB, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_HUB
				+ " Get LogTransferDestTypeMst, Method=getLogTransferDestTypeMstList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new HubControllerBean().getTransferInfoDestTypeMstList();
	}
	
	/**
	 * 文字列収集情報を検索する。
	 * 
	 * @param query
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 */
	public StringQueryResult queryCollectStringData(StringQueryInfo query) throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		m_log.debug("queryCollectStringData");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HUB, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_HUB
				+ " Get LogTransferDestTypeMst, Method=queryCollectStringData, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new HubControllerBean().queryCollectStringData(query);
	}
}