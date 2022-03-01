/*_############################################################################
  _## 
  _##  SNMP4J - Snmp.java  
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
package org.snmp4j;

import java.io.*;
import java.util.*;

import org.snmp4j.event.*;
import org.snmp4j.log.*;
import org.snmp4j.mp.*;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.*;
import org.snmp4j.util.*;

/**
 * The {@code Snmp} class is the core of SNMP4J. It provides functions to
 * send and receive SNMP PDUs. All SNMP PDU types can be send. Confirmed
 * PDUs can be sent synchronously and asynchronously.
 * <p>
 * The {@code Snmp} class is transport protocol independent. Support for
 * a specific {@link TransportMapping} instance is added by calling the
 * {@link #addTransportMapping(TransportMapping transportMapping)} method or
 * creating a {@code Snmp} instance by using the non-default constructor
 * with the corresponding transport mapping. Transport mappings are used
 * for incoming and outgoing messages.</p>
 * <p>
 * To setup a default SNMP session for UDP transport and with SNMPv3 support
 * the following code snippet can be used:</p>
 *
 * <pre>
 *   Address targetAddress = GenericAddress.parse("udp:127.0.0.1/161");
 *   TransportMapping transport = new DefaultUdpTransportMapping();
 *   snmp = new Snmp(transport);
 *   USM usm = new USM(SecurityProtocols.getInstance(),
 *                     new OctetString(MPv3.createLocalEngineID()), 0);
 *   SecurityModels.getInstance().addSecurityModel(usm);
 *   transport.listen();
 * </pre>
 *
 * How a synchronous SNMPv3 message with authentication and privacy is then
 * sent illustrates the following code snippet:
 *
 * <pre>
 *   // add user to the USM
 *   snmp.getUSM().addUser(new OctetString("MD5DES"),
 *                         new UsmUser(new OctetString("MD5DES"),
 *                                     AuthMD5.ID,
 *                                     new OctetString("MD5DESUserAuthPassword"),
 *                                     PrivDES.ID,
 *                                     new OctetString("MD5DESUserPrivPassword")));
 *   // create the target
 *   UserTarget target = new UserTarget();
 *   target.setAddress(targetAddress);
 *   target.setRetries(1);
 *   target.setTimeout(5000);
 *   target.setVersion(SnmpConstants.version3);
 *   target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
 *   target.setSecurityName(new OctetString("MD5DES"));
 *
 *   // create the PDU
 *   PDU pdu = new ScopedPDU();
 *   pdu.add(new VariableBinding(new OID("1.3.6")));
 *   pdu.setType(PDU.GETNEXT);
 *
 *   // send the PDU
 *   ResponseEvent response = snmp.send(pdu, target);
 *   // extract the response PDU (could be null if timed out)
 *   PDU responsePDU = response.getResponse();
 *   // extract the address used by the agent to send the response:
 *   Address peerAddress = response.getPeerAddress();
 * </pre>
 *
 * <p>
 * An asynchronous SNMPv1 request is sent by the following code:
 * </p>
 *
 * <pre>
 *   // setting up target
 *   CommunityTarget target = new CommunityTarget();
 *   target.setCommunity(new OctetString("public"));
 *   target.setAddress(targetAddress);
 *   target.setRetries(2);
 *   target.setTimeout(1500);
 *   target.setVersion(SnmpConstants.version1);
 *   // creating PDU
 *   PDU pdu = new PDU();
 *   pdu.add(new VariableBinding(new OID(new int[] {1,3,6,1,2,1,1,1})));
 *   pdu.add(new VariableBinding(new OID(new int[] {1,3,6,1,2,1,1,2})));
 *   pdu.setType(PDU.GETNEXT);
 *   // sending request
 *   ResponseListener listener = new ResponseListener() {
 *     public void onResponse(ResponseEvent event) {
 *       // Always cancel async request when response has been received
 *       // otherwise a memory leak is created! Not canceling a request
 *       // immediately can be useful when sending a request to a broadcast
 *       // address.
 *       ((Snmp)event.getSource()).cancel(event.getRequest(), this);
 *       System.out.println("Received response PDU is: "+event.getResponse());
 *     }
 *   };
 *   snmp.sendPDU(pdu, target, null, listener);
 * </pre>
 *
 * Traps (notifications) and other SNMP PDUs can be received by adding the
 * following code to the first code snippet above:
 *
 * <pre>
 *   CommandResponder trapPrinter = new CommandResponder() {
 *     public synchronized void processPdu(CommandResponderEvent e) {
 *       PDU command = e.getPDU();
 *       if (command != null) {
 *         System.out.println(command.toString());
 *       }
 *     }
 *   };
 *   snmp.addCommandResponder(trapPrinter);
 * </pre>
 *
 *
 * @author Frank Fock
 * @version 1.10
 */
public class Snmp implements Session, CommandResponder {

  private static final LogAdapter logger = LogFactory.getLogger(Snmp.class);

  private static final int DEFAULT_MAX_REQUEST_STATUS = 2;
  private static final int ENGINE_ID_DISCOVERY_MAX_REQUEST_STATUS = 0;

  // Message processing implementation
  private MessageDispatcher messageDispatcher;

  /**
   * The {@code pendingRequests} table contains pending requests
   * accessed trough the key {@code PduHandle}
   */
  private final Map<PduHandle, PendingRequest> pendingRequests = new Hashtable<PduHandle, PendingRequest>(50);

  /**
   * The {@code asyncRequests} table contains pending requests
   * accessed trough the key userObject
   */
  private final Map<Object, PduHandle> asyncRequests = new Hashtable<Object, PduHandle>(50);

  // Timer for retrying pending requests
  private CommonTimer timer;

  // Listeners for request and trap PDUs
  private List<CommandResponder> commandResponderListeners;

  private TimeoutModel timeoutModel = new DefaultTimeoutModel();

  // Dispatcher for notification listeners - not needed by default
  private NotificationDispatcher notificationDispatcher = null;

  // Default ReportHandler
  private ReportHandler reportHandler = new ReportProcessor();

  private Map<Address,OctetString> contextEngineIDs =
      Collections.synchronizedMap(new HashMap<Address,OctetString>());
  private boolean contextEngineIdDiscoveryDisabled;

  private CounterSupport counterSupport;

  /**
   * Creates a {@code Snmp} instance that uses a
   * {@code MessageDispatcherImpl} with no message processing
   * models and no security protols (by default). You will have to add
   * those by calling the appropriate methods on
   * {@link #getMessageDispatcher()}.
   * <p>
   * At least one transport mapping has to be added before {@link #listen()}
   * is called in order to be able to send and receive SNMP messages.
   * <p>
   * To initialize a {@code Snmp} instance created with this constructor
   * follow this sample code:
   * <pre>
   * Transport transport = ...;
   * Snmp snmp = new Snmp();
   * SecurityProtocols.getInstance().addDefaultProtocols();
   * MessageDispatcher disp = snmp.getMessageDispatcher();
   * disp.addMessageProcessingModel(new MPv1());
   * disp.addMessageProcessingModel(new MPv2c());
   * snmp.addTransportMapping(transport);
   * OctetString localEngineID = new OctetString(
   *    MPv3.createLocalEngineID());
   *    // For command generators, you may use the following code to avoid
   *    // engine ID clashes:
   *    // MPv3.createLocalEngineID(
   *    //   new OctetString("MyUniqueID"+System.currentTimeMillis())));
   * USM usm = new USM(SecurityProtocols.getInstance(), localEngineID, 0);
   * disp.addMessageProcessingModel(new MPv3(usm));
   * snmp.listen();
   * </pre>
   */
  public Snmp() {
    this.messageDispatcher = new MessageDispatcherImpl();
    if (SNMP4JSettings.getSnmp4jStatistics() != SNMP4JSettings.Snmp4jStatistics.none) {
      counterSupport = CounterSupport.getInstance();
    }
  }

  /**
   * Interface for handling reports.
   *
   * @author Frank Fock
   * @version 1.6
   * @since 1.6
   */
  public interface ReportHandler {
    void processReport(PduHandle pduHandle, CommandResponderEvent event);
  }

  protected final void initMessageDispatcher() {
    this.messageDispatcher.addCommandResponder(this);
    this.messageDispatcher.addMessageProcessingModel(new MPv2c());
    this.messageDispatcher.addMessageProcessingModel(new MPv1());
    this.messageDispatcher.addMessageProcessingModel(new MPv3());
    SecurityProtocols.getInstance().addDefaultProtocols();
}

  /**
   * Creates a <code>Snmp</code> instance that uses a
   * <code>MessageDispatcherImpl</code> with all supported message processing
   * models and the default security protols for dispatching.
   * <p>
   * To initialize a <code>Snmp</code> instance created with this constructor
   * follow this sample code:
   * <pre>
   * Transport transport = ...;
   * Snmp snmp = new Snmp(transport);
   * OctetString localEngineID =
   *   new OctetString(snmp.getMPv3().getLocalEngineID());
   * USM usm = new USM(SecurityProtocols.getInstance(), localEngineID, 0);
   * SecurityModels.getInstance().addSecurityModel(usm);
   * snmp.listen();
   * </pre>
   *
   * @param transportMapping TransportMapping
   *    the initial <code>TransportMapping</code>. You can add more or remove
   *    the same later.
   */
  public Snmp(TransportMapping<? extends Address> transportMapping) {
    this();
    initMessageDispatcher();
    if (transportMapping != null) {
      addTransportMapping(transportMapping);
    }
  }

