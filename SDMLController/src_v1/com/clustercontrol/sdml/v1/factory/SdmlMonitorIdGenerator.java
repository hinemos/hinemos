/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.v1.factory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Singletons;

/**
 * 監視設定を自動作成する際に監視項目IDを自動生成するためのクラス
 *
 */
public class SdmlMonitorIdGenerator {
	private static Log logger = LogFactory.getLog(SdmlMonitorIdGenerator.class);

	// 監視項目IDの区切り文字
	private static final String MONITOR_ID_DELIMITER = "_";
	// 監視項目IDの最大長
	private static final int MONITOR_ID_LIMIT = 64;

	private Object lock = new Object();

	// 重複を避けるため使用した接尾辞を一時的に保持するリスト
	private List<String> postfixList = null;

	private SimpleDateFormat sdf = null;

	/**
	 * コンストラクタではなく、{@link Singletons#get(Class)}を使用してください。
	 */
	public SdmlMonitorIdGenerator() {
		postfixList = new ArrayList<>();
		sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		sdf.setTimeZone(HinemosTime.getTimeZone());
	}

	private External external = new External();

	// UnitTest向けに外部に依存する処理を書き換え可能とする
	static class External {
		public String getMonitorIdPrefix() {
			return HinemosPropertyCommon.sdml_monitor_setting_id_prefix.getStringValue();
		}

		public Date getNowDate() {
			return HinemosTime.getDateInstance();
		}
	}

	/**
	 * UnitTest向け<BR>
	 * 通常は使用しないこと<BR>
	 */
	public void setExternal(External external) {
		this.external = external;
	}

	/**
	 * 監視項目ID生成
	 * 
	 * @param applicationId
	 * @param args
	 *            アプリケーションIDの後に結合するパラメータ<BR>
	 *            変更を考慮し可変長とする<BR>
	 * @return
	 */
	public String generateMonitorId(String applicationId, String... params) {
		StringBuilder sb = new StringBuilder();

		// 接頭辞
		sb.append(getPrefix());

		// アプリケーションID
		sb.append(applicationId);

		// パラメータが指定されていれば以降に結合する
		if (params != null && params.length > 0) {
			for (String param : params) {
				if (param != null && !param.isEmpty()) {
					sb.append(MONITOR_ID_DELIMITER);
					sb.append(param.replaceAll(" ", "")); // 空白は除去
				}
			}
		}

		// 接尾辞
		String postfix = generatePostfix();
		if (sb.length() + postfix.length() > MONITOR_ID_LIMIT) {
			// 接尾辞を除いた文字数が監視項目IDの上限を超過している場合、超過している分は切り捨てる
			// 接尾辞によって最低限IDの重複は回避できているため情報の欠落は許容する
			sb.delete(MONITOR_ID_LIMIT - postfix.length(), sb.length());
		}
		sb.append(postfix);

		return sb.toString();
	}

	/**
	 * 接頭辞を取得する
	 * 
	 * @return
	 */
	private String getPrefix() {
		String pre = external.getMonitorIdPrefix();
		// Hinemosプロパティで空文字を設定した場合は空文字を返す
		if (pre == null || pre.isEmpty()) {
			return "";
		}
		return pre + MONITOR_ID_DELIMITER;
	}

	/**
	 * 重複がないように接尾辞を生成する<BR>
	 * この接尾辞はアプリケーションIDに依存せずマネージャ全体で重複しない<BR>
	 * これはアプリケーションIDも途中で切られる可能性があることを考慮している<BR>
	 * 
	 * @return
	 */
	private String generatePostfix() {
		try {
			synchronized (lock) {
				// ミリ秒まで含めた現在日時の文字列
				String now = sdf.format(external.getNowDate());

				// 使用済みの日時だった場合
				if (postfixList.contains(now)) {
					String inc = now;
					while (true) {
						// 重複がないように日時のミリ秒を1ミリ秒ずらす
						inc = (Long.parseLong(inc) + 1) + "";
						if (!postfixList.contains(inc)) {
							// ずらした日時が重複していなければその日時を返す
							postfixList.add(inc);
							return MONITOR_ID_DELIMITER + inc;
						}
					}

				} else {
					// 重複がない場合は現在日時をそのまま返却する
					// またこのタイミングで過去分の使用済みリストをクリアする
					// この動きは時間が不可逆であることを前提としている
					// 運用中に時刻を戻すことはレアケースであり、またその際に監視設定が残っており
					// ミリ秒まで一致するIDを生成することは非常に稀であると想定されるため、考慮しない
					postfixList.clear();
					postfixList.add(now);
					return MONITOR_ID_DELIMITER + now;
				}
			}
		} catch (Exception e) {
			// 念のため受けておくが通常到達しない想定
			logger.error("generatePostfix() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			return MONITOR_ID_DELIMITER + "YYYYMMDDHHMMSSSSS";
		}
	}

}
