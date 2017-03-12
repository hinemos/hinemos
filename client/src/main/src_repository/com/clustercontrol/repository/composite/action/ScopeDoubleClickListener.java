/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.repository.composite.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.repository.action.GetScopeListTableDefine;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.composite.ScopeListComposite;
import com.clustercontrol.repository.dialog.ScopeCreateDialog;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.repository.view.ScopeListView;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * リポジトリ[スコープ]ビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 *
 * @version 4.1.0
 */
public class ScopeDoubleClickListener implements IDoubleClickListener {
	/** リポジトリ[スコープ]ビュー用のコンポジット */
	private ScopeListComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite リポジトリ[スコープ]ビュー用のコンポジット
	 */
	public ScopeDoubleClickListener(ScopeListComposite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * リポジトリ[スコープ]ビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>選択変更イベントから選択行を取得し、選択行からファシリティIDを取得します。</li>
	 * <li>リポジトリ[スコープ]ビュー用のコンポジットからファシリティツリーアイテムを取得します。</li>
	 * <li>取得したファシリティツリーアイテムから、ファシリティIDが一致するファシリティツリーアイテムを取得します。</li>
	 * <li>リポジトリ[スコープ]ビュー用のコンポジットに、ファシリティIDが一致するファシリティツリーアイテムを設定します。</li>
	 * <li>ファシリティIDからスコープ情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see com.clustercontrol.repository.dialog.ScopeCreateDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		FacilityTreeItem selectFacilityTreeItem = null;
		ArrayList<?> item = null;

		String facilityId = null;

		if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
			//選択アイテムを取得
			item = (ArrayList<?>) ((StructuredSelection) event.getSelection()).getFirstElement();
		}

		//リポジトリ[スコープ]ビューのインスタンスを取得
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IViewPart viewPart = page.findView(ScopeListView.ID);

		if(viewPart != null) {
			if(item != null){
				facilityId = (String) item.get(GetScopeListTableDefine.FACILITY_ID);

				if (m_composite.getFacilityTreeItem() != null) {
					List<FacilityTreeItem> items = m_composite.getFacilityTreeItem().getChildren();

					for(int i = 0; i < items.size(); i++){
						if(facilityId.equals(items.get(i).getData().getFacilityId())){
							selectFacilityTreeItem = items.get(i);
							break;
						}
					}
				}
			}

			if (selectFacilityTreeItem != null) {
				//選択ツリーアイテムを設定
				m_composite.setSelectFacilityTreeItem(selectFacilityTreeItem);

				// スコープダイアログのみ表示する
				if(facilityId != null
						&& !selectFacilityTreeItem.getData().isBuiltInFlg()
						&& selectFacilityTreeItem.getData().getFacilityType() == FacilityConstant.TYPE_SCOPE){
					FacilityTreeItem manager = ScopePropertyUtil.getManager(selectFacilityTreeItem);
					String managerName = manager.getData().getFacilityId();

					// ダイアログを生成
					ScopeCreateDialog dialog = new ScopeCreateDialog(m_composite.getShell(), managerName, facilityId, true);

					// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
					if (dialog.open() == IDialogConstants.OK_ID) {
						ClientSession.doCheck();
						m_composite.update(selectFacilityTreeItem);

					}
				}
			}
		}
	}

}
