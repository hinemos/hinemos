/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.plugin;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.HinemosManagerMain;
import com.clustercontrol.HinemosManagerMain.StartupMode;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.sdml.factory.SdmlVersionManager;
import com.clustercontrol.sdml.util.SdmlInternalCheckTask;
import com.clustercontrol.sdml.v1.SdmlV1Option;
import com.clustercontrol.util.KeyCheck;
import com.clustercontrol.util.Singletons;

public class SdmlControllerService implements HinemosPlugin {
	private static Log logger = LogFactory.getLog(SdmlControllerService.class);

	private SdmlInternalCheckTask internalCheckTask;

	public SdmlControllerService() {
		super();
	}

	@Override
	public Set<String> getDependency() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> getRequiredKeys() {
		return null;
	}

	/**
	 * 必要なキーが存在するかどうかをチェックする
	 */
	protected boolean checkRequiredKeys() {
		for (String key : getRequiredKeys()) {
			if (!KeyCheck.checkKey(key)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void create() {
		logger.info("create() : creating " + getClass().getSimpleName() + "...");

		internalCheckTask = new SdmlInternalCheckTask();
	}

	@Override
	public void activate() {
		logger.info("activate() : activating " + getClass().getSimpleName() + "...");

		// バージョンごとのオプションを追加
		Singletons.get(SdmlVersionManager.class).addSdmlOption(SdmlV1Option.VERSION, new SdmlV1Option());

		// メンテナンスモードの場合は機能障害検知のタスクは起動しない
		if (HinemosManagerMain._startupMode == StartupMode.MAINTENANCE) {
			logger.info("activate() : SDML Internal check activation is skipped. (startup mode is MAINTENANCE)");
			return;
		}

		internalCheckTask.start();
	}

	@Override
	public void deactivate() {
		logger.info("deactivate() : deactivating " + getClass().getSimpleName() + "...");

		if (HinemosManagerMain._startupMode == StartupMode.MAINTENANCE) {
			logger.info("deactivate() : SDML Internal check deactivation is skipped. (startup mode is MAINTENANCE)");
			return;
		}

		internalCheckTask.shutdown();
	}

	@Override
	public void destroy() {
		logger.info("destroy() : destroying " + getClass().getSimpleName() + "...");

	}
}
