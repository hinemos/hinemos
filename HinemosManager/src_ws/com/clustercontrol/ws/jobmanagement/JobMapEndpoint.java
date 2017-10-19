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
package com.clustercontrol.ws.jobmanagement;

import java.util.ArrayList;
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
import com.clustercontrol.fault.IconFileDuplicate;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.jobmanagement.bean.JobmapIconImage;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.util.KeyCheck;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * ジョブマップ用のWebAPIエンドポイント
 */
@javax.jws.WebService(targetNamespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobMapEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( JobMapEndpoint.class );
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

	public String getVersion() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return KeyCheck.getResultEnterprise();
	}

	/**
	 * ジョブマップ用アイコン情報を登録します。<BR>
	 *
	 * JobManagementAdd権限が必要
	 *
	 * @param info ジョブマップ用アイコン情報
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 * @throws IconFileDuplicate
	 */
	public void addJobmapIconImage(JobmapIconImage info) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, IconFileDuplicate {
		if (info == null || info.getIconId() == null) {
			throw new HinemosUnknown("jobmapIconImage is null");
		}
		m_log.debug("addJobmapIconImage : iconID=" + info.getIconId());

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", iconID=");
		msg.append(info.getIconId());

		try {
			new JobControllerBean().addJobmapIconImage(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Add JobmapIconImage Failed, Method=addJobmapIconImage, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Add JobmapIconImage, Method=addJobmapIconImage, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	/**
	 * ジョブマップ用アイコン情報を変更します。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @param info ジョブマップ用アイコン情報
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 * @throws IconFileNotFound
	 */
	public void modifyJobmapIconImage(JobmapIconImage info) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, IconFileNotFound {
		if (info == null) {
			throw new HinemosUnknown("jobmapIconImage is null");
		}
		m_log.debug("modifyJobmapIconImage : iconID=" + info.getIconId());
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", iconID=");
		msg.append(info.getIconId());

		try {
			new JobControllerBean().modifyJobmapIconImage(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Change JobmapIconImage Failed, Method=modifyJobmapIconImage, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Change JobmapIconImage, Method=modifyJobmapIconImage, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	/**
	 * ジョブマップ用アイコン情報を削除します。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @param iconIdList アイコンIDリスト
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 * @throws IconFileNotFound
	 */
	public void deleteJobmapIconImage(List<String> iconIdList) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, IconFileNotFound {
		m_log.debug("deleteJobmapIconImage : iconID=" + iconIdList);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", iconID=");
		msg.append(iconIdList);

		try {
			new JobControllerBean().deleteJobmapIconImage(iconIdList);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Delete JobmapIconImage Failed, Method=deleteJobmapIconImage, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Delete JobmapIconImage, Method=deleteJobmapIconImage, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	/**
	 * アイコンIDと一致するジョブマップ用アイコン情報を返します。<BR>
	 * @param iconId アイコンID
	 * @return ジョブマップ用アイコン情報
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public JobmapIconImage getJobmapIconImage(String iconId) throws IconFileNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobmapIconImage : iconID=" + iconId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", iconID=");
		msg.append(iconId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobmapIconImage, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getJobmapIconImage(iconId);
	}

	/**
	 * ジョブマップ用アイコンイメージ（ジョブ用）のデフォルトアイコンIDを取得する。<BR>
	 * 
	 * @return デフォルトアイコンID
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public String getJobmapIconIdJobDefault() throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobmapIconIdJobDefault");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobmapIconIdJobDefault, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getJobmapIconIdJobDefault();
	}

	/**
	 * ジョブマップ用アイコンイメージ（ジョブネット用）のデフォルトアイコンIDを取得する。<BR>
	 * 
	 * @return デフォルトアイコンID
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public String getJobmapIconIdJobnetDefault() throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobmapIconIdJobnetDefault");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobmapIconIdJobnetDefault, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getJobmapIconIdJobnetDefault();
	}

	/**
	 * ジョブマップ用アイコンイメージ（承認ジョブ用）のデフォルトアイコンIDを取得する。<BR>
	 * 
	 * @return デフォルトアイコンID
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public String getJobmapIconIdApprovalDefault() throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobmapIconIdApprovalDefault");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobmapIconIdApprovalDefault, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getJobmapIconIdApprovalDefault();
	}

	/**
	 * ジョブマップ用アイコンイメージ（監視ジョブ用）のデフォルトアイコンIDを取得する。<BR>
	 * 
	 * @return デフォルトアイコンID
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public String getJobmapIconIdMonitorDefault() throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobmapIconIdMonitorDefault");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobmapIconIdMonitorDefault, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getJobmapIconIdMonitorDefault();
	}

	/**
	 * ジョブマップ用アイコンイメージ（ファイル転送ジョブ用）のデフォルトアイコンIDを取得する。<BR>
	 * 
	 * @return デフォルトアイコンID
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public String getJobmapIconIdFileDefault() throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobmapIconIdFileDefault");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobmapIconIdFileDefault, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getJobmapIconIdFileDefault();
	}
}
