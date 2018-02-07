/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ui;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.Workbench;


/**
 * Dynamic perspective menu
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class PerspectiveMenu extends ContributionItem {
	private static Log log = LogFactory.getLog(PerspectiveMenu.class);

	private static final String DATAKEY = "PerspectiveDescriptor";

	private final SelectionListener selectionListener = new SwitchPerspectiveSelectionListener();

	public PerspectiveMenu() {}

	public PerspectiveMenu(String id) {
		super(id);
	}

	@Override
	public void fill(Menu menu, int index) {
		String activePerspective = getPerspectiveId();

		// create the menu items
		IPerspectiveDescriptor[] perspectives = PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives();

		// sorting
		Arrays.sort(perspectives, Comparator.comparing(IPerspectiveDescriptor::getLabel));

		int cnt = 0;
		for (IPerspectiveDescriptor descriptor: perspectives) {
			// Check activity to see if the perspective is filtered(disabled)
			if(WorkbenchActivityHelper.filterItem(descriptor))
				continue;

			MenuItem item = new MenuItem(menu, SWT.RADIO, index + cnt++);
			item.setData(DATAKEY, descriptor);
			item.setText(descriptor.getLabel());
			final Image image = descriptor.getImageDescriptor().createImage();
			item.setImage(image);

			item.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					image.dispose();
				}
			});

			item.addSelectionListener(selectionListener);

			if(descriptor.getId().equals(activePerspective)){
				item.setSelection(true);
			}
		}
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	/**
	 * Menu listener for switching perspective
	 */
	private static final class SwitchPerspectiveSelectionListener extends SelectionAdapter{
		@Override
		public void widgetSelected(SelectionEvent e) {
			MenuItem item = (MenuItem) e.widget;
			IPerspectiveDescriptor descriptor = (IPerspectiveDescriptor) item.getData(DATAKEY);

			IWorkbenchPage page = getActivePage();
			if(null!=page){
				page.setPerspective(descriptor);
			} else {
				IAdaptable input = ((Workbench) PlatformUI.getWorkbench()).getDefaultPageInput();
				try {
					getActiveWindow().openPage(descriptor.getId(), input);
				} catch (WorkbenchException ex) {
					log.error("Perspective could not be opened.", ex); //$NON-NLS-1$
				}
			}

		}
	}

	private static String getPerspectiveId(){
		IWorkbenchPage page = getActivePage();
		if(null != page){
			IPerspectiveDescriptor descriptor = page.getPerspective();
			if(null != descriptor){
				return descriptor.getId();
			}
		}
		return null;
	}

	private static IWorkbenchWindow getActiveWindow(){
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	private static IWorkbenchPage getActivePage(){
		IWorkbenchWindow window = getActiveWindow();
		if(null != window){
			return window.getActivePage();
		}
		return null;
	}
}
