/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.viewer;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.clustercontrol.repository.util.FacilityTreeItemResponse;

/**
 * スコープツリー用のコンテンツプロバイダクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class FacilityTreeContentProvider implements ITreeContentProvider {

	// ----- instance フィールド ----- //

	// ----- コンストラクタ ----- //

	// ----- instance メソッド ----- //

	@Override
	public Object getParent(Object element) {
		return ((FacilityTreeItemResponse) element).getParent();
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return ((FacilityTreeItemResponse) parentElement).getChildren().toArray();
	}

	@Override
	public boolean hasChildren(Object element) {
		return ((FacilityTreeItemResponse) element).getChildren().size() > 0;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public void dispose() {
	}
}
