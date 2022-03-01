/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.views;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

import com.clustercontrol.util.RestLoginManager;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.ElementBaseModeWatch;
import com.clustercontrol.xcloud.model.base.IElement;

public abstract class AbstractCloudViewPart extends ViewPart {
	protected abstract class Watcher<T> implements ElementBaseModeWatch.AnyPropertyWatcher {
		protected Class<T> targetClass;
		protected Runnable refreshTask;
		
		@SuppressWarnings("unchecked")
		public Watcher() {
			Type type = ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
			if (!(type instanceof Class))
				throw new CloudModelException("");
			targetClass = (Class<T>)type;
		}
		
		@Override
		public void elementAdded(ElementAddedEvent event) {
			if (targetClass.isAssignableFrom(event.getAddedElement().getClass()))
				modified();
		}

		@Override
		public void elementRemoved(ElementRemovedEvent event) {
			if (targetClass.isAssignableFrom(event.getRemovedElement().getClass()))
				modified();
		}

		@Override
		public void propertyChanged(ValueChangedEvent event) {
			if (targetClass.isAssignableFrom(event.getSource().getClass())) {
				modified();
			}
		}

		@Override
		public void unwatched(IElement owner, IElement owned) {
			unwatchedOwner(owner, owned);
		}
		
		protected void modified() {
			if (refreshTask == null) {
				refreshTask = new Runnable() {
					@Override
					public void run() {
						try {
							Watcher.this.asyncRefresh();
						} finally {
							refreshTask = null;
						}
					}
				};
				Display.getCurrent().asyncExec(refreshTask);
			}
		}

		protected abstract void asyncRefresh();

		protected abstract void unwatchedOwner(IElement owning, IElement owned);
	};
	
	public abstract String getId();
	
	public AbstractCloudViewPart() {
		if (0 < RestLoginManager.getLoginAttempts()) {
			if(!RestLoginManager.isLogin()){
				// TODO whey need to login again here?
				RestLoginManager.login(getViewSite().getWorkbenchWindow());
			}
		}
	}
	
	@Override
	public void createPartControl(Composite parent) {
		internalCreatePartControl(parent);
		if (isNeedDefaultPopupMenu()) createDefaultContextMenu();
	}

	protected abstract void internalCreatePartControl(Composite parent);

	protected abstract StructuredViewer getViewer();
	
	@Override
	public void setFocus() {
		getViewer().getControl().setFocus();
	}
	
	protected void createDefaultContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu(getViewer().getControl());
		getViewer().getControl().setMenu(menu);
		getSite().registerContextMenu(menuManager, getSite().getSelectionProvider());
	}

	protected void fillContextMenu(IMenuManager manager) {
	}

	protected boolean isNeedDefaultPopupMenu() {
		return true;
	}
	
	public abstract void update();
}
