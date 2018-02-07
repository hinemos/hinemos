/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorJudgementInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.xcloud.common.CloudMessageConstant;
import com.clustercontrol.xcloud.factory.monitors.CloudServiceBillingDetailRunMonitor;


public class CloudMessageUtil {
	public static final String pluginId_cloud = "CLOUD";
	public static final String InternalScopeText = "Hinemos_Internal";
	
	public static String getExceptionStackTrace(Exception exception) {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(byteArray);
		exception.printStackTrace(writer);
		writer.flush();
		return byteArray.toString();
	}
	
	public static void notify_AutoUpadate_Error_InstanceOperator(
			String cloudScopeId,
			String instanceId,
			Exception exception
			) {
		CloudUtil.notifyInternalMessage(
			CloudUtil.Priority.WARNING,
			pluginId_cloud,
			cloudScopeId,
			"CloudScopeId=" + cloudScopeId + " , ComputeId=" + instanceId,
			InternalScopeText,
			CloudMessageConstant.AUTODETECTION.getMessage(),
			CloudMessageConstant.AUTOUPDATE_ERROR.getMessage(),
			getExceptionStackTrace(exception));
	}
	
	
	public static void notify_AutoUpadate_Error(
			String cloudScopeId,
			String locationId,
			Exception exception
			) {
		CloudUtil.notifyInternalMessage(
			CloudUtil.Priority.WARNING,
			pluginId_cloud,
			cloudScopeId,
			"CloudScopeId=" + cloudScopeId + " , LocationId=" + locationId,
			InternalScopeText,
			CloudMessageConstant.AUTODETECTION.getMessage(),
			CloudMessageConstant.AUTOUPDATE_ERROR.getMessage(),
			getExceptionStackTrace(exception));
	}

	/**
	 * 課金詳細監視([増分])
	 * @param ba
	 * @param judgements
	 * @param targetDate
	 * @param cost
	 * @param resourceIds
	 */
	public static void notifyDelta(MonitorInfo ba, String facilityId, TreeMap<Integer, MonitorJudgementInfo> judgements, Long targetDate, double cost, Set<String> resourceIds) {
		StringBuffer sb = new StringBuffer();
		for (String s: resourceIds) {
			if (sb.length() > 0)
				sb.append(',');
			sb.append(s);
		}
		
		String truncatedPrice = new BigDecimal(cost).setScale(8, BigDecimal.ROUND_UP).toPlainString();
		Logger.getLogger(CloudMessageUtil.class).debug("facilityId=" + ba.getFacilityId() + " , OriginalPriceData=" +cost+" , TruncatedPrice=" + truncatedPrice);
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd H:mm:ss");
		Date d = new Date();
		notifyCost(
				ba,
				facilityId,
				judgements,
				cost,
				d.getTime(),
				CloudMessageConstant.BILLINGALARM_NOTIFY_DELTA.getMessage(format.format(targetDate), truncatedPrice),
				CloudMessageConstant.BILLINGALARM_NOTIFY_ORG_DELTA.getMessage(format.format(targetDate), truncatedPrice, format.format(d.getTime())) + (resourceIds.isEmpty() ? "nothing": sb.toString())
				);
	}
	
	public static void notifySum(MonitorInfo ba, String facilityId, TreeMap<Integer, MonitorJudgementInfo> judgements, double cost, Set<String> resourceIds) {
		StringBuffer sb = new StringBuffer();
		for (String s: resourceIds) {
			if (sb.length() > 0)
				sb.append(',');
			sb.append(s);
		}
		
		String truncatedPrice = new BigDecimal(cost).setScale(8, BigDecimal.ROUND_UP).toPlainString();
		Logger.getLogger(CloudMessageUtil.class).debug("facilityId=" + ba.getFacilityId() + " , OriginalPriceData=" +cost+" , TruncatedPrice=" + truncatedPrice);
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd H:mm:ss");
		Date d = new Date();
		notifyCost(
				ba,
				facilityId,
				judgements,
				cost,
				d.getTime(),
				CloudMessageConstant.BILLINGALARM_NOTIFY_SUM.getMessage(truncatedPrice),
				CloudMessageConstant.BILLINGALARM_NOTIFY_ORG_SUM.getMessage(truncatedPrice, format.format(d.getTime())) + " resourceIds:" + (resourceIds.isEmpty() ? "nothing": sb.toString())
				);
	}
	
