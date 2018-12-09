/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.viewer;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * CommonTableTreeViewerクラス用のContentProviderクラス
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class JobTableTreeContentProvider implements ITreeContentProvider {

	/**
	 * @param element
	 * @return
	 */
	@Override
	public Object getParent(Object element) {
		return ((JobTreeItem) element).getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {

		return getChildren(inputElement);
	}

	/**
	 * @param parentElement
	 * @return
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		List<JobTreeItem> children = ((JobTreeItem) parentElement).getChildren();
		return children.toArray(new Object[0]);
	}

	/**
	 * @param element
	 * @return
	 */
	@Override
	public boolean hasChildren(Object element) {
		return ((JobTreeItem) element).getChildren().size() > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
	}
}
