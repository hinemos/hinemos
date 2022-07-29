/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.openapitools.client.model.AddNodeConfigSettingInfoRequest;
import org.openapitools.client.model.NodeConfigSettingInfoResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;
import org.openapitools.client.model.ImportNodeConfigSettingRecordRequest;
import org.openapitools.client.model.ImportNodeConfigSettingRequest;
import org.openapitools.client.model.ImportNodeConfigSettingResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
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
import com.clustercontrol.utility.settings.platform.conv.CommonConv;
import com.clustercontrol.utility.settings.platform.conv.NodeConfigSettingConv;
import com.clustercontrol.utility.settings.platform.xml.NodeConfigInfo;
import com.clustercontrol.utility.settings.platform.xml.NodeConfigList;
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
 * リポジトリ-構成情報取得設定をインポート・エクスポート・削除するアクションクラス<br>
 *
 * @version 6.2.0
 * @since 6.2.0
 * 
 */
public class NodeConfigSettingAction {

	NodeConfigSettingConv nodeConfigConv = new NodeConfigSettingConv();
	protected static Logger log = Logger.getLogger(NodeConfigSettingAction.class);

	/**
	 * コンストラクター <BR>
	 * SessionBeanを初期化します。
	 * @throws ConvertorException
	 */
	public NodeConfigSettingAction() throws ConvertorException {
		super();
	}

