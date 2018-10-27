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
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.conv.CommonConv;
import com.clustercontrol.utility.settings.platform.conv.LogFormatConv;
import com.clustercontrol.utility.settings.platform.xml.LogFormat;
import com.clustercontrol.utility.settings.platform.xml.LogFormatInfo;
import com.clustercontrol.utility.settings.platform.xml.LogFormatType;
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
//import com.clustercontrol.ws.hub.LogFormatDuplicate_Exception;
import com.clustercontrol.ws.hub.LogFormatDuplicate_Exception;


/**
 * ログフォーマット定義情報をインポート・エクスポート・削除するアクションクラス<br>
 *
 * @version 6.1.0
 * @since 6.0.0
 *
 */
public class LogFormatAction {

	protected static Logger log = Logger.getLogger(LogFormatAction.class);

	public LogFormatAction() throws ConvertorException {
		super();
	}

	/**
	 * 情報をマネージャから削除します。<BR>
	 *
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearLogFormat() {

		log.debug("Start Clear PlatformLogFormat ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		// ログフォーマット定義一覧の取得
		List<com.clustercontrol.ws.hub.LogFormat> logformatList = null;

		try {
			logformatList = HubEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getLogFormatList();
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear PlatformLogFormat (Error)");
			return ret;
		}

		// ログフォーマット定義の削除
		for (com.clustercontrol.ws.hub.LogFormat logFormatInfo : logformatList) {
			try {
				HubEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteLogFormat(Arrays.asList(logFormatInfo.getLogFormatId()));
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + logFormatInfo.getLogFormatId());
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
		log.debug("End Clear PlatformLogFormat ");

		return ret;
	}

	/**
	 * 情報をマネージャから取得し、XMLに出力します。<BR>
	 *
	 * @param 出力するXMLファイル
	 * @return 終了コード
	 */
	@ExportMethod
	public int exportLogFormat(String xmlFile) {

		log.debug("Start Export PlatformLogFormat ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		// ログフォーマット定義一覧の取得
		List<com.clustercontrol.ws.hub.LogFormat> logformatList = null;
		try {
			logformatList = HubEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getLogFormatList();
			Collections.sort(logformatList, new Comparator<com.clustercontrol.ws.hub.LogFormat>() {
				@Override
				public int compare(
						com.clustercontrol.ws.hub.LogFormat info1,
						com.clustercontrol.ws.hub.LogFormat info2) {
					return info1.getLogFormatId().compareTo(info2.getLogFormatId());
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Export PlatformLogFormat (Error)");
			return ret;
		}

		// ログフォーマット定義の取得
		LogFormat logformat = new LogFormat();
		for (com.clustercontrol.ws.hub.LogFormat logformat2 : logformatList) {
			try {
				logformat.addLogFormatInfo(LogFormatConv.getLogFormat(logformat2));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + logformat2.getLogFormatId());
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}

		// XMLファイルに出力
		try {
			logformat.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));

			//スキーマ情報のセット
			logformat.setSchemaInfo(LogFormatConv.getSchemaVersion());

			try(FileOutputStream fos = new FileOutputStream(xmlFile);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				logformat.marshal(osw);
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
		}

		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}

		log.debug("End Export PlatformLogFormat ");
		return ret;
	}

	/**
	 * XMLの情報をマネージャに投入します。<BR>
	 *
	 * @param 入力するXMLファイル
	 * @return 終了コード
	 */
	@ImportMethod
	public int importLogFormat(String xmlFile) {

		log.debug("Start Import PlatformLogFormat ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			getLogger().debug("End Import PlatformLogFormat (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}

		LogFormatType logFormat = null;
//
		// XMLファイルからの読み込み
		try {
			logFormat = LogFormat.unmarshal(new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformLogFormat (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(logFormat.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		// ログフォーマット定義の登録
		List<String> objectIdList = new ArrayList<String>();
		for (LogFormatInfo logFormatInfo : logFormat.getLogFormatInfo()) {
			com.clustercontrol.ws.hub.LogFormat info = null;
			try {
				info = LogFormatConv.getLogFormatData(logFormatInfo);
				HubEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).addLogFormat(info);
				objectIdList.add(logFormatInfo.getLogFormatId());
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + logFormatInfo.getLogFormatId());
			} catch (LogFormatDuplicate_Exception e) {
				//重複時、インポート処理方法を確認する
				if(!ImportProcessMode.isSameprocess()){
					String[] args = {logFormatInfo.getLogFormatId()};
					ImportProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.import.confirm2", args));
					ImportProcessMode.setProcesstype(dialog.open());
					ImportProcessMode.setSameprocess(dialog.getToggleState());
				}

				if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.UPDATE){
					try {
						HubEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).modifyLogFormat(info);
						objectIdList.add(logFormatInfo.getLogFormatId());
						log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + logFormatInfo.getLogFormatId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
				} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + logFormatInfo.getLogFormatId());
				} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
					break;
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
		importObjectPrivilege(HinemosModuleConstant.PLATFORM_LOG_FORMAT, objectIdList);
		
		//差分削除
		checkDelete(logFormat);
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Import PlatformLogFormat ");
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
		int res = LogFormatConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo sci = LogFormatConv.getSchemaVersion();
		
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

		log.debug("Start Differrence PlatformLogFormat ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		LogFormat logFormat1 = null;
		LogFormat logFormat2 = null;

		// XMLファイルからの読み込み
		try {
			logFormat1 = (LogFormat) LogFormat.unmarshal(new InputStreamReader(new FileInputStream(filePath1), "UTF-8"));
			logFormat2 = (LogFormat) LogFormat.unmarshal(new InputStreamReader(new FileInputStream(filePath2), "UTF-8"));
			sort(logFormat1);
			sort(logFormat2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence PlatformLogFormat (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(logFormat1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(logFormat2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(logFormat1, logFormat2, LogFormat.class, resultA);
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

		getLogger().debug("End Differrence PlatformLogFormatConv");

		getLogger().debug("End Differrence PlatformLogFormat");

		return ret;
	}

	private void sort(LogFormat logFormat) {
		LogFormatInfo[] infoList = logFormat.getLogFormatInfo();
		Arrays.sort(
			infoList,
			new Comparator<LogFormatInfo>() {
				@Override
				public int compare(LogFormatInfo info1, LogFormatInfo info2) {
					return info1.getLogFormatId().compareTo(info2.getLogFormatId());
				}
			});
		logFormat.setLogFormatInfo(infoList);
	}

	public Logger getLogger() {
		return log;
	}

	protected void checkDelete(LogFormatType xmlElements){
		List<com.clustercontrol.ws.hub.LogFormat> subList = null;
		try {
			subList = HubEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getLogFormatList();
		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}

		if(subList == null || subList.size() <= 0){
			return;
		}

		List<LogFormatInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getLogFormatInfo()));
		for(com.clustercontrol.ws.hub.LogFormat mgrInfo: new ArrayList<>(subList)){
			for(LogFormatInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getLogFormatId().equals(xmlElement.getLogFormatId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}

		if(subList.size() > 0){
			for(com.clustercontrol.ws.hub.LogFormat info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getLogFormatId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}

				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					try {
						HubEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteLogFormat(Arrays.asList(info.getLogFormatId()));
						getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getLogFormatId());
						} catch (Exception e1) {
							getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
						}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getLogFormatId());
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
