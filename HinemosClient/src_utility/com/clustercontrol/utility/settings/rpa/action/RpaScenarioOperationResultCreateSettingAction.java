/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.rpa.action;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openapitools.client.model.AddRpaScenarioOperationResultCreateSettingRequest;
import org.openapitools.client.model.ImportRpaScenarioOperationResultCreateSettingRecordRequest;
import org.openapitools.client.model.ImportRpaScenarioOperationResultCreateSettingRequest;
import org.openapitools.client.model.ImportRpaScenarioOperationResultCreateSettingResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;
import org.openapitools.client.model.RpaScenarioOperationResultCreateSettingResponse;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.RpaScenarioOperationResultCreateSettingNotFound;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
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
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.rpa.conv.RpaScenarioOperationResultCreateSettingConv;
import com.clustercontrol.utility.settings.rpa.xml.RpaScenarioOperationResultCreateSetting;
import com.clustercontrol.utility.settings.rpa.xml.RpaScenarioOperationResultCreateSettings;
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
 * 
 * @param action 動作
 * @param XMLファイルパス（ユーザ情報定義の入力元）
 * 
 */
public class RpaScenarioOperationResultCreateSettingAction {

	/* ロガー */
	protected static Logger log = Logger.getLogger(RpaScenarioOperationResultCreateSettingAction.class);

	public RpaScenarioOperationResultCreateSettingAction() throws ConvertorException {
		super();
	}
	
