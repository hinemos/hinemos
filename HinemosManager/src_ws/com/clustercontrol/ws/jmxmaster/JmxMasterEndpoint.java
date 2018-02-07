/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.jmxmaster;

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
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.jmx.model.JmxMasterInfo;
import com.clustercontrol.jmx.session.JmxMasterControllerBean;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * JMX 監視項目マスタ用のWebAPIエンドポイント
 */
@javax.jws.WebService(targetNamespace = "http://jmxmaster.ws.clustercontrol.com")
public class JmxMasterEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( JmxMasterEndpoint.class );
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
	 * JMX 監視項目マスタデータを一括で登録します。
	 * @param JmxMasterInfo
	 * @return 登録に成功した場合、true
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	public boolean addJmxMasterList(List<JmxMasterInfo> jmxMasterInfos) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.debug("addJmxMasterList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList, true);


		boolean ret = false;

		// 認証済み操作ログ
		try {
			ret = new JmxMasterControllerBean().addJmxMasterList(jmxMasterInfos);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Add JMX Master Failed, Method=addJmxMasterList, User="
					+ HttpAuthenticator.getUserAccountString(wsctx));
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Add JMX Master, Method=addJmxMasterList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return ret;
	}

	/**
	 * JMX 監視項目マスタ情報を全て削除します。
	 * 
	 * @return 削除に成功した場合、true
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public boolean deleteJmxMasterAll() throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("deleteJmxMasterAll");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList, true);

		boolean ret = false;

		// 認証済み操作ログ
		try {
			ret = new JmxMasterControllerBean().deleteJmxMasterAll();
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Delete JMX Master Failed, Method=deleteJmxMasterAll, User="
					+ HttpAuthenticator.getUserAccountString(wsctx));
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Delete JMX Master, Method=deleteJmxMasterAll, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return ret;
	}


	/**
	 * JMX 監視項目マスタデータを一括で削除します。
	 * @param jmxMasterIds
	 * @return 登録に成功した場合、true
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public boolean deleteJmxMasterList(List<String> jmxMasterIds) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("deleteJmxMasterList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList, true);

		boolean ret = false;

		// 認証済み操作ログ
		try {
			ret = new JmxMasterControllerBean().deleteJmxMasterList(jmxMasterIds);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Delete JMX Master Failed, Method=deleteJmxMasterList, User="
					+ HttpAuthenticator.getUserAccountString(wsctx));
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Delete JMX Master, Method=deleteJmxMasterList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return ret;
	}

	/**
	 * JMX 監視項目マスタ情報を一括で取得します。
	 * 
	 * @return 収集マスタ情報
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public List<JmxMasterInfo> getJmxMasterInfoList() throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJmxMasterInfoList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Get JMX Master, Method=getJmxMasterInfoList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JmxMasterControllerBean().getJmxMasterList();
	}

}
