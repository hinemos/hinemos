/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.rpa.conv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openapitools.client.model.RpaManagementToolAccountResponse;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RpaManagementToolAccountNotFound;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.rpa.xml.RpaManagementToolAccount;
import com.clustercontrol.utility.settings.rpa.xml.RpaManagementToolAccountInfo;
import com.clustercontrol.utility.settings.rpa.xml.RpaManagementToolAccounts;
import com.clustercontrol.version.util.VersionUtil;

public class RpaManagementToolAccountConv {
	
	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	static private final String schemaType=VersionUtil.getSchemaProperty("RPA.RPAMANAGEMENTTOOLACCOUNT.SCHEMATYPE");
	static private final String schemaVersion=VersionUtil.getSchemaProperty("RPA.RPAMANAGEMENTTOOLACCOUNT.SCHEMAVERSION");
	static private final String schemaRevision=VersionUtil.getSchemaProperty("RPA.RPAMANAGEMENTTOOLACCOUNT.SCHEMAREVISION");
	
	/* ロガー */
	private static Logger log = Logger.getLogger(RpaManagementToolAccountConv.class);
	/**
	 * Versionなどの共通部分について、DTOからXMLのBeanに変換します。
	 * 
	 * @param ver　Version情報などのハッシュテーブル。
	 * @return
	 */
	public static com.clustercontrol.utility.settings.rpa.xml.Common
			versionRpaDto2Xml(Hashtable<String,String> ver){
	
		com.clustercontrol.utility.settings.rpa.xml.Common com =
				new com.clustercontrol.utility.settings.rpa.xml.Common();
				
		com.setHinemosVersion(ver.get("hinemosVersion"));
		com.setToolVersion(ver.get("toolVersion"));
		com.setGenerator(ver.get("generator"));
		com.setAuthor(System.getProperty("user.name"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		com.setGenerateDate(dateFormat.format(new Date()));
		com.setRuntimeHost(ver.get("runtimeHost"));
		com.setConnectedManager(ver.get("connectedManager"));
		
		return com;
	}
	
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
	static public com.clustercontrol.utility.settings.rpa.xml.SchemaInfo getSchemaVersion(){
	
		com.clustercontrol.utility.settings.rpa.xml.SchemaInfo schema =
			new com.clustercontrol.utility.settings.rpa.xml.SchemaInfo();
		
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		
		return schema;
	}
	
	public static RpaManagementToolAccount getRpaManagementToolAccount(RpaManagementToolAccountResponse accountRes)
			throws IndexOutOfBoundsException, ParseException {

		RpaManagementToolAccount account = new RpaManagementToolAccount();
		
		RpaManagementToolAccountInfo accountInfo = new RpaManagementToolAccountInfo();
		// findbugs対応 null チェック追加
		if(accountRes != null){
			accountInfo.setRpaScopeId(accountRes.getRpaScopeId());
			accountInfo.setRpaScopeName(accountRes.getRpaScopeName());
			accountInfo.setDescription(accountRes.getDescription());
			accountInfo.setOwnerRoleId(accountRes.getOwnerRoleId());
			accountInfo.setRpaManagementToolId(accountRes.getRpaManagementToolId());
			accountInfo.setUrl(accountRes.getUrl());
			accountInfo.setAccountId(accountRes.getAccountId());
			accountInfo.setPassword(accountRes.getPassword());
			accountInfo.setTenantName(accountRes.getTenantName());
			accountInfo.setDisplayName(accountRes.getDisplayName());
			accountInfo.setProxyFlg(accountRes.getProxyFlg());
		}
		if(accountRes != null && accountRes.getProxyFlg()){
			if(accountRes.getProxyUrl() != null){
				accountInfo.setProxyUrl(accountRes.getProxyUrl());
			}
			// findbugs対応 不要なnullチェックを削除
			if(accountRes.getProxyPort() != null){
				accountInfo.setProxyPort(accountRes.getProxyPort());
			}
			if(accountRes.getProxyUser() != null){
				accountInfo.setProxyUser(accountRes.getProxyUser());
			}
			if(accountRes.getProxyPassword() != null){
				accountInfo.setProxyPassword(accountRes.getProxyPassword());
			}
		}
		
		account.setRpaManagementToolAccountInfo(accountInfo);

		return account;
	}
	
	/**
	 * Castor で作成した形式の RPAシナリオ設定情報を DTO へ変換する<BR>
	 *
	 */
	public static List<RpaManagementToolAccountResponse> createRpaManagementToolAccountList(RpaManagementToolAccounts accounts) 
			throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, RpaManagementToolAccountNotFound, InvalidSetting, ParseException {
		List<RpaManagementToolAccountResponse> accountList = new LinkedList<RpaManagementToolAccountResponse>();

		for (RpaManagementToolAccount account : accounts.getRpaManagementToolAccount()) {
			log.debug("RPA scope Id : " + account.getRpaManagementToolAccountInfo().getRpaScopeId());
			RpaManagementToolAccountResponse accountInfo = 
					createRpaManagementToolAccount(account.getRpaManagementToolAccountInfo());

			accountList.add(accountInfo);
		}

		return accountList;
	}
	
	public static RpaManagementToolAccountResponse createRpaManagementToolAccount(RpaManagementToolAccountInfo info) {
	
		RpaManagementToolAccountResponse ret =new RpaManagementToolAccountResponse();
		
		try {
			ret.setRpaScopeId(info.getRpaScopeId());
			ret.setRpaScopeName(info.getRpaScopeName());
			ret.setDescription(info.getDescription());
			ret.setOwnerRoleId(info.getOwnerRoleId());
			ret.setRpaManagementToolId(info.getRpaManagementToolId());
			ret.setUrl(info.getUrl());
			ret.setAccountId(info.getAccountId());
			ret.setPassword(info.getPassword());
			ret.setTenantName(info.getTenantName());
			ret.setDisplayName(info.getDisplayName());
			ret.setProxyFlg(info.getProxyFlg());
			if(info.hasProxyFlg() && info.getProxyFlg()){
				if(info.getProxyUrl() != null){
					ret.setProxyUrl(info.getProxyUrl());
				}
				if(info.hasProxyPort()){
					ret.setProxyPort((int) info.getProxyPort());
				}
				if(info.getProxyUser() != null){
					ret.setProxyUser(info.getProxyUser());
				}
				if(info.getProxyPassword() != null){
					ret.setProxyPassword(info.getProxyPassword());
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	
		return ret;
	}
}
