/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.view;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.rpa.composite.RpaScenarioOperationResultSearchComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.view.CommonViewPart;

/**
 * シナリオ実績検索[マネージャ名＜スコープ名＞]ビュークラス<BR>
 */
public class RpaScenarioOperationResultSearchView extends CommonViewPart {
	
	public static final String ID = RpaScenarioOperationResultSearchView.class.getName();
	
	public static final String SECONDARYID_SEPARATOR = ",node=";
	
	private RpaScenarioOperationResultSearchComposite rpaScenarioOperationResultSearchComposite = null;

	/**
	 * Number of selected items
	 */
	private int selectedNum;

	/**
	 * <BR><B>使用しないこと。</B></BR>
	 * シナリオ実績検索[マネージャ名＜スコープ名＞]ビューは、
	 * RpaScenarioOperationResultSearchView.createSearchViewを利用し作成すること。
	 */
	@Deprecated
	public RpaScenarioOperationResultSearchView() {
		super();
	}

	/**
	 * 検索の際のビュー作成を行う。
	 * <BR><B>シナリオ実績検索ビューを作成する際は、ここを必ず使用すること。</B></BR>
	 * secondaryIdを、マネージャ名@ファシリティID で作成する
	 * @param page
	 * @param manager
	 * @param facilityid
	 * @return
	 * @throws PartInitException
	 * 
	 * XXX
	 * マネージャ名は、次の文字列の入力を受け付けてしまう。
	 * 半角文字列の -^\@[;:],./\=~|`{+*}<>?_!"#$%&'()
	 * 全角文字列の ー＾￥＠「；：」、。・￥＝～｜‘｛＋＊｝＜＞？＿！”＃＄％＆’（）
	 * そのため、上記の文字をセパレータとして使用することできない。
	 * 暫定対処で、",node="をセパレータとする。
	 */
	public static RpaScenarioOperationResultSearchView createSearchView(
			IWorkbenchPage page, String manager, String facilityid) throws PartInitException{
		
		//セカンダリーIDにコロンがあるとエラーになるので置き換え
		String managerRep = manager.replaceAll(":", "@");
		//マネージャ名とファシリティIDで一意なセカンダリIDを作成する
		String secondaryId = 
				managerRep + SECONDARYID_SEPARATOR + facilityid;
		return (RpaScenarioOperationResultSearchView) page.showView(RpaScenarioOperationResultSearchView.ID, secondaryId,IWorkbenchPage.VIEW_ACTIVATE);
	}
	
	/**
	 * ViewPartへのコントロール作成処理<BR>
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {

		rpaScenarioOperationResultSearchComposite = new RpaScenarioOperationResultSearchComposite(parent, SWT.NONE);

		/*
		 * シナリオ実績[検索]ビューのタイトルにFacilityIDを含める
		 */
		if (this.getViewSite().getSecondaryId() != null) {
			
			String title = this.getViewSite().getSecondaryId();
			//getSecondaryIdに、マネージャ名とファシリティIDが含まれているので、そこから取得する
			String[] spltStr = title.split(SECONDARYID_SEPARATOR);
			String manager = spltStr[0];
			String facilityId = spltStr[1];
			//ビュータイトルを設定
			this.setPartName(Messages.getString("view.rpa.scenario.operation.result.search", new Object[]{manager,facilityId}));

			rpaScenarioOperationResultSearchComposite.setManager(manager);
			rpaScenarioOperationResultSearchComposite.setFacilityId(facilityId);
		}
		
		// ポップアップメニュー作成
		 createContextMenu();

		//ビューを更新
		this.update();
	}
	
	/**
	 * ポップアップメニュー作成
	 */
	protected void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		Menu menu = menuManager.createContextMenu(rpaScenarioOperationResultSearchComposite.getTable());
		rpaScenarioOperationResultSearchComposite.getTable().setMenu(menu);
		getSite().registerContextMenu( menuManager, this.rpaScenarioOperationResultSearchComposite.getTableViewer() );
	}

	/**
	 * シナリオ実績[検索]ビュー更新<BR>
	 */
	public void update() {
		rpaScenarioOperationResultSearchComposite.update();
	}
	
	public RpaScenarioOperationResultSearchComposite getRpaScenarioOperationResultSearchComposite() {
		return rpaScenarioOperationResultSearchComposite;
	}

	/**
	 * Get the number of selected items
	 * @return
	 */
	public int getSelectedNum(){
		return this.selectedNum;
	}

	@Override
	protected String getViewName() {
		return this.getClass().getName();
	}
	
	/**
	 * ビューのアクションの有効/無効を設定します。
	 *
	 * @param num 選択イベント数
	 * @param selection ボタン（アクション）を有効にするための情報
	 */
	public void setEnabledAction( int num, ISelection selection ){
		this.selectedNum = num;
		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}
	
	@Override
	public void setFocus() {
		super.setFocus();
		if (rpaScenarioOperationResultSearchComposite != null) {
			rpaScenarioOperationResultSearchComposite.setFocus();
		}
	}
}
