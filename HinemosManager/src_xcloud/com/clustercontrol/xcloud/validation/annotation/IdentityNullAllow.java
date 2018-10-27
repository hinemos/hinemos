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

@Retention(RetentionPolicy.RUNTIME)  
@Target({ElementType.METHOD, ElementType.PARAMETER})
@ValidatedBy(com.clustercontrol.xcloud.validation.ValidationUtil.IdNullAllowValidator.class)
public @interface IdentityNullAllow {
	String elementId() default "";
	int max() default 64;
	String validationId() default "com.clustercontrol.xcloud.validation.annotation.IdentityNullAllow";
	String[] groups() default {};
}
