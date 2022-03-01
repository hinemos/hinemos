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
 * 列挙型に関する DTO と INFO の変換を指定します。<br>
 * 本アノテーションをDTOオブジェクトのフィールドに指定することで下記の通りの変換を実施します。
 * 
 * ・列挙型(AAA) <-> 内部コード("0")
 * 
 * 変換対象となる列挙型については必ず com.clustercontrol.rest.dto.EnumDto を実装したクラスとしてください。
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface RestBeanConvertEnum {

}
