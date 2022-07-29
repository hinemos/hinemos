/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.ILoginUser;
import com.clustercontrol.xcloud.model.cloud.RoleRelation;
import com.clustercontrol.xcloud.ui.views.HinemosRole;

public class EditAssignRoleDialog extends CommonDialog implements CloudStringConstants {
	public static final long serialVersionUID = 1L;

	public static class DialogOutput{
		private String cloudScopeId;
		private String cloudUserId;
		public String getCloudScopeId() {return cloudScopeId;}
		public void setCloudScopeId(String cloudScopeId) {this.cloudScopeId = cloudScopeId;}
		public String getCloudUserId() {return cloudUserId;}
		public void setCloudUserId(String cloudUserId) {this.cloudUserId = cloudUserId;}
	}

	private List<DialogOutput> output;

	private Map<String, String> relationMap = new HashMap<String, String>();

	private HinemosRole role;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public EditAssignRoleDialog(Shell parentShell, HinemosRole role) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.TITLE | SWT.APPLICATION_MODAL);
		this.role = role;
	}

	@Override
	protected void customizeDialog(Composite parent) {
		GridLayout gl_container = new GridLayout(10, true);
		gl_container.marginWidth = 10;
		gl_container.marginHeight = 10;
		parent.setLayout(gl_container);
		GridData gd_container = new GridData(SWT.FILL, SWT.FILL, true, false);
		parent.setLayoutData(gd_container);

		Label lblManager = new Label(parent, SWT.RIGHT);
		lblManager.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		lblManager.setText(strManager + " " + strSeparator + " ");

		Label txtManager = new Label(parent, SWT.LEFT);
		txtManager.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		txtManager.setText(role.getManager().getManagerName());

		Label lblRole = new Label(parent, SWT.RIGHT);
		lblRole.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		lblRole.setText(strRole + " " + strSeparator + " ");

		Label txtRole = new Label(parent, SWT.LEFT);
		txtRole.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		txtRole.setText(role.getRoleInfo().getRoleName());

		ICloudScope[] entries = role.getManager().getCloudScopes().getCloudScopes();

		for(ICloudScope scope: entries){
			for(ILoginUser user: scope.getLoginUsers().getLoginUsers()){
				for(RoleRelation relation : user.getRoleRelations()){
					if(relation.getId().equals(role.getRoleInfo().getRoleId())){
						relationMap.put(scope.getId(), user.getId());
					}
				}
			}
		}

		TableViewer tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.CENTER);
		Table table = tableViewer.getTable();
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 10, 1);
		gd_table.heightHint = SWT.MIN;
		gd_table.widthHint = 400;
		table.setLayoutData(gd_table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableViewerColumn tvcCloudPlatform = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNewColumn = tvcCloudPlatform.getColumn();
		tblclmnNewColumn.setText(strCloudPlatform);
		tblclmnNewColumn.setWidth(220);
		tvcCloudPlatform.setLabelProvider(new ColumnLabelProvider() {
			@Override public String getText(Object element) {
				return ((ICloudScope) element).getCloudPlatform().getName()+" ( "+((ICloudScope) element).getPlatformId()+" ) ";
			}
		});
		
		TableViewerColumn tvcCloudScope = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNewColumn_1 = tvcCloudScope.getColumn();
		tblclmnNewColumn_1.setText(strCloudScope);
		tblclmnNewColumn_1.setWidth(130);
		tvcCloudScope.setLabelProvider(new ColumnLabelProvider() {
			@Override public String getText(Object element) {
				return ((ICloudScope) element).getName()+" ( "+((ICloudScope) element).getId()+" ) ";
			}
		});

		TableViewerColumn tvcAssignedUser = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNewColumn3 = tvcAssignedUser.getColumn();
		tblclmnNewColumn3.setText(strAssignedUser);
		tblclmnNewColumn3.setWidth(130);
		
		tvcAssignedUser.setLabelProvider(new ColumnLabelProvider() {
			
			@Override public String getText(Object element) {
				ICloudScope entry = (ICloudScope) element;
				if (relationMap.get(entry.getId()) != null) {
					for (ILoginUser user : entry.getLoginUsers().getLoginUsers()) {
						if (relationMap.get(entry.getId()).equals(user.getId())) {
							return user.getName()+" ( "+user.getId()+" ) "+(entry.getAccountId().equals(user.getId()) ? " [" + strMain + "]":"");
						}
					}
				}
				return strUnassgin;
			}
		});
		
		tvcAssignedUser.setEditingSupport(new EditingSupport(tableViewer) {
			@Override protected boolean canEdit(Object element) {return true;}
			@Override protected CellEditor getCellEditor(Object element) {
				ICloudScope entry = (ICloudScope) element;
				List<String> items = new ArrayList<>();
				items.add(strUnassgin);
				for(ILoginUser user: entry.getLoginUsers().getLoginUsers()){
					items.add(user.getName()+" ( "+user.getId()+" ) "+(entry.getAccountId().equals(user.getId()) ? " [" + strMain + "]":""));
				}
				return new ComboBoxCellEditor((Table)getViewer().getControl(), items.toArray(new String[]{}), SWT.READ_ONLY);
			}
			@Override protected Object getValue(Object element) {
				ICloudScope entry = (ICloudScope) element;
				if (relationMap.get(entry.getId()) == null)
					return 0;
				
				for (int i = 0; i < entry.getLoginUsers().getLoginUsers().length; ++i) {
					if (entry.getLoginUsers().getLoginUsers()[i].getId().equals(relationMap.get(entry.getId())))
						return i+1;
				}
				return -1;
			}
			@Override
			protected void setValue(Object element, Object value) {
				ICloudScope entry = (ICloudScope) element;
				int selected = Integer.parseInt(value.toString());
				if (0 > selected || selected > entry.getLoginUsers().getLoginUsers().length)
					return;

				if (selected == 0) {
					relationMap.remove(entry.getId());
				} else {
					relationMap.put(entry.getId(), entry.getLoginUsers().getLoginUsers()[selected-1].getId());
				}
				getViewer().refresh();
			}
		});
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(entries);

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
				false);
		createButton(parent, DialogConstants.CANCEL_ID,
				DialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 * 640
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(597, 265);
	}

	@Override
	protected void okPressed() {
		output = new ArrayList<>();
		for(Map.Entry<String, String> entry: relationMap.entrySet()){
			DialogOutput item = new DialogOutput();
			item.setCloudScopeId(entry.getKey());
			item.setCloudUserId(entry.getValue());
			output.add(item);
		}

		setReturnCode(OK);
		close();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(dlgRoleAddModify);
	}

	public List<DialogOutput> getOutput() {
		return output;
	}
}
