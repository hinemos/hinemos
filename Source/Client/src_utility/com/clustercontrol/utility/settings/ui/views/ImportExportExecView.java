/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.views;

import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.utility.settings.ui.bean.FuncInfo;
import com.clustercontrol.utility.settings.ui.composite.HinemosFuncTreeComposite;
import com.clustercontrol.utility.settings.ui.composite.XMLListComposite;
import com.clustercontrol.utility.settings.ui.composite.action.ImportExportSelectionChangedListener;
import com.clustercontrol.utility.settings.ui.views.commands.DeleteSettingCommand;
import com.clustercontrol.utility.settings.ui.views.commands.DiffSettingCommand;
import com.clustercontrol.utility.settings.ui.views.commands.ExportSettingCommand;
import com.clustercontrol.utility.settings.ui.views.commands.ImportSettingCommand;
import com.clustercontrol.utility.settings.ui.views.commands.RefreshXMLCommand;
import com.clustercontrol.view.CommonViewPart;


/**
 * インポートエクスポートメインビュークラスです。
 *
 * @version 6.1.0
 * @since 1.2.0
 */
public class ImportExportExecView extends CommonViewPart {
	/** ビューID */
	public static final String ID = "com.clustercontrol.utility.settings.ui.views.ImportExportExecView";
	/** サッシュ */
	private SashForm m_sash = null;
	/** 機能ツリー用コンポジット */
	private HinemosFuncTreeComposite m_FuncTree = null;
	/** [一覧]ビュー用のコンポジット */
	private XMLListComposite m_XMLList = null;
	/** ツリーアイテム */
	//protected FuncTreeItem m_copyFuncTreeItem = null;


	/**
 	* コンストラクタ
 	*/
	public ImportExportExecView() {
		super();
	}

	/**
 	* ビューを構築します。
 	*
 	* @param parent 親コンポジット
 	*
 	* @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
 	* @see #createContextMenu()
 	* @see #update()
 	*/
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		m_sash = new SashForm(parent, SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_sash.setLayoutData(gridData);

		//ジョブ階層ツリー作成
		m_FuncTree = new HinemosFuncTreeComposite(m_sash, SWT.NONE);

		//ジョブ一覧作成
		m_XMLList = new XMLListComposite(m_sash, SWT.NONE);

		//Sashの境界を調整 左部25% 右部75%
		m_sash.setWeights(new int[] { 23, 77 });

		//ポップアップメニュー作成
		createContextMenu();

		// ボタン（アクション）を制御するリスナーを登録
		this.m_FuncTree.getTreeViewer().addSelectionChangedListener(new ImportExportSelectionChangedListener());

		//ビューを更新
		this.update();
		//ツリーを開く
		m_FuncTree.getTreeViewer().expandToLevel(2);
	}

	/**
	 * コンテキストメニューを作成します。
	 *
	 * @see org.eclipse.jface.action.MenuManager
	 * @see org.eclipse.swt.widgets.Menu
	 */
	protected void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		Menu treeMenu = menuManager.createContextMenu(m_FuncTree.getTree());
		WidgetTestUtil.setTestId(this, null, treeMenu);
		m_FuncTree.getTree().setMenu(treeMenu);
		getSite().registerContextMenu( menuManager, this.m_FuncTree.getTreeViewer() );

		menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		Menu listMenu = menuManager.createContextMenu(m_XMLList.getTable());
		WidgetTestUtil.setTestId(this, null, listMenu);
		m_XMLList.getTable().setMenu(listMenu);
		getSite().registerContextMenu( menuManager, this.m_XMLList.getTableViewer() );
	}

	/**
	 * ビューを更新します。
	 *
	 */
	public void update() {
	   // m_FuncTree.update();
		m_XMLList.update();
	}

	/**
	 * 機能ツリー用のコンポジットを返します。
	 *
	 * @return 機能ー用のコンポジット
	 */
	public HinemosFuncTreeComposite getJobTreeComposite() {
		return m_FuncTree;
	}

	/**
	 * 一覧ビュー用のコンポジットを返します。
	 *
	 * @return ジョブ[一覧]ビュー用のコンポジット
	 */
	public XMLListComposite getJobListComposite() {
		return m_XMLList;
	}

	/**
	 *機能ツリーを表示します。
	 */
	/*
	public void show() {
		m_sash.setMaximizedControl(null);
	}
	*/
	/**
	 * 機能ツリーを非表示にします。
	 */
	/*
	public void hide() {
		m_sash.setMaximizedControl(m_XMLList);
	}
	*/
	/**
	 * 選択されている機能のリストを返します。(delegate)
	 * @return 選択されている機能のリスト
	 */
	public List<FuncInfo> getCheckedFunc(){
		return m_FuncTree.getCheckedFunc();

	}
	
	/**
	 * オブジェクト権限を返します。(delegate)
	 * @return オブジェクト権限
	 */
	public FuncInfo getObjectPrivilegeFunc(){
		return m_FuncTree.getObjectPrivilegeFunc();

	}

	/**
	 * ビューのアクションの有効/無効を設定
	 *
	 */
	public void setEnabledAction() {
		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			service.refreshElements(ImportSettingCommand.ID, null);
			service.refreshElements(ExportSettingCommand.ID, null);
			service.refreshElements(DeleteSettingCommand.ID, null);
			service.refreshElements(DiffSettingCommand.ID, null);
			service.refreshElements(RefreshXMLCommand.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);

		}

	}

	@Override
	protected String getViewName() {
		return this.getClass().getName();
	}

}