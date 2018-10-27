/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.platform.conv;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.notify.bean.ExecFacilityConstant;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.constant.YesNoConstant;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.util.DateUtil;
import com.clustercontrol.ws.notify.NotifyCommandInfo;
import com.clustercontrol.ws.notify.NotifyEventInfo;
import com.clustercontrol.ws.notify.NotifyInfo;
import com.clustercontrol.ws.notify.NotifyInfraInfo;
import com.clustercontrol.ws.notify.NotifyJobInfo;
import com.clustercontrol.ws.notify.NotifyLogEscalateInfo;
import com.clustercontrol.ws.notify.NotifyMailInfo;
import com.clustercontrol.ws.notify.NotifyStatusInfo;

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
	static private final String schemaRevision="1" ;


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
	 */
	public static NotifyInfo convXml2DtoNotify(com.clustercontrol.utility.settings.platform.xml.NotifyInfo notifyInfo) {
		
		//通知設定(1つ）の戻りインスタンスを生成します。
		NotifyInfo ret = new NotifyInfo();

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

		//通知タイプ(ステータス、イベント、メール、ジョブ、ログ、コマンド)
		ret.setNotifyType(notifyInfo.getNotifyType());
		
		ret.setInitialCount(notifyInfo.getInitialCount());
		ret.setRenotifyType(notifyInfo.getRenotifyType());
		
		//renotifyPeriodが未入力の場合はnullをセットする
		if (notifyInfo.getRenotifyPeriod() == 0) {
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
			ret.setRegDate(regDate);
		}
		//登録ユーザ
		ret.setRegUser(notifyInfo.getRegUser());

		/*
		 * 更新日時、更新ユーザはマネージャで付加するので
		 * 意味はありません。
		 */

		//更新日時
		ret.setUpdateDate(now);

		// 有効/無効
		ret.setValidFlg(notifyInfo.getValidFlg());

		ret.setNotFirstNotify(notifyInfo.getNotFirstNotify());

		ret.setCalendarId(notifyInfo.getCalendarId());
		
		switch (ret.getNotifyType()){

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
	 */
	private static NotifyStatusInfo convXml2DtoStatus(com.clustercontrol.utility.settings.platform.xml.NotifyStatusInfo notifyStatusInfo) {
		//具象クラスとして、
		NotifyStatusInfo info = new NotifyStatusInfo();
		info.setInfoValidFlg(notifyStatusInfo.getInfoValidFlg());
		info.setWarnValidFlg(notifyStatusInfo.getWarnValidFlg());
		info.setCriticalValidFlg(notifyStatusInfo.getCriticalValidFlg());
		info.setUnknownValidFlg(notifyStatusInfo.getUnknownValidFlg());
		
		info.setStatusInvalidFlg(notifyStatusInfo.getStatusInvalidFlg());
		info.setStatusUpdatePriority(notifyStatusInfo.getStatusUpdatePriority());
		info.setStatusValidPeriod(notifyStatusInfo.getStatusValidPeriod());

		return info;
	}


	/**
	 * 通知詳細情報(イベント)の情報をXMLからDTOのBeanに変換する。 
	 * 
	 * @param notifyID 通知ID
	 * @param notifyEventInfo イベント通知定義 XML Bean
	 * @return イベント通知定義 Hinemos Bean(DTO)
	 */
	private static NotifyEventInfo convXml2DtoEvent(com.clustercontrol.utility.settings.platform.xml.NotifyEventInfo notifyEventInfo) {
		//具象クラスとして、
		NotifyEventInfo info = new NotifyEventInfo();

		info.setInfoValidFlg(notifyEventInfo.getInfoValidFlg());
		info.setWarnValidFlg(notifyEventInfo.getWarnValidFlg());
		info.setCriticalValidFlg(notifyEventInfo.getCriticalValidFlg());
		info.setUnknownValidFlg(notifyEventInfo.getUnknownValidFlg());

		info.setInfoEventNormalState(notifyEventInfo.getInfoEventNormalState());
		info.setWarnEventNormalState(notifyEventInfo.getWarnEventNormalState());
		info.setCriticalEventNormalState(notifyEventInfo.getCriticalEventNormalState());
		info.setUnknownEventNormalState(notifyEventInfo.getUnknownEventNormalState());

		return info;
	}


	/**
	 * 通知詳細情報(メール)の情報をXMLからDTOのBeanに変換する。 
	 * 
	 * @param notifyID 通知ID
	 * @param notifyMailInfo メール通知定義 XML Bean
	 * @return メール通知定義 Hinemos Bean(DTO)
	 */
	private static NotifyMailInfo convXml2DtoMail(com.clustercontrol.utility.settings.platform.xml.NotifyMailInfo notifyMailInfo) {
		//具象クラスとして、
		NotifyMailInfo info = new NotifyMailInfo();

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
	 */
	private static NotifyJobInfo convXml2DtoJob(com.clustercontrol.utility.settings.platform.xml.NotifyJobInfo notifyJobInfo) {
		//具象クラスとして、
		NotifyJobInfo info = new NotifyJobInfo();

		info.setInfoValidFlg(notifyJobInfo.getInfoValidFlg());
		info.setWarnValidFlg(notifyJobInfo.getWarnValidFlg());
		info.setCriticalValidFlg(notifyJobInfo.getCriticalValidFlg());
		info.setUnknownValidFlg(notifyJobInfo.getUnknownValidFlg());

		info.setInfoJobFailurePriority(notifyJobInfo.getInfoJobFailurePriority());
		info.setWarnJobFailurePriority(notifyJobInfo.getWarnJobFailurePriority());
		info.setCriticalJobFailurePriority(notifyJobInfo.getCriticalJobFailurePriority());
		info.setUnknownJobFailurePriority(notifyJobInfo.getUnknownJobFailurePriority());

		if(isValidJob(notifyJobInfo.getInfoJobunitId(), notifyJobInfo.getInfoJobId())){
			info.setInfoJobunitId(notifyJobInfo.getInfoJobunitId());
			info.setInfoJobId(notifyJobInfo.getInfoJobId());
		}

		if(isValidJob(notifyJobInfo.getWarnJobunitId(), notifyJobInfo.getWarnJobId())){
			info.setWarnJobunitId(notifyJobInfo.getWarnJobunitId());
			info.setWarnJobId(notifyJobInfo.getWarnJobId());
		}

		if(isValidJob(notifyJobInfo.getCriticalJobunitId(), notifyJobInfo.getCriticalJobId())){
			info.setCriticalJobunitId(notifyJobInfo.getCriticalJobunitId());
			info.setCriticalJobId(notifyJobInfo.getCriticalJobId());
		}

		if(isValidJob(notifyJobInfo.getUnknownJobunitId(), notifyJobInfo.getUnknownJobId())){
			info.setUnknownJobunitId(notifyJobInfo.getUnknownJobunitId());
			info.setUnknownJobId(notifyJobInfo.getUnknownJobId());
		}

		info.setJobExecFacilityFlg(notifyJobInfo.getJobExecFacilityFlg());

		// ジョブ通知でのジョブ実行対象を、「イベントが発生したノード」に設定している場合、
		// 実行対象のファシリティIDをnullにセットする
		if (info.getJobExecFacilityFlg().intValue() == ExecFacilityConstant.TYPE_GENERATION) {
			info.setJobExecFacility(null);
		} else {
			info.setJobExecFacility(notifyJobInfo.getJobExecFacility());
		}

		info.setJobExecScope(notifyJobInfo.getJobExecScope());

		return info;
	}
	/**
	 * 通知詳細情報(環境構築)の情報をXMLからDTOのBeanに変換する。 
	 * 
	 * @param notifyID 通知ID
	 * @param notifyInfraInfo 環境構築通知定義 XML Bean
	 * @return ジョブ通知定義 Hinemos Bean(DTO)
	 */
	private static NotifyInfraInfo convXml2DtoInfra(com.clustercontrol.utility.settings.platform.xml.NotifyInfraInfo notifyInfraInfo) {
		//具象クラスとして、
		NotifyInfraInfo info = new NotifyInfraInfo();

		info.setInfoValidFlg(notifyInfraInfo.getInfoValidFlg());
		info.setWarnValidFlg(notifyInfraInfo.getWarnValidFlg());
		info.setCriticalValidFlg(notifyInfraInfo.getCriticalValidFlg());
		info.setUnknownValidFlg(notifyInfraInfo.getUnknownValidFlg());

		info.setInfoInfraId(notifyInfraInfo.getInfoInfraId());
		info.setWarnInfraId(notifyInfraInfo.getWarnInfraId());
		info.setCriticalInfraId(notifyInfraInfo.getCriticalInfraId());
		info.setUnknownInfraId(notifyInfraInfo.getUnknownInfraId());
		
		info.setInfoInfraFailurePriority(notifyInfraInfo.getInfoInfraFailurePriority());
		info.setWarnInfraFailurePriority(notifyInfraInfo.getWarnInfraFailurePriority());
		info.setCriticalInfraFailurePriority(notifyInfraInfo.getCriticalInfraFailurePriority());
		info.setUnknownInfraFailurePriority(notifyInfraInfo.getUnknownInfraFailurePriority());

		info.setInfraExecFacilityFlg(notifyInfraInfo.getInfraExecFacilityFlg());

		// 環境構築通知でのジョブ実行対象を、「イベントが発生したノード」に設定している場合、
		// 実行対象のファシリティIDをnullにセットする
		if (info.getInfraExecFacilityFlg().intValue() == ExecFacilityConstant.TYPE_GENERATION) {
			info.setInfraExecFacility(null);
		} else {
			info.setInfraExecFacility(notifyInfraInfo.getInfraExecFacility());
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
	 */
	private static NotifyLogEscalateInfo convXml2DtoLogEscalate(com.clustercontrol.utility.settings.platform.xml.NotifyLogEscalateInfo notifyLogInfo) {
		//具象クラスとして、
		NotifyLogEscalateInfo info = new NotifyLogEscalateInfo();

		info.setInfoValidFlg(notifyLogInfo.getInfoValidFlg());
		info.setWarnValidFlg(notifyLogInfo.getWarnValidFlg());
		info.setCriticalValidFlg(notifyLogInfo.getCriticalValidFlg());
		info.setUnknownValidFlg(notifyLogInfo.getUnknownValidFlg());

		// イベントが発生したスコープをエスカレーション先とする場合はnullをセットする
		if (notifyLogInfo.getEscalateFacility() != null && !notifyLogInfo.getEscalateFacility().equals("")){
			info.setEscalateFacility(notifyLogInfo.getEscalateFacility());
		} else {
			info.setEscalateFacility(null);
		}
		
		info.setEscalateFacilityFlg(notifyLogInfo.getEsaclateFacilityFlg());
		info.setEscalatePort(notifyLogInfo.getEscalatePort());
		info.setEscalateScope(notifyLogInfo.getEscalateScope());

		info.setInfoEscalateMessage(notifyLogInfo.getInfoEscalateMessage());
		info.setInfoSyslogFacility(notifyLogInfo.getInfoSyslogFacility());
		info.setInfoSyslogPriority(notifyLogInfo.getInfoSyslogPriority());
		
		info.setWarnEscalateMessage(notifyLogInfo.getWarnEscalateMessage());
		info.setWarnSyslogFacility(notifyLogInfo.getWarnSyslogFacility());
		info.setWarnSyslogPriority(notifyLogInfo.getWarnSyslogPriority());

		info.setCriticalEscalateMessage(notifyLogInfo.getCriticalEscalateMessage());
		info.setCriticalSyslogFacility(notifyLogInfo.getCriticalSyslogFacility());
		info.setCriticalSyslogPriority(notifyLogInfo.getCriticalSyslogPriority());

		info.setUnknownEscalateMessage(notifyLogInfo.getUnknownEscalateMessage());
		info.setUnknownSyslogFacility(notifyLogInfo.getUnknownSyslogFacility());
		info.setUnknownSyslogPriority(notifyLogInfo.getUnknownSyslogPriority());
		
		return info;
	}

	
	/**
	 * 通知詳細情報(コマンド)の情報をXMLからDTOのBeanに変換する。 
	 * 
	 * @param notifyID 通知ID
	 * @param notifyCommandlList コマンド通知定義 XML Bean
	 * @return コマンド通知定義 Hinemos Bean(DTO)
	 */
	private static NotifyCommandInfo convXml2DtoCommand(com.clustercontrol.utility.settings.platform.xml.NotifyCommandInfo notifyCommandInfo) {
		//com.clustercontrol.notify.bean.NotifyCommandInfoは中小クラスなので、
		//EJBから自動生成されるクラスを利用する。
		NotifyCommandInfo info = new NotifyCommandInfo();

		info.setInfoValidFlg(notifyCommandInfo.getInfoValidFlg());
		info.setWarnValidFlg(notifyCommandInfo.getWarnValidFlg());
		info.setCriticalValidFlg(notifyCommandInfo.getCriticalValidFlg());
		info.setUnknownValidFlg(notifyCommandInfo.getUnknownValidFlg());

		info.setSetEnvironment(YesNoConstant.BOOLEAN_YES);
		info.setTimeout(notifyCommandInfo.getCommandTimeout());
		
		info.setInfoCommand(notifyCommandInfo.getInfoCommand());
		info.setInfoEffectiveUser(notifyCommandInfo.getInfoEffectiveUser());

		info.setWarnCommand(notifyCommandInfo.getWarnCommand());
		info.setWarnEffectiveUser(notifyCommandInfo.getWarnEffectiveUser());

		info.setCriticalCommand(notifyCommandInfo.getCriticalCommand());
		info.setCriticalEffectiveUser(notifyCommandInfo.getCriticalEffectiveUser());

		info.setUnknownCommand(notifyCommandInfo.getUnknownCommand());
		info.setUnknownEffectiveUser(notifyCommandInfo.getUnknownEffectiveUser());
		
		return info;
		
	}
	
	/**
	 * 通知定義の、Hinemos DTO BeanからXML Beanへ変換する。
	 * 
	 * @param notifyInfo 通知定義 Hinemos Bean
	 * @return 通知定義 XML Bean
	 */
	public static com.clustercontrol.utility.settings.platform.xml.NotifyInfo convDto2XmlNotify(NotifyInfo notifyInfo) {
		
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
		ret.setNotifyType(notifyInfo.getNotifyType());
		
		//通知までのカウント
		ret.setInitialCount(notifyInfo.getInitialCount());
		
		//再通知種別
		ret.setRenotifyType(notifyInfo.getRenotifyType());
		
		//再通知抑制期間
		Integer renotifyPeriod = notifyInfo.getRenotifyPeriod();
		if (renotifyPeriod != null)
			ret.setRenotifyPeriod(renotifyPeriod.intValue());
		
		//登録日時
		String regDate = DateUtil.convEpoch2DateString(notifyInfo.getRegDate());
		if (regDate != null && !regDate.isEmpty()) {
			ret.setRegDate(regDate);
		}
		
		//登録ユーザ
		ret.setRegUser(notifyInfo.getRegUser());
		
		//更新日時
		String updateDate = DateUtil.convEpoch2DateString(notifyInfo.getUpdateDate());
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
		convDto2XmlInfra(String notifyId, NotifyInfraInfo notify) {

		com.clustercontrol.utility.settings.platform.xml.NotifyInfraInfo ret = 
				new com.clustercontrol.utility.settings.platform.xml.NotifyInfraInfo();

		ret.setNotifyId(notifyId);
		ret.setInfoValidFlg(notify.isInfoValidFlg());
		ret.setWarnValidFlg(notify.isWarnValidFlg());
		ret.setCriticalValidFlg(notify.isCriticalValidFlg());
		ret.setUnknownValidFlg(notify.isUnknownValidFlg());

		ret.setInfraExecFacility(notify.getInfraExecFacility());
		ret.setInfraExecFacilityFlg(notify.getInfraExecFacilityFlg());
		ret.setInfraExecScope(notify.getInfraExecScope());
			
		ret.setInfoInfraFailurePriority(notify.getInfoInfraFailurePriority());
		ret.setInfoInfraId(notify.getInfoInfraId());

		ret.setWarnInfraFailurePriority(notify.getWarnInfraFailurePriority());
		ret.setWarnInfraId(notify.getWarnInfraId());

		ret.setCriticalInfraFailurePriority(notify.getCriticalInfraFailurePriority());
		ret.setCriticalInfraId(notify.getCriticalInfraId());

		ret.setUnknownInfraFailurePriority(notify.getUnknownInfraFailurePriority());
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
		convDto2XmlStatus(String notifyId, NotifyStatusInfo notify) {

		com.clustercontrol.utility.settings.platform.xml.NotifyStatusInfo ret = 
			new com.clustercontrol.utility.settings.platform.xml.NotifyStatusInfo();

		ret.setNotifyId(notifyId);
		ret.setInfoValidFlg(notify.isInfoValidFlg());
		ret.setWarnValidFlg(notify.isWarnValidFlg());
		ret.setCriticalValidFlg(notify.isCriticalValidFlg());
		ret.setUnknownValidFlg(notify.isUnknownValidFlg());

		ret.setStatusInvalidFlg(notify.getStatusInvalidFlg());
		ret.setStatusUpdatePriority(notify.getStatusUpdatePriority());
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
		convDto2XmlEvent(String notifyId, NotifyEventInfo notify) {

		com.clustercontrol.utility.settings.platform.xml.NotifyEventInfo ret = 
			new com.clustercontrol.utility.settings.platform.xml.NotifyEventInfo();

		ret.setNotifyId(notifyId);
		
		ret.setInfoValidFlg(notify.isInfoValidFlg());
		ret.setWarnValidFlg(notify.isWarnValidFlg());
		ret.setCriticalValidFlg(notify.isCriticalValidFlg());
		ret.setUnknownValidFlg(notify.isUnknownValidFlg());
		
		ret.setInfoEventNormalState(notify.getInfoEventNormalState());
		ret.setWarnEventNormalState(notify.getWarnEventNormalState());
		ret.setCriticalEventNormalState(notify.getCriticalEventNormalState());
		ret.setUnknownEventNormalState(notify.getUnknownEventNormalState());

		return ret;
	}


	/**
	 * 通知詳細情報(メール)の情報をHinemos DTOからDXMLのBeanに変換する。 
	 * 
	 * @param NotifyMailInfo メール通知定義　Hinemos Bean(DTO)
	 * @return メール通知定義　XML Bean
	 */
	private static com.clustercontrol.utility.settings.platform.xml.NotifyMailInfo 
		convDto2XmlMail(String notifyId, NotifyMailInfo notify) {

		com.clustercontrol.utility.settings.platform.xml.NotifyMailInfo ret = 
			new com.clustercontrol.utility.settings.platform.xml.NotifyMailInfo();

		ret.setNotifyId(notifyId);
		ret.setInfoValidFlg(notify.isInfoValidFlg());
		ret.setWarnValidFlg(notify.isWarnValidFlg());
		ret.setCriticalValidFlg(notify.isCriticalValidFlg());
		ret.setUnknownValidFlg(notify.isUnknownValidFlg());

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
		convDto2XmlJob(String notifyId, NotifyJobInfo notify) {

		com.clustercontrol.utility.settings.platform.xml.NotifyJobInfo ret = 
				new com.clustercontrol.utility.settings.platform.xml.NotifyJobInfo();

		ret.setNotifyId(notifyId);
		ret.setInfoValidFlg(notify.isInfoValidFlg());
		ret.setWarnValidFlg(notify.isWarnValidFlg());
		ret.setCriticalValidFlg(notify.isCriticalValidFlg());
		ret.setUnknownValidFlg(notify.isUnknownValidFlg());

		ret.setJobExecFacility(notify.getJobExecFacility());
		ret.setJobExecFacilityFlg(notify.getJobExecFacilityFlg());
		ret.setJobExecScope(notify.getJobExecScope());
			
		ret.setInfoJobFailurePriority(notify.getInfoJobFailurePriority());
		ret.setInfoJobunitId(notify.getInfoJobunitId());
		ret.setInfoJobId(notify.getInfoJobId());

		ret.setWarnJobFailurePriority(notify.getWarnJobFailurePriority());
		ret.setWarnJobunitId(notify.getWarnJobunitId());
		ret.setWarnJobId(notify.getWarnJobId());

		ret.setCriticalJobFailurePriority(notify.getCriticalJobFailurePriority());
		ret.setCriticalJobunitId(notify.getCriticalJobunitId());
		ret.setCriticalJobId(notify.getCriticalJobId());

		ret.setUnknownJobFailurePriority(notify.getUnknownJobFailurePriority());
		ret.setUnknownJobunitId(notify.getUnknownJobunitId());
		ret.setUnknownJobId(notify.getUnknownJobId());

		return ret;
	}
	/**
	 * 通知詳細情報(ログエスカレーション)の情報をHinemos DTOからDXMLのBeanに変換する。 
	 * 
	 * @param NotifyLogInfo ログエスカレーション通知定義　Hinemos Bean(DTO)
	 * @return ログエスカレーション通知定義　XML Bean
	 */
	private static com.clustercontrol.utility.settings.platform.xml.NotifyLogEscalateInfo 
		convDto2XmlLogEscalate(String notifyId, NotifyLogEscalateInfo notify) {

		com.clustercontrol.utility.settings.platform.xml.NotifyLogEscalateInfo ret = 
			new com.clustercontrol.utility.settings.platform.xml.NotifyLogEscalateInfo();

		ret.setNotifyId(notifyId);
		ret.setInfoValidFlg(notify.isInfoValidFlg());
		ret.setWarnValidFlg(notify.isWarnValidFlg());
		ret.setCriticalValidFlg(notify.isCriticalValidFlg());
		ret.setUnknownValidFlg(notify.isUnknownValidFlg());
		
		ret.setEsaclateFacilityFlg(notify.getEscalateFacilityFlg());
		ret.setEscalateFacility(notify.getEscalateFacility());
		ret.setEscalatePort(notify.getEscalatePort());
		ret.setEscalateScope(notify.getEscalateScope());
		
		ret.setInfoEscalateMessage(notify.getInfoEscalateMessage());
		ret.setInfoSyslogPriority(notify.getInfoSyslogPriority());
		ret.setInfoSyslogFacility(notify.getInfoSyslogFacility());

		ret.setWarnEscalateMessage(notify.getWarnEscalateMessage());
		ret.setWarnSyslogPriority(notify.getWarnSyslogPriority());
		ret.setWarnSyslogFacility(notify.getWarnSyslogFacility());
		
		ret.setCriticalEscalateMessage(notify.getCriticalEscalateMessage());
		ret.setCriticalSyslogPriority(notify.getCriticalSyslogPriority());
		ret.setCriticalSyslogFacility(notify.getCriticalSyslogFacility());
		
		ret.setUnknownEscalateMessage(notify.getUnknownEscalateMessage());
		ret.setUnknownSyslogPriority(notify.getUnknownSyslogPriority());
		ret.setUnknownSyslogFacility(notify.getUnknownSyslogFacility());
		
		return ret;
	}
	
	/**
	 * 通知詳細情報(コマンド)の情報をHinemos DTOからDXMLのBeanに変換する。 
	 * 
	 * @param NotifyCommandInfo コマンド通知定義　Hinemos Bean(DTO)
	 * @return コマンド通知定義　XML Bean
	 */
	private static com.clustercontrol.utility.settings.platform.xml.NotifyCommandInfo 
		convDto2XmlCommand(String notifyId, NotifyCommandInfo notify) {
		
		com.clustercontrol.utility.settings.platform.xml.NotifyCommandInfo ret = 
			new com.clustercontrol.utility.settings.platform.xml.NotifyCommandInfo();

		ret.setNotifyId(notifyId);
		
		ret.setInfoValidFlg(notify.isInfoValidFlg());
		ret.setWarnValidFlg(notify.isWarnValidFlg());
		ret.setCriticalValidFlg(notify.isCriticalValidFlg());
		ret.setUnknownValidFlg(notify.isUnknownValidFlg());
		
		ret.setCommandTimeout(notify.getTimeout());
		ret.setInfoCommand(notify.getInfoCommand());
		ret.setInfoEffectiveUser(notify.getInfoEffectiveUser());

		ret.setWarnCommand(notify.getWarnCommand());
		ret.setWarnEffectiveUser(notify.getWarnEffectiveUser());

		ret.setCriticalCommand(notify.getCriticalCommand());
		ret.setCriticalEffectiveUser(notify.getCriticalEffectiveUser());

		ret.setUnknownCommand(notify.getUnknownCommand());
		ret.setUnknownEffectiveUser(notify.getUnknownEffectiveUser());
		
		return ret;
	}
	
	private static boolean isValidJob(String jobUnitId, String jobId){
		if(jobUnitId != null && jobId != null){
			return true;
		}else{
			log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
					+ "(JobunitID) : "
					+ jobUnitId
					+ ", (JobID) : "
					+ jobId);
			return false;
		}
	}
}
