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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.EditLockResponse;
import org.openapitools.client.model.GetEditLockRequest;
import org.openapitools.client.model.GetJobFullListRequest;
import org.openapitools.client.model.HasSystemPrivilegeRequest;
import org.openapitools.client.model.ImportJobMasterRecordRequest;
import org.openapitools.client.model.ImportJobMasterRequest;
import org.openapitools.client.model.ImportJobMasterResponse;
import org.openapitools.client.model.JobInfoRequestP1;
import org.openapitools.client.model.JobInfoResponse;
import org.openapitools.client.model.JobTreeItemResponseP1;

import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import org.openapitools.client.model.JobTreeItemResponseP2;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.SystemPrivilegeInfoRequestP1;
import org.openapitools.client.model.SystemPrivilegeInfoRequestP1.SystemFunctionEnum;
import org.openapitools.client.model.SystemPrivilegeInfoRequestP1.SystemPrivilegeEnum;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;

import com.clustercontrol.accesscontrol.util.AccessRestClientWrapper;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.JobInvalid;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.OtherUserGetLock;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.UpdateTimeNotLatest;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;
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
import com.clustercontrol.utility.settings.job.conv.MasterConv;
import com.clustercontrol.utility.settings.job.xml.JobInfo;
import com.clustercontrol.utility.settings.job.xml.JobMasterDataList;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.platform.conv.CommonConv;
import com.clustercontrol.utility.settings.ui.util.CommmandCallMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.ImportClientController;
import com.clustercontrol.utility.util.ImportRecordConfirmer;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;
import com.clustercontrol.utility.util.XmlMarshallUtil;
/**
 * ジョブマスタ情報を処理するためのアクションクラスです。<br>
 * マネージャに接続し、ジョブマスタの取得、設定、削除をします。<br>
 * XMLファイルに定義されたジョブマスタ情報を取得します。<br>
 * 
 * @param action 動作
 * @param XMLファイルパス（ユーザ情報定義の入力元）
 * 
 * @version 6.1.0
 * @since 1.0.0
 */
public class JobMasterAction {
	// ロガー
	private static Logger log = Logger.getLogger(JobMasterAction.class);

	/**
	 * コンストラクタ
	 * 
	 */
	public JobMasterAction(){
	}

