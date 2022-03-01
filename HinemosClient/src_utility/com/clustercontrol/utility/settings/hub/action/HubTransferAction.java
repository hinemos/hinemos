/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.hub.action;

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
import org.openapitools.client.model.AddTransferInfoRequest;
import org.openapitools.client.model.ImportTransferRecordRequest;
import org.openapitools.client.model.ImportTransferRequest;
import org.openapitools.client.model.ImportTransferResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;
import org.openapitools.client.model.TransferInfoResponse;
import org.openapitools.client.model.TransferInfoResponseP1;

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
import com.clustercontrol.utility.settings.hub.conv.HubConv;
import com.clustercontrol.utility.settings.hub.conv.HubTransferConv;
import com.clustercontrol.utility.settings.hub.xml.Transfer;
import com.clustercontrol.utility.settings.hub.xml.TransferInfo;
import com.clustercontrol.utility.settings.hub.xml.TransferType;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
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
 * 転送設定定義情報をインポート・エクスポート・削除するアクションクラス<br>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class HubTransferAction {

	protected static Logger log = Logger.getLogger(HubTransferAction.class);

	public HubTransferAction() throws ConvertorException {
		super();
	}

	/**
	 * 情報をマネージャから削除します。<BR>
	 *
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearTransfer() {

		log.debug("Start Clear HubTransfer ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		// 転送設定定義一覧の取得
		List<TransferInfoResponseP1> transferList = null;

		try {
			transferList = HubRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getTransferInfoIdList(null);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			 ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear HubTransfer (Error)");
			 return ret;
		}

		// 転送設定定義の削除
		for (TransferInfoResponseP1 transferInfo : transferList) {
			try {
				HubRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteTransferInfo(transferInfo.getTransferId());
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + transferInfo.getTransferId());
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
		log.debug("End Clear HubTransfer ");

		return ret;
	}

	/**
	 * 情報をマネージャから取得し、XMLに出力します。<BR>
	 *
	 * @param 出力するXMLファイル
	 * @return 終了コード
	 */
	@ExportMethod
	public int exportTransfer(String xmlFile) {

		log.debug("Start Export HubTransfer ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		// 転送設定定義一覧の取得
		List<TransferInfoResponse> transferList = null;
		try {
			transferList = HubRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getTransferListByOwnerRole(null);
			Collections.sort(transferList, new Comparator<TransferInfoResponse>() {
				@Override
				public int compare(
						TransferInfoResponse info1,
						TransferInfoResponse info2) {
					return info1.getTransferId().compareTo(info2.getTransferId());
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Export HubTransfer (Error)");
			return ret;
		}

		// 転送設定定義の取得
		Transfer transfer = new Transfer();
		for (TransferInfoResponse transfer2 : transferList) {
			try {
				transfer.addTransferInfo(HubTransferConv.getTransferInfo(transfer2));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + transfer2.getTransferId());
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + transfer2.getTransferId() +" ", e);
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}

		// XMLファイルに出力
		try {
			transfer.setCommon(HubConv.versionCollectDto2Xml(Config.getVersion()));
			// スキーマ情報のセット
			transfer.setSchemaInfo(HubTransferConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlFile);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				transfer.marshal(osw);
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed")+" ", e);
			ret=SettingConstants.ERROR_INPROCESS;
		}
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}

		log.debug("End Export HubTransfer ");

		return ret;
	}

	/**
	 * XMLの情報をマネージャに投入します。<BR>
	 *
	 * @param 入力するXMLファイル
	 * @return 終了コード
	 */
	@ImportMethod
	public int importTransfer(String xmlFile) {

		log.debug("Start Import HubTransfer ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			log.debug("End Import HubTransfer (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}

		TransferType transferType = null;

		// XMLファイルからの読み込み
		try {
			transferType = XmlMarshallUtil.unmarshall(TransferType.class,new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Import HubTransfer (Error)");
			return ret;
		}
		//スキーマのバージョンチェック
		if(!checkSchemaVersion(transferType.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		// 転送設定定義の登録
		List<String> objectIdList = new ArrayList<String>();
		
		/////////////////////////////////////
		// 転送設定定義情報のインポート処理
		/////////////////////////////////////
		
		// レコードの確認(転送設定定義情報)
		ImportTransferRecordConfirmer transferConfirmer = new ImportTransferRecordConfirmer( log, transferType.getTransferInfo());
		int transferConfirmerRet = transferConfirmer.executeConfirm();
		if (transferConfirmerRet != 0) {
			ret = transferConfirmerRet;
		}
		// レコードの登録（転送設定定義情報）
		if (!(transferConfirmer.getImportRecDtoList().isEmpty())) {
			ImportTransferClientController transferController = new ImportTransferClientController(log,
					Messages.getString("hub.transfer"), transferConfirmer.getImportRecDtoList(), true);
			int transferControllerRet = transferController.importExecute();
			for (RecordRegistrationResponse rec: transferController.getImportSuccessList() ){
				objectIdList.add(rec.getImportKeyValue());
			}
			if (transferControllerRet != 0) {
				ret = transferControllerRet;
			}
		}
		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		//オブジェクト権限同時インポート
		importObjectPrivilege(HinemosModuleConstant.HUB_TRANSFER, objectIdList);
		
		//差分削除
		checkDelete(transferType);
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}

		log.debug("End Import HubTransfer ");
		return ret;
	}
	
	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.hub.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = HubTransferConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.hub.xml.SchemaInfo sci = HubTransferConv.getSchemaVersion();
		
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

		log.debug("Start Differrence HubTransfer ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		TransferType transferType1 = null;
		TransferType transferType2 = null;

		// XMLファイルからの読み込み
		try {
			transferType1 = XmlMarshallUtil.unmarshall(TransferType.class,new InputStreamReader(new FileInputStream(filePath1), "UTF-8"));
			transferType2 = XmlMarshallUtil.unmarshall(TransferType.class,new InputStreamReader(new FileInputStream(filePath2), "UTF-8"));
			sort(transferType1);
			sort(transferType2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence PlatformTransfer (Error)");
			return ret;
		}

		//スキーマのバージョンチェック
		if(!checkSchemaVersion(transferType1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(transferType2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(transferType1, transferType2, Transfer.class, resultA);
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
						log.warn(String.format("Fail to delete File. %s", f.getAbsolutePath()));
				}
			}
		}
		catch (Exception e) {
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

		return ret;
	}

	private void sort(TransferType transferType) {
		TransferInfo[] infoList = transferType.getTransferInfo();
		Arrays.sort(
			infoList,
			new Comparator<TransferInfo>() {
				@Override
				public int compare(TransferInfo info1, TransferInfo info2) {
					return info1.getTransferId().compareTo(info2.getTransferId());
				}
			});
		transferType.setTransferInfo(infoList);
	}

	protected void checkDelete(TransferType xmlElements){
		List<TransferInfoResponseP1> subList = null;
		try {
			subList = HubRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getTransferInfoIdList(null);
		}
		catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			log.debug(e.getMessage(), e);
		}

		if(subList == null || subList.size() <= 0){
			return;
		}

		List<TransferInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getTransferInfo()));
		for(TransferInfoResponseP1 mgrInfo: new ArrayList<>(subList)){
			for(TransferInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getTransferId().equals(xmlElement.getTransferId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}

		if(subList.size() > 0){
			for(TransferInfoResponseP1 info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getTransferId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}

				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					try {
						HubRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteTransferInfo(info.getTransferId());
						log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getTransferId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getTransferId());
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
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
	
	/**
	 *  転送設定定義情報 インポート向けのレコード確認用クラス
	 * 
	 */
	protected static class ImportTransferRecordConfirmer extends ImportRecordConfirmer<TransferInfo, ImportTransferRecordRequest, String>{
		
		public ImportTransferRecordConfirmer(Logger logger, TransferInfo[] importRecDtoList) {
			super(logger, importRecDtoList);
		}
		
		@Override
		protected ImportTransferRecordRequest convertDtoXmlToRestReq(TransferInfo xmlDto)
				throws HinemosUnknown, InvalidSetting {
			// xmlから変換
			AddTransferInfoRequest dto = HubTransferConv.getTransferData(xmlDto);
			ImportTransferRecordRequest dtoRec = new ImportTransferRecordRequest();
			dtoRec.setImportData(new AddTransferInfoRequest());
			RestClientBeanUtil.convertBean(dto, dtoRec.getImportData());
			
			dtoRec.setImportKeyValue(dtoRec.getImportData().getTransferId());
			return dtoRec;
		}

		@Override
		protected Set<String> getExistIdSet() throws Exception {
			Set<String> retSet = new HashSet<String>();
			List<TransferInfoResponseP1> transferInfoInfoList = HubRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getTransferInfoIdList(null);
			for (TransferInfoResponseP1 rec : transferInfoInfoList) {
				retSet.add(rec.getTransferId());
			}
			return retSet;
		}
		@Override
		protected boolean isLackRestReq(ImportTransferRecordRequest restDto) {
			return (restDto == null || restDto.getImportData().getTransferId() == null || restDto.getImportData().getTransferId().equals(""));
		}
		@Override
		protected String getKeyValueXmlDto(TransferInfo xmlDto) {
			return xmlDto.getTransferId();
		}
		@Override
		protected String getId(TransferInfo xmlDto) {
			return xmlDto.getTransferId();
		}
		@Override
		protected void setNewRecordFlg(ImportTransferRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
	}

	/**
	 *  転送設定定義情報 インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportTransferClientController extends ImportClientController<ImportTransferRecordRequest, ImportTransferResponse, RecordRegistrationResponse>{
		
		public ImportTransferClientController(Logger logger, String importInfoName, List<ImportTransferRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportTransferResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportTransferResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportTransferRecordRequest importRec) {
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
		protected ImportTransferResponse callImportWrapper(List<ImportTransferRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportTransferRequest reqDto = new ImportTransferRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importTransfer(reqDto);
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