  /**
   * Creates a <code>Snmp</code> instance by supplying a <code>
   * MessageDispatcher</code> and a <code>TransportMapping</code>.
   * <p>
   * As of version 1.1, the supplied message dispatcher is not altered
   * in terms of adding any message processing models to it. This has to be
   * done now outside the Snmp class.
   * <p>
   * To initialize a <code>Snmp</code> instance created with this constructor
   * follow this sample code:
   * <pre>
   * Transport transport = ...;
   * SecurityProtocols.getInstance().addDefaultProtocols();
   * MessageDispatcher disp = new MessageDispatcherImpl();
   * disp.addMessageProcessingModel(new MPv1());
   * disp.addMessageProcessingModel(new MPv2c());
   * Snmp snmp = new Snmp(disp, transport);
   * OctetString localEngineID = new OctetString(
   *    MPv3.createLocalEngineID());
   *    // For command generators, you may use the following code to avoid
   *    // engine ID clashes:
   *    // MPv3.createLocalEngineID(
   *    //   new OctetString("MyUniqueID"+System.currentTimeMillis())));
   * USM usm = new USM(SecurityProtocols.getInstance(), localEngineID, 0);
   * disp.addMessageProcessingModel(new MPv3(usm));
   * snmp.listen();
   * </pre>
   * @param messageDispatcher
   *    a <code>MessageDispatcher</code> instance that will be used to
   *    dispatch incoming and outgoing messages.
   * @param transportMapping
   *    the initial <code>TransportMapping</code>,
   *    which may be <code>null</code>. You can add or remove transport
   *    mappings later using {@link #addTransportMapping} and
   *    {@link #removeTransportMapping} respectively.
   */
  public Snmp(MessageDispatcher messageDispatcher,
              TransportMapping<? extends Address> transportMapping) {
    this.messageDispatcher = messageDispatcher;
    this.messageDispatcher.addCommandResponder(this);
    if (transportMapping != null) {
      addTransportMapping(transportMapping);
    }
    if (SNMP4JSettings.getSnmp4jStatistics() != SNMP4JSettings.Snmp4jStatistics.none) {
      counterSupport = CounterSupport.getInstance();
    }
  }

  /**
   * Creates a <code>Snmp</code> instance by supplying a <code>
   * MessageDispatcher</code>.
   * <p>
   * The supplied message dispatcher is not altered
   * in terms of adding any message processing models to it. This has to be
   * done now outside the Snmp class.
   * </p>
   * <p>
   * Do not forget to add at least one transport mapping before calling the
   * listen method!
   * <p>
   * To initialize a <code>Snmp</code> instance created with this constructor
   * follow this sample code:
   * <pre>
   * Transport transport = ...;
   * SecurityProtocols.getInstance().addDefaultProtocols();
   * MessageDispatcher disp = new MessageDispatcherImpl();
   * disp.addMessageProcessingModel(new MPv1());
   * disp.addMessageProcessingModel(new MPv2c());
   * Snmp snmp = new Snmp(disp);
   * snmp.addTransportMapping(transport);
   * OctetString localEngineID = new OctetString(
   *    MPv3.createLocalEngineID());
   *    // For command generators, you may use the following code to avoid
   *    // engine ID clashes:
   *    // MPv3.createLocalEngineID(
   *    //   new OctetString("MyUniqueID"+System.currentTimeMillis())));
   * USM usm = new USM(SecurityProtocols.getInstance(), localEngineID, 0);
   * disp.addMessageProcessingModel(new MPv3(usm));
   * snmp.listen();
   * </pre>
   *
   * @param messageDispatcher
   *    a <code>MessageDispatcher</code> instance that will be used to
   *    dispatch incoming and outgoing messages.
   * @since 1.5
   */
  public Snmp(MessageDispatcher messageDispatcher) {
    this.messageDispatcher = messageDispatcher;
    this.messageDispatcher.addCommandResponder(this);
    if (SNMP4JSettings.getSnmp4jStatistics() != SNMP4JSettings.Snmp4jStatistics.none) {
      counterSupport = CounterSupport.getInstance();
    }
  }

  /**
   * Returns the message dispatcher associated with this SNMP session.
   * @return
   *   a <code>MessageDispatcher</code> instance.
   * @since 1.1
   */
  public MessageDispatcher getMessageDispatcher() {
    return messageDispatcher;
  }

  /**
   * Sets the message dispatcher associated with this SNMP session. The {@link CommandResponder} registration is
   * removed from the existing message dispatcher (if not {@code null}).
   * @param messageDispatcher
   *    a message dispatcher that processes incoming SNMP {@link PDU}s.
   * @since 2.5.7
   */
  public void setMessageDispatcher(MessageDispatcher messageDispatcher) {
    if (messageDispatcher == null) {
      throw new NullPointerException();
    }
    Collection<TransportMapping> existingTransportMappings = new LinkedList<TransportMapping>();
    if (this.messageDispatcher != null) {
      existingTransportMappings = messageDispatcher.getTransportMappings();
      for (TransportMapping tm : existingTransportMappings) {
        removeTransportMapping(tm);
      }
      this.messageDispatcher.removeCommandResponder(this);
    }
    this.messageDispatcher = messageDispatcher;
    this.messageDispatcher.addCommandResponder(this);
    for (TransportMapping tm : existingTransportMappings) {
      addTransportMapping(tm);
    }
  }

  /**
   * Adds a <code>TransportMapping</code> to this SNMP session.
   * @param transportMapping
   *    a <code>TransportMapping</code> instance.
   */
  public void addTransportMapping(TransportMapping<? extends Address> transportMapping) {
    // connect transport mapping with message dispatcher
    messageDispatcher.addTransportMapping(transportMapping);
    transportMapping.addTransportListener(messageDispatcher);
  }

  /**
   * Removes the specified transport mapping from this SNMP session.
   * If the transport mapping is not currently part of this SNMP session,
   * this method will have no effect.
   * @param transportMapping
   *    a previously added <code>TransportMapping</code>.
   */
  public void removeTransportMapping(TransportMapping<? extends Address> transportMapping) {
    messageDispatcher.removeTransportMapping(transportMapping);
    transportMapping.removeTransportListener(messageDispatcher);
  }

  /**
   * Adds a notification listener to this Snmp instance. Calling this method
   * will create a transport mapping for the specified listening address and
   * registers the provided <code>CommandResponder</code> with the internal
   * <code>NotificationDispatcher</code>.
   *
   * @param transportMapping
   *    the TransportMapping that is listening on the provided listenAddress.
   *    Call <code>TransportMappings.getInstance().createTransportMapping(listenAddress);</code>
   *    to create such a transport mapping.
   * @param listenAddress
   *    the <code>Address</code> denoting the transport end-point
   *    (interface and port) to listen for incoming notifications.
   * @param listener
   *    the <code>CommandResponder</code> instance that should handle
   *    the received notifications.
   * @return
   *    <code>true</code> if registration was successful and <code>false</code>
   *    if, for example, the transport mapping for the listen address could not
   *    be created.
   * @since 2.5.0
   */
  public synchronized boolean addNotificationListener(TransportMapping transportMapping,
                                                      Address listenAddress,
                                                      CommandResponder listener) {
    if (transportMapping instanceof ConnectionOrientedTransportMapping) {
      ((ConnectionOrientedTransportMapping)transportMapping).setConnectionTimeout(0);
    }
    transportMapping.addTransportListener(messageDispatcher);
    if (notificationDispatcher == null) {
      notificationDispatcher = new NotificationDispatcher();
      addCommandResponder(notificationDispatcher);
    }
    notificationDispatcher.addNotificationListener(listenAddress, transportMapping, listener);
    try {
      transportMapping.listen();
      if (logger.isInfoEnabled()) {
        logger.info("Added notification listener for address: "+
            listenAddress);
      }
      return true;
    }
    catch (IOException ex) {
      logger.warn("Failed to initialize notification listener for address '"+
          listenAddress+"': "+ex.getMessage());
      return false;
    }

  }

  /**
   * Adds a notification listener to this Snmp instance. Calling this method
   * will create a transport mapping for the specified listening address and
   * registers the provided <code>CommandResponder</code> with the internal
   * <code>NotificationDispatcher</code>.
   *
   * @param listenAddress
   *    the <code>Address</code> denoting the transport end-point
   *    (interface and port) to listen for incoming notifications.
   * @param listener
   *    the <code>CommandResponder</code> instance that should handle
   *    the received notifications.
   * @return
   *    <code>true</code> if registration was successful and <code>false</code>
   *    if, for example, the transport mapping for the listen address could not
   *    be created.
   * @since 1.6
   */
  public synchronized boolean addNotificationListener(Address listenAddress,
                                                      CommandResponder listener)
  {
    TransportMapping tm =
        TransportMappings.getInstance().createTransportMapping(listenAddress);
    if (tm == null) {
      if (logger.isInfoEnabled()) {
        logger.info("Failed to add notification listener for address: "+
                    listenAddress);
      }
      return false;
    }
    return addNotificationListener(tm, listenAddress, listener);
  }

