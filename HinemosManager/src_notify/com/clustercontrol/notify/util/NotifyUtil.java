/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.jobmanagement.factory.CreateJobSession;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.EventNotifyInfo;
import com.clustercontrol.notify.bean.NotifyJobType;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.NotifyCloudInfo;
import com.clustercontrol.notify.model.NotifyCommandInfo;
import com.clustercontrol.notify.model.NotifyEventInfo;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.model.NotifyInfoDetail;
import com.clustercontrol.notify.model.NotifyInfraInfo;
import com.clustercontrol.notify.model.NotifyJobInfo;
import com.clustercontrol.notify.model.NotifyLogEscalateInfo;
import com.clustercontrol.notify.model.NotifyMailInfo;
import com.clustercontrol.notify.model.NotifyMessageInfo;
import com.clustercontrol.notify.model.NotifyRestInfo;
import com.clustercontrol.notify.model.NotifyStatusInfo;
import com.clustercontrol.notify.monitor.util.OwnerDispatcher;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.NodeConfigSettingInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.repository.util.FacilityUtil;
import com.clustercontrol.repository.util.RepositoryUtil;
import com.clustercontrol.rest.endpoint.notify.dto.CloudNotifyLinkInfoKeyValueObjectRequest;
import com.clustercontrol.rest.endpoint.notify.dto.CloudNotifyLinkInfoKeyValueObjectResponse;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResultCreateSetting;
import com.clustercontrol.sdml.model.SdmlControlSettingInfo;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 通知に関するUtilityクラス<br/>
 *
 */
public class NotifyUtil {

	private static Log log = LogFactory.getLog(NotifyUtil.class);

	/** 監視キー */
	private static final String _KEY_PRIORITY = "PRIORITY";
	private static final String _KEY_PRIORITY_NUM = "PRIORITY_NUM";
	private static final String _KEY_PRIORITY_JP = "PRIORITY_JP";
	private static final String _KEY_PRIORITY_EN = "PRIORITY_EN";

	private static final String _KEY_PLUGIN_ID = "PLUGIN_ID";
	private static final String _KEY_PLUGIN_NAME = "PLUGIN_NAME";
	private static final String _KEY_MONITOR_ID = "MONITOR_ID";
	private static final String _KEY_MONITOR_DETAIL_ID = "MONITOR_DETAIL_ID";
	private static final String _KEY_MONITOR_DESCRIPTION = "MONITOR_DESCRIPTION";
	private static final String _KEY_FACILITY_ID = "FACILITY_ID";
	private static final String _KEY_SCOPE = "SCOPE";

	private static final String _KEY_FACILITY_NAME = "FACILITY_NAME";

	private static final String _KEY_GENERATION_DATE = "GENERATION_DATE";
	private static final String _KEY_APPLICATION = "APPLICATION";
	private static final String _KEY_MESSAGE = "MESSAGE";
	private static final String _KEY_ORG_MESSAGE = "ORG_MESSAGE";

	private static final String _KEY_MONITOR_OWNER_ROLE_ID = "MONITOR_OWNER_ROLE_ID";
	private static final String _KEY_CALENDAR_ID = "CALENDAR_ID";

	private static final String _KEY_JOB_MESSAGE = "JOB_MESSAGE:";
	
	// ★
	private static final String _KEY_JOB_APPROVAL_TEXT = "JOB_APPROVAL_TEXT";
	private static final String _KEY_JOB_APPROVAL_MAIL = "JOB_APPROVAL_MAIL";

	private static final String _KEY_NOTIFY_UUID = "NOTIFY_UUID";

	/** 通知キー */
	private static final String _KEY_NOTIFY_ID = "NOTIFY_ID";
	private static final String _KEY_NOTIFY_DESCRIPTION = "NOTIFY_DESCRIPTION";

	
	private static final String NOTIFY_LOCALE_KEY = "notify.locale";

	/**
	 * 通知情報をハッシュとして返す。
	 * @param outputInfo 通知情報
	 * @return 通知情報のハッシュ
	 */
	public static Map<String, String> createParameter(OutputBasicInfo outputInfo, ArrayList<String> inKeyList) {
		return createParameter(outputInfo, null, inKeyList);
	}

