/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.repository.util;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.repository.bean.NodeFilterConstant;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;

/**
 * ノードフィルタ用プロパティを作成するクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class NodeFilterProperty {

	private static Log m_log = LogFactory.getLog(NodeFilterProperty.class);

	/** ----- 初期値キャッシュ ----- */
	private static Map<Locale, Property> cachedInitProperty = null;

	static {
		cachedInitProperty = new ConcurrentHashMap<Locale, Property>();
	}

	/**
	 * ノードフィルタ用プロパティを返します。
	 *
	 * @param locale
	 * @return ノードフィルタ用プロパティ
	 */
	public static Property getProperty() {
		return getProperty( Locale.getDefault() );
	}

	/**
	 * ノードフィルタ用プロパティを返します。
	 *
	 * @param locale
	 * @return ノードフィルタ用プロパティ
	 */
	public static Property getProperty(Locale locale) {

		// 初期値のキャッシュが存在すれば、それを返す
		if (cachedInitProperty.containsKey(locale)) {
			m_log.debug("using a initial property of node's filter to cache. (locale = " + locale + ")");
			return PropertyUtil.copy(cachedInitProperty.get(locale));
		}

		//マネージャ
		Property manager =
				new Property(NodeFilterConstant.MANAGER, Messages.getString("facility.manager", locale), PropertyDefineConstant.EDITOR_SELECT);
		//ファシリティID
		Property facilityId =
				new Property(NodeFilterConstant.FACILITY_ID, Messages.getString("facility.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//ファシリティ名
		Property facilityName =
				new Property(NodeFilterConstant.FACILITY_NAME, Messages.getString("facility.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//説明
		Property description =
				new Property(NodeFilterConstant.DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//IPアドレス v4
		Property ipAddressV4 =
				new Property(NodeFilterConstant.IP_ADDRESS_V4, Messages.getString("ip.address.v4", locale), PropertyDefineConstant.EDITOR_IPV4);
		//IPアドレス v6
		Property ipAddressV6 =
				new Property(NodeFilterConstant.IP_ADDRESS_V6, Messages.getString("ip.address.v6", locale), PropertyDefineConstant.EDITOR_IPV6);
		//OS名
		Property osName =
				new Property(NodeFilterConstant.OS_NAME, Messages.getString("os.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//OSリリース
		Property osRelease =
				new Property(NodeFilterConstant.OS_RELEASE, Messages.getString("os.release", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//管理者
		Property administrator =
				new Property(NodeFilterConstant.ADMINISTRATOR, Messages.getString("administrator", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//連絡先
		Property contact =
				new Property(NodeFilterConstant.CONTACT, Messages.getString("contact", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);

		//ネットワーク
		Property network =
				new Property(NodeFilterConstant.NETWORK, Messages.getString("network", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//OS
		Property os =
				new Property(NodeFilterConstant.OS, Messages.getString("os", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//保守
		Property maintenance =
				new Property(NodeFilterConstant.MAINTENANCE, Messages.getString("maintenance", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);

		Object[] obj = EndpointManager.getActiveManagerSet().toArray();
		Object[] val = new Object[obj.length + 1];
		val[0] = "";
		for(int i = 0; i<obj.length; i++) {
			val[i + 1] = obj[i];
		}

		Object[][] managerValues = {val, val};
		manager.setSelectValues(managerValues);
		manager.setValue("");

		//値を初期化
		facilityId.setValue("");
		facilityName.setValue("");
		description.setValue("");
		ipAddressV4.setValue("");
		ipAddressV6.setValue("");
		osName.setValue("");
		osRelease.setValue("");
		administrator.setValue("");
		contact.setValue("");

		os.setValue("");
		network.setValue("");
		maintenance.setValue("");

		//変更の可/不可を設定
		manager.setModify(PropertyDefineConstant.MODIFY_OK);
		facilityId.setModify(PropertyDefineConstant.MODIFY_OK);
		facilityName.setModify(PropertyDefineConstant.MODIFY_OK);
		description.setModify(PropertyDefineConstant.MODIFY_OK);
		ipAddressV4.setModify(PropertyDefineConstant.MODIFY_OK);
		ipAddressV6.setModify(PropertyDefineConstant.MODIFY_OK);
		osName.setModify(PropertyDefineConstant.MODIFY_OK);
		osRelease.setModify(PropertyDefineConstant.MODIFY_OK);
		administrator.setModify(PropertyDefineConstant.MODIFY_OK);
		contact.setModify(PropertyDefineConstant.MODIFY_OK);

		os.setModify(PropertyDefineConstant.MODIFY_NG);
		network.setModify(PropertyDefineConstant.MODIFY_NG);
		maintenance.setModify(PropertyDefineConstant.MODIFY_NG);

		Property property = new Property(null, null, "");

		// 初期表示ツリーを構成。
		property.removeChildren();
		property.addChildren(manager);
		property.addChildren(facilityId);
		property.addChildren(facilityName);
		property.addChildren(description);
		property.addChildren(network);
		property.addChildren(os);
		property.addChildren(maintenance);

		// ネットワークツリー
		network.removeChildren();
		network.addChildren(ipAddressV4);
		network.addChildren(ipAddressV6);

		// OSツリー
		os.removeChildren();
		os.addChildren(osName);
		os.addChildren(osRelease);

		// 保守ツリー
		maintenance.removeChildren();
		maintenance.addChildren(administrator);
		maintenance.addChildren(contact);

		// 初期値をキャッシュに登録（次回から高速化に初期値を取得）
		m_log.info("adding a initial property of user's filter to cache. (locale = " + locale + ")");
		cachedInitProperty.put(locale, PropertyUtil.copy(property));

		return property;
	}
}
