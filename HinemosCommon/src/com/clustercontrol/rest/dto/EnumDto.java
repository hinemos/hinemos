/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.dto;

/**
 * リクエスト、レスポンスの DTO で利用する Enum はこのインタフェースを実装してください。
 */
public interface EnumDto<T> {
	public T getCode();
}
