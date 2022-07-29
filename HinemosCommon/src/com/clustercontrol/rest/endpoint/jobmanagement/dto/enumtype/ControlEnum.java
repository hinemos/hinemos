/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum ControlEnum implements EnumDto<Integer> {
	/** 開始[即時] */START_AT_ONCE(OperationConstant.TYPE_START_AT_ONCE),
	/** 開始[中断解除] */START_SUSPEND(OperationConstant.TYPE_START_SUSPEND),
	/** 開始[スキップ解除] */START_SKIP (OperationConstant.TYPE_START_SKIP),
	/** 開始[保留解除] */START_WAIT(OperationConstant.TYPE_START_WAIT),
	/** 開始[強制実行] */START_FORCE_RUN(OperationConstant.TYPE_START_FORCE_RUN),

	/** 停止[コマンド] */STOP_AT_ONCE(OperationConstant.TYPE_STOP_AT_ONCE),
	/** 停止[中断] */STOP_SUSPEND(OperationConstant.TYPE_STOP_SUSPEND),
	/** 停止[スキップ] */STOP_SKIP(OperationConstant.TYPE_STOP_SKIP),
	/** 停止[保留] */STOP_WAIT(OperationConstant.TYPE_STOP_WAIT),
	/** 停止[状態変更] */STOP_MAINTENANCE(OperationConstant.TYPE_STOP_MAINTENANCE),
	/** 停止[状態指定] */STOP_SET_END_VALUE(OperationConstant.TYPE_STOP_SET_END_VALUE),
	/** 停止[状態指定](強制) */STOP_SET_END_VALUE_FORCE(OperationConstant.TYPE_STOP_SET_END_VALUE_FORCE),
	/** 停止[強制] */STOP_FORCE(OperationConstant.TYPE_STOP_FORCE),
	/** RPAシナリオのスクリーンショット取得 */RPA_SCREENSHOT(OperationConstant.TYPE_RPA_SCREENSHOT);

	private final Integer code;

	private ControlEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
	
}
