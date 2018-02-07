/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmptrap.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.snmptrap.util.SnmpTrapConstants;

/**
 * SNMP Trap の OID を v1 および v2 形式にて保持する<BR>
 * また、v1 および v2 形式の Trap OID を汎用的に比較可能にしている。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class TrapId implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(TrapId.class);

	private int version;
	
	private int genericId;
	private int specificId;
	
	private String enterpriseId;
	private String snmpTrapOid;
	
	public TrapId() { }
	
	public TrapId(int version) {
		this.version = version;
	}
	
	public TrapId(String enterpriseId, int genericId, int specificId) {
		this.version = SnmpVersionConstant.TYPE_V1;
		setEnterpriseId(enterpriseId);
		this.genericId = genericId;
		this.specificId = specificId;
	}
	
	public TrapId(String snmpTrapOid, String enterpriseId) {
		this.version = SnmpVersionConstant.TYPE_V2;
		setEnterpriseId(enterpriseId);
		this.snmpTrapOid = snmpTrapOid;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}
	
	public int getVersion() {
		return version;
	}
	
	public void setGenericId(int genericId) {
		this.genericId = genericId;
	}
	
	public int getGenericId() {
		return genericId;
	}
	
	public void setSpecificId(int specificId) {
		this.specificId = specificId;
	}
	
	public int getSpecificId() {
		return specificId;
	}
	
	public void setEnterpriseId(String enterpriseId) {
		if (enterpriseId != null && !enterpriseId.startsWith(".")) {
			enterpriseId = "." + enterpriseId;
		}
		this.enterpriseId = enterpriseId;
	}
	
	public String getEnterpriseId() {
		return enterpriseId;
	}
	
	public void setSnmpTrapOid(String snmpTrapOid) {
		this.snmpTrapOid = snmpTrapOid;
	}
	
	public String getSnmpTrapOid() {
		return snmpTrapOid;
	}
	
	public TrapId asTrapV1Id() {
		if (version == SnmpVersionConstant.TYPE_V1) {
			return this;
		}
		
		String enterpriseId = this.enterpriseId;
		int genericId = -1;
		int specificId = -1;

		int dotIdx = snmpTrapOid.lastIndexOf(".");
		int index = -1;
		try {
			index = Integer.parseInt(snmpTrapOid.substring(dotIdx + 1));
			if (logger.isDebugEnabled()) {
				logger.debug("SnmpTrap v2 PDU (oid) : index = " + index);
			}
		} catch (Exception e) {
			logger.warn("SnmpTrap v2 PDU (oid) : snmpTrapOID.0 = " + snmpTrapOid, e);
		}

		if (SnmpTrapConstants.genericTrapV2Set.contains(snmpTrapOid)) {
			if (logger.isDebugEnabled()) {
				logger.debug("SnmpTrap v2 PDU (this is Generic Trap) : oid = " + snmpTrapOid);
			}

			// スタンダードトラップの場合
			genericId = index - 1;
			specificId = 0;

			if (enterpriseId != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("SnmpTrap v2 PDU (SnmpObjectId found) : enterpriseId = " + enterpriseId);
				}
			}
			else {
				// varbindの値にSNMPトラップ値をセット（RFC 1907）
				enterpriseId = SnmpTrapConstants.genericTrapV1Map.get(genericId);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("SnmpTrap v2 PDU : enterpriseId = " + enterpriseId + ", genericId = " + genericId + ", specificId = " + specificId);
			}
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("SnmpTrap v2 PDU (this is not Generic Trap) : oid = " + snmpTrapOid);
			}

			// スタンダードでないトラップの場合
			genericId = SnmpTrapConstants.SNMP_GENERIC_enterpriseSpecific;
			specificId = index;

			int nextDotIdx = snmpTrapOid.lastIndexOf(".", dotIdx - 1);
			String nextIndex = snmpTrapOid.substring(nextDotIdx + 1, dotIdx);

			if ("0".equals(nextIndex)) {
				enterpriseId = snmpTrapOid.substring(0, nextDotIdx);
			} else {
				enterpriseId = snmpTrapOid.substring(0, dotIdx);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("SnmpTrap v2 PDU : enterpriseId = " + enterpriseId + ", genericId = " + genericId + ", specificId = " + specificId);
			}
		}
		return new TrapId(enterpriseId, genericId, specificId);
	}
	
	public List<TrapId> asTrapV2Id() {
		if (version == SnmpVersionConstant.TYPE_V2) {
			return Arrays.asList(this);
		}
		
		List<TrapId> traps = new ArrayList<TrapId>();
		
		switch (genericId) {
		case SnmpTrapConstants.SNMP_GENERIC_enterpriseSpecific:
			String snmpTrapOid1 = enterpriseId + ".0." + specificId;
			String snmpTrapOid2 = enterpriseId + "." + specificId;
			traps.add(createSnmpTrapV2Id(snmpTrapOid1, null));
			traps.add(createSnmpTrapV2Id(snmpTrapOid2, null));
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("converted v1 (enterpriseId = %s, genericId = %d, specificId = %d) to V2 (snmpTrapOid1 = %s, snmpTrapOid2 = %s)", enterpriseId, genericId, specificId, snmpTrapOid1, snmpTrapOid2));
			}
			break;
		default:
			assert genericId < SnmpTrapConstants.SNMP_GENERIC_enterpriseSpecific;
			assert specificId == 0;
			String genericTrapOid = SnmpTrapConstants.genericTrapV1Map.get(genericId);
			traps.add(createSnmpTrapV2Id(genericTrapOid, enterpriseId));
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("converted v1 (enterpriseId = %s, genericId = %d, specificId = %d) to V2 (snmpTrapOid = %s, enterpriseId = %s)", enterpriseId, genericId, specificId, genericTrapOid, enterpriseId));
			}
			break;
		}
		return traps;
	}

	public boolean match(TrapId other) {
		if (version == SnmpVersionConstant.TYPE_V1) {
			if (this.equals(other))
				return true;

			TrapId otherTrap = (TrapId)other;
			if (other.version == SnmpVersionConstant.TYPE_V1)
				otherTrap = (TrapId)other;
			else
				otherTrap = ((TrapId)other).asTrapV1Id();

			if (this.enterpriseId.equals(otherTrap.enterpriseId) && this.genericId == otherTrap.genericId && this.specificId == otherTrap.specificId)
				return true;

			// Generic Trap か確認。
			if (this.genericId != SnmpTrapConstants.SNMP_GENERIC_enterpriseSpecific && this.genericId == otherTrap.genericId) {
				String genericTrapId = SnmpTrapConstants.genericTrapV1Map.get(this.genericId);

				// Generic Trap の場合、どちらかが既定の Generic Trap Oid にマッチすると一致。
				// 4.1 以下との互換性のために追加。
				if (genericTrapId.equals(this.enterpriseId) || genericTrapId.equals(otherTrap.enterpriseId)) {
					return true;
				}
			}
			return false;
		} else {
			if (this.equals(other))
				return true;

			if (other.version == SnmpVersionConstant.TYPE_V1) {
				TrapId otherTrap = (TrapId)other;
				return otherTrap.match(this);
			}
			else {
				TrapId otherTrap = (TrapId)other;
				if (this.enterpriseId.equals(otherTrap.enterpriseId))
					return true;
			}
			return false;
		}
	}

	@Override
	public int hashCode() {
		if (version == SnmpVersionConstant.TYPE_V1) {
			final int prime = 31;
			int result = 1;
			result = prime * result + version;
			result = prime * result + ((enterpriseId == null) ? 0 : enterpriseId.hashCode());
			result = prime * result + genericId;
			result = prime * result + specificId;
			return result;
		} else {
			final int prime = 31;
			int result = 1;
			result = prime * result + version;
			result = prime * result + ((enterpriseId == null) ? 0 : enterpriseId.hashCode());
			result = prime * result + ((snmpTrapOid == null) ? 0 : snmpTrapOid.hashCode());
			return result;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (version == SnmpVersionConstant.TYPE_V1) {
			if (this == obj) {
				return true;
			}
			
			if (obj instanceof TrapId) {
				TrapId cast = (TrapId)obj;
				if (enterpriseId != null) {
					return enterpriseId.equals(cast.enterpriseId) && genericId == cast.genericId && specificId == cast.specificId;
				}
			}
			return false;
		} else {
			if (this == obj) {
				return true;
			}
			
			if (obj instanceof TrapId) {
				TrapId cast = (TrapId)obj;
				if (enterpriseId != null && snmpTrapOid != null) {
					return enterpriseId.equals(cast.enterpriseId) && snmpTrapOid.equals(cast.snmpTrapOid);
				}
			}
			return false;
		}
	}

	@Override
	public String toString() {
		if (version == SnmpVersionConstant.TYPE_V1) {
			return "TrapId [version=" + version + "]";
		} else {
			return "TrapV2Id [snmpTrapOid=" + snmpTrapOid + ", enterpriseId="
					+ enterpriseId + "]";
		}
	}

	public static TrapId createSnmpTrapV1Id(String enterprizeId, int genericId, int specificId) {
		return new TrapId(enterprizeId, genericId, specificId);
	}

	public static TrapId createSnmpTrapV2Id(String snmpTrapOid) {
		return new TrapId(snmpTrapOid, null);
	}

	public static TrapId createSnmpTrapV2Id(String snmpTrapOid, String enterpriseId) {
		return new TrapId(snmpTrapOid, enterpriseId);
	}
}