  /**
   * Removes (deletes) the notification listener for the specified transport
   * endpoint.
   * @param listenAddress
   *    the listen <code>Address</code> to be removed.
   * @return
   *    <code>true</code> if the notification listener has been removed
   *    successfully.
   */
  public synchronized boolean removeNotificationListener(Address listenAddress)
  {
    if (notificationDispatcher != null) {
      if (logger.isInfoEnabled()) {
        logger.info("Removing notification listener for address: "+
                    listenAddress);
      }
      return notificationDispatcher.removeNotificationListener(listenAddress);
    }
    else {
      return false;
    }
  }

  /**
   * Gets the transport mapping registered for the specified listen address.
   * @param listenAddress
   *    the listen address.
   * @return
   *    the {@link TransportMapping} for the specified listen address or <code>null</code>
   *    if there is no notification listener for that address.
   * @since 2.5.0
   */
  public TransportMapping getNotificationListenerTM(Address listenAddress) {
    NotificationDispatcher nd = notificationDispatcher;
     if (nd != null) {
        return nd.getTransportMapping(listenAddress);
     }
    return null;
  }

  /**
   * Puts all associated transport mappings into listen mode.
   * @throws IOException
   *    if a transport mapping throws an <code>IOException</code> when its
   *    {@link TransportMapping#listen()} method has been called.
   */
  public void listen() throws IOException {
    for (TransportMapping tm : messageDispatcher.getTransportMappings()) {
      if (!tm.isListening()) {
        tm.listen();
      }
    }
  }

  /**
   * Gets the next unique request ID. The returned ID is unique across
   * the last 2^31-1 IDs generated by this message dispatcher.
   * @return
   *    an integer value in the range 1..2^31-1. The returned ID can be used
   *    to map responses to requests send through this message dispatcher.
   * @since 1.1
   * @see MessageDispatcher#getNextRequestID
   */
  public int getNextRequestID() {
    return messageDispatcher.getNextRequestID();
  }

  /**
   * Closes the session and frees any allocated resources, i.e. sockets and
   * the internal thread for processing request timeouts.
   * <p>
   * If there are any pending requests, the {@link ResponseListener} associated
   * with the pending requests, will be called with a <code>null</code>
   * response and a {@link InterruptedException} in the error member of the
   * {@link ResponseEvent} returned.
   * <p>
   * After a <code>Session</code> has been closed it must not be used anymore.
   * @throws IOException
   *    if a transport mapping cannot be closed successfully.
   */
  public void close() throws IOException {
    for (TransportMapping tm : messageDispatcher.getTransportMappings()) {
      tm.close();
    }
    CommonTimer t = timer;
    timer = null;
    if (t != null) {
      t.cancel();
    }
    // close all notification listeners
    if (notificationDispatcher != null) {
      notificationDispatcher.closeAll();
    }
    List<PendingRequest> pr;
    synchronized (pendingRequests) {
      pr = new ArrayList<PendingRequest>(pendingRequests.values());
    }
    for (PendingRequest pending : pr) {
      pending.cancel();
      ResponseEvent e =
          new ResponseEvent(this, null, pending.pdu, null, pending.userObject,
              new InterruptedException(
                  "Snmp session has been closed"));
      ResponseListener l = pending.listener;
      if (l != null) {
        l.onResponse(e);
      }
    }
    pendingRequests.clear();
    asyncRequests.clear();
  }

  /**
   * Sends a GET request to a target. This method sets the PDU's type to
   * {@link PDU#GET} and then sends a synchronous request to the supplied
   * target.
   * @param pdu
   *    a <code>PDU</code> instance. For SNMPv3 messages, the supplied PDU
   *    instance has to be a <code>ScopedPDU</code> instance.
   * @param target
   *    the Target instance representing the target SNMP engine where to send
   *    the <code>pdu</code>.
   * @return
   *    the received response encapsulated in a <code>ResponseEvent</code>
   *    instance. To obtain the received response <code>PDU</code> call
   *    {@link ResponseEvent#getResponse()}. If the request timed out,
   *    that method will return <code>null</code>.
   * @throws IOException
   *    if the PDU cannot be sent to the target.
   * @since 1.1
   */
  public ResponseEvent get(PDU pdu, Target target) throws IOException {
    pdu.setType(PDU.GET);
    return send(pdu, target);
  }

  /**
   * Asynchronously sends a GET request <code>PDU</code> to the given target.
   * The response is then returned by calling the supplied
   * <code>ResponseListener</code> instance.
   *
   * @param pdu
   *    the PDU instance to send.
   * @param target
   *    the Target instance representing the target SNMP engine where to send
   *    the <code>pdu</code>.
   * @param userHandle
   *    an user defined handle that is returned when the request is returned
   *    via the <code>listener</code> object.
   * @param listener
   *    a <code>ResponseListener</code> instance that is called when
   *    <code>pdu</code> is a confirmed PDU and the request is either answered
   *    or timed out.
   * @throws IOException
   *    if the PDU cannot be sent to the target.
   * @since 1.1
   */
  public void get(PDU pdu, Target target, Object userHandle,
                  ResponseListener listener) throws IOException {
    pdu.setType(PDU.GET);
    send(pdu, target, userHandle, listener);
  }

  /**
   * Sends a GETNEXT request to a target. This method sets the PDU's type to
   * {@link PDU#GETNEXT} and then sends a synchronous request to the supplied
   * target. This method is a convenience wrapper for the
   * {@link #send(PDU pdu, Target target)} method.
   * @param pdu
   *    a <code>PDU</code> instance. For SNMPv3 messages, the supplied PDU
   *    instance has to be a <code>ScopedPDU</code> instance.
   * @param target
   *    the Target instance representing the target SNMP engine where to send
   *    the <code>pdu</code>.
   * @return
   *    the received response encapsulated in a <code>ResponseEvent</code>
   *    instance. To obtain the received response <code>PDU</code> call
   *    {@link ResponseEvent#getResponse()}. If the request timed out,
   *    that method will return <code>null</code>.
   * @throws IOException
   *    if the PDU cannot be sent to the target.
   * @since 1.1
   */
  public ResponseEvent getNext(PDU pdu, Target target) throws IOException {
    pdu.setType(PDU.GETNEXT);
    return send(pdu, target);
  }

  /**
   * Asynchronously sends a GETNEXT request <code>PDU</code> to the given
   * target. The response is then returned by calling the supplied
   * <code>ResponseListener</code> instance.
   *
   * @param pdu
   *    the PDU instance to send.
   * @param target
   *    the Target instance representing the target SNMP engine where to send
   *    the <code>pdu</code>.
   * @param userHandle
   *    an user defined handle that is returned when the request is returned
   *    via the <code>listener</code> object.
   * @param listener
   *    a <code>ResponseListener</code> instance that is called when
   *    <code>pdu</code> is a confirmed PDU and the request is either answered
   *    or timed out.
   * @throws IOException
   *    if the PDU cannot be sent to the target.
   * @since 1.1
   */
  public void getNext(PDU pdu, Target target, Object userHandle,
                      ResponseListener listener) throws IOException {
    pdu.setType(PDU.GETNEXT);
    send(pdu, target, userHandle, listener);
  }

  /**
   * Sends a GETBULK request to a target. This method sets the PDU's type to
   * {@link PDU#GETBULK} and then sends a synchronous request to the supplied
   * target. This method is a convenience wrapper for the
   * {@link #send(PDU pdu, Target target)} method.
   * @param pdu
   *    a <code>PDU</code> instance. For SNMPv3 messages, the supplied PDU
   *    instance has to be a <code>ScopedPDU</code> instance.
   * @param target
   *    the Target instance representing the target SNMP engine where to send
   *    the <code>pdu</code>.
   * @return
   *    the received response encapsulated in a <code>ResponseEvent</code>
   *    instance. To obtain the received response <code>PDU</code> call
   *    {@link ResponseEvent#getResponse()}. If the request timed out,
   *    that method will return <code>null</code>.
   * @throws IOException
   *    if the PDU cannot be sent to the target.
   * @since 1.1
   */
  public ResponseEvent getBulk(PDU pdu, Target target) throws IOException {
    pdu.setType(PDU.GETBULK);
    return send(pdu, target);
  }

  /**
   * Asynchronously sends a GETBULK request <code>PDU</code> to the given
   * target. The response is then returned by calling the supplied
   * <code>ResponseListener</code> instance.
   *
   * @param pdu
   *    the PDU instance to send.
   * @param target
   *    the Target instance representing the target SNMP engine where to send
   *    the <code>pdu</code>.
   * @param userHandle
   *    an user defined handle that is returned when the request is returned
   *    via the <code>listener</code> object.
   * @param listener
   *    a <code>ResponseListener</code> instance that is called when
   *    <code>pdu</code> is a confirmed PDU and the request is either answered
   *    or timed out.
   * @throws IOException
   *    if the PDU cannot be sent to the target.
   * @since 1.1
   */
  public void getBulk(PDU pdu, Target target, Object userHandle,
                      ResponseListener listener) throws IOException {
    pdu.setType(PDU.GETBULK);
    send(pdu, target, userHandle, listener);
  }

