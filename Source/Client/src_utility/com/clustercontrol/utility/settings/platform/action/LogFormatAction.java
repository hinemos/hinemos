/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.platform.action;

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
import org.openapitools.client.model.AddLogFormatRequest;
import org.openapitools.client.model.ImportLogFormatRecordRequest;
import org.openapitools.client.model.ImportLogFormatRequest;
import org.openapitools.client.model.ImportLogFormatResponse;
import org.openapitools.client.model.LogFormatResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.hub.util.HubRestClientWrapper;
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
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.conv.CommonConv;
import com.clustercontrol.utility.settings.platform.conv.LogFormatConv;
import com.clustercontrol.utility.settings.platform.xml.LogFormat;
import com.clustercontrol.utility.settings.platform.xml.LogFormatInfo;
import com.clustercontrol.utility.settings.platform.xml.LogFormatType;
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
 * ログフォーマット定義情報をインポート・エクスポート・削除するアクションクラス<br>
 *
 * @version 6.1.0
 * @since 6.0.0
 *
 */
public class LogFormatAction {

	protected static Logger log = Logger.getLogger(LogFormatAction.class);

	public LogFormatAction() throws ConvertorException {
		super();
	}

	/**
	 * 情報をマネージャから削除します。<BR>
	 *
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearLogFormat() {

		log.debug("Start Clear PlatformLogFormat ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		// ログフォーマット定義一覧の取得
		List<LogFormatResponse> logformatList = null;

		try {
			logformatList = HubRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getLogFormatListByOwnerRole(null);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear PlatformLogFormat (Error)");
			return ret;
		}

		// ログフォーマット定義の削除
		for (LogFormatResponse logFormatInfo : logformatList) {
			try {
				HubRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteLogFormat(logFormatInfo.getLogFormatId());
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + logFormatInfo.getLogFormatId());
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}

		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ClearCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Clear PlatformLogFormat ");

		return ret;
	}

	/**
	 * 情報をマネージャから取得し、XMLに出力します。<BR>
	 *
	 * @param 出力するXMLファイル
	 * @return 終了コード
	 */
	@ExportMethod
	public int exportLogFormat(String xmlFile) {

		log.debug("Start Export PlatformLogFormat ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		// ログフォーマット定義一覧の取得
		List<LogFormatResponse> logformatList = null;
		try {
			logformatList = HubRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getLogFormatListByOwnerRole(null);
			Collections.sort(logformatList, new Comparator<LogFormatResponse>() {
				@Override
				public int compare(
						LogFormatResponse info1,
						LogFormatResponse info2) {
					return info1.getLogFormatId().compareTo(info2.getLogFormatId());
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Export PlatformLogFormat (Error)");
			return ret;
		}

		// ログフォーマット定義の取得
		LogFormat logformat = new LogFormat();
		for (LogFormatResponse logformat2 : logformatList) {
			try {
				logformat.addLogFormatInfo(LogFormatConv.getLogFormat(logformat2));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + logformat2.getLogFormatId());
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}

		// XMLファイルに出力
		try {
			logformat.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));

			//スキーマ情報のセット
			logformat.setSchemaInfo(LogFormatConv.getSchemaVersion());

			try(FileOutputStream fos = new FileOutputStream(xmlFile);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				logformat.marshal(osw);
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
		}

		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}

		log.debug("End Export PlatformLogFormat ");
		return ret;
	}

	/**
	 * XMLの情報をマネージャに投入します。<BR>
	 *
	 * @param 入力するXMLファイル
	 * @return 終了コード
	 */
	@ImportMethod
	public int importLogFormat(String xmlFile) {

		log.debug("Start Import PlatformLogFormat ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			getLogger().debug("End Import PlatformLogFormat (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}

		LogFormatType logFormat = null;

		// XMLファイルからの読み込み
		try {
			logFormat = XmlMarshallUtil.unmarshall(LogFormatType.class,new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformLogFormat (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(logFormat.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		// ログフォーマット定義の登録
		List<String> objectIdList = new ArrayList<String>();
		
		ImportLogFormatRecordConfirmer logFormatConfirmer = 
				new ImportLogFormatRecordConfirmer(log,logFormat.getLogFormatInfo());
		int logFormatConfirmerRet = logFormatConfirmer.executeConfirm();
		if (logFormatConfirmerRet != 0) {
			ret = logFormatConfirmerRet;
		}
		
		// レコードの登録（ログフォーマット）
		if (!(logFormatConfirmer.getImportRecDtoList().isEmpty())) {
			ImportLogFormatClientController logFormatController = new ImportLogFormatClientController(log,
					Messages.getString("platform.logformat"), logFormatConfirmer.getImportRecDtoList(), true);
			int logFormatControllerRet = logFormatController.importExecute();
			for (RecordRegistrationResponse rec: logFormatController.getImportSuccessList() ){
				objectIdList.add(rec.getImportKeyValue());
			}
			if (logFormatControllerRet != 0) {
				ret = logFormatControllerRet;
			}
		}
		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		//オブジェクト権限同時インポート
		importObjectPrivilege(HinemosModuleConstant.PLATFORM_LOG_FORMAT, objectIdList);
		
		//差分削除
		checkDelete(logFormat);
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Import PlatformLogFormat ");
		return ret;
	}
	
	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.platform.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = LogFormatConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo sci = LogFormatConv.getSchemaVersion();
		
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	/**
	 * 差分比較処理を行います。
	 * XMLファイル２つ（filePath1,filePath2）を比較する。
	 * 差分がある場合：差分をＣＳＶファイルで出力する。
	 * 差分がない場合：出力しない。
	 * 				   または、すでに存在している同一名のＣＳＶファイルを削除する。
	 *
	 * @param filePath1 XMLファイル名
	 * @param filePath2 XMLファイル名
	 * @return 終了コード
	 * @throws ConvertorException
	 */
	@DiffMethod
	public int diffXml(String filePath1, String filePath2) throws ConvertorException {

		log.debug("Start Differrence PlatformLogFormat ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		LogFormat logFormat1 = null;
		LogFormat logFormat2 = null;

		// XMLファイルからの読み込み
		try {
			logFormat1 = XmlMarshallUtil.unmarshall(LogFormat.class,new InputStreamReader(new FileInputStream(filePath1), "UTF-8"));
			logFormat2 = XmlMarshallUtil.unmarshall(LogFormat.class,new InputStreamReader(new FileInputStream(filePath2), "UTF-8"));
			sort(logFormat1);
			sort(logFormat2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence PlatformLogFormat (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(logFormat1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(logFormat2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(logFormat1, logFormat2, LogFormat.class, resultA);
			assert resultA.getResultBs().size() == 1;
			
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_1;
			}
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(filePath2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(filePath2 + ".csv");
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

		getLogger().debug("End Differrence PlatformLogFormatConv");

		getLogger().debug("End Differrence PlatformLogFormat");

		return ret;
	}

	private void sort(LogFormat logFormat) {
		LogFormatInfo[] infoList = logFormat.getLogFormatInfo();
		Arrays.sort(
			infoList,
			new Comparator<LogFormatInfo>() {
				@Override
				public int compare(LogFormatInfo info1, LogFormatInfo info2) {
					return info1.getLogFormatId().compareTo(info2.getLogFormatId());
				}
			});
		logFormat.setLogFormatInfo(infoList);
	}

	public Logger getLogger() {
		return log;
	}

	protected void checkDelete(LogFormatType xmlElements){
		List<LogFormatResponse> subList = null;
		try {
			subList = HubRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getLogFormatListByOwnerRole(null);
		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}

		if(subList == null || subList.size() <= 0){
			return;
		}

		List<LogFormatInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getLogFormatInfo()));
		for(LogFormatResponse mgrInfo: new ArrayList<>(subList)){
			for(LogFormatInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getLogFormatId().equals(xmlElement.getLogFormatId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}

		if(subList.size() > 0){
			for(LogFormatResponse info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getLogFormatId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}

				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					try {
						HubRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteLogFormat(info.getLogFormatId());
						getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getLogFormatId());
						} catch (Exception e1) {
							getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
						}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getLogFormatId());
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
					return;
				}
			}
		}
	}
	
	/**
	 * ログフォーマット インポート向けのレコード確認用クラス
	 * 
	 */
	protected static class ImportLogFormatRecordConfirmer extends ImportRecordConfirmer<LogFormatInfo, ImportLogFormatRecordRequest, String>{
		
		public ImportLogFormatRecordConfirmer(Logger logger, LogFormatInfo[] importRecDtoList) {
			super(logger, importRecDtoList);
		}
		
		@Override
		protected ImportLogFormatRecordRequest convertDtoXmlToRestReq(LogFormatInfo xmlDto)
				throws HinemosUnknown, InvalidSetting {
			
			AddLogFormatRequest dto = LogFormatConv.getLogFormatData(xmlDto);
			ImportLogFormatRecordRequest dtoRec = new ImportLogFormatRecordRequest();
			dtoRec.setImportData(dto);
			RestClientBeanUtil.convertBean(dto, dtoRec.getImportData());
			dtoRec.setImportKeyValue(dtoRec.getImportData().getLogFormatId());
			
			return dtoRec;
		}

		@Override
		protected Set<String> getExistIdSet() throws Exception {
			Set<String> retSet = new HashSet<String>();
			List<LogFormatResponse> logformatList = HubRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getLogFormatListByOwnerRole(null);
			for (LogFormatResponse rec : logformatList) {
				retSet.add(rec.getLogFormatId());
			}
			return retSet;
		}
		@Override
		protected boolean isLackRestReq(ImportLogFormatRecordRequest restDto) {
			return (restDto == null || restDto.getImportData().getLogFormatId() == null || restDto.getImportData().getLogFormatId().equals(""));
		}
		@Override
		protected String getKeyValueXmlDto(LogFormatInfo xmlDto) {
			return xmlDto.getLogFormatId();
		}
		@Override
		protected String getId(LogFormatInfo xmlDto) {
			return xmlDto.getLogFormatId();
		}
		@Override
		protected void setNewRecordFlg(ImportLogFormatRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
	}
	
	/**
	 * ログフォーマット インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportLogFormatClientController extends ImportClientController<ImportLogFormatRecordRequest, ImportLogFormatResponse, RecordRegistrationResponse>{
		
		public ImportLogFormatClientController(Logger logger, String importInfoName, List<ImportLogFormatRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportLogFormatResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportLogFormatResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportLogFormatRecordRequest importRec) {
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
		protected ImportLogFormatResponse callImportWrapper(List<ImportLogFormatRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportLogFormatRequest reqDto = new ImportLogFormatRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importLogFormat(reqDto);
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
