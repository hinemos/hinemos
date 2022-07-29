/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AddCloudNotifyRequest;
import org.openapitools.client.model.AddCommandNotifyRequest;
import org.openapitools.client.model.AddEventNotifyRequest;
import org.openapitools.client.model.AddInfraNotifyRequest;
import org.openapitools.client.model.AddJobNotifyRequest;
import org.openapitools.client.model.AddLogEscalateNotifyRequest;
import org.openapitools.client.model.AddMailNotifyRequest;
import org.openapitools.client.model.AddMessageNotifyRequest;
import org.openapitools.client.model.AddRestNotifyRequest;
import org.openapitools.client.model.AddStatusNotifyRequest;
import org.openapitools.client.model.CloudNotifyInfoResponse;
import org.openapitools.client.model.CommandNotifyInfoResponse;
import org.openapitools.client.model.EventNotifyDetailInfoRequest;
import org.openapitools.client.model.EventNotifyInfoResponse;
import org.openapitools.client.model.InfraNotifyDetailInfoRequest;
import org.openapitools.client.model.InfraNotifyInfoResponse;
import org.openapitools.client.model.JobNotifyDetailInfoRequest;
import org.openapitools.client.model.JobNotifyInfoResponse;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoRequest;
import org.openapitools.client.model.LogEscalateNotifyInfoResponse;
import org.openapitools.client.model.MailNotifyInfoResponse;
import org.openapitools.client.model.MessageNotifyInfoResponse;
import org.openapitools.client.model.ModifyCloudNotifyRequest;
import org.openapitools.client.model.ModifyCommandNotifyRequest;
import org.openapitools.client.model.ModifyEventNotifyRequest;
import org.openapitools.client.model.ModifyInfraNotifyRequest;
import org.openapitools.client.model.ModifyJobNotifyRequest;
import org.openapitools.client.model.ModifyLogEscalateNotifyRequest;
import org.openapitools.client.model.ModifyMailNotifyRequest;
import org.openapitools.client.model.ModifyMessageNotifyRequest;
import org.openapitools.client.model.ModifyRestNotifyRequest;
import org.openapitools.client.model.ModifyStatusNotifyRequest;
import org.openapitools.client.model.RestNotifyInfoResponse;
import org.openapitools.client.model.StatusNotifyDetailInfoRequest;
import org.openapitools.client.model.StatusNotifyInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.notify.bean.RenotifyTypeMessage;
import com.clustercontrol.notify.dialog.bean.NotifyInfoInputData;
import com.clustercontrol.util.RestClientBeanUtil;

public class NotifyConvertUtil {
	private static Log m_log = LogFactory.getLog(NotifyConvertUtil.class);

