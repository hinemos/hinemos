/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * TableViewerのSorterクラス
 */
public class TableViewerSorter extends ViewerSorter {
	private static final Log logger = LogFactory.getLog(TableViewerSorter.class);

	/** Viewerへソート基準を保存するときの名前 */
	protected static final String KEY_SO = TableViewerSorter.class.getName() + "#sortingOrder";

	/** ソート基準情報 */
	protected static class SortingOrder {
		/** ソート基準カラム */
		ColumnLabelProvider column;

		/** ソートオーダー( 1:順方向, -1:逆方向 ) */
		int order;

		@Override
		public String toString() {
			return str(column) + "/" + order;
		}
	};

	/** 今回のソート基準 */
	protected SortingOrder curr;

	/** 前回のソート基準 */
	protected SortingOrder prev;

	/** コンストラクタ */
	public TableViewerSorter(ColumnViewer tableViewer, ColumnLabelProvider column){
		super();

		// 前回のソート基準を取得
		synchronized (tableViewer) {
			prev = (SortingOrder) tableViewer.getData(KEY_SO);
			if (prev == null) {
				// 今回が初
				curr = new SortingOrder();
				curr.column = column;
				curr.order = 1;
				tableViewer.setData(KEY_SO, curr);
				logger.debug("ctor: New, viewer=" + str(tableViewer) + ", curr=" + curr);
				return;
			}

			// 今回分を設定。前回と今回が同じ列の場合は、並び順を反転させる。
			curr = new SortingOrder();
			curr.column = column;
			if (curr.column.equals(prev.column)) {
				curr.order = prev.order * -1;
				logger.debug("ctor: Reversed, viewer=" + str(tableViewer) + ", prev=" + prev + ", curr=" + curr);
			} else {
				curr.order = 1;
				logger.debug("ctor: Changed, viewer=" + str(tableViewer) + ", prev=" + prev + ", curr=" + curr);
			}
			tableViewer.setData(KEY_SO, curr);
		}
	}

	/**
	 * テーブル内行データ(e1, e2)の比較処理
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int result = super.compare(viewer, curr.column.getText(e1), curr.column.getText(e2));

		// 現在列で同順なら、前回選択した別の列を基準に並べ替える
		if (result == 0 && prev != null && !prev.column.equals(curr.column)) {
			result = super.compare(viewer, prev.column.getText(e1), prev.column.getText(e2));
		}

		return result * curr.order;
	}

	/**
	 * オブジェクトの文字列表現をログ出力用に加工して返します。
	 * <p>
	 * 具体的にはピリオド'.'より後ろにある文字列を返します。
	 * これは、Object#toStringをオーバーライドせず使用している場合の「単純クラス名＋ハッシュコード」の部分が該当します。
	 * (Class#getSimpleNameで代用できそうですが、匿名クラスで空文字列になるので使用していません。)
	 */
	protected static String str(Object toStringObject) {
		String s = toStringObject.toString();
		int i = s.lastIndexOf('.');
		if (i < 0 || i == s.length() - 1) {
			return s;
		}
		return s.substring(i + 1);
	}
}
