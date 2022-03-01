/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.handler;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.jobmanagement.rpa.bean.RoboResultInfo;
import com.clustercontrol.jobmanagement.rpa.bean.RoboRunInfo;
import com.clustercontrol.jobmanagement.rpa.util.RoboFileManager;

/**
 * RPAシナリオの実行完了後に実行結果ファイルを出力するクラス
 */
public class ResultFileHandler extends AbstractHandler {
	/** ロガー */
	private static Log m_log = LogFactory.getLog(ResultFileHandler.class);

	/**
	 * コンストラクタ
	 * 
	 * @param roboInfo
	 */
	public ResultFileHandler(RoboRunInfo roboRunInfo) {
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
		m_log.debug("handle() : " + roboResultInfo);
		RoboFileManager roboFileManager = new RoboFileManager(roboFileDir);
		try {
			roboFileManager.write(roboResultInfo);
		} catch (IOException e) {
			m_log.error("handle() : write result file failed, " + e.getMessage(), e);
		}
		super.handle(roboResultInfo); // 次のハンドラの呼び出し
	}

}
