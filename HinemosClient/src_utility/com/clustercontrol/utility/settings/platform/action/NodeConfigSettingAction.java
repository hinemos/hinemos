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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

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
import com.clustercontrol.utility.settings.platform.conv.CommonConv;
import com.clustercontrol.utility.settings.platform.conv.NodeConfigSettingConv;
import com.clustercontrol.utility.settings.platform.xml.NodeConfigInfo;
import com.clustercontrol.utility.settings.platform.xml.NodeConfigList;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.repository.HinemosUnknown_Exception;
import com.clustercontrol.ws.repository.InvalidRole_Exception;
import com.clustercontrol.ws.repository.InvalidSetting_Exception;
import com.clustercontrol.ws.repository.InvalidUserPass_Exception;
import com.clustercontrol.ws.repository.NodeConfigSettingDuplicate_Exception;
import com.clustercontrol.ws.repository.NodeConfigSettingInfo;

/**
 * リポジトリ-構成情報州設定をインポート・エクスポート・削除するアクションクラス<br>
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
		List<NodeConfigSettingInfo> nodeConfigList;

		// 構成情報収集設定一覧の取得
		RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		try {
			nodeConfigList = wrapper.getNodeConfigSettingListAll();
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear PlatformRepositoryNode (Error)");
			return ret;
		}

		// 構成情報収集設定の削除
		List<String> ids = new ArrayList<>();
		for (NodeConfigSettingInfo nodeConfig : nodeConfigList) {
			ids.add(nodeConfig.getSettingId());
		}

		try {
			wrapper.deleteNodeConfigSetting(ids);
			log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + ids.toString());
		} catch (InvalidUserPass_Exception e) {
			log.warn(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (InvalidRole_Exception e) {
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

		List<NodeConfigSettingInfo> list;
		// 構成情報収集設定一覧の取得
		try {
			list = RepositoryEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getNodeConfigSettingListAll();
			Collections.sort(list, new Comparator<com.clustercontrol.ws.repository.NodeConfigSettingInfo>() {
				@Override
				public int compare(
						com.clustercontrol.ws.repository.NodeConfigSettingInfo info1,
						com.clustercontrol.ws.repository.NodeConfigSettingInfo info2) {
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
	 */
	@ImportMethod
	public int importNodeConfigSetting(String xmlNodeConfigSetting) {

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
			nodeConfig = NodeConfigList.unmarshal(new InputStreamReader(new FileInputStream(xmlNodeConfigSetting), "UTF-8"));
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

		List<NodeConfigSettingInfo> nodeConfigSettingList = nodeConfigConv.convXml2Dto(nodeConfig);
		RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		for (NodeConfigSettingInfo nodeConfigSetting : nodeConfigSettingList) {

			try {
				// 構成情報収集設定の登録
				wrapper.addNodeConfigSetting(nodeConfigSetting);
			} catch (NodeConfigSettingDuplicate_Exception e) {
				//重複時、インポート処理方法を確認する
				if(!ImportProcessMode.isSameprocess()){
					String[] args = {nodeConfigSetting.getSettingId()};
					UtilityProcessDialog dialog = UtilityDialogInjector.createImportProcessDialog(
							null, Messages.getString("message.import.confirm2", args));
					ImportProcessMode.setProcesstype(dialog.open());
					ImportProcessMode.setSameprocess(dialog.getToggleState());
				}

				if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.UPDATE){
					try {
						wrapper.modifyNodeConfigSetting(nodeConfigSetting);
						log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + nodeConfigSetting.getSettingId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
				} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + nodeConfigSetting.getSettingId());
				} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
					ret = SettingConstants.ERROR_INPROCESS;
					break;
				}
			} catch (HinemosUnknown_Exception | InvalidSetting_Exception e) {
				log.info(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidRole_Exception e) {
				log.info(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidUserPass_Exception e) {
				log.info(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		//差分削除
		checkDelete(nodeConfigSettingList);

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
	 * @param xmlHostname1
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
			nodeConfig1 = NodeConfigList.unmarshal(new InputStreamReader(new FileInputStream(xmlNodeConfig1), "UTF-8"));
			nodeConfig2 = NodeConfigList.unmarshal(new InputStreamReader(new FileInputStream(xmlNodeConfig2), "UTF-8"));

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

		try (FileOutputStream fos = new FileOutputStream(xmlNodeConfig1 + ".csv")) {
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			} else {
				//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
				File f = new File(xmlNodeConfig1 + ".csv");
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

	protected void checkDelete(List<NodeConfigSettingInfo> nodeList){
		List<NodeConfigSettingInfo> subList = null;
		RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		try {
			subList = wrapper.getNodeConfigSettingListAll();
		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}

		if(subList == null || subList.size() <= 0){
			return;
		}

		for(NodeConfigSettingInfo mgrInfo: new ArrayList<>(subList)) {
			for(NodeConfigSettingInfo xmlElement: new ArrayList<>(nodeList)) {
				if(mgrInfo.getSettingId().equals(xmlElement.getSettingId())) {
					subList.remove(mgrInfo);
					nodeList.remove(xmlElement);
					break;
				}
			}
		}

		for(NodeConfigSettingInfo info: subList) {
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
					wrapper.deleteNodeConfigSetting(args);
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
}
