/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.platform.conv;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.CloudNotifyDetailInfoResponse;
import org.openapitools.client.model.CloudNotifyDetailInfoResponse.PlatformTypeEnum;
import org.openapitools.client.model.CloudNotifyLinkInfoKeyValueObjectResponse;
import org.openapitools.client.model.CommandNotifyDetailInfoResponse;
import org.openapitools.client.model.CommandNotifyDetailInfoResponse.CommandSettingTypeEnum;
import org.openapitools.client.model.EventNotifyDetailInfoResponse;
import org.openapitools.client.model.EventNotifyDetailInfoResponse.CriticalEventNormalStateEnum;
import org.openapitools.client.model.EventNotifyDetailInfoResponse.InfoEventNormalStateEnum;
import org.openapitools.client.model.EventNotifyDetailInfoResponse.UnknownEventNormalStateEnum;
import org.openapitools.client.model.EventNotifyDetailInfoResponse.WarnEventNormalStateEnum;
import org.openapitools.client.model.InfraNotifyDetailInfoResponse;
import org.openapitools.client.model.InfraNotifyDetailInfoResponse.CriticalInfraFailurePriorityEnum;
import org.openapitools.client.model.InfraNotifyDetailInfoResponse.InfoInfraFailurePriorityEnum;
import org.openapitools.client.model.InfraNotifyDetailInfoResponse.InfraExecFacilityFlgEnum;
import org.openapitools.client.model.InfraNotifyDetailInfoResponse.UnknownInfraFailurePriorityEnum;
import org.openapitools.client.model.InfraNotifyDetailInfoResponse.WarnInfraFailurePriorityEnum;
import org.openapitools.client.model.JobNotifyDetailInfoResponse;
import org.openapitools.client.model.JobNotifyDetailInfoResponse.CriticalJobFailurePriorityEnum;
import org.openapitools.client.model.JobNotifyDetailInfoResponse.InfoJobFailurePriorityEnum;
import org.openapitools.client.model.JobNotifyDetailInfoResponse.JobExecFacilityFlgEnum;
import org.openapitools.client.model.JobNotifyDetailInfoResponse.NotifyJobTypeEnum;
import org.openapitools.client.model.JobNotifyDetailInfoResponse.UnknownJobFailurePriorityEnum;
import org.openapitools.client.model.JobNotifyDetailInfoResponse.WarnJobFailurePriorityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.CriticalSyslogFacilityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.CriticalSyslogPriorityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.EscalateFacilityFlgEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.InfoSyslogFacilityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.InfoSyslogPriorityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.UnknownSyslogFacilityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.UnknownSyslogPriorityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.WarnSyslogFacilityEnum;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse.WarnSyslogPriorityEnum;
import org.openapitools.client.model.MailNotifyDetailInfoResponse;
import org.openapitools.client.model.MessageNotifyDetailInfoResponse;
import org.openapitools.client.model.RestNotifyDetailInfoResponse;
import org.openapitools.client.model.StatusNotifyDetailInfoResponse;
import org.openapitools.client.model.StatusNotifyDetailInfoResponse.StatusInvalidFlgEnum;
import org.openapitools.client.model.StatusNotifyDetailInfoResponse.StatusUpdatePriorityEnum;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.dialog.bean.NotifyInfoInputData;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.platform.xml.KeyValueData;
import com.clustercontrol.utility.util.DateUtil;
import com.clustercontrol.utility.util.OpenApiEnumConverter;


/**
 * 通知設定情報をJavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 * 
 * @version 6.1.0
 * @since 1.0.0
 * 
 */
public class NotifyInfoConv {


	private static Log log = LogFactory.getLog(NotifyInfoConv.class);

	static private final String schemaType="H";
	static private final String schemaVersion="1";
	static private final String schemaRevision="3" ;


	/**
	 * XMLとツールの対応バージョンをチェック */
	static public int checkSchemaVersion(String type, String version ,String revision){

		return BaseConv.checkSchemaVersion(
				schemaType,schemaVersion,schemaRevision,
				type,version,revision);
	}

	/**
	 * スキーマのバージョンを返します。
	 * @return
	 */
	static public com.clustercontrol.utility.settings.platform.xml.SchemaInfo getSchemaVersion(){
	
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo schema =
			new com.clustercontrol.utility.settings.platform.xml.SchemaInfo();
		
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		
		return schema;
	}
	
	/**
	 * 通知定義に関して、XML BeanからHinemos Beanへ変換する。
	 * 
	 * @param notifyInfo 通知定義 XML Bean
	 * @return 通知定義 Hinemos Bean
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public static NotifyInfoInputData convXml2DtoNotify(com.clustercontrol.utility.settings.platform.xml.NotifyInfo notifyInfo) throws InvalidSetting, HinemosUnknown {
		//通知設定(1つ）の戻りインスタンスを生成します。
		NotifyInfoInputData ret = new NotifyInfoInputData();

		// 登録日時、更新日時に利用する日時（実行日時とする）
		long now = new Date().getTime();

		//通知ID
		if(notifyInfo.getNotifyId() != null && 
				!"".equals(notifyInfo.getNotifyId())){
			ret.setNotifyId(notifyInfo.getNotifyId());
		}else{
			log.warn(Messages.getString("SettingTools.EssentialValueInvalid") 
					+ "(NotifyId) : " + notifyInfo.getNotifyId().toString());
			return null;
		}

		//説明
		if(notifyInfo.getDescription() != null && !notifyInfo.getDescription().isEmpty()){
			ret.setDescription(notifyInfo.getDescription());
		}
		
		//オーナーロールID
		ret.setOwnerRoleId(notifyInfo.getOwnerRoleId());

		//通知タイプ(ステータス)
		ret.setNotifyType(notifyInfo.getNotifyType());
		
		ret.setInitialCount(notifyInfo.getInitialCount());
		
		//再通知種別
		ret.setRenotifyType(notifyInfo.getRenotifyType());
		
		//renotifyPeriodが未入力の場合はnullをセットする
		if (notifyInfo.getRenotifyPeriod() == 0 || notifyInfo.getRenotifyType() != 1) {
			ret.setRenotifyPeriod(null);
		} else {
			ret.setRenotifyPeriod(notifyInfo.getRenotifyPeriod());
		}
		
		//登録日時
		long regDate = -1;
		try {
			regDate = DateUtil.convDateString2Epoch(notifyInfo.getRegDate());
		} catch (ParseException e) {
			log.error(e);
		}
		if(regDate >= 0){
			ret.setRegDate(notifyInfo.getRegDate());
		}
		//登録ユーザ
		ret.setRegUser(notifyInfo.getRegUser());

		/*
		 * 更新日時、更新ユーザはマネージャで付加するので
		 * 意味はありません。
		 */

		//更新日時
		String nowString = DateUtil.convEpoch2DateString(now);
		ret.setUpdateDate(nowString);

		// 有効/無効
		ret.setValidFlg(notifyInfo.getValidFlg());

		ret.setNotFirstNotify(notifyInfo.getNotFirstNotify());

		ret.setCalendarId(notifyInfo.getCalendarId());
		
		switch ( ret.getNotifyType() ){

		case NotifyTypeConstant.TYPE_STATUS:
			ret.setNotifyStatusInfo(convXml2DtoStatus(notifyInfo.getNotifyStatusInfo()));
			break;
		case NotifyTypeConstant.TYPE_EVENT:
			ret.setNotifyEventInfo(convXml2DtoEvent(notifyInfo.getNotifyEventInfo()));
			break;
		case NotifyTypeConstant.TYPE_MAIL:
			ret.setNotifyMailInfo(convXml2DtoMail(notifyInfo.getNotifyMailInfo()));
			break;
		case NotifyTypeConstant.TYPE_JOB:
			ret.setNotifyJobInfo(convXml2DtoJob(notifyInfo.getNotifyJobInfo()));
			break;
		case NotifyTypeConstant.TYPE_LOG_ESCALATE:
			ret.setNotifyLogEscalateInfo(convXml2DtoLogEscalate(notifyInfo.getNotifyLogEscalateInfo()));
			break;
		case NotifyTypeConstant.TYPE_COMMAND:
			ret.setNotifyCommandInfo(convXml2DtoCommand(notifyInfo.getNotifyCommandInfo()));
			break;
		case NotifyTypeConstant.TYPE_INFRA:
			ret.setNotifyInfraInfo(convXml2DtoInfra(notifyInfo.getNotifyInfraInfo()));
			break;
		case NotifyTypeConstant.TYPE_REST:
			ret.setNotifyRestInfo(convXml2DtoRest(notifyInfo.getNotifyRestInfo()));
			break;
		case NotifyTypeConstant.TYPE_MESSAGE:
			ret.setNotifyMessageInfo(convXml2DtoMessage(notifyInfo.getNotifyMessageInfo()));
			break;
		case NotifyTypeConstant.TYPE_CLOUD:
			ret.setNotifyCloudInfo(convXml2DtoCloud(notifyInfo.getNotifyCloudInfo()));
			break;
			
		default:
			log.debug("Check notify type." + ret.getNotifyType());
			break;
		}

