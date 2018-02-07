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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.ws.WebServiceException;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import com.clustercontrol.ClusterControlPlugin;
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
import com.clustercontrol.utility.settings.platform.conv.UserRoleConv;
import com.clustercontrol.utility.settings.platform.xml.AccountRoleUser;
import com.clustercontrol.utility.settings.platform.xml.Role;
import com.clustercontrol.utility.settings.platform.xml.RoleInfo;
import com.clustercontrol.utility.settings.platform.xml.RoleUserInfo;
import com.clustercontrol.utility.settings.platform.xml.User;
import com.clustercontrol.utility.settings.platform.xml.UserInfo;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.ImportProcessDialog;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.ws.access.HinemosUnknown_Exception;
import com.clustercontrol.ws.access.InvalidRole_Exception;
import com.clustercontrol.ws.access.InvalidSetting_Exception;
import com.clustercontrol.ws.access.InvalidUserPass_Exception;
import com.clustercontrol.ws.access.RoleDuplicate_Exception;
import com.clustercontrol.ws.access.UnEditableRole_Exception;
import com.clustercontrol.ws.access.UserDuplicate_Exception;

/**
 * ユーザ・ロール定義情報をインポート・エクスポート・削除するアクションクラス<br>
 * 
 * @version 6.1.0
 * @since 2.2.0
 */
public class UserRoleAction {
	protected static Logger log = Logger.getLogger(UserRoleAction.class);

	public UserRoleAction() throws ConvertorException {
		super();
	}

