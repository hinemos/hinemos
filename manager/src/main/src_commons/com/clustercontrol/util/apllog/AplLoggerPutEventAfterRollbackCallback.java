/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util.apllog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.monitor.bean.EventConfirmConstant;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;

/**
 * (rollback -> execution) -> 上位トランザクションのRollbackに伴い、executionを発動するJpaTransactionCallbackクラス
 * Rollback時もINTERNALイベントは出力する必要があるため、Rollback時に動作するよう設定
 * イベント出力以外は、Rollbackされても動作するため、対応するのはイベント出力のみ
 * 
 */
public class AplLoggerPutEventAfterRollbackCallback implements JpaTransactionCallback {

	private static Log m_log = LogFactory.getLog(AplLoggerPutEventAfterRollbackCallback.class);
	
	private final OutputBasicInfo info;
	private boolean rollbackFlg = false;
	
	public AplLoggerPutEventAfterRollbackCallback(OutputBasicInfo info) {
		this.info = info;
	}

	@Override
	public boolean isTransaction() {
		return false;
	}

	@Override
	public void preFlush() {}

	@Override
	public void postFlush() {}

	@Override
	public void preCommit() {}

	@Override
	public void postCommit() {
	}

	@Override
	public void preRollback() {}

	/**
	 * Rollback後に、AplLogerで通知を行うためフラグを立てる
	 */
	@Override
	public void postRollback() {
		this.rollbackFlg = true;
	}

	@Override
	public void preClose() {}

	/**
	 * Rollbackされた場合、Close後にAplLogerで通知を行う
	 */
	@Override
	public void postClose() {
		if(rollbackFlg) {
			JpaTransactionManager jtm = new JpaTransactionManager();
			
			try {
				jtm.begin();
				new NotifyControllerBean().insertEventLog(info, EventConfirmConstant.TYPE_UNCONFIRMED);
				jtm.commit();
			} catch (Exception e) {
				m_log.warn("put internal event failure. (monitorId = " + info.getMonitorId() + ", facilityId = " + info.getFacilityId() + ", message = " + info.getMessage() + ")", e);
				jtm.rollback();
			} finally {
				jtm.close();
			}
		}
	}

}