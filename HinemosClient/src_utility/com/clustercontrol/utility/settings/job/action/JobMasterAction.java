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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.jobmanagement.bean.JobConstant;
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
import com.clustercontrol.utility.settings.job.conv.MasterConv;
import com.clustercontrol.utility.settings.job.xml.JobMasterDataList;
import com.clustercontrol.utility.settings.job.xml.JobMasters;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.platform.conv.CommonConv;
import com.clustercontrol.utility.settings.ui.dialog.ImportProcessDialog;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.ws.jobmanagement.HinemosUnknown_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidSetting_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidUserPass_Exception;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobMasterNotFound_Exception;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.jobmanagement.NotifyNotFound_Exception;
import com.clustercontrol.ws.jobmanagement.UserNotFound_Exception;
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
	private static final String DATA_FORMAT = "yyyy-MM-dd HH:mm:ss";

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
		
		Iterator<String> itr = idList.iterator();
		String jobunitId = null;
		while(itr.hasNext()) {
			try {
				jobunitId = itr.next();
				JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).deleteJobunit(jobunitId);
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + jobunitId);
			} catch (HinemosUnknown_Exception e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (InvalidRole_Exception e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (InvalidUserPass_Exception e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				continue;
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
		
		if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
			getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			getLogger().debug("End Import JobMaster (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}

		int ret=0;
		JobTreeItem jti =null ;

		//XMからBeanに取り込みます。
		FileInputStream input = null;
		JobMasterDataList jobXML=null;
		try {
			jobXML = JobMasters.unmarshal(new InputStreamReader(
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

		try {
			jti = MasterConv.masterXml2Dto(jobXML.getJobInfo(), jobunitList);
		} catch (Exception e) {
			log.warn(Messages.getString("SettingTools.ImportFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}

		//マネージャに投入
		List<JobTreeItem> xmlJobunitList = jti.getChildren();
		int statusCode = checkUpdate(xmlJobunitList);
		if(statusCode == -1){
	    	getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
	    	getLogger().debug("End Import JobMaster (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		List<String> objectIdList = new ArrayList<String>();
		for (JobTreeItem xmlJobunit : xmlJobunitList) {
			try {
				JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).registerJobunit(xmlJobunit);
				objectIdList.add(xmlJobunit.getData().getJobunitId());
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + xmlJobunit.getData().getJobunitId());
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
		importObjectPrivilege(HinemosModuleConstant.JOB, objectIdList);
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		}else{
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
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
	 * Fetch jobInfo for tree and append to job list recursively
	 * 
	 * @param jobList
	 * @param jti
	 */
	private void jobTreeItem2JobList(List<JobInfo> jobList, JobTreeItem jti){
		for(JobTreeItem child : jti.getChildren()){
			JobInfo job = child.getData();
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
	private List<JobInfo> jobTreeItem2JobListByJobunitId(JobTreeItem jti, List<String> jobunitList){
		List<JobInfo> jobList = new ArrayList<>();

		// jti = rootItem
		for(JobTreeItem topItem : jti.getChildren()){ // top level
			for(JobTreeItem child : topItem.getChildren()){ // Jobunit level

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
		
		boolean treeOnly=false; //ジョブの中身も含む
		JobTreeItem jti =null ;
		try {
			//マネージャからジョブの一覧（ツリー）を取得
			jti = JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getJobTree(null, treeOnly);
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

			if(jti !=null){
				//ジョブ情報のセット(ジョブツリーからXML用のリストに変換)
				masterXML.setJobInfo(masterDto2Xml(jobTreeItem2JobListByJobunitId(jti, jobunitList)));
			}

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
			JobTreeItem jobTreeItem = JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getJobTree(null, true);
			List<JobTreeItem> jobunitList = jobTreeItem.getChildren().get(0).getChildren();
			
			for (JobTreeItem jobunit : jobunitList) {
				ArrayList<String> jobunit_arrayList = new ArrayList<String>();
				jobunit_arrayList.add(jobunit.getData().getJobunitId());
				jobunit_arrayList.add(jobunit.getData().getName());
				jobunit_arrayList.add(jobunit.getData().getDescription());
				
				ret.add(jobunit_arrayList);
			}
			///////////////////////////////////////////////////////////////////
		} catch (HinemosUnknown_Exception e) {
			log.error(HinemosMessage.replace(e.getMessage()));
		} catch (InvalidRole_Exception e) {
			log.error(HinemosMessage.replace(e.getMessage()));
		} catch (InvalidUserPass_Exception e) {
			log.error(HinemosMessage.replace(e.getMessage()));
		} catch (JobMasterNotFound_Exception e) {
			log.error(HinemosMessage.replace(e.getMessage()));
		} catch (NotifyNotFound_Exception e) {
			log.error(HinemosMessage.replace(e.getMessage()));
		} catch (UserNotFound_Exception e) {
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
			jobXML = JobMasters.unmarshal(new InputStreamReader(
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
	private void sortJobMaster(JobTreeItem jobTreeItem) {
		log.debug("sort start : " + jobTreeItem.getData().getId());

		List<JobTreeItem> children = jobTreeItem.getChildren();
		if (log.isDebugEnabled()) {
			for (JobTreeItem child : children) {
				log.debug("before sort : " + child.getData().getId());
			}
		}
		
		Collections.sort(children, new Comparator<JobTreeItem>() {
			@Override
			public int compare(JobTreeItem jobTreeItem1, JobTreeItem jobTreeItem2) {
				return jobTreeItem1.getData().getId().compareTo(jobTreeItem2.getData().getId());
			}
		});
		if (log.isDebugEnabled()) {
			for (JobTreeItem child : children) {
				log.debug("after sort : " + child.getData().getId());
			}
		}
		
		for (JobTreeItem item : children) {
			this.sortJobMaster(item);
		}
	}

	/**
	 * MGRで利用されているスケジュールデータをXMLのBeanにマッピングします。
	 * @param scheduleMgr マネージャで利用されいる形式のスケジュールデータ
	 * 
	 */
	private com.clustercontrol.utility.settings.job.xml.JobInfo[] masterDto2Xml(List<JobInfo> jobList) {
		List<JobInfo> fullJobList = null;
		try {
			//マネージャからジョブ情報Listを取得
			fullJobList = JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getJobFullList(jobList);
		} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | NotifyNotFound_Exception | UserNotFound_Exception e) {
			log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
		}

		if(null != fullJobList && 0 < fullJobList.size()){
			com.clustercontrol.utility.settings.job.xml.JobInfo[] jobs =
					new com.clustercontrol.utility.settings.job.xml.JobInfo[fullJobList.size()];

			int i=0;
			for(JobInfo job : fullJobList){
				//親ジョブID取得,オーナーロールID取得
				if(job.getParentId() == null){
					for(JobInfo info : jobList){
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
			jobMaster1 = JobMasters.unmarshal(new InputStreamReader(new FileInputStream(xmlFile1), "UTF-8"));
			jobMaster2 = JobMasters.unmarshal(new InputStreamReader(new FileInputStream(xmlFile2), "UTF-8"));
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
	
	private Map<String, JobInfo> createJobMap(JobTreeItem tree){
		if(tree != null){
			Map<String, JobInfo> jobMap = new HashMap<>();
			appendJobMap(jobMap, tree);
			return jobMap;
		}
		return null;
	}
	
	private void appendJobMap(Map<String, JobInfo> jobMap, JobTreeItem tree){
		jobMap.put(tree.getData().getJobunitId()+tree.getData().getId(), tree.getData());
		if(tree.getChildren() != null && !tree.getChildren().isEmpty()){
			for(JobTreeItem child: tree.getChildren()){
				appendJobMap(jobMap, child);
			}
		}
	}
	
	private int checkUpdate(List<JobTreeItem> jobUnitList){
		JobTreeItem tree = null;
		try {
			tree = JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getJobTree(null, false);
		} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | JobMasterNotFound_Exception | NotifyNotFound_Exception | UserNotFound_Exception e) {
			log.error(HinemosMessage.replace(e.getMessage()));
		}
		
		if(tree == null){
			return 0;
		}
		
		Map<String, JobInfo> jobMap = createJobMap(tree);
		
		Boolean sameProcess = false;
		Integer processType = null;
		
		for(JobTreeItem jobUnit: new ArrayList<>(jobUnitList)){
			//マネージャに存在しない場合は無条件で登録
			if(!jobMap.containsKey(jobUnit.getData().getJobunitId()+jobUnit.getData().getId())){
				continue;
			}
			Long updateTime = jobUnit.getData().getUpdateTime();
			Long mgrUpdTime = null;
			try {
				JobInfo info = JobEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getJobFull(jobMap.get(createKey(jobUnit)));
				SimpleDateFormat dateFormat = new SimpleDateFormat(DATA_FORMAT);
				mgrUpdTime = dateFormat.parse(dateFormat.format(info.getUpdateTime())).getTime();
			} catch (ParseException e) {
				log.error(e);
				continue;
			} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | JobMasterNotFound_Exception	| NotifyNotFound_Exception | UserNotFound_Exception e) {
				log.error(HinemosMessage.replace(e.getMessage()));
				continue;
			}
			
			if (updateTime < mgrUpdTime) {
				if (!sameProcess) {
					String[] args = {jobUnit.getData().getJobunitId()};
					ImportProcessDialog dialog = new ImportProcessDialog(
							null, Messages.getString("message.import.confirm4", args));
					processType = dialog.open();
					sameProcess = dialog.getToggleState();
				}
				if (processType == ImportProcessDialog.SKIP){
					jobUnitList.remove(jobUnit);
				} else if (processType == ImportProcessDialog.CANCEL){
					return -1;
				}
			}
		}
		
		return 1;
	}
	
	private String createKey(JobTreeItem tree){
		return tree.getData().getJobunitId()+tree.getData().getId();
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
}
