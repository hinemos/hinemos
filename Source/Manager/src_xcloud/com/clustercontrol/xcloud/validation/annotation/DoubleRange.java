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
@Target(ElementType.METHOD)
@ValidatedBy(com.clustercontrol.xcloud.validation.ValidationUtil.DoubleRangeValidator.class)
public @interface DoubleRange {
	String elementId() default "";
	double min() default Double.MIN_VALUE;
	double max() default Double.MAX_VALUE;
	String validationId() default "com.clustercontrol.xcloud.validation.annotation.DoubleRange";
	String[] groups() default {};
}
