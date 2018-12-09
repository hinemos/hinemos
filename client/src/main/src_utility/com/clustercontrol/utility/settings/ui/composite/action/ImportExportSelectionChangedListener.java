/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.composite.action;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.utility.settings.ui.views.ImportExportExecView;

/**
 * インポートエクスポートビューのテーブルビューア用のSelectionChangedListenerクラス<BR>
 * 
 * @version 6.1.0
 * @since 1.2.0
 */
public class ImportExportSelectionChangedListener implements ISelectionChangedListener {
	
	/**
	* コンストラクタ
	*/
	public ImportExportSelectionChangedListener() {

	}
	
	/**
	* 選択変更時に呼び出されます。<BR>
	* インポートエクスポートビューのツリービューアを選択した際に、<BR>
	* 選択した行の内容でビューのアクションの有効・無効を設定します。
	* <P>
	* <ol>
	 * <li>選択変更イベントから選択行を取得し、選択行からイベントの表示内容を取得します。</li>
	 * <li>取得したイベントからインポートエクスポートビューのアクションの有効・無効を設定します。</li>
	 * </ol>
	 * 
	* @param event 選択変更イベント
	* 
	* @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	*/
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		//インポートエクスポートビューのインスタンスを取得
		IWorkbenchPage page = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow().getActivePage();
		IViewPart viewPart = page.findView(ImportExportExecView.ID);
	   
		//選択アイテムを取得
		StructuredSelection selection = (StructuredSelection) event.getSelection();
		
		if ( viewPart != null && selection != null) {
			ImportExportExecView view = viewPart.getAdapter(ImportExportExecView.class);
			
			//ビューのボタン（アクション）の使用可/不可を設定する
			view.setEnabledAction();
		}
	}
}