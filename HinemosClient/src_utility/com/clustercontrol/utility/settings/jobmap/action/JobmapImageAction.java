/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.jobmap.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openapitools.client.model.AddJobmapIconImageRequest;
import org.openapitools.client.model.JobmapIconIdDefaultListResponse;
import org.openapitools.client.model.JobmapIconImageInfoResponse;
import org.openapitools.client.model.ModifyJobmapIconImageRequest;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.IconFileDuplicate;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.jobmap.util.JobMapRestClientWrapper;
import com.clustercontrol.jobmap.util.JobmapIconImageCacheEntry;
import com.clustercontrol.jobmap.util.JobmapImageCacheUtil;
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
import com.clustercontrol.utility.settings.jobmap.conv.JobmapImageConv;
import com.clustercontrol.utility.settings.jobmap.xml.Jobmap;
import com.clustercontrol.utility.settings.jobmap.xml.JobmapInfo;
import com.clustercontrol.utility.settings.jobmap.xml.JobmapType;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;
import com.clustercontrol.utility.settings.ui.util.BackupUtil;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.MultiManagerPathUtil;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.XmlMarshallUtil;

/**
 * 
 * @param action 動作
 * @param XMLファイルパス
 * 
 * @version 6.0.0
 * @since 6.0.0
 * 
 */
public class JobmapImageAction {

	/* ロガー */
	private static Logger log = Logger.getLogger(JobmapImageAction.class);
	private static final String[] DEFAULT_IMAGE = {
			"APPROVALJOB_DEFAULT",
			"FILEJOB_DEFAULT",
			"JOBNET_DEFAULT",
			"JOB_DEFAULT",
			"MONITORJOB_DEFAULT",
			"FILECHECKJOB_DEFAULT",
			"JOBLINKRCVJOB_DEFAULT",
			"JOBLINKSENDJOB_DEFAULT",
			"RESOURCEJOB_DEFAULT"
			};

	public JobmapImageAction() throws ConvertorException {
		super();
	}

