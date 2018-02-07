/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.dialog;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogLabelKeys;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.internal.activities.ws.ActivityViewerFilter;
import org.eclipse.ui.internal.dialogs.PerspContentProvider;
import org.eclipse.ui.model.PerspectiveLabelProvider;

import com.clustercontrol.client.ui.WorkbenchMessagesX;

/**
 * Customized perspective dialog
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
@SuppressWarnings("restriction")
public class PerspectiveDialog extends Dialog implements ISelectionChangedListener {
	final private static int LIST_HEIGHT = 320;
	final private static int LIST_WIDTH = 320;

	private Set<String> selectedPerspectives = new LinkedHashSet<>();

	private TableViewer perspectiveTable;
	private Button okButton;
	private IPerspectiveRegistry perspReg;

	private ActivityViewerFilter activityViewerFilter = new ActivityViewerFilter();

	/**
	 * Constructor
	 */
	public PerspectiveDialog(Shell parent, IPerspectiveRegistry perspReg) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.SHEET);

		this.perspReg = perspReg;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(WorkbenchMessagesX.SelectPerspective_shellTitle);
	}

	/**
	 * Create dialog
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		createViewer(composite);
		return composite;
	}

	/**
	 * Add buttons
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, JFaceResources.getString(IDialogLabelKeys.OK_LABEL_KEY), true);
		createButton(parent, IDialogConstants.CANCEL_ID, JFaceResources.getString(IDialogLabelKeys.CANCEL_LABEL_KEY), false);
		updateButtons();
	}

	/**
	 * Update the button enablement state.
	 */
	protected void updateButtons() {
		okButton.setEnabled(!selectedPerspectives.isEmpty());
	}

	/**
	 * Handle double click event
	 */
	protected void handleDoubleClickEvent() {
		okPressed();
	}

	/**
	 * Create a table viewer to list perspectives
	 */
	private void createViewer(Composite parent) {
		perspectiveTable = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		perspectiveTable.getTable().setFont(parent.getFont());
		perspectiveTable.setLabelProvider(new PerspectiveLabelProvider());
		perspectiveTable.setContentProvider(new PerspContentProvider());
		perspectiveTable.addFilter(activityViewerFilter);
		perspectiveTable.setComparator(new ViewerComparator());
		perspectiveTable.setInput(perspReg);
		perspectiveTable.addSelectionChangedListener(this);
		perspectiveTable.addDoubleClickListener(event -> handleDoubleClickEvent());

		// layout
		GridData spec = new GridData(GridData.FILL_BOTH);
		spec.widthHint = LIST_WIDTH;
		spec.heightHint = LIST_HEIGHT;
		perspectiveTable.getControl().setLayoutData(spec);
	}

	/**
	 * Returns the selections.
	 */
	public Set<String> getSelectedPerspectives() {
		return selectedPerspectives;
	}

	/**
	 * Clear the selections before cancel
	 */
	@Override
	protected void cancelPressed() {
		selectedPerspectives.clear();
		super.cancelPressed();
	}

	/**
	 * Update selected perspectives set while selection changed
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		// Clear and get selected perspectives
		selectedPerspectives.clear();
		IStructuredSelection selected = (IStructuredSelection)perspectiveTable.getSelection();

		if (!selected.isEmpty()) {
			Iterator<?> iter = selected.iterator();
			while(iter.hasNext()){
				Object obj = iter.next();
				if (obj instanceof IPerspectiveDescriptor) {
					selectedPerspectives.add(((IPerspectiveDescriptor)obj).getId());
				}
			}
		}
		updateButtons();
	}

}
