/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util.filemonitor;

import org.openapitools.client.model.AgtCalendarInfoResponse;

public abstract class AbstractFileMonitorInfoWrapper {
	/**
	 * IDを取得
	 * 
	 * @return ID
	 */
	public abstract String getId();

	/**
	 * ディレクトリを取得
	 * 
	 * @return ディレクトリ
	 */
	public abstract String getDirectory();

	/**
	 * ファイル名を取得
	 * 
	 * @return ファイル名
	 */
	public abstract String getFileName();

	/**
	 * ファイルのエンコードを取得
	 * 
	 * @return ファイルのエンコード
	 */
	public abstract String getFileEncoding();

	/**
	 * ファイルの改行コードを取得
	 * 
	 * @return ファイルの改行コード
	 */
	public abstract String getFileReturnCode();

	/**
	 * 登録日時を取得
	 * 
	 * @return 登録日時
	 */
	public abstract Long getRegDate();

	/**
	 * 更新日時を取得
	 * 
	 * @return 更新日時
	 */
	public abstract Long getUpdateDate();

	/**
	 * 最大読み取り文字数を取得
	 * 
	 * @return 最大読み取り文字数
	 */
	public abstract Integer getMaxStringLength();

	/**
	 * 先頭パターンの文字列取得
	 * 
	 * @return 先頭パターンの文字列
	 */
	public abstract String getStartRegexString();

	/**
	 * 終端パターンの文字列取得
	 * 
	 * @return 終端パターンの文字列
	 */
	public abstract String getEndRegexString();

	/**
	 * カレンダIDを取得
	 * 
	 * @return カレンダID
	 */
	public abstract String getCalendarId();

	/**
	 * カレンダ情報を取得
	 * 
	 * @return カレンダ情報
	 */
	public abstract AgtCalendarInfoResponse getCalendar();

	/**
	 * 監視の有効無効フラグを取得
	 * 
	 * @return 監視の有効無効フラグ
	 */
	public abstract Boolean getMonitorFlg();

	/**
	 * 収集の有効無効フラグを取得
	 * 
	 * @return 収集の有効無効フラグ
	 */
	public abstract Boolean getCollectorFlg();

	/**
	 * 収集の有効無効フラグを取得
	 * 
	 * @return 収集の有効無効フラグ
	 */
	public abstract boolean isMonitorJob();
}
