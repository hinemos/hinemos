/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.msgfilter.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.msgfilter.IMsgFilterClientOption;
import com.clustercontrol.msgfilter.MsgFilterClientOptionManager;

public class MsgFilterClientUtil {

	private static Log logger = LogFactory.getLog(MsgFilterClientUtil.class);

	/**
	 * パースペクティブIDを取得
	 * 
	 * @return
	 */
	public static String getPerspectiveId() {
		IMsgFilterClientOption option = MsgFilterClientOptionManager.getInstance().getMsgFilterClientOption();

		if (option == null) {
			logger.warn("getPerspectiveId() : MsgFilter Option is null.");
			return "";
		}
		return option.getPerspectiveId();
	}
}
