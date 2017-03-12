package com.clustercontrol.calendar.viewer;

import java.util.ArrayList;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.editor.TextAreaDialogCellEditor;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * 月間カレンダテーブルビューワークラス
 * CommonTableViewerクラスを使用していたが、
 * このテーブルでは、ソートは必要ないので、新たに作成。
 *
 */
public class CalendarMonthTableViewer extends TableViewer{

	private ArrayList<TableColumnInfo> m_tableColumnList = null;

	/**
	 * コンストラクタ
	 *
	 * @param table
	 */
	public CalendarMonthTableViewer(Table table) {
		super(table);

	}

	/**
	 * テーブルカラムの作成処理
	 *
	 * @param tableColumnList
	 */
	public void createTableColumn(ArrayList<TableColumnInfo> tableColumnList) {

		this.m_tableColumnList = tableColumnList;

		//カラム・プロパティの設定
		String[] properties = new String[this.m_tableColumnList.size()];
		// 各カラムに設定するセル・エディタの配列
		CellEditor[] editors = new CellEditor[this.m_tableColumnList.size()];

		for (int i = 0; i < this.m_tableColumnList.size(); i++) {
			TableColumnInfo tableColumnInfo = this.m_tableColumnList.get(i);
			TableColumn calMonthColumn = new TableColumn(getTable(), tableColumnInfo.getStyle(), i);
			WidgetTestUtil.setTestId(this, null, calMonthColumn);
			//calMonthColumn.setData(ClusterControlPlugin.CUSTOM_WIDGET_ID, "calendarMonthTableViewerColumn");
			calMonthColumn.setText(tableColumnInfo.getName());
			calMonthColumn.setWidth(tableColumnInfo.getWidth());

			//カラム・プロパティの設定
			properties[i] = String.valueOf(i);
			// 各カラムに設定するセル・エディタの配列
			if(tableColumnInfo.getType() == TableColumnInfo.TEXT_DIALOG){
				TextAreaDialogCellEditor dialog = new TextAreaDialogCellEditor(getTable());
				dialog.setTitle(tableColumnInfo.getName());
				dialog.setModify(false);
				editors[i] = dialog;
			}
			else{
				editors[i] = null;
			}
		}
		//カラム・プロパティの設定
		setColumnProperties(properties);
		//セル・エディタの設定
		setCellEditors(editors);
	}


}
