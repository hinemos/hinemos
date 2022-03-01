/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.dto;

import com.clustercontrol.fault.InvalidSetting;

/**
 * リクエストDTO が実装するインタフェース
 */
public interface RequestDto {

	// フィールド間の相関チェックが必要な場合は中身を実装する
	public void correlationCheck() throws InvalidSetting;
}
