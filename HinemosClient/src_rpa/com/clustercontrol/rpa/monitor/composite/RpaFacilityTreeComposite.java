/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.monitor.composite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.rpa.util.RpaConstants;
import com.clustercontrol.util.FacilityTreeCache;
import com.clustercontrol.util.FacilityTreeItemUtil;
import com.clustercontrol.util.HinemosMessage;

/**
 * RPA管理ツールを表すスコープを表示するコンポジット
 * RPA管理ツールサービス監視向け
 */
public class RpaFacilityTreeComposite extends FacilityTreeComposite {
	// ログ
	private static Log m_log = LogFactory.getLog( RpaFacilityTreeComposite.class );


	public RpaFacilityTreeComposite(Composite parent, int style, String managerName, String ownerRoleId) {
		super(parent, style, managerName, ownerRoleId, true, false, false);
	}
	
	/**
	 * ビューの表示内容を更新します。
	 * @see FacilityTreeComposite#update
	 */
	@Override
	public void update() {
		// 外部契機でファシリティツリーが更新された場合に、自分の画面もリフレッシュ
		if (this.ownerRoleId != null) {
			try {
				m_log.debug("getFacilityTree " + managerName);
				treeItem = addEmptyParent(RepositoryRestClientWrapper.getWrapper(managerName).getFacilityTree(this.ownerRoleId));
			} catch (Exception e) {
				m_log.warn("getTreeItem(), " + e.getMessage(), e);
				return;
			}
		} else {
			treeItem = FacilityTreeCache.getTreeItem(managerName);
		}

		Date cacheDate = null;
		if (managerName != null) {
			cacheDate = FacilityTreeCache.getCacheDate(managerName);
		}
		if (cacheDate != null && cacheDate.equals(this.cacheDate)) {
			return;
		}
		this.cacheDate = cacheDate;

		if( null == treeItem ){
			m_log.trace("treeItem is null. Skip.");
		}else {
			FacilityTreeItemResponse scope = (treeItem.getChildren()).get(0);
			scope.getData().setFacilityName(HinemosMessage.replace(scope.getData().getFacilityName()));

			// RPAスコープ以外のスコープを消す。
			FacilityTreeItemUtil.keepChild(scope, RpaConstants.RPA);
			// 管理ツール無しのノード向けの組み込みスコープを消す。
			FacilityTreeItemUtil.removeChild(scope.getChildren().get(0), RpaConstants.RPA_NO_MGR_UIPATH);
			FacilityTreeItemUtil.removeChild(scope.getChildren().get(0), RpaConstants.RPA_NO_MGR_WINACTOR);
			
			// ノードを消す。
			FacilityTreeItemUtil.removeNode(scope);

			// SWTアクセスを許可するスレッドからの操作用
			checkAsyncExec(new Runnable(){
				@Override
				public void run() {
					m_log.trace("FacilityTreeComposite.checkAsyncExec() do runnnable");

					Control control = treeViewer.getControl();
					if (control == null || control.isDisposed()) {
						m_log.info("treeViewer is disposed. ");
						return;
					}
					
					FacilityTreeItemResponse oldTreeItem = (FacilityTreeItemResponse)treeViewer.getInput();
					m_log.debug("run() oldTreeItem=" + oldTreeItem);
					if( null != oldTreeItem ){
						if (!oldTreeItem.equals(treeItem)) {
							ArrayList<String> expandIdList = new ArrayList<String>();
							for (Object item : treeViewer.getExpandedElements()) {
								expandIdList.add(((FacilityTreeItemResponse)item).getData().getFacilityId());
							}
							m_log.debug("expandIdList.size=" + expandIdList.size());
							treeViewer.setInput(treeItem);
							treeViewer.refresh();
							expand(treeItem, expandIdList);
						}
					}else{
						treeViewer.setInput(treeItem);
						List<FacilityTreeItemResponse> selectItem = treeItem.getChildren();
						treeViewer.setSelection(new StructuredSelection(selectItem.get(0)), true);
						//スコープのレベルまで展開
						treeViewer.expandToLevel(3);
					}
				}

				private void expand(FacilityTreeItemResponse item, List<String> expandIdList) {
					if (expandIdList.contains(item.getData().getFacilityId())) {
						treeViewer.expandToLevel(item, 1);
					}
					for (FacilityTreeItemResponse child : item.getChildren()) {
						expand(child, expandIdList);
					}
				}
			});
		}
	}

}
