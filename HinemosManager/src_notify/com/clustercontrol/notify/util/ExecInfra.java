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

package com.clustercontrol.notify.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.infra.bean.AccessInfo;
import com.clustercontrol.infra.bean.ModuleResult;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.infra.model.InfraModuleInfo;
import com.clustercontrol.infra.session.InfraControllerBean;
import com.clustercontrol.infra.util.InfraManagementValidator;
import com.clustercontrol.notify.bean.ExecFacilityConstant;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.NotifyInfraInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * syslogに転送するクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class ExecInfra implements Notifier {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( ExecInfra.class );
	
	NotifyInfraInfo infraInfo = null;
	
	@Override
	public void notify(NotifyRequestMessage requestMessage)
			throws NotifyNotFound {
		if(m_log.isDebugEnabled()){
			m_log.debug("notify() " + requestMessage);
		}
		OutputBasicInfo outputInfo = requestMessage.getOutputInfo();
		String notifyId = requestMessage.getNotifyId();
		executeInfra(outputInfo, notifyId);
	}

	private void executeInfra(OutputBasicInfo outputInfo, String notifyId) {
		if(m_log.isDebugEnabled()){
			m_log.debug("notify() " + outputInfo);
		}

		// 該当する重要度の通知情報を取得する
		try {
			infraInfo = QueryUtil.getNotifyInfraInfoPK(notifyId);
			
			// 実行対象の環境構築設定が存在するかのチェック(存在しない場合はInternalイベントを出力して終了)
			try{
				InfraManagementValidator.validateInfraManagementId(getManagementId(infraInfo, outputInfo.getPriority()), false);
			} catch (InvalidRole | InvalidSetting e) {
				// 参照権限がない場合
				// 実行対象が存在しない場合の処理
				int outputPriority = outputInfo.getPriority(); 
				int failurePriority = getFailurePriority(infraInfo, outputPriority);

				String[] args = { notifyId, outputInfo.getMonitorId(), getManagementId(infraInfo, outputInfo.getPriority()) };
				AplLogger.put(failurePriority, HinemosModuleConstant.PLATFORM_NOTIFY, MessageConstant.MESSAGE_SYS_009_NOTIFY, args, null);
				return;
			}
			
			// 実行対象の環境構築設定を取得
			InfraManagementInfo managementInfo = com.clustercontrol.infra.util.QueryUtil.getInfraManagementInfoPK(getManagementId(infraInfo, outputInfo.getPriority()));
			List<String> moduleIdList = new ArrayList<>();
			for (InfraModuleInfo<?> module : managementInfo.getModuleList()) {
				moduleIdList.add(module.getModuleId());
			}
			
			String targetFacilityId = null;
			// 環境構築設定のスコープがnullなら、通知設定のスコープ設定を利用する
			if (managementInfo.getFacilityId() == null) {
				// 通知設定が「固定スコープ」になっていた場合は、環境構築設定に渡すファシリティIDを変更する
				if (infraInfo.getInfraExecFacilityFlg() == ExecFacilityConstant.TYPE_FIX) {
					outputInfo.setFacilityId(infraInfo.getInfraExecFacility());
				}
				targetFacilityId = outputInfo.getFacilityId();
			} else {
				targetFacilityId = managementInfo.getFacilityId();
			}
			
			// 環境構築実行時は、すべてノードプロパティの認証情報を利用する
			List<AccessInfo> accessList = new ArrayList<>();
			List<String> facilityIdList =  new RepositoryControllerBean().getExecTargetFacilityIdList(
					targetFacilityId,
					infraInfo.getNotifyInfoEntity().getOwnerRoleId());
			for (String facilityId : facilityIdList) {
				AccessInfo access = new AccessInfo();
				access.setFacilityId(facilityId);
				accessList.add(access);
			}
			
			// 環境構築設定を実行する
			InfraControllerBean bean = new InfraControllerBean();
			String session = bean.createSession(managementInfo.getManagementId(), moduleIdList, accessList);
			
			while (true) {
				ModuleResult result = bean.runInfraModule(session);
				if (!result.isHasNext()) {
					break;
				}
			}
			
		} catch (Exception e) {
			m_log.warn("executionInfra() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			int priority;
			String message = "";
			if (infraInfo == null) {
				priority = PriorityConstant.TYPE_CRITICAL;
			} else {
				priority = getFailurePriority(infraInfo, outputInfo.getPriority());
				message = " : infraManagementId=" + getManagementId(infraInfo, outputInfo.getPriority());
			}
			
			internalErrorNotify(priority, notifyId, MessageConstant.MESSAGE_SYS_007_NOTIFY, e.getMessage() + message);
		}

	}

	private String getManagementId(NotifyInfraInfo infraInfo, int priority) {
		switch(priority) {
		case PriorityConstant.TYPE_INFO:
			return infraInfo.getInfoInfraId();
		case PriorityConstant.TYPE_WARNING:
			return infraInfo.getWarnInfraId();
		case PriorityConstant.TYPE_CRITICAL:
			return infraInfo.getCriticalInfraId();
		case PriorityConstant.TYPE_UNKNOWN:
			return infraInfo.getUnknownInfraId();
		}
		
		return null;
	}

	private int getFailurePriority(NotifyInfraInfo infraInfo, int priority) {
		switch(priority) {
		case PriorityConstant.TYPE_INFO:
			return infraInfo.getInfoInfraFailurePriority();
		case PriorityConstant.TYPE_WARNING:
			return infraInfo.getWarnInfraFailurePriority();
		case PriorityConstant.TYPE_CRITICAL:
			return infraInfo.getCriticalInfraFailurePriority();
		case PriorityConstant.TYPE_UNKNOWN:
			return infraInfo.getUnknownInfraFailurePriority();
		}
		
		return -1;
	}
	
	/**
	 * 通知失敗時の内部エラー通知を定義します
	 */
	@Override
	public void internalErrorNotify(int priority, String notifyId, MessageConstant msgCode, String detailMsg) {
		String[] args = { notifyId };

		// 通知失敗メッセージを出力
		AplLogger.put(priority, HinemosModuleConstant.PLATFORM_NOTIFY, msgCode, args, detailMsg);
	}
}