  /**
   * Sends an INFORM request to a target. This method sets the PDU's type to
   * {@link PDU#INFORM} and then sends a synchronous request to the supplied
   * target. This method is a convenience wrapper for the
   * {@link #send(PDU pdu, Target target)} method.
   * @param pdu
   *    a <code>PDU</code> instance. For SNMPv3 messages, the supplied PDU
   *    instance has to be a <code>ScopedPDU</code> instance.
   * @param target
   *    the Target instance representing the target SNMP engine where to send
   *    the <code>pdu</code>.
   * @return
   *    the received response encapsulated in a <code>ResponseEvent</code>
   *    instance. To obtain the received response <code>PDU</code> call
   *    {@link ResponseEvent#getResponse()}. If the request timed out,
   *    that method will return <code>null</code>.
   * @throws IOException
   *    if the inform request could not be send to the specified target.
   * @since 1.1
   */
  public ResponseEvent inform(PDU pdu, Target target) throws IOException {
    pdu.setType(PDU.INFORM);
    return send(pdu, target);
  }

  /**
   * Asynchronously sends an INFORM request <code>PDU</code> to the given
   * target. The response is then returned by calling the supplied
   * <code>ResponseListener</code> instance.
   *
   * @param pdu
   *    the PDU instance to send.
   * @param target
   *    the Target instance representing the target SNMP engine where to send
   *    the <code>pdu</code>.
   * @param userHandle
   *    an user defined handle that is returned when the request is returned
   *    via the <code>listener</code> object.
   * @param listener
   *    a <code>ResponseListener</code> instance that is called when
   *    <code>pdu</code> is a confirmed PDU and the request is either answered
   *    or timed out.
   * @throws IOException
   *    if the PDU cannot be sent to the target.
   * @since 1.1
   */
  public void inform(PDU pdu, Target target, Object userHandle,
                     ResponseListener listener) throws IOException {
    pdu.setType(PDU.INFORM);
    send(pdu, target, userHandle, listener);
  }

  /**
   * Sends a SNMPv1 trap to a target. This method sets the PDU's type to
   * {@link PDU#V1TRAP} and then sends it to the supplied target. This method
   * is a convenience wrapper for the  {@link #send(PDU pdu, Target target)}
   * method.
   * @param pdu
   *    a <code>PDUv1</code> instance.
   * @param target
   *    the Target instance representing the target SNMP engine where to send
   *    the <code>pdu</code>. The selected SNMP protocol version for the
   *    target must be {@link SnmpConstants#version1}.
   * @throws IOException
   *    if the trap cannot be sent.
   * @since 1.1
   */
  public void trap(PDUv1 pdu, Target target) throws IOException {
    if (target.getVersion() != SnmpConstants.version1) {
      throw new IllegalArgumentException(
          "SNMPv1 trap PDU must be used with SNMPv1");
    }
    pdu.setType(PDU.V1TRAP);
    send(pdu, target);
  }

  /**
   * Sends a SNMPv2c or SNMPv3 notification to a target. This method sets the
   * PDU's type to {@link PDU#NOTIFICATION} and then sends it to the supplied
   * target. This method is a convenience wrapper for the
   * {@link #send(PDU pdu, Target target)} method.
   * @param pdu
   *    a <code>PDUv1</code> instance.
   * @param target
   *    the Target instance representing the target SNMP engine where to send
   *    the <code>pdu</code>. The selected SNMP protocol version for the
   *    target must be {@link SnmpConstants#version2c} or
   *    {@link SnmpConstants#version2c}.
   * @throws IOException
   *    if the notification cannot be sent.
   * @since 1.1
   */
  public void notify(PDU pdu, Target target) throws IOException {
    if (target.getVersion() == SnmpConstants.version1) {
      throw new IllegalArgumentException(
          "Notifications PDUs cannot be used with SNMPv1");
    }
    pdu.setType(PDU.NOTIFICATION);
    send(pdu, target);
  }


  /**
   * Sends a SET request to a target. This method sets the PDU's type to
   * {@link PDU#SET} and then sends a synchronous request to the supplied
   * target.
   * @param pdu
   *    a <code>PDU</code> instance. For SNMPv3 messages, the supplied PDU
   *    instance has to be a <code>ScopedPDU</code> instance.
   * @param target
   *    the Target instance representing the target SNMP engine where to send
   *    the <code>pdu</code>.
   * @return
   *    the received response encapsulated in a <code>ResponseEvent</code>
   *    instance. To obtain the received response <code>PDU</code> call
   *    {@link ResponseEvent#getResponse()}. If the request timed out,
   *    that method will return <code>null</code>.
   * @throws IOException
   *    if the PDU cannot be sent to the target.
   * @since 1.1
   */
  public ResponseEvent set(PDU pdu, Target target) throws IOException {
    pdu.setType(PDU.SET);
    return send(pdu, target);
  }

  /**
   * Asynchronously sends a SET request <code>PDU</code> to the given target.
   * The response is then returned by calling the supplied
   * <code>ResponseListener</code> instance.
   *
   * @param pdu
   *    the PDU instance to send.
   * @param target
   *    the Target instance representing the target SNMP engine where to send
   *    the <code>pdu</code>.
   * @param userHandle
   *    an user defined handle that is returned when the request is returned
   *    via the <code>listener</code> object.
   * @param listener
   *    a <code>ResponseListener</code> instance that is called when
   *    <code>pdu</code> is a confirmed PDU and the request is either answered
   *    or timed out.
   * @throws IOException
   *    if the PDU cannot be sent to the target.
   * @since 1.1
   */
  public void set(PDU pdu, Target target, Object userHandle,
                  ResponseListener listener) throws IOException {
    pdu.setType(PDU.SET);
    send(pdu, target, userHandle, listener);
  }

  public ResponseEvent send(PDU pdu, Target target) throws IOException {
    return send(pdu, target, null);
  }

  /**
   * Sends a <code>PDU</code> to the given target and if the <code>PDU</code>
   * is a confirmed request, then the received response is returned
   * synchronously.
   * @param pdu
   *    a <code>PDU</code> instance. When sending a SNMPv1 trap PDU, the
   *    supplied PDU instance must be a <code>PDUv1</code>. For all types of
   *    SNMPv3 messages, the supplied PDU instance has to be a
   *    <code>ScopedPDU</code> instance.
   * @param target
   *    the Target instance representing the target SNMP engine where to send
   *    the <code>pdu</code>.
   * @param transport
   *    specifies the <code>TransportMapping</code> to be used when sending
   *    the PDU. If <code>transport</code> is <code>null</code>, the associated
   *    message dispatcher will try to determine the transport mapping by the
   *    <code>target</code>'s address.
   * @return
   *    the received response encapsulated in a <code>ResponseEvent</code>
   *    instance. To obtain the received response <code>PDU</code> call
   *    {@link ResponseEvent#getResponse()}. If the request timed out,
   *    that method will return <code>null</code>. If the sent <code>pdu</code>
   *    is an unconfirmed PDU (notification, response, or report), then
   *    <code>null</code> will be returned.
   * @throws IOException
   *    if the message could not be sent.
   * @see PDU
   * @see ScopedPDU
   * @see PDUv1
   */
  public ResponseEvent send(PDU pdu, Target target,
                            TransportMapping transport) throws IOException {
    return send(pdu, target, transport, DEFAULT_MAX_REQUEST_STATUS);
  }

  private static long time_waited = 0;
  private static long send_cnt = 0;
  private static Object lockdebug  = new Object();
  private void waitForResponse( final SyncResponseListener syncResponse, long stopTime, long totalTimeout ) throws InterruptedException{
		long waittimer = 50;
		while( syncResponse.getResponse() == null ){
			long start = System.currentTimeMillis();
			syncResponse.wait( waittimer );
			long end = System.currentTimeMillis();
			synchronized(lockdebug){
				time_waited += end - start;
			}
			waittimer *= 2;
			if( System.currentTimeMillis() + waittimer > stopTime ) break;
		}
		synchronized(lockdebug){
			send_cnt++;
		}
		if( send_cnt % 10000 == 0 ){
			logger.debug( "time waited = " + time_waited + ", count = " + send_cnt );
		}
	}

  private ResponseEvent send(PDU pdu, Target target,
                             TransportMapping transport,
                             int maxRequestStatus) throws IOException {
    if (!pdu.isConfirmedPdu()) {
      sendMessage(pdu, target, transport, null);
      return null;
    }
    if (timer == null) {
      createPendingTimer();
    }
    final SyncResponseListener syncResponse = new SyncResponseListener();
    PendingRequest retryRequest = null;
    synchronized (syncResponse) {
      PduHandle handle = null;
      PendingRequest request =
          new PendingRequest(syncResponse, target, pdu, target, transport);
      request.maxRequestStatus = maxRequestStatus;
      handle = sendMessage(request.pdu, target, transport, request);
      long totalTimeout =
          timeoutModel.getRequestTimeout(target.getRetries(),
                                         target.getTimeout());
      long stopTime = System.currentTimeMillis() + totalTimeout;
      try {
        waitForResponse(syncResponse, stopTime, totalTimeout);
        retryRequest = pendingRequests.remove(handle);
        if (logger.isDebugEnabled()) {
          logger.debug("Removed pending request with handle: " + handle);
        }
        request.setFinished();
        request.cancel();
      }
      catch (InterruptedException iex) {
        logger.warn(iex);
        // cleanup request
        request.setFinished();
        request.cancel();
        retryRequest = pendingRequests.remove(handle);
        if (retryRequest != null) {
          retryRequest.setFinished();
          retryRequest.cancel();
        }
        Thread.currentThread().interrupt();
      }
      finally {
        if (!request.finished) {
          // free resources
          retryRequest = pendingRequests.remove(handle);
          if (retryRequest != null) {
            retryRequest.setFinished();
            retryRequest.cancel();
          }
        }
      }
    }
    if (retryRequest != null) {
      retryRequest.setFinished();
      retryRequest.cancel();
    }
    if (syncResponse.getResponse() == null) {
      syncResponse.response =
          new ResponseEvent(Snmp.this, null, pdu, null, null);
    }
    return syncResponse.response;
  }

