/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.viewer;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/**
 * カレンダ詳細情報一覧のテーブルビューアークラス<BR>
 * 
 * @version 2.1.0
 * @since 2.1.0
 */
public class CalendarDetailListTableViewer extends TableViewer {

	/**
	 * インスタンスを返します。
	 * 
	 * @param parent 親のコンポジット
	 * @since 2.1.0
	 * 
	 * @see org.eclipse.jface.viewers.TableViewer#TableViewer(org.eclipse.swt.widgets.Composite)
	 * @see com.clustercontrol.monitor.run.viewer.StringValueListTableLabelProvider
	 */
	public CalendarDetailListTableViewer(Composite parent) {
		super(parent);
		setLabelProvider(new CalendarDetailListTableLabelProvider());
		setContentProvider(new ArrayContentProvider());
	}

	/**
	 * インスタンスを返します。
	 * 
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @since 2.1.0
	 * 
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.jface.viewers.TableViewer#TableViewer(org.eclipse.swt.widgets.Composite)
	 * @see com.clustercontrol.monitor.run.viewer.StringValueListTableLabelProvider
	 */
	public CalendarDetailListTableViewer(Composite parent, int style) {
		super(parent, style);
		setLabelProvider(new CalendarDetailListTableLabelProvider());
		setContentProvider(new ArrayContentProvider());
	}

	/**
	 * インスタンスを返します。
	 * 
	 * @param table テーブル
	 * @since 2.1.0
	 * 
	 * @see org.eclipse.jface.viewers.TableViewer#TableViewer(org.eclipse.swt.widgets.Composite)
	 * @see com.clustercontrol.monitor.run.viewer.StringValueListTableLabelProvider
	 */
	public CalendarDetailListTableViewer(Table table) {
		super(table);
		setLabelProvider(new CalendarDetailListTableLabelProvider());
		setContentProvider(new ArrayContentProvider());
	}
}
