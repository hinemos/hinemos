/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeFunction;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;

/**
 * REST向けリソースメソッドに関する、実行に必要なシステム権限の指定用アノテーション。
 * <br>
 * 機能名とシステム権限の種別を設定します。
 */
@Repeatable(RepeatRestSystemPrivilegeSetting.class)
@Retention(RetentionPolicy.RUNTIME)  
@Target({ 
	ElementType.TYPE,
	ElementType.METHOD
})
public @interface RestSystemPrivilege {
	SystemPrivilegeFunction function();
	SystemPrivilegeMode[] modeList();

}
