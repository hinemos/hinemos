/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.settings.platform.conv;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.RestAccessAuthHttpHeaderResponse;
import org.openapitools.client.model.RestAccessInfoResponse;
import org.openapitools.client.model.RestAccessSendHttpHeaderResponse;

import com.clustercontrol.notify.restaccess.bean.HttpMethodMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.platform.xml.AuthHttpHeaders;
import com.clustercontrol.utility.settings.platform.xml.RestAccessInfo;
import com.clustercontrol.utility.settings.platform.xml.SendHttpHeaders;
import com.clustercontrol.version.util.VersionUtil;

/**
 * RESTアクセス情報をJavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 * 
 */

public class RestAccessInfoConv  {
	
	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	static private final String schemaType=VersionUtil.getSchemaProperty("PLATFORM.RESTACCESSINFO.SCHEMATYPE");
	static private final String schemaVersion=VersionUtil.getSchemaProperty("PLATFORM.RESTACCESSINFO.SCHEMAVERSION");
	static private final String schemaRevision=VersionUtil.getSchemaProperty("PLATFORM.RESTACCESSINFO.SCHEMAREVISION");

	private static Log log = LogFactory.getLog(MailTemplateConv.class);

	/**
	 * XMLとツールの対応バージョンをチェック */
	static public int checkSchemaVersion(String type, String version ,String revision){
		return BaseConv.checkSchemaVersion(
				schemaType,schemaVersion,schemaRevision,
				type,version,revision);
	}

	/**
	 * スキーマのバージョンを返します。
	 * @return
	 */
	static public com.clustercontrol.utility.settings.platform.xml.SchemaInfo getSchemaVersion(){
	
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo schema =
			new com.clustercontrol.utility.settings.platform.xml.SchemaInfo();
		
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		
		return schema;
	}
	
	
	/**
	 * RESTアクセス情報に関して、XML BeanからHinemos Beanへ変換する。
	 * 
	 * @param notifyInfo RESTアクセス情報 XML Bean
	 * @return RESTアクセス情報 Hinemos Bean
	 */
	public static RestAccessInfoResponse getRestAccessInfoResponse(RestAccessInfo restAccessInfo) {
		RestAccessInfoResponse ret = new RestAccessInfoResponse();
		
		if(restAccessInfo.getRestAccessId() != null &&
				!restAccessInfo.getRestAccessId().equals("")){
			ret.setRestAccessId(restAccessInfo.getRestAccessId());
		}else{
			log.warn(Messages.getString("SettingTools.EssentialValueInvalid") 
					+ "(RestAccessId) : " + restAccessInfo.toString());
			return null;
		}
		ret.setDescription(restAccessInfo.getDescription());
		
		ret.setOwnerRoleId(restAccessInfo.getOwnerRoleId());
		ret.setSendUrlString(restAccessInfo.getSendUrlString());
		if( restAccessInfo.getSendHttpMethodType()!= null){
			ret.setSendHttpMethodType(
					RestAccessInfoResponse.SendHttpMethodTypeEnum.fromValue(restAccessInfo.getSendHttpMethodType()));
		}
		ret.setSendHttpHeaders(getSendHttpHeaderResponseList(restAccessInfo.getSendHttpHeaders()));
		ret.setSendHttpBody(restAccessInfo.getSendHttpBody());
		if( restAccessInfo.getAuthType()!= null){
			ret.setAuthType(RestAccessInfoResponse.AuthTypeEnum.fromValue(restAccessInfo.getAuthType()));
		}
		ret.setAuthBasicUser(restAccessInfo.getAuthBasicUser());
		ret.setAuthBasicPassword(restAccessInfo.getAuthBasicPassword());
		ret.setAuthUrlString(restAccessInfo.getAuthUrlString());
		if (restAccessInfo.getAuthUrlMethodType() != null) {
			ret.setAuthUrlMethodType(
					RestAccessInfoResponse.AuthUrlMethodTypeEnum.fromValue(restAccessInfo.getAuthUrlMethodType()));
		} else {
			// 認証設定のメソッドはURL認証以外の場合は入力の必要がないため、nullにならないようにデフォルト値で補完する
			ret.setAuthUrlMethodType(
					RestAccessInfoResponse.AuthUrlMethodTypeEnum.fromValue(HttpMethodMessage.getHttpMethodDefault()));
		}
		ret.setAuthHttpHeaders(getAuthHttpHeaderResponseList(restAccessInfo.getAuthHttpHeaders()));
		ret.setAuthUrlBody(restAccessInfo.getAuthUrlBody());
		ret.setAuthUrlGetRegex(restAccessInfo.getAuthUrlGetRegex() );
		if (restAccessInfo.hasAuthUrlValidTerm()) {
			ret.setAuthUrlValidTerm(restAccessInfo.getAuthUrlValidTerm());
		}
		if (restAccessInfo.hasHttpConnectTimeout()) {
			ret.setHttpConnectTimeout(restAccessInfo.getHttpConnectTimeout());
		}
		if (restAccessInfo.hasHttpRequestTimeout()) {
			ret.setHttpRequestTimeout(restAccessInfo.getHttpRequestTimeout());
		}
		if (restAccessInfo.hasHttpRetryNum()) {
			ret.setHttpRetryNum(restAccessInfo.getHttpRetryNum());
		}
		if (restAccessInfo.hasUseWebProxy()) {
			ret.setUseWebProxy(restAccessInfo.getUseWebProxy());
		}
		ret.setWebProxyUrlString(restAccessInfo.getWebProxyUrlString());
		if (restAccessInfo.hasWebProxyPort()) {
			ret.setWebProxyPort(restAccessInfo.getWebProxyPort());
		}
		ret.setWebProxyAuthUser(restAccessInfo.getWebProxyAuthUser());
		ret.setWebProxyAuthPassword(restAccessInfo.getWebProxyAuthPassword());
		
		return ret;
	}
	
