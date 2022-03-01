/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.bean;

/**
 * ジョブの設定値に関する定数クラス<BR>
 * 値の範囲など共通で利用したい定数を定義するために利用する<BR>
 */
public class JobInfoParameterConstant {

	/** ジョブ制御タブ：試行間隔の上限 **/
	public static final int JOB_RETRY_INTERVAL_HIGH = 10;

	/** ファイルチェックジョブのタイムアウト（分）の下限 **/
	public static final int FILECHECK_TIMEOUT_LEN_MIN = 1;

	/** ファイルチェックジョブのタイムアウト（分）の上限 **/
	public static final int FILECHECK_TIMEOUT_LEN_MAX = 1440;

	/** ジョブスケジュール：一定間隔-繰り返し実行（分）の上限 **/
	public static final int JOBSCHEDULE_INTERVAL_EVERY_MINUTES_MAX = 1440;

}