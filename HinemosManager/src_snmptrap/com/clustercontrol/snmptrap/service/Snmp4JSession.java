/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmptrap.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.StateReference;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Opaque;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.clustercontrol.bean.SnmpProtocolConstant;
import com.clustercontrol.bean.SnmpSecurityLevelConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.snmptrap.bean.SnmpTrap;
import com.clustercontrol.snmptrap.bean.SnmpVarBind;
import com.clustercontrol.snmptrap.bean.TrapId;
import com.clustercontrol.snmptrap.util.SnmpTrapConstants;
import com.clustercontrol.util.HinemosTime;

/**
 * snmp4J のセッションをラップする。<BR>
 * 受信した Snmp Trap 情報は、com.clustercontrol.snmptrap.bean.SnmpTrap へ変換され、<BR>
 * 登録された com.clustercontrol.snmptrap.service.SnmpTrapReceiver へ渡される。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class Snmp4JSession implements SnmpTrapSession {
	private static Logger logger = Logger.getLogger(Snmp4JSession.class);
	
	private String address;
	private int port;
	private SnmpTrapReceiver receiver;
	
	private List<SnmpTrap> receivedTrapBuffer = new ArrayList<SnmpTrap>();
	private Object receivedTrapBufferLock = new Object();
	private boolean stopOnReceivedThread = false;

	// snmp4J のsessionインスタンス
	private Snmp snmp;

	@Override
	public void open() {
		if (address == null || receiver == null)
			throw new IllegalStateException("Address and Receiver must be set before calling open method.");

		logger.info(String.format("starting %s. [address %s, port = %s, handler = %s",
				this.getClass().getSimpleName(), address, port, receiver.getClass().getName()));

		try {
			initSnmp();
			snmp.addCommandResponder(new SnmpTrapCommandResponder());
			
			snmp.listen();
		
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (!stopOnReceivedThread) {
						try {
							synchronized (receivedTrapBufferLock) {
								if (!receivedTrapBuffer.isEmpty()) {
									receiver.onReceived(new ArrayList<SnmpTrap>(receivedTrapBuffer));
									receivedTrapBuffer.clear();
								}
							}
							
							Thread.sleep(1000);
						} catch (Exception e) {
							logger.error(e);
						}
					}
					
					if (!receivedTrapBuffer.isEmpty()) {
						receiver.onReceived(new ArrayList<SnmpTrap>(receivedTrapBuffer));
						receivedTrapBuffer.clear();
					}					
				}
			}, "Snmp4JSessionOnReceived").start();
		}
		catch (Exception e) {
			 throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	public class SnmpTrapCommandResponder implements CommandResponder {

		/* (non-Javadoc)
		 * @see org.snmp4j.CommandResponder#processPdu(org.snmp4j.CommandResponderEvent)
		 */
		@Override
		public void processPdu(CommandResponderEvent event) {
			long receivedTime = HinemosTime.currentTimeMillis();
			String community = new String(event.getSecurityName());
			
			UdpAddress peerAddress = (UdpAddress) event.getPeerAddress();
			InetAddress peerAddr = peerAddress.getInetAddress();
			
			SnmpTrap snmptrap = pduToSnmpTrap(receivedTime, peerAddr, community, event.getPDU());
			
			synchronized (receivedTrapBufferLock) {
				receivedTrapBuffer.add(snmptrap);
			}

			sendResponseIfInform(event);
		}
		
	}
	
	public static SnmpTrap pduToSnmpTrap(long receivedTimestamp, InetAddress srcIpaddr, String community, PDU pdu) {
		long timestamp = 0;
		
		TrapId trapId = null;
		if (pdu.getType() == PDU.V1TRAP && pdu instanceof PDUv1) { //v1
			PDUv1 pduV1 = (PDUv1)pdu;
			
			if (logger.isDebugEnabled()) {
				logger.debug("SnmpTrap v1 PDU : community = " + community);
				logger.debug("SnmpTrap v1 PDU : IP ADDRESS = " + pduV1.getAgentAddress());
				logger.debug("SnmpTrap v1 PDU : Enterprise = " + pduV1.getEnterprise().toDottedString());
				logger.debug("SnmpTrap v1 PDU : Generic = " + pduV1.getGenericTrap());
				logger.debug("SnmpTrap v1 PDU : Specific = " + pduV1.getSpecificTrap());
				logger.debug("SnmpTrap v1 PDU : Timestamp = " + new Date(pduV1.getTimestamp()));
				logger.debug("SnmpTrap v1 PDU : Length = " + pduV1.getBERLength());
			}
			trapId = TrapId.createSnmpTrapV1Id(SnmpTrapConstants.formalizeOid(pduV1.getEnterprise().toString()), pduV1.getGenericTrap(), pduV1.getSpecificTrap());
			timestamp = pduV1.getTimestamp();
		}
		else { //v2c
			VariableBinding firstVb = pdu.getVariableBindings().get(0);
			String firstOid = SnmpTrapConstants.formalizeOid(firstVb.getOid().toDottedString());
			if (firstOid != null && firstOid.startsWith(SnmpTrapConstants.SNMP_SYS_UP_TIME_PREFIX_OID)) {
				// 一部の機器(extremeなどで末尾に0が付与されていない場合がある)への対策
				firstOid = SnmpTrapConstants.SNMP_SYS_UP_TIME_OID;
			} else {
				logger.info("SnmpTrap v2 PDU (first varbind must be sysUpTime.0) : oid = " + firstOid);
				logger.info("dicarding unexpected SnmpTrap v2 received from host " + srcIpaddr);
				throw new RuntimeException("SnmpTrap v2 PDU (first varbind must be sysUpTime.0) : oid = " + firstOid);
			}

			Variable firstVar = firstVb.getVariable();
			if (firstVar.getSyntax() != SMIConstants.SYNTAX_TIMETICKS) {
				logger.info("SnmpTrap v2 PDU (first varbind must be timeticks) : value = " + firstVar.toString());
				logger.info("dicarding unexpected SnmpTrap v2 received from host " + srcIpaddr);
				throw new RuntimeException("SnmpTrap v2 PDU (first varbind must be timeticks) : value = " + firstVar.toString());
			}
			timestamp = firstVar.toLong();

			// 2番目はSNMPトラップOID
			VariableBinding secondVb = pdu.getVariableBindings().get(1);
			String secondOid = SnmpTrapConstants.formalizeOid(secondVb.getOid().toDottedString());
			if (! SnmpTrapConstants.SNMP_TRAP_OID.equals(secondOid)) {
				logger.info("SnmpTrap v2 PDU (second varbind must be snmpTrapOID.0) : oid = " + secondOid);
				logger.info("dicarding unexpected SnmpTrap v2 received from host " + srcIpaddr);
				throw new RuntimeException("SnmpTrap v2 PDU (second varbind must be snmpTrapOID.0) : oid = " + secondOid);
			}

			Variable secondVar = secondVb.getVariable();
			if (secondVar.getSyntax() != SMIConstants.SYNTAX_OBJECT_IDENTIFIER) {
				logger.info("SnmpTrap v2 PDU (second varbind must be object id) : value = " + secondVar.toString());
				logger.info("dicarding unexpected SnmpTrap v2 received from host " + srcIpaddr);
				throw new RuntimeException("SnmpTrap v2 PDU (second varbind must be object id) : value = " + secondVar.toString());
			}
			String secondVarOid = SnmpTrapConstants.formalizeOid(secondVar.toString());

			if (logger.isDebugEnabled()) {
				logger.debug("SnmpTrap v2 : community = " + community);
				logger.debug("SnmpTrap v2 PDU : Peer Address = " + srcIpaddr);
				logger.debug("SnmpTrap v2 PDU : snmpTrapOID = " + secondVarOid);
				logger.debug("SnmpTrap v2 PDU : Request ID = " + pdu.getRequestID());
				logger.debug("SnmpTrap v2 PDU : Length = " + pdu.getBERLength());
				logger.debug("SnmpTrap v2 PDU : Error Status = " + pdu.getErrorStatus());
				logger.debug("SnmpTrap v2 PDU : Error Index = " + pdu.getErrorIndex());
			}

			String enterpriseId = null;
			for (int i = 2; i < pdu.getVariableBindings().size(); i++) {
				VariableBinding vb = pdu.getVariableBindings().get(i);
				String oid = SnmpTrapConstants.formalizeOid(vb.getOid().toDottedString());
				if (SnmpTrapConstants.SNMP_TRAP_ENTERPRISE_OID.equals(oid)) {
					Variable var = vb.getVariable();
					if (var.getSyntax() == SMIConstants.SYNTAX_OBJECT_IDENTIFIER) {
						if (logger.isDebugEnabled()) {
							logger.debug("SnmpTrap v2 PDU (SnmpObjectId found) : enterpriseId = " + var.toString());
						}

						// SNMPTRAP_ENTERPRISE_OIDの1番目を採用する(複数定義されている可能性もある)
						enterpriseId = var.toString();
						break;
					}
				}

			}
			trapId = TrapId.createSnmpTrapV2Id(secondVarOid, enterpriseId);
		}

		int index;
		if (pdu.getType() == PDU.V1TRAP && pdu instanceof PDUv1) {
			index = 0;
		} else {
			// v2cの場合、index=0にsysUpTime、index=1にOIDが入り、varbindはindex=2以降に入るため、
			// index=2からループさせる
			index = 2;
		}
		List<SnmpVarBind> varbinds = new ArrayList<SnmpVarBind>();
		for (int i = index; i < pdu.getVariableBindings().size(); i++) {
			VariableBinding varbind = pdu.getVariableBindings().get(i);

			SnmpVarBind.SyntaxType type;
			Variable variable = varbind.getVariable();
			
			byte value[] = variable.toString().getBytes();
			
			switch (varbind.getSyntax()) {
			case SMIConstants.SYNTAX_COUNTER32:
				type = SnmpVarBind.SyntaxType.Counter32;
				break;
			case SMIConstants.SYNTAX_COUNTER64:
				type = SnmpVarBind.SyntaxType.Counter64;
				break;
			case SMIConstants.SYNTAX_GAUGE32://SYNTAX_UNSIGNED_INTEGER32と同じ
				type = SnmpVarBind.SyntaxType.Gauge32;
				break;
			case SMIConstants.SYNTAX_INTEGER32:
				type = SnmpVarBind.SyntaxType.Int32;
				break;
			case SMIConstants.SYNTAX_OPAQUE:
				type = SnmpVarBind.SyntaxType.Opaque;
				//旧実装と同じ結果を返すため
				String temp = ((Opaque)variable).toHexString(' ');
				if (temp.length() != 0) {
					temp += " ";
				}
				value = temp.getBytes();
				break;
			case SMIConstants.SYNTAX_IPADDRESS:
				type = SnmpVarBind.SyntaxType.IPAddress;
				break;
			case SMIConstants.SYNTAX_OBJECT_IDENTIFIER:
				type = SnmpVarBind.SyntaxType.ObjectId;
				break;
			case SMIConstants.SYNTAX_TIMETICKS:
				type = SnmpVarBind.SyntaxType.TimeTicks;
				value = ((TimeTicks)variable).toString("{0}d {1}h {2}m {3}s {4}0ms").getBytes();
				break;
			case SMIConstants.SYNTAX_OCTET_STRING:
				type = SnmpVarBind.SyntaxType.OctetString;
				try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
					variable.encodeBER(os);
					os.flush();
					value = os.toByteArray();
					
					// varbind のタイプと長さが格納された 2 バイトをスキップ
					if (value.length > 2) {
						value = Arrays.copyOfRange(value, 2, value.length);
					} else {
						// 2バイト以下の場合は空とする
						value = new byte[0];
					}
				} catch (Exception e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
				break;
			default:
				type = SnmpVarBind.SyntaxType.Null;
				break;
			}
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("SnmpTrap PDU (varbind[%d]) : name =%s, value = %s",
						i, varbind.getOid().toDottedString(),
						new String(value)));
			}
			varbinds.add(new SnmpVarBind(SnmpTrapConstants.formalizeOid(varbind.getOid().toString()), type, value));
		}

		return new SnmpTrap(trapId, receivedTimestamp, community, srcIpaddr.getHostAddress(), timestamp, varbinds);
		
	}
	
	private void initSnmp() throws IOException {
		DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping(new UdpAddress(String.format("%s/%s", address, port)));
		MessageDispatcher dispatcher = new MessageDispatcherImpl();
		snmp = new Snmp(dispatcher, transport);
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
		
		SecurityProtocols.getInstance().addDefaultProtocols();
		USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3(usm));
		SecurityModels.getInstance().addSecurityModel(usm);
		
		String user = HinemosPropertyCommon.monitor_snmptrap_v3_user.getStringValue();
		String authPass = HinemosPropertyCommon.monitor_snmptrap_v3_auth_password.getStringValue();
		String privPass = HinemosPropertyCommon.monitor_snmptrap_v3_priv_password.getStringValue();
		String authProtocolStr = HinemosPropertyCommon.monitor_snmptrap_v3_auth_protocol.getStringValue();
		String privProtocolStr = HinemosPropertyCommon.monitor_snmptrap_v3_priv_protocol.getStringValue();
		String securityLevel = HinemosPropertyCommon.monitor_snmptrap_v3_security_level.getStringValue();
		
		OID authProtocol = AuthMD5.ID;
		if (SnmpProtocolConstant.SHA.equals(authProtocolStr)) {
			authProtocol = AuthSHA.ID;
		}
		
		OID privProtocol = PrivDES.ID;
		if (SnmpProtocolConstant.AES.equals(privProtocolStr)) {
			privProtocol = PrivAES128.ID;
		}
		
		UsmUser usmUser;
		if (SnmpSecurityLevelConstant.NOAUTH_NOPRIV.equals(securityLevel)) {
			usmUser = new UsmUser(
					new OctetString(user), 
					null, 
					null,
					null,
					null);
		} else if (SnmpSecurityLevelConstant.AUTH_NOPRIV.equals(securityLevel)) {
			usmUser = new UsmUser(
					new OctetString(user),
					authProtocol,
					new OctetString(authPass),
					null, 
					null);
		} else {
			// AUTH_PRIV
			usmUser = new UsmUser(
					new OctetString(user),
					authProtocol,
					new OctetString(authPass),
					privProtocol, 
					new OctetString(privPass));
		}
		
		snmp.getUSM().addUser(usmUser);
	}

	private void sendResponseIfInform(CommandResponderEvent event) {
		if (event.getPDU().getType() != PDU.INFORM)
			return;

		PDU responsePDU = event.getPDU();
		responsePDU.setErrorIndex(0);
		responsePDU.setErrorStatus(0);
		responsePDU.setType(PDU.RESPONSE);

		/*
		 * StatusInformation represents status information of a SNMPv3 message
		 * that is needed to return a report message.
		 */
		StatusInformation statusInfo = new StatusInformation();

		/*
		 *  StateReference represents state information associated with SNMP messages.
		 *  The state reference is used to send response or report (SNMPv3 only).
		 *  Depending on the security model not all fields may be filled.
		 */
		StateReference stateRef = event.getStateReference();

		// return response PDU
		try {
			event.getMessageDispatcher().returnResponsePdu(
					event.getMessageProcessingModel(),
					event.getSecurityModel(),
					event.getSecurityName(),
					event.getSecurityLevel(),
					responsePDU,
					event.getMaxSizeResponsePDU(),
					stateRef,
					statusInfo);
		} catch (MessageException e) {
			logger.warn(e);
		}
	}

	@Override
	public void setListenAddress(String address, int port) {
		this.address = address;
		this.port = port;
	}

	@Override
	public void registReceiver(SnmpTrapReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void close() {
		logger.info(String.format("stopping %s. [address %s, port = %s, handler = %s",
				this.getClass().getSimpleName(), address, port, receiver.getClass().getName()));

		try {
			snmp.close();
		} catch (IOException e) {
			logger.error(e);
		}
		
		stopOnReceivedThread = true;
	}
}