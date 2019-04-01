/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.util;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.analytics.bean.IntegrationComparisonMethod;
import com.clustercontrol.analytics.model.CorrelationCheckInfo;
import com.clustercontrol.analytics.model.IntegrationCheckInfo;
import com.clustercontrol.analytics.model.IntegrationConditionInfo;
import com.clustercontrol.analytics.model.LogcountCheckInfo;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.RunInterval;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.binary.bean.BinaryConstant;
import com.clustercontrol.binary.bean.BinarySearchBean;
import com.clustercontrol.binary.model.BinaryCheckInfo;
import com.clustercontrol.binary.model.BinaryPatternInfo;
import com.clustercontrol.binary.model.PacketCheckInfo;
import com.clustercontrol.binary.util.BinaryBeanUtil;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.custom.bean.CustomConstant;
import com.clustercontrol.custom.model.CustomCheckInfo;
import com.clustercontrol.customtrap.model.CustomTrapCheckInfo;
import com.clustercontrol.fault.CollectorNotFound;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.http.model.HttpCheckInfo;
import com.clustercontrol.http.model.HttpScenarioCheckInfo;
import com.clustercontrol.http.model.Page;
import com.clustercontrol.http.model.Variable;
import com.clustercontrol.http.util.GetHttpResponse;
import com.clustercontrol.jmx.model.JmxCheckInfo;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.logfile.model.LogfileCheckInfo;
import com.clustercontrol.monitor.run.bean.MonitorNumericType;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.performance.monitor.model.PerfCheckInfo;
import com.clustercontrol.ping.model.PingCheckInfo;
import com.clustercontrol.port.model.PortCheckInfo;
import com.clustercontrol.process.model.ProcessCheckInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.snmp.model.SnmpCheckInfo;
import com.clustercontrol.snmptrap.model.TrapCheckInfo;
import com.clustercontrol.snmptrap.model.TrapValueInfo;
import com.clustercontrol.snmptrap.model.TrapValueInfoPK;
import com.clustercontrol.snmptrap.model.VarBindPattern;
import com.clustercontrol.sql.model.SqlCheckInfo;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.winevent.model.WinEventCheckInfo;
import com.clustercontrol.winservice.model.WinServiceCheckInfo;

/**
 * 監視設定チェッククラス
 * 
 * @version 6.1.0
 */
public class MonitorValidator {

	private static Log m_log = LogFactory.getLog( MonitorValidator.class );

