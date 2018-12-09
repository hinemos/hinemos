/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmptrap.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Snmp Trap の OID に関わる定数定義。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class SnmpTrapConstants {
	public static final String SNMP_SYS_UP_TIME_OID = ".1.3.6.1.2.1.1.3.0";
	public static final String SNMP_SYS_UP_TIME_PREFIX_OID = SNMP_SYS_UP_TIME_OID
			.substring(0, SNMP_SYS_UP_TIME_OID.length() - 1);

	public static final String SNMP_TRAP_OID = ".1.3.6.1.6.3.1.1.4.1.0";

	public static final String SNMP_TRAP_ENTERPRISE_OID = ".1.3.6.1.6.3.1.1.4.3.0";

	public static final int SNMP_GENERIC_ColdStartOid = 0;
	public static final int SNMP_GENERIC_WarnStartOid = 1;
	public static final int SNMP_GENERIC_LinkDownOid = 2;
	public static final int SNMP_GENERIC_LinkUpOid = 3;
	public static final int SNMP_GENERIC_AuthFailure = 4;
	public static final int SNMP_GENERIC_EgpNeighborLoss = 5;
	public static final int SNMP_GENERIC_enterpriseSpecific = 6;

	public static final String SNMP_GENERIC_TRAP_ColdStartOid = "." + "1.3.6.1.6.3.1.1.5.1";
	public static final String SNMP_GENERIC_TRAP_WarnStartOid = "." + "1.3.6.1.6.3.1.1.5.2";
	public static final String SNMP_GENERIC_TRAP_LinkDownOid = "." + "1.3.6.1.6.3.1.1.5.3";
	public static final String SNMP_GENERIC_TRAP_LinkUpOid = "." + "1.3.6.1.6.3.1.1.5.4";
	public static final String SNMP_GENERIC_TRAP_AuthFailure = "." + "1.3.6.1.6.3.1.1.5.5";
	public static final String SNMP_GENERIC_TRAP_EgpNeighborLoss = "." + "1.3.6.1.6.3.1.1.5.6";

	public static String formalizeOid(String oid) {
		return "." + oid;
	}

	public static final SortedSet<String> genericTrapV2Set = Collections.unmodifiableSortedSet(new TreeSet<String>(
			Arrays.asList(
				SNMP_GENERIC_TRAP_ColdStartOid,
				SNMP_GENERIC_TRAP_WarnStartOid,
				SNMP_GENERIC_TRAP_LinkDownOid,
				SNMP_GENERIC_TRAP_LinkUpOid,
				SNMP_GENERIC_TRAP_AuthFailure,
				SNMP_GENERIC_TRAP_EgpNeighborLoss)
		));

	public static final SortedMap<Integer, String> genericTrapV1Map = Collections.unmodifiableSortedMap(new TreeMap<Integer, String>(){
			private static final long serialVersionUID = 1L;
			{
				put(SNMP_GENERIC_ColdStartOid, snmpTrapsOid + '.' + (SNMP_GENERIC_ColdStartOid + 1));
				put(SNMP_GENERIC_WarnStartOid, snmpTrapsOid + '.' + (SNMP_GENERIC_WarnStartOid + 1));
				put(SNMP_GENERIC_LinkDownOid, snmpTrapsOid + '.' + (SNMP_GENERIC_LinkDownOid + 1));
				put(SNMP_GENERIC_LinkUpOid, snmpTrapsOid + '.' + (SNMP_GENERIC_LinkUpOid + 1));
				put(SNMP_GENERIC_AuthFailure, snmpTrapsOid + '.' + (SNMP_GENERIC_AuthFailure + 1));
				put(SNMP_GENERIC_EgpNeighborLoss, snmpTrapsOid + '.' + (SNMP_GENERIC_EgpNeighborLoss + 1));
			}
		});

	public static final String snmpTrapsOid = ".1.3.6.1.6.3.1.1.5";

	private SnmpTrapConstants() {
	}
}
