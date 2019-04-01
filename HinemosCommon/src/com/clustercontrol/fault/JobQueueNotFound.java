/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * ジョブキュー(同時実行制御キュー)が存在しない場合に利用するfalut
 * 
 * @version 6.2.0
 */
public class JobQueueNotFound extends HinemosException {

	// 実装を変更したときのバージョン番号に合わせる。 {major(high)}_{major(low)}_{minor}_{patch}
	private static final long serialVersionUID = 6_02_00_00000000L;

	private String queueId;

	public JobQueueNotFound(String messages, String queueId) {
		super(messages);
		this.queueId = queueId;
	}

	public JobQueueNotFound(String messages, Throwable e, String queueId) {
		super(messages, e);
		this.queueId = queueId;
	}

	public String getQueueId() {
		return queueId;
	}
	
}
