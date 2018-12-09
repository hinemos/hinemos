/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.bean;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.util.MessageConstant;

/**
 * 将来予測監視で使用する予測方法を定数として格納するクラス<BR>
 * 
 * @version 6.1.0
 */
public class MonitorPredictionMethodConstant {
	/** 線形回帰 */
	public static final String POLYNOMIAL_1 = "POLYNOMIAL_1";
	/** 非線形回帰（2次） */
	public static final String POLYNOMIAL_2 = "POLYNOMIAL_2";
	/** 非線形回帰（3次） */
	public static final String POLYNOMIAL_3 = "POLYNOMIAL_3";
	/** デフォルト */
	public static final String DEFALUT = POLYNOMIAL_1;

	/**
	 * 予測方法に対応したメッセージを返す
	 * 
	 * @param typeName タイプ
	 * @return
	 */
	public static String typeToMessage(String type) {
		 if (type.equals(POLYNOMIAL_1)) {
		 	return MessageConstant.PREDICTION_METHOD_POLYNOMIAL_1.getMessage();
		 } else if (type.equals(POLYNOMIAL_2)) {
		 	return MessageConstant.PREDICTION_METHOD_POLYNOMIAL_2.getMessage();
		 } else if (type.equals(POLYNOMIAL_3)) {
		 	return MessageConstant.PREDICTION_METHOD_POLYNOMIAL_3.getMessage();
		 } else {
		 	return "";
		 }
	}

	/**
	 * 解析方法に対応したメッセージを返す
	 * 
	 * @param typeName タイプ
	 * @return
	 */
	public static List<String> types() {
		List<String> typeList = new ArrayList<String>();
		typeList.add(POLYNOMIAL_1);
		typeList.add(POLYNOMIAL_2);
		typeList.add(POLYNOMIAL_3);
		return typeList;
	}

	private MonitorPredictionMethodConstant() {
		throw new IllegalStateException("ConstClass");
	}
}