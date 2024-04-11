/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.jobmanagement.queue.JobQueueContainer;
import com.clustercontrol.jobmanagement.util.JobMultiplicityCache;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.jobmanagement.util.RpaJobWorker;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.util.Singletons;
import com.clustercontrol.xcloud.util.ResourceJobWorker;

public class JobInitializerPlugin implements HinemosPlugin {
	public static final Log log = LogFactory.getLog(JobInitializerPlugin.class);

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(Log4jReloadPlugin.class.getName());
		dependency.add(CacheInitializerPlugin.class.getName());
		dependency.add(AsyncWorkerPlugin.class.getName());
		return dependency;
	}

	@Override
	public Set<String> getRequiredKeys() {
		return null;
	}

	@Override
	public void create() {
	}

	@Override
	public void activate() {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			JobMultiplicityCache.refresh();

			// ジョブキューコンテナを生成(ジョブキュー機能を起動)
			Singletons.get(JobQueueContainer.class);
			
			// 実行途中のRPAシナリオジョブを別スレッドで再実行
			RpaJobWorker.restartRunningJob();

			// 実行途中のリソース制御ジョブを別スレッドで再実行
			ResourceJobWorker.restartRunningJob();

			// 監視ジョブマップ生成
			if (HinemosPropertyCommon.job_monitor_restart.getBooleanValue()) {
				MonitorJobWorker.restartMonitorJob();
			}

			jtm.commit();
		} catch (Exception e) {
			log.error(e);
			if (jtm != null) {
				jtm.rollback();
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}

	@Override
	public void deactivate() {
		// ジョブキューコンテナを終了(ジョブキュー機能を終了)
		Singletons.get(JobQueueContainer.class).terminate();
	}

	@Override
	public void destroy() {
	}

}
