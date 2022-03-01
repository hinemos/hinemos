/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.system.conv;

import org.openapitools.client.model.HinemosPropertyResponse;
import org.openapitools.client.model.HinemosPropertyResponse.TypeEnum;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.maintenance.HinemosPropertyTypeConstant;
import com.clustercontrol.utility.settings.maintenance.xml.HinemosPropertyInfo;
import com.clustercontrol.utility.settings.model.BaseConv;

/**
 * Hinemosプロパティ設定情報をJavaBeanとXML(Bean)のbindingとの間でやりとりを 行うクラス<BR>
 * 
 * @version 6.1.0
 * @since 5.0.a
 * 
 */
public class HinemosPropertyConv extends BaseConv {

	// スキーマのタイプ、バージョン、リビジョンをそれぞれ返す
	@Override
	protected String getType() {
		return "G";
	}

	@Override
	protected String getVersion() {
		return "1";
	}

	@Override
	protected String getRevision() {
		return "1";
	}

	/**
	 * DTOのBeanからXMLのBeanに変換する。
	 * 
	 * @param info
	 *            DTOのBean
	 * @return
	 * @throws Exception
	 */
	public HinemosPropertyInfo getXmlInfo(HinemosPropertyResponse info)
			throws Exception {

		HinemosPropertyInfo ret = new HinemosPropertyInfo();

		// 情報のセット(主部分)
		ret.setKey(info.getKey());
		ret.setDescription(ifNull2Empty(info.getDescription()));
		ret.setOwnerRoleId(ifNull2Empty(info.getOwnerRoleId()));
 
		switch (info.getType()) {
		case STRING:
			ret.setValueType(HinemosPropertyTypeConstant.TYPE_STRING);
			ret.setValue(info.getValue());
			break;
		case NUMERIC:
			ret.setValueType(HinemosPropertyTypeConstant.TYPE_NUMERIC);
			ret.setValue(ifNull2EmptyAndNonNull2String(info.getValue()));
			break;
		case BOOLEAN:
			ret.setValueType(HinemosPropertyTypeConstant.TYPE_TRUTH);
			ret.setValue(ifNull2EmptyAndNonNull2String(info.getValue()));
			break;
		default:
			throw new Exception(info.getKey() + " has undefined value type.");
		}

		return ret;
	}

	public HinemosPropertyResponse getDTO(HinemosPropertyInfo info) throws Exception {
		HinemosPropertyResponse ret = new HinemosPropertyResponse();
		ret.setKey(info.getKey());
		ret.setDescription(ifNull2Empty(info.getDescription()));
		ret.setOwnerRoleId(RoleIdConstant.ADMINISTRATORS);
		ret.setValue(info.getValue());

		switch (info.getValueType()) {
		case HinemosPropertyTypeConstant.TYPE_STRING:
			ret.setType(TypeEnum.STRING);
			break;
		case HinemosPropertyTypeConstant.TYPE_NUMERIC:
			ret.setType(TypeEnum.NUMERIC);
			break;
		case HinemosPropertyTypeConstant.TYPE_TRUTH:
			ret.setType(TypeEnum.BOOLEAN);
			break;
		default:
			throw new Exception(info.getKey() + " has undefined value type.");
		}
		return ret;
	}
}
