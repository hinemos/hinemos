/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.clustercontrol.xcloud.validation.CustomEntityValidator;

@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.TYPE)
public @interface CustomEntityValidation {
	Class<? extends CustomEntityValidator<?>> value();
}