  private synchronized void createPendingTimer() {
    if (timer == null) {
      timer = SNMP4JSettings.getTimerFactory().createTimer();
    }
  }

  public void send(PDU pdu, Target target,
                   Object userHandle,
                   ResponseListener listener) throws IOException {
    send(pdu, target, null, userHandle, listener);
  }

  public void send(PDU pdu, Target target,
                   TransportMapping transport,
                   Object userHandle,
                   ResponseListener listener) throws IOException {
    if (!pdu.isConfirmedPdu()) {
      sendMessage(pdu, target, transport, null);
      return;
    }
    if (timer == null) {
      createPendingTimer();
    }
    PendingRequest request =
        new AsyncPendingRequest(listener, userHandle, pdu, target, transport);
    sendMessage(request.pdu, target, transport, request);
  }

  /**
   * Actually sends a PDU to a target and returns a handle for the sent PDU.
   * @param pdu
   *    the <code>PDU</code> instance to be sent.
   * @param target
   *    a <code>Target</code> instance denoting the target SNMP entity.
   * @param transport
   *    the (optional) transport mapping to be used to send the request.
   *    If <code>transport</code> is <code>null</code> a suitable transport
   *    mapping is determined from the <code>target</code> address.
   * @param pduHandleCallback
   *    callback for newly created PDU handles before the request is sent out.
   * @throws IOException
   *    if the transport fails to send the PDU or the if the message cannot
   *    be BER encoded.
   * @return PduHandle
   *    that uniquely identifies the sent PDU for further reference.
   */
  protected PduHandle sendMessage(PDU pdu, Target target,
                                  TransportMapping transport,
                                  PduHandleCallback<PDU> pduHandleCallback)
      throws IOException
  {
    TransportMapping tm = transport;
    if (tm == null) {
      tm = lookupTransportMapping(target);
    }
    PduHandle handle = messageDispatcher.sendPdu(tm, target,
                                       pdu, true, pduHandleCallback);
    return handle;
  }

  protected TransportMapping lookupTransportMapping(Target target) {
    List<TransportMapping<? extends Address>> preferredTransports =
        target.getPreferredTransports();
    if (preferredTransports != null) {
      for (TransportMapping<? extends Address> tm : preferredTransports) {
        if (tm.getSupportedAddressClass().isInstance(target.getAddress())) {
          return tm;
        }
      }
    }
    return null;
  }

  public void cancel(PDU request, ResponseListener listener) {
    AsyncRequestKey key = new AsyncRequestKey(request, listener);
    PduHandle pending = asyncRequests.remove(key);
    if (logger.isDebugEnabled()) {
      logger.debug("Cancelling pending request with handle " + pending);
    }
    if (pending != null) {
      PendingRequest pendingRequest =
              pendingRequests.remove(pending);
      if (pendingRequest != null) {
        synchronized (pendingRequest) {
          pendingRequest.setFinished();
          pendingRequest.cancel();
        }
      }
    }
  }

  /**
   * Sets the local engine ID for the SNMP entity represented by this
   * <code>Snmp</code> instance. This is a convenience method that sets
   * the local engine ID in the associated <code>MPv3</code> and
   * <code>USM</code>.
   * @param engineID
   *    a byte array containing the local engine ID. The length and content
   *    has to comply with the constraints defined in the SNMP-FRAMEWORK-MIB.
   * @param engineBoots
   *    the number of boots of this SNMP engine (zero based).
   * @param engineTime
   *    the number of seconds since the value of engineBoots last changed.
   * @see MPv3
   * @see USM
   */
  public void setLocalEngine(byte[] engineID,
                             int engineBoots,
                             int engineTime) {
    MPv3 mpv3 = getMPv3();
    mpv3.setLocalEngineID(engineID);
    mpv3.setCurrentMsgID(MPv3.randomMsgID(engineBoots));
    USM usm = (USM) mpv3.getSecurityModel(SecurityModel.SECURITY_MODEL_USM);
    if (usm != null) {
      usm.setLocalEngine(new OctetString(engineID), engineBoots, engineTime);
    }
  }

  /**
   * Gets the local engine ID if the MPv3 is available, otherwise a runtime
   * exception is thrown.
   * @return byte[]
   *    the local engine ID.
   */
  public byte[] getLocalEngineID() {
    return getMPv3().getLocalEngineID();
  }

  private MPv3 getMPv3() {
    MPv3 mpv3 = (MPv3) getMessageProcessingModel(MessageProcessingModel.MPv3);
    if (mpv3 == null) {
      throw new NoSuchElementException("MPv3 not available");
    }
    return mpv3;
  }

  /**
   * Discovers the engine ID of the SNMPv3 entity denoted by the supplied
   * address. This method does not need to be called for normal operation,
   * because SNMP4J automatically discovers authoritative engine IDs and
   * also automatically synchronize engine time values.
   * <p>
   * <em>For this method to operate successfully, the discover engine IDs
   * flag in {@link USM} must be <code>true</code> (which is the default).
   * </em>
   * @param address
   *    an Address instance representing the transport address of the SNMPv3
   *    entity for which its authoritative engine ID should be discovered.
   * @param timeout
   *    the maximum time in milliseconds to wait for a response.
   * @return
   *    a byte array containing the authoritative engine ID or <code>null</code>
   *    if it could not be discovered.
   * @see USM#setEngineDiscoveryEnabled(boolean enableEngineDiscovery)
   */
  public byte[] discoverAuthoritativeEngineID(Address address, long timeout) {
    MPv3 mpv3 = getMPv3();
    // We need to remove the engine ID explicitly to be sure that it is updated
    OctetString engineID = mpv3.removeEngineID(address);
    // Now try to remove the engine as well
    if (engineID != null) {
      USM usm = getUSM();
      if (usm != null) {
        usm.removeEngineTime(engineID);
      }
    }
    ScopedPDU scopedPDU = new ScopedPDU();
    scopedPDU.setType(PDU.GET);
    SecureTarget target = new UserTarget();
    target.setTimeout(timeout);
    target.setAddress(address);
    target.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
    try {
      send(scopedPDU, target, null, ENGINE_ID_DISCOVERY_MAX_REQUEST_STATUS);
      OctetString authoritativeEngineID = mpv3.getEngineID(address);
      if (authoritativeEngineID == null) {
        return null;
      }
      // we copy the byte array here, so we are sure nobody can modify the
      // internal cache.
      return new OctetString(authoritativeEngineID.getValue()).getValue();
    }
    catch (IOException ex) {
      logger.error(
          "IO error while trying to discover authoritative engine ID: " +
          ex);
      return null;
    }
  }

  /**
   * Gets the User Based Security Model (USM). This is a convenience method
   * that uses the {@link MPv3#getSecurityModel} method of the associated MPv3
   * instance to get the USM.
   * @return
   *    the <code>USM</code> instance associated with the MPv3 bound to this
   *    <code>Snmp</code> instance, or <code>null</code> otherwise.
   */
  public USM getUSM() {
    MPv3 mp = (MPv3) getMessageProcessingModel(MPv3.ID);
    if (mp != null) {
      return (USM)mp.getSecurityModel(SecurityModel.SECURITY_MODEL_USM);
    }
    return null;
  }

  /**
   * Gets the message processing model for the supplied ID.
   * @param messageProcessingModel
   *    a mesage processing model ID as defined in {@link MessageProcessingModel}.
   * @return MessageProcessingModel
   *    a <code>MessageProcessingModel</code> if
   *    <code>messageProcessingModel</code> has been registered with the
   *    message dispatcher associated with this SNMP session.
   */
  public MessageProcessingModel getMessageProcessingModel(int
      messageProcessingModel) {
    return messageDispatcher.getMessageProcessingModel(messageProcessingModel);
  }