	/**
	 * ジョブマップ定義情報を全て削除します。<BR>
	 * 
	 * @since 6.0
	 * @return 終了コード
	 * @throws RestConnectFailed 
	 */
	@ClearMethod
	public int clearJobmapImage(){
		log.debug("Start Clear JobmapImage ");

		int ret = 0;
		List<JobmapIconImageCacheEntry> list;
		
		// ジョブマップ用アイコンファイル一覧情報取得
		try {
			list = JobmapImageCacheUtil.getJobmapIconImageList(UtilityManagerUtil.getCurrentManagerName());
		} catch (HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | IconFileNotFound | RestConnectFailed e) {
			log.error(Messages.getString("JobmapImage.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		List<String> iconIdList = new ArrayList<String>();
		for (JobmapIconImageCacheEntry jobmapIconImage : list){
			iconIdList.add(jobmapIconImage.getJobmapIconImage().getIconId());
		}
		
		JobMapRestClientWrapper wrapper =JobMapRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		//デフォルトIDの一覧を取得
		List<JobmapIconIdDefaultListResponse> defaultListRes = null;
		try {
			defaultListRes = wrapper.getJobmapIconIdDefaultList();
		} catch (HinemosUnknown | InvalidRole | InvalidUserPass | RestConnectFailed e) {
			log.error(Messages.getString("JobmapImage.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		Set<String> defaultIdSet = new HashSet<String>();
		for (JobmapIconIdDefaultListResponse rec : defaultListRes){
			defaultIdSet.add(rec.getDefaultId());
		}
		
		List<String> iconIds = new ArrayList<String>();
		for (String  iconId: iconIdList){
			if(defaultIdSet.contains(iconId)){
				log.info(Messages.getString("SettingTools.SkipDefaultIcon") + " : Id : " + iconId);
				continue;// skip
			}
			iconIds.clear();
			iconIds.add(iconId);
			try {
				wrapper.deleteJobmapIconImage(String.join(",", iconIds));
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + String.join(",", iconIds));
			} catch (HinemosUnknown e){
				log.error(Messages.getString("JobmapImage.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidRole | InvalidUserPass | InvalidSetting | IconFileNotFound | RestConnectFailed e) {
				log.error(Messages.getString("JobmapImage.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}

		// 処理の終了
		log.info(Messages.getString("JobmapImage.ClearCompleted"));

		return ret;
	}
	
	/**
	 *	定義情報をマネージャから読み出します。
	 * @return
	 */
	@ExportMethod
	public int exportJobmapImage(String xmlFile) {
		log.debug("Start Export Jobmap Image ");
		int ret = 0;

		boolean backup = false;
		String directoryPath = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML);
		if (directoryPath != null && (xmlFile.length() > directoryPath.length())) {
			backup = xmlFile.substring(directoryPath.length()).contains(BackupUtil.getBackupFolder());
		}

		List<JobmapIconImageCacheEntry> list;
		
		// ジョブマップ用アイコンファイル一覧情報取得
		try {
			list = JobmapImageCacheUtil.getJobmapIconImageList(UtilityManagerUtil.getCurrentManagerName());
		} catch (HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | IconFileNotFound | RestConnectFailed e) {
			log.error(Messages.getString("JobmapImage.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		//list = new ArrayList<>();
		
		//XML作成
		Jobmap jobmap = new Jobmap();
		for (JobmapIconImageCacheEntry jobmaImage : list) {
			try{
				jobmap.addJobmapInfo(JobmapImageConv.getJobmap(jobmaImage.getJobmapIconImage()));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + jobmaImage.getJobmapIconImage().getIconId());
				
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}

		try {
			jobmap.setCommon(JobmapImageConv.versionJobmapDto2Xml(Config.getVersion()));
			// スキーマ情報のセット
			jobmap.setSchemaInfo(JobmapImageConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				jobmap.marshal(osw);
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
		}
		
		// Imageファイル保存
		byte[] fileData=null;
		String path = null;
		String basePath = getFolderPath(backup);
		
		for (JobmapIconImageCacheEntry jobmaImage : list) {
			fileData = jobmaImage.getFiledata();
			path = getFilePath(jobmaImage.getJobmapIconImage().getIconId(), basePath);
			log.debug("path = " + path);
			FileOutputStream fileOutStm = null;
			try {
				fileOutStm = new FileOutputStream(path);
				fileOutStm.write(fileData);
			} catch (IOException e) {
				log.error(Messages.getString("JobMapImage.ExportFailed") + " IconId=" + jobmaImage.getJobmapIconImage().getIconId(),e);//処理続行
				ret = SettingConstants.ERROR_INPROCESS;
			}finally{
				try {
					if(fileOutStm != null)
						fileOutStm.close();
				} catch (IOException e) {
				}
				fileOutStm = null;
			}
		}
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		
		log.debug("End Export JobmapImage");
		return ret;
	}
	
	/**
	 * 定義情報をマネージャに投入します。
	 * 
	 * @return
	 */
	@ImportMethod
	public int importJobmapImage(String xmlFile){
		log.debug("Start Import Jobmap Image ");
		int ret = 0;
		
		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			getLogger().debug("End Import JobmapImage (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		// XMLファイルからの読み込み
		JobmapType jobmap = null;
		try {
			jobmap = XmlMarshallUtil.unmarshall( JobmapType.class, new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import JobmapImage (Error)");
			return ret;
		}
		
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(jobmap.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		List<String> objectIdList = new ArrayList<String>();
		JobMapRestClientWrapper wrapper = JobMapRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());

		// 重複確認用の一覧を取得
		 List<JobmapIconImageInfoResponse> retList = null;
		try {
			retList = wrapper.getJobmapIconImageList();
		} catch (HinemosUnknown | InvalidRole | InvalidUserPass | IconFileNotFound | RestConnectFailed e) {
			log.error(Messages.getString("SettingTools.EndWithErrorCode") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		}
		Set<String> exitIdSet = new HashSet<String>();
		for (JobmapIconImageInfoResponse info : retList) {
			exitIdSet.add(info.getIconId());
		}

		// インポート
		for (JobmapInfo info : jobmap.getJobmapInfo()) {
			JobmapIconImageInfoResponse jobmapImage =  JobmapImageConv.getJobmapInfoDto(info);
			File imageFile = JobmapImageConv.getJobmapImageFile(jobmapImage.getIconId());
			try {
				if(!exitIdSet.contains(info.getIconId())){
					// 新規IDなら追加
					AddJobmapIconImageRequest reqDto = new AddJobmapIconImageRequest();
					RestClientBeanUtil.convertBean(jobmapImage, reqDto);
					wrapper.addJobmapIconImage(imageFile ,reqDto);
					objectIdList.add(info.getIconId());
					log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + info.getIconId());
				}else{
					// ID重複時、インポート処理方法を確認して、必要なら変更
					if(!ImportProcessMode.isSameprocess()){
						String[] args = {info.getIconId()};
						UtilityProcessDialog dialog = UtilityDialogInjector.createImportProcessDialog(
								null, Messages.getString("message.import.confirm2", args));
						ImportProcessMode.setProcesstype(dialog.open());
						ImportProcessMode.setSameprocess(dialog.getToggleState());
					}
					if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.UPDATE){
						ModifyJobmapIconImageRequest reqDto = new ModifyJobmapIconImageRequest();
						RestClientBeanUtil.convertBean(jobmapImage, reqDto);
						wrapper.modifyJobmapIconImage(jobmapImage.getIconId(), imageFile,reqDto);
						objectIdList.add(info.getIconId());
						log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + info.getIconId());
					} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
						log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + info.getIconId());
					} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
						log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
						return ret;
					}
				}
			} catch (IconFileDuplicate e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()) + " : " +jobmapImage.getIconId());
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (HinemosUnknown e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()) + " : " +jobmapImage.getIconId());
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidRole e) {
				log.error(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()) + " : " +jobmapImage.getIconId());
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidUserPass e) {
				log.error(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()) + " : " +jobmapImage.getIconId());
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (RestConnectFailed e){
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()) + " : " +jobmapImage.getIconId());
				return SettingConstants.ERROR_INPROCESS; 
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()) + " : " +jobmapImage.getIconId());
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		//オブジェクト権限同時インポート
		importObjectPrivilege(HinemosModuleConstant.JOBMAP_IMAGE_FILE, objectIdList);
		
		//差分削除
		checkDelete(jobmap);

		//インポート終了時にキャッシュをリフレッシュ
		JobmapImageCacheUtil iconCache = JobmapImageCacheUtil.getInstance();
		iconCache.refresh();
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Import JobmapImage ");
		
		return ret;
	}
	
	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.jobmap.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = JobmapImageConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.jobmap.xml.SchemaInfo sci = JobmapImageConv.getSchemaVersion();
		
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	protected void checkDelete(JobmapType xmlElements){
		List<JobmapIconImageCacheEntry> subList;
		
		// ジョブマップ用アイコンファイル一覧情報取得
		try {
			subList = JobmapImageCacheUtil.getJobmapIconImageList(UtilityManagerUtil.getCurrentManagerName());
		} catch (HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | IconFileNotFound | RestConnectFailed e) {
			log.error(Messages.getString("SettingTools.EndWithErrorCode") + " : " + HinemosMessage.replace(e.getMessage()));
			return;
		}
		JobMapRestClientWrapper wrapper = JobMapRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		
		//デフォルトIDの一覧を取得
		List<JobmapIconIdDefaultListResponse> defaultListRes = null;
		try {
			defaultListRes = wrapper.getJobmapIconIdDefaultList();
		} catch (HinemosUnknown | InvalidRole | InvalidUserPass | RestConnectFailed e) {
			log.error(Messages.getString("SettingTools.EndWithErrorCode") + " : " + HinemosMessage.replace(e.getMessage()));
			return;
		}
		Set<String> defaultIdSet = new HashSet<String>();
		for (JobmapIconIdDefaultListResponse rec : defaultListRes){
			defaultIdSet.add(rec.getDefaultId());
		}
		
		// 既設ID一覧を抽出（デフォルトは除外）
		List<JobmapIconImageCacheEntry> listExclusion = new ArrayList<JobmapIconImageCacheEntry>();
		for (JobmapIconImageCacheEntry jobmapIconImage : subList){
			if(defaultIdSet.contains(jobmapIconImage.getJobmapIconImage().getIconId())){
				continue;// skip
			}
			listExclusion.add(jobmapIconImage);
		}
		
		List<JobmapInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getJobmapInfo()));
		
		for(JobmapIconImageCacheEntry mgrInfo: new ArrayList<>(listExclusion)){
			for(JobmapInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getJobmapIconImage().getIconId().equals(xmlElement.getIconId())){
					listExclusion.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
			
		}
		
		if(listExclusion.size() > 0){
			for(JobmapIconImageCacheEntry info: listExclusion){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getJobmapIconImage().getIconId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}

				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					try {
						JobMapRestClientWrapper
							.getWrapper(UtilityManagerUtil.getCurrentManagerName())
							.deleteJobmapIconImage(info.getJobmapIconImage().getIconId());
						getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getJobmapIconImage().getIconId());
					} catch (Exception e1) {
						getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getJobmapIconImage().getIconId());
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
					return;
				}
				
			}
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
		log.debug("Start Differrence JobmapImage ");

		int ret = 0;
		// XMLファイルからの読み込み
		JobmapType jobmap1 = null;
		JobmapType jobmap2 = null;
		try {
			jobmap1 = XmlMarshallUtil.unmarshall(JobmapType.class,new InputStreamReader(new FileInputStream(xmlFile1), "UTF-8"));
			jobmap2 = XmlMarshallUtil.unmarshall(JobmapType.class,new InputStreamReader(new FileInputStream(xmlFile2), "UTF-8"));
			sort(jobmap1);
			sort(jobmap2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence JobmapImage (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(jobmap1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		if(!checkSchemaVersion(jobmap2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(jobmap1, jobmap2, JobmapType.class, resultA);
			assert resultA.getResultBs().size() == 1;
			
			if(diff){
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
						log.warn(String.format("Fail to delete file. %s", f.getAbsolutePath()));
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
		
		getLogger().debug("End Differrence JobmapImage");

		return ret;
	}
	
	private void sort(JobmapType report) {
		JobmapInfo[] infoList = report.getJobmapInfo();
		Arrays.sort(infoList,
				new Comparator<JobmapInfo>() {
					@Override
					public int compare(JobmapInfo info1, JobmapInfo info2) {
						return info1.getIconId().compareTo(info2.getIconId());
					}
				});
		report.setJobmapInfo(infoList);
	}
	
	public Logger getLogger(){
		return log;
	}
	
	private String getFilePath(String iconId, String folderPath){
		StringBuffer sb = new StringBuffer();
		sb.append(folderPath);
		sb.append(File.separator);
		sb.append(iconId);
		
		return sb.toString();
	}
	
	private String getFolderPath(boolean backup){
		StringBuffer sb = new StringBuffer();
		
		sb.append(MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML));
		sb.append(File.separator);
		
		if (backup) {
			sb.append(BackupUtil.getBackupFolder());
			sb.append(File.separator);
			sb.append(MultiManagerPathUtil.getPreference(SettingToolsXMLPreferencePage.VALUE_JOBMAP_IMAGE_FOLDER));
			sb.append("_" + BackupUtil.getTimeStampString());
		} else {
			sb.append(MultiManagerPathUtil.getPreference(SettingToolsXMLPreferencePage.VALUE_JOBMAP_IMAGE_FOLDER));
		}
		isExsitsAndCreate(sb.toString());
		
		return sb.toString();
	}
	
	protected void isExsitsAndCreate(String directoryPath){
		File dir = new File(directoryPath);
		if(!dir.exists() && !directoryPath.endsWith("null")){
			if (!dir.mkdir())
				log.warn(String.format("Fail to create Directory. %s", dir.getAbsolutePath()));
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
}
