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

import com.clustercontrol.jobmanagement.rpa.bean.RoboResultInfo;
import com.clustercontrol.jobmanagement.rpa.bean.RoboRunInfo;

/**
 * RPAシナリオ実行完了後の処理行うハンドラの抽象クラス
 */
public abstract class AbstractHandler {
	/** ロガー */
	private static Log m_log = LogFactory.getLog(AbstractHandler.class);
	/** RPAツールエグゼキューター連携用ファイル出力先フォルダ */
	protected static final String roboFileDir = System.getProperty("hinemos.agent.rpa.dir");
	/** 処理完了後に実行する次のハンドラ */
	private AbstractHandler next;
	/** RPAシナリオ実行指示情報 */
	protected RoboRunInfo roboRunInfo;

	/**
	 * コンストラクタ
	 * 
	 * @param roboRunInfo
	 */
	public AbstractHandler(RoboRunInfo roboRunInfo) {
		this.roboRunInfo = roboRunInfo;
	}

	/**
	 * 次のハンドラを実行します。
	 * 
	 * @param roboResultInfo
	 *            RPAシナリオの実行結果情報
	 */
	public void handle(RoboResultInfo roboResultInfo) {
		if (next != null) {
			m_log.debug("handle() : call next handler=" + next.getClass() + ", roboResultInfo=" + roboResultInfo);
			next.handle(roboResultInfo);
		}
	}

	/**
	 * ハンドラを追加します。<br>
	 * ハンドラは追加された順に実行されます。
	 * 
	 * @param handler
	 */
	public void add(AbstractHandler handler) {
		if (next != null) {
			next.add(handler);
		} else {
			m_log.debug("add() : handler=" + handler.getClass());
			next = handler;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		AbstractHandler handler = this;
		while (handler != null) {
			sb.append(handler.getClass().getSimpleName());
			handler = handler.next;
			if (handler != null)
				sb.append("->");
		}
		return sb.toString();
	}
}
