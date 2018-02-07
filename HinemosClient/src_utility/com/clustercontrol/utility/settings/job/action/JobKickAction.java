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
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.jobmanagement.bean.JobKickConstant;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
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
import com.clustercontrol.utility.settings.job.xml.ManualInfo;
import com.clustercontrol.utility.settings.job.xml.ManualList;
import com.clustercontrol.utility.settings.job.xml.ScheduleInfo;
import com.clustercontrol.utility.settings.job.xml.ScheduleList;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.platform.conv.CommonConv;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.ImportProcessDialog;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.ws.jobmanagement.HinemosUnknown_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidSetting_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidUserPass_Exception;
import com.clustercontrol.ws.jobmanagement.JobFileCheck;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobKick;
import com.clustercontrol.ws.jobmanagement.JobKickDuplicate_Exception;
import com.clustercontrol.ws.jobmanagement.JobSchedule;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;


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
	public int importJobSchedule(String xmlSchedule, String xmlFileCheck, String xmlManual){

		log.debug("Start Import JobKick");

		if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
			log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			log.debug("End Import JobKick (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		int ret=0;
		JobSchedule schedule = null;
		JobFileCheck fileCheck = null;
		JobKick manual = null;

		//スケジュール情報のXMからBeanに取り込みます。
		ScheduleList scheduleList=null;
		try {
			scheduleList = ScheduleList.unmarshal(new InputStreamReader(
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
			fileCheckList = FileCheckList.unmarshal(new InputStreamReader(
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
			//manualList = ManualList.unmarshal(new InputStreamReader(new FileInputStream(xmlManual), "UTF-8"));
			try (FileInputStream fis = new FileInputStream(xmlManual);
					InputStreamReader isr = new InputStreamReader(fis, "UTF-8");) {
				manualList = ManualList.unmarshal(isr);
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
		
		///////////////////////////////////////////////////////////////////
		// ログインユーザで参照可能なジョブユニットを取得するメソッドをマネージャ側に用意し、
		// この部分の実装は修正する
		// treeOnly = trueの場合は、ログインユーザで参照可能なジョブユニットと 配下のジョブが取れる
		JobTreeItem jobTreeItem = null;
		try {
			jobTreeItem = JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getJobTree(null, true);
		} catch (Exception e) {
			log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret=SettingConstants.ERROR_INPROCESS;
		}

		List<String> objectIdList = new ArrayList<String>();
		String kickId = null;
		for (int i = 0; i < scheduleList.getScheduleInfoCount(); i++) {
			try {
				//XMLからDTOに変換
				schedule = kickConv.scheduleXml2Dto(scheduleList.getScheduleInfo(i));
				kickId = schedule.getId();
				String jobunitId = schedule.getJobunitId();

				log.debug("schedule jobunitId : " + jobunitId);

				// ジョブユニットのリスト
				List<JobTreeItem> jobunitList = jobTreeItem.getChildren().get(0).getChildren();
				
				boolean retImport = false;
				for (JobTreeItem jobunit : jobunitList) {
					JobInfo jobinfo = jobunit.getData();
					log.debug("jobunitId : " + jobinfo.getJobunitId());

					// スケジュールに設定されたjobunitIdとマッチした場合、
					// ログインユーザで参照可能なスケジュール設定ということなので、登録する
					if (jobunitId.equals(jobinfo.getJobunitId())) {
						JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).addSchedule(schedule);
						objectIdList.add(kickId);
						log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + kickId);
						retImport = true;
					}
				}
				if(!retImport){
					log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + kickId + " "
							+ Messages.getString("SettingTools.InvalidJobId"));
					ret = SettingConstants.ERROR_INPROCESS;
				}
			} catch (JobKickDuplicate_Exception e) {
				//重複時、インポート処理方法を確認する
				if(!ImportProcessMode.isSameprocess()){
					String[] args = {kickId};
					ImportProcessDialog dialog = new ImportProcessDialog(
							null, Messages.getString("message.import.confirm2", args));
					ImportProcessMode.setProcesstype(dialog.open());
					ImportProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				if(ImportProcessMode.getProcesstype() == ImportProcessDialog.UPDATE){
					try {
						JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).modifySchedule(schedule);
						objectIdList.add(kickId);
						log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + kickId);
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
				} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.SKIP){
					log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + kickId);
				} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
					log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
					ret = SettingConstants.ERROR_INPROCESS;
					return ret;
				}
			} catch (HinemosUnknown_Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidRole_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidUserPass_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidSetting_Exception e) {
				log.warn(Messages.getString("SettingTools.InvalidSetting") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			}
		}
		
		for (int i = 0; i < fileCheckList.getFileCheckInfoCount(); i++) {
			try {
				//XMLからDTOに変換
				fileCheck = kickConv.fileCheckXml2Dto(fileCheckList.getFileCheckInfo(i));
				kickId = fileCheck.getId();
				String jobunitId = fileCheck.getJobunitId();

				log.debug("schedule jobunitId : " + jobunitId);

				// ジョブユニットのリスト
				List<JobTreeItem> jobunitList = jobTreeItem.getChildren().get(0).getChildren();
				
				boolean retImport = false;
				for (JobTreeItem jobunit : jobunitList) {
					JobInfo jobinfo = jobunit.getData();
					log.debug("jobunitId : " + jobinfo.getJobunitId());

					// ファイルチェックに設定されたjobunitIdとマッチした場合、
					// ログインユーザで参照可能なファイルチェック設定ということなので、登録する
					if (jobunitId.equals(jobinfo.getJobunitId())) {
						JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).addFileCheck(fileCheck);
						objectIdList.add(kickId);
						log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + kickId);
						retImport = true;
					}
				}
				if(!retImport){
					log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + kickId + " "
							+ Messages.getString("SettingTools.InvalidJobId"));
					ret = SettingConstants.ERROR_INPROCESS;
				}
			} catch (JobKickDuplicate_Exception e) {
				//重複時、インポート処理方法を確認する
				if(!ImportProcessMode.isSameprocess()){
					String[] args = {kickId};
					ImportProcessDialog dialog = new ImportProcessDialog(
							null, Messages.getString("message.import.confirm2", args));
					ImportProcessMode.setProcesstype(dialog.open());
					ImportProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				if(ImportProcessMode.getProcesstype() == ImportProcessDialog.UPDATE){
					try {
						JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).modifyFileCheck(fileCheck);
						objectIdList.add(kickId);
						log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + kickId);
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
				} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.SKIP){
					log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + kickId);
				} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
					log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
					ret = SettingConstants.ERROR_INPROCESS;
					return ret;
				}
			} catch (HinemosUnknown_Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidRole_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidUserPass_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidSetting_Exception e) {
				log.warn(Messages.getString("SettingTools.InvalidSetting") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}

		/////////////////////////////////////
		// マニュアル契機のインポート処理
		/////////////////////////////////////
		for (int i = 0; i < manualList.getManualInfoCount(); i++) {
			try {
				// XMLからDTOに変換
				manual = kickConv.manualXml2Dto(manualList.getManualInfo(i));
				kickId = manual.getId();
				String jobunitId = manual.getJobunitId();

				log.debug("schedule jobunitId : " + jobunitId);

				// ジョブユニットのリスト
				List<JobTreeItem> jobunitList = jobTreeItem.getChildren().get(0).getChildren();

				boolean retImport = false;
				for (JobTreeItem jobunit : jobunitList) {
					JobInfo jobinfo = jobunit.getData();
					log.debug("jobunitId : " + jobinfo.getJobunitId());

					// マニュアルに設定されたjobunitIdとマッチした場合、
					// ログインユーザで参照可能なマニュアル設定ということなので、登録する
					if (jobunitId.equals(jobinfo.getJobunitId())) {
						JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName())
								.addJobManual(manual);
						objectIdList.add(kickId);
						log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + kickId);
						retImport = true;
					}
				}
				if (!retImport) {
					log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + kickId + " "
							+ Messages.getString("SettingTools.InvalidJobId"));
					ret = SettingConstants.ERROR_INPROCESS;
				}
			} catch (JobKickDuplicate_Exception e) {
				// 重複時、インポート処理方法を確認する
				if (!ImportProcessMode.isSameprocess()) {
					String[] args = { kickId };
					ImportProcessDialog dialog = new ImportProcessDialog(null,
							Messages.getString("message.import.confirm2", args));
					ImportProcessMode.setProcesstype(dialog.open());
					ImportProcessMode.setSameprocess(dialog.getToggleState());
				}

				if (ImportProcessMode.getProcesstype() == ImportProcessDialog.UPDATE) {
					try {
						JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName())
								.modifyJobManual(manual);
						objectIdList.add(kickId);
						log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + kickId);
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
				} else if (ImportProcessMode.getProcesstype() == ImportProcessDialog.SKIP) {
					log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + kickId);
				} else if (ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL) {
					log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
					ret = SettingConstants.ERROR_INPROCESS;
					return ret;
				}
			} catch (HinemosUnknown_Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidRole_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidUserPass_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidSetting_Exception e) {
				log.warn(Messages.getString("SettingTools.InvalidSetting") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		//オブジェクト権限同時インポート
		importObjectPrivilege(HinemosModuleConstant.JOB_KICK, objectIdList);
		
		//差分削除
		checkDelete(scheduleList, fileCheckList, manualList);
		
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
	 */
	@ExportMethod
	public  int exportJobSchedule(String xmlSchedule, String xmlFileCheck, String xmlManual){

		log.debug("Start Export JobKick");

		int ret = 0;
		//マネージャからスケジュールのリストを取得する。
		//List<JobKick> kickList = new ArrayList<JobKick>() ;
		List<JobKick> kickList = null;
		try {
			kickList = JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getJobKickList();
			if (null == kickList){
				log.error(Messages.getString("SettingTools.EndWithErrorCode") );
				return SettingConstants.ERROR_INPROCESS;
			}
			Collections.sort(kickList, new Comparator<JobKick>() {
				@Override
				public int compare(JobKick kick1, JobKick kick2) {
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
		
		
		// スケジュール１つ分
		for (JobKick kick : kickList) {
			if (kick instanceof JobSchedule) {
				JobSchedule schedule = (JobSchedule) kick;
				scheduleList.addScheduleInfo(kickConv.scheduleDto2Xml(schedule));
				log.info(String.format("%s : %s", Messages.getString("SettingTools.ExportSucceeded"), kick.getId()));
			} else if (kick instanceof JobFileCheck) {
				JobFileCheck fileCheck = (JobFileCheck) kick;
				fileCheckList.addFileCheckInfo(kickConv.fileCheckDto2Xml(fileCheck));
				log.info(String.format("%s : %s", Messages.getString("SettingTools.ExportSucceeded"), kick.getId()));
			} else if (kick instanceof JobKick) {
				manualList.addManualInfo(kickConv.manualDto2Xml(kick));
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
		List<JobKick> kickList =null;
		try {
			kickList = JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getJobKickList();
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
			for (JobKick kick : kickList) {
				//順番に実行契機IDを取得し、
				kickId= kick.getId();
				if (kick.getType() == JobKickConstant.TYPE_SCHEDULE) {
					scheduleList.add(kickId);
				} else if (kick.getType() == JobKickConstant.TYPE_FILECHECK) {
					fileCheckList.add(kickId);
				} else if (kick.getType() == JobKickConstant.TYPE_MANUAL) {
					manualList.add(kickId);
				}
			}

			//順番に消し込みを行う。
			List<String> currentList = Collections.emptyList();
			try {
				currentList = scheduleList;
				JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).deleteSchedule(currentList);
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + currentList.toString());

				currentList = fileCheckList;
				JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).deleteFileCheck(currentList);
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + currentList.toString());

				currentList = manualList;
				JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).deleteJobManual(currentList);
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + currentList.toString());
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
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
	public int diffXml(String xmlSchedule1, String xmlFileCheck1, String xmlManual1, String xmlSchedule2, String xmlFileCheck2, String xmlManual2)
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

		// XMLファイルからの読み込み
		try {
			scheduleList1 = ScheduleList.unmarshal(new InputStreamReader(new FileInputStream(xmlSchedule1), "UTF-8"));
			scheduleList2 = ScheduleList.unmarshal(new InputStreamReader(new FileInputStream(xmlSchedule2), "UTF-8"));
			fileCheckList1 = FileCheckList.unmarshal(new InputStreamReader(new FileInputStream(xmlFileCheck1), "UTF-8"));
			fileCheckList2 = FileCheckList.unmarshal(new InputStreamReader(new FileInputStream(xmlFileCheck2), "UTF-8"));
			manualList1 = ManualList.unmarshal(new InputStreamReader(new FileInputStream(xmlManual1), "UTF-8"));
			manualList2 = ManualList.unmarshal(new InputStreamReader(new FileInputStream(xmlManual2), "UTF-8"));
			sort(manualList1);
			sort(manualList2);
			sort(scheduleList1);
			sort(scheduleList2);
			sort(fileCheckList1);
			sort(fileCheckList2);
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
	
//	public Logger log {
//		return log;
//	}
	
	protected void checkDelete(ScheduleList xmlElements1, FileCheckList xmlElements2, ManualList xmlElements3){
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
		
		List<com.clustercontrol.ws.jobmanagement.JobKick> subList = null;
		try {
			subList = JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getJobKickList();
		}
		catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			log.debug(e.getMessage(), e);
		}
		
		if(subList == null || subList.size() <= 0){
			return;
		}
		
		for(com.clustercontrol.ws.jobmanagement.JobKick mgrInfo: new ArrayList<>(subList)){
			for(String xmlElement: new ArrayList<>(jobKickIds)){
				if(mgrInfo.getId().equals(xmlElement)){
					subList.remove(mgrInfo);
					jobKickIds.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(com.clustercontrol.ws.jobmanagement.JobKick info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getId()};
					DeleteProcessDialog dialog = new DeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.DELETE){
					try {
						List<String> args = new ArrayList<>();
						args.add(info.getId());
						if(info.getType() == JobKickConstant.TYPE_SCHEDULE){
							JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).deleteSchedule(args);
						} else if (info.getType() == JobKickConstant.TYPE_FILECHECK){
							JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).deleteFileCheck(args);
						} else if (info.getType() == JobKickConstant.TYPE_MANUAL){
							JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).deleteJobManual(args);
						}
						log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.SKIP){
					log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getId());
				} else if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.CANCEL){
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
}
