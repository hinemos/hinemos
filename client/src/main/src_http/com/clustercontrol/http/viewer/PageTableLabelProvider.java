/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.http.viewer;

import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;
import com.clustercontrol.ws.monitor.Page;;

/**
 * 文字列監視の判定情報一覧のラベルプロバイダークラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class PageTableLabelProvider extends CommonTableLabelProvider<Page> {

	public PageTableLabelProvider(ITableItemCompositeDefine<Page> define) {
		super(define);
	}

	/**
	 * カラム文字列を返します。
	 * 
	 * @since 5.0.0
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {

		if (element instanceof Page) {
			Page pageInfo = (Page) element;

			if (columnIndex == GetPageTableDefine.ORDER_NO) {
				return String.valueOf(indexOf(pageInfo) + 1);
			} else if (columnIndex == GetPageTableDefine.URL) {
				if(pageInfo.getUrl() != null){
					return pageInfo.getUrl();
				}
			} else if (columnIndex == GetPageTableDefine.DESCRIPTION) {
				if (pageInfo.getDescription() != null) {
					return pageInfo.getDescription();
				}
			}
		}
		return "";
	}
}
