/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.rpa.bean.RpaJobErrorTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum RpaJobErrorTypeEnum implements EnumDto<Integer> {

	/** ログインされていない（RPAツールエグゼキューターが起動していない ） */
	NOT_LOGIN(RpaJobErrorTypeConstant.NOT_LOGIN),
	/** RPAツールが既に起動している */
	ALREADY_RUNNING(RpaJobErrorTypeConstant.ALREADY_RUNNING),
	/** RPAツールが異常終了した */
	ABNORMAL_EXIT(RpaJobErrorTypeConstant.ABNORMAL_EXIT),
	/** シナリオファイルパスが存在しない */
	FILE_DOES_NOT_EXIST(RpaJobErrorTypeConstant.FILE_DOES_NOT_EXIST), 
	/** ログインに失敗しました */
	LOGIN_ERROR(RpaJobErrorTypeConstant.LOGIN_ERROR), 
	/** ログインセッションが複数あります */
	TOO_MANY_LOGIN_SESSION(RpaJobErrorTypeConstant.TOO_MANY_LOGIN_SESSION), 
	/** RPAシナリオエグゼキューターが起動していません */
	NOT_RUNNING_EXECUTOR(RpaJobErrorTypeConstant.NOT_RUNNING_EXECUTOR), 
	/** エラーが発生しました */
	ERROR_OCCURRED(RpaJobErrorTypeConstant.ERROR_OCCURRED), 
	/** ログインセッションが失われました */
	LOST_LOGIN_SESSION(RpaJobErrorTypeConstant.LOST_LOGIN_SESSION), 
	/** スクリーンショット取得失敗 */
	SCREENSHOT_FAILED(RpaJobErrorTypeConstant.SCREENSHOT_FAILED), 
	/** それ以外 */
	OTHER(RpaJobErrorTypeConstant.OTHER);

	private final Integer code;

	private RpaJobErrorTypeEnum(final Integer code) {
		this.code = code;
	}

	@Override
	public Integer getCode() {
		return code;
	}

}