	/**
	 * 監視設定(MonitorInfo)の妥当性チェック
	 * @param monitorInfo
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateMonitorInfo(MonitorInfo monitorInfo) throws InvalidSetting, InvalidRole {

		// 監視共通
		validateMonitorCommonSettings(monitorInfo);

		// 監視種別のチェック
		int monitorType = monitorInfo.getMonitorType();
		if(monitorType == MonitorTypeConstant.TYPE_TRUTH){
			validateMonitorTruthSettings(monitorInfo);
		}else if(monitorType == MonitorTypeConstant.TYPE_NUMERIC){
			validateMonitorNumericSettings(monitorInfo);
		}else if(monitorType == MonitorTypeConstant.TYPE_STRING){
			validateMonitorStringSettings(monitorInfo);
		}else if(monitorType == MonitorTypeConstant.TYPE_TRAP){
			// validateSnmptrap() で纏めて validate。
		}else if(monitorType == MonitorTypeConstant.TYPE_SCENARIO){
			// validateHttpScenario() で纏めて validate。
		} else if (monitorType == MonitorTypeConstant.TYPE_BINARY) {
			validateMonitorBinarySettings(monitorInfo);
		}else{
			InvalidSetting e = new InvalidSetting("validateMonitorInfo() Invalid Monitor Type. monitorType = "
					+ monitorInfo.getMonitorType());
			m_log.info("validateMonitorInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// 監視種別ID(プラグインID)のチェック
		String monitorTypeId = monitorInfo.getMonitorTypeId();
		if(HinemosModuleConstant.MONITOR_AGENT.equals(monitorTypeId)){
			validateHinemosAgent(monitorInfo);
		}else if (HinemosModuleConstant.MONITOR_HTTP_N.equals(monitorTypeId)) {
			validateHttpNumeric(monitorInfo);
		}else if (HinemosModuleConstant.MONITOR_HTTP_S.equals(monitorTypeId)) {
			validateHttp(monitorInfo);
		}else if (HinemosModuleConstant.MONITOR_HTTP_SCENARIO.equals(monitorTypeId)) {
			validateHttpScenario(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorTypeId)){
			validatePerformance(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_PING.equals(monitorTypeId)){
			validatePing(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_PORT.equals(monitorTypeId)){
			validatePort(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_PROCESS.equals(monitorTypeId)){
			validateProcess(monitorInfo);
		}else if (HinemosModuleConstant.MONITOR_SNMP_N.equals(monitorTypeId)){
			validateSnmpNumeric(monitorInfo);
		}else if (HinemosModuleConstant.MONITOR_SNMP_S.equals(monitorTypeId)) {
			validateSnmp(monitorInfo);
		}else if (HinemosModuleConstant.MONITOR_SQL_N.equals(monitorTypeId)) {
			validateSqlNumeric(monitorInfo);
		}else if (HinemosModuleConstant.MONITOR_SQL_S.equals(monitorTypeId)) {
			validateSql(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorTypeId)){
			validateSystemlog(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorTypeId)){
			validateLogfile(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_LOGCOUNT.equals(monitorTypeId)){
			validateLogcount(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_CUSTOM_N.equals(monitorTypeId)){
			validateCustom(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_CUSTOM_S.equals(monitorTypeId)){
			validateCustomString(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_SNMPTRAP.equals(monitorTypeId)){
			validateSnmptrap(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_WINSERVICE.equals(monitorTypeId)){
			validateWinService(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_WINEVENT.equals(monitorTypeId)){
			validateWinEvent(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_JMX.equals(monitorTypeId)){
			validateJMX(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N.equals(monitorTypeId)){
			validateCustomTrap(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S.equals(monitorTypeId)){
			validateCustomTrapString(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_CORRELATION.equals(monitorTypeId)){
			validateCorrelation(monitorInfo);
		}else if(HinemosModuleConstant.MONITOR_INTEGRATION.equals(monitorTypeId)){
			validateIntegration(monitorInfo);
		} else if (HinemosModuleConstant.MONITOR_BINARYFILE_BIN.equals(monitorTypeId)) {
			validateBinaryContinuous(monitorInfo);
		} else if (HinemosModuleConstant.MONITOR_PCAP_BIN.equals(monitorTypeId)) {
			validatePcap(monitorInfo);
		}else {

			// クラウド管理オプションの専用肝機能追加のため、本例外処理をコメントアプトする

			/*
			InvalidSetting e = new InvalidSetting("Invalid monitorTypeId. monitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validateMonitorInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
			*/
		}

	}


	/**
	 * 監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	private static void validateMonitorCommonSettings(MonitorInfo monitorInfo) throws InvalidSetting, InvalidRole {

		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateMonitorCommonSettings() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateMonitorCommonSettings() monitorId = " + monitorInfo.getMonitorId());

		//
		// 共通項目を対象とする。ただし、監視間隔については各監視機能で実装する(トラップ系の監視があるため)
		// 数値監視、文字列監視、真偽値監視は各継承したクラスで実装する
		//

		// monitorId
		if (monitorInfo.getMonitorId() == null || monitorInfo.getMonitorId().length() == 0) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MONITOR_ID.getMessage());
			m_log.info("validateMonitorCommonSettings() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateId(MessageConstant.MONITOR_ID.getMessage(), monitorInfo.getMonitorId(), 64);

		// monitorTypeId
		// monitorType

		// クラウド管理オプションの専用機能追加のため、本例外処理をコメントアプトする
		/*
		boolean flag = true;
		for (ArrayList<Object> a : MonitorTypeMstConstant.getListAll()) {
			if (a.get(0).equals(monitorInfo.getMonitorTypeId()) &&
					a.get(1).equals(monitorInfo.getMonitorType())) {
				flag = false;
				break;
			}
		}
		if (flag) {
			InvalidSetting e = new InvalidSetting("Invalid MonitorType. monitorTyeId(pluginId) = " + monitorInfo.getMonitorTypeId()
					+ ", monitorType = " + monitorInfo.getMonitorType());
			m_log.info("validateMonitorCommonSettings() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		*/

		// description : not implemented
		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(),
				monitorInfo.getDescription(), false, 0, 256);

		// ownerRoleId
		CommonValidator.validateOwnerRoleId(monitorInfo.getOwnerRoleId(), true,
				monitorInfo.getMonitorId(), HinemosModuleConstant.MONITOR);

		// facilityId
		if(monitorInfo.getFacilityId() == null || "".equals(monitorInfo.getFacilityId())){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE.getMessage());
			m_log.info("validateMonitorCommonSettings() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}else{
			try {
				FacilityTreeCache.validateFacilityId(monitorInfo.getFacilityId(), monitorInfo.getOwnerRoleId(), false);
			} catch (FacilityNotFound e) {
				throw new InvalidSetting(e.getMessage(), e);
			}
		}

		// runInterval : not implemented
		if(monitorInfo.getRunInterval() != RunInterval.TYPE_SEC_30.toSec()
				&& monitorInfo.getRunInterval() != RunInterval.TYPE_MIN_01.toSec()
				&& monitorInfo.getRunInterval() != RunInterval.TYPE_MIN_05.toSec()
				&& monitorInfo.getRunInterval() != RunInterval.TYPE_MIN_10.toSec()
				&& monitorInfo.getRunInterval() != RunInterval.TYPE_MIN_30.toSec()
				&& monitorInfo.getRunInterval() != RunInterval.TYPE_MIN_60.toSec()){

			// if polling type monitoring
			if(!HinemosModuleConstant.MONITOR_SNMPTRAP.equals(monitorInfo.getMonitorTypeId()) &&
					!HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorInfo.getMonitorTypeId()) &&
					!HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorInfo.getMonitorTypeId()) &&
					!HinemosModuleConstant.MONITOR_WINEVENT.equals(monitorInfo.getMonitorTypeId()) && 
					!HinemosModuleConstant.MONITOR_CUSTOMTRAP_N.equals(monitorInfo.getMonitorTypeId()) &&
					!HinemosModuleConstant.MONITOR_CUSTOMTRAP_S.equals(monitorInfo.getMonitorTypeId()) &&
					!HinemosModuleConstant.MONITOR_BINARYFILE_BIN.equals(monitorInfo.getMonitorTypeId()) &&
					!HinemosModuleConstant.MONITOR_PCAP_BIN.equals(monitorInfo.getMonitorTypeId())
					){
				InvalidSetting e = new InvalidSetting("RunInterval is not 1 min / 5 min / 10 min / 30 min / 60 min.");
				m_log.info("validateMonitorCommonSettings() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		// delayTime : not implemented
		// triggerType : not implemented

		// calendarId
		CommonValidator.validateCalenderId(monitorInfo.getCalendarId(), false, monitorInfo.getOwnerRoleId());

		// failurePriority : not implemented

		// application
		if(monitorInfo.getMonitorFlg()){
			CommonValidator.validateString(MessageConstant.APPLICATION.getMessage(),
					monitorInfo.getApplication(), true, 1, 64);
		} else {
			CommonValidator.validateString(MessageConstant.APPLICATION.getMessage(),
					monitorInfo.getApplication(), false, 0, 64);
		}

		// notifyGroupId : not implemented

		// notifyId
		if(monitorInfo.getNotifyRelationList() != null
				&& monitorInfo.getNotifyRelationList().size() > 0){
			for(NotifyRelationInfo notifyInfo : monitorInfo.getNotifyRelationList()){
				CommonValidator.validateNotifyId(notifyInfo.getNotifyId(), true, monitorInfo.getOwnerRoleId());
			}
		}

		// notifyId(将来予測用)
		if(monitorInfo.getPredictionNotifyRelationList() != null
				&& monitorInfo.getPredictionNotifyRelationList().size() > 0){
			for(NotifyRelationInfo predictionNotifyInfo : monitorInfo.getPredictionNotifyRelationList()){
				CommonValidator.validateNotifyId(predictionNotifyInfo.getNotifyId(), true, monitorInfo.getOwnerRoleId());
			}
		}

		// notifyId(変化点監視用)
		if(monitorInfo.getChangeNotifyRelationList() != null
				&& monitorInfo.getChangeNotifyRelationList().size() > 0){
			for(NotifyRelationInfo changeNotifyInfo : monitorInfo.getChangeNotifyRelationList()){
				CommonValidator.validateNotifyId(changeNotifyInfo.getNotifyId(), true, monitorInfo.getOwnerRoleId());
			}
		}

		// monitorFlg : not implemented
		// collectorFlg
		if(monitorInfo.getCollectorFlg()){
			// 収集蓄積機能実装に伴い、文字列監視やトラップ監視も収集可能にする
			if(monitorInfo.getMonitorType() == MonitorTypeConstant.TYPE_TRUTH){
				InvalidSetting e = new InvalidSetting("CollectorFlg is true. but this monitorType is truth.");
				m_log.info("validateMonitorCommonSettings() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

	}

	/**
	 * 文字列用監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateMonitorStringSettings(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateMonitorStringSettings() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateMonitorStringSettings() monitorId = " + monitorInfo.getMonitorId());

		List<MonitorStringValueInfo> stringValueInfoList = monitorInfo.getStringValueInfo();
		if(stringValueInfoList == null || stringValueInfoList.size() == 0){
			// 収集蓄積機能実装に当たり、監視を設定していなくても、収集が実行できるようにする。
//			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_REGEX.getMessage());
//			m_log.info("validateMonitorStringSettings() : "
//					+ e.getClass().getSimpleName() + ", " + e.getMessage());
//			throw e;
		}else{
			int orderNo = 0;
			for(MonitorStringValueInfo info : stringValueInfoList){

				// monitorId : not implemented

				// orderNo
				++orderNo;
				// monitorId : not implemented
				// description
				String description = info.getDescription();
				if (description != null) {
					CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(), description, true, 0, 256);
				}
				// processType : not implemented
				// priority : not implemented

				// message
				if (info.getProcessType()) {
					CommonValidator.validateString(MessageConstant.MESSAGE.getMessage(), info.getMessage(), true, 1,
							256);
				} else {
					CommonValidator.validateString(MessageConstant.MESSAGE.getMessage(), info.getMessage(), false, 0,
							256);
				}

				// pattern
				if (info.getPattern() == null) {
					InvalidSetting e = new InvalidSetting("Pattern is not defined. monitorId = "
							+ monitorInfo.getMonitorId() + ", orderNo = " + orderNo);
					m_log.info("validateMonitorStringSettings() : " + e.getClass().getSimpleName() + ", "
							+ e.getMessage());
					throw e;
				} else if ("".equals(info.getPattern()) && info.getProcessType()) {
					InvalidSetting e = new InvalidSetting("Pattern is empty string. monitorId = "
							+ monitorInfo.getMonitorId() + ", orderNo = " + orderNo);
					m_log.info("validateMonitorStringSettings() : " + e.getClass().getSimpleName() + ", "
							+ e.getMessage());
					throw e;
				} else {
					CommonValidator.validateString(MessageConstant.PATTERN_MATCHING_EXPRESSION.getMessage(),
							info.getPattern(), true, 1, 1024);
				}
				try{
					Pattern.compile(info.getPattern());
				}
				catch(PatternSyntaxException e){
					InvalidSetting e1 = new InvalidSetting("Pattern is not regular expression. monitorId = "
							+ monitorInfo.getMonitorId() + ", orderNo = " + orderNo, e);
					m_log.info("validateMonitorStringSettings() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				}
				// caseSensitivityFlg : not implemented

				// validFlg : not implemented
			}
		}
	}

	/**
	 * バイナリ監視設定(MonitorInfo)の共通項目の妥当性チェック
	 * 
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateMonitorBinarySettings(MonitorInfo monitorInfo) throws InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// --監視設定全体.
		if (monitorInfo == null) {
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.error(methodName + " : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug(methodName + " monitorId = " + monitorInfo.getMonitorId());

		// 監視無効の場合はチェック終了(監視/収集どちらも無効のダミーデータは入力チェックしない).
		if (!monitorInfo.getMonitorFlg()) {
			return;
		}

		// --監視欄(監視有効の場合のみ).
		// フィルタ条件.
		List<BinaryPatternInfo> binaryProvisionList = monitorInfo.getBinaryPatternInfo();
		if (binaryProvisionList == null || binaryProvisionList.size() == 0) {
			// フィルタ条件が設定されてない場合.
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_FILTER.getMessage());
			m_log.info(methodName + " : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		} else {
			int orderNo = 0;
			for (BinaryPatternInfo patternInfo : binaryProvisionList) {
				// orderNo
				++orderNo;

				// monitorId : not implemented

				// description
				String description = patternInfo.getDescription();
				if (description != null) {
					CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(), description, true, 0, 256);
				}

				// 16進数の検索文字列と検索方式を取得.
				BinarySearchBean binarySearchBean = BinaryBeanUtil.getSearchBean(patternInfo.getGrepString());

				// 検索文字列 : 必須・DB入力チェック
				if (patternInfo.getGrepString() == null) {
					InvalidSetting e = new InvalidSetting("Pattern is not defined. monitorId = "
							+ monitorInfo.getMonitorId() + ", orderNo = " + orderNo);
					m_log.info("validateMonitorStringSettings() : " + e.getClass().getSimpleName() + ", "
							+ e.getMessage());
					throw e;
				} else if ("".equals(patternInfo.getGrepString()) && patternInfo.getProcessType()) {
					InvalidSetting e = new InvalidSetting("Pattern is empty string. monitorId = "
							+ monitorInfo.getMonitorId() + ", orderNo = " + orderNo);
					m_log.info("validateMonitorStringSettings() : " + e.getClass().getSimpleName() + ", "
							+ e.getMessage());
					throw e;
				} else {
					CommonValidator.validateString(MessageConstant.PATTERN_MATCHING_EXPRESSION.getMessage(),
							patternInfo.getGrepString(), true, 1, 1024);
				}

				// 検索文字列: 16進数検索の文字列チェック.
				if (binarySearchBean.getSearchType() == BinaryConstant.SearchType.HEX) {
					if (binarySearchBean.getOnlyHexString() == null) {
						InvalidSetting e = new InvalidSetting(
								MessageConstant.MESSAGE_HUB_BINARY_SEARCH_HEX_INVALID.getMessage());
						m_log.error(methodName + " : " + e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
				}

				// エンコーディング : 文字列検索の場合と16進数検索でチェック内容が分かれる.
				if (binarySearchBean.getSearchType() != BinaryConstant.SearchType.HEX) {
					// エンコーディング : 文字列検索の場合必須・妥当なエンコーディング名か.
					CommonValidator.validateString(MessageConstant.JOB_SCRIPT_ENCODING.getMessage(),
							patternInfo.getEncoding(), true, 1, 32);
					try {
						Charset.forName(patternInfo.getEncoding());
					} catch (IllegalCharsetNameException exception) {
						InvalidSetting e = new InvalidSetting(
								MessageConstant.MESSAGE_INPUT_CORRECT_CHARSET.getMessage(), exception);
						m_log.info(methodName + " : " + e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					} catch (UnsupportedCharsetException exception) {
						String[] messageArgs = new String[] { patternInfo.getEncoding() };
						InvalidSetting e = new InvalidSetting(
								MessageConstant.MESSAGE_INPUT_SUPPORT_CHARSET.getMessage(messageArgs), exception);
						m_log.info(methodName + " : " + e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
				} else {
					// エンコーディング : 16進数検索の場合は基本未入力なのでDBinputチェック.
					CommonValidator.validateString(MessageConstant.JOB_SCRIPT_ENCODING.getMessage(),
							patternInfo.getEncoding(), false, 0, 32);
				}
				// processType : not implemented
				// priority : not implemented

				// message
				if (patternInfo.getProcessType()) {
					CommonValidator.validateString(MessageConstant.MESSAGE.getMessage(), patternInfo.getMessage(), true,
							1, 256);
				} else {
					CommonValidator.validateString(MessageConstant.MESSAGE.getMessage(), patternInfo.getMessage(),
							false, 0, 256);
				}

				// validFlg : not implemented
			}
		}
	}

	/**
	 * バイナリ監視設定(MonitorInfo)のバイナリファイル監視の場合の独自項目の妥当性チェック
	 * 
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateBinaryContinuous(MonitorInfo monitorInfo) throws InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// ----ファイル全体・増分共通項目のチェック
		// --バイナリ監視基本項目のチェック(条件欄).
		BinaryCheckInfo binaryInfo = monitorInfo.getBinaryCheckInfo();
		if (binaryInfo == null) {
			InvalidSetting e = new InvalidSetting("BinaryCheckInfo is not defined.");
			m_log.error(methodName + " : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// 収集方式 : 必須・DB入力チェック.
		CommonValidator.validateString(MessageConstant.COLLECT_TYPE.getMessage(), binaryInfo.getCollectType(), true, 1,
				64);

		// ディレクトリ : 必須・DB入力チェック
		CommonValidator.validateString(MessageConstant.DIRECTORY.getMessage(), binaryInfo.getDirectory(), true, 1,
				1024);

		// ファイル名 : 必須・DB入力チェック・正規表現チェック
		CommonValidator.validateString(MessageConstant.FILE_NAME.getMessage(), binaryInfo.getFileName(), true, 1, 1024);
		try {
			Pattern.compile(binaryInfo.getFileName());
		} catch (PatternSyntaxException exception) {
			String[] messageArgs = new String[] { binaryInfo.getFileName() };
			InvalidSetting e = new InvalidSetting(
					MessageConstant.MESSAGE_INPUT_REGULAR_EXPRESSION.getMessage(messageArgs), exception);
			m_log.info(methodName + " : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// -----収集方式:ファイル全体の場合に必要なチェックはここまでなので終了.
		if (BinaryConstant.COLLECT_TYPE_WHOLE_FILE.equals(binaryInfo.getCollectType())) {
			return;
		}

		// ----収集方式:増分のみの場合のチェック.
		// レコード分割方法(画面項目「データ構造」で時間区切りかそれ以外か) : 必須(空文字は許容)・DB入力チェック.
		CommonValidator.validateString(MessageConstant.DATA_STRUCTURE.getMessage(), binaryInfo.getCutType(), true, 0,
				64);

		// ----レコード分割方法:時間区切りの場合のチェック.
		// 監視間隔 : 時間区切りの場合は必須.
		if (BinaryConstant.CUT_TYPE_INTERVAL.equals(binaryInfo.getCutType())) {
			if (monitorInfo.getRunInterval() != RunInterval.TYPE_SEC_30.toSec()
					&& monitorInfo.getRunInterval() != RunInterval.TYPE_MIN_01.toSec()
					&& monitorInfo.getRunInterval() != RunInterval.TYPE_MIN_05.toSec()
					&& monitorInfo.getRunInterval() != RunInterval.TYPE_MIN_10.toSec()
					&& monitorInfo.getRunInterval() != RunInterval.TYPE_MIN_30.toSec()
					&& monitorInfo.getRunInterval() != RunInterval.TYPE_MIN_60.toSec()) {
				// if polling type monitoring
				InvalidSetting e = new InvalidSetting(
						"RunInterval is not 30 sec / 1 min / 5 min / 10 min / 30 min / 60 min.");
				m_log.info(methodName + " : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// ----レコード分割方法:時間区切りの場合は必要なチェックがここまでなので終了.
			return;
		}

		// ----レコード分割方法:レコード長指定の場合のチェック.
		// タグ種類(プリセット名) : null可・DB入力チェック.
		CommonValidator.validateString(MessageConstant.DATA_STRUCTURE.getMessage(), binaryInfo.getTagType(), false, 0,
				1024);

		// ファイルヘッダサイズ : null可・DB入力チェック.
		CommonValidator.validateLong(MessageConstant.FILE_HEADER_SIZE.getMessage(), binaryInfo.getFileHeadSize(), 0,
				Long.MAX_VALUE);

		// レコード長指定方法(可変長/固定長) : 必須・DB入力チェック.
		CommonValidator.validateString(MessageConstant.LENGTH_TYPE.getMessage(), binaryInfo.getLengthType(), true, 1, 64);

		// レコードサイズ : 固定長データの場合必須・DB入力チェック.
		if (BinaryConstant.LENGTH_TYPE_FIXED.equals(binaryInfo.getLengthType())) {
			CommonValidator.validateInt(MessageConstant.RECORD_SIZE.getMessage(), binaryInfo.getRecordSize(), 1,
					Integer.MAX_VALUE);
		} else {
			CommonValidator.validateInt(MessageConstant.RECORD_SIZE.getMessage(), binaryInfo.getRecordSize(), 0,
					Integer.MAX_VALUE);
		}

		// レコードヘッダサイズ : 可変長データの場合必須・DB入力チェック
		// サイズ位置 : 可変長データの場合必須・DB入力チェック・レコードヘッダ内の指定か.
		// サイズ表現バイト長 : 可変長データの場合必須・0～4byte・DB入力チェック.
		if (BinaryConstant.LENGTH_TYPE_VARIABLE.equals(binaryInfo.getLengthType())) {
			CommonValidator.validateInt(MessageConstant.RECORD_HEADER_SIZE.getMessage(), binaryInfo.getRecordHeadSize(), 1,
					Integer.MAX_VALUE);
			CommonValidator.validateInt(MessageConstant.RECORD_SIZE_POSITION.getMessage(), binaryInfo.getSizePosition(),
					1, Integer.MAX_VALUE);
			CommonValidator.validateInt(MessageConstant.RECORD_SIZE_BYTE_LENGTH.getMessage(),
					binaryInfo.getSizeLength(), 1, 4);
			// サイズ表現バイナリがレコードヘッダ内の指定となっているか.
			if((binaryInfo.getSizePosition() > binaryInfo.getRecordHeadSize()) 
					|| (binaryInfo.getSizePosition() + binaryInfo.getSizeLength() -1) > binaryInfo.getRecordHeadSize()){
				String[] messageArgs = new String[] { MessageConstant.RECORD_SIZE_BINARY.getMessage(),
						MessageConstant.RECORD_HEADER.getMessage() };
				InvalidSetting e = new InvalidSetting(
						MessageConstant.MESSAGE_INPUT_IN_POSITION.getMessage(messageArgs));
				m_log.info(methodName + " : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} else {
			CommonValidator.validateInt(MessageConstant.RECORD_HEADER_SIZE.getMessage(), binaryInfo.getRecordHeadSize(), 0,
					Integer.MAX_VALUE);
			CommonValidator.validateInt(MessageConstant.RECORD_SIZE_POSITION.getMessage(), binaryInfo.getSizePosition(),
					0, Integer.MAX_VALUE);
			CommonValidator.validateInt(MessageConstant.RECORD_SIZE_BYTE_LENGTH.getMessage(),
					binaryInfo.getSizeLength(), 0, 4);
		}

		// タイムスタンプ有無 : チェックなし(boolean).
		// タイムスタンプ位置 : タイムスタンプありの場合必須・DB入力チェック.
		// タイムスタンプ種類 : タイムスタンプありの場合必須・DB入力チェック.
		if (binaryInfo.isHaveTs()) {
			CommonValidator.validateInt(MessageConstant.TIMESTAMP_POSITION.getMessage(), binaryInfo.getTsPosition(), 1,
					Integer.MAX_VALUE);
			CommonValidator.validateString(MessageConstant.TIMESTAMP_TYPE.getMessage(), binaryInfo.getTsType(), true, 1,
					64);
		} else {
			CommonValidator.validateInt(MessageConstant.TIMESTAMP_POSITION.getMessage(), binaryInfo.getTsPosition(), 0,
					Integer.MAX_VALUE);
			CommonValidator.validateString(MessageConstant.TIMESTAMP_TYPE.getMessage(), binaryInfo.getTsType(), false,
					0, 64);
		}

		// リトルエンディアン方式 : チェックなし(boolean).
	}

	/**
	 * バイナリ監視設定(MonitorInfo)のパケットキャプチャ独自項目の妥当性チェック
	 * 
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validatePcap(MonitorInfo monitorInfo) throws InvalidSetting {
		PacketCheckInfo packetInfo = monitorInfo.getPacketCheckInfo();

		// フィルタ：null可・DB入力チェック.
		CommonValidator.validateString(MessageConstant.BPF_FILTER.getMessage(), packetInfo.getFilterStr(), false, 0,
				1024);
		
		// プロミスキャスモード:チェックなし(boolean).
	}
	
	/**
	 * 数値用監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK） 
	 * 
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateMonitorNumericSettings(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateMonitorNumericSettings() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		// itemName
		CommonValidator.validateCollect(MessageConstant.COLLECTION_DISPLAY_NAME.getMessage(), monitorInfo.getItemName(), 256);

		// measure
		CommonValidator.validateCollect(MessageConstant.COLLECTION_UNIT.getMessage(), monitorInfo.getMeasure(), 64);

		// prediction method
		CommonValidator.validateString(MessageConstant.PREDICTION_METHOD.getMessage(), monitorInfo.getPredictionMethod(), true, 1, 64);

		// prediction analysys range
		CommonValidator.validateInt(MessageConstant.PREDICTION_ANALYSYS_RANGE.getMessage(), monitorInfo.getPredictionAnalysysRange(), 1, DataRangeConstant.INTEGER_HIGH);

		// prediction target
		CommonValidator.validateInt(MessageConstant.PREDICTION_TARGET.getMessage(), monitorInfo.getPredictionTarget(), 1, DataRangeConstant.INTEGER_HIGH);
		
		// 将来予測
		if(monitorInfo.getPredictionFlg() && !monitorInfo.getCollectorFlg()) {
			InvalidSetting e = new InvalidSetting(
				MessageConstant.MESSAGE_IVALID_RELATION.getMessage(
						MessageConstant.MONITOR_PREDICTION.getMessage(),
						MessageConstant.ENABLE.getMessage(),
						MessageConstant.MONITOR_COLLECT.getMessage(),
						MessageConstant.ENABLE.getMessage()
				)					
			);
			m_log.info("validatePredictionFlg() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		// application(将来予測) 
		if(monitorInfo.getPredictionFlg()){ 
			CommonValidator.validateString(MessageConstant.PREDICTION_APPLICATION.getMessage(), 
							monitorInfo.getPredictionApplication(), true, 1, 64);
		} else {
			CommonValidator.validateString(MessageConstant.PREDICTION_APPLICATION.getMessage(), 
					monitorInfo.getPredictionApplication(), false, 0, 64); 
		}

		// change analysys range
		CommonValidator.validateInt(MessageConstant.CHANGE_ANALYSYS_RANGE.getMessage(), monitorInfo.getChangeAnalysysRange(), 1, DataRangeConstant.INTEGER_HIGH);
		
		// 変化量
		if(monitorInfo.getChangeFlg() && !monitorInfo.getCollectorFlg()) {
			InvalidSetting e = new InvalidSetting(
				MessageConstant.MESSAGE_IVALID_RELATION.getMessage(
						MessageConstant.MONITOR_CHANGE.getMessage(),
						MessageConstant.ENABLE.getMessage(),
						MessageConstant.MONITOR_COLLECT.getMessage(),
						MessageConstant.ENABLE.getMessage()
				)					
			);
			m_log.info("validatePredictionFlg() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		// application(変更点) 
		if(monitorInfo.getChangeFlg()){ 
			CommonValidator.validateString(MessageConstant.CHANGE_APPLICATION.getMessage(), 
							monitorInfo.getChangeApplication(), true, 1, 64); 
		} else {
			CommonValidator.validateString(MessageConstant.CHANGE_APPLICATION.getMessage(), 
					monitorInfo.getChangeApplication(), false, 0, 64); 
		}
	}

	private static void validateNumeric(MonitorInfo monitorInfo, int timeout)
			throws InvalidSetting {

		Double infoLower = null;
		Double infoUpper = null;
		Double warnLower = null;
		Double warnUpper = null;
		Double infoChangeLower = null;
		Double infoChangeUpper = null;
		Double warnChangeLower = null;
		Double warnChangeUpper = null;
		for (MonitorNumericValueInfo monitorNumericValueInfo : monitorInfo.getNumericValueInfo()) {
			if (MonitorNumericType.TYPE_BASIC.getType().equals(monitorNumericValueInfo.getMonitorNumericType())) {
				if (PriorityConstant.TYPE_INFO == monitorNumericValueInfo.getPriority()) {
					infoLower = monitorNumericValueInfo.getThresholdLowerLimit();
					infoUpper = monitorNumericValueInfo.getThresholdUpperLimit();
				}
				if (PriorityConstant.TYPE_WARNING == monitorNumericValueInfo.getPriority()) {
					warnLower = monitorNumericValueInfo.getThresholdLowerLimit();
					warnUpper = monitorNumericValueInfo.getThresholdUpperLimit();
				}
			} else if (MonitorNumericType.TYPE_CHANGE.getType().equals(monitorNumericValueInfo.getMonitorNumericType())) {
				if (PriorityConstant.TYPE_INFO == monitorNumericValueInfo.getPriority()) {
					infoChangeLower = monitorNumericValueInfo.getThresholdLowerLimit();
					infoChangeUpper = monitorNumericValueInfo.getThresholdUpperLimit();
				}
				if (PriorityConstant.TYPE_WARNING == monitorNumericValueInfo.getPriority()) {
					warnChangeLower = monitorNumericValueInfo.getThresholdLowerLimit();
					warnChangeUpper = monitorNumericValueInfo.getThresholdUpperLimit();
				}
			}
		}

		// 変更点監視の設定確認
		if (infoChangeLower == null || infoChangeUpper == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_INFO_UPPERVALUE_EXCEED_LOWERVALUE_CHANGE.getMessage());
			m_log.info("validateNumeric() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		if (warnChangeLower == null || warnChangeUpper == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_WARN_UPPERVALUE_EXCEED_LOWERVALUE_CHANGE.getMessage());
			m_log.info("validateNumeric() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		if (infoChangeLower > infoChangeUpper) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_INFO_UPPERVALUE_EXCEED_LOWERVALUE_CHANGE.getMessage());
			m_log.info("validateNumeric() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		if (warnChangeLower > warnChangeUpper) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_WARN_UPPERVALUE_EXCEED_LOWERVALUE_CHANGE.getMessage());
			m_log.info("validateNumeric() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		CommonValidator.validateDouble(MessageConstant.INFO.getMessage(), infoChangeLower, DataRangeConstant.DOUBLE_LOW, DataRangeConstant.DOUBLE_HIGH);
		CommonValidator.validateDouble(MessageConstant.INFO.getMessage(), infoChangeUpper, DataRangeConstant.DOUBLE_LOW, DataRangeConstant.DOUBLE_HIGH);
		CommonValidator.validateDouble(MessageConstant.WARNING.getMessage(), warnChangeLower, DataRangeConstant.DOUBLE_LOW, DataRangeConstant.DOUBLE_HIGH);
		CommonValidator.validateDouble(MessageConstant.WARNING.getMessage(), warnChangeUpper, DataRangeConstant.DOUBLE_LOW, DataRangeConstant.DOUBLE_HIGH);

		int runInterval = monitorInfo.getRunInterval();

		if (infoLower == null || infoUpper == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_INFO_UPPERVALUE_EXCEED_LOWERVALUE.getMessage());
			m_log.info("validateNumeric() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		if (warnLower == null || warnUpper == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_WARN_UPPERVALUE_EXCEED_LOWERVALUE.getMessage());
			m_log.info("validateNumeric() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateDouble(MessageConstant.INFO.getMessage(), infoLower, DataRangeConstant.DOUBLE_LOW, DataRangeConstant.DOUBLE_HIGH);
		CommonValidator.validateDouble(MessageConstant.INFO.getMessage(), infoUpper, DataRangeConstant.DOUBLE_LOW, DataRangeConstant.DOUBLE_HIGH);
		CommonValidator.validateDouble(MessageConstant.WARNING.getMessage(), warnLower, DataRangeConstant.DOUBLE_LOW, DataRangeConstant.DOUBLE_HIGH);
		CommonValidator.validateDouble(MessageConstant.WARNING.getMessage(), warnUpper, DataRangeConstant.DOUBLE_LOW, DataRangeConstant.DOUBLE_HIGH);

		// ping監視のみ通常のinfo/warnの閾値ではない
		if(!HinemosModuleConstant.MONITOR_PING.equals(monitorInfo.getMonitorTypeId())){
			if (infoLower > infoUpper) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_INFO_UPPERVALUE_EXCEED_LOWERVALUE.getMessage());
				m_log.info("validateNumeric() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			if (warnLower > warnUpper) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_WARN_UPPERVALUE_EXCEED_LOWERVALUE.getMessage());
				m_log.info("validateNumeric() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		if (timeout < 0) {
			return;
		}
		// 間隔よりタイムアウトが大きい場合
		if (runInterval*1000 < timeout) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_TIMEOUT_LOWERVALUE.getMessage());
			m_log.info("validateNumeric() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		if(HinemosModuleConstant.MONITOR_PING.equals(monitorInfo.getMonitorTypeId())){
			if (timeout < infoLower) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_INFO_LOWERVALUE.getMessage());
				m_log.info("validateNumeric() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			if (timeout < warnLower) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_WARN_LOWERVALUE.getMessage());
				m_log.info("validateNumeric() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} else {
			// タイムアウトより通知の上限が大きい場合
			if (timeout < infoUpper) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_INFO_LOWERVALUE.getMessage());
				m_log.info("validateNumeric() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// タイムアウトより警告の上限が大きい場合
			if (timeout < warnUpper) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_WARN_LOWERVALUE.getMessage());
				m_log.info("validateNumeric() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
	}

	/**
	 * 真偽値用監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateMonitorTruthSettings(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateMonitorTruthSettings() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateMonitorTruthSettings() monitorId = " + monitorInfo.getMonitorId());
		m_log.debug("validateMonitorTruthSettings() is not implemented. ");
	}

	/**
	 * Hinemosエージェント監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateHinemosAgent(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateHinemosAgent() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateHinemosAgent() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo : not impletemted( check info is not exists)
		m_log.debug("validateHinemosAgent() is not needed. ");

		// monitorType
		if(!HinemosModuleConstant.MONITOR_AGENT.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Agent Monitor Setting. But MonitorTypeId = "
					+ monitorInfo.getMonitorTypeId());
			m_log.info("validateHinemosAgent() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * HTTP監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateHttpNumeric(MonitorInfo monitorInfo) throws InvalidSetting {
		validateHttp(monitorInfo);

		HttpCheckInfo checkInfo = monitorInfo.getHttpCheckInfo();

		// input validate
		validateNumeric(monitorInfo, checkInfo.getTimeout());

	}

	/**
	 * HTTP監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateHttp(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateHttp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateHttp() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		HttpCheckInfo checkInfo = monitorInfo.getHttpCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("HTTP Monitor Setting is not defined. monitorId = "
					+ monitorInfo.getMonitorId());
			m_log.info("validateHttp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if (!HinemosModuleConstant.MONITOR_HTTP_N.equals(monitorInfo.getMonitorTypeId())
				&& !HinemosModuleConstant.MONITOR_HTTP_S.equals(monitorInfo.getMonitorTypeId())) {
			InvalidSetting e = new InvalidSetting("This is HTTP Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validateHttp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// requestUrl
		if(checkInfo.getRequestUrl() == null || "".equals(checkInfo.getRequestUrl())){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_URL.getMessage());
			m_log.info("validateHttp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}else{
			String url = checkInfo.getRequestUrl();
			// format check
			if (url.length() > 0 && (!url.startsWith("http://") && !url.startsWith("https://"))) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_UR_CORRECT_FORMAT.getMessage());
				m_log.info("validateHttp() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			else if((url.startsWith("http://") && url.length() == 7) || (url.startsWith("https://") && url.length() == 8)){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_UR_CORRECT_FORMAT.getMessage());
				m_log.info("validateHttp() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		CommonValidator.validateString(MessageConstant.REQUEST_URL.getMessage(),
				checkInfo.getRequestUrl(), true, 8, 2083);

		// urlReplace : not implemented

		// timeout : not implemented
		if(checkInfo.getTimeout() == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT_TIMEOUT.getMessage());
			m_log.info("validateHttp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(MessageConstant.TIME_OUT.getMessage(),
				checkInfo.getTimeout(), 1, 60 * 60 * 1000);

		// proxySet : not implemented

		// proxyHost : not implemented

		// proxyPort : not implemented
	}




	private static void validateHttpScenario(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateHttpScenario() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateHttp() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		HttpScenarioCheckInfo checkInfo = monitorInfo.getHttpScenarioCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("HTTP Scenario Monitor Setting is not defined. monitorId = "
					+ monitorInfo.getMonitorId());
			m_log.info("validateHttpScenario() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if (!HinemosModuleConstant.MONITOR_HTTP_SCENARIO.equals(monitorInfo.getMonitorTypeId())) {
			InvalidSetting e = new InvalidSetting("This is HTTP Scenario Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validateHttpScenario() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if (checkInfo.getAuthType() != null) {
			boolean match = false;
			for (GetHttpResponse.AuthType at: GetHttpResponse.AuthType.values()) {
				if (at.name().equals(checkInfo.getAuthType())) {
					match = true;
					break;
				}
			}
			if (!match) {
				InvalidSetting e = new InvalidSetting("This is HTTP Scenario Monitor Setting. But AuthType = " + checkInfo.getAuthType());
				m_log.info("validateHttpScenario() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		if (checkInfo.getAuthUser() != null) {
			CommonValidator.validateString(MessageConstant.MONITOR_HTTP_SCENARIO_AUTHUSER.getMessage(), checkInfo.getAuthUser(), false, 0, 64);
		}
		if (checkInfo.getAuthPassword() != null) {
			CommonValidator.validateString(MessageConstant.MONITOR_HTTP_SCENARIO_AUTHPASSWORD.getMessage(), checkInfo.getAuthPassword(), false, 0, 64);
		}
		if (checkInfo.getProxyFlg()) {
			if ("".equals(checkInfo.getProxyUrl())){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_URL.getMessage());
				m_log.info("validateHttpScenario() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			else {
				String url = checkInfo.getProxyUrl();
				// format check
				if (url.length() > 0 && (!url.startsWith("http://") && !url.startsWith("https://"))) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MONITOR_HTTP_SCENARIO_PROXYURL.getMessage());
					m_log.info("validateHttpScenario() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				else if((url.startsWith("http://") && url.length() == 7) || (url.startsWith("https://") && url.length() == 8)){
					InvalidSetting e = new InvalidSetting(MessageConstant.MONITOR_HTTP_SCENARIO_PROXYURL.getMessage());
					m_log.info("validateHttpScenario() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			CommonValidator.validateString(MessageConstant.MONITOR_HTTP_SCENARIO_PROXYURL.getMessage(), checkInfo.getProxyUrl(), false, 0, 1024);
			if (checkInfo.getProxyPort() != null) {
				CommonValidator.validateInt(MessageConstant.MONITOR_HTTP_SCENARIO_PROXYPORT.getMessage(), checkInfo.getProxyPort(), 0, 65535);
			} else {
				InvalidSetting e = new InvalidSetting(MessageConstant.MONITOR_HTTP_SCENARIO_PROXYPORT.getMessage());
				m_log.info("validateHttpScenario() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		if (checkInfo.getProxyUser() != null) {
			CommonValidator.validateString(MessageConstant.MONITOR_HTTP_SCENARIO_PROXYUSER.getMessage(), checkInfo.getProxyUser(), false, 0, 64);
		}
		if (checkInfo.getProxyPassword() != null) {
			CommonValidator.validateString(MessageConstant.MONITOR_HTTP_SCENARIO_PROXYPASSWORD.getMessage(), checkInfo.getProxyPassword(), false, 0, 64);
		}
		
		// checkInfo.getMonitoringPerPageFlg();
		if (checkInfo.getUserAgent() != null) {
			CommonValidator.validateString(MessageConstant.MONITOR_HTTP_SCENARIO_USERAGENT.getMessage(), checkInfo.getUserAgent(), false, 0, 1024);
		}
		// checkInfo.getCancelProxyCacheFlg();
		if (checkInfo.getConnectTimeout() == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MONITOR_HTTP_SCENARIO_CONNECTTIMEOUT.getMessage());
			m_log.info("validateHttpScenario() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		if (checkInfo.getConnectTimeout() != null) {
			CommonValidator.validateInt(MessageConstant.MONITOR_HTTP_SCENARIO_CONNECTTIMEOUT.getMessage(), checkInfo.getConnectTimeout(), 0, 60 * 60 * 1000);
		}
		if (checkInfo.getRequestTimeout() != null) {
			CommonValidator.validateInt(MessageConstant.MONITOR_HTTP_SCENARIO_CONNECTTIMEOUT.getMessage(), checkInfo.getRequestTimeout(), 0, 60 * 60 * 1000);
		}

		if (checkInfo.getPages().isEmpty()) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_MUST_SET_ONE_OR_MORE_PATTERNS.getMessage());
			m_log.info("validateHttpScenario() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		else {
			for (Page p: checkInfo.getPages()) {
				if (p.getUrl() == null && "".equals(p.getUrl())){
					InvalidSetting e = new InvalidSetting(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_URL.getMessage());
					m_log.info("validateHttpScenario() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				else {
					String url = p.getUrl();
					// format check
					if (url.length() > 0 && (!url.startsWith("http://") && !url.startsWith("https://"))) {
						InvalidSetting e = new InvalidSetting(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_URL.getMessage());
						m_log.info("validateHttpScenario() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
					else if((url.startsWith("http://") && url.length() == 7) || (url.startsWith("https://") && url.length() == 8)){
						InvalidSetting e = new InvalidSetting(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_URL.getMessage());
						m_log.info("validateHttpScenario() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
				}
				CommonValidator.validateString(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_URL.getMessage(), p.getUrl(), false, 0, 1024);
				CommonValidator.validateString(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_DESCRIPTION.getMessage(), p.getDescription(), false, 0, 1024);
				
				// status code
				CommonValidator.validateString(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_STATUSCODE.getMessage(), p.getStatusCode(), true, 3, 256);
				if (!Pattern.matches("^(\\s*\\d+,)*\\s*\\d+\\s*$", p.getStatusCode())) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_STATUSCODE.getMessage());
					m_log.info("validateHttpScenario() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				CommonValidator.validateString(MessageConstant.MONITOR_HTTP_SCENARIO_POST.getMessage(), p.getPost(), false, 0, 1024);

				for (com.clustercontrol.http.model.Pattern pt: p.getPatterns()) {
					CommonValidator.validateString(MessageConstant.MONITOR_HTTP_SCENARIO_PATTERN_PATTERN.getMessage(), pt.getPattern(), false, 0, 1024);
					CommonValidator.validateString(MessageConstant.MONITOR_HTTP_SCENARIO_PATTERN_DESCRIPTION.getMessage(), pt.getDescription(), false, 0, 256);
					// processType : not implemented pt.getProcessType();
					// pt.getCaseSensitivityFlg();
					// pt.getValidFlg();
				}

				for (Variable v: p.getVariables()) {
					CommonValidator.validateString(MessageConstant.MONITOR_HTTP_SCENARIO_VARIABLE_NAME.getMessage(), v.getName(), true, 0, 64);
					CommonValidator.validateString(MessageConstant.MONITOR_HTTP_SCENARIO_VARIABLE_VALUE.getMessage(), v.getValue(), true, 0, 1024);
					// v.getMatchingWithResponseFlg();
				}
			}
		}
	}

	/**
	 * ログファイル監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateLogfile(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateLogfile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateLogfile() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		LogfileCheckInfo checkInfo = monitorInfo.getLogfileCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Logfile Monitor Setting is not defined. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validateLogfile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Logfile Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validateLogfile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		//Directory
		if(checkInfo.getDirectory() == null || "".equals(checkInfo.getDirectory())){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_DIR.getMessage());
			m_log.info("validateLogfile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		//FileName
		if(checkInfo.getFileName() == null || "".equals(checkInfo.getFileName())){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_FILENAME.getMessage());
			m_log.info("validateLogfile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		try {
			Pattern.compile(checkInfo.getFileName());
		} catch (PatternSyntaxException  ex) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_REGEX_INVALID.getMessage(MessageConstant.LOGFILE_FILENAME.getMessage()));
			m_log.info("validateLogfile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		//FileEncoding
		if(checkInfo.getFileEncoding() == null || "".equals(checkInfo.getFileEncoding())){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_FILE_ENCODING.getMessage());
			m_log.info("validateLogfile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		//FileReturnCode
		if(checkInfo.getFileReturnCode() == null || "".equals(checkInfo.getFileReturnCode())){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_FILE_RETURNCODE.getMessage());
			m_log.info("validateLogfile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		CommonValidator.validateString(MessageConstant.DIRECTORY.getMessage(), checkInfo.getDirectory(), true, 1, 1024);
		CommonValidator.validateString(MessageConstant.FILE_NAME.getMessage(), checkInfo.getFileName(), true, 1, 1024);
		CommonValidator.validateString(MessageConstant.FILE_ENCODING.getMessage(), checkInfo.getFileEncoding(), true, 1, 32);
		CommonValidator.validateString(MessageConstant.FILE_RETURNCODE.getMessage(), checkInfo.getFileEncoding(), true, 1, 16);
				
		if (checkInfo.getMaxBytes() != null && checkInfo.getMaxBytes() <= 0) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MAX_READ_BYTE.getMessage());
			m_log.info("validateLogfile() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * ログ件数監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateLogcount(MonitorInfo monitorInfo) throws InvalidSetting, InvalidRole {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateLogcount() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateLogcount() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		LogcountCheckInfo checkInfo = monitorInfo.getLogcountCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Log Count Monitor Setting is not defined. monitorId = "
					+ monitorInfo.getMonitorId());
			m_log.info("validateLogcount() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_LOGCOUNT.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Log Count Monitor Setting. But MonitorTypeId = "
					+ monitorInfo.getMonitorTypeId());
			m_log.info("validateLogcount() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// targetMonitorId
		String targetMonitorId = checkInfo.getTargetMonitorId();
		if (targetMonitorId == null || targetMonitorId.equals("")) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_TARGET_MONITOR_ID.getMessage());
			m_log.info("validateLogcount() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		MonitorInfo targetMonitorInfo = null;
		try {
			targetMonitorInfo = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_OR(
					targetMonitorId, monitorInfo.getOwnerRoleId());
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_TARGET_MONITOR_NOT_FOUND.getMessage(new String[]{targetMonitorId}));
			m_log.info("validateLogcount() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e1;
		}
		if (targetMonitorInfo.getMonitorType() != MonitorTypeConstant.TYPE_STRING
				&& targetMonitorInfo.getMonitorType() != MonitorTypeConstant.TYPE_TRAP) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_TARGET_MONITOR_ID.getMessage());
			m_log.info("validateLogcount() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		try {
			if (!monitorInfo.getFacilityId().equals(targetMonitorInfo.getFacilityId())
					&& !(new RepositoryControllerBean().getFacilityIdList(
							targetMonitorInfo.getFacilityId(), 0).contains(monitorInfo.getFacilityId()))) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_TARGET_MONITOR_ID.getMessage());
				m_log.info("validateLogcount() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (HinemosUnknown e) {
			InvalidSetting ex = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_TARGET_MONITOR_ID.getMessage());
			m_log.info("validateLogcount() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw ex;
		}

		// keyword
		CommonValidator.validateString(MessageConstant.KEYWORD.getMessage(), checkInfo.getKeyword(), false, 0, 1024);

		// tag
		if (checkInfo.getTag() != null && !checkInfo.getTag().isEmpty()) {
			try {
				List<String> tagList = new MonitorSettingControllerBean().getMonitorStringTagList(
						targetMonitorInfo.getMonitorId(), monitorInfo.getOwnerRoleId());
				if (tagList == null || !tagList.contains(checkInfo.getTag())) {
					throw new InvalidSetting();
				}
			} catch (InvalidRole e) {
				throw e;
			} catch (Exception e) {
				String[] args = {targetMonitorId, checkInfo.getTag()};
				InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_LOGFORMAT_TAG_NOT_FOUND.getMessage(args));
				m_log.info("validateLogcount() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e1;
			}
		}

		validateNumeric(monitorInfo, -1);
	}

	/**
	 * リソース監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validatePerformance(MonitorInfo monitorInfo) throws InvalidSetting {

		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validatePerformance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validatePerformance() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		PerfCheckInfo checkInfo = monitorInfo.getPerfCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Performance Monitor Setting is not defined. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validatePerformance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_PERFORMANCE.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Performance Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validatePerformance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// itemCode
		if(checkInfo.getItemCode() == null || "".equals(checkInfo.getItemCode())){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MONITOR_ITEM.getMessage());
			m_log.info("validatePerformance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}else{
			try {
				com.clustercontrol.performance.monitor.util.QueryUtil.getCollectorItemCodeMstPK(checkInfo.getItemCode());
			} catch (CollectorNotFound e) {
				throw new InvalidSetting(e.getMessage(), e);
			}
		}

		// deviceDisplayName : not implemented
		if(checkInfo.getDeviceDisplayName() == null){
			InvalidSetting e = new InvalidSetting("Target Display Name is not defined.");
			m_log.info("validatePerformance() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		// breakdownFlg : not implemented

		validateNumeric(monitorInfo, -1);
	}

	/**
	 * ping監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validatePing(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validatePing() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validatePing() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		PingCheckInfo checkInfo = monitorInfo.getPingCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Ping Monitor Setting is not defined. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validatePing() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_PING.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Ping Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validatePing() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// runCount : implement
		if(checkInfo.getRunCount() == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT_RUNCOUNT.getMessage());
			m_log.info("validatePing() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(MessageConstant.RUN_COUNT.getMessage(),
				checkInfo.getRunCount(), 1, 9);

		// runInterval : implement
		if(checkInfo.getRunInterval() == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT_RUNINTERVAL.getMessage());
			m_log.info("validatePing() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(MessageConstant.RUN_INTERVAL.getMessage(),
				checkInfo.getRunInterval(), 0, 5  * 1000);

		// timeout
		if(checkInfo.getTimeout() == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT_TIMEOUT.getMessage());
			m_log.info("validatePing() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(MessageConstant.TIME_OUT.getMessage(),
				checkInfo.getTimeout(), 1, 60 * 60 * 1000);

		// input validate
		int runInterval = monitorInfo.getRunInterval();
		int runCount = checkInfo.getRunCount();
		int interval = checkInfo.getRunInterval();
		int timeout = checkInfo.getTimeout();

		// 間隔よりチェック設定の「回数×タイムアウト＋間隔」が大きい場合
		double total = runCount * ((double)timeout / 1000) + ((double)interval / 1000);
		if (runInterval <= (int)total) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_VALUE_SMALLER_INTERVAL.getMessage());
			m_log.info("validatePing() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		validateNumeric(monitorInfo, timeout);

		// パケット紛失(%)は0-100の間
		for (MonitorNumericValueInfo monitorNumericValueInfo : monitorInfo.getNumericValueInfo()) {
			if (MonitorNumericType.TYPE_BASIC.getType().equals(monitorNumericValueInfo.getMonitorNumericType())) {
				if (PriorityConstant.TYPE_INFO == monitorNumericValueInfo.getPriority()
						|| PriorityConstant.TYPE_WARNING == monitorNumericValueInfo.getPriority()) {
					CommonValidator.validateDouble(MessageConstant.PING_REACH.getMessage(),
							monitorNumericValueInfo.getThresholdUpperLimit(),
							0f,100f);
				}
			}
		}
	}

	/**
	 * ポート監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validatePort(MonitorInfo monitorInfo) throws InvalidSetting {

		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validatePort() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validatePort() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		PortCheckInfo checkInfo = monitorInfo.getPortCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Port Monitor Setting is not defined. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validatePort() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_PORT.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Port Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validatePort() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// portNo : not implemented
		if(checkInfo.getPortNo() == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_PORT_NUMBER.getMessage());
			m_log.info("validatePort() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(MessageConstant.PORT_NUMBER.getMessage(), checkInfo.getPortNo(), 1, 65535);

		// runCount : implement
		if(checkInfo.getRunCount() == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_COUNT.getMessage());
			m_log.info("validatePing() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(MessageConstant.RUN_COUNT.getMessage(),
				checkInfo.getRunCount(), 1, 9);

		// runInterval : not implemented
		if(checkInfo.getRunInterval() == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_INTERVAL.getMessage());
			m_log.info("validatePort() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(MessageConstant.RUN_INTERVAL.getMessage(),
				checkInfo.getRunInterval(), 0, 5  * 1000);

		// timeout
		if(checkInfo.getTimeout() == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT_TIMEOUT.getMessage());
			m_log.info("validatePort() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(MessageConstant.TIME_OUT.getMessage(),
				checkInfo.getTimeout(), 1, 60 * 60 * 1000);

		// serviceId : not implemented
		if(checkInfo.getServiceId() == null || "".equals(checkInfo.getServiceId())){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SERVICE_PROTOCOL.getMessage());
			m_log.info("validatePort() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}else{
			try {
				com.clustercontrol.port.util.QueryUtil.getMonitorProtocolMstPK(checkInfo.getServiceId());
			} catch (MonitorNotFound e) {
				throw new InvalidSetting(e.getMessage(), e);
			}
		}

		// input validate
		int runInterval = monitorInfo.getRunInterval();
		int runCount = checkInfo.getRunCount();
		int interval = checkInfo.getRunInterval();
		int timeout = checkInfo.getTimeout();

		// 間隔よりチェック設定の「回数×タイムアウト＋間隔」が大きい場合
		double total = runCount * ((double)timeout / 1000) + ((double)interval / 1000);
		if (runInterval <= (int)total) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_VALUE_SMALLER_INTERVAL.getMessage());
			m_log.info("validatePort() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		validateNumeric(monitorInfo, timeout);
	}

	/**
	 * プロセス監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateProcess(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateProcess() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateProcess() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		ProcessCheckInfo checkInfo = monitorInfo.getProcessCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Process Monitor Setting is not defined. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validateProcess() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_PROCESS.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Process Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validateProcess() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// command
		if(checkInfo.getCommand() == null || "".equals(checkInfo.getCommand())){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_COMMAND.getMessage());
			m_log.info("validateProcess() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(MessageConstant.COMMAND.getMessage(),
				checkInfo.getCommand(), true, 1, 256);
		try{
			Pattern.compile(checkInfo.getCommand());
		}
		catch(PatternSyntaxException e){
			InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_REGEX_TO_COMMAND.getMessage(), e);
			m_log.info("validateProcess() : "
					+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
			throw e1;
		}

		// param
		if (checkInfo.getParam() != null) {
			CommonValidator.validateString(MessageConstant.PARAM.getMessage(),
					checkInfo.getParam(), false, 0, 256);
			try {
				Pattern.compile(checkInfo.getParam());
			} catch(PatternSyntaxException e) {
				InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_REGEX_TO_ARGUMENT.getMessage());
				m_log.info("validateProcess() : "
						+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
				throw e1;
			}
		}

		validateNumeric(monitorInfo, -1);
	}

	/**
	 * SNMPトラップ用監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateSnmptrap(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateSnmptrap() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateSnmptrap() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		TrapCheckInfo checkInfo = monitorInfo.getTrapCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("SNMP Trap Monitor Setting is not defined. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validateSnmptrap() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_SNMPTRAP.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is SNMP Trap Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validateSnmptrap() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		CommonValidator.validateString(MessageConstant.COMMUNITY_NAME.getMessage(),
				checkInfo.getCommunityName(), false, 0, 64);

		// communityName
		if(checkInfo.getCommunityCheck()){
			if(checkInfo.getCommunityName() == null || "".equals(checkInfo.getCommunityName())){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_COMMUNITY_NAME.getMessage());
				m_log.info("validateSnmptrap() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		CommonValidator.validateString(MessageConstant.CHARSET_SNMPTRAP_CODE.getMessage(), checkInfo.getCharsetName(),
				false, 1, 64);

		// charsetName
		if(checkInfo.getCharsetConvert()){
			if(checkInfo.getCharsetName() == null || "".equals(checkInfo.getCharsetName())){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CHARACTER_CODE.getMessage());
				m_log.info("validateSnmptrap() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		// PriorityUnspecified : not implemented

		ArrayList<TrapValueInfoPK> pkList = new ArrayList<TrapValueInfoPK>();
		for (TrapValueInfo v: checkInfo.getTrapValueInfos()) {
			CommonValidator.validateString(MessageConstant.MONITOR_SNMPTRAP_VALUE_MIB.getMessage(), v.getMib(), true, 1, 1024);
			CommonValidator.validateString(MessageConstant.TRAP_NAME.getMessage(), v.getUei(), true, 1, 256);
			CommonValidator.validateString(MessageConstant.OID.getMessage(), v.getTrapOid(), true, 1, 1024);
			
			//.と[0-9]以外はNG
			char c = 'a';
			for (int i = 0; i < v.getTrapOid().length(); i++) {
				c = v.getTrapOid().charAt(i);
				if (c != '.' && !('0' <= c && c <= '9')) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OID.getMessage(new String[]{v.getTrapOid()}));
					m_log.info("validateSnmptrap() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			
			// GenericId
			// SpecificId
			// トラップ定義の重複チェック
			TrapValueInfoPK entityPk = new TrapValueInfoPK(
					checkInfo.getMonitorId(), 
					v.getMib(),
					v.getTrapOid(), 
					v.getGenericId(),
					v.getSpecificId());
			if (pkList.contains(entityPk)) {
				String arg;
				if (v.getVersion() == SnmpVersionConstant.TYPE_V1) {
					arg = String.format("MIB=%s,OID=%s,generic_id=%s,specific_id=%s", v.getMib(), v.getTrapOid(), v.getGenericId(), v.getSpecificId());
				} else {
					arg = String.format("MIB=%s,OID=%s", v.getMib(), v.getTrapOid());
				}
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_TRAPOID_OVERLAPS.getMessage(new String[]{arg}));
				m_log.info("validateSnmptrap() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			pkList.add(entityPk);


			// Logmsg : not implemented
			// Description : not implemented

			if (v.getProcessingVarbindSpecified()) {
				if (v.getVarBindPatterns().isEmpty()) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_MUST_SET_MORE_THAN_ONE_PATTERN.getMessage());
					m_log.info("validateSnmptrap() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			CommonValidator.validateString(MessageConstant.MONITOR_SNMPTRAP_VALUE_VARBINDPATTERN.getMessage(), v.getFormatVarBinds(), false, 0, 128);

			for (VarBindPattern p: v.getVarBindPatterns()) {
				CommonValidator.validateString(MessageConstant.PATTERN_MATCHING_EXPRESSION.getMessage(), p.getPattern(), false, 0, 1024);
				CommonValidator.validateString(MessageConstant.MONITOR_SNMPTRAP_VALUE_PATTERN_DESCRIPTION.getMessage(), p.getDescription(), false, 0, 256);
				// processType : not implemented
				// caseSensitivityFlg : not implemented
				// validFlg : not implemented
				// priority : not implemented
			}
		}
	}


	/**
	 * SNMP監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateSnmpNumeric(MonitorInfo monitorInfo) throws InvalidSetting {
		validateSnmp(monitorInfo);

		validateNumeric(monitorInfo, -1);
	}

	/**
	 * SNMP監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateSnmp(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateSnmp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateSnmp() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		SnmpCheckInfo checkInfo = monitorInfo.getSnmpCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("SNMP Monitor Setting is not defined. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validateSnmp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if (!HinemosModuleConstant.MONITOR_SNMP_N.equals(monitorInfo.getMonitorTypeId())
				&& !HinemosModuleConstant.MONITOR_SNMP_S.equals(monitorInfo.getMonitorTypeId())) {
			InvalidSetting e = new InvalidSetting("This is SNMP Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validateSnmp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// communityName : not implemented

		// convertFlg
		if(checkInfo.getConvertFlg() == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_METHOD_OF_CALCULATION.getMessage());
			m_log.info("validateSnmp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// snmpOid
		String oid = checkInfo.getSnmpOid();
		if(oid == null || "".equals(oid)){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_OID.getMessage());
			m_log.info("validateSnmp() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(MessageConstant.OID.getMessage(), oid, true, 1, 1024);
		//.と[0-9]以外はNG
		char c = 'a';
		for (int i = 0; i < oid.length(); i++) {
			c = oid.charAt(i);
			if (c != '.' && !('0' <= c && c <= '9')) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_OID.getMessage());
				m_log.info("validateSnmp() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		// snmpPort : not implemented

		// snmpVersion : not implemented
	}

	/**
	 * SQL監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateSqlNumeric(MonitorInfo monitorInfo) throws InvalidSetting {
		validateSql(monitorInfo);

		validateNumeric(monitorInfo, -1);
	}

	/**
	 * SQL監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateSql(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateSql() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateSql() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		SqlCheckInfo checkInfo = monitorInfo.getSqlCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("SQL Monitor Setting is not defined. monitorId = "
					+ monitorInfo.getMonitorId());
			m_log.info("validateSql() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if (!HinemosModuleConstant.MONITOR_SQL_N.equals(monitorInfo.getMonitorTypeId())
				&& !HinemosModuleConstant.MONITOR_SQL_S.equals(monitorInfo.getMonitorTypeId())) {
			InvalidSetting e = new InvalidSetting("This is SQL Monitor Setting. But MonitorTypeId = "
					+ monitorInfo.getMonitorTypeId());
			m_log.info("validateSql() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// connectionUrl
		String url = checkInfo.getConnectionUrl();
		if(url == null || "".equals(url) || url.length() < 6 ||
				!url.startsWith("jdbc:")){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CONNECTION_URL_CORRECT_FORMAT.getMessage());
			m_log.info("validateSql() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(MessageConstant.CONNECTION_URL.getMessage(),
				checkInfo.getConnectionUrl(), true, 1, 256);

		// user
		if(checkInfo.getUser() == null || "".equals(checkInfo.getUser())){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_USER_ID.getMessage());
			m_log.info("validateSql() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(MessageConstant.USER_ID.getMessage(),
				checkInfo.getUser(), true, 1, 64);

		// password
		if(checkInfo.getPassword() == null || "".equals(checkInfo.getPassword())){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_PASSWORD.getMessage());
			m_log.info("validateSql() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(MessageConstant.PASSWORD.getMessage(),
				checkInfo.getPassword(), true, 1, 64);

		// query
		if(checkInfo.getQuery() == null || checkInfo.getQuery().length() < 7){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SELECT_STATEMENT_IN_SQL.getMessage());
			m_log.info("validateSql() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}else {
			String work = checkInfo.getQuery().substring(0, 6);
			if(!work.equalsIgnoreCase("SELECT")){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SELECT_STATEMENT_IN_SQL.getMessage());
				m_log.info("validateSql() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		CommonValidator.validateString(MessageConstant.SQL_STRING.getMessage(),
				checkInfo.getQuery(), true, 1, 1024);

		// jdbcDriver
		if(checkInfo.getJdbcDriver() == null || "".equals(checkInfo.getJdbcDriver())){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CONNECTION_URL.getMessage());
			m_log.info("validateSql() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * システムログ監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateSystemlog(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateSystemlog() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateSystemlog() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo : not impletemted( check info is not exists)
		m_log.debug("validateSystemlog() is not needed. ");

		// monitorType
		if(!HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Systemlog Monitor Setting. But MonitorTypeId = " + monitorInfo.getMonitorTypeId());
			m_log.info("validateSystemlog() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	/**
	 * コマンド監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	private static void validateCustom(MonitorInfo monitorInfo) throws InvalidSetting, InvalidRole {

		validateCustomString(monitorInfo);

		validateNumeric(monitorInfo, -1);
	}
	/**
	 * カスタム監視（文字列）設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	private static void validateCustomString(MonitorInfo monitorInfo) throws InvalidSetting, InvalidRole {

		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateCustom() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		CustomCheckInfo checkInfo = monitorInfo.getCustomCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Custom Monitor Setting is not defined. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_CUSTOM_N.equals(monitorInfo.getMonitorTypeId())
				&& !HinemosModuleConstant.MONITOR_CUSTOM_S.equals(monitorInfo.getMonitorTypeId())) {
			InvalidSetting e = new InvalidSetting("This is Custom Monitor Setting. But MonitorTypeId = "
					+ monitorInfo.getMonitorTypeId());
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// execType
		if(checkInfo.getCommandExecType() == null &&
				(checkInfo.getCommandExecType().equals(CustomConstant.CommandExecType.SELECTED)
						|| checkInfo.getCommandExecType().equals(CustomConstant.CommandExecType.INDIVIDUAL))){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_HOW_TO_RUN_COMMAND.getMessage());
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// selectedFacilityId
		if(checkInfo.getCommandExecType().equals(CustomConstant.CommandExecType.SELECTED)){
			if(checkInfo.getSelectedFacilityId() == null || "".equals(checkInfo.getSelectedFacilityId())){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SELECT_NODE_RUN_COMMAND.getMessage());
				m_log.info("validateCustom() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			try {
				FacilityTreeCache.validateFacilityId(checkInfo.getSelectedFacilityId(), monitorInfo.getOwnerRoleId(), false);
			} catch (FacilityNotFound e) {
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SELECT_NODE_RUN_COMMAND.getMessage());
			}
		}
		// effectiveUser
		if ((checkInfo.getSpecifyUser().booleanValue() || !checkInfo.getSpecifyUser().booleanValue()) &&
				(checkInfo.getSpecifyUser().booleanValue() &&(checkInfo.getEffectiveUser() == null || "".equals(checkInfo.getEffectiveUser())))) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT_EFFECTIVEUSER.getMessage());
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		if (checkInfo.getSpecifyUser()) {
			CommonValidator.validateString(MessageConstant.EFFECTIVE_USER.getMessage(), checkInfo.getEffectiveUser(),
					true, 1, 64);
		}

		// command
		if(checkInfo.getCommand() == null || "".equals(checkInfo.getCommand())){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT_COMMAND.getMessage());
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(MessageConstant.COMMAND.getMessage(), checkInfo.getCommand(),
				true, 1, 1024);

		// timeout
		if(checkInfo.getTimeout() == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT_TIMEOUT.getMessage());
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(MessageConstant.TIME_OUT.getMessage(),
				checkInfo.getTimeout(), 1, 60 * 60 * 1000);
		int timeout = checkInfo.getTimeout();
		if (monitorInfo.getRunInterval() * 1000 < timeout) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT_TIMEOUT_SHORTER_THAN_MONITOR_INTERVAL.getMessage());
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}
	/**
	 * カスタムトラップ監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	private static void validateCustomTrap(MonitorInfo monitorInfo) throws InvalidSetting, InvalidRole {
		validateCustomTrapString(monitorInfo);
		
		validateNumeric(monitorInfo, -1);
	}
	/**
	 * カスタムトラップ監視（文字列）設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	private static void validateCustomTrapString(MonitorInfo monitorInfo) throws InvalidSetting, InvalidRole {
		
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateCustom() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		CustomTrapCheckInfo checkInfo = monitorInfo.getCustomTrapCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("CustomTrap Monitor Setting is not defined. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validateCustomTrapString() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_CUSTOMTRAP_N.equals(monitorInfo.getMonitorTypeId())
				&& !HinemosModuleConstant.MONITOR_CUSTOMTRAP_S.equals(monitorInfo.getMonitorTypeId())) {
			InvalidSetting e = new InvalidSetting("This is CustomTrap Monitor Setting. But MonitorTypeId = "
					+ monitorInfo.getMonitorTypeId());
			m_log.info("validateCustomTrapString() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		// キーパターン
		CommonValidator.validateString(MessageConstant.MONITOR_CUSTOMTRAP_KEY_PATTERN.getMessage(),
			checkInfo.getTargetKey(), false, 1, 64);
		CommonValidator.validateRegex(
			MessageConstant.MONITOR_CUSTOMTRAP_KEY_PATTERN.getMessage(), 
			checkInfo.getTargetKey(), 
			false);
	}

	/**
	 * Windowsサービス監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateWinService(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateWinService() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateWinService() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		WinServiceCheckInfo checkInfo = monitorInfo.getWinServiceCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Windows Service Monitor Setting is not defined. monitorId = "
					+ monitorInfo.getMonitorId());
			m_log.info("validateWinService() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_WINSERVICE.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Windows Service Monitor Setting. But MonitorTypeId = "
					+ monitorInfo.getMonitorTypeId());
			m_log.info("validateWinService() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// serviceName
		if(checkInfo.getServiceName() == null || "".equals(checkInfo.getServiceName())){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_WIN_SERVICE_NAME.getMessage());
			m_log.info("validateWinService() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(MessageConstant.WINSERVICE_NAME.getMessage(),
				checkInfo.getServiceName(), true, 1, 1024);
	}

	/**
	 * Windowsイベント監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateWinEvent(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateWinService() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateWinEvent() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		WinEventCheckInfo checkInfo = monitorInfo.getWinEventCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Windows Event Monitor Setting is not defined. monitorId = "
					+ monitorInfo.getMonitorId());
			m_log.info("validateWinEvent() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_WINEVENT.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Windows Event Monitor Setting. But MonitorTypeId = "
					+ monitorInfo.getMonitorTypeId());
			m_log.info("validateWinEvent() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// level
		if(!(checkInfo.isLevelCritical() || checkInfo.isLevelError() || checkInfo.isLevelInformational() || checkInfo.isLevelVerbose() || checkInfo.isLevelWarning()) ){
			InvalidSetting e = new InvalidSetting("Windows Event Monitor Setting must have one or more enabled levels. ");
			m_log.info("validateWinEvent() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// logName
		if(checkInfo.getLogName() == null || checkInfo.getLogName().size() == 0){
			InvalidSetting e = new InvalidSetting("Windows Event Monitor Setting must have one or more log names. ");
			m_log.info("validateWinEvent() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		////
		// Length Check
		////

		//logName : character varying(256)（winevent.log）
		List<String> lists = checkInfo.getLogName();
		Collections.sort(lists);
		String str = null;
		for(String s : lists) {
			if (s.equals(str)) {
				InvalidSetting e = new InvalidSetting("same logname : " + s);
				m_log.info("validateString() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			str = s;
		}
		for(String logName : checkInfo.getLogName()){
			CommonValidator.validateString(MessageConstant.WINEVENT_LOG.getMessage() + ":" + logName,logName, true, 1, 256);
		}

		//source : character varying(256)（winevent.source）
		lists = checkInfo.getSource();
		Collections.sort(lists);
		str = null;
		for(String s : lists) {
			if (s.equals(str)) {
				InvalidSetting e = new InvalidSetting("same source : " + s);
				m_log.info("validateString() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			str = s;
		}
		for(String source : checkInfo.getSource()){
			CommonValidator.validateString(MessageConstant.WINEVENT_SOURCE.getMessage() + ":" + source,source, true, 1, 256);
		}

		Integer ii = null;

		//eventId : int(winevent.id)
		List<Integer> listi = checkInfo.getEventId();
		Collections.sort(listi);
		ii = null;
		for(Integer i : listi) {
			if (i.equals(ii)) {
				InvalidSetting e = new InvalidSetting("same event id : " + i);
				m_log.info("validateString() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			ii = i;
		}
		for(Integer eventId : checkInfo.getEventId()){
			CommonValidator.validateInt(MessageConstant.WINEVENT_ID.getMessage() + ":" + eventId, eventId, 0, 65535);
		}

		//category : smallint（winevent.category）
		listi = checkInfo.getCategory();
		Collections.sort(listi);
		ii = null;
		for(Integer i : listi) {
			if (i.equals(ii)) {
				InvalidSetting e = new InvalidSetting("same category : " + i);
				m_log.info("validateString() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			ii = i;
		}
		for(Integer category : checkInfo.getCategory()){
			CommonValidator.validateInt(MessageConstant.WINEVENT_CATEGORY.getMessage() + ":" + category, category, 0, 32767);
		}

		Long ll = null;
		//keywaord : bigint(winevent.keywords)
		List<Long> listl = checkInfo.getKeywords();
		Collections.sort(listl);
		for(Long l : listl) {
			if (l.equals(ll)) {
				InvalidSetting e = new InvalidSetting("same keyword : " + l);
				m_log.info("validateString() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			ll = l;
		}
		for(Long keyword : checkInfo.getKeywords()){
			CommonValidator.validateLong(MessageConstant.WINEVENT_KEYWORDS.getMessage() + ":" + keyword, keyword, 0, Long.MAX_VALUE);
		}

	}

	/**
	 * Windowsサービス監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateJMX(MonitorInfo monitorInfo) throws InvalidSetting {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateJMX() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateJMX() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		JmxCheckInfo checkInfo = monitorInfo.getJmxCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("JMX Monitor Setting is not defined. monitorId = "
					+ monitorInfo.getMonitorId());
			m_log.info("validateJMX() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_JMX.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is JMX Monitor Setting. But MonitorTypeId = "
					+ monitorInfo.getMonitorTypeId());
			m_log.info("validateJMX() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		if (checkInfo.getAuthPassword() != null) {
			CommonValidator.validateString(MessageConstant.MONITOR_JMX_AUTHPASSWORD.getMessage(), checkInfo.getAuthPassword(), false, 0, 64);
		}

		if (checkInfo.getPort() == null) {
			InvalidSetting e = new InvalidSetting("JMX Monitor Setting must hava a destination port. monitorId = " + monitorInfo.getMonitorId());
			m_log.info("validateJMX() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(MessageConstant.MONITOR_JMX_PORT.getMessage(), checkInfo.getPort(), 0, 65535);
		
		if (checkInfo.getMasterId() == null || "".equals(checkInfo.getMasterId())) {
			CommonValidator.validateString(MessageConstant.MONITOR_JMX_MASTER_ID.getMessage(), checkInfo.getAuthPassword(), false, 0, 64);
		}
		
		validateNumeric(monitorInfo, -1);
	}

	/**
	 * 相関係数監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateCorrelation(MonitorInfo monitorInfo) throws InvalidSetting, InvalidRole {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateCorrelation() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateCorrelation() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		CorrelationCheckInfo checkInfo = monitorInfo.getCorrelationCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Correlation Monitor Setting is not defined. monitorId = "
					+ monitorInfo.getMonitorId());
			m_log.info("validateCorrelation() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_CORRELATION.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Correlation Monitor Setting. But MonitorTypeId = "
					+ monitorInfo.getMonitorTypeId());
			m_log.info("validateCorrelation() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// analysysRange
		CommonValidator.validateInt(MessageConstant.ANALYSYS_RANGE.getMessage(), checkInfo.getAnalysysRange(), 1, DataRangeConstant.INTEGER_HIGH);
		
		// targetMonitorId, targetDisplayName, targetItemName
		String targetMonitorId = checkInfo.getTargetMonitorId();
		if (targetMonitorId == null || targetMonitorId.isEmpty()
				|| checkInfo.getTargetDisplayName() == null
				|| checkInfo.getTargetItemName() == null || checkInfo.getTargetItemName().isEmpty()) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_TARGET_DISPLAY_NAME.getMessage());
			m_log.info("validateCorrelation() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		MonitorInfo targetMonitorInfo = null;
		// 監視設定IDのみ存在チェックを行う
		try {
			targetMonitorInfo = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_OR(
					targetMonitorId, monitorInfo.getOwnerRoleId());
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_TARGET_MONITOR_NOT_FOUND.getMessage(new String[]{targetMonitorId}));
			m_log.info("validateCorrelation() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e1;
		}
		if (targetMonitorInfo.getMonitorType() != MonitorTypeConstant.TYPE_NUMERIC) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_TARGET_MONITOR_NOT_FOUND.getMessage(new String[]{targetMonitorId}));
			m_log.info("validateCorrelation() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		try {
			if (!monitorInfo.getFacilityId().equals(targetMonitorInfo.getFacilityId())
					&& !(new RepositoryControllerBean().getFacilityIdList(
							targetMonitorInfo.getFacilityId(), 0).contains(monitorInfo.getFacilityId()))) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_TARGET_MONITOR_NOT_FOUND.getMessage(new String[]{targetMonitorId}));
				m_log.info("validateCorrelation() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (HinemosUnknown e) {
			InvalidSetting ex = new InvalidSetting(MessageConstant.MESSAGE_TARGET_MONITOR_NOT_FOUND.getMessage(new String[]{targetMonitorId}));
			m_log.info("validateCorrelation() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw ex;
		}


		// referMonitorId, referDisplayName, referItemName
		String referMonitorId = checkInfo.getReferMonitorId();
		if (referMonitorId == null || referMonitorId.isEmpty()
				|| checkInfo.getReferDisplayName() == null
				|| checkInfo.getReferItemName() == null || checkInfo.getReferItemName().isEmpty()) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_REFER_DISPLAY_NAME.getMessage());
			m_log.info("validateCorrelation() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		MonitorInfo referMonitorInfo = null;
		// 監視設定IDのみ存在チェックを行う
		try {
			referMonitorInfo = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_OR(
					referMonitorId, monitorInfo.getOwnerRoleId());
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_TARGET_MONITOR_NOT_FOUND.getMessage(new String[]{referMonitorId}));
			m_log.info("validateCorrelation() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e1;
		}
		if (referMonitorInfo.getMonitorType() != MonitorTypeConstant.TYPE_NUMERIC) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_TARGET_MONITOR_NOT_FOUND.getMessage(new String[]{referMonitorId}));
			m_log.info("validateCorrelation() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		// referFacilityId
		String referFacilityId = checkInfo.getReferFacilityId();
		if (referFacilityId == null || referFacilityId.equals("")) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_REFER_SCOPE.getMessage());
			m_log.info("validateCorrelation() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		try {
			FacilityTreeCache.validateFacilityId(referFacilityId, monitorInfo.getOwnerRoleId(), false);
		} catch (FacilityNotFound e) {
			throw new InvalidSetting(e.getMessage(), e);
		}
		try {
			if (!referMonitorInfo.getFacilityId().equals(referFacilityId)
					&& !(new RepositoryControllerBean().getFacilityIdList(
							referMonitorInfo.getFacilityId(), 0).contains(referFacilityId))) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_TARGET_MONITOR_NOT_FOUND.getMessage(new String[]{referMonitorId}));
				m_log.info("validateCorrelation() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (HinemosUnknown e) {
			InvalidSetting ex = new InvalidSetting(MessageConstant.MESSAGE_TARGET_MONITOR_NOT_FOUND.getMessage(new String[]{referMonitorId}));
			m_log.info("validateCorrelation() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw ex;
		}

		validateNumeric(monitorInfo, -1);
	}

	/**
	 * 収集値統合監視設定(MonitorInfo)の基本設定の妥当性チェック（関連テーブルへのリンク & NULL CHECK）
	 * @param monitorInfo
	 * @throws InvalidSetting
	 */
	private static void validateIntegration(MonitorInfo monitorInfo) throws InvalidSetting, InvalidRole {
		if(monitorInfo == null){
			InvalidSetting e = new InvalidSetting("MonitorInfo is not defined.");
			m_log.info("validateIntegration() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("validateIntegration() monitorId = " + monitorInfo.getMonitorId());

		// CheckInfo
		IntegrationCheckInfo checkInfo = monitorInfo.getIntegrationCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Integration Monitor Setting is not defined. monitorId = "
					+ monitorInfo.getMonitorId());
			m_log.info("validateIntegration() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_INTEGRATION.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Integration Monitor Setting. But MonitorTypeId = "
					+ monitorInfo.getMonitorTypeId());
			m_log.info("validateIntegration() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// timeout
		CommonValidator.validateInt(MessageConstant.TIME_OUT.getMessage(), checkInfo.getTimeout(), 1, DataRangeConstant.INTEGER_HIGH);

		// messageOk
		CommonValidator.validateString(MessageConstant.MESSAGE.getMessage() + "(OK)", checkInfo.getMessageOk(), true, 1, 256);

		// messageNg
		CommonValidator.validateString(MessageConstant.MESSAGE.getMessage() + "(NG)", checkInfo.getMessageNg(), true, 1, 256);

		if (checkInfo.getConditionList() == null || checkInfo.getConditionList().size() == 0) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_TARGET_CONDITION.getMessage());
			m_log.info("validateIntegration() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		for (IntegrationConditionInfo condition : checkInfo.getConditionList()) {

			// 説明
			CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(), condition.getDescription(), false, 0, 256);

			String targetFacilityId = "";
			if (condition.getMonitorNode()) {
				targetFacilityId = monitorInfo.getFacilityId();
			} else {
				targetFacilityId = condition.getTargetFacilityId();
			}
			if (targetFacilityId == null || targetFacilityId.isEmpty()) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_TARGET_NODE.getMessage(new String[]{""}));
				m_log.info("validateIntegration() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			if (!condition.getMonitorNode()) {
				try {
					FacilityTreeCache.validateFacilityId(targetFacilityId, monitorInfo.getOwnerRoleId(), true);
				} catch (FacilityNotFound e) {
					throw new InvalidSetting(e.getMessage(), e);
				}
			}

			// targetMonitorId
			String targetMonitorId = condition.getTargetMonitorId();
			if (targetMonitorId == null || targetMonitorId.isEmpty()) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_TARGET_DISPLAY_NAME.getMessage());
				m_log.info("validateIntegration() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			MonitorInfo targetMonitorInfo = null;
			// 監視設定IDのみ存在チェックを行う
			try {
				targetMonitorInfo = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_OR(
						targetMonitorId, monitorInfo.getOwnerRoleId());
			} catch (InvalidRole e) {
				throw e;
			} catch (Exception e) {
				InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_TARGET_MONITOR_NOT_FOUND.getMessage(new String[]{targetMonitorId}));
				m_log.info("validateIntegration() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e1;
			}
			
			if (targetMonitorInfo.getMonitorType() != MonitorTypeConstant.TYPE_NUMERIC
					&& targetMonitorInfo.getMonitorType() != MonitorTypeConstant.TYPE_STRING) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_TARGET_MONITOR_NOT_FOUND.getMessage(new String[]{targetMonitorId}));
				m_log.info("validateIntegration() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			if (condition.getTargetDisplayName() == null || condition.getTargetItemName() == null) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_TARGET_DISPLAY_NAME.getMessage());
				m_log.info("validateIntegration() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			if (targetMonitorInfo.getMonitorType() == MonitorTypeConstant.TYPE_NUMERIC) {
				if (condition.getTargetItemName().isEmpty()) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_TARGET_DISPLAY_NAME.getMessage());
					m_log.info("validateIntegration() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			try {
				if (!targetFacilityId.equals(targetMonitorInfo.getFacilityId())
						&& !(new RepositoryControllerBean().getFacilityIdList(
								targetMonitorInfo.getFacilityId(), 0).contains(targetFacilityId))) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_TARGET_MONITOR_NOT_MATCH_FACILITY_ID.getMessage(
							new String[]{targetFacilityId, targetMonitorId}));
					m_log.info("validateIntegration() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} catch (HinemosUnknown e) {
				InvalidSetting ex = new InvalidSetting(MessageConstant.MESSAGE_TARGET_MONITOR_NOT_FOUND.getMessage(new String[]{targetMonitorId}));
				m_log.info("validateIntegration() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw ex;
			}

			// comparison_value
			CommonValidator.validateString(MessageConstant.COMPARISON_VALUE.getMessage(), condition.getComparisonValue(), true, 1, 1024);

			// comparison_method
			if (condition.getComparisonMethod() == null || condition.getComparisonMethod().isEmpty()) {
				InvalidSetting ex = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_COMPARISON_METHOD.getMessage(new String[]{""}));
				m_log.info("validateIntegration() : " + ex.getClass().getSimpleName() + ", " + ex.getMessage());
				throw ex;
			}
			if (targetMonitorInfo.getMonitorType() == MonitorTypeConstant.TYPE_NUMERIC) {
				if (!IntegrationComparisonMethod.symbols().contains(condition.getComparisonMethod())) {
					String[] args = new String[]{String.format("monitorType:%s, comparisonMethod:%s", "TYPE_NUMERIC", condition.getComparisonMethod())};
					InvalidSetting ex = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_COMPARISON_METHOD.getMessage(args));
					m_log.info("validateIntegration() : " + ex.getClass().getSimpleName() + ", " + ex.getMessage());
					throw ex;
				}
				try {
			        double comparisonValue = Double.parseDouble(condition.getComparisonValue());
					CommonValidator.validateDouble(MessageConstant.COMPARISON_VALUE.getMessage(), comparisonValue, DataRangeConstant.DOUBLE_LOW, DataRangeConstant.DOUBLE_HIGH);
			    } catch (NumberFormatException e) {
						String[] args = new String[]{String.format("monitorType:%s, comparisonValue:%s", "TYPE_NUMERIC", condition.getComparisonValue())};
						InvalidSetting ex = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_COMPARISON_VALUE.getMessage(args));
						m_log.info("validateIntegration() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
						throw ex;
			    }
			}
			if (targetMonitorInfo.getMonitorType() == MonitorTypeConstant.TYPE_STRING
					&& !IntegrationComparisonMethod.EQ.symbol().equals(condition.getComparisonMethod())) {
				String[] args = new String[]{String.format("monitorType:%s, comparisonMethod:%s", "TYPE_STRING", condition.getComparisonMethod())};
				InvalidSetting ex = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_COMPARISON_METHOD.getMessage(args));
				m_log.info("validateIntegration() : " + ex.getClass().getSimpleName() + ", " + ex.getMessage());
				throw ex;
			}
		}
	}

	/**
	 * 他の機能にて、監視設定が参照状態であるか調査する。
	 * 参照状態の場合、メッセージダイアログが出力される。
	 * @param monitorId
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public static void valideDeleteMonitor(String monitorId) throws InvalidSetting, HinemosUnknown{
		try{
			//ジョブ
			List<JobMstEntity> jobMstList =
					com.clustercontrol.jobmanagement.util.QueryUtil.getJobMstEntityFindByMonitorId_NONE(monitorId);
			if (jobMstList != null) {
				for(JobMstEntity jobMst : jobMstList){
					m_log.debug("valideDeleteMonitor() target JobMaster " + jobMst.getId().getJobId() + ", monitorId = " + monitorId);
					if(jobMst.getMonitorId() != null){
						String[] args = {jobMst.getId().getJobId(),monitorId};
						throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_JOB_REFERENCE_TO_MONITOR.getMessage(args));
					}
				}
			}
		} catch (InvalidSetting e) {
			throw e;
		} catch (Exception e) {
			HinemosUnknown e1 = new HinemosUnknown(e.getMessage(), e);
			m_log.warn("valideDeleteMonitor() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e1;
		}
	}

	/**
	 * 過去分のログ件数監視集計の妥当性チェック
	 * @param monitorId
	 * @param startDate
	 * @param endDate
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateSummaryLogcount(String monitorId, Long startDate, Long endDate) throws InvalidSetting, InvalidRole {

		// 監視設定
		MonitorInfo monitorInfo = null;
		try {
			monitorInfo = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(monitorId, ObjectPrivilegeMode.MODIFY);
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_TARGET_MONITOR_NOT_FOUND.getMessage(new String[]{monitorId}));
			m_log.info("validateLogcount() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e1;
		}

		// CheckInfo
		LogcountCheckInfo checkInfo = monitorInfo.getLogcountCheckInfo();
		if(checkInfo == null){
			InvalidSetting e = new InvalidSetting("Log Count Monitor Setting is not defined. monitorId = "
					+ monitorInfo.getMonitorId());
			m_log.info("validateSummaryLogcount() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// monitorType
		if(!HinemosModuleConstant.MONITOR_LOGCOUNT.equals(monitorInfo.getMonitorTypeId())){
			InvalidSetting e = new InvalidSetting("This is Log Count Monitor Setting. But MonitorTypeId = "
					+ monitorInfo.getMonitorTypeId());
			m_log.info("validateSummaryLogcount() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// 収集期間
		if (startDate == null || startDate == 0) {
			m_log.warn("validateSummaryLogcount() " + MessageConstant.START.getMessage());
			String[] args = { "(" + MessageConstant.START.getMessage() + ")" };
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SUMMARY_DATE.getMessage(args));
		}
		if (endDate == null || endDate == 0) {
			m_log.warn("validateSummaryLogcount() " + MessageConstant.END.getMessage());
			String[] args = { "(" + MessageConstant.END.getMessage() + ")" };
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SUMMARY_DATE.getMessage(args));
		}
		if (startDate >= endDate) {
			m_log.warn("validateSummaryLogcount() " + MessageConstant.END.getMessage());
			String[] args = { MessageConstant.TIME.getMessage() + "(" + MessageConstant.END.getMessage() + ")",
					MessageConstant.TIME.getMessage() + "(" + MessageConstant.START.getMessage() + ")" };
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_LATER_DATE_AND_TIME.getMessage(args));
		}
	}
}
