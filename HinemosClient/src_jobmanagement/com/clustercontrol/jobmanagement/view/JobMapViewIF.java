/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view;

import org.eclipse.ui.IViewPart;

import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブマップ用のinterfaceです。
 * 
 */
public interface JobMapViewIF extends IViewPart {
	public void update(String managerName, String sessionId, JobTreeItem jobTreeItem);
}
