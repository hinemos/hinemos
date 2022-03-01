/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * REST向けのリソースメソッドに関する、実行に必要なシステム権限の指定用アノテーション。
 * <br>
 * admin権限が必要な場合に設定します。
 */
@Retention(RetentionPolicy.RUNTIME)  
@Target({ 
	ElementType.TYPE,
	ElementType.METHOD
})
public @interface RestSystemAdminPrivilege {
	boolean isNeed();
}
