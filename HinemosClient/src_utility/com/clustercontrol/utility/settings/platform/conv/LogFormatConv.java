/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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
import org.openapitools.client.model.AddLogFormatRequest;
import org.openapitools.client.model.LogFormatKeyRequest;
import org.openapitools.client.model.LogFormatKeyRequest.KeyTypeEnum;
import org.openapitools.client.model.LogFormatKeyRequest.ValueTypeEnum;
import org.openapitools.client.model.LogFormatKeyResponse;
import org.openapitools.client.model.LogFormatResponse;

import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.platform.xml.LogFormatInfo;
import com.clustercontrol.utility.settings.platform.xml.LogFormatKey;
import com.clustercontrol.version.util.VersionUtil;

/**
 * ログフォーマット情報をJavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 *
 * @version 6.1.0
 * @since 6.0.0
 *
 */
public class LogFormatConv {

	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	static private final String schemaType=VersionUtil.getSchemaProperty("PLATFORM.LOGFORMAT.SCHEMATYPE");
	static private final String schemaVersion=VersionUtil.getSchemaProperty("PLATFORM.LOGFORMAT.SCHEMAVERSION");
	static private final String schemaRevision=VersionUtil.getSchemaProperty("PLATFORM.LOGFORMAT.SCHEMAREVISION");

	private static Log log = LogFactory.getLog(LogFormatConv.class);

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
	 * ログフォーマット定義に関して、XML BeanからHinemos Beanへ変換する。
	 *
	 * @param notifyInfo ログフォーマット定義 XML Bean
	 * @return ログフォーマット定義 Hinemos Bean
	 */
	public static AddLogFormatRequest getLogFormatData(LogFormatInfo logformat) {
		AddLogFormatRequest ret = new AddLogFormatRequest();

		// LogFormatID
		if(logformat.getLogFormatId() != null &&
				!logformat.getLogFormatId().equals("")){
			ret.setLogFormatId(logformat.getLogFormatId());
		}else{
			log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
					+ "(MailTemplateId) : " + logformat.toString());
			return null;
		}

		// Description
		if(logformat.getDescription() != null){
			ret.setDescription(logformat.getDescription());
		} else {
			ret.setDescription("");
		}

		// Tags
		LogFormatKeyRequest logFormatKey = null;
		for (LogFormatKey key : logformat.getLogFormatKey()) {
			logFormatKey = new LogFormatKeyRequest();
			logFormatKey.setKey(key.getKey());
			if(key.getDescription() != null) {
				logFormatKey.setDescription(key.getDescription());
			} else {
				logFormatKey.setDescription("");
			}
			ValueTypeEnum[] valueTypeList = ValueTypeEnum.values();
			ValueTypeEnum valueType = valueTypeList[key.getValueType()];
			logFormatKey.setValueType(valueType);

			KeyTypeEnum[] keyTypeList = KeyTypeEnum.values();
			KeyTypeEnum keyType = keyTypeList[key.getKeyType()];
			logFormatKey.setKeyType(keyType);

			logFormatKey.setPattern(ifNull2Empty(key.getPattern()));
			if(key.getValue() != null
					&& key.getKeyType() == 1){
				logFormatKey.setValue(key.getValue());
			} else if(key.getValue() == null
					&& key.getKeyType() == 1) {
				logFormatKey.setValue("");
			}

			ret.getKeyPatternList().add(logFormatKey);
		}
		// timestamp regex
		if(logformat.getTimestampRegex() != null){
			ret.setTimestampRegex(logformat.getTimestampRegex());
		} else {
			ret.setTimestampRegex("");
		}
		// timestamp format
		if(logformat.getTimestampFormat() != null){
			ret.setTimestampFormat(logformat.getTimestampFormat());
		} else {
			ret.setTimestampFormat("");
		}

		ret.setOwnerRoleId(logformat.getOwnerRoleId());
		
		return ret;
	}

	/**
	 * ログフォーマット定義に関して、Hinemos BeanからXML Beanへ変換する。
	 *
	 * @param mailTemplateInfo ログフォーマット定義 Hinemos Bean
	 * @return ログフォーマット定義 XML Bean
	 */
	public static LogFormatInfo getLogFormat(LogFormatResponse logformat) {
		LogFormatInfo ret = new LogFormatInfo();

		ret.setLogFormatId(logformat.getLogFormatId());
		if(logformat.getDescription() != null
				&& !"".equals(logformat.getDescription())){
			ret.setDescription(logformat.getDescription());
		}

		List<LogFormatKey> logFormatKeyList = new ArrayList<LogFormatKey>();
		LogFormatKey logFormatKey = null;
		for (LogFormatKeyResponse key : logformat.getKeyPatternList()) {
			logFormatKey = new LogFormatKey();
			logFormatKey.setKey(key.getKey());
			if(key.getDescription() != null
					&& !"".equals(key.getDescription())){
				logFormatKey.setDescription(key.getDescription());
			}
			logFormatKey.setValueType(key.getValueType().ordinal());
			logFormatKey.setKeyType(key.getKeyType().ordinal());
			if(key.getPattern() != null
					&& !"".equals(key.getPattern())){
				logFormatKey.setPattern(key.getPattern());
			}
			if(key.getValue() != null
					&& !"".equals(key.getValue())){
				logFormatKey.setValue(key.getValue());
			}

			logFormatKeyList.add(logFormatKey);

		}
		ret.setLogFormatKey(logFormatKeyList.toArray(new LogFormatKey[0]));
		
		if(logformat.getTimestampRegex() != null
				&& !"".equals(logformat.getTimestampRegex())){
			ret.setTimestampRegex(logformat.getTimestampRegex());
		}
		if(logformat.getTimestampFormat() != null
				&& !"".equals(logformat.getTimestampFormat())){
			ret.setTimestampFormat(logformat.getTimestampFormat());
		}
		ret.setOwnerRoleId(logformat.getOwnerRoleId());

		return ret;
	}
	
	protected static String ifNull2Empty(String str){
		if(str == null){
			return "";
		}
		return str;
	}
}