	/**
	 * 情報をマネージャから削除します。<BR>
	 * 
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearAccess() {

		log.debug("Start Clear PlatformUserRole ");
		int ret = 0;
		List<com.clustercontrol.ws.access.UserInfo> userList = null;
		List<com.clustercontrol.ws.access.RoleInfo> roleList = null;
		
		// ユーザID一覧の取得
		try {
			userList = AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getUserInfoList();
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			 ret=SettingConstants.ERROR_INPROCESS ;
			 log.debug("End Clear PlatformUserRole (Error)");
			 return ret;
		}
		
		// ロール情報一覧の取得
		try {
			roleList = AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getRoleInfoList();
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear PlatformUserRole (Error)");
			return ret;
		}

		// ユーザ情報の削除
		List<String> ids = new ArrayList<String>();
		String userId = null;
		for (int i = 0; i < userList.size(); i++) {
			userId = userList.get(i).getUserId();
			ids.add(userId);
		}

		List<String> tmp = new ArrayList<>();
		for(String id: ids){
			tmp.clear();
			tmp.add(id);
			try {
				if (id.equals(Config.getConfig("Login.USER"))) {
					// 現在のログインユーザは削除しない
					log.info(Messages.getString("SettingTools.ExceptUser") + " : User : " + id);
				} else {
					AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).deleteUserInfo(tmp);
					log.info(Messages.getString("SettingTools.ClearSucceeded") + " : User : " + id);
				}
			} catch (HinemosUnknown_Exception e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidRole_Exception e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidUserPass_Exception e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		// ロール情報の削除
		String roleId = null;
		for (int i = 0; i < roleList.size(); i++) {
			roleId = roleList.get(i).getRoleId();
			try {
				AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).assignUserRole(roleId, new ArrayList<String>());
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : RoleUser : " + roleId);
				
				AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).deleteRoleInfo(Arrays.asList(new String[]{roleId}));
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : Role : " + roleId);
			} catch (UnEditableRole_Exception e) {
				// 編集不可なロールはスキップする
				log.info(Messages.getString("SettingTools.SkipSystemRole") + " : Role : " + roleId);
			} catch (HinemosUnknown_Exception e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidRole_Exception e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidUserPass_Exception e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (WebServiceException e) {
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
		log.debug("End Clear PlatformUserRole ");
		return ret;
	}

	/**
	 * 情報をマネージャから取得し、XMLに出力します。<BR>
	 * 
	 * @param 出力するXMLファイル
	 * @return 終了コード
	 */
	@ExportMethod
	public int exportAccess(String xmlUser, String xmlRole, String xmlRoleUser) {

		log.debug("Start Export PlatformUserRole ");
		
		int ret = 0;
		List<com.clustercontrol.ws.access.UserInfo> userList = null;
		List<com.clustercontrol.ws.access.RoleInfo> roleList = null;

		// ユーザ情報一覧の取得
		try {
			userList = AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getUserInfoList();
			Collections.sort(userList, new Comparator<com.clustercontrol.ws.access.UserInfo>() {
				@Override
				public int compare(
						com.clustercontrol.ws.access.UserInfo info1,
						com.clustercontrol.ws.access.UserInfo info2) {
					return info1.getUserId().compareTo(info2.getUserId());
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Export PlatformUserRole (Error)");
			return ret;
		}
		
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
			log.error(Messages.getString("SettingTools.FailToGetList"), e);
			log.debug("End Export PlatformUserRole (Error)");
			return ret;
		}
		
		User user = new User();
		Role role = new Role();
		AccountRoleUser accountRoleUser = new AccountRoleUser();

		// ユーザ情報の取得
		for (com.clustercontrol.ws.access.UserInfo userInfo : userList) {
			try {
				user.addUserInfo(UserRoleConv.convUserDto2Xml(userInfo));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : User : " + userInfo.getUserId());
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			}
		}
		
		// ロール情報の取得
		for (com.clustercontrol.ws.access.RoleInfo info : roleList) {
			try {
				com.clustercontrol.ws.access.RoleInfo roleInfo = AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getRoleInfo(info.getRoleId());
				role.addRoleInfo(UserRoleConv.convRoleDto2Xml(roleInfo));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : Role : " + info.getRoleId());

				// ロール内ユーザ定義情報一覧の取得
				List<String> userIdList = roleInfo.getUserList();
				Collections.sort(userIdList, new Comparator<String>() {
					@Override
					public int compare(String id1, String id2) {
						return id1.compareTo(id2);
					}
				});
				for (String userId : userIdList) {
					try {
						accountRoleUser.addRoleUserInfo(UserRoleConv.convRoleUserDto2Xml(info.getRoleId(), userId));
						log.info(Messages.getString("SettingTools.ExportSucceeded") + " : RoleUser : " + info.getRoleId());
					} catch (Exception e) {
						log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
						break;
					}
				}

			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				log.debug("End Export PlatformUserRole (Error)");
			}
		}

		// XMLファイルに出力
		try {
			//ユーザ情報
			user.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			user.setSchemaInfo(UserRoleConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlUser);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				user.marshal(osw);
			}
			
			//ロール情報
			role.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			role.setSchemaInfo(UserRoleConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlRole);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				role.marshal(osw);
			}
			
			//ロール内ユーザ定義情報
			accountRoleUser.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			accountRoleUser.setSchemaInfo(UserRoleConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlRoleUser);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				accountRoleUser.marshal(osw);
			}
		} catch (UnsupportedEncodingException | MarshalException | ValidationException e) {
			log.warn(String.format(Messages.getString("SettingTools.MarshalXmlFailed"), e));
			ret = SettingConstants.ERROR_INPROCESS;
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
		log.debug("End Export PlatformUserRole ");
		return ret;
	}

