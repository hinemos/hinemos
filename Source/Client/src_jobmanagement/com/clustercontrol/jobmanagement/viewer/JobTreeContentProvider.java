/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.viewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;

/**
 * ジョブツリー用コンポジットのツリービューア用のITreeContentProviderクラスです。
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class JobTreeContentProvider implements ITreeContentProvider {

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
		List<JobTreeItemWrapper> ret = null;
		if(parentElement instanceof ArrayList<?>) {
			ArrayList<?> list = (ArrayList<?>)parentElement;
			ret = ((JobTreeItemWrapper)list.get(0)).getChildren();
		} else if (parentElement instanceof JobTreeItemWrapper){
			ret = ((JobTreeItemWrapper)parentElement).getChildren();
		} else {
			throw new InternalError("ret is null.");
		}
		Collections.sort(ret, new Comparator<JobTreeItemWrapper>(){
			@Override
			public int compare(JobTreeItemWrapper o1, JobTreeItemWrapper o2) {
				String s1 = o1.getData().getId();
				String s2 = o2.getData().getId();
				return s1.compareTo(s2);
			}
		});
		return ret.toArray();
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
		return ((JobTreeItemWrapper) element).getParent();
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
		return ((JobTreeItemWrapper) element).getChildren().size() > 0;
	}

}
