/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.castor.xml.XMLProperties;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.XMLContext;
import org.openapitools.client.model.ImportMonitorCommonRecordRequest;
import org.openapitools.client.model.ImportMonitorCommonRequest;
import org.openapitools.client.model.ImportMonitorCommonResponse;
import org.openapitools.client.model.MonitorInfoRequestForUtility;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
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
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.AccountUtil;
import com.clustercontrol.utility.util.ImportClientController;
import com.clustercontrol.utility.util.ImportRecordConfirmer;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;
import com.clustercontrol.utility.util.XmlMarshallUtil;

/**
 * 監視クラスの抽象クラスとなります。
 *
 * @version 6.1.0
 * @since 2.0.0
 *
 *
 */
public abstract class AbstractMonitorAction<T> {
	
	protected String targetTitle = Messages.getString("monitor");
	public AbstractMonitorAction() throws ConvertorException {
		super();
	}

	@SuppressWarnings("unchecked")
	@ImportMethod
	public int importXml(String filePath) throws ConvertorException, InvalidRole, InvalidUserPass, MonitorNotFound, InvalidSetting, HinemosUnknown, ParseException, RestConnectFailed {
		getLogger().debug("Start Import "  + getDataClass().getSimpleName());

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
	    	getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
	    	getLogger().debug("End Import "  + getDataClass().getSimpleName() + " (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
	    }

		int returnValue = SettingConstants.SUCCESS;

		// XMLファイルからの読み込み
		T object = null;
		try {
			// ジェネリックの限界。テンプレート パラメータに指定した型に所属する static な関数を直接よべない。
			// そのため、Class 型を返す関数を用意したり、 Unmarshaller を直接呼ぶ必要が発生する。
			// 下位互換向けにXMLの内容確認（順番チェック）を緩くする
			object = XmlMarshallUtil.unmarshall(getDataClass(),new InputStreamReader(new FileInputStream(filePath), "UTF-8"));

		} catch (Exception e) {
			returnValue = handleCastorException(e);
			getLogger().debug("End Import "  + getDataClass().getSimpleName() + " (Error)");
			return returnValue;
		}

		// スキーマのバージョンチェック
		if (!checkSchemaVersionScope(object)) {
			return SettingConstants.ERROR_SCHEMA_VERSION;
		}

		// castor の 情報を DTO に変換。
		List<MonitorInfoResponse> monitorInfoList = null;
		try {
			monitorInfoList = createMonitorInfoList(object);
		} catch (Exception e) {
			if (e instanceof ConvertorException) {
				// ConvertorException は現状、数値監視情報の閾値設定件数間違いで出力されるのでスタックトレースはなし
				getLogger().warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			} else {
				getLogger().warn(Messages.getString("SettingTools.ImportFailed"), e);
			}
			// DTO変換中に例外が発生した場合、 monitorInfoList は初期化状態のまま(件数0)なので、インポートを中断
			return SettingConstants.ERROR_INPROCESS;
		}

		// MonitorInfo をマネージャに登録。
		List<String> objectIdList = new ArrayList<String>();
		returnValue = importMonitorList(monitorInfoList ,objectIdList);

		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			getLogger().info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		//オブジェクト権限同時インポート
		importObjectPrivilege(com.clustercontrol.bean.HinemosModuleConstant.MONITOR, objectIdList);
		
		//差分削除
		checkDelete(monitorInfoList);
		
		// 処理の終了
		if (returnValue == 0) {
			getLogger().info(Messages.getString("SettingTools.ImportCompleted"));
		}else{
			getLogger().error(Messages.getString("SettingTools.EndWithErrorCode") );
		}
		getLogger().debug("End Import " + getDataClass().getSimpleName());

		return returnValue;
	}

