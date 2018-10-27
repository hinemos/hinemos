/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.hub.conv;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.hub.xml.TransferDestProp;
import com.clustercontrol.utility.settings.hub.xml.TransferInfo;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.util.Config;


/**
 * 転送設定情報をJavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 *
 */
public class HubTransferConv {

	static private final String schemaType="H";
	static private final String schemaVersion="1";
	static private final String schemaRevision="1" ;

	private static Log log = LogFactory.getLog(HubTransferConv.class);

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
	static public com.clustercontrol.utility.settings.hub.xml.SchemaInfo getSchemaVersion(){

		com.clustercontrol.utility.settings.hub.xml.SchemaInfo schema =
			new com.clustercontrol.utility.settings.hub.xml.SchemaInfo();

		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);

		return schema;
	}


	/**
	 * 転送設定定義に関して、XML BeanからHinemos Beanへ変換する。
	 *
	 * @param notifyInfo 転送設定定義 XML Bean
	 * @return 転送設定定義 Hinemos Bean
	 */
	public static com.clustercontrol.ws.hub.TransferInfo getTransferData(TransferInfo transferInfo) {
		com.clustercontrol.ws.hub.TransferInfo ret = new com.clustercontrol.ws.hub.TransferInfo();

		// 登録日時、更新日時に利用する日時（実行日時とする）
		long now = new Date().getTime();

		// TransferId
		if(transferInfo.getTransferId() != null &&
				!transferInfo.getTransferId().equals("")){
			ret.setTransferId(transferInfo.getTransferId());
		}else{
			log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
					+ "(MailTemplateId) : " + transferInfo.toString());
			return null;
		}

		// Description
		if(transferInfo.getDescription() != null){
			ret.setDescription(transferInfo.getDescription());
		}

		com.clustercontrol.ws.hub.TransferDestProp transferDestProp = null;
		for (TransferDestProp prop : transferInfo.getTransferDestProp()) {
			transferDestProp = new com.clustercontrol.ws.hub.TransferDestProp();
			transferDestProp.setName(prop.getName());
			transferDestProp.setValue(prop.getValue());
			ret.getDestProps().add(transferDestProp);
		}
		ret.setOwnerRoleId(transferInfo.getOwnerRoleId());

		//DataType
		com.clustercontrol.ws.hub.DataType[] values = com.clustercontrol.ws.hub.DataType.values();
		com.clustercontrol.ws.hub.DataType dataType = values[transferInfo.getDataType()];
		ret.setDataType(dataType);

		// DestType
		ret.setDestTypeId(transferInfo.getDestTypeID());

		// TransType
		ret.setTransType(getEnum(transferInfo.getTransType()));
		ret.setInterval(transferInfo.getInterval());
		ret.setCalendarId(transferInfo.getCalendarId());

		ret.setRegDate(now);
		ret.setUpdateDate(now);
		ret.setRegUser(Config.getConfig("Login.USER"));
		ret.setUpdateUser(Config.getConfig("Login.USER"));
		ret.setValidFlg(transferInfo.getValidFlg());
		return ret;
	}

	private static com.clustercontrol.ws.hub.TransferType getEnum(String str) {
		com.clustercontrol.ws.hub.TransferType[] enumArray = com.clustercontrol.ws.hub.TransferType.values();
		for(com.clustercontrol.ws.hub.TransferType enumStr : enumArray) {
			if (str.equals(enumStr.name().toString())){
				return enumStr;
			}
		}
		return null;
	}


	/**
	 * 転送設定定義に関して、Hinemos BeanからXML Beanへ変換する。
	 *
	 * @param mailTemplateInfo 転送設定定義 Hinemos Bean
	 * @return 転送設定定義 XML Bean
	 */
	public static TransferInfo getTransferInfo(com.clustercontrol.ws.hub.TransferInfo transferInfo) {
		TransferInfo ret = new TransferInfo();

		ret.setTransferId(transferInfo.getTransferId());

		List<TransferDestProp> transferDestPropList = new ArrayList<TransferDestProp>();
		TransferDestProp transferDestProp = null;
		for (com.clustercontrol.ws.hub.TransferDestProp prop : transferInfo.getDestProps()) {
			transferDestProp = new TransferDestProp();
			transferDestProp.setName(prop.getName());
			transferDestProp.setValue(prop.getValue());
			transferDestPropList.add(transferDestProp);

		}
		ret.setTransferDestProp(transferDestPropList.toArray(new TransferDestProp[0]));
		ret.setOwnerRoleId(transferInfo.getOwnerRoleId());
		ret.setDescription(transferInfo.getDescription());
		ret.setDataType(transferInfo.getDataType().ordinal());
		ret.setDestTypeID(transferInfo.getDestTypeId());
		ret.setTransType(transferInfo.getTransType().name());
		if (null != transferInfo.getInterval()){
			ret.setInterval(transferInfo.getInterval());
		}
		ret.setCalendarId(transferInfo.getCalendarId());
		ret.setValidFlg(transferInfo.isValidFlg());
		return ret;
	}
}
