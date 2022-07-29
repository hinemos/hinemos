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

import com.clustercontrol.util.MessageConstant;

@Retention(RetentionPolicy.RUNTIME)  
@Target({ 
	ElementType.FIELD,
	ElementType.PARAMETER
})

public @interface RestItemName {

	//項目名称用のメッセージ定義
	MessageConstant value(); 

}
