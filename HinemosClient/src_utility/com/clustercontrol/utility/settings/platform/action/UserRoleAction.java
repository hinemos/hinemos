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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.openapitools.client.model.AddRoleInfoRequest;
import org.openapitools.client.model.AddUserInfoRequest;
import org.openapitools.client.model.AssignUserWithRoleRequest;
import org.openapitools.client.model.ImportRoleRecordRequest;
import org.openapitools.client.model.ImportRoleRequest;
import org.openapitools.client.model.ImportRoleResponse;
import org.openapitools.client.model.ImportRoleUserRecordRequest;
import org.openapitools.client.model.ImportRoleUserRequest;
import org.openapitools.client.model.ImportRoleUserResponse;
import org.openapitools.client.model.ImportUserRecordRequest;
import org.openapitools.client.model.ImportUserRequest;
import org.openapitools.client.model.ImportUserResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RoleInfoResponse;
import org.openapitools.client.model.UserInfoResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;

import com.clustercontrol.accesscontrol.util.AccessRestClientWrapper;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.UnEditableRole;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.MessageConstant;
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
import com.clustercontrol.utility.settings.platform.conv.UserRoleConv;
import com.clustercontrol.utility.settings.platform.xml.AccountRoleUser;
import com.clustercontrol.utility.settings.platform.xml.Role;
import com.clustercontrol.utility.settings.platform.xml.RoleInfo;
import com.clustercontrol.utility.settings.platform.xml.RoleUserInfo;
import com.clustercontrol.utility.settings.platform.xml.User;
import com.clustercontrol.utility.settings.platform.xml.UserInfo;
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
 * ユーザ・ロール定義情報をインポート・エクスポート・削除するアクションクラス<br>
 * 
 * @version 6.1.0
 * @since 2.2.0
 */
public class UserRoleAction {
	/** パスワード入力範囲 */
	private static final String PASSWORD_REGEX = "^[\\x21-\\x7e]+$";
	/** 文字コード */
	private static final Charset CHARSET = Charset.forName("UTF-8");
	/** システムロール */
	private static final Set<String> systemRoleSet = new HashSet<String>(Arrays.asList(
			"ADMINISTRATORS", "ALL_USERS", "INTERNAL")
			);
	
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
		AccessRestClientWrapper wrapper = AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		List<UserInfoResponse> userList = null;
		List<RoleInfoResponse> roleList = null;
		
		// ユーザID一覧の取得
		try {
			userList =wrapper.getUserInfoList();
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			 ret=SettingConstants.ERROR_INPROCESS ;
			 log.debug("End Clear PlatformUserRole (Error)");
			 return ret;
		}
		
		// ロール情報一覧の取得
		try {
			roleList = wrapper.getRoleInfoList();
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
					wrapper.deleteUserInfo(String.join(",", tmp));
					log.info(Messages.getString("SettingTools.ClearSucceeded") + " : User : " + id);
				}
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
		