	/**
	 * RPAシナリオ実績作成設定定義情報を全て削除します。<BR>
	 * 
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearRpaScenarioOperationResultCreateSetting(){
		
		log.debug("Start Clear RPA Scenario Operation Result Create Setting");
		int ret = 0;

		List<RpaScenarioOperationResultCreateSettingResponse> settingList = null;

		RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		
		try {
			settingList = wrapper.getRpaScenarioOperationResultCreateSettingList();
		} catch ( RestConnectFailed |HinemosUnknown	| InvalidRole | InvalidUserPass e) {
			log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		List<String> deleteSettingIdList = new ArrayList<String>();
		for (RpaScenarioOperationResultCreateSettingResponse setting : settingList){
			deleteSettingIdList.add(setting.getScenarioOperationResultCreateSettingId());
		}
		
		if (!deleteSettingIdList.isEmpty()){
			for (String targetId : deleteSettingIdList){
				try {
					wrapper.deleteRpaScenarioOperationResultCreateSetting(targetId);
					log.info(Messages.getString("SettingTools.ClearSucceeded") + " id:" + targetId);
				} catch ( RestConnectFailed |HinemosUnknown	| InvalidRole | InvalidUserPass | RpaScenarioOperationResultCreateSettingNotFound e) {
					log.error(Messages.getString("SettingTools.ClearFailed")+ " id:" + targetId + " , " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				}
			}
		}
		
		// 処理の終了
		log.info(Messages.getString("SettingTools.ClearCompleted"));
		
		log.debug("End Clear RPA Scenario Operation Result Create Setting");
		return ret;
	}
	
	/**
	 *	定義情報をマネージャから読み出します。
	 * @return
	 */
	@ExportMethod
	public int exportRpaScenarioOperationResultCreateSetting(String xmlFile) {

		log.debug("Start Export RPA Scenario Operation Result Create Setting");

		int ret = 0;
		RpaRestClientWrapper wrapper =
				RpaRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		
		List<RpaScenarioOperationResultCreateSettingResponse> settingList =null;
		
		try {
			settingList = wrapper.getRpaScenarioOperationResultCreateSettingList();
			Collections.sort(
					settingList,
					new Comparator<RpaScenarioOperationResultCreateSettingResponse>() {
						@Override
						public int compare(RpaScenarioOperationResultCreateSettingResponse settingInfo1, RpaScenarioOperationResultCreateSettingResponse settingInfo2) {
							return settingInfo1.getScenarioOperationResultCreateSettingId().compareTo(settingInfo2.getScenarioOperationResultCreateSettingId());
						}
					});
		} catch ( RestConnectFailed |HinemosUnknown	| InvalidRole | InvalidUserPass e) {
			log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		RpaScenarioOperationResultCreateSettings  settings = new RpaScenarioOperationResultCreateSettings();
		RpaScenarioOperationResultCreateSetting setting = new RpaScenarioOperationResultCreateSetting();
		
		for (RpaScenarioOperationResultCreateSettingResponse info : settingList) {
			try{
				setting = RpaScenarioOperationResultCreateSettingConv.getRpaScenarioOperationResultCreateSetting(info);
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + info.getScenarioOperationResultCreateSettingId());
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
			
			settings.addRpaScenarioOperationResultCreateSetting(setting);
		}
		
		// XMLファイルに出力
		try {
			settings.setCommon(RpaScenarioOperationResultCreateSettingConv.versionRpaDto2Xml(Config.getVersion()));
			// スキーマ情報のセット
			settings.setSchemaInfo(RpaScenarioOperationResultCreateSettingConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				settings.marshal(osw);
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
		}
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Export RPA Scenario Operation Result Create Setting");
		return ret;
	}
	
	/**
	 * 定義情報をマネージャに投入します。
	 * 
	 * @return
	 */
	@ImportMethod
	public int importRpaScenarioOperationResultCreateSetting(String xmlFile) 
			throws ConvertorException, InvalidRole, InvalidUserPass, RpaScenarioOperationResultCreateSettingNotFound, 
			InvalidSetting, HinemosUnknown, ParseException, RestConnectFailed {
		log.debug("Start Import RPA Scenario Operation Result Create Setting");

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			getLogger().debug("End Import RPA Scenario Operation Result Create Setting (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		int ret = 0;
		
		// XMLファイルからの読み込み
		RpaScenarioOperationResultCreateSettings settings = null;
		try {
			settings = XmlMarshallUtil.unmarshall(RpaScenarioOperationResultCreateSettings.class,new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import RPA Scenario Operation Result Create Setting (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(settings.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		// castor の 情報を DTO に変換。
		List<RpaScenarioOperationResultCreateSettingResponse> settingList = null;
		try {
			settingList = createRpaScenarioOperationResultCreateSettingList(settings);
		} catch (Exception e) {
			if (e instanceof ConvertorException) {
				getLogger().warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			} else {
				getLogger().warn(Messages.getString("SettingTools.ImportFailed"), e);
			}
			// DTO変換中に例外が発生した場合、RpaScenarioList は初期化状態のまま(件数0)なので、インポートを中断
			return SettingConstants.ERROR_INPROCESS;
		}

		// RpaScenarioOperationResultCreateSettingInfo をマネージャに登録。
		List<String> objectIdList = new ArrayList<String>();
		ret = importRpaScenarioOperationResultCreateSettingList(settingList, objectIdList);

		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		//オブジェクト権限同時インポート
		importObjectPrivilege(com.clustercontrol.bean.HinemosModuleConstant.RPA_SCENARIO_CREATE, objectIdList);
		
		checkDelete(settingList);
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Import RPA Scenario Operation Result Create Setting");
		
		return ret;
	}
	
	public List<RpaScenarioOperationResultCreateSettingResponse> createRpaScenarioOperationResultCreateSettingList(RpaScenarioOperationResultCreateSettings settings) 
			throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, 
			RpaScenarioOperationResultCreateSettingNotFound, InvalidSetting, ParseException {
		return RpaScenarioOperationResultCreateSettingConv.createRpaScenarioOperationResultCreateSettingList(settings);
	}
	
	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.rpa.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = RpaScenarioOperationResultCreateSettingConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.rpa.xml.SchemaInfo sci = RpaScenarioOperationResultCreateSettingConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	protected void checkDelete(List<RpaScenarioOperationResultCreateSettingResponse> xmlElements){

		List<RpaScenarioOperationResultCreateSettingResponse> subList = null;
		try {
			subList = getFilterdRpaScenarioOperationResultCreateSettingList();
		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}

		if(subList == null || subList.size() <= 0){
			return;
		}
		
		for(RpaScenarioOperationResultCreateSettingResponse mgrInfo: new ArrayList<>(subList)){
			for(RpaScenarioOperationResultCreateSettingResponse xmlElement: new ArrayList<>(xmlElements)){
				if(mgrInfo.getScenarioOperationResultCreateSettingId().equals(xmlElement.getScenarioOperationResultCreateSettingId())){
					subList.remove(mgrInfo);
					xmlElements.remove(xmlElement);
					break;
				}
			}
		}

		if(subList.size() > 0){
			
			for(RpaScenarioOperationResultCreateSettingResponse info: subList){
				
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getScenarioOperationResultCreateSettingId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					
					try {
						RpaRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteRpaScenarioOperationResultCreateSetting(info.getScenarioOperationResultCreateSettingId());
						getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getScenarioOperationResultCreateSettingId());
					} catch (Exception e1) {
						getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getScenarioOperationResultCreateSettingId());
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
					return;
				}
			}
		}
	}
	
	/**
	 *  RpaScenarioOperationResultCreateSetting をインポートする。
	 *  
	 */
	protected int importRpaScenarioOperationResultCreateSettingList(List<RpaScenarioOperationResultCreateSettingResponse> settingList, List<String> objectIdList) 
			throws HinemosUnknown,InvalidRole, InvalidUserPass, RpaScenarioOperationResultCreateSettingNotFound, RestConnectFailed{
		int returnValue =0;
		ImportRecordConfirmer<RpaScenarioOperationResultCreateSettingResponse, ImportRpaScenarioOperationResultCreateSettingRecordRequest, String> confirmer =
				new ImportRecordConfirmer<RpaScenarioOperationResultCreateSettingResponse, ImportRpaScenarioOperationResultCreateSettingRecordRequest, String>(
				getLogger(), settingList.toArray(new RpaScenarioOperationResultCreateSettingResponse[settingList.size()])) {
			@Override
			protected ImportRpaScenarioOperationResultCreateSettingRecordRequest convertDtoXmlToRestReq(RpaScenarioOperationResultCreateSettingResponse xmlDto)
					throws HinemosUnknown, InvalidSetting {
				ImportRpaScenarioOperationResultCreateSettingRecordRequest dtoRec = new ImportRpaScenarioOperationResultCreateSettingRecordRequest();
				dtoRec.setImportData(new AddRpaScenarioOperationResultCreateSettingRequest());
				RestClientBeanUtil.convertBean(xmlDto, dtoRec.getImportData());
				dtoRec.setImportKeyValue(dtoRec.getImportData().getScenarioOperationResultCreateSettingId());
				return dtoRec;
			}
			@Override
			protected Set<String> getExistIdSet() throws Exception {
				Set<String> retSet = new HashSet<String>();
				for(RpaScenarioOperationResultCreateSettingResponse rec :getFilterdRpaScenarioOperationResultCreateSettingList()){
					retSet.add(rec.getScenarioOperationResultCreateSettingId());
				}
				return retSet;
			}
			@Override
			protected boolean isLackRestReq(ImportRpaScenarioOperationResultCreateSettingRecordRequest restDto) {
				return false;
			}
			@Override
			protected String getKeyValueXmlDto(RpaScenarioOperationResultCreateSettingResponse xmlDto) {
				return xmlDto.getScenarioOperationResultCreateSettingId();
			}
			@Override
			protected String getId(RpaScenarioOperationResultCreateSettingResponse xmlDto) {
				return xmlDto.getScenarioOperationResultCreateSettingId();
			}
			@Override
			protected void setNewRecordFlg(ImportRpaScenarioOperationResultCreateSettingRecordRequest restDto, boolean flag) {
				restDto.setIsNewRecord(flag);
			}
		};
		int confirmRet = confirmer.executeConfirm();
		if( confirmRet != SettingConstants.SUCCESS && confirmRet != SettingConstants.ERROR_CANCEL ){
			//変換エラーならUnmarshalXml扱いで処理打ち切り(キャンセルはキャンセル以前の選択結果を反映するので次に進む)
			getLogger().warn(Messages.getString("SettingTools.UnmarshalXmlFailed"));
			return confirmRet;
		}

		ImportClientController<ImportRpaScenarioOperationResultCreateSettingRecordRequest, ImportRpaScenarioOperationResultCreateSettingResponse, RecordRegistrationResponse> importController =
				new ImportClientController<ImportRpaScenarioOperationResultCreateSettingRecordRequest, ImportRpaScenarioOperationResultCreateSettingResponse, RecordRegistrationResponse>(
				getLogger(), Messages.getString("rpa.scenario.operation.result.create.setting"), confirmer.getImportRecDtoList(),true) {
			@Override
			protected List<RecordRegistrationResponse> getResRecList(ImportRpaScenarioOperationResultCreateSettingResponse importResponse) {
				return importResponse.getResultList();
			};
			@Override
			protected Boolean getOccurException(ImportRpaScenarioOperationResultCreateSettingResponse importResponse) {
				return importResponse.getIsOccurException();
			};
			@Override
			protected String getReqKeyValue(ImportRpaScenarioOperationResultCreateSettingRecordRequest importRec) {
				return importRec.getImportData().getScenarioOperationResultCreateSettingId();
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
			protected ImportRpaScenarioOperationResultCreateSettingResponse callImportWrapper(List<ImportRpaScenarioOperationResultCreateSettingRecordRequest> importRecList)
					throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
				ImportRpaScenarioOperationResultCreateSettingRequest reqDto = new ImportRpaScenarioOperationResultCreateSettingRequest();
				reqDto.setRecordList(importRecList);
				reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
				return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importRpaScenarioOperationResultCreateSetting(reqDto);
			}
			@Override
			protected String getRestExceptionMessage(RecordRegistrationResponse responseRec) {
				if (responseRec.getExceptionInfo() != null) {
					return responseRec.getExceptionInfo().getException() +":"+ responseRec.getExceptionInfo().getMessage();
				}
				return null;
			};
		};
		returnValue = importController.importExecute();
		for( RecordRegistrationResponse rec: importController.getImportSuccessList() ){
			objectIdList.add(rec.getImportKeyValue());
		}
		
		return returnValue;
	}
	
	protected List<RpaScenarioOperationResultCreateSettingResponse> getFilterdRpaScenarioOperationResultCreateSettingList() 
			throws HinemosUnknown, InvalidRole, InvalidUserPass, RpaScenarioOperationResultCreateSettingNotFound, RestConnectFailed {
		List<RpaScenarioOperationResultCreateSettingResponse> settingList = null;
		
		settingList =
				RpaRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getRpaScenarioOperationResultCreateSettingList();
		
		return settingList;
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
		log.debug("Start Differrence RPA Scenario Operation Result Create Setting ");

		int ret = 0;
		// XMLファイルからの読み込み
		RpaScenarioOperationResultCreateSettings settings1 = null;
		RpaScenarioOperationResultCreateSettings settings2 = null;
		try {
			settings1 = XmlMarshallUtil.unmarshall(RpaScenarioOperationResultCreateSettings.class,new InputStreamReader(new FileInputStream(xmlFile1), "UTF-8"));
			settings2 = XmlMarshallUtil.unmarshall(RpaScenarioOperationResultCreateSettings.class,new InputStreamReader(new FileInputStream(xmlFile2), "UTF-8"));
			sort(settings1);
			sort(settings2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence RPA Scenario Operation Result Create Setting (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(settings1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		if(!checkSchemaVersion(settings2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(settings1, settings2, RpaScenarioOperationResultCreateSettings.class, resultA);
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
		getLogger().debug("End Differrence RPA Scenario Operation Result Create Setting");

		return ret;
	}
	
	private void sort(RpaScenarioOperationResultCreateSettings settings) {
		RpaScenarioOperationResultCreateSetting[] infoList = settings.getRpaScenarioOperationResultCreateSetting();
		Arrays.sort(infoList,
			new Comparator<RpaScenarioOperationResultCreateSetting>() {
				@Override
				public int compare(RpaScenarioOperationResultCreateSetting info1, RpaScenarioOperationResultCreateSetting info2) {
					return info1.getRpaScenarioOperationResultCreateSettingInfo().getRpaScenarioOperationResultCreateSettingId()
							.compareTo(info2.getRpaScenarioOperationResultCreateSettingInfo().getRpaScenarioOperationResultCreateSettingId());
				}
			});
		settings.setRpaScenarioOperationResultCreateSetting(infoList);
	}
	
	public Logger getLogger() {
		return log;
	}
}
