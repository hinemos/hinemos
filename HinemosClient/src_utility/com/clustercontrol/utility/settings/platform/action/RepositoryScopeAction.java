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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
import org.openapitools.client.model.AddScopeRequest;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.ImportScopeRecordRequest;
import org.openapitools.client.model.ImportScopeRequest;
import org.openapitools.client.model.ImportScopeResponse;
import org.openapitools.client.model.NodeInfoResponseP1;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.ScopeInfoRequest;
import org.openapitools.client.model.ScopeInfoResponseP1;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.constant.HinemosModuleConstant;
import com.clustercontrol.utility.difference.CSVUtil;
import com.clustercontrol.utility.difference.DiffAnnotation;
import com.clustercontrol.utility.difference.DiffUtil;
import com.clustercontrol.utility.difference.ResultA;
import com.clustercontrol.utility.settings.ClearMethod;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.DiffMethod;
import com.clustercontrol.utility.settings.ExportMethod;
import com.clustercontrol.utility.settings.ImportMethod;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.conv.RepositoryConv;
import com.clustercontrol.utility.settings.platform.conv.RepositoryScopeConv;
import com.clustercontrol.utility.settings.platform.xml.RepositoryScope;
import com.clustercontrol.utility.settings.platform.xml.RepositoryScopeNode;
import com.clustercontrol.utility.settings.platform.xml.ScopeInfo;
import com.clustercontrol.utility.settings.platform.xml.ScopeNodeInfo;
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
 * リポジトリ-スコープ-情報をインポート・エクスポート・削除するアクションクラス<br>
 *
 * @version 6.1.0
 * @since 1.0.0
 * 
 */
public class RepositoryScopeAction {
	protected static Logger logger = Logger.getLogger(RepositoryScopeAction.class);

	/**
	 * コンストラクター <BR>
	 * @throws ConvertorException
	 */
	public RepositoryScopeAction() throws ConvertorException {
		super();
	}

