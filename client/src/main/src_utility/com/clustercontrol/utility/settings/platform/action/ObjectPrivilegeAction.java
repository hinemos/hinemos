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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.util.AccessEndpointWrapper;
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
import com.clustercontrol.utility.settings.platform.conv.ObjectPrivilegeConv;
import com.clustercontrol.utility.settings.platform.xml.ObjectPrivilege;
import com.clustercontrol.utility.settings.platform.xml.ObjectPrivilegeInfo;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.ImportProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.access.HinemosUnknown_Exception;
import com.clustercontrol.ws.access.InvalidRole_Exception;
import com.clustercontrol.ws.access.InvalidSetting_Exception;
import com.clustercontrol.ws.access.InvalidUserPass_Exception;
import com.clustercontrol.ws.access.PrivilegeDuplicate_Exception;

/**
 * オブジェクト権限定義情報をインポート・エクスポート・削除するアクションクラス<br>
 * 
 * @version 6.1.0
 * @since 2.2.0
 * 
 */
public class ObjectPrivilegeAction {

	private static Logger log = Logger.getLogger(ObjectPrivilegeAction.class);

	public ObjectPrivilegeAction() throws ConvertorException {
		super();
	}
	
	/**
	 * 情報をマネージャから削除します。<BR>
	 * 
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearAccess() {

		log.debug("Start Clear PlatformObjectPrivilege ");
		int ret = 0;
		List<com.clustercontrol.ws.access.ObjectPrivilegeInfo> objectPrivilegeList = null;
		
		// オブジェクト権限情報一覧の取得
		try {
			objectPrivilegeList = AccessEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getObjectPrivilegeInfoList(null);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear PlatformObjectPrivilege (Error)");
			return ret;
		}
		
		// オブジェクト権限情報の削除
		HashMap<String, List<com.clustercontrol.ws.access.ObjectPrivilegeInfo>> mapObjectPrivilege =
				new HashMap<String, List<com.clustercontrol.ws.access.ObjectPrivilegeInfo>>();
		for (int i = 0; i < objectPrivilegeList.size(); i++) {
			com.clustercontrol.ws.access.ObjectPrivilegeInfo privilegeInfo = objectPrivilegeList.get(i);
			List<com.clustercontrol.ws.access.ObjectPrivilegeInfo> list =
					mapObjectPrivilege.get(privilegeInfo.getObjectType() + ";" + privilegeInfo.getObjectId());
			if(list == null)
				list = new ArrayList<com.clustercontrol.ws.access.ObjectPrivilegeInfo>();
			list.add(privilegeInfo);
			mapObjectPrivilege.put(privilegeInfo.getObjectType() + ";" + privilegeInfo.getObjectId(), list);
		}
		
		for(Entry<String, List<com.clustercontrol.ws.access.ObjectPrivilegeInfo>> entry : mapObjectPrivilege.entrySet()){
			String[] object = entry.getKey().split(";");
			try {
				AccessEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).replaceObjectPrivilegeInfo(
						object[0],
						object[1],
						new ArrayList<com.clustercontrol.ws.access.ObjectPrivilegeInfo>());
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + object[0] + "-" + object[1]);
			} catch (HinemosUnknown_Exception e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (InvalidRole_Exception e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (InvalidUserPass_Exception e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				continue;
			}
		}
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ClearCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Clear PlatformObjectPrivilege ");
		return ret;
	}

	/**
	 * 情報をマネージャから取得し、XMLに出力します。<BR>
	 * 
	 * @param 出力するXMLファイル
	 * @return 終了コード
	 */
	@ExportMethod
	public int exportAccess(String xmlObjectPrivilege) {

		log.debug("Start Export PlatformObjectPrivilege ");

		int ret = 0;
		List<com.clustercontrol.ws.access.ObjectPrivilegeInfo> objectPrivilegeList = null;
		ObjectPrivilege objectPrivilege = new ObjectPrivilege();

		// オブジェクト権限情報一覧の取得
		try {
			objectPrivilegeList = AccessEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getObjectPrivilegeInfoList(null);
			Collections.sort(objectPrivilegeList, new Comparator<com.clustercontrol.ws.access.ObjectPrivilegeInfo>() {
				@Override
				public int compare(
						com.clustercontrol.ws.access.ObjectPrivilegeInfo info1,
						com.clustercontrol.ws.access.ObjectPrivilegeInfo info2) {
					int ret = info1.getObjectType().compareTo(info2.getObjectType());
					if(ret != 0){
						return ret;
					} else {
						ret = info1.getObjectId().compareTo(info2.getObjectId());
						if(ret != 0){
							return ret;
						} else {
							ret = info1.getRoleId().compareTo(info2.getRoleId());
							if(ret != 0){
								return ret;
							} else {
								return info1.getObjectPrivilege().compareTo(info2.getObjectPrivilege());
							}
						}
					}
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Export PlatformObjectPrivilege (Error)");
			return ret;
		}
		
		// オブジェクト権限情報の取得
		for (int j = 0; j < objectPrivilegeList.size(); j++) {
			com.clustercontrol.ws.access.ObjectPrivilegeInfo privilegeInfo = objectPrivilegeList.get(j);
			try {
				objectPrivilege.addObjectPrivilegeInfo(ObjectPrivilegeConv.convObjectPrivilegeDto2Xml(privilegeInfo));
				log.info(Messages.getString(
						"SettingTools.ExportSucceeded") + " : " + privilegeInfo.getObjectType() + "-" + privilegeInfo.getObjectId());
			} catch (Exception e) {
				log.error(Messages.getString(
						"SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			}
		}

		// XMLファイルに出力
		try {
			//オブジェクト権限情報
			objectPrivilege.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			objectPrivilege.setSchemaInfo(ObjectPrivilegeConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlObjectPrivilege);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				objectPrivilege.marshal(osw);
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
		log.debug("End Export PlatformObjectPrivilege ");
		return ret;
	}
	
	/**
	 * XMLの情報をマネージャに投入します。<BR>
	 * 
	 * @param 入力するXMLファイル
	 * @return 終了コード
	 */
	@ImportMethod
	public int importAccess(String xmlObjectPrivilege) {

		log.debug("Start Import PlatformObjectPrivilege ");
		
		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			log.debug("End Import PlatformObjectPrivilege (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		int ret = 0;
		ObjectPrivilege objectPrivilege = null;
		
		// オブジェクト権限情報をXMLファイルからの読み込み
		try {
			objectPrivilege = ObjectPrivilege.unmarshal(
					new InputStreamReader(new FileInputStream(xmlObjectPrivilege), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformObjectPrivilege (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(objectPrivilege.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		// オブジェクト権限情報の登録
		Map<List<String>, List<com.clustercontrol.ws.access.ObjectPrivilegeInfo>> objPrivilMapXML = new LinkedHashMap<>();
		for (int i = 0; i < objectPrivilege.getObjectPrivilegeInfoCount(); i++) {
			ObjectPrivilegeInfo privilegeInfo = objectPrivilege.getObjectPrivilegeInfo(i);

			List<String> key = new ArrayList<>(2);
			key.add(privilegeInfo.getObjectId());
			key.add(privilegeInfo.getObjectType());

			List<com.clustercontrol.ws.access.ObjectPrivilegeInfo> list = objPrivilMapXML.get(key);
			if(list == null){
				list = new ArrayList<com.clustercontrol.ws.access.ObjectPrivilegeInfo>();
			}
			list.add(ObjectPrivilegeConv.convObjectPrivilegeXml2Dto(privilegeInfo));
			objPrivilMapXML.put(key, list);
		}

		// Arrange conventional list as map with [objectId, objectType] as its keys for compare
		Map<List<String>, List<com.clustercontrol.ws.access.ObjectPrivilegeInfo>> objPrivilMap = new HashMap<>();
		try {
			List<com.clustercontrol.ws.access.ObjectPrivilegeInfo> tmpObjList = null;
			for(com.clustercontrol.ws.access.ObjectPrivilegeInfo objPrivil : AccessEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getObjectPrivilegeInfoList(null)){
				// Arrange
				List<String> key = new ArrayList<String>(3);
				key.add(objPrivil.getObjectId());
				key.add(objPrivil.getObjectType());
				tmpObjList = objPrivilMap.get(key);
				if(null == tmpObjList){
					tmpObjList = new ArrayList<com.clustercontrol.ws.access.ObjectPrivilegeInfo>();
					objPrivilMap.put( key, tmpObjList );
				}
				tmpObjList.add(objPrivil);
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			log.debug(e.getMessage(), e);
		}

		for(Entry<List<String>, List<com.clustercontrol.ws.access.ObjectPrivilegeInfo>> entry : objPrivilMapXML.entrySet()){
			String objectType = entry.getKey().get(1);
			String objectId = entry.getKey().get(0);
			String key = objectType + " " + objectId;

			try {
				if(!objPrivilMap.containsKey( entry.getKey())){
					// Insert as new
					AccessEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).replaceObjectPrivilegeInfo(objectType, objectId, entry.getValue());
					log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + key);
				}else{
					// 重複時、インポート処理方法を確認する
					if(!ImportProcessMode.isSameprocess()){
						String[] args = {key};
						ImportProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(null, Messages.getString("message.import.confirm2", args));
						ImportProcessMode.setProcesstype(dialog.open());
						ImportProcessMode.setSameprocess(dialog.getToggleState());
					}

					if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.UPDATE){
						AccessEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).replaceObjectPrivilegeInfo(objectType, objectId, entry.getValue());
						log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + key + " :"  + entry.getValue().get(0).getRoleId());
					} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
						log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + key + " :"  + entry.getValue().get(0).getRoleId());
					} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
						log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
						ret = SettingConstants.ERROR_INPROCESS;
						break;
					}
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
			} catch (PrivilegeDuplicate_Exception e) {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}

		checkDelete(objectPrivilege);

		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Import PlatformObjectPrivilege ");
		return ret;
	}
	
	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private static boolean checkSchemaVersion(com.clustercontrol.utility.settings.platform.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = ObjectPrivilegeConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo sci = ObjectPrivilegeConv.getSchemaVersion();
		
		return BaseAction.checkSchemaVersionResult(log, res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	public static int importAccessExtraction(String xmlObjectPrivilege, String objectType, List<String> objectIdList, Logger log) {
		log.debug("Start Import PlatformObjectPrivilege ");

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			log.debug("End Import PlatformObjectPrivilege (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		int ret = 0;
		ObjectPrivilege objectPrivilege = null;
		
		// オブジェクト権限情報をXMLファイルからの読み込み
		try {
			objectPrivilege = ObjectPrivilege.unmarshal(
					new InputStreamReader(new FileInputStream(xmlObjectPrivilege), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformObjectPrivilege (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(objectPrivilege.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		// オブジェクト権限情報の登録
		Map<List<String>, List<com.clustercontrol.ws.access.ObjectPrivilegeInfo>> objPrivilMapXML = new LinkedHashMap<>();
		for (int i = 0; i < objectPrivilege.getObjectPrivilegeInfoCount(); i++) {
			ObjectPrivilegeInfo privilegeInfo = objectPrivilege.getObjectPrivilegeInfo(i);
			if(objectIdList.contains(privilegeInfo.getObjectId()) && objectType.equals(privilegeInfo.getObjectType())){
				List<String> key = new ArrayList<>(2);
				key.add(privilegeInfo.getObjectId());
				key.add(privilegeInfo.getObjectType());
				List<com.clustercontrol.ws.access.ObjectPrivilegeInfo> list = objPrivilMapXML.get(key);
				if(list == null){
					list = new ArrayList<com.clustercontrol.ws.access.ObjectPrivilegeInfo>();
				}
				list.add(ObjectPrivilegeConv.convObjectPrivilegeXml2Dto(privilegeInfo));
				objPrivilMapXML.put(key, list);
			}
		}
		
		for(Entry<List<String>, List<com.clustercontrol.ws.access.ObjectPrivilegeInfo>> entry : objPrivilMapXML.entrySet()){
			String targetObjectType = entry.getKey().get(1);
			String targetObjectId = entry.getKey().get(0);
			String key = targetObjectType + " " + targetObjectId;

			try {
				AccessEndpointWrapper.getWrapper(
						UtilityManagerUtil.getCurrentManagerName()).replaceObjectPrivilegeInfo(
								targetObjectType, targetObjectId, entry.getValue());
				log.info(Messages.getString("message.import.succeeded.object.privilege") + " : " + key);
			} catch (HinemosUnknown_Exception e) {
				log.error(Messages.getString("message.import.failed.object.privilege") + " : " + HinemosMessage.replace(e.getMessage()));
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
			} catch (PrivilegeDuplicate_Exception e) {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		log.debug("End Import PlatformObjectPrivilege ");
		return ret;
	}
	
	
	/**
	 * 差分比較処理を行います。
	 * XMLファイル２つ（filePath1,filePath2）を比較する。
	 * 差分がある場合：差分をＣＳＶファイルで出力する。
	 * 差分がない場合：出力しない。
	 * 				   または、すでに存在している同一名のＣＳＶファイルを削除する。
	 *
	 * @param xmlObjectPrivilege1 XMLファイル名
	 * @param xmlObjectPrivilege2 XMLファイル名
	 * @return 終了コード
	 * @throws ConvertorException
	 */
	@DiffMethod
	public int diffXml(String xmlObjectPrivilege1, String xmlObjectPrivilege2) throws ConvertorException {
		log.debug("Search Differrence PlatformObjectPrivilege ");

		int ret = 0;
		// XMLファイルからの読み込み
		ObjectPrivilege objectPrivilege1 = null;
		ObjectPrivilege objectPrivilege2 = null;
		try {
			objectPrivilege1 = ObjectPrivilege.unmarshal(
					new InputStreamReader(new FileInputStream(xmlObjectPrivilege1), "UTF-8"));
			objectPrivilege2 = ObjectPrivilege.unmarshal(
					new InputStreamReader(new FileInputStream(xmlObjectPrivilege2), "UTF-8"));
			sort(objectPrivilege1);
			sort(objectPrivilege2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence PlatformObjectPrivilege (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(objectPrivilege1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(objectPrivilege2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(objectPrivilege1, objectPrivilege2, ObjectPrivilege.class, resultA);
			assert resultA.getResultBs().size() == 1;
			
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_1;
			}
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlObjectPrivilege2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(xmlObjectPrivilege2 + ".csv");
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
		
		log.debug("End Differrence PlatformObjectPrivilege");

		return ret;
	}
	
	private void sort(ObjectPrivilege objectPrivilege) {
		ObjectPrivilegeInfo[] infoList = objectPrivilege.getObjectPrivilegeInfo();
		Arrays.sort(infoList,
			new Comparator<ObjectPrivilegeInfo>() {
				@Override
				public int compare(ObjectPrivilegeInfo info1, ObjectPrivilegeInfo info2) {
					int ret = info1.getObjectType().compareTo(info2.getObjectType());
					if(ret != 0){
						return ret;
					} else {
						ret = info1.getObjectId().compareTo(info2.getObjectId());
						if(ret != 0){
							return ret;
						} else {
							ret = info1.getRoleId().compareTo(info2.getRoleId());
							if(ret != 0){
								return ret;
							} else {
								return info1.getObjectPrivilege().compareTo(info2.getObjectPrivilege());
							}
						}
					}
				}
			});
		objectPrivilege.setObjectPrivilegeInfo(infoList);
	}

	private void checkDelete(ObjectPrivilege xmlElements){
		// Get key list from DB. Use list to keep the sorting order
		List<List<String>> keysInDB = new ArrayList<>();
		try {
			for(com.clustercontrol.ws.access.ObjectPrivilegeInfo objPrivil : AccessEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getObjectPrivilegeInfoList(null)){
				// Array key pair
				List<String> key = new ArrayList<String>(2);
				key.add(objPrivil.getObjectId());   // [0]: objectId
				key.add(objPrivil.getObjectType()); // [1]: objectType

				if(!keysInDB.contains( key )){
					keysInDB.add(key);
				}
			}
		}
		catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			log.debug(e.getMessage(), e);
		}

		if(keysInDB.size() == 0){
			return;
		}

		// Get key list from XML
		List<List<String>> keysInXML = new ArrayList<>();
		for(ObjectPrivilegeInfo objPrivilXML : xmlElements.getObjectPrivilegeInfo()){
			// Array key pair
			List<String> key = new ArrayList<String>(2);
			key.add(objPrivilXML.getObjectId());   // [0]: objectId
			key.add(objPrivilXML.getObjectType()); // [1]: objectType

			if(!keysInXML.contains( key )){
				keysInXML.add(key);
			}
		}

		for(List<String> key : keysInDB){
			if(!keysInXML.contains( key )){
				String objId = key.get(0);
				String objType = key.get(1);

				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {objType + " " + objId};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}

				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					try {
						// Replace with an empty one
						AccessEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).replaceObjectPrivilegeInfo(objType, objId, new ArrayList<com.clustercontrol.ws.access.ObjectPrivilegeInfo>());
						log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + objType + " " + objId);
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + objType + " " + objId);
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					log.info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
					return;
				}
			}
		}
	}
	public Logger getLogger() {
		return log;
	}
}
