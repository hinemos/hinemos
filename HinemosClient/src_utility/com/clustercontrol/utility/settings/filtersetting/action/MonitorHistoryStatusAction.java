/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.filtersetting.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.openapitools.client.model.FilterSettingSummariesResponse;
import org.openapitools.client.model.FilterSettingSummaryResponse;
import org.openapitools.client.model.ImportFilterSettingResponse;
import org.openapitools.client.model.ImportStatusFilterSettingRecordRequest;
import org.openapitools.client.model.ImportStatusFilterSettingRequest;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;
import org.openapitools.client.model.StatusFilterSettingRequestForUtility;
import org.openapitools.client.model.StatusFilterSettingResponse;

import com.clustercontrol.fault.FilterSettingNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.filtersetting.util.FilterSettingRestClientWrapper;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.RestConnectManager;
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
import com.clustercontrol.utility.settings.filtersetting.conv.MonitorHistoryStatusConv;
import com.clustercontrol.utility.settings.filtersetting.xml.FilterMonitorHistoryStatus;
import com.clustercontrol.utility.settings.filtersetting.xml.FiltersettingMonitorHistoryStatuses;
import com.clustercontrol.utility.settings.filtersetting.xml.SchemaInfo;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.FilterSettingProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.AccountUtil;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.FilterSettingUtil;
import com.clustercontrol.utility.util.ImportClientController;
import com.clustercontrol.utility.util.ImportRecordConfirmer;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;
import com.clustercontrol.utility.util.XmlMarshallUtil;
/**
 * 監視設定[ステータス]フィルタ設定情報を処理するためのアクションクラスです。<br>
 * マネージャに接続し、監視設定[ステータス]フィルタ設定の取得、設定、削除をします。<br>
 * XMLファイルに定義された監視設定[ステータス]フィルタ設定情報を取得します。<br>
 * 
 */
public class MonitorHistoryStatusAction {
	// ロガー
	private static Logger log = Logger.getLogger(MonitorHistoryStatusAction.class);

	/**
	 * コンストラクタ
	 * 
	 */
	public MonitorHistoryStatusAction(){
	}

