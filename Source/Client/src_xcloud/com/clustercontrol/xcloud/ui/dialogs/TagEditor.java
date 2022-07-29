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

import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.openapitools.client.model.TagRequest;
import org.openapitools.client.model.TagRequest.TagTypeEnum;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.xcloud.common.CloudStringConstants;

public class TagEditor implements CloudStringConstants {
	private static final Map<TagTypeEnum, String> tagTypes = new HashMap<TagTypeEnum, String>() {
		private static final long serialVersionUID = 1L;{
			put(TagTypeEnum.AUTO, strAuto);
			put(TagTypeEnum.CLOUD, strCloud);
			put(TagTypeEnum.LOCAL, strLocal);
		}
	};

	protected TableViewer tableViewer;
	protected Button btnNewTag;
	protected Button btnDeleteTag;
	protected List<TagRequest> editingTag = new ArrayList<>();
	
	protected final int[] defaultColumnSizes = {80, 180, 180};
	protected int[] actualColumnSizes;
	
	public TagEditor(TableViewer tableViewer, int[] columnSizes, Button btnNewTag, Button btnDeleteTag, List<TagRequest> editingTag) {
		this.tableViewer = tableViewer;
		this.btnNewTag = btnNewTag;
		this.btnDeleteTag = btnDeleteTag;
		this.editingTag = editingTag;
		
		this.actualColumnSizes = getColumnSizes(columnSizes);
		
		initTableViewer();
		initBtnNewTag();
		initBtnDeleteTag();
	}
	
	protected void initTableViewer() {
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				if (selection.isEmpty()) {
					btnDeleteTag.setEnabled(false);
					return;
				}
				TagRequest selected = (TagRequest)selection.getFirstElement();
				btnDeleteTag.setEnabled(selected.getTagType() != TagTypeEnum.AUTO);
			}
		});
		
		TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNewColumn = tableViewerColumn.getColumn();
		tblclmnNewColumn.setWidth(actualColumnSizes[0]);
		tblclmnNewColumn.setText(strType);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override public String getText(Object element) {return tagTypes.get(((TagRequest)element).getTagType());}
		});
		tableViewerColumn.setEditingSupport(new EditingSupport(tableViewer) {
			String[] items = new String[]{tagTypes.get(TagTypeEnum.CLOUD), tagTypes.get(TagTypeEnum.LOCAL)};
			@Override protected boolean canEdit(Object element) {return ((TagRequest)element).getTagType() != TagTypeEnum.AUTO;}
			@Override protected CellEditor getCellEditor(Object element) {
				return new ComboBoxCellEditor((Table)getViewer().getControl(), items, SWT.READ_ONLY);
			}
			@Override protected Object getValue(Object element) {
				TagRequest t = (TagRequest)element;
				for (int i = 0; i < items.length; ++i) {
					if (tagTypes.get(t.getTagType()).equals(items[i]))
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
				for (Map.Entry<TagTypeEnum, String> entry: tagTypes.entrySet()) {
					if (entry.getValue().equals(selected)) {
						((TagRequest)element).setTagType(entry.getKey());
						getViewer().refresh();
						break;
					}
				}
			}
		});
		
		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNewColumn_1 = tableViewerColumn_1.getColumn();
		tblclmnNewColumn_1.setWidth(actualColumnSizes[1]);
		tblclmnNewColumn_1.setText(strName);
		tableViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {
			@Override public String getText(Object element) {return ((TagRequest)element).getKey();}
			@Override public Color getBackground(Object element) {
				TagRequest tag = (TagRequest)element;
				return tag.getKey() == null || tag.getKey().isEmpty() ? RequiredFieldColorConstant.COLOR_REQUIRED: null;
			}
		});
		tableViewerColumn_1.setEditingSupport(new EditingSupport(tableViewer) {
			@Override protected boolean canEdit(Object element) {return ((TagRequest)element).getTagType() != TagTypeEnum.AUTO;}
			@Override protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor((Table)getViewer().getControl());
			}
			@Override protected Object getValue(Object element) {return ((TagRequest)element).getKey();}
			@Override protected void setValue(Object element, Object value) {
				if (value == null || value.toString().isEmpty()) {
					MessageDialog.openError(null, Messages.getString("failed"), msgTagKeyFromOneChar);
					return;
				}
				
				for (TagRequest t: editingTag) {
					if (t == element)
						continue;
					
					if (t.getKey().equals(value)) {
						MessageDialog.openError(null, Messages.getString("failed"), msgTagKeyDuplicate);
						return;
					}
				}
				((TagRequest)element).setKey(value.toString());
				getViewer().refresh();
			}
		});
		
		TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNewColumn_2 = tableViewerColumn_2.getColumn();
		tblclmnNewColumn_2.setWidth(actualColumnSizes[2]);
		tblclmnNewColumn_2.setText(strValue);
		tableViewerColumn_2.setLabelProvider(new ColumnLabelProvider() {
			@Override public String getText(Object element) {return ((TagRequest)element).getValue();}
		});
		tableViewerColumn_2.setEditingSupport(new EditingSupport(tableViewer) {
			@Override protected boolean canEdit(Object element) {return ((TagRequest)element).getTagType() != TagTypeEnum.AUTO;}
			@Override protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor((Table)getViewer().getControl());
			}
			@Override protected Object getValue(Object element) {
				TagRequest tag = (TagRequest)element;
				return tag.getValue() == null ? "": tag.getValue();
			}
			@Override protected void setValue(Object element, Object value) {((TagRequest)element).setValue(value.toString());getViewer().refresh();}
		});
		
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(editingTag);
	}
	
	protected void initBtnNewTag() {
		btnNewTag.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TagRequest nt = new TagRequest();
				nt.setTagType(TagTypeEnum.LOCAL);
				
				int i = 0;
				while (true) {
					boolean matched = false;
					String key = String.format("key%d", i);
					for (TagRequest t: editingTag) {
						if (t.getKey().equals(key)) {
							matched = true;
							break;
						}
					}
					if (matched) {
						++i;
					} else {
						nt.setKey(key);
						break;
					}
				}
				
				editingTag.add(nt);
				tableViewer.refresh();
				tableViewer.setSelection(new StructuredSelection(nt));
			}
		});
	}
	
	protected void initBtnDeleteTag() {
		btnDeleteTag.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
				if (selection.isEmpty()) {
					btnDeleteTag.setEnabled(false);
					return;
				}
				TagRequest selected = (TagRequest)selection.getFirstElement();
				editingTag.remove(selected);
				tableViewer.refresh();
			}
		});
	}
	
	protected int[] getColumnSizes(int[] columnSizes) {
		int[] actualColumnSizes = new int[3];
		for (int i = 0; i < defaultColumnSizes.length; ++i) {
			if (columnSizes != null && columnSizes.length > i) {
				actualColumnSizes[i] = columnSizes[i];
			} else {
				actualColumnSizes[i] = defaultColumnSizes[i];
			}
		}
		return actualColumnSizes;
	}
	
	public List<TagRequest> getTags() {
		return editingTag;
	}
}
