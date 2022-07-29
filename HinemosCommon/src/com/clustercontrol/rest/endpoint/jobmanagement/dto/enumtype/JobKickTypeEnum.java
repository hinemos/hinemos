/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.bean.JobKickConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum JobKickTypeEnum  implements EnumDto<Integer> {
	/** スケジュール */ SCHEDULE(JobKickConstant.TYPE_SCHEDULE),
	/** ファイルチェック */ FILECHECK(JobKickConstant.TYPE_FILECHECK),
	/** 手動実行 */ MANUAL(JobKickConstant.TYPE_MANUAL),
	/** ジョブ連携受信 */ JOBLINKRCV(JobKickConstant.TYPE_JOBLINKRCV);

	private final Integer code;

	private JobKickTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

}
