/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
	package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum StatusEnum implements EnumDto<Integer> {

	/** 実行予定(状態の種別) */SCHEDULED(StatusConstant.TYPE_SCHEDULED),
	/** 待機(状態の種別) */WAIT(StatusConstant.TYPE_WAIT),
	/** 保留中(状態の種別) */ RESERVING( StatusConstant.TYPE_RESERVING),
	/** スキップ(状態の種別) */SKIP( StatusConstant.TYPE_SKIP),
	/** 実行中(状態の種別) */RUNNING(StatusConstant.TYPE_RUNNING),
	/** 停止処理中(状態の種別) */STOPPING(StatusConstant.TYPE_STOPPING),
	/** 実行中(キュー待機) (状態の種別) **/ RUNNING_QUEUE(StatusConstant.TYPE_RUNNING_QUEUE),
	/** 中断(状態の種別) */ SUSPEND(StatusConstant.TYPE_SUSPEND),
	/** コマンド停止(状態の種別) */STOP(StatusConstant.TYPE_STOP),
	/** * 中断(キュー待機) (状態の種別) */ SUSPEND_QUEUE(StatusConstant.TYPE_SUSPEND_QUEUE),
	/** 終了(状態の種別) */END(StatusConstant.TYPE_END),
	/** 変更済(状態の種別) */ MODIFIED(StatusConstant.TYPE_MODIFIED),
	/** 終了(条件未達成) (状態の種別) */END_UNMATCH(StatusConstant.TYPE_END_UNMATCH),
	/** 終了(カレンダ) (状態の種別) */END_CALENDAR(StatusConstant.TYPE_END_CALENDAR),
	/** 終了(スキップ) (状態の種別) */ END_SKIP(StatusConstant.TYPE_END_SKIP),
	/** 終了(開始遅延) (状態の種別) */ END_START_DELAY(StatusConstant.TYPE_END_START_DELAY),
	/** 終了(終了遅延) (状態の種別) */ END_END_DELAY(StatusConstant.TYPE_END_END_DELAY),
	/** 終了(排他条件分岐) (状態の種別) */ END_EXCLUSIVE_BRANCH(StatusConstant.TYPE_END_EXCLUSIVE_BRANCH),
	/** 終了(キューサイズ超過) (状態の種別) */END_QUEUE_LIMIT(StatusConstant.TYPE_END_QUEUE_LIMIT),
	/** 終了(ファイル出力失敗) (状態の種別) */END_FAILED_OUTPUT(StatusConstant.TYPE_END_FAILED_OUTPUT),
	/** 起動失敗(状態の種別) */ERROR(StatusConstant.TYPE_ERROR),
	/** 未実行(管理対象外)) */NOT_MANAGED(StatusConstant.TYPE_NOT_MANAGED);
	private final Integer code;

	private StatusEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
	
}
