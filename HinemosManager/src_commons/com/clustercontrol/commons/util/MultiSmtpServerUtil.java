/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.HinemosTime;

public class MultiSmtpServerUtil {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog(MultiSmtpServerUtil.class);

	/** SMTPサーバとロールの対応を保存するマップ **/
	private static final Map<String, List<Integer>> ROLE_SERVER_MAP;

	/** 各SMTPサーバの設定 **/
	private static final List<MailServerSettings> MAIL_SERVER_SETTINGS;

	/** 各SMTPサーバにて最後に送信失敗した時刻のリスト **/
	private static final List<Date> LAST_FAILURE_TIME_LIST;

	/** 各SMTPサーバごとの送信失敗後のクールタイムのリスト **/
	private static final Long[] FAILURE_COOLTIME;

	private static final Boolean SEND_ALL_SERVER;

	static {
		/*
		 * 追加のプロパティは mail.1.XXXX ～ mail.10.XXXX の10個
		 * mail.X系プロパティについては起動時の値を参照したいため、
		 * staticイニシャライザでプロパティを読み込んでstatic変数に保存しておく。
		 */
		List<Set<String>> roleSettingList = new ArrayList<Set<String>>();
		List<MailServerSettings> mailServerSettingList = new ArrayList<MailServerSettings>();
		List<Date> lastFailureTimeList = new ArrayList<Date>();
		List<Long> coolTimeList = new ArrayList<Long>();

		for (int i = 1; i < 11; i++) {
			m_log.debug("static initializer MultiSmtpServerUtil() : slot " + i);
			String str = HinemosPropertyCommon.mail_$_server_ownerroleid_list.getStringValue("" + i, "");
			m_log.debug("Slot " + i + " roleId list string : " + str);
			Set<String> set = new HashSet<String>(Arrays.asList(str.split(",")));
			set.remove(null);
			set.remove("");
			roleSettingList.add(set);

			MailServerSettings mailServerSetting = new MailServerSettings(i);
			mailServerSettingList.add(mailServerSetting);

			lastFailureTimeList.add(null);

			long coolTime = HinemosPropertyCommon.mail_$_server_failure_waittime.getNumericValue("" + i, 0L);
			m_log.debug("Slot " + i + " cooltime long : " + coolTime);
			// mail.X.server.failure.waittaimeの単位はminなのでmsecにしておく
			coolTimeList.add(coolTime * 60 * 1000);
		}

		MAIL_SERVER_SETTINGS = Collections.unmodifiableList(mailServerSettingList);
		LAST_FAILURE_TIME_LIST = Collections.synchronizedList(lastFailureTimeList);
		FAILURE_COOLTIME = new Long[coolTimeList.size()];
		coolTimeList.toArray(FAILURE_COOLTIME);
		SEND_ALL_SERVER = HinemosPropertyCommon.mail_enable_send_all_server.getBooleanValue();

		Map<String, List<Integer>> roleServerMap = new HashMap<String, List<Integer>>();
		// インターナルの通知は必ずデフォルトのSMTPサーバに送るように仕込んでおく
		roleServerMap.put("INTERNAL", new ArrayList<Integer>());
		roleServerMap.get("INTERNAL").add(0);
		for (int i = 0; i < roleSettingList.size(); i++) {
			Set<String> set = roleSettingList.get(i);
			if (set == null || set.isEmpty()) {
				continue;
			}

			for (String roleId : set) {
				if (roleServerMap.containsKey(roleId)) {
					roleServerMap.get(roleId).add(i + 1);
				} else {
					List<Integer> list = new ArrayList<Integer>();
					list.add(i + 1);
					roleServerMap.put(roleId, list);
				}
			}
		}

		ROLE_SERVER_MAP = Collections.unmodifiableMap(Collections.synchronizedMap(roleServerMap));
		m_log.debug(ROLE_SERVER_MAP);
	}

