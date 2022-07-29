/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
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
public class MonitorPredictionMethod {
	/** 線形回帰 */
	public static final String POLYNOMIAL_1 = "POLYNOMIAL_1";
	/** 非線形回帰（2次） */
	public static final String POLYNOMIAL_2 = "POLYNOMIAL_2";
	/** 非線形回帰（3次） */
	public static final String POLYNOMIAL_3 = "POLYNOMIAL_3";
	/** デフォルト */
	public static final String DEFALUT = POLYNOMIAL_1;

	/** Enum判定用のName */
	private static final String ENUM_NAME_POLYNOMIAL_1 = "POLYNOMIAL_1";
	private static final String ENUM_NAME_POLYNOMIAL_2 = "POLYNOMIAL_2";
	private static final String ENUM_NAME_POLYNOMIAL_3 = "POLYNOMIAL_3";

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
	 * Enumからメッセージに変換します。<BR>
	 * ※Enumの型は引数で指定できますが、列挙子のNameは統一されている必要があります。<BR>
	 * 
	 * @param value 変換するEnum
	 * @param enumType Enumの型
	 * @return 文字列
	 */
	public static <T extends Enum<T>> String enumToMessage(T value, Class<T> enumType) {
		if (value != null) {
			String name = value.toString();
			if (name.equals(ENUM_NAME_POLYNOMIAL_1)) {
				return MessageConstant.PREDICTION_METHOD_POLYNOMIAL_1.getMessage();
			} else if (name.equals(ENUM_NAME_POLYNOMIAL_2)) {
				return MessageConstant.PREDICTION_METHOD_POLYNOMIAL_2.getMessage();
			} else if (name.equals(ENUM_NAME_POLYNOMIAL_3)) {
				return MessageConstant.PREDICTION_METHOD_POLYNOMIAL_3.getMessage();
			}
		}
		return "";
	}

	/**
	 * 解析方法に対応したメッセージを返す
	 * 
	 * @param typeName タイプ
	 * @return
	 */
	public static List<String> types() {
		List<String> typeList = new ArrayList<>();
		typeList.add(POLYNOMIAL_1);
		typeList.add(POLYNOMIAL_2);
		typeList.add(POLYNOMIAL_3);
		return typeList;
	}

}