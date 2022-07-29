/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import org.openapitools.client.model.JmxMasterInfoResponse;

import com.clustercontrol.utility.settings.master.xml.JmxMasterInfo;
import com.clustercontrol.utility.settings.model.BaseConv;

/**
 * JMX マスタ設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 * 
 * @version 6.1.0
 * @since 5.0.a
 * 
 * 
 */
public class JmxMasterConv extends BaseConv{
	
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
	public JmxMasterInfo getXmlInfo(JmxMasterInfoResponse info) throws Exception {
		JmxMasterInfo ret = new JmxMasterInfo();

		//情報のセット(主部分)
		ret.setMasterId(info.getId());
		
		ret.setKeys(ifNull2Empty(info.getKeys()));
		ret.setAttributeName(ifNull2Empty(info.getAttributeName()));
		ret.setMeasure(ifNull2Empty(info.getMeasure()));
		ret.setName(ifNull2Empty(info.getName()));
		ret.setObjectName(ifNull2Empty(info.getObjectName()));
				
		return ret;
	}

	public JmxMasterInfoResponse getDTO(JmxMasterInfo info) throws Exception {
		
		JmxMasterInfoResponse ret = new JmxMasterInfoResponse();

		//情報のセット(主部分)
		ret.setId(info.getMasterId());
		
		ret.setKeys(ifNull2Empty(info.getKeys()));
		ret.setAttributeName(ifNull2Empty(info.getAttributeName()));
		ret.setMeasure(ifNull2Empty(info.getMeasure()));
		ret.setName(ifNull2Empty(info.getName()));
		ret.setObjectName(ifNull2Empty(info.getObjectName()));
				
		return ret;
	}
}