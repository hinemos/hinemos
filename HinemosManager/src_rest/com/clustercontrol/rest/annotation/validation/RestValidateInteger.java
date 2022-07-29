/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.annotation.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.clustercontrol.util.MessageConstant;

@Retention(RetentionPolicy.RUNTIME)  
@Target({ 
	ElementType.FIELD,
	ElementType.PARAMETER
})
public @interface RestValidateInteger {

	//null不可
	boolean notNull() default false; 
	
	//最小値
	int minVal() default Integer.MIN_VALUE;
	
	//最大値
	int maxVal() default Integer.MAX_VALUE;

}
