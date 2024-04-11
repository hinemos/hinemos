/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.collect.conv;

import org.openapitools.client.model.CollectorCalcMethodMstDataRequest;
import org.openapitools.client.model.CollectorCalcMethodMstDataResponse;
import org.openapitools.client.model.CollectorCategoryCollectMstDataRequest;
import org.openapitools.client.model.CollectorCategoryCollectMstDataResponse;
import org.openapitools.client.model.CollectorCategoryMstDataRequest;
import org.openapitools.client.model.CollectorCategoryMstDataResponse;
import org.openapitools.client.model.CollectorItemCalcMethodMstDataRequest;
import org.openapitools.client.model.CollectorItemCalcMethodMstDataResponse;
import org.openapitools.client.model.CollectorItemCodeMstDataRequest;
import org.openapitools.client.model.CollectorItemCodeMstDataResponse;
import org.openapitools.client.model.CollectorPollingMstDataRequest;
import org.openapitools.client.model.CollectorPollingMstDataResponse;

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
import com.clustercontrol.version.util.VersionUtil;

/**
 * 収集項目定義情報のJavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 * 
 * @version 6.0.0
 * @since 1.2.0
 * 
 */
public class CollectMasterConv {
	
	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	static private final String schemaType=VersionUtil.getSchemaProperty("COLLECT.COLLECTMASTER.SCHEMATYPE");
	static private final String schemaVersion=VersionUtil.getSchemaProperty("COLLECT.COLLECTMASTER.SCHEMAVERSION");
	static private String schemaRevision =VersionUtil.getSchemaProperty("COLLECT.COLLECTMASTER.SCHEMAREVISION");
	
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
	public static CollectorCalcMethodMstDataRequest xml2dto(CollectorCalcMethodData xmlBean) {

		CollectorCalcMethodMstDataRequest data = new CollectorCalcMethodMstDataRequest();
		
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
	public static CollectorCalcMethods dto2Xml(CollectorCalcMethodMstDataResponse data) {

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
	public static CollectorCategoryMstDataRequest xml2dto(CollectorCategoryData xmlBean) {

		CollectorCategoryMstDataRequest data = new CollectorCategoryMstDataRequest();
		
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
	public static CollectorItems dto2Xml(CollectorCategoryMstDataResponse data) {

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
	public static CollectorItemCodeMstDataRequest xml2dto(CollectorItemCodeData xmlBean) {

		CollectorItemCodeMstDataRequest data = new CollectorItemCodeMstDataRequest();
		
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
	public static CollectorItemCodes dto2Xml(CollectorItemCodeMstDataResponse data) {

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
		ret.setDeviceSupport(data.getDeviceSupport());
		// デバイス種別
		ret.setDeviceType(data.getDeviceType());
		// グラフレンジ
		ret.setGraphRange(data.getGraphRange());

		return ret;
	}
	
	/**
	 * 収集項目毎の計算方法を、XML BeanからHinemos Bean(DTO)へ変換する。
	 * 
	 * @param 収集項目毎の計算方法 XML Bean
	 * @return  Hinemos Bean(DTO)
	 */
	public static CollectorItemCalcMethodMstDataRequest xml2dto(CollectorItemCalcMethodData xmlBean) {

		CollectorItemCalcMethodMstDataRequest data = new CollectorItemCalcMethodMstDataRequest();
		
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
	public static CollectorItemCalcMethods dto2Xml(CollectorItemCalcMethodMstDataResponse data) {

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
	public static CollectorPollingMstDataRequest xml2dto(CollectorPollingData xmlBean) {

		CollectorPollingMstDataRequest data = new CollectorPollingMstDataRequest();
		
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
	public static PollingCollector dto2Xml(CollectorPollingMstDataResponse data) {

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
	public static CollectorCategoryCollectMstDataRequest xml2dto(CollectorCategoryCollectData xmlBean) {

		CollectorCategoryCollectMstDataRequest data = new CollectorCategoryCollectMstDataRequest();
		
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
	public static CollectorCategoryCollects dto2Xml(CollectorCategoryCollectMstDataResponse data) {

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
