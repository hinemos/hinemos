/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
import com.clustercontrol.notify.session.NotifyControllerBean;
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
	 * @throws HinemosUnknown 
	 * 
	 * @see com.clustercontrol.bean.EndStatusConstant
	 * @see com.clustercontrol.bean.JobConstant
	 * @see com.clustercontrol.monitor.message.LogOutputNotifyInfo
	 */
	protected void notify(String maintenanceId, Integer type, int result) throws HinemosUnknown {
		m_log.debug("notify() : maintenanceId=" + maintenanceId + ", type=" + type);

		//MaintenanceInfoを取得する
		MaintenanceInfo info = null;
		try {
			info = QueryUtil.getMaintenanceInfoPK(maintenanceId);

			if(info.getNotifyGroupId() != null && info.getNotifyGroupId().length() > 0){
				//通知する

				//通知情報作成
				OutputBasicInfo notice = new OutputBasicInfo();

				//プラグインID
				notice.setPluginId(HinemosModuleConstant.SYSYTEM_MAINTENANCE);
				//アプリケーション
				notice.setApplication(info.getApplication());
				//監視項目ID
				notice.setMonitorId(maintenanceId);

				//メンテナンス機能では該当する値なし
				notice.setFacilityId(FacilityTreeAttributeConstant.INTERNAL_SCOPE);
				notice.setScopeText(FacilityTreeAttributeConstant.INTERNAL_SCOPE_TEXT);

				//メッセージID、メッセージ、オリジナルメッセージ
				if(type.intValue() == PriorityConstant.TYPE_INFO){
					String[] args1 = {maintenanceId};
					notice.setMessage(MessageConstant.MESSAGE_MAINTENACE_STOPPED_SUCCESS.getMessage(args1));
				}
				else if(type.intValue() == PriorityConstant.TYPE_CRITICAL){
					String[] args1 = {maintenanceId};
					notice.setMessage(MessageConstant.MESSAGE_MAINTENANCE_STOPPED_FAILED.getMessage(args1));
				}
				notice.setMessageOrg(info.getMaintenanceTypeMstEntity().getType_id() + " : " + result + " records");

				//重要度
				notice.setPriority(type.intValue());
				//発生日時
				notice.setGenerationDate(HinemosTime.getDateInstance().getTime());

				// 通知処理
				new NotifyControllerBean().notify(notice, info.getNotifyGroupId());
			}
		} catch (MaintenanceNotFound e) {
		} catch (InvalidRole e) {
		}
	}
}
