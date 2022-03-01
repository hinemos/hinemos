/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.jobutil.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import org.openapitools.client.model.JobNextJobOrderInfoResponse;

import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestLoginManager;
import com.clustercontrol.utility.settings.ExportMethod;
import com.clustercontrol.utility.settings.ImportMethod;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.job.conv.MasterConv;
import com.clustercontrol.utility.settings.job.xml.JobMasterDataList;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.conv.CommonConv;
import com.clustercontrol.utility.settings.ui.action.CommandAction;
import com.clustercontrol.utility.settings.ui.action.CommandAction.AccountInfo;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.IUtilityPreferenceStore;
import com.clustercontrol.utility.util.UtilityPreferenceStore;
import com.clustercontrol.utility.util.XmlMarshallUtil;

public class JobConvert {
	
	public static Logger log = Logger.getLogger(JobConvert.class);

	public static String preImportJobCheck(JobTreeItemWrapper selectJob, JobTreeItemWrapper importJob) {
		// インポート対象のジョブがジョブユニットの場合
		if (importJob.getData().getType().equals(JobInfoWrapper.TypeEnum.JOBUNIT)) {
			// インポート先として選択している対象はマネージャだけOK
			if (!selectJob.getData().getType().equals(JobInfoWrapper.TypeEnum.MANAGER)) {
				String errorMsg = Messages.getString("message.job.import.select.fail",
						new String[]{
								JobStringUtil.toJobTypeStringForEnum(selectJob.getData().getType()),
								selectJob.getData().getId(),
								JobStringUtil.toJobTypeStringForEnum(importJob.getData().getType()),
								importJob.getData().getId()});
				
				log.warn(errorMsg);
				return errorMsg;
			}
		// インポート対象のジョブがジョブユニット以外の場合
		} else {
			if (selectJob.getData().getType().equals(JobInfoWrapper.TypeEnum.MANAGER)) {
				String errorMsg = Messages.getString("message.job.import.select.fail",
						new String[]{
								JobStringUtil.toJobTypeStringForEnum(selectJob.getData().getType()),
								selectJob.getData().getId(),
								JobStringUtil.toJobTypeStringForEnum(importJob.getData().getType()),
								importJob.getData().getId()});
				
				log.warn(errorMsg);
				return errorMsg;
			} else {
				if (importJob.getData().getParam() != null && importJob.getData().getParam().size() != 0) {
					String errorMsg = Messages.getString("message.job.import.select.fail",
							new String[]{
									JobStringUtil.toJobTypeStringForEnum(selectJob.getData().getType()),
									selectJob.getData().getId(),
									JobStringUtil.toJobTypeStringForEnum(importJob.getData().getType()),
									importJob.getData().getId()});
					
					log.warn(errorMsg);
					return errorMsg;
				}
			}
		}
		// ジョブ変数はジョブユニットに対してのみ設定可能
		String errorMsg = checkChildParams(importJob);
		if (errorMsg.equals("")) {
			return errorMsg;
		}
		return "";
	}
	
	/**
	 * 親ジョブ配下の子ジョブにジョブ変数が設定されていないか</BR>
	 * 再帰的にチェックする
	 * 
	 * @param importJob(親ジョブ)
	 * @return チェック結果
	 */
	private static String checkChildParams(JobTreeItemWrapper importJob){
			List<JobTreeItemWrapper> childJobsList = importJob.getChildren();
			String errorMsg;
			if (childJobsList != null && childJobsList.size() != 0) {
				for (JobTreeItemWrapper childjob : childJobsList) {
					if (childjob.getData().getParam() != null && childjob.getData().getParam().size() != 0) {
						errorMsg = Messages.getString("message.job.import.select.fail",
								new String[]{
										JobStringUtil.toJobTypeStringForEnum(importJob.getData().getType()),
										importJob.getData().getId(),
										JobStringUtil.toJobTypeStringForEnum(childjob.getData().getType()),
										childjob.getData().getId()});
						
						log.warn(errorMsg);
						return errorMsg;
					}else{
						errorMsg=checkChildParams(childjob);
						if (errorMsg.equals("")) {
							return errorMsg;
						}
					}
				}
			}
		return "";
	}
	
