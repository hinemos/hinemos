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
package com.clustercontrol.ws.maintenance;

import java.util.ArrayList;

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
import com.clustercontrol.fault.MaintenanceDuplicate;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.maintenance.model.MaintenanceTypeMst;
import com.clustercontrol.maintenance.session.MaintenanceControllerBean;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * メンテナンス用のWebAPIエンドポイント
 */
@javax.jws.WebService(targetNamespace = "http://maintenance.ws.clustercontrol.com")
public class MaintenanceEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( MaintenanceEndpoint.class );
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
	 * メンテナンス情報を追加します。
	 * 
	 * MaintenanceAdd権限が必要
	 * 
	 * @throws MaintenanceDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * 
	 */
	public void addMaintenance(MaintenanceInfo info) throws HinemosUnknown, MaintenanceDuplicate, InvalidUserPass, InvalidRole,InvalidSetting
	{
		m_log.debug("addMaintenance");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MAINTENANCE, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", MaintenanceID=");
			msg.append(info.getMaintenanceId());
		}

		try {
			new MaintenanceControllerBean().addMaintenance(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Add Failed, Method=addMaintenance, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Add, Method=addMaintenance, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * メンテナンス情報を変更します。
	 * 
	 * MaintenanceWrite権限が必要
	 * 
	 * @throws MaintenanceNotFound
	 * @throws NotifyNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 */
	public void modifyMaintenance(MaintenanceInfo info) throws HinemosUnknown, NotifyNotFound, MaintenanceNotFound, InvalidUserPass, InvalidRole,InvalidSetting
	{
		m_log.debug("modifyMaintenance");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MAINTENANCE, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", MaintenanceID=");
			msg.append(info.getMaintenanceId());
		}

		try {
			new MaintenanceControllerBean().modifyMaintenance(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Change Failed, Method=modifyMaintenance, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Change, Method=modifyMaintenance, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * メンテナンス情報を削除します。
	 * 
	 * MaintenanceWrite権限が必要
	 * 
	 * @throws MaintenanceNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 */
	public void deleteMaintenance(String maintenanceId) throws HinemosUnknown, MaintenanceNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("deleteMaintenance");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MAINTENANCE, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", MaintenanceID=");
		msg.append(maintenanceId);

		try {
			new MaintenanceControllerBean().deleteMaintenance(maintenanceId);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Delete Failed, Method=deleteMaintenance, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Delete, Method=deleteMaintenance, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * メンテナンス情報を取得します。
	 *
	 * MaintenanceRead権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws MaintenanceNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 */
	public MaintenanceInfo getMaintenanceInfo(String maintenanceId) throws MaintenanceNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getMaintenanceInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MAINTENANCE, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", MaintenanceID=");
		msg.append(maintenanceId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Get, Method=getMaintenanceInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new MaintenanceControllerBean().getMaintenanceInfo(maintenanceId);
	}

	/**
	 * メンテナンス情報の一覧を取得します。<BR>
	 * 
	 * MaintenanceRead権限が必要
	 * 
	 * @return メンテナンス情報の一覧を保持するArrayList
	 * @throws MaintenanceNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 */
	public ArrayList<MaintenanceInfo> getMaintenanceList() throws HinemosUnknown, MaintenanceNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("getMaintenanceList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MAINTENANCE, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Get, Method=getMaintenanceList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MaintenanceControllerBean().getMaintenanceList();
	}

	/**
	 * メンテナンス種別の一覧を取得します。<BR>
	 * 
	 * MaintenanceRead権限が必要
	 * 
	 * @return メンテナンス種別の一覧を保持するArrayList
	 * @throws HinemosUnknown
	 * @throws MaintenanceNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<MaintenanceTypeMst> getMaintenanceTypeList() throws MaintenanceNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getMaintenanceTypeList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MAINTENANCE, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Get, Method=getMaintenanceTypeList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new MaintenanceControllerBean().getMaintenanceTypeList();
	}

	/**
	 * メンテナンスの有効、無効を変更するメソッドです。
	 * 
	 * MaintenanceWrite権限が必要
	 * 
	 * @throws HinemosUnknown
	 * @throws MaintenanceNotFound
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 */
	public void setMaintenanceStatus(String maintenanceId, boolean validFlag) throws NotifyNotFound, MaintenanceNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("setMaintenanceStatus");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MAINTENANCE, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", MaintenanceID=");
		msg.append(maintenanceId);
		msg.append(", ValidFlag=");
		msg.append(validFlag);

		try {
			new MaintenanceControllerBean().setMaintenanceStatus(maintenanceId, validFlag);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Change Valid Failed, Method=setMaintenanceStatus, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Change Valid, Method=setMaintenanceStatus, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

	}
}