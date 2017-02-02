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
package com.clustercontrol.ws.collectmaster;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.performance.bean.CollectMasterInfo;
import com.clustercontrol.performance.session.PerformanceCollectMasterControllerBean;
import com.clustercontrol.repository.entity.CollectorPlatformMstData;
import com.clustercontrol.repository.entity.CollectorSubPlatformMstData;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * 収集項目マスタ用のWebAPIエンドポイント
 */
@javax.jws.WebService(targetNamespace = "http://collectmaster.ws.clustercontrol.com")
public class PerformanceCollectMasterEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( PerformanceCollectMasterEndpoint.class );
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
	 * 収集項目マスタデータを一括で登録します。
	 * @param collectMasterInfo
	 * @return 登録に成功した場合、true
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public boolean addCollectMaster(CollectMasterInfo collectMasterInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("addCollectMaster");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList, true);

		boolean ret = false;

		// 認証済み操作ログ
		try {
			ret = new PerformanceCollectMasterControllerBean().addCollectMaster(collectMasterInfo);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Add Master Failed, Method=addCollectMaster, User="
					+ HttpAuthenticator.getUserAccountString(wsctx));
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Add Master, Method=addCollectMaster, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return ret;
	}

	/**
	 * 収集項目のマスタ情報を全て削除します。
	 * 
	 * @return 削除に成功した場合、true
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public boolean deleteCollectMasterAll() throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("deleteCollectMasterAll");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList, true);

		boolean ret = false;

		// 認証済み操作ログ
		try {
			ret = new PerformanceCollectMasterControllerBean().deleteCollectMasterAll();
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Delete Master Failed, Method=deleteCollectMasterAll, User="
					+ HttpAuthenticator.getUserAccountString(wsctx));
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Delete Master, Method=deleteCollectMasterAll, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return ret;
	}

	/**
	 * 収集マスタ情報を一括で取得します。
	 * 
	 * @return 収集マスタ情報
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public CollectMasterInfo getCollectMasterInfo() throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getCollectMasterInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList, true);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Get Master, Method=getCollectMasterInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new PerformanceCollectMasterControllerBean().getCollectMasterInfo();
	}

	/**
	 * プラットフォーム定義を登録します。
	 * 
	 * @param data プラットフォーム定義情報
	 * @return 登録に成功した場合、true
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean addCollectPlatformMaster(CollectorPlatformMstData data) throws  HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("addCollectPlatformMaster");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList, true);

		boolean ret = false;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", PlatformId=");
		msg.append(data.getPlatformId());

		try {
			ret = new PerformanceCollectMasterControllerBean().addCollectPlatformMaster(data);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Add Platform Failed, Method=addCollectPlatformMaster, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Add Platform, Method=addCollectPlatformMaster, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * サブプラットフォーム定義を登録します。
	 * 
	 * @param data サブプラットフォーム定義情報
	 * @return 登録に成功した場合、true
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean addCollectSubPlatformMaster(CollectorSubPlatformMstData data) throws  HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("addCollectSubPlatformMaster");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList, true);

		boolean ret = false;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SubPlatformId=");
		msg.append(data.getSubPlatformId());

		try {
			ret = new PerformanceCollectMasterControllerBean().addCollectSubPlatformMaster(data);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Add SubPlatform Failed, Method=addCollectSubPlatformMaster, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Add SubPlatform, Method=addCollectSubPlatformMaster, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * プラットフォーム定義を削除します。
	 * 
	 * @param platformId プラットフォームID
	 * @return 削除に成功した場合、true
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean deleteCollectPlatformMaster(String platformId) throws  HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("deleteCollectPlatformMaster");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList, true);

		boolean ret = false;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", PlatformId=");
		msg.append(platformId);

		try {
			ret = new PerformanceCollectMasterControllerBean().deleteCollectPlatformMaster(platformId);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Delete Platform Failed, Method=deleteCollectPlatformMaster, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Delete Platform, Method=deleteCollectPlatformMaster, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * サブプラットフォーム定義を削除します。
	 * 
	 * @param subPlatformId サブプラットフォームID
	 * @return 削除に成功した場合、true
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean deleteCollectSubPlatformMaster(String subPlatformId) throws  HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("deleteCollectSubPlatformMaster");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList, true);

		boolean ret = false;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SubPlatformId=");
		msg.append(subPlatformId);

		try {
			ret = new PerformanceCollectMasterControllerBean().deleteCollectSubPlatformMaster(subPlatformId);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Delete SubPlatform Failed, Method=deleteCollectSubPlatformMaster, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Delete SubPlatform, Method=deleteCollectSubPlatformMaster, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * プラットフォーム定義を取得します。
	 * 
	 * @return プラットフォーム定義のリスト
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<CollectorPlatformMstData> getCollectPlatformMaster() throws  HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getCollectPlatformMaster");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList, true);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Get Platform, Method=getCollectPlatformMaster, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new PerformanceCollectMasterControllerBean().getCollectPlatformMaster();
	}

	/**
	 * サブプラットフォーム定義を取得します。
	 * 
	 * @return サブプラットフォーム定義のリスト
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<CollectorSubPlatformMstData> getCollectSubPlatformMaster() throws  HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getCollectSubPlatformMaster");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList, true);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Get SubPlatform, Method=getCollectSubPlatformMaster, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new PerformanceCollectMasterControllerBean().getCollectSubPlatformMaster();
	}
}
