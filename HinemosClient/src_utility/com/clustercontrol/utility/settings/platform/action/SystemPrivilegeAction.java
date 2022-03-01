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


import org.apache.log4j.Logger;
import org.openapitools.client.model.ImportSystemPrivilegeInfoRecordRequest;
import org.openapitools.client.model.RecordRegistrationResponse;

import org.openapitools.client.model.ImportSystemPrivilegeInfoRequest;
import org.openapitools.client.model.ImportSystemPrivilegeInfoResponse;
import org.openapitools.client.model.ReplaceSystemPrivilegeWithRoleRequest;
import org.openapitools.client.model.RoleInfoResponse;
import org.openapitools.client.model.SystemPrivilegeInfoRequestP1;
import org.openapitools.client.model.SystemPrivilegeInfoResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.util.AccessRestClientWrapper;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.UnEditableRole;
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
import com.clustercontrol.utility.settings.platform.conv.SystemPrivilegeConv;
import com.clustercontrol.utility.settings.platform.xml.SystemPrivilege;
import com.clustercontrol.utility.settings.platform.xml.SystemPrivilegeInfo;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.ImportClientController;
import com.clustercontrol.utility.util.OpenApiEnumConverter;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;
import com.clustercontrol.utility.util.XmlMarshallUtil;

/**
 * ユーザ定義情報をインポート・エクスポート・削除するアクションクラス<br>
 * 
 * @version 6.1.0
 * @since 1.0.0
 * 
 */
public class SystemPrivilegeAction {

	protected static Logger log = Logger.getLogger(SystemPrivilegeAction.class);

	public SystemPrivilegeAction() throws ConvertorException {
		super();
	}
	
