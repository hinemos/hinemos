/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobRpaCheckEndValueInfoResponse;
import org.openapitools.client.model.RpaManagementToolEndStatusResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.HinemosMessage;

/**
 * RPAシナリオジョブ（間接実行）終了値判定条件 テーブルビューワークラス
 * このテーブルではソートは必要ないので、CommonTableViewerを使用せず新たに作成しています。
 */
public class RpaIndirectEndValueTableViewer extends TableViewer {

	/** ロガー */
	private static final Log log = LogFactory.getLog(RpaIndirectEndValueTableViewer.class);
	/** 終了値入力ルール */
	private EndValueEditingSupport m_endValueEditingSupport = new EndValueEditingSupport(this);

	/**
	 * 終了値を入力可能項目にするためのクラス
	 */
	private static class EndValueEditingSupport extends EditingSupport {
		private final TableViewer viewer;
		private final CellEditor editor;
		private final Text text;
		/** 編集モードの時だけ入力可能にするためのフラグ */
		private boolean canEdit = false;

		public EndValueEditingSupport(TableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
			this.editor = new TextCellEditor(viewer.getTable());
			this.text = ((Text) this.editor.getControl());
			this.text.addVerifyListener(
					new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected boolean canEdit(Object element) {
			return canEdit;
		}

		@Override
		protected Object getValue(Object element) {
			RpaManagementToolEndStatusResponse p = (RpaManagementToolEndStatusResponse) element;
			return String.valueOf(p.getEndValue());
		}

		@Override
		protected void setValue(Object element, Object value) {
			RpaManagementToolEndStatusResponse p = (RpaManagementToolEndStatusResponse) element;
			if (JobDialogUtil.validateNumberText(this.text)) {
				p.setEndValue(Integer.valueOf((String) value));
			}
			viewer.update(element, null);
		}

		/**
		 * 入力可/不可を切り替えます。
		 */
		protected void setEditable(boolean enabled) {
			this.canEdit = enabled;
		}
	}

	/**
	 * コンストラクタ
	 *
	 * @param table
	 */
	public RpaIndirectEndValueTableViewer(Table table) {
		super(table);
		this.setContentProvider(ArrayContentProvider.getInstance());
		this.createTableColumn(GetRpaIndirectEndValueTableDefine.get());
	}

	/**
	 * テーブルカラムの作成処理
	 *
	 * @param tableColumnList
	 */
	public void createTableColumn(List<TableColumnInfo> tableColumnList) {

		for (int i = 0; i < tableColumnList.size(); i++) {
			TableColumnInfo tableColumn = tableColumnList.get(i);
			TableViewerColumn viewerColumn = new TableViewerColumn(this, tableColumn.getStyle());
			viewerColumn.getColumn().setWidth(tableColumn.getWidth());
			viewerColumn.getColumn().setText(tableColumn.getName());
			switch (i) {
			case (GetRpaIndirectEndValueTableDefine.END_STATUS):
				viewerColumn.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						RpaManagementToolEndStatusResponse p = (RpaManagementToolEndStatusResponse) element;
						return p.getEndStatus();
					}
				});
				break;
			case (GetRpaIndirectEndValueTableDefine.END_VALUE):
				viewerColumn.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						RpaManagementToolEndStatusResponse p = (RpaManagementToolEndStatusResponse) element;
						return String.valueOf(p.getEndValue());
					}

					@Override
					public Color getBackground(Object element) {
						RpaManagementToolEndStatusResponse p = (RpaManagementToolEndStatusResponse) element;
						if (p.getEndValue() == null) {
							// 必須項目を表示
							return RequiredFieldColorConstant.COLOR_REQUIRED;
						}
						return super.getBackground(element);
					}
				});
				// 値を入力可能にする
				viewerColumn.setEditingSupport(m_endValueEditingSupport);
				break;
			case (GetRpaIndirectEndValueTableDefine.DESCRIPTION):
				viewerColumn.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						RpaManagementToolEndStatusResponse p = (RpaManagementToolEndStatusResponse) element;
						return HinemosMessage.replace(p.getDescription());
					}
				});
				break;
			default:
				log.warn("createTableColumn() : unknown column, index= " + i);
			}
		}
	}

	/**
	 * テーブルに入力された値からジョブ設定DTOを生成し返します。
	 */
	public List<JobRpaCheckEndValueInfoResponse> getEndValueInfos() {
		List<JobRpaCheckEndValueInfoResponse> endValueInfos = new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<RpaManagementToolEndStatusResponse> rows = (List<RpaManagementToolEndStatusResponse>) this.getInput();
		if (rows == null) {
			return Collections.emptyList();
		}
		for (RpaManagementToolEndStatusResponse row : rows) {
			JobRpaCheckEndValueInfoResponse endValueInfo = new JobRpaCheckEndValueInfoResponse();
			endValueInfo.setEndStatusId(row.getEndStatusId());
			endValueInfo.setEndValue(row.getEndValue());
			endValueInfos.add(endValueInfo);
		}
		return endValueInfos;
	}

	/**
	 * ジョブ設定DTOの設定値をテーブルに反映します。
	 * 
	 * @param runParams
	 *            ジョブ設定DTO
	 */
	public void setEndValueInfos(List<JobRpaCheckEndValueInfoResponse> runParams) {
		// 設定値をテーブルに反映
		if (runParams == null) {
			return;
		}
		@SuppressWarnings("unchecked")
		List<RpaManagementToolEndStatusResponse> rows = (List<RpaManagementToolEndStatusResponse>) this.getInput();
		for (JobRpaCheckEndValueInfoResponse runParam : runParams) {
			for (RpaManagementToolEndStatusResponse row : rows) {
				if (runParam.getEndStatusId().equals(row.getEndStatusId())) {
					row.setEndValue(runParam.getEndValue());
					this.update(row, null);
					break;
				}
			}
		}
	}

	/**
	 * パラメータ値の入力可/不可を切り替えます。
	 * 
	 * @param enabled
	 *            入力可/不可フラグ
	 */
	public void setEditable(boolean enabled) {
		m_endValueEditingSupport.setEditable(enabled);
	}
}
