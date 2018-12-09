/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.winevent.bean;

public class WinEventConstant {

	public final static int UNDEFINED = -1;

	/** 情報*/
	public final static String INFORMATION_STRING = "Information";
	public final static int INFORMATION_LEVEL = 0;	// 0 or 4
	public final static int INFORMATION_LEVEL0 = 0;
	public final static int INFORMATION_LEVEL4 = 4;
	public final static int INFORMATION_TYPE = 3;

	/** 警告*/
	public final static String WARNING_STRING = "Warning";
	public final static int WARNING_LEVEL = 3;
	public final static int WARNING_TYPE = 2;

	/** エラー*/
	public final static String ERROR_STRING = "Error";
	public final static int ERROR_LEVEL = 2;
	public final static int ERROR_TYPE = 1;

	/** 重大*/
	public final static String CRITICAL_STRING = "Critical";
	public final static int CRITICAL_LEVEL = 1;

	/** 詳細*/
	public final static String VERBOSE_STRING = "Verbose";
	public final static int VERBOSE_LEVEL = 5;

	/** 失敗の監査 */
	public final static String AUDIT_FAILURE_STRING = "Audit Failure";
	public final static String AUDIT_FAILURE_STRING_OLD = "FailureAudit";
	public final static long AUDIT_FAILURE_LONG = 4503599627370496l;
	public final static int AUDIT_FAILURE_TYPE = 4;

	/** 成功の監査 */
	public final static String AUDIT_SUCCESS_STRING = "Audit Success";
	public final static String AUDIT_SUCCESS_STRING_OLD = "SuccessAudit";
	public final static long AUDIT_SUCCESS_LONG = 9007199254740992l;
	public final static int AUDIT_SUCCESS_TYPE = 5;

	/** クラシック */
	public final static String CLASSIC_STRING = "Classic";
	public final static long CLASSIC_LONG = 36028797018963968l;

	/** 相関のヒント */
	public final static String CORRELATION_HINT_STRING = "Correlation Hint";
	public final static long CORRELATION_HINT_LONG = 18014398509481984l;

	/** 応答時間  */
	public final static String RESPONSE_TIME_STRING = "Response Time";
	public final static long RESPONSE_TIME_LONG = 281474976710656l;

	/** SQM  */
	public final static String SQM_STRING = "SQM";
	public final static long SQM_LONG = 2251799813685248l;

	/** WDIコンテキスト  */
	public final static String WDI_CONTEXT_STRING = "WDI Context";
	public final static long WDI_CONTEXT_LONG = 562949953421312l;

	/** WDI診断  */
	public final static String WDI_DIAG_STRING = "WDI Diag";
	public final static long WDI_DIAG_LONG = 1125899906842624l;

	private WinEventConstant() {
		throw new IllegalStateException("ConstClass");
	}
}
