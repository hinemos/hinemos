package com.clustercontrol.repository.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlType;


/**
 * The persistent class for the cc_cfg_node_disk database table.
 * 
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_disk", schema="setting")
@Cacheable(true)
public class NodeDiskInfo extends NodeDeviceInfo {
	private static final long serialVersionUID = 1L;
	private Integer deviceDiskRpm = 0;

	public NodeDiskInfo() {
		super();
	}

	public NodeDiskInfo(
			String facilityId,
			Integer deviceIndex,
			String deviceType,
			String deviceName) {
		super(facilityId,
				deviceIndex,
				deviceType,
				deviceName);
	}

	public NodeDiskInfo(NodeDeviceInfoPK pk) {
		super(pk);
	}

	@Column(name="device_disk_rpm")
	public Integer getDiskRpm() {
		return this.deviceDiskRpm;
	}

	public void setDiskRpm(Integer deviceDiskRpm) {
		this.deviceDiskRpm = deviceDiskRpm;
	}

	/**
	 * nodeEntityオブジェクト参照設定<BR>
	 * 
	 * nodeEntity設定時はSetterに代わりこちらを使用すること。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	@Override
	public void relateToNodeEntity(NodeInfo nodeEntity) {
		this.setNodeEntity(nodeEntity);
		if (nodeEntity != null) {
			List<NodeDiskInfo> list = nodeEntity.getNodeDiskInfo();
			if (list == null) {
				list = new ArrayList<NodeDiskInfo>();
			} else {
				for(NodeDiskInfo entity : list){
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			nodeEntity.setNodeDiskInfo(list);
		}
	}
	
	
	@Override
	public NodeDiskInfo clone() {
		NodeDiskInfo cloneInfo = (NodeDiskInfo)super.clone();
		cloneInfo.deviceDiskRpm = this.deviceDiskRpm;

		return cloneInfo;
	}
}