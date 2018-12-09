/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.platform.conv;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.platform.xml.LogFormatInfo;
import com.clustercontrol.utility.settings.platform.xml.LogFormatKey;
import com.clustercontrol.utility.util.Config;


/**
 * ログフォーマット情報をJavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 *
 * @version 6.1.0
 * @since 6.0.0
 *
 */
public class LogFormatConv {

	static private final String schemaType="H";
	static private final String schemaVersion="1";
	static private final String schemaRevision="1" ;

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
	public static com.clustercontrol.ws.hub.LogFormat getLogFormatData(LogFormatInfo logformat) {
		com.clustercontrol.ws.hub.LogFormat ret = new com.clustercontrol.ws.hub.LogFormat();

		// 登録日時、更新日時に利用する日時（実行日時とする）
		long now = new Date().getTime();

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
		}

		com.clustercontrol.ws.hub.LogFormatKey logFormatKey = null;
		for (LogFormatKey key : logformat.getLogFormatKey()) {
			logFormatKey = new com.clustercontrol.ws.hub.LogFormatKey();
			logFormatKey.setKey(key.getKey());
			if(key.getDescription() != null
					&& !"".equals(key.getDescription())){
				logFormatKey.setDescription(key.getDescription());
			}
			com.clustercontrol.ws.hub.ValueType[] valueTypeList = com.clustercontrol.ws.hub.ValueType.values();
			com.clustercontrol.ws.hub.ValueType valueType = valueTypeList[key.getKeyType()];
			logFormatKey.setValueType(valueType);

			com.clustercontrol.ws.hub.KeyType[] keyTypeList = com.clustercontrol.ws.hub.KeyType.values();
			com.clustercontrol.ws.hub.KeyType keyType = keyTypeList[key.getKeyType()];
			logFormatKey.setKeyType(keyType);

			logFormatKey.setPattern(ifNull2Empty(key.getPattern()));
			if(key.getValue() != null
					&& !"".equals(key.getValue())){
				logFormatKey.setValue(key.getValue());
			}
			
			ret.getKeyPatternList().add(logFormatKey);
		}
		// timestamp regex
		if(logformat.getTimestampRegex() != null
				&& !"".equals(logformat.getTimestampRegex())){
			ret.setTimestampRegex(logformat.getTimestampRegex());
		}
		// timestamp format
		if(logformat.getTimestampFormat() != null
				&& !"".equals(logformat.getTimestampFormat())){
			ret.setTimestampFormat(logformat.getTimestampFormat());
		}

		ret.setOwnerRoleId(logformat.getOwnerRoleId());
		ret.setRegDate(now);
		ret.setUpdateDate(now);
		ret.setRegUser(Config.getConfig("Login.USER"));
		ret.setUpdateUser(Config.getConfig("Login.USER"));

		return ret;
	}

	/**
	 * ログフォーマット定義に関して、Hinemos BeanからXML Beanへ変換する。
	 *
	 * @param mailTemplateInfo ログフォーマット定義 Hinemos Bean
	 * @return ログフォーマット定義 XML Bean
	 */
	public static LogFormatInfo getLogFormat(com.clustercontrol.ws.hub.LogFormat logformat) {
		LogFormatInfo ret = new LogFormatInfo();

		ret.setLogFormatId(logformat.getLogFormatId());
		if(logformat.getDescription() != null
				&& !"".equals(logformat.getDescription())){
			ret.setDescription(logformat.getDescription());
		}

		List<LogFormatKey> logFormatKeyList = new ArrayList<LogFormatKey>();
		LogFormatKey logFormatKey = null;
		for (com.clustercontrol.ws.hub.LogFormatKey key : logformat.getKeyPatternList()) {
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
