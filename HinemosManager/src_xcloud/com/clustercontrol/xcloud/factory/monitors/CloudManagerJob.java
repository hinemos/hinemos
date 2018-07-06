/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory.monitors;

import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.HinemosCredential;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.Session.SessionScope;

public abstract class CloudManagerJob {

	public void execute() throws CloudManagerException {
		try (SessionScope sessionScope = SessionScope.open()) {
			String userId = HinemosPropertyCommon.xcloud_internal_thread_admin_user.getStringValue();
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, userId);
			Session.current().setHinemosCredential(new HinemosCredential(userId));
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, new AccessControllerBean().isAdministrator());

			internalExecute();
		} catch (Exception e) {
			throw new CloudManagerException(e);
		} finally {
		}
	}

	protected abstract void internalExecute() throws Exception;
}