	/**
	 * ジョブ設定情報を削除します。<BR>
	 * 
	 * @since 1.0
	 * 
	 * @param idList 削除対象となるジョブユニットIDのリスト
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearMaster(ArrayList<String> idList){
		log.debug("Start Clear JobMaster :" );

		int ret=0;
		
		// ジョブの編集モード取得に必要なシステム権限チェックを行う
		try {
			hasSystemPrivilegeForGetEditLock();
		} catch (Exception e) {
			log.info(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}

		Map<String, Long> jobUnitUpdateTimeMap = getJobUnitUpdateTimeMap(idList);
		Iterator<String> itr = idList.iterator();
		String jobunitId = null;
		Long updateTime = null;
		String managerName = UtilityManagerUtil.getCurrentManagerName();
		Integer editSession = null;
		while(itr.hasNext()) {
			try {
				jobunitId = itr.next();
				updateTime = jobUnitUpdateTimeMap.get(jobunitId);

				// 削除時はジョブユニットの編集ロックを取得する。
				editSession = getEditLock(managerName, jobunitId, updateTime);
				if (editSession == null) {
					ret = SettingConstants.ERROR_INPROCESS;
				}else{
					JobRestClientWrapper.getWrapper(managerName).deleteJobunit(jobunitId);
					log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + jobunitId);
				}
			} catch (HinemosUnknown e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				releaseEditLock(managerName, jobunitId, editSession);
			} catch (InvalidRole e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				releaseEditLock(managerName, jobunitId, editSession);
			} catch (InvalidUserPass e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				releaseEditLock(managerName, jobunitId, editSession);
			} catch (OtherUserGetLock e) {
				log.error(Messages.getString("SettingTools.CannotGetLockToClearJob") + " : " + jobunitId);
				ret = SettingConstants.ERROR_INPROCESS;				
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				releaseEditLock(managerName, jobunitId, editSession);
			}
		}

		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ClearCompleted"));
		}else{
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
		}

		log.debug("End Clear JobMaster : " );

		return ret;
	}

	/**
	 * 
	 * ジョブ情報をマネージャに投入します。
	 * 
	 * @param fileList インポート処理で使用するXMLファイルのリスト
	 * @param jobunitList インポート対象となるジョブユニットIDのリスト
	 * @return
	 */
	@ImportMethod
	public int importMaster(ArrayList<String> fileList, ArrayList<String> jobunitList) {
		
		// ジョブの場合、ファイル名はひとつ
		String fileName = fileList.get(0);
		
		log.debug("Start Import JobMaster :" + fileName);
		
		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			getLogger().debug("End Import JobMaster (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}

		int ret=0;
		JobTreeItemWrapper jti =null ;

		//XMからBeanに取り込みます。
		FileInputStream input = null;
		JobMasterDataList jobXML=null;
		try {
			jobXML = XmlMarshallUtil.unmarshall(JobMasterDataList.class,new InputStreamReader(
					(input = new FileInputStream(fileName)), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import JobMaster (Error)");
			return ret;
		}
		finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					log.warn(e);
				}
			}
		}

		/* スキーマのバージョンチェック*/
		if(!checkSchemaVersion(jobXML.getSchemaInfo())){
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}

		// jobMastersXML内のparentJobIdの存在チェック
		//（可能ならマネージャ側のバリテーションに委ねたいが、DTOのJobTreeItemではデータ構造上、難しいのでここでチェック）
		if(!checkParentJobId(jobXML.getJobInfo(),jobunitList)){
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}

		try {
			jti = MasterConv.masterXml2Dto(jobXML.getJobInfo(), jobunitList);
		} catch (ParseException pe) {
			log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + pe.getMessage());
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		} catch (Exception e) {
			log.warn(Messages.getString("SettingTools.ImportFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}

		//更新の有無と編集ロックについて確認
		ImportJobMasterRecordConfirmer confirmer = new ImportJobMasterRecordConfirmer(
				log, jti.getChildren().toArray(new JobTreeItemWrapper[0]));
		confirmer.setConfirmMessageId("message.import.confirm4");
		int confirmRet = confirmer.executeConfirm();
		if( confirmRet != SettingConstants.SUCCESS && confirmRet != SettingConstants.ERROR_CANCEL ){
			//変換エラーなら処理打ち切り(キャンセルはキャンセル以前の選択結果を反映するので次に進む)
			log.warn(Messages.getString("SettingTools.EndWithErrorCode"));
			return confirmRet;
		}
		
		//マネージャに投入
		List<String> objectIdList = new ArrayList<String>();

		// 更新単位の件数毎にインポートメソッドを呼び出し、結果をログ出力
		// API異常発生時はそこで中断、レコード個別の異常発生時はユーザ選択次第で続行
		ImportJobMasterClientController importController = new ImportJobMasterClientController(
				log, Messages.getString("job.management.master"), confirmer.getImportRecDtoList(),true);

		ret = importController.importExecute();
		for (RecordRegistrationResponse rec: importController.getImportSuccessList() ){
			objectIdList.add(rec.getImportKeyValue());
		}

		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}

		//オブジェクト権限同時インポート
		importObjectPrivilege(HinemosModuleConstant.JOB, objectIdList);
		
		// 処理の終了
		if (ret == 0 && !(confirmer.isOccurAbnormal) ) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		}else{
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
			if(confirmer.isOccurAbnormal){
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		log.debug("End Import JobMaster : " + fileName);

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
		int res = MasterConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.job.xml.SchemaInfo sci = MasterConv.getSchemaVersion();
		
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	/**
	 * スキーマ内のJobInfoについて親ジョブID存在チェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのjobInfo
	 * @return チェック結果
	 */
	private boolean checkParentJobId(com.clustercontrol.utility.settings.job.xml.JobInfo[] jobMastersXML,List<String> jobunitList) {

		boolean ret =true;
		//各ジョブユニット毎にて 親ジョブになれるジョブID一覧を作成
		Map<String,Set<String>> unitMap = new HashMap<String,Set<String>>();
		for (int i = 0; i < jobMastersXML.length; i++) {
			//インポート対象ジョブユニットに含まれない場合は除外
			if(jobunitList.contains(jobMastersXML[i].getJobunitId()) == false){
				continue;
			}
			Set<String> jobIdSet = unitMap.get(jobMastersXML[i].getJobunitId());
			if(jobIdSet == null){
				jobIdSet = new HashSet<String>();
				unitMap.put(jobMastersXML[i].getJobunitId(), jobIdSet);
			}
			//親ジョブになれるジョブIDのみ、リストに加える。(ジョブユニット、ジョブネット)
			if(jobMastersXML[i].getType() == JobConstant.TYPE_JOBUNIT
				|| jobMastersXML[i].getType() == JobConstant.TYPE_JOBNET ){
				jobIdSet.add(jobMastersXML[i].getId());
			}
		}

		//各JobInfoに設定された親ジョブIDが同一ジョブユニット内に存在するかチェック
		Set<String> checkedJobIdSet = new HashSet<String>();
		for (int i = 0; i < jobMastersXML.length; i++) {
			//ジョブユニット自身の場合はノーチェック
			if (jobMastersXML[i].getType() == JobConstant.TYPE_JOBUNIT) {
				continue;
			}
			//インポート対象ジョブユニットに含まれない場合もノーチェック
			if(jobunitList.contains(jobMastersXML[i].getJobunitId()) == false && jobunitList.contains(jobMastersXML[i].getParentJobunitId()) == false ){
				continue;
			}
			//親ジョブユニットID＝ジョブ自身のジョブユニットID でないならエラー
			Set<String> jobIdSet = unitMap.get(jobMastersXML[i].getParentJobunitId());
			if(jobIdSet == null || jobMastersXML[i].getParentJobunitId().equals(jobMastersXML[i].getJobunitId()) == false){
				String[] mesArgs = { jobMastersXML[i].getJobunitId(),jobMastersXML[i].getId() , jobMastersXML[i].getParentJobunitId() };
 				log.error( Messages.getString("SettingTools.InvalidParentJobunitId",mesArgs) );
				ret =false;
				continue;
			}
			//自身のジョブIDと親ジョブIDが同じ場合はエラー
			if(jobMastersXML[i].getId().equals(jobMastersXML[i].getParentJobId())){
				String[] mesArgs = { jobMastersXML[i].getJobunitId(),jobMastersXML[i].getId() , jobMastersXML[i].getParentJobId() };
 				log.error( Messages.getString("SettingTools.InvalidParentJobId",mesArgs) );

				ret =false;
				continue;
			}
			//同じジョブユニット内に親ジョブIDが存在しないならエラー
			if(jobIdSet.contains(jobMastersXML[i].getParentJobId()) == false){
				String[] mesArgs = { jobMastersXML[i].getJobunitId(),jobMastersXML[i].getId() , jobMastersXML[i].getParentJobId() };
 				log.error( Messages.getString("SettingTools.InvalidParentJobId",mesArgs) );

				ret =false;
				continue;
			}
			//親ジョブIDが循環参照になっている場合はエラー
			if (!checkedJobIdSet.contains(jobMastersXML[i].getId())) {
				Set<String> newJobIdSet = new HashSet<String>();
				JobInfo errJob = checkCycle(jobMastersXML,  jobMastersXML[i].getJobunitId(), jobMastersXML[i].getParentJobId(), newJobIdSet, checkedJobIdSet);
				if (errJob != null) {
					String[] mesArgs = { jobMastersXML[i].getJobunitId(), errJob.getId(), errJob.getParentJobId() };
	 				log.error( Messages.getString("SettingTools.InvalidCircularReference",mesArgs) );
					ret =false;
				}
				checkedJobIdSet.addAll(newJobIdSet);
			}
		}
		return ret;
	}
	
	/**
	 * 循環参照をチェックする。
	 * 
	 * @param XMLファイルのjobInfo
	 * @param ジョブユニットID
	 * @param チェック対象ジョブID
	 * @param 親ジョブID
	 * @return エラージョブ
	 */
	public static JobInfo checkCycle(JobInfo[] jobMastersXML, String jobunitId, String jobid, Set<String> jobIdSet, Set<String> checkedJobIdSet) {
		jobIdSet.add(jobid);
		for (JobInfo jobInfo : jobMastersXML) {
			if (jobInfo.getId() == null || !jobInfo.getId().equals(jobid)) {
				continue;
			}
			if (jobInfo.getJobunitId() == null || !jobInfo.getJobunitId().equals(jobunitId)) {
				return null;	// 親ユニットが不正なため本チェックの対象外
			}
			if (jobInfo.getType() == JobConstant.TYPE_JOBUNIT) {
				return null;	// 親がユニットならチェック終了
			}
			if (jobInfo.getParentJobId() == null) {
				return null;	// 親ジョブID不正はチェックの対象外
			}
			if (checkedJobIdSet.contains(jobInfo.getParentJobId())) {
				return null;	// 既にチェック済
			}
			if (jobIdSet.contains(jobInfo.getParentJobId())) {
				return jobInfo;	// 循環参照エラー
			}
			return checkCycle(jobMastersXML, jobunitId, jobInfo.getParentJobId(), jobIdSet, checkedJobIdSet);
		}
		return null;	// 親ジョブIDが不正なのでチェックの対象外
	}

	/**
	 * Fetch jobInfo for tree and append to job list recursively
	 * 
	 * @param jobList
	 * @param jti
	 */
	private void jobTreeItem2JobList(List<JobInfoWrapper> jobList, JobTreeItemWrapper jti){
		for(JobTreeItemWrapper child : jti.getChildren()){
			JobInfoWrapper job = child.getData();
			job.setParentId(jti.getData().getId());
			jobList.add(job);

			// Add children items to list recursively
			jobTreeItem2JobList(jobList, child);
		}
	}

	/**
	 * Convert tree item to job list
	 * 
	 * @param jti
	 * @param jobunitList エクスポート対象となるジョブユニットIDのリスト
	 * @return
	 */
	private List<JobInfoWrapper> jobTreeItem2JobListByJobunitId(JobTreeItemWrapper jti, List<String> jobunitList){
		List<JobInfoWrapper> jobList = new ArrayList<>();
		if (jti == null) {
			return jobList;
		}
		// jti = rootItem
		for(JobTreeItemWrapper topItem : jti.getChildren()){ // top level
			for(JobTreeItemWrapper child : topItem.getChildren()){ // Jobunit level

				if(jobunitList.contains(child.getData().getId())){
					// Add jobunit to list
					jobList.add(child.getData());

					// Also add its child items to list
					jobTreeItem2JobList(jobList, child);
				}
			}
		}

		return jobList;
	}

	/**
	 * ジョブ情報をHinemosマネージャから取得し、XMLに出力します。
	 * 
	 * @param fileList エクスポート処理で使用するXMLファイルのリスト
	 * @param jobunitList エクスポート対象となるジョブユニットIDのリスト
	 * @return
	 */
	@ExportMethod
	public int exportMaster(ArrayList<String> fileList, ArrayList<String> jobunitList) {
		// ジョブの場合、ファイルはひとつ
		String fileName = fileList.get(0);
		log.debug("Start Export JobMaster : " + fileName);

		int ret = 0;
		
		JobTreeItemWrapper jti =null ;
		try {
			//マネージャからジョブの一覧（ツリー）を取得
			JobTreeItemResponseP2  res = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobTreeJobInfoFull(null);
			jti = JobTreeItemUtil.getItemFromP2(res);
			if (null != jti){
				sortJobMaster(jti);
			}
		} catch (Exception e1) {
			log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export JobMaster : " + fileName +"(Error)");
			return ret;
		}

		com.clustercontrol.utility.settings.job.xml.JobMasters masterXML
			= new com.clustercontrol.utility.settings.job.xml.JobMasters();

		FileOutputStream output = null;
		try {

			//共通情報のセット
			masterXML.setCommon(CommonConv.versionJobDto2Xml(Config.getVersion()));

			//スキーマ情報のセット
			masterXML.setSchemaInfo(MasterConv.getSchemaVersion());

			//ジョブ情報のセット(ジョブツリーからXML用のリストに変換)
			masterXML.setJobInfo(masterDto2Xml(jobTreeItem2JobListByJobunitId(jti, jobunitList)));

			//export対象として選択しなかったものをリストから削除する
			com.clustercontrol.utility.settings.job.xml.JobInfo[] masters = masterXML.getJobInfo();
			for (int i = 0; i < masters.length; i++) {
				boolean exist = false;
				
				for (String jobunitId : jobunitList) {
					if (jobunitId.equals(masters[i].getJobunitId())){
						exist = true;
						break;
					}
				}
				if (!exist) {
					masterXML.removeJobInfo(masters[i]);
				} else {
					if (masters[i].getType() == JobConstant.TYPE_JOBUNIT)
					log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + masters[i].getJobunitId());
				}
			}

			//マーシャリング
			masterXML.marshal(new OutputStreamWriter(
					(output = new FileOutputStream(fileName)), "UTF-8"));
		} catch (Exception e) {
			log.warn(Messages.getString("SettingTools.ExportFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					log.warn(e);
				}
			}
		}

		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		}else{
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
		}

		log.debug("End Export JobMaster : " + fileName);
		return ret;
	}

	/**
	 * Hinemosマネージャからジョブユニットのリストを取得します。
	 * ログインユーザが参照可能なジョブユニットのみを返します。
	 * 
	 * 以下の情報を持つArrayListの2次元配列を返します。
	 * 
	 * <li>ジョブユニットのジョブID
	 * <li>ジョブユニットのジョブ名
	 * <li>説明
	 * 
	 * @return ジョブユニットのジョブID, ジョブユニットのジョブ名, 説明を含むArrayListの2次元配列
	 */
	public List<List<String>> getJobunitList() {
		List<List<String>> ret = new ArrayList<>();
		
		try {
			
			///////////////////////////////////////////////////////////////////
			// ログインユーザで参照可能なジョブユニットを取得するメソッドをマネージャ側に用意し、
			// この部分の実装は修正する
			// treeOnly = trueの場合は、ログインユーザで参照可能なジョブユニットと 配下のジョブが取れる
			JobTreeItemResponseP1 res = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobTree(null);
			JobTreeItemWrapper jobTreeItem;
			if(res.getData().getDescription() == null) {
				JobTreeItemResponseP2 dtoP2 = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobTreeJobInfoFull(null);
				jobTreeItem = JobTreeItemUtil.getItemFromP2ForTreeView(dtoP2);
			} else {
				jobTreeItem = JobTreeItemUtil.getItemFromP1ForTreeView(res); 
			}
			
			List<JobTreeItemWrapper> jobunitList = jobTreeItem.getChildren().get(0).getChildren();
			
			for (JobTreeItemWrapper jobunit : jobunitList) {
				ArrayList<String> jobunit_arrayList = new ArrayList<String>();
				jobunit_arrayList.add(jobunit.getData().getJobunitId());
				jobunit_arrayList.add(jobunit.getData().getName());
				jobunit_arrayList.add(jobunit.getData().getDescription());
				
				ret.add(jobunit_arrayList);
			}
			///////////////////////////////////////////////////////////////////
		} catch (HinemosUnknown e) {
			log.error(HinemosMessage.replace(e.getMessage()));
		} catch (InvalidRole e) {
			log.error(HinemosMessage.replace(e.getMessage()));
		} catch (InvalidUserPass e) {
			log.error(HinemosMessage.replace(e.getMessage()));
		} catch (JobMasterNotFound e) {
			log.error(HinemosMessage.replace(e.getMessage()));
		} catch (NotifyNotFound e) {
			log.error(HinemosMessage.replace(e.getMessage()));
		} catch (UserNotFound e) {
			log.error(HinemosMessage.replace(e.getMessage()));
		} catch (RestConnectFailed e) {
			log.error(HinemosMessage.replace(e.getMessage()));
		}
		
		
		log.debug("End getJobunitList");
		return ret;
	}
	
	/**
	 * ジョブマスタXMLに定義されている、ジョブユニットのリストを返します。
	 * ログインユーザが参照可能なジョブユニットのみを返します。
	 * 
	 * 以下の情報を持つArrayListの2次元配列を返します。
	 * 
	 * <li>ジョブユニットのジョブID
	 * <li>ジョブユニットのジョブ名
	 * <li>説明
	 * 
	 * @return ジョブユニットのジョブID, ジョブユニットのジョブ名, 説明を含むArrayListの2次元配列
	 */
	public List<List<String>> getJobunitListFromXML(String fileName, String uid) {
		
		List<List<String>> ret = new ArrayList<>();
		
		//XMからBeanに取り込みます。
		JobMasterDataList jobXML=null;
		try {
			jobXML = XmlMarshallUtil.unmarshall(JobMasterDataList.class,new InputStreamReader(
					new FileInputStream(fileName), "UTF-8"));
			
			String loginUser = uid;
			
			com.clustercontrol.utility.settings.job.xml.JobInfo[] xmlJobInfo = jobXML.getJobInfo();
			for (int i = 0; i < xmlJobInfo.length; i++) {
				
				boolean referable = false;
				
				if (xmlJobInfo[i].getType() == JobConstant.TYPE_JOBUNIT ) {
					log.debug("jobunitId : " + xmlJobInfo[i].getJobunitId()
							+ ", loginUser : " + loginUser + ", referable : "
							+ referable);
					
					ArrayList<String> jobunit_arrayList = new ArrayList<String>();
					jobunit_arrayList.add(xmlJobInfo[i].getJobunitId());
					jobunit_arrayList.add(xmlJobInfo[i].getName());
					jobunit_arrayList.add(xmlJobInfo[i].getDescription());
					ret.add(jobunit_arrayList);
				}
			}
			
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			// 存在しないファイルがある場合
			MessageDialog.openError(
					null,
					Messages.getString("message.error"),
					Messages.getString("SettingTools.UnmarshalXmlFailed") + " : " + fileName + "\n\n" + e);
			return null;
		}
		
		return ret;
		
	}

	/**
	 * JobTreeItemソート用メソッド。
	 * 引数で渡されたJobTreeItemの子要素リストをソートした後、再帰的に繰り返す。
	 * 
	 * @param jobTreeItem
	 */
	private void sortJobMaster(JobTreeItemWrapper jobTreeItem) {
		log.debug("sort start : " + jobTreeItem.getData().getId());

		List<JobTreeItemWrapper> children = jobTreeItem.getChildren();
		if (log.isDebugEnabled()) {
			for (JobTreeItemWrapper child : children) {
				log.debug("before sort : " + child.getData().getId());
			}
		}
		
		Collections.sort(children, new Comparator<JobTreeItemWrapper>() {
			@Override
			public int compare(JobTreeItemWrapper jobTreeItem1, JobTreeItemWrapper jobTreeItem2) {
				return jobTreeItem1.getData().getId().compareTo(jobTreeItem2.getData().getId());
			}
		});
		if (log.isDebugEnabled()) {
			for (JobTreeItemWrapper child : children) {
				log.debug("after sort : " + child.getData().getId());
			}
		}
		
		for (JobTreeItemWrapper item : children) {
			this.sortJobMaster(item);
		}
	}

	/**
	 * MGRで利用されているスケジュールデータをXMLのBeanにマッピングします。
	 * @param scheduleMgr マネージャで利用されいる形式のスケジュールデータ
	 * @throws ParseException 
	 * @throws NullPointerException 
	 * 
	 */
	private com.clustercontrol.utility.settings.job.xml.JobInfo[] masterDto2Xml(List<JobInfoWrapper> jobList) throws NullPointerException, ParseException {
		List<JobInfoWrapper> fullJobList = null;
		try {
			//マネージャからジョブ情報Listを取得
			GetJobFullListRequest request = new GetJobFullListRequest();
			request.setJobList(new ArrayList<JobInfoRequestP1>());
			for( JobInfoWrapper orgRec : jobList ){
				JobInfoRequestP1 findRec = new JobInfoRequestP1();
				findRec.setId(orgRec.getId());
				findRec.setJobunitId(orgRec.getJobunitId());
				request.getJobList().add(findRec);
			}
			List<JobInfoResponse>fullJobListTmp = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobFullList(request);
			fullJobList = JobTreeItemUtil.getInfoListFromDtoList(fullJobListTmp);
		} catch (HinemosUnknown| InvalidRole| InvalidUserPass| NotifyNotFound| UserNotFound |JobMasterNotFound |RestConnectFailed e) {
			log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
		}

		if(null != fullJobList && 0 < fullJobList.size()){
			com.clustercontrol.utility.settings.job.xml.JobInfo[] jobs =
					new com.clustercontrol.utility.settings.job.xml.JobInfo[fullJobList.size()];

			int i=0;
			for(JobInfoWrapper job : fullJobList){
				//親ジョブID取得,オーナーロールID取得
				if(job.getParentId() == null){
					for(JobInfoWrapper info : jobList){
						if(job.getJobunitId().equals(info.getJobunitId()) 
								&& job.getId().equals(info.getId())){
							job.setParentId(info.getParentId());
							job.setOwnerRoleId(info.getOwnerRoleId());
							break;
						}
					}
				}
				//データを生成
				jobs[i++] = MasterConv.setXMLJobData(job, job.getParentId());
			}
			return jobs;
		}else{
			return new com.clustercontrol.utility.settings.job.xml.JobInfo[0];
		}
	}

	/**
	 * 差分比較処理を行います。
	 * XMLファイル２つ（filePath1,filePath2）を比較する。
	 * 差分がある場合：差分をＣＳＶファイルで出力する。
	 * 差分がない場合：出力しない。
	 * 				   または、すでに存在している同一名のＣＳＶファイルを削除する。
	 *
	 * @param xmlFile1 XMLファイル名
	 * @param xmlFile2 XMLファイル名
	 * @return 終了コード
	 * @throws ConvertorException
	 */
	@DiffMethod
	public int diffXml(String xmlFile1, String xmlFile2) throws ConvertorException {

		log.debug("Start Differrence JobMaster ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		JobMasterDataList jobMaster1 = null;
		JobMasterDataList jobMaster2 = null;

		// XMLファイルからの読み込み
		try {
			jobMaster1 = XmlMarshallUtil.unmarshall(JobMasterDataList.class,new InputStreamReader(new FileInputStream(xmlFile1), "UTF-8"));
			jobMaster2 = XmlMarshallUtil.unmarshall(JobMasterDataList.class,new InputStreamReader(new FileInputStream(xmlFile2), "UTF-8"));
			sort(jobMaster1);
			sort(jobMaster2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence JobMaster (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(jobMaster1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(jobMaster2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(jobMaster1, jobMaster2, JobMasterDataList.class, resultA);
			assert resultA.getResultBs().size() == 1;
			
			if (diff) {
				ret += SettingConstants.SUCCESS_DIFF_1;
			}
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlFile2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(xmlFile2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						log.warn(String.format("file can not be deleted. file name=%s", f.getName()));
				}
			}
		}
		catch (Exception e) {
			getLogger().error("unexpected: ", e);
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

		getLogger().debug("End Differrence JobMaster");

		return ret;
	}

	private void sort(JobMasterDataList jobMaster) {
		com.clustercontrol.utility.settings.job.xml.JobInfo[] infoList = jobMaster.getJobInfo();
		Arrays.sort(
			infoList,
			new Comparator<com.clustercontrol.utility.settings.job.xml.JobInfo>() {
				@Override
				public int compare(
						com.clustercontrol.utility.settings.job.xml.JobInfo info1,
						com.clustercontrol.utility.settings.job.xml.JobInfo info2) {
					int ret = info1.getJobunitId().compareTo(info2.getJobunitId());
					if(ret != 0){
						return ret;
					} else {
						return info1.getId().compareTo(info2.getId());
					}
				}
			});
		 jobMaster.setJobInfo(infoList);
	}
	
	public Logger getLogger() {
		return log;
	}
	
	private Map<String, JobInfoWrapper> createJobMap(JobTreeItemWrapper tree){
		if(tree != null){
			Map<String, JobInfoWrapper> jobMap = new HashMap<>();
			appendJobMap(jobMap, tree);
			return jobMap;
		}
		return null;
	}
	
	private void appendJobMap(Map<String, JobInfoWrapper> jobMap, JobTreeItemWrapper tree){
		jobMap.put(tree.getData().getJobunitId()+tree.getData().getId(), tree.getData());
		if(tree.getChildren() != null && !tree.getChildren().isEmpty()){
			for(JobTreeItemWrapper child: tree.getChildren()){
				appendJobMap(jobMap, child);
			}
		}
	}
	
	/**
	 * ジョブユニットの編集ロックを取得する。
	 * 
	 * @param jobUnitId ジョブユニットID
	 * @param managerUpdateTime マネージャのジョブユニット更新日時
	 * @return 編集用のセッション
	 * @throws OtherUserGetLock 
	 */
	private Integer getEditLock(String managerName, String jobUnitId, Long managerUpdateTime) throws OtherUserGetLock {
		if (CommmandCallMode.isCommandLine()) {
			// コマンドラインツールからの呼び出しの場合、ダイアログ表示を回避するため処理を分岐する。
			return getEditLockForCommandLine(managerName, jobUnitId, managerUpdateTime);
		}
		
		try {
			return JobUtil.getEditLock(managerName, jobUnitId, managerUpdateTime, false);
		} catch (OtherUserGetLock e) {
			String message = e.getMessage() + "\n"
					+ HinemosMessage.replace(MessageConstant.MESSAGE_WANT_TO_GET_LOCK.getMessage());
			if (MessageDialog.openQuestion(
					null,
					Messages.getString("confirmed"),
					message)) {
				try {
					return JobUtil.getEditLock(managerName, jobUnitId, managerUpdateTime, true);
				} catch (Exception e1) {
					// ここに入ることはない想定
					log.error("getEditLock() : logical error");
				}
			} else {
				throw e;
			}
		}
		return null;
	}
	/**
	 * 編集ロックを取得します。(コマンドラインツール向け)
	 * 
	 * @param managerName
	 * @param jobunitId
	 * @param updateTime
	 * @return 編集セッション番号
	 * @throws OtherUserGetLock_Exception
	 */
	private static Integer getEditLockForCommandLine(String managerName, String jobunitId, Long updateTime) throws OtherUserGetLock {
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			GetEditLockRequest req = new GetEditLockRequest(); 
			req.setForceFlag(false);
			if(updateTime != null){
				req.setUpdateTime(TimezoneUtil.getSimpleDateFormat().format(new Date( updateTime)));
			}
			EditLockResponse ret = wrapper.getEditLock(jobunitId, req);
			return ret.getEditSession();
		} catch (OtherUserGetLock e) {
			throw e;
		} catch (InvalidRole e) {
			log.warn("getEditLockForCommandLine(), " + Messages.getString("message.accesscontrol.16") + ", " + HinemosMessage.replace(e.getMessage()));
		} catch (UpdateTimeNotLatest e) {
			log.warn("getEditLockForCommandLine(), " + HinemosMessage.replace(e.getMessage()));
		} catch (JobInvalid e) {
			log.warn("getEditLockForCommandLine(), " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			log.warn("getEditLockForCommandLine(), " + Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return null;
	}
	
	/**
	 * ジョブユニットの編集ロックを解放する。
	 * 
	 * @param managerName ログイン中のマネージャ名
	 * @param editSession 編集用のセッション
	 */
	private void releaseEditLock(String managerName, String jobunitId, Integer editSession){
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			wrapper.releaseEditLock(jobunitId, editSession);
		} catch(Exception e){
			// ここに入ることはない想定
			log.error("releaseEditLock() : logical error");
		}
	}
	
	/**
	 * マネージャのジョブユニット更新日時を取得する。
	 * 
	 * @param jobUnitIdList　ジョブユニットIDのList
	 * @return jobUnitUpdateTimeMap key:ジョブユニットID, value:更新日時
	 */
	private Map<String, Long> getJobUnitUpdateTimeMap(List<String> jobUnitIdList){
		Map<String, Long> jobUnitUpdateTimeMap = new HashMap<>();
		try {
			JobTreeItemResponseP1 resDto = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobTree(null);
			if(resDto.getData().getUpdateTime() == null) {
				JobTreeItemResponseP2 dtoP2 = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobTreeJobInfoFull(null);
				for( JobTreeItemResponseP2 jobunit : dtoP2.getChildren().get(0).getChildren() ){
					if(jobunit.getData().getUpdateTime() != null && jobUnitIdList.contains(jobunit.getData().getJobunitId()) ){ 
						jobUnitUpdateTimeMap.put(jobunit.getData().getJobunitId(),
								JobTreeItemUtil.convertDtStringtoLong(jobunit.getData().getUpdateTime()));
					}
				}
			} else {
				for( JobTreeItemResponseP1 jobunit : resDto.getChildren().get(0).getChildren() ){
					if(jobunit.getData().getUpdateTime() != null && jobUnitIdList.contains(jobunit.getData().getJobunitId()) ){ 
						jobUnitUpdateTimeMap.put(jobunit.getData().getJobunitId(),
								JobTreeItemUtil.convertDtStringtoLong(jobunit.getData().getUpdateTime()));
					}
				}
			}
		} catch (RestConnectFailed| HinemosUnknown| InvalidRole| InvalidUserPass| JobMasterNotFound| NotifyNotFound| UserNotFound e) {
			log.error(HinemosMessage.replace(e.getMessage()));
		}
		for (String jobunitId : jobUnitIdList) {
			if (!(jobUnitUpdateTimeMap.containsKey(jobunitId))) {
				jobUnitUpdateTimeMap.put(jobunitId, null);
			}
		}
		
		
		return jobUnitUpdateTimeMap;
	}

	/**
	 * ジョブの編集モード取得に必要なシステム権限チェックを行う
	 * 
	 * @throws Exception
	 */
	private void hasSystemPrivilegeForGetEditLock() throws Exception {
		List<String> labels = new ArrayList<>();

		AccessRestClientWrapper access = AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());

		HasSystemPrivilegeRequest hasSystemPrivilegeRequest = new HasSystemPrivilegeRequest();
		hasSystemPrivilegeRequest.setSystemPrivilegeInfo(new SystemPrivilegeInfoRequestP1());
		hasSystemPrivilegeRequest.getSystemPrivilegeInfo().setSystemFunction(SystemFunctionEnum.JOBMANAGEMENT);
		hasSystemPrivilegeRequest.getSystemPrivilegeInfo().setSystemPrivilege(SystemPrivilegeEnum.READ);
		if (!access.hasSystemPrivilege(hasSystemPrivilegeRequest).getResult()) {
			labels.add(SystemFunctionEnum.JOBMANAGEMENT.getValue() + "." + SystemPrivilegeEnum.READ.getValue());
		}
		hasSystemPrivilegeRequest.getSystemPrivilegeInfo().setSystemPrivilege(SystemPrivilegeEnum.ADD);
		if (!access.hasSystemPrivilege(hasSystemPrivilegeRequest).getResult()) {
			labels.add(SystemFunctionEnum.JOBMANAGEMENT.getValue() + "." + SystemPrivilegeEnum.ADD.getValue());
		}
		hasSystemPrivilegeRequest.getSystemPrivilegeInfo().setSystemPrivilege(SystemPrivilegeEnum.MODIFY);
		if (!access.hasSystemPrivilege(hasSystemPrivilegeRequest).getResult()) {
			labels.add(SystemFunctionEnum.JOBMANAGEMENT.getValue() + "." + SystemPrivilegeEnum.MODIFY.getValue());
		}
		if (!labels.isEmpty()) {
			String messageArg = String.join(",", labels);
			String message = Messages.getString("message.user.auth.not.enough.roll", new String[]{messageArg});
			throw new InvalidRole(message);
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
					getLogger());
		}
	}
	
	/**
	 * ジョブインポート向けのレコード確認用クラス（更新の有無と編集ロック確認）
	 * 
	 */
	protected class ImportJobMasterRecordConfirmer extends ImportRecordConfirmer<JobTreeItemWrapper, ImportJobMasterRecordRequest, String>{
		//ジョブ情報保管用マップ
		private Map<String, JobInfoWrapper> jobMap = null;
		//編集ロック保管用マップ
		private Map<String, Integer> editLockMap = new HashMap<String, Integer>();
		//異常発生フラグ（インポートへ進めたいけど 異常発生とした場合に true）
		private boolean isOccurAbnormal = false; 
		
		public ImportJobMasterRecordConfirmer(Logger logger, JobTreeItemWrapper[] importList){
			super(logger, importList);			
		}
		@Override
		protected ImportJobMasterRecordRequest convertDtoXmlToRestReq(JobTreeItemWrapper xmlDto)
				throws HinemosUnknown, InvalidSetting {
			ImportJobMasterRecordRequest dtoRec =new ImportJobMasterRecordRequest();
			dtoRec.setImportData(JobTreeItemUtil.getRequestFromItem(xmlDto));
			dtoRec.setImportKeyValue(dtoRec.getImportData().getData().getJobunitId());
			dtoRec.setEditLockData(new GetEditLockRequest() );

			String updateTime = xmlDto.getData().getUpdateTime();
			// サーバ側で強制モードで編集ロックを取得させる（クライアント制御でも取得するが 異常時の編集ロック解除制御の都合上 API上でも改めて取得させる）
			dtoRec.getEditLockData().setUpdateTime(updateTime);
			dtoRec.getEditLockData().setForceFlag(true);

			return dtoRec;
		}

		//Set<String> は利用していない  this.jobMap で代替
		@Override
		protected Set<String> getExistIdSet() throws Exception {
			JobTreeItemWrapper tree = null;
			JobTreeItemResponseP1 orgTree = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobTree(null);
			if(orgTree.getData().getDescription() == null) {
				JobTreeItemResponseP2 dtoP2 = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobTreeJobInfoFull(null);
				tree = JobTreeItemUtil.getItemFromP2ForTreeView(dtoP2);
			} else {
				tree =JobTreeItemUtil.getItemFromP1ForTreeView(orgTree);
			}
			this.jobMap = createJobMap(tree);
			return null;
		}
		//存在確認＋更新済み確認を実施
		@Override
		protected boolean isExistRecord(JobTreeItemWrapper xmlDto){
			//マネージャに存在しない場合は無条件で登録
			JobInfoWrapper info = jobMap.get(xmlDto.getData().getJobunitId()+xmlDto.getData().getId());
			if (info == null) {
				return false;
			}
			Long updateTime = JobTreeItemUtil.convertDtStringtoLong(xmlDto.getData().getUpdateTime());
			Long mgrUpdTime = JobTreeItemUtil.convertDtStringtoLong(info.getUpdateTime());
			//更新時間がマネージャ側の方が進んでいたら警告
			if (updateTime < mgrUpdTime) {
				return true;
			}else{
				return false;
			}
		};

		@Override
		protected boolean additionalCheck(ImportJobMasterRecordRequest restDto){
			//編集ロックの取得（取得したロックの解除はAPI側にゆだねるがチェックの都合上 クライアントでも一旦取得する）
			try {
				JobInfoWrapper info = jobMap.get(restDto.getImportData().getData().getJobunitId()+restDto.getImportData().getData().getId());
				if (info == null) {
					//新規登録の場合、ロック取得に変更日時は不要なので削除する
					restDto.getEditLockData().setUpdateTime(null);
				}else{
					//上書きの場合、確認した時点の最新の日時に変更する。
					restDto.getEditLockData().setUpdateTime(info.getUpdateTime());
				}
				// インポート時はジョブユニットの編集ロックを取得する。(取得できなければ 異常で処理停止 )
				// 他でロック済み（OtherUserGetLock）の場合は、該当レコードを飛ばすが処理は続行。
				Integer ret =getEditLock(UtilityManagerUtil.getCurrentManagerName(),restDto.getImportData().getData().getJobunitId(),
						JobTreeItemUtil.convertDtStringtoLong(restDto.getEditLockData().getUpdateTime()));
				if (ret != null) {
					editLockMap.put(restDto.getImportData().getData().getJobunitId(), ret);
					return true;
				}else{
					this.ret = SettingConstants.ERROR_INPROCESS;
				}
			} catch (OtherUserGetLock e) {
				log.error(Messages.getString("SettingTools.CannotGetLockToImportJob") + " : " + restDto.getImportData().getData().getJobunitId());
				isOccurAbnormal = true;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				this.ret = SettingConstants.ERROR_INPROCESS;
			}
			return false;
		}
		
		@Override
		protected boolean isLackRestReq(ImportJobMasterRecordRequest restDto) {
			//利用しないので空実装(ここに来るまでにチェック済み)
			return false;
		}

		@Override
		protected String getKeyValueXmlDto(JobTreeItemWrapper xmlDto) {
			return xmlDto.getData().getJobunitId();
		}

		@Override
		protected String getId(JobTreeItemWrapper xmlDto) {
			return xmlDto.getData().getJobunitId();
		}

		@Override
		protected void setNewRecordFlg(ImportJobMasterRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
		public boolean isOccurAbnormal() {
			return isOccurAbnormal;
		}
		
		@Override
		public int executeConfirm(){
			int confirmRet = super.executeConfirm();
			//異常停止する場合、取得済みの編集ロックを開放する
			if( confirmRet != SettingConstants.SUCCESS && confirmRet != SettingConstants.ERROR_CANCEL ){
				String managerName = UtilityManagerUtil.getCurrentManagerName();
				for( Map.Entry<String,Integer> rec : editLockMap.entrySet() ){
					releaseEditLock(managerName, rec.getKey(), rec.getValue());
				}
			}
			return ret;
		}
		@Override
		protected void hasSystemPrivilege() throws Exception {
			hasSystemPrivilegeForGetEditLock();
		}
	}
	
	/**
	 * ジョブインポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportJobMasterClientController extends ImportClientController<ImportJobMasterRecordRequest, ImportJobMasterResponse, RecordRegistrationResponse>{
		
		public ImportJobMasterClientController(Logger logger, String importInfoName, List<ImportJobMasterRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);			
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportJobMasterResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportJobMasterResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportJobMasterRecordRequest importRec) {
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
		protected ImportJobMasterResponse callImportWrapper(List<ImportJobMasterRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportJobMasterRequest reqDto = new ImportJobMasterRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importJobMaster(reqDto);
		}

		@Override
		protected String getRestExceptionMessage(RecordRegistrationResponse responseRec) {
			if (responseRec.getExceptionInfo() != null) {
				return responseRec.getExceptionInfo().getException() +":"+ responseRec.getExceptionInfo().getMessage();
			}
			return null;
		};
	}
	
}
