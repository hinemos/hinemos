/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.annotation.beanconverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 日時データに関する DTO と INFO の変換を指定します。
 * 本アノテーションをDTOオブジェクトのフィールドに指定することで下記の通りの変換を実施します。
 * 
 * ・文字列日時情報(yyyy-MM-dd HH:mm:ss) <-> ミリ秒単位の日時情報(xxxxx msec)
 * 
 * 文字列日時情報のフォーマットは Hinemos プロパティより取得します。
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface RestBeanConvertDatetime {

}
