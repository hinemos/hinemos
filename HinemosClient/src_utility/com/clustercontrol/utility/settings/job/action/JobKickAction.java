/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.job.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.openapitools.client.model.AddFileCheckRequest;
import org.openapitools.client.model.AddJobLinkRcvRequest;
import org.openapitools.client.model.AddJobManualRequest;
import org.openapitools.client.model.AddScheduleRequest;
import org.openapitools.client.model.ImportFileCheckRecordRequest;
import org.openapitools.client.model.ImportFileCheckRequest;
import org.openapitools.client.model.ImportFileCheckResponse;
import org.openapitools.client.model.ImportJobLinkRcvRecordRequest;
import org.openapitools.client.model.ImportJobLinkRcvRequest;
import org.openapitools.client.model.ImportJobLinkRcvResponse;
import org.openapitools.client.model.ImportJobManualRecordRequest;
import org.openapitools.client.model.ImportJobManualRequest;
import org.openapitools.client.model.ImportJobManualResponse;
import org.openapitools.client.model.ImportScheduleRecordRequest;
import org.openapitools.client.model.ImportScheduleRequest;
import org.openapitools.client.model.ImportScheduleResponse;
import org.openapitools.client.model.JobFileCheckResponse;
import org.openapitools.client.model.JobKickResponse;
import org.openapitools.client.model.JobLinkRcvResponse;
import org.openapitools.client.model.JobScheduleResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.constant.HinemosModuleConstant;
import com.clustercontrol.utility.difference.CSVUtil;
import com.clustercontrol.utility.difference.DiffUtil;
import com.clustercontrol.utility.difference.ResultA;
import com.clustercontrol.utility.settings.ClearMethod;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.DiffMethod;
import com.clustercontrol.utility.settings.ExportMethod;
import com.clustercontrol.utility.settings.ImportMethod;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.job.conv.KickConv;
import com.clustercontrol.utility.settings.job.xml.FileCheckInfo;
import com.clustercontrol.utility.settings.job.xml.FileCheckList;
import com.clustercontrol.utility.settings.job.xml.JobLinkRcvInfo;
import com.clustercontrol.utility.settings.job.xml.JobLinkRcvList;
import com.clustercontrol.utility.settings.job.xml.ManualInfo;
import com.clustercontrol.utility.settings.job.xml.ManualList;
import com.clustercontrol.utility.settings.job.xml.ScheduleInfo;
import com.clustercontrol.utility.settings.job.xml.ScheduleList;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.platform.conv.CommonConv;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.AccountUtil;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.ImportClientController;
import com.clustercontrol.utility.util.ImportRecordConfirmer;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;
import com.clustercontrol.utility.util.XmlMarshallUtil;

/**
 * JOB管理実行契機情報を取得、設定、削除します。<br>
 * XMLファイルに定義されたジョブスケジュール情報及びジョブファイルチェック情報をマネージャに反映させるクラス<br>
 * 
 * @version 6.1.0
 * @since 1.0.0
 * 
 */
public class JobKickAction {

	/*ロガー*/
	protected static Logger log = Logger.getLogger(JobKickAction.class);
	
	private KickConv kickConv = new KickConv();
	
	public JobKickAction() throws ConvertorException {
		super();
	}
	
