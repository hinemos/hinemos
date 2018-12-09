/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlType;


/**
 * The persistent class for the cc_cfg_node_cpu database table.
 * 
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_cpu", schema="setting")
@Cacheable(true)
public class NodeCpuInfo extends NodeDeviceInfo {
	private static final long serialVersionUID = 1L;

	public NodeCpuInfo() {
		super();
	}

	public NodeCpuInfo(
			String facilityId,
			Integer deviceIndex,
			String deviceType,
			String deviceName) {
		super(facilityId,
				deviceIndex,
				deviceType,
				deviceName);
	}
	
	public NodeCpuInfo(NodeDeviceInfoPK pk) {
		super(pk);
	}
	
	public void relateToNodeEntity(NodeInfo nodeEntity) {
		this.setNodeEntity(nodeEntity);
		if (nodeEntity != null) {
			List<NodeCpuInfo> list = nodeEntity.getNodeCpuInfo();
			if (list == null) {
				list = new ArrayList<NodeCpuInfo>();
			} else {
				for(NodeCpuInfo entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			nodeEntity.setNodeCpuInfo(list);
		}
	}
	
	@Override
	public NodeCpuInfo clone() {
		NodeCpuInfo cloneInfo = (NodeCpuInfo)super.clone();
		return cloneInfo;
	}
}