	/**
	 * 情報をマネージャから削除します。<BR>
	 *
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearRepositoryScope() {
		logger.debug("Start Clear ScopeInfo ");
		int resultNumber = SettingConstants.SUCCESS;
		RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());

		if (AccountUtil.isAdministrator(UtilityManagerUtil.getCurrentManagerName())) {
			// ADMINISTRATORS権限を持つ場合
			// スコープ情報、スコープ内ノード情報の取得
			List<FacilityInfoResponse> scopeList;
			try {
				scopeList =wrapper.getFacilityList("");
			}
			catch (Exception e) {
				handleDTOException(e, Messages.getString("SettingTools.FailToGetList"), HinemosMessage.replace(e.getMessage()));
				return SettingConstants.ERROR_INPROCESS;
			}

			// スコープの削除
			List<String> idList = new ArrayList<>();
			for (FacilityInfoResponse scope : scopeList) {
				// 起点となるスコープのみ削除
				if (!RepositoryConv.checkInternalScope(scope.getFacilityId())) {
					idList.add(scope.getFacilityId());
				}
			}

			try {
				wrapper.deleteScope(String.join(",", idList));
				logger.info(Messages.getString("SettingTools.ClearSucceeded") + "(Scope) : " + idList.toString());
			} catch (Exception e) {
				logger.warn(Messages.getString("SettingTools.ClearFailed") + "(Scope) : " + HinemosMessage.replace(e.getMessage()));
				resultNumber = SettingConstants.ERROR_INPROCESS;
			}
		} else {
			// ADMINISTRATORS権限を持たない場合
			// スコープ情報、スコープ内ノード情報の取得
			List<String> idList = null;
			try {
				FacilityTreeItemResponse facilityTreeItem = wrapper.getFacilityTree(null);
				idList = createScopeList(facilityTreeItem);
			} catch (Exception e) {
				handleDTOException(e, Messages.getString("SettingTools.FailToGetList"), HinemosMessage.replace(e.getMessage()));
				return SettingConstants.ERROR_INPROCESS;
			}

			for (String id : idList) {
				try {
					wrapper.deleteScope(String.join(",", id));
					logger.info(Messages.getString("SettingTools.ClearSucceeded") + "(Scope) : " + id);
				} catch (InvalidRole | InvalidUserPass | UsedFacility e) {
					logger.warn(Messages.getString("SettingTools.ClearFailed") + "(Scope) : " + id + ", " + HinemosMessage.replace(e.getMessage()));
					resultNumber = SettingConstants.ERROR_INPROCESS;
				} catch (RestConnectFailed e) {
					logger.error(Messages.getString("SettingTools.ClearFailed") + "(Scope) : " + id + ", " + HinemosMessage.replace(e.getMessage()));
					resultNumber = SettingConstants.ERROR_INPROCESS;
				} catch (Exception e) {
					// 所属元のスコープが既に削除されている場合も通るため、ここではトレースログだけ出力する
					logger.trace(Messages.getString("SettingTools.ClearFailed") + "(Scope) : " + id + ", " + HinemosMessage.replace(e.getMessage()));
				}
			}
		}

		if (resultNumber == SettingConstants.SUCCESS) {
			logger.info(Messages.getString("SettingTools.ClearCompleted") + "(Scope)");
		}

		logger.debug("End Clear PlatformRepositoryScope ");
		return resultNumber;
	}

	/**
	 * 指定されたスコープと、その配下にあるスコープのファシリティIDリストを返す
	 * 
	 * ただし、ファシリティIDリストからはシステムスコープを除く。
	 * 
	 * @param treeItem ファシリティツリー
	 * @return ツリー配下のスコープのファシリティID
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 */
	private List<String> createScopeList(FacilityTreeItemResponse treeItem) throws HinemosUnknown, InvalidRole, InvalidUserPass {

		List<String> rtnList = new ArrayList<>();

		FacilityInfoResponse scopeInfo_ws = treeItem.getData();

		if (scopeInfo_ws.getFacilityType() == FacilityInfoResponse.FacilityTypeEnum.SCOPE
				|| scopeInfo_ws.getFacilityType() ==  FacilityInfoResponse.FacilityTypeEnum.COMPOSITE) {
			if (RepositoryConv.checkInternalScope(scopeInfo_ws.getFacilityId())) {
				return rtnList;
			}
			if (scopeInfo_ws.getFacilityType() ==  FacilityInfoResponse.FacilityTypeEnum.SCOPE) {
				rtnList.add(scopeInfo_ws.getFacilityId());
			}

			// 子供を探索
			for (FacilityTreeItemResponse childItem : treeItem.getChildren()) {
				rtnList.addAll(createScopeList(childItem));
			}
		}
		return rtnList;
	}

