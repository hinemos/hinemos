package com.clustercontrol.vm.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The primary key class for the cc_vm_protocol_mst database table.
 *
 */
@Embeddable
public class VmProtocolMstEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String subPlatformId;
	private String protocol;

	public VmProtocolMstEntityPK() {
	}

	public VmProtocolMstEntityPK(String subPlatformFamily, String protocol) {
		this.setSubPlatformId(subPlatformFamily);
		this.setProtocol(protocol);
	}

	@Column(name="sub_platform_id")
	public String getSubPlatformId() {
		return this.subPlatformId;
	}
	public void setSubPlatformId(String subPlatformId) {
		this.subPlatformId = subPlatformId;
	}

	public String getProtocol() {
		return this.protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof VmProtocolMstEntityPK)) {
			return false;
		}
		VmProtocolMstEntityPK castOther = (VmProtocolMstEntityPK)other;
		return
				this.subPlatformId.equals(castOther.subPlatformId)
				&& this.protocol.equals(castOther.protocol);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.subPlatformId.hashCode();
		hash = hash * prime + this.protocol.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"subPlatformId",
				"protocol"
		};
		String[] values = {
				this.subPlatformId,
				this.protocol
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}