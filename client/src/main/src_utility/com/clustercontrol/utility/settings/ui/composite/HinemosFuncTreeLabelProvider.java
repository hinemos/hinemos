/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.composite;

import org.eclipse.jface.viewers.LabelProvider;

import com.clustercontrol.utility.settings.ui.bean.FuncInfo;
import com.clustercontrol.utility.settings.ui.bean.FuncTreeItem;

/**
 * ジョブツリー用コンポジットのツリービューア用のLabelProviderクラスです。
 * 
 * @version 6.1.0
 * @since 1.2.0
 */
class HinemosFuncTreeLabelProvider extends LabelProvider {
	
	/**
	 * ジョブツリーアイテムから表示名を作成し返します。
	 * 
	 * @param ジョブツリーアイテム
	 * @return 表示名
	 * 
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		FuncInfo info = ((FuncTreeItem) element).getData();
			return info.getName();
	}

   
}