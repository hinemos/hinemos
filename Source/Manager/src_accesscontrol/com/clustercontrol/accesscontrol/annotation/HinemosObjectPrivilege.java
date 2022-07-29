/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface HinemosObjectPrivilege {
	String objectType();

	boolean isModifyCheck() default false;

	/**
	 * エンティティのID(PK)がオブジェクト権限のオブジェクトIDと等しくない場合、
	 * オブジェクトIDからPKオブジェクトを生成する public static メソッドの名前を指定してください。
	 */
	String idFactory() default "";

	/**
	 * オブジェクト権限機能マーカーとしての作用を持たせるなら true にします。
	 * デフォルトは false です。
	 */
	boolean objectPrivilegeAvailable() default false;

}
