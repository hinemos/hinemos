/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck.monitor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.ActivationKeyConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.KeyCheck;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * ライセンスキーが評価版かのチェックをする処理の実装クラス
 */
public class ActivationKeyMonitor {
	
	private static Log m_log = LogFactory.getLog(ActivationKeyMonitor.class);
	private static final String PLUGIN_ID = HinemosModuleConstant.SYSYTEM_SELFCHECK;

	public void execute() {
		for (String option : ActivationKeyConstant.getOptions()) {
			m_log.info(option + " activation key check start.");
			LocalDate expireDate = KeyCheck.getExpireDate(option);
			String activationKeyFilename = KeyCheck.getActivationKeyFilename(expireDate, option);
			LocalDate now = LocalDate.now();

			// キャッシュしている日付がない場合は何もしない
			if (expireDate == null) {
				m_log.info(option + " is expireDate not exists.");
				continue;
			}
			
			// アクティベーションキーファイル名が見つからない場合は警告
			if (activationKeyFilename == null) {
				m_log.warn(option + " activation key not found.");
				continue;
			}

			int expireDateYYYYMM = expireDate.getYear() * 100 + expireDate.getMonthValue();
			// キャッシュしている日付が正式版の日付の場合は何もしない
			if (expireDateYYYYMM == ActivationKeyConstant.ACTIVATION_KEY_YYYYMM) {
				m_log.info(option + " is valid activation key.");
				continue;
			}

			m_log.debug("option = " + option + ", expireDate = " + expireDate + ", now = " + now);
			// 評価版の日付が現在日時より大きい場合
			if (expireDate.compareTo(now) >= 0) {
				// 評価版で動いている場合
				String expireDateStr = expireDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
				if (ActivationKeyConstant.TYPE_ENTERPRISE.equals(option)){
					AplLogger.put(PriorityConstant.TYPE_INFO, PLUGIN_ID,  MessageConstant.MESSAGE_SYS_014_SYS_SFC, new String[]{expireDateStr, activationKeyFilename});
				} else if (ActivationKeyConstant.TYPE_XCLOUD.equals(option)) {
					AplLogger.put(PriorityConstant.TYPE_INFO, PLUGIN_ID,  MessageConstant.MESSAGE_SYS_015_SYS_SFC, new String[]{expireDateStr, activationKeyFilename});
				}
				m_log.info( option + " is evaluation key.");
			} else {
				// 評価版かつ期限切れの場合
				if (ActivationKeyConstant.TYPE_ENTERPRISE.equals(option)) {
					AplLogger.put(PriorityConstant.TYPE_WARNING, PLUGIN_ID,  MessageConstant.MESSAGE_SYS_016_SYS_SFC, new String[]{activationKeyFilename});
				} else if (ActivationKeyConstant.TYPE_XCLOUD.equals(option)) {
					AplLogger.put(PriorityConstant.TYPE_WARNING, PLUGIN_ID,  MessageConstant.MESSAGE_SYS_017_SYS_SFC, new String[]{activationKeyFilename});
				}
				m_log.info( option + " is expired evaluation key.");
			}
		}
	}
}
