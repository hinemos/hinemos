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

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.monitor.run.util.EventCacheModifyCallback;

/**
 * イベント履歴の削除処理
 *
 * @version 4.0.0
 * @since 3.1.0
 *
 */
public class MaintenanceEvent extends MaintenanceObject{

	private static Log m_log = LogFactory.getLog( MaintenanceEvent.class );

	/**
	 * 削除処理
	 */
	@Override
	protected int _delete(Long boundary, boolean status, String ownerRoleId) {
		m_log.debug("_delete() start : status = " + status);

		int ret = -1;
		String ownerRoleId2 = null;

		//オーナーロールIDがADMINISTRATORSの場合
		if(RoleIdConstant.isAdministratorRole(ownerRoleId)) {
			//SQL文の実行
			if(status){
				// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0) : タイムアウト値設定
				ret = QueryUtil.deleteEventLogByGenerationDate(boundary, HinemosPropertyUtil.getHinemosPropertyNum(_QUERY_TIMEOUT_KEY, Long.valueOf(0)).intValue());
			} else {
				// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0) : タイムアウト値設定
				//status=falseの場合は確認済みイベントのみを削除する
				ret = QueryUtil.deleteEventLogByGenerationDateConfigFlg(boundary, HinemosPropertyUtil.getHinemosPropertyNum(_QUERY_TIMEOUT_KEY, Long.valueOf(0)).intValue());
			}
		}
		//オーナーロールが一般ロールの場合
		else {
			ownerRoleId2 = ownerRoleId;
			//SQL文の実行
			if(status){
				// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0) : タイムアウト値設定
				ret = QueryUtil.deleteEventLogByGenerationDateAndOwnerRoleId(boundary, HinemosPropertyUtil.getHinemosPropertyNum(_QUERY_TIMEOUT_KEY, Long.valueOf(0)).intValue(), ownerRoleId);
			} else {
				// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0) : タイムアウト値設定
				//status=falseの場合は確認済みイベントのみを削除する
				ret = QueryUtil.deleteEventLogByGenerationDateConfigFlgAndOwnerRoleId(boundary, HinemosPropertyUtil.getHinemosPropertyNum(_QUERY_TIMEOUT_KEY, Long.valueOf(0)).intValue(), ownerRoleId);
			}
		}

		// cache内も消す
		// status=trueは全削除、status=falseはConfirmFlgが1(確認)のものを削除
		new JpaTransactionManager().addCallback(new EventCacheModifyCallback(boundary, status, ownerRoleId2));

		//終了
		m_log.debug("_delete() count : " + ret);
		return ret;
	}

}
