/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util.apllog;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.jobmanagement.bean.JobLinkExpInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;

/**
 * (rollback -> execution) ->
 * 上位トランザクションのRollbackに伴い、executionを発動するJpaTransactionCallbackクラス
 * Rollback時もINTERNALイベントは出力する必要があるため、Rollback時に動作するよう設定
 * 
 */
public class AplLoggerPutJobLinkMessageAfterRollbackCallback implements JpaTransactionCallback {

	private static Log m_log = LogFactory.getLog(AplLoggerPutJobLinkMessageAfterRollbackCallback.class);

	private final OutputBasicInfo info;
	private List<JobLinkExpInfo> expList;
	private boolean rollbackFlg = false;

	public AplLoggerPutJobLinkMessageAfterRollbackCallback(OutputBasicInfo info, List<JobLinkExpInfo> expList) {
		this.info = info;
		this.expList = expList;
	}

	@Override
	public boolean isTransaction() {
		return false;
	}

	@Override
	public void preFlush() {
	}

	@Override
	public void postFlush() {
	}

	@Override
	public void preCommit() {
	}

	@Override
	public void postCommit() {
	}

	@Override
	public void preRollback() {
	}

	/**
	 * Rollback後に、AplLogerで通知を行うためフラグを立てる
	 */
	@Override
	public void postRollback() {
		this.rollbackFlg = true;
	}

	@Override
	public void preClose() {
	}

	/**
	 * Rollbackされた場合、Close後にAplLogerで通知を行う
	 */
	@Override
	public void postClose() {
		if (rollbackFlg) {
			JpaTransactionManager jtm = new JpaTransactionManager();

			try {
				jtm.begin();
				new NotifyControllerBean().sendJobLinkMessage(info, expList);
				jtm.commit();
			} catch (Exception e) {
				m_log.warn("put internal job link message failure. (monitorId = " + info.getMonitorId()
						+ ", facilityId = " + info.getFacilityId() + ", message = " + info.getMessage() + ")", e);
				jtm.rollback();
			} finally {
				jtm.close();
			}
		}
	}

}