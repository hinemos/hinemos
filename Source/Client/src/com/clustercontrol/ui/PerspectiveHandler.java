/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ui;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;

import com.clustercontrol.dialog.PerspectiveDialog;

/**
 * Customized perspective handler
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public final class PerspectiveHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		// Get the view identifier, if any.
		@SuppressWarnings("rawtypes")
		final Map parameters = event.getParameters();
		final Object perspectiveId = parameters.get(IWorkbenchCommandConstants.PERSPECTIVES_SHOW_PERSPECTIVE_PARM_ID);

		if (perspectiveId == null) {
			openSelectDialog(window);
		} else {
			openPerspective((String) perspectiveId, window);
		}
		return null;
	}

	/**
	 * Opens a perspective selection dialog
	 */
	private final void openSelectDialog(final IWorkbenchWindow window) throws ExecutionException {
		final PerspectiveDialog dialog = new PerspectiveDialog(
				window.getShell(), WorkbenchPlugin.getDefault().getPerspectiveRegistry());

		if (dialog.open() == Window.CANCEL) {
			return;
		}

		Set<String> perspectives = dialog.getSelectedPerspectives();
		
		if (!perspectives.isEmpty()) {
			for(String perspectiveId: perspectives){
				openPerspective(perspectiveId, window);
			}
		}
	}

	/**
	 * Open the perspective while a ID is passed.
	 */
	private final void openPerspective(final String perspectiveId, final IWorkbenchWindow window) throws ExecutionException {
		final IWorkbench workbench = window.getWorkbench();
		final IWorkbenchPage activePage = window.getActivePage();
		IPerspectiveDescriptor desc = window.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
		if (desc == null) {
			throw new ExecutionException("Perspective " + perspectiveId + " not found!"); //$NON-NLS-1$
		}

		try {
			if (activePage != null) {
				activePage.setPerspective(desc);
			} else {
				IAdaptable input = ((Workbench) workbench).getDefaultPageInput();
				window.openPage(perspectiveId, input);
			}
		} catch (WorkbenchException e) {
			throw new ExecutionException("Perspective could not be opened.", e); //$NON-NLS-1$
		}
	}

}
