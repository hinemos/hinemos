/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.rpa.bean.RoboResultInfo;
import com.clustercontrol.jobmanagement.rpa.bean.RoboRunInfo;
import com.clustercontrol.jobmanagement.rpa.util.CommandProxy;
import com.clustercontrol.util.CommandExecutor.CommandResult;

/**
 * RPAツールのプロセスを停止するハンドラクラス
 */
public class DestroyProcessHandler extends AbstractHandler {
	/** ロガー */
	private static Log m_log = LogFactory.getLog(DestroyProcessHandler.class);

	/**
	 * コンストラクタ
	 * 
	 * @param roboRunInfo
	 */
	public DestroyProcessHandler(RoboRunInfo roboRunInfo) {
		super(roboRunInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.clustercontrol.rpa.handler.AbstractHandler#handle(com.clustercontrol.
	 * jobmanagement.rpa.bean.RoboResultInfo)
	 */
	@Override
	public void handle(RoboResultInfo roboResultInfo) {
		// RPAのプロセスが終了しておらず、プロセスを終了する場合
		if (roboResultInfo.getReturnCode() == null && roboRunInfo.getDestroy()) {
			CommandResult ret = null;
			try {
				ret = CommandProxy.execute(roboRunInfo.getDestroyCommand());
			} catch (HinemosUnknown e) {
				m_log.error("handle() : command execution failed, " + e.getMessage(), e);
			}
			if (ret != null) {
				m_log.debug(
						"handle() : exitCode=" + ret.exitCode + ", stdout=" + ret.stdout + ", stderr=" + ret.stderr);
			}
			m_log.info("handle() : destory process");
		}
		super.handle(roboResultInfo); // 次のハンドラの呼び出し
	}
}
