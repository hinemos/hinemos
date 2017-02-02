/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
