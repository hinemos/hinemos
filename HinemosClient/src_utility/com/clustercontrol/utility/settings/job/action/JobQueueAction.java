/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;

import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.MessageConstant;
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
import com.clustercontrol.utility.settings.job.conv.QueueConv;
import com.clustercontrol.utility.settings.job.xml.JobQueueInfo;
import com.clustercontrol.utility.settings.job.xml.JobQueueList;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.platform.conv.CommonConv;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidSetting_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidUserPass_Exception;
import com.clustercontrol.ws.jobmanagement.JobQueueSetting;
import com.clustercontrol.ws.jobmanagement.JobQueueSettingViewInfo;
import com.clustercontrol.ws.jobmanagement.JobQueueSettingViewInfoListItem;

public class JobQueueAction {

	/*ロガー*/
	protected static Logger log = Logger.getLogger(JobQueueAction.class);

	private QueueConv queueConv = new QueueConv();

	public JobQueueAction() throws ConvertorException {
		super();
	}

	/**
	 * 同時実行制御キュー情報をマネージャに設定します。
	 * 
	 * @param インポートするXML
	 * @param マネージャへのコネクション
	 * 
	 */
	@ImportMethod
	public int importJobQueue(String xmlFile) {
		log.debug("Start Import JobQueue : " + xmlFile);

		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			log.debug("End Import JobQueue (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}

		int ret = SettingConstants.SUCCESS;
		//スケジュール情報のXMからBeanに取り込みます。
		JobQueueList queueList = null;
		try {
			queueList = JobQueueList.unmarshal(new InputStreamReader(
					new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import JobQueue (Error)");
			return ret;
		}

		/* スキーマのバージョンチェック*/
		if (!checkSchemaVersion(queueList.getSchemaInfo())) {
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		List<String> objectIdList = new ArrayList<String>();

		//XMLからDTOに変換
		List<JobQueueSetting> queues = queueConv.queueXml2Dto(queueList);

		JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		for (JobQueueSetting queue : queues) {
			try {
				wrapper.addJobQueue(queue);
				objectIdList.add(queue.getQueueId());
			} catch (InvalidSetting_Exception e) {
				if (!e.getMessage().contains(MessageConstant.MESSAGE_JOBQUEUE_DUPLICATION.toString())) {
					log.info(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
					continue;
				}
				//重複時、インポート処理方法を確認する
				if(!ImportProcessMode.isSameprocess()) {
					String[] arg = {queue.getQueueId()};
					UtilityProcessDialog dialog = UtilityDialogInjector.createImportProcessDialog(
							null, Messages.getString("message.import.confirm2", arg));
					ImportProcessMode.setProcesstype(dialog.open());
					ImportProcessMode.setSameprocess(dialog.getToggleState());
				}

				if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.UPDATE) {
					try {
						wrapper.modifyJobQueue(queue);
						objectIdList.add(queue.getQueueId());
						getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + queue.getQueueId());
					} catch (Exception e1) {
						getLogger().warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
				} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.SKIP) {
					getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + queue.getQueueId());
				} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
					getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
					ret = SettingConstants.ERROR_INPROCESS;
					break;
				}
			} catch (InvalidRole_Exception e) {
				log.info(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidUserPass_Exception e) {
				log.info(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (Exception e) {
				log.info(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}

		//オブジェクト権限同時インポート
		importObjectPrivilege(HinemosModuleConstant.JOB_QUEUE, objectIdList);

		//差分削除
		checkDelete(queues);

		// 処理の終了
		if (ret == SettingConstants.SUCCESS) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Import JobQueue : " + ret);

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
		int res = queueConv.checkSchemaVersion(schmaversion.getSchemaType(),
				schmaversion.getSchemaVersion(),
				schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.job.xml.SchemaInfo sci = queueConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	/**
	 * 情報をマネージャからエクスポートします。
	 * 
	 * @param 出力するXMLファイル
	 * @return 終了コード
	 */
	@ExportMethod
	public int exportJobQueue(String fileName) {
		log.debug("Start Export JobQueue : " + fileName);

		int ret = SettingConstants.SUCCESS;
		//マネージャから同時実行制御キューのビューを取得する。
		JobQueueSettingViewInfo viewInfo;
		try {
			//マネージャからキューの一覧(ビュー)を取得
			viewInfo = JobEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobQueueSettingViewInfo(null);
		} catch (Exception e1) {
			log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export JobQueue : " + fileName +"(Error)");
			return ret;
		}

		com.clustercontrol.utility.settings.job.xml.JobQueueList queueXML
		= new com.clustercontrol.utility.settings.job.xml.JobQueueList();

		//共通情報のセット
		queueXML.setCommon(CommonConv.versionJobDto2Xml(Config.getVersion()));

		//スキーマ情報のセット
		queueXML.setSchemaInfo(queueConv.getSchemaVersion());

		if(viewInfo != null){
			//ジョブ情報のセット(ジョブツリーからXML用のリストに変換)
			queueXML.setJobQueueInfo(queueConv.view2queueXML(viewInfo));
		}

		try (FileOutputStream output = new FileOutputStream(fileName)) {
			//マーシャリング
			queueXML.marshal(new OutputStreamWriter(output , "UTF-8"));
		} catch (Exception e) {
			log.warn(Messages.getString("SettingTools.ExportFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
		}

		// 処理の終了
		if (ret == SettingConstants.SUCCESS) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		}else{
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
		}
		log.debug("End Export JobQueue");
		return ret;
	}

	@ClearMethod
	public int clearJobQueue() {
		log.debug("Start Clear JobQueue");

		int ret = SettingConstants.SUCCESS;
		//マネージャから同時実行制御キューのビューを取得する。
		JobQueueSettingViewInfo viewInfo;
		JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		try {
			//マネージャからキューの一覧(ビュー)を取得
			viewInfo = wrapper.getJobQueueSettingViewInfo(null);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export JobQueue : (Error)");
			return ret;
		}

		for (JobQueueSettingViewInfoListItem queue : viewInfo.getItems()) {
			try {
				wrapper.deleteJobQueue(queue.getQueueId());
			} catch (WebServiceException e) {
				getLogger().error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (Exception e) {
				getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		// 処理の終了
		if (ret == SettingConstants.SUCCESS) {
			log.info(Messages.getString("SettingTools.ClearCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
		}
		log.debug("End Clear JobQueue");

		return ret;
	}

	/**
	 * 差分比較処理を行います。
	 * XMLファイル２つ（filePath1,filePath2）を比較する。
	 * 差分がある場合：差分をＣＳＶファイルで出力する。
	 * 差分がない場合：出力しない。または、すでに存在している同一名のＣＳＶファイルを削除する。
	 * @param xmlQueue 同時実行制御のXMLファイル1
	 * @param xmlQueue2 同時実行制御のXMLファイル1
	 * @return 終了コード
	 * @throws ConvertorException
	 */
	@DiffMethod
	public int diffXml(String xmlQueue, String xmlQueue2) throws ConvertorException {

		log.debug("Start Differrence JobQueue ");

		// 返り値変数(条件付き正常終了用）
		int ret = SettingConstants.SUCCESS;

		JobQueueList queueList1 = null;
		JobQueueList queueList2 = null;

		// XMLファイルからの読み込み
		try {
			queueList1 = JobQueueList.unmarshal(new InputStreamReader(new FileInputStream(xmlQueue), "UTF-8"));
			queueList2 = JobQueueList.unmarshal(new InputStreamReader(new FileInputStream(xmlQueue2), "UTF-8"));
			sort(queueList1);
			sort(queueList2);

		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence JobQueue (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(queueList1.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(queueList2.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		ResultA resultA = new ResultA();
		//比較処理に渡す
		boolean diff = DiffUtil.diffCheck2(queueList1, queueList2, JobQueueList.class, resultA);
		assert resultA.getResultBs().size() == 1;

		if(diff){
			ret += SettingConstants.SUCCESS_DIFF_1;
		}

		FileOutputStream fos = null;
		try {
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlQueue2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			} else {
				//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
				File f = new File(xmlQueue2 + ".csv");
				if (f.exists()) {
					if (!f.delete()) {
						log.warn(String.format("Fail to delete File. %s", f.getAbsolutePath()));
					}
				}
			}
		} catch (Exception e) {
			log.error("unexpected: ", e);
			ret = SettingConstants.ERROR_INPROCESS;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}

		// 処理の終了
		if ((ret >= SettingConstants.SUCCESS) && (ret<=SettingConstants.SUCCESS_MAX)) {
			log.info(Messages.getString("SettingTools.DiffCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}

		log.debug("End Differrence JobQueue");

		return ret;
	}

	private void sort(JobQueueList queueList) {
		JobQueueInfo[] infoList = queueList.getJobQueueInfo();
		Arrays.sort(
				infoList,
				new Comparator<JobQueueInfo>() {
					@Override
					public int compare(JobQueueInfo obj1, JobQueueInfo obj2) {
						return obj1.getQueueId().compareTo(obj2.getQueueId());
					}
				});
		queueList.setJobQueueInfo(infoList);
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

	protected void checkDelete(List<JobQueueSetting> xmlElements){
		JobQueueSettingViewInfo subList = null;
		JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		try {
			subList = wrapper.getJobQueueSettingViewInfo(null);
		} catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}

		if(subList == null || subList.getItems().size() <= 0) {
			return;
		}

		for(JobQueueSettingViewInfoListItem mgrInfo: new ArrayList<>(subList.getItems())) {
			for(JobQueueSetting xmlElement: new ArrayList<>(xmlElements)) {
				if(mgrInfo.getQueueId().equals(xmlElement.getQueueId())){
					subList.getItems().remove(mgrInfo);
					xmlElements.remove(xmlElement);
					break;
				}
			}
		}

		for(JobQueueSettingViewInfoListItem info: subList.getItems()){
			//マネージャのみに存在するデータがあった場合の削除方法を確認する
			if(!DeleteProcessMode.isSameprocess()) {
				String[] args = {info.getQueueId()};
				DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
						null, Messages.getString("message.delete.confirm4", args));
				DeleteProcessMode.setProcesstype(dialog.open());
				DeleteProcessMode.setSameprocess(dialog.getToggleState());
			}

			if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE) {
				try {
					wrapper.deleteJobQueue(info.getQueueId());
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getQueueId());
				} catch (Exception e) {
					getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				}
			} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP) {
				getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getQueueId());
			} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
				getLogger().info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
				return;
			}
		}
	}
}