	/**
	 * 情報をマネージャから削除します。<BR>
	 *
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearNodeConfigSetting() {

		log.debug("Start Clear NodeConfigSetting ");

		// 返り値変数(条件付き正常終了用）
		int ret = SettingConstants.SUCCESS;
		List<NodeConfigSettingInfoResponse> nodeConfigList;

		// 構成情報収集設定一覧の取得
		RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		try {
			nodeConfigList = wrapper.getNodeConfigSettingList();
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear PlatformRepositoryNode (Error)");
			return ret;
		}

		// 構成情報収集設定の削除
		List<String> ids = new ArrayList<>();
		for (NodeConfigSettingInfoResponse nodeConfig : nodeConfigList) {
			ids.add(nodeConfig.getSettingId());
		}
		String idsString = String.join(",", ids);

		try {
			wrapper.deleteNodeConfigSettingInfo(idsString);
			log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + ids.toString());
		} catch (InvalidUserPass e) {
			log.warn(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (InvalidRole e) {
			log.warn(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (Exception e) {
			log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		}

		// 処理の終了
		if (ret == SettingConstants.SUCCESS) {
			log.info(Messages.getString("SettingTools.ClearCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}

		log.debug("End Clear NodeConfigSetting ");
		return ret;
	}

	/**
	 * 情報をマネージャから取得し、XMLに出力します。<BR>
	 *
	 * @param 出力するXMLファイル
	 * @return 終了コード
	 */
	@ExportMethod
	public int exportNodeConfigSetting(String xmlNodeConfigSetting) {

		log.debug("Start Export NodeConfigSetting ");

		// 返り値変数(条件付き正常終了用）
		int ret = SettingConstants.SUCCESS;

		List<NodeConfigSettingInfoResponse> list;
		// 構成情報収集設定一覧の取得
		try {
			list = RepositoryRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getNodeConfigSettingList();
			Collections.sort(list, new Comparator<NodeConfigSettingInfoResponse>() {
				@Override
				public int compare(
						NodeConfigSettingInfoResponse info1,
						NodeConfigSettingInfoResponse info2) {
					return info1.getSettingId().compareTo(info2.getSettingId());
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export NodeConfigSetting (Error)");
			return ret;
		}

		NodeConfigList nodeConfig = new NodeConfigList();
		nodeConfig.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
		nodeConfig.setSchemaInfo(nodeConfigConv.getSchemaVersion());
		// XML Beanに情報を格納
		nodeConfig.setNodeConfigInfo(nodeConfigConv.convDto2Xml(list));

		// XMLファイルに出力
		try(FileOutputStream fos = new FileOutputStream(xmlNodeConfigSetting);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
			nodeConfig.marshal(osw);
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed") + e.getMessage());
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
		}

		// 処理の終了
		if (ret == SettingConstants.SUCCESS) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}

		log.debug("End Export NodeConfigSetting ");
		return ret;
	}

	/**
	 * XMLの情報をマネージャに投入します。<BR>
	 *
	 * @param 入力するXMLファイル
	 * @return 終了コード
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * @throws RestConnectFailed 
	 */
	@ImportMethod
	public int importNodeConfigSetting(String xmlNodeConfigSetting) throws InvalidSetting, HinemosUnknown, RestConnectFailed {

		log.debug("Start Import NodeConfigSetting ");

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			getLogger().debug("End Import PlatformRepositoryNode (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}

		// 返り値変数(条件付き正常終了用）
		int ret = SettingConstants.SUCCESS;
		NodeConfigList nodeConfig;

		// XMLファイルからの読み込み
		try {
			// 下位互換向けにXMLの内容確認（順番チェック）を緩くしておく
			nodeConfig = XmlMarshallUtil.unmarshall(NodeConfigList.class,new InputStreamReader(new FileInputStream(xmlNodeConfigSetting), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import NodeConfigSetting (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(nodeConfig.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		ImportNodeConfigSettingRecordConfirmer nodeConfifSettingConfirmer = new ImportNodeConfigSettingRecordConfirmer( log, nodeConfig.getNodeConfigInfo() );
		int nodeConfigSettingConfirmerRet = nodeConfifSettingConfirmer.executeConfirm();
		if (nodeConfigSettingConfirmerRet != 0) {
			ret = nodeConfigSettingConfirmerRet;
		}
		
		// レコードの登録（構成情報取得）
		if (!(nodeConfifSettingConfirmer.getImportRecDtoList().isEmpty())) {
			ImportNodeConfigSettingClientController nodeConfifSettingController = new ImportNodeConfigSettingClientController(log,
					Messages.getString("node.config.setting"), nodeConfifSettingConfirmer.getImportRecDtoList(), true);
			int nodeConfifSettingControllerRet = nodeConfifSettingController.importExecute();
			if (nodeConfifSettingControllerRet != 0) {
				ret = nodeConfifSettingControllerRet;
			}
		}
		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		// 差分削除
		checkDelete(nodeConfifSettingConfirmer.getImportRecDtoList());

		// 処理の終了
		if (ret == SettingConstants.SUCCESS) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}

		log.debug("End Import NodeConfigSetting ");
		return ret;
	}


	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.platform.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = nodeConfigConv.checkSchemaVersion(schmaversion.getSchemaType(),
				schmaversion.getSchemaVersion(),
				schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo sci = nodeConfigConv.getSchemaVersion();

		return BaseAction.checkSchemaVersionResult(getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	/**
	 *差分比較処理を行います。
	 * XMLファイル２つを比較する。
	 * 差分がある場合：差分をＣＳＶファイルで出力する。
	 * 差分がない場合：出力しない。
	 * 				   または、すでに存在している同一名のＣＳＶファイルを削除する。
	 * @param xmlNodeConfig1
	 * @param xmlNodeConfig2
	 * @return 終了コード
	 */
	@DiffMethod
	public int diffXml(String xmlNodeConfig1, String xmlNodeConfig2) {

		log.debug("Start Differrence NodeConfigSetting ");

		// 返り値変数(条件付き正常終了用）
		int ret = SettingConstants.SUCCESS;
		
		NodeConfigList nodeConfig1;
		NodeConfigList nodeConfig2;

		// XMLファイルからの読み込み
		try {
			nodeConfig1 = XmlMarshallUtil.unmarshall(NodeConfigList.class,new InputStreamReader(new FileInputStream(xmlNodeConfig1), "UTF-8"));
			nodeConfig2 = XmlMarshallUtil.unmarshall(NodeConfigList.class,new InputStreamReader(new FileInputStream(xmlNodeConfig2), "UTF-8"));

			sort(nodeConfig1);
			sort(nodeConfig2);
		} catch (FileNotFoundException e) {
			log.warn(e.getMessage());
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		} catch (MarshalException | ValidationException | UnsupportedEncodingException e) {
			log.warn(Messages.getString("SettingTools.UnmarshalXmlFailed") + e.getMessage());
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence NodeConfigSetting (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(nodeConfig1.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(nodeConfig2.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		ResultA resultA = new ResultA();
		//比較処理に渡す
		boolean diff = DiffUtil.diffCheck2(nodeConfig1, nodeConfig2, NodeConfigList.class, resultA);
		assert resultA.getResultBs().size() == 1;

		if(diff){
			ret += SettingConstants.SUCCESS_DIFF_1;
		}

		FileOutputStream fos = null;
		try {
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlNodeConfig2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			} else {
				//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
				File f = new File(xmlNodeConfig2 + ".csv");
				if (f.exists()) {
					if (!f.delete()) {
						log.warn(String.format("Fail to delete File. %s", f.getAbsolutePath()));
					}
				}
			}
		} catch (Exception e) {
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

		log.debug("End Differrence NodeConfigSetting");

		return ret;
	}

	private void sort(NodeConfigList nodeConfig) {
		NodeConfigInfo[] infoList = nodeConfig.getNodeConfigInfo();
		
		Arrays.sort(
				infoList,
				new Comparator<NodeConfigInfo>() {
					@Override
					public int compare(NodeConfigInfo info1, NodeConfigInfo info2) {
						return info1.getSettingId().compareTo(info2.getSettingId());
					}
				});
		nodeConfig.setNodeConfigInfo(infoList);
	}

	public Logger getLogger() {
		return log;
	}

	protected void checkDelete(List<ImportNodeConfigSettingRecordRequest> nodeList){
		List<NodeConfigSettingInfoResponse> subList = null;
		RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		try {
			subList = wrapper.getNodeConfigSettingList();
		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}

		if(subList == null || subList.size() <= 0){
			return;
		}

		for(NodeConfigSettingInfoResponse mgrInfo: new ArrayList<>(subList)) {
			for(ImportNodeConfigSettingRecordRequest xmlElement: new ArrayList<>(nodeList)) {
				if(mgrInfo.getSettingId().equals(xmlElement.getImportData().getSettingId())) {
					subList.remove(mgrInfo);
					nodeList.remove(xmlElement);
					break;
				}
			}
		}

		for(NodeConfigSettingInfoResponse info: subList) {
			//マネージャのみに存在するデータがあった場合の削除方法を確認する
			if(!DeleteProcessMode.isSameprocess()) {
				String[] args = {info.getSettingId()};
				DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
						null, Messages.getString("message.delete.confirm4", args));
				DeleteProcessMode.setProcesstype(dialog.open());
				DeleteProcessMode.setSameprocess(dialog.getToggleState());
			}

			if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
				try {
					List<String> args = new ArrayList<>();
					args.add(info.getSettingId());
					String argsStr = String.join(",", args);
					wrapper.deleteNodeConfigSettingInfo(argsStr);
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getSettingId());
				} catch (Exception e1) {
					getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
				}
			} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
				getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getSettingId());
			} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
				getLogger().info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
				return;
			}
		}
	}
	
	/**
	 * 構成情報取得 インポート向けのレコード確認用クラス
	 * 
	 */
	protected class ImportNodeConfigSettingRecordConfirmer extends ImportRecordConfirmer<NodeConfigInfo, ImportNodeConfigSettingRecordRequest, String>{
		
		public ImportNodeConfigSettingRecordConfirmer(Logger logger, NodeConfigInfo[] importRecDtoList) {
			super(logger, importRecDtoList);
		}
		
		@Override
		protected ImportNodeConfigSettingRecordRequest convertDtoXmlToRestReq(NodeConfigInfo xmlDto)
				throws HinemosUnknown, InvalidSetting {
			
			AddNodeConfigSettingInfoRequest nodeConfigSettingList = nodeConfigConv.convXml2Dto(xmlDto);
			ImportNodeConfigSettingRecordRequest dtoRec = new ImportNodeConfigSettingRecordRequest();
			dtoRec.setImportData(new AddNodeConfigSettingInfoRequest());
			RestClientBeanUtil.convertBean(nodeConfigSettingList, dtoRec.getImportData());
			
			dtoRec.setImportKeyValue(dtoRec.getImportData().getSettingId());
			return dtoRec;
		}

		@Override
		protected Set<String> getExistIdSet() throws Exception {
			Set<String> retSet = new HashSet<String>();
			List<NodeConfigSettingInfoResponse> nodeConfigSeeingList = RepositoryRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getNodeConfigSettingList();
			for (NodeConfigSettingInfoResponse rec : nodeConfigSeeingList) {
				retSet.add(rec.getSettingId());
			}
			return retSet;
		}
		@Override
		protected boolean isLackRestReq(ImportNodeConfigSettingRecordRequest restDto) {
			return (restDto == null || restDto.getImportData().getSettingId() == null || restDto.getImportData().getSettingId().equals(""));
		}
		@Override
		protected String getKeyValueXmlDto(NodeConfigInfo xmlDto) {
			return xmlDto.getSettingId();
		}
		@Override
		protected String getId(NodeConfigInfo xmlDto) {
			return xmlDto.getSettingId();
		}
		@Override
		protected void setNewRecordFlg(ImportNodeConfigSettingRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
	}
	
	/**
	 * 構成情報取得 インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportNodeConfigSettingClientController extends ImportClientController<ImportNodeConfigSettingRecordRequest, ImportNodeConfigSettingResponse, RecordRegistrationResponse>{
		
		public ImportNodeConfigSettingClientController(Logger logger, String importInfoName, List<ImportNodeConfigSettingRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportNodeConfigSettingResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportNodeConfigSettingResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportNodeConfigSettingRecordRequest importRec) {
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
		protected ImportNodeConfigSettingResponse callImportWrapper(List<ImportNodeConfigSettingRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportNodeConfigSettingRequest reqDto = new ImportNodeConfigSettingRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importNodeConfigSetting(reqDto);
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
