/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.ws.xcloud.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;

@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.METHOD)
public @interface HinemosAccessRight {
	String roleName();
	SystemPrivilegeMode[] right() default SystemPrivilegeMode.READ;
}