	/**
	 * XMLの情報をマネージャに投入します。<BR>
	 * 
	 * @param 入力するXMLファイル
	 * @return 終了コード
	 */
	@ImportMethod
	public int importAccess(String xmlUser, String xmlRole, String xmlRoleUser) {

		log.debug("Start Import PlatformUserRole ");
		
		if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
			log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			log.debug("End Import PlatformUserRole (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		int ret = 0;
		User user;
		// ユーザ情報をXMLファイルからの読み込み
		try {
			user = User.unmarshal(new InputStreamReader(new FileInputStream(xmlUser), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformUserRole (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(user.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		// ロール情報をXMLファイルからの読み込み
		Role role;
		try {
			role = Role.unmarshal(new InputStreamReader(new FileInputStream(xmlRole), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformUserRole (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(role.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		// ロール内ユーザ情報をXMLファイルからの読み込み
		AccountRoleUser accountRoleUser;
		try {
			accountRoleUser = AccountRoleUser.unmarshal(
					new InputStreamReader(new FileInputStream(xmlRoleUser), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformUserRole (Error)");
			return ret;
		}
		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(accountRoleUser.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		// ロール情報の登録
		for (int i = 0; i < role.getRoleInfoCount(); i++) {
			RoleInfo roleInfo = role.getRoleInfo(i);
			com.clustercontrol.ws.access.RoleInfo dto = null;

			if (!roleInfo.getRoleId().equals("")) {
				try {
					dto = UserRoleConv.convRoleXml2Dto(roleInfo);
					AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).addRoleInfo(dto);
					log.info(Messages.getString("SettingTools.ImportSucceeded") + " : Role : " + roleInfo.getRoleId());
					
				} catch (UnEditableRole_Exception e) {
					// 編集不可なロールはスキップする
					log.info(Messages.getString("SettingTools.SkipSystemRole") + " : Role : " + roleInfo.getRoleId());
				} catch (RoleDuplicate_Exception e) {
					//重複時、インポート処理方法を確認する
					if(!ImportProcessMode.isSameprocess()){
						String[] args = {roleInfo.getRoleId()};
						ImportProcessDialog dialog = new ImportProcessDialog(
								null, Messages.getString("message.import.confirm2", args));
						ImportProcessMode.setProcesstype(dialog.open());
						ImportProcessMode.setSameprocess(dialog.getToggleState());
					}
					
					//システムロールの場合は無視する
					//本来は本体APIのaddRoleInfo側でUnEditableRole_Exceptionを返却すべき
					if("ADMINISTRATORS".equals(roleInfo.getRoleId()) ||
							"ALL_USERS".equals(roleInfo.getRoleId()) ||
							"INTERNAL".equals(roleInfo.getRoleId())){
						log.info(Messages.getString("SettingTools.SkipSystemRole") + " : Role : " + roleInfo.getRoleId());
					}
					//システムロールではない場合
					else{
						if(ImportProcessMode.getProcesstype() == ImportProcessDialog.UPDATE){
							try {
								AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).modifyRoleInfo(dto);
								log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : Role : " + roleInfo.getRoleId());
							} catch (Exception e1) {
								log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
								ret = SettingConstants.ERROR_INPROCESS;
							}
						} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.SKIP){
							log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : Role : " + roleInfo.getRoleId());
						} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
							log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
							ret = SettingConstants.ERROR_INPROCESS;
							return ret;
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
				} catch (WebServiceException e) {
					log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				} catch (Exception e) {
					log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				}
			}
		}

		// ユーザ情報の登録
		for (int i = 0; i < user.getUserInfoCount(); i++) {
			UserInfo userInfo = user.getUserInfo(i);
			com.clustercontrol.ws.access.UserInfo dto = null;

			if (!userInfo.getUserId().equals("")) {
				try {
					dto = UserRoleConv.convUserXml2Dto(userInfo);
					if (userInfo.getUserId().equals(Config.getConfig("Login.USER"))) {
						log.info(Messages.getString("SettingTools.ExceptUser") + " : User : " + userInfo.getUserId());
					} else {
						AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).addUserInfo(dto);
						log.info(Messages.getString("SettingTools.ImportSucceeded") + " : User : " + userInfo.getUserId());

						if(null != userInfo.getPassword() && !"".equals(userInfo.getPassword())){
							byte[] bytes = MessageDigest.getInstance("MD5").digest(userInfo.getPassword().getBytes());
							String passwordHash = Base64.encodeBase64String(bytes);
							AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).changePassword(userInfo.getUserId(), passwordHash);
							log.info(Messages.getString("SettingTools.ChangePassword") + " : User : " + userInfo.getUserId());
						}
					}
				} catch (UserDuplicate_Exception e) {
					//重複時、インポート処理方法を確認する
					if(!ImportProcessMode.isSameprocess()){
						String[] args = {userInfo.getUserId()};
						ImportProcessDialog dialog = new ImportProcessDialog(
								null, Messages.getString("message.import.confirm2", args));
						ImportProcessMode.setProcesstype(dialog.open());
						ImportProcessMode.setSameprocess(dialog.getToggleState());
					}

					if(ImportProcessMode.getProcesstype() == ImportProcessDialog.UPDATE){
						try {
							AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).modifyUserInfo(dto);
							log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : User : " + userInfo.getUserId());

							if(null != userInfo.getPassword() && !"".equals(userInfo.getPassword())){
								String passwordHash = Base64.encodeBase64String(MessageDigest.getInstance("MD5").digest(userInfo.getPassword().getBytes()));
								AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).changePassword(userInfo.getUserId(), passwordHash);
								log.info(Messages.getString("SettingTools.ImportSucceeded") + " : User : " + userInfo.getUserId());
							}
						} catch (Exception e1) {
							log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
							ret = SettingConstants.ERROR_INPROCESS;
						}
					} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.SKIP){
						log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : User : " + userInfo.getUserId());
					} else if(ImportProcessMode.getProcesstype() == ImportProcessDialog.CANCEL){
						log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
						ret = SettingConstants.ERROR_INPROCESS;
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
		}

		// ロール内ユーザ情報の登録
		HashMap<String, List<String>> mapRoleUser = new HashMap<String, List<String>>();
		for (int i = 0; i < accountRoleUser.getRoleUserInfoCount(); i++) {
			RoleUserInfo roleUserInfo = accountRoleUser.getRoleUserInfo(i);
			List<String> list = mapRoleUser.get(roleUserInfo.getRoleId());
			if(list == null)
				list = new ArrayList<String>();
			list.add(roleUserInfo.getUserId());
			mapRoleUser.put(roleUserInfo.getRoleId(), list);
		}
		
		for(Entry<String, List<String>> entry : mapRoleUser.entrySet()){
			try {
				AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).assignUserRole(entry.getKey(), entry.getValue());
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : RoleUser : " + entry.getKey());
			} catch (UnEditableRole_Exception e) {
				// 編集不可なロールはスキップする
				log.info(Messages.getString("SettingTools.SkipSystemRole") + " : RoleUser : " + entry.getKey());
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
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		checkDelete(user);
		checkDelete(role);
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Import PlatformUserRole ");
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
		int res = UserRoleConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo sci = UserRoleConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	/**
	 * 差分比較処理を行います。
	 * XMLファイル２つ（filePath1,filePath2）を比較する。
	 * 差分がある場合：差分をＣＳＶファイルで出力する。
	 * 差分がない場合：出力しない。
	 * 				   または、すでに存在している同一名のＣＳＶファイルを削除する。
	 *
	 * @param xmlUser1 XMLファイル名
	 * @param xmlRole1 XMLファイル名
	 * @param xmlRoleUser1 XMLファイル名
	 * @param xmlUser2 XMLファイル名
	 * @param xmlRole2 XMLファイル名
	 * @param xmlRoleUser2 XMLファイル名
	 * @return 終了コード
	 * @throws ConvertorException
	 */
	@DiffMethod
	public int diffXml(String xmlUser1, String xmlRole1, String xmlRoleUser1,
			String xmlUser2, String xmlRole2, String xmlRoleUser2) throws ConvertorException {
		log.debug("Search Differrence PlatformUserRole ");

		int ret = 0;
		// XMLファイルからの読み込み
		User user1 = null;
		User user2 = null;
		Role role1 = null;
		Role role2 = null;
		AccountRoleUser accountRoleUser1;
		AccountRoleUser accountRoleUser2;
		try {
			user1 = User.unmarshal(new InputStreamReader(new FileInputStream(xmlUser1), "UTF-8"));
			user2 = User.unmarshal(new InputStreamReader(new FileInputStream(xmlUser2), "UTF-8"));
			sort(user1);
			sort(user2);
			
			role1 = Role.unmarshal(new InputStreamReader(new FileInputStream(xmlRole1), "UTF-8"));
			role2 = Role.unmarshal(new InputStreamReader(new FileInputStream(xmlRole2), "UTF-8"));
			sort(role1);
			sort(role2);
			
			accountRoleUser1 = AccountRoleUser.unmarshal(
					new InputStreamReader(new FileInputStream(xmlRoleUser1), "UTF-8"));
			accountRoleUser2 = AccountRoleUser.unmarshal(
					new InputStreamReader(new FileInputStream(xmlRoleUser2), "UTF-8"));
			sort(accountRoleUser1);
			sort(accountRoleUser2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence PlatformUserRole (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersion(user1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(user2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(role1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(role2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(accountRoleUser1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(accountRoleUser2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(user1, user2, User.class, resultA);
			assert resultA.getResultBs().size() == 1;
			
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_1;
			}
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlUser2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(xmlUser2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						log.warn(String.format("Fail to delete file. %s", f.getAbsolutePath()));
				}
			}
			
			resultA = new ResultA();
			//比較処理に渡す
			diff = DiffUtil.diffCheck2(role1, role2, Role.class, resultA);
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_2;
			}
			assert resultA.getResultBs().size() == 1;
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlRole2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(xmlRole2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						log.warn(String.format("Fail to delete file. %s", f.getAbsolutePath()));
				}
			}
			
			resultA = new ResultA();
			//比較処理に渡す
			diff = DiffUtil.diffCheck2(accountRoleUser1, accountRoleUser2, AccountRoleUser.class, resultA);
			assert resultA.getResultBs().size() == 1;
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_3;
			}
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlRoleUser2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(xmlRoleUser2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						log.warn(String.format("Fail to delete file. %s", f.getAbsolutePath()));
				}
			}
		} catch (FileNotFoundException e) {
			log.warn(e.getMessage());
			ret = SettingConstants.ERROR_INPROCESS;
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
		log.debug("End Differrence PlatformUserRole");

		return ret;
	}
	
	private void sort(User user) {
		UserInfo[] infoList = user.getUserInfo();
		Arrays.sort(infoList,
			new Comparator<UserInfo>() {
				@Override
				public int compare(UserInfo info1, UserInfo info2) {
					return info1.getUserId().compareTo(info2.getUserId());
				}
			});
		user.setUserInfo(infoList);
	}
	
	private void sort(Role role) {
		RoleInfo[] infoList = role.getRoleInfo();
		Arrays.sort(infoList,
			new Comparator<RoleInfo>() {
				@Override
				public int compare(RoleInfo info1, RoleInfo info2) {
					return info1.getRoleId().compareTo(info2.getRoleId());
				}
			});
		role.setRoleInfo(infoList);
	}
	
	private void sort(AccountRoleUser roleUser) {
		try {
			RoleUserInfo[] infoList = roleUser.getRoleUserInfo();
			Arrays.sort(infoList,
				new Comparator<RoleUserInfo>() {
					@Override
					public int compare(RoleUserInfo info1, RoleUserInfo info2) {
						int ret = info1.getRoleId().compareTo(info1.getRoleId());
						if(ret != 0){
							return ret;
						} else {
							return info1.getUserId().compareTo(info1.getUserId());
						}
					}
				});
			roleUser.setRoleUserInfo(infoList);
		}
		catch (Exception e) {
		}
	}

	protected void checkDelete(User xmlElements){
		
		List<com.clustercontrol.ws.access.UserInfo> subList = null;
		try {
			subList = AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getUserInfoList();
		}
		catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			log.debug(e.getMessage(), e);
		}
		
		if(subList == null || subList.size() <= 0){
			return;
		}
		
		List<UserInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getUserInfo()));
		for(com.clustercontrol.ws.access.UserInfo mgrInfo: new ArrayList<>(subList)){
			for(UserInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getUserId().equals(xmlElement.getUserId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(com.clustercontrol.ws.access.UserInfo info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getUserId()};
					DeleteProcessDialog dialog = new DeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.DELETE){
					try {
						List<String> args = new ArrayList<>();
						args.add(info.getUserId());
						AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).deleteUserInfo(args);
						log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getUserId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.SKIP){
					log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getUserId());
				} else if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.CANCEL){
					log.info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
					return;
				}
			}
		}
	}

	protected void checkDelete(Role xmlElements){
		
		List<com.clustercontrol.ws.access.RoleInfo> subList = null;
		try {
			subList = AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getRoleInfoList();
		}
		catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			log.debug(e.getMessage(), e);
		}
		
		if(subList == null || subList.size() <= 0){
			return;
		}
		
		List<RoleInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getRoleInfo()));
		for(com.clustercontrol.ws.access.RoleInfo mgrInfo: new ArrayList<>(subList)){
			for(RoleInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getRoleId().equals(xmlElement.getRoleId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(com.clustercontrol.ws.access.RoleInfo info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getRoleId()};
					DeleteProcessDialog dialog = new DeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.DELETE){
					try {
						List<String> args = new ArrayList<>();
						args.add(info.getRoleId());
						AccessEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).deleteRoleInfo(args);
						log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getRoleId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.SKIP){
					log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getRoleId());
				} else if(DeleteProcessMode.getProcesstype() == DeleteProcessDialog.CANCEL){
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
