/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.msgfilter.extensions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.clustercontrol.ClusterControlPlugin;

public class RestConnectMsgFilterExtension {
	public static final String pointId = "restConnectMsgFilter";

	public static final String elementName = "restConnectMsgFilter";

	public static final String RestConnectClassAttributeName = "restConnectMsgFilter";

	private static final Log logger = LogFactory.getLog(RestConnectMsgFilterExtension.class);

	private IRestConnectMsgFilter restConnect = null;

	private static final RestConnectMsgFilterExtension instance = new RestConnectMsgFilterExtension();

	public static RestConnectMsgFilterExtension getInstance(){
		return instance;
	}
	
	private RestConnectMsgFilterExtension() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();

		// 拡張ポイントを取得
		IExtensionPoint point = registry
				.getExtensionPoint(ClusterControlPlugin.getDefault().getBundle().getSymbolicName() + "." + pointId);
		// RCPの場合、RCP/plugin.xmlに拡張ポイントの定義がされていないためpointはnullとなる。
		// RAPの場合、RAP/plugin.xmlに拡張ポイントの定義がされているためpointはnullとならない。
		// MsgFilterClientOptionが無い場合はpoint.getExtensions()が空になる。
		if(point == null){
			return;
		}
		for (IExtension ex : point.getExtensions()) {
			for (IConfigurationElement element : ex.getConfigurationElements()) {
				// 要素名が該当するpluginInfoのIdだった場合、ExtensionTypeの情報を取得
				if (element.getName().equals(elementName)) {
					try {
						if (element.getAttribute(RestConnectClassAttributeName) != null) {
							restConnect = (IRestConnectMsgFilter) element
									.createExecutableExtension(RestConnectClassAttributeName);
						}
					} catch (Exception e) {
						logger.warn(e.getMessage(), e);
					}
				}
			}
		}
	}

	
	/**
	 * インターフェースを介してMsgFilterClientOptionから実装クラスを取得します。
	 * RCPではMsgFilterClientOptionが未対応のためnullが返ります。
	 * 
	 * @see IRestConnectMsgFilter
	 * @see com.clustercontrol.msgfilter.plugin#RestConnectMgsFilterImpl
	 * @return 
	 */
	public IRestConnectMsgFilter getRestConnectMsgFilter() {
		if (restConnect == null) {
			return null;
		}
		return restConnect;
	}
}