  /**
   * Process an incoming request or notification PDU.
   *
   * @param event
   *   a <code>CommandResponderEvent</code> with the decoded incoming PDU as
   *   dispatched to this method call by the associated message dispatcher.
   */
  public void processPdu(CommandResponderEvent event) {
    PduHandle handle = event.getPduHandle();
    if ((SNMP4JSettings.getSnmp4jStatistics() == SNMP4JSettings.Snmp4jStatistics.extended) &&
        (handle instanceof RequestStatistics)) {
      RequestStatistics requestStatistics = (RequestStatistics)handle;
      counterSupport.fireIncrementCounter(new CounterEvent(Snmp.this, SnmpConstants.snmp4jStatsRequestRuntime,
          requestStatistics.getResponseRuntimeNanos()));
      CounterEvent counterEvent =
          new CounterEvent(Snmp.this, SnmpConstants.snmp4jStatsReqTableRuntime, event.getPeerAddress(),
              requestStatistics.getResponseRuntimeNanos());
      counterSupport.fireIncrementCounter(counterEvent);
    }
    PDU pdu = event.getPDU();
    if (pdu.getType() == PDU.REPORT) {
      event.setProcessed(true);
      reportHandler.processReport(handle, event);
    }
    else if (pdu.getType() == PDU.RESPONSE) {
      event.setProcessed(true);
      PendingRequest request;
      if (logger.isDebugEnabled()) {
        logger.debug("Looking up pending request with handle " + handle);
      }
      synchronized (pendingRequests) {
        request = pendingRequests.get(handle);
        if (request != null) {
          request.responseReceived();
        }
      }
      if (request == null) {
        if (logger.isWarnEnabled()) {
          logger.warn("Received response that cannot be matched to any " +
              "outstanding request, address=" +
              event.getPeerAddress() +
              ", requestID=" + pdu.getRequestID());
        }
      }
      else if (!resendRequest(request, pdu)) {
        ResponseListener l = request.listener;
        if (l != null) {
          l.onResponse(new ResponseEvent(this,
                                         event.getPeerAddress(),
                                         request.pdu,
                                         pdu,
                                         request.userObject));
        }
      }
    }
    else {
      if (logger.isDebugEnabled()) {
        logger.debug("Fire process PDU event: " + event.toString());
      }
      fireProcessPdu(event);
    }
  }

  /**
   * Checks whether RFC5343 based context engine ID discovery is disabled or not.
   * The default value is <code>false</code>.
   * @return
   *    <code>true</code> if context engine ID discovery is disabled.
   * @since 2.0
   */
  public boolean isContextEngineIdDiscoveryDisabled() {
    return contextEngineIdDiscoveryDisabled;
  }

  /**
   * Sets the RFC5343 based context engine ID discovery.
   * The default value is <code>false</code>.
   * @param contextEngineIdDiscoveryDisabled
   *    <code>true</code> to disable context engine ID discovery,
   *    <code>false</code> to enable context engine ID discovery.
   * @since 2.0
   */
  public void setContextEngineIdDiscoveryDisabled(boolean contextEngineIdDiscoveryDisabled) {
    this.contextEngineIdDiscoveryDisabled = contextEngineIdDiscoveryDisabled;
  }

  protected boolean resendRequest(PendingRequest request, PDU response) {
    if (request.useNextPDU()) {
      request.responseReceived = false;
      synchronized (pendingRequests) {
        pendingRequests.remove(request.key);
        PduHandle holdKeyUntilResendDone = request.key;
        request.key = null;
        handleInternalResponse(response, request.pdu, request.target.getAddress());
        try {
          sendMessage(request.pdu, request.target, request.transport, request);
        }
        catch (IOException e) {
          logger.error("IOException while resending request after RFC 5343 context engine ID discovery: " +
              e.getMessage(), e);
        }
        // now the previous retry can be released
        if (logger.isDebugEnabled()) {
          logger.debug("Releasing PDU handle "+holdKeyUntilResendDone);
        }
        holdKeyUntilResendDone = null;
      }
      return true;
    }
    return false;
  }

  protected void handleInternalResponse(PDU response, PDU pdu, Address target) {
    Variable contextEngineID = response.getVariable(SnmpConstants.snmpEngineID);
    if (contextEngineID instanceof OctetString) {
      if (pdu instanceof ScopedPDU) {
        ((ScopedPDU)pdu).setContextEngineID((OctetString) contextEngineID);
        if (logger.isInfoEnabled()) {
          logger.info("Discovered contextEngineID '"+contextEngineID+
                      "' by RFC 5343 for " + target);
        }
      }
    }
  }

  class ReportProcessor implements ReportHandler {

    public void processReport(PduHandle handle, CommandResponderEvent e) {
      PDU pdu = e.getPDU();
      logger.debug("Searching pending request with handle" + handle);
      PendingRequest request = pendingRequests.get(handle);

      VariableBinding vb = checkReport(e, pdu, request);
      if (vb == null) return;

      OID firstOID = vb.getOid();
      boolean resend = false;
      if (request.requestStatus < request.maxRequestStatus) {
        switch (request.requestStatus) {
          case 0:
            if (SnmpConstants.usmStatsUnknownEngineIDs.equals(firstOID)) {
              resend = true;
            }
            else if (SnmpConstants.usmStatsNotInTimeWindows.equals(firstOID)) {
              request.requestStatus++;
              resend = true;
            }
            break;
          case 1:
            if (SnmpConstants.usmStatsNotInTimeWindows.equals(firstOID)) {
              resend = true;
            }
            break;
        }
      }
      // if legal report PDU received, then resend request
      if (resend) {
        logger.debug("Send new request after report.");
        request.requestStatus++;
        try {
          // We need no callback here because we already have an equivalent
          // handle registered.
          PduHandle resentHandle =
              sendMessage(request.pdu, request.target, e.getTransportMapping(),
                          null);
          // make sure reference to handle is hold until request is finished,
          // because otherwise cache information may get lost (WeakHashMap)
          request.key = resentHandle;
        }
        catch (IOException iox) {
          logger.error("Failed to send message to " + request.target + ": " +
                       iox.getMessage());
          return;
        }
      }
      else {
        boolean intime;
        // Get the request members needed before canceling the request
        // which resets it
        ResponseListener reqListener = request.listener;
        PDU reqPDU = request.pdu;
        Object reqUserObject = request.userObject;
        synchronized (request) {
          intime = request.cancel();
        }
        // remove pending request
        // (sync is not needed as request is already canceled)
        pendingRequests.remove(handle);
        if (intime && (reqListener != null)) {
          // return report
          reqListener.onResponse(new ResponseEvent(Snmp.this,
              e.getPeerAddress(),
              reqPDU,
              pdu,
              reqUserObject));
        }
        else {
          // silently drop late report
          if (logger.isInfoEnabled()) {
            logger.info("Received late report from " +
                        e.getPeerAddress() +
                        " with request ID " + pdu.getRequestID());
          }
        }
      }
    }

    protected VariableBinding checkReport(CommandResponderEvent e, PDU pdu, PendingRequest request) {
      if (request == null) {
        logger.warn("Unmatched report PDU received from " + e.getPeerAddress());
        return null;
      }
      if (pdu.size() == 0) {
        logger.error("Illegal report PDU received from " + e.getPeerAddress() +
                     " missing report variable binding");
        return null;
      }
      VariableBinding vb = pdu.get(0);
      if (vb == null) {
        logger.error("Received illegal REPORT PDU from " + e.getPeerAddress());
        return null;
      }
      // RFC 7.2.11 (b):
      if (e.getSecurityModel() != request.target.getSecurityModel()) {
        logger.warn("RFC3412 §7.2.11.b: Received REPORT PDU with different security model than cached one: "+e);
        return null;
      }
      // RFC 7.2.11 (b):
      if ((e.getSecurityLevel() == SecurityLevel.NOAUTH_NOPRIV) &&
          (SNMP4JSettings.getReportSecurityLevelStrategy() !=
           SNMP4JSettings.ReportSecurityLevelStrategy.noAuthNoPrivIfNeeded) &&
          ((e.getSecurityLevel() != request.target.getSecurityLevel()) &&
           (!SnmpConstants.usmStatsUnknownUserNames.equals(vb.getOid())) &&
           (!SnmpConstants.usmStatsUnknownEngineIDs.equals(vb.getOid())))) {
        logger.warn("RFC3412 §7.2.11.b:Received REPORT PDU with security level noAuthNoPriv from '"+e+"'. "+
                    "Ignoring it, because report strategy is set to "+
                    SNMP4JSettings.getReportSecurityLevelStrategy());
        return null;
      }
      return vb;
    }
  }


  /**
   * Removes a <code>CommandResponder</code> from this SNMP session.
   * @param listener
   *    a previously added <code>CommandResponder</code> instance.
   */
  public synchronized void removeCommandResponder(CommandResponder listener) {
    if (commandResponderListeners != null &&
        commandResponderListeners.contains(listener)) {
      ArrayList<CommandResponder> v = new ArrayList<CommandResponder>(commandResponderListeners);
      v.remove(listener);
      commandResponderListeners = v;
    }
  }

  /**
   * Adds a <code>CommandResponder</code> to this SNMP session.
   * The command responder will then be informed about incoming SNMP PDUs of
   * any kind that are not related to any outstanding requests of this SNMP
   * session.
   *
   * @param listener
   *    the <code>CommandResponder</code> instance to be added.
   */
  public synchronized void addCommandResponder(CommandResponder listener) {
    ArrayList<CommandResponder> v = (commandResponderListeners == null) ?
        new ArrayList<CommandResponder>(2) :
        new ArrayList<CommandResponder>(commandResponderListeners);
    if (!v.contains(listener)) {
      v.add(listener);
      commandResponderListeners = v;
    }
  }

