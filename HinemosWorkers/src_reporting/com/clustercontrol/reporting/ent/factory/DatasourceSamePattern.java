/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ent.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clustercontrol.reporting.ent.bean.DataKey;
import com.clustercontrol.reporting.ent.bean.OutputFacilityInfo;
import com.clustercontrol.reporting.ent.bean.ResourceChart;
import com.clustercontrol.reporting.factory.DatasourceBase;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;

/**
 * 同一のテンプレートを使用し、DataKeyクラスをもとに収集グラフのレポート作成を行うクラス
 * 
 * @version 5.0.a
 * @since 5.0.a
 */
public abstract class DatasourceSamePattern extends DatasourceBase {
	protected Map<String, Object> m_retMap = new HashMap<String, Object>();
	
	/**
	 * このクラスを使用してデータソース（CSVファイル）をHinemos DBから生成する際はList<DataKey>が必要
	 */
	@Override
	public HashMap<String, Object> createDataSource(int num) throws ReportingPropertyNotFound {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * データソース（CSVファイル）をHinemos DBから生成する
	 */
	public abstract Map<String, Object> createDataSource(ResourceChart chart, int num) throws ReportingPropertyNotFound;

	public abstract void collectDataSource(OutputFacilityInfo rootFacility,int num) throws ReportingPropertyNotFound;

	public abstract List<DataKey> getKeys(String facilityId);
}
