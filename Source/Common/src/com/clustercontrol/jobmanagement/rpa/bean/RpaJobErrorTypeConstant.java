/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.rpa.bean;

/**
 * RPAシナリオジョブの異常の種類を表す定数クラス
 */
public class RpaJobErrorTypeConstant {
	/** ログインされていない（RPAツールエグゼキューターが起動していない ） */
	public static final int NOT_LOGIN = 0;
	/** RPAツールが既に起動している */
	public static final int ALREADY_RUNNING = 1;
	/** RPAツールが異常終了した */
	public static final int ABNORMAL_EXIT = 2;
	/** シナリオファイルパスが存在しない */
	public static final int FILE_DOES_NOT_EXIST = 3;
	/** ログインに失敗しました */
	public static final int LOGIN_ERROR = 4;
	/** ログインセッションが複数あります */
	public static final int TOO_MANY_LOGIN_SESSION = 5;
	/** RPAシナリオエグゼキューターが起動していません */
	public static final int NOT_RUNNING_EXECUTOR = 6;
	/** エラーが発生しました */
	public static final int ERROR_OCCURRED = 7;
	/** ログインセッションが失われました */
	public static final int LOST_LOGIN_SESSION = 8;
	/** スクリーンショット取得失敗 */
	public static final int SCREENSHOT_FAILED = 9;
	/** それ以外 */
	public static final int OTHER = 99;
}
