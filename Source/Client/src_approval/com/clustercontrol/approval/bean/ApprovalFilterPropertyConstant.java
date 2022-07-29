/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.approval.bean;

/**
 * 承認ビューのフィルタ用文字列定義を定数として定義するクラス<BR
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class ApprovalFilterPropertyConstant {
	/** マネージャ */
	public static final String MANAGER = "manager";

	/** 承認状態 */
	public static final String APPROVAL_STATUS = "approvalStatus";

	/** 承認状態(承認待) */
	public static final String APPROVAL_STATUS_PENDING = "approvalStatusPending";

	/** 承認状態(未承認) */
	public static final String APPROVAL_STATUS_STILL = "approvalStatusStill";
	
	/** 承認状態(中断中) */
	public static final String APPROVAL_STATUS_SUSPEND = "approvalStatusSuspend";
	
	/** 承認状態(停止(取り下げ)) */
	public static final String APPROVAL_STATUS_STOP = "approvalStatusStop";
	
	/** 承認状態(承認済) */
	public static final String APPROVAL_STATUS_FINISHED = "approvalStatusFinished";
	
	/** 承認結果 */
	public static final String APPROVAL_RESULT = "approvalResult";

	/** セッションID */
	public static final String SESSION_ID = "sessionId";

	/** ジョブユニットID */
	public static final String JOBUNIT_ID = "jobunitId";

	/** ジョブID */
	public static final String JOB_ID = "jobId";

	/** ジョブ名 */
	public static final String JOB_NAME = "jobName";

	/** 実行ユーザ */
	public static final String RQUEST_USER = "requestUser";

	/** 承認ユーザ */
	public static final String APPROVAL_USER = "approvalUser";

	/** 承認依頼日時（開始） */
	public static final String START_FROM_DATE = "startFromDate";

	/** 承認依頼日時（終了） */
	public static final String START_TO_DATE = "startToDate";

	/** 承認完了日時（開始） */
	public static final String END_FROM_DATE = "endFromDate";

	/** 承認完了日時（終了） */
	public static final String END_TO_DATE = "endToDate";

	/** 承認依頼文 */
	public static final String RQUEST_SENTENCE = "requestSentence";

	/** コメント */
	public static final String COMMENT = "comment";

	/** 承認依頼日時 */
	public static final String START_DATE = "startDate";

	/** 承認完了日時 */
	public static final String END_DATE = "endDate";
}