/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.action;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.bean.Property;
import com.clustercontrol.repository.util.NodePropertyUtil;
import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.repository.FacilityNotFound_Exception;
import com.clustercontrol.ws.repository.InvalidRole_Exception;
import com.clustercontrol.ws.repository.NodeInfo;

/**
 * ノード属性情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetNodeProperty {

	// ログ
	private static Log m_log = LogFactory.getLog( GetNodeProperty.class );

	// マネージャ名
	private String managerName = null;

	// ノード情報
	private NodeInfo nodeInfo = null;

	// FacilityId
	private String facilityId = null;

	// mode
	private int mode;

	// 対象日時
	private Long targetDatetime = 0L;

	public GetNodeProperty(String managerName, String facilityId, int mode) {
		this.managerName = managerName;
		this.facilityId = facilityId;
		this.mode = mode;
		try {
			if (managerName == null || managerName.length() == 0
					|| this.facilityId == null || this.facilityId.length() == 0) {
				this.nodeInfo = new NodeInfo();
				NodePropertyUtil.setDefaultNode(this.nodeInfo);
			} else {
				RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
				this.nodeInfo = wrapper.getNodeFull(this.facilityId);
			}
		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (FacilityNotFound_Exception e) {
			// 指定のファシリティがマネージャに存在しない。
			// 何も表示しない。
			e.printStackTrace();
			m_log.warn("getProperty(), " + e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getProperty(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
	}

	public GetNodeProperty(String managerName, String facilityId, int mode, Long targetDatetime, NodeInfo nodeFilterInfo) {
		this.managerName = managerName;
		this.facilityId = facilityId;
		this.mode = mode;
		this.targetDatetime = targetDatetime;
		try {
			if (managerName == null || managerName.length() == 0
					|| this.facilityId == null || this.facilityId.length() == 0) {
				this.nodeInfo = new NodeInfo();
				NodePropertyUtil.setDefaultNode(this.nodeInfo);
			} else {
				RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
				this.nodeInfo = wrapper.getNodeFullByTargetDatetime(this.facilityId, this.targetDatetime, nodeFilterInfo);
			}
		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (FacilityNotFound_Exception e) {
			// 指定のファシリティがマネージャに存在しない。
			// 何も表示しない。
			e.printStackTrace();
			m_log.warn("getProperty(), " + e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getProperty(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
	}

	/**
	 * ノード属性情報を取得します。
	 * 
	 * @param isNodeMap true:ノードマップ（構成情報検索）表示
	 * @return ノード属性情報
	 */
	public Property getProperty(boolean isNodeMap) {
		return NodePropertyUtil.node2property(this.managerName, this.nodeInfo, this.mode, Locale.getDefault(), isNodeMap);
	}

	/**
	 * ノード情報を取得します。
	 *
	 * @return ノード情報
	 */
	public NodeInfo getNodeInfo() {
		return this.nodeInfo;
	}
}
