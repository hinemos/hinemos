/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.http.viewer;

import org.openapitools.client.model.PageResponse;

import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;

/**
 * 文字列監視の判定情報一覧のラベルプロバイダークラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class PageTableLabelProvider extends CommonTableLabelProvider<PageResponse> {

	public PageTableLabelProvider(ITableItemCompositeDefine<PageResponse> define) {
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

		if (element instanceof PageResponse) {
			PageResponse pageInfo = (PageResponse) element;

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