	/**
	 * 通知情報をハッシュとして返す。
	 * @param outputInfo 通知情報
	 * @param notifyInfo 通知設定情報
	 * @return 通知情報のハッシュ
	 */
	public static Map<String, String> createParameter(OutputBasicInfo outputInfo, NotifyInfo notifyInfo, ArrayList<String> inKeyList) {
		Map<String, String> param = null;
		SimpleDateFormat sdf = null;
		param = new HashMap<String, String>();

		if (outputInfo != null) {

			// 言語情報
			Locale locale = getNotifyLocale();

			/** 日時フォーマット。 */
			String subjectDateFormat = HinemosPropertyCommon.notify_date_format.getStringValue();
			if(log.isDebugEnabled()){
				log.debug("TextReplacer.static SUBJECT_DATE_FORMAT = " + subjectDateFormat);
			}

			sdf = new SimpleDateFormat(subjectDateFormat);
			sdf.setTimeZone(HinemosTime.getTimeZone());

			param.put(_KEY_PRIORITY_NUM, String.valueOf(outputInfo.getPriority()));
			if (Messages.getString(PriorityConstant.typeToMessageCode(outputInfo.getPriority()), locale) != null) {
				param.put(_KEY_PRIORITY, Messages.getString(PriorityConstant.typeToMessageCode(outputInfo.getPriority()), locale));
			} else {
				param.put(_KEY_PRIORITY, null);
			}
			if (Messages.getString(PriorityConstant.typeToMessageCode(outputInfo.getPriority()), Locale.JAPANESE) != null) {
				param.put(_KEY_PRIORITY_JP, Messages.getString(PriorityConstant.typeToMessageCode(outputInfo.getPriority()), Locale.JAPANESE));
			} else {
				param.put(_KEY_PRIORITY_JP, null);
			}
			if (Messages.getString(PriorityConstant.typeToMessageCode(outputInfo.getPriority()), Locale.ENGLISH)!= null) {
				param.put(_KEY_PRIORITY_EN, Messages.getString(PriorityConstant.typeToMessageCode(outputInfo.getPriority()), Locale.ENGLISH));
			} else {
				param.put(_KEY_PRIORITY_EN, null);
			}

			String pluginId = outputInfo.getPluginId();
			param.put(_KEY_PLUGIN_ID, pluginId);
			param.put(_KEY_PLUGIN_NAME, Messages.getString(HinemosModuleConstant.nameToMessageCode(pluginId), locale));
			String monitorId = outputInfo.getMonitorId();
			param.put(_KEY_MONITOR_ID, outputInfo.getMonitorId());
			if (monitorId != null && pluginId != null && pluginId.startsWith("MON_")) {
				try {
					MonitorInfo monitorInfo = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(monitorId);
					param.put(_KEY_MONITOR_DESCRIPTION, monitorInfo.getDescription());
					param.put(_KEY_CALENDAR_ID, monitorInfo.getCalendarId());
					param.put(_KEY_MONITOR_OWNER_ROLE_ID, monitorInfo.getOwnerRoleId());
				} catch (MonitorNotFound e) {
					log.debug("createParameter() : monitor not found. " + e.getMessage());
				} catch (InvalidRole e) {
					log.debug("createParameter() : InvalidRole. " + e.getMessage());
				}
			} else {
				param.put(_KEY_MONITOR_DESCRIPTION, "");
				param.put(_KEY_CALENDAR_ID, "");
				param.put(_KEY_MONITOR_OWNER_ROLE_ID, "");
			}
			param.put(_KEY_MONITOR_DETAIL_ID, outputInfo.getSubKey());

			param.put(_KEY_FACILITY_ID, outputInfo.getFacilityId());
			param.put(_KEY_SCOPE, HinemosMessage.replace(outputInfo.getScopeText(), locale));

			if (outputInfo.getGenerationDate() != null) {
				param.put(_KEY_GENERATION_DATE, sdf.format(outputInfo.getGenerationDate()));
			} else {
				param.put(_KEY_GENERATION_DATE, null);
			}
			param.put(_KEY_APPLICATION, HinemosMessage.replace(outputInfo.getApplication(), locale));
			param.put(_KEY_MESSAGE, HinemosMessage.replace(outputInfo.getMessage(), locale));
			param.put(_KEY_ORG_MESSAGE, HinemosMessage.replace(outputInfo.getMessageOrg(), locale));

			List<String> jobFacilityIdList = outputInfo.getJobFacilityId();
			List<String> jobMessageList = outputInfo.getJobMessage();

			if (jobFacilityIdList != null) {
				for (int i = 0; i < jobFacilityIdList.size(); ++i) {
					String key = _KEY_JOB_MESSAGE + jobFacilityIdList.get(i);
					String value = HinemosMessage.replace(jobMessageList.get(i), locale);
					param.put(key, value);
					log.debug("NotifyUtil.createParameter  >>> param.put = : " + key  + "  value = " +  value);
				}

			}

			if (outputInfo.getFacilityId() != null) {
				try {
					RepositoryControllerBean repositoryCtrl = new RepositoryControllerBean();
					FacilityInfo facility
					= repositoryCtrl.getFacilityEntityByPK(outputInfo.getFacilityId());
					param.put(_KEY_FACILITY_NAME, HinemosMessage.replace(facility.getFacilityName(), locale));
					if (FacilityUtil.isNode(facility)) {
						NodeInfo nodeInfo = repositoryCtrl.getNode(outputInfo.getFacilityId());
						Map<String, String> variable = RepositoryUtil.createNodeParameter(nodeInfo, inKeyList);
						param.putAll(variable);
					}
				} catch (FacilityNotFound e) {
					log.debug("createParameter() : facility not found. " + e.getMessage());
				} catch (InvalidRole e) {
					log.debug("createParameter() : InvalidRole. " + e.getMessage());
				} catch (HinemosUnknown e) {
					log.debug("createParameter() : HinemosUnknown. " + e.getMessage());
				} catch (Exception e) {
					log.warn("facility not found. (" + outputInfo.getFacilityId() + ") : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				}
			}
			
			if (outputInfo.getJobApprovalText() != null) {
				param.put(_KEY_JOB_APPROVAL_TEXT, HinemosMessage.replace(outputInfo.getJobApprovalText()));
				log.info("_KEY_JOB_APPROVAL_TEXT" + outputInfo.getJobApprovalText());
			}
			if (outputInfo.getJobApprovalText() != null) {
				param.put(_KEY_JOB_APPROVAL_MAIL, HinemosMessage.replace(outputInfo.getJobApprovalMail()));
				log.info("_KEY_JOB_APPROVAL_MAIL" + outputInfo.getJobApprovalMail());
			}
			param.put(_KEY_NOTIFY_UUID, outputInfo.getNotifyUUID());
		}

		if (notifyInfo != null) {
			param.put(_KEY_NOTIFY_ID, String.valueOf(notifyInfo.getNotifyId()));
			param.put(_KEY_NOTIFY_DESCRIPTION, notifyInfo.getDescription());
		}
		
		if(log.isTraceEnabled()){
			for(Map.Entry<String, String> entry : param.entrySet()){
				log.trace("createParameter() : param[" + entry.getKey() + "]=" + entry.getValue());
			}
		}

		return param;
	}

	public static void copyProperties(NotifyCommandInfo command,
			NotifyCommandInfo entity) {
		entity.setInfoValidFlg(command.getInfoValidFlg());
		entity.setWarnValidFlg(command.getWarnValidFlg());
		entity.setCriticalValidFlg(command.getCriticalValidFlg());
		entity.setUnknownValidFlg(command.getUnknownValidFlg());

		entity.setInfoCommand(command.getInfoCommand());
		entity.setWarnCommand(command.getWarnCommand());
		entity.setCriticalCommand(command.getCriticalCommand());
		entity.setUnknownCommand(command.getUnknownCommand());

		entity.setInfoEffectiveUser(command.getInfoEffectiveUser());
		entity.setWarnEffectiveUser(command.getWarnEffectiveUser());
		entity.setCriticalEffectiveUser(command.getCriticalEffectiveUser());
		entity.setUnknownEffectiveUser(command.getUnknownEffectiveUser());

		entity.setTimeout(command.getTimeout());
		entity.setCommandSettingType(command.getCommandSettingType());
	}

	public static void copyProperties(NotifyEventInfo event,
			NotifyEventInfo entity) {
		entity.setInfoValidFlg(event.getInfoValidFlg());
		entity.setWarnValidFlg(event.getWarnValidFlg());
		entity.setCriticalValidFlg(event.getCriticalValidFlg());
		entity.setUnknownValidFlg(event.getUnknownValidFlg());
		entity.setInfoEventNormalState(event.getInfoEventNormalState());
		entity.setWarnEventNormalState(event.getWarnEventNormalState());
		entity.setCriticalEventNormalState(event.getCriticalEventNormalState());
		entity.setUnknownEventNormalState(event.getUnknownEventNormalState());
	}

	public static void copyProperties(NotifyJobInfo job,
			NotifyJobInfo entity) {
		entity.setNotifyJobType(job.getNotifyJobType());
		entity.setInfoValidFlg(job.getInfoValidFlg());
		entity.setWarnValidFlg(job.getWarnValidFlg());
		entity.setCriticalValidFlg(job.getCriticalValidFlg());
		entity.setUnknownValidFlg(job.getUnknownValidFlg());

		if (job.getNotifyJobType() == NotifyJobType.TYPE_DIRECT) {
			entity.setInfoJobunitId(job.getInfoJobunitId());
			entity.setWarnJobunitId(job.getWarnJobunitId());
			entity.setCriticalJobunitId(job.getCriticalJobunitId());
			entity.setUnknownJobunitId(job.getUnknownJobunitId());
	
			entity.setInfoJobId(job.getInfoJobId());
			entity.setWarnJobId(job.getWarnJobId());
			entity.setCriticalJobId(job.getCriticalJobId());
			entity.setUnknownJobId(job.getUnknownJobId());
	
			entity.setInfoJobFailurePriority(job.getInfoJobFailurePriority());
			entity.setWarnJobFailurePriority(job.getWarnJobFailurePriority());
			entity.setCriticalJobFailurePriority(job.getCriticalJobFailurePriority());
			entity.setUnknownJobFailurePriority(job.getUnknownJobFailurePriority());
	
			entity.setJobExecFacilityFlg(job.getJobExecFacilityFlg());
			entity.setJobExecFacility(job.getJobExecFacility());

			entity.setRetryFlg(null);
			entity.setRetryCount(null);
			entity.setSuccessInternalFlg(null);
			entity.setFailureInternalFlg(null);
			entity.setJoblinkSendSettingId(null);

		} else if (job.getNotifyJobType() == NotifyJobType.TYPE_JOB_LINK_SEND) {
			entity.setRetryFlg(job.getRetryFlg());
			entity.setRetryCount(job.getRetryCount());
			entity.setSuccessInternalFlg(job.getSuccessInternalFlg());
			entity.setFailureInternalFlg(job.getFailureInternalFlg());
			entity.setJoblinkSendSettingId(job.getJoblinkSendSettingId());
			entity.setInfoJobunitId(null);
			entity.setWarnJobunitId(null);
			entity.setCriticalJobunitId(null);
			entity.setUnknownJobunitId(null);
			entity.setInfoJobId(null);
			entity.setWarnJobId(null);
			entity.setCriticalJobId(null);
			entity.setUnknownJobId(null);
			entity.setInfoJobFailurePriority(null);
			entity.setWarnJobFailurePriority(null);
			entity.setCriticalJobFailurePriority(null);
			entity.setUnknownJobFailurePriority(null);
			entity.setJobExecFacilityFlg(null);
			entity.setJobExecFacility(null);
		}
	}

	public static void copyProperties(NotifyLogEscalateInfo log,
			NotifyLogEscalateInfo entity) {
		entity.setInfoValidFlg(log.getInfoValidFlg());
		entity.setWarnValidFlg(log.getWarnValidFlg());
		entity.setCriticalValidFlg(log.getCriticalValidFlg());
		entity.setUnknownValidFlg(log.getUnknownValidFlg());

		entity.setInfoEscalateMessage(log.getInfoEscalateMessage());
		entity.setWarnEscalateMessage(log.getWarnEscalateMessage());
		entity.setCriticalEscalateMessage(log.getCriticalEscalateMessage());
		entity.setUnknownEscalateMessage(log.getUnknownEscalateMessage());

		entity.setInfoSyslogFacility(log.getInfoSyslogFacility());
		entity.setWarnSyslogFacility(log.getWarnSyslogFacility());
		entity.setCriticalSyslogFacility(log.getCriticalSyslogFacility());
		entity.setUnknownSyslogFacility(log.getUnknownSyslogFacility());

		entity.setInfoSyslogPriority(log.getInfoSyslogPriority());
		entity.setWarnSyslogPriority(log.getWarnSyslogPriority());
		entity.setCriticalSyslogPriority(log.getCriticalSyslogPriority());
		entity.setUnknownSyslogPriority(log.getUnknownSyslogPriority());

		entity.setEscalateFacilityFlg(log.getEscalateFacilityFlg());
		entity.setEscalatePort(log.getEscalatePort());
		entity.setEscalateFacility(log.getEscalateFacility());
	}

	public static void copyProperties(NotifyMailInfo mail,
			NotifyMailInfo entity) {
		entity.setInfoValidFlg(mail.getInfoValidFlg());
		entity.setWarnValidFlg(mail.getWarnValidFlg());
		entity.setCriticalValidFlg(mail.getCriticalValidFlg());
		entity.setUnknownValidFlg(mail.getUnknownValidFlg());

		entity.setInfoMailAddress(mail.getInfoMailAddress());
		entity.setWarnMailAddress(mail.getWarnMailAddress());
		entity.setCriticalMailAddress(mail.getCriticalMailAddress());
		entity.setUnknownMailAddress(mail.getUnknownMailAddress());
	}

	public static void copyProperties(NotifyStatusInfo status,
			NotifyStatusInfo entity) {
		entity.setInfoValidFlg(status.getInfoValidFlg());
		entity.setWarnValidFlg(status.getWarnValidFlg());
		entity.setCriticalValidFlg(status.getCriticalValidFlg());
		entity.setUnknownValidFlg(status.getUnknownValidFlg());

		entity.setStatusInvalidFlg(status.getStatusInvalidFlg());
		entity.setStatusUpdatePriority(status.getStatusUpdatePriority());
		entity.setStatusValidPeriod(status.getStatusValidPeriod());
	}
	
	public static void copyProperties(NotifyMessageInfo job, NotifyMessageInfo entity) {
		entity.setInfoValidFlg(job.getInfoValidFlg());
		entity.setWarnValidFlg(job.getWarnValidFlg());
		entity.setCriticalValidFlg(job.getCriticalValidFlg());
		entity.setUnknownValidFlg(job.getUnknownValidFlg());

		entity.setInfoRulebaseId(job.getInfoRulebaseId());
		entity.setWarnRulebaseId(job.getWarnRulebaseId());
		entity.setCriticalRulebaseId(job.getCriticalRulebaseId());
		entity.setUnknownRulebaseId(job.getUnknownRulebaseId());
	}

	public static void copyProperties(NotifyInfraInfo job,
			NotifyInfraInfo entity) {
		entity.setInfoValidFlg(job.getInfoValidFlg());
		entity.setWarnValidFlg(job.getWarnValidFlg());
		entity.setCriticalValidFlg(job.getCriticalValidFlg());
		entity.setUnknownValidFlg(job.getUnknownValidFlg());

		entity.setInfoInfraId(job.getInfoInfraId());
		entity.setWarnInfraId(job.getWarnInfraId());
		entity.setCriticalInfraId(job.getCriticalInfraId());
		entity.setUnknownInfraId(job.getUnknownInfraId());

		entity.setInfoInfraFailurePriority(job.getInfoInfraFailurePriority());
		entity.setWarnInfraFailurePriority(job.getWarnInfraFailurePriority());
		entity.setCriticalInfraFailurePriority(job.getCriticalInfraFailurePriority());
		entity.setUnknownInfraFailurePriority(job.getUnknownInfraFailurePriority());

		entity.setInfraExecFacilityFlg(job.getInfraExecFacilityFlg());
		entity.setInfraExecFacility(job.getInfraExecFacility());
	}
	
	public static void copyProperties(NotifyCloudInfo cloud, NotifyCloudInfo entity){
		entity.setFacilityId(cloud.getFacilityId());
		entity.setTextScope(cloud.getScopeText());
		entity.setPlatformType(cloud.getPlatformType());
		
		entity.setInfoValidFlg(cloud.getInfoValidFlg());
		entity.setWarnValidFlg(cloud.getWarnValidFlg());
		entity.setCriticalValidFlg(cloud.getCriticalValidFlg());
		entity.setUnknownValidFlg(cloud.getUnknownValidFlg());

		entity.setInfoAccessKey(cloud.getInfoAccessKey());
		entity.setInfoDataVersion(cloud.getInfoDataVersion());
		entity.setInfoDetailType(cloud.getInfoDetailType());
		entity.setInfoEventBus(cloud.getInfoEventBus());
		entity.setInfoJsonData(cloud.getInfoJsonData());
		entity.setInfoSource(cloud.getInfoSource());
		
		entity.setWarnAccessKey(cloud.getWarnAccessKey());
		entity.setWarnDataVersion(cloud.getWarnDataVersion());
		entity.setWarnDetailType(cloud.getWarnDetailType());
		entity.setWarnEventBus(cloud.getWarnEventBus());
		entity.setWarnJsonData(cloud.getWarnJsonData());
		entity.setWarnSource(cloud.getWarnSource());
		
		entity.setCritAccessKey(cloud.getCritAccessKey());
		entity.setCritDataVersion(cloud.getCritDataVersion());
		entity.setCritDetailType(cloud.getCritDetailType());
		entity.setCritEventBus(cloud.getCritEventBus());
		entity.setCritJsonData(cloud.getCritJsonData());
		entity.setCritSource(cloud.getCritSource());
		
		entity.setUnkAccessKey(cloud.getUnkAccessKey());
		entity.setUnkDataVersion(cloud.getUnkDataVersion());
		entity.setUnkDetailType(cloud.getUnkDetailType());
		entity.setUnkEventBus(cloud.getUnkEventBus());
		entity.setUnkJsonData(cloud.getUnkJsonData());
		entity.setUnkSource(cloud.getUnkSource());
		
	}

	public static void copyProperties(NotifyRestInfo rest, NotifyRestInfo entity) {
		entity.setInfoValidFlg(rest.getInfoValidFlg());
		entity.setWarnValidFlg(rest.getWarnValidFlg());
		entity.setCriticalValidFlg(rest.getCriticalValidFlg());
		entity.setUnknownValidFlg(rest.getUnknownValidFlg());

		entity.setInfoRestAccessId(rest.getInfoRestAccessId());
		entity.setWarnRestAccessId(rest.getWarnRestAccessId());
		entity.setCriticalRestAccessId(rest.getCriticalRestAccessId());
		entity.setUnknownRestAccessId(rest.getUnknownRestAccessId());
	}

	public static ArrayList<Integer> getValidFlgIndexes(NotifyInfoDetail info) {
		Boolean[] validFlgs = new Boolean[] {
				info.getInfoValidFlg(),
				info.getWarnValidFlg(),
				info.getCriticalValidFlg(),
				info.getUnknownValidFlg() };

		ArrayList<Integer> validFlgIndexes = new ArrayList<Integer>();
		for (int i = 0; i < validFlgs.length; i++) {
			if (validFlgs[i].booleanValue()) {
				validFlgIndexes.add(i);
			}
		}
		return validFlgIndexes;
	}

	public static String getOwnerRoleId(NotifyRequestMessage msg, boolean isEvent) {
		OutputBasicInfo info = msg.getOutputInfo();
		if (info == null) {
			return null;
		}
		return getOwnerRoleId(info.getPluginId(), info.getMonitorId(), info.getSubKey(), info.getFacilityId(), isEvent);
	}
	
	public static String getOwnerRoleId(String pluginId, String monitorId, String monitorDetailId, String facilityId, boolean isEvent) {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			
			// 通知元が監視の場合
			if(pluginId.matches(HinemosModuleConstant.MONITOR+".*")){
				// オブジェクト権限チェックのため、cc_monitor_infoのowner_role_idを設定する
				MonitorInfo monitorInfo
				= em.find(MonitorInfo.class, monitorId, ObjectPrivilegeMode.NONE);
				if (monitorInfo != null && monitorInfo.getOwnerRoleId() != null) {
					return monitorInfo.getOwnerRoleId();
				} else {
					return RoleIdConstant.INTERNAL;
				}
			}
			// 通知元がジョブの場合
			else if(pluginId.matches(HinemosModuleConstant.JOB+".*")) {
				if (HinemosModuleConstant.SYSYTEM.equals(monitorId)) {
					// monitorIdがSYSの場合はINTERNAL
					return RoleIdConstant.INTERNAL;
				}
				// オブジェクト権限チェックのため、cc_job_session_jobのowner_role_idを設定する
				JobSessionEntity jobSessionEntity
				= em.find(JobSessionEntity.class, monitorId, ObjectPrivilegeMode.NONE);
				JobSessionJobEntity jobSessionJobEntity = null;
				if (jobSessionEntity == null) {
					log.warn("EventLogEntity(Job) is null : " + monitorId);
				} else {
					List<JobSessionJobEntity> jobSessionJobEntityList = jobSessionEntity.getJobSessionJobEntities();
					Iterator<JobSessionJobEntity> it = jobSessionJobEntityList.iterator();
					while(it.hasNext()) {
						jobSessionJobEntity = it.next();
						// ジョブユニット「_ROOT_」でない場合はwhileを抜ける
						// ジョブユニットが「_ROOT_」であるものは、オーナーロールIDが「ALL_USERS」であるため
						if(!jobSessionJobEntity.getId().getJobunitId().matches(CreateJobSession.TOP_JOBUNIT_ID)) {
							break;
						}
					}
				}

				if (jobSessionJobEntity != null && jobSessionJobEntity.getOwnerRoleId() != null) {
					return jobSessionJobEntity.getOwnerRoleId();
				} else {
					return RoleIdConstant.INTERNAL;
				}
			}
			// 通知元がメンテナンスの場合
			else if(pluginId.matches(HinemosModuleConstant.SYSYTEM_MAINTENANCE)){
				// オブジェクト権限チェックのため、cc_maintenance_infoのowner_role_idを設定する
				MaintenanceInfo maintenanceInfoEntity
				= em.find(MaintenanceInfo.class, monitorId, ObjectPrivilegeMode.NONE);
				if (maintenanceInfoEntity != null && maintenanceInfoEntity.getOwnerRoleId() != null) {
					return maintenanceInfoEntity.getOwnerRoleId();
				} else {
					return RoleIdConstant.INTERNAL;
				}
			}
			// 通知元が自動デバイスサーチの場合
			else if(pluginId.matches(HinemosModuleConstant.REPOSITORY_DEVICE_SEARCH)){
				// 6.1より仕様変更
				// 当該ノードのオーナーロールIDをイベントのオーナーロールIDとして指定。
				FacilityInfo facility = FacilityTreeCache.getFacilityInfo(facilityId);
				if (facility != null && facility.getOwnerRoleId() != null) {
					return facility.getOwnerRoleId();
				} else {
					// 通常はないはず
					return RoleIdConstant.INTERNAL;
				}
			}
			// 通知元が環境構築の場合
			else if(pluginId.matches(HinemosModuleConstant.INFRA)){
				// オブジェクト権限チェックのため、cc_maintenance_infoのowner_role_idを設定する
				InfraManagementInfo infraEntity
				= em.find(InfraManagementInfo.class, monitorId, ObjectPrivilegeMode.NONE);
				
				if (infraEntity != null && infraEntity.getOwnerRoleId() != null) {
					return infraEntity.getOwnerRoleId();
				} else {
					return RoleIdConstant.INTERNAL;
				}
			}
			// 通知元が構成情報設定の場合
			else if(pluginId.matches(HinemosModuleConstant.NODE_CONFIG_SETTING)){
				// オブジェクト権限チェックのため、cc_node_config_setting_infoのowner_role_idを設定する
				NodeConfigSettingInfo nodeConfigSettingEntity
				= em.find(NodeConfigSettingInfo.class, monitorId, ObjectPrivilegeMode.NONE);
				
				if (nodeConfigSettingEntity != null && nodeConfigSettingEntity.getOwnerRoleId() != null) {
					return nodeConfigSettingEntity.getOwnerRoleId();
				} else {
					return RoleIdConstant.INTERNAL;
				}
			}
			// 通知元がSDMLの場合
			else if(pluginId.matches(HinemosModuleConstant.SDML_CONTROL)){
				// オブジェクト権限チェックのため、cc_sdml_control_setting_infoのowner_role_idを設定する
				SdmlControlSettingInfo sdmlControlSettingEntity
				= em.find(SdmlControlSettingInfo.class, monitorId, ObjectPrivilegeMode.NONE);
				
				if (sdmlControlSettingEntity != null && sdmlControlSettingEntity.getOwnerRoleId() != null) {
					return sdmlControlSettingEntity.getOwnerRoleId();
				} else {
					return RoleIdConstant.INTERNAL;
				}
			}
			// 通知元がシナリオ実績作成設定、およびシナリオ実績更新の場合
			else if(pluginId.matches(HinemosModuleConstant.RPA_SCENARIO_CREATE) || pluginId.matches(HinemosModuleConstant.RPA_SCENARIO_CORRECT)) {
				// オブジェクト権限チェックのため、cc_rpa_scenario_operation_result_create_settingのowner_role_idを設定する
				RpaScenarioOperationResultCreateSetting rpaScenarioOperationResultCreateSettingEntity
				= em.find(RpaScenarioOperationResultCreateSetting.class, monitorId, ObjectPrivilegeMode.NONE);
				if (rpaScenarioOperationResultCreateSettingEntity != null && rpaScenarioOperationResultCreateSettingEntity.getOwnerRoleId() != null) {
					return rpaScenarioOperationResultCreateSettingEntity.getOwnerRoleId();
				} else {
					return RoleIdConstant.INTERNAL;
				}
			}
			// 通知元が上記以外のプラグインIDの場合
			// ここでオプション等の任意イベント（設定に紐付かないタイプのもの）についてオーナロールを決定することが可能。
			// プラグインIDをキーにして、ObjectSharingServiceにIEventOwnerDeterminerの実装クラスを登録し、
			// そこでオーナロールを決定する。
			// 事前に当該プラグインIDに対応するIEventOwnerDeterminerの実装クラスが登録されていない場合には、
			// オーナはINTERNALとなる。
			// 但し、このルートではリフレクションを使うため、多くのイベントが発生するオプションなどでこの機構を使うと性能的に
			// 問題になる可能性が高いため、そういう場合にはここに分岐を作って直接処理をするべき。
			else {
				return OwnerDispatcher.getOptionalOwner(monitorId, monitorDetailId, pluginId, facilityId, isEvent);
			}
		}
	}
	
	public static Locale getNotifyLocale() {
		String localeStr = HinemosPropertyCommon.notify_locale.getStringValue();
		Locale locale = Locale.getDefault();
		if (localeStr != null) {
			try {
				locale = new Locale(localeStr);
			} catch (Exception e) {
				log.info("unknown language (" + NOTIFY_LOCALE_KEY + ") : " + e.getMessage());
			}
		}
		return locale;
	}
	
	public static void setUserItemValue(EventNotifyInfo event, int index, String value) {
		switch (index) {
		case 1:
			event.setUserItem01(value);
			break;
		
		case 2:
			event.setUserItem02(value);
			break;
		
		case 3:
			event.setUserItem03(value);
			break;
		
		case 4:
			event.setUserItem04(value);
			break;
		
		case 5:
			event.setUserItem05(value);
			break;
		
		case 6:
			event.setUserItem06(value);
			break;
		
		case 7:
			event.setUserItem07(value);
			break;
		
		case 8:
			event.setUserItem08(value);
			break;
		
		case 9:
			event.setUserItem09(value);
			break;
		
		case 10:
			event.setUserItem10(value);
			break;
		
		case 11:
			event.setUserItem11(value);
			break;
		
		case 12:
			event.setUserItem12(value);
			break;
		
		case 13:
			event.setUserItem13(value);
			break;
		
		case 14:
			event.setUserItem14(value);
			break;
		
		case 15:
			event.setUserItem15(value);
			break;
		
		case 16:
			event.setUserItem16(value);
			break;
		
		case 17:
			event.setUserItem17(value);
			break;
		
		case 18:
			event.setUserItem18(value);
			break;
		
		case 19:
			event.setUserItem19(value);
			break;
		
		case 20:
			event.setUserItem20(value);
			break;
		
		case 21:
			event.setUserItem21(value);
			break;
		
		case 22:
			event.setUserItem22(value);
			break;
		
		case 23:
			event.setUserItem23(value);
			break;
		
		case 24:
			event.setUserItem24(value);
			break;
		
		case 25:
			event.setUserItem25(value);
			break;
		
		case 26:
			event.setUserItem26(value);
			break;
		
		case 27:
			event.setUserItem27(value);
			break;
		
		case 28:
			event.setUserItem28(value);
			break;
		
		case 29:
			event.setUserItem29(value);
			break;
		
		case 30:
			event.setUserItem30(value);
			break;
		
		case 31:
			event.setUserItem31(value);
			break;
		
		case 32:
			event.setUserItem32(value);
			break;
		
		case 33:
			event.setUserItem33(value);
			break;
		
		case 34:
			event.setUserItem34(value);
			break;
		
		case 35:
			event.setUserItem35(value);
			break;
		
		case 36:
			event.setUserItem36(value);
			break;
		
		case 37:
			event.setUserItem37(value);
			break;
		
		case 38:
			event.setUserItem38(value);
			break;
		
		case 39:
			event.setUserItem39(value);
			break;
		
		case 40:
			event.setUserItem40(value);
			break;
			
		default:
			break;
		}
	}
	
	public static String getUserItemValue(EventNotifyInfo event, int index) {
		switch (index) {
		case 1:
			return event.getUserItem01();
		case 2:
			return event.getUserItem02();
		case 3:
			return event.getUserItem03();
		case 4:
			return event.getUserItem04();
		case 5:
			return event.getUserItem05();
		case 6:
			return event.getUserItem06();
		case 7:
			return event.getUserItem07();
		case 8:
			return event.getUserItem08();
		case 9:
			return event.getUserItem09();
		case 10:
			return event.getUserItem10();
		case 11:
			return event.getUserItem11();
		case 12:
			return event.getUserItem12();
		case 13:
			return event.getUserItem13();
		case 14:
			return event.getUserItem14();
		case 15:
			return event.getUserItem15();
		case 16:
			return event.getUserItem16();
		case 17:
			return event.getUserItem17();
		case 18:
			return event.getUserItem18();
		case 19:
			return event.getUserItem19();
		case 20:
			return event.getUserItem20();
		case 21:
			return event.getUserItem21();
		case 22:
			return event.getUserItem22();
		case 23:
			return event.getUserItem23();
		case 24:
			return event.getUserItem24();
		case 25:
			return event.getUserItem25();
		case 26:
			return event.getUserItem26();
		case 27:
			return event.getUserItem27();
		case 28:
			return event.getUserItem28();
		case 29:
			return event.getUserItem29();
		case 30:
			return event.getUserItem30();
		case 31:
			return event.getUserItem31();
		case 32:
			return event.getUserItem32();
		case 33:
			return event.getUserItem33();
		case 34:
			return event.getUserItem34();
		case 35:
			return event.getUserItem35();
		case 36:
			return event.getUserItem36();
		case 37:
			return event.getUserItem37();
		case 38:
			return event.getUserItem38();
		case 39:
			return event.getUserItem39();
		case 40:
			return event.getUserItem40();
		default:
			break;
		}
		return null;
	}
	
	
	public static void setUserItemValue(OutputBasicInfo event, int index, String value) {
		switch (index) {
		case 1:
			event.setUserItem01(value);
			break;
		
		case 2:
			event.setUserItem02(value);
			break;
		
		case 3:
			event.setUserItem03(value);
			break;
		
		case 4:
			event.setUserItem04(value);
			break;
		
		case 5:
			event.setUserItem05(value);
			break;
		
		case 6:
			event.setUserItem06(value);
			break;
		
		case 7:
			event.setUserItem07(value);
			break;
		
		case 8:
			event.setUserItem08(value);
			break;
		
		case 9:
			event.setUserItem09(value);
			break;
		
		case 10:
			event.setUserItem10(value);
			break;
		
		case 11:
			event.setUserItem11(value);
			break;
		
		case 12:
			event.setUserItem12(value);
			break;
		
		case 13:
			event.setUserItem13(value);
			break;
		
		case 14:
			event.setUserItem14(value);
			break;
		
		case 15:
			event.setUserItem15(value);
			break;
		
		case 16:
			event.setUserItem16(value);
			break;
		
		case 17:
			event.setUserItem17(value);
			break;
		
		case 18:
			event.setUserItem18(value);
			break;
		
		case 19:
			event.setUserItem19(value);
			break;
		
		case 20:
			event.setUserItem20(value);
			break;
		
		case 21:
			event.setUserItem21(value);
			break;
		
		case 22:
			event.setUserItem22(value);
			break;
		
		case 23:
			event.setUserItem23(value);
			break;
		
		case 24:
			event.setUserItem24(value);
			break;
		
		case 25:
			event.setUserItem25(value);
			break;
		
		case 26:
			event.setUserItem26(value);
			break;
		
		case 27:
			event.setUserItem27(value);
			break;
		
		case 28:
			event.setUserItem28(value);
			break;
		
		case 29:
			event.setUserItem29(value);
			break;
		
		case 30:
			event.setUserItem30(value);
			break;
		
		case 31:
			event.setUserItem31(value);
			break;
		
		case 32:
			event.setUserItem32(value);
			break;
		
		case 33:
			event.setUserItem33(value);
			break;
		
		case 34:
			event.setUserItem34(value);
			break;
		
		case 35:
			event.setUserItem35(value);
			break;
		
		case 36:
			event.setUserItem36(value);
			break;
		
		case 37:
			event.setUserItem37(value);
			break;
		
		case 38:
			event.setUserItem38(value);
			break;
		
		case 39:
			event.setUserItem39(value);
			break;
		
		case 40:
			event.setUserItem40(value);
			break;
			
		default:
			break;
		}
	}
	
	public static String getUserItemValue(OutputBasicInfo out, int index) {
		switch (index) {
		case 1:
			return out.getUserItem01();
		case 2:
			return out.getUserItem02();
		case 3:
			return out.getUserItem03();
		case 4:
			return out.getUserItem04();
		case 5:
			return out.getUserItem05();
		case 6:
			return out.getUserItem06();
		case 7:
			return out.getUserItem07();
		case 8:
			return out.getUserItem08();
		case 9:
			return out.getUserItem09();
		case 10:
			return out.getUserItem10();
		case 11:
			return out.getUserItem11();
		case 12:
			return out.getUserItem12();
		case 13:
			return out.getUserItem13();
		case 14:
			return out.getUserItem14();
		case 15:
			return out.getUserItem15();
		case 16:
			return out.getUserItem16();
		case 17:
			return out.getUserItem17();
		case 18:
			return out.getUserItem18();
		case 19:
			return out.getUserItem19();
		case 20:
			return out.getUserItem20();
		case 21:
			return out.getUserItem21();
		case 22:
			return out.getUserItem22();
		case 23:
			return out.getUserItem23();
		case 24:
			return out.getUserItem24();
		case 25:
			return out.getUserItem25();
		case 26:
			return out.getUserItem26();
		case 27:
			return out.getUserItem27();
		case 28:
			return out.getUserItem28();
		case 29:
			return out.getUserItem29();
		case 30:
			return out.getUserItem30();
		case 31:
			return out.getUserItem31();
		case 32:
			return out.getUserItem32();
		case 33:
			return out.getUserItem33();
		case 34:
			return out.getUserItem34();
		case 35:
			return out.getUserItem35();
		case 36:
			return out.getUserItem36();
		case 37:
			return out.getUserItem37();
		case 38:
			return out.getUserItem38();
		case 39:
			return out.getUserItem39();
		case 40:
			return out.getUserItem40();
		default:
			break;
		}
		return null;
	}

	/**
	 * クラウド通知のDTO個別変換用メソッド<BR>
	 * ディテール/データのリストをjsonに変換します。
	 * 
	 * @param mapData
	 * @return
	 * @throws HinemosUnknown
	 */
	public static String getJsonStringForCloudNotify(List<CloudNotifyLinkInfoKeyValueObjectRequest> infoList) throws HinemosUnknown {
		
		// 空の場合は変換せずに空文字を返す
		if (infoList == null || infoList.isEmpty()){
			return "";
		}
		
		HashMap<String,String> mapData = new HashMap<String,String>();
		
		for(CloudNotifyLinkInfoKeyValueObjectRequest data: infoList){
			mapData.put(data.getName(), data.getValue());
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = "";
		try {
			jsonString = mapper.writeValueAsString(mapData);
		} catch (JsonProcessingException e) {
			log.error("getJsonStringForCloudNotify(): Failed parsing map to json", e);
			throw new HinemosUnknown(e);
		}

		return jsonString;
	}

	/**
	 * クラウド通知のDTO個別変換用メソッド<BR>
	 * jsonのディテールデータをリストに変換します。
	 * 
	 * @param jsonString
	 * @return
	 * @throws HinemosUnknown
	 */
	public static List<CloudNotifyLinkInfoKeyValueObjectResponse> getDataListForCloudNotify(String jsonString) throws HinemosUnknown {
		
		List<CloudNotifyLinkInfoKeyValueObjectResponse> dataList = new ArrayList<CloudNotifyLinkInfoKeyValueObjectResponse>();

		if (jsonString == null || jsonString.isEmpty()) {
			return dataList;
		}

		ObjectMapper om = new ObjectMapper();
		HashMap<String, String> mapData = null;
		try {
			// キーがString、値がObjectのマップに読み込みます。
			mapData = om.readValue(jsonString, new TypeReference<HashMap<String, String>>() {
			});
		} catch (Exception e) {
			// mapへの変換に失敗した場合
			log.error("getMapDataForCloudNotify(): Map Parse failed", e);
			throw new HinemosUnknown(e);
		}
		
		// listへの変換
		
		for (Entry<String, String> tmpSet : mapData.entrySet()){
			CloudNotifyLinkInfoKeyValueObjectResponse dataObject = new CloudNotifyLinkInfoKeyValueObjectResponse();
			dataObject.setName(tmpSet.getKey());
			dataObject.setValue(tmpSet.getValue());
			dataList.add(dataObject);
		}
		
		return dataList;
	}

}