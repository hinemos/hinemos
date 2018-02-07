/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

/**
 * プロパティ値のアクセサー定義。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public interface PropValue {
	Object getRealValue();
	String getTranslatedString();
	String getResourceString();
}