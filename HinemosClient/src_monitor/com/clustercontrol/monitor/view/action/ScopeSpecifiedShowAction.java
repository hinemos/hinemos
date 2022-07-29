/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.view.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.FacilityInfoResponse;

import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.monitor.action.GetScopeListTableDefine;
import com.clustercontrol.monitor.view.ScopeView;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;

/**
 * 監視[スコープ]ビューでダブルクリック時にスコープ階層ペインを更新するクライアント側アクションクラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class ScopeSpecifiedShowAction implements IDoubleClickListener {

	/**
	 * 監視[スコープ]ビューの選択されたアイテムのファシリティIDを取得し、
	 * スコープ階層ペインの対応するファシリティIDのアイテムを選択状態にします。
	 * <p>
	 * <ol>
	 * <li>監視[スコープ]ビューで選択されているアイテムより、ファシリティIDを取得します。</li>
	 * <li>取得したファシリティIDのスコープツリーアイテムを選択状態にします。 </li>
	 * </ol>
	 *
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {

		// ダブルクリックしたスコープのファシリティIDを取得する
		ArrayList<?> list = (ArrayList<?>) ((StructuredSelection)event.getSelection()).getFirstElement();
		String facilityId = (String)list.get(GetScopeListTableDefine.FACILITY_ID);

		if(facilityId != null && !"".equals(facilityId)){
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

			ScopeView scopeView = (ScopeView) page.findView(ScopeView.ID);

			if (scopeView != null) {

				List<FacilityTreeItemResponse> children = null;

				// 現在選択されているツリーアイテムを取得
				FacilityTreeComposite facilityTree = scopeView.getScopeTreeComposite();
				FacilityTreeItemResponse selectItem = facilityTree.getSelectItem();

				// ツリーアイテムが選択されていない場合
				if(selectItem == null){

					// 最上位のツリーアイテムを取得
					List<FacilityTreeItemResponse> root = ((FacilityTreeItemResponse)facilityTree.getTreeViewer().getInput()).getChildren();
					if(root != null && root.size()>0){
						children = root.get(0).getChildren();
					}
				}
				// ツリーアイテムが選択されている場合
				else{

					// ダブルクリックしたスコープが、現在選択されているツリーアイテムではない場合、
					// 選択されているツリーアイテムの直下のツリーアイテムを取得
					String selectFacilityId = selectItem.getData().getFacilityId();
					if(!facilityId.equals(selectFacilityId)){
						children = selectItem.getChildren();
					}
				}

				if(children != null){

					FacilityInfoResponse tmpInfo = null;
					List<FacilityTreeItemResponse> grandchild = null;
					String tmpFacilityId = null;

					// ダブルクリックしたスコープのファシリティIDと一致するツリーアイテムを取得
					for(int index=0; index<children.size(); index++){

						tmpInfo = children.get(index).getData();
						if(tmpInfo != null){
							tmpFacilityId = tmpInfo.getFacilityId();
							if(facilityId.equals(tmpFacilityId)){

								// ダブルクリックしたスコープがノードではない場合、
								// FacilityIDに対応するツリーアイテムを選択状態にする
								grandchild = children.get(index).getChildren();
								if(grandchild != null && grandchild.size()>0){

									facilityTree.getTreeViewer().setSelection(
											new StructuredSelection(children.get(index)), true);
								}
								break;
							}
						}
					}
				}
			}
		}
	}
}
