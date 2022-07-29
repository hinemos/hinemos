/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.viewer;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

public class RoleSettingTreeViewer extends TreeViewer{

	public RoleSettingTreeViewer(Composite parent) {
		super(parent);
	}

	public RoleSettingTreeViewer(Tree parent) {
		super(parent);
	}
}
