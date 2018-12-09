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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.ws.repository.FacilityTreeItem;
import com.clustercontrol.ws.xcloud.AutoAssignNodePatternEntry;
import com.clustercontrol.ws.xcloud.AutoAssignNodePatternEntryType;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.util.CloudUtil;

public class AutoAssignNodeRuleEditDialog extends CommonDialog implements CloudStringConstants {
	public static final long serialVersionUID = 1L;
	
	private String managerName;
	private List<AutoAssignNodePatternEntry> entries = new ArrayList<>();
	private List<AutoAssignNodePatternEntry> completed = new ArrayList<>();
	
	private Table table;
	private Button btnArrowUp;
	private Button btnArrowDown;
	private Button btnDelete;
	
	private static final Map<AutoAssignNodePatternEntryType, String> patternTypes = new HashMap<AutoAssignNodePatternEntryType, String>() {
		private static final long serialVersionUID = 1L;{
			put(AutoAssignNodePatternEntryType.INSTANCE_NAME, strComputeName);
			put(AutoAssignNodePatternEntryType.CIDR, strIpAddress);
		}};
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public AutoAssignNodeRuleEditDialog(Shell parentShell, String managerName) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.TITLE | SWT.APPLICATION_MODAL);
		this.managerName = managerName;
	}

	@Override
	protected void customizeDialog(Composite parent) {
		GridData gd_parent = new GridData(GridData.FILL_BOTH);
		parent.setLayoutData(gd_parent);
		GridLayout gl_parent = new GridLayout(3, false);
		gl_parent.marginBottom = 10;
		gl_parent.marginTop = 10;
		gl_parent.marginRight = 10;
		gl_parent.marginLeft = 10;
		parent.setLayout(gl_parent);
		
		final TableViewer tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (!tableViewer.getSelection().isEmpty())
					updateButton();
			}
		});
		table = tableViewer.getTable();
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (e.count != 2) {
					Point p =new Point(e.x, e.y);
					ViewerCell cell = tableViewer.getCell(p);
					if (cell != null && cell.getColumnIndex() == 3) {
						ScopeTreeDialog dialog = new ScopeTreeDialog(AutoAssignNodeRuleEditDialog.this.getShell(), managerName, "ADMINISTRATORS", true, false);
						p = AutoAssignNodeRuleEditDialog.this.getShell().toDisplay(p);
						dialog.create();
						dialog.getShell().setLocation(p.x, p.y);
						if (dialog.open() == DialogConstants.OK_ID) {
							FacilityTreeItem item = dialog.getSelectItem();
							((AutoAssignNodePatternEntry)cell.getElement()).setScopeId(item.getData().getFacilityId());
							tableViewer.refresh();
							valid();
						}
					}
				}
			}
		});
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 3);
		gd_table.heightHint = 233;
		table.setLayoutData(gd_table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNewColumn = tableViewerColumn.getColumn();
		tblclmnNewColumn.setWidth(50);
		tblclmnNewColumn.setText(strPriority);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override public String getText(Object element) {return Integer.toString(entries.indexOf(element) + 1);}
		});
		
		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNewColumn_1 = tableViewerColumn_1.getColumn();
		tblclmnNewColumn_1.setWidth(100);
		tblclmnNewColumn_1.setText(strEvaluteItem);
		tableViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {
			@Override public String getText(Object element) {
				AutoAssignNodePatternEntry entry = (AutoAssignNodePatternEntry)element;
				String typeName = patternTypes.get(entry.getPatternType());
				return typeName != null ? typeName: entry.getPatternType().name();
			}
		});
		tableViewerColumn_1.setEditingSupport(new EditingSupport(tableViewer) {
			String[] items = new String[]{patternTypes.get(AutoAssignNodePatternEntryType.INSTANCE_NAME), patternTypes.get(AutoAssignNodePatternEntryType.CIDR)};
			@Override protected boolean canEdit(Object element) {return true;}
			@Override protected CellEditor getCellEditor(Object element) {
				return new ComboBoxCellEditor((Table)getViewer().getControl(), items);
			}
			@Override protected Object getValue(Object element) {
				AutoAssignNodePatternEntry entry = (AutoAssignNodePatternEntry)element;
				for (int i = 0; i < items.length; ++i) {
					if (patternTypes.get(entry.getPatternType()).equals(items[i]))
						return i;
				}
				return -1;
			}
			@Override
			protected void setValue(Object element, Object value) {
				int index = Integer.parseInt(value.toString());
				if (0 > index || index >= items.length)
					return;
				String selected = items[index];
				for (Map.Entry<AutoAssignNodePatternEntryType, String> entry: patternTypes.entrySet()) {
					if (entry.getValue().equals(selected)) {
						((AutoAssignNodePatternEntry)element).setPatternType(entry.getKey());
						getViewer().refresh();
						break;
					}
				}
				valid();
			}
		});
		
		TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNewColumn_2 = tableViewerColumn_2.getColumn();
		tblclmnNewColumn_2.setWidth(150);
		tblclmnNewColumn_2.setText(strMatchingCondition);
		tableViewerColumn_2.setLabelProvider(new ColumnLabelProvider() {
			@Override public String getText(Object element) {return ((AutoAssignNodePatternEntry)element).getPattern();}
			@Override public Color getBackground(Object element) {
				AutoAssignNodePatternEntry entry = (AutoAssignNodePatternEntry)element;
				return entry.getPattern() == null || entry.getPattern().isEmpty() ? RequiredFieldColorConstant.COLOR_REQUIRED: null;
			}
		});
		tableViewerColumn_2.setEditingSupport(new EditingSupport(tableViewer) {
			@Override protected boolean canEdit(Object element) {return true;}
			@Override protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor((Table)getViewer().getControl());
			}
			@Override protected Object getValue(Object element) {return ((AutoAssignNodePatternEntry)element).getPattern();}
			@Override protected void setValue(Object element, Object value) {((AutoAssignNodePatternEntry)element).setPattern(value.toString());getViewer().refresh();valid();}
		});
		
		TableViewerColumn tableViewerColumn_3 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNewColumn_3 = tableViewerColumn_3.getColumn();
		tblclmnNewColumn_3.setWidth(150);
		tblclmnNewColumn_3.setText(strScope);
		tableViewerColumn_3.setLabelProvider(new ColumnLabelProvider() {
			@Override public String getText(Object element) {
				AutoAssignNodePatternEntry entry = (AutoAssignNodePatternEntry)element;
				return entry.getScopeId() == null || entry.getScopeId().isEmpty() ? "": CloudUtil.getFacilityPath(managerName, entry.getScopeId());
			}
			@Override public Color getBackground(Object element) {
				AutoAssignNodePatternEntry entry = (AutoAssignNodePatternEntry)element;
				return entry.getScopeId() == null || entry.getScopeId().isEmpty() ? RequiredFieldColorConstant.COLOR_REQUIRED: null;
			}
		});
		
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(entries);

		btnArrowUp = new Button(parent, SWT.NONE);
		btnArrowUp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
				if (!selection.isEmpty()) {
					int index = entries.indexOf(selection.getFirstElement());
					if (index != 0) {
						entries.remove(selection.getFirstElement());
						entries.add(index - 1, (AutoAssignNodePatternEntry)selection.getFirstElement());
						tableViewer.refresh();
						tableViewer.setSelection(new StructuredSelection(selection.getFirstElement()));
					}
				}
				updateButton();
			}
		});
		
		GridData gd_btnNewButton_2 = new GridData(SWT.CENTER, SWT.FILL, false, false, 1, 1);
		gd_btnNewButton_2.widthHint = 30;
		btnArrowUp.setLayoutData(gd_btnNewButton_2);
		btnArrowUp.setText(strArrowUp);
		btnArrowUp.setEnabled(false);
		
		btnArrowDown = new Button(parent, SWT.NONE);
		btnArrowDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
				if (!selection.isEmpty()) {
					int index = entries.indexOf(selection.getFirstElement());
					if (index != entries.size() - 1) {
						entries.remove(selection.getFirstElement());
						entries.add(index + 1, (AutoAssignNodePatternEntry)selection.getFirstElement());
						tableViewer.refresh();
						tableViewer.setSelection(new StructuredSelection(selection.getFirstElement()));
					}
				}
				updateButton();
			}
		});
		GridData gd_btnNewButton_3 = new GridData(SWT.CENTER, SWT.FILL, false, false, 1, 1);
		gd_btnNewButton_3.widthHint = 30;
		btnArrowDown.setLayoutData(gd_btnNewButton_3);
		btnArrowDown.setText(strArrowDown);
		btnArrowDown.setEnabled(false);
		
		Composite composite_1 = new Composite(parent, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		
		Button button = new Button(composite_1, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AutoAssignNodePatternEntry entry = new AutoAssignNodePatternEntry();
				entry.setPatternType(AutoAssignNodePatternEntryType.CIDR);
				entry.setPattern("");
				entry.setScopeId("");
				entries.add(entry);
				tableViewer.refresh();
				tableViewer.setSelection(new StructuredSelection(entry));
				updateButton();
				valid();
			}
		});
		button.setText(strAdd);
		button.setBounds(272, 0, 90, 25);
		
		btnDelete = new Button(composite_1, SWT.NONE);
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
				if (selection.isEmpty()) {
					updateButton();
					return;
				}
				
				int index = entries.indexOf(selection.getFirstElement());
				entries.remove(selection.getFirstElement());
				tableViewer.refresh();
				if (!entries.isEmpty())
					tableViewer.setSelection(new StructuredSelection(entries.get(Math.min(index, entries.size() - 1))));
				updateButton();
				valid();
			}
		});
		btnDelete.setText(strDelete);
		btnDelete.setEnabled(false);
		btnDelete.setBounds(368, 0, 92, 25);
		
		updateButton();
		
		//pack:resize to be its preferred size
		getShell().pack();
		getShell().setSize(getInitialSize());
		
		Display display = getShell().getDisplay();
		getShell().setLocation((display.getBounds().width - getShell().getSize().x) / 2,
				(display.getBounds().height - getShell().getSize().y) / 2);
	}
	
	private void updateButton() {
		int index = table.getSelectionIndex();
		if (index != -1) {
			btnDelete.setEnabled(true);
			if (table.getItemCount() > 1) {
				if (index == 0) {
					btnArrowUp.setEnabled(false);
					btnArrowDown.setEnabled(true);
				} else if (index == table.getItemCount() - 1) {
					btnArrowUp.setEnabled(true);
					btnArrowDown.setEnabled(false);
				} else {
					btnArrowUp.setEnabled(true);
					btnArrowDown.setEnabled(true);
				}
			} else {
				btnArrowUp.setEnabled(false);
				btnArrowDown.setEnabled(false);
			}
		} else {
			btnDelete.setEnabled(false);
			btnArrowUp.setEnabled(false);
			btnArrowDown.setEnabled(false);
		}
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control c = super.createContents(parent);
		valid();
		return c;
	}

	
	private void valid() {
		boolean valid = true;
		for (AutoAssignNodePatternEntry entry: this.entries) {
			if ((entry.getScopeId() == null || entry.getScopeId().isEmpty()) ||
				entry.getPatternType() == null ||
				(entry.getPattern() == null || entry.getPattern().isEmpty())
				) {
				valid = false;
				break;
			}
		}
		getButton(DialogConstants.OK_ID).setEnabled(valid);
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
		return new Point(539, 390);
	}
	
	public void setInput(List<AutoAssignNodePatternEntry> entries) {
		this.entries.clear();
		this.completed.clear();
		for (AutoAssignNodePatternEntry entry: entries) {
			AutoAssignNodePatternEntry e = new AutoAssignNodePatternEntry();
			e.setScopeId(entry.getScopeId());
			e.setPatternType(entry.getPatternType());
			e.setPattern(entry.getPattern());
			this.entries.add(e);
		}
	}
	
	public List<AutoAssignNodePatternEntry> getOutput() {
		return completed;
	}
	
	@Override
	protected void okPressed() {
		completed.clear();
		for (AutoAssignNodePatternEntry entry: this.entries) {
			AutoAssignNodePatternEntry e = new AutoAssignNodePatternEntry();
			e.setScopeId(entry.getScopeId());
			e.setPatternType(entry.getPatternType());
			e.setPattern(entry.getPattern());
			this.completed.add(e);
		}
		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(dlgComputeAssignScopeRule);
	}
}
