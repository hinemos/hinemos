/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.queue.internal;

/**
 * ジョブ同時実行制御キューのアイテム(ジョブ)の状態を表します。
 * <pre>
 * STANDBY(初期) ←→ PENDING
 *   ↓
 * ACTIVE
 * </pre>
 * 
 * @since 6.2.0
 */
public enum JobQueueItemStatus {
	/** 実行 (実行領域内) */
	ACTIVE(1),
	/** 実行待機 (待機領域内) */
	STANDBY(2),
	/** 中断 (待機領域内) */
	PENDING(3);

	private final int id;
	
	private JobQueueItemStatus(int id) {
		this.id = id;
	}

	/**
	 * ID(DB内の数値表現)を返します。
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * ID(DB内の数値表現)に該当するenum値を返します。
	 * 
	 * @throws IllegalArgumentException 該当するenum値がありません。
	 */
	public static JobQueueItemStatus valueOf(int id) {
		for (JobQueueItemStatus v : values()) {
			if (v.getId() == id) return v;
		}
		throw new IllegalArgumentException("Undefined ID=" + id);
	}

}
