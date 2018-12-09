/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.infra.conv;

import com.clustercontrol.utility.settings.infra.xml.InfraFileInfo;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.util.DateUtil;

/**
 * 環境構築ファイル定義情報をJavaBeanとXML(Bean)のbindingとの間でやりとりを
 * 行うクラス<BR>
 * 
 * @version 5.0.a
 * @since 5.0.a
 */
public class InfraFileConv extends BaseConv {
	
	// スキーマのタイプ、バージョン、リビジョンをそれぞれ返す
	@Override
	protected String getType() {return "G";}
	@Override
	protected String getVersion() {return "1";}
	@Override
	protected String getRevision() {return "1";}
	
	/**
	 * DTOのBeanからXMLのBeanに変換する。
	 * 
	 * @param info　DTOのBean
	 * @return 
	 * @throws Exception 
	 */
	public InfraFileInfo getXmlInfo(com.clustercontrol.ws.infra.InfraFileInfo info) throws Exception {

		InfraFileInfo ret = new InfraFileInfo();

		//情報のセット(主部分)

		ret.setFileId(info.getFileId());

		ret.setFileName(ifNull2Empty(info.getFileName()));
		ret.setCreateDatetime(DateUtil.convEpoch2DateString(info.getCreateDatetime()));
		ret.setCreateUserId(ifNull2Empty(info.getCreateUserId()));
		ret.setModifyDatetime(DateUtil.convEpoch2DateString(info.getModifyDatetime()));
		ret.setModifyUserId(ifNull2Empty(info.getModifyUserId()));
		ret.setOwnerRoleId(ifNull2Empty(info.getOwnerRoleId()));

		return ret;
	}

	public com.clustercontrol.ws.infra.InfraFileInfo getDTO(InfraFileInfo info) throws Exception {
		com.clustercontrol.ws.infra.InfraFileInfo ret = new com.clustercontrol.ws.infra.InfraFileInfo();

		//情報のセット(主部分)

		ret.setFileId(info.getFileId());
		
		ret.setFileName(ifNull2Empty(info.getFileName()));
		ret.setCreateDatetime(DateUtil.convDateString2Epoch(info.getCreateDatetime()));
		ret.setCreateUserId(ifNull2Empty(info.getCreateUserId()));
		ret.setModifyDatetime(DateUtil.convDateString2Epoch(info.getModifyDatetime()));
		ret.setModifyUserId(ifNull2Empty(info.getModifyUserId()));
		ret.setOwnerRoleId(ifNull2Empty(info.getOwnerRoleId()));

		return ret;
	}
}
