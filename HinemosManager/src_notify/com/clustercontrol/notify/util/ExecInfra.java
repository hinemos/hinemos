/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
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

		InfraControllerBean bean = new InfraControllerBean();
		String session = null;
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
				Integer failurePriority = getFailurePriority(infraInfo, outputPriority);

				String[] args = { notifyId, outputInfo.getMonitorId(), getManagementId(infraInfo, outputInfo.getPriority()) };
				AplLogger.put(InternalIdCommon.PLT_NTF_SYS_009, failurePriority, args, null);
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
			int nodeInputType = HinemosPropertyCommon.infra_management_access_input_type.getIntegerValue();
			session = bean.createSession(managementInfo.getManagementId(), moduleIdList, nodeInputType, accessList).getSessionId();
			
			while (true) {
				ModuleResult result = bean.runInfraModule(session);
				if (!result.isHasNext()) {
					break;
				}
			}
			
		} catch (Exception e) {
			m_log.warn("executionInfra() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			Integer priority = null;
			String message = "";
			if (infraInfo != null) {
				priority = getFailurePriority(infraInfo, outputInfo.getPriority());
				message = " : infraManagementId=" + getManagementId(infraInfo, outputInfo.getPriority());
			}
			String[] args = { notifyId };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_010, priority, args, e.getMessage() + message);
		} finally {
			if (session != null) {
				try {
					bean.deleteSession(session);
					m_log.debug("executeInfra() : delete session, sessionId=" + session);
				} catch (Exception e) {
					m_log.warn("executeInfra() : delete session failed, sessionId=" + session, e);
				}
			}
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

	private Integer getFailurePriority(NotifyInfraInfo infraInfo, int priority) {
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
		
		return null;
	}
}