	/**
	 * フィルタ設定情報をマネージャに投入します。
	 * 
	 */
	@ImportMethod
	public int importMonitorHistoryStatus(String xmlFile) 
			throws InvalidRole, InvalidUserPass, FilterSettingNotFound, HinemosUnknown, RestConnectFailed, InvalidSetting {
		
		ArrayList<Boolean> filterTypeList = FilterSettingProcessMode.getFilterTypeList(this.getClass().getName());
		Boolean userRange = FilterSettingProcessMode.getUserFilterRange(this.getClass().getName());
		String fileName = xmlFile;
		
		log.debug("Start Import FilterSettingMonitorHistoryStatus :" + fileName);
		
		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			getLogger().debug("End Import FilterSettingMonitorHistoryStatus (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		int ret=0;
		
		// XMLファイルからの読み込み
		FiltersettingMonitorHistoryStatuses filters = null;
		try {
			filters = XmlMarshallUtil.unmarshall(FiltersettingMonitorHistoryStatuses.class,new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import FilterSettingMonitorHistoryStatus (Error)");
			return ret;
		}
		
		/* スキーマのバージョンチェック*/
		if(!checkSchemaVersion(filters.getSchemaInfo())){
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		// castor の 情報を DTO に変換。
		List<StatusFilterSettingResponse> filterList = null;
		try {
			filterList = createFilterSettingList(filters, filterTypeList, userRange);
		} catch (Exception e) {
			if (e instanceof ConvertorException) {
				getLogger().warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			} else {
				getLogger().warn(Messages.getString("SettingTools.ImportFailed"), e);
			}
			// DTO変換中に例外が発生した場合、filterList は初期化状態のまま(件数0)なので、インポートを中断
			return SettingConstants.ERROR_INPROCESS;
		}
		// 一意チェック（複数項目構成の一意チェックは編集Excelでは対応していないため）
		try {
			FilterSettingUtil.confirmUniq(FilterSettingUtil.getKeyListStatus(filterList));
		} catch (Exception e) {
			getLogger().warn(Messages.getString("SettingTools.UnmarshalXmlFailed") + " : " + e.getMessage());
			return SettingConstants.ERROR_INPROCESS;
		}

		
		//マネージャに投入
		List<String> objectIdList = new ArrayList<String>();
		ret = importFilterSettingList(filterList, objectIdList);
		
		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}

		//オブジェクト権限同時インポート
		importObjectPrivilege(HinemosModuleConstant.FILTER_SETTING, objectIdList);
		
		checkDelete(filterList, filterTypeList, userRange);
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		}else{
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
		}
		
		log.debug("End Import FilterSettingMonitorHistoryStatus : " + fileName);

		return ret;
	}
	
	protected void checkDelete(List<StatusFilterSettingResponse> xmlElements, ArrayList<Boolean> filterTypeList, Boolean userRange){

		List<FilterSettingSummaryResponse> subList = new ArrayList<>();
		try {
			FilterSettingSummariesResponse commonRes = null;
			FilterSettingSummariesResponse userRes = null;
			
			FilterSettingRestClientWrapper wrapper = 
					FilterSettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
			
			// 共有フィルタ
			if (filterTypeList.contains(true)){
				commonRes = wrapper.getCommonFilterSettingSummaries(FilterCategoryEnum.STATUS, "");
				
				if(commonRes != null){
					for(FilterSettingSummaryResponse res : commonRes.getSummaries()){
						subList.add(res);
					}
				}
			}
			
			// ユーザフィルタ
			if (filterTypeList.contains(false)){
				if(userRange){
					userRes = wrapper.getAllUserFilterSettingSummaries(FilterCategoryEnum.STATUS, "");
				} else {
					userRes = wrapper.getUserFilterSettingSummaries(FilterCategoryEnum.STATUS, "", 
							RestConnectManager.getLoginUserId(UtilityManagerUtil.getCurrentManagerName()));
				}
				
				if(userRes != null){
					for(FilterSettingSummaryResponse res :userRes.getSummaries()){
						subList.add(res);
					}
				}
			}
		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}

		if(subList == null || subList.size() <= 0){
			return;
		}
		
		for(FilterSettingSummaryResponse mgrInfo: new ArrayList<>(subList)){
			for(StatusFilterSettingResponse xmlElement: new ArrayList<>(xmlElements)){
				if(mgrInfo.getFilterId().equals(xmlElement.getFilterId())
						&& ((mgrInfo.getOwnerUserId() != null 
							&& mgrInfo.getOwnerUserId().equals(xmlElement.getOwnerUserId())) 
						|| (mgrInfo.getOwnerUserId() == null 
							&& xmlElement.getOwnerUserId().equals(MonitorHistoryStatusConv.getCommonFilterOwnerValue())))){
					subList.remove(mgrInfo);
					xmlElements.remove(xmlElement);
					break;
				}
			}
		}

		if(subList.size() > 0){
			
			for(FilterSettingSummaryResponse info: subList){
				
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getFilterId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					
					try {
						if(info.getCommon()){
							FilterSettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName())
							.deleteCommonStatusFilterSetting(info.getFilterId());
						} else {
							FilterSettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName())
							.deleteUserStatusFilterSetting(info.getFilterId(), info.getOwnerUserId());
						}
						
						getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getFilterId());
					} catch (Exception e1) {
						getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getFilterId());
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
					return;
				}
			}
		}
	}
	
	/**
	 *  FilterSetting をインポートする。
	 *  
	 */
	protected int importFilterSettingList(List<StatusFilterSettingResponse> filterList, List<String> objectIdList) 
			throws HinemosUnknown,InvalidRole, InvalidUserPass, FilterSettingNotFound, RestConnectFailed, InvalidSetting{
		int returnValue =0;
		ImportRecordConfirmer<StatusFilterSettingResponse, ImportStatusFilterSettingRecordRequest, StatusFilterSettingResponse> confirmer =
				new ImportRecordConfirmer<StatusFilterSettingResponse, ImportStatusFilterSettingRecordRequest, StatusFilterSettingResponse>(
				getLogger(), filterList.toArray(new StatusFilterSettingResponse[filterList.size()])) {
			@Override
			protected ImportStatusFilterSettingRecordRequest convertDtoXmlToRestReq(StatusFilterSettingResponse xmlDto)
					throws HinemosUnknown, InvalidSetting {
				ImportStatusFilterSettingRecordRequest dtoRec = new ImportStatusFilterSettingRecordRequest();
				dtoRec.setImportData(new StatusFilterSettingRequestForUtility());
				RestClientBeanUtil.convertBean(xmlDto, dtoRec.getImportData());
				dtoRec.setImportKeyValue(dtoRec.getImportData().getFilterId());
				return dtoRec;
			}
			@Override
			protected boolean isExistRecord(StatusFilterSettingResponse xmlDto){
				// 複合キーでチェックする
				Optional<StatusFilterSettingResponse> existRecord = 
						existIdSet.stream().filter(res -> res.getFilterId().equals(xmlDto.getFilterId()) 
								&& ((res.getOwnerUserId() != null 
									&& res.getOwnerUserId().equals(xmlDto.getOwnerUserId())) 
								|| (res.getOwnerUserId() == null 
									&& xmlDto.getOwnerUserId().equals(MonitorHistoryStatusConv.getCommonFilterOwnerValue()))))
						.findFirst();
				
				return existRecord.isPresent();
			};
			@Override
			protected Set<StatusFilterSettingResponse> getExistIdSet() throws Exception {
				Set<StatusFilterSettingResponse> retSet = new HashSet<StatusFilterSettingResponse>();
				FilterSettingRestClientWrapper wrapper = 
						FilterSettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
				// 共有フィルタ
				FilterSettingSummariesResponse commonList =
						wrapper.getCommonFilterSettingSummaries(FilterCategoryEnum.STATUS, "");
				for(FilterSettingSummaryResponse summary :commonList.getSummaries()){
					StatusFilterSettingResponse rec =
							wrapper.getCommonStatusFilterSetting(summary.getFilterId());
					retSet.add(rec);
				}
				
				// ユーザフィルタ(アクセス権限に合わせて呼び出すメソッドを変更)
				FilterSettingSummariesResponse userList = null;
				String managerName =UtilityManagerUtil.getCurrentManagerName();
				boolean belongingAdmins = AccountUtil.isAdministrator(managerName);
				if(belongingAdmins){
					userList = wrapper.getAllUserFilterSettingSummaries(FilterCategoryEnum.STATUS, "");
				}else{
					userList = wrapper.getUserFilterSettingSummaries(FilterCategoryEnum.STATUS, "", 
							RestConnectManager.getLoginUserId(UtilityManagerUtil.getCurrentManagerName()));
				}
				for(FilterSettingSummaryResponse summary :userList.getSummaries()){
					StatusFilterSettingResponse rec =
							wrapper.getUserStatusFilterSetting(summary.getFilterId(), summary.getOwnerUserId());
					retSet.add(rec);
				}
				
				return retSet;
			}
			@Override
			protected boolean isLackRestReq(ImportStatusFilterSettingRecordRequest restDto) {
				return false;
			}
			@Override
			protected String getKeyValueXmlDto(StatusFilterSettingResponse xmlDto) {
				return xmlDto.getFilterId();
			}
			@Override
			protected StatusFilterSettingResponse getId(StatusFilterSettingResponse xmlDto) {
				return xmlDto;
			}
			@Override
			protected void setNewRecordFlg(ImportStatusFilterSettingRecordRequest restDto, boolean flag) {
				restDto.setIsNewRecord(flag);
			}
		};
		int confirmRet = confirmer.executeConfirm();
		if( confirmRet != SettingConstants.SUCCESS && confirmRet != SettingConstants.ERROR_CANCEL ){
			//変換エラーならUnmarshalXml扱いで処理打ち切り(キャンセルはキャンセル以前の選択結果を反映するので次に進む)
			getLogger().warn(Messages.getString("SettingTools.UnmarshalXmlFailed"));
			return confirmRet;
		}

		ImportClientController<ImportStatusFilterSettingRecordRequest, ImportFilterSettingResponse, RecordRegistrationResponse> importController =
				new ImportClientController<ImportStatusFilterSettingRecordRequest, ImportFilterSettingResponse, RecordRegistrationResponse>(
				getLogger(), Messages.getString("fltset.dialog.title.status"), confirmer.getImportRecDtoList(),true) {
			@Override
			protected List<RecordRegistrationResponse> getResRecList(ImportFilterSettingResponse importResponse) {
				return importResponse.getResultList();
			};
			@Override
			protected Boolean getOccurException(ImportFilterSettingResponse importResponse) {
				return importResponse.getIsOccurException();
			};
			@Override
			protected String getReqKeyValue(ImportStatusFilterSettingRecordRequest importRec) {
				return importRec.getImportData().getFilterId();
			};
			@Override
			protected String getResKeyValue(RecordRegistrationResponse responseRec) {
				// フィルタ所有者,フィルタID の形式の場合 IDのみで返却
				String[] importKeyValueArray = responseRec.getImportKeyValue().split(",");
				if (importKeyValueArray.length == 2) {
					return importKeyValueArray[1];
				}
				return responseRec.getImportKeyValue();
			};
			@Override
			protected boolean isResNormal(RecordRegistrationResponse responseRec) {
				return (responseRec.getResult() == ResultEnum.NORMAL) ;
			};
			@Override
			protected ImportFilterSettingResponse callImportWrapper(List<ImportStatusFilterSettingRecordRequest> importRecList)
					throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
				ImportStatusFilterSettingRequest reqDto = new ImportStatusFilterSettingRequest();
				reqDto.setRecordList(importRecList);
				reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
				return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName())
						.importFilterSettingMonitorHistoryStatus(reqDto);
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
			String[] importKeyValue = rec.getImportKeyValue().split(",");
			
			// 0:フィルタ所有者,1:フィルタIDを想定
			if(importKeyValue[0].equals(MonitorHistoryStatusConv.getCommonFilterOwnerValue())){
				StatusFilterSettingResponse importRes = FilterSettingRestClientWrapper
						.getWrapper(UtilityManagerUtil.getCurrentManagerName())
						.getCommonStatusFilterSetting(importKeyValue[1]);
				objectIdList.add(importRes.getObjectId());
			}
		}
		
		return returnValue;
	}

	
	public List<StatusFilterSettingResponse> createFilterSettingList(FiltersettingMonitorHistoryStatuses filters, 
			ArrayList<Boolean> filterTypeList, Boolean userRange) 
					throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, 
					FilterSettingNotFound, InvalidSetting, ParseException {
		return MonitorHistoryStatusConv.createMonitorHistoryStatusList(filters, filterTypeList, userRange);
	}

	
	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.filtersetting.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = MonitorHistoryStatusConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		SchemaInfo sci = MonitorHistoryStatusConv.getSchemaVersion();
		
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	/**
	 * フィルタ設定情報をHinemosマネージャから取得し、XMLに出力します。
	 * 
	 */
	@ExportMethod
	public int exportMonitorHistoryStatus(String xmlFile) {
		ArrayList<Boolean> filterTypeList = FilterSettingProcessMode.getFilterTypeList(this.getClass().getName());
		Boolean userRange = FilterSettingProcessMode.getUserFilterRange(this.getClass().getName());
		String fileName = xmlFile;
		log.debug("Start Export FilterSettingMonitorHistoryStatus : " + fileName);

		int ret = 0;
		
		FilterSettingSummariesResponse commonRes = null;
		FilterSettingSummariesResponse userRes = null;
		
		FilterSettingRestClientWrapper wrapper = 
				FilterSettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		
		try {
			if (filterTypeList.contains(true)){
				commonRes = wrapper.getCommonFilterSettingSummaries(FilterCategoryEnum.STATUS, "");
			}
			if (filterTypeList.contains(false)){
				if(userRange){
					userRes = wrapper.getAllUserFilterSettingSummaries(FilterCategoryEnum.STATUS, "");
				} else {
					userRes = wrapper.getUserFilterSettingSummaries(FilterCategoryEnum.STATUS, "", 
							RestConnectManager.getLoginUserId(UtilityManagerUtil.getCurrentManagerName()));
				}
			}
		} catch (Exception e1) {
			log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export FilterSetting : " + fileName +"(Error)");
			return ret;
		}

		FiltersettingMonitorHistoryStatuses filters = new FiltersettingMonitorHistoryStatuses();
		// findbugs対応 利用されない初期インスタンスを nullに変更
		FilterMonitorHistoryStatus filter = null;
		
		if(commonRes != null){
			for( FilterSettingSummaryResponse summary : commonRes.getSummaries()){
				StatusFilterSettingResponse info = null;
				try{
					info  = wrapper.getCommonStatusFilterSetting(summary.getFilterId());
					filter = MonitorHistoryStatusConv.getFilterSettingMonitorHistoryStatus(info);
					filters.addFilterMonitorHistoryStatus(filter);
					log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + info.getFilterId());
				} catch (RestConnectFailed e) {
					log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
					break;
				} catch (Exception e) {
					log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				}
			}
		}
		
		if(userRes != null){
			for( FilterSettingSummaryResponse summary : userRes.getSummaries()){
				StatusFilterSettingResponse info = null;
				try{
					info  = wrapper.getUserStatusFilterSetting(summary.getFilterId(), 
							summary.getOwnerUserId());
					filter = MonitorHistoryStatusConv.getFilterSettingMonitorHistoryStatus(info);
					filters.addFilterMonitorHistoryStatus(filter);
					log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + info.getFilterId());
				} catch (RestConnectFailed e) {
					log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
					break;
				} catch (Exception e) {
					log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				}
			}
		}
		
		FileOutputStream output = null;
		try {
			//共通情報のセット
			filters.setCommon(MonitorHistoryStatusConv.versionFilterSettingDto2Xml(Config.getVersion()));
			//スキーマ情報のセット
			filters.setSchemaInfo(MonitorHistoryStatusConv.getSchemaVersion());

			//マーシャリング
			filters.marshal(new OutputStreamWriter(
					(output = new FileOutputStream(fileName)), "UTF-8"));
		} catch (Exception e) {
			log.warn(Messages.getString("SettingTools.ExportFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					log.warn(e);
				}
			}
		}

		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		}else{
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
		}

		log.debug("End Export FilterSettingMonitorHistoryStatus : " + fileName);
		return ret;
	}
	
	/**
	 * フィルタ設定情報を全て削除します。
	 * 
	 */
	@ClearMethod
	public int clearMonitorHistoryStatus(){
		ArrayList<Boolean> filterTypeList = FilterSettingProcessMode.getFilterTypeList(this.getClass().getName());
		Boolean userRange = FilterSettingProcessMode.getUserFilterRange(this.getClass().getName());
		
		log.debug("Start Clear FilterSettingMonitorHistoryStatus");
		int ret = 0;

		FilterSettingSummariesResponse commonRes = null;
		FilterSettingSummariesResponse userRes = null;
		
		FilterSettingRestClientWrapper wrapper = 
				FilterSettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		
		try {
			if (filterTypeList.contains(true)){
				commonRes = wrapper.getCommonFilterSettingSummaries(FilterCategoryEnum.STATUS, "");
			}
			if (filterTypeList.contains(false)){
				if(userRange){
					userRes = wrapper.getAllUserFilterSettingSummaries(FilterCategoryEnum.STATUS, "");
				} else {
					userRes = wrapper.getUserFilterSettingSummaries(FilterCategoryEnum.STATUS, "", 
							RestConnectManager.getLoginUserId(UtilityManagerUtil.getCurrentManagerName()));
				}
			}
		} catch ( RestConnectFailed |HinemosUnknown	| InvalidRole | InvalidUserPass | InvalidSetting e) {
			log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		List<String> deleteCommonFilterIdList = new ArrayList<String>();
		if(commonRes != null){
			for(FilterSettingSummaryResponse summary : commonRes.getSummaries()){
				deleteCommonFilterIdList.add(summary.getFilterId());
			}
		}
		
		List<String> cmnFilterIds = new ArrayList<>();
		Map<String, List<String>> usrFilterIdsMap = new HashMap<>();
		if(userRes != null){
			// フィルタIDを共通/ユーザ別に仕分け
			deleteFilterSettings_divideFilterIds(userRes.getSummaries(), cmnFilterIds, usrFilterIdsMap);
		}
		
		if (!deleteCommonFilterIdList.isEmpty()){
			for(String targetId : deleteCommonFilterIdList){
				try {
					wrapper.deleteCommonStatusFilterSetting(targetId);
					log.info(Messages.getString("SettingTools.ClearSucceeded") + " id:" + targetId);
				} catch (Exception e) {
					log.warn(Messages.getString("SettingTools.ClearFailed") + " id:" + targetId +" , " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				}
			}
		}
		
		if (!usrFilterIdsMap.isEmpty()){
			for (Entry<String, List<String>> iterUser : usrFilterIdsMap.entrySet()) {
				String userId = iterUser.getKey();
				for(String targetId : iterUser.getValue()){
					try {
						wrapper.deleteUserStatusFilterSetting(targetId, userId);
						log.info(Messages.getString("SettingTools.ClearSucceeded") + " id:" + targetId);
					} catch (Exception e) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " id:" + targetId +" , " + HinemosMessage.replace(e.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
				}
			}
		}
		
		// 処理の終了
		log.info(Messages.getString("SettingTools.ClearCompleted"));
		
		log.debug("End Clear FilterSettingMonitorHistoryStatus");
		return ret;
	}
	
	/**
	 * フィルタ設定の概要情報リスト filterSummaries から、フィルタIDをAPIの呼び出し単位(共通/ユーザ)で仕分けして、
	 * 結果を受け取るコレクション cmnFilterIds, usrFilterIdsMap へセットします。
	 * 
	 */
	private void deleteFilterSettings_divideFilterIds(List<FilterSettingSummaryResponse> filterSummaries,
			List<String> cmnFilterIds, Map<String, List<String>> usrFilterIdsMap) {
		for (FilterSettingSummaryResponse summ : filterSummaries) {
			if (summ.getCommon().booleanValue()) {
				cmnFilterIds.add(summ.getFilterId());
			} else {
				List<String> usrFilterIds = usrFilterIdsMap.get(summ.getOwnerUserId());
				if (usrFilterIds == null) {
					usrFilterIds = new ArrayList<>();
					usrFilterIdsMap.put(summ.getOwnerUserId(), usrFilterIds);
				}
				usrFilterIds.add(summ.getFilterId());
			}
		}
	}
	
	/**
	 * 差分比較処理を行います。
	 * XMLファイル２つ（filePath1,filePath2）を比較する。
	 * 差分がある場合：差分をＣＳＶファイルで出力する。
	 * 差分がない場合：出力しない。
	 * または、すでに存在している同一名のＣＳＶファイルを削除する。
	 *
	 * @param xmlFile1 XMLファイル名
	 * @param xmlFile2 XMLファイル名
	 * @return 終了コード
	 * @throws ConvertorException
	 */
	@DiffMethod
	public int diffXml(String xmlFile1, String xmlFile2) throws ConvertorException {

		log.debug("Start Differrence FilterSettingMonitorHistoryStatus ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		FiltersettingMonitorHistoryStatuses xml1 = null;
		FiltersettingMonitorHistoryStatuses xml2 = null;

		// XMLファイルからの読み込み
		try {
			xml1 = XmlMarshallUtil.unmarshall(FiltersettingMonitorHistoryStatuses.class,new InputStreamReader(new FileInputStream(xmlFile1), "UTF-8"));
			xml2 = XmlMarshallUtil.unmarshall(FiltersettingMonitorHistoryStatuses.class,new InputStreamReader(new FileInputStream(xmlFile2), "UTF-8"));
			sort(xml1);
			sort(xml2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence FilterSettingMonitorHistoryStatus (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(xml1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(xml2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(xml1, xml2, FiltersettingMonitorHistoryStatuses.class, resultA);
			assert resultA.getResultBs().size() == 1;
			
			if (diff) {
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
						log.warn(String.format("file can not be deleted. file name=%s", f.getName()));
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

		getLogger().debug("End Differrence FilterSettingMonitorHistoryStatus");

		return ret;
	}
//
	private void sort(FiltersettingMonitorHistoryStatuses data) {
		FilterMonitorHistoryStatus[] infoList = data.getFilterMonitorHistoryStatus();
		Arrays.sort(
			infoList,
			new Comparator<FilterMonitorHistoryStatus>() {
				@Override
				public int compare(
						FilterMonitorHistoryStatus info1,
						FilterMonitorHistoryStatus info2) {
					int ret = info1.getFiltersetting().getOwnerUserId()
							.compareTo(info2.getFiltersetting().getOwnerUserId());
					if(ret != 0){
						return ret;
					} else {
						return info1.getFiltersetting().getFilterId()
								.compareTo(info2.getFiltersetting().getFilterId());
					}
				}
			});
		 data.setFilterMonitorHistoryStatus(infoList);
	}
	
	public Logger getLogger() {
		return log;
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