	/**
	 * 情報をマネージャから削除します。<BR>
	 * 
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearAccess() {

		log.debug("Start Clear PlatformSystemPrivilege ");
		int ret = 0;
		List<RoleInfoResponse> roleList = null;
		
		// ロール情報一覧の取得
		try {
			roleList = AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getRoleInfoList();
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear PlatformSystemPrivilege (Error)");
			return ret;
		}
		
		// ロール情報の削除
		String roleId = null;
		for (int i = 0; i < roleList.size(); i++) {
			roleId = roleList.get(i).getRoleId();
			try {
				List<SystemPrivilegeInfoRequestP1> infos = new ArrayList<SystemPrivilegeInfoRequestP1>();
				SystemPrivilegeInfoRequestP1 info = new SystemPrivilegeInfoRequestP1();
				info.setSystemFunction(SystemPrivilegeInfoRequestP1.SystemFunctionEnum.REPOSITORY );
				info.setSystemPrivilege(SystemPrivilegeInfoRequestP1.SystemPrivilegeEnum.READ);
				infos.add(info);
				ReplaceSystemPrivilegeWithRoleRequest reqDto = new ReplaceSystemPrivilegeWithRoleRequest();
				reqDto.setSystemPrivilegeList(infos);
				AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).replaceSystemPrivilegeWithRole(roleId, reqDto);
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + roleId);
			} catch (UnEditableRole e) {
				// 編集不可なロールはスキップする
				log.info(Messages.getString("SettingTools.SkipSystemRole") + " : " + roleId);
			} catch (RestConnectFailed e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ClearCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Clear PlatformSystemPrivilege ");
		return ret;
	}

	/**
	 * 情報をマネージャから取得し、XMLに出力します。<BR>
	 * 
	 * @param 出力するXMLファイル
	 * @return 終了コード
	 */
	@ExportMethod
	public int exportAccess(String xmlSystemPrivilege) {

		log.debug("Start Export PlatformSystemPrivilege ");
		
		int ret = 0;
		List<RoleInfoResponse> roleList = null;
		List<SystemPrivilegeInfoResponse> systemPrivilegeList = null;
		
		// ロール情報一覧の取得
		try {
			roleList = AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getRoleInfoList();
			Collections.sort(roleList, new Comparator<RoleInfoResponse>() {
				@Override
				public int compare(
						RoleInfoResponse info1,
						RoleInfoResponse info2) {
					return info1.getRoleId().compareTo(info2.getRoleId());
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Export PlatformSystemPrivilege (Error)");
			return ret;
		}
		
		SystemPrivilege systemPrivilege = new SystemPrivilege();
		
		// ロール情報の取得
		for (int i = 0; i < roleList.size(); i++) {
			RoleInfoResponse roleInfo = roleList.get(i);
			try {
				// システム権限情報一覧の取得
				try {
					systemPrivilegeList = AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getSystemPrivilegeInfoListByRoleId(roleInfo.getRoleId());
					Collections.sort(systemPrivilegeList, new Comparator<SystemPrivilegeInfoResponse>() {
						@Override
						public int compare(
								SystemPrivilegeInfoResponse info1,
								SystemPrivilegeInfoResponse info2) {
							String info1SysFunction = OpenApiEnumConverter.enumToString(info1.getSystemFunction());
							String info2SysFunction = OpenApiEnumConverter.enumToString(info2.getSystemFunction());
							int ret = info1SysFunction.compareTo(info2SysFunction);
							if(ret != 0){
								return ret;
							} else {
								String info1SysPrivilege = OpenApiEnumConverter.enumToString(info1.getSystemPrivilege());
								String info2SysPrivilege = OpenApiEnumConverter.enumToString(info2.getSystemPrivilege());
								return info1SysPrivilege.compareTo(info2SysPrivilege);
							}
						}
					});
					for (int j = 0; j < systemPrivilegeList.size(); j++) {
						SystemPrivilegeInfoResponse privilegeInfo = systemPrivilegeList.get(j);
						try {
							systemPrivilege.addSystemPrivilegeInfo(SystemPrivilegeConv.convSystemPrivilegeDto2Xml(roleInfo.getRoleId(), privilegeInfo));
							log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + roleInfo.getRoleId());
						} catch (Exception e) {
							log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
							ret = SettingConstants.ERROR_INPROCESS;
							break;
						}
					}
				} catch (Exception e) {
					log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
					ret=SettingConstants.ERROR_INPROCESS;
					log.debug("End Export PlatformSystemPrivilege (Error)");
					return ret;
				}
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			}
		}

		// XMLファイルに出力
		try {
			//システム権限情報
			systemPrivilege.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			systemPrivilege.setSchemaInfo(SystemPrivilegeConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlSystemPrivilege);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				systemPrivilege.marshal(osw);
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
		log.debug("End Export PlatformSystemPrivilege ");
		return ret;
	}

	/**
	 * XMLの情報をマネージャに投入します。<BR>
	 * 
	 * @param 入力するXMLファイル
	 * @return 終了コード
	 */
	@ImportMethod
	public int importAccess(String xmlSystemPrivilege) {

		log.debug("Start Import PlatformSystemPrivilege ");

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			getLogger().debug("End Import PlatformSystemPrivilege (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}

		int ret = 0;
		SystemPrivilege systemPrivilege = null;

		// システム権限情報をXMLファイルからの読み込み
		try {
			systemPrivilege = XmlMarshallUtil.unmarshall(SystemPrivilege.class,
					new InputStreamReader(new FileInputStream(xmlSystemPrivilege), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformSystemPrivilege (Error)");
			return ret;
		}

		// スキーマのバージョンチェック
		if(!checkSchemaVersion(systemPrivilege.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		// システム権限情報の登録. Key = [roldId> SystemFunction]
		Map<String, Map<String, List<SystemPrivilegeInfoResponse>>> sysPrivMapXML = new LinkedHashMap<>();
		try {
			for (int i = 0; i < systemPrivilege.getSystemPrivilegeInfoCount(); i++) {
				SystemPrivilegeInfo privilegeInfo = systemPrivilege.getSystemPrivilegeInfo(i);
				SystemPrivilegeInfoResponse dto = SystemPrivilegeConv.convSystemPrivilegeXml2Dto(privilegeInfo);
				
				Map<String, List<SystemPrivilegeInfoResponse>> subMap;
				subMap = sysPrivMapXML.get(privilegeInfo.getRoleId());
				if(null == subMap){
					subMap = new LinkedHashMap<>();
	
					List<SystemPrivilegeInfoResponse> list = new ArrayList<SystemPrivilegeInfoResponse>();
					list.add(dto);
	
					subMap.put(privilegeInfo.getSystemFunction(), list);
					sysPrivMapXML.put(privilegeInfo.getRoleId(), subMap);
				}else{
					List<SystemPrivilegeInfoResponse> list = subMap.get(privilegeInfo.getSystemFunction());
					if(null == list){
						list = new ArrayList<SystemPrivilegeInfoResponse>();
						subMap.put(privilegeInfo.getSystemFunction(), list);
					}
					list.add(dto);
				}
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformSystemPrivilege (Error)");
			return ret;
		}

		//[ロールID+機能] 毎 の 重複確認 とDto変換
		Map<String,List<String>> resultMessageMap = new HashMap<String,List<String>>();
		List<ImportSystemPrivilegeInfoRecordRequest> dtoList = new ArrayList<ImportSystemPrivilegeInfoRecordRequest>(); 
		boolean isBorken = false; // loop control
		for(Entry<String, Map<String, List<SystemPrivilegeInfoResponse>>> entryByRole : sysPrivMapXML.entrySet()){
			String roleId = entryByRole.getKey();
			List<SystemPrivilegeInfoResponse> sysPrivLstByRole = null;
			//ロールID毎の情報を取得
			try {
				sysPrivLstByRole = AccessRestClientWrapper
						.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getSystemPrivilegeInfoListByRoleId(roleId);
				
				ImportSystemPrivilegeInfoRecordRequest dtoRec = new ImportSystemPrivilegeInfoRecordRequest();
				dtoRec.setImportData(new ReplaceSystemPrivilegeWithRoleRequest());
				dtoRec.setRoleId(roleId);
				dtoRec.setImportKeyValue(roleId);
				
				if(null == sysPrivLstByRole || 0 == sysPrivLstByRole.size()){
					// 該当のロールIDのシステム権限が存在しないので すべて追加
					dtoRec.getImportData().setSystemPrivilegeList(getImportRecData(entryByRole.getValue()));
					dtoList.add(dtoRec);
					List<String> resultMsgs = new ArrayList<>();
					for(Entry<String, List<SystemPrivilegeInfoResponse>> entry : entryByRole.getValue().entrySet()){
						String sysFunc = entry.getKey();
						resultMsgs.add( Messages.getString("SettingTools.ImportSucceeded") + " : " +  roleId + " " + sysFunc );
					}
					resultMessageMap.put(roleId,resultMsgs);
				}
				else{
					// 該当のロールIDのシステム権限について 機能毎で重複確認
	
					// First, reform to map
					Map<String, List<SystemPrivilegeInfoResponse>> subMapDB = new HashMap<>();
					for(SystemPrivilegeInfoResponse info : sysPrivLstByRole){
						String sysFunc = OpenApiEnumConverter.enumToString(info.getSystemFunction());
						List<SystemPrivilegeInfoResponse> list = subMapDB.get(sysFunc);
						if(null == list){
							list = new ArrayList<>();
							subMapDB.put(sysFunc, list);
						}
						//リポジトリ - 参照 はインポート対象外とする
						if(!(sysFunc.equals(FunctionConstant.REPOSITORY) && 
								info.getSystemPrivilege().equals(SystemPrivilegeInfoResponse.SystemPrivilegeEnum.READ)))
							list.add(info);
					}
	
					List<String> resultMsgs = new ArrayList<>();
					boolean updated = false;
					for(Entry<String, List<SystemPrivilegeInfoResponse>> entry : entryByRole.getValue().entrySet()){
						String sysFunc = entry.getKey();
	
						if(null == subMapDB.get(sysFunc) || 0 == subMapDB.get(sysFunc).size()){
							// Append if not found in DB
							subMapDB.put( sysFunc, entry.getValue() );
							updated = true;
							resultMsgs.add(Messages.getString("SettingTools.ImportSucceeded") + " : " + roleId + " " + sysFunc);
						}else{
							// 重複時、インポート処理方法を確認する
							if(!ImportProcessMode.isSameprocess()){
								UtilityProcessDialog dialog = UtilityDialogInjector.createImportProcessDialog(
										null, Messages.getString("message.import.confirm2", new String[]{roleId + " " + sysFunc}));
								ImportProcessMode.setProcesstype(dialog.open());
								ImportProcessMode.setSameprocess(dialog.getToggleState());
							}
			
							if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.UPDATE){
								subMapDB.put( sysFunc, entry.getValue());
								updated = true;
								resultMsgs.add(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + roleId + " " + sysFunc);
							} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
								resultMsgs.add(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + roleId + " " + sysFunc);
							} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
								ret = SettingConstants.ERROR_INPROCESS;
								isBorken = true;
								break;
							}
						}
					}
					if(updated){
						dtoRec.getImportData().setSystemPrivilegeList(getImportRecData(subMapDB));
						dtoList.add(dtoRec);
						resultMessageMap.put(roleId,resultMsgs);
					}
					// Break again after all
					if(isBorken){
						break;
					}
				}
			} catch (HinemosUnknown e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidRole e) {
				log.error(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidUserPass e) {
				log.error(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (RestConnectFailed e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
				
		// 更新単位の件数毎にインポートメソッドを呼び出し、結果をログ出力
		// API異常発生時はそこで中断、レコード個別の異常発生時はユーザ選択次第で続行
		ImportClientController<ImportSystemPrivilegeInfoRecordRequest, ImportSystemPrivilegeInfoResponse, RecordRegistrationResponse> importController = new ImportClientController<ImportSystemPrivilegeInfoRecordRequest, ImportSystemPrivilegeInfoResponse, RecordRegistrationResponse>(
				log, Messages.getString("platform.accesscontrol.system.privilege"),dtoList,true) {
			@Override
			protected List<RecordRegistrationResponse> getResRecList(ImportSystemPrivilegeInfoResponse importResponse) {
				return importResponse.getResultList();
			};
			@Override
			protected Boolean getOccurException(ImportSystemPrivilegeInfoResponse importResponse) {
				return importResponse.getIsOccurException();
			};
			@Override
			protected String getReqKeyValue(ImportSystemPrivilegeInfoRecordRequest importRec) {
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
			protected ImportSystemPrivilegeInfoResponse callImportWrapper(List<ImportSystemPrivilegeInfoRecordRequest> importRecList)
					throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
				ImportSystemPrivilegeInfoRequest reqDto = new ImportSystemPrivilegeInfoRequest();
				reqDto.setRecordList(importRecList);
				reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
				return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importSystemPrivilegeInfo(reqDto);
			}
			@Override
			protected String getRestExceptionMessage(RecordRegistrationResponse responseRec) {
				if (responseRec.getExceptionInfo() != null) {
					return responseRec.getExceptionInfo().getException() +":"+ responseRec.getExceptionInfo().getMessage();
				}
				return null;
			};
			@Override
			protected boolean isResSkip( RecordRegistrationResponse responseRec ){
				if ( responseRec.getExceptionInfo() != null) {
					if( responseRec.getExceptionInfo().getException().equals(UnEditableRole.class.getName()) ){
						//編集不可によるエラーはskip扱いとする。
						return true;
					} 
				}
				return false;
			}
			@Override
			protected void setResultLog( RecordRegistrationResponse responseRec ){
				if ( responseRec.getExceptionInfo() != null) {
					if( responseRec.getExceptionInfo().getException().equals(UnEditableRole.class.getName()) ){
						//編集不可によるエラーは専用のログを出力する。
						log.info(Messages.getString("SettingTools.SkipSystemRole") + " : " + this.importInfoName + ":"
								+ responseRec.getImportKeyValue());
						return;
					}
				}
				String keyValue = getResKeyValue(responseRec);
				if ( isResNormal(responseRec) ) {
					//正常終了時は重複確認時の選択に戻づくメッセージを出力
					for (String message : resultMessageMap.get(keyValue)) {
						log.info(message);
					}
				} else {
					log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + this.importInfoName + ":" + keyValue + " : "
							+ HinemosMessage.replace(getRestExceptionMessage(responseRec)));
				}
			}
		};
		ret = importController.importExecute();

		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		checkDelete(sysPrivMapXML);

		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Import PlatformSystemPrivilege ");
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
		int res = SystemPrivilegeConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo sci = SystemPrivilegeConv.getSchemaVersion();
		
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	/**
	 * 差分比較処理を行います。
	 * XMLファイル２つ（filePath1,filePath2）を比較する。
	 * 差分がある場合：差分をＣＳＶファイルで出力する。
	 * 差分がない場合：出力しない。
	 * 				   または、すでに存在している同一名のＣＳＶファイルを削除する。
	 *
	 * @param xmlSystemPrivilege1 XMLファイル名
	 * @param xmlSystemPrivilege2 XMLファイル名
	 * @return 終了コード
	 * @throws ConvertorException
	 */
	@DiffMethod
	public int diffXml(String xmlSystemPrivilege1, String xmlSystemPrivilege2) throws ConvertorException {
		log.debug("Search Differrence PlatformSystemPrivilege ");

		int ret = 0;
		// XMLファイルからの読み込み
		SystemPrivilege systemPrivilege1 = null;
		SystemPrivilege systemPrivilege2 = null;
		try {
			systemPrivilege1 = XmlMarshallUtil.unmarshall(SystemPrivilege.class,
					new InputStreamReader(new FileInputStream(xmlSystemPrivilege1), "UTF-8"));
			systemPrivilege2 = XmlMarshallUtil.unmarshall(SystemPrivilege.class,
					new InputStreamReader(new FileInputStream(xmlSystemPrivilege2), "UTF-8"));
			sort(systemPrivilege1);
			sort(systemPrivilege2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence PlatformSystemPrivilege (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(systemPrivilege1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(systemPrivilege2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(systemPrivilege1, systemPrivilege2, SystemPrivilege.class, resultA);
			assert resultA.getResultBs().size() == 1;
			
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_1;
			}
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlSystemPrivilege2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(xmlSystemPrivilege2 + ".csv");
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
		
		getLogger().debug("End Differrence PlatformSystemPrivilege");

		return ret;
	}
	
	private void sort(SystemPrivilege systemPrivilege) {
		SystemPrivilegeInfo[] infoList = systemPrivilege.getSystemPrivilegeInfo();
		Arrays.sort(infoList,
			new Comparator<SystemPrivilegeInfo>() {
				@Override
				public int compare(SystemPrivilegeInfo info1, SystemPrivilegeInfo info2) {
					int ret = info1.getRoleId().compareTo(info2.getRoleId());
					if(ret != 0){
						return ret;
					} else {
						ret = info1.getSystemFunction().compareTo(info2.getSystemFunction());
						if(ret != 0){
							return ret;
						} else {
							return info1.getSystemPrivilege().compareTo(info2.getSystemPrivilege());
						}
					}
				}
			});
		systemPrivilege.setSystemPrivilegeInfo(infoList);
	}
	
	public Logger getLogger() {
		return log;
	}
	protected void checkDelete(Map<String, Map<String, List<SystemPrivilegeInfoResponse>>> sysPrivMapXML){
		// Get システム権限情報 from DB. Key = [roldId > SystemFunction]
		Map<String, Map<String, List<SystemPrivilegeInfoResponse>>> sysPrivMapDB = new LinkedHashMap<>();
		try {
			for(RoleInfoResponse role: AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getRoleInfoList()){
				for(SystemPrivilegeInfoResponse info: AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getSystemPrivilegeInfoListByRoleId(role.getRoleId())){
					Map<String, List<SystemPrivilegeInfoResponse>> subMap;
					subMap = sysPrivMapDB.get(role.getRoleId());
					if(null == subMap){
						subMap = new LinkedHashMap<>();
		
						List<SystemPrivilegeInfoResponse> list = new ArrayList<SystemPrivilegeInfoResponse>();
						list.add(info);
		
						subMap.put(OpenApiEnumConverter.enumToString(info.getSystemFunction()), list);
						sysPrivMapDB.put(role.getRoleId(), subMap);
					}else{
						List<SystemPrivilegeInfoResponse> list = subMap.get(OpenApiEnumConverter.enumToString(info.getSystemFunction()));
						if(null == list){
							list = new ArrayList<SystemPrivilegeInfoResponse>();
							subMap.put(OpenApiEnumConverter.enumToString(info.getSystemFunction()), list);
						}
						list.add(info);
					}
				}
			}
		} catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}

		if(0 == sysPrivMapDB.size()){
			return;
		}

		// Check difference and delete if confirmed
		for(Entry<String, Map<String, List<SystemPrivilegeInfoResponse>>> entryByRole : sysPrivMapDB.entrySet()){
			String roleId = entryByRole.getKey();

			boolean allNotFound = !sysPrivMapXML.containsKey(roleId);

			Map<String, List<SystemPrivilegeInfoResponse>> subMap = entryByRole.getValue();
			String[] sysFuncLst = subMap.keySet().toArray(new String[]{});
			for(String sysFunc: sysFuncLst){
				boolean notFound;
				if(allNotFound){
					notFound = true;
				}else{
					notFound = !sysPrivMapXML.get(roleId).containsKey(sysFunc);
				}

				if(notFound){
					// マネージャのみに存在するデータがあった場合の削除方法を確認する
					if(!DeleteProcessMode.isSameprocess()){
						DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(null, Messages.getString("message.delete.confirm4", new String[]{roleId + " " + sysFunc }));
						DeleteProcessMode.setProcesstype(dialog.open());
						DeleteProcessMode.setSameprocess(dialog.getToggleState());
					}

					if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
						try {
							// Remove by systemFunction
							subMap.remove( sysFunc );

							// Convert to a full list and replace by roleId
							List<SystemPrivilegeInfoResponse> fullList = new ArrayList<>();
							for(Entry<String, List<SystemPrivilegeInfoResponse>> entry : subMap.entrySet()){
								fullList.addAll( entry.getValue());
							}

							List<SystemPrivilegeInfoRequestP1> infos = new ArrayList<SystemPrivilegeInfoRequestP1>();
							for ( SystemPrivilegeInfoResponse recOrg : fullList  ){
								SystemPrivilegeInfoRequestP1  rec = new SystemPrivilegeInfoRequestP1();
								RestClientBeanUtil.convertBean(recOrg, rec);
								infos.add(rec);
							}
							ReplaceSystemPrivilegeWithRoleRequest reqDto = new ReplaceSystemPrivilegeWithRoleRequest();
							reqDto.setSystemPrivilegeList(infos);
							
							AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).replaceSystemPrivilegeWithRole(roleId, reqDto);
							getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + roleId + " " + sysFunc);
						} catch (Exception e1) {
							getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + roleId + " " + sysFunc + ":" + HinemosMessage.replace(e1.getMessage()));
						}
					} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
						getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + roleId + " " + sysFunc);
					} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
						getLogger().info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
						return;
					}
				}
			}
		}
	}
	private List<SystemPrivilegeInfoRequestP1> getImportRecData( Map<String, List<SystemPrivilegeInfoResponse>> srcData) {
		List<SystemPrivilegeInfoRequestP1> ret = new ArrayList<SystemPrivilegeInfoRequestP1>();
		for( Entry<String, List<SystemPrivilegeInfoResponse>> entFunc : srcData.entrySet() ){
			for(SystemPrivilegeInfoResponse entPrivilege : entFunc.getValue()){
				SystemPrivilegeInfoRequestP1 dtoPrivilege = new SystemPrivilegeInfoRequestP1();
				try {
					RestClientBeanUtil.convertBean(entPrivilege, dtoPrivilege);
				} catch (HinemosUnknown e) {
					
				}
				ret.add(dtoPrivilege);
			}
		}
		return ret;
	}
}