	/**
	 * RESTアクセス情報に関して、Hinemos BeanからXML Beanへ変換する。
	 * 
	 * @param restAccessInfo RESTアクセス情報 Hinemos Bean
	 * @return RESTアクセス情報 XML Bean
	 */
	public static RestAccessInfo getRestAccessInfo(RestAccessInfoResponse restAccessInfo) {
		RestAccessInfo ret = new RestAccessInfo();
		
		ret.setRestAccessId(restAccessInfo.getRestAccessId());
		ret.setDescription(restAccessInfo.getDescription());
		ret.setOwnerRoleId(restAccessInfo.getOwnerRoleId());
		ret.setSendUrlString(restAccessInfo.getSendUrlString());
		if (restAccessInfo.getSendHttpMethodType() != null) {
			ret.setSendHttpMethodType(restAccessInfo.getSendHttpMethodType().getValue());
		}
		if (restAccessInfo.getSendHttpHeaders() != null) {
			ret.setSendHttpHeaders(getSendHttpHeadersArray(restAccessInfo.getSendHttpHeaders()));
		}
		ret.setSendHttpBody(restAccessInfo.getSendHttpBody());
		if (restAccessInfo.getAuthType() != null) {
			ret.setAuthType(restAccessInfo.getAuthType().getValue());
		}
		ret.setAuthBasicUser(restAccessInfo.getAuthBasicUser());
		ret.setAuthBasicPassword(restAccessInfo.getAuthBasicPassword());
		ret.setAuthUrlString(restAccessInfo.getAuthUrlString());
		if (restAccessInfo.getAuthUrlMethodType() != null) {
			ret.setAuthUrlMethodType(restAccessInfo.getAuthUrlMethodType().getValue());
		}
		if (restAccessInfo.getAuthHttpHeaders() != null) {
			ret.setAuthHttpHeaders(getAuthHttpHeadersArray(restAccessInfo.getAuthHttpHeaders()));
		}
		ret.setAuthUrlBody(restAccessInfo.getAuthUrlBody());
		ret.setAuthUrlGetRegex(restAccessInfo.getAuthUrlGetRegex() );
		if (restAccessInfo.getAuthUrlValidTerm() != null) {
			ret.setAuthUrlValidTerm(restAccessInfo.getAuthUrlValidTerm());
		}
		if (restAccessInfo.getHttpConnectTimeout() != null) {
			ret.setHttpConnectTimeout(restAccessInfo.getHttpConnectTimeout());
		}
		if (restAccessInfo.getHttpRequestTimeout() != null) {
			ret.setHttpRequestTimeout(restAccessInfo.getHttpRequestTimeout());
		}
		if (restAccessInfo.getHttpRetryNum() != null) {
			ret.setHttpRetryNum( restAccessInfo.getHttpRetryNum());
		}
		if (restAccessInfo.getUseWebProxy() != null) {
			ret.setUseWebProxy(restAccessInfo.getUseWebProxy());
		}
		ret.setWebProxyUrlString(restAccessInfo.getWebProxyUrlString());
		if (restAccessInfo.getWebProxyPort() != null) {
			ret.setWebProxyPort(restAccessInfo.getWebProxyPort());
		}
		ret.setWebProxyAuthUser(restAccessInfo.getWebProxyAuthUser());
		ret.setWebProxyAuthPassword(restAccessInfo.getWebProxyAuthPassword());
		
		return ret;
	}
	
