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

/**
 * 【仕様】
 * @RestValidateString(type = CheckType.ID, notNull = true)
 * 値あり(ID有効文字列) : OK
 * 値あり(ID無効文字列) : NG
 * 値なし(空文字列)     : OK
 * 値なし(null)         : NG
 * ※ minLen = 0 指定時も同様の動作
 * 
 * @RestValidateString(type = CheckType.ID, notNull = true, minLen = 1)
 * 値あり(ID有効文字列) : OK
 * 値あり(ID無効文字列) : NG
 * 値なし(空文字列)     : NG
 * 値なし(null)         : NG
 * 
 * @RestValidateString(type = CheckType.ID, notNull = false)
 * 値あり(ID有効文字列) : OK
 * 値あり(ID無効文字列) : NG
 * 値なし(空文字列)     : OK
 * 値なし(null)         : OK
 * ※ minLen = 0 指定時も同様の動作
 * 
 * @RestValidateString(type = CheckType.ID, notNull = false, minLen = 1)
 * 値あり(ID有効文字列) : OK
 * 値あり(ID無効文字列) : NG
 * 値なし(空文字列)     : NG
 * 値なし(null)         : OK
 */

@Retention(RetentionPolicy.RUNTIME)  
@Target({ 
	ElementType.FIELD,
	ElementType.PARAMETER
})
public @interface RestValidateString {

	//チェックの型
	public enum CheckType{
		ID,       //ID向けの禁則文字チェック
		COLLECT, //収集値向け禁則文字チェック
		NULL      //通常
	}

	//null不可
	boolean notNull() default false;

	//最小長
	int minLen() default -1;

	//最大長
	int maxLen() default -1;

	//チェックの型指定
	CheckType type() default CheckType.NULL;
}
