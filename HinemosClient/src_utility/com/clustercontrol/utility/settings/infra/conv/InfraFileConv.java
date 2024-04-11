/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.infra.conv;

import org.openapitools.client.model.AddInfraFileRequest;
import org.openapitools.client.model.InfraFileInfoResponse;

import com.clustercontrol.utility.settings.infra.xml.InfraFileInfo;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.version.util.VersionUtil;

/**
 * 環境構築ファイル定義情報をJavaBeanとXML(Bean)のbindingとの間でやりとりを
 * 行うクラス<BR>
 * 
 * @version 5.0.a
 * @since 5.0.a
 */
public class InfraFileConv extends BaseConv {
	
	// スキーマのタイプ、バージョン、リビジョンをそれぞれ返す
	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	@Override
	protected String getType() {return VersionUtil.getSchemaProperty("INFRA.INFRAFILE.SCHEMATYPE");}
	@Override
	protected String getVersion() {return VersionUtil.getSchemaProperty("INFRA.INFRAFILE.SCHEMAVERSION");}
	@Override
	protected String getRevision() {return VersionUtil.getSchemaProperty("INFRA.INFRAFILE.SCHEMAREVISION");}
	
	/**
	 * DTOのBeanからXMLのBeanに変換する。
	 * 
	 * @param info　DTOのBean
	 * @return 
	 * @throws Exception 
	 */
	public InfraFileInfo getXmlInfo(InfraFileInfoResponse info) throws Exception {

		InfraFileInfo ret = new InfraFileInfo();

		//情報のセット(主部分)
		ret.setFileId(info.getFileId());
		ret.setFileName(ifNull2Empty(info.getFileName()));
		ret.setCreateDatetime(info.getCreateDatetime());
		ret.setCreateUserId(ifNull2Empty(info.getCreateUserId()));
		ret.setModifyDatetime(info.getModifyDatetime());
		ret.setModifyUserId(ifNull2Empty(info.getModifyUserId()));
		ret.setOwnerRoleId(ifNull2Empty(info.getOwnerRoleId()));

		return ret;
	}

	public AddInfraFileRequest getDTO(InfraFileInfo info) throws Exception {
		AddInfraFileRequest ret = new AddInfraFileRequest();

		//情報のセット(主部分)
		ret.setFileId(info.getFileId());
		ret.setFileName(ifNull2Empty(info.getFileName()));
		ret.setOwnerRoleId(ifNull2Empty(info.getOwnerRoleId()));

		return ret;
	}
}
