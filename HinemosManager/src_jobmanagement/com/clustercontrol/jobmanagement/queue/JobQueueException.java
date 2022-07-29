/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.queue;

/**
 * ジョブキュー(同時実行制御キュー)機能が投げる例外の基本クラスです。
 * <p>
 * 本クラスはHinemosException派生ではありません。
 * 本クラス及び派生クラスそのものを、JAX-WSを通じてHinemosマネージャの外へ伝播させないようにしてください。
 */
public class JobQueueException extends Exception {

	// 実装を変更したときのバージョン番号に合わせる。
	// {major(high)}_{major(low)}_{minor}_{patch}
	private static final long serialVersionUID = 6_02_00_00000000L;

	protected JobQueueException() {
		super();
	}

	protected JobQueueException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	protected JobQueueException(String message, Throwable cause) {
		super(message, cause);
	}
	
	protected JobQueueException(String message) {
		super(message);
	}

	protected JobQueueException(Throwable cause) {
		super(cause);
	}
}
