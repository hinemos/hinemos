/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openapitools.client.model.AddJobLinkSendSettingRequest;
import org.openapitools.client.model.ImportJobLinkSendRecordRequest;
import org.openapitools.client.model.ImportJobLinkSendRequest;
import org.openapitools.client.model.ImportJobLinkSendResponse;
import org.openapitools.client.model.JobLinkSendSettingResponse;
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
import com.clustercontrol.utility.settings.job.conv.JobLinkSendConv;
import com.clustercontrol.utility.settings.job.xml.JobLinkSendInfo;
import com.clustercontrol.utility.settings.job.xml.JobLinkSendList;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.platform.conv.CommonConv;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.ImportClientController;
import com.clustercontrol.utility.util.ImportRecordConfirmer;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;
import com.clustercontrol.utility.util.XmlMarshallUtil;

/**
 * JOB管理ジョブ連携送信情報を取得、設定、削除します。<br>
 * XMLファイルに定義されたジョブ連携送信情報をマネージャに反映させるクラス<br>
 */
public class JobLinkSendAction {

	/* ロガー */
	protected static Logger log = Logger.getLogger(JobLinkSendAction.class);

	private JobLinkSendConv jobLinkSendConv = new JobLinkSendConv();

	public JobLinkSendAction() throws ConvertorException {
		super();
	}

