/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.nodemap.action;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.ImportNodeMapModelRecordRequest;
import org.openapitools.client.model.ImportNodeMapModelRequest;
import org.openapitools.client.model.ImportNodeMapModelResponse;
import org.openapitools.client.model.NodeMapModelResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;
import org.openapitools.client.model.RegisterNodeMapModelRequest;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NodeMapException;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.nodemap.util.NodeMapRestClientWrapper;
import com.clustercontrol.repository.bean.FacilityIdConstant;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
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
import com.clustercontrol.utility.settings.nodemap.conv.NodeMapConv;
import com.clustercontrol.utility.settings.nodemap.xml.NodeMap;
import com.clustercontrol.utility.settings.nodemap.xml.NodeMapModel;
import com.clustercontrol.utility.settings.nodemap.xml.NodeMapType;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.AccountUtil;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.ImportClientController;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;
import com.clustercontrol.utility.util.XmlMarshallUtil;

/**
 * 
 * @param action 動作
 * @param XMLファイルパス（ユーザ情報定義の入力元）
 * 
 * @version 6.1.0
 * @since 6.0.0
 * 
 */
public class NodeMapAction {

	/* ロガー */
	protected static Logger log = Logger.getLogger(NodeMapAction.class);

	public NodeMapAction() throws ConvertorException {
		super();
	}
	
