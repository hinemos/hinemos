/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.system.conv;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.maintenance.HinemosPropertyTypeConstant;
import com.clustercontrol.utility.settings.maintenance.xml.HinemosPropertyInfo;
import com.clustercontrol.utility.settings.model.BaseConv;

/**
 * Hinemosプロパティ設定情報をJavaBeanとXML(Bean)のbindingとの間でやりとりを
 * 行うクラス<BR>
 * 
 * @version 6.1.0
 * @since 5.0.a
 * 
 */
public class HinemosPropertyConv extends BaseConv {
	
	// スキーマのタイプ、バージョン、リビジョンをそれぞれ返す
	@Override
	protected String getType() {return "G";}
	@Override
	protected String getVersion() {return "1";}
	@Override
	protected String getRevision() {return "1";}
	
	/**
	 * DTOのBeanからXMLのBeanに変換する。
	 * 
	 * @param info　DTOのBean
	 * @return 
	 * @throws Exception 
	 */
	public HinemosPropertyInfo getXmlInfo(com.clustercontrol.ws.maintenance.HinemosPropertyInfo info) throws Exception {

		HinemosPropertyInfo ret = new HinemosPropertyInfo();

		//情報のセット(主部分)
		ret.setKey(info.getKey());
		ret.setDescription(ifNull2Empty(info.getDescription()));
		ret.setOwnerRoleId(ifNull2Empty(info.getOwnerRoleId()));
		
		switch (info.getValueType()){
		case HinemosPropertyTypeConstant.TYPE_STRING:
			ret.setValueType(HinemosPropertyTypeConstant.TYPE_STRING);
			ret.setValue(info.getValueString());
			break;
		case HinemosPropertyTypeConstant.TYPE_NUMERIC:
			ret.setValueType(HinemosPropertyTypeConstant.TYPE_NUMERIC);
			ret.setValue(ifNull2EmptyAndNonNull2String(info.getValueNumeric()));
			break;
		case HinemosPropertyTypeConstant.TYPE_TRUTH:
			ret.setValueType(HinemosPropertyTypeConstant.TYPE_TRUTH);
			ret.setValue(ifNull2EmptyAndNonNull2String(info.isValueBoolean()));
			break;
		default:
			throw new Exception(info.getKey() + " has undefined value type.");
		}

		return ret;
	}

	public com.clustercontrol.ws.maintenance.HinemosPropertyInfo getDTO(HinemosPropertyInfo info) throws Exception {
		com.clustercontrol.ws.maintenance.HinemosPropertyInfo ret = new com.clustercontrol.ws.maintenance.HinemosPropertyInfo();
		ret.setKey(info.getKey());
		ret.setDescription(ifNull2Empty(info.getDescription()));
		ret.setOwnerRoleId(RoleIdConstant.ADMINISTRATORS);

		switch (info.getValueType()){
		case HinemosPropertyTypeConstant.TYPE_STRING:
			ret.setValueType(HinemosPropertyTypeConstant.TYPE_STRING);
			ret.setValueString(info.getValue());
			break;
		case HinemosPropertyTypeConstant.TYPE_NUMERIC:
			ret.setValueType(HinemosPropertyTypeConstant.TYPE_NUMERIC);
			ret.setValueNumeric(str2Long(info.getValue()));
			break;
		case HinemosPropertyTypeConstant.TYPE_TRUTH:
			ret.setValueType(HinemosPropertyTypeConstant.TYPE_TRUTH);
			ret.setValueBoolean(str2Bool(info.getValue()));
			break;
		default:
			throw new Exception(info.getKey() + " has undefined value type.");
		}
		return ret;
	}
}
