/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.dialogs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.ws.xcloud.Instance;
import com.clustercontrol.ws.xcloud.ModifyInstanceRequest;
import com.clustercontrol.ws.xcloud.Tag;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.extensions.CloudOptionExtension;

public class ModifyInstanceTagDialog extends CommonDialog implements CloudStringConstants {
	public static final long serialVersionUID = 1L;
	
	private Instance instance;
	private String cloudPlatformId;
	private List<Tag> editingTag = new ArrayList<>();
	private String editingMemo;
	private ModifyInstanceRequest completed;
	
	private Table table;
	private Text text;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public ModifyInstanceTagDialog(Shell parentShell, Instance instance, String cloudPlatformId) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.TITLE | SWT.APPLICATION_MODAL);
		setInput(instance);
		this.cloudPlatformId = cloudPlatformId;
	}

	@Override
	protected void customizeDialog(Composite parent) {
		GridLayout gl_parent = new GridLayout(1, false);
		gl_parent.marginBottom = 10;
		gl_parent.marginTop = 10;
		gl_parent.marginRight = 10;
		gl_parent.marginLeft = 10;
		
		parent.setLayout(gl_parent);
		GridData gd_parent = new GridData(GridData.FILL_BOTH);
		parent.setLayoutData(gd_parent);
		
		Group group = new Group(parent, SWT.NONE);
		group.setText(strAttribute);
		group.setLayout(new GridLayout(4, false));
		GridData gd_group = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_group.heightHint = 227;
		group.setLayoutData(gd_group);
		
		final TableViewer tableViewer = new TableViewer(group, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
		gd_table.heightHint = 100;
		table.setLayoutData(gd_table);
		
		Composite composite = new Composite(group, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		
		Button btnNewButton_1 = new Button(composite, SWT.NONE);
		btnNewButton_1.setBounds(272, 0, 90, 25);
		btnNewButton_1.setText(strAdd);
		
		Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.setBounds(368, 0, 92, 25);
		btnNewButton.setText(strDelete);
		btnNewButton.setEnabled(false);
		
		Label lblNewLabel = new Label(parent, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		lblNewLabel.setText(strMemo + strSeparator);
		
		text = new Text(parent, SWT.BORDER | SWT.MULTI);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				editingMemo = text.getText();
			}
		});
		GridData gd_text = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_text.heightHint = 103;
		text.setLayoutData(gd_text);
		
		text.setText(editingMemo);
		
		new TagEditor(tableViewer, null, btnNewButton_1, btnNewButton, editingTag);
		
		//pack:resize to be its preferred size
		getShell().pack();
		getShell().setSize(new Point(getShell().getSize().x, getShell().getSize().y));
		
		Display display = getShell().getDisplay();
		getShell().setLocation((display.getBounds().width - getShell().getSize().x) / 2,
				(display.getBounds().height - getShell().getSize().y) / 2);
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, DialogConstants.OK_ID, DialogConstants.OK_LABEL,
				true);
		createButton(parent, DialogConstants.CANCEL_ID,
				DialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(512, 498);
	}
	
	protected void setInput(Instance instance) {
		completed = null;
		this.instance =instance;
		editingTag.clear();
		for (Tag t: instance.getTags()) {
			Tag nt = new Tag();
			nt.setTagType(t.getTagType());
			nt.setKey(t.getKey());
			nt.setValue(t.getValue());
			this.editingTag.add(nt);
		}
		editingMemo = instance.getMemo() != null ? instance.getMemo(): "";
	}

	public ModifyInstanceRequest getOutput() {
		return completed;
	}

	@Override
	protected void okPressed() {
		completed = new ModifyInstanceRequest();
		
		completed.setInstanceId(instance.getId());
		completed.getTags().clear();
		for (Tag t: editingTag) {
			Tag nt = new Tag();
			nt.setTagType(t.getTagType());
			nt.setKey(t.getKey());
			nt.setValue(t.getValue());
			this.completed.getTags().add(nt);
		}
		completed.setMemo(text.getText());
		
		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(MessageFormat.format(dlgComputeShowDetail, CloudOptionExtension.getOptions().get(cloudPlatformId)));
	}
}
