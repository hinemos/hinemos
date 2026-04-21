/*
 * Copyright (c) 2025 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * RPAシナリオジョブによるスクリーンショットが存在しない場合に利用するException
 */
public class JobRpaScreenshotFileNotFound extends HinemosNotFound {

	private static final long serialVersionUID = -5559647358532498359L;

	public JobRpaScreenshotFileNotFound() {
		super();
	}
	
	public JobRpaScreenshotFileNotFound(String messages) {
		super(messages);
	}
	
	public JobRpaScreenshotFileNotFound(Throwable e) {
		super(e);
	}

	public JobRpaScreenshotFileNotFound(String messages, Throwable e) {
		super(messages, e);
	}
}
