/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.composite.action;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;

import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.view.ScopeListView;

/**
 * リポジトリ[スコープ]ビューのツリービューア用のSelectionChangedListenerクラス<BR>
 *
 * @version 2.2.0
 * @since 2.2.0
 */
public class FacilityTreeSelectionChangedListener implements ISelectionChangedListener {

	// ログ
	private static Log m_log = LogFactory.getLog( FacilityTreeSelectionChangedListener.class );

	/**
	 * コンストラクタ
	 */
	public FacilityTreeSelectionChangedListener() {

	}

	/**
	 * 選択変更時に呼び出されます。<BR>
	 * リポジトリ[スコープ]ビューのツリービューアを選択した際に、<BR>
	 * 選択したアイテムの内容でリポジトリ[スコープ]ビューのアクションの有効・無効を設定します。
	 * <P>
	 * <ol>
	 * <li>選択変更イベントからファシリティツリーアイテムを取得します。</li>
	 * <li>リポジトリ[スコープ]ビューのアクションの有効・無効を設定します。</li>
	 * </ol>
	 *
	 * @param event 選択変更イベント
	 *
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		FacilityTreeItemResponse selectItem = null;

		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			//選択アイテムを取得
			selectItem = (FacilityTreeItemResponse) ((StructuredSelection) event.getSelection()).getFirstElement();
		}

		// リポジトリ[登録]ビューのインスタンスを取得
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IViewPart viewPart = page.findView(ScopeListView.ID);

		if (viewPart != null && selectItem != null) {
			ScopeListView view = (ScopeListView) viewPart.getAdapter(ScopeListView.class);

			if (view == null) {
				m_log.info("selection changed: view is null");
				return;
			}

			// Set last focus
			FacilityTreeComposite composite = view.getScopeTreeComposite();
			if( composite != null && composite.getTree().isFocusControl() ){
				view.setLastFocusComposite( composite );
			}

			TreeSelection selection = (TreeSelection)event.getSelection();
			boolean builtin = false;
			if (selectItem.getData().getFacilityType() != FacilityTypeEnum.MANAGER) {
				builtin = isBuiltin((List<?>)selection.toList());
			}

			// ビューのアクションの有効/無効を設定
			view.setEnabledAction(builtin, selectItem.getData().getFacilityType(), event.getSelection(), selectItem.getData().getNotReferFlg());
		}

	}

	private boolean isBuiltin(List<?> treeList) {
		boolean ret = false;
			for(Object obj : treeList) {
				if (obj instanceof FacilityTreeItemResponse == false) {
					continue;
				}
				FacilityTreeItemResponse tree = (FacilityTreeItemResponse)obj;
				m_log.debug("facilityId:" + tree.getData().getFacilityId());
				ret = ret || tree.getData().getBuiltInFlg();
				if (ret == false && tree.getChildren().isEmpty() == false) {
					ret = isBuiltin(tree.getChildren());
				}
				if (ret) {
					return ret;
				}
			}
		return ret;
	}
}
