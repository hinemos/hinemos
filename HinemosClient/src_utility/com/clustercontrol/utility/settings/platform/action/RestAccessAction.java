/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
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
import org.openapitools.client.model.AddRestAccessInfoRequest;
import org.openapitools.client.model.ImportRestAccessInfoRecordRequest;
import org.openapitools.client.model.ImportRestAccessInfoRequest;
import org.openapitools.client.model.ImportRestAccessInfoResponse;
import org.openapitools.client.model.RestAccessInfoResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;

import com.clustercontrol.common.util.CommonRestClientWrapper;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
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
import com.clustercontrol.utility.settings.platform.conv.RestAccessInfoConv;
import com.clustercontrol.utility.settings.platform.xml.RestAccess;
import com.clustercontrol.utility.settings.platform.xml.RestAccessInfo;
import com.clustercontrol.utility.settings.platform.xml.RestAccessType;
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

public class RestAccessAction{

	protected static Logger log = Logger.getLogger(RestAccessAction.class);

	public RestAccessAction() throws ConvertorException {
		super();
	}

	/**
	 * 情報をマネージャから削除します。<BR>
	 *
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearRestAccessInfo() {

		log.debug("Start Clear PlatformRestAccessInfo ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;
		// RESTアクセス情報一覧の取得
		List<RestAccessInfoResponse> RestAccessInfoList = null;
		try {
			RestAccessInfoList = CommonRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getRestAccessInfoList(null);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear PlatformRestAccessInfo (Error)");
			return ret;
		}

		// RESTアクセス情報の削除
		for (RestAccessInfoResponse RestAccessInfo : RestAccessInfoList) {
			try {
				CommonRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.deleteRestAccessInfo(RestAccessInfo.getRestAccessId());
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + RestAccessInfo.getRestAccessId());
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
		log.debug("End Clear PlatformRestAccessInfo ");
		return ret;
	}

	/**
	 * 情報をマネージャから取得し、XMLに出力します。<BR>
	 *
	 * @param 出力するXMLファイル
	 * @return 終了コード
	 */
	@ExportMethod
	public int exportRestAccessInfo(String xmlFile) {

		log.debug("Start Export PlatformRestAccessInfo ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		// RESTアクセス情報一覧の取得
		List<RestAccessInfoResponse> RestAccessInfoList = null;
		try {
			RestAccessInfoList = CommonRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getRestAccessInfoList(null);
			Collections.sort(RestAccessInfoList, new Comparator<RestAccessInfoResponse>() {
				@Override
				public int compare(
						RestAccessInfoResponse info1,
						RestAccessInfoResponse info2) {
					return info1.getRestAccessId().compareTo(info2.getRestAccessId());
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Export PlatformRestAccessInfo (Error)");
			return ret;
		}
		// RESTアクセス情報の取得
		RestAccess restAccess = new RestAccess();
		for (RestAccessInfoResponse rec : RestAccessInfoList) {
			try {
				restAccess.addRestAccessInfo(RestAccessInfoConv.getRestAccessInfo(rec));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + rec.getRestAccessId());
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}

		// XMLファイルに出力
		try {
			restAccess.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));

			//スキーマ情報のセット
			restAccess.setSchemaInfo(RestAccessInfoConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlFile);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				restAccess.marshal(osw);
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
		log.debug("End Export PlatformRestAccessInfo ");
		return ret;
	}

	/**
	 * XMLの情報をマネージャに投入します。<BR>
	 *
	 * @param 入力するXMLファイル
	 * @return 終了コード
	 */
	@ImportMethod
	public int importRestAccessInfo(String xmlFile) {

		log.debug("Start Import PlatformRestAccessInfo ");

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			getLogger().debug("End Import PlatformRestAccessInfo (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		// 返り値変数(条件付き正常終了用）
		int ret = 0;
		RestAccessType restAccess = null;

		// XMLファイルからの読み込み
		try {
			restAccess = XmlMarshallUtil.unmarshall(RestAccessType.class,new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformRestAccessInfo (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(restAccess.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION ;
			return ret;
		}

		// RESTアクセス情報の登録
		List<String> objectIdList = new ArrayList<String>();
		
		ImportRestAccessInfoConfirmer importRestAccessInfoConfirmer = new ImportRestAccessInfoConfirmer(log, restAccess.getRestAccessInfo());
		int RestAccessInfoConfirmerRet = importRestAccessInfoConfirmer.executeConfirm();
		if (RestAccessInfoConfirmerRet != 0) {
			ret = RestAccessInfoConfirmerRet;
		}
		
		// レコードの登録（RESTアクセス情報）
		if (!(importRestAccessInfoConfirmer.getImportRecDtoList().isEmpty())) {
			ImportRestAccessInfoClientController notifyController = new ImportRestAccessInfoClientController(log,
					Messages.getString("platform.restaccess"), importRestAccessInfoConfirmer.getImportRecDtoList(), true);
			int RestAccessInforRet = notifyController.importExecute();
			for (RecordRegistrationResponse rec: notifyController.getImportSuccessList() ){
				objectIdList.add(rec.getImportKeyValue());
			}
			if (RestAccessInforRet != 0) {
				ret = RestAccessInforRet;
			}
		}
		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		//オブジェクト権限同時インポート
		importObjectPrivilege(HinemosModuleConstant.PLATFORM_REST_ACCESS , objectIdList);
		
		//差分削除
		checkDelete(restAccess);
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Import PlatformRestAccessInfo ");
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
		int res = RestAccessInfoConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo sci = RestAccessInfoConv.getSchemaVersion();
		
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

		log.debug("Start Differrence PlatformRestAccessInfo ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		RestAccess RestAccessInfo1 = null;
		RestAccess RestAccessInfo2 = null;

		// XMLファイルからの読み込み
		try {
			RestAccessInfo1 = XmlMarshallUtil.unmarshall(RestAccess.class,new InputStreamReader(new FileInputStream(filePath1), "UTF-8"));
			RestAccessInfo2 = XmlMarshallUtil.unmarshall(RestAccess.class,new InputStreamReader(new FileInputStream(filePath2), "UTF-8"));
			sort(RestAccessInfo1);
			sort(RestAccessInfo2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"),e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence PlatformRestAccessInfo (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(RestAccessInfo1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(RestAccessInfo2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(RestAccessInfo1, RestAccessInfo2, RestAccess.class, resultA);
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
						log.warn(String.format("Fail to delete file. %s", f.getAbsolutePath()));;
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
		
		getLogger().debug("End Differrence PlatformRestAccessInfo");

		return ret;
	}
	
	private void sort(RestAccess restAccess) {
		RestAccessInfo[] infoList = restAccess.getRestAccessInfo();
		Arrays.sort(
			infoList,
			new Comparator<RestAccessInfo>() {
				@Override
				public int compare(RestAccessInfo info1, RestAccessInfo info2) {
					return info1.getRestAccessId().compareTo(info2.getRestAccessId());
				}
			});
		restAccess.setRestAccessInfo(infoList);
	}

	public Logger getLogger() {
		return log;
	}

	protected void checkDelete(RestAccessType xmlElements){
		List<RestAccessInfoResponse> subList = null;
		try {
			subList = CommonRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getRestAccessInfoList(null);
		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}
		
		if(subList == null || subList.size() <= 0){
			return;
		}
		
		List<RestAccessInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getRestAccessInfo()));
		for(RestAccessInfoResponse mgrInfo: new ArrayList<>(subList)){
			for(RestAccessInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getRestAccessId().equals(xmlElement.getRestAccessId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(RestAccessInfoResponse info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getRestAccessId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}

				if (DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE) {
					try {
						CommonRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName())
								.deleteRestAccessInfo(info.getRestAccessId());
						getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : "
								+ info.getRestAccessId());
					} catch (Exception e1) {
						getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : "
								+ HinemosMessage.replace(e1.getMessage()));
					}
				} else if (DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP) {
					getLogger().info(
							Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getRestAccessId());
				} else if (DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
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
					getLogger());
		}
	}
	
	/**
	 * メールテンプレート インポート向けのレコード確認用クラス
	 * 
	 */
	protected static class ImportRestAccessInfoConfirmer extends ImportRecordConfirmer<RestAccessInfo, ImportRestAccessInfoRecordRequest, String>{
		
		public ImportRestAccessInfoConfirmer(Logger logger, RestAccessInfo[] importRecDtoList) {
			super(logger, importRecDtoList);
		}
		
		@Override
		protected ImportRestAccessInfoRecordRequest convertDtoXmlToRestReq(RestAccessInfo xmlDto)
				throws HinemosUnknown, InvalidSetting {
			
			RestAccessInfoResponse  dto = RestAccessInfoConv.getRestAccessInfoResponse(xmlDto);
			ImportRestAccessInfoRecordRequest dtoRec = new ImportRestAccessInfoRecordRequest();
			dtoRec.setImportData(new AddRestAccessInfoRequest());
			RestClientBeanUtil.convertBean(dto, dtoRec.getImportData());
			dtoRec.setImportKeyValue(dtoRec.getImportData().getRestAccessId());
			return dtoRec;
		}
		
		@Override
		protected Set<String> getExistIdSet() throws Exception {
			Set<String> retSet = new HashSet<String>();
			List<RestAccessInfoResponse> RestAccessInfoList = CommonRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getRestAccessInfoList(null);
			for (RestAccessInfoResponse rec : RestAccessInfoList) {
				retSet.add(rec.getRestAccessId());
			}
			return retSet;
		}
		@Override
		protected boolean isLackRestReq(ImportRestAccessInfoRecordRequest restDto) {
			return (restDto == null || restDto.getImportData().getRestAccessId() == null || restDto.getImportData().getRestAccessId().equals(""));
		}
		@Override
		protected String getKeyValueXmlDto(RestAccessInfo xmlDto) {
			return xmlDto.getRestAccessId();
		}
		@Override
		protected String getId(RestAccessInfo xmlDto) {
			return xmlDto.getRestAccessId();
		}
		@Override
		protected void setNewRecordFlg(ImportRestAccessInfoRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
	}
	
	/**
	 * メールテンプレート インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportRestAccessInfoClientController extends ImportClientController<ImportRestAccessInfoRecordRequest, ImportRestAccessInfoResponse, RecordRegistrationResponse>{
		
		public ImportRestAccessInfoClientController(Logger logger, String importInfoName, List<ImportRestAccessInfoRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportRestAccessInfoResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportRestAccessInfoResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportRestAccessInfoRecordRequest importRec) {
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
		protected ImportRestAccessInfoResponse callImportWrapper(List<ImportRestAccessInfoRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportRestAccessInfoRequest reqDto = new ImportRestAccessInfoRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importRestAccessInfo(reqDto);
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