	public static Boolean isSendAll() {
		return SEND_ALL_SERVER;
	}

	/*
	 * 通知のオーナーロールIDから使用するSMTPサーバの一覧を返す
	 */
	public static List<Integer> getRoleServerList(String ownerRoldId) {
		return ROLE_SERVER_MAP.get(ownerRoldId);
	}

	/*
	 * indexは「mail.X.～～」のXをそのまま受け入れる。(X:1-10) 内部のListのindexは0-9なので変換してから返却する
	 */
	public static MailServerSettings getMailServerSettings(int index) {
		if (index < 1 || index > 10) {
			m_log.debug("getMailServerSettings(" + index + ") : index out of bounds, return null");
			return null;
		}
		return MAIL_SERVER_SETTINGS.get(index - 1);

	}

	/*
	 * indexは「mail.X.～～」のXをそのまま受け入れる。(X:1-10) 内部のListのindexは0-9なので変換してから返却する
	 */
	public static Date getLastFailureTime(int index) {
		if (index < 1 || index > 10) {
			m_log.debug("getLastFailureTime(" + index + ") : index out of bounds, return null");
			return null;
		}
		return LAST_FAILURE_TIME_LIST.get(index - 1);
	}

	/*
	 * indexは「mail.X.～～」のXをそのまま受け入れる。(X:1-10) 内部のListのindexは0-9なので変換してから処理をする
	 */
	public static void setLastFailureTime(int index, Date date) {
		if (index < 1 || index > 10) {
			m_log.debug("setLastFailureTime(" + index + ") : index out of bounds, do nothing");
			return;
		}
		LAST_FAILURE_TIME_LIST.set(index - 1, date);
	}

	/*
	 * indexは「mail.X.～～」のXをそのまま受け入れる。(X:1-10) 内部のListのindexは0-9なので変換してから処理をする
	 */
	public static void clearLastFailureTime(int index) {
		if (index < 1 || index > 10) {
			m_log.debug("clearLastFailureTime(" + index + ") : index out of bounds, do nothing");
			return;
		}
		LAST_FAILURE_TIME_LIST.set(index - 1, null);
	}

	/*
	 * indexは「mail.X.～～」のXをそのまま受け入れる。(X:1-10) 内部の配列のindexは0-9なので変換してから返却する
	 */
	public static Long getFailureCooltime(int index) {
		if (index < 1 || index > 10) {
			m_log.debug("getFailureCooltime(" + index + ") : index out of bounds, return 0L");
			return 0L;
		}
		return FAILURE_COOLTIME[index - 1];
	}

	/*
	 * indexは「mail.X.～～」のXをそのまま受け入れる。(X:1-10) 内部の配列のindexは0-9なので変換してから返却する
	 */
	public static Boolean isInCooltime(int index) {
		if (index < 1 || index > 10) {
			m_log.debug("isInCooltime(" + index + ") : index out of bounds, return false");
			return false;
		}
		Date now = HinemosTime.getDateInstance();
		Date last = getLastFailureTime(index);
		long wait = getFailureCooltime(index);
		m_log.debug("isInCooltime(" + index + ") : now " + now);
		m_log.debug("isInCooltime(" + index + ") : Server " + index + " lastFailure is " + last);
		m_log.debug("isInCooltime(" + index + ") : Server " + index + " failureCooltime is " + wait);

		if (last == null || wait < 1) {
			m_log.debug("isInCooltime(" + index + ") : Server " + index
					+ " has never failed or failure cooltime is invalid, return false");
			return false;
		}

		long sinceLast = now.getTime() - last.getTime();
		if (sinceLast > wait) {
			m_log.debug("isInCooltime(" + index + ") : Server " + index + " cooltime is over, return false");
			return false;
		}

		m_log.debug("isInCooltime(" + index + ") : Server " + index
				+ " has not had enough time since last failure, return true");
		return true;
	}

}
