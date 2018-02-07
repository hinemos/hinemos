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

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.bean.FunctionConstant;
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
import com.clustercontrol.utility.settings.platform.conv.SystemPrivilegeConv;
import com.clustercontrol.utility.settings.platform.xml.SystemPrivilege;
import com.clustercontrol.utility.settings.platform.xml.SystemPrivilegeInfo;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.ImportProcessDialog;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.ws.access.HinemosUnknown_Exception;
import com.clustercontrol.ws.access.InvalidRole_Exception;
import com.clustercontrol.ws.access.InvalidSetting_Exception;
import com.clustercontrol.ws.access.InvalidUserPass_Exception;
import com.clustercontrol.ws.access.UnEditableRole_Exception;

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
		List<com.clustercontrol.ws.access.RoleInfo> roleList = null;
		
		// ロール情報一覧の取得
		try {
			roleList = AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getRoleInfoList();
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
				List<com.clustercontrol.ws.access.SystemPrivilegeInfo> infos =
						new ArrayList<com.clustercontrol.ws.access.SystemPrivilegeInfo>();
				com.clustercontrol.ws.access.SystemPrivilegeInfo info =
						new com.clustercontrol.ws.access.SystemPrivilegeInfo();
				info.setSystemFunction(FunctionConstant.REPOSITORY);
				info.setSystemPrivilege("READ");
				infos.add(info);
				AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).replaceSystemPrivilegeRole(roleId, infos);
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + roleId);
			} catch (UnEditableRole_Exception e) {
				// 編集不可なロールはスキップする
				log.info(Messages.getString("SettingTools.SkipSystemRole") + " : " + roleId);
			} catch (WebServiceException e) {
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
		List<com.clustercontrol.ws.access.RoleInfo> roleList = null;
		List<com.clustercontrol.ws.access.SystemPrivilegeInfo> systemPrivilegeList = null;
		
		// ロール情報一覧の取得
		try {
			roleList = AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getRoleInfoList();
			Collections.sort(roleList, new Comparator<com.clustercontrol.ws.access.RoleInfo>() {
				@Override
				public int compare(
						com.clustercontrol.ws.access.RoleInfo info1,
						com.clustercontrol.ws.access.RoleInfo info2) {
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
			com.clustercontrol.ws.access.RoleInfo roleInfo = roleList.get(i);
			try {
				// システム権限情報一覧の取得
				try {
					systemPrivilegeList = AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getSystemPrivilegeInfoListByRoleId(roleInfo.getRoleId());
					Collections.sort(systemPrivilegeList, new Comparator<com.clustercontrol.ws.access.SystemPrivilegeInfo>() {
						@Override
						public int compare(
								com.clustercontrol.ws.access.SystemPrivilegeInfo info1,
								com.clustercontrol.ws.access.SystemPrivilegeInfo info2) {
							int ret = info1.getSystemFunction().compareTo(info2.getSystemFunction());
							if(ret != 0){
								return ret;
							} else {
								return info1.getSystemPrivilege().compareTo(info2.getSystemPrivilege());
							}
						}
					});
					for (int j = 0; j < systemPrivilegeList.size(); j++) {
						com.clustercontrol.ws.access.SystemPrivilegeInfo privilegeInfo = systemPrivilegeList.get(j);
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

		if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
			getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			getLogger().debug("End Import PlatformSystemPrivilege (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}

		int ret = 0;
		SystemPrivilege systemPrivilege = null;

		// システム権限情報をXMLファイルからの読み込み
		try {
			systemPrivilege = SystemPrivilege.unmarshal(
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
		Map<String, Map<String, List<com.clustercontrol.ws.access.SystemPrivilegeInfo>>> sysPrivMapXML = new LinkedHashMap<>();
		for (int i = 0; i < systemPrivilege.getSystemPrivilegeInfoCount(); i++) {
			SystemPrivilegeInfo privilegeInfo = systemPrivilege.getSystemPrivilegeInfo(i);
			com.clustercontrol.ws.access.SystemPrivilegeInfo dto = SystemPrivilegeConv.convSystemPrivilegeXml2Dto(privilegeInfo);
			
			Map<String, List<com.clustercontrol.ws.access.SystemPrivilegeInfo>> subMap;
			subMap = sysPrivMapXML.get(privilegeInfo.getRoleId());
			if(null == subMap){
				subMap = new LinkedHashMap<>();

				List<com.clustercontrol.ws.access.SystemPrivilegeInfo> list = new ArrayList<com.clustercontrol.ws.access.SystemPrivilegeInfo>();
				list.add(dto);

				subMap.put(privilegeInfo.getSystemFunction(), list);
				sysPrivMapXML.put(privilegeInfo.getRoleId(), subMap);
			}else{
				List<com.clustercontrol.ws.access.SystemPrivilegeInfo> list = subMap.get(privilegeInfo.getSystemFunction());
				if(null == list){
					list = new ArrayList<com.clustercontrol.ws.access.SystemPrivilegeInfo>();
					subMap.put(privilegeInfo.getSystemFunction(), list);
				}
				list.add(dto);
			}
		}

		boolean isBorken = false; // loop control
		for(Entry<String, Map<String, List<com.clustercontrol.ws.access.SystemPrivilegeInfo>>> entryByRole : sysPrivMapXML.entrySet()){
			String roleId = entryByRole.getKey();
			try {
				List<com.clustercontrol.ws.access.SystemPrivilegeInfo> sysPrivLstByRole = AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getSystemPrivilegeInfoListByRoleId(roleId);

				if(null == sysPrivLstByRole || 0 == sysPrivLstByRole.size()){
					// Add if not found

					List<String> resultMsgs = new ArrayList<>();
					List<com.clustercontrol.ws.access.SystemPrivilegeInfo> fullList = new ArrayList<>();
					for(Entry<String, List<com.clustercontrol.ws.access.SystemPrivilegeInfo>> entry : entryByRole.getValue().entrySet()){
						String sysFunc = entry.getKey();
						resultMsgs.add( roleId + " " + sysFunc );

						fullList.addAll( entry.getValue());
					}
					AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).replaceSystemPrivilegeRole(roleId, fullList);

					for(String msg : resultMsgs){
						log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + msg);
					}
				}else{
					// Get difference between DB

					// First, reform to map
					Map<String, List<com.clustercontrol.ws.access.SystemPrivilegeInfo>> subMapDB = new HashMap<>();
					for(com.clustercontrol.ws.access.SystemPrivilegeInfo info : sysPrivLstByRole){
						List<com.clustercontrol.ws.access.SystemPrivilegeInfo> list = subMapDB.get(info.getSystemFunction());

						if(null == list){
							list = new ArrayList<>();
							subMapDB.put(info.getSystemFunction(), list);
						}
						//リポジトリ - 参照 はインポート対象外とする
						if(!(info.getSystemFunction().equals(FunctionConstant.REPOSITORY) && 
								info.getSystemPrivilege().equals("READ")))
							list.add(info);
					}

					List<String> resultMsgs = new ArrayList<>();
					boolean updated = false;
					for(Entry<String, List<com.clustercontrol.ws.access.SystemPrivilegeInfo>> entry : entryByRole.getValue().entrySet()){
						String sysFunc = entry.getKey();

						if(null == subMapDB.get(sysFunc) || 0 == subMapDB.get(sysFunc).size()){
							// Append if not found in DB
							subMapDB.put( sysFunc, entry.getValue() );
							updated = true;
							resultMsgs.add(Messages.getString("SettingTools.ImportSucceeded") + " : " + roleId + " " + sysFunc);
						}else{
							// 重複時、インポート処理方法を確認する
							if(!ImportProcessMode.isSameprocess()){
								ImportProcessDialog dialog = new ImportProcessDialog(null, Messages.getString("message.import.confirm2", new String[]{roleId + " " + sysFunc}));
								ImportProcessMode.setProcesstype(dialog.open());
								ImportProcessMode.setSameprocess(dialog.getToggleState());
							}
			
							if(ImportProcessMode.getProcesstype() == ImportProcessDialog.UPDATE){
								subMapDB.put( sysFunc, entry.getValue());
								updated = true;
								resultMsgs.add(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + roleId + " " + sysFunc);
							} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.SKIP){
								resultMsgs.add(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + roleId + " " + sysFunc);
							} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
								resultMsgs.add(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
								ret = SettingConstants.ERROR_INPROCESS;
								isBorken = true;
								break;
							}
						}
					}
					if(updated){
						// Convert to a full list and replace by roleId
						List<com.clustercontrol.ws.access.SystemPrivilegeInfo> fullList = new ArrayList<>();
						for(Entry<String, List<com.clustercontrol.ws.access.SystemPrivilegeInfo>> entry : subMapDB.entrySet()){
							fullList.addAll( entry.getValue());
						}
						AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).replaceSystemPrivilegeRole(roleId, fullList);
					}
					for(String msg : resultMsgs){
						log.info(msg);
					}

					// Break again after all
					if(isBorken){
						break;
					}
				}
			} catch (UnEditableRole_Exception e) {
				// 編集不可なロールはスキップする
				log.info(Messages.getString("SettingTools.SkipSystemRole") + " : " + roleId);
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
			systemPrivilege1 = SystemPrivilege.unmarshal(
					new InputStreamReader(new FileInputStream(xmlSystemPrivilege1), "UTF-8"));
			systemPrivilege2 = SystemPrivilege.unmarshal(
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
	protected void checkDelete(Map<String, Map<String, List<com.clustercontrol.ws.access.SystemPrivilegeInfo>>> sysPrivMapXML){
		// Get システム権限情報 from DB. Key = [roldId > SystemFunction]
		Map<String, Map<String, List<com.clustercontrol.ws.access.SystemPrivilegeInfo>>> sysPrivMapDB = new LinkedHashMap<>();
		try {
			for(com.clustercontrol.ws.access.RoleInfo role: AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getRoleInfoList()){
				for(com.clustercontrol.ws.access.SystemPrivilegeInfo info: AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getSystemPrivilegeInfoListByRoleId(role.getRoleId())){
					Map<String, List<com.clustercontrol.ws.access.SystemPrivilegeInfo>> subMap;
					subMap = sysPrivMapDB.get(role.getRoleId());
					if(null == subMap){
						subMap = new LinkedHashMap<>();
		
						List<com.clustercontrol.ws.access.SystemPrivilegeInfo> list = new ArrayList<com.clustercontrol.ws.access.SystemPrivilegeInfo>();
						list.add(info);
		
						subMap.put(info.getSystemFunction(), list);
						sysPrivMapDB.put(role.getRoleId(), subMap);
					}else{
						List<com.clustercontrol.ws.access.SystemPrivilegeInfo> list = subMap.get(info.getSystemFunction());
						if(null == list){
							list = new ArrayList<com.clustercontrol.ws.access.SystemPrivilegeInfo>();
							subMap.put(info.getSystemFunction(), list);
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
		for(Entry<String, Map<String, List<com.clustercontrol.ws.access.SystemPrivilegeInfo>>> entryByRole : sysPrivMapDB.entrySet()){
			String roleId = entryByRole.getKey();

			boolean allNotFound = !sysPrivMapXML.containsKey(roleId);

			Map<String, List<com.clustercontrol.ws.access.SystemPrivilegeInfo>> subMap = entryByRole.getValue();
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
						DeleteProcessDialog dialog = new DeleteProcessDialog(null, Messages.getString("message.delete.confirm4", new String[]{roleId + " " + sysFunc }));
						DeleteProcessMode.setProcesstype(dialog.open());
						DeleteProcessMode.setSameprocess(dialog.getToggleState());
					}

					if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.DELETE){
						try {
							// Remove by systemFunction
							subMap.remove( sysFunc );

							// Convert to a full list and replace by roleId
							List<com.clustercontrol.ws.access.SystemPrivilegeInfo> fullList = new ArrayList<>();
							for(Entry<String, List<com.clustercontrol.ws.access.SystemPrivilegeInfo>> entry : subMap.entrySet()){
								fullList.addAll( entry.getValue());
							}
							AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).replaceSystemPrivilegeRole(roleId, fullList);
							getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + roleId + " " + sysFunc);
						} catch (Exception e1) {
							getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
						}
					} else if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.SKIP){
						getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + roleId + " " + sysFunc);
					} else if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.CANCEL){
						getLogger().info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
						return;
					}
				}
			}
		}
	}
}
