/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.http.viewer;

import com.clustercontrol.bean.ValidMessage;
import com.clustercontrol.monitor.bean.HttpStatusMessage;
import com.clustercontrol.monitor.run.composite.ITableItemCompositeDefine;
import com.clustercontrol.monitor.run.viewer.CommonTableLabelProvider;
import com.clustercontrol.ws.monitor.Pattern;

/**
 * 文字列監視の判定情報一覧のラベルプロバイダークラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class PatternTableLabelProvider extends CommonTableLabelProvider<Pattern> {

	public PatternTableLabelProvider(ITableItemCompositeDefine<Pattern> define) {
		super(define);
	}

	/**
	 * カラム文字列を返します。
	 * 
	 * @since 2.1.0
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {

		if (element instanceof Pattern) {
			Pattern Pattern = (Pattern) element;
			if (columnIndex == GetPageTableDefine.ORDER_NO) {
				return String.valueOf(indexOf(Pattern) + 1);
			} else if (columnIndex == GetPatternTableDefine.PROCESS_TYPE) {
				// 処理する(true)->異常(abnormal)／処理しない(false)->正常(normal)
				return HttpStatusMessage.typeToString(Pattern.isProcessType());
			} else if (columnIndex == GetPatternTableDefine.PATTERN_STRING) {
				if (Pattern.getPattern() != null) {
					return Pattern.getPattern();
				}
			} else if (columnIndex == GetPatternTableDefine.DESCRIPTION) {
				if (Pattern.getDescription() != null) {
					return Pattern.getDescription();
				}
			} else if (columnIndex == GetPatternTableDefine.VALID_FLG) {
				return ValidMessage.typeToString(Pattern.isValidFlg());
			}
		}
		return "";
	}
}