		// ロール情報の削除
		String roleId = null;
		for (int i = 0; i < roleList.size(); i++) {
			roleId = roleList.get(i).getRoleId();
			try {
				AssignUserWithRoleRequest reqDto = new AssignUserWithRoleRequest();
				reqDto.setUserIdList(new ArrayList<String>());
				wrapper.assignUserWithRole(roleId, reqDto);
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : RoleUser : " + roleId);
				wrapper.deleteRoleInfo(roleId);
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : Role : " + roleId);
			} catch (UnEditableRole e) {
				// 編集不可なロールはスキップする
				log.info(Messages.getString("SettingTools.SkipSystemRole") + " : Role : " + roleId);
			} catch (HinemosUnknown e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidRole e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidUserPass e) {
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
		List<UserInfoResponse> userList = null;
		List<RoleInfoResponse> roleList = null;

		AccessRestClientWrapper wrapper = AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());

		// ユーザ情報一覧の取得
		try {
			userList = wrapper.getUserInfoList();
			Collections.sort(userList, new Comparator<UserInfoResponse>() {
				@Override
				public int compare(
						UserInfoResponse info1,
						UserInfoResponse info2) {
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
			roleList = wrapper.getRoleInfoList();
			Collections.sort(roleList, new Comparator<RoleInfoResponse>() {
				@Override
				public int compare(
						RoleInfoResponse info1,
						RoleInfoResponse info2) {
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
		for (UserInfoResponse userInfo : userList) {
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
		for (RoleInfoResponse info : roleList) {
			try {
				RoleInfoResponse roleInfo = wrapper.getRoleInfo(info.getRoleId());
				role.addRoleInfo(UserRoleConv.convRoleDto2Xml(roleInfo));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : Role : " + info.getRoleId());

				// ロール内ユーザ定義情報一覧の取得
				List<UserInfoResponse> userIdList = roleInfo.getUserInfoList();
				Collections.sort(userIdList, new Comparator<UserInfoResponse>() {
					@Override
					public int compare(UserInfoResponse id1, UserInfoResponse id2) {
						return id1.getUserId().compareTo(id2.getUserId());
					}
				});
				for (UserInfoResponse userId : userIdList) {
					try {
						accountRoleUser.addRoleUserInfo(UserRoleConv.convRoleUserDto2Xml(info.getRoleId(), userId.getUserId()));
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
		
		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			log.debug("End Import PlatformUserRole (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		int ret = 0;
		User user;
		// ユーザ情報をXMLファイルからの読み込み
		try {
			user = XmlMarshallUtil.unmarshall(User.class,new InputStreamReader(new FileInputStream(xmlUser), "UTF-8"));
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
			role = XmlMarshallUtil.unmarshall(Role.class,new InputStreamReader(new FileInputStream(xmlRole), "UTF-8"));
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
			accountRoleUser = XmlMarshallUtil.unmarshall(AccountRoleUser.class,
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
		// レコードの重複確認（ロール）
		ImportRoleRecordConfirmer roleConfirmer = new ImportRoleRecordConfirmer(log, role.getRoleInfo());
		int roleConfirmerRet = roleConfirmer.executeConfirm();
		if( roleConfirmerRet != SettingConstants.SUCCESS && roleConfirmerRet != SettingConstants.ERROR_CANCEL ){
			// 変換エラーならUnmarshalXml扱いで処理打ち切り(キャンセルはキャンセル以前の選択結果を反映するので次に進む)
			log.warn(Messages.getString("SettingTools.UnmarshalXmlFailed"));
			return roleConfirmerRet;
		}
		// レコードの登録（ロール）
		if (!(roleConfirmer.getImportRecDtoList().isEmpty())) {
			ImportRoleClientController roleController = new ImportRoleClientController(log,
					Messages.getString("platform.accesscontrol.role"), roleConfirmer.getImportRecDtoList(), true);
			int roleControllerRet = roleController.importExecute();
			if (roleControllerRet != 0) {
				ret = roleControllerRet;
			}
		}
		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}

		// ユーザ情報の登録
		// レコードの重複確認（ユーザ）
		ImportUserRecordConfirmer userConfirmer = new ImportUserRecordConfirmer(log, user.getUserInfo());
		int userConfirmerRet = userConfirmer.executeConfirm();
		if( userConfirmerRet != SettingConstants.SUCCESS && userConfirmerRet != SettingConstants.ERROR_CANCEL ){
			// 変換エラーならUnmarshalXml扱いで処理打ち切り(キャンセルはキャンセル以前の選択結果を反映するので次に進む)
			log.warn(Messages.getString("SettingTools.UnmarshalXmlFailed"));
			return userConfirmerRet;
		}
		// レコードの登録（ユーザ）
		if (!(userConfirmer.getImportRecDtoList().isEmpty())) {
			ImportUserClientController userController = new ImportUserClientController(log,
					Messages.getString("platform.accesscontrol.user"), userConfirmer.getImportRecDtoList(), true);
			int userControllerRet = userController.importExecute();
			if (userControllerRet != 0) {
				ret = userControllerRet;
			}
		}
		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			log.info(Messages.getString("SettingTools.ImportCompleted.Cancel"));
			return SettingConstants.ERROR_INPROCESS;
		}
		
		// ロール内ユーザ情報の登録
		HashMap<String, List<String>> mapRoleUser = new HashMap<String, List<String>>();
		for(ImportRoleRecordRequest roleRecordRequest : roleConfirmer.getImportRecDtoList()){
			List<String> list = new ArrayList<String>();
			mapRoleUser.put(roleRecordRequest.getImportKeyValue(), list);
			
		}
		
		for (int i = 0; i < accountRoleUser.getRoleUserInfoCount(); i++) {
			RoleUserInfo roleUserInfo = accountRoleUser.getRoleUserInfo(i);
			
			// システムロール、もしくは登録対象のロールの場合に、ロール内ユーザ情報を登録
			if (systemRoleSet.contains(roleUserInfo.getRoleId()) || 
					mapRoleUser.containsKey(roleUserInfo.getRoleId())){
				
				List<String> list = mapRoleUser.get(roleUserInfo.getRoleId());
				if(list == null)
					list = new ArrayList<String>();
				
				list.add(roleUserInfo.getUserId());
				mapRoleUser.put(roleUserInfo.getRoleId(), list);
			}
		}
		
		List<ImportRoleUserRecordRequest> roleUserList = new ArrayList<ImportRoleUserRecordRequest>();
		for(Entry<String, List<String>> entry : mapRoleUser.entrySet()){
			ImportRoleUserRecordRequest rec = new ImportRoleUserRecordRequest();
			rec.setImportData(new AssignUserWithRoleRequest());
			rec.setRoleId(entry.getKey());
			rec.getImportData().setUserIdList(entry.getValue());
			rec.setImportKeyValue(entry.getKey());
			rec.setIsNewRecord(false);
			roleUserList.add(rec);
		}
		ImportRoleUserClientController roleUserController = new ImportRoleUserClientController(
				log, Messages.getString("platform.accesscontrol.role.user"), roleUserList,true);
		int roleUserControllerRet = roleUserController.importExecute();
		if (roleUserControllerRet != 0) {
			ret = roleUserControllerRet;
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
			user1 = XmlMarshallUtil.unmarshall(User.class,new InputStreamReader(new FileInputStream(xmlUser1), "UTF-8"));
			user2 = XmlMarshallUtil.unmarshall(User.class,new InputStreamReader(new FileInputStream(xmlUser2), "UTF-8"));
			sort(user1);
			sort(user2);
			
			role1 = XmlMarshallUtil.unmarshall(Role.class,new InputStreamReader(new FileInputStream(xmlRole1), "UTF-8"));
			role2 = XmlMarshallUtil.unmarshall(Role.class,new InputStreamReader(new FileInputStream(xmlRole2), "UTF-8"));
			sort(role1);
			sort(role2);
			
			accountRoleUser1 = XmlMarshallUtil.unmarshall(AccountRoleUser.class,
					new InputStreamReader(new FileInputStream(xmlRoleUser1), "UTF-8"));
			accountRoleUser2 = XmlMarshallUtil.unmarshall(AccountRoleUser.class,
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
		
		List<UserInfoResponse> subList = null;
		try {
			subList = AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getUserInfoList();
		}
		catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			log.debug(e.getMessage(), e);
		}
		
		if(subList == null || subList.size() <= 0){
			return;
		}
		
		List<UserInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getUserInfo()));
		for( UserInfoResponse mgrInfo: new ArrayList<>(subList)){
			for(UserInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getUserId().equals(xmlElement.getUserId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(UserInfoResponse info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getUserId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					try {
						AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteUserInfo(info.getUserId());
						log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getUserId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getUserId());
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					log.info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
					return;
				}
			}
		}
	}

	protected void checkDelete(Role xmlElements){
		
		List<RoleInfoResponse> subList = null;
		try {
			subList = AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getRoleInfoList();
		}
		catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			log.debug(e.getMessage(), e);
		}
		
		if(subList == null || subList.size() <= 0){
			return;
		}
		
		List<RoleInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getRoleInfo()));
		for(RoleInfoResponse mgrInfo: new ArrayList<>(subList)){
			for(RoleInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getRoleId().equals(xmlElement.getRoleId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(RoleInfoResponse info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getRoleId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					try {
						AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteRoleInfo(info.getRoleId());
						log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getRoleId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getRoleId());
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
	 * ロールインポート向けのレコード確認用クラス
	 * 
	 * 重複確認に加えて、以下を実施
	 * ・システムロールはインポート対象から除外
	 */
	protected static class ImportRoleRecordConfirmer extends ImportRecordConfirmer<RoleInfo, ImportRoleRecordRequest, String>{
		public ImportRoleRecordConfirmer(Logger logger, RoleInfo[] importRecDtoList) {
			super(logger, importRecDtoList);
		}

		@Override
		protected ImportRoleRecordRequest convertDtoXmlToRestReq(RoleInfo xmlDto)
				throws HinemosUnknown, InvalidSetting {
			//システムロールはインポート対象から除外（重複確認もしない）
			if(systemRoleSet.contains(xmlDto.getRoleId())){
				log.info(Messages.getString("SettingTools.SkipSystemRole") + " : " + xmlDto.getRoleId());
				return null;
			}
			RoleInfoResponse dto = UserRoleConv.convRoleXml2Dto(xmlDto);
			ImportRoleRecordRequest dtoRec = new ImportRoleRecordRequest();
			dtoRec.setImportData(new AddRoleInfoRequest());
			RestClientBeanUtil.convertBean(dto, dtoRec.getImportData());
			dtoRec.setImportKeyValue(dtoRec.getImportData().getRoleId());
				
			return dtoRec;
		}

		@Override
		protected Set<String> getExistIdSet() throws Exception {
			Set<String> retSet = new HashSet<String>();
			List<RoleInfoResponse> roleInfoList = AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getRoleInfoList();
			for (RoleInfoResponse rec : roleInfoList) {
				retSet.add(rec.getRoleId());
			}
			return retSet;
		}
		@Override
		protected boolean isLackRestReq(ImportRoleRecordRequest restDto) {
			return (restDto == null || restDto.getImportData().getRoleId() == null || restDto.getImportData().getRoleId().equals(""));
		}
		@Override
		protected String getKeyValueXmlDto(RoleInfo xmlDto) {
			return xmlDto.getRoleId();
		}
		@Override
		protected String getId(RoleInfo xmlDto) {
			return xmlDto.getRoleId();
		}
		@Override
		protected void setNewRecordFlg(ImportRoleRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
	}

	/**
	 * ロールインポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportRoleClientController extends ImportClientController<ImportRoleRecordRequest, ImportRoleResponse, RecordRegistrationResponse>{
		
		public ImportRoleClientController(Logger logger, String importInfoName, List<ImportRoleRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportRoleResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportRoleResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportRoleRecordRequest importRec) {
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
		protected ImportRoleResponse callImportWrapper(List<ImportRoleRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportRoleRequest reqDto = new ImportRoleRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importRole(reqDto);
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

	/**
	 * ユーザインポート向けのレコード確認用クラス
	 * 
	 * 重複確認に加えて、以下を実施
	 * ・パスワードの内容チェック
	 * ・操作ユーザ自身のアカウントはインポート対象から除外
	 * 
	 */
	protected static class ImportUserRecordConfirmer extends ImportRecordConfirmer<UserInfo, ImportUserRecordRequest, String>{
		public ImportUserRecordConfirmer(Logger logger, UserInfo[] importRecDtoList) {
			super(logger, importRecDtoList);
		}

		@Override
		protected ImportUserRecordRequest convertDtoXmlToRestReq(UserInfo xmlDto)
				throws HinemosUnknown, InvalidSetting {
			UserInfoResponse dto = UserRoleConv.convUserXml2Dto(xmlDto);
			//操作ユーザ自身のアカウントはインポート対象から除外
			if (xmlDto.getUserId().equals(Config.getConfig("Login.USER"))) {
				this.log.info(Messages.getString("SettingTools.ExceptUser") + " : User : " + xmlDto.getUserId());
				return null;
			}
			ImportUserRecordRequest dtoRec = new ImportUserRecordRequest();
			dtoRec.setImportData(new AddUserInfoRequest());
			//パスワードのhash化
			if( setPassword(dtoRec,xmlDto.getPassword()) != 0 ){
				this.ret = SettingConstants.ERROR_INPROCESS;
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + getKeyValueXmlDto(xmlDto));
				return null;
			}
			RestClientBeanUtil.convertBean(dto, dtoRec.getImportData());
			dtoRec.setImportKeyValue(dtoRec.getImportData().getUserId());
			return dtoRec;
		}

		@Override
		protected Set<String> getExistIdSet() throws Exception {
			Set<String> retSet = new HashSet<String>();
			List<UserInfoResponse> UserInfoList = AccessRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getUserInfoList();
			for (UserInfoResponse rec : UserInfoList) {
				retSet.add(rec.getUserId());
			}
			return retSet;
		}
		@Override
		protected boolean isLackRestReq(ImportUserRecordRequest restDto) {
			boolean ret = (restDto == null || restDto.getImportData().getUserId() == null || restDto.getImportData().getUserId().equals(""));
			return ret;
		}
		@Override
		protected String getKeyValueXmlDto(UserInfo xmlDto) {
			return xmlDto.getUserId();
		}
		@Override
		protected String getId(UserInfo xmlDto) {
			return xmlDto.getUserId();
		}
		@Override
		protected void setNewRecordFlg(ImportUserRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
		/**
		 * パスワード セット<BR>
		 * パスワードが入力されている場合、文字数制限・属性チェックを実施後、パスワードのセットを行う<BR>
		 * 
		 * @param rec import用レコード
		 * @param password パスワード
		 * @param ret 終了コード(インポート)
		 * @return 終了コード(インポート)
		 */
		private int setPassword(ImportUserRecordRequest rec, String password) {
			int rtn = 0; 
			if (password != null && !(password.isEmpty())) {
				// 文字数制限チェック
				if (password.length() > 64) {
					String[] args = { HinemosMessage.replace(MessageConstant.PASSWORD.getMessage()), "64" };
					this.log.warn(Messages.getString("SettingTools.InvalidSetting") + " : "
							+ Messages.getString("message.common.2", args));
					return SettingConstants.ERROR_INPROCESS;
				}

				// パスワードの文字属性チェック
				Pattern pattern = Pattern.compile(PASSWORD_REGEX);
				Matcher match = pattern.matcher(password);
				if (!match.matches()) {
					this.log.warn(Messages.getString("SettingTools.InvalidSetting") + " : "
							+ Messages.getString("message.accesscontrol.71"));
					return SettingConstants.ERROR_INPROCESS;
				}
				rec.setPassword(password);
			}
			return rtn;
		}
	}

	/**
	 * ユーザインポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportUserClientController extends ImportClientController<ImportUserRecordRequest, ImportUserResponse, RecordRegistrationResponse>{
		
		public ImportUserClientController(Logger logger, String importInfoName, List<ImportUserRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportUserResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportUserResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportUserRecordRequest importRec) {
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
		protected ImportUserResponse callImportWrapper(List<ImportUserRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportUserRequest reqDto = new ImportUserRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importUser(reqDto);
		}

		@Override
		protected String getRestExceptionMessage(RecordRegistrationResponse responseRec) {
			if (responseRec.getExceptionInfo() != null) {
				return responseRec.getExceptionInfo().getException() +":"+ responseRec.getExceptionInfo().getMessage();
			}
			return null;
		};
	}


	/**
	 * ロールへのユーザの割り当て情報インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportRoleUserClientController extends ImportClientController<ImportRoleUserRecordRequest, ImportRoleUserResponse, RecordRegistrationResponse>{
		
		public ImportRoleUserClientController(Logger logger, String importInfoName, List<ImportRoleUserRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportRoleUserResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportRoleUserResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportRoleUserRecordRequest importRec) {
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
		protected ImportRoleUserResponse callImportWrapper(List<ImportRoleUserRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportRoleUserRequest reqDto = new ImportRoleUserRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importRoleUser(reqDto);
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
