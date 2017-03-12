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
