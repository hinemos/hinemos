/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

/**
 * 自動登録の処理結果<br>
 * <br>
 * エラーの場合はExceptionをthrowする想定なのでステータスなし. <br>
 * <br>
 * - REGISTERED 自動登録完了<br>
 * - EXIST 既存で登録済(手動/自動両方)<br>
 * - INVALID 自動登録無効<br>
 * 
 */
public enum AutoRegisterStatus {
	REGISTERED,
	EXIST,
	INVALID
}