	/**
	 * XMLの情報をマネージャに投入します。<BR>
	 *
	 * @param CPUInfoA 情報を取り出す XML ファイルパス。
	 * @param ScopeNode 情報を取り出す  XML ファイルパス。
	 * @return 終了コード
	 */
	@ImportMethod
	public int importRepositoryScope(String xmlScope, String xmlScopeNode) {
		logger.debug("Start Import RepositoryScope and RepositoryScopeNode");

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			logger.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			logger.debug("End Import RepositoryScope and RepositoryScopeNode (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		// XMLファイルからの読み込み
		RepositoryScope scope = null;
		RepositoryScopeNode scopeNode = null;
		try {
			scope = XmlMarshallUtil.unmarshall(RepositoryScope.class,new InputStreamReader(new FileInputStream(xmlScope), "UTF-8"));
			scopeNode = XmlMarshallUtil.unmarshall(RepositoryScopeNode.class,new InputStreamReader(new FileInputStream(xmlScopeNode), "UTF-8"));
		}
		catch (Exception e) {
			logger.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			logger.debug("End Import RepositoryScope and RepositoryScopeNode (Error)");
			return SettingConstants.ERROR_INPROCESS;
		}

		// スキーマのバージョンチェック
		if(!(checkSchemaVersionScope(scope.getSchemaInfo()) &&
				checkSchemaVersionScope(scopeNode.getSchemaInfo())
				)){
			return SettingConstants.ERROR_SCHEMA_VERSION;
		}

		//　スコープ情報の登録（スコープ内ノード情報含む）
		List<String> objectIdList = new ArrayList<String>();
		int resultNumber_Scope = SettingConstants.SUCCESS;

		resultNumber_Scope = registScopeInfoAndScopeNode(scope,scopeNode,objectIdList);
		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			logger.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		// 全スコープの登録に成功した場合
		if (resultNumber_Scope == SettingConstants.SUCCESS) {
			logger.info(Messages.getString("SettingTools.ImportCompleted") + "(Scope)");
			logger.info(Messages.getString("SettingTools.ImportCompleted") + "(ScopeNode)");
		}else{
			logger.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		
		//オブジェクト権限同時インポート
		importObjectPrivilege(HinemosModuleConstant.PLATFORM_REPOSITORY, objectIdList);
		
		//差分削除
		checkDelete(scope);
		
		int resultNumber;
		if (resultNumber_Scope == SettingConstants.SUCCESS ) {
			resultNumber = SettingConstants.SUCCESS;
		}
		else {
			resultNumber = SettingConstants.ERROR_INPROCESS;
		}

		logger.debug("End Import RepositoryScope and RepositoryScopeNode");
		return resultNumber;
	}

	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private boolean checkSchemaVersionScope(com.clustercontrol.utility.settings.platform.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = RepositoryConv.checkSchemaVersionScope(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo sci = RepositoryConv.getSchemaVersionScope();
		
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	/**
	 * マネージャへ登録した際にスローされる例外の処理。<BR>
	 *
	 * @param 発生した例外。
	 * @param
	 * @param
	 * @return 処理の継続判定。
	 */
	private static boolean handleDTOException(Exception e, String title, String scopeNodeString) {
		boolean isContinue = true;

		if (e instanceof HinemosUnknown) {
			logger.error(title + scopeNodeString);
			logger.error(Messages.getString("SettingTools.UnexpectedError"));
		}
		else if (e instanceof InvalidRole) {
			logger.error(title + scopeNodeString);
			logger.error(Messages.getString("SettingTools.InvalidRole"));
		}
		else if (e instanceof InvalidUserPass) {
			logger.error(title + scopeNodeString);
			logger.error(Messages.getString("SettingTools.InvalidUserPass"));
		}
		else if (e instanceof InvalidSetting) {
			logger.warn(title + scopeNodeString);
			logger.warn(Messages.getString("SettingTools.InvalidSetting"));
		}
		else if (e instanceof RestConnectFailed) {
			logger.error(title + scopeNodeString);
		}
		else {
			logger.warn(title + scopeNodeString);
		}
		logger.debug(e);

		return isContinue;
	}

	/**
	 * Caster のスコープ情報を List化する<BR>
	 */
	private static void recursiveConvertXmlScopeInfoToList(
			com.clustercontrol.utility.settings.platform.xml.ScopeInfo clildScopeInfo,
			List<com.clustercontrol.utility.settings.platform.xml.ScopeInfo> list) {
		list.add(clildScopeInfo);
		for (com.clustercontrol.utility.settings.platform.xml.ScopeInfo gchildInfo_ca : clildScopeInfo.getScopeInfo()) {
			recursiveConvertXmlScopeInfoToList(gchildInfo_ca, list);
		}
	}
	/**
	 * RestのFacilityTree のスコープID情報を List化する<BR>
	 */
	private static void recursiveConvertDtoScopeInfoToIdList(
			FacilityTreeItemResponse faciltyInfo,
			List<String> list) {
		if (faciltyInfo.getData().getFacilityType() == FacilityInfoResponse.FacilityTypeEnum.SCOPE) {
			list.add(faciltyInfo.getData().getFacilityId());
		}
		for (FacilityTreeItemResponse childInfo : faciltyInfo.getChildren() ) {
			recursiveConvertDtoScopeInfoToIdList(childInfo, list);
		}
	}

	/**
	 * Caster のスコープ情報（スコープ内ノード情報含む）を インポートする<BR>
	 */
	private static int registScopeInfoAndScopeNode( RepositoryScope scope, RepositoryScopeNode scopeNode,List<String> objectIdList){
		int ret = 0;
		
		final HashMap<String, List<String>> mapScopeNode = new HashMap<>();
		for (com.clustercontrol.utility.settings.platform.xml.ScopeNodeInfo scopeNodeInfo : scopeNode.getScopeNodeInfo()) {
			if (!mapScopeNode.containsKey(scopeNodeInfo.getScopeFacilityId())) {
				mapScopeNode.put(scopeNodeInfo.getScopeFacilityId(), new ArrayList<String>());
			}
			mapScopeNode.get(scopeNodeInfo.getScopeFacilityId()).add(scopeNodeInfo.getNodeFacilityId());
		}
		
		// インポート向けデータの存在チェックとREST向けDtoへの変換（既設レコードなら上書き/スキップの確認も行う）
		List<ScopeInfo> importObjectList = new ArrayList<ScopeInfo>();
		for (com.clustercontrol.utility.settings.platform.xml.ScopeInfo childScope : scope.getScopeInfo()) {
			recursiveConvertXmlScopeInfoToList( childScope, importObjectList);
		}

		//スコープのIDの一意チェックも行う
		Set<String> ScopeIdSet = new HashSet<String>();
		for (ScopeInfo scopeRec : importObjectList) {
			if( ScopeIdSet.contains(scopeRec.getFacilityId()) ){
				String[] args = { Messages.getString("word.facility.id"), scopeRec.getFacilityId() };
				logger.warn(Messages.getString("message.repository.32", args));
				logger.warn(Messages.getString("SettingTools.UnmarshalXmlFailed"));
				return SettingConstants.ERROR_INPROCESS;
			}else{
				ScopeIdSet.add(scopeRec.getFacilityId());
			}
		}
		
		com.clustercontrol.utility.settings.platform.xml.ScopeInfo[] importObjectArray = importObjectList.toArray( new com.clustercontrol.utility.settings.platform.xml.ScopeInfo[0]);
		ImportRecordConfirmer<ScopeInfo, ImportScopeRecordRequest, String> confirmer = new ImportRecordConfirmer<ScopeInfo, ImportScopeRecordRequest, String>(
				logger,importObjectArray) {
			@Override
			protected ImportScopeRecordRequest convertDtoXmlToRestReq(ScopeInfo xmlDto)
					throws HinemosUnknown, InvalidSetting {
				ScopeInfoResponseP1 info = RepositoryScopeConv.createScopeInfo_ws(xmlDto);
				ImportScopeRecordRequest dtoRec = new ImportScopeRecordRequest();
				dtoRec.setImportData(new AddScopeRequest());
				dtoRec.getImportData().setScopeInfo(new ScopeInfoRequest());
				RestClientBeanUtil.convertBean(info, dtoRec.getImportData().getScopeInfo());
				dtoRec.getImportData().setParentFacilityId(xmlDto.getParentFacilityId());
				dtoRec.setImportKeyValue( dtoRec.getImportData().getScopeInfo().getFacilityId());
				//スコープ内ノード情報も併せて送信
				dtoRec.setAssignFacilityIdList(mapScopeNode.get(xmlDto.getFacilityId()));
				
				return dtoRec;
			}

			@Override
			protected Set<String> getExistIdSet() throws Exception {
				List<String> idList = new ArrayList<String>();
				FacilityTreeItemResponse ret = RepositoryRestClientWrapper
						.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getFacilityTree(null);
				recursiveConvertDtoScopeInfoToIdList(ret, idList);
				Set<String> idSet = new HashSet<String>();
				for (String id : idList) {
					idSet.add(id);
				}
				return idSet;
			}

			@Override
			protected boolean isLackRestReq(ImportScopeRecordRequest restDto) {
				return (restDto == null || restDto.getImportData().getScopeInfo().getFacilityId() == null || restDto.getImportData().getScopeInfo().getFacilityId().equals(""));
			}

			@Override
			protected String getKeyValueXmlDto(ScopeInfo xmlDto) {
				return xmlDto.getFacilityId();
			}

			@Override
			protected String getId(ScopeInfo xmlDto) {
				return xmlDto.getFacilityId();
			}

			@Override
			protected void setNewRecordFlg(ImportScopeRecordRequest restDto, boolean flag) {
				restDto.setIsNewRecord(flag);
			}
		};
		int confirmRet = confirmer.executeConfirm();
		//変換エラーならUnmarshalXml扱いで処理打ち切り(キャンセルはキャンセル以前の選択結果を反映するので次に進む)
		if( confirmRet != SettingConstants.SUCCESS && confirmRet != SettingConstants.ERROR_CANCEL ){
			logger.warn(Messages.getString("SettingTools.UnmarshalXmlFailed"));
			return confirmRet;
		}
		
		// 更新単位の件数毎にインポートメソッドを呼び出し、結果をログ出力
		// API異常発生時はそこで中断、レコード個別の異常発生時はユーザ選択次第で続行
		ImportClientController<ImportScopeRecordRequest, ImportScopeResponse, RecordRegistrationResponse> importController = new ImportClientController<ImportScopeRecordRequest, ImportScopeResponse, RecordRegistrationResponse>(
				logger, Messages.getString("platform.repository.scope"), confirmer.getImportRecDtoList(),true) {
			@Override
			protected List<RecordRegistrationResponse> getResRecList(ImportScopeResponse importResponse) {
				return importResponse.getResultList();
			};

			@Override
			protected Boolean getOccurException(ImportScopeResponse importResponse) {
				return importResponse.getIsOccurException();
			};

			@Override
			protected String getReqKeyValue(ImportScopeRecordRequest importRec) {
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
			protected ImportScopeResponse callImportWrapper(List<ImportScopeRecordRequest> importRecList)
					throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
				ImportScopeRequest reqDto = new ImportScopeRequest();
				reqDto.setRecordList(importRecList);
				reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
				return UtilityRestClientWrapper
						.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importScope(reqDto);
			}

			@Override
			protected String getRestExceptionMessage(RecordRegistrationResponse responseRec) {
				if (responseRec.getExceptionInfo() != null) {
					return responseRec.getExceptionInfo().getException() +":"+ responseRec.getExceptionInfo().getMessage();
				}
				return null;
			};

		};
		ret = importController.importExecute();
		for (RecordRegistrationResponse rec: importController.getImportSuccessList() ){
			objectIdList.add(rec.getImportKeyValue());
		}
		
		return ret;
	}

	/**
	 * RepositoryEndpointWrapper を利用して、スコープおよびノードのリストを取得する際にいくつか問題が発生 (3/22 現在)。
	 *
	 * 1. RepositoryEndpointWrapper.getScopeList は、配下のノードも含めており、 FacilityType まで FacilityConstant.TYPE_SCOPE (0) に設定している。
	 * マネージャ側のコードを確認したところ、FacilityType を初期化していないので、0 (FacilityConstant.TYPE_SCOPE) となっている。
	 * また、ロジックの本体である RepositoryControllerBean.getScopeList が、@Deprecated になっている。
	 *
	 * 2. RepositoryEndpointWrapper.getFacilityTree では、各 Facility のDescription が抜けている。
	 *
	 * 上記を踏まえて、RepositoryEndpointWrapper.getFacilityTree で階層を確認し、詳細は、RepositoryEndpointWrapper.getScope や
	 * RepositoryEndpointWrapper.getNode で取得する。
	 *
	 * @version 6.0.0
	 * @since 2.0.0
	 * 
	 * 
	 */
	public static class WrappedFacilityTreeItem implements RepositoryScopeConv.IFacilityTreeItem {
		private List<RepositoryScopeConv.IFacilityTreeItem> children;
	    private FacilityInfoResponse data;
	    private FacilityTreeItemResponse facilityTreeItem;
	    private RepositoryScopeConv.IFacilityTreeItem parent;

	    private WrappedFacilityTreeItem(RepositoryScopeConv.IFacilityTreeItem parent, FacilityTreeItemResponse facilityTreeItem) throws ConvertorException {
	    	super();
	    	assert facilityTreeItem != null;

	    	this.parent = parent;
	    	this.facilityTreeItem = facilityTreeItem;
	    }

		@Override
	    public List<RepositoryScopeConv.IFacilityTreeItem> getChildren() throws Exception {
	    	if (children == null) {
	    		children = new ArrayList<RepositoryScopeConv.IFacilityTreeItem>();

	    		for (FacilityTreeItemResponse child: facilityTreeItem.getChildren()) {
	    			children.add(new WrappedFacilityTreeItem(this, child));
	    		}
	    	}

	    	return children;
	    }

		@Override
		public FacilityInfoResponse getData() throws Exception {
			if (data == null) {
				switch (facilityTreeItem.getData().getFacilityType()) {
				case SCOPE:
					ScopeInfoResponseP1 scopeInfo = RepositoryRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getScope(facilityTreeItem.getData().getFacilityId()); 
					data = new FacilityInfoResponse();
					RestClientBeanUtil.convertBean(scopeInfo, data);
					data.setFacilityType( FacilityInfoResponse.FacilityTypeEnum.SCOPE);
					break;
				case NODE:
					NodeInfoResponseP1 nodeInfo = RepositoryRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getNode(facilityTreeItem.getData().getFacilityId());
					data = new FacilityInfoResponse();
					RestClientBeanUtil.convertBean(nodeInfo, data);
					data.setFacilityType( FacilityInfoResponse.FacilityTypeEnum.NODE);
					break;
				default:
					data = facilityTreeItem.getData();
				}
			}

	        return data;
	    }

		@Override
	    public RepositoryScopeConv.IFacilityTreeItem getParent() {
	        return parent;
	    }
	}

	/**
	 * 情報をマネージャから取得し、XMLに出力します。<BR>
	 *
	 * @param CPUInfoA 情報を格納するXML ファイルパス。
	 * @param ScopeNode 情報を格納するXML ファイルパス。
	 * @return 終了コード
	 */
	@ExportMethod
	public int exportRepositoryScope(String xmlScope, String xmlScopeNode) {
		logger.debug("Start Export RepositoryScope and RepositoryScopeNode");

		RepositoryScope rs = null;
		RepositoryScopeNode rsn = null;
		try {
			FacilityTreeItemResponse facilityTreeItem = RepositoryRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getFacilityTree(null);
			WrappedFacilityTreeItem treeItem = new WrappedFacilityTreeItem(null, facilityTreeItem);

			// Caster のデータ構造に変換。
			rs = RepositoryScopeConv.createRepositoryScope(treeItem);

			// Caster のデータ構造に変換。
			rsn = RepositoryScopeConv.createRepositoryScopeNode(treeItem);

		}
		catch (Exception e) {
			logger.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			logger.debug("End Export PlatformRepositoryScope (Error)");
			return SettingConstants.ERROR_INPROCESS;
		}

		// XMLファイルの作成
		try {
			try(FileOutputStream fos = new FileOutputStream(xmlScope);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				rs.marshal(osw);
			}
			try(FileOutputStream fos = new FileOutputStream(xmlScopeNode);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				rsn.marshal(osw);
			}
		}
		catch (Exception e) {
			logger.error(Messages.getString("SettingTools.MarshalXmlFailed"), e);
			logger.error("End Export PlatformRepositoryScope (Error)");
			return SettingConstants.ERROR_INPROCESS;
		}

		logger.info(Messages.getString("SettingTools.ExportCompleted") + "(Scope)");
		logger.info(Messages.getString("SettingTools.ExportCompleted") + "(ScopeNode)");
		logger.debug("End Export RepositoryScope and RepositoryScopeNode");

		return SettingConstants.SUCCESS;
	}
	
	@DiffAnnotation(value={
		"{\"type\":\"Root\", \"funcName\":\"RepositoryScope_funcName\"}",
		"{\"type\":\"OrderBy\"," +
		"\"props\":[" +
			"\"FacilityName\"," +
			"\"Description\"," +
			"\"ScopeInfo\"," +
			"\"Nodes.*.ScopeFacilityName\"," +
			"\"Nodes.*.NodeFacilityName\"" +
		"]}"
		})
	public static class ScopeNodeRoot {
		public Map<String, Scope> scopes = new HashMap<String, Scope>();

		@DiffAnnotation("{\"type\":\"Comparison\"}")
		public Scope[] getScopes() {
			List<Scope> list = new ArrayList<Scope>(scopes.values());
			Collections.sort(list, new Comparator<Scope>() {
				@Override
				public int compare(Scope o1, Scope o2) {
					return o1.id.compareTo(o2.id);
				}
			});
			return list.toArray(new Scope[0]);
		}
	}

	@DiffAnnotation("{\"type\":\"Element\"}")
	public static class Scope {
		public String id;

		public List<ScopeNodeInfo> scopeNodeInfos = new ArrayList<ScopeNodeInfo>();

		@DiffAnnotation("{\"type\":\"PrimaryKey\"}")
		public String getId() {
			return id;
		}

		@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"ScopeInfo_node\"}", "{\"type\":\"Array\"}"})
		public ScopeNodeInfo[] getNodes() {
			return scopeNodeInfos.toArray(new ScopeNodeInfo[0]);
		}
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
	public int diffXml(String xmlScope1, String xmlScopeNode1, String xmlScope2, String xmlScopeNode2) throws ConvertorException {
		logger.debug("Start Differrence RepositoryScope and RepositoryScopeNode");

		int ret = 0;
		
		// XMLファイルからの読み込み
		RepositoryScope scope1 = null;
		RepositoryScopeNode scopeNode1 = null;
		RepositoryScope scope2 = null;
		RepositoryScopeNode scopeNode2 = null;
		try {
			scope1 = XmlMarshallUtil.unmarshall(RepositoryScope.class,new InputStreamReader(new FileInputStream(xmlScope1), "UTF-8"));
			scopeNode1 = XmlMarshallUtil.unmarshall(RepositoryScopeNode.class,new InputStreamReader(new FileInputStream(xmlScopeNode1), "UTF-8"));
			scope2 = XmlMarshallUtil.unmarshall(RepositoryScope.class,new InputStreamReader(new FileInputStream(xmlScope2), "UTF-8"));
			scopeNode2 = XmlMarshallUtil.unmarshall(RepositoryScopeNode.class,new InputStreamReader(new FileInputStream(xmlScopeNode2), "UTF-8"));
		}
		catch (Exception e) {
			logger.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			logger.debug("End Differrence RepositoryScope and RepositoryScopeNode (Error)");
			return SettingConstants.ERROR_INPROCESS;
		}

		// スキーマのバージョンチェック
		if(!(checkSchemaVersionScope(scope1.getSchemaInfo()) &&
				checkSchemaVersionScope(scopeNode1.getSchemaInfo())
				)){
			return SettingConstants.ERROR_SCHEMA_VERSION;
		}

		// スキーマのバージョンチェック
		if(!(checkSchemaVersionScope(scope2.getSchemaInfo()) &&
				checkSchemaVersionScope(scopeNode2.getSchemaInfo())
				)){
			return SettingConstants.ERROR_SCHEMA_VERSION;
		}
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(scope1, scope2, RepositoryScope.class, resultA);
			assert resultA.getResultBs().size() == 1;
			
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_1;
			}
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlScope2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(xmlScope2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						logger.warn(String.format("Fail to delete file. %s", f.getAbsolutePath()));
				}
			}
			ScopeNodeRoot root1 = new ScopeNodeRoot();
			for (ScopeNodeInfo info: scopeNode1.getScopeNodeInfo()) {
				Scope scope = root1.scopes.get(info.getScopeFacilityId());
				if (scope == null) {
					scope = new Scope();
					scope.id = info.getScopeFacilityId();
					root1.scopes.put(info.getScopeFacilityId(), scope);
				}
				scope.scopeNodeInfos.add(info);
			}
			ScopeNodeRoot root2 = new ScopeNodeRoot();
			for (ScopeNodeInfo info: scopeNode2.getScopeNodeInfo()) {
				Scope scope = root2.scopes.get(info.getScopeFacilityId());
				if (scope == null) {
					scope = new Scope();
					scope.id = info.getScopeFacilityId();
					root2.scopes.put(info.getScopeFacilityId(), scope);
				}
				scope.scopeNodeInfos.add(info);
			}
			//下位スコープ、ノードの比較処理
			diff = DiffUtil.diffCheck2(root1, root2, ScopeNodeRoot.class, resultA);
			assert resultA.getResultBs().size() == 1;
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_2;
			}
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlScopeNode2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(xmlScopeNode2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						logger.warn(String.format("Fail to delete File. %s", f.getAbsolutePath()));
				}
			}
		} catch (FileNotFoundException e) {
			logger.warn(e.getMessage());
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (Exception e) {
			logger.error("unexpected: ", e);
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
			logger.info(Messages.getString("SettingTools.DiffCompleted"));
		}else{
			logger.error(Messages.getString("SettingTools.EndWithErrorCode") );
		}

		logger.debug("End Differrence RepositoryScope and RepositoryScopeNode");
		
		return ret;
	}
	

	protected void checkDelete(RepositoryScope xmlElements){
		List<com.clustercontrol.utility.settings.platform.xml.ScopeInfo> xmlElementList = createXmlElementList(xmlElements.getScopeInfo());
		FacilityTreeItemResponse facilityTreeItem = null;
		try {
			facilityTreeItem = RepositoryRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getFacilityTree("");
		} catch (Exception e) {
			logger.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			logger.debug(e.getMessage(), e);
		}
		if (facilityTreeItem != null) {
			recursiveCheckDelete(xmlElementList, facilityTreeItem);
		}
	}

	private List<com.clustercontrol.utility.settings.platform.xml.ScopeInfo> 
			createXmlElementList(com.clustercontrol.utility.settings.platform.xml.ScopeInfo[] xmlElements) {
		List<com.clustercontrol.utility.settings.platform.xml.ScopeInfo> xmlElementList = new ArrayList<>();
		xmlElementList.addAll(Arrays.asList(xmlElements));
		for (com.clustercontrol.utility.settings.platform.xml.ScopeInfo xmlElement : xmlElements) {
			if (xmlElement.getScopeInfo().length > 0) {
				// 子供を探索
				xmlElementList.addAll(createXmlElementList(xmlElement.getScopeInfo()));
			}
		}
		return xmlElementList;
	}

	/**
	 * 指定されたスコープとその配下のスコープを探索し、マネージャのみに存在するデータの削除確認をする
	 * 
	 * @param xmlElementList
	 * @param treeItem
	 */
	private void recursiveCheckDelete(List<com.clustercontrol.utility.settings.platform.xml.ScopeInfo> xmlElementList, FacilityTreeItemResponse treeItem) {
		FacilityInfoResponse scopeInfo = treeItem.getData();
		
		if (scopeInfo.getFacilityType() != FacilityInfoResponse.FacilityTypeEnum.SCOPE
				&& scopeInfo.getFacilityType() !=  FacilityInfoResponse.FacilityTypeEnum.COMPOSITE) {
			return;
		}
		if (RepositoryConv.checkInternalScope(scopeInfo.getFacilityId())) {
			return;
		}
		
		if (scopeInfo.getFacilityType() ==  FacilityInfoResponse.FacilityTypeEnum.SCOPE) {
			boolean delete = true;
			for(com.clustercontrol.utility.settings.platform.xml.ScopeInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(scopeInfo.getFacilityId().equals(xmlElement.getFacilityId())){
					delete = false;
					xmlElementList.remove(xmlElement);
					break;
				}
			}
			if (delete) {
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {scopeInfo.getFacilityId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					try {
						List<String> args = new ArrayList<>();
						args.add(scopeInfo.getFacilityId());
						RepositoryRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteScope(String.join(",",args));
						logger.info(Messages.getString("SettingTools.SubSucceeded.Delete") + "(Scope) : " + scopeInfo.getFacilityId());
						// 成功した場合はリターン、それ以外は子供を探索
						return;
					} catch (InvalidRole | InvalidUserPass | UsedFacility e) {
						logger.warn(Messages.getString("SettingTools.ClearFailed") + "(Scope) : " + scopeInfo.getFacilityId() + ", " + HinemosMessage.replace(e.getMessage()));
					} catch (Exception e) {
						logger.error(Messages.getString("SettingTools.ClearFailed") + "(Scope) : " + scopeInfo.getFacilityId() + ", " + HinemosMessage.replace(e.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					logger.info(Messages.getString("SettingTools.SubSucceeded.Skip") + "(Scope) : " + scopeInfo.getFacilityId());
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					logger.info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
					return;
				}
			}
		}
		// 子供を探索
		for (FacilityTreeItemResponse childItem : treeItem.getChildren()) {
			recursiveCheckDelete(xmlElementList, childItem);
			if (DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
				// キャンセルの場合は終了
				break;
			}
		}
		return;
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
					logger);
		}
	}
	
	public Logger getLogger() {
		return logger;
	}
}