	private List<FacilityInfoResponse> getScopeList() throws RestConnectFailed{
		List<FacilityInfoResponse>  scopeList = new ArrayList<>();
		if (AccountUtil.isAdministrator(UtilityManagerUtil.getCurrentManagerName())) {
			String parentScopeId = "";
			scopeList = getScopeList(parentScopeId);
			// ROOT付与
			FacilityInfoResponse facilityInfo = new FacilityInfoResponse();
			facilityInfo.setFacilityType(FacilityInfoResponse.FacilityTypeEnum.SCOPE);
			facilityInfo.setFacilityId(FacilityIdConstant.ROOT);
			
			scopeList.add(0, facilityInfo);
		} else {
			try {
				FacilityTreeItemResponse facilityTreeItem = RepositoryRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName())
						.getFacilityTree(null);
				scopeList = createScopeListForUser(facilityTreeItem);
			} catch (HinemosUnknown | InvalidRole | InvalidUserPass e) {
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			}
		}
		return scopeList;
	}
	
	private List<FacilityInfoResponse> getScopeList(String parentScopeId){
		List<FacilityInfoResponse> scopeListAll = new ArrayList<FacilityInfoResponse>();
		List<FacilityInfoResponse> scopeList = null;
		try {
			scopeList =
					RepositoryRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getFacilityList(parentScopeId);
			for (FacilityInfoResponse info:scopeList){
				if (info.getFacilityType() == FacilityInfoResponse.FacilityTypeEnum.SCOPE){
					scopeListAll.add(info);
					scopeListAll.addAll(getScopeList(info.getFacilityId()));
				}
			}
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass e) {
			log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
		}
		
		return scopeListAll;
	}

	private List<FacilityInfoResponse> createScopeListForUser(FacilityTreeItemResponse treeItem) {

		List<FacilityInfoResponse> scopeList = new ArrayList<>();
		FacilityInfoResponse scopeInfo_ws = treeItem.getData();
		
		if (scopeInfo_ws.getFacilityType() == FacilityInfoResponse.FacilityTypeEnum.SCOPE
				|| scopeInfo_ws.getFacilityType() == FacilityInfoResponse.FacilityTypeEnum.COMPOSITE) {
			scopeList.add(scopeInfo_ws);
			
			// 子供を探索
			for (FacilityTreeItemResponse childItem : treeItem.getChildren()) {
				scopeList.addAll(createScopeListForUser(childItem));
			}
		}
		return scopeList;
	}

	/**
	 * 項目定義情報を全て削除します。<BR>
	 * 
	 * @since 1.0
	 * @return 終了コード
	 * @throws Exception
	 */
	@ClearMethod
	public int clearNodeMap() throws Exception{
		int ret = 0;
		return ret;
	}

	/**
	 * 収集項目定義情報をマネージャから読み出します。
	 * @return
	 * @throws Exception
	 */
	@ExportMethod
	public int exportNodeMap(String xlmFile) throws Exception{
		log.debug("Start Import exportNodeMap :" + xlmFile);
		int ret = SettingConstants.SUCCESS;
		
		List<FacilityInfoResponse>  scopeList = getScopeList();
		List<NodeMapModelResponse> nodeMapList = new ArrayList<>();
		
		NodeMapRestClientWrapper wrapperNodeMap = 
				NodeMapRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		for (FacilityInfoResponse facilityInfo : scopeList) {
			try {
				NodeMapModelResponse nodeMapModel 
						= wrapperNodeMap.getNodeMapModel(facilityInfo.getFacilityId());
				nodeMapList.add(nodeMapModel);
			} catch (HinemosUnknown 
					| InvalidRole 
					| InvalidUserPass | NodeMapException e) {
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + facilityInfo.getFacilityId()
						+ ", " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		//XML作成
		NodeMap nodemapList = new NodeMap();
		for (NodeMapModelResponse nodeMapModelWs: nodeMapList) {
			try{
				NodeMapModel nodeMapModel = NodeMapConv.dto2Xml(nodeMapModelWs);
				nodemapList.addNodeMapModel(nodeMapModel);
				
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + nodeMapModelWs.getMapId());
			
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		// XMLファイルに出力
		try {
			nodemapList.setCommon(NodeMapConv.versionNodeMapDto2Xml(Config.getVersion()));
			// スキーマ情報のセット
			nodemapList.setSchemaInfo(NodeMapConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xlmFile);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				nodemapList.marshal(osw);
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
		log.debug("End Export NodeMap ");
		return ret;
	}

	/**
	 * 項目定義情報をマネージャに投入します。
	 * 
	 * @return
	 */
	@ImportMethod
	public int importNodeMap(String xmlFile){
		
		log.debug("Start Import importNodeMap :" + xmlFile);
		
		int ret=0;
		
		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			log.debug("End Import importNodeMap (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		// XMLファイルからの読み込み
		
		NodeMapType nodeMap = null;
		
		try {
			nodeMap =  XmlMarshallUtil.unmarshall(NodeMapType.class,new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import NodeMap (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(nodeMap.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		NodeMapModel[] nodeMapModels = nodeMap.getNodeMapModel();
		
		// 重複確認無しのため、ImportRecordConfirmerは利用しない
		List<ImportNodeMapModelRecordRequest> dtoRecList = new ArrayList<ImportNodeMapModelRecordRequest>();
		for(NodeMapModel nodeMapModel:nodeMapModels){
			RegisterNodeMapModelRequest nodeMapModelDto =  NodeMapConv.xml2Dto(nodeMapModel);
			ImportNodeMapModelRecordRequest dtoRec = new ImportNodeMapModelRecordRequest();
			dtoRec.setImportData(nodeMapModelDto);
			dtoRec.setIsNewRecord(true);
			dtoRec.setImportKeyValue(dtoRec.getImportData().getMapId());
			dtoRecList.add(dtoRec);
		}

		// レコードの登録（ノードマップ）
		if (!(dtoRecList.isEmpty())) {
			ImportNodeMapModelClientController nodeMapModelController = new ImportNodeMapModelClientController(log,
					Messages.getString("nodemap.setting"), dtoRecList, true);
			int nodeMapModelControllerRet = nodeMapModelController.importExecute();
			if (nodeMapModelControllerRet != 0) {
				ret = nodeMapModelControllerRet;
			}
		}
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Import NodeMap ");
		
		return ret;
		
	}

	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.nodemap.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = NodeMapConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.nodemap.xml.SchemaInfo sci = NodeMapConv.getSchemaVersion();
		
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	@DiffMethod
	public int diffXml(String xmlFile1, String xmlFile2) throws ConvertorException {
		log.debug("Start Differrence Nodemap ");
		int ret =0;
		
		// XMLファイルからの読み込み
		NodeMapType nodeMapType1 = null;
		NodeMapType nodeMapType2 = null;
		
		try {
			nodeMapType1 = XmlMarshallUtil.unmarshall(NodeMapType.class,new InputStreamReader(new FileInputStream(xmlFile1), "UTF-8"));
			nodeMapType2 = XmlMarshallUtil.unmarshall(NodeMapType.class,new InputStreamReader(new FileInputStream(xmlFile2), "UTF-8"));
			
			sort(nodeMapType1);
			sort(nodeMapType2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence NodeMap (Error)");
			return ret;
		}
		
		/*スキーマのバージョンチェック*/
		// PgImage
		if(!checkSchemaVersion(nodeMapType1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(nodeMapType2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			// PgImage
			boolean diff = DiffUtil.diffCheck2(nodeMapType1, nodeMapType2, NodeMapType.class, resultA);
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
		
		log.debug("End Differrence Nodemap");
		
		return ret;
	}
	
	private void sort(NodeMapType nodeMapType) {
		NodeMapModel[] infoList = nodeMapType.getNodeMapModel();
		Arrays.sort(infoList,
				new Comparator<NodeMapModel>() {
					@Override
					public int compare(NodeMapModel info1, NodeMapModel info2) {
						return info1.getMapId().compareTo(info2.getMapId());
					}
				});
		nodeMapType.setNodeMapModel(infoList);
	}
	
	public Logger getLogger() {
		return log;
	}
	
	/**
	 * ノードマップ インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportNodeMapModelClientController extends ImportClientController<ImportNodeMapModelRecordRequest, ImportNodeMapModelResponse, RecordRegistrationResponse>{
		
		public ImportNodeMapModelClientController(Logger logger, String importInfoName, List<ImportNodeMapModelRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportNodeMapModelResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportNodeMapModelResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportNodeMapModelRecordRequest importRec) {
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
		protected ImportNodeMapModelResponse callImportWrapper(List<ImportNodeMapModelRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportNodeMapModelRequest reqDto = new ImportNodeMapModelRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importNodeMapModel(reqDto);
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
