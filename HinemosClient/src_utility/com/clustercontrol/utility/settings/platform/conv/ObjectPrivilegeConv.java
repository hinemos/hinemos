/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.platform.conv;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.ObjectPrivilegeInfoResponse;

import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.platform.xml.ObjectPrivilegeInfo;
import com.clustercontrol.version.util.VersionUtil;

/**
 * ユーザ情報をJavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 * 
 * @version 6.1.0
 * @since 2.2.0
 * 
 */
public class ObjectPrivilegeConv {

	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	static private final String schemaType=VersionUtil.getSchemaProperty("PLATFORM.OBJECTPRIVILEGE.SCHEMATYPE");
	static private final String schemaVersion=VersionUtil.getSchemaProperty("PLATFORM.OBJECTPRIVILEGE.SCHEMAVERSION");
	static private final String schemaRevision=VersionUtil.getSchemaProperty("PLATFORM.OBJECTPRIVILEGE.SCHEMAREVISION");
	
	/* ロガー */
	private static Log log = LogFactory.getLog(ObjectPrivilegeConv.class);
	

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
	 * XMLから生成したオブジェクト（オブジェクト権限情報）から、DTOのシステム権限情報オブジェクトを生成する<br>
	 * 
	 * @param privilegeInfo
	 *            XMLオブジェクト権限情報オブジェクト
	 * @return Dtoオブジェクト権限情報オブジェクト
	 */
	public static ObjectPrivilegeInfoResponse convObjectPrivilegeXml2Dto(ObjectPrivilegeInfo privilegeInfo) {

		ObjectPrivilegeInfoResponse dto = new ObjectPrivilegeInfoResponse();

		try {
			if(privilegeInfo.getObjectId() != null
					&& !"".equals(privilegeInfo.getObjectId())){
				dto.setObjectId(privilegeInfo.getObjectId());
			}else{
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid") 
						+ "(ObjectId) : " + privilegeInfo.toString());
				return null;
			}

			if(privilegeInfo.getObjectType() != null
					&& !"".equals(privilegeInfo.getObjectType())){
				dto.setObjectType(privilegeInfo.getObjectType());
			}else{
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid") 
						+ "(ObjectType) : " + privilegeInfo.toString());
				return null;
			}
			
			if(privilegeInfo.getObjectPrivilege() != null
					&& !"".equals(privilegeInfo.getObjectPrivilege())){
				dto.setObjectPrivilege(privilegeInfo.getObjectPrivilege());
			}else{
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid") 
						+ "(ObjectPrivilege) : " + privilegeInfo.toString());
				return null;
			}
			
			if(privilegeInfo.getRoleId() != null
					&& !"".equals(privilegeInfo.getRoleId())){
				dto.setRoleId(privilegeInfo.getRoleId());
			}else{
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid") 
						+ "(RoleId) : " + privilegeInfo.toString());
				return null;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return dto;
	}

	/**
	 * DTOのシステム権限情報オブジェクトからXMLから生成したオブジェクト（オブジェクト権限情報）を生成する<br>
	 * 
	 * @param dto
	 *            DTOオブジェクト権限情報オブジェクト
	 * @return XMLオブジェクト権限情報オブジェクト
	 */
	public static ObjectPrivilegeInfo convObjectPrivilegeDto2Xml(ObjectPrivilegeInfoResponse dto) {

		ObjectPrivilegeInfo privilegeInfo = new ObjectPrivilegeInfo();

		privilegeInfo.setObjectId(dto.getObjectId());
		privilegeInfo.setObjectType(dto.getObjectType());
		privilegeInfo.setObjectPrivilege(dto.getObjectPrivilege());
		privilegeInfo.setRoleId(dto.getRoleId());

		return privilegeInfo;
	}
}
