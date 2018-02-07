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
import java.util.List;
import java.util.Map;

import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
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
import com.clustercontrol.utility.settings.ui.dialog.ImportProcessDialog;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.ws.repository.FacilityDuplicate_Exception;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;
import com.clustercontrol.ws.repository.HinemosUnknown_Exception;
import com.clustercontrol.ws.repository.InvalidRole_Exception;
import com.clustercontrol.ws.repository.InvalidSetting_Exception;
import com.clustercontrol.ws.repository.InvalidUserPass_Exception;

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

		// スコープ情報、スコープ内ノード情報の取得
		List<com.clustercontrol.ws.repository.FacilityInfo> scopeList;
		try {
			scopeList = RepositoryEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getFacilityList("");
		}
		catch (Exception e) {
			handleDTOException(e, Messages.getString("SettingTools.FailToGetList"), HinemosMessage.replace(e.getMessage()));
			return SettingConstants.ERROR_INPROCESS;
		}

		// スコープの削除
		List<String> ids = new ArrayList<>();
		int resultNumber = SettingConstants.SUCCESS;
		for (com.clustercontrol.ws.repository.FacilityInfo scope : scopeList) {
			// 起点となるスコープのみ削除
			if (!RepositoryConv.checkInternalScope(scope.getFacilityId())) {
				ids.add(scope.getFacilityId());
			}
		}

		try {
			RepositoryEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).deleteScope(ids);
			logger.info(Messages.getString("SettingTools.ClearSucceeded") + "(Scope) : " + ids.toString());
		} catch (WebServiceException e) {
			logger.error(Messages.getString("SettingTools.ClearFailed") + "(Scope) : " + HinemosMessage.replace(e.getMessage()));
			resultNumber = SettingConstants.ERROR_INPROCESS;
		} catch (Exception e) {
			logger.warn(Messages.getString("SettingTools.ClearFailed") + "(Scope) : " + HinemosMessage.replace(e.getMessage()));
			resultNumber = SettingConstants.ERROR_INPROCESS;
		}

		if (resultNumber == SettingConstants.SUCCESS) {
			logger.info(Messages.getString("SettingTools.ClearCompleted") + "(Scope)");
		}

		logger.debug("End Clear PlatformRepositoryScope ");
		return resultNumber;
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

		if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
	    	logger.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
	    	logger.debug("End Import RepositoryScope and RepositoryScopeNode (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
	    }
		
		// XMLファイルからの読み込み
		RepositoryScope scope = null;
		RepositoryScopeNode scopeNode = null;
		try {
			scope = RepositoryScope.unmarshal(new InputStreamReader(new FileInputStream(xmlScope), "UTF-8"));
			scopeNode = RepositoryScopeNode.unmarshal(new InputStreamReader(new FileInputStream(xmlScopeNode), "UTF-8"));
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

		//　スコープ情報の登録。
		List<String> objectIdList = new ArrayList<String>();
		int resultNumber_Scope = SettingConstants.SUCCESS;
		for (com.clustercontrol.utility.settings.platform.xml.ScopeInfo childScope : scope.getScopeInfo()) {
			resultNumber_Scope = recursiveRegistScopeInfo(null, childScope, objectIdList);
			if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
				return SettingConstants.ERROR_INPROCESS;
		    }
		}

		// 全スコープの登録に成功した場合
		if (resultNumber_Scope == SettingConstants.SUCCESS) {
			logger.info(Messages.getString("SettingTools.ImportCompleted") + "(Scope)");
		}

		// スコープ内ノード情報の登録
		int resultNumber_ScopeNode = SettingConstants.SUCCESS;
		for (com.clustercontrol.utility.settings.platform.xml.ScopeNodeInfo scopeNodeInfo : scopeNode.getScopeNodeInfo()) {
			ArrayList<String> facilityIdList = new ArrayList<String>();
			facilityIdList.add(scopeNodeInfo.getNodeFacilityId());
			try {
				RepositoryEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).assignNodeScope(scopeNodeInfo.getScopeFacilityId(), facilityIdList);
				logger.info(
						Messages.getString("SettingTools.ImportSucceeded") + "(ScopeNode) : " + scopeNodeInfo.getNodeFacilityId() + "@" + scopeNodeInfo.getScopeFacilityId()
						);
			}
			catch (Exception e) {
				resultNumber_ScopeNode = SettingConstants.ERROR_INPROCESS;
				String scopeNodeString = "(ScopeNode) : " + scopeNodeInfo.getNodeFacilityId() + "@" + scopeNodeInfo.getScopeFacilityId() + " " + HinemosMessage.replace(e.getMessage());
				if (!handleDTOException(e, Messages.getString("SettingTools.ImportFailed"), scopeNodeString)) {
					break;
				}
			}
		}

		// 全スコープ内ノードの登録に成功した場合
		if (resultNumber_ScopeNode == SettingConstants.SUCCESS) {
			logger.info(Messages.getString("SettingTools.ImportCompleted") + "(ScopeNode)");
		}
		
		//オブジェクト権限同時インポート
		importObjectPrivilege(HinemosModuleConstant.PLATFORM_REPOSITORY, objectIdList);
		
		//差分削除
		checkDelete(scope);
		
		int resultNumber;
		if (resultNumber_Scope == SettingConstants.SUCCESS && resultNumber_ScopeNode == SettingConstants.SUCCESS) {
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

		if (e instanceof HinemosUnknown_Exception) {
			logger.error(title + scopeNodeString);
			logger.error(Messages.getString("SettingTools.UnexpectedError"));
		}
		else if (e instanceof InvalidRole_Exception) {
			logger.error(title + scopeNodeString);
			logger.error(Messages.getString("SettingTools.InvalidRole"));
		}
		else if (e instanceof InvalidUserPass_Exception) {
			logger.error(title + scopeNodeString);
			logger.error(Messages.getString("SettingTools.InvalidUserPass"));
		}
		else if (e instanceof InvalidSetting_Exception) {
			logger.warn(title + scopeNodeString);
			logger.warn(Messages.getString("SettingTools.InvalidSetting"));
		}
		else if (e instanceof WebServiceException) {
			logger.error(title + scopeNodeString);
		}
		else {
			logger.warn(title + scopeNodeString);
		}
		logger.debug(e);

		return isContinue;
	}

	/**
	 * Caster のスコープ情報を DTO 構造に変換し登録する<BR>
	 *
	 * @param 親の FacilityId。 null の場合、ルートの "スコープ" スコープに接続。
	 * @param 登録する ScopeInfo。
	 * @return
	 */
	private static int recursiveRegistScopeInfo(
			String parentFacilityId,
			com.clustercontrol.utility.settings.platform.xml.ScopeInfo childScope_ca,
			List<String> objectIdList) {

		com.clustercontrol.ws.repository.ScopeInfo childScope_ws = RepositoryScopeConv.createScopeInfo_ws(childScope_ca);

		int resultNumber = SettingConstants.SUCCESS;
		try {
			childScope_ws.setFacilityType(FacilityConstant.TYPE_SCOPE);
			// parentFacilityId が null の場合、ルートのスコープに接続。
			RepositoryEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).addScope(parentFacilityId == null ? "" : parentFacilityId, childScope_ws);
			objectIdList.add(childScope_ws.getFacilityId());
			logger.info(Messages.getString("SettingTools.ImportSucceeded") + "(Scope) : " + childScope_ca.getFacilityId());
		}
		catch (FacilityDuplicate_Exception e) {
			//重複時、インポート処理方法を確認する
			if(!ImportProcessMode.isSameprocess()){
				String[] args = {childScope_ca.getFacilityId()};
				ImportProcessDialog dialog = new ImportProcessDialog(
						null, Messages.getString("message.import.confirm2", args));
			    ImportProcessMode.setProcesstype(dialog.open());
			    ImportProcessMode.setSameprocess(dialog.getToggleState());
			}

			if(ImportProcessMode.getProcesstype() == ImportProcessDialog.UPDATE){
				try {
					RepositoryEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).modifyScope(childScope_ws);

					objectIdList.add(childScope_ws.getFacilityId());
					logger.info(Messages.getString("SettingTools.ImportSucceeded.Update") + "(Scope) : " + childScope_ca.getFacilityId());
				} catch (HinemosUnknown_Exception e1) {
					logger.warn(Messages.getString("SettingTools.ImportFailed") + "(Scope) : " + HinemosMessage.replace(e.getMessage()));
					resultNumber = SettingConstants.ERROR_INPROCESS;
				} catch (InvalidRole_Exception e1) {
					logger.warn(Messages.getString("SettingTools.InvalidRole") + "(Scope) : " + HinemosMessage.replace(e.getMessage()));
					resultNumber = SettingConstants.ERROR_INPROCESS;
				} catch (InvalidUserPass_Exception e1) {
					logger.warn(Messages.getString("SettingTools.InvalidUserPass") + "(Scope) : " + HinemosMessage.replace(e.getMessage()));
					resultNumber = SettingConstants.ERROR_INPROCESS;
				} catch (InvalidSetting_Exception e1) {
					logger.warn(Messages.getString("SettingTools.InvalidSetting") + "(Scope) : " + HinemosMessage.replace(e.getMessage()));
					resultNumber = SettingConstants.ERROR_INPROCESS;
				} catch (Exception e1) {
					logger.warn(Messages.getString("SettingTools.ImportFailed") + "(Scope) : " + HinemosMessage.replace(e.getMessage()));
					resultNumber = SettingConstants.ERROR_INPROCESS;
				}
			} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.SKIP){
				logger.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + "(Scope) : " + childScope_ca.getFacilityId());
			} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
				logger.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
				return SettingConstants.ERROR_INPROCESS;
			}
		} catch (Exception e) {
			String scopeInfoString = "(Scope) : " + childScope_ca.getFacilityId() + " " + HinemosMessage.replace(e.getMessage());
			resultNumber = SettingConstants.ERROR_INPROCESS;
			if (!handleDTOException(e, Messages.getString("SettingTools.ImportFailed"), scopeInfoString)) {
				return resultNumber;
			}
		}

		// スコープ情報の登録。
		// 途中、例外が発生しても継続する。
		for (com.clustercontrol.utility.settings.platform.xml.ScopeInfo gchildInfo_ca : childScope_ca.getScopeInfo()) {
			recursiveRegistScopeInfo(childScope_ca.getFacilityId(), gchildInfo_ca, objectIdList);
			if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
				break;
		    }
		}

		return resultNumber;
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
	    private FacilityInfo data;
	    private FacilityTreeItem facilityTreeItem;
	    private RepositoryScopeConv.IFacilityTreeItem parent;

	    private WrappedFacilityTreeItem(RepositoryScopeConv.IFacilityTreeItem parent, FacilityTreeItem facilityTreeItem) throws ConvertorException {
	    	super();
	    	assert facilityTreeItem != null;

	    	this.parent = parent;
	    	this.facilityTreeItem = facilityTreeItem;
	    }

		@Override
	    public List<RepositoryScopeConv.IFacilityTreeItem> getChildren() throws Exception {
	    	if (children == null) {
	    		children = new ArrayList<RepositoryScopeConv.IFacilityTreeItem>();

	    		for (FacilityTreeItem child: facilityTreeItem.getChildren()) {
	    			children.add(new WrappedFacilityTreeItem(this, child));
	    		}
	    	}

	    	return children;
	    }

		@Override
	    public FacilityInfo getData() throws Exception {
	    	if (data == null) {
	    		switch (facilityTreeItem.getData().getFacilityType()) {
	    		case FacilityConstant.TYPE_SCOPE:
			    	data = RepositoryEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getScope(facilityTreeItem.getData().getFacilityId());
			    	data.setFacilityType(FacilityConstant.TYPE_SCOPE);//Manager側のバグ対応
			    	break;
	    		case FacilityConstant.TYPE_NODE:
			    	data = RepositoryEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getNode(facilityTreeItem.getData().getFacilityId());
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
			FacilityTreeItem facilityTreeItem = RepositoryEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getFacilityTree(null);
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
			scope1 = RepositoryScope.unmarshal(new InputStreamReader(new FileInputStream(xmlScope1), "UTF-8"));
			scopeNode1 = RepositoryScopeNode.unmarshal(new InputStreamReader(new FileInputStream(xmlScopeNode1), "UTF-8"));
			scope2 = RepositoryScope.unmarshal(new InputStreamReader(new FileInputStream(xmlScope2), "UTF-8"));
			scopeNode2 = RepositoryScopeNode.unmarshal(new InputStreamReader(new FileInputStream(xmlScopeNode2), "UTF-8"));
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
		List<com.clustercontrol.ws.repository.FacilityInfo> subList = null;
		try {
			subList = RepositoryEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getFacilityList("");
		}
		catch (Exception e) {
			logger.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			logger.debug(e.getMessage(), e);
		}
		
		if(subList == null || subList.size() <= 0){
			return;
		}
		
		List<ScopeInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getScopeInfo()));
		for(com.clustercontrol.ws.repository.FacilityInfo mgrInfo: new ArrayList<>(subList)){
			if(!RepositoryConv.checkInternalScope(mgrInfo.getFacilityId())){
				for(ScopeInfo xmlElement: new ArrayList<>(xmlElementList)){
					if(mgrInfo.getFacilityId().equals(xmlElement.getFacilityId())){
						subList.remove(mgrInfo);
						xmlElementList.remove(xmlElement);
						break;
					}
				}
			} else {
				subList.remove(mgrInfo);
			}
		}
		
		if(subList.size() > 0){
			for(com.clustercontrol.ws.repository.FacilityInfo info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getFacilityId()};
					DeleteProcessDialog dialog = new DeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
			    
			    if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.DELETE){
			    	try {
			    		List<String> args = new ArrayList<>();
			    		args.add(info.getFacilityId());
			    		RepositoryEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).deleteScope(args);
			    		logger.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getFacilityId());
					} catch (Exception e1) {
						logger.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
			    } else if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.SKIP){
			    	logger.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getFacilityId());
			    } else if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.CANCEL){
			    	logger.info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
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
					logger);
		}
	}
	
	public Logger getLogger() {
		return logger;
	}
}