/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.openapitools.client.model.GetNodeFullByTargetDatetimeRequest;
import org.openapitools.client.model.GetNodeListRequest;
import org.openapitools.client.model.NodeInfoResponse;

import com.clustercontrol.bean.Property;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.repository.util.NodePropertyUtil;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.TimezoneUtil;

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
	private NodeInfoResponse nodeInfo = null;

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
				this.nodeInfo = new NodeInfoResponse();
				NodePropertyUtil.setDefaultNode(this.nodeInfo);
			} else {
				RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(managerName);
				this.nodeInfo = wrapper.getNodeFull(this.facilityId);
			}
		} catch (InvalidRole e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (FacilityNotFound e) {
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

	public GetNodeProperty(String managerName, String facilityId, int mode, Long targetDatetime, GetNodeListRequest nodeFilterInfo) {
		this.managerName = managerName;
		this.facilityId = facilityId;
		this.mode = mode;
		this.targetDatetime = targetDatetime;
		try {
			if (managerName == null || managerName.length() == 0
					|| this.facilityId == null || this.facilityId.length() == 0) {
				this.nodeInfo = new NodeInfoResponse();
				NodePropertyUtil.setDefaultNode(this.nodeInfo);
			} else {
				RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(managerName);
				GetNodeFullByTargetDatetimeRequest requestDto = new GetNodeFullByTargetDatetimeRequest();
				SimpleDateFormat format = TimezoneUtil.getSimpleDateFormat();
				RestClientBeanUtil.convertBean(nodeFilterInfo, requestDto);
				requestDto.setTargetDatetime(format.format(new Date(this.targetDatetime)));
				this.nodeInfo = wrapper.getNodeFullByTargetDatetime(this.facilityId, requestDto);
			}
		} catch (InvalidRole e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (FacilityNotFound e) {
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
	public NodeInfoResponse getNodeInfo() {
		return this.nodeInfo;
	}
}