	/**
	 * 実行契機情報をマネージャに設定します。
	 * 
	 * @param インポートするXML
	 * @param マネージャへのコネクション
	 * 
	 */
	@ImportMethod
	public int importJobSchedule(String xmlSchedule, String xmlFileCheck, String xmlManual, String xmlJobLinkRcv){

		log.debug("Start Import JobKick");

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			log.debug("End Import JobKick (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		int ret=0;

		//スケジュール情報のXMからBeanに取り込みます。
		ScheduleList scheduleList=null;
		try {
			scheduleList = XmlMarshallUtil.unmarshall(ScheduleList.class,new InputStreamReader(
					new FileInputStream(xmlSchedule), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import JobKick (Error)");
			return ret;
		}
		
		/* スキーマのバージョンチェック*/
		if(!checkSchemaVersion(scheduleList.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		//ファイルチェック情報のXMからBeanに取り込みます。
		FileCheckList fileCheckList = null;
		try {
			fileCheckList = XmlMarshallUtil.unmarshall(FileCheckList.class,new InputStreamReader(
					new FileInputStream(xmlFileCheck), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import JobKick (Error)");
			return ret;
		}
		
		/* スキーマのバージョンチェック*/
		if(!checkSchemaVersion(fileCheckList.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		// マニュアル実行情報のXMLからBeanに取り込みます。
		ManualList manualList = null;
		try {
			try (FileInputStream fis = new FileInputStream(xmlManual);
					InputStreamReader isr = new InputStreamReader(fis, "UTF-8");) {
				manualList = XmlMarshallUtil.unmarshall(ManualList.class,isr);
			}
		} catch (Exception e) {
			log.error(String.format("%s %s", Messages.getString("SettingTools.UnmarshalXmlFailed"), e));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import JobKick (Error)");
			return ret;
		}

		// スキーマのバージョンチェック
		if (!checkSchemaVersion(manualList.getSchemaInfo())) {
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		// ジョブ連携受信情報のXMLからBeanに取り込みます。
		JobLinkRcvList jobLinkRcvList = null;
		try {
			try (FileInputStream fis = new FileInputStream(xmlJobLinkRcv);
					InputStreamReader isr = new InputStreamReader(fis, "UTF-8");) {
				jobLinkRcvList = XmlMarshallUtil.unmarshall(JobLinkRcvList.class,isr);
			}
		} catch (Exception e) {
			log.error(String.format("%s %s", Messages.getString("SettingTools.UnmarshalXmlFailed"), e));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import JobKick (Error)");
			return ret;
		}

		// スキーマのバージョンチェック
		if (!checkSchemaVersion(jobLinkRcvList.getSchemaInfo())) {
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		List<String> objectIdList = new ArrayList<String>();
		
		/////////////////////////////////////
		// スケジュールのインポート処理
		/////////////////////////////////////
		
		// レコードの確認(スケジュール)
		ImportScheduleRecordConfirmer scheduleConfirmer = new ImportScheduleRecordConfirmer( log, scheduleList.getScheduleInfo());
		int scheduleConfirmerRet = scheduleConfirmer.executeConfirm();
		if (scheduleConfirmerRet != 0) {
			ret = scheduleConfirmerRet;
		}
		// レコードの登録（スケジュール）
		if (!(scheduleConfirmer.getImportRecDtoList().isEmpty())) {
			ImportScheduleClientController scheduleController = new ImportScheduleClientController(log,
					Messages.getString("job.management.schedule"), scheduleConfirmer.getImportRecDtoList(), true);
			int scheduleControllerRet = scheduleController.importExecute();
			for (RecordRegistrationResponse rec: scheduleController.getImportSuccessList() ){
				objectIdList.add(rec.getImportKeyValue());
			}
			if (scheduleControllerRet != 0) {
				ret = scheduleControllerRet;
			}
		}
		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		/////////////////////////////////////
		// ファイルチェックのインポート処理
		/////////////////////////////////////
		
		// レコードの確認(ファイルチェック)
		ImportFileCheckRecordConfirmer fileCheckConfirmer = new ImportFileCheckRecordConfirmer( log, fileCheckList.getFileCheckInfo() );
		int fileCheckConfirmerRet = fileCheckConfirmer.executeConfirm();
		if (fileCheckConfirmerRet != 0) {
			ret = fileCheckConfirmerRet;
		}
		// レコードの登録（ファイルチェック）
		if (!(fileCheckConfirmer.getImportRecDtoList().isEmpty())) {
			ImportFileCheckClientController fileCheckController = new ImportFileCheckClientController(log,
					Messages.getString("job.management.filecheck"), fileCheckConfirmer.getImportRecDtoList(), true);
			int fileCheckControllerRet = fileCheckController.importExecute();
			for (RecordRegistrationResponse rec: fileCheckController.getImportSuccessList() ){
				objectIdList.add(rec.getImportKeyValue());
			}
			if (fileCheckControllerRet != 0) {
				ret = fileCheckControllerRet;
			}
		}
		
		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		/////////////////////////////////////
		// マニュアル契機のインポート処理
		/////////////////////////////////////
		
		// レコードの確認(マニュアル実行契機)
		ImportJobManualRecordConfirmer jobManualConfirmer = new ImportJobManualRecordConfirmer( log, manualList.getManualInfo() );
		int jobManualConfirmerRet = jobManualConfirmer.executeConfirm();
		if (jobManualConfirmerRet != 0) {
			ret = jobManualConfirmerRet;
		}
		// レコードの登録（マニュアル実行契機）
		if (!(jobManualConfirmer.getImportRecDtoList().isEmpty())) {
			ImportJobManualClientController jobManualController = new ImportJobManualClientController(log,
					Messages.getString("job.management.manual"), jobManualConfirmer.getImportRecDtoList(), true);
			int jobManualControllerRet = jobManualController.importExecute();
			for (RecordRegistrationResponse rec: jobManualController.getImportSuccessList() ){
				objectIdList.add(rec.getImportKeyValue());
			}
			if (jobManualControllerRet != 0) {
				ret = jobManualControllerRet;
			}
		}
		
		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		/////////////////////////////////////
		// ジョブ連携受信契機のインポート処理
		/////////////////////////////////////
		
		// レコードの確認(ジョブ連携受信実行契機)
		ImportJobLinkRcvRecordConfirmer jobLinkRcvConfirmer = new ImportJobLinkRcvRecordConfirmer( log, jobLinkRcvList.getJobLinkRcvInfo() );
		int jobLinkRcvConfirmerRet = jobLinkRcvConfirmer.executeConfirm();
		if (jobLinkRcvConfirmerRet != 0) {
			ret = jobLinkRcvConfirmerRet;
		}
		// レコードの登録（ジョブ連携受信実行契機）
		if (!(jobLinkRcvConfirmer.getImportRecDtoList().isEmpty())) {
			ImportJobLinkRcvClientController jobLinkRcvController = new ImportJobLinkRcvClientController(log,
					Messages.getString("job.management.joblinkrcv"), jobLinkRcvConfirmer.getImportRecDtoList(), true);
			int jobLinkRcvControllerRet = jobLinkRcvController.importExecute();
			for (RecordRegistrationResponse rec: jobLinkRcvController.getImportSuccessList() ){
				objectIdList.add(rec.getImportKeyValue());
			}
			if (jobLinkRcvControllerRet != 0) {
				ret = jobLinkRcvControllerRet;
			}
		}
		
		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		//オブジェクト権限同時インポート
		importObjectPrivilege(HinemosModuleConstant.JOB_KICK, objectIdList);
		
		//差分削除
		checkDelete(scheduleList, fileCheckList, manualList, jobLinkRcvList);
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		}else{
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
		}
		log.debug("End Import JobKick");

		return ret;
	}

	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.job.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = kickConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.job.xml.SchemaInfo sci = kickConv.getSchemaVersion();
		
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	/**
	 * 実行契機情報をマネージャからエクスポートします。
	 * 
	 * @param 出力するXMLファイル
	 * @return 終了コード
	 * @throws HinemosUnknown 
	 */
	@ExportMethod
	public  int exportJobSchedule(String xmlSchedule, String xmlFileCheck, String xmlManual, String xmlJobLinkRcv) throws HinemosUnknown{

		log.debug("Start Export JobKick");

		int ret = 0;
		//マネージャからスケジュールのリストを取得する。
		//List<JobKick> kickList = new ArrayList<JobKick>() ;
		List<JobKickResponse> kickList = null;
		try {
			kickList = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobKickList();
			if (null == kickList){
				log.error(Messages.getString("SettingTools.EndWithErrorCode") );
				return SettingConstants.ERROR_INPROCESS;
			}
			Collections.sort(kickList, new Comparator<JobKickResponse>() {
				@Override
				public int compare(JobKickResponse kick1, JobKickResponse kick2) {
					return kick1.getId().compareTo(kick2.getId());
				}
			});
		} catch (Exception e1) {
			log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export JobKick (Error)");
			return ret;
		}
		
		// XMLバインディング用のリスト
		ScheduleList scheduleList = new ScheduleList();
		FileCheckList fileCheckList = new FileCheckList();
		ManualList manualList = new ManualList();
		JobLinkRcvList jobLinkRcvList = new JobLinkRcvList();
		
		
		// スケジュール１つ分
		for (JobKickResponse kick : kickList) {
			if(kick.getType() == JobKickResponse.TypeEnum.SCHEDULE){
				JobScheduleResponse jobScheduleResponse = new JobScheduleResponse();
				RestClientBeanUtil.convertBeanSimple(kick, jobScheduleResponse);
				
				// Enumの変換は別途実施する
				jobScheduleResponse.setScheduleType(JobScheduleResponse.ScheduleTypeEnum.valueOf(kick.getScheduleType().getValue()));
				if(kick.getSessionPremakeScheduleType() != null){
					jobScheduleResponse.setSessionPremakeScheduleType(JobScheduleResponse.SessionPremakeScheduleTypeEnum.valueOf(kick.getSessionPremakeScheduleType().getValue()));
				}
				if(kick.getSessionPremakeEveryXHour() != null){
					jobScheduleResponse.setSessionPremakeEveryXHour(JobScheduleResponse.SessionPremakeEveryXHourEnum.valueOf(kick.getSessionPremakeEveryXHour().name()));
				}
				
				scheduleList.addScheduleInfo(kickConv.scheduleDto2Xml(jobScheduleResponse));
				log.info(String.format("%s : %s", Messages.getString("SettingTools.ExportSucceeded"), kick.getId()));
			}
			else if(kick.getType() == JobKickResponse.TypeEnum.FILECHECK){
				JobFileCheckResponse jobFileCheckResponse = new JobFileCheckResponse();
				RestClientBeanUtil.convertBeanSimple(kick, jobFileCheckResponse);
				
				jobFileCheckResponse.setEventType(JobFileCheckResponse.EventTypeEnum.fromValue(kick.getEventType().getValue()));
				if(kick.getModifyType() != null){
					jobFileCheckResponse.setModifyType(JobFileCheckResponse.ModifyTypeEnum.fromValue(kick.getModifyType().getValue()));
				}				
				
				fileCheckList.addFileCheckInfo(kickConv.fileCheckDto2Xml(jobFileCheckResponse));
				log.info(String.format("%s : %s", Messages.getString("SettingTools.ExportSucceeded"), kick.getId()));
			}
			else if(kick.getType() == JobKickResponse.TypeEnum.MANUAL){
				manualList.addManualInfo(kickConv.manualDto2Xml(kick));
				log.info(String.format("%s : %s", Messages.getString("SettingTools.ExportSucceeded"), kick.getId()));
			}
			else if(kick.getType() == JobKickResponse.TypeEnum.JOBLINKRCV){
				jobLinkRcvList.addJobLinkRcvInfo(kickConv.jobLinkRcvDto2Xml(kick));
				log.info(String.format("%s : %s", Messages.getString("SettingTools.ExportSucceeded"), kick.getId()));
			}
			
		}
		
		try {
			// スケジュール情報をXMLに出力
			scheduleList.setCommon(CommonConv.versionJobDto2Xml(Config.getVersion()));
			scheduleList.setSchemaInfo(kickConv.getSchemaVersion());
			try (FileOutputStream fos = new FileOutputStream(xmlSchedule);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");) {
				scheduleList.marshal(osw);
			}
			
			// ファイルチェック情報をXMLに出力
			fileCheckList.setCommon(CommonConv.versionJobDto2Xml(Config.getVersion()));
			fileCheckList.setSchemaInfo(kickConv.getSchemaVersion());
			try (FileOutputStream fos = new FileOutputStream(xmlFileCheck);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");) {
				fileCheckList.marshal(osw);
			}
			
			// マニュアル情報をXMLに出力
			manualList.setCommon(CommonConv.versionJobDto2Xml(Config.getVersion()));
			manualList.setSchemaInfo(kickConv.getSchemaVersion());
			try (FileOutputStream fos = new FileOutputStream(xmlManual);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");) {
				manualList.marshal(osw);
			}
			
			// ジョブ連携受信情報をXMLに出力
			jobLinkRcvList.setCommon(CommonConv.versionJobDto2Xml(Config.getVersion()));
			jobLinkRcvList.setSchemaInfo(kickConv.getSchemaVersion());
			try (FileOutputStream fos = new FileOutputStream(xmlJobLinkRcv);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");) {
				jobLinkRcvList.marshal(osw);
			}
		} catch (UnsupportedEncodingException | MarshalException | ValidationException e) {
			log.warn(String.format(Messages.getString("SettingTools.MarshalXmlFailed"), e));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (Exception e) {
			log.warn(Messages.getString("SettingTools.ExportFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
		}
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		}else{
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
		}
		log.debug("End Export JobKick");
		return ret;
	}


	@ClearMethod
	public int clearJobSchedule(){

		log.debug("Start Clear JobKick");

		int ret=0;

		//	マネージャから実行契機のリストを取得する。
		List<JobKickResponse> kickList =null;
		try {
			kickList = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobKickList();
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear JobKick (Error)");
			return ret;
		}

		String kickId ;
		if( kickList == null){
			ret = SettingConstants.ERROR_INPROCESS;
		}else{
			List<String> scheduleList = new ArrayList<>();
			List<String> fileCheckList = new ArrayList<>();
			List<String> manualList = new ArrayList<>();
			List<String> jobLinkRcvList = new ArrayList<>();
			for (JobKickResponse kick : kickList) {
				//順番に実行契機IDを取得し、
				kickId= kick.getId();
				if (kick.getType() == JobKickResponse.TypeEnum.SCHEDULE) {
					scheduleList.add(kickId);
				} else if (kick.getType() == JobKickResponse.TypeEnum.FILECHECK) {
					fileCheckList.add(kickId);
				} else if (kick.getType() == JobKickResponse.TypeEnum.MANUAL) {
					manualList.add(kickId);
				} else if (kick.getType() == JobKickResponse.TypeEnum.JOBLINKRCV) {
					jobLinkRcvList.add(kickId);
				}
			}

			//順番に消し込みを行う。
			List<String> currentList = Collections.emptyList();
			if (AccountUtil.isAdministrator(UtilityManagerUtil.getCurrentManagerName())) {
				// ADMINISTRATORS権限がある場合
				try {
					currentList = scheduleList;
					JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteSchedule(String.join(",", currentList));
					log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + currentList.toString());
	
					currentList = fileCheckList;
					JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteFileCheck(String.join(",", currentList));
					log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + currentList.toString());
	
					currentList = manualList;
					JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteJobManual(String.join(",", currentList));
					log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + currentList.toString());

					currentList = jobLinkRcvList;
					JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteJobLinkRcv(String.join(",", currentList));
					log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + currentList.toString());
				} catch (Exception e) {
					log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				}
			} else {
				// ADMINISTRATORS権限がない場合
				for (String id : scheduleList) {
					try {
						JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteSchedule(id);
						log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + id);
					} catch (Exception e) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
				}
				for (String id : fileCheckList) {
					try {
						JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteFileCheck(id);
						log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + id);
					} catch (Exception e) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
				}
				for (String id : manualList) {
					try {
						JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteJobManual(id);
						log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + id);
					} catch (Exception e) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
				}
				for (String id : jobLinkRcvList) {
					try {
						JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteJobLinkRcv(id);
						log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + id);
					} catch (Exception e) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
				}
			}
		}
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ClearCompleted"));
		}else{
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
		}
		log.debug("End Clear JobKick");
		return ret;
	}
	
	/**
	 * 差分比較処理を行います。
	 * XMLファイル２つ（filePath1,filePath2）を比較する。
	 * 差分がある場合：差分をＣＳＶファイルで出力する。
	 * 差分がない場合：出力しない。または、すでに存在している同一名のＣＳＶファイルを削除する。
	 * @param xmlSchedule1 スケジュールのXMLファイル1
	 * @param xmlFileCheck1 ファイルチェックのXMLファイル1
	 * @param xmlManual1 マニュアルのXMLファイル1
	 * @param xmlSchedule2 スケジュールのXMLファイル2
	 * @param xmlFileCheck2 ファイルチェックのXMLファイル2
	 * @param xmlManual2 マニュアルのXMLファイル2
	 * @return
	 * @throws ConvertorException
	 */
	@DiffMethod
	public int diffXml(String xmlSchedule1, String xmlFileCheck1, String xmlManual1, String xmlJobLinkRcv1, String xmlSchedule2, String xmlFileCheck2, String xmlManual2, String xmlJobLinkRcv2)
			throws ConvertorException {

		log.debug("Start Differrence JobKick ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		ScheduleList scheduleList1 = null;
		ScheduleList scheduleList2 = null;
		FileCheckList fileCheckList1 = null;
		FileCheckList fileCheckList2 = null;
		ManualList manualList1 = null;
		ManualList manualList2 = null;
		JobLinkRcvList jobLinkRcvList1 = null;
		JobLinkRcvList jobLinkRcvList2 = null;

		// XMLファイルからの読み込み
		try {
			scheduleList1 = XmlMarshallUtil.unmarshall(ScheduleList.class,new InputStreamReader(new FileInputStream(xmlSchedule1), "UTF-8"));
			scheduleList2 = XmlMarshallUtil.unmarshall(ScheduleList.class,new InputStreamReader(new FileInputStream(xmlSchedule2), "UTF-8"));
			fileCheckList1 = XmlMarshallUtil.unmarshall(FileCheckList.class,new InputStreamReader(new FileInputStream(xmlFileCheck1), "UTF-8"));
			fileCheckList2 = XmlMarshallUtil.unmarshall(FileCheckList.class,new InputStreamReader(new FileInputStream(xmlFileCheck2), "UTF-8"));
			manualList1 = XmlMarshallUtil.unmarshall(ManualList.class,new InputStreamReader(new FileInputStream(xmlManual1), "UTF-8"));
			manualList2 = XmlMarshallUtil.unmarshall(ManualList.class,new InputStreamReader(new FileInputStream(xmlManual2), "UTF-8"));
			jobLinkRcvList1 = XmlMarshallUtil.unmarshall(JobLinkRcvList.class,new InputStreamReader(new FileInputStream(xmlJobLinkRcv1), "UTF-8"));
			jobLinkRcvList2 = XmlMarshallUtil.unmarshall(JobLinkRcvList.class,new InputStreamReader(new FileInputStream(xmlJobLinkRcv2), "UTF-8"));
			sort(manualList1);
			sort(manualList2);
			sort(scheduleList1);
			sort(scheduleList2);
			sort(fileCheckList1);
			sort(fileCheckList2);
			sort(jobLinkRcvList1);
			sort(jobLinkRcvList2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence JobKick (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(scheduleList1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(scheduleList2.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		if(!checkSchemaVersion(fileCheckList1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(fileCheckList2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		if (!checkSchemaVersion(manualList1.getSchemaInfo())) {
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if (!checkSchemaVersion(manualList2.getSchemaInfo())) {
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		if (!checkSchemaVersion(jobLinkRcvList1.getSchemaInfo())) {
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if (!checkSchemaVersion(jobLinkRcvList2.getSchemaInfo())) {
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(scheduleList1, scheduleList2, ScheduleList.class, resultA);
			assert resultA.getResultBs().size() == 1;
			
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_1;
			}
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlSchedule2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(xmlSchedule2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						log.warn(String.format("Fail to delete File. %s", f.getAbsolutePath()));
				}
			}

			resultA = new ResultA();
			//比較処理に渡す
			diff = DiffUtil.diffCheck2(fileCheckList1, fileCheckList2, FileCheckList.class, resultA);
			assert resultA.getResultBs().size() == 1;
			
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_2;
			}
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlFileCheck2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(xmlFileCheck2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						log.warn(String.format("Fail to delete file. %s", f.getAbsolutePath()));
				}
			}

			resultA = new ResultA();
			// 比較処理に渡す
			diff = DiffUtil.diffCheck2(manualList1, manualList2, ManualList.class, resultA);
			assert resultA.getResultBs().size() == 1;
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_3;
			}
			
			// 差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlManual2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			// 差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(xmlManual2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						log.warn(String.format("Fail to delete File. %s", f.getAbsolutePath()));
				}
			}

			resultA = new ResultA();
			// 比較処理に渡す
			diff = DiffUtil.diffCheck2(jobLinkRcvList1, jobLinkRcvList2, JobLinkRcvList.class, resultA);
			assert resultA.getResultBs().size() == 1;
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_4;
			}
			
			// 差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlJobLinkRcv2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			// 差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(xmlJobLinkRcv2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						log.warn(String.format("Fail to delete File. %s", f.getAbsolutePath()));
				}
			}

		} catch (FileNotFoundException e) {
			log.error("unexpected: ", e);
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (Exception e) {
			log.error("unexpected: ", e);
			ret = SettingConstants.ERROR_INPROCESS;
		}
		finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
		
		// 処理の終了
		if ((ret >= SettingConstants.SUCCESS) && (ret<=SettingConstants.SUCCESS_MAX)){
			log.info(Messages.getString("SettingTools.DiffCompleted"));
		}else{
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
		}
		
		log.debug("End Differrence JobKick");

		return ret;
	}
	
	private void sort(ScheduleList scheduleList) {
		ScheduleInfo[] infoList = scheduleList.getScheduleInfo();
		Arrays.sort(
			infoList,
			new Comparator<ScheduleInfo>() {
				@Override
				public int compare(ScheduleInfo obj1,ScheduleInfo obj2) {
					return obj1.getId().compareTo(obj2.getId());
				}
			});
		 scheduleList.setScheduleInfo(infoList);
	}
	
	private void sort(FileCheckList fileCheckList) {
		FileCheckInfo[] infoList = fileCheckList.getFileCheckInfo();
		Arrays.sort(
			infoList,
			new Comparator<FileCheckInfo>() {
				@Override
				public int compare(FileCheckInfo obj1,FileCheckInfo obj2) {
					return obj1.getId().compareTo(obj2.getId());
				}
			});
		 fileCheckList.setFileCheckInfo(infoList);
	}

	private void sort(ManualList manualList) {
		ManualInfo[] infoList = manualList.getManualInfo();
		Arrays.sort(
			infoList,
			new Comparator<ManualInfo>() {
				@Override
				public int compare(ManualInfo obj1,ManualInfo obj2) {
					return obj1.getId().compareTo(obj2.getId());
				}
			});
		manualList.setManualInfo(infoList);
	}

	private void sort(JobLinkRcvList jobLinkRcvList) {
		JobLinkRcvInfo[] infoList = jobLinkRcvList.getJobLinkRcvInfo();
		Arrays.sort(
			infoList,
			new Comparator<JobLinkRcvInfo>() {
				@Override
				public int compare(JobLinkRcvInfo obj1,JobLinkRcvInfo obj2) {
					return obj1.getId().compareTo(obj2.getId());
				}
			});
		jobLinkRcvList.setJobLinkRcvInfo(infoList);
	}
	
	protected void checkDelete(ScheduleList xmlElements1, FileCheckList xmlElements2, ManualList xmlElements3, JobLinkRcvList xmlElements4){
		
		List<String> jobKickIds = new ArrayList<>();
		
		for(ScheduleInfo sInfo: xmlElements1.getScheduleInfo()){
			jobKickIds.add(sInfo.getId());
		}

		for(FileCheckInfo fInfo: xmlElements2.getFileCheckInfo()){
			jobKickIds.add(fInfo.getId());
		}

		for(ManualInfo mInfo: xmlElements3.getManualInfo()){
			jobKickIds.add(mInfo.getId());
		}

		for(JobLinkRcvInfo mInfo: xmlElements4.getJobLinkRcvInfo()){
			jobKickIds.add(mInfo.getId());
		}
		
		List<JobKickResponse> subList = null;
		try {
			subList = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobKickList();
		}
		catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			log.debug(e.getMessage(), e);
		}
		
		if(subList == null || subList.size() <= 0){
			return;
		}
		
		for(JobKickResponse mgrInfo: new ArrayList<>(subList)){
			for(String xmlElement: new ArrayList<>(jobKickIds)){
				if(mgrInfo.getId().equals(xmlElement)){
					subList.remove(mgrInfo);
					jobKickIds.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(JobKickResponse info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					try {
						List<String> args = new ArrayList<>();
						args.add(info.getId());
						if(info.getType() == JobKickResponse.TypeEnum.SCHEDULE){
							JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteSchedule(String.join(",", args));
						} else if (info.getType() == JobKickResponse.TypeEnum.FILECHECK){
							JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteFileCheck(String.join(",", args));
						} else if (info.getType() == JobKickResponse.TypeEnum.MANUAL){
							JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteJobManual(String.join(",", args));
						} else if (info.getType() == JobKickResponse.TypeEnum.JOBLINKRCV){
							JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteJobLinkRcv(String.join(",", args));
						}
						log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getId());
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					log.info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
					return;
				}
			}
		}
	}
	
	/**
	 * オブジェクト権限同時インポート
	 * 
	 * @param objectType
	 * @param objectIdList
	 */
	protected void importObjectPrivilege(String objectType, List<String> objectIdList){
		if(ImportProcessMode.isSameObjectPrivilege()){
			ObjectPrivilegeAction.importAccessExtraction(
					ImportProcessMode.getXmlObjectPrivilege(),
					objectType,
					objectIdList,
					log);
		}
	}
	
	public Logger getLogger() {
		return log;
	}
	
	/**
	 * ファイルチェック インポート向けのレコード確認用クラス
	 * 
	 */
	protected class ImportFileCheckRecordConfirmer extends ImportRecordConfirmer<FileCheckInfo, ImportFileCheckRecordRequest, String>{
		
		public ImportFileCheckRecordConfirmer(Logger logger, FileCheckInfo[] importRecDtoList) {
			super(logger, importRecDtoList);
		}
		
		@Override
		protected ImportFileCheckRecordRequest convertDtoXmlToRestReq(FileCheckInfo xmlDto)
				throws HinemosUnknown, InvalidSetting {
			// xmlから変換
			JobFileCheckResponse dto = kickConv.fileCheckXml2Dto(xmlDto);
			ImportFileCheckRecordRequest dtoRec = new ImportFileCheckRecordRequest();
			dtoRec.setImportData(new AddFileCheckRequest());
			RestClientBeanUtil.convertBean(dto, dtoRec.getImportData());
			dtoRec.setImportKeyValue(dtoRec.getImportData().getId());
			return dtoRec;
		}

		@Override
		protected Set<String> getExistIdSet() throws Exception {
			Set<String> retSet = new HashSet<String>();
			List<JobKickResponse> jobKickInfoList = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobKickList();
			for (JobKickResponse rec : jobKickInfoList) {
				retSet.add(rec.getId());
			}
			return retSet;
		}
		@Override
		protected boolean isLackRestReq(ImportFileCheckRecordRequest restDto) {
			return (restDto == null || restDto.getImportData().getId() == null || restDto.getImportData().getId().equals(""));
		}
		@Override
		protected String getKeyValueXmlDto(FileCheckInfo xmlDto) {
			return xmlDto.getId();
		}
		@Override
		protected String getId(FileCheckInfo xmlDto) {
			return xmlDto.getId();
		}
		@Override
		protected void setNewRecordFlg(ImportFileCheckRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
	}

	/**
	 * ファイルチェック インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportFileCheckClientController extends ImportClientController<ImportFileCheckRecordRequest, ImportFileCheckResponse, RecordRegistrationResponse>{
		
		public ImportFileCheckClientController(Logger logger, String importInfoName, List<ImportFileCheckRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportFileCheckResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportFileCheckResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportFileCheckRecordRequest importRec) {
			return importRec.getImportKeyValue();
		};

		@Override
		protected String getResKeyValue(RecordRegistrationResponse responseRec) {
			return responseRec.getImportKeyValue();
		};

		@Override
		protected boolean isResNormal(RecordRegistrationResponse responseRec) {
			return (responseRec.getResult() == ResultEnum.NORMAL) ;
		};

		@Override
		protected boolean isResSkip(RecordRegistrationResponse responseRec) {
			return (responseRec.getResult() == ResultEnum.SKIP) ;
		};

		@Override
		protected ImportFileCheckResponse callImportWrapper(List<ImportFileCheckRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportFileCheckRequest reqDto = new ImportFileCheckRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importFileCheck(reqDto);
		}

		@Override
		protected String getRestExceptionMessage(RecordRegistrationResponse responseRec) {
			if (responseRec.getExceptionInfo() != null) {
				return responseRec.getExceptionInfo().getException() +":"+ responseRec.getExceptionInfo().getMessage();
			}
			return null;
		};

		@Override
		protected void setResultLog( RecordRegistrationResponse responseRec ){
			String keyValue = getResKeyValue(responseRec);
			if ( isResNormal(responseRec) ) {
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : "+ this.importInfoName + ":" + keyValue);
			} else if(isResSkip(responseRec)){
				log.info(Messages.getString("SettingTools.SkipSystemRole") + " : " + this.importInfoName + ":" + keyValue);
			} else {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : "+ this.importInfoName + ":" + keyValue + " : "
						+ HinemosMessage.replace(getRestExceptionMessage(responseRec)));
			}
		}
	}
	
	/**
	 * スケジュール インポート向けのレコード確認用クラス
	 * 
	 */
	protected class ImportScheduleRecordConfirmer extends ImportRecordConfirmer<ScheduleInfo, ImportScheduleRecordRequest, String>{
		
		public ImportScheduleRecordConfirmer(Logger logger, ScheduleInfo[] importRecDtoList) {
			super(logger, importRecDtoList);
		}
		
		@Override
		protected ImportScheduleRecordRequest convertDtoXmlToRestReq(ScheduleInfo xmlDto)
				throws HinemosUnknown, InvalidSetting {
			// xmlから変換
			JobScheduleResponse dto = kickConv.scheduleXml2Dto(xmlDto);
			ImportScheduleRecordRequest dtoRec = new ImportScheduleRecordRequest();
			dtoRec.setImportData(new AddScheduleRequest());
			RestClientBeanUtil.convertBean(dto, dtoRec.getImportData());
			dtoRec.setImportKeyValue(dtoRec.getImportData().getId());
			return dtoRec;
		}

		@Override
		protected Set<String> getExistIdSet() throws Exception {
			Set<String> retSet = new HashSet<String>();
			List<JobKickResponse> jobKickInfoList = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobKickList();
			for (JobKickResponse rec : jobKickInfoList) {
				retSet.add(rec.getId());
			}
			return retSet;
		}
		@Override
		protected boolean isLackRestReq(ImportScheduleRecordRequest restDto) {
			return (restDto == null || restDto.getImportData().getId() == null || restDto.getImportData().getId().equals(""));
		}
		@Override
		protected String getKeyValueXmlDto(ScheduleInfo xmlDto) {
			return xmlDto.getId();
		}
		@Override
		protected String getId(ScheduleInfo xmlDto) {
			return xmlDto.getId();
		}
		@Override
		protected void setNewRecordFlg(ImportScheduleRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
	}

	/**
	 * スケジュール インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportScheduleClientController extends ImportClientController<ImportScheduleRecordRequest, ImportScheduleResponse, RecordRegistrationResponse>{
		
		public ImportScheduleClientController(Logger logger, String importInfoName, List<ImportScheduleRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportScheduleResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportScheduleResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportScheduleRecordRequest importRec) {
			return importRec.getImportKeyValue();
		};

		@Override
		protected String getResKeyValue(RecordRegistrationResponse responseRec) {
			return responseRec.getImportKeyValue();
		};

		@Override
		protected boolean isResNormal(RecordRegistrationResponse responseRec) {
			return (responseRec.getResult() == ResultEnum.NORMAL) ;
		};

		@Override
		protected boolean isResSkip(RecordRegistrationResponse responseRec) {
			return (responseRec.getResult() == ResultEnum.SKIP) ;
		};

		@Override
		protected ImportScheduleResponse callImportWrapper(List<ImportScheduleRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportScheduleRequest reqDto = new ImportScheduleRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importSchedule(reqDto);
		}

		@Override
		protected String getRestExceptionMessage(RecordRegistrationResponse responseRec) {
			if (responseRec.getExceptionInfo() != null) {
				return responseRec.getExceptionInfo().getException() +":"+ responseRec.getExceptionInfo().getMessage();
			}
			return null;
		};

		@Override
		protected void setResultLog( RecordRegistrationResponse responseRec ){
			String keyValue = getResKeyValue(responseRec);
			if ( isResNormal(responseRec) ) {
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : "+ this.importInfoName + ":" + keyValue);
			} else if(isResSkip(responseRec)){
				log.info(Messages.getString("SettingTools.SkipSystemRole") + " : " + this.importInfoName + ":" + keyValue);
			} else {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : "+ this.importInfoName + ":" + keyValue + " : "
						+ HinemosMessage.replace(getRestExceptionMessage(responseRec)));
			}
		}
	}
	
	/**
	 * マニュアル実行契機 インポート向けのレコード確認用クラス
	 * 
	 */
	protected class ImportJobManualRecordConfirmer extends ImportRecordConfirmer<ManualInfo, ImportJobManualRecordRequest, String>{
		
		public ImportJobManualRecordConfirmer(Logger logger, ManualInfo[] importRecDtoList) {
			super(logger, importRecDtoList);
		}
		
		@Override
		protected ImportJobManualRecordRequest convertDtoXmlToRestReq(ManualInfo xmlDto)
				throws HinemosUnknown, InvalidSetting {
			// xmlから変換
			JobKickResponse dto = kickConv.manualXml2Dto(xmlDto);
			ImportJobManualRecordRequest dtoRec = new ImportJobManualRecordRequest();
			dtoRec.setImportData(new AddJobManualRequest());
			RestClientBeanUtil.convertBean(dto, dtoRec.getImportData());
			dtoRec.setImportKeyValue(dtoRec.getImportData().getId());
			return dtoRec;
		}

		@Override
		protected Set<String> getExistIdSet() throws Exception {
			Set<String> retSet = new HashSet<String>();
			List<JobKickResponse> jobKickInfoList = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobKickList();
			for (JobKickResponse rec : jobKickInfoList) {
				retSet.add(rec.getId());
			}
			return retSet;
		}
		@Override
		protected boolean isLackRestReq(ImportJobManualRecordRequest restDto) {
			return (restDto == null || restDto.getImportData().getId() == null || restDto.getImportData().getId().equals(""));
		}
		@Override
		protected String getKeyValueXmlDto(ManualInfo xmlDto) {
			return xmlDto.getId();
		}
		@Override
		protected String getId(ManualInfo xmlDto) {
			return xmlDto.getId();
		}
		@Override
		protected void setNewRecordFlg(ImportJobManualRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
	}

	/**
	 * マニュアル実行契機 インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportJobManualClientController extends ImportClientController<ImportJobManualRecordRequest, ImportJobManualResponse, RecordRegistrationResponse>{
		
		public ImportJobManualClientController(Logger logger, String importInfoName, List<ImportJobManualRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportJobManualResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportJobManualResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportJobManualRecordRequest importRec) {
			return importRec.getImportKeyValue();
		};

		@Override
		protected String getResKeyValue(RecordRegistrationResponse responseRec) {
			return responseRec.getImportKeyValue();
		};

		@Override
		protected boolean isResNormal(RecordRegistrationResponse responseRec) {
			return (responseRec.getResult() == ResultEnum.NORMAL) ;
		};

		@Override
		protected boolean isResSkip(RecordRegistrationResponse responseRec) {
			return (responseRec.getResult() == ResultEnum.SKIP) ;
		};

		@Override
		protected ImportJobManualResponse callImportWrapper(List<ImportJobManualRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportJobManualRequest reqDto = new ImportJobManualRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importJobManual(reqDto);
		}

		@Override
		protected String getRestExceptionMessage(RecordRegistrationResponse responseRec) {
			if (responseRec.getExceptionInfo() != null) {
				return responseRec.getExceptionInfo().getException() +":"+ responseRec.getExceptionInfo().getMessage();
			}
			return null;
		};

		@Override
		protected void setResultLog( RecordRegistrationResponse responseRec ){
			String keyValue = getResKeyValue(responseRec);
			if ( isResNormal(responseRec) ) {
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : "+ this.importInfoName + ":" + keyValue);
			} else if(isResSkip(responseRec)){
				log.info(Messages.getString("SettingTools.SkipSystemRole") + " : " + this.importInfoName + ":" + keyValue);
			} else {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : "+ this.importInfoName + ":" + keyValue + " : "
						+ HinemosMessage.replace(getRestExceptionMessage(responseRec)));
			}
		}
	}
	
	/**
	 * ジョブ連携受信 インポート向けのレコード確認用クラス
	 * 
	 */
	protected class ImportJobLinkRcvRecordConfirmer extends ImportRecordConfirmer<JobLinkRcvInfo, ImportJobLinkRcvRecordRequest, String>{
		
		public ImportJobLinkRcvRecordConfirmer(Logger logger, JobLinkRcvInfo[] importRecDtoList) {
			super(logger, importRecDtoList);
		}
		
		@Override
		protected ImportJobLinkRcvRecordRequest convertDtoXmlToRestReq(JobLinkRcvInfo xmlDto)
				throws HinemosUnknown, InvalidSetting {
			// xmlから変換
			JobLinkRcvResponse dto = kickConv.jobLinkRcvXml2Dto(xmlDto);
			ImportJobLinkRcvRecordRequest dtoRec = new ImportJobLinkRcvRecordRequest();
			dtoRec.setImportData(new AddJobLinkRcvRequest());
			RestClientBeanUtil.convertBean(dto, dtoRec.getImportData());
			dtoRec.setImportKeyValue(dtoRec.getImportData().getId());
			return dtoRec;
		}

		@Override
		protected Set<String> getExistIdSet() throws Exception {
			Set<String> retSet = new HashSet<String>();
			List<JobKickResponse> jobKickInfoList = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobKickList();
			for (JobKickResponse rec : jobKickInfoList) {
				retSet.add(rec.getId());
			}
			return retSet;
		}
		@Override
		protected boolean isLackRestReq(ImportJobLinkRcvRecordRequest restDto) {
			return (restDto == null || restDto.getImportData().getId() == null || restDto.getImportData().getId().equals(""));
		}
		@Override
		protected String getKeyValueXmlDto(JobLinkRcvInfo xmlDto) {
			return xmlDto.getId();
		}
		@Override
		protected String getId(JobLinkRcvInfo xmlDto) {
			return xmlDto.getId();
		}
		@Override
		protected void setNewRecordFlg(ImportJobLinkRcvRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
	}

	/**
	 * ジョブ連携受信 インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportJobLinkRcvClientController extends ImportClientController<ImportJobLinkRcvRecordRequest, ImportJobLinkRcvResponse, RecordRegistrationResponse>{
		
		public ImportJobLinkRcvClientController(Logger logger, String importInfoName, List<ImportJobLinkRcvRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportJobLinkRcvResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportJobLinkRcvResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportJobLinkRcvRecordRequest importRec) {
			return importRec.getImportKeyValue();
		};

		@Override
		protected String getResKeyValue(RecordRegistrationResponse responseRec) {
			return responseRec.getImportKeyValue();
		};

		@Override
		protected boolean isResNormal(RecordRegistrationResponse responseRec) {
			return (responseRec.getResult() == ResultEnum.NORMAL) ;
		};

		@Override
		protected boolean isResSkip(RecordRegistrationResponse responseRec) {
			return (responseRec.getResult() == ResultEnum.SKIP) ;
		};

		@Override
		protected ImportJobLinkRcvResponse callImportWrapper(List<ImportJobLinkRcvRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportJobLinkRcvRequest reqDto = new ImportJobLinkRcvRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importJobLinkRcv(reqDto);
		}

		@Override
		protected String getRestExceptionMessage(RecordRegistrationResponse responseRec) {
			if (responseRec.getExceptionInfo() != null) {
				return responseRec.getExceptionInfo().getException() +":"+ responseRec.getExceptionInfo().getMessage();
			}
			return null;
		};

		@Override
		protected void setResultLog( RecordRegistrationResponse responseRec ){
			String keyValue = getResKeyValue(responseRec);
			if ( isResNormal(responseRec) ) {
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : "+ this.importInfoName + ":" + keyValue);
			} else if(isResSkip(responseRec)){
				log.info(Messages.getString("SettingTools.SkipSystemRole") + " : " + this.importInfoName + ":" + keyValue);
			} else {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : "+ this.importInfoName + ":" + keyValue + " : "
						+ HinemosMessage.replace(getRestExceptionMessage(responseRec)));
			}
		}
	}
}
