/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.bean;

import org.openapitools.client.model.RestAccessInfoResponse;

import com.clustercontrol.util.Messages;

public class HttpMethodMessage {
	/** GET */
	public static final String STRING_GET = Messages.getString("http.method.get");

	/** POST */
	public static final String STRING_POST = Messages.getString("http.method.post");

	/** PUT */
	public static final String STRING_PUT = Messages.getString("http.method.put");

	/** DELETE */
	public static final String STRING_DELETE = Messages.getString("http.method.delete");

	/**
	 * 種別から文字列に変換します。
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeEnumValueToString(String type) {
		if( type == null ){
			return "";
		}
		if (RestAccessInfoResponse.SendHttpMethodTypeEnum.GET.getValue().equals(type)) {
			return STRING_GET;
		} else if (RestAccessInfoResponse.SendHttpMethodTypeEnum.POST.getValue().equals(type)) {
			return STRING_POST;
		} else if (RestAccessInfoResponse.SendHttpMethodTypeEnum.PUT.getValue().equals(type)) {
			return STRING_PUT;
		} else if (RestAccessInfoResponse.SendHttpMethodTypeEnum.DELETE.getValue().equals(type)) {
			return STRING_DELETE;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。
	 * 
	 * @param string 文字列
	 * @return 種別
	 */
	public static String stringToTypeEnumValue(String string) {
		if (string.equals(STRING_GET)) {
			return RestAccessInfoResponse.SendHttpMethodTypeEnum.GET.getValue();
		} else if (string.equals(STRING_POST)) {
			return RestAccessInfoResponse.SendHttpMethodTypeEnum.POST.getValue();
		} else if (string.equals(STRING_PUT)) {
			return RestAccessInfoResponse.SendHttpMethodTypeEnum.PUT.getValue();
		} else if (string.equals(STRING_DELETE)) {
			return RestAccessInfoResponse.SendHttpMethodTypeEnum.DELETE.getValue();
		}
		return null;
	}

	/**
	 * HTTPメソッドのデフォルト値です。
	 * 
	 * @return
	 */
	public static String getHttpMethodDefault() {
		return STRING_GET;
	}

}