	/**
	 * ステータス通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertStatusNotifyToRequest(NotifyInfoInputData info, AddStatusNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), AddStatusNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), AddStatusNotifyRequest.RenotifyTypeEnum.class));
			// ステータス通知用個別変換
			request.getNotifyStatusInfo().setStatusInvalidFlg(enumToEnum(info.getNotifyStatusInfo().getStatusInvalidFlg(), StatusNotifyDetailInfoRequest.StatusInvalidFlgEnum.class));
			request.getNotifyStatusInfo().setStatusUpdatePriority(enumToEnum(info.getNotifyStatusInfo().getStatusUpdatePriority(), StatusNotifyDetailInfoRequest.StatusUpdatePriorityEnum.class));
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * ステータス通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertStatusNotifyToRequest(NotifyInfoInputData info, ModifyStatusNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), ModifyStatusNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), ModifyStatusNotifyRequest.RenotifyTypeEnum.class));
			// ステータス通知用個別変換
			request.getNotifyStatusInfo().setStatusInvalidFlg(enumToEnum(info.getNotifyStatusInfo().getStatusInvalidFlg(), StatusNotifyDetailInfoRequest.StatusInvalidFlgEnum.class));
			request.getNotifyStatusInfo().setStatusUpdatePriority(enumToEnum(info.getNotifyStatusInfo().getStatusUpdatePriority(), StatusNotifyDetailInfoRequest.StatusUpdatePriorityEnum.class));
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * ステータス通知情報変換処理
	 *  レスポンス用のDTO → 画面入力情報
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertStatusNotifyToInputData(StatusNotifyInfoResponse dto, NotifyInfoInputData info) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(dto, info);
			// 個別変換
			info.setNotifyType(NotifyTypeUtil.enumToType(dto.getNotifyType(), StatusNotifyInfoResponse.NotifyTypeEnum.class));
			info.setRenotifyType(RenotifyTypeMessage.enumToType(dto.getRenotifyType(), StatusNotifyInfoResponse.RenotifyTypeEnum.class));
			// ステータス通知用個別変換
			info.setNotifyStatusInfo(dto.getNotifyStatusInfo());
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * イベント通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertEventNotifyToRequest(NotifyInfoInputData info, AddEventNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), AddEventNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), AddEventNotifyRequest.RenotifyTypeEnum.class));
			// イベント通知用個別変換
			request.getNotifyEventInfo().setInfoEventNormalState(enumToEnum(info.getNotifyEventInfo().getInfoEventNormalState(), EventNotifyDetailInfoRequest.InfoEventNormalStateEnum.class));
			request.getNotifyEventInfo().setWarnEventNormalState(enumToEnum(info.getNotifyEventInfo().getWarnEventNormalState(), EventNotifyDetailInfoRequest.WarnEventNormalStateEnum.class));
			request.getNotifyEventInfo().setCriticalEventNormalState(enumToEnum(info.getNotifyEventInfo().getCriticalEventNormalState(), EventNotifyDetailInfoRequest.CriticalEventNormalStateEnum.class));
			request.getNotifyEventInfo().setUnknownEventNormalState(enumToEnum(info.getNotifyEventInfo().getUnknownEventNormalState(), EventNotifyDetailInfoRequest.UnknownEventNormalStateEnum.class));
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * イベント通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertEventNotifyToRequest(NotifyInfoInputData info, ModifyEventNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), ModifyEventNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), ModifyEventNotifyRequest.RenotifyTypeEnum.class));
			// イベント通知用個別変換
			request.getNotifyEventInfo().setInfoEventNormalState(enumToEnum(info.getNotifyEventInfo().getInfoEventNormalState(), EventNotifyDetailInfoRequest.InfoEventNormalStateEnum.class));
			request.getNotifyEventInfo().setWarnEventNormalState(enumToEnum(info.getNotifyEventInfo().getWarnEventNormalState(), EventNotifyDetailInfoRequest.WarnEventNormalStateEnum.class));
			request.getNotifyEventInfo().setCriticalEventNormalState(enumToEnum(info.getNotifyEventInfo().getCriticalEventNormalState(), EventNotifyDetailInfoRequest.CriticalEventNormalStateEnum.class));
			request.getNotifyEventInfo().setUnknownEventNormalState(enumToEnum(info.getNotifyEventInfo().getUnknownEventNormalState(), EventNotifyDetailInfoRequest.UnknownEventNormalStateEnum.class));
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * イベント通知情報変換処理
	 *  レスポンス用のDTO → 画面入力情報
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertEventNotifyToInputData(EventNotifyInfoResponse dto, NotifyInfoInputData info) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(dto, info);
			// 個別変換
			info.setNotifyType(NotifyTypeUtil.enumToType(dto.getNotifyType(), EventNotifyInfoResponse.NotifyTypeEnum.class));
			info.setRenotifyType(RenotifyTypeMessage.enumToType(dto.getRenotifyType(), EventNotifyInfoResponse.RenotifyTypeEnum.class));
			// イベント通知用個別変換
			info.setNotifyEventInfo(dto.getNotifyEventInfo());
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * メール通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertMailNotifyToRequest(NotifyInfoInputData info, AddMailNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), AddMailNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), AddMailNotifyRequest.RenotifyTypeEnum.class));
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * メール通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertMailNotifyToRequest(NotifyInfoInputData info, ModifyMailNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), ModifyMailNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), ModifyMailNotifyRequest.RenotifyTypeEnum.class));
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * メール通知情報変換処理
	 *  レスポンス用のDTO → 画面入力情報
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertMailNotifyToInputData(MailNotifyInfoResponse dto, NotifyInfoInputData info) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(dto, info);
			// 個別変換
			info.setNotifyType(NotifyTypeUtil.enumToType(dto.getNotifyType(), MailNotifyInfoResponse.NotifyTypeEnum.class));
			info.setRenotifyType(RenotifyTypeMessage.enumToType(dto.getRenotifyType(), MailNotifyInfoResponse.RenotifyTypeEnum.class));
			// メール通知用個別変換
			info.setNotifyMailInfo(dto.getNotifyMailInfo());
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * ジョブ通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertJobNotifyToRequest(NotifyInfoInputData info, AddJobNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), AddJobNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), AddJobNotifyRequest.RenotifyTypeEnum.class));
			// ジョブ通知用個別変換
			request.getNotifyJobInfo().setNotifyJobType(enumToEnum(info.getNotifyJobInfo().getNotifyJobType(), JobNotifyDetailInfoRequest.NotifyJobTypeEnum.class));
			if (request.getNotifyJobInfo().getNotifyJobType() == JobNotifyDetailInfoRequest.NotifyJobTypeEnum.DIRECT) {
				request.getNotifyJobInfo().setJobExecFacilityFlg(enumToEnum(info.getNotifyJobInfo().getJobExecFacilityFlg(), JobNotifyDetailInfoRequest.JobExecFacilityFlgEnum.class));
				request.getNotifyJobInfo().setInfoJobFailurePriority(enumToEnum(info.getNotifyJobInfo().getInfoJobFailurePriority(), JobNotifyDetailInfoRequest.InfoJobFailurePriorityEnum.class));
				request.getNotifyJobInfo().setWarnJobFailurePriority(enumToEnum(info.getNotifyJobInfo().getWarnJobFailurePriority(), JobNotifyDetailInfoRequest.WarnJobFailurePriorityEnum.class));
				request.getNotifyJobInfo().setCriticalJobFailurePriority(enumToEnum(info.getNotifyJobInfo().getCriticalJobFailurePriority(), JobNotifyDetailInfoRequest.CriticalJobFailurePriorityEnum.class));
				request.getNotifyJobInfo().setUnknownJobFailurePriority(enumToEnum(info.getNotifyJobInfo().getUnknownJobFailurePriority(), JobNotifyDetailInfoRequest.UnknownJobFailurePriorityEnum.class));
			}
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * ジョブ通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertJobNotifyToRequest(NotifyInfoInputData info, ModifyJobNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), ModifyJobNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), ModifyJobNotifyRequest.RenotifyTypeEnum.class));
			// ジョブ通知用個別変換
			request.getNotifyJobInfo().setNotifyJobType(enumToEnum(info.getNotifyJobInfo().getNotifyJobType(), JobNotifyDetailInfoRequest.NotifyJobTypeEnum.class));
			if (request.getNotifyJobInfo().getNotifyJobType() == JobNotifyDetailInfoRequest.NotifyJobTypeEnum.DIRECT) {
				request.getNotifyJobInfo().setJobExecFacilityFlg(enumToEnum(info.getNotifyJobInfo().getJobExecFacilityFlg(), JobNotifyDetailInfoRequest.JobExecFacilityFlgEnum.class));
				request.getNotifyJobInfo().setInfoJobFailurePriority(enumToEnum(info.getNotifyJobInfo().getInfoJobFailurePriority(), JobNotifyDetailInfoRequest.InfoJobFailurePriorityEnum.class));
				request.getNotifyJobInfo().setWarnJobFailurePriority(enumToEnum(info.getNotifyJobInfo().getWarnJobFailurePriority(), JobNotifyDetailInfoRequest.WarnJobFailurePriorityEnum.class));
				request.getNotifyJobInfo().setCriticalJobFailurePriority(enumToEnum(info.getNotifyJobInfo().getCriticalJobFailurePriority(), JobNotifyDetailInfoRequest.CriticalJobFailurePriorityEnum.class));
				request.getNotifyJobInfo().setUnknownJobFailurePriority(enumToEnum(info.getNotifyJobInfo().getUnknownJobFailurePriority(), JobNotifyDetailInfoRequest.UnknownJobFailurePriorityEnum.class));
			}
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * ジョブ通知情報変換処理
	 *  レスポンス用のDTO → 画面入力情報
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertJobNotifyToInputData(JobNotifyInfoResponse dto, NotifyInfoInputData info) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(dto, info);
			// 個別変換
			info.setNotifyType(NotifyTypeUtil.enumToType(dto.getNotifyType(), JobNotifyInfoResponse.NotifyTypeEnum.class));
			info.setRenotifyType(RenotifyTypeMessage.enumToType(dto.getRenotifyType(), JobNotifyInfoResponse.RenotifyTypeEnum.class));
			// ジョブ通知用個別変換
			info.setNotifyJobInfo(dto.getNotifyJobInfo());
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * ログエスカレーション通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertLogEscalateNotifyToRequest(NotifyInfoInputData info, AddLogEscalateNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), AddLogEscalateNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), AddLogEscalateNotifyRequest.RenotifyTypeEnum.class));
			// ログエスカレーション通知用個別変換
			request.getNotifyLogEscalateInfo().setEscalateFacilityFlg(enumToEnum(info.getNotifyLogEscalateInfo().getEscalateFacilityFlg(), LogEscalateNotifyDetailInfoRequest.EscalateFacilityFlgEnum.class));
			request.getNotifyLogEscalateInfo().setInfoSyslogPriority(enumToEnum(info.getNotifyLogEscalateInfo().getInfoSyslogPriority(), LogEscalateNotifyDetailInfoRequest.InfoSyslogPriorityEnum.class));
			request.getNotifyLogEscalateInfo().setWarnSyslogPriority(enumToEnum(info.getNotifyLogEscalateInfo().getWarnSyslogPriority(), LogEscalateNotifyDetailInfoRequest.WarnSyslogPriorityEnum.class));
			request.getNotifyLogEscalateInfo().setCriticalSyslogPriority(enumToEnum(info.getNotifyLogEscalateInfo().getCriticalSyslogPriority(), LogEscalateNotifyDetailInfoRequest.CriticalSyslogPriorityEnum.class));
			request.getNotifyLogEscalateInfo().setUnknownSyslogPriority(enumToEnum(info.getNotifyLogEscalateInfo().getUnknownSyslogPriority(), LogEscalateNotifyDetailInfoRequest.UnknownSyslogPriorityEnum.class));
			request.getNotifyLogEscalateInfo().setInfoSyslogFacility(enumToEnum(info.getNotifyLogEscalateInfo().getInfoSyslogFacility(), LogEscalateNotifyDetailInfoRequest.InfoSyslogFacilityEnum.class));
			request.getNotifyLogEscalateInfo().setWarnSyslogFacility(enumToEnum(info.getNotifyLogEscalateInfo().getWarnSyslogFacility(), LogEscalateNotifyDetailInfoRequest.WarnSyslogFacilityEnum.class));
			request.getNotifyLogEscalateInfo().setCriticalSyslogFacility(enumToEnum(info.getNotifyLogEscalateInfo().getCriticalSyslogFacility(), LogEscalateNotifyDetailInfoRequest.CriticalSyslogFacilityEnum.class));
			request.getNotifyLogEscalateInfo().setUnknownSyslogFacility(enumToEnum(info.getNotifyLogEscalateInfo().getUnknownSyslogFacility(), LogEscalateNotifyDetailInfoRequest.UnknownSyslogFacilityEnum.class));
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * ログエスカレーション通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertLogEscalateNotifyToRequest(NotifyInfoInputData info, ModifyLogEscalateNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), ModifyLogEscalateNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), ModifyLogEscalateNotifyRequest.RenotifyTypeEnum.class));
			// ログエスカレーション通知用個別変換
			request.getNotifyLogEscalateInfo().setEscalateFacilityFlg(enumToEnum(info.getNotifyLogEscalateInfo().getEscalateFacilityFlg(), LogEscalateNotifyDetailInfoRequest.EscalateFacilityFlgEnum.class));
			request.getNotifyLogEscalateInfo().setInfoSyslogPriority(enumToEnum(info.getNotifyLogEscalateInfo().getInfoSyslogPriority(), LogEscalateNotifyDetailInfoRequest.InfoSyslogPriorityEnum.class));
			request.getNotifyLogEscalateInfo().setWarnSyslogPriority(enumToEnum(info.getNotifyLogEscalateInfo().getWarnSyslogPriority(), LogEscalateNotifyDetailInfoRequest.WarnSyslogPriorityEnum.class));
			request.getNotifyLogEscalateInfo().setCriticalSyslogPriority(enumToEnum(info.getNotifyLogEscalateInfo().getCriticalSyslogPriority(), LogEscalateNotifyDetailInfoRequest.CriticalSyslogPriorityEnum.class));
			request.getNotifyLogEscalateInfo().setUnknownSyslogPriority(enumToEnum(info.getNotifyLogEscalateInfo().getUnknownSyslogPriority(), LogEscalateNotifyDetailInfoRequest.UnknownSyslogPriorityEnum.class));
			request.getNotifyLogEscalateInfo().setInfoSyslogFacility(enumToEnum(info.getNotifyLogEscalateInfo().getInfoSyslogFacility(), LogEscalateNotifyDetailInfoRequest.InfoSyslogFacilityEnum.class));
			request.getNotifyLogEscalateInfo().setWarnSyslogFacility(enumToEnum(info.getNotifyLogEscalateInfo().getWarnSyslogFacility(), LogEscalateNotifyDetailInfoRequest.WarnSyslogFacilityEnum.class));
			request.getNotifyLogEscalateInfo().setCriticalSyslogFacility(enumToEnum(info.getNotifyLogEscalateInfo().getCriticalSyslogFacility(), LogEscalateNotifyDetailInfoRequest.CriticalSyslogFacilityEnum.class));
			request.getNotifyLogEscalateInfo().setUnknownSyslogFacility(enumToEnum(info.getNotifyLogEscalateInfo().getUnknownSyslogFacility(), LogEscalateNotifyDetailInfoRequest.UnknownSyslogFacilityEnum.class));
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * ログエスカレーション通知情報変換処理
	 *  レスポンス用のDTO → 画面入力情報
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertLogEscalateNotifyToInputData(LogEscalateNotifyInfoResponse dto, NotifyInfoInputData info) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(dto, info);
			// 個別変換
			info.setNotifyType(NotifyTypeUtil.enumToType(dto.getNotifyType(), LogEscalateNotifyInfoResponse.NotifyTypeEnum.class));
			info.setRenotifyType(RenotifyTypeMessage.enumToType(dto.getRenotifyType(), LogEscalateNotifyInfoResponse.RenotifyTypeEnum.class));
			// ログエスカレーション通知用個別変換
			info.setNotifyLogEscalateInfo(dto.getNotifyLogEscalateInfo());
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * コマンド通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertCommandNotifyToRequest(NotifyInfoInputData info, AddCommandNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), AddCommandNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), AddCommandNotifyRequest.RenotifyTypeEnum.class));
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * コマンド通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertCommandNotifyToRequest(NotifyInfoInputData info, ModifyCommandNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), ModifyCommandNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), ModifyCommandNotifyRequest.RenotifyTypeEnum.class));
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * コマンド通知情報変換処理
	 *  レスポンス用のDTO → 画面入力情報
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertCommandNotifyToInputData(CommandNotifyInfoResponse dto, NotifyInfoInputData info) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(dto, info);
			// 個別変換
			info.setNotifyType(NotifyTypeUtil.enumToType(dto.getNotifyType(), CommandNotifyInfoResponse.NotifyTypeEnum.class));
			info.setRenotifyType(RenotifyTypeMessage.enumToType(dto.getRenotifyType(), CommandNotifyInfoResponse.RenotifyTypeEnum.class));
			// コマンド通知用個別変換
			info.setNotifyCommandInfo(dto.getNotifyCommandInfo());
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * 環境構築通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertInfraNotifyToRequest(NotifyInfoInputData info, AddInfraNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), AddInfraNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), AddInfraNotifyRequest.RenotifyTypeEnum.class));
			// 環境構築通知用個別変換
			request.getNotifyInfraInfo().setInfraExecFacilityFlg(enumToEnum(info.getNotifyInfraInfo().getInfraExecFacilityFlg(), InfraNotifyDetailInfoRequest.InfraExecFacilityFlgEnum.class));
			request.getNotifyInfraInfo().setInfoInfraFailurePriority(enumToEnum(info.getNotifyInfraInfo().getInfoInfraFailurePriority(), InfraNotifyDetailInfoRequest.InfoInfraFailurePriorityEnum.class));
			request.getNotifyInfraInfo().setWarnInfraFailurePriority(enumToEnum(info.getNotifyInfraInfo().getWarnInfraFailurePriority(), InfraNotifyDetailInfoRequest.WarnInfraFailurePriorityEnum.class));
			request.getNotifyInfraInfo().setCriticalInfraFailurePriority(enumToEnum(info.getNotifyInfraInfo().getCriticalInfraFailurePriority(), InfraNotifyDetailInfoRequest.CriticalInfraFailurePriorityEnum.class));
			request.getNotifyInfraInfo().setUnknownInfraFailurePriority(enumToEnum(info.getNotifyInfraInfo().getUnknownInfraFailurePriority(), InfraNotifyDetailInfoRequest.UnknownInfraFailurePriorityEnum.class));
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * 環境構築通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertInfraNotifyToRequest(NotifyInfoInputData info, ModifyInfraNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), ModifyInfraNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), ModifyInfraNotifyRequest.RenotifyTypeEnum.class));
			// 環境構築通知用個別変換
			request.getNotifyInfraInfo().setInfraExecFacilityFlg(enumToEnum(info.getNotifyInfraInfo().getInfraExecFacilityFlg(), InfraNotifyDetailInfoRequest.InfraExecFacilityFlgEnum.class));
			request.getNotifyInfraInfo().setInfoInfraFailurePriority(enumToEnum(info.getNotifyInfraInfo().getInfoInfraFailurePriority(), InfraNotifyDetailInfoRequest.InfoInfraFailurePriorityEnum.class));
			request.getNotifyInfraInfo().setWarnInfraFailurePriority(enumToEnum(info.getNotifyInfraInfo().getWarnInfraFailurePriority(), InfraNotifyDetailInfoRequest.WarnInfraFailurePriorityEnum.class));
			request.getNotifyInfraInfo().setCriticalInfraFailurePriority(enumToEnum(info.getNotifyInfraInfo().getCriticalInfraFailurePriority(), InfraNotifyDetailInfoRequest.CriticalInfraFailurePriorityEnum.class));
			request.getNotifyInfraInfo().setUnknownInfraFailurePriority(enumToEnum(info.getNotifyInfraInfo().getUnknownInfraFailurePriority(), InfraNotifyDetailInfoRequest.UnknownInfraFailurePriorityEnum.class));
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * 環境構築通知情報変換処理
	 *  レスポンス用のDTO → 画面入力情報
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertInfraNotifyToInputData(InfraNotifyInfoResponse dto, NotifyInfoInputData info) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(dto, info);
			// 個別変換
			info.setNotifyType(NotifyTypeUtil.enumToType(dto.getNotifyType(), InfraNotifyInfoResponse.NotifyTypeEnum.class));
			info.setRenotifyType(RenotifyTypeMessage.enumToType(dto.getRenotifyType(), InfraNotifyInfoResponse.RenotifyTypeEnum.class));
			// 環境構築通知用個別変換
			info.setNotifyInfraInfo(dto.getNotifyInfraInfo());
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * REST通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertRestNotifyToRequest(NotifyInfoInputData info, AddRestNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), AddRestNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), AddRestNotifyRequest.RenotifyTypeEnum.class));
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * REST通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertRestNotifyToRequest(NotifyInfoInputData info, ModifyRestNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), ModifyRestNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), ModifyRestNotifyRequest.RenotifyTypeEnum.class));
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * REST通知情報変換処理
	 *  レスポンス用のDTO → 画面入力情報
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertRestNotifyToInputData(RestNotifyInfoResponse dto, NotifyInfoInputData info) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(dto, info);
			// 個別変換
			info.setNotifyType(NotifyTypeUtil.enumToType(dto.getNotifyType(), RestNotifyInfoResponse.NotifyTypeEnum.class));
			info.setRenotifyType(RenotifyTypeMessage.enumToType(dto.getRenotifyType(), RestNotifyInfoResponse.RenotifyTypeEnum.class));
			// REST通知用個別変換
			info.setNotifyRestInfo(dto.getNotifyRestInfo());
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}
	
	/**
	 * クラウド通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertCloudNotifyToRequest(NotifyInfoInputData info, AddCloudNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), AddCloudNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), AddCloudNotifyRequest.RenotifyTypeEnum.class));
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * クラウド通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertCloudNotifyToRequest(NotifyInfoInputData info, ModifyCloudNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), ModifyCloudNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), ModifyCloudNotifyRequest.RenotifyTypeEnum.class));
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * クラウド通知情報変換処理
	 *  レスポンス用のDTO → 画面入力情報
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertCloudNotifyToInputData(CloudNotifyInfoResponse dto, NotifyInfoInputData info) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(dto, info);
			// 個別変換
			info.setNotifyType(NotifyTypeUtil.enumToType(dto.getNotifyType(), CloudNotifyInfoResponse.NotifyTypeEnum.class));
			info.setRenotifyType(RenotifyTypeMessage.enumToType(dto.getRenotifyType(), CloudNotifyInfoResponse.RenotifyTypeEnum.class));
			// 環境構築通知用個別変換
			info.setNotifyCloudInfo(dto.getNotifyCloudInfo());
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}
	
	/**
	 * メッセージ通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertMessageNotifyToRequest(NotifyInfoInputData info, AddMessageNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), AddMessageNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), AddMessageNotifyRequest.RenotifyTypeEnum.class));
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * メッセージ通知情報変換処理
	 *  画面入力情報 → リクエスト用のDTO
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertMessageNotifyToRequest(NotifyInfoInputData info, ModifyMessageNotifyRequest request) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(info, request);
			// 個別変換
			request.setNotifyType(NotifyTypeUtil.typeToEnum(info.getNotifyType(), ModifyMessageNotifyRequest.NotifyTypeEnum.class));
			request.setRenotifyType(RenotifyTypeMessage.typeToEnum(info.getRenotifyType(), ModifyMessageNotifyRequest.RenotifyTypeEnum.class));
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * メッセージ通知情報変換処理
	 *  レスポンス用のDTO → 画面入力情報
	 * 
	 * @param info
	 * @param request
	 * @throws HinemosUnknown
	 */
	public static void convertMessageNotifyToInputData(MessageNotifyInfoResponse dto, NotifyInfoInputData info) throws HinemosUnknown {
		try {
			RestClientBeanUtil.convertBean(dto, info);
			// 個別変換
			info.setNotifyType(NotifyTypeUtil.enumToType(dto.getNotifyType(), MessageNotifyInfoResponse.NotifyTypeEnum.class));
			info.setRenotifyType(RenotifyTypeMessage.enumToType(dto.getRenotifyType(), MessageNotifyInfoResponse.RenotifyTypeEnum.class));
			// メッセージ通知用個別変換
			info.setNotifyMessageInfo(dto.getNotifyMessageInfo());
		} catch (HinemosUnknown e) {
			throw e;
		} catch (Exception e) {
			m_log.error(e.getMessage(),e);
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}

	/**
	 * Enumから同じNameを持った別のEnumに変換します。<BR>
	 * ※Enumの型は引数で指定できますが、列挙子のNameは統一されている必要があります。<BR>
	 * 
	 * @param value 変換元のEnum
	 * @param targetEnumType 変換先のEnumの型
	 * @return 変換先のEnum
	 */
	private static <T1 extends Enum<T1>, T2 extends Enum<T2>> T2 enumToEnum(T1 value, Class<T2> targetEnumType) {
		if (value == null) {
			return null;
		}
		return Enum.valueOf(targetEnumType, value.name());
	}
}
