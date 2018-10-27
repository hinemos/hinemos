/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * json 形式で記載した目印情報を クラス、プロパティの get 関数に紐づけるアノテーション。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
@Retention(RetentionPolicy.RUNTIME)  
@Target({ElementType.METHOD, ElementType.TYPE})  
public @interface DiffAnnotation {
	String[] value();
}
