/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.platform.conv;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.platform.xml.MailTemplateInfo;
import com.clustercontrol.utility.util.Config;

/**
 * メールテンプレート情報をJavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 * 
 * @version 6.1.0
 * @since 1.0.0
 * 
 */
public class MailTemplateConv {
	
	static private final String schemaType="E";
	static private final String schemaVersion="1";
	static private final String schemaRevision="2" ;

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
	 * メールテンプレート定義に関して、XML BeanからHinemos Beanへ変換する。
	 * 
	 * @param notifyInfo メールテンプレート定義 XML Bean
	 * @return メールテンプレート定義 Hinemos Bean
	 */
	public static com.clustercontrol.ws.mailtemplate.MailTemplateInfo getMailTemplateInfoData(MailTemplateInfo mailTemplateInfo) {
		com.clustercontrol.ws.mailtemplate.MailTemplateInfo ret = new com.clustercontrol.ws.mailtemplate.MailTemplateInfo();
		
		// 登録日時、更新日時に利用する日時（実行日時とする）
		long now = new Date().getTime();
		
		if(mailTemplateInfo.getMailTemplateId() != null &&
				!mailTemplateInfo.getMailTemplateId().equals("")){
			ret.setMailTemplateId(mailTemplateInfo.getMailTemplateId());
		}else{
			log.warn(Messages.getString("SettingTools.EssentialValueInvalid") 
					+ "(MailTemplateId) : " + mailTemplateInfo.toString());
			return null;
		}
		if(mailTemplateInfo.getDescription() != null){
			ret.setDescription(mailTemplateInfo.getDescription());
		}
		
		ret.setOwnerRoleId(mailTemplateInfo.getOwnerRoleId());
		ret.setSubject(mailTemplateInfo.getSubject());
		ret.setBody(mailTemplateInfo.getBody());

		ret.setRegDate(now);
		ret.setUpdateDate(now);
		ret.setRegUser(Config.getConfig("Login.USER"));
		ret.setUpdateUser(Config.getConfig("Login.USER"));
		
		return ret;
	}
	
	/**
	 * メールテンプレート定義に関して、Hinemos BeanからXML Beanへ変換する。
	 * 
	 * @param mailTemplateInfo メールテンプレート定義 Hinemos Bean
	 * @return メールテンプレート定義 XML Bean
	 */
	public static MailTemplateInfo getMailTemplateInfo(com.clustercontrol.ws.mailtemplate.MailTemplateInfo mailTemplateInfo) {
		MailTemplateInfo ret = new MailTemplateInfo();
		
		ret.setMailTemplateId(mailTemplateInfo.getMailTemplateId());
		ret.setDescription(mailTemplateInfo.getDescription());
		ret.setOwnerRoleId(mailTemplateInfo.getOwnerRoleId());
		ret.setSubject(mailTemplateInfo.getSubject());
		ret.setBody(mailTemplateInfo.getBody());
		
		return ret;
	}
	
}
