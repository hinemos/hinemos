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
import org.openapitools.client.model.RoleInfoResponse;
import org.openapitools.client.model.UserInfoResponse;

import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.accesscontrol.bean.RoleTreeItemWrapper;

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
		List<RoleTreeItemWrapper> ret = ((RoleTreeItemWrapper) parentElement).getChildren();
		Collections.sort(ret, new Comparator<RoleTreeItemWrapper>(){
			@Override
			public int compare(RoleTreeItemWrapper o1, RoleTreeItemWrapper o2) {
				if (o1.getData() instanceof RoleInfoResponse && o2.getData() instanceof RoleInfoResponse) {
					RoleInfoResponse r1 = (RoleInfoResponse) o1.getData();
					RoleInfoResponse r2 = (RoleInfoResponse) o2.getData();
					String s1 = null;
					if(!RoleSettingTreeConstant.MANAGER.equals(r1.getRoleId())) {
						s1 = r1.getRoleType().getValue();
					}
					String s2 = null;
					if(!RoleSettingTreeConstant.MANAGER.equals(r1.getRoleId())) {
						s2 = r2.getRoleType().getValue();
					}
					m_log.trace("s1=" + s1 + ", s2=" + s2);
					if (s1 == null || s2 == null) {
						// マネージャの比較
						return r1.getRoleName().compareTo(r2.getRoleName());
					} else if (s1.equals(s2)) {
						return r1.getRoleId().compareTo(r2.getRoleId());
					} else {
						return r1.getRoleType().compareTo(r2.getRoleType()) * -1;
					}
				} else if (o1.getData() instanceof UserInfoResponse && o2.getData() instanceof UserInfoResponse) {
					UserInfoResponse u1 = (UserInfoResponse) o1.getData();
					UserInfoResponse u2 = (UserInfoResponse) o2.getData();
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
		return ((RoleTreeItemWrapper) element).getParent();
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
		return ((RoleTreeItemWrapper) element).getChildren().size() > 0;
	}

}
