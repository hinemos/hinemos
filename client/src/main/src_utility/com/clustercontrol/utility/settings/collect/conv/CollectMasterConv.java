/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.collect.conv;

import com.clustercontrol.utility.settings.master.xml.CollectorCalcMethodData;
import com.clustercontrol.utility.settings.master.xml.CollectorCalcMethods;
import com.clustercontrol.utility.settings.master.xml.CollectorCategoryCollectData;
import com.clustercontrol.utility.settings.master.xml.CollectorCategoryCollects;
import com.clustercontrol.utility.settings.master.xml.CollectorCategoryData;
import com.clustercontrol.utility.settings.master.xml.CollectorItemCalcMethodData;
import com.clustercontrol.utility.settings.master.xml.CollectorItemCalcMethods;
import com.clustercontrol.utility.settings.master.xml.CollectorItemCodeData;
import com.clustercontrol.utility.settings.master.xml.CollectorItemCodes;
import com.clustercontrol.utility.settings.master.xml.CollectorItems;
import com.clustercontrol.utility.settings.master.xml.CollectorPollingData;
import com.clustercontrol.utility.settings.master.xml.PollingCollector;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.ws.collectmaster.CollectorCalcMethodMstData;
import com.clustercontrol.ws.collectmaster.CollectorCategoryCollectMstData;
import com.clustercontrol.ws.collectmaster.CollectorCategoryMstData;
import com.clustercontrol.ws.collectmaster.CollectorItemCalcMethodMstData;
import com.clustercontrol.ws.collectmaster.CollectorItemCodeMstData;
import com.clustercontrol.ws.collectmaster.CollectorPollingMstData;

/**
 * 収集項目定義情報のJavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 * 
 * @version 6.0.0
 * @since 1.2.0
 * 
 */
public class CollectMasterConv {
	
	static private final String schemaType="H";
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
	 * 計算定義情報を、XML BeanからHinemos Bean(DTO)へ変換する。
	 * 
	 * @param 計算定義 XML Bean
	 * @return  Hinemos Bean(DTO)
	 */
	public static CollectorCalcMethodMstData xml2dto(CollectorCalcMethodData xmlBean) {

		CollectorCalcMethodMstData data = new CollectorCalcMethodMstData();
		
		// 計算方法
		if (xmlBean.getCalcMethod() != null && !xmlBean.getCalcMethod().equals("")) {
			data.setCalcMethod(xmlBean.getCalcMethod());
		}
		// クラス名
		data.setClassName(xmlBean.getClassName());
		// 計算式
		data.setExpression(xmlBean.getExpression());
		
		return data;
		
	}
	
	/**
	 * 計算定義情報を、Hinemos Bean(DTO)からXML Beanへ変換する。
	 * 
	 * @param Hinemos Bean(DTO)
	 * @return 計算定義 XML Bean
	 */
	public static CollectorCalcMethods dto2Xml(CollectorCalcMethodMstData data) {

		CollectorCalcMethods ret =
			new CollectorCalcMethods();

		// 計算方法
		ret.setCalcMethod(data.getCalcMethod());
		// クラス名
		ret.setClassName(data.getClassName());
		// 計算式
		ret.setExpression(data.getExpression());

		return ret;
	}
	
	/**
	 * カテゴリコード情報を、XML BeanからHinemos Bean(DTO)へ変換する。
	 * 
	 * @param カテゴリコード XML Bean
	 * @return  Hinemos Bean(DTO)
	 */
	public static CollectorCategoryMstData xml2dto(CollectorCategoryData xmlBean) {

		CollectorCategoryMstData data = new CollectorCategoryMstData();
		
		// カテゴリコード
		if (xmlBean.getCategoryCode() != null && !xmlBean.getCategoryCode().equals("")) {
			data.setCategoryCode(xmlBean.getCategoryCode());
		}
		// カテゴリ名
		data.setCategoryName(xmlBean.getCategoryName());
		
		return data;
		
	}
	
	/**
	 * カテゴリコード情報を、Hinemos Bean(DTO)からXML Beanへ変換する。
	 * 
	 * @param Hinemos Bean(DTO)
	 * @return カテゴリコード XML Bean
	 */
	public static CollectorItems dto2Xml(CollectorCategoryMstData data) {

		CollectorItems ret =
			new CollectorItems();

		// カテゴリコード
		ret.setCategoryCode(data.getCategoryCode());
		// カテゴリ名
		ret.setCategoryName(data.getCategoryName());

		return ret;
	}
	
