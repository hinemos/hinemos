/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.LogFormatKeyPatternDuplicate;
import com.clustercontrol.fault.LogFormatUsed;
import com.clustercontrol.hub.model.LogFormat;
import com.clustercontrol.hub.model.LogFormatKey;
import com.clustercontrol.hub.model.TransferInfo;
import com.clustercontrol.hub.session.HubControllerBean;
import com.clustercontrol.hub.session.TransferFactory;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.util.MessageConstant;

public class HubValidator {
	private static Logger m_log = Logger.getLogger( HubValidator.class );


	/**
	 * ログフォーマット
	 * @param format
	 * @throws InvalidSetting
	 * @throws LogIdDuplicate 
	 */
	public static void validateLogFormat(LogFormat format, boolean isModify) throws InvalidSetting, LogFormatKeyPatternDuplicate {
		if(format == null){
			InvalidSetting e = new InvalidSetting("LogFormat is not defined.");
			m_log.info("validate() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		//FormatId
		CommonValidator.validateId(MessageConstant.HUB_LOG_FORMAT_ID.getMessage(), format.getLogFormatId(), 64);
		
		//Description
		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(), format.getDescription(), false, 0, 256);
		
		if (!isModify) {
			//OwnerRoleId
			CommonValidator.validateOwnerRoleId(format.getOwnerRoleId(), true, format.getLogFormatId(), HinemosModuleConstant.HUB_LOGFORMAT);
		}
		//TimestampRegex
		CommonValidator.validateRegex(MessageConstant.HUB_LOG_FORMAT_DATE_EXTRACTION_PATTERN.getMessage(), format.getTimestampRegex(), false);
		//LogFormatKey
		Set<String> set = new HashSet<String>();
		for (LogFormatKey key : format.getKeyPatternList()) {
			//key
			CommonValidator.validateId(MessageConstant.HUB_LOG_FORMAT_KEY.getMessage(),key.getKey(), 64);
			if (set.contains(key.getKey())) {
				LogFormatKeyPatternDuplicate e = new LogFormatKeyPatternDuplicate(MessageConstant.MESSAGE_HUB_LOG_FORMAT_DUPLICATION_KEY.getMessage(key.getKey()));
				m_log.info("validateLogFormat() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}else {
				set.add(key.getKey());
			}
			//Description
			CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(), key.getDescription(), false, 0, 256);
			//Regex
			CommonValidator.validateRegex(key.getKey() + " - " + key.getPattern(), key.getPattern(), false);
			//Value Type
//			CommonValidator.validateString(Messages.getString("word.format.value.type"),key.getValueType(), true, 1, 64);
		}
	}
	/**
	 * ログフォーマットは、ログ収集設定で使用する。
	 * ログ収集設定で使用済みのログフォーマットは削除できないようにここでチェックする。
	 * @param formatId
	 * @throws InvalidSetting
	 * @throws HinemosUnknown 
	 */
	public static void validateDeleteLogFormat(String formatId) throws LogFormatUsed, HinemosUnknown {
		List<MonitorInfo> monitorList = com.clustercontrol.monitor.run.util.QueryUtil.getAllMonitorInfo();
		for(MonitorInfo monitor : monitorList){
			m_log.debug("validateDeleteLogFormat() target MonitorInfo " + monitor.getMonitorId() + ", logFormatId = " + formatId);
			if (monitor.getLogFormatId() != null && monitor.getLogFormatId().equals(formatId)) {
				throw new LogFormatUsed(MessageConstant.MESSAGE_HUB_LOG_FORMAT_USED.getMessage(monitor.getMonitorId(), formatId));
			}
		}
	}
	

	/**
	 * ログフォーマット
	 * @param format
	 * @throws InvalidSetting
	 * @throws LogIdDuplicate 
	 */
	public static void validateTransferInfo(TransferInfo transferInfo, boolean isModify) throws InvalidSetting {
		if (transferInfo == null)
			throwInvalidSetting("Transfer setting is not defined.");
		
		//TransferId
		CommonValidator.validateId(MessageConstant.HUB_TRANSFER_ID.getMessage(), transferInfo.getTransferId(), 64);
		//Description
		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(), transferInfo.getDescription(), false, 0, 256);
		
		if (!isModify) {
			//OwnerRoleId
			CommonValidator.validateOwnerRoleId(transferInfo.getOwnerRoleId(), true, transferInfo.getTransferId(), HinemosModuleConstant.HUB_TRANSFER);
		}
		
		// 転送種別
		if (transferInfo.getTransType() == null)
			throwInvalidSetting("TransType must be set.");
		
		switch (transferInfo.getTransType()) {
		case realtime:
			break;
		case batch:
			if (transferInfo.getInterval() == null)
				throwInvalidSetting("Interval must be set.");
			
			if (transferInfo.getInterval() >= 24 && transferInfo.getInterval() < 0)
				throwInvalidSetting("Interval must be 0 or over, and less than 24 when TransType is batch.");
			break;
		case delay:
			if (transferInfo.getInterval() > 30 && transferInfo.getInterval() < 0)
				throwInvalidSetting("Interval must be 0 or over, and 30 or less when TransType is delay.");
			break;
		}
		
		TransferFactory factory = HubControllerBean.getTransferFactory(transferInfo.getDestTypeId());
		factory.validate(transferInfo);
	}
	
	private static void throwInvalidSetting(String message, Object...args) throws InvalidSetting {
		InvalidSetting e = new InvalidSetting(String.format(message, args));
		m_log.info("validate() : "
				+ e.getClass().getSimpleName() + ", " + e.getMessage());
		throw e;
	}
}