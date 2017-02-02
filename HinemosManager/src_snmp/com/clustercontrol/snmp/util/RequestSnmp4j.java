/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.snmp.util;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.util.DefaultPDUFactory;

import com.clustercontrol.poller.impl.Snmp4jPollerImpl;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

// Snmp4jPollerImplと統合すること！
public class RequestSnmp4j {
	private static Log log = LogFactory.getLog(RequestSnmp4j.class);

	/** 取得値 */
	private String value;

	/** 取得日時 */
	private long date;

	/** タイプ */
	private int type = SMIConstants.SYNTAX_NULL;

	/** メッセージ */
	private String message = null;

	public long getDate() {
		return date;
	}

	public String getMessage() {
		return message;
	}

	public String getValue() {
		return value;
	}

	public int getType() {
		return type;
	}

	/**
	 * メインルーチン
	 * IPアドレスと　Hashtableを受け取り、
	 * ポーリングした結果をHashtableに代入する
	 */
	public boolean polling (
			String ipAddress,
			String community,
			int portNumber,
			String oidText,
			int version,
			int timeout,
			int retries,
			String securityLevel,
			String user,
			String authPassword,
			String privPassword,
			String authProtocol,
			String privProtocol
			) {

		log.debug("polling() start :" + ipAddress.toString());

		//retriesは本当が試行回数だが、hinemosで実行回数になっているため、それに1を減らす。
		if (--retries < 0) {
			retries =0;
		}
		
		Target target = Snmp4jPollerImpl.createTarget(ipAddress, portNumber, version,
				community, retries, timeout, securityLevel, user);

		DefaultPDUFactory factory = new DefaultPDUFactory();
		factory.setPduType(PDU.GET);
		PDU pdu = factory.createPDU(target);

		pdu.add(new VariableBinding(new OID(oidText)));

		Snmp snmp = null;
		try {
			if (version == SnmpConstants.version3) {
				snmp = Snmp4jPollerImpl.getInstance().createV3Snmp(securityLevel, user, authPassword,
						privPassword, authProtocol, privProtocol);
			} else {
				snmp = Snmp4jPollerImpl.getInstance().getNotV3SnmpFromPool();
			}
			
			snmp.listen();
			ResponseEvent resp = snmp.get(pdu, target);

			date = HinemosTime.currentTimeMillis();
			PDU respPdu = resp.getResponse();
			if (respPdu == null) {
				log.info("snmpTimeoutError():" + ipAddress
						+ " " + oidText
						+ " polling failed at TimeoutError" );
				message = MessageConstant.MESSAGE_COULD_NOT_GET_VALUE.getMessage() + " snmpTimeoutError." + ipAddress
						+ " " + oidText;
				return false;
			}

			if (respPdu.getErrorStatus() != SnmpConstants.SNMP_ERROR_SUCCESS) {
				message = MessageConstant.MESSAGE_COULD_NOT_GET_VALUE.getMessage() + " Error Status:" + respPdu.getErrorStatus();
				return false;
			}

			for (VariableBinding binding : respPdu.getVariableBindings()) {
				Variable var = binding.getVariable();
				if (var instanceof Null) {
					message = MessageConstant.MESSAGE_COULD_NOT_GET_VALUE.getMessage() + " SnmpV2Error. Value:" + binding.toString();
				} else {
					value = var.toString();
					type = binding.getVariable().getSyntax();
				}
				break;
			}
		} catch (IOException e) {
			log.info("polling : class=" + e.getClass().getName() + ", message=" + e.getMessage() + ", ip=" + ipAddress);
			message = MessageConstant.MESSAGE_COULD_NOT_GET_VALUE.getMessage() + " (" + e.getMessage() + ")";
			return false;
		} finally {
			if (version == SnmpConstants.version3 && snmp != null) {
				try {
					snmp.close();
				} catch (IOException e) {
					log.warn("polling : " + e.getMessage());
				}
			}
		}

		return true;
	}
}
