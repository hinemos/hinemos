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
import org.exolab.castor.xml.Unmarshaller;
import org.openapitools.client.model.NotifyRequestForUtility;
import org.openapitools.client.model.NotifyRequestForUtility.NotifyTypeEnum;
import org.openapitools.client.model.NotifyRequestForUtility.RenotifyTypeEnum;
import org.openapitools.client.model.CloudNotifyInfoResponse;
import org.openapitools.client.model.CommandNotifyInfoResponse;
import org.openapitools.client.model.EventNotifyInfoResponse;
import org.openapitools.client.model.ImportNotifyRecordRequest;
import org.openapitools.client.model.ImportNotifyRequest;
import org.openapitools.client.model.ImportNotifyResponse;
import org.openapitools.client.model.InfraNotifyInfoResponse;
import org.openapitools.client.model.JobNotifyInfoResponse;
import org.openapitools.client.model.LogEscalateNotifyInfoResponse;
import org.openapitools.client.model.MailNotifyInfoResponse;
import org.openapitools.client.model.MessageNotifyInfoResponse;
import org.openapitools.client.model.NotifyInfoResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;
import org.openapitools.client.model.RestNotifyInfoResponse;
import org.openapitools.client.model.StatusNotifyInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.notify.action.DeleteNotify;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.dialog.bean.NotifyInfoInputData;
import com.clustercontrol.notify.util.NotifyRestClientWrapper;
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
import com.clustercontrol.utility.settings.platform.conv.NotifyInfoConv;
import com.clustercontrol.utility.settings.platform.xml.Notify;
import com.clustercontrol.utility.settings.platform.xml.NotifyInfo;
import com.clustercontrol.utility.settings.platform.xml.NotifyType;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.AccountUtil;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.ImportClientController;
import com.clustercontrol.utility.util.ImportRecordConfirmer;
import com.clustercontrol.utility.util.OpenApiEnumConverter;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;
import com.clustercontrol.utility.util.XmlMarshallUtil;

/**
 * 通知定義情報をインポート・エクスポート・削除するアクションクラス<br>
 *
 * @version 6.1.0
 * @since 1.0.0
 * 
 */
public class NotifyAction {

	protected static Logger log = Logger.getLogger(NotifyAction.class);

	public NotifyAction() throws ConvertorException {
		super();
	}

