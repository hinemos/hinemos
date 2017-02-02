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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ViewPluginAction;
import org.eclipse.ui.part.ViewPart;

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
	@SuppressWarnings("unchecked")
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
}
