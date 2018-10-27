/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.hub.action;

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
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;

import com.clustercontrol.hub.util.HubEndpointWrapper;
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
import com.clustercontrol.utility.settings.hub.conv.HubConv;
import com.clustercontrol.utility.settings.hub.conv.HubTransferConv;
import com.clustercontrol.utility.settings.hub.xml.Transfer;
import com.clustercontrol.utility.settings.hub.xml.TransferInfo;
import com.clustercontrol.utility.settings.hub.xml.TransferType;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.ImportProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.hub.HinemosUnknown_Exception;
import com.clustercontrol.ws.hub.InvalidRole_Exception;
import com.clustercontrol.ws.hub.InvalidSetting_Exception;
import com.clustercontrol.ws.hub.InvalidUserPass_Exception;
/**
 * 転送設定定義情報をインポート・エクスポート・削除するアクションクラス<br>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class HubTransferAction {

	protected static Logger log = Logger.getLogger(HubTransferAction.class);

	public HubTransferAction() throws ConvertorException {
		super();
	}

	/**
	 * 情報をマネージャから削除します。<BR>
	 *
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearTransfer() {

		log.debug("Start Clear HubTransfer ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		// 転送設定定義一覧の取得
		List<com.clustercontrol.ws.hub.TransferInfo> transferList = null;

		try {
			transferList = HubEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getTransferInfoList();
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			 ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear HubTransfer (Error)");
			 return ret;
		}

		// 転送設定定義の削除
		for (com.clustercontrol.ws.hub.TransferInfo transferInfo : transferList) {
			try {
				HubEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteTransferInfo(Arrays.asList(transferInfo.getTransferId()));
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + transferInfo.getTransferId());
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
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
		log.debug("End Clear HubTransfer ");

		return ret;
	}

	/**
	 * 情報をマネージャから取得し、XMLに出力します。<BR>
	 *
	 * @param 出力するXMLファイル
	 * @return 終了コード
	 */
	@ExportMethod
	public int exportTransfer(String xmlFile) {

		log.debug("Start Export HubTransfer ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		// 転送設定定義一覧の取得
		List<com.clustercontrol.ws.hub.TransferInfo> transferList = null;
		try {
			transferList = HubEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getTransferInfoList();
			Collections.sort(transferList, new Comparator<com.clustercontrol.ws.hub.TransferInfo>() {
				@Override
				public int compare(
						com.clustercontrol.ws.hub.TransferInfo info1,
						com.clustercontrol.ws.hub.TransferInfo info2) {
					return info1.getTransferId().compareTo(info2.getTransferId());
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Export HubTransfer (Error)");
			return ret;
		}

		// 転送設定定義の取得
		Transfer transfer = new Transfer();
		for (com.clustercontrol.ws.hub.TransferInfo transfer2 : transferList) {
			try {
				transfer.addTransferInfo(HubTransferConv.getTransferInfo(transfer2));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + transfer2.getTransferId());
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + transfer2.getTransferId() +" ", e);
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}

		// XMLファイルに出力
		try {
			transfer.setCommon(HubConv.versionCollectDto2Xml(Config.getVersion()));
			// スキーマ情報のセット
			transfer.setSchemaInfo(HubTransferConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlFile);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				transfer.marshal(osw);
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed")+" ", e);
			ret=SettingConstants.ERROR_INPROCESS;
		}
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}

		log.debug("End Export HubTransfer ");

		return ret;
	}

	/**
	 * XMLの情報をマネージャに投入します。<BR>
	 *
	 * @param 入力するXMLファイル
	 * @return 終了コード
	 */
	@ImportMethod
	public int importTransfer(String xmlFile) {

		log.debug("Start Import HubTransfer ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			log.debug("End Import HubTransfer (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}

		TransferType transferType = null;
//
		// XMLファイルからの読み込み
		try {
			transferType = Transfer.unmarshal(new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Import HubTransfer (Error)");
			return ret;
		}
		//スキーマのバージョンチェック
		if(!checkSchemaVersion(transferType.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		// 転送設定定義の登録
		List<String> objectIdList = new ArrayList<String>();
		for (TransferInfo transferInfo : transferType.getTransferInfo()) {
			com.clustercontrol.ws.hub.TransferInfo info = null;
			try {
				info = HubTransferConv.getTransferData(transferInfo);
				HubEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).addTransferInfo(info);
				objectIdList.add(transferInfo.getTransferId());
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + transferInfo.getTransferId());

			} catch (com.clustercontrol.ws.hub.LogTransferDuplicate_Exception e) {
				//重複時、インポート処理方法を確認する
				if(!ImportProcessMode.isSameprocess()){
					String[] args = {transferInfo.getTransferId()};
					ImportProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.import.confirm2", args));
					ImportProcessMode.setProcesstype(dialog.open());
					ImportProcessMode.setSameprocess(dialog.getToggleState());
				}

				if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.UPDATE){
					try {
						HubEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).modifyTransferInfo(info);
						objectIdList.add(transferInfo.getTransferId());
						log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + transferInfo.getTransferId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
				} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + transferInfo.getTransferId());
				} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
					return ret;
				}
			} catch (HinemosUnknown_Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidRole_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidUserPass_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidSetting_Exception e) {
				log.warn(Messages.getString("SettingTools.InvalidSetting") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		//オブジェクト権限同時インポート
		importObjectPrivilege(HinemosModuleConstant.HUB_TRANSFER, objectIdList);
		
		//差分削除
		checkDelete(transferType);
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}

		log.debug("End Import HubTransfer ");
		return ret;
	}
	
	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.hub.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = HubTransferConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.hub.xml.SchemaInfo sci = HubTransferConv.getSchemaVersion();
		
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
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

		log.debug("Start Differrence HubTransfer ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		TransferType transferType1 = null;
		TransferType transferType2 = null;

		// XMLファイルからの読み込み
		try {
			transferType1 = Transfer.unmarshal(new InputStreamReader(new FileInputStream(filePath1), "UTF-8"));
			transferType2 = Transfer.unmarshal(new InputStreamReader(new FileInputStream(filePath2), "UTF-8"));
			sort(transferType1);
			sort(transferType2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence PlatformTransfer (Error)");
			return ret;
		}

		//スキーマのバージョンチェック
		if(!checkSchemaVersion(transferType1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(transferType2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(transferType1, transferType2, Transfer.class, resultA);
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
						log.warn(String.format("Fail to delete File. %s", f.getAbsolutePath()));
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

		return ret;
	}

	private void sort(TransferType transferType) {
		TransferInfo[] infoList = transferType.getTransferInfo();
		Arrays.sort(
			infoList,
			new Comparator<TransferInfo>() {
				@Override
				public int compare(TransferInfo info1, TransferInfo info2) {
					return info1.getTransferId().compareTo(info2.getTransferId());
				}
			});
		transferType.setTransferInfo(infoList);
	}

//	public Logger getLogger() {
//		return log;
//	}

	protected void checkDelete(TransferType xmlElements){
		List<com.clustercontrol.ws.hub.TransferInfo> subList = null;
		try {
			subList = HubEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getTransferInfoList();
		}
		catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			log.debug(e.getMessage(), e);
		}

		if(subList == null || subList.size() <= 0){
			return;
		}

		List<TransferInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getTransferInfo()));
		for(com.clustercontrol.ws.hub.TransferInfo mgrInfo: new ArrayList<>(subList)){
			for(TransferInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getTransferId().equals(xmlElement.getTransferId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}

		if(subList.size() > 0){
			for(com.clustercontrol.ws.hub.TransferInfo info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getTransferId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}

				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					try {
						HubEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteTransferInfo(Arrays.asList(info.getTransferId()));
						log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getTransferId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getTransferId());
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					log.info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
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
					log);
		}
	}
	
	public Logger getLogger() {
		return log;
	}
}
