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
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.maintenance.util.QueryUtil;
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
	 * @param sessionId セッションID
	 * @param maintenanceId メンテナンスID
	 * @param type 終了状態
	 * @param result メンテナンス実行結果
	 * @return 通知情報
	 * @throws HinemosUnknown 
	 * 
	 * @see com.clustercontrol.bean.EndStatusConstant
	 * @see com.clustercontrol.bean.JobConstant
	 * @see com.clustercontrol.monitor.message.LogOutputNotifyInfo
	 */
	protected OutputBasicInfo createOutputBasicInfo(String maintenanceId, Integer type, int result) throws HinemosUnknown {
		m_log.debug("createOutputBasicInfo() : maintenanceId=" + maintenanceId + ", type=" + type);

		OutputBasicInfo rtn = null;

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
				//プラグインID
				rtn.setPluginId(HinemosModuleConstant.SYSYTEM_MAINTENANCE);
				//アプリケーション
				rtn.setApplication(info.getApplication());
				//監視項目ID
				rtn.setMonitorId(maintenanceId);

				//メンテナンス機能では該当する値なし
				rtn.setFacilityId(FacilityTreeAttributeConstant.INTERNAL_SCOPE);
				rtn.setScopeText(FacilityTreeAttributeConstant.INTERNAL_SCOPE_TEXT);

				//メッセージID、メッセージ、オリジナルメッセージ
				if(type.intValue() == PriorityConstant.TYPE_INFO){
					String[] args1 = {maintenanceId};
					rtn.setMessage(MessageConstant.MESSAGE_MAINTENACE_STOPPED_SUCCESS.getMessage(args1));
				}
				else if(type.intValue() == PriorityConstant.TYPE_CRITICAL){
					String[] args1 = {maintenanceId};
					rtn.setMessage(MessageConstant.MESSAGE_MAINTENANCE_STOPPED_FAILED.getMessage(args1));
				}
				rtn.setMessageOrg(info.getMaintenanceTypeMstEntity().getType_id() + " : " + result + " records");

				//重要度
				rtn.setPriority(type.intValue());
				//発生日時
				rtn.setGenerationDate(HinemosTime.getDateInstance().getTime());
			}
		} catch (MaintenanceNotFound e) {
		} catch (InvalidRole e) {
		}
		return rtn;
	}
}
