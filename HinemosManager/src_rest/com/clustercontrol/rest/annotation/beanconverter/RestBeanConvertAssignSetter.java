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

@Retention(RetentionPolicy.RUNTIME)
 @Target({ ElementType.FIELD })

/**
 * DTO <=> INFOの変換時にて、
 * dest側のメンバ変数値を設定するためのsetterメソッドの名称を指定します。（通常はメンバ変数名に基づく自動生成）<br>
 * 本アノテーションを変換先オブジェクトのフィールドに指定してください。
 * 
 */
public @interface RestBeanConvertAssignSetter {
	String setterName();

}
