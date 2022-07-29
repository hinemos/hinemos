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
 * REST向けのリソースメソッドに関する、ログの出力用アノテーション。
 * <br>
 * 共通基盤による自動的な操作ログの出力を抑止したい場合に指定します。
 */
@Retention(RetentionPolicy.RUNTIME)  
@Target({ 
	ElementType.TYPE,
	ElementType.METHOD
})
public @interface RestLogPut {
	boolean auto();
}
