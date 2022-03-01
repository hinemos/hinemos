/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.HinemosCredential;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.Session.SessionScope;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity.OptionExecutor;
import com.clustercontrol.xcloud.model.LocationEntity;

/**
 * クラウド通知を実行するクラス このクラスから各オプションの実際に通信処理に受け渡し
 */
public class CloudNotifyExecuter {
	private static Log m_log = LogFactory.getLog(CloudNotifyExecuter.class);

	public static void execNotify(String facilityId, String ownerRoleId, ConcurrentHashMap<String, Object> requestMap)
			throws CloudManagerException {

		try (SessionScope sessionScope = SessionScope.open()) {
			// ログインユーザの取得処理
			// その他クラウド系の監視に合わせ、処理を実施する際の内部的なユーザは
			// ADMINISTRATORSのユーザ（デフォルトhinemosユーザ）とする
			// そのため、通知設定のロールにメイン以外のログインユーザが割り当てられていても、
			// メインのログインユーザが使用される
			String userId = HinemosPropertyCommon.xcloud_internal_thread_admin_user.getStringValue();
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, userId);
			Session.current().setHinemosCredential(new HinemosCredential(userId));
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, true);

			// ファシリティIDからlocationID、cloudScopeIdを取り出す。
			String locationId = "";
			String prefixRemoved = "";
			String cloudScopeId = "";
			try {
				locationId = facilityId.substring(facilityId.lastIndexOf("_") + 1, facilityId.length());
				prefixRemoved = facilityId.replaceFirst("_[A-Z]+_[A-Z]+_", "");
				cloudScopeId = prefixRemoved.substring(0, prefixRemoved.lastIndexOf("_"));
			} catch (Exception e) {
				// この時点でバリデーションされているので、上記の文字列操作でExceptionになることはないはずだが念のため。
				m_log.error("execNotify(): CloudScope Id not found for facilityid: " + facilityId, e);
				throw new CloudManagerException("CloudScope Id not found for facilityid: " + facilityId);
			}

			if (m_log.isDebugEnabled()) {
				m_log.debug("execNotify(): FacilityID: " + facilityId + " CloudScopeID: " + cloudScopeId
						+ " LocationID: " + locationId);
			}

			// ログインユーザの取得
			final CloudLoginUserEntity user = CloudManager.singleton().getLoginUsers()
					.getPrimaryCloudLoginUserByCurrent(cloudScopeId);

			// ログインユーザが見つからなかった場合は、エラー
			if (user == null) {
				m_log.error("execNotify(): Login User not found for facilityid: " + facilityId);
				throw new CloudManagerException("Login User not found for facilityid: " + facilityId);
			}
			
			// ロケーションの取得
			LocationEntity location = user.getCloudScope().getLocation(locationId);
			// ロケーションが見つからない場合もエラー
			if (location == null) {
				m_log.error("execNotify(): Location not found for locationId: " + locationId);
				throw new CloudManagerException("Location not found for locationId: " + locationId);
			}
			
			// ログインアカウントから適切なオプションのクラウド通知実行処理を呼び出す
			// VM管理を呼び出すとexceptionになる（通常はあり得ない）
			user.getCloudScope().optionExecute(new OptionExecutor() {
				@Override
				public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
					option.getResourceManagement(location, user).execNotify(requestMap);
				}
			});
		}
	}
}
