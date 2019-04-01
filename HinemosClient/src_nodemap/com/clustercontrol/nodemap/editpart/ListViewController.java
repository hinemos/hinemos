/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.editpart;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.nodemap.bean.ReservedFacilityIdConstant;
import com.clustercontrol.nodemap.util.NodeMapEndpointWrapper;
import com.clustercontrol.nodemap.view.NodeListView;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.nodemap.InvalidSetting_Exception;
import com.clustercontrol.ws.repository.NodeInfo;
import com.sun.xml.internal.ws.client.ClientTransportException;

/**
 * ノードマップビューをコントロールするクラス。
 * 表示はNodeListViewクラス。
 * 
 * @since 6.2.0
 */
public class ListViewController {

	// ログ
	private static Log m_log = LogFactory.getLog( ListViewController.class );

	private final NodeListView _view;
	private final String _secondaryId;
	private final String _currentScope;

	// 描画対象のマップの情報を保持したモデル(マネージャ名、NodeInfoリスト)
	private ConcurrentHashMap<String, List<NodeInfo>> _nodeListMap = new ConcurrentHashMap<>();

	public ListViewController(NodeListView view, String secondaryId, String currentScope){
		this._secondaryId = secondaryId;
		this._currentScope = currentScope;
		this._view = view;
	}

	/**
	 * マネージャから情報を取得しマップを更新する
	 * NodeMapView以外からは呼ぶべきではない
	 * 
	 * @param nodeFilterInfo ノードフィルタ情報
	 * @throws Exception
	 */
	public void update(NodeInfo nodeFilterInfo) throws Exception {

		// マネージャからノード情報を取得
		try {
			_nodeListMap = new ConcurrentHashMap<>();
			if (_view.getListComposite().getManagerName() == null
					|| _view.getListComposite().getManagerName().isEmpty()) {
				StringBuilder sbErrMsg = new StringBuilder();
				for(String managerName : EndpointManager.getActiveManagerNameList()) {
					try {
						NodeMapEndpointWrapper wrapper = NodeMapEndpointWrapper.getWrapper(managerName);
						List<NodeInfo> tmpList = wrapper.getNodeList(ReservedFacilityIdConstant.ROOT_SCOPE, nodeFilterInfo);
						if (tmpList == null) {
							continue;
						}
						if (!_nodeListMap.containsKey(managerName)) {
							_nodeListMap.put(managerName, new ArrayList<>());
						}
						_nodeListMap.get(managerName).addAll(tmpList);
					} catch (Exception e) {
						if (sbErrMsg.length() != 0) {
							sbErrMsg.append("\n");
						}
						if (e instanceof ClientTransportException) {
							m_log.debug("reload(), " + e.getMessage());
							sbErrMsg.append(Messages.getString("message.hinemos.failure.transfer") 
									+ ", " + e.getMessage());
						} else if (e instanceof InvalidSetting_Exception) {
							m_log.warn("reload(), " + e.getMessage(), e);
							sbErrMsg.append(HinemosMessage.replace(e.getMessage()));
						} else {
							m_log.warn("reload(), " + e.getMessage(), e);
							sbErrMsg.append(Messages.getString("message.hinemos.failure.unexpected") 
									+ ", " + HinemosMessage.replace(e.getMessage()));
						}
					}
				}
				// エラーが発生した場合は、まとめてメッセージを表示する。
				if (sbErrMsg.length() != 0) {
					MessageDialog.openInformation(null, Messages.getString("message"), sbErrMsg.toString());
				}
			} else {
				String managerName = _view.getListComposite().getManagerName();
				String facilityId = _currentScope;
				NodeMapEndpointWrapper nodeMapWrapper = NodeMapEndpointWrapper.getWrapper(managerName);
				List<NodeInfo> tmpList = nodeMapWrapper.getNodeList(facilityId, nodeFilterInfo);
				if (tmpList == null) {
					throw new Exception("facility list is null");
				}
				if (!_nodeListMap.containsKey(managerName)) {
					_nodeListMap.put(managerName, new ArrayList<>());
				}
				_nodeListMap.get(managerName).addAll(tmpList);
			}
		} catch (Exception e) {
			m_log.warn("update() : Failed to get node list. : " + e.getMessage());
			throw e;
		}

		// ビューの描画
		if (nodeFilterInfo != null) {
			_view.m_listComposite.setTableList(_nodeListMap, nodeFilterInfo.getNodeConfigTargetDatetime());
		} else {
			_view.m_listComposite.setTableList(_nodeListMap, null);
		}
	}

	/**
	 * 描画対象スコープのcurrentScopeを返す
	 * @return 描画対象スコープのcurrentScopeを返す
	 */
	public String getCurrentScope(){
		return _currentScope;
	}

	/**
	 * 描画対象スコープのsecondaryIdを返す
	 * @return 描画対象スコープのsecondaryIdを返す
	 */
	public String getSecondaryId(){
		return _secondaryId;
	}

	public String getManagerName() {
		return _view.getListComposite().getManagerName();
	}
}