	/**
	 * 情報をマネージャから削除します。<BR>
	 *
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearNotify() {

		log.debug("Start Clear PlatformNotify ");

		int ret = 0;
		// 通知定義一覧の取得
		List<NotifyInfoResponse> notifyInfoList = null;
		try {
			notifyInfoList = NotifyRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getNotifyList("");
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear PlatformNotify (Error)");
			return ret;
		}

		// すべての通知定義の削除
		List<String> ids = new ArrayList<>();
		for (NotifyInfoResponse notifyInfo : notifyInfoList) {
			// 使用されている箇所があるか確認する
			boolean useCheckResult = true;
			useCheckResult = new DeleteNotify().useCheckForUtility(UtilityManagerUtil.getCurrentManagerName(), 
					Arrays.asList(notifyInfo.getNotifyId()));

			if (!useCheckResult) {
				// 処理をキャンセルする
				DeleteProcessMode.setProcesstype(UtilityDialogConstant.SKIP);
				getLogger().info(Messages.getString("SettingTools.ClearFailed") + " : Delete Notify process canceled." + " : " + notifyInfo.getNotifyId());
			} else {
				ids.add(notifyInfo.getNotifyId());
			}
		}

		if (AccountUtil.isAdministrator(UtilityManagerUtil.getCurrentManagerName())) {
			// ADMINISTRATORS権限がある場合
			try {
				String idsString = String.join(",", ids);
				NotifyRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteNotify(idsString);
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + ids.toString());
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		} else {
			// ADMINISTRATORS権限がない場合
			for (String id : ids) {
				try {
					NotifyRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteNotify(id);
					log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + id);
				} catch (Exception e) {
					log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				}
			}
		}
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ClearCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Clear PlatformNotify ");
		return ret;
	}

	/**
	 * 情報をマネージャから取得し、XMLに出力します。<BR>
	 *
	 * @param 出力するXMLファイル
	 * @return 終了コード
	 */
	@ExportMethod
	public int exportNotify(String xmlFile) {

		log.debug("Start Export PlatformNotify ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		// 通知定義一覧の取得
		List<NotifyInfoResponse> notifyInfoList = null;
		try {
			notifyInfoList = NotifyRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getNotifyList("");
			Collections.sort(notifyInfoList, new Comparator<NotifyInfoResponse>() {
				@Override
				public int compare(
						NotifyInfoResponse info1,
						NotifyInfoResponse info2) {
					return info1.getNotifyId().compareTo(info2.getNotifyId());
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export PlatformNotify (Error)");
			return ret;
		}
		
		// 通知定義の取得
		Notify notify = new Notify();
		NotifyInfoInputData notifyInfoInputData = null;
		for (NotifyInfoResponse notifyInfo : notifyInfoList) {
			try {
				notifyInfoInputData = new NotifyInfoInputData();
				RestClientBeanUtil.convertBean(notifyInfo, notifyInfoInputData);
				
				// Enumを変換し個別セット
				int notifyTypeEnumInt = OpenApiEnumConverter.enumToInteger(notifyInfo.getNotifyType());
				notifyInfoInputData.setNotifyType(notifyTypeEnumInt);
				int renotifyTypeInt = OpenApiEnumConverter.enumToInteger(notifyInfo.getRenotifyType());
				notifyInfoInputData.setRenotifyType(renotifyTypeInt);
				// 通知種別に応じて詳細を個別セット
				setNotifyDetailInfoResponse(notifyInfo, notifyInfoInputData);
				
				notify.addNotifyInfo(NotifyInfoConv.convDto2XmlNotify(notifyInfoInputData));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + notifyInfo.getNotifyId());
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
			
		// XMLファイルに出力
		try {
			notify.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			// スキーマ情報のセット
			notify.setSchemaInfo(NotifyInfoConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlFile);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				notify.marshal(osw);
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
		}
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		
		log.debug("End Export PlatformNotify ");
		return ret;
	}

	/**
	 * XMLの情報をマネージャに投入します。<BR>
	 *
	 * @param 入力するXMLファイル
	 * @return 終了コード
	 * @throws ConvertorException 
	 */
	@ImportMethod
	public int importNotify(String xmlNotify) throws ConvertorException {

		log.debug("Start Import PlatformNotify ");

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
	    	getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
	    	getLogger().debug("End Import PlatformNotify (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
	    }
		
		// 返り値変数(条件付き正常終了用）
		int ret = 0;
		NotifyType notifyInfoList = null;

		// XMLファイルからの読み込み
		try {
			// 下位互換向けにXMLの内容確認（順番チェック）を緩くしておく
			notifyInfoList = XmlMarshallUtil.unmarshall(NotifyType.class,new InputStreamReader(new FileInputStream(xmlNotify), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformNotify (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(notifyInfoList.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		// 通知情報の登録
		List<String> objectIdList = new ArrayList<String>();

		ImportNotifyRecordConfirmer notifyConfirmer = new ImportNotifyRecordConfirmer( log, notifyInfoList.getNotifyInfo() );
		int notifyConfirmerRet = notifyConfirmer.executeConfirm();
		if (notifyConfirmerRet != 0) {
			ret = notifyConfirmerRet;
		}
		// レコードの登録（通知）
		if (!(notifyConfirmer.getImportRecDtoList().isEmpty())) {
			ImportNotifyClientController notifyController = new ImportNotifyClientController(log,
					Messages.getString("platform.notify"), notifyConfirmer.getImportRecDtoList(), true);
			int notifyControllerRet = notifyController.importExecute();
			for (RecordRegistrationResponse rec: notifyController.getImportSuccessList() ){
				objectIdList.add(rec.getImportKeyValue());
			}
			if (notifyControllerRet != 0) {
				ret = notifyControllerRet;
			}
		}
		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		//オブジェクト権限同時インポート
		importObjectPrivilege(HinemosModuleConstant.PLATFORM_NOTIFY, objectIdList);
		
		//差分削除
		checkDelete(notifyInfoList);
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		
		log.debug("End Import PlatformNotify ");
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
		int res = NotifyInfoConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo sci = NotifyInfoConv.getSchemaVersion();
		
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
		log.debug("Start Differrence PlatformNotify ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		Notify notify1 = null;
		Notify notify2 = null;

		// XMLファイルからの読み込み
		try {
			notify1 = XmlMarshallUtil.unmarshall(Notify.class,new InputStreamReader(new FileInputStream(filePath1), "UTF-8"));
			notify2 = XmlMarshallUtil.unmarshall(Notify.class,new InputStreamReader(new FileInputStream(filePath2), "UTF-8"));
			sort(notify1);
			sort(notify2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence PlatformNotify (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(notify1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(notify2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(notify1, notify2, Notify.class, resultA);
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

		getLogger().debug("End Differrence PlatformNotify");

		return ret;
	}
	
	private void sort(Notify notify) {
		NotifyInfo[] infoList = notify.getNotifyInfo();
		Arrays.sort(
			infoList,
			new Comparator<NotifyInfo>() {
				@Override
				public int compare(NotifyInfo info1, NotifyInfo info2) {
					return info1.getNotifyId().compareTo(info2.getNotifyId());
				}
			});
		 notify.setNotifyInfo(infoList);
	}

	public Logger getLogger() {
		return log;
	}
	

	protected void checkDelete(NotifyType xmlElements){
		List<NotifyInfoResponse> subList = null;
		try {
			subList = NotifyRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getNotifyList("");
		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}
		
		if(subList == null || subList.size() <= 0){
			return;
		}
		
		List<NotifyInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getNotifyInfo()));
		for(NotifyInfoResponse mgrInfo: new ArrayList<>(subList)){
			for(NotifyInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getNotifyId().equals(xmlElement.getNotifyId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(NotifyInfoResponse info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getNotifyId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){

					// 使用されている箇所があるか確認する
					boolean useCheckResult = true;
					useCheckResult = new DeleteNotify().useCheckForUtility(UtilityManagerUtil.getCurrentManagerName(), 
							Arrays.asList(info.getNotifyId()));

					if (!useCheckResult) {
						// 処理をキャンセルする
						getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getNotifyId());
						continue;
					}

					try {
						List<String> args = new ArrayList<>();
						args.add(info.getNotifyId());
						String notifyIdString = String.join(",", args);
						NotifyRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteNotify(notifyIdString);
						getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getNotifyId());
					} catch (Exception e1) {
						getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getNotifyId());
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
					return;
				}
			}
		}
	}

	/**
	 * 通知 インポート向けのレコード確認用クラス
	 * 
	 */
	protected static class ImportNotifyRecordConfirmer extends ImportRecordConfirmer<NotifyInfo, ImportNotifyRecordRequest, String>{
		
		public ImportNotifyRecordConfirmer(Logger logger, NotifyInfo[] importRecDtoList) {
			super(logger, importRecDtoList);
		}
		
		@Override
		protected ImportNotifyRecordRequest convertDtoXmlToRestReq(NotifyInfo xmlDto)
				throws HinemosUnknown, InvalidSetting, ConvertorException {
			
			NotifyInfoInputData dto = NotifyInfoConv.convXml2DtoNotify(xmlDto);
			ImportNotifyRecordRequest dtoRec = new ImportNotifyRecordRequest();
			dtoRec.setImportData(new NotifyRequestForUtility());
			RestClientBeanUtil.convertBean(dto, dtoRec.getImportData());
			
			// Enumを個別に変換しセット
			NotifyTypeEnum notifyTypeEnum 
			= OpenApiEnumConverter.integerToEnum(dto.getNotifyType(), NotifyTypeEnum.class);			
			dtoRec.getImportData().setNotifyType(notifyTypeEnum);
			RenotifyTypeEnum renotifyTypeEnum 
			= OpenApiEnumConverter.integerToEnum(dto.getRenotifyType(), RenotifyTypeEnum.class);
			dtoRec.getImportData().setRenotifyType(renotifyTypeEnum);
			
			dtoRec.setImportKeyValue(dtoRec.getImportData().getNotifyId());
			return dtoRec;
		}

		@Override
		protected Set<String> getExistIdSet() throws Exception {
			Set<String> retSet = new HashSet<String>();
			List<NotifyInfoResponse> notifyInfoList = NotifyRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getNotifyList("");
			for (NotifyInfoResponse rec : notifyInfoList) {
				retSet.add(rec.getNotifyId());
			}
			return retSet;
		}
		@Override
		protected boolean isLackRestReq(ImportNotifyRecordRequest restDto) {
			return (restDto == null || restDto.getImportData().getNotifyId() == null || restDto.getImportData().getNotifyId().equals(""));
		}
		@Override
		protected String getKeyValueXmlDto(NotifyInfo xmlDto) {
			return xmlDto.getNotifyId();
		}
		@Override
		protected String getId(NotifyInfo xmlDto) {
			return xmlDto.getNotifyId();
		}
		@Override
		protected void setNewRecordFlg(ImportNotifyRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
	}

	/**
	 * 通知インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportNotifyClientController extends ImportClientController<ImportNotifyRecordRequest, ImportNotifyResponse, RecordRegistrationResponse>{
		
		public ImportNotifyClientController(Logger logger, String importInfoName, List<ImportNotifyRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportNotifyResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportNotifyResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportNotifyRecordRequest importRec) {
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
		protected ImportNotifyResponse callImportWrapper(List<ImportNotifyRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportNotifyRequest reqDto = new ImportNotifyRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importNotify(reqDto);
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
	/**
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
	 * 通知種別に応じて詳細を取得し、NotifyInfoInputDataにセットする
	 * 
	 * @param notifyInfo
	 * @throws HinemosUnknown 
	 * @throws RestConnectFailed 
	 * @throws InvalidRole 
	 * @throws InvalidUserPass 
	 * @throws NotifyNotFound 
	 */
	private NotifyInfoInputData setNotifyDetailInfoResponse(NotifyInfoResponse notifyInfo, NotifyInfoInputData notifyInfoInputData) 
			throws NotifyNotFound, InvalidUserPass, InvalidRole, RestConnectFailed, HinemosUnknown{
		
		// Enumをintに変換し個別セット
		int notifyTypeEnumInt = OpenApiEnumConverter.enumToInteger(notifyInfo.getNotifyType());
		notifyInfoInputData.setNotifyType(notifyTypeEnumInt);
		int renotifyTypeInt = OpenApiEnumConverter.enumToInteger(notifyInfo.getRenotifyType());
		notifyInfoInputData.setRenotifyType(renotifyTypeInt);
		
		NotifyRestClientWrapper wrapper = 
				NotifyRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		
		switch(notifyTypeEnumInt){
		
		case NotifyTypeConstant.TYPE_STATUS:
			StatusNotifyInfoResponse status = wrapper.getStatusNotify(notifyInfo.getNotifyId());
			notifyInfoInputData.setNotifyStatusInfo(status.getNotifyStatusInfo());
			break;
			
		case NotifyTypeConstant.TYPE_EVENT:
			EventNotifyInfoResponse event = wrapper.getEventNotify(notifyInfo.getNotifyId());
			notifyInfoInputData.setNotifyEventInfo(event.getNotifyEventInfo());
			break;
			
		case NotifyTypeConstant.TYPE_MAIL:
			MailNotifyInfoResponse mail = wrapper.getMailNotify(notifyInfo.getNotifyId());
			notifyInfoInputData.setNotifyMailInfo(mail.getNotifyMailInfo());
			break;
			
		case NotifyTypeConstant.TYPE_JOB:
			JobNotifyInfoResponse job = wrapper.getJobNotify(notifyInfo.getNotifyId());
			notifyInfoInputData.setNotifyJobInfo(job.getNotifyJobInfo());
			break;
			
		case NotifyTypeConstant.TYPE_LOG_ESCALATE:
			LogEscalateNotifyInfoResponse logescalate = wrapper.getLogEscalateNotify(notifyInfo.getNotifyId());
			notifyInfoInputData.setNotifyLogEscalateInfo(logescalate.getNotifyLogEscalateInfo());
			break;
			
		case NotifyTypeConstant.TYPE_COMMAND:
			CommandNotifyInfoResponse command = wrapper.getCommandNotify(notifyInfo.getNotifyId());
			notifyInfoInputData.setNotifyCommandInfo(command.getNotifyCommandInfo());
			break;
			
		case NotifyTypeConstant.TYPE_INFRA:
			InfraNotifyInfoResponse infra = wrapper.getInfraNotify(notifyInfo.getNotifyId());
			notifyInfoInputData.setNotifyInfraInfo(infra.getNotifyInfraInfo());
			break;
			
		case NotifyTypeConstant.TYPE_REST:
			RestNotifyInfoResponse rest = wrapper.getRestNotify(notifyInfo.getNotifyId());
			notifyInfoInputData.setNotifyRestInfo(rest.getNotifyRestInfo());
			break;
			

		case NotifyTypeConstant.TYPE_MESSAGE:
			MessageNotifyInfoResponse message = wrapper.getMessageNotify(notifyInfo.getNotifyId());
			notifyInfoInputData.setNotifyMessageInfo(message.getNotifyMessageInfo());
			break;
			
		case NotifyTypeConstant.TYPE_CLOUD:
			CloudNotifyInfoResponse cloud = wrapper.getCloudNotify(notifyInfo.getNotifyId());
			notifyInfoInputData.setNotifyCloudInfo(cloud.getNotifyCloudInfo());
			break;
			
		default:
			log.debug("Check notify type." + notifyTypeEnumInt);
			break;
		}
		
		return notifyInfoInputData;
	}
}
