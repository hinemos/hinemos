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
import org.openapitools.client.model.SystemPrivilegeInfoResponse;

import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.platform.xml.SystemPrivilegeInfo;
import com.clustercontrol.utility.util.OpenApiEnumConverter;
import com.clustercontrol.version.util.VersionUtil;

/**
 * ユーザ情報をJavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 * 
 * @version 6.1.0
 * @since 2.2.0
 * 
 */
public class SystemPrivilegeConv {

	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	static private final String schemaType=VersionUtil.getSchemaProperty("PLATFORM.SYSTEMPRIVILEGE.SCHEMATYPE");
	static private final String schemaVersion=VersionUtil.getSchemaProperty("PLATFORM.SYSTEMPRIVILEGE.SCHEMAVERSION");
	static private final String schemaRevision=VersionUtil.getSchemaProperty("PLATFORM.SYSTEMPRIVILEGE.SCHEMAREVISION");
	
	/* ロガー */
	private static Log log = LogFactory.getLog(SystemPrivilegeConv.class);
	

	/**
	 * XMLとツールの対応バージョンをチェック */
	static public int checkSchemaVersion(String type, String version ,String revision){

		if(type.equals(schemaType)){
			//スキーマタイプ一致
			if(Integer.parseInt(schemaVersion) == Integer.parseInt(schemaVersion) ){
				
				//バージョン一致
				return 0;
			}else if(Integer.parseInt(schemaVersion) > Integer.parseInt(schemaVersion)){
				//APの方がXMLのバージョンより新しい
				return -3;
			}else{
				//XMLの方がAPのバージョンより新しい
				return -2;
			}
		}else{
			//スキーマタイプ不一致
			return -1;
		}	
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
	 * XMLから生成したオブジェクト（システム権限情報）から、DTOのシステム権限情報オブジェクトを生成する<br>
	 * 
	 * @param privilegeInfo
	 *            XMLシステム権限情報オブジェクト
	 * @return Dtoシステム権限情報オブジェクト
	 * @throws Exception 
	 */
	public static SystemPrivilegeInfoResponse convSystemPrivilegeXml2Dto(SystemPrivilegeInfo privilegeInfo) throws Exception {

		SystemPrivilegeInfoResponse dto = new SystemPrivilegeInfoResponse();

		try {
			if(privilegeInfo.getSystemFunction() != null
					&& !"".equals(privilegeInfo.getSystemFunction())){
				dto.setSystemFunction(OpenApiEnumConverter.stringToEnum(privilegeInfo.getSystemFunction(), SystemPrivilegeInfoResponse.SystemFunctionEnum.class));
				dto.setEditType(SystemPrivilegeInfoResponse.EditTypeEnum.DIALOG );//1固定
			}else{
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid") 
						+ "(SystemFunction) : " + privilegeInfo.toString());
				return null;
			}

			if(privilegeInfo.getSystemPrivilege() != null
					&& !"".equals(privilegeInfo.getSystemPrivilege())){
				dto.setSystemPrivilege(OpenApiEnumConverter.stringToEnum(privilegeInfo.getSystemPrivilege(), SystemPrivilegeInfoResponse.SystemPrivilegeEnum.class));
			}else{
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid") 
						+ "(SystemPrivilege) : " + privilegeInfo.toString());
				return null;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
		
		return dto;
	}

	/**
	 * DTOのシステム権限情報オブジェクトからXMLから生成したオブジェクト（システム権限情報）を生成する<br>
	 * 
	 * @param roleId　ロールID
	 * @param dto
	 *            DTOシステム権限情報オブジェクト
	 * @return XMLシステム権限情報オブジェクト
	 */
	/**
	 * @param roleId
	 * @param dto
	 * @return
	 */
	public static SystemPrivilegeInfo convSystemPrivilegeDto2Xml(String roleId, SystemPrivilegeInfoResponse dto) {

		SystemPrivilegeInfo privilegeInfo = new SystemPrivilegeInfo();

		privilegeInfo.setRoleId(roleId);
		privilegeInfo.setSystemFunction(OpenApiEnumConverter.enumToString(dto.getSystemFunction()));
		
		privilegeInfo.setSystemPrivilege(OpenApiEnumConverter.enumToString(dto.getSystemPrivilege()));
		//privilegeInfo.setEditType(dto.getEditType());

		return privilegeInfo;
	}
}
