/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageId;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.notify.bean.NotifyTriggerType;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;


/**
 * 監視管理に通知するクラスです。
 *
 * @version 3.0.0
 * @since 2.0.0
 */
public class Notice {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( Notice.class );

	/**
	 * メンテナンスIDからメンテナンス通知情報を取得し、<BR>
	 * メンテナンス通知情報と終了状態を基に、ログ出力情報作成し、監視管理に通知します。
	 * 
	 * @param maintenanceId メンテナンスID
	 * @param result メンテナンス実行結果
	 * @return 通知情報
	 * @throws HinemosUnknown 
	 * 
	 * @see com.clustercontrol.bean.EndStatusConstant
	 * @see com.clustercontrol.bean.JobConstant
	 * @see com.clustercontrol.monitor.message.LogOutputNotifyInfo
	 */
	protected OutputBasicInfo createOutputBasicInfo(String maintenanceId, int result) throws HinemosUnknown {
		m_log.debug("createOutputBasicInfo() : maintenanceId=" + maintenanceId);

		OutputBasicInfo rtn = null;
		// result=-1の場合は、以降の処理で異常終了とするためnullを返す。
		if (result < 0) {
			return rtn;
		}

		//MaintenanceInfoを取得する
		MaintenanceInfo info = null;
		try {
			info = QueryUtil.getMaintenanceInfoPK(maintenanceId);

			if(info.getNotifyGroupId() != null && info.getNotifyGroupId().length() > 0){
				//通知する

				//通知情報作成
				rtn = new OutputBasicInfo();

				// 通知グループID
				rtn.setNotifyGroupId(info.getNotifyGroupId());
				// ジョブ連携メッセージID
				rtn.setJoblinkMessageId(JobLinkMessageId.getId(NotifyTriggerType.MAINTENANCE,
						HinemosModuleConstant.SYSYTEM_MAINTENANCE, maintenanceId));
				//プラグインID
				rtn.setPluginId(HinemosModuleConstant.SYSYTEM_MAINTENANCE);
				//アプリケーション
				rtn.setApplication(info.getApplication());
				//監視項目ID
				rtn.setMonitorId(maintenanceId);
				
				//7.0.0との互換性保持のため、Hinemosプロパティで出力するか制御
				boolean flg = HinemosPropertyCommon.notify_output_trigger_subkey_$.getBooleanValue(HinemosModuleConstant.SYSYTEM_MAINTENANCE);
				if (flg) {
					rtn.setSubKey(info.getTypeId() + "|" + NotifyTriggerType.MAINTENANCE.name());
				}

				//メンテナンス機能では該当する値なし
				rtn.setFacilityId(FacilityTreeAttributeConstant.INTERNAL_SCOPE);
				rtn.setScopeText(FacilityTreeAttributeConstant.INTERNAL_SCOPE_TEXT);

				//重要度、メッセージ、オリジナルメッセージ
				rtn.setPriority(PriorityConstant.TYPE_INFO);
				String[] args1 = {maintenanceId};
				rtn.setMessage(MessageConstant.MESSAGE_MAINTENACE_STOPPED_SUCCESS.getMessage(args1));
				rtn.setMessageOrg(info.getMaintenanceTypeMstEntity().getType_id() + " : " + result + " records");

				//発生日時
				rtn.setGenerationDate(HinemosTime.getDateInstance().getTime());
			}
		} catch (MaintenanceNotFound e) {
			m_log.warn("createOutputBasicInfo() : " 
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			rtn = null;
		} catch (InvalidRole e) {
			m_log.warn("createOutputBasicInfo() : " 
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			rtn = null;
		}
		return rtn;
	}
}