	@ImportMethod
	public static Integer importJob(JobTreeItemWrapper selection, JobTreeItemWrapper importJob) {
		// インポート対象のジョブがジョブユニットの場合
		if (importJob.getData().getType().equals(JobInfoWrapper.TypeEnum.JOBUNIT)) {
			// インポート先として選択している対象はマネージャだけOK
			if (!selection.getData().getType().equals(JobInfoWrapper.TypeEnum.MANAGER)) {
				log.warn(Messages.getString("message.job.import.select.fail",
						new String[]{
								JobStringUtil.toJobTypeStringForEnum(selection.getData().getType()),
								selection.getData().getId(),
								JobStringUtil.toJobTypeStringForEnum(importJob.getData().getType()),
								importJob.getData().getId()}
						));
				return SettingConstants.ERROR_INPROCESS;
			}
			setJobUnitId(importJob, importJob);
			// インポート対象のジョブがジョブネットの場合
		} else {
			if (selection.getData().getType().equals(JobInfoWrapper.TypeEnum.MANAGER)) {
				log.warn(Messages.getString("message.job.import.select.fail",
						new String[]{
								JobStringUtil.toJobTypeStringForEnum(selection.getData().getType()),
								selection.getData().getId(),
								JobStringUtil.toJobTypeStringForEnum(importJob.getData().getType()),
								importJob.getData().getId()}
						));
				return SettingConstants.ERROR_INPROCESS;
			}
			setJobUnitId(selection, importJob);
		}
		JobTreeItemUtil.addChildren(selection, importJob);
		return SettingConstants.SUCCESS;
	}
	
