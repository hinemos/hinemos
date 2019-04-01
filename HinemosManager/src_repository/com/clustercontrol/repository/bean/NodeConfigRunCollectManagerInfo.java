/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

import java.io.Serializable;

import com.clustercontrol.repository.factory.NodeConfigRunCollectManager.RunStatus;

public class NodeConfigRunCollectManagerInfo implements Serializable {

	// カラム構成変更したら更新すること.
	private static final long serialVersionUID = -4324117941296918253L;

	/** 実行指示日時 */
	private Long instructedDate = null;

	/** 実行ステータス */
	private RunStatus runStatus = null;

	// コンストラクタ.
	/**
	 * 新規作成時のコンストラクタ.
	 */
	public NodeConfigRunCollectManagerInfo(Long instructedDate) {
		this.instructedDate = instructedDate;
		this.runStatus = RunStatus.SETTING;
	}

	// setterとgetter.
	/** 実行指示日時 */
	public Long getInstructedDate() {
		return instructedDate;
	}

	/** 実行指示日時 */
	public void setInstructedDate(Long instructedDate) {
		this.instructedDate = instructedDate;
	}

	/** 実行ステータス */
	public RunStatus getRunStatus() {
		return runStatus;
	}

	/** 実行ステータス */
	public void setRunStatus(RunStatus runStatus) {
		this.runStatus = runStatus;
	}

}
