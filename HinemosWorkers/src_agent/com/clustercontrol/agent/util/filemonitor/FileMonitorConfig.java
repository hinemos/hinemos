/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util.filemonitor;

public interface FileMonitorConfig {

	/**
	 * ファイル監視スレッド数上限の取得
	 * 
	 * @return ファイル監視スレッド数上限
	 */
	public int getMaxThreads();

	/**
	 * スレッド名の取得
	 * 
	 * @return スレッド名
	 */
	public String getThreadName();

	/**
	 * ファイル監視間隔（ミリ秒）の取得
	 * 
	 * @return ファイル監視間隔（ミリ秒）
	 */
	public int getRunInterval();

	/**
	 * ファイル変更チェック期間設定（ミリ秒）の取得
	 * 
	 * @return ファイル変更チェック期間設定（ミリ秒）
	 */
	public int getUnchangedStatsPeriod();

	/**
	 * ファイル変更詳細チェック（冒頭データ比較）期間（ミリ秒）
	 * 
	 * @return ファイル変更詳細チェック（冒頭データ比較）期間（ミリ秒）
	 */
	public int getFirstPartDataCheckPeriod();

	/**
	 * ファイル変更詳細チェック（冒頭データ比較）サイズ（byte）の取得
	 * 
	 * @return ファイル変更詳細チェック（冒頭データ比較）サイズ（byte）
	 */
	public int getFirstPartDataCheckSize();

	/**
	 * 上限ファイルサイズ設定（byte）の取得
	 * 
	 * @return 上限ファイルサイズ設定（byte）
	 */
	public long getFileMaxSize();

	/**
	 * 上限ファイル数の取得
	 * 
	 * @return 上限ファイル数
	 */
	public long getFileMaxFiles();

	/**
	 * オリジナルメッセージのサイズ上限の取得
	 * 
	 * @return オリジナルメッセージのサイズ上限
	 */
	public int getFilMessageLength();

	/**
	 * オリジナルメッセージの読み込み行数上限の取得
	 * 
	 * @return オリジナルメッセージの読み込み行数上限
	 */
	public int getFilMessageLine();

	/**
	 * ファイル読込繰越データ長グファイル読込（バッファ単位取得、末尾まで連続）次回繰越データ最大長の取得
	 * 
	 * @return ファイル読込繰越データ長：ファイル読込（バッファ単位取得、末尾まで連続）次回繰越データ最大長
	 */
	public int getFileReadCarryOverLength();

	/**
	 * ログ先頭に定義するプログラム名の取得
	 * 
	 * @return ログ先頭に定義するプログラム名
	 */
	public String getProgram();

	/**
	 * 最大ファイル数超過通知を出す間隔
	 * @return
	 */
	public long getMaxFileNotifyInterval();
}
