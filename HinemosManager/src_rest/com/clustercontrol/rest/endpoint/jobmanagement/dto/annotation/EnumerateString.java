/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.clustercontrol.rest.dto.EnumDto;

/**
 * JSONに列挙値で指定されたパラメータを内部コード(String)の変数に変換します。<br>
 * 変換の定義Enum(EnumDto)をenumDtoに指定してください。<br>
 * <br>
 * ・列挙型(AAA) <-> 内部コード("AAA")
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface EnumerateString {
	Class<? extends EnumDto<String>> enumDto();
}
