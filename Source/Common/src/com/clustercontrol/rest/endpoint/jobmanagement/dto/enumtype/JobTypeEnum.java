/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype;

import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.rest.dto.EnumDto;

public enum JobTypeEnum implements EnumDto<Integer> {
	/** ツリーのトップ */COMPOSITE(JobConstant.TYPE_COMPOSITE),
	/** ジョブユニット(ジョブの種別) */ JOBUNIT(JobConstant.TYPE_JOBUNIT),
	/** ジョブネット(ジョブの種別) */JOBNET(JobConstant.TYPE_JOBNET),
	/** ジョブ(ジョブの種別) */JOB(JobConstant.TYPE_JOB),
	/** ファイル転送ジョブ(ジョブの種別) */FILEJOB(JobConstant.TYPE_FILEJOB),
	/** unreferable jobunit (ジョブの種別) */JOBUNIT_UNREFERABLE(JobConstant.TYPE_JOBUNIT_UNREFERABLE),
	/** 参照ジョブ(ジョブの種別) */ REFERJOB(JobConstant.TYPE_REFERJOB),
	/** マネージャ(ジョブの種別) */ MANAGER(JobConstant.TYPE_MANAGER),
	/** 参照ジョブネット(ジョブの種別) */ REFERJOBNET(JobConstant.TYPE_REFERJOBNET),
	/** 承認ジョブ(ジョブの種別) */APPROVALJOB(JobConstant.TYPE_APPROVALJOB),
	/** 監視ジョブ(ジョブの種別) */ MONITORJOB(JobConstant.TYPE_MONITORJOB),
	/** リソース制御ジョブ(ジョブの種別) */ RESOURCEJOB(JobConstant.TYPE_RESOURCEJOB),
	/** ジョブ連携送信ジョブ(ジョブの種別) */ JOBLINKSENDJOB(JobConstant.TYPE_JOBLINKSENDJOB),
	/** ジョブ連携待機ジョブ(ジョブの種別) */ JOBLINKRCVJOB(JobConstant.TYPE_JOBLINKRCVJOB),
	/** ファイルチェックジョブ(ジョブの種別) */ FILECHECKJOB(JobConstant.TYPE_FILECHECKJOB),
	/** RPAシナリオジョブ(ジョブの種別) */ RPAJOB(JobConstant.TYPE_RPAJOB);
	private final Integer code;

	private JobTypeEnum(final Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}
	
}
