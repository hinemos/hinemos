/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.viewer;

import java.util.ArrayList;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.openapitools.client.model.MaintenanceScheduleResponse;

/**
 * CommonTableViewerクラス用のViewerSorterクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class CommonTableViewerSorter extends ViewerSorter {
	/** カラムインデックス */
	private int m_columnIndex = 0;

	/** カラムインデックス */
	private int m_columnIndex2 = 0;

	/** ソートオーダー */
	private int m_order = 1;

	/**
	 * コンストラクタ
	 * 
	 * @param column
	 *            ソート対象カラムインデックス
	 * @since 1.0.0
	 */
	public CommonTableViewerSorter(int columnIndex) {
		this(columnIndex,-1,1);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param column
	 *            ソート対象カラムインデックス
	 * @param columnIndex
	 */
	public CommonTableViewerSorter(int columnIndex,int columIndex2) {
		this(columnIndex, columIndex2,1);
	}
	/**
	 * コンストラクタ
	 * 
	 * @param column
	 *            ソート対象カラムインデックス
	 * @param order
	 *            ソートオーダー
	 * @since 1.0.0
	 */
	public CommonTableViewerSorter(int columnIndex, int columnIndex2, int order) {
		super();

		this.m_columnIndex = columnIndex;
		//セカンドソーターを必要としない場合には-1が入ってる。
		this.m_columnIndex2 = columnIndex2;

		//昇順・降順の設定　1: 昇順　-1:降順
		this.m_order = order;
	}

	/**
	 * 比較処理
	 * 
	 * @param viewer
	 * @param e1
	 * @param e2
	 * @return 比較結果。superクラスの結果をソートオーダーにより反転する
	 * @since 1.0.0
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof ArrayList && e2 instanceof ArrayList) {
			Object object1 = ((ArrayList<?>) e1).get(m_columnIndex);
			Object object2 = ((ArrayList<?>) e2).get(m_columnIndex);

			int rtn = 0;
			rtn = subCompare(viewer, object1, object2);

			//第1ソートが等価で第２ソートが有効な場合には
			//第2ソートを行う。
			if( rtn == 0 && m_columnIndex2 >= 0){

				object1 = ((ArrayList<?>) e1).get(m_columnIndex2);
				object2 = ((ArrayList<?>) e2).get(m_columnIndex2);

				rtn = subCompare(viewer, object1, object2);
			}
			if (m_order == 1) {
				return rtn;
			} else {
				return - rtn;
			}
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	private int subCompare(Viewer viewer, Object obj1, Object obj2) {
		int rtn = 0;
		if (obj1 == null && obj2 != null) {
			return -1;
		} else if (obj1 != null && obj2 == null) {
			return 1;
		} else if (obj1 != null && obj2 != null &&
				obj1 instanceof Comparable && obj2 instanceof Comparable &&
				obj1.getClass().equals(obj2.getClass())) {
			rtn = ((Comparable<Object>)obj1).compareTo(obj2);
		} else if (obj1 != null && obj2 != null &&
				obj1 instanceof MaintenanceScheduleResponse && obj2 instanceof MaintenanceScheduleResponse){
			MaintenanceScheduleResponse schedule1 = (MaintenanceScheduleResponse)obj1;
			MaintenanceScheduleResponse schedule2 = (MaintenanceScheduleResponse)obj2;

			// month
			if (schedule1.getMonth() != null && schedule2.getMonth() == null) {
				return 1;
			} else if (schedule1.getMonth() == null && schedule2.getMonth() != null){
				return -1;
			} else if (schedule1.getMonth() != null && schedule2.getMonth() != null) {
				if (!schedule1.getMonth().equals(schedule2.getMinute())) {
					return schedule1.getMonth() - schedule2.getMonth();
				}
			}
			// day
			if (schedule1.getDay() != null && schedule2.getDay() == null) {
				return 1;
			} else if (schedule1.getDay() == null && schedule2.getDay() != null){
				return -1;
			} else if (schedule1.getDay() != null && schedule2.getDay() != null) {
				if (!schedule1.getDay().equals(schedule2.getDay())) {
					return schedule1.getDay() - schedule2.getDay();
				}
			}
			// week
			if (schedule1.getWeek() != null && schedule2.getWeek() == null) {
				return 1;
			} else if (schedule1.getWeek() == null && schedule2.getWeek() != null){
				return -1;
			} else if (schedule1.getWeek() != null && schedule2.getWeek() != null) {
				if (!schedule1.getWeek().equals(schedule2.getWeek())) {
					return schedule1.getWeek() - schedule2.getWeek();
				}
			}
			// hour
			if (schedule1.getHour() != null && schedule2.getHour() == null) {
				return 1;
			} else if (schedule1.getHour() == null && schedule2.getHour() != null){
				return -1;
			} else if (schedule1.getHour() != null && schedule2.getHour() != null) {
				if (!schedule1.getHour().equals(schedule2.getHour())) {
					return schedule1.getHour() - schedule2.getHour();
				}
			}
			// minute
			if (schedule1.getMinute() != null && schedule2.getMinute() == null) {
				return 1;
			} else if (schedule1.getMinute() == null && schedule2.getMinute() != null){
				return -1;
			} else if (schedule1.getMinute() != null && schedule2.getMinute() != null) {
				if (!schedule1.getMinute().equals(schedule2.getMinute())) {
					return schedule1.getMinute() - schedule2.getMinute();
				}
			}
			return 0;
		}else {
			// 文字列で比較。
			rtn = super.compare(viewer, obj1, obj2);
		}
		return rtn;
	}

	/**
	 * ソート対象カラムインデックス取得
	 * 
	 * @return カラムインデックス
	 * @since 1.0.0
	 */
	public int getColumnIndex() {
		return this.m_columnIndex;
	}
}
