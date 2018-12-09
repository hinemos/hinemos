/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;


/**
 * ジョブ種別（ユニット、ネット、ジョブ）の定数クラス<BR>
 *
 * @version 4.1.0
 * @since 1.0.0
 */
public class JobConstant {
	/** ツリーのトップ */
	public static final int TYPE_COMPOSITE = -1;

	/** ジョブユニット(ジョブの種別) */
	public static final int TYPE_JOBUNIT = 0;

	/** ジョブネット(ジョブの種別) */
	public static final int TYPE_JOBNET = 1;

	/** ジョブ(ジョブの種別) */
	public static final int TYPE_JOB = 2;

	/** ファイル転送ジョブ(ジョブの種別) */
	public static final int TYPE_FILEJOB = 3;

	/** unreferable jobunit (ジョブの種別) */
	public static final int TYPE_JOBUNIT_UNREFERABLE = 4;

	/** 参照ジョブ(ジョブの種別) */
	public static final int TYPE_REFERJOB = 5;

	/** マネージャ(ジョブの種別) */
	public static final int TYPE_MANAGER = 6;

	/** 参照ジョブネット(ジョブの種別) */
	public static final int TYPE_REFERJOBNET = 7;
	
	/** 承認ジョブ(ジョブの種別) */
	public static final int TYPE_APPROVALJOB = 8;

	/** 監視ジョブ(ジョブの種別) */
	public static final int TYPE_MONITORJOB = 9;

	/** ツリーのトップ */
	public static final String STRING_COMPOSITE = "";
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToMessageCode(int type) {
		if (type == TYPE_COMPOSITE) {
			return STRING_COMPOSITE;
		} else if (type == TYPE_JOBUNIT) {
			return "JOBUNIT";
		} else if (type == TYPE_JOBNET) {
			return "JOBNET";
		} else if (type == TYPE_JOB) {
			return "COMMAND_JOB";
		} else if (type == TYPE_FILEJOB) {
			return "FORWARD_FILE_JOB";
		} else if (type == TYPE_REFERJOB) {
			return "REFER_JOB";
		} else if (type == TYPE_REFERJOBNET) {
			return "REFER_JOBNET";
		} else if (type == TYPE_APPROVALJOB) {
			return "APPROVAL_JOB";
		} else if (type == TYPE_MONITORJOB) {
			return "MONITOR_JOB";
		}
		return STRING_COMPOSITE;
	}

	private JobConstant() {
		throw new IllegalStateException("ConstClass");
	}
}