/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

/**
 * エージェントのアップデート状況を示す定数。
 */
public enum AgentUpdateStatus {
	/** 済 : 最新版 */
	DONE,
	/** 未 : 他のいずれの状況にも該当しない場合 */
	NOT_YET,
	/** 再起動中: 再起動キューに入っている状態 */
	RESTARTING,
	/** 更新中: 更新キューに入っている状態 */
	UPDATING,
	/** 判定中: マネージャがプロファイル情報を未受領の状態 */
	UNKNOWN,
	/** 非対応: アップデート非対応のバージョン */
	UNSUPPORTED,
}
