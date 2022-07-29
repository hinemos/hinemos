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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.openapitools.client.model.JobRpaRunParamInfoResponse;
import org.openapitools.client.model.RpaManagementToolRunParamResponse;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * RPAシナリオジョブ（間接実行）起動パラメータ テーブルビューワークラス
 * このテーブルではソートは必要ないので、CommonTableViewerを使用せず新たに作成しています。
 */
public class RpaIndirectParameterTableViewer extends TableViewer {

	/** ロガー */
	private static final Log log = LogFactory.getLog(RpaIndirectParameterTableViewer.class);
	/** 起動パラメータ入力ルール */
	private ParamValueEditingSupport m_paramValueEditingSupport = new ParamValueEditingSupport(this);
	/** 読み取り専用モードのフラグ */
	private boolean m_enabled = false;

	/**
	 * パラメータ値を入力可能項目にするためのクラス
	 */
	private static class ParamValueEditingSupport extends EditingSupport {
		private final TableViewer viewer;
		private final CellEditor editor;
		/** 編集モードの時だけ入力可能にするためのフラグ */
		private boolean canEdit = false;

		public ParamValueEditingSupport(TableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
			this.editor = new TextCellEditor(viewer.getTable());
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected boolean canEdit(Object element) {
			RpaManagementToolRunParamResponse p = (RpaManagementToolRunParamResponse) element;
			// 値が固定の項目は編集可能にしない
			return canEdit && p.getEditable();
		}

		@Override
		protected Object getValue(Object element) {
			RpaManagementToolRunParamResponse p = (RpaManagementToolRunParamResponse) element;
			return p.getParamValue();
		}

		@Override
		protected void setValue(Object element, Object value) {
			RpaManagementToolRunParamResponse p = (RpaManagementToolRunParamResponse) element;
			p.setParamValue((String) value);
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
	public RpaIndirectParameterTableViewer(Table table) {
		super(table);
		this.setContentProvider(ArrayContentProvider.getInstance());
		this.createTableColumn(GetRpaIndirectParameterTableDefine.get());
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
			case (GetRpaIndirectParameterTableDefine.PARAM_NAME):
				viewerColumn.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						RpaManagementToolRunParamResponse p = (RpaManagementToolRunParamResponse) element;
						return p.getParamName();
					}
				});
				break;
			case (GetRpaIndirectParameterTableDefine.PARAM_VALUE):
				viewerColumn.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						RpaManagementToolRunParamResponse p = (RpaManagementToolRunParamResponse) element;
						return p.getParamValue();
					}

					@Override
					public Color getBackground(Object element) {
						RpaManagementToolRunParamResponse p = (RpaManagementToolRunParamResponse) element;
						if (!p.getEditable()) {
							// 編集不可の項目はグレーアウト
							return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
						}
						if (m_enabled && p.getRequired() && p.getParamValue().isEmpty()) {
							// 必須項目を表示
							return RequiredFieldColorConstant.COLOR_REQUIRED;
						}
						return super.getBackground(element);
					}

				});
				// 値を入力可能にする
				viewerColumn.setEditingSupport(m_paramValueEditingSupport);
				break;
			case (GetRpaIndirectParameterTableDefine.DESCRIPTION):
				viewerColumn.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						RpaManagementToolRunParamResponse p = (RpaManagementToolRunParamResponse) element;
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
	 * 
	 * @throws InvalidSetting
	 */
	public List<JobRpaRunParamInfoResponse> createRunParamInfos() {
		List<JobRpaRunParamInfoResponse> runParams = new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<RpaManagementToolRunParamResponse> rows = (List<RpaManagementToolRunParamResponse>) this.getInput();
		if (rows == null) {
			return Collections.emptyList();
		}
		for (RpaManagementToolRunParamResponse row : rows) {
			// 入力されているものだけ登録する
			if (!row.getParamValue().isEmpty()) {
				JobRpaRunParamInfoResponse runParam = new JobRpaRunParamInfoResponse();
				runParam.setParamId(row.getParamId());
				runParam.setParamValue(row.getParamValue());
				runParams.add(runParam);
			}
		}
		return runParams;
	}

	public ValidateResult validateRunParamInfos() {
		ValidateResult result = null;
		@SuppressWarnings("unchecked")
		List<RpaManagementToolRunParamResponse> rows = (List<RpaManagementToolRunParamResponse>) this.getInput();
		for (RpaManagementToolRunParamResponse row : rows) {
			if (row.getParamValue().isEmpty()) {
				// 必須項目が未入力
				if (row.getRequired()) {
					return JobDialogUtil.getValidateResult(Messages.getString("message.hinemos.1"),
							Messages.getString("message.job.rpa.28"));
				}
			}
		}
		return result;

	}

	/**
	 * ジョブ設定DTOの設定値をテーブルに反映します。
	 * 
	 * @param runParams
	 *            ジョブ設定DTO
	 */
	public void setRunParamInfos(List<JobRpaRunParamInfoResponse> runParams) {
		// 設定値をテーブルに反映
		if (runParams == null) {
			return;
		}
		@SuppressWarnings("unchecked")
		List<RpaManagementToolRunParamResponse> rows = (List<RpaManagementToolRunParamResponse>) this.getInput();
		for (JobRpaRunParamInfoResponse runParam : runParams) {
			for (RpaManagementToolRunParamResponse row : rows) {
				if (runParam.getParamId().equals(row.getParamId())) {
					row.setParamValue(runParam.getParamValue());
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
		m_paramValueEditingSupport.setEditable(enabled);
		m_enabled = enabled;
		refresh(); // 読み込み専用時は必須項目を明示しない
	}
}