	@SuppressWarnings("unchecked")
	@DiffMethod
	public int diffXml(String filePath1, String filePath2) throws ConvertorException {
		getLogger().debug("Search differrence: "  + getDataClass().getSimpleName());

		int returnValue = SettingConstants.SUCCESS;

		// XMLファイルからの読み込み
		T object1 = null;
		T object2 = null;
		try {
			// ジェネリックの限界。テンプレート パラメータに指定した型に所属する static な関数を直接よべない。
			// そのため、Class 型を返す関数を用意したり、 Unmarshaller を直接呼ぶ必要が発生する。
			object1 = XmlMarshallUtil.unmarshall(getDataClass(),new InputStreamReader(new FileInputStream(filePath1), "UTF-8"));
			object2 = XmlMarshallUtil.unmarshall(getDataClass(),new InputStreamReader(new FileInputStream(filePath2), "UTF-8"));
			sort(object1);
			sort(object2);
		} catch (Exception e) {
			returnValue = handleCastorException(e);
			getLogger().debug("End Import "  + getDataClass().getSimpleName() + " (Error)");
			return returnValue;
		}

		// スキーマのバージョンチェック
		if (!checkSchemaVersionScope(object1)) {
			return SettingConstants.ERROR_SCHEMA_VERSION;
		}
		if (!checkSchemaVersionScope(object2)) {
			return SettingConstants.ERROR_SCHEMA_VERSION;
		}

		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			boolean diff = DiffUtil.diffCheck2(object1, object2, getDataClass(), resultA);
			assert resultA.getResultBs().size() == 1;
			
			if (diff){
				returnValue += SettingConstants.SUCCESS_DIFF_1;
			}

			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(filePath2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			else {
				File f = new File(filePath2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						getLogger().warn(String.format("Fail to delete file. %s", f.getAbsolutePath()));
				}
			}
		}
		catch (Exception e) {
			getLogger().error("unexpected: ", e);
			returnValue = SettingConstants.ERROR_INPROCESS;
		}
		catch (Error e) {
			getLogger().error("unexpected: ", e);
			returnValue = SettingConstants.ERROR_INPROCESS;
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
		if ((returnValue >= SettingConstants.SUCCESS) && (returnValue<=SettingConstants.SUCCESS_MAX)){
			getLogger().info(Messages.getString("SettingTools.DiffCompleted"));
		}else{
			getLogger().error(Messages.getString("SettingTools.EndWithErrorCode") );
		}

		getLogger().debug("End differrence: " + getDataClass().getSimpleName());

		return returnValue;
	}

	/**
	 * スキーマのバージョンチェックを行う。
	 *
	 * @param Castor で生成されたクラスのインスタンス
	 * @return
	 */
	protected abstract boolean checkSchemaVersionScope(T object);

	/**
	 * Castor で生成されたクラスの Class 型を返す。
	 *
	 * @return
	 */
	protected abstract Class<T> getDataClass();

	/**
	 * Castor で生成されたクラスの インスタンスから、MonitorInfo のリストを作成する。
	 *
	 * @param Castor で生成されたクラスのインスタンス
	 * @return
	 * @throws ParseException 
	 * @throws InvalidSetting 
	 * @throws MonitorNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	protected abstract List<MonitorInfoResponse> createMonitorInfoList(T object) throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, InvalidSetting, ParseException, RestConnectFailed  ;

	@ExportMethod
	public int exportDTO(String filePath) throws ConvertorException {
		getLogger().debug("Start Export " + getDataClass().getSimpleName());

		// エージェント監視情報を取得。
		List<MonitorInfoResponse> monitorInfoList_dto = null;
		try {
			monitorInfoList_dto = getFilterdMonitorList();
			Collections.sort(
				monitorInfoList_dto,
				new Comparator<MonitorInfoResponse>() {
					/**
					 * 監視項目IDを比較する。
					 */
					@Override
					public int compare(MonitorInfoResponse monitorInfo1, MonitorInfoResponse monitorInfo2) {
						return monitorInfo1.getMonitorId().compareTo(monitorInfo2.getMonitorId());
					}
				});
		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " " + e);
			getLogger().debug(e.getMessage(), e);
			getLogger().debug("End Export " + getDataClass().getSimpleName() + " (Error)");
			return SettingConstants.ERROR_INPROCESS;
		}


		// XMLファイルに出力
		int ret = SettingConstants.SUCCESS;
		try {
			// Caster のデータ構造に変換。
			Object monitorInfoList_cas = createCastorData(monitorInfoList_dto);
			try(FileOutputStream fos = new FileOutputStream(filePath);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
					Marshaller.marshal(monitorInfoList_cas, osw);

			}

			for (MonitorInfoResponse monitorInfo: monitorInfoList_dto){
				getLogger().info(Messages.getString("SettingTools.ExportSucceeded") + " : " + monitorInfo.getMonitorId());
			}

		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.MarshalXmlFailed") + " " + e);
			getLogger().debug(e.getMessage(), e);
			ret = SettingConstants.ERROR_INPROCESS;
		}catch(Error e){
			// Error時もメッセージ出力
			getLogger().error(Messages.getString("SettingTools.MarshalXmlFailed") + " " + e);
			getLogger().debug(e.getMessage(), e);
			ret = SettingConstants.ERROR_INPROCESS;
		}

		// 処理の終了
		if (ret == 0) {
			getLogger().info(Messages.getString("SettingTools.ExportCompleted"));
		}else{
			getLogger().error(Messages.getString("SettingTools.EndWithErrorCode") );
		}

		getLogger().debug("End Export " + getDataClass().getSimpleName());
		return ret;
	}

	/**
	 * 特定の種別でフィルターされた MonitorInfo のリストを返す。
	 *
	 * @return
	 * @throws MonitorNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	protected abstract List<MonitorInfoResponse> getFilterdMonitorList() throws HinemosUnknown,InvalidRole, InvalidUserPass, MonitorNotFound, InvalidSetting, RestConnectFailed;

	/**
	 * 指定した MonitorInfo から、Castor のデータを作成する。
	 *
	 * @param
	 * @return
	 * @throws ParseException 
	 * @throws RestConnectFailed 
	 * @throws MonitorNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	protected abstract T createCastorData(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException;

	/**
	 *  MonitorInfo をインポートする。
	 * 
	 * 個別に対応が必要な場合はオーバーライドする事

	 * @return
	 * @throws MonitorNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	protected int importMonitorList(List<MonitorInfoResponse> monitorInfoList , List<String> objectIdList) throws HinemosUnknown,InvalidRole, InvalidUserPass, MonitorNotFound,RestConnectFailed{
		int returnValue =0;
		ImportRecordConfirmer<MonitorInfoResponse, ImportMonitorCommonRecordRequest, String> confirmer = new ImportRecordConfirmer<MonitorInfoResponse, ImportMonitorCommonRecordRequest, String>(
				getLogger(), monitorInfoList.toArray(new MonitorInfoResponse[0])) {
			@Override
			protected ImportMonitorCommonRecordRequest convertDtoXmlToRestReq(MonitorInfoResponse xmlDto)
					throws HinemosUnknown, InvalidSetting {
				ImportMonitorCommonRecordRequest dtoRec = new ImportMonitorCommonRecordRequest();
				dtoRec.setImportData(new MonitorInfoRequestForUtility());
				RestClientBeanUtil.convertBean(xmlDto, dtoRec.getImportData());
				dtoRec.setImportKeyValue(dtoRec.getImportData().getMonitorId());
				dtoRec.setMonitorModule(dtoRec.getImportData().getMonitorTypeId());
				return dtoRec;
			}
			@Override
			protected Set<String> getExistIdSet() throws Exception {
				Set<String> retSet = new HashSet<String>();
				for(MonitorInfoResponse rec :getFilterdMonitorList()){
					retSet.add(rec.getMonitorId());
				}
				return retSet;
			}
			@Override
			protected boolean isLackRestReq(ImportMonitorCommonRecordRequest restDto) {
				return false;
			}
			@Override
			protected String getKeyValueXmlDto(MonitorInfoResponse xmlDto) {
				return xmlDto.getMonitorId();
			}
			@Override
			protected String getId(MonitorInfoResponse xmlDto) {
				return xmlDto.getMonitorId();
			}
			@Override
			protected void setNewRecordFlg(ImportMonitorCommonRecordRequest restDto, boolean flag) {
				restDto.setIsNewRecord(flag);
			}
		};
		int confirmRet = confirmer.executeConfirm();
		if( confirmRet != SettingConstants.SUCCESS && confirmRet != SettingConstants.ERROR_CANCEL ){
			//変換エラーならUnmarshalXml扱いで処理打ち切り(キャンセルはキャンセル以前の選択結果を反映するので次に進む)
			getLogger().warn(Messages.getString("SettingTools.UnmarshalXmlFailed"));
			return confirmRet;
		}

		ImportClientController<ImportMonitorCommonRecordRequest, ImportMonitorCommonResponse, RecordRegistrationResponse> importController = new ImportClientController<ImportMonitorCommonRecordRequest, ImportMonitorCommonResponse, RecordRegistrationResponse>(
				getLogger(), targetTitle, confirmer.getImportRecDtoList(),true) {
			@Override
			protected List<RecordRegistrationResponse> getResRecList(ImportMonitorCommonResponse importResponse) {
				return importResponse.getResultList();
			};
			@Override
			protected Boolean getOccurException(ImportMonitorCommonResponse importResponse) {
				return importResponse.getIsOccurException();
			};
			@Override
			protected String getReqKeyValue(ImportMonitorCommonRecordRequest importRec) {
				return importRec.getImportData().getMonitorId();
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
			protected ImportMonitorCommonResponse callImportWrapper(List<ImportMonitorCommonRecordRequest> importRecList)
					throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
				ImportMonitorCommonRequest reqDto = new ImportMonitorCommonRequest();
				reqDto.setRecordList(importRecList);
				reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
				return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importMonitorCommon(reqDto);
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

	@ClearMethod
	public int clear() throws ConvertorException {
		getLogger().debug("Start Clear " + getDataClass().getSimpleName());

		// 対象種別の監視ID一覧の取得
		List<MonitorInfoResponse> monitorList = null;
		try {
			monitorList = getFilterdMonitorList();
		} catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(HinemosMessage.replace(e.getMessage()), e);
			getLogger().debug("End Clear " + getDataClass().getSimpleName() + " (Error)");
			return SettingConstants.ERROR_INPROCESS;
		}
		// 監視設定の削除
		int returnValue = SettingConstants.SUCCESS;

		Map<String, List<String>> monitorMap = new HashMap<>();
		
		for (MonitorInfoResponse monitorInfo : monitorList) {
			if(!monitorMap.containsKey(monitorInfo.getMonitorTypeId())){
				monitorMap.put(monitorInfo.getMonitorTypeId(), new ArrayList<String>());
			}
			monitorMap.get(monitorInfo.getMonitorTypeId()).add(monitorInfo.getMonitorId());
		}

		for(Entry<String, List<String>> ent : monitorMap.entrySet()){
			if (AccountUtil.isAdministrator(UtilityManagerUtil.getCurrentManagerName())) {
				// ADMINISTRATORS権限がある場合
				try {
					
					MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteMonitor(String.join(",", ent.getValue()));
					getLogger().info(Messages.getString("SettingTools.ClearSucceeded") + " : " + ent.getValue().toString());
				} catch (Exception e) {
					getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
					returnValue = SettingConstants.ERROR_INPROCESS;
				}
			} else {
				// ADMINISTRATORS権限がない場合
				for (String id : ent.getValue()) {
					try {
						MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteMonitor(id);
						getLogger().info(Messages.getString("SettingTools.ClearSucceeded") + " : " + id);
					} catch (Exception e) {
						getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
						returnValue = SettingConstants.ERROR_INPROCESS;
					}
				}
			}
		}

		// 処理の終了
		if (returnValue == 0) {
			getLogger().info(Messages.getString("SettingTools.ClearCompleted"));
		}else{
			getLogger().error(Messages.getString("SettingTools.EndWithErrorCode") );
		}

		getLogger().debug("End Clear " + getDataClass().getSimpleName());

		return returnValue;
	}

	/**
	 * Castor のマーシャリング中に発生した例外の処理をする。
	 *
	 * @param
	 */
	protected int handleCastorException(Exception e) {

		getLogger().error(Messages.getString("SettingTools.UnmarshalXmlFailed") + " " + e);
		getLogger().debug(e.getMessage(), e);

		return SettingConstants.ERROR_INPROCESS;
	}

	/**
	 * XML 側の情報をソートする。
	 *
	 * @param
	 */
	protected abstract void sort(T object);

	private Logger logger = Logger.getLogger(this.getClass());
	public Logger getLogger(){return logger;}

	protected void checkDelete(List<MonitorInfoResponse> xmlElements){

		List<MonitorInfoResponse> subList = null;
		try {
			subList = getFilterdMonitorList();
		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}

		if(subList == null || subList.size() <= 0){
			return;
		}

		for(MonitorInfoResponse mgrInfo: new ArrayList<>(subList)){
			for(MonitorInfoResponse xmlElement: new ArrayList<>(xmlElements)){
				if(mgrInfo.getMonitorId().equals(xmlElement.getMonitorId())){
					subList.remove(mgrInfo);
					xmlElements.remove(xmlElement);
					break;
				}
			}
		}

		if(subList.size() > 0){
			for(MonitorInfoResponse info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getMonitorId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}

				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					try {
						MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteMonitor(info.getMonitorId());
						getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getMonitorId());
					} catch (Exception e1) {
						getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getMonitorId());
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
}
