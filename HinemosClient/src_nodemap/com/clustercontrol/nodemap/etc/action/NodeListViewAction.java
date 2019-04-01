/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.etc.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import com.clustercontrol.nodemap.util.RelationViewController;
import com.clustercontrol.nodemap.view.NodeListView;
import com.clustercontrol.repository.bean.FacilityConstant;

/**
 * ノード一覧ビューを表示するクライアント側アクションクラス<BR>
 * @since 6.2.0
 */
public class NodeListViewAction extends AbstractHandler {

	// ログ
	private static Log m_log = LogFactory.getLog( NodeListViewAction.class );

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		m_log.debug("NodeListViewAction new view");

		// 新規ビューを表示する
		RelationViewController.createNewView("", FacilityConstant.STRING_COMPOSITE, NodeListView.class);
		
		return null;
	}
}