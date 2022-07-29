/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.grafana.dto.enumtype;

import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum JobHistoryJobTriggerTypeEnum implements EnumDto<Integer> {
	/** 不明 */ UNKOWN(JobTriggerTypeConstant.TYPE_UNKOWN),
	/** スケジュール */ SCHEDULE(JobTriggerTypeConstant.TYPE_SCHEDULE),
	/** ファイルチェック */ FILECHECK(JobTriggerTypeConstant.TYPE_FILECHECK),
	/** 手動実行 */ MANUAL(JobTriggerTypeConstant.TYPE_MANUAL),
	/** 監視連動 */ MONITOR(JobTriggerTypeConstant.TYPE_MONITOR),
	/** ジョブ連携受信実行契機 */ JOBLINKRCV(JobTriggerTypeConstant.TYPE_JOBLINKRCV);

	private final Integer code;

	private JobHistoryJobTriggerTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

}
