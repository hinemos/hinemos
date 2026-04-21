/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util.filemonitor;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Locale;

import com.clustercontrol.bean.PriorityConstant;

public interface FileMonitorConfig {
	public enum CarryoverFlushPolicy {
		TIMEOUT("timeout"),
		SPECIFIC_TIME("specifictime");

		private final String propertyValue;

		private CarryoverFlushPolicy(String propertyValue) {
			this.propertyValue = propertyValue;
		}

		public static CarryoverFlushPolicy fromProperty(String value) {
			if (value == null) {
				throw new IllegalArgumentException("policy is null");
			}
			String normalized = value.trim().toLowerCase(Locale.ENGLISH);
			for (CarryoverFlushPolicy policy : values()) {
				if (policy.propertyValue.equals(normalized)) {
					return policy;
				}
			}
			throw new IllegalArgumentException("unknown policy: " + value);
		}

		@Override
		public String toString() {
			return propertyValue;
		}
	}
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
	
	/**
	 * 監視項目の単位で時限区切りの監視を許容するかどうか
	 * @return true：許容する、false：許容しない
	 */
	public default boolean isCarryoverFlushSupported() {
		return false;
	}
	/**
	 * 先頭パターン指定時に時間区切りの監視を実施するかどうか
	 */
	public default boolean isCarryoverFlushEnabledForStartRegex() {
		return false;
	}
	/**
	 * 終端パターン指定時に時間区切りの監視を実施するかどうか
	 */
	public default boolean isCarryoverFlushEnabledForEndRegex() {
		return false;
	}
	/**
	 * 改行区切り指定時に時間区切りの監視を実施するかどうか
	 */
	public default boolean isCarryoverFlushEnabledForReturnCode() {
		return false;
	}
	/**
	 * 時間区切りの監視を実施した際にマネージャへ通知するかどうか
	 */
	public default boolean isCarryoverFlushNotifyEnabled() {
		return false;
	}

	public default int getCarryoverFlushNotifyPriority() {
		return PriorityConstant.TYPE_INFO;
	}

	public default long getCarryoverFlushTimeout() {
		return 0L;
	}

	public default LocalTime getCarryoverFlushSpecificTime() {
		return null;
	}
	public default ZoneId getCarryoverFlushSpecificTimeOffset() {
		return null;
	}

	public default CarryoverFlushPolicy getCarryoverFlushPolicy() {
		return null;
	}
}