	private static void notifyCost(MonitorInfo ba, String facilityId, TreeMap<Integer, MonitorJudgementInfo> judgements, double cost, Long generationDate, String message, String messageOrg) {
		CloudUtil.Priority priority = CloudUtil.checkPriorityRange(judgements, cost);
		
		notifyResult(
				priority,
				ba.getMonitorId(),
				facilityId,
				ba.getApplication(),
				ba.getNotifyGroupId(),
				generationDate,
				message,
				messageOrg
				);
	}
	/**
	 * 失敗時の通知
	 * @param ba
	 * @param exception
	 */
	public static void notifyUnknown(MonitorInfo ba, Exception exception) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd H:mm:ss");
		Date d = new Date();
		notifyResult(
				CloudUtil.Priority.UNKNOWN,
				ba.getMonitorId(),
				ba.getFacilityId(),
				ba.getApplication(),
				ba.getNotifyGroupId(),
				d.getTime(),
				CloudMessageConstant.BILLINGALARM_NOTIFY_UNKNOWN.getMessage(exception.getMessage()),
				CloudMessageConstant.BILLINGALARM_NOTIFY_ORG_UNKNOWN.getMessage(format.format(d.getTime()), CloudMessageUtil.getExceptionStackTrace(exception))
				);
	}

	// 基本通知処理。
	private static void notifyResult(
			CloudUtil.Priority priority,
			String alarmId,
			String facilityId,
			String application,
			String notifyGroupId, 
			Long generationDate, 
			String message,
			String messageOrg) {
		Logger logger = Logger.getLogger(CloudMessageUtil.class);

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {

			//通知情報作成
			OutputBasicInfo notice = CloudUtil.createOutputBasicInfo(priority, CloudServiceBillingDetailRunMonitor.monitorTypeId, alarmId, "", application, facilityId, null, message, messageOrg, generationDate);

			// 通知処理
			notice.setNotifyGroupId(notifyGroupId);
			// 通知設定
			jtm.addCallback(new NotifyCallback(notice));
			logger.debug(alarmId + " is notified.");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	

	public static String createCloudServiceMonitorMessage(String result, String cloudId, String targetName, String message) {
		return CloudMessageConstant.CLOUDSERVICE_MESSAGE_FORMAT.getMessage(
				result,
				CloudMessageConstant.CLOUDSERVICE_CLOUDID.getMessage(),
				cloudId,
				CloudMessageConstant.CLOUDSERVICE_TARGETID.getMessage(),
				targetName,
				message);
	}

	public static String createCloudServiceMonitorMessageOrg(String result, String cloudId, String targetName, String message) {
		return CloudMessageConstant.CLOUDSERVICE_ORG_MESSAGE_FORMAT.getMessage(
				result,
				CloudMessageConstant.CLOUDSERVICE_CLOUDID.getMessage(),
				cloudId,
				CloudMessageConstant.CLOUDSERVICE_TARGETID.getMessage(),
				targetName,
				CloudMessageConstant.CLOUDSERVICE_MESSAGE.getMessage(),
				message);
	}

	public static String createCloudServiceMonitorExceptionMessage(String cloudId, String targetName) {
		return CloudMessageConstant.CLOUDSERVICE_MESSAGE_FORMAT2.getMessage(
				CloudMessageConstant.CLOUDSERVICE_EXCEPTION.getMessage(),
				CloudMessageConstant.CLOUDSERVICE_CLOUDID.getMessage(),
				cloudId,
				CloudMessageConstant.CLOUDSERVICE_TARGETID.getMessage(),
				targetName);
	}

	public static String createCloudServiceMonitorExceptionMessageOrg(String cloudId, String targetName, String message, String stackTrace) {
		return CloudMessageConstant.CLOUDSERVICE_ORG_MESSAGE_FORMAT2.getMessage(
				CloudMessageConstant.CLOUDSERVICE_EXCEPTION.getMessage(),
				CloudMessageConstant.CLOUDSERVICE_CLOUDID.getMessage(),
				cloudId,
				CloudMessageConstant.CLOUDSERVICE_TARGETID.getMessage(),
				targetName,
				CloudMessageConstant.CLOUDSERVICE_MESSAGE.getMessage(),
				message,
				stackTrace);
	}

	public static String createCloudServiceMonitorExceptionMessageOrg(String cloudId, String targetName, Exception exception) {
		return CloudMessageConstant.CLOUDSERVICE_ORG_MESSAGE_FORMAT2.getMessage(
				CloudMessageConstant.CLOUDSERVICE_EXCEPTION.getMessage(),
				CloudMessageConstant.CLOUDSERVICE_CLOUDID.getMessage(),
				cloudId,
				CloudMessageConstant.CLOUDSERVICE_TARGETID.getMessage(),
				targetName,
				CloudMessageConstant.CLOUDSERVICE_MESSAGE.getMessage(),
				exception.getMessage(),
				getExceptionStackTrace(exception));
	}
	
	public static String createCloudServiceMonitorExceptionMessageOrg2(Exception exception) {
		return CloudMessageConstant.CLOUDSERVICE_ORG_MESSAGE_FORMAT3.getMessage(
				CloudMessageConstant.CLOUDSERVICE_EXCEPTION.getMessage(),
				CloudMessageConstant.CLOUDSERVICE_MESSAGE.getMessage(),
				exception.getMessage(),
				getExceptionStackTrace(exception));
	}
}
