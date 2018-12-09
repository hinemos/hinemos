/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.collect.conv;

import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.ws.collectmaster.CollectorPlatformMstData;

/**
 * プラットフォーム情報のJavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 * 
 * @version 6.0.0
 * @since 1.2.0
 * 
 */
public class PlatformMasterConv {
	
	static private final String schemaType="E";
	static private final String schemaVersion="1";
	static private String schemaRevision ="1";
	
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
	static public com.clustercontrol.utility.settings.master.xml.SchemaInfo getSchemaVersion(){
	
		com.clustercontrol.utility.settings.master.xml.SchemaInfo schema =
			new com.clustercontrol.utility.settings.master.xml.SchemaInfo();
		
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		
		return schema;
	}
	
	/**
	 * プラットフォーム情報を、XML BeanからHinemos Bean(DTO)へ変換する。
	 * 
	 * @param プラットフォーム XML Bean
	 * @return  Hinemos Bean(DTO)
	 */
	public static CollectorPlatformMstData xml2dto(com.clustercontrol.utility.settings.master.xml.CollectorPlatforms xmlBean) {

		CollectorPlatformMstData data = new CollectorPlatformMstData();
		
		// プラットフォームID
		if (xmlBean.getPlatformId() != null && !xmlBean.getPlatformId().equals("")) {
			data.setPlatformId(xmlBean.getPlatformId());
		}
		// プラットフォーム名
		data.setPlatformName(xmlBean.getPlatformName());
		// オーダーナンバー
		data.setOrderNo((short)xmlBean.getOrderNo());
		
		return data;
		
	}
	
	/**
	 * プラットフォーム情報を、Hinemos Bean(DTO)からXML Beanへ変換する。
	 * 
	 * @param Hinemos Bean(DTO)
	 * @return プラットフォーム XML Bean
	 */
	public static com.clustercontrol.utility.settings.master.xml.CollectorPlatforms dto2Xml(CollectorPlatformMstData data) {

		com.clustercontrol.utility.settings.master.xml.CollectorPlatforms ret =
			new com.clustercontrol.utility.settings.master.xml.CollectorPlatforms();

		//プラットフォームID
		ret.setPlatformId(data.getPlatformId());
		//プラットフォーム名
		ret.setPlatformName(data.getPlatformName());
		//オーダーナンバー
		ret.setOrderNo(data.getOrderNo());

		return ret;
	}
	
}
