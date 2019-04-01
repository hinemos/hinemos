/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.ViewPluginAction;
import org.eclipse.ui.part.ViewPart;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.DefaultLayoutSettingManager.ViewLayout;
import com.clustercontrol.util.FocusUtil;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 共通ViewPartクラス<BR>
 * 
 * クラスタコントローラ用のViewにて基底クラスとして使用する
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class CommonViewPart extends ViewPart {
	private static Log m_log = LogFactory.getLog(CommonViewPart.class); 
	
	//ビューレイアウト情報
	private ViewLayout defaultViewLayout = null;
	
	
	@Override
	public void init( IViewSite site ) throws PartInitException{
		super.init( site );

		// Set testid for test
		Widget widget;
		widget = ((ToolBarManager)getViewSite().getActionBars().getToolBarManager()).getControl();
		if( null != widget ){
			WidgetTestUtil.setTestId( this, null, widget );
		}
		widget = ((MenuManager)getViewSite().getActionBars().getMenuManager()).getMenu();
		if( null != widget ){
			WidgetTestUtil.setTestId( this, null, widget );
		}
		
		if (this.isDefaultLayoutView()) {
			defaultViewLayout = ClusterControlPlugin.getDefault().getDefaultLayoutSettingManager().getViewLayout(this.getClass().getSimpleName());
		}
	}

	/**
	 * コンストラクタ
	 */
	public CommonViewPart(){
		super(); 
	}

	protected abstract String getViewName();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		// this.addListenerObject(listener);
		m_log.debug("createPartControl");
		try {
			this.getSite().getPage().addPartListener(new IPartListener2() {
				
				@Override
				public void partActivated(IWorkbenchPartReference partRef) {
					try {
						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						m_log.trace("Activate " + getViewName() + ", " + page.getActivePart().getClass().getName());
						FocusUtil.setView(page.getActivePart().getClass().getName());
					} catch (Exception e) {
						m_log.warn("partActivated : " + e.getMessage());
					}
				}
				@Override
				public void partBroughtToTop(IWorkbenchPartReference partRef) {
					m_log.trace("BroughtToTop " + getViewName());
				}
				@Override
				public void partClosed(IWorkbenchPartReference partRef) {
					m_log.trace("Closed " + getViewName());
				}
				@Override
				public void partDeactivated(IWorkbenchPartReference partRef) {
					m_log.trace("Deactivated " + getViewName());
				}
				@Override
				public void partOpened(IWorkbenchPartReference partRef) {
					m_log.trace("Opened " + getViewName());
				}
				@Override
				public void partHidden(IWorkbenchPartReference partRef) {
					m_log.trace("Hidden " + getViewName());
				}
				@Override
				public void partVisible(IWorkbenchPartReference partRef) {
					m_log.trace("Visible " + getViewName());
				}
				@Override
				public void partInputChanged(IWorkbenchPartReference partRef) {
					m_log.trace("InputChanged " + getViewName());
				}

			});
		} catch (Exception e) {
			m_log.warn("createPartControl() " + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		m_log.trace("setFocus " + getViewName());
	}

	/**
	 * アダプターとして要求された場合、自身のインスタンスを渡します。
	 * 
	 * @return 自身のインスタンス
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 * @since 1.0.0
	 */
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class cls) {
		if (cls.isInstance(this)) {
			return this;
		} else {
			return super.getAdapter(cls);
		}
	}

	/**
	 * @param enable
	 */
	public void setEnabledAction(String actionID, boolean enable) {
		ActionContributionItem ci = (ActionContributionItem)getViewSite().getActionBars().getToolBarManager().find(actionID);
		IAction action =  ci.getAction();
		action.setEnabled(enable);
	}

	/**
	 * 
	 * @since
	 */
	public void setEnabledActionAll(boolean enable) {
		IToolBarManager tm = getViewSite().getActionBars().getToolBarManager();
		IContributionItem[] cis = tm.getItems();

		for (int i = 0; i < cis.length; i++) {
			if (cis[i] instanceof ActionContributionItem) {
				ActionContributionItem ci = (ActionContributionItem) cis[i];
				ci.getAction().setEnabled(enable);
			}
		}
	}

	/**
	 * @param enable
	 */
	public void setEnabledAction(String actionID,ISelection selection) {
		ActionContributionItem ci = (ActionContributionItem)getViewSite().getActionBars().getToolBarManager().find(actionID);
		ViewPluginAction action =  (ViewPluginAction)ci.getAction();
		action.selectionChanged(selection);
	}

	/**
	 * コマンドを実行します。
	 * 
	 * @param commandId 実行するコマンドのID。
	 */
	protected void executeCommand(String commandId) {
		IWorkbenchPartSite site = getSite();
		if (site == null) {
			m_log.info("executeCommand: WorkbenchPartSite not found.");
			return;
		}
		IHandlerService service = (IHandlerService) site.getService(IHandlerService.class);
		if (service == null) {
			m_log.info("executeCommand: HandlerService not found.");
			return;
		}
		try {
			service.executeCommand(commandId, null);
		} catch (Exception e) {
			m_log.warn("executeCommand: Exception. ", e);
		}
	}

	/**
	 * コマンドに紐付いた要素をリフレッシュします。
	 * 
	 * @param commandIds 対象のコマンドのID。
	 */
	protected void refreshCommands(String... commandIds) {
		IWorkbenchPartSite site = getSite();
		if (site == null) {
			m_log.info("executeCommand: WorkbenchPartSite not found.");
			return;
		}
		ICommandService service = (ICommandService) site.getService(ICommandService.class);
		if (service == null) {
			m_log.info("refreshCommands: CommandService not found.");
			return;
		}
		for (String id : commandIds) {
			service.refreshElements(id, null);
		}

		// Update ToolBar after elements refreshed
		// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
		getViewSite().getActionBars().updateActionBars();
		getViewSite().getActionBars().getToolBarManager().update(false);
	}

	/**
	 * デフォルトレイアウトカスタマイズ対応ビューの場合、trueを返却
	 */
	public boolean isDefaultLayoutView() {
		return false;
	}
	
	protected ViewLayout getDefaultViewLayout() {
		return this.defaultViewLayout;
	}
}
