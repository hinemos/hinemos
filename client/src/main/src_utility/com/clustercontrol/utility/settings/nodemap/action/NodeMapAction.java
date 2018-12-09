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

import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;

import com.clustercontrol.nodemap.util.NodeMapEndpointWrapper;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
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
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.nodemap.NodeMapException_Exception;
import com.clustercontrol.ws.repository.HinemosUnknown_Exception;
import com.clustercontrol.ws.repository.InvalidRole_Exception;
import com.clustercontrol.ws.repository.InvalidUserPass_Exception;

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

	private List<com.clustercontrol.ws.repository.FacilityInfo> getScopeList(String parentScopeId){
		List<com.clustercontrol.ws.repository.FacilityInfo> scopeListAll = new ArrayList<com.clustercontrol.ws.repository.FacilityInfo>();
		List<com.clustercontrol.ws.repository.FacilityInfo> scopeList = null;
		try {
			scopeList =
					RepositoryEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getFacilityList(parentScopeId);
			for (com.clustercontrol.ws.repository.FacilityInfo info:scopeList){
				if (info.getFacilityType() == FacilityConstant.TYPE_SCOPE){
					scopeListAll.add(info);
					scopeListAll.addAll(getScopeList(info.getFacilityId()));
				}
			}
		} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception e) {
			log.error(HinemosMessage.replace(e.getMessage()));
		}
		
		return scopeListAll;
	}
	
	private List<com.clustercontrol.ws.nodemap.NodeMapModel> getNodeMapList(
			List<com.clustercontrol.ws.repository.FacilityInfo> scopeList) throws Exception{
		if (scopeList == null){
			return null;
		}
		List<com.clustercontrol.ws.nodemap.NodeMapModel> nodeMapList =
				new ArrayList<com.clustercontrol.ws.nodemap.NodeMapModel>();
		
		com.clustercontrol.ws.nodemap.NodeMapModel nodeMapModel = null;
		NodeMapEndpointWrapper wrapperNodeMap =
				NodeMapEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		for (com.clustercontrol.ws.repository.FacilityInfo facilityInfo: scopeList){
			try {
				nodeMapModel = wrapperNodeMap.getNodeMapModel(facilityInfo.getFacilityId());
				nodeMapList.add(nodeMapModel);
			} catch (com.clustercontrol.ws.nodemap.HinemosUnknown_Exception
					| com.clustercontrol.ws.nodemap.InvalidRole_Exception
					| com.clustercontrol.ws.nodemap.InvalidUserPass_Exception | NodeMapException_Exception e) {
				log.error(HinemosMessage.replace(e.getMessage()));
				throw	e;
			}
		}
		
		return nodeMapList;
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
		int ret =0;
		
		String parentScopeId ="";
		List<com.clustercontrol.ws.repository.FacilityInfo> scopeList = getScopeList(parentScopeId);
		// ROOT付与
		com.clustercontrol.ws.repository.FacilityInfo ｆacilityInfo=new com.clustercontrol.ws.repository.FacilityInfo();
		ｆacilityInfo.setFacilityType(FacilityConstant.TYPE_SCOPE);
		ｆacilityInfo.setFacilityId("_ROOT_");
		
		scopeList.add(0, ｆacilityInfo);
		
		List<com.clustercontrol.ws.nodemap.NodeMapModel> nodeMapList =
				getNodeMapList(scopeList);
		
		//XML作成
		NodeMap nodemapList = new NodeMap();
		for (com.clustercontrol.ws.nodemap.NodeMapModel nodeMapModelWs: nodeMapList) {
			try{
				NodeMapModel nodeMapModel = NodeMapConv.dto2Xml(nodeMapModelWs);
				nodemapList.addNodeMapModel(nodeMapModel);
				
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + nodeMapModelWs.getMapId());
			
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
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
			nodeMap =  NodeMap.unmarshal(new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
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
		
		NodeMapEndpointWrapper wrapperNodeMap =
				NodeMapEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());

		// インポート
		// BGFile
		for (NodeMapModel nodeMapModel: nodeMapModels) {
			try {
				com.clustercontrol.ws.nodemap.NodeMapModel nodeMapModelDto =  NodeMapConv.xml2Dto(nodeMapModel);
				wrapperNodeMap.registerNodeMapModel(nodeMapModelDto);
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + nodeMapModelDto.getMapId());
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
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
			nodeMapType1 = NodeMap.unmarshal(new InputStreamReader(new FileInputStream(xmlFile1), "UTF-8"));
			nodeMapType2 = NodeMap.unmarshal(new InputStreamReader(new FileInputStream(xmlFile2), "UTF-8"));
			
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
}
