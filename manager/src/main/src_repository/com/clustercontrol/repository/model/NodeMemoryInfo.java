package com.clustercontrol.repository.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlType;


/**
 * The persistent class for the cc_cfg_node_memory database table.
 * 
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_memory", schema="setting")
@Cacheable(true)
public class NodeMemoryInfo extends NodeDeviceInfo {
	private static final long serialVersionUID = 1L;

	public NodeMemoryInfo() {
		super();
	}

	public NodeMemoryInfo(
			String facilityId,
			Integer deviceIndex,
			String deviceType,
			String deviceName) {
		super(facilityId,
				deviceIndex,
				deviceType,
				deviceName);
	}

	public NodeMemoryInfo(NodeDeviceInfoPK pk) {
		super(pk);
	}
	
	public void relateToNodeEntity(NodeInfo nodeEntity) {
		this.setNodeEntity(nodeEntity);
		if (nodeEntity != null) {
			List<NodeMemoryInfo> list = nodeEntity.getNodeMemoryInfo();
			if (list == null) {
				list = new ArrayList<NodeMemoryInfo>();
			} else {
				for(NodeMemoryInfo entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			nodeEntity.setNodeMemoryInfo(list);
		}
	}
	
	@Override
	public NodeMemoryInfo clone() {
		NodeMemoryInfo cloneInfo = (NodeMemoryInfo)super.clone();
		return cloneInfo;
	}
}