  /**
   * Fires a <code>CommandResponderEvent</code> event to inform listeners about
   * a received PDU. If a listener has marked the event as processed further
   * listeners will not be informed about the event.
   * @param event
   *    a <code>CommandResponderEvent</code>.
   */
  protected void fireProcessPdu(CommandResponderEvent event) {
    if (commandResponderListeners != null) {
      List<CommandResponder> listeners = commandResponderListeners;
      for (CommandResponder listener : listeners) {
        listener.processPdu(event);
        // if event is marked as processed the event is not forwarded to
        // remaining listeners
        if (event.isProcessed()) {
          return;
        }
      }
    }
  }

  /**
   * Gets the timeout model associated with this SNMP session.
   * @return
   *    a TimeoutModel instance (never <code>null</code>).
   * @see #setTimeoutModel(TimeoutModel timeoutModel)
   */
  public TimeoutModel getTimeoutModel() {
    return timeoutModel;
  }

  /**
   * Returns the report handler which is used internally to process reports
   * received from command responders.
   * @return
   *    the <code>ReportHandler</code> instance.
   * @since 1.6
   */
  public ReportHandler getReportHandler() {
    return reportHandler;
  }

  /**
   * Gets the counter support for Snmp related counters. These are for example:
   * snmp4jStatsRequestTimeouts,
   * snmp4jStatsRequestTimeouts,
   * snmp4jStatsRequestWaitTime
   * @return
   *   the counter support if available. If the {@link SNMP4JSettings#getSnmp4jStatistics()} value is
   *   {@link org.snmp4j.SNMP4JSettings.Snmp4jStatistics#none} then no counter support will be created
   *   and no statistics will be collected.
   * @since 2.4.2
   */
  public CounterSupport getCounterSupport() {
    return counterSupport;
  }

  /**
   * Sets the counter support instance to handle counter events on behalf of this Snmp instance.
   * @param counterSupport
   *   the counter support instance that collects the statistics events created by this Snmp instance.
   *   See also {@link #getCounterSupport()}.
   * @since 2.4.2
   */
  public void setCounterSupport(CounterSupport counterSupport) {
    this.counterSupport = counterSupport;
  }

  /**
   * Sets the timeout model for this SNMP session. The default timeout model
   * sends retries whenever the time specified by the <code>timeout</code>
   * parameter of the target has elapsed without a response being received for
   * the request. By specifying a different timeout model this behaviour can
   * be changed.
   * @param timeoutModel
   *    a <code>TimeoutModel</code> instance (must not be <code>null</code>).
   */
  public void setTimeoutModel(TimeoutModel timeoutModel) {
    if (timeoutModel == null) {
      throw new NullPointerException("Timeout model cannot be null");
    }
    this.timeoutModel = timeoutModel;
  }

  /**
   * Sets the report handler and overrides the default report handler.
   * @param reportHandler
   *    a <code>ReportHandler</code> instance which must not be
   *    <code>null</code>.
   * @since 1.6
   */
  public void setReportHandler(ReportHandler reportHandler) {
    if (reportHandler == null) {
      throw new IllegalArgumentException("ReportHandler must not be null");
    }
    this.reportHandler = reportHandler;
  }

  /**
   * Gets the number of currently pending synchronous requests.
   * @return
   *    the size of the synchronous request queue.
   * @since 2.4.2
   */
  public int getPendingSyncRequestCount() {
    return pendingRequests.size();
  }

  /**
   * Gets the number of currently pending asynchronous requests.
   * @return
   *    the size of the asynchronous request queue.
   * @since 2.4.2
   */
  public int getPendingAsyncRequestCount() {
    return asyncRequests.size();
  }

  private boolean isEmptyContextEngineID(PDU pdu) {
    if (pdu instanceof ScopedPDU) {
      ScopedPDU scopedPDU = (ScopedPDU) pdu;
      return ((scopedPDU.getContextEngineID() == null) ||
              (scopedPDU.getContextEngineID().length() == 0));
    }
    return false;
  }


  class PendingRequest extends TimerTask implements PduHandleCallback<PDU>, Cloneable {

    private PduHandle key;
    protected int retryCount;
    protected ResponseListener listener;
    protected Object userObject;

    protected PDU pdu;
    protected Target target;
    protected TransportMapping transport;

    private int requestStatus = 0;
    // Maximum request status - allows to receive up to two reports and then
    // send the original request again. A value of 0 is used for discovery.
    private int maxRequestStatus = DEFAULT_MAX_REQUEST_STATUS;

    private volatile boolean finished = false;
    private volatile boolean responseReceived = false;
    private volatile boolean pendingRetry = false;
    private volatile boolean cancelled = false;

    private CounterEvent waitTime;
    private CounterEvent waitTimeTarget;

    /**
     * The <code>nextPDU</code> field holds a PDU that has to be sent
     * when the response of the <code>pdu</code> has been received.
     * Usually, this is used for (context) engine ID discovery.
     */
    private PDU nextPDU;

    public PendingRequest(ResponseListener listener,
                          Object userObject,
                          PDU pdu,
                          Target target,
                          TransportMapping transport) {
      this.userObject = userObject;
      this.listener = listener;
      this.retryCount = target.getRetries();
      this.pdu = pdu;
      this.target = target.duplicate();
      this.transport = transport;
      if (SNMP4JSettings.getSnmp4jStatistics() != SNMP4JSettings.Snmp4jStatistics.none) {
        waitTime = new CounterEvent(this, SnmpConstants.snmp4jStatsRequestWaitTime, System.nanoTime());
        if (SNMP4JSettings.getSnmp4jStatistics() == SNMP4JSettings.Snmp4jStatistics.extended) {
          waitTimeTarget =
              new CounterEvent(Snmp.this, SnmpConstants.snmp4jStatsReqTableWaitTime,
                  target.getAddress(), System.nanoTime());
        }
      }
      if (isEmptyContextEngineID(pdu)) {
        OctetString contextEngineID =  contextEngineIDs.get(target.getAddress());
        if (contextEngineID != null) {
            ((ScopedPDU)pdu).setContextEngineID(contextEngineID);
        }
        else if (!contextEngineIdDiscoveryDisabled) {
          discoverContextEngineID();
        }
      }
    }

    private PendingRequest(PendingRequest other) {
      this.userObject = other.userObject;
      this.listener = other.listener;
      this.retryCount = other.retryCount - 1;
      this.pdu = other.pdu;
      this.target = other.target;
      this.requestStatus = other.requestStatus;
      this.responseReceived = other.responseReceived;
      this.transport = other.transport;
      this.nextPDU = other.nextPDU;
      this.waitTime = other.waitTime;
    }

    private void discoverContextEngineID() {
      MessageProcessingModel mp = messageDispatcher.getMessageProcessingModel(target.getVersion());
      if ((mp instanceof MPv3) && (target instanceof SecureTarget)) {
        MPv3 mpv3 = (MPv3)mp;
        SecureTarget st = (SecureTarget)target;
        SecurityModel sm = mpv3.getSecurityModel(st.getSecurityModel());
        if ((sm != null) && (!sm.supportsEngineIdDiscovery())) {
          // Perform context engine ID discovery according to RFC 5343
          if (logger.isInfoEnabled()) {
            logger.info("Performing RFC 5343 contextEngineID discovery on "+target);
          }
          ScopedPDU discoverPDU = new ScopedPDU();
          discoverPDU.setContextEngineID(MPv3.LOCAL_ENGINE_ID);
          discoverPDU.add(new VariableBinding(SnmpConstants.snmpEngineID));
          insertFirstPDU(discoverPDU);
        }
      }
    }

    protected void registerRequest(PduHandle handle) {
      // overwritten by subclasses
    }

    public boolean useNextPDU() {
      if (nextPDU != null) {
        pdu = nextPDU;
        nextPDU = null;
        return true;
      }
      return false;
    }

    public void insertFirstPDU(PDU firstPDU) {
      nextPDU = this.pdu;
      this.pdu = firstPDU;
    }

    public void responseReceived() {
      this.responseReceived = true;
      if (waitTime != null) {
        CounterSupport counterSupport = getCounterSupport();
        if (counterSupport != null) {
          long increment = (System.nanoTime()-waitTime.getIncrement())/SnmpConstants.MILLISECOND_TO_NANOSECOND;
          waitTime.setIncrement(increment);
          counterSupport.fireIncrementCounter(waitTime);
          if (waitTimeTarget != null) {
            waitTimeTarget.setIncrement(increment);
            counterSupport.fireIncrementCounter(waitTimeTarget);
          }
        }
      }
    }

    public PDU getNextPDU() {
      return nextPDU;
    }

    public void setNextPDU(PDU nextPDU) {
      this.nextPDU = nextPDU;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
      return super.clone();
    }

    public synchronized void pduHandleAssigned(PduHandle handle, PDU pdu) {
      if (key == null) {
        key = handle;
        // get pointer to target before adding request to pending list
        // to make sure that this retry is not being cancelled before we
        // got the target pointer.
        Target t = target;
        if ((t != null) && (!cancelled)) {
          pendingRequests.put(handle, this);
          registerRequest(handle);
          if (logger.isDebugEnabled()) {
            logger.debug("Running pending " +
                         ((listener instanceof SyncResponseListener) ?
                          "sync" : "async") +
                         " request with handle " + handle +
                         " and retry count left " + retryCount);
          }
          long delay =
              timeoutModel.getRetryTimeout(t.getRetries() - retryCount,
                                           t.getRetries(),
                                           t.getTimeout());
          if ((!finished) && (!responseReceived) && (!cancelled)) {
            try {
              CommonTimer timerCopy = timer;
              if (timerCopy != null) {
                timerCopy.schedule(this, delay);
              }
              // pending request will be removed by the close() call
            }
            catch (IllegalStateException isex) {
              // ignore
            }
          }
          else {
            pendingRequests.remove(handle);
          }
        }
      }
    }

    /**
     * Process retries of a pending request.
     */
    public synchronized void run() {
      PduHandle m_key = key;
      PDU m_pdu = pdu;
      Target m_target = target;
      TransportMapping m_transport = transport;
      ResponseListener m_listener = listener;
      Object m_userObject = userObject;

      if ((m_key == null) || (m_pdu == null) || (m_target == null) ||
          (m_listener == null)) {
        if (logger.isDebugEnabled()) {
          logger.debug("PendingRequest canceled key="+m_key+", pdu="+m_pdu+
              ", target="+m_target+", transport="+m_transport+", listener="+
              m_listener);
        }
        return;
      }

      try {
        synchronized (pendingRequests) {
          this.pendingRetry =
              (!finished) && (retryCount > 0) && (!responseReceived);
        }
        if (this.pendingRetry) {
          try {
            PendingRequest nextRetry = new PendingRequest(this);
            sendMessage(m_pdu, m_target, m_transport, nextRetry);
            this.pendingRetry = false;
            if (waitTime != null) {
              CounterSupport counterSupport = getCounterSupport();
              if (counterSupport != null) {
                counterSupport.fireIncrementCounter(
                    new CounterEvent(Snmp.this, SnmpConstants.snmp4jStatsRequestRetries));
                if (SNMP4JSettings.getSnmp4jStatistics() == SNMP4JSettings.Snmp4jStatistics.extended) {
                  counterSupport.fireIncrementCounter(
                      new CounterEvent(Snmp.this, SnmpConstants.snmp4jStatsReqTableRetries, m_target.getAddress(), 1));
                }
              }
            }
          }
          catch (IOException ex) {
            ResponseListener l = listener;
            finished = true;
            logger.error("Failed to send SNMP message to " + m_target +
                         ": " +
                         ex.getMessage());
            messageDispatcher.releaseStateReference(m_target.getVersion(),
                m_key);
            if (l != null) {
              listener.onResponse(new ResponseEvent(Snmp.this, null,
                  m_pdu, null, m_userObject, ex));
            }
          }
        }
        else if (!finished) {
          finished = true;
          pendingRequests.remove(m_key);
          if (!cancelled) {
            // request timed out
            if (logger.isDebugEnabled()) {
              logger.debug("Request timed out: " + m_key.getTransactionID());
            }
            messageDispatcher.releaseStateReference(m_target.getVersion(),
                                                    m_key);
            m_listener.onResponse(new ResponseEvent(Snmp.this, null,
                                                  m_pdu, null, m_userObject));
          }
        }
        else {
          // make sure pending request is removed even if response listener
          // failed to call Snmp.cancel
          pendingRequests.remove(m_key);
        }
      }
      catch (RuntimeException ex) {
        logger.error("Failed to process pending request " + m_key +
                     " because " + ex.getMessage(), ex);
        throw ex;
      }
      catch (Error er) {
        logger.fatal("Failed to process pending request " + m_key +
                     " because " + er.getMessage(), er);
        throw er;
      }
    }

    public boolean setFinished() {
      boolean currentState = finished;
      this.finished = true;
      return currentState;
    }

    public void setMaxRequestStatus(int maxRequestStatus) {
      this.maxRequestStatus = maxRequestStatus;
    }

    public int getMaxRequestStatus() {
      return maxRequestStatus;
    }

    public boolean isResponseReceived() {
      return responseReceived;
    }

    /**
     * Cancels the request and clears all internal fields by setting them
     * to <code>null</code>.
     * @return
     *    <code>true</code> if cancellation was successful.
     */
    public boolean cancel(){
      cancelled = true;
      boolean result = super.cancel();
      Target m_target = target;
      if (waitTime != null && !isResponseReceived()) {
        CounterSupport counterSupport = getCounterSupport();
        if (counterSupport != null) {
          counterSupport.fireIncrementCounter(
              new CounterEvent(Snmp.this, SnmpConstants.snmp4jStatsRequestTimeouts));
          if (SNMP4JSettings.getSnmp4jStatistics() == SNMP4JSettings.Snmp4jStatistics.extended && (m_target != null)) {
            counterSupport.fireIncrementCounter(
                new CounterEvent(Snmp.this, SnmpConstants.snmp4jStatsReqTableTimeouts, m_target.getAddress(), 1));
          }
        }
      }

      // free objects early
      if (!pendingRetry) {
        key = null;
        pdu = null;
        target = null;
        transport = null;
        listener = null;
        userObject = null;
      }
      return result;
    }
  }

  class AsyncPendingRequest extends PendingRequest {
    public AsyncPendingRequest(ResponseListener listener,
                               Object userObject,
                               PDU pdu,
                               Target target,
                               TransportMapping transport) {
      super(listener, userObject, pdu, target, transport);
    }

    protected void registerRequest(PduHandle handle) {
      AsyncRequestKey key = new AsyncRequestKey(super.pdu, super.listener);
      asyncRequests.put(key, handle);
    }

  }

  static class AsyncRequestKey {
    private PDU request;
    private ResponseListener listener;

    public AsyncRequestKey(PDU request, ResponseListener listener) {
      this.request = request;
      this.listener = listener;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj argument;
     *   <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
      if (obj instanceof AsyncRequestKey) {
        AsyncRequestKey other = (AsyncRequestKey) obj;
        return (request.equals(other.request) && listener.equals(other.listener));
      }
      return false;
    }

    public int hashCode() {
      return request.hashCode();
    }
  }

  static class SyncResponseListener implements ResponseListener {

    private ResponseEvent response = null;

    public synchronized void onResponse(ResponseEvent event) {
      this.response = event;
      this.notify();
    }

    public ResponseEvent getResponse() {
      return response;
    }

  }

  /**
   * The <code>NotificationDispatcher</code> dispatches traps, notifications,
   * and to registered listeners.
   *
   * @author Frank Fock
   * @version 2.5.0
   * @since 1.6
   */
  class NotificationDispatcher implements CommandResponder {
    // A mapping of transport addresses to transport mappings of notification
    // listeners
    private Hashtable<Address, TransportMapping> notificationListeners = new Hashtable<Address, TransportMapping>(10);
    private Hashtable<TransportMapping, CommandResponder> notificationTransports = new Hashtable<TransportMapping, CommandResponder>(10);

    protected NotificationDispatcher() {
    }

    public TransportMapping getTransportMapping(Address listenAddress) {
      return notificationListeners.get(listenAddress);
    }

    public synchronized void addNotificationListener(Address listenAddress,
                                                     TransportMapping transport,
                                                     CommandResponder listener){
      notificationListeners.put(listenAddress, transport);
      notificationTransports.put(transport, listener);
    }

    public synchronized boolean
        removeNotificationListener(Address listenAddress)
    {
      TransportMapping tm =
              notificationListeners.remove(listenAddress);
      if (tm == null) {
        return false;
      }
      tm.removeTransportListener(messageDispatcher);
      notificationTransports.remove(tm);

      closeTransportMapping(tm);
      return true;
    }

    public synchronized void closeAll() {
      notificationTransports.clear();
      for (TransportMapping tm : notificationListeners.values()) {
        closeTransportMapping(tm);
      }
      notificationListeners.clear();
    }

    public void processPdu(CommandResponderEvent event) {
      CommandResponder listener;
      synchronized (this) {
        listener = notificationTransports.get(event.getTransportMapping());
      }
      if ((event.getPDU() != null) &&
          (event.getPDU().getType() == PDU.INFORM)) {
        // try to send INFORM response
        try {
          sendInformResponse(event);
        }
        catch (MessageException mex) {
          if (logger.isWarnEnabled()) {
            logger.warn("Failed to send response on INFORM PDU event (" +
                        event + "): " + mex.getMessage());
          }
        }
      }
      if (listener != null) {
        listener.processPdu(event);
      }
    }

    /**
     * Sends a RESPONSE PDU to the source address of a INFORM request.
     * @param event
     *    the <code>CommandResponderEvent</code> with the INFORM request.
     * @throws
     *    MessageException if the response could not be created and sent.
     */
    protected void sendInformResponse(CommandResponderEvent event) throws
        MessageException {
      PDU responsePDU = (PDU) event.getPDU().clone();
      responsePDU.setType(PDU.RESPONSE);
      responsePDU.setErrorStatus(PDU.noError);
      responsePDU.setErrorIndex(0);
      messageDispatcher.returnResponsePdu(event.getMessageProcessingModel(),
                                          event.getSecurityModel(),
                                          event.getSecurityName(),
                                          event.getSecurityLevel(),
                                          responsePDU,
                                          event.getMaxSizeResponsePDU(),
                                          event.getStateReference(),
                                          new StatusInformation());
    }
  }

  protected void closeTransportMapping(TransportMapping tm) {
    try {
      tm.close();
    }
    catch (IOException ex) {
      logger.error(ex);
      if (logger.isDebugEnabled()) {
        ex.printStackTrace();
      }
    }
  }

}
