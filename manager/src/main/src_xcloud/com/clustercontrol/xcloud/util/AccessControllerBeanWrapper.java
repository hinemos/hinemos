/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import com.clustercontrol.accesscontrol.session.AccessControllerBean;

public class AccessControllerBeanWrapper extends AccessControllerBean {
	private static ThreadLocal<AccessControllerBeanWrapper> instance  = new ThreadLocal<AccessControllerBeanWrapper>() {
		protected AccessControllerBeanWrapper initialValue()
		{
			return null;
		}
	};

	public static AccessControllerBeanWrapper bean() {
		AccessControllerBeanWrapper bean = instance.get();
		if (bean == null) {
			bean = new AccessControllerBeanWrapper();
			instance.set(bean);
		}
		return bean;
	}
}