	public static List<JobTreeItemWrapper> convertJobTreeItem(String fileName, boolean scope, boolean notify) {
		JobTreeItemWrapper item;
		try {
			JobMasterDataList jobXML;
			try (FileInputStream input = new FileInputStream(fileName)) {
				
				jobXML = XmlMarshallUtil.unmarshall(JobMasterDataList.class,
						new InputStreamReader(input, "UTF-8"));
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				log.debug("End Import JobMaster (Error)");
				return null;
			}
			
			/* スキーマのバージョンチェック*/
			if (!checkSchemaVersion(jobXML.getSchemaInfo())) {
				
				return null;
			}
			
			item = MasterConv.masterXml2Dto(jobXML.getJobInfo(), getTopJobIdList(jobXML.getJobInfo()));
		} catch (Exception e) {
			log.warn(Messages.getString("SettingTools.ImportFailed"), e);
			return null;
		}
		log.debug("End Convert JobMaster : " + fileName);
		
		//設定書き換え
		List<JobTreeItemWrapper> importTopJobs = new ArrayList<>();
		for (JobTreeItemWrapper importTop : item.getChildren()) {
			changeSetImportJob(importTop, scope, notify);
			importTopJobs.add(importTop);
		}
		return importTopJobs;
	}
	
	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private static boolean checkSchemaVersion(com.clustercontrol.utility.settings.job.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = MasterConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.job.xml.SchemaInfo sci = MasterConv.getSchemaVersion();
		
		return BaseAction.checkSchemaVersionResult(log, res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	@ExportMethod
	public static int exportJobXML(com.clustercontrol.utility.settings.job.xml.JobMasters jobXML, String fileName) {
		int ret = SettingConstants.SUCCESS;

		try (FileOutputStream output = new FileOutputStream(fileName)) {
			//マーシャリング
			jobXML.marshal(new OutputStreamWriter(output, "UTF-8"));
		} catch (Exception e) {
			log.warn(Messages.getString("SettingTools.ExportFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;

			File file = new File(fileName);
			if (file.exists()) {
				if (!file.delete())
					log.warn(String.format("File can not be deleted. file name=%s", fileName));
			}
		}

		// 処理の終了
		if (ret == SettingConstants.SUCCESS) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}

		log.debug("End Export JobMaster : " + fileName);
		return ret;
	}

	/**
	 * 指定ジョブのジョブユニットを反映する
	 * @param parentJob 指定ジョブ
	 * @param importJob 反映対象
	 */
	public static void setJobUnitId(JobTreeItemWrapper parentJob, JobTreeItemWrapper importJob) {
		importJob.getData().setParentId(parentJob.getData().getId());
		importJob.getData().setJobunitId(parentJob.getData().getJobunitId());
		if (importJob.getData().getType() != JobInfoWrapper.TypeEnum.JOBUNIT)
			importJob.getData().setOwnerRoleId(null);
		
		if (importJob.getData().getWaitRule() != null && !importJob.getData().getWaitRule().getExclusiveBranchNextJobOrderList().isEmpty()) {
			for (JobNextJobOrderInfoResponse nextJob : importJob.getData().getWaitRule().getExclusiveBranchNextJobOrderList()) {
				nextJob.setJobunitId(parentJob.getData().getJobunitId());
			}
		}
		
		setChildJobUnitId(parentJob, importJob);
	}
	
	private static void setChildJobUnitId(JobTreeItemWrapper parentJob, JobTreeItemWrapper importJob) {
		for(JobTreeItemWrapper child : importJob.getChildren()){
			child.getData().setJobunitId(parentJob.getData().getJobunitId());
			child.setParent(importJob);
			child.getData().setOwnerRoleId(null);
			if (child.getData().getType().equals(JobInfoWrapper.TypeEnum.REFERJOB)) {
				child.getData().setReferJobUnitId(parentJob.getData().getJobunitId());
			}
			if (child.getData().getWaitRule() != null && !child.getData().getWaitRule().getExclusiveBranchNextJobOrderList().isEmpty()) {
				for (JobNextJobOrderInfoResponse nextJob : child.getData().getWaitRule().getExclusiveBranchNextJobOrderList()) {
					nextJob.setJobunitId(parentJob.getData().getJobunitId());
				}
			}
			setChildJobUnitId(parentJob, child);
		}
	}
	
	public static com.clustercontrol.utility.settings.job.xml.JobMasters convertJobMastersXML(JobTreeItemWrapper jti, boolean scope, boolean notify) throws NullPointerException, ParseException {
		List<JobInfoWrapper> jobList = changeSetExportJob(jti, scope, notify);
		
		AccountInfo accountInfo = new CommandAction().getCurrentAccountInfo();
		assert accountInfo != null : "unexpected";
		log.debug(accountInfo.userid + ", " + accountInfo.url);
		
		Config.putConfig("Login.URL", accountInfo.url);
		Config.putConfig("Login.USER", accountInfo.userid);
		IUtilityPreferenceStore clientStore = UtilityPreferenceStore.get();
		Config.putConfig("HTTP.CONNECT.TIMEOUT", Integer.toString(clientStore.getInt(RestLoginManager.KEY_HTTP_REQUEST_TIMEOUT)));
		Config.putConfig("HTTP.REQUEST.TIMEOUT", Integer.toString(clientStore.getInt(RestLoginManager.KEY_HTTP_REQUEST_TIMEOUT)));
		
		com.clustercontrol.utility.settings.job.xml.JobMasters masterXML = new com.clustercontrol.utility.settings.job.xml.JobMasters();

		//共通情報のセット
		masterXML.setCommon(CommonConv.versionJobDto2Xml(Config.getVersion()));

		//スキーマ情報のセット
		masterXML.setSchemaInfo(MasterConv.getSchemaVersion());

		//ジョブ情報のセット(ジョブツリーからXML用のリストに変換)
		masterXML.setJobInfo(convertJobInfoXMLs(jobList));
		return masterXML;
	}


	public static com.clustercontrol.utility.settings.job.xml.JobInfo[] convertJobInfoXMLs(List<JobInfoWrapper> jobList) throws NullPointerException, ParseException {
		com.clustercontrol.utility.settings.job.xml.JobInfo[] jobs =
				new com.clustercontrol.utility.settings.job.xml.JobInfo[jobList.size()];

		int i = 0;
		for(JobInfoWrapper job : jobList){
			//データを生成
			jobs[i++] = MasterConv.setXMLJobData(job, job.getParentId());
		}
		return jobs;
	}

	public static List<JobInfoWrapper> changeSetExportJob(JobTreeItemWrapper topJob, boolean scope, boolean notify) {
		List<JobInfoWrapper> jobList = new ArrayList<>();

		JobInfoWrapper job = JobTreeItemUtil.clone(topJob, topJob).getData();
		//詳細情報が必要なのでsetJobFull実行
		String managerName = null;
		JobTreeItemWrapper mgrTree = JobTreeItemUtil.getManager(topJob);
		if(mgrTree == null) {
			managerName = topJob.getChildren().get(0).getData().getId();
		} else {
			managerName = mgrTree.getData().getId();
		}
		JobPropertyUtil.setJobFull(managerName, job);
		
		// jobNet が先頭だったら、unit の情報を上書き、jobNet を TOP にする
		if (topJob.getData().getType() != JobInfoWrapper.TypeEnum.JOBUNIT) {
			job.setParentId("TOP");
			job.setJobunitId(job.getId());
			job.setOwnerRoleId(getJobUnit(topJob).getData().getOwnerRoleId());
		}
		if (notify) {
			job.getNotifyRelationInfos().clear();
		}
		// 編集モード中に作成されたジョブネットは更新日時が入ってこないため、現時刻を入れておく
		if (job.getUpdateTime() == null) {
			job.setUpdateTime(new Date().toString());
		}
		
		jobList.add(job);
		
		changeSetChildExportJob(jobList, managerName ,job, topJob, scope, notify);
		return jobList;
	}

	private static void changeSetChildExportJob(List<JobInfoWrapper> jobList, String managerName, JobInfoWrapper topJob, JobTreeItemWrapper parentJob, boolean scope, boolean notify){
		for (JobTreeItemWrapper child : parentJob.getChildren()) {
			JobInfoWrapper job = JobTreeItemUtil.clone(child, parentJob).getData();
			//詳細情報が必要なのでsetJobFull実行
			JobPropertyUtil.setJobFull(managerName, job);
			
			job.setParentId(parentJob.getData().getId());
			job.setJobunitId(topJob.getId());
			job.setOwnerRoleId(topJob.getOwnerRoleId());
			
			if (scope) {
				if( job.getType() == JobInfoWrapper.TypeEnum.MONITORJOB ){ 
					job.getMonitor().setFacilityID("");
				}else if(job.getType() == JobInfoWrapper.TypeEnum.JOB){
					job.getCommand().setFacilityID("");
				}else if(job.getType() == JobInfoWrapper.TypeEnum.FILEJOB){
					job.getFile().setSrcScope("");
					job.getFile().setSrcFacilityID("");
					job.getFile().setDestFacilityID("");
				}else if(job.getType() == JobInfoWrapper.TypeEnum.RESOURCEJOB){
					job.getResource().setResourceNotifyScope("");
				}else if( job.getType() == JobInfoWrapper.TypeEnum.APPROVALJOB || job.getType() == JobInfoWrapper.TypeEnum.REFERJOB){
					//処理なし
					// findbugs対応 ログ出力を追加
					log.trace("changeSetChildExportJob() : job.getType() = APPROVALJOB or REFERJOB");
				}else{
					//処理なし
				}
			}
			if (notify) {
				job.getNotifyRelationInfos().clear();
			}
			// 編集モード中に作成されたジョブは更新日時が入ってこないため、現時刻を入れておく
			if (job.getUpdateTime() == null) {
				job.setUpdateTime(new Date().toString());
			}
			jobList.add(job);

			// Add children items to list recursively
			changeSetChildExportJob(jobList, managerName, topJob, child, scope, notify);
		}
	}
	
	public static List<JobInfoWrapper> changeSetImportJob(JobTreeItemWrapper topJob, boolean scope, boolean notify) {
		List<JobInfoWrapper> jobList = new ArrayList<>();

		JobInfoWrapper job = topJob.getData();
		
		if (notify) {
			job.getNotifyRelationInfos().clear();
		}

		jobList.add(job);

		changeSetChildImportJob(jobList, topJob, topJob, scope, notify);
		return jobList;
	}
	
	private static void changeSetChildImportJob(List<JobInfoWrapper> jobList, JobTreeItemWrapper topJob, JobTreeItemWrapper parentJob, boolean scope, boolean notify){
		for (JobTreeItemWrapper child : parentJob.getChildren()) {
			JobInfoWrapper job = child.getData();
			
			job.setParentId(parentJob.getData().getId());
			job.setJobunitId(topJob.getData().getId());
			
			if (scope) {
				if( job.getType() == JobInfoWrapper.TypeEnum.MONITORJOB ){ 
					job.getMonitor().setFacilityID("");
				}else if(job.getType() == JobInfoWrapper.TypeEnum.JOB){
					job.getCommand().setFacilityID("");
				}else if(job.getType() == JobInfoWrapper.TypeEnum.FILEJOB){
					job.getFile().setSrcScope("");
					job.getFile().setSrcFacilityID("");
					job.getFile().setDestFacilityID("");
				}else if(job.getType() == JobInfoWrapper.TypeEnum.RESOURCEJOB){
					job.getResource().setResourceNotifyScope("");
				}else if( job.getType() == JobInfoWrapper.TypeEnum.APPROVALJOB || job.getType() == JobInfoWrapper.TypeEnum.REFERJOB){
					//処理なし
					// findbugs対応 ログ出力を追加
					log.trace("changeSetChildImportJob() : job.getType() = APPROVALJOB or REFERJOB");
				}else{
					//処理なし
				}
			} else {
				Map<String, String> map = JobStringUtil.getScopeMap();
				if( job.getType() == JobInfoWrapper.TypeEnum.MONITORJOB ){ 
					job.getMonitor().setScope(map.get(job.getMonitor().getFacilityID()) != null ? map.get(job.getMonitor().getFacilityID()) : "");
				}else if(job.getType() == JobInfoWrapper.TypeEnum.JOB){
					job.getCommand().setScope(map.get(job.getCommand().getFacilityID()) != null ? map.get(job.getCommand().getFacilityID()) : "");
				}else if(job.getType() == JobInfoWrapper.TypeEnum.FILEJOB){
					job.getFile().setSrcScope(map.get(job.getFile().getSrcFacilityID()) != null ? map.get(job.getFile().getSrcFacilityID()) : "");
					job.getFile().setDestScope(map.get(job.getFile().getDestFacilityID()) != null ? map.get(job.getFile().getDestFacilityID()) : "");
				}else if(job.getType() == JobInfoWrapper.TypeEnum.RESOURCEJOB){
					job.getResource().setResourceNotifyScopePath(map.get(job.getResource().getResourceNotifyScope()) != null ? map.get(job.getResource().getResourceNotifyScope()) : "");
				}else if( job.getType() == JobInfoWrapper.TypeEnum.APPROVALJOB || job.getType() == JobInfoWrapper.TypeEnum.REFERJOB){
					//処理なし
					// findbugs対応 ログ出力を追加
					log.trace("changeSetChildImportJob() : job.getType() = APPROVALJOB or REFERJOB");
				}else{
					//処理なし
				}
			}
			
			if (notify) {
				job.getNotifyRelationInfos().clear();
			}

			jobList.add(job);

			// Add children items to list recursively
			changeSetChildImportJob(jobList, topJob, child, scope, notify);
		}
	}
	
	private static ArrayList<String> getTopJobIdList(com.clustercontrol.utility.settings.job.xml.JobInfo[] infos) {
		ArrayList<String> list = new ArrayList<>();
		for (com.clustercontrol.utility.settings.job.xml.JobInfo info : infos) {
			if (info.getParentJobId().equals("TOP")) {
				list.add(info.getId());
			}
		}
		if (list.isEmpty())
			throw new IllegalArgumentException("not find parent job.");
		
		return list;
	}
	
	public static JobTreeItemWrapper getJobUnit(JobTreeItemWrapper item) {
		if (item.getData().getType().equals(JobInfoWrapper.TypeEnum.JOBUNIT))
			return item;
		
		return getJobUnit(item.getParent());
	}
}