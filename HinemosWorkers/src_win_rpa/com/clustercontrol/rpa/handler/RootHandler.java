/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.handler;

import com.clustercontrol.jobmanagement.rpa.bean.RoboRunInfo;

/**
 * RPAシナリオの実行完了後のハンドラのルートとなるクラス
 */
public class RootHandler extends AbstractHandler {

	public RootHandler(RoboRunInfo roboRunInfo) {
		super(roboRunInfo);
	}

}
