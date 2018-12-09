/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;

public class NotifyCallback implements JpaTransactionCallback {
	
	public final List<OutputBasicInfo> notifyInfoList;

	public boolean isCommit = false;

	public NotifyCallback(List<OutputBasicInfo> notifyInfoList) {
		this.notifyInfoList = notifyInfoList;
	}
	
	public NotifyCallback(OutputBasicInfo notifyInfo) {
		this.notifyInfoList = new ArrayList<>();
		this.notifyInfoList.add(notifyInfo);
	}

	@Override
	public boolean isTransaction() {
		return false;
	}

	@Override
	public void preFlush() { }

	@Override
	public void postFlush() { }

	@Override
	public void preCommit() { }

	@Override
	public void postCommit() {
		isCommit = true;
	}

	@Override
	public void preRollback() { }

	@Override
	public void postRollback() { }

	@Override
	public void preClose() { }

	@Override
	public void postClose() {
		if (isCommit) {
			NotifyControllerBean.notify(notifyInfoList);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((notifyInfoList == null) ? 0 : notifyInfoList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NotifyCallback other = (NotifyCallback) obj;
		if (notifyInfoList == null) {
			if (other.notifyInfoList != null)
				return false;
		} else if (!notifyInfoList.equals(other.notifyInfoList))
			return false;
		return true;
	}

	
}
