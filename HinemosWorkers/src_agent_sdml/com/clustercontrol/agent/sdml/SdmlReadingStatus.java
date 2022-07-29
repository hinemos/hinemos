/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.sdml;

import java.io.File;

import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatus;

public class SdmlReadingStatus extends AbstractReadingStatus<SdmlFileMonitorInfoWrapper> {

	public SdmlReadingStatus(AbstractFileMonitorManager<SdmlFileMonitorInfoWrapper> fileMonitorManager,
			String monitorId, File filePath, int firstPartDataCheckSize, File rsFilePath, boolean tail) {
		// SDML制御ログは常に先頭から読む
		super(fileMonitorManager, monitorId, filePath, firstPartDataCheckSize, rsFilePath, false);
	}

	@Override
	protected boolean initialize() {
		initialized = true;
		return initialized;
	}
	
	@Override
	public void store() {
		// SDML制御ログは読み取り状態の永続化は行わない
	}

	@Override
	public void clear() {
		// SDML制御ログは読み取り状態の永続化は行わない
	}
}
