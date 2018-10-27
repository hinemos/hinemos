/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.notify;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.notify.bean.NotifyCheckIdResultInfo;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * 通知用のWebAPIエンドポイント
 */
@javax.jws.WebService(targetNamespace = "http://notify.ws.clustercontrol.com")
public class NotifyEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( NotifyEndpoint.class );
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
	 * 通知情報を作成します。
	 *
	 * NotifyAdd権限が必要
	 *
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 * @throws NotifyDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.notify.factory.AddNotify#add(NotifyInfo)
	 */
	public boolean addNotify(NotifyInfo info) throws HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole,InvalidSetting {
		m_log.debug("addNotify");

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", NotifyID=");
			msg.append(info.getNotifyId());
		}

		try {
			ret = new NotifyControllerBean().addNotify(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Add Failed, Method=addNotify, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Add, Method=addNotify, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * 通知情報を変更します。
	 *
	 * NotifyWrite権限が必要
	 *
	 * @param info 変更対象の通知情報
	 * @return 変更に成功した場合、<code> true </code>
	 * @throws HinemosUnknown
	 * @throws NotifyDuplicate
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.notify.factory.ModifyNotify#modify(NotifyInfo)
	 */
	public boolean modifyNotify(NotifyInfo info) throws NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole,InvalidSetting {
		m_log.debug("modifyNotify");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", NotifyID=");
			msg.append(info.getNotifyId());
		}

		try {
			ret = new NotifyControllerBean().modifyNotify(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Change Failed, Method=modifyNotify, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Change, Method=modifyNotify, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * 通知情報を削除します。
	 *
	 * NotifyWrite権限が必要
	 *
	 * @param notifyIds 削除対象の通知IDリスト
	 * @return 削除に成功した場合、<code> true </code>
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.notify.factory.DeleteNotify#delete(String)
	 */
	public boolean deleteNotify(String[] notifyIds) throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("deleteNotify");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", NotifyID=");
		msg.append(Arrays.toString(notifyIds));

		try {
			ret = new NotifyControllerBean().deleteNotify(notifyIds);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Delete Failed, Method=deleteNotify, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Delete, Method=deleteNotify, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * 引数で指定された通知情報を返します。
	 *
	 * NotifyRead権限が必要
	 *
	 * @param notifyId 取得対象の通知ID
	 * @return 通知情報
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.notify.factory.SelectNotify#getNotify(String)
	 */
	public NotifyInfo getNotify(String notifyId) throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getNotify");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", NotifyID=");
		msg.append(notifyId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Get, Method=getNotify, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new NotifyControllerBean().getNotify(notifyId);
	}

	/**
	 * 通知情報一覧を返します。
	 *
	 * NotifyRead権限が必要
	 *
	 * @return 通知情報一覧（Objectの2次元配列）
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.notify.factory.SelectNotify#getNotifyList()
	 */
	public ArrayList<NotifyInfo> getNotifyList() throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getNotifyList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Get, Method=getNotifyList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new NotifyControllerBean().getNotifyList();
	}

	/**
	 * オーナーロールIDを条件として通知情報一覧を返します。
	 *
	 * NotifyRead権限が必要
	 *
	 * @param オーナーロールID
	 * @return 通知情報一覧（Objectの2次元配列）
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.notify.factory.SelectNotify#getNotifyList()
	 */
	public ArrayList<NotifyInfo> getNotifyListByOwnerRole(String ownerRoleId) throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getNotifyListByOwnerRole");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Get, Method=getNotifyListByOwnerRole, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new NotifyControllerBean().getNotifyListByOwnerRole(ownerRoleId);
	}

	/**
	 *　引数で指定した通知IDを利用している通知グループIDを取得する。
	 *
	 * NotifyRead権限が必要
	 *
	 * @param notifyIds
	 * @return　通知グループIDのリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<NotifyCheckIdResultInfo> checkNotifyId(String[] notifyIds) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("checkNotifyId");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", NotifyID=");
		msg.append(Arrays.toString(notifyIds));
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Check, Method=checkNotifyId, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new NotifyControllerBean().checkNotifyId(notifyIds);
	}

	/**
	 *　指定した通知IDを有効化する。(現在の有効/無効の判定なし)
	 *
	 * NotifyWrite権限が必要
	 *
	 * @param notifyId 通知ID
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * @throws NotifyDuplicate
	 */
	public void setNotifyStatus(String notifyId, boolean validFlag) throws InvalidUserPass, InvalidRole, HinemosUnknown, NotifyNotFound, NotifyDuplicate {
		m_log.debug("enableNotify() notifyId = " + notifyId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", NotifyID=");
		msg.append(notifyId);
		msg.append(", ValidFlag=");
		msg.append(validFlag);

		try {
			new NotifyControllerBean().setNotifyStatus(notifyId, validFlag);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Change Valid Failed, Method=setNotifyStatus, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Change Valid, Method=setNotifyStatus, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * 外部から直接通知処理を実行します。

	 *
	 * @param pluginId プラグインID
	 * @param monitorId 監視項目ID
	 * @param facilityId ファシリティID
	 * @param subKey 抑制用のサブキー（任意の文字列）
	 * @param generationDate 出力日時（エポック秒）
	 * @param priority 重要度
	 * @param application アプリケーション
	 * @param message メッセージ
	 * @param messageOrg オリジナルメッセージ
	 * @param notifyIdList 通知IDのリスト
	 * @param srcId 送信元を特定するためのID
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * @throws FacilityNotFound
	 */
	public void notify(
			String pluginId,
			String monitorId,
			String facilityId,
			String subKey,
			long generationDate,
			int priority,
			String application,
			String message,
			String messageOrg,
			ArrayList<String> notifyIdList,
			String srcId)  throws InvalidRole, InvalidUserPass, HinemosUnknown, NotifyNotFound, FacilityNotFound {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		sdf.setTimeZone(HinemosTime.getTimeZone());
		String msg = ", pluginID=" + pluginId
				+ ", monitorID=" + monitorId
				+ ", facilityID=" + facilityId
				+ ", subKey=" + subKey
				+ ", generationDate=" + sdf.format(new Date(generationDate))
				+ ", priority=" + priority
				+ ", application=" + application
				+ ", message=" + message
				+ ", messageOrg=" + messageOrg
				+ ", srcID=" + srcId;

		m_log.debug("notify() " + msg);

		// 認証済み操作ログ
		try {
			ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.NOTIFY, SystemPrivilegeMode.READ));
			HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
			new NotifyControllerBean().notify(
					pluginId,
					monitorId,
					facilityId,
					subKey,
					generationDate,
					priority,
					application,
					message,
					messageOrg,
					notifyIdList,
					srcId);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Run Failed, Method=notify, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Run, Method=notify, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
}
