/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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
import com.clustercontrol.nodemap.view.NodeMapView;
import com.clustercontrol.repository.bean.FacilityConstant;

/**
 * ノードマップビューを表示するクライアント側アクションクラス<BR>
 * @since 1.0.0
 */
public class NodeMapViewAction extends AbstractHandler {

	// ログ
	private static Log m_log = LogFactory.getLog( NodeMapViewAction.class );

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		m_log.debug("NodeMapViewAction new view");

		// 新規ビューを表示する
		RelationViewController.createNewView("", FacilityConstant.STRING_COMPOSITE, NodeMapView.class);
		
		return null;
	}
}