		return ret;
	}

	/**
	 * 通知詳細情報(ステータス)の情報をXMLからDTOのBeanに変換する。 
	 * 
	 * @param notifyID 通知ID
	 * @param notifyStatusInfo ステータス通知定義 XML Bean
	 * @return ステータス通知定義 Hinemos Bean(DTO)
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	private static StatusNotifyDetailInfoResponse convXml2DtoStatus(com.clustercontrol.utility.settings.platform.xml.NotifyStatusInfo notifyStatusInfo) throws InvalidSetting, HinemosUnknown {
		// 詳細情報、
		StatusNotifyDetailInfoResponse info = new StatusNotifyDetailInfoResponse();
		info.setInfoValidFlg(notifyStatusInfo.getInfoValidFlg());
		info.setWarnValidFlg(notifyStatusInfo.getWarnValidFlg());
		info.setCriticalValidFlg(notifyStatusInfo.getCriticalValidFlg());
		info.setUnknownValidFlg(notifyStatusInfo.getUnknownValidFlg());
		
		StatusInvalidFlgEnum statusInvalidFlgEnum = OpenApiEnumConverter.integerToEnum(notifyStatusInfo.getStatusInvalidFlg(), StatusInvalidFlgEnum.class);
		info.setStatusInvalidFlg(statusInvalidFlgEnum);
		
		StatusUpdatePriorityEnum notifyPriorityEnum = OpenApiEnumConverter.integerToEnum(notifyStatusInfo.getStatusUpdatePriority(), StatusUpdatePriorityEnum.class);
		info.setStatusUpdatePriority(notifyPriorityEnum);
		
		info.setStatusValidPeriod(notifyStatusInfo.getStatusValidPeriod());
		
		return info;
	}


	/**
	 * 通知詳細情報(イベント)の情報をXMLからDTOのBeanに変換する。 
	 * 
	 * @param notifyID 通知ID
	 * @param notifyEventInfo イベント通知定義 XML Bean
	 * @return イベント通知定義 Hinemos Bean(DTO)
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	private static EventNotifyDetailInfoResponse convXml2DtoEvent(com.clustercontrol.utility.settings.platform.xml.NotifyEventInfo notifyEventInfo) throws InvalidSetting, HinemosUnknown {
		//具象クラスとして、
		EventNotifyDetailInfoResponse info = new EventNotifyDetailInfoResponse();
		
		info.setInfoValidFlg(notifyEventInfo.getInfoValidFlg());
		info.setWarnValidFlg(notifyEventInfo.getWarnValidFlg());
		info.setCriticalValidFlg(notifyEventInfo.getCriticalValidFlg());
		info.setUnknownValidFlg(notifyEventInfo.getUnknownValidFlg());
		
		InfoEventNormalStateEnum  infoEventNormalStateEnum =  OpenApiEnumConverter.integerToEnum(notifyEventInfo.getInfoEventNormalState(), InfoEventNormalStateEnum.class);
		info.setInfoEventNormalState(infoEventNormalStateEnum);
		
		WarnEventNormalStateEnum warnEventNormalStateEnum = OpenApiEnumConverter.integerToEnum(notifyEventInfo.getWarnEventNormalState(), WarnEventNormalStateEnum.class);
		info.setWarnEventNormalState(warnEventNormalStateEnum);
		
		CriticalEventNormalStateEnum criticalEventNormalStateEnum = OpenApiEnumConverter.integerToEnum(notifyEventInfo.getCriticalEventNormalState(), CriticalEventNormalStateEnum.class);
		info.setCriticalEventNormalState(criticalEventNormalStateEnum);
		
		UnknownEventNormalStateEnum unknownEventNormalStateEnum = OpenApiEnumConverter.integerToEnum(notifyEventInfo.getUnknownEventNormalState(), UnknownEventNormalStateEnum.class);
		info.setUnknownEventNormalState(unknownEventNormalStateEnum);

		return info;
	}


	/**
	 * 通知詳細情報(メール)の情報をXMLからDTOのBeanに変換する。 
	 * 
	 * @param notifyID 通知ID
	 * @param notifyMailInfo メール通知定義 XML Bean
	 * @return メール通知定義 Hinemos Bean(DTO)
	 */
	private static MailNotifyDetailInfoResponse convXml2DtoMail(com.clustercontrol.utility.settings.platform.xml.NotifyMailInfo notifyMailInfo) {
		//具象クラスとして、
		MailNotifyDetailInfoResponse info = new MailNotifyDetailInfoResponse();

		info.setInfoValidFlg(notifyMailInfo.getInfoValidFlg());
		info.setWarnValidFlg(notifyMailInfo.getWarnValidFlg());
		info.setCriticalValidFlg(notifyMailInfo.getCriticalValidFlg());
		info.setUnknownValidFlg(notifyMailInfo.getUnknownValidFlg());

		info.setInfoMailAddress(notifyMailInfo.getInfoMailAddress());
		info.setWarnMailAddress(notifyMailInfo.getWarnMailAddress());
		info.setCriticalMailAddress(notifyMailInfo.getCriticalMailAddress());
		info.setUnknownMailAddress(notifyMailInfo.getUnknownMailAddress());

		if(notifyMailInfo.getMailTemplateId() != null &&
				!notifyMailInfo.getMailTemplateId().equals("")){
			info.setMailTemplateId(notifyMailInfo.getMailTemplateId());
		}
		return info;
	}


	/**
	 * 通知詳細情報(ジョブ)の情報をXMLからDTOのBeanに変換する。 
	 * 
	 * @param notifyID 通知ID
	 * @param notifyJobInfo ジョブ通知定義 XML Bean
	 * @return ジョブ通知定義 Hinemos Bean(DTO)
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	private static JobNotifyDetailInfoResponse convXml2DtoJob(com.clustercontrol.utility.settings.platform.xml.NotifyJobInfo notifyJobInfo) throws InvalidSetting, HinemosUnknown {
		//具象クラスとして、
		JobNotifyDetailInfoResponse info = new JobNotifyDetailInfoResponse();

		info.setInfoValidFlg(notifyJobInfo.getInfoValidFlg());
		info.setWarnValidFlg(notifyJobInfo.getWarnValidFlg());
		info.setCriticalValidFlg(notifyJobInfo.getCriticalValidFlg());
		info.setUnknownValidFlg(notifyJobInfo.getUnknownValidFlg());

		NotifyJobTypeEnum notifyJobTypeEnum =
				OpenApiEnumConverter.integerToEnum(notifyJobInfo.getNotifyJobType(),NotifyJobTypeEnum.class);
		info.setNotifyJobType(notifyJobTypeEnum);

		// 実行モードに応じた値のみ設定する
		if (NotifyJobTypeEnum.DIRECT.equals(notifyJobTypeEnum)) {
			// 直接実行

			// 呼出失敗時の重要度が入力されていない場合、ダイアログと同じデフォルト値で初期化する
			int defaultPriority = PriorityMessage.getNotifyJobFailurePriorityDefaultValue();

			int infoFailurePriority = defaultPriority;
			if (notifyJobInfo.hasInfoJobFailurePriority()) {
				infoFailurePriority = notifyJobInfo.getInfoJobFailurePriority();
			}
			InfoJobFailurePriorityEnum infoJobFailurePriorityEnum = OpenApiEnumConverter
					.integerToEnum(infoFailurePriority, InfoJobFailurePriorityEnum.class);
			info.setInfoJobFailurePriority(infoJobFailurePriorityEnum);

			int warnFailurePriority = defaultPriority;
			if (notifyJobInfo.hasWarnJobFailurePriority()) {
				warnFailurePriority = notifyJobInfo.getWarnJobFailurePriority();
			}
			WarnJobFailurePriorityEnum WarnJobFailurePriorityEnum = OpenApiEnumConverter
					.integerToEnum(warnFailurePriority, WarnJobFailurePriorityEnum.class);
			info.setWarnJobFailurePriority(WarnJobFailurePriorityEnum);

			int criticalFailurePriority = defaultPriority;
			if (notifyJobInfo.hasCriticalJobFailurePriority()) {
				criticalFailurePriority = notifyJobInfo.getCriticalJobFailurePriority();
			}
			CriticalJobFailurePriorityEnum criticalJobFailurePriorityEnum = OpenApiEnumConverter
					.integerToEnum(criticalFailurePriority, CriticalJobFailurePriorityEnum.class);
			info.setCriticalJobFailurePriority(criticalJobFailurePriorityEnum);

			int unknownFailurePriority = defaultPriority;
			if (notifyJobInfo.hasUnknownJobFailurePriority()) {
				unknownFailurePriority = notifyJobInfo.getUnknownJobFailurePriority();
			}
			UnknownJobFailurePriorityEnum unknownJobFailurePriorityEnum = OpenApiEnumConverter
					.integerToEnum(unknownFailurePriority, UnknownJobFailurePriorityEnum.class);
			info.setUnknownJobFailurePriority(unknownJobFailurePriorityEnum);

			if (isValidJob(notifyJobInfo.getInfoJobunitId(), notifyJobInfo.getInfoJobId())) {
				info.setInfoJobunitId(notifyJobInfo.getInfoJobunitId());
				info.setInfoJobId(notifyJobInfo.getInfoJobId());
			}

			if (isValidJob(notifyJobInfo.getWarnJobunitId(), notifyJobInfo.getWarnJobId())) {
				info.setWarnJobunitId(notifyJobInfo.getWarnJobunitId());
				info.setWarnJobId(notifyJobInfo.getWarnJobId());
			}

			if (isValidJob(notifyJobInfo.getCriticalJobunitId(), notifyJobInfo.getCriticalJobId())) {
				info.setCriticalJobunitId(notifyJobInfo.getCriticalJobunitId());
				info.setCriticalJobId(notifyJobInfo.getCriticalJobId());
			}

			if (isValidJob(notifyJobInfo.getUnknownJobunitId(), notifyJobInfo.getUnknownJobId())) {
				info.setUnknownJobunitId(notifyJobInfo.getUnknownJobunitId());
				info.setUnknownJobId(notifyJobInfo.getUnknownJobId());
			}

			if (notifyJobInfo.hasJobExecFacilityFlg()) {
				JobExecFacilityFlgEnum jobExecFacilityFlgEnum = OpenApiEnumConverter
						.integerToEnum(notifyJobInfo.getJobExecFacilityFlg(), JobExecFacilityFlgEnum.class);
				info.setJobExecFacilityFlg(jobExecFacilityFlgEnum);
				// ジョブ通知でのジョブ実行対象を、「イベントが発生したノード」に設定している場合、
				// 実行対象のファシリティIDをnullにセットする
				if (info.getJobExecFacilityFlg() == JobExecFacilityFlgEnum.GENERATION) {
					info.setJobExecFacilityId(null);
				} else {
					info.setJobExecFacilityId(notifyJobInfo.getJobExecFacility());
				}
			}

			info.setJobExecScope(notifyJobInfo.getJobExecScope());

		} else if (NotifyJobTypeEnum.JOB_LINK_SEND.equals(notifyJobTypeEnum)) {
			// ジョブ連携メッセージ送信

			if (notifyJobInfo.hasRetryFlg()) {
				info.setRetryFlg(notifyJobInfo.getRetryFlg());
			}
			if (notifyJobInfo.hasRetryCount()) {
				info.setRetryCount(notifyJobInfo.getRetryCount());
			}
			if (notifyJobInfo.hasSuccessInternalFlg()) {
				info.setSuccessInternalFlg(notifyJobInfo.getSuccessInternalFlg());
			}
			if (notifyJobInfo.hasFailureInternalFlg()) {
				info.setFailureInternalFlg(notifyJobInfo.getFailureInternalFlg());
			}
			info.setJoblinkSendSettingId(notifyJobInfo.getJoblinkSendSettingId());

		}

		return info;
	}
	/**
	 * 通知詳細情報(環境構築)の情報をXMLからDTOのBeanに変換する。 
	 * 
	 * @param notifyID 通知ID
	 * @param notifyInfraInfo 環境構築通知定義 XML Bean
	 * @return ジョブ通知定義 Hinemos Bean(DTO)
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	private static InfraNotifyDetailInfoResponse convXml2DtoInfra(com.clustercontrol.utility.settings.platform.xml.NotifyInfraInfo notifyInfraInfo) throws InvalidSetting, HinemosUnknown {
		//具象クラスとして、
		InfraNotifyDetailInfoResponse info = new InfraNotifyDetailInfoResponse();

		info.setInfoValidFlg(notifyInfraInfo.getInfoValidFlg());
		info.setWarnValidFlg(notifyInfraInfo.getWarnValidFlg());
		info.setCriticalValidFlg(notifyInfraInfo.getCriticalValidFlg());
		info.setUnknownValidFlg(notifyInfraInfo.getUnknownValidFlg());

		info.setInfoInfraId(notifyInfraInfo.getInfoInfraId());
		info.setWarnInfraId(notifyInfraInfo.getWarnInfraId());
		info.setCriticalInfraId(notifyInfraInfo.getCriticalInfraId());
		info.setUnknownInfraId(notifyInfraInfo.getUnknownInfraId());
		
		InfoInfraFailurePriorityEnum infoInfraFailurePriorityEnum =
				OpenApiEnumConverter.integerToEnum(notifyInfraInfo.getInfoInfraFailurePriority(),InfoInfraFailurePriorityEnum.class);
		info.setInfoInfraFailurePriority(infoInfraFailurePriorityEnum);
		
		WarnInfraFailurePriorityEnum warnInfraFailurePriorityEnum =
				OpenApiEnumConverter.integerToEnum(notifyInfraInfo.getWarnInfraFailurePriority(),WarnInfraFailurePriorityEnum.class);
		info.setWarnInfraFailurePriority(warnInfraFailurePriorityEnum);
		
		CriticalInfraFailurePriorityEnum criticalInfraFailurePriority =
				OpenApiEnumConverter.integerToEnum(notifyInfraInfo.getCriticalInfraFailurePriority(),CriticalInfraFailurePriorityEnum.class);
		info.setCriticalInfraFailurePriority(criticalInfraFailurePriority);
		
		UnknownInfraFailurePriorityEnum unknownInfraFailurePriorityEnum =
				OpenApiEnumConverter.integerToEnum(notifyInfraInfo.getUnknownInfraFailurePriority(),UnknownInfraFailurePriorityEnum.class);
		info.setUnknownInfraFailurePriority(unknownInfraFailurePriorityEnum);
		
		InfraExecFacilityFlgEnum infraExecFacilityFlgEnum =
				OpenApiEnumConverter.integerToEnum(notifyInfraInfo.getInfraExecFacilityFlg(),InfraExecFacilityFlgEnum.class);
		info.setInfraExecFacilityFlg(infraExecFacilityFlgEnum);

		// 環境構築通知でのジョブ実行対象を、「イベントが発生したノード」に設定している場合、
		// 実行対象のファシリティIDをnullにセットする
		if (info.getInfraExecFacilityFlg() == InfraExecFacilityFlgEnum.GENERATION) {
			info.setInfraExecFacilityId(null);
		} else {
			info.setInfraExecFacilityId(notifyInfraInfo.getInfraExecFacility());
		}

		info.setInfraExecScope(notifyInfraInfo.getInfraExecScope());

		return info;
	}



	/**
	 * 通知詳細情報(ログエスカレート)の情報をXMLからDTOのBeanに変換する。 
	 * 
	 * @param notifyID 通知ID
	 * @param notifyLogInfo ログエスカレート通知定義 XML Bean
	 * @return ログエスカレート通知定義 Hinemos Bean(DTO)
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	private static LogEscalateNotifyDetailInfoResponse convXml2DtoLogEscalate(com.clustercontrol.utility.settings.platform.xml.NotifyLogEscalateInfo notifyLogInfo) throws InvalidSetting, HinemosUnknown {
		//具象クラスとして、
		LogEscalateNotifyDetailInfoResponse info = new LogEscalateNotifyDetailInfoResponse();

		info.setInfoValidFlg(notifyLogInfo.getInfoValidFlg());
		info.setWarnValidFlg(notifyLogInfo.getWarnValidFlg());
		info.setCriticalValidFlg(notifyLogInfo.getCriticalValidFlg());
		info.setUnknownValidFlg(notifyLogInfo.getUnknownValidFlg());

		info.setEscalateFacilityId(notifyLogInfo.getEscalateFacility());
		
		EscalateFacilityFlgEnum escalateFacilityFlg =
				OpenApiEnumConverter.integerToEnum(notifyLogInfo.getEsaclateFacilityFlg(),EscalateFacilityFlgEnum.class);
		info.setEscalateFacilityFlg(escalateFacilityFlg);
		
		info.setEscalatePort(notifyLogInfo.getEscalatePort());
		info.setEscalateScope(notifyLogInfo.getEscalateScope());
		
		info.setInfoEscalateMessage(notifyLogInfo.getInfoEscalateMessage());
		
		InfoSyslogFacilityEnum infoSyslogFacilityEnum =
				OpenApiEnumConverter.integerToEnum(notifyLogInfo.getInfoSyslogFacility(),InfoSyslogFacilityEnum.class);
		info.setInfoSyslogFacility(infoSyslogFacilityEnum);
		
		InfoSyslogPriorityEnum infoSyslogPriorityEnum =
				OpenApiEnumConverter.integerToEnum(notifyLogInfo.getInfoSyslogPriority(),InfoSyslogPriorityEnum.class);
		info.setInfoSyslogPriority(infoSyslogPriorityEnum);
		
		info.setWarnEscalateMessage(notifyLogInfo.getWarnEscalateMessage());
		
		WarnSyslogFacilityEnum warnSyslogFacilityEnum =
				OpenApiEnumConverter.integerToEnum(notifyLogInfo.getWarnSyslogFacility(),WarnSyslogFacilityEnum.class);
		info.setWarnSyslogFacility(warnSyslogFacilityEnum);
		
		WarnSyslogPriorityEnum warnSyslogPriorityEnum = 
				OpenApiEnumConverter.integerToEnum(notifyLogInfo.getWarnSyslogPriority(),WarnSyslogPriorityEnum.class);
		info.setWarnSyslogPriority(warnSyslogPriorityEnum);
		
		info.setCriticalEscalateMessage(notifyLogInfo.getCriticalEscalateMessage());
		
		CriticalSyslogFacilityEnum criticalSyslogFacilityEnum =
				OpenApiEnumConverter.integerToEnum(notifyLogInfo.getCriticalSyslogFacility(),CriticalSyslogFacilityEnum.class);
		info.setCriticalSyslogFacility(criticalSyslogFacilityEnum);
		
		CriticalSyslogPriorityEnum criticalSyslogPriorityEnum =
				OpenApiEnumConverter.integerToEnum(notifyLogInfo.getCriticalSyslogPriority(),CriticalSyslogPriorityEnum.class);
		info.setCriticalSyslogPriority(criticalSyslogPriorityEnum);
		
		info.setUnknownEscalateMessage(notifyLogInfo.getUnknownEscalateMessage());
		
		UnknownSyslogFacilityEnum unknownSyslogFacilityEnum =
				OpenApiEnumConverter.integerToEnum(notifyLogInfo.getUnknownSyslogFacility(),UnknownSyslogFacilityEnum.class);
		info.setUnknownSyslogFacility(unknownSyslogFacilityEnum);
		
		UnknownSyslogPriorityEnum unknownSyslogPriorityEnum =
				OpenApiEnumConverter.integerToEnum(notifyLogInfo.getUnknownSyslogPriority(),UnknownSyslogPriorityEnum.class);
		info.setUnknownSyslogPriority(unknownSyslogPriorityEnum);
		
		return info;
	}

	
	/**
	 * 通知詳細情報(コマンド)の情報をXMLからDTOのBeanに変換する。 
	 * 
	 * @param notifyID 通知ID
	 * @param notifyCommandlList コマンド通知定義 XML Bean
	 * @return コマンド通知定義 Hinemos Bean(DTO)
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	private static CommandNotifyDetailInfoResponse convXml2DtoCommand(com.clustercontrol.utility.settings.platform.xml.NotifyCommandInfo notifyCommandInfo) throws InvalidSetting, HinemosUnknown {
		//com.clustercontrol.notify.bean.NotifyCommandInfoは中小クラスなので、
		//EJBから自動生成されるクラスを利用する。
		CommandNotifyDetailInfoResponse info = new CommandNotifyDetailInfoResponse();

		info.setInfoValidFlg(notifyCommandInfo.getInfoValidFlg());
		info.setWarnValidFlg(notifyCommandInfo.getWarnValidFlg());
		info.setCriticalValidFlg(notifyCommandInfo.getCriticalValidFlg());
		info.setUnknownValidFlg(notifyCommandInfo.getUnknownValidFlg());

//		info.setSetEnvironment(YesNoConstant.BOOLEAN_YES);
		info.setCommandTimeout(notifyCommandInfo.getCommandTimeout());
		
		info.setInfoCommand(notifyCommandInfo.getInfoCommand());
		info.setInfoEffectiveUser(notifyCommandInfo.getInfoEffectiveUser());

		info.setWarnCommand(notifyCommandInfo.getWarnCommand());
		info.setWarnEffectiveUser(notifyCommandInfo.getWarnEffectiveUser());

		info.setCriticalCommand(notifyCommandInfo.getCriticalCommand());
		info.setCriticalEffectiveUser(notifyCommandInfo.getCriticalEffectiveUser());

		info.setUnknownCommand(notifyCommandInfo.getUnknownCommand());
		info.setUnknownEffectiveUser(notifyCommandInfo.getUnknownEffectiveUser());

		if (notifyCommandInfo.getCommandSettingType() == 0) {
			// commandSettingType の設定値がない場合、0がセットされてくるのでnullに変換( CommandSettingTypeConstant に 0 は無いため問題なし）
			info.setCommandSettingType(null);
		}else{
			CommandSettingTypeEnum commandSettingTypeEnum = OpenApiEnumConverter.integerToEnum(notifyCommandInfo.getCommandSettingType(),CommandSettingTypeEnum.class);
			info.setCommandSettingType(commandSettingTypeEnum);
		}
		
		return info;
		
	}

	/**
	 * 通知詳細情報(REST)の情報をXMLからDTOのBeanに変換する。 
	 * 
	 * @param notifyID 通知ID
	 * @param notifyRestlList コマンド通知定義 XML Bean
	 * @return コマンド通知定義 Hinemos Bean(DTO)
	 */
	private static RestNotifyDetailInfoResponse convXml2DtoRest(com.clustercontrol.utility.settings.platform.xml.NotifyRestInfo notifyRestInfo) {
		//com.clustercontrol.notify.bean.NotifyCommandInfoは中小クラスなので、
		//EJBから自動生成されるクラスを利用する。
		RestNotifyDetailInfoResponse info = new RestNotifyDetailInfoResponse();

		info.setInfoValidFlg(notifyRestInfo.getInfoValidFlg());
		info.setWarnValidFlg(notifyRestInfo.getWarnValidFlg());
		info.setCriticalValidFlg(notifyRestInfo.getCriticalValidFlg());
		info.setUnknownValidFlg(notifyRestInfo.getUnknownValidFlg());

		info.setInfoRestAccessId(notifyRestInfo.getInfoRestAccessId());

		info.setWarnRestAccessId(notifyRestInfo.getWarnRestAccessId());

		info.setCriticalRestAccessId(notifyRestInfo.getCriticalRestAccessId());

		info.setUnknownRestAccessId(notifyRestInfo.getUnknownRestAccessId());
		
		return info;
	}
	
	/**
	 * 通知詳細情報(メッセージ)の情報をXMLからDTOのBeanに変換する。 
	 * 
	 * @param notifyID 通知ID
	 * @param notifyMessageList メッセージ通知定義 XML Bean
	 * @return メッセージ通知定義 Hinemos Bean(DTO)
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	private static MessageNotifyDetailInfoResponse convXml2DtoMessage(com.clustercontrol.utility.settings.platform.xml.NotifyMessageInfo notifyMessageInfo) throws InvalidSetting, HinemosUnknown {
		//com.clustercontrol.notify.bean.NotifyMessageInfoは中小クラスなので、
		//EJBから自動生成されるクラスを利用する。
		MessageNotifyDetailInfoResponse info = new MessageNotifyDetailInfoResponse();

		info.setInfoValidFlg(notifyMessageInfo.getInfoValidFlg());
		info.setWarnValidFlg(notifyMessageInfo.getWarnValidFlg());
		info.setCriticalValidFlg(notifyMessageInfo.getCriticalValidFlg());
		info.setUnknownValidFlg(notifyMessageInfo.getUnknownValidFlg());
		
		if (notifyMessageInfo.getInfoValidFlg()) {
			info.setInfoRulebaseId(notifyMessageInfo.getInfoRulebaseId());
		}

		if (notifyMessageInfo.getWarnValidFlg()) {
			info.setWarnRulebaseId(notifyMessageInfo.getWarnRulebaseId());
		}

		if (notifyMessageInfo.getCriticalValidFlg()) {
			info.setCriticalRulebaseId(notifyMessageInfo.getCriticalRulebaseId());
		}

		if (notifyMessageInfo.getUnknownValidFlg()){
			info.setUnknownRulebaseId(notifyMessageInfo.getUnknownRulebaseId());
		}
		
		return info;
	}
	
	/**
	 * 通知詳細情報(クラウド)の情報をXMLからDTOのBeanに変換する。 
	 * 
	 * @param notifyID 通知ID
	 * @param notifyCloudList クラウド通知定義 XML Bean
	 * @return クラウド通知定義 Hinemos Bean(DTO)
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	private static CloudNotifyDetailInfoResponse convXml2DtoCloud(com.clustercontrol.utility.settings.platform.xml.NotifyCloudInfo notifyCloudInfo) throws InvalidSetting, HinemosUnknown {
		//com.clustercontrol.notify.bean.NotifyCloudInfoは中小クラスなので、
		//EJBから自動生成されるクラスを利用する。
		CloudNotifyDetailInfoResponse info = new CloudNotifyDetailInfoResponse();

		info.setInfoValidFlg(notifyCloudInfo.getInfoValidFlg());
		info.setWarnValidFlg(notifyCloudInfo.getWarnValidFlg());
		info.setCriticalValidFlg(notifyCloudInfo.getCriticalValidFlg());
		info.setUnknownValidFlg(notifyCloudInfo.getUnknownValidFlg());
		
		info.setFacilityId(notifyCloudInfo.getFacilityId());
		info.setPlatformType(OpenApiEnumConverter.integerToEnum(notifyCloudInfo.getPlatformType(), PlatformTypeEnum.class));
		
		info.setInfoEventBus(notifyCloudInfo.getInfoEventBus());
		info.setInfoDetailType(notifyCloudInfo.getInfoDetailType());
		info.setInfoSource(notifyCloudInfo.getInfoSource());
		info.setInfoDataVersion(notifyCloudInfo.getInfoDataVersion());
		info.setInfoAccessKey(notifyCloudInfo.getInfoAccessKey());
		
		ArrayList<CloudNotifyLinkInfoKeyValueObjectResponse> infoList = new ArrayList<CloudNotifyLinkInfoKeyValueObjectResponse>();
		for(KeyValueData data : notifyCloudInfo.getKeyValueData()){
			CloudNotifyLinkInfoKeyValueObjectResponse dataRes = new CloudNotifyLinkInfoKeyValueObjectResponse();
			if(data.getPriority() == PriorityConstant.TYPE_INFO){
				dataRes.setName(data.getName());
				dataRes.setValue(data.getValue());
				infoList.add(dataRes);
			}
		}
		info.setInfoKeyValueDataList(infoList);

		info.setWarnEventBus(notifyCloudInfo.getWarnEventBus());
		info.setWarnDetailType(notifyCloudInfo.getWarnDetailType());
		info.setWarnSource(notifyCloudInfo.getWarnSource());
		info.setWarnDataVersion(notifyCloudInfo.getWarnDataVersion());
		info.setWarnAccessKey(notifyCloudInfo.getWarnAccessKey());
		
		ArrayList<CloudNotifyLinkInfoKeyValueObjectResponse> warnList = new ArrayList<CloudNotifyLinkInfoKeyValueObjectResponse>();
		for(KeyValueData data : notifyCloudInfo.getKeyValueData()){
			CloudNotifyLinkInfoKeyValueObjectResponse dataRes = new CloudNotifyLinkInfoKeyValueObjectResponse();
			if(data.getPriority() == PriorityConstant.TYPE_WARNING){
				dataRes.setName(data.getName());
				dataRes.setValue(data.getValue());
				warnList.add(dataRes);
			}
		}
		info.setWarnKeyValueDataList(warnList);

		info.setCritEventBus(notifyCloudInfo.getCritEventBus());
		info.setCritDetailType(notifyCloudInfo.getCritDetailType());
		info.setCritSource(notifyCloudInfo.getCritSource());
		info.setCritDataVersion(notifyCloudInfo.getCritDataVersion());
		info.setCritAccessKey(notifyCloudInfo.getCritAccessKey());
		
		ArrayList<CloudNotifyLinkInfoKeyValueObjectResponse> critList = new ArrayList<CloudNotifyLinkInfoKeyValueObjectResponse>();
		for(KeyValueData data : notifyCloudInfo.getKeyValueData()){
			CloudNotifyLinkInfoKeyValueObjectResponse dataRes = new CloudNotifyLinkInfoKeyValueObjectResponse();
			if(data.getPriority() == PriorityConstant.TYPE_CRITICAL){
				dataRes.setName(data.getName());
				dataRes.setValue(data.getValue());
				critList.add(dataRes);
			}
		}
		info.setCritKeyValueDataList(critList);

		info.setUnkEventBus(notifyCloudInfo.getUnkEventBus());
		info.setUnkDetailType(notifyCloudInfo.getUnkDetailType());
		info.setUnkSource(notifyCloudInfo.getUnkSource());
		info.setUnkDataVersion(notifyCloudInfo.getUnkDataVersion());
		info.setUnkAccessKey(notifyCloudInfo.getUnkAccessKey());
		
		ArrayList<CloudNotifyLinkInfoKeyValueObjectResponse> unkList = new ArrayList<CloudNotifyLinkInfoKeyValueObjectResponse>();
		for(KeyValueData data : notifyCloudInfo.getKeyValueData()){
			CloudNotifyLinkInfoKeyValueObjectResponse dataRes = new CloudNotifyLinkInfoKeyValueObjectResponse();
			if(data.getPriority() == PriorityConstant.TYPE_UNKNOWN){
				dataRes.setName(data.getName());
				dataRes.setValue(data.getValue());
				unkList.add(dataRes);
			}
		}
		info.setUnkKeyValueDataList(unkList);
		
		return info;
	}
	
	/**
	 * 通知定義の、Hinemos DTO BeanからXML Beanへ変換する。
	 * 
	 * @param notifyInfo 通知定義 Hinemos Bean
	 * @return 通知定義 XML Bean
	 */
	public static com.clustercontrol.utility.settings.platform.xml.NotifyInfo convDto2XmlNotify(NotifyInfoInputData notifyInfo) {
		
		//return のインスタンスを生成
		com.clustercontrol.utility.settings.platform.xml.NotifyInfo ret = 
			new com.clustercontrol.utility.settings.platform.xml.NotifyInfo();
		
		//通知ID
		ret.setNotifyId(notifyInfo.getNotifyId());
		
		//説明
		ret.setDescription(notifyInfo.getDescription());
		
		//オーナーロールID
		ret.setOwnerRoleId(notifyInfo.getOwnerRoleId());
		
		//通知タイプID
		ret.setNotifyType( notifyInfo.getNotifyType() );
		
		//通知までのカウント
		ret.setInitialCount(notifyInfo.getInitialCount());
		
		//再通知種別
		ret.setRenotifyType( notifyInfo.getRenotifyType() );
		
		//再通知抑制期間
		Integer renotifyPeriod = notifyInfo.getRenotifyPeriod();
		if (renotifyPeriod != null)
			ret.setRenotifyPeriod(renotifyPeriod.intValue());
		
		//登録日時
		String regDate = notifyInfo.getRegDate();
		if (regDate != null && !regDate.isEmpty()) {
			ret.setRegDate(regDate);
		}
		
		//登録ユーザ
		ret.setRegUser(notifyInfo.getRegUser());
		
		//更新日時
		String updateDate = notifyInfo.getUpdateDate();
		if (updateDate != null && !updateDate.isEmpty()) {
			ret.setUpdateDate(updateDate);
		}
		
		//更新ユーザ
		ret.setUpdateUser(notifyInfo.getUpdateUser());
		
		//有効・無効
		ret.setValidFlg(notifyInfo.isValidFlg());
		
		ret.setCalendarId(notifyInfo.getCalendarId());
		ret.setNotFirstNotify(notifyInfo.isNotFirstNotify());	
		
		switch (ret.getNotifyType()){
		
		case NotifyTypeConstant.TYPE_STATUS:
			ret.setNotifyStatusInfo(convDto2XmlStatus(notifyInfo.getNotifyId(),notifyInfo.getNotifyStatusInfo()));
			break;
		case NotifyTypeConstant.TYPE_EVENT:
			ret.setNotifyEventInfo(convDto2XmlEvent(notifyInfo.getNotifyId(),notifyInfo.getNotifyEventInfo()));
			break;	
		case NotifyTypeConstant.TYPE_MAIL:
			ret.setNotifyMailInfo(convDto2XmlMail(notifyInfo.getNotifyId(),notifyInfo.getNotifyMailInfo()));
			break;
		case NotifyTypeConstant.TYPE_JOB:
			ret.setNotifyJobInfo(convDto2XmlJob(notifyInfo.getNotifyId(),notifyInfo.getNotifyJobInfo()));
			break;	
		case NotifyTypeConstant.TYPE_LOG_ESCALATE:
			ret.setNotifyLogEscalateInfo(convDto2XmlLogEscalate(notifyInfo.getNotifyId(),notifyInfo.getNotifyLogEscalateInfo()));
			break;
		case NotifyTypeConstant.TYPE_COMMAND:
			ret.setNotifyCommandInfo(convDto2XmlCommand(notifyInfo.getNotifyId(),notifyInfo.getNotifyCommandInfo()));
			break;
		case NotifyTypeConstant.TYPE_INFRA:
			ret.setNotifyInfraInfo(convDto2XmlInfra(notifyInfo.getNotifyId(),notifyInfo.getNotifyInfraInfo()));
			break;			
		case NotifyTypeConstant.TYPE_REST:
			ret.setNotifyRestInfo(convDto2XmlRest(notifyInfo.getNotifyId(),notifyInfo.getNotifyRestInfo()));
			break;
		case NotifyTypeConstant.TYPE_MESSAGE:
			ret.setNotifyMessageInfo(convDto2XmlMessage(notifyInfo.getNotifyId(),notifyInfo.getNotifyMessageInfo()));
			break;
		case NotifyTypeConstant.TYPE_CLOUD:
			ret.setNotifyCloudInfo(convDto2XmlCloud(notifyInfo.getNotifyId(),notifyInfo.getNotifyCloudInfo()));
			break;
		default:
			log.debug("Check notify type." + ret.getNotifyType());
			break;
		}
		
		return ret;
	}

	/**
	 * 通知詳細情報(ステータス)の情報をHinemos DTOからDXMLのBeanに変換する。 
	 * 
	 * @param NotifyStatusInfo ステータス通知定義　Hinemos Bean(DTO)
	 * @return 環境通知定義　XML Bean
	*/
	private static com.clustercontrol.utility.settings.platform.xml.NotifyInfraInfo 
		convDto2XmlInfra(String notifyId, InfraNotifyDetailInfoResponse  notify) {

		com.clustercontrol.utility.settings.platform.xml.NotifyInfraInfo ret = 
				new com.clustercontrol.utility.settings.platform.xml.NotifyInfraInfo();
		
		ret.setNotifyId(notifyId);
		ret.setInfoValidFlg(notify.getInfoValidFlg());
		ret.setWarnValidFlg(notify.getWarnValidFlg());
		ret.setCriticalValidFlg(notify.getCriticalValidFlg());
		ret.setUnknownValidFlg(notify.getUnknownValidFlg());
		
		ret.setInfraExecFacility(notify.getInfraExecFacilityId());
		
		int infraExecFacilityFlgInt = OpenApiEnumConverter.enumToInteger(notify.getInfraExecFacilityFlg());
		ret.setInfraExecFacilityFlg(infraExecFacilityFlgInt);
		ret.setInfraExecScope(notify.getInfraExecScope());
		
		int infoInfraFailurePriorityInt = OpenApiEnumConverter.enumToInteger(notify.getInfoInfraFailurePriority());
		ret.setInfoInfraFailurePriority(infoInfraFailurePriorityInt);
		ret.setInfoInfraId(notify.getInfoInfraId());
		
		int warnInfraFailurePriorityInt = OpenApiEnumConverter.enumToInteger(notify.getWarnInfraFailurePriority());
		ret.setWarnInfraFailurePriority(warnInfraFailurePriorityInt);
		ret.setWarnInfraId(notify.getWarnInfraId());
		
		int CriticalInfraFailurePriorityInt = OpenApiEnumConverter.enumToInteger(notify.getCriticalInfraFailurePriority());
		ret.setCriticalInfraFailurePriority(CriticalInfraFailurePriorityInt);
		ret.setCriticalInfraId(notify.getCriticalInfraId());
		
		int unknownInfraFailurePriorityInt = OpenApiEnumConverter.enumToInteger(notify.getUnknownInfraFailurePriority());
		ret.setUnknownInfraFailurePriority(unknownInfraFailurePriorityInt);
		ret.setUnknownInfraId(notify.getUnknownInfraId());
		
		return ret;		
		
	}

	/**
	 * 通知詳細情報(ステータス)の情報をHinemos DTOからDXMLのBeanに変換する。 
	 * 
	 * @param NotifyStatusInfo ステータス通知定義　Hinemos Bean(DTO)
	 * @return ステータス通知定義　XML Bean
	 */
	private static com.clustercontrol.utility.settings.platform.xml.NotifyStatusInfo 
		convDto2XmlStatus(String notifyId, StatusNotifyDetailInfoResponse  notify) {
		
		com.clustercontrol.utility.settings.platform.xml.NotifyStatusInfo ret = 
			new com.clustercontrol.utility.settings.platform.xml.NotifyStatusInfo();
		
		ret.setNotifyId(notifyId);
		ret.setInfoValidFlg(notify.getInfoValidFlg());
		ret.setWarnValidFlg(notify.getWarnValidFlg());
		ret.setCriticalValidFlg(notify.getCriticalValidFlg());
		ret.setUnknownValidFlg(notify.getUnknownValidFlg());
		
		int statusInvalidFlgInt = OpenApiEnumConverter.enumToInteger(notify.getStatusInvalidFlg());
		ret.setStatusInvalidFlg(statusInvalidFlgInt);
		
		int StatusUpdatePriority = OpenApiEnumConverter.enumToInteger(notify.getStatusUpdatePriority());
		ret.setStatusUpdatePriority(StatusUpdatePriority);
		
		ret.setStatusValidPeriod(notify.getStatusValidPeriod());
		
		return ret;
	}
	
	/**
	 * 通知詳細情報(イベント)の情報をHinemos DTOからDXMLのBeanに変換する。 
	 * 
	 * @param NotifyEventInfo イベント通知定義　Hinemos Bean(DTO)
	 * @return イベント通知定義　XML Bean
	 */
	private static com.clustercontrol.utility.settings.platform.xml.NotifyEventInfo 
		convDto2XmlEvent(String notifyId, EventNotifyDetailInfoResponse  notify) {
		
		com.clustercontrol.utility.settings.platform.xml.NotifyEventInfo ret = 
			new com.clustercontrol.utility.settings.platform.xml.NotifyEventInfo();
		
		ret.setNotifyId(notifyId);
		ret.setInfoValidFlg(notify.getInfoValidFlg());
		ret.setWarnValidFlg(notify.getWarnValidFlg());
		ret.setCriticalValidFlg(notify.getCriticalValidFlg());
		ret.setUnknownValidFlg(notify.getUnknownValidFlg());
		
		int infoEventNormalStateInt = OpenApiEnumConverter.enumToInteger(notify.getInfoEventNormalState());
		ret.setInfoEventNormalState(infoEventNormalStateInt);
		
		int warnEventNormalStateInt = OpenApiEnumConverter.enumToInteger(notify.getWarnEventNormalState());
		ret.setWarnEventNormalState(warnEventNormalStateInt);
		
		int criticalEventNormalStateInt = OpenApiEnumConverter.enumToInteger(notify.getCriticalEventNormalState());
		ret.setCriticalEventNormalState(criticalEventNormalStateInt);
		
		int unknownEventNormalStateInt = OpenApiEnumConverter.enumToInteger(notify.getUnknownEventNormalState());
		ret.setUnknownEventNormalState(unknownEventNormalStateInt);

		return ret;
	}


	/**
	 * 通知詳細情報(メール)の情報をHinemos DTOからDXMLのBeanに変換する。 
	 * 
	 * @param NotifyMailInfo メール通知定義　Hinemos Bean(DTO)
	 * @return メール通知定義　XML Bean
	 */
	private static com.clustercontrol.utility.settings.platform.xml.NotifyMailInfo 
		convDto2XmlMail(String notifyId, MailNotifyDetailInfoResponse  notify) {
		
		com.clustercontrol.utility.settings.platform.xml.NotifyMailInfo ret = 
			new com.clustercontrol.utility.settings.platform.xml.NotifyMailInfo();
		
		ret.setNotifyId(notifyId);
		ret.setInfoValidFlg(notify.getInfoValidFlg());
		ret.setWarnValidFlg(notify.getWarnValidFlg());
		ret.setCriticalValidFlg(notify.getCriticalValidFlg());
		ret.setUnknownValidFlg(notify.getUnknownValidFlg());
		
		ret.setInfoMailAddress(notify.getInfoMailAddress());
		ret.setWarnMailAddress(notify.getWarnMailAddress());
		ret.setCriticalMailAddress(notify.getCriticalMailAddress());
		ret.setUnknownMailAddress(notify.getUnknownMailAddress());
		
		ret.setMailTemplateId(notify.getMailTemplateId());
		
		return ret;
	}
	/**
	 * 通知詳細情報(ジョブ)の情報をHinemos DTOからDXMLのBeanに変換する。 
	 * 
	 * @param NotifyJobInfo ジョブ通知定義　Hinemos Bean(DTO)
	 * @return ジョブ通知定義　XML Bean
	 */
	private static com.clustercontrol.utility.settings.platform.xml.NotifyJobInfo 
		convDto2XmlJob(String notifyId, JobNotifyDetailInfoResponse  notify) {
		
		com.clustercontrol.utility.settings.platform.xml.NotifyJobInfo ret = 
				new com.clustercontrol.utility.settings.platform.xml.NotifyJobInfo();
		
		ret.setNotifyId(notifyId);
		ret.setInfoValidFlg(notify.getInfoValidFlg());
		ret.setWarnValidFlg(notify.getWarnValidFlg());
		ret.setCriticalValidFlg(notify.getCriticalValidFlg());
		ret.setUnknownValidFlg(notify.getUnknownValidFlg());
		
		int notifyJobTypeEnum = OpenApiEnumConverter.enumToInteger(notify.getNotifyJobType());
		ret.setNotifyJobType(notifyJobTypeEnum);

		// 実行モードに応じた値のみ出力する
		if (NotifyJobTypeEnum.DIRECT.equals(notify.getNotifyJobType())) {
			// 直接実行

			ret.setJobExecFacility(notify.getJobExecFacilityId());

			if (notify.getJobExecFacilityFlg() != null) {
				int jobExecFacilityFlgInt = OpenApiEnumConverter.enumToInteger(notify.getJobExecFacilityFlg());
				ret.setJobExecFacilityFlg(jobExecFacilityFlgInt);
			}

			ret.setJobExecScope(notify.getJobExecScope());

			if (notify.getInfoJobFailurePriority() != null) {
				int infoJobFailurePriority = OpenApiEnumConverter.enumToInteger(notify.getInfoJobFailurePriority());
				ret.setInfoJobFailurePriority(infoJobFailurePriority);
			}

			ret.setInfoJobunitId(notify.getInfoJobunitId());
			ret.setInfoJobId(notify.getInfoJobId());

			if (notify.getWarnJobFailurePriority() != null) {
				int warnJobFailurePriority = OpenApiEnumConverter.enumToInteger(notify.getWarnJobFailurePriority());
				ret.setWarnJobFailurePriority(warnJobFailurePriority);
			}

			ret.setWarnJobunitId(notify.getWarnJobunitId());
			ret.setWarnJobId(notify.getWarnJobId());

			if (notify.getCriticalJobFailurePriority() != null) {
				int criticalJobFailurePriority = OpenApiEnumConverter
						.enumToInteger(notify.getCriticalJobFailurePriority());
				ret.setCriticalJobFailurePriority(criticalJobFailurePriority);
			}

			ret.setCriticalJobunitId(notify.getCriticalJobunitId());
			ret.setCriticalJobId(notify.getCriticalJobId());

			if (notify.getUnknownJobFailurePriority() != null) {
				int unknownJobFailurePriority = OpenApiEnumConverter
						.enumToInteger(notify.getUnknownJobFailurePriority());
				ret.setUnknownJobFailurePriority(unknownJobFailurePriority);
			}

			ret.setUnknownJobunitId(notify.getUnknownJobunitId());
			ret.setUnknownJobId(notify.getUnknownJobId());

		} else if (NotifyJobTypeEnum.JOB_LINK_SEND.equals(notify.getNotifyJobType())) {
			// ジョブ連携メッセージ送信

			if (notify.getRetryFlg() != null) {
				ret.setRetryFlg(notify.getRetryFlg());
			}
			if (notify.getRetryCount() != null) {
				ret.setRetryCount(notify.getRetryCount());
			}
			if (notify.getSuccessInternalFlg() != null) {
				ret.setSuccessInternalFlg(notify.getSuccessInternalFlg());
			}
			if (notify.getFailureInternalFlg() != null) {
				ret.setFailureInternalFlg(notify.getFailureInternalFlg());
			}
			ret.setJoblinkSendSettingId(notify.getJoblinkSendSettingId());

		}

		return ret;
	}
	/**
	 * 通知詳細情報(ログエスカレーション)の情報をHinemos DTOからDXMLのBeanに変換する。 
	 * 
	 * @param NotifyLogInfo ログエスカレーション通知定義　Hinemos Bean(DTO)
	 * @return ログエスカレーション通知定義　XML Bean
	 */
	private static com.clustercontrol.utility.settings.platform.xml.NotifyLogEscalateInfo 
		convDto2XmlLogEscalate(String notifyId, LogEscalateNotifyDetailInfoResponse  notify) {
		
		com.clustercontrol.utility.settings.platform.xml.NotifyLogEscalateInfo ret = 
			new com.clustercontrol.utility.settings.platform.xml.NotifyLogEscalateInfo();
		
		ret.setNotifyId(notifyId);
		ret.setInfoValidFlg(notify.getInfoValidFlg());
		ret.setWarnValidFlg(notify.getWarnValidFlg());
		ret.setCriticalValidFlg(notify.getCriticalValidFlg());
		ret.setUnknownValidFlg(notify.getUnknownValidFlg());
		
		int esaclateFacilityFlgInt = OpenApiEnumConverter.enumToInteger(notify.getEscalateFacilityFlg());
		ret.setEsaclateFacilityFlg(esaclateFacilityFlgInt);
		ret.setEscalateFacility(notify.getEscalateFacilityId());
		ret.setEscalatePort(notify.getEscalatePort());
		ret.setEscalateScope(notify.getEscalateScope());
		
		ret.setInfoEscalateMessage(notify.getInfoEscalateMessage());
		
		int infoSyslogPriorityInt = OpenApiEnumConverter.enumToInteger(notify.getInfoSyslogPriority());
		ret.setInfoSyslogPriority(infoSyslogPriorityInt);
		
		int infoSyslogFacilityInt = OpenApiEnumConverter.enumToInteger(notify.getInfoSyslogFacility());
		ret.setInfoSyslogFacility(infoSyslogFacilityInt);
		
		ret.setWarnEscalateMessage(notify.getWarnEscalateMessage());
		
		int warnSyslogPriorityInt = OpenApiEnumConverter.enumToInteger(notify.getWarnSyslogPriority());
		ret.setWarnSyslogPriority(warnSyslogPriorityInt);
		
		int warnSyslogFacilityInt = OpenApiEnumConverter.enumToInteger(notify.getWarnSyslogFacility());
		ret.setWarnSyslogFacility(warnSyslogFacilityInt);
		
		ret.setCriticalEscalateMessage(notify.getCriticalEscalateMessage());
		
		int criticalSyslogPriorityInt = OpenApiEnumConverter.enumToInteger(notify.getCriticalSyslogPriority());
		ret.setCriticalSyslogPriority(criticalSyslogPriorityInt);
		
		int criticalSyslogFacilityInt = OpenApiEnumConverter.enumToInteger(notify.getCriticalSyslogFacility());
		ret.setCriticalSyslogFacility(criticalSyslogFacilityInt);
		
		ret.setUnknownEscalateMessage(notify.getUnknownEscalateMessage());
		
		int unknownSyslogPriorityInt = OpenApiEnumConverter.enumToInteger(notify.getUnknownSyslogPriority());
		ret.setUnknownSyslogPriority(unknownSyslogPriorityInt);
		
		int unknownSyslogFacilityInt = OpenApiEnumConverter.enumToInteger(notify.getUnknownSyslogFacility());
		ret.setUnknownSyslogFacility(unknownSyslogFacilityInt);
		
		return ret;
	}
	
	/**
	 * 通知詳細情報(コマンド)の情報をHinemos DTOからDXMLのBeanに変換する。 
	 * 
	 * @param NotifyCommandInfo コマンド通知定義　Hinemos Bean(DTO)
	 * @return コマンド通知定義　XML Bean
	 */
	private static com.clustercontrol.utility.settings.platform.xml.NotifyCommandInfo 
		convDto2XmlCommand(String notifyId, CommandNotifyDetailInfoResponse  notify) {
		
		com.clustercontrol.utility.settings.platform.xml.NotifyCommandInfo ret = 
			new com.clustercontrol.utility.settings.platform.xml.NotifyCommandInfo();
		
		ret.setNotifyId(notifyId);
		
		ret.setInfoValidFlg(notify.getInfoValidFlg());
		ret.setWarnValidFlg(notify.getWarnValidFlg());
		ret.setCriticalValidFlg(notify.getCriticalValidFlg());
		ret.setUnknownValidFlg(notify.getUnknownValidFlg());
		
		ret.setCommandTimeout(notify.getCommandTimeout());
		ret.setInfoCommand(notify.getInfoCommand());
		ret.setInfoEffectiveUser(notify.getInfoEffectiveUser());
		
		ret.setWarnCommand(notify.getWarnCommand());
		ret.setWarnEffectiveUser(notify.getWarnEffectiveUser());
		
		ret.setCriticalCommand(notify.getCriticalCommand());
		ret.setCriticalEffectiveUser(notify.getCriticalEffectiveUser());
		
		ret.setUnknownCommand(notify.getUnknownCommand());
		ret.setUnknownEffectiveUser(notify.getUnknownEffectiveUser());
		
		int commandSettingTypeInt = OpenApiEnumConverter.enumToInteger(notify.getCommandSettingType());
		ret.setCommandSettingType(commandSettingTypeInt);
		
		return ret;
	}
	
	/**
	 * 通知詳細情報(REST)の情報をHinemos DTOからDXMLのBeanに変換する。 
	 * 
	 * @param NotifyRestInfo REST通知定義　Hinemos Bean(DTO)
	 * @return REST通知定義　XML Bean
	 */
	private static com.clustercontrol.utility.settings.platform.xml.NotifyRestInfo 
		convDto2XmlRest(String notifyId, RestNotifyDetailInfoResponse  notify) {
		
		com.clustercontrol.utility.settings.platform.xml.NotifyRestInfo ret = 
			new com.clustercontrol.utility.settings.platform.xml.NotifyRestInfo();
		
		ret.setNotifyId(notifyId);
		
		ret.setInfoValidFlg(notify.getInfoValidFlg());
		ret.setWarnValidFlg(notify.getWarnValidFlg());
		ret.setCriticalValidFlg(notify.getCriticalValidFlg());
		ret.setUnknownValidFlg(notify.getUnknownValidFlg());
		
		ret.setInfoRestAccessId(notify.getInfoRestAccessId());
		
		ret.setWarnRestAccessId(notify.getWarnRestAccessId());
		
		ret.setCriticalRestAccessId(notify.getCriticalRestAccessId());
		
		ret.setUnknownRestAccessId(notify.getUnknownRestAccessId());
		
		return ret;
	}

	/**
	 * 通知詳細情報(メッセージ)の情報をHinemos DTOからDXMLのBeanに変換する。 
	 * 
	 * @param NotifyMessageInfo メッセージ通知定義　Hinemos Bean(DTO)
	 * @return メッセージ通知定義　XML Bean
	 */
	private static com.clustercontrol.utility.settings.platform.xml.NotifyMessageInfo 
		convDto2XmlMessage(String notifyId, MessageNotifyDetailInfoResponse  notify) {
		
		com.clustercontrol.utility.settings.platform.xml.NotifyMessageInfo ret = 
			new com.clustercontrol.utility.settings.platform.xml.NotifyMessageInfo();
		
		ret.setNotifyId(notifyId);
		
		ret.setInfoValidFlg(notify.getInfoValidFlg());
		ret.setWarnValidFlg(notify.getWarnValidFlg());
		ret.setCriticalValidFlg(notify.getCriticalValidFlg());
		ret.setUnknownValidFlg(notify.getUnknownValidFlg());
		
		ret.setInfoRulebaseId(notify.getInfoRulebaseId());
		ret.setWarnRulebaseId(notify.getWarnRulebaseId());
		ret.setCriticalRulebaseId(notify.getCriticalRulebaseId());
		ret.setUnknownRulebaseId(notify.getUnknownRulebaseId());
		
		return ret;
	}
	
	/**
	 * 通知詳細情報(クラウド)の情報をHinemos DTOからDXMLのBeanに変換する。 
	 * 
	 * @param NotifyCloudInfo クラウド通知定義　Hinemos Bean(DTO)
	 * @return クラウド通知定義　XML Bean
	 */
	private static com.clustercontrol.utility.settings.platform.xml.NotifyCloudInfo 
		convDto2XmlCloud(String notifyId, CloudNotifyDetailInfoResponse  notify) {
		
		com.clustercontrol.utility.settings.platform.xml.NotifyCloudInfo ret = 
			new com.clustercontrol.utility.settings.platform.xml.NotifyCloudInfo();
		
		ret.setNotifyId(notifyId);
		
		ret.setInfoValidFlg(notify.getInfoValidFlg());
		ret.setWarnValidFlg(notify.getWarnValidFlg());
		ret.setCriticalValidFlg(notify.getCriticalValidFlg());
		ret.setUnknownValidFlg(notify.getUnknownValidFlg());
		
		ret.setFacilityId(notify.getFacilityId());
		ret.setTextScope(notify.getTextScope());
		ret.setPlatformType(OpenApiEnumConverter.enumToInteger(notify.getPlatformType()));
		
		ret.setInfoEventBus(notify.getInfoEventBus());
		ret.setInfoDetailType(notify.getInfoDetailType());
		ret.setInfoSource(notify.getInfoSource());
		ret.setInfoDataVersion(notify.getInfoDataVersion());
		ret.setInfoAccessKey(notify.getInfoAccessKey());
		
		ArrayList<KeyValueData> keyValueDataList = new ArrayList<KeyValueData>();
		for(CloudNotifyLinkInfoKeyValueObjectResponse res : notify.getInfoKeyValueDataList()){
			KeyValueData data = new KeyValueData();
			data.setPriority(PriorityConstant.TYPE_INFO);
			data.setName(res.getName());
			data.setValue(res.getValue());
			keyValueDataList.add(data);
		}

		ret.setWarnEventBus(notify.getWarnEventBus());
		ret.setWarnDetailType(notify.getWarnDetailType());
		ret.setWarnSource(notify.getWarnSource());
		ret.setWarnDataVersion(notify.getWarnDataVersion());
		ret.setWarnAccessKey(notify.getWarnAccessKey());
		
		for(CloudNotifyLinkInfoKeyValueObjectResponse res : notify.getWarnKeyValueDataList()){
			KeyValueData data = new KeyValueData();
			data.setPriority(PriorityConstant.TYPE_WARNING);
			data.setName(res.getName());
			data.setValue(res.getValue());
			keyValueDataList.add(data);
		}
		

		ret.setCritEventBus(notify.getCritEventBus());
		ret.setCritDetailType(notify.getCritDetailType());
		ret.setCritSource(notify.getCritSource());
		ret.setCritDataVersion(notify.getCritDataVersion());
		ret.setCritAccessKey(notify.getCritAccessKey());
		
		for(CloudNotifyLinkInfoKeyValueObjectResponse res : notify.getCritKeyValueDataList()){
			KeyValueData data = new KeyValueData();
			data.setPriority(PriorityConstant.TYPE_CRITICAL);
			data.setName(res.getName());
			data.setValue(res.getValue());
			keyValueDataList.add(data);
		}

		ret.setUnkEventBus(notify.getUnkEventBus());
		ret.setUnkDetailType(notify.getUnkDetailType());
		ret.setUnkSource(notify.getUnkSource());
		ret.setUnkDataVersion(notify.getUnkDataVersion());
		ret.setUnkAccessKey(notify.getUnkAccessKey());
		
		for(CloudNotifyLinkInfoKeyValueObjectResponse res : notify.getUnkKeyValueDataList()){
			KeyValueData data = new KeyValueData();
			data.setPriority(PriorityConstant.TYPE_UNKNOWN);
			data.setName(res.getName());
			data.setValue(res.getValue());
			keyValueDataList.add(data);
		}
		
		ret.setKeyValueData(keyValueDataList.toArray(new KeyValueData[keyValueDataList.size()]));
		
		return ret;
	}
	
	private static boolean isValidJob(String jobUnitId, String jobId){
		if(jobUnitId != null && jobId != null){
			return true;
		}else{
			log.debug("isValidJob() " 
					+ "(JobunitID) : "
					+ jobUnitId
					+ ", (JobID) : "
					+ jobId);
			return false;
		}
	}
}
