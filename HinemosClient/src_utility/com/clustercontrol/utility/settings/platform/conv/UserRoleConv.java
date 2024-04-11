/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.platform.conv;

import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.RoleInfoResponse;
import org.openapitools.client.model.UserInfoResponse;

import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.platform.xml.RoleInfo;
import com.clustercontrol.utility.settings.platform.xml.RoleUserInfo;
import com.clustercontrol.utility.settings.platform.xml.UserInfo;
import com.clustercontrol.version.util.VersionUtil;

/**
 * ユーザ情報をJavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 * 
 * @version 6.0.0
 * @since 2.2.0
 * 
 */
public class UserRoleConv {

	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	static private final String schemaType=VersionUtil.getSchemaProperty("PLATFORM.USERROLE.SCHEMATYPE");
	static private final String schemaVersion=VersionUtil.getSchemaProperty("PLATFORM.USERROLE.SCHEMAVERSION");
	static private final String schemaRevision=VersionUtil.getSchemaProperty("PLATFORM.USERROLE.SCHEMAREVISION");
	
	/* ロガー */
	private static Log log = LogFactory.getLog(UserRoleConv.class);
	

	/**
	 * XMLとツールの対応バージョンをチェック */
	static public int checkSchemaVersion(String type, String version ,String revision){
		return BaseConv.checkSchemaVersion(
				schemaType,schemaVersion,schemaRevision,
				type,version,revision);
	}

	/**
	 * スキーマのバージョンを返します。
	 * @return
	 */
	static public com.clustercontrol.utility.settings.platform.xml.SchemaInfo getSchemaVersion(){
	
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo schema =
			new com.clustercontrol.utility.settings.platform.xml.SchemaInfo();
		
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		
		return schema;
	}

	/**
	 * XMLから生成したオブジェクト（ユーザ情報）から、DTOのユーザ情報オブジェクトを生成する<br>
	 * 
	 * @param userInfo
	 *            XMLユーザ情報オブジェクト
	 * @return Dtoユーザ情報オブジェクト
	 */
	public static UserInfoResponse convUserXml2Dto(UserInfo userInfo) {

		UserInfoResponse dto = new UserInfoResponse();

		try {
			if(userInfo.getUserId() != null
					&& !"".equals(userInfo.getUserId())){
				dto.setUserId(userInfo.getUserId());
			}else{
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid") 
						+ "(UserId) : " + userInfo.toString());
				return null;
			}

			if(userInfo.getName() != null && !"".equals(userInfo.getName())){
				dto.setUserName(userInfo.getName());
			}else{
				dto.setUserName("");
			}
			// userInfo.getName()で取得したStringがnullでなく空文字でないならdtoにセットして、それ以外なら空文字セットするなら、空文字はそのまま入れればいい
			dto.setUserName(Objects.isNull(userInfo.getName())?"":userInfo.getName());

			if(userInfo.getDescription() != null
					&& !"".equals(userInfo.getDescription())){
				dto.setDescription(userInfo.getDescription());
			}else{
				dto.setDescription("");
			}
			
			if (userInfo.getMailAddress() != null 
					&& !userInfo.getMailAddress().isEmpty()){
				dto.setMailAddress(userInfo.getMailAddress());
			}else{
				dto.setMailAddress("");
			}
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return dto;
	}

	/**
	 * DTOのユーザ情報オブジェクトからXMLから生成したオブジェクト（ユーザ情報）を生成する<br>
	 * ただし、パスワードは符号化されているため、空文字とする。
	 * 
	 * @param dto
	 *            DTOユーザ情報オブジェクト
	 * @return XMLユーザ情報オブジェクト
	 */
	public static UserInfo convUserDto2Xml(UserInfoResponse dto) {
		UserInfo userInfo = new UserInfo();
		userInfo.setUserId(dto.getUserId());
		userInfo.setName(dto.getUserName());
		userInfo.setPassword("");
		userInfo.setDescription(dto.getDescription());
		userInfo.setMailAddress(dto.getMailAddress());

		return userInfo;
	}
	
	/**
	 * XMLから生成したオブジェクト（ロール情報）から、DTOのロール情報オブジェクトを生成する<br>
	 * 
	 * @param roleInfo
	 *            XMLロール情報オブジェクト
	 * @return Dtoロール情報オブジェクト
	 */
	public static RoleInfoResponse convRoleXml2Dto(RoleInfo roleInfo) {

		RoleInfoResponse dto = new RoleInfoResponse();

		try {
			if(roleInfo.getRoleId() != null
					&& !"".equals(roleInfo.getRoleId())){
				dto.setRoleId(roleInfo.getRoleId());
			}else{
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid") 
						+ "(RoleId) : " + roleInfo.toString());
				return null;
			}
			
			dto.setRoleName(Objects.isNull(roleInfo.getRoleName())?"":roleInfo.getRoleName());

			if(roleInfo.getDescription() != null
					&& !"".equals(roleInfo.getDescription())){
				dto.setDescription(roleInfo.getDescription());
			}else{
				dto.setDescription("");
			}
			
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return dto;
	}

	/**
	 * DTOのロール情報オブジェクトからXMLから生成したオブジェクト（ロール情報）を生成する<br>
	 * 
	 * @param dto
	 *            DTOロール情報オブジェクト
	 * @return XMLロール情報オブジェクト
	 */
	public static RoleInfo convRoleDto2Xml(RoleInfoResponse dto) {

		RoleInfo roleInfo = new RoleInfo();

		roleInfo.setRoleId(dto.getRoleId());
		roleInfo.setRoleName(dto.getRoleName());

		roleInfo.setDescription(dto.getDescription());
		
		return roleInfo;
	}

	/**
	 * DTOのロールID及びユーザIDからXMLから生成したオブジェクト（ロール内ユーザ定義情報）を生成する<br>
	 * 
	 * @param roleId ロールID
	 * @param userId ユーザID
	 * @return XMLロール内ユーザ定義情報オブジェクト
	 */
	public static RoleUserInfo convRoleUserDto2Xml(String roleId, String userId) {

		RoleUserInfo roleUserInfo = new RoleUserInfo();

		roleUserInfo.setRoleId(roleId);
		roleUserInfo.setUserId(userId);
		
		return roleUserInfo;
	}
}
