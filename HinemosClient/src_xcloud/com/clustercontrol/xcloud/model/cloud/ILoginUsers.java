/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import com.clustercontrol.xcloud.model.base.CollectionObserver;
import com.clustercontrol.xcloud.model.base.IElement;
import com.clustercontrol.xcloud.model.base.PropertyId;


public interface ILoginUsers extends IElement {
	public interface p {
		static final PropertyId<CollectionObserver<ILoginUser>> loginUsers = new PropertyId<CollectionObserver<ILoginUser>>("loginUsers"){};
	}
	
	ILoginUser[] getLoginUsers();
	ILoginUser getLoginUser(String loginUserId);
	
	void update();
	
	ICloudScope getCloudScope();
}
