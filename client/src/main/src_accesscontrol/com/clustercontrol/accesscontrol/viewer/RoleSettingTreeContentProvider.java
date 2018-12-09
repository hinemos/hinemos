/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.viewer;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.clustercontrol.ws.access.RoleInfo;
import com.clustercontrol.ws.access.UserInfo;
import com.clustercontrol.ws.accesscontrol.RoleTreeItem;

/**
 * ロールツリー用コンポジットのツリービューア用のITreeContentProviderクラスです。
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class RoleSettingTreeContentProvider implements ITreeContentProvider {
	private static Log m_log = LogFactory.getLog( RoleSettingTreeContentProvider.class );

	/**
	 * ロールツリーアイテムの要素(子のジョブツリーアイテム)を返します。
	 * 
	 * @param inputElement ジョブツリーアイテム
	 * @return ロールツリーアイテムの配列
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
	 * ロールツリーアイテムの子のロールツリーアイテムを返します。
	 * 
	 * @param parentElement ロールツリーアイテム
	 * @return ロールツリーアイテムの配列
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		List<RoleTreeItem> ret = ((RoleTreeItem) parentElement).getChildren();
		Collections.sort(ret, new Comparator<RoleTreeItem>(){
			@Override
			public int compare(RoleTreeItem o1, RoleTreeItem o2) {
				if (o1.getData() instanceof RoleInfo && o2.getData() instanceof RoleInfo) {
					RoleInfo r1 = (RoleInfo) o1.getData();
					RoleInfo r2 = (RoleInfo) o2.getData();
					String s1 = r1.getRoleType();
					String s2 = r2.getRoleType();
					m_log.trace("s1=" + s1 + ", s2=" + s2);
					if (s1 == null || s2 == null) {
						// マネージャの比較
						return r1.getRoleName().compareTo(r2.getRoleName());
					} else if (s1.equals(s2)) {
						return r1.getRoleId().compareTo(r2.getRoleId());
					} else {
						return r1.getRoleType().compareTo(r2.getRoleType()) * -1;
					}
				} else if (o1.getData() instanceof UserInfo && o2.getData() instanceof UserInfo) {
					UserInfo u1 = (UserInfo) o1.getData();
					UserInfo u2 = (UserInfo) o2.getData();
					m_log.trace("u1=" + u1 + ", u2=" + u2);
					return u1.getUserId().compareTo(u2.getUserId());
				}
				m_log.warn("unknown error");
				return -1; // ここには到達しないはず
			}
		});
		return ret.toArray();
	}

	/**
	 * ロールツリーアイテムの親ロールツリーアイテムを返します。
	 * 
	 * @param element 親ロールツリーアイテム
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		return ((RoleTreeItem) element).getParent();
	}

	/**
	 * ロールツリーアイテムが子のロールツリーアイテムを持っているかを返します。
	 * 
	 * @param element ロールツリーアイテム
	 * @return true：子がある、false：子がない
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		return ((RoleTreeItem) element).getChildren().size() > 0;
	}

}