	/**
	 * 収集項目コード情報を、XML BeanからHinemos Bean(DTO)へ変換する。
	 * 
	 * @param 収集毎の計算方法 XML Bean
	 * @return  Hinemos Bean(DTO)
	 */
	public static CollectorItemCodeMstData xml2dto(CollectorItemCodeData xmlBean) {

		CollectorItemCodeMstData data = new CollectorItemCodeMstData();
		
		// 収集項目コード
		if (xmlBean.getItemCode() != null && !xmlBean.getItemCode().equals("")) {
			data.setItemCode(xmlBean.getItemCode());
		}
		// カテゴリコード
		data.setCategoryCode(xmlBean.getCategoryCode());
		// 親収集項目コード
		data.setParentItemCode(xmlBean.getParentItemCode());
		// 収集項目名
		data.setItemName(xmlBean.getItemName());
		// 単位
		data.setMeasure(xmlBean.getMeasure());
		// デバイスのサポート
		data.setDeviceSupport(xmlBean.getDeviceSupport());
		// デバイス種別
		data.setDeviceType(xmlBean.getDeviceType());
		// グラフレンジ
		data.setGraphRange(xmlBean.getGraphRange());
		
		return data;
		
	}
	
	/**
	 * 収集項目コード情報を、Hinemos Bean(DTO)からXML Beanへ変換する。
	 * 
	 * @param Hinemos Bean(DTO)
	 * @return 収集項目コード XML Bean
	 */
	public static CollectorItemCodes dto2Xml(CollectorItemCodeMstData data) {

		CollectorItemCodes ret =
			new CollectorItemCodes();

		// 収集項目コード
		ret.setItemCode(data.getItemCode());
		//カテゴリコード
		ret.setCategoryCode(data.getCategoryCode());
		// 親収集項目コード
		ret.setParentItemCode(data.getParentItemCode());
		// 収集項目名
		ret.setItemName(data.getItemName());
		// 単位
		ret.setMeasure(data.getMeasure());
		// デバイスのサポート
		ret.setDeviceSupport(data.isDeviceSupport());
		// デバイス種別
		ret.setDeviceType(data.getDeviceType());
		// グラフレンジ
		ret.setGraphRange(data.isGraphRange());

		return ret;
	}
	
	/**
	 * 収集項目毎の計算方法を、XML BeanからHinemos Bean(DTO)へ変換する。
	 * 
	 * @param 収集項目毎の計算方法 XML Bean
	 * @return  Hinemos Bean(DTO)
	 */
	public static CollectorItemCalcMethodMstData xml2dto(CollectorItemCalcMethodData xmlBean) {

		CollectorItemCalcMethodMstData data = new CollectorItemCalcMethodMstData();
		
		// 収集方法
		if (xmlBean.getCollectMethod() != null && !xmlBean.getCollectMethod().equals("")) {
			data.setCollectMethod(xmlBean.getCollectMethod());
		}
		// プラットフォームID
		if (xmlBean.getPlatformId() != null && !xmlBean.getPlatformId().equals("")) {
			data.setPlatformId(xmlBean.getPlatformId());
		}
		// サブプラットフォームID
		if (xmlBean.getSubPlatformId() != null && !xmlBean.getSubPlatformId().equals("")) {
			data.setSubPlatformId(xmlBean.getSubPlatformId());
		}
		else {
			data.setSubPlatformId("");
		}
		// 収集項目
		if (xmlBean.getItemCode() != null && !xmlBean.getItemCode().equals("")) {
			data.setItemCode(xmlBean.getItemCode());
		}
		// 計算方法
		data.setCalcMethod(xmlBean.getCalcMethod());
		
		return data;
		
	}
	
