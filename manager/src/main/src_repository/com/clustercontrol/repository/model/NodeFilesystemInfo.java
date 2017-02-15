package com.clustercontrol.repository.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlType;


/**
 * The persistent class for the cc_cfg_node_filesystem database table.
 * 
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_filesystem", schema="setting")
@Cacheable(true)
public class NodeFilesystemInfo extends NodeDeviceInfo {
	private static final long serialVersionUID = 1L;
	private String deviceFilesystemType			= "";

	public NodeFilesystemInfo() {
		super();
	}

	public NodeFilesystemInfo(
			String facilityId,
			Integer deviceIndex,
			String deviceType,
			String deviceName) {
		super(facilityId,
				deviceIndex,
				deviceType,
				deviceName);
	}

	public NodeFilesystemInfo(NodeDeviceInfoPK pk) {
		super(pk);
	}
	
	@Column(name="device_filesystem_type")
	public String getFilesystemType() {
		return this.deviceFilesystemType;
	}

	public void setFilesystemType(String deviceFilesystemType) {
		this.deviceFilesystemType = deviceFilesystemType;
	}
	
	@Override
	public void relateToNodeEntity(NodeInfo nodeEntity) {
		this.setNodeEntity(nodeEntity);
		if (nodeEntity != null) {
			List<NodeFilesystemInfo> list = nodeEntity.getNodeFilesystemInfo();
			if (list == null) {
				list = new ArrayList<NodeFilesystemInfo>();
			} else {
				for(NodeFilesystemInfo entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			nodeEntity.setNodeFilesystemInfo(list);
		}
	}
	
	@Override
	public NodeFilesystemInfo clone() {
		NodeFilesystemInfo cloneInfo = (NodeFilesystemInfo)super.clone();
		cloneInfo.deviceFilesystemType = this.deviceFilesystemType;

		return cloneInfo;
	}
}