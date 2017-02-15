package com.clustercontrol.repository.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_device", schema="setting")
@Cacheable(true)
public class NodeGeneralDeviceInfo extends NodeDeviceInfo {
	private static final long serialVersionUID = 1L;

	public NodeGeneralDeviceInfo() {
		super();
	}

	public NodeGeneralDeviceInfo(
			String facilityId,
			Integer deviceIndex,
			String deviceType,
			String deviceName) {
		super(facilityId,
				deviceIndex,
				deviceType,
				deviceName);
	}
	
	public NodeGeneralDeviceInfo(NodeDeviceInfoPK pk) {
		super(pk);
	}
	
	@Override
	public void relateToNodeEntity(NodeInfo nodeEntity) {
		this.setNodeEntity(nodeEntity);
		if (nodeEntity != null) {
			List<NodeGeneralDeviceInfo> list = nodeEntity.getNodeDeviceInfo();
			if (list == null) {
				list = new ArrayList<NodeGeneralDeviceInfo>();
			} else {
				for(NodeGeneralDeviceInfo entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			nodeEntity.setNodeDeviceInfo(list);
		}
	}
	
	@Override
	public NodeGeneralDeviceInfo clone() {
		NodeGeneralDeviceInfo cloneInfo = (NodeGeneralDeviceInfo)super.clone();
		return cloneInfo;
	}
}