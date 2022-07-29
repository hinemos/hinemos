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
 * RestApiSystemPrivilegeSetting を @Repeatable指定 にするために存在し、直接利用されることは想定していません。
 */
@Retention(RetentionPolicy.RUNTIME)  
@Target({ 
	ElementType.TYPE,
	ElementType.METHOD
})
public @interface RepeatRestSystemPrivilegeSetting {
	RestSystemPrivilege[]  value();
}
