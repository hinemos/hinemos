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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.openapitools.client.model.AddRpaScenarioTagRequest;
import org.openapitools.client.model.ImportRpaScenarioTagRecordRequest;
import org.openapitools.client.model.ImportRpaScenarioTagRequest;
import org.openapitools.client.model.ImportRpaScenarioTagResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;
import org.openapitools.client.model.RpaScenarioTagResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.RpaScenarioTagNotFound;
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
import com.clustercontrol.utility.settings.rpa.conv.RpaScenarioTagConv;
import com.clustercontrol.utility.settings.rpa.xml.RpaScenarioTag;
import com.clustercontrol.utility.settings.rpa.xml.RpaScenarioTags;
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
public class RpaScenarioTagAction {

	/* ロガー */
	protected static Logger log = Logger.getLogger(RpaScenarioTagAction.class);

	public RpaScenarioTagAction() throws ConvertorException {
		super();
	}
	
	/**
	 * RPAシナリオタグ定義情報を全て削除します。<BR>
	 * 
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearRpaScenarioTag(){
		
		log.debug("Start Clear RPA Scenario Tag");
		int ret = 0;

		List<RpaScenarioTagResponse> scenarioTagList = null;
		
		RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());

		try {
			scenarioTagList = wrapper.getRpaScenarioTagList(null);
		} catch ( RestConnectFailed |HinemosUnknown	| InvalidRole | InvalidUserPass | RpaScenarioTagNotFound e) {
			log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		List<String> deleteTagIdList = new ArrayList<String>();
		for (RpaScenarioTagResponse scenarioTag : scenarioTagList){
			deleteTagIdList.add(scenarioTag.getTagId());
		}
		
		if (!deleteTagIdList.isEmpty()){
			for(String targetId : deleteTagIdList){
				try {
					wrapper.deleteRpaScenarioTag(targetId);
					log.info(Messages.getString("SettingTools.ClearSucceeded") + " id:" + targetId);
				} catch ( RestConnectFailed |HinemosUnknown	| InvalidRole | InvalidUserPass | RpaScenarioTagNotFound e) {
					log.error(Messages.getString("SettingTools.ClearFailed")  + " id:" + targetId + " , " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				}
			}
		}
		
		// 処理の終了
		log.info(Messages.getString("SettingTools.ClearCompleted"));
		
		log.debug("End Clear RPA Scenario Tag");
		return ret;
	}
	
	/**
	 *	定義情報をマネージャから読み出します。
	 * @return
	 */
	@ExportMethod
	public int exportRpaScenarioTag(String xmlFile) {

		log.debug("Start Export RPA Scenario Tag");

		int ret = 0;
		RpaRestClientWrapper wrapper =
				RpaRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		
		List<RpaScenarioTagResponse> scenarioTagList =null;
		
		try {
			scenarioTagList = wrapper.getRpaScenarioTagList(null);
			Collections.sort(
					scenarioTagList,
					new Comparator<RpaScenarioTagResponse>() {
						@Override
						public int compare(RpaScenarioTagResponse tagInfo1, RpaScenarioTagResponse tagInfo2) {
							return tagInfo1.getTagId().compareTo(tagInfo2.getTagId());
						}
					});
		} catch ( RestConnectFailed |HinemosUnknown	| InvalidRole | InvalidUserPass | RpaScenarioTagNotFound e) {
			log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		RpaScenarioTags scenarioTags = new RpaScenarioTags();
		RpaScenarioTag scenarioTag = new RpaScenarioTag();
		
		for (RpaScenarioTagResponse info : scenarioTagList) {
			try{
				scenarioTag = RpaScenarioTagConv.getScenarioTag(info);
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + info.getTagId());
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
			
			scenarioTags.addRpaScenarioTag(scenarioTag);
		}
		
		// XMLファイルに出力
		try {
			scenarioTags.setCommon(RpaScenarioTagConv.versionRpaDto2Xml(Config.getVersion()));
			// スキーマ情報のセット
			scenarioTags.setSchemaInfo(RpaScenarioTagConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				scenarioTags.marshal(osw);
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
		log.debug("End Export RPA Scenario Tag");
		return ret;
	}
	
	/**
	 * 定義情報をマネージャに投入します。
	 * 
	 * @return
	 */
	@ImportMethod
	public int importRpaScenarioTag(String xmlFile) 
			throws ConvertorException, InvalidRole, InvalidUserPass, RpaScenarioTagNotFound, 
			InvalidSetting, HinemosUnknown, ParseException, RestConnectFailed {
		log.debug("Start Import RPA Scenario Tag");

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			getLogger().debug("End Import RPA Scenario Tag (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		int ret = 0;
		
		// XMLファイルからの読み込み
		RpaScenarioTags scenarioTags = null;
		try {
			scenarioTags = XmlMarshallUtil.unmarshall(RpaScenarioTags.class,new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import RPA Scenario Tag (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(scenarioTags.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		RpaRestClientWrapper wrapper =
				RpaRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		
		List<RpaScenarioTagResponse> managerTagList =null;
		
		try {
			managerTagList = wrapper.getRpaScenarioTagList(null);
		} catch ( RestConnectFailed |HinemosUnknown	| InvalidRole | InvalidUserPass | RpaScenarioTagNotFound e) {
			log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		// castor の 情報を DTO に変換。
		List<RpaScenarioTagResponse> scenarioTagList = null;
		try {
			scenarioTagList = createRpaScenarioTagList(scenarioTags, managerTagList);
			
			// ScenarioTagInfo をマネージャに登録。
			ret = importRpaScenarioTagList(scenarioTagList, managerTagList);
		} catch (Exception e) {
			if (e instanceof ConvertorException) {
				getLogger().warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			} else {
				getLogger().warn(Messages.getString("SettingTools.ImportFailed"), e);
			}
			// DTO変換中に例外が発生した場合、RpaScenarioTagList は初期化状態のまま(件数0)なので、インポートを中断
			return SettingConstants.ERROR_INPROCESS;
		}

		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		checkDelete(scenarioTagList, scenarioTags);
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Import RPA Scenario ");
		
		return ret;
	}
	
	public List<RpaScenarioTagResponse> createRpaScenarioTagList(RpaScenarioTags scenarioTags, List<RpaScenarioTagResponse> managerTagList) 
			throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, 
			RpaScenarioTagNotFound, InvalidSetting, ParseException {
		return RpaScenarioTagConv.createRpaScenarioTagList(scenarioTags, managerTagList);
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
		int res = RpaScenarioTagConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.rpa.xml.SchemaInfo sci = RpaScenarioTagConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	protected void checkDelete(List<RpaScenarioTagResponse> xmlElements, RpaScenarioTags scenarioTags){

		List<RpaScenarioTagResponse> subList = null;
		try {
			subList = getFilterdRpaScenarioTagList();
		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}

		if(subList == null || subList.size() <= 0){
			return;
		}
		
		// XML上のタグ同士の関連性のマップ
		Map<String, List<String>> xmlParentTagIdMap = new HashMap<>();
		for (RpaScenarioTag tag : scenarioTags.getRpaScenarioTag()){
			if(tag.getRpaScenarioTagInfo().getTagPath() == null){
				tag.getRpaScenarioTagInfo().setTagPath("");
			}
			
			// 直近の親タグを取得
			String parentId = RpaScenarioTagConv.getParentTagId(tag.getRpaScenarioTagInfo().getTagPath());
			
			if(!parentId.isEmpty()){
				if (xmlParentTagIdMap.get(parentId) == null){
					// 直近の親タグがマップに存在しない場合は親タグをキーにしてentryに保持
					xmlParentTagIdMap.put(parentId, new ArrayList<String>());
					xmlParentTagIdMap.get(parentId).add(tag.getRpaScenarioTagInfo().getTagId());
				} else {
					// 直近の親タグがマップに存在する場合は親タグがキーであるentryに保持
					xmlParentTagIdMap.get(parentId).add(tag.getRpaScenarioTagInfo().getTagId());
				}
			}
		}
		
		// DB上のタグ同士の関連性のマップ
		Map<String, List<String>> managerParentTagIdMap = new HashMap<>();
		for (RpaScenarioTagResponse tag : subList){
			if(tag.getTagPath() == null){
				tag.setTagPath("");
			}
			
			// 直近の親タグを取得
			String parentId = RpaScenarioTagConv.getParentTagId(tag.getTagPath());
			
			if(!parentId.isEmpty()){
				if (managerParentTagIdMap.get(parentId) == null){
					// 直近の親タグがマップに存在しない場合は親タグをキーにしてentryに保持
					managerParentTagIdMap.put(parentId, new ArrayList<String>());
					managerParentTagIdMap.get(parentId).add(tag.getTagId());
				} else {
					// 直近の親タグがマップに存在する場合は親タグがキーであるentryに保持
					managerParentTagIdMap.get(parentId).add(tag.getTagId());
				}
			}
		}
		
		// DB上のタグの階層タグのマップ
		Map<String,List<String>> tagPathMap = new HashMap<>();
		List<String> tagPathList = new ArrayList<String>();
		for (RpaScenarioTagResponse tag : subList) {
			tagPathList = Arrays.asList(tag.getTagPath().split("\\\\"));
			tagPathMap.put(tag.getTagId(), tagPathList);
		}

		for(RpaScenarioTagResponse mgrInfo: new ArrayList<>(subList)){
			for(RpaScenarioTagResponse xmlElement: new ArrayList<>(xmlElements)){
				if(mgrInfo.getTagId().equals(xmlElement.getTagId())){
					subList.remove(mgrInfo);
					xmlElements.remove(xmlElement);
					break;
				}
			}
		}

		if(subList.size() > 0){
			List<String> deleteTagIdList = new ArrayList<String>();
			List<String> deleteChildTagIdList = new ArrayList<String>();
			
			for(RpaScenarioTagResponse info: subList){
				
				// 既に削除対象（子タグ）として登録されている場合はスキップする
				if (deleteTagIdList.contains(info.getTagId())){
					continue;
				}
				
				// XML上で他から参照されている場合はスキップする
				if(xmlParentTagIdMap.get(info.getTagId()) != null){
					continue;
				} else {
					List<String> childTagList = managerParentTagIdMap.get(info.getTagId());
					if(childTagList != null &&
							childTagList.stream().filter(t -> xmlParentTagIdMap.get(t) != null).findFirst().isPresent()){
						continue;
					}
				}
				
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getTagId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args)
							+ Messages.getString("SettingTools.RpaScenarioTag.message.delete.confirm"));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				// タグが親タグの場合、子タグを保持する
				deleteChildTagIdList.clear();
				for (Map.Entry<String, List<String>> tagEntry : tagPathMap.entrySet()) {
					if (tagEntry.getValue().contains(info.getTagId())){
						deleteChildTagIdList.add(tagEntry.getKey());
					}
				}

				// タグ同士の関連性がある為、削除対象のIDを保持しておき最後に一斉削除する
				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					
					// 子タグが存在しない場合
					if (deleteChildTagIdList.isEmpty() && !deleteTagIdList.contains(info.getTagId())){
						deleteTagIdList.add(info.getTagId());
						
						continue;
					} else if(deleteChildTagIdList.isEmpty()){
						continue;
					}
					
					// 子タグが存在する場合
					if (!deleteTagIdList.contains(info.getTagId())) {
						deleteTagIdList.add(info.getTagId());
						for (String childTagId : deleteChildTagIdList){
							if(!deleteTagIdList.contains(childTagId)){
								deleteTagIdList.add(childTagId);
							}
						}
					}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getTagId());
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
					return;
				}
			}
			
			if (deleteTagIdList.isEmpty()){
				return;
			}
			
			try {
				RpaRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteRpaScenarioTag(String.join(",", deleteTagIdList));
				getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + String.join(",", deleteTagIdList));
			} catch (Exception e1) {
				getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
			}
		}
	}
	
	/**
	 *  RpaScenarioTag をインポートする。
	 *  
	 */
	protected int importRpaScenarioTagList(List<RpaScenarioTagResponse> rpaScenarioList, List<RpaScenarioTagResponse> managerTagList) 
			throws HinemosUnknown,InvalidRole, InvalidUserPass, RpaScenarioTagNotFound, RestConnectFailed, IndexOutOfBoundsException, ConvertorException, ParseException{
		int returnValue =0;
		ImportRecordConfirmer<RpaScenarioTagResponse, ImportRpaScenarioTagRecordRequest, String> confirmer =
				new ImportRecordConfirmer<RpaScenarioTagResponse, ImportRpaScenarioTagRecordRequest, String>(
				getLogger(), rpaScenarioList.toArray(new RpaScenarioTagResponse[rpaScenarioList.size()])) {
			@Override
			protected ImportRpaScenarioTagRecordRequest convertDtoXmlToRestReq(RpaScenarioTagResponse xmlDto)
					throws HinemosUnknown, InvalidSetting {
				ImportRpaScenarioTagRecordRequest dtoRec = new ImportRpaScenarioTagRecordRequest();
				dtoRec.setImportData(new AddRpaScenarioTagRequest());
				RestClientBeanUtil.convertBean(xmlDto, dtoRec.getImportData());
				dtoRec.setImportKeyValue(dtoRec.getImportData().getTagId());
				return dtoRec;
			}
			@Override
			protected Set<String> getExistIdSet() throws Exception {
				Set<String> retSet = new HashSet<String>();
				for(RpaScenarioTagResponse rec :getFilterdRpaScenarioTagList()){
					retSet.add(rec.getTagId());
				}
				return retSet;
			}
			@Override
			protected boolean isLackRestReq(ImportRpaScenarioTagRecordRequest restDto) {
				return false;
			}
			@Override
			protected String getKeyValueXmlDto(RpaScenarioTagResponse xmlDto) {
				return xmlDto.getTagId();
			}
			@Override
			protected String getId(RpaScenarioTagResponse xmlDto) {
				return xmlDto.getTagId();
			}
			@Override
			protected void setNewRecordFlg(ImportRpaScenarioTagRecordRequest restDto, boolean flag) {
				restDto.setIsNewRecord(flag);
			}
		};
		int confirmRet = confirmer.executeConfirm();
		if( confirmRet != SettingConstants.SUCCESS && confirmRet != SettingConstants.ERROR_CANCEL ){
			//変換エラーならUnmarshalXml扱いで処理打ち切り(キャンセルはキャンセル以前の選択結果を反映するので次に進む)
			getLogger().warn(Messages.getString("SettingTools.UnmarshalXmlFailed"));
			return confirmRet;
		}
		
		// XMLのタグ階層チェックを行う（スキップされたデータを除いたデータでのみ行う）
		List<ImportRpaScenarioTagRecordRequest> importRecDtoList = confirmer.getImportRecDtoList();
		List<RpaScenarioTag> xmlTagList = new ArrayList<>();
		for( ImportRpaScenarioTagRecordRequest importRecDto : importRecDtoList){
			RpaScenarioTagResponse xmlDto = new RpaScenarioTagResponse();
			RestClientBeanUtil.convertBean(importRecDto.getImportData(), xmlDto);
			xmlTagList.add(RpaScenarioTagConv.getScenarioTag(xmlDto));
		}
		for(RpaScenarioTag tag : xmlTagList){
			RpaScenarioTagConv.checkTagPath(tag, xmlTagList, managerTagList);
		}
		
		// 更新後のタグ階層でより上位の親になっているタグから更新する為、タグ階層でソート
		Collections.sort(confirmer.getImportRecDtoList(), new Comparator<ImportRpaScenarioTagRecordRequest>() {
			@Override
			public int compare(ImportRpaScenarioTagRecordRequest first, ImportRpaScenarioTagRecordRequest second) {
				
				return first.getImportData().getTagPath().compareTo(second.getImportData().getTagPath());
			}
		});

		ImportClientController<ImportRpaScenarioTagRecordRequest, ImportRpaScenarioTagResponse, RecordRegistrationResponse> importController =
				new ImportClientController<ImportRpaScenarioTagRecordRequest, ImportRpaScenarioTagResponse, RecordRegistrationResponse>(
				getLogger(), Messages.getString("rpa.scenario.tag"), confirmer.getImportRecDtoList(),true) {
			@Override
			protected List<RecordRegistrationResponse> getResRecList(ImportRpaScenarioTagResponse importResponse) {
				return importResponse.getResultList();
			};
			@Override
			protected Boolean getOccurException(ImportRpaScenarioTagResponse importResponse) {
				return importResponse.getIsOccurException();
			};
			@Override
			protected String getReqKeyValue(ImportRpaScenarioTagRecordRequest importRec) {
				return importRec.getImportData().getTagId();
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
			protected ImportRpaScenarioTagResponse callImportWrapper(List<ImportRpaScenarioTagRecordRequest> importRecList)
					throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
				ImportRpaScenarioTagRequest reqDto = new ImportRpaScenarioTagRequest();
				reqDto.setRecordList(importRecList);
				reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
				return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importRpaScenarioTag(reqDto);
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
		
		return returnValue;
	}
	
	protected List<RpaScenarioTagResponse> getFilterdRpaScenarioTagList() 
			throws HinemosUnknown, InvalidRole, InvalidUserPass, RpaScenarioTagNotFound, RestConnectFailed {
		List<RpaScenarioTagResponse> scenarioTagList = null;
		
		scenarioTagList =
				RpaRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getRpaScenarioTagList(null);
		
		return scenarioTagList;
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
		log.debug("Start Differrence RPA Scenario Tag ");

		int ret = 0;
		// XMLファイルからの読み込み
		RpaScenarioTags tags1 = null;
		RpaScenarioTags tags2 = null;
		try {
			tags1 = XmlMarshallUtil.unmarshall(RpaScenarioTags.class,new InputStreamReader(new FileInputStream(xmlFile1), "UTF-8"));
			tags2 = XmlMarshallUtil.unmarshall(RpaScenarioTags.class,new InputStreamReader(new FileInputStream(xmlFile2), "UTF-8"));
			sort(tags1);
			sort(tags2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence Report Template (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(tags1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		if(!checkSchemaVersion(tags2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(tags1, tags2, RpaScenarioTags.class, resultA);
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
		getLogger().debug("End Differrence RPA Scenario Tag");

		return ret;
	}
	
	private void sort(RpaScenarioTags tags) {
		RpaScenarioTag[] infoList = tags.getRpaScenarioTag();
		Arrays.sort(infoList,
			new Comparator<RpaScenarioTag>() {
				@Override
				public int compare(RpaScenarioTag info1, RpaScenarioTag info2) {
					return info1.getRpaScenarioTagInfo().getTagId().compareTo(info2.getRpaScenarioTagInfo().getTagId());
				}
			});
		tags.setRpaScenarioTag(infoList);
	}
	
	public Logger getLogger() {
		return log;
	}
}
