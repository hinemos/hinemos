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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


import org.apache.log4j.Logger;
import org.openapitools.client.model.ImportObjectPrivilegeInfoRecordRequest;
import org.openapitools.client.model.ImportObjectPrivilegeInfoRequest;
import org.openapitools.client.model.ImportObjectPrivilegeInfoResponse;
import org.openapitools.client.model.ObjectPrivilegeInfoRequestP1;
import org.openapitools.client.model.ObjectPrivilegeInfoResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.ReplaceObjectPrivilegeRequest;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;

import com.clustercontrol.accesscontrol.util.AccessRestClientWrapper;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
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
		List<ObjectPrivilegeInfoResponse> objectPrivilegeList = null;
		
		// オブジェクト権限情報一覧の取得
		try {
			objectPrivilegeList = AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getObjectPrivilegeInfoList(null,null,null,null);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear PlatformObjectPrivilege (Error)");
			return ret;
		}
		
		// オブジェクト権限情報の削除
		HashMap<String, List<ObjectPrivilegeInfoResponse>> mapObjectPrivilege =
				new HashMap<String, List<ObjectPrivilegeInfoResponse>>();
		for (int i = 0; i < objectPrivilegeList.size(); i++) {
			ObjectPrivilegeInfoResponse privilegeInfo = objectPrivilegeList.get(i);
			List<ObjectPrivilegeInfoResponse> list =
					mapObjectPrivilege.get(privilegeInfo.getObjectType() + ";" + privilegeInfo.getObjectId());
			if(list == null)
				list = new ArrayList<ObjectPrivilegeInfoResponse>();
			list.add(privilegeInfo);
			mapObjectPrivilege.put(privilegeInfo.getObjectType() + ";" + privilegeInfo.getObjectId(), list);
		}
		
		for(Entry<String, List<ObjectPrivilegeInfoResponse>> entry : mapObjectPrivilege.entrySet()){
			String[] object = entry.getKey().split(";");
			try {
				ReplaceObjectPrivilegeRequest reqDto = new ReplaceObjectPrivilegeRequest();
				reqDto.setObjectType(object[0]);
				reqDto.setObjectId(object[1]);
				reqDto.setObjectPrigilegeInfoList(new ArrayList<ObjectPrivilegeInfoRequestP1>());
				AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).replaceObjectPrivilege(reqDto);
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + object[0] + " " + object[1]);
			} catch (HinemosUnknown e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidRole e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidUserPass e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (RestConnectFailed e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
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
		List<ObjectPrivilegeInfoResponse> objectPrivilegeList = null;
		ObjectPrivilege objectPrivilege = new ObjectPrivilege();

		// オブジェクト権限情報一覧の取得
		try {
			objectPrivilegeList = AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getObjectPrivilegeInfoList(null,null,null,null);
			Collections.sort(objectPrivilegeList, new Comparator<ObjectPrivilegeInfoResponse>() {
				@Override
				public int compare(
						ObjectPrivilegeInfoResponse info1,
						ObjectPrivilegeInfoResponse info2) {
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
			ObjectPrivilegeInfoResponse privilegeInfo = objectPrivilegeList.get(j);
			try {
				objectPrivilege.addObjectPrivilegeInfo(ObjectPrivilegeConv.convObjectPrivilegeDto2Xml(privilegeInfo));
				log.info(Messages.getString(
						"SettingTools.ExportSucceeded") + " : " + privilegeInfo.getObjectType() + " " + privilegeInfo.getObjectId());
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
			objectPrivilege = XmlMarshallUtil.unmarshall(ObjectPrivilege.class,
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
		//  <ID,TYPE> 毎に リストを集約。
		final Map<ObjectPrivilegeInfoKey, List<ObjectPrivilegeInfoResponse>> objPrivilMapXML = new LinkedHashMap<>();
		for (int i = 0; i < objectPrivilege.getObjectPrivilegeInfoCount(); i++) {
			ObjectPrivilegeInfo privilegeInfo = objectPrivilege.getObjectPrivilegeInfo(i);
			ObjectPrivilegeInfoKey key  = new ObjectPrivilegeInfoKey();
			key.setObjectID(privilegeInfo.getObjectId());
			key.setObjectType(privilegeInfo.getObjectType());
			List<ObjectPrivilegeInfoResponse> list = objPrivilMapXML.get(key);
			if(list == null){
				list = new ArrayList<ObjectPrivilegeInfoResponse>();
			}
			list.add(ObjectPrivilegeConv.convObjectPrivilegeXml2Dto(privilegeInfo));
			objPrivilMapXML.put(key, list);
		}

		//  重複確認しつつDTO形式に変換
		ImportRecordConfirmer<ObjectPrivilegeInfoKey, ImportObjectPrivilegeInfoRecordRequest, ObjectPrivilegeInfoKey> confirmer = new ImportRecordConfirmer<ObjectPrivilegeInfoKey, ImportObjectPrivilegeInfoRecordRequest, ObjectPrivilegeInfoKey >(
				log , objPrivilMapXML.keySet().toArray( new ObjectPrivilegeInfoKey[0]) ) {
			@Override
			protected ImportObjectPrivilegeInfoRecordRequest convertDtoXmlToRestReq(ObjectPrivilegeInfoKey xmlDto ){
				ImportObjectPrivilegeInfoRecordRequest dtoRec = new ImportObjectPrivilegeInfoRecordRequest();
				dtoRec.setImportKeyValue(xmlDto.getObjectType() + " " + xmlDto.getObjectID());
				dtoRec.setImportData(new ReplaceObjectPrivilegeRequest());
				dtoRec.getImportData().setObjectId(xmlDto.getObjectID());
				dtoRec.getImportData().setObjectType(xmlDto.getObjectType());
				dtoRec.getImportData().setObjectPrigilegeInfoList(new ArrayList<ObjectPrivilegeInfoRequestP1>());
				for( ObjectPrivilegeInfoResponse entryValueRec: objPrivilMapXML.get(xmlDto)){
					ObjectPrivilegeInfoRequestP1 subListRec = new ObjectPrivilegeInfoRequestP1() ;
					subListRec.setObjectPrivilege(entryValueRec.getObjectPrivilege());
					subListRec.setRoleId(entryValueRec.getRoleId());
					dtoRec.getImportData().getObjectPrigilegeInfoList().add(subListRec);
				}
				return dtoRec;
			}

			@Override
			protected Set<ObjectPrivilegeInfoKey> getExistIdSet() throws Exception {
				Set<ObjectPrivilegeInfoKey> retSet = new HashSet<ObjectPrivilegeInfoKey>();
				List<ObjectPrivilegeInfoResponse> objectPrivilegeList = AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getObjectPrivilegeInfoList(null,null,null,null);
				for (ObjectPrivilegeInfoResponse rec : objectPrivilegeList) {
					ObjectPrivilegeInfoKey set = new ObjectPrivilegeInfoKey();
					set.setObjectType(rec.getObjectType());
					set.setObjectID(rec.getObjectId());
					retSet.add(set);
				}
				return retSet;
			}

			@Override
			protected boolean isLackRestReq(ImportObjectPrivilegeInfoRecordRequest restDto) {
				return (restDto == null);
			}

			@Override
			protected String getKeyValueXmlDto(ObjectPrivilegeInfoKey xmlDto) {
				String ret = xmlDto.getObjectType() +" "+xmlDto.objectID;
				return ret;
			}

			@Override
			protected ObjectPrivilegeInfoKey getId(ObjectPrivilegeInfoKey xmlDto) {
				return xmlDto;
			}

			@Override
			protected void setNewRecordFlg(ImportObjectPrivilegeInfoRecordRequest restDto, boolean flag) {
				restDto.setIsNewRecord(flag);
			}

		};
		int confirmRet = confirmer.executeConfirm();
		if (confirmRet != SettingConstants.SUCCESS && confirmRet != SettingConstants.ERROR_CANCEL) {
			// 変換エラーならUnmarshalXml扱いで処理打ち切り(キャンセルはキャンセル以前の選択結果を反映するので次に進む)
			log.warn(Messages.getString("SettingTools.UnmarshalXmlFailed"));
			return confirmRet;
		}

		//  DTOを元にインポート
		ImportObjectPrivilegeInfoClientController importController =  new ImportObjectPrivilegeInfoClientController (log, Messages.getString("object.privilege"),confirmer.getImportRecDtoList(),true);
		int controllRet = importController.importExecute();
		if( controllRet != 0){
			ret = controllRet;
		}
		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
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
			objectPrivilege = XmlMarshallUtil.unmarshall(ObjectPrivilege.class,
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
		//  <ID,TYPE> 毎に リストを集約。対象リスト（objectType objectIdList）との突合せもあわせて行う
		Map<ObjectPrivilegeInfoKey, List<ObjectPrivilegeInfoResponse>> objPrivilMapXML = new LinkedHashMap<>();
		for (int i = 0; i < objectPrivilege.getObjectPrivilegeInfoCount(); i++) {
			ObjectPrivilegeInfo privilegeInfo = objectPrivilege.getObjectPrivilegeInfo(i);
			if(objectIdList.contains(privilegeInfo.getObjectId()) && objectType.equals(privilegeInfo.getObjectType())){
				ObjectPrivilegeInfoKey key = new ObjectPrivilegeInfoKey();
				key.setObjectID(privilegeInfo.getObjectId());
				key.setObjectType(privilegeInfo.getObjectType());
				List<ObjectPrivilegeInfoResponse> list = objPrivilMapXML.get(key);
				if(list == null){
					list = new ArrayList<ObjectPrivilegeInfoResponse>();
				}
				list.add(ObjectPrivilegeConv.convObjectPrivilegeXml2Dto(privilegeInfo));
				objPrivilMapXML.put(key, list);
			}
		}

		//  集約したデータをDTOに合わせた形に変換
		List<ImportObjectPrivilegeInfoRecordRequest> reqList = new ArrayList<ImportObjectPrivilegeInfoRecordRequest>();
		for (Entry<ObjectPrivilegeInfoKey, List<ObjectPrivilegeInfoResponse>> entry : objPrivilMapXML.entrySet()) {
			ImportObjectPrivilegeInfoRecordRequest dtoRec = new ImportObjectPrivilegeInfoRecordRequest();
			dtoRec.setImportKeyValue(entry.getKey().getObjectType() + " " + entry.getKey().getObjectID());
			dtoRec.setIsNewRecord(false);
			dtoRec.setImportData(new ReplaceObjectPrivilegeRequest());
			dtoRec.getImportData().setObjectId(entry.getKey().getObjectID());
			dtoRec.getImportData().setObjectType(entry.getKey().getObjectType());
			dtoRec.getImportData().setObjectPrigilegeInfoList(new ArrayList<ObjectPrivilegeInfoRequestP1>());
			for( ObjectPrivilegeInfoResponse entryValueRec: entry.getValue()){
				ObjectPrivilegeInfoRequestP1 subListRec = new ObjectPrivilegeInfoRequestP1() ;
				subListRec.setObjectPrivilege(entryValueRec.getObjectPrivilege());
				subListRec.setRoleId(entryValueRec.getRoleId());
				dtoRec.getImportData().getObjectPrigilegeInfoList().add(subListRec);
			}
			reqList.add(dtoRec);
		}
		
		//  DTOを元にインポート
		ImportObjectPrivilegeInfoClientController importController =  new ImportObjectPrivilegeInfoClientController (log, Messages.getString("object.privilege"),reqList,true);
		ret = importController.importExecute();

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
			objectPrivilege1 = XmlMarshallUtil.unmarshall(ObjectPrivilege.class,
					new InputStreamReader(new FileInputStream(xmlObjectPrivilege1), "UTF-8"));
			objectPrivilege2 = XmlMarshallUtil.unmarshall(ObjectPrivilege.class,
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
			for(ObjectPrivilegeInfoResponse objPrivil : AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getObjectPrivilegeInfoList(null,null,null,null)){
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
						ReplaceObjectPrivilegeRequest reqDto = new ReplaceObjectPrivilegeRequest();
						reqDto.setObjectType(objType);
						reqDto.setObjectId(objId);
						reqDto.setObjectPrigilegeInfoList(new ArrayList<ObjectPrivilegeInfoRequestP1>());
						AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).replaceObjectPrivilege(reqDto);
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

	/**
	 * ObjectPrivilegeInfoの集約キーを表すクラス
	 * 
	 * MapのKeyとして扱うので hashcode と equalsは メンバの値が同じなら、等価になるようにしておく
	 */
	private static class ObjectPrivilegeInfoKey {
		private String objectType;
		private String objectID;

		public String getObjectType() {
			return objectType;
		}
		public void setObjectType(String objectType) {
			this.objectType = objectType;
		}
		public String getObjectID() {
			return objectID;
		}
		public void setObjectID(String objectID) {
			this.objectID = objectID;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((objectID == null) ? 0 : objectID.hashCode());
			result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ObjectPrivilegeInfoKey other = (ObjectPrivilegeInfoKey) obj;
			if (objectID == null) {
				if (other.objectID != null)
					return false;
			} else if (!objectID.equals(other.objectID))
				return false;
			if (objectType == null) {
				if (other.objectType != null)
					return false;
			} else if (!objectType.equals(other.objectType))
				return false;
			return true;
		}
		
		
	}
	
	/**
	 * インポート向けのレコード登録用クラス
	 * 
	 */
	private static class ImportObjectPrivilegeInfoClientController extends ImportClientController<ImportObjectPrivilegeInfoRecordRequest, ImportObjectPrivilegeInfoResponse, RecordRegistrationResponse>{
		public ImportObjectPrivilegeInfoClientController(Logger logger, String importInfoName, List<ImportObjectPrivilegeInfoRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);			
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportObjectPrivilegeInfoResponse importResponse) {
			return importResponse.getResultList();
		};
		@Override
		protected Boolean getOccurException(ImportObjectPrivilegeInfoResponse importResponse) {
			return importResponse.getIsOccurException();
		};
		@Override
		protected String getReqKeyValue(ImportObjectPrivilegeInfoRecordRequest importRec) {
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
		protected ImportObjectPrivilegeInfoResponse callImportWrapper(List<ImportObjectPrivilegeInfoRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportObjectPrivilegeInfoRequest reqDto = new ImportObjectPrivilegeInfoRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			UtilityRestClientWrapper utilityWrapper = UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
			return utilityWrapper.importObjectPrivilegeInfo(reqDto);
		}
		@Override
		protected String getRestExceptionMessage(RecordRegistrationResponse responseRec) {
			if (responseRec.getExceptionInfo() != null) {
				return responseRec.getExceptionInfo().getException() +":"+ responseRec.getExceptionInfo().getMessage();
			}
			return null;
		};
	}
}
