/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
import com.clustercontrol.fault.HinemosPropertyDuplicate;
import com.clustercontrol.fault.HinemosPropertyNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.maintenance.model.HinemosPropertyInfo;
import com.clustercontrol.maintenance.session.HinemosPropertyControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * 共通設定用のWebAPIエンドポイント
 */
@javax.jws.WebService(targetNamespace = "http://maintenance.ws.clustercontrol.com")
public class HinemosPropertyEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( HinemosPropertyEndpoint.class );
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
	 * 共通設定情報を追加します。
	 *
	 * HinemosPropertyAdd権限が必要
	 *
	 * @throws HinemosPropertyDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 *
	 */
	public void addHinemosProperty(HinemosPropertyInfo info) throws HinemosUnknown, HinemosPropertyDuplicate, InvalidUserPass, InvalidRole,InvalidSetting
	{
		m_log.debug("addHinemosProperty");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MAINTENANCE, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList, true);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", key=");
			msg.append(info.getKey());
		}

		try {
			new HinemosPropertyControllerBean().addHinemosProperty(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Add Failed, Method=addHinemosProperty, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Add, Method=addHinemosProperty, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * 共通設定情報を変更します。
	 *
	 * HinemosPropertyWrite権限が必要
	 *
	 * @throws NotifyNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 */
	public void modifyHinemosProperty(HinemosPropertyInfo info) throws HinemosUnknown, HinemosPropertyNotFound, InvalidUserPass, InvalidRole,InvalidSetting
	{
		m_log.debug("modifyHinemosProperty");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MAINTENANCE, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList, true);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", key=");
			msg.append(info.getKey());
		}

		try {
			new HinemosPropertyControllerBean().modifyHinemosProperty(info);;
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Change Failed, Method=modifyHinemosProperty, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Change, Method=modifyHinemosProperty, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * 共通設定情報を削除します。
	 *
	 * HinemosPropertyWrite権限が必要
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 */
	public void deleteHinemosProperty(String key) throws HinemosUnknown, HinemosPropertyNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("deleteHinemosProperty");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MAINTENANCE, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList, true);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", key=");
		msg.append(key);

		try {
			new HinemosPropertyControllerBean().deleteHinemosProperty(key);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Delete Failed, Method=deleteHinemosProperty, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Delete, Method=deleteHinemosProperty, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * 共通設定情報を取得します。
	 *
	 * HinemosPropertyRead権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 */
	public HinemosPropertyInfo getHinemosProperty(String key) throws HinemosPropertyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getHinemosProperty");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MAINTENANCE, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", key=");
		msg.append(key);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Get, Method=getHinemosProperty, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new HinemosPropertyControllerBean().getHinemosPropertyInfo(key);
	}

	/**
	 * 共通設定情報の一覧を取得します。<BR>
	 *
	 * HinemosPropertyRead権限が必要
	 *
	 * @return 共通設定情報の一覧を保持するArrayList
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 */
	public ArrayList<HinemosPropertyInfo> getHinemosPropertyList() throws  HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getHinemosPropertyList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MAINTENANCE, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MAINTENANCE + " Get, Method=getHinemosPropertyList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new HinemosPropertyControllerBean().getHinemosPropertyList();
	}

	/**
	 * 現在のHinemos時刻を返します。<br/>
	 * システム権限は不要ですが、ユーザとパスワードのチェックは行います。
	 * 
	 * @return UTC 1970年1月1日0時からの経過ミリ秒により表現されるHinemos時刻。
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public long getHinemosTime() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getHinemosTime");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		try {
			return HinemosTime.currentTimeMillis();
		} catch (Throwable t) {
			m_log.warn("Error.", t);
			throw new HinemosUnknown("Failed to retrieve HinemosTime.");
		}
	}
}