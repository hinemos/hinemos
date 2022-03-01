/*_############################################################################
  _## 
  _##  SNMP4J - SnmpURI.java  
  _## 
  _##  Copyright (C) 2003-2020  Frank Fock (SNMP4J.org)
  _##  
  _##  Licensed under the Apache License, Version 2.0 (the "License");
  _##  you may not use this file except in compliance with the License.
  _##  You may obtain a copy of the License at
  _##  
  _##      http://www.apache.org/licenses/LICENSE-2.0
  _##  
  _##  Unless required by applicable law or agreed to in writing, software
  _##  distributed under the License is distributed on an "AS IS" BASIS,
  _##  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  _##  See the License for the specific language governing permissions and
  _##  limitations under the License.
  _##  
  _##########################################################################*/

package org.snmp4j.uri;


import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.log.ConsoleLogFactory;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * The <code>SnmpURI</code> class provides a SNMP service based on the SNMP URI
 * as defined by RFC 4088.
 *
 * @author Frank Fock
 * @since 2.1
 */
public class SnmpURI {

  public enum SnmpUriType { GET, NEXT, SUBTREE };

  private Session snmp;
  private USM usm;
  private Target defaultTarget;

  private int version = SnmpConstants.version3;
  private int securityModel = SecurityModel.SECURITY_MODEL_USM;
  private long timeout = 5000;
  private int retries = 1;

  private String defaultUserInfo = "public";

  private PDUFactory pduFactory = new DefaultPDUFactory();


  public SnmpURI(Session snmp) throws IOException {
    this.snmp = snmp;
    if (snmp instanceof Snmp) {
      usm = ((Snmp) snmp).getUSM();
    }
  }

  public SnmpURI(Session snmp, Target defaultTarget) throws IOException {
    this.snmp = snmp;
    this.defaultTarget = defaultTarget;
    if (snmp instanceof Snmp) {
      usm = ((Snmp) snmp).getUSM();
    }
  }

  public PDUFactory getPduFactory() {
    return pduFactory;
  }

  public void setPduFactory(PDUFactory pduFactory) {
    this.pduFactory = pduFactory;
  }

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  public int getRetries() {
    return retries;
  }

  public void setRetries(int retries) {
    this.retries = retries;
  }

  public int getSecurityModel() {
    return securityModel;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public void setSecurityModel(int securityModel) {
    this.securityModel = securityModel;
  }

  public void setSnmp(Session snmp) {
    this.snmp = snmp;
  }

  public USM getUsm() {
    return usm;
  }

  public void setUsm(USM usm) {
    this.usm = usm;
  }

  public String getDefaultUserInfo() {
    return defaultUserInfo;
  }

  public void setDefaultUserInfo(String defaultUserInfo) {
    this.defaultUserInfo = defaultUserInfo;
  }

  public void browse(URI url, SnmpUriCallback callback, Object userObject) throws UnknownHostException {
    Request request = createSnmpRequest(url);
    PDU pdu = request.getPdu();
    switch (request.getType()) {
      case GET:
        pdu.setType(PDU.GET);
        pdu.addAll(VariableBinding.createFromOIDs(request.getOIDs()));
        sendSnmpRequest(request, pdu, url, callback, userObject);
        break;
      case NEXT:
        pdu.setType(PDU.GETNEXT);
        pdu.addAll(VariableBinding.createFromOIDs(request.getOIDs()));
        sendSnmpRequest(request, pdu, url, callback, userObject);
        break;
      case SUBTREE:
        TreeUtils treeUtils = new TreeUtils(snmp, pduFactory);
        TreeListener treeListener = new AsyncTreeListener(url, callback);
        treeUtils.walk(request.getTarget(), request.getOIDs(), userObject, treeListener);
        break;
    }
  }
  
  public SnmpUriResponse browse(URI url) throws UnknownHostException {
    SnmpUriResponse response = new SnmpUriResponse(PDU.genErr);
    Request request = createSnmpRequest(url);
    PDU pdu = request.getPdu();
    switch (request.getType()) {
      case GET:
        pdu.setType(PDU.GET);
        pdu.addAll(VariableBinding.createFromOIDs(request.getOIDs()));
        response = sendSnmpRequest(request, pdu);
        break;
      case NEXT:
        pdu.setType(PDU.GETNEXT);
        pdu.addAll(VariableBinding.createFromOIDs(request.getOIDs()));
        response = sendSnmpRequest(request, pdu);
        break;
      case SUBTREE:
        TreeUtils treeUtils = new TreeUtils(snmp, pduFactory);
        List<TreeEvent> treeEventList = treeUtils.walk(request.getTarget(), request.getOIDs());
        List<VariableBinding[]> vbs = new ArrayList<VariableBinding[]>(treeEventList.size());
        int errorStatus = PDU.noError;
        for (TreeEvent treeEvent : treeEventList) {
          vbs.add(treeEvent.getVariableBindings());
          errorStatus = treeEvent.getStatus();
        }
        response = new SnmpUriResponse(vbs, errorStatus);
        break;
    }
    return response;
  }

  public SnmpUriResponse updateByValue(URI url, List<Variable> values) throws UnknownHostException {
    SnmpUriResponse response;
    Request request = createSnmpRequest(url);
    PDU pdu = request.getPdu();
    pdu.setType(PDU.SET);
    OID[] oids = request.getOIDs();
    for (int i=0; i<oids.length && i<values.size(); i++) {
      pdu.add(new VariableBinding(oids[i], values.get(i)));
    }
    response = sendSnmpRequest(request, pdu);
    return response;
  }

  public SnmpUriResponse updateByBinding(URI url, List<VariableBinding> values) throws UnknownHostException {
    return sendByBinding(url, values, PDU.SET);
  }

  public SnmpUriResponse sendByBinding(URI url, List<VariableBinding> values, int pduType) throws UnknownHostException {
    SnmpUriResponse response;
    Request request = createSnmpRequest(url);
    PDU pdu = request.getPdu();
    pdu.setType(pduType);
    for (VariableBinding vb : values) {
      pdu.add(vb);
    }
    response = sendSnmpRequest(request, pdu);
    return response;
  }

  private SnmpUriResponse sendSnmpRequest(Request request, PDU pdu) {
    SnmpUriResponse response =  new SnmpUriResponse(PDU.genErr);
    try {
      ResponseEvent responseEvent = snmp.send(pdu, request.getTarget());
      if (responseEvent != null) {
        PDU responsePDU = responseEvent.getResponse();
        if (responsePDU != null) {
          if (responsePDU.getErrorStatus() != PDU.noError) {
            response = new SnmpUriResponse(responsePDU.getErrorStatus());
          }
          else {
            response = new SnmpUriResponse(
                Collections.<VariableBinding[]>singletonList(
                    responsePDU.getVariableBindings().toArray(new VariableBinding[responsePDU.size()])));
          }
        }
        else {
          response = new SnmpUriResponse(SnmpUriResponse.Type.TIMEOUT);
        }
      }
      else {
        response = new SnmpUriResponse(SnmpUriResponse.Type.FINAL);
      }
    } catch (IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    return response;
  }

  private void sendSnmpRequest(Request request, PDU pdu, URI url, SnmpUriCallback callback, Object userObject) {
    ResponseListener listener = new AsyncResponseListener(url, callback);
    try {
      snmp.send(pdu, request.getTarget(), userObject, listener);
    } catch (IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }  
  
  private Request createSnmpRequest(URI url) throws UnknownHostException {
    String host = url.getHost();
    int port = url.getPort();
    if (port == -1) {
      port = SnmpConstants.DEFAULT_COMMAND_RESPONDER_PORT;
    }
    String userInfo = url.getUserInfo();
    if (userInfo == null) {
      userInfo = defaultUserInfo;
    }
    String path = url.getPath();
    String[] segments = path.split("/");
    String contextInfo = "";
    String contextName = "";
    OctetString contextEngineID = null;
    String oidPart = null;
    if (segments.length > 1) {
      contextInfo = segments[0];
      oidPart = segments[1];
      String[] contextInfos = contextInfo.split(";");
      if (contextInfos.length > 1) {
        contextEngineID = OctetString.fromHexStringPairs(contextInfos[1]);
      }
      contextName = contextInfos[0];
    }
    else if (segments.length == 1) {
      oidPart = segments[0];
    }
    Target t = createTarget(new OctetString(userInfo));
    if (host != null) {
      if (t instanceof CertifiedTarget) {
        t.setAddress(new TlsAddress(InetAddress.getByName(host), port));
      }
      else {
        t.setAddress(new UdpAddress(InetAddress.getByName(host), port));
      }
    }
    else {
      t = defaultTarget;
    }
    PDU pdu = pduFactory.createPDU(t);
    if (pdu instanceof ScopedPDU) {
      if (contextEngineID != null) {
        ((ScopedPDU)pdu).setContextEngineID(contextEngineID);
      }
      if (contextName != null) {
        ((ScopedPDU)pdu).setContextName(new OctetString(contextName));
      }
    }
    SnmpUriType type = SnmpUriType.GET;
    if (oidPart != null && oidPart.endsWith(".*")) {
      type = SnmpUriType.SUBTREE;
      oidPart = oidPart.substring(0, oidPart.length()-2);
    }
    else if (oidPart != null && oidPart.endsWith("+")) {
      type = SnmpUriType.NEXT;
      oidPart = oidPart.substring(0, oidPart.length()-1);
    }
    List<OID> oids;
    if (oidPart != null && oidPart.contains("(")) {
      String[] oidStrings = oidPart.split("[\\(,\\),\\,]");
      oids = new ArrayList<OID>(oidStrings.length);
      for (String oidString : oidStrings) {
        if (oidString.length() > 0) {
          OID o = new OID(oidString);
          if (o.isValid()) {
            oids.add(o);
          }
        }
      }
    }
    else if (oidPart != null) {
      oids = Collections.<OID>singletonList(new OID(oidPart));
    }
    else {
      oids = Collections.<OID>emptyList();
    }
    return new Request(t, pdu, oids.toArray(new OID[oids.size()]), type);
  }

  private Target createTarget(OctetString userInfo) {
    if ((userInfo == null) || (userInfo.length() == 0)) {
      return defaultTarget;
    }
    if (version == SnmpConstants.version3) {
      if ((securityModel == SecurityModel.SECURITY_MODEL_USM) && (usm != null)) {
        UsmUserEntry user = usm.getUser(null, userInfo);
        UserTarget target = new UserTarget();
        if (user != null) {
          if (user.getAuthenticationKey() != null) {
            if (user.getPrivacyKey() != null) {
              target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
            }
            else {
              target.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
            }
          }
          else {
            target.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
          }
        }
        target.setVersion(version);
        target.setSecurityName(userInfo);
        target.setSecurityModel(securityModel);
        target.setTimeout(timeout);
        target.setRetries(retries);
        return target;
      }
      else if (securityModel == SecurityModel.SECURITY_MODEL_TSM) {
        CertifiedTarget target = new CertifiedTarget(userInfo);
        target.setVersion(version);
        target.setSecurityModel(securityModel);
        target.setTimeout(timeout);
        target.setRetries(retries);
        return target;
      }
    }
    else {
      CommunityTarget target = new CommunityTarget();
      target.setCommunity(userInfo);
      target.setVersion(version);
      target.setSecurityModel(securityModel);
      target.setTimeout(timeout);
      target.setRetries(retries);
      return target;
    }
    return null;
  }

  private class AsyncResponseListener implements ResponseListener {
    private SnmpUriCallback callback;
    private URI url;

    private AsyncResponseListener(URI url, SnmpUriCallback callback) {
      this.url = url;
      this.callback = callback;
    }

    @Override
    public void onResponse(ResponseEvent event) {
      SnmpUriResponse response = new SnmpUriResponse(SnmpUriResponse.Type.TIMEOUT);
      PDU responsePDU = event.getResponse();
      if (responsePDU != null) {
        if (responsePDU.getErrorStatus() != PDU.noError) {
          response = new SnmpUriResponse(responsePDU.getErrorStatus());
        }
        else {
          response = new SnmpUriResponse(
              Collections.<VariableBinding[]>singletonList(
                  responsePDU.getVariableBindings().toArray(new VariableBinding[responsePDU.size()])));
        }
      }
      callback.onResponse(response, url , event.getUserObject());
    }
  }
  
  private class Request {
    private Target target;
    private PDU pdu;
    private OID[] oids;

    SnmpUriType type = SnmpUriType.GET;

    private Request(Target target, PDU pdu, OID[] oids) {
      this.target = target;
      this.pdu = pdu;
      this.oids = oids;
    }

    private Request(Target target, PDU pdu, OID[] oids, SnmpUriType type) {
      this(target, pdu, oids);
      this.type = type;
    }

    public Target getTarget() {
      return target;
    }

    public PDU getPdu() {
      return pdu;
    }

    public OID[] getOIDs() {
      return oids;
    }

    public SnmpUriType getType() {
      return type;
    }
  }

  private class AsyncTreeListener implements TreeListener {

    private volatile boolean finished = false;
    private URI url;
    private SnmpUriCallback callback;
    
    public AsyncTreeListener(URI url, SnmpUriCallback callback) {
      this.url = url;
      this.callback = callback;
    }

    @Override
    public boolean next(TreeEvent event) {
      if (!finished) {
        SnmpUriResponse response = createResponse(event);
        finished |= callback.onResponse(response, url, event.getUserObject());
      }
      return !finished;
    }

    private SnmpUriResponse createResponse(TreeEvent event) {
      SnmpUriResponse response = new SnmpUriResponse(SnmpUriResponse.Type.TIMEOUT);
      if (event.getStatus() == TreeEvent.STATUS_OK) {
        VariableBinding[] vbs = event.getVariableBindings();
        int errorStatus = event.getStatus();
        response = new SnmpUriResponse(Collections.singletonList(vbs), errorStatus);
        if (errorStatus == PDU.noError) {
          response.setResponseType(SnmpUriResponse.Type.NEXT);
        }
      }
      else if (event.getStatus() == TreeEvent.STATUS_EXCEPTION) {
        response = new SnmpUriResponse(SnmpUriResponse.Type.IO_ERROR, event.getException().getMessage());
      }
      else if (event.getStatus() == TreeEvent.STATUS_REPORT) {
        response = new SnmpUriResponse(SnmpUriResponse.Type.SECURITY_ERROR,
            (event.getReportPDU().size()>0 ? event.getReportPDU().get(0).toString() : "<empty report PDU>"));
      }
      else if (event.getStatus() == TreeEvent.STATUS_WRONG_ORDER) {
        response = new SnmpUriResponse(SnmpUriResponse.Type.LEXICOGRAPHIC_ORDER_ERROR);
      }
      return response;
    }

    @Override
    public void finished(TreeEvent event) {
      if (!finished) {
        SnmpUriResponse response = createResponse(event);
        if (response.getResponseType() == SnmpUriResponse.Type.NEXT) {
          response.setResponseType(SnmpUriResponse.Type.FINAL);
        }
        callback.onResponse(response, url, event.getUserObject());
        finished = true;
      }
    }

    @Override
    public boolean isFinished() {
      return finished;
    }
  }
}