	/**
	 * 収集項目毎の計算方法を、Hinemos Bean(DTO)からXML Beanへ変換する。
	 * 
	 * @param Hinemos Bean(DTO)
	 * @return 収集項目毎の計算方法 XML Bean
	 */
	public static CollectorItemCalcMethods dto2Xml(CollectorItemCalcMethodMstData data) {

		CollectorItemCalcMethods ret =
			new CollectorItemCalcMethods();

		// 収集方法
		ret.setCollectMethod(data.getCollectMethod());
		// プラットフォームID
		ret.setPlatformId(data.getPlatformId());
		// サブプラットフォームID
		ret.setSubPlatformId(data.getSubPlatformId());
		// 収集項目
		ret.setItemCode(data.getItemCode());
		// 計算方法
		ret.setCalcMethod(data.getCalcMethod());

		return ret;
	}
	
	
	/**
	 * ポーリング定義情報を、XML BeanからHinemos Bean(DTO)へ変換する。
	 * 
	 * @param ポーリング定義 XML Bean
	 * @return  Hinemos Bean(DTO)
	 */
	public static CollectorPollingMstData xml2dto(CollectorPollingData xmlBean) {

		CollectorPollingMstData data = new CollectorPollingMstData();
		
		// 収集方法
		if (xmlBean.getCollectMethod() != null && !xmlBean.getCollectMethod().equals("")) {
			data.setCollectMethod(xmlBean.getCollectMethod());
		}
		// プラットフォームID
		if (xmlBean.getPlatformId() != null && !xmlBean.getPlatformId().equals("")) {
			data.setPlatformId(xmlBean.getPlatformId());
		}
		// サブプラットフォームID
		if (xmlBean.getSubPlatformId() != null && !xmlBean.getSubPlatformId().equals("")) {
			data.setSubPlatformId(xmlBean.getSubPlatformId());
		}
		else {
			data.setSubPlatformId("");
		}
		// 収集項目
		if (xmlBean.getItemCode() != null && !xmlBean.getItemCode().equals("")) {
			data.setItemCode(xmlBean.getItemCode());
		}
		// 変数名
		if (xmlBean.getVariableId() != null && !xmlBean.getVariableId().equals("")) {
			data.setVariableId(xmlBean.getVariableId());
		}
		// エントリーキー
		data.setEntryKey(xmlBean.getEntryKey());
		// 値の型
		data.setValueType(xmlBean.getValueType());
		// ポーリング対象
		data.setPollingTarget(xmlBean.getPollingTarget());
		// 取得失敗時の値
		data.setFailureValue(xmlBean.getFailureValue());

		return data;
		
	}
	
	/**
	 * ポーリング定義情報を、Hinemos Bean(DTO)からXML Beanへ変換する。
	 * 
	 * @param Hinemos Bean(DTO)
	 * @return ポーリング定義 XML Bean
	 */
	public static PollingCollector dto2Xml(CollectorPollingMstData data) {

		PollingCollector ret =
			new PollingCollector();

		// 収集方法
		ret.setCollectMethod(data.getCollectMethod());
		// プラットフォームID
		ret.setPlatformId(data.getPlatformId());
		// サブプラットフォームID
		ret.setSubPlatformId(data.getSubPlatformId());
		// 収集項目
		ret.setItemCode(data.getItemCode());
		// 変数名
		ret.setVariableId(data.getVariableId());
		// エントリーキー
		ret.setEntryKey(data.getEntryKey());
		// 値の型
		ret.setValueType(data.getValueType());
		// ポーリング対象
		ret.setPollingTarget(data.getPollingTarget());
		// 取得失敗時の値
		ret.setFailureValue(data.getFailureValue());

		return ret;
	}
	
	/**
	 * カテゴリ毎の収集方法を、XML BeanからHinemos Bean(DTO)へ変換する。
	 * 
	 * @param カテゴリ毎の収集方法 XML Bean
	 * @return  Hinemos Bean(DTO)
	 */
	public static CollectorCategoryCollectMstData xml2dto(CollectorCategoryCollectData xmlBean) {

		CollectorCategoryCollectMstData data = new CollectorCategoryCollectMstData();
		
		// プラットフォームID
		if (xmlBean.getPlatformId() != null && !xmlBean.getPlatformId().equals("")) {
			data.setPlatformId(xmlBean.getPlatformId());
		}
		// サブプラットフォームID
		if (xmlBean.getSubPlatformId() != null && !xmlBean.getSubPlatformId().equals("")) {
			data.setSubPlatformId(xmlBean.getSubPlatformId());
		}
		else {
			data.setSubPlatformId("");
		}
		// カテゴリコード
		if (xmlBean.getCategoryCode() != null && !xmlBean.getCategoryCode().equals("")) {
			data.setCategoryCode(xmlBean.getCategoryCode());
		}
		// 収集方法
		data.setCollectMethod(xmlBean.getCollectMethod());
		
		return data;
		
	}
	
	/**
	 * カテゴリ毎の収集方法を、Hinemos Bean(DTO)からXML Beanへ変換する。
	 * 
	 * @param Hinemos Bean(DTO)
	 * @return カテゴリ毎の収集方法 XML Bean
	 */
	public static CollectorCategoryCollects dto2Xml(CollectorCategoryCollectMstData data) {

		CollectorCategoryCollects ret =
			new CollectorCategoryCollects();

		// プラットフォームID
		ret.setPlatformId(data.getPlatformId());
		// サブプラットフォームID
		ret.setSubPlatformId(data.getSubPlatformId());
		// カテゴリコード
		ret.setCategoryCode(data.getCategoryCode());
		// 収集方法
		ret.setCollectMethod(data.getCollectMethod());

		return ret;
	}
}
