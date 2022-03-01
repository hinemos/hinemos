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
 * DTO <=>INFO の変換において、ID変換(※)の対象となるINFOクラスとクラス内のID用メンバ を指定するアノテーションです。<br>
 * 
 * ※ID変換：IDを表すメンバについて、INFOクラス側ではID用メンバクラスの内部メンバとして収容しているが
 * DTOではDTO内メンバーに直接収容しているため、それぞれが対応する形に互換させること。 <br>
 * 本アノテーションは DTOでの指定専用です。
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })

public @interface RestBeanConvertIdClassSet {
	Class<?> infoClass(); // id変換の対象となるinfoクラス

	String idName(); // infoクラス内のid変換対象メンバの名称
}
