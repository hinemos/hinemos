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

@Retention(RetentionPolicy.RUNTIME)  
@Target({ 
	ElementType.FIELD,
	ElementType.PARAMETER
})
public @interface RestValidateCollection {
	
	//null不可
	boolean notNull() default false; 
	
	//最小サイズ
	int minSize() default -1;
	
	//最大サイズ
	int maxSize() default -1;
}
