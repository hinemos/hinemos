/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.composite;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.clustercontrol.utility.settings.ui.bean.FuncTreeItem;

/**
 * ジョブツリー用コンポジットのツリービューア用のITreeContentProviderクラスです。
 * 
 * @version 6.1.0
 * @since 1.2.0
 */
public class HinemosFuncTreeContentProvider implements ITreeContentProvider {

	/**
 	* ジョブツリーアイテムの要素(子のジョブツリーアイテム)を返します。
 	* 
 	* @param inputElement ジョブツリーアイテム
 	* @return ジョブツリーアイテムの配列
 	* 
 	* @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
 	*/
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/* (non-Javadoc)
 	* @see org.eclipse.jface.viewers.IContentProvider#dispose()
 	*/
	@Override
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/**
	 * ジョブツリーアイテムの子のジョブツリーアイテムを返します。
	 * 
	 * @param parentElement ジョブツリーアイテム
	 * @return ジョブツリーアイテムの配列
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		return ((FuncTreeItem) parentElement).getChildren();
	}

	/**
	 * ジョブツリーアイテムの親ジョブツリーアイテムを返します。
	 * 
	 * @param element 親ジョブツリーアイテム
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		return ((FuncTreeItem) element).getParent();
	}

	/**
	 * ジョブツリーアイテムが子のジョブツリーアイテムを持っているかを返します。
	 * 
	 * @param element ジョブツリーアイテム
	 * @return true：子がある、false：子がない
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		return ((FuncTreeItem) element).size() > 0;
	}

}