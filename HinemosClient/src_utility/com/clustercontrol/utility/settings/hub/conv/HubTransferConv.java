/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.hub.conv;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AddTransferInfoRequest;
import org.openapitools.client.model.AddTransferInfoRequest.IntervalEnum;
import org.openapitools.client.model.AddTransferInfoRequest.TransTypeEnum;
import org.openapitools.client.model.TransferDestPropRequest;
import org.openapitools.client.model.TransferDestPropResponse;
import org.openapitools.client.model.TransferInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.hub.xml.TransferDestProp;
import com.clustercontrol.utility.settings.hub.xml.TransferInfo;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.util.OpenApiEnumConverter;
import com.clustercontrol.utility.util.StringUtil;
import com.clustercontrol.version.util.VersionUtil;


/**
 * 転送設定情報をJavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 *
 */
public class HubTransferConv {

	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	static private final String schemaType=VersionUtil.getSchemaProperty("HUB.HUBTRANSFER.SCHEMATYPE");
	static private final String schemaVersion=VersionUtil.getSchemaProperty("HUB.HUBTRANSFER.SCHEMAVERSION");
	static private final String schemaRevision=VersionUtil.getSchemaProperty("HUB.HUBTRANSFER.SCHEMAREVISION");

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
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public static AddTransferInfoRequest getTransferData(TransferInfo transferInfo) throws InvalidSetting, HinemosUnknown {
		AddTransferInfoRequest ret = new AddTransferInfoRequest();

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

		TransferDestPropRequest transferDestProp = null;
		for (TransferDestProp prop : transferInfo.getTransferDestProp()) {
			transferDestProp = new TransferDestPropRequest();
			transferDestProp.setName(prop.getName());
			transferDestProp.setValue(prop.getValue());
			ret.getDestProps().add(transferDestProp);
		}
		ret.setOwnerRoleId(transferInfo.getOwnerRoleId());

		//DataType
		AddTransferInfoRequest.DataTypeEnum[] values = AddTransferInfoRequest.DataTypeEnum.values();
		AddTransferInfoRequest.DataTypeEnum dataType = values[transferInfo.getDataType()];
		ret.setDataType(dataType);

		// DestType
		ret.setDestTypeId(transferInfo.getDestTypeID());

		// TransType
		if (!StringUtil.isNullOrEmpty(transferInfo.getTransType())) {
			TransTypeEnum transTypeEum = TransTypeEnum.valueOf(transferInfo.getTransType());
			ret.setTransType(transTypeEum);
		}
		IntervalEnum intervalEnum = null;
		// XMLにinterval項目がなかったら、対応DTOメンバに 0 が設定されることを考慮（nullとみなす）
		if (transferInfo.getInterval() != 0) {
			intervalEnum = OpenApiEnumConverter.integerToEnum(transferInfo.getInterval(), IntervalEnum.class);
		}
		ret.setInterval(intervalEnum);
		ret.setCalendarId(transferInfo.getCalendarId());

		ret.setValidFlg(transferInfo.getValidFlg());
		return ret;
	}

	/**
	 * 転送設定定義に関して、Hinemos BeanからXML Beanへ変換する。
	 *
	 * @param mailTemplateInfo 転送設定定義 Hinemos Bean
	 * @return 転送設定定義 XML Bean
	 */
	public static TransferInfo getTransferInfo(TransferInfoResponse transferInfo) {
		TransferInfo ret = new TransferInfo();

		ret.setTransferId(transferInfo.getTransferId());

		List<TransferDestProp> transferDestPropList = new ArrayList<TransferDestProp>();
		TransferDestProp transferDestProp = null;
		for (TransferDestPropResponse prop : transferInfo.getDestProps()) {
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
			int interval = OpenApiEnumConverter.enumToInteger(transferInfo.getInterval());
			ret.setInterval(interval);
		}
		ret.setCalendarId(transferInfo.getCalendarId());
		ret.setValidFlg(transferInfo.getValidFlg());
		return ret;
	}
}