	/**
	 * ジョブ連携送信情報をマネージャに設定します。
	 * 
	 * @param インポートするXML
	 * @param マネージャへのコネクション
	 * 
	 */
	@ImportMethod
	public int importJobLinkSend(String xmlFile) {

		log.debug("Start Import JobLinkSend");

		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			log.debug("End Import JobQueue (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}

		int ret = SettingConstants.SUCCESS;
		// ジョブ連携送信情報のXMLからBeanに取り込みます。
		JobLinkSendList jobLinkSendList = null;
		try {
			jobLinkSendList = XmlMarshallUtil.unmarshall(JobLinkSendList.class,new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import JobLinkSend (Error)");
			return ret;
		}

		/* スキーマのバージョンチェック */
		if (!checkSchemaVersion(jobLinkSendList.getSchemaInfo())) {
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		List<String> objectIdList = new ArrayList<String>();

		// レコードの登録（ジョブ連携送信設定）
		ImportJobLinkSendRecordConfirmer jobLinkSendConfirmer = new ImportJobLinkSendRecordConfirmer(log,
				jobLinkSendList.getJobLinkSendInfo());
		int jobLinkSendConfirmerRet = jobLinkSendConfirmer.executeConfirm();
		if (jobLinkSendConfirmerRet != 0) {
			ret = jobLinkSendConfirmerRet;
		}
		// レコードの登録（ジョブ連携送信設定）
		if (!(jobLinkSendConfirmer.getImportRecDtoList().isEmpty())) {
			ImportJobLinkSendClientController jobLinkSendController = new ImportJobLinkSendClientController(log,
					Messages.getString("jobLinkSend"), jobLinkSendConfirmer.getImportRecDtoList(), true);
			int jobLinkSendControllerRet = jobLinkSendController.importExecute();
			for (RecordRegistrationResponse rec : jobLinkSendController.getImportSuccessList()) {
				objectIdList.add(rec.getImportKeyValue());
			}
			if (jobLinkSendControllerRet != 0) {
				ret = jobLinkSendControllerRet;
			}
		}
		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}

		// オブジェクト権限同時インポート
		importObjectPrivilege(HinemosModuleConstant.JOBLINK_SEND_SETTING, objectIdList);

		// 差分削除
		checkDelete(jobLinkSendList);

		// 処理の終了
		if (ret == SettingConstants.SUCCESS) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Import JobLinkSend : " + ret);

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
		/* スキーマのバージョンチェック */
		int res = jobLinkSendConv.checkSchemaVersion(schmaversion.getSchemaType(), schmaversion.getSchemaVersion(),
				schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.job.xml.SchemaInfo sci = jobLinkSendConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(),
				sci.getSchemaRevision());
	}

	/**
	 * ジョブ連携送信情報をマネージャからエクスポートします。
	 * 
	 * @param 出力するXMLファイル
	 * @return 終了コード
	 * @throws HinemosUnknown
	 */
	@ExportMethod
	public int exportJobLinkSend(String fileName) throws HinemosUnknown {
		log.debug("Start Export JobLinkSend : " + fileName);

		int ret = SettingConstants.SUCCESS;

		// マネージャからジョブ連携送信設定を取得する。
		List<JobLinkSendSettingResponse> jobLinkSendList;
		try {
			jobLinkSendList = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getJobLinkSendSettingList(null);
			if (null == jobLinkSendList) {
				log.error(Messages.getString("SettingTools.EndWithErrorCode"));
				return SettingConstants.ERROR_INPROCESS;
			}
			Collections.sort(jobLinkSendList, new Comparator<JobLinkSendSettingResponse>() {
				@Override
				public int compare(JobLinkSendSettingResponse jobLinkSend1, JobLinkSendSettingResponse jobLinkSend2) {
					return jobLinkSend1.getJoblinkSendSettingId().compareTo(jobLinkSend2.getJoblinkSendSettingId());
				}
			});
		} catch (Exception e1) {
			log.error(
					Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export JobLinkSend : " + fileName + "(Error)");
			return ret;
		}

		JobLinkSendList jobLinkSendXML = new JobLinkSendList();

		// 共通情報のセット
		jobLinkSendXML.setCommon(CommonConv.versionJobDto2Xml(Config.getVersion()));

		// スキーマ情報のセット
		jobLinkSendXML.setSchemaInfo(jobLinkSendConv.getSchemaVersion());

		// ジョブ情報のセット(ジョブツリーからXML用のリストに変換)
		// findbugs対応 不要なnullチェックを追加
		jobLinkSendXML.setJobLinkSendInfo(jobLinkSendConv.jobLinkDto2XML(jobLinkSendList));

		try (FileOutputStream output = new FileOutputStream(fileName)) {
			// マーシャリング
			jobLinkSendXML.marshal(new OutputStreamWriter(output, "UTF-8"));
		} catch (Exception e) {
			log.warn(Messages.getString("SettingTools.ExportFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
		}

		// 処理の終了
		if (ret == SettingConstants.SUCCESS) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Export JobLinkSend");
		return ret;
	}

	@ClearMethod
	public int clearJobLinkSend() {
		log.debug("Start Clear JobLinkSend");

		int ret = SettingConstants.SUCCESS;

		// マネージャからジョブ連携送信設定を取得する。
		List<JobLinkSendSettingResponse> jobLinkSendList;
		JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		try {
			jobLinkSendList = wrapper.getJobLinkSendSettingList(null);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export JobLinkSend : (Error)");
			return ret;
		}

		for (JobLinkSendSettingResponse jobLinkSend : jobLinkSendList) {
			try {
				wrapper.deleteJobLinkSendSetting(jobLinkSend.getJoblinkSendSettingId());
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " id:" + jobLinkSend.getJoblinkSendSettingId());
			} catch (Exception e) {
				getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " id:"
						+ jobLinkSend.getJoblinkSendSettingId() + " , " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		// 処理の終了
		if (ret == SettingConstants.SUCCESS) {
			log.info(Messages.getString("SettingTools.ClearCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Clear JobLinkSend");

		return ret;
	}

	/**
	 * 差分比較処理を行います。 XMLファイル２つ（filePath1,filePath2）を比較する。
	 * 差分がある場合：差分をＣＳＶファイルで出力する。 差分がない場合：出力しない。または、すでに存在している同一名のＣＳＶファイルを削除する。
	 * 
	 * @param xmlJobLinkSend1
	 *            ジョブ連携送信設定のXMLファイル1
	 * @param xmlJobLinkSend2
	 *            ジョブ連携送信設定のXMLファイル1
	 * @return 終了コード
	 * @throws ConvertorException
	 */
	@DiffMethod
	public int diffXml(String xmlJobLinkSend1, String xmlJobLinkSend2) throws ConvertorException {

		log.debug("Start Differrence JobLinkSend ");

		// 返り値変数(条件付き正常終了用）
		int ret = SettingConstants.SUCCESS;

		JobLinkSendList jobLinkSendList1 = null;
		JobLinkSendList jobLinkSendList2 = null;

		// XMLファイルからの読み込み
		try {
			jobLinkSendList1 = XmlMarshallUtil.unmarshall(JobLinkSendList.class,new InputStreamReader(new FileInputStream(xmlJobLinkSend1), "UTF-8"));
			jobLinkSendList2 = XmlMarshallUtil.unmarshall(JobLinkSendList.class,new InputStreamReader(new FileInputStream(xmlJobLinkSend2), "UTF-8"));
			sort(jobLinkSendList1);
			sort(jobLinkSendList2);

		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence JobLinkSend (Error)");
			return ret;
		}

		/* スキーマのバージョンチェック */
		if (!checkSchemaVersion(jobLinkSendList1.getSchemaInfo())) {
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if (!checkSchemaVersion(jobLinkSendList2.getSchemaInfo())) {
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		ResultA resultA = new ResultA();
		// 比較処理に渡す
		boolean diff = DiffUtil.diffCheck2(jobLinkSendList1, jobLinkSendList2, JobLinkSendList.class, resultA);
		assert resultA.getResultBs().size() == 1;

		if (diff) {
			ret += SettingConstants.SUCCESS_DIFF_1;
		}

		FileOutputStream fos = null;
		try {
			// 差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlJobLinkSend2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			} else {
				// 差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
				File f = new File(xmlJobLinkSend2 + ".csv");
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
		if ((ret >= SettingConstants.SUCCESS) && (ret <= SettingConstants.SUCCESS_MAX)) {
			log.info(Messages.getString("SettingTools.DiffCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}

		log.debug("End Differrence JobLinkSend");

		return ret;
	}

	private void sort(JobLinkSendList jobLinkSendList) {
		JobLinkSendInfo[] infoList = jobLinkSendList.getJobLinkSendInfo();
		Arrays.sort(infoList, new Comparator<JobLinkSendInfo>() {
			@Override
			public int compare(JobLinkSendInfo obj1, JobLinkSendInfo obj2) {
				return obj1.getJoblinkSendSettingId().compareTo(obj2.getJoblinkSendSettingId());
			}
		});
		jobLinkSendList.setJobLinkSendInfo(infoList);
	}

	/**
	 * オブジェクト権限同時インポート
	 * 
	 * @param objectType
	 * @param objectIdList
	 */
	protected void importObjectPrivilege(String objectType, List<String> objectIdList) {
		if (ImportProcessMode.isSameObjectPrivilege()) {
			ObjectPrivilegeAction.importAccessExtraction(ImportProcessMode.getXmlObjectPrivilege(), objectType,
					objectIdList, log);
		}
	}

	public Logger getLogger() {
		return log;
	}

	protected void checkDelete(JobLinkSendList xmlElements) {
		List<JobLinkSendSettingResponse> subList = null;
		JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		try {
			subList = JobRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getJobLinkSendSettingList(null);
		} catch (Exception e) {
			getLogger().error(
					Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}

		if (subList == null || subList.size() <= 0) {
			return;
		}

		List<JobLinkSendInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getJobLinkSendInfo()));

		for (JobLinkSendSettingResponse mgrInfo : new ArrayList<>(subList)) {
			for (JobLinkSendInfo xmlElement : new ArrayList<>(xmlElementList)) {
				if (mgrInfo.getJoblinkSendSettingId().equals(xmlElement.getJoblinkSendSettingId())) {
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}

		if (subList.size() > 0) {
			for (JobLinkSendSettingResponse info : subList) {
				if (!DeleteProcessMode.isSameprocess()) {
					String[] args = { info.getJoblinkSendSettingId() };
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(null,
							Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}

				if (DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE) {
					try {
						List<String> args = new ArrayList<>();
						args.add(info.getJoblinkSendSettingId());
						wrapper.deleteJobLinkSendSetting(String.join(",", args));
						getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : "
								+ info.getJoblinkSendSettingId());
					} catch (Exception e) {
						getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : "
								+ HinemosMessage.replace(e.getMessage()));
					}
				} else if (DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP) {
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : "
							+ info.getJoblinkSendSettingId());
				} else if (DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
					return;
				}
			}
		}
	}

	/**
	 * ジョブ連携送信設定 インポート向けのレコード確認用クラス
	 * 
	 */
	protected class ImportJobLinkSendRecordConfirmer
			extends ImportRecordConfirmer<JobLinkSendInfo, ImportJobLinkSendRecordRequest, String> {

		public ImportJobLinkSendRecordConfirmer(Logger logger, JobLinkSendInfo[] importRecDtoList) {
			super(logger, importRecDtoList);
		}

		@Override
		protected ImportJobLinkSendRecordRequest convertDtoXmlToRestReq(JobLinkSendInfo xmlDto)
				throws HinemosUnknown, InvalidSetting {
			AddJobLinkSendSettingRequest dto = jobLinkSendConv.jobLinkSendXml2Dto(xmlDto);
			ImportJobLinkSendRecordRequest dtoRec = new ImportJobLinkSendRecordRequest();
			dtoRec.setImportData(new AddJobLinkSendSettingRequest());
			RestClientBeanUtil.convertBean(dto, dtoRec.getImportData());

			dtoRec.setImportKeyValue(dtoRec.getImportData().getJoblinkSendSettingId());
			return dtoRec;
		}

		@Override
		protected Set<String> getExistIdSet() throws Exception {
			Set<String> retSet = new HashSet<String>();
			List<JobLinkSendSettingResponse> jobLinkSendInfoList = JobRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getJobLinkSendSettingList(null);
			for (JobLinkSendSettingResponse rec : jobLinkSendInfoList) {
				retSet.add(rec.getJoblinkSendSettingId());
			}
			return retSet;
		}

		@Override
		protected boolean isLackRestReq(ImportJobLinkSendRecordRequest restDto) {
			return (restDto == null || restDto.getImportData().getJoblinkSendSettingId() == null
					|| restDto.getImportData().getJoblinkSendSettingId().equals(""));
		}

		@Override
		protected String getKeyValueXmlDto(JobLinkSendInfo xmlDto) {
			return xmlDto.getJoblinkSendSettingId();
		}

		@Override
		protected String getId(JobLinkSendInfo xmlDto) {
			return xmlDto.getJoblinkSendSettingId();
		}

		@Override
		protected void setNewRecordFlg(ImportJobLinkSendRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
	}

	/**
	 * ジョブ連携送信設定 インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportJobLinkSendClientController extends
			ImportClientController<ImportJobLinkSendRecordRequest, ImportJobLinkSendResponse, RecordRegistrationResponse> {

		public ImportJobLinkSendClientController(Logger logger, String importInfoName,
				List<ImportJobLinkSendRecordRequest> importRecList, boolean displayFailed) {
			super(logger, importInfoName, importRecList, displayFailed);
		}

		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportJobLinkSendResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportJobLinkSendResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportJobLinkSendRecordRequest importRec) {
			return importRec.getImportKeyValue();
		};

		@Override
		protected String getResKeyValue(RecordRegistrationResponse responseRec) {
			return responseRec.getImportKeyValue();
		};

		@Override
		protected boolean isResNormal(RecordRegistrationResponse responseRec) {
			return (responseRec.getResult() == ResultEnum.NORMAL);
		};

		@Override
		protected boolean isResSkip(RecordRegistrationResponse responseRec) {
			return (responseRec.getResult() == ResultEnum.SKIP);
		};

		@Override
		protected ImportJobLinkSendResponse callImportWrapper(List<ImportJobLinkSendRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportJobLinkSendRequest reqDto = new ImportJobLinkSendRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.importJobLinkSend(reqDto);
		}

		@Override
		protected String getRestExceptionMessage(RecordRegistrationResponse responseRec) {
			if (responseRec.getExceptionInfo() != null) {
				return responseRec.getExceptionInfo().getException() + ":"
						+ responseRec.getExceptionInfo().getMessage();
			}
			return null;
		};

		@Override
		protected void setResultLog(RecordRegistrationResponse responseRec) {
			String keyValue = getResKeyValue(responseRec);
			if (isResNormal(responseRec)) {
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + this.importInfoName + ":"
						+ keyValue);
			} else if (isResSkip(responseRec)) {
				log.info(Messages.getString("SettingTools.SkipSystemRole") + " : " + this.importInfoName + ":"
						+ keyValue);
			} else {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + this.importInfoName + ":" + keyValue
						+ " : " + HinemosMessage.replace(getRestExceptionMessage(responseRec)));
			}
		}
	}

}
