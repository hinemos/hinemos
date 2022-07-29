/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.system.action;

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
import org.openapitools.client.model.AddMaintenanceRequest;
import org.openapitools.client.model.ImportMaintenanceRecordRequest;
import org.openapitools.client.model.ImportMaintenanceRequest;
import org.openapitools.client.model.ImportMaintenanceResponse;
import org.openapitools.client.model.MaintenanceInfoResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.maintenance.util.MaintenanceRestClientWrapper;
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
import com.clustercontrol.utility.settings.maintenance.xml.Maintenance;
import com.clustercontrol.utility.settings.maintenance.xml.MaintenanceInfo;
import com.clustercontrol.utility.settings.maintenance.xml.MaintenanceType;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;

import com.clustercontrol.utility.settings.system.conv.MaintenanceConv;
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
 * メンテナンス定義情報をインポート・エクスポート・削除するアクションクラス<br>
 * 
 * @version 6.1.0
 * @since 1.0.0
 * 
 */
public class MaintenanceAction {

	protected static Logger log = Logger.getLogger(MaintenanceAction.class);

	public MaintenanceAction() throws ConvertorException {
		super();
	}
	
	/**
	 * 情報をマネージャから削除します。<BR>
	 * 
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearMaintenance() {

		log.debug("Start Clear PlatformMaintenance ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		// メンテナンス定義一覧の取得
		List<MaintenanceInfoResponse>  maintenanceInfoList = null;
		try {
			maintenanceInfoList = MaintenanceRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMaintenanceList();
			Collections.sort(maintenanceInfoList, new Comparator<MaintenanceInfoResponse>() {
				@Override
				public int compare(
						MaintenanceInfoResponse info1,
						MaintenanceInfoResponse info2) {
					return info1.getMaintenanceId().compareTo(info2.getMaintenanceId());
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear PlatformMaintenance (Error)");
			return ret;
		}

		// メンテナンス定義の削除
		for (MaintenanceInfoResponse maintenanceInfo : maintenanceInfoList) {
			try {
				MaintenanceRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteMaintenance(maintenanceInfo.getMaintenanceId());
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + maintenanceInfo.getMaintenanceId());
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
		log.debug("End Clear PlatformMaintenance ");
		return ret;

	}

	/**
	 * 情報をマネージャから取得し、XMLに出力します。<BR>
	 * 
	 * @param 出力するXMLファイル
	 * @return 終了コード
	 */
	@ExportMethod
	public int exportMaintenance(String xmlFile) {

		log.debug("Start Export PlatformMaintenance ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		// メンテナンス定義一覧の取得
		List<MaintenanceInfoResponse> maintenanceInfoList = null;
		try {
			maintenanceInfoList = MaintenanceRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMaintenanceList();
			Collections.sort(maintenanceInfoList, new Comparator<MaintenanceInfoResponse>() {
				@Override
				public int compare(
						MaintenanceInfoResponse info1,
						MaintenanceInfoResponse info2) {
					return info1.getMaintenanceId().compareTo(info2.getMaintenanceId());
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export PlatformMaintenance (Error)");
			return ret;
		}

		// メンテナンス定義の格納
		Maintenance maintenance = new Maintenance();
		for (MaintenanceInfoResponse maintenanceInfo : maintenanceInfoList) {
			try {
				maintenance.addMaintenanceInfo(MaintenanceConv.getMaintenanceInfo(maintenanceInfo));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + maintenanceInfo.getMaintenanceId());
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		// XMLファイルに出力
		try {
			maintenance.setCommon(com.clustercontrol.utility.settings.platform.conv.CommonConv.versionMaintenanceDto2Xml(Config.getVersion()));
			maintenance.setSchemaInfo(MaintenanceConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlFile);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				maintenance.marshal(osw);
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Export PlatformMaintenance ");
		return ret;
	}

	/**
	 * XMLの情報をマネージャに投入します。<BR>
	 * 
	 * @param 入力するXMLファイル
	 * @return 終了コード
	 */
	@ImportMethod
	public int importMaintenance(String xmlFile) {

		log.debug("Start Import PlatformMaintenance ");
		
		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
	    	getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
	    	getLogger().debug("End Import PlatformMaintenance (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
	    }
		
		// 返り値変数(条件付き正常終了用）
		int ret = 0;
		MaintenanceType maintenance = null;
		// XMLファイルからの読み込み
		try {
			maintenance = XmlMarshallUtil.unmarshall(MaintenanceType.class,new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Inport PlatformMaintenance (Error)");
			return ret;
		}
		
		/*スキーマのバージョンチェック*/
		if(!this.checkSchemaVersion(maintenance.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		// メンテナンス定義の登録
		List<String> objectIdList = new ArrayList<String>();
		ImportMaintenanceRecordConfirmer maintenanceConfirmer = new ImportMaintenanceRecordConfirmer( log, maintenance.getMaintenanceInfo() );
		int maintenanceConfirmerRet = maintenanceConfirmer.executeConfirm();
		if (maintenanceConfirmerRet != 0) {
			ret = maintenanceConfirmerRet;
		}
		// レコードの登録（履歴削除）
		if (!(maintenanceConfirmer.getImportRecDtoList().isEmpty())) {
			ImportMaintenanceClientController maintenanceController = new ImportMaintenanceClientController(log,
					Messages.getString("maintenance.name"), maintenanceConfirmer.getImportRecDtoList(), true);
			int maintenanceControllerRet = maintenanceController.importExecute();
			for (RecordRegistrationResponse rec: maintenanceController.getImportSuccessList() ){
				objectIdList.add(rec.getImportKeyValue());
			}
			if (maintenanceControllerRet != 0) {
				ret = maintenanceControllerRet;
			}
		}
		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		//オブジェクト権限同時インポート
		importObjectPrivilege(HinemosModuleConstant.SYSYTEM_MAINTENANCE, objectIdList);
		
		//差分削除
		checkDelete(maintenance);
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Inport PlatformMaintenance ");
		return ret;
	}
	
	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.maintenance.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = MaintenanceConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.maintenance.xml.SchemaInfo sci = MaintenanceConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(log, res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
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

		log.debug("Start Differrence PlatformMaintenance ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		Maintenance maintenance1 = null;
		Maintenance maintenance2 = null;

		// XMLファイルからの読み込み
		try {
			maintenance1 = XmlMarshallUtil.unmarshall(Maintenance.class,new InputStreamReader(new FileInputStream(filePath1), "UTF-8"));
			maintenance2 = XmlMarshallUtil.unmarshall(Maintenance.class,new InputStreamReader(new FileInputStream(filePath2), "UTF-8"));
			sort(maintenance1);
			sort(maintenance2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence PlatformMaintenance (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(maintenance1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(maintenance2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(maintenance1, maintenance2, Maintenance.class, resultA);
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
		getLogger().debug("End Differrence PlatformMaintenance");

		return ret;
	}
	
	private void sort(Maintenance maintenance) {
		MaintenanceInfo[] infoList = maintenance.getMaintenanceInfo();
		Arrays.sort(
			infoList,
			new Comparator<MaintenanceInfo>() {
				@Override
				public int compare(MaintenanceInfo info1, MaintenanceInfo info2) {
					return info1.getMaintenanceId().compareTo(info2.getMaintenanceId());
				}
			});
		 maintenance.setMaintenanceInfo(infoList);
	}

	public Logger getLogger() {
		return log;
	}

	protected void checkDelete(MaintenanceType xmlElements){
		List<MaintenanceInfoResponse>  subList = null;
		try {
			subList = MaintenanceRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMaintenanceList();
		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}
		
		if(subList == null || subList.size() <= 0){
			return;
		}
		
		List<MaintenanceInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getMaintenanceInfo()));
		for(MaintenanceInfoResponse mgrInfo: new ArrayList<>(subList)){
			for(MaintenanceInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getMaintenanceId().equals(xmlElement.getMaintenanceId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(MaintenanceInfoResponse info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getMaintenanceId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
			    
			    if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
			    	try {
			    		MaintenanceRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteMaintenance(info.getMaintenanceId());
			    		getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getMaintenanceId());
					} catch (Exception e1) {
						getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
			    } else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
			    	getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getMaintenanceId());
			    } else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
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
	 * 履歴削除 インポート向けのレコード確認用クラス
	 * 
	 */
	protected static class ImportMaintenanceRecordConfirmer extends ImportRecordConfirmer<MaintenanceInfo, ImportMaintenanceRecordRequest, String>{
		
		public ImportMaintenanceRecordConfirmer(Logger logger, MaintenanceInfo[] importRecDtoList) {
			super(logger, importRecDtoList);
		}
		
		@Override
		protected ImportMaintenanceRecordRequest convertDtoXmlToRestReq(MaintenanceInfo xmlDto)
				throws HinemosUnknown, InvalidSetting {
			
			AddMaintenanceRequest  dto = MaintenanceConv.getMaintenanceInfoData(xmlDto);
			ImportMaintenanceRecordRequest dtoRec = new ImportMaintenanceRecordRequest();
			dtoRec.setImportData(dto);
			dtoRec.setImportKeyValue(dtoRec.getImportData().getMaintenanceId());
			
			return dtoRec;
		}

		@Override
		protected Set<String> getExistIdSet() throws Exception {
			Set<String> retSet = new HashSet<String>();
			List<MaintenanceInfoResponse> maintenanceInfoList = MaintenanceRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMaintenanceList();
			for (MaintenanceInfoResponse rec : maintenanceInfoList) {
				retSet.add(rec.getMaintenanceId());
			}
			return retSet;
		}
		@Override
		protected boolean isLackRestReq(ImportMaintenanceRecordRequest restDto) {
			return (restDto == null || restDto.getImportData().getMaintenanceId() == null || restDto.getImportData().getMaintenanceId().equals(""));
		}
		@Override
		protected String getKeyValueXmlDto(MaintenanceInfo xmlDto) {
			return xmlDto.getMaintenanceId();
		}
		@Override
		protected String getId(MaintenanceInfo xmlDto) {
			return xmlDto.getMaintenanceId();
		}
		@Override
		protected void setNewRecordFlg(ImportMaintenanceRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
	}
	
	/**
	 * 履歴削除 インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportMaintenanceClientController extends ImportClientController<ImportMaintenanceRecordRequest, ImportMaintenanceResponse, RecordRegistrationResponse>{
		
		public ImportMaintenanceClientController(Logger logger, String importInfoName, List<ImportMaintenanceRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportMaintenanceResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportMaintenanceResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportMaintenanceRecordRequest importRec) {
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
		protected ImportMaintenanceResponse callImportWrapper(List<ImportMaintenanceRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportMaintenanceRequest reqDto = new ImportMaintenanceRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importMaintenance(reqDto);
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