	/**
	 * 認証用HTTPヘッダーに関して、XML BeanからHinemos Beanへ変換する。
	 * 
	 * @param headerArray 認証用HTTPヘッダー XML Bean
	 * @return 認証用HTTPヘッダー Hinemos Bean
	 */
	private static List<RestAccessAuthHttpHeaderResponse> getAuthHttpHeaderResponseList(AuthHttpHeaders[] headerArray) {
		if (headerArray == null) {
			return null;
		}
		List<RestAccessAuthHttpHeaderResponse> retList = new ArrayList<RestAccessAuthHttpHeaderResponse>();
		for (AuthHttpHeaders xmlRec : headerArray) {
			RestAccessAuthHttpHeaderResponse transRec = new RestAccessAuthHttpHeaderResponse();
			transRec.setHeaderOrderNo(xmlRec.getHeaderOrderNo());
			transRec.setKey(xmlRec.getKey());
			transRec.setValue(xmlRec.getValue());
			retList.add(transRec);
		}
		return retList;
	}
	/**
	 * 送信用HTTPヘッダーに関して、XML BeanからHinemos Beanへ変換する。
	 * 
	 * @param headerArray送信用HTTPヘッダー XML Bean
	 * @return 送信用HTTPヘッダー Hinemos Bean
	 */
	private static List<RestAccessSendHttpHeaderResponse> getSendHttpHeaderResponseList(SendHttpHeaders[] headerArray) {
		if (headerArray == null) {
			return null;
		}
		List<RestAccessSendHttpHeaderResponse> retList = new ArrayList<RestAccessSendHttpHeaderResponse>();
		for (SendHttpHeaders xmlRec : headerArray) {
			RestAccessSendHttpHeaderResponse transRec = new RestAccessSendHttpHeaderResponse();
			transRec.setHeaderOrderNo(xmlRec.getHeaderOrderNo());
			transRec.setKey(xmlRec.getKey());
			transRec.setValue(xmlRec.getValue());
			retList.add(transRec);
		}
		return retList;
	}

	/**
	 * 認証用HTTPヘッダーに関して、Hinemos Beanから XML Bean へ変換する。
	 * 
	 * @param 認証用HTTPヘッダー
	 *            Hinemos Bean
	 * @return headerArray 認証用HTTPヘッダー XML Bean
	 */
	private static AuthHttpHeaders[] getAuthHttpHeadersArray(List<RestAccessAuthHttpHeaderResponse> headerList) {
		if (headerList == null) {
			return null;
		}
		AuthHttpHeaders[] retArray = new AuthHttpHeaders[headerList.size()];
		for (int recCount = 0, maxCount = headerList.size(); recCount < maxCount; recCount++) {
			AuthHttpHeaders transRec = new AuthHttpHeaders();
			RestAccessAuthHttpHeaderResponse orgRec = headerList.get(recCount);
			transRec.setHeaderOrderNo(orgRec.getHeaderOrderNo());
			transRec.setKey(orgRec.getKey());
			transRec.setValue(orgRec.getValue());
			retArray[recCount] = transRec;
		}
		return retArray;
	}
	/**
	 * 認証用HTTPヘッダーに関して、Hinemos Beanから XML Bean へ変換する。
	 * 
	 * @param 認証用HTTPヘッダー Hinemos Bean
	 * @return headerArray 認証用HTTPヘッダー XML Bean
	 */
	private static SendHttpHeaders[] getSendHttpHeadersArray(List<RestAccessSendHttpHeaderResponse> headerList) {
		if (headerList == null) {
			return null;
		}
		SendHttpHeaders[] retArray = new SendHttpHeaders[headerList.size()];
		for (int recCount = 0, maxCount = headerList.size(); recCount < maxCount; recCount++) {
			SendHttpHeaders transRec = new SendHttpHeaders();
			RestAccessSendHttpHeaderResponse orgRec = headerList.get(recCount);
			transRec.setHeaderOrderNo(orgRec.getHeaderOrderNo());
			transRec.setKey(orgRec.getKey());
			transRec.setValue(orgRec.getValue());
			retArray[recCount] = transRec;
		}
		return retArray;
	}
}