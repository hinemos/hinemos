/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.SdmlControlSettingNotFound;
import com.clustercontrol.notify.bean.NotifyTriggerType;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.sdml.model.SdmlControlSettingInfo;
import com.clustercontrol.sdml.model.SdmlControlStatus;
import com.clustercontrol.sdml.model.SdmlControlStatusPK;
import com.clustercontrol.util.DateUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * SDML機能障害検知実装クラス
 *
 */
public class SdmlInternalCheckTask implements Runnable {
	private static Log logger = LogFactory.getLog(SdmlInternalCheckTask.class);

	private final ScheduledExecutorService _scheduler;

	/**
	 * 通知済みのリスト（シングルスレッドで利用する想定）<br>
	 * ※一度通知されたものは再度通知しない（マネージャ再起動時の通知は許容する）
	 */
	private static List<SdmlControlStatusPK> notifiedPKList = new ArrayList<>();

	private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

	public SdmlInternalCheckTask() {
		_scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "SdmlInternalCheckScheduler");
			}
		});
	}

	public void start() {
		_scheduler.scheduleWithFixedDelay(this,
				HinemosPropertyCommon.sdml_internal_check_startup_deray.getNumericValue(),
				HinemosPropertyCommon.sdml_internal_check_interval.getNumericValue(), TimeUnit.SECONDS);
	}

	public void shutdown() {
		_scheduler.shutdown();
		long _shutdownTimeoutMsec = HinemosPropertyCommon.hinemos_selfcheck_shutdown_timeout.getNumericValue();

		try {
			if (!_scheduler.awaitTermination(_shutdownTimeoutMsec, TimeUnit.MILLISECONDS)) {
				List<Runnable> remained = _scheduler.shutdownNow();
				if (remained != null) {
					logger.info("shutdown timeout. runnable remained. (size = " + remained.size() + ")");
				}
			}
		} catch (InterruptedException e) {
			_scheduler.shutdownNow();
		}
	}

	@Override
	public void run() {
		if (!HinemosPropertyCommon.sdml_internal_check_enable.getBooleanValue()) {
			logger.debug("run() : skip");
			return;
		}
		logger.debug("run() : execute");
		long start = HinemosTime.currentTimeMillis();
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			List<SdmlControlStatus> statusList = ControlStatusUtil.getAll();
			List<SdmlControlStatus> badList = new ArrayList<>();
			for (SdmlControlStatus status : statusList) {
				// 受信状態に問題がないかチェックする
				if (ControlStatusUtil.checkControlLogLastUpdate(status)) {
					// 問題ない場合通知済みリストにあれば消す
					if (notifiedPKList.contains(status.getId())) {
						notifiedPKList.remove(status.getId());
					}
				} else {
					badList.add(status);
				}
			}
			if (!badList.isEmpty()) {
				List<OutputBasicInfo> notifyInfoList = new ArrayList<>();
				for (SdmlControlStatus status : badList) {
					if (notifiedPKList.contains(status.getId())) {
						// 既に通知済みの場合はスキップ
						continue;
					}
					String lastUpdate = DateUtil.millisToString(status.getLastUpdateDate(), DATE_FORMAT);
					SdmlControlSettingInfo controlSetting = QueryUtil
							.getSdmlControlSettingInfoPK(status.getApplicationId(), ObjectPrivilegeMode.NONE);
					notifyInfoList.add(SdmlUtil.createOutputBasicInfo(controlSetting, status.getFacilityId(),
							PriorityConstant.TYPE_WARNING,
							MessageConstant.SDML_MSG_LOG_NO_RECEPTION
									.getMessage(status.getInternalCheckInterval().toString()),
							MessageConstant.SDML_MSG_LAST_RECEIVED_TIME.getMessage(lastUpdate),
							NotifyTriggerType.SDML_CONTROL_ABNORMAL));

					// 通知済みリストに格納
					notifiedPKList.add(new SdmlControlStatusPK(status.getApplicationId(), status.getFacilityId()));
				}

				// 通知
				jtm.addCallback(new NotifyCallback(notifyInfoList));
			}
			jtm.commit();
		} catch (InvalidSetting | SdmlControlSettingNotFound | InvalidRole | HinemosUnknown e) {
			logger.warn("run() : internal check failed. " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
		} catch (Exception e) {
			logger.error("run() : internal check failed. " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
			long end = HinemosTime.currentTimeMillis();
			logger.debug("run() : end time=" + (end - start));
			if (((end - start) / 1000) > HinemosPropertyCommon.sdml_internal_check_interval.getNumericValue()) {
				logger.warn("run() : is taking a long time. time=" + (end - start));
			}
		}
	}
}
