/*_############################################################################
  _## 
  _##  SNMP4J - TLSTM.java  
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

package org.snmp4j.transport;

import org.snmp4j.SNMP4JSettings;
import org.snmp4j.TransportStateReference;
import org.snmp4j.asn1.BER;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.event.CounterEvent;
import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.CounterSupport;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.*;
import org.snmp4j.transport.tls.TlsTmSecurityCallback;
import org.snmp4j.transport.tls.TlsTrustManager;
import org.snmp4j.util.CommonTimer;
import org.snmp4j.util.SnmpConfigurator;
import org.snmp4j.util.WorkerTask;

import javax.net.ssl.*;
import javax.security.auth.x500.X500Principal;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The <code>TLSTM</code> implements the Transport Layer Security
 * Transport Mapping (TLS-TM) as defined by RFC 5953
 * with the new IO API and {@link javax.net.ssl.SSLEngine}.
 * <p>
 * It uses a single thread for processing incoming and outgoing messages.
 * The thread is started when the <code>listen</code> method is called, or
 * when an outgoing request is sent using the <code>sendMessage</code> method.
 *
 * @author Frank Fock
 * @version 2.6.4
 * @since 2.0
 */
public class TLSTM extends TcpTransportMapping {

  private static final LogAdapter logger = LogFactory.getLogger(TLSTM.class);
  public static final int TLS_MAX_FRAGMENT_SIZE = 16384;

  private Map<Address, SocketEntry> sockets = new Hashtable<Address, SocketEntry>();
  private WorkerTask server;
  private ServerThread serverThread;

  private CommonTimer socketCleaner;
  // 1 minute default timeout
  private long connectionTimeout = 60000;
  private boolean serverEnabled = false;

  private long nextSessionID = 1;

  private SSLEngineConfigurator sslEngineConfigurator =
      new DefaultSSLEngineConfiguration();

  private TlsTmSecurityCallback<X509Certificate> securityCallback;
  private CounterSupport counterSupport;

  public static final String DEFAULT_TLSTM_PROTOCOLS = "TLSv1";
  public static final int MAX_TLS_PAYLOAD_SIZE = 32*1024;

  private String localCertificateAlias;
  private String keyStore;
  private String keyStorePassword;
  private String trustStore;
  private String trustStorePassword;
  private String[] tlsProtocols;
  private TLSTMTrustManagerFactory trustManagerFactory = new DefaultTLSTMTrustManagerFactory();
  private int tlsMaxFragmentSize = TLS_MAX_FRAGMENT_SIZE;

  /**
   * Creates a default TCP transport mapping with the server for incoming
   * messages disabled.
   * @throws UnknownHostException
   *    if the local host cannot be determined.
   */
  public TLSTM() throws UnknownHostException {
     super(new TlsAddress(InetAddress.getLocalHost(), 0));
    this.counterSupport = CounterSupport.getInstance();
    super.maxInboundMessageSize = MAX_TLS_PAYLOAD_SIZE;
  }

  /**
   * Creates a TLS transport mapping with the server for incoming
   * messages bind to the given address. The {@code securityCallback}
   * needs to be specified before {@link #listen()} is called.
   *
   * @param address
   *    the address to bind for incoming requests.
   * @throws java.io.IOException
   *    on failure of binding a local port.
   */
  public TLSTM(TlsAddress address)
      throws IOException {
    super(address);
    super.maxInboundMessageSize = MAX_TLS_PAYLOAD_SIZE;
    this.serverEnabled = true;
    this.counterSupport = CounterSupport.getInstance();
    try {
      if (Class.forName("javax.net.ssl.X509ExtendedTrustManager") != null) {
        Class trustManagerFactoryClass =
            Class.forName("org.snmp4j.transport.tls.TLSTMExtendedTrustManagerFactory");
        Constructor c = trustManagerFactoryClass.getConstructors()[0];
        TLSTMTrustManagerFactory trustManagerFactory =
            (TLSTMTrustManagerFactory) c.newInstance(CounterSupport.getInstance(), securityCallback);
        setTrustManagerFactory(trustManagerFactory);
      }
    }
    catch (ClassNotFoundException ex) {
      //throw new IOException("Failed to load TLSTMTrustManagerFactory: "+ex.getMessage(), ex);
    }
    catch (InvocationTargetException ex) {
      throw new IOException("Failed to init TLSTMTrustManagerFactory: "+ex.getMessage(), ex);
    }
    catch (IllegalArgumentException ex) {
      throw new IOException("Failed to setup TLSTMTrustManagerFactory: "+ex.getMessage(), ex);
    }
    catch (IllegalAccessException ex) {
      throw new IOException("Failed to access TLSTMTrustManagerFactory: "+ex.getMessage(), ex);
    }
    catch (InstantiationException ex) {
      throw new IOException("Failed to instantiate TLSTMTrustManagerFactory: " + ex.getMessage(), ex);
    }
  }

  /**
   * Creates a TLS transport mapping that binds to the given address
   * (interface) on the local host.
   *
   * @param securityCallback
   *    a security name callback to resolve X509 certificates to tmSecurityNames.
   * @param serverAddress
   *    the TcpAddress instance that describes the server address to listen
   *    on incoming connection requests.
   * @throws java.io.IOException
   *    if the given address cannot be bound.
   */
  public TLSTM(TlsTmSecurityCallback<X509Certificate> securityCallback,
               TlsAddress serverAddress) throws IOException
  {
     this(securityCallback, serverAddress, CounterSupport.getInstance());
  }

  /**
   * Creates a TLS transport mapping that binds to the given address
   * (interface) on the local host.
   *
   * @param securityCallback
   *    a security name callback to resolve X509 certificates to tmSecurityNames.
   * @param serverAddress
   *    the TcpAddress instance that describes the server address to listen
   *    on incoming connection requests.
   * @param counterSupport
   *    The CounterSupport instance to be used to count events created by this
   *    TLSTM instance. To get a default instance, use
   *    {@link CounterSupport#getInstance()}.
   * @throws java.io.IOException
   *    if the given address cannot be bound.
   */
  public TLSTM(TlsTmSecurityCallback<X509Certificate> securityCallback,
               TlsAddress serverAddress, CounterSupport counterSupport) throws IOException
  {
    super(serverAddress);
    super.maxInboundMessageSize = MAX_TLS_PAYLOAD_SIZE;
    this.serverEnabled = true;
    this.securityCallback = securityCallback;
    this.counterSupport = counterSupport;
  }

  public String getLocalCertificateAlias() {
    if (localCertificateAlias == null) {
      return System.getProperty(SnmpConfigurator.P_TLS_LOCAL_ID, null);
    }
    return localCertificateAlias;
  }

  public String[] getTlsProtocols() {
    if (tlsProtocols == null) {
      String s = System.getProperty(SnmpConfigurator.P_TLS_VERSION, DEFAULT_TLSTM_PROTOCOLS);
      return s.split(",");
    }
    return tlsProtocols;
  }

  /**
   * Sets the TLS protocols/versions that TLSTM should use during handshake.
   * The default is defined by {@link #DEFAULT_TLSTM_PROTOCOLS}.
   *
   * @param tlsProtocols
   *    an array of TLS protocol (version) names supported by the SunJSSE provider.
   *    The order in the array defines which protocol is tried during handshake
   *    first.
   * @since 2.0.3
   */
  public void setTlsProtocols(String[] tlsProtocols) {
    this.tlsProtocols = tlsProtocols;
  }

  public String getKeyStore() {
    if (keyStore == null) {
      return System.getProperty("javax.net.ssl.keyStore");
    }
    return keyStore;
  }

  public void setKeyStore(String keyStore) {
    this.keyStore = keyStore;
  }

  public String getKeyStorePassword() {
    if (keyStorePassword == null) {
      return System.getProperty("javax.net.ssl.keyStorePassword");
    }
    return keyStorePassword;
  }

  public void setKeyStorePassword(String keyStorePassword) {
    this.keyStorePassword = keyStorePassword;
  }

  /**
   * Sets the certificate alias used for client and server authentication
   * by this TLSTM. Setting this property to a value other than {@code null}
   * filters out any certificates which are not in the chain of the given
   * alias.
   *
   * @param localCertificateAlias
   *    a certificate alias which filters a single certification chain from
   *    the {@code javax.net.ssl.keyStore} key store to be used to
   *    authenticate this TLS transport mapping. If {@code null} no
   *    filtering appears, which could lead to more than a single chain
   *    available for authentication by the peer, which would violate the
   *    TLSTM standard requirements.
   */
  public void setLocalCertificateAlias(String localCertificateAlias) {
    this.localCertificateAlias = localCertificateAlias;
  }

  public CounterSupport getCounterSupport() {
    return counterSupport;
  }

  @Override
  public Class<? extends Address> getSupportedAddressClass() {
    return TlsAddress.class;
  }

  public TlsTmSecurityCallback<X509Certificate> getSecurityCallback() {
    return securityCallback;
  }

  public void setSecurityCallback(TlsTmSecurityCallback<X509Certificate> securityCallback) {
    this.securityCallback = securityCallback;
  }

  public TLSTMTrustManagerFactory getTrustManagerFactory() {
    return trustManagerFactory;
  }

  /**
   * Set the TLSTM trust manager factory. Using a trust manager factory other than the
   * default allows to add support for Java 1.7 X509ExtendedTrustManager.
   * @param trustManagerFactory
   *   a X.509 trust manager factory implementing the interface {@link TLSTMTrustManagerFactory}.
   * @since 2.0.3
   */
  public void setTrustManagerFactory(TLSTMTrustManagerFactory trustManagerFactory) {
    if (trustManagerFactory == null) {
      throw new NullPointerException();
    }
    this.trustManagerFactory = trustManagerFactory;
  }

  /**
   * Listen for incoming and outgoing requests. If the {@code serverEnabled}
   * member is {@code false} the server for incoming requests is not
   * started. This starts the internal server thread that processes messages.
   * @throws java.net.SocketException
   *    when the transport is already listening for incoming/outgoing messages.
   * @throws java.io.IOException
   *    if the listen port could not be bound to the server thread.
   */
  public synchronized void listen() throws IOException {
    if (server != null) {
      throw new SocketException("Port already listening");
    }
    try {
      serverThread = new ServerThread();
      if (logger.isInfoEnabled()) {
        logger.info("TCP address "+getListenAddress()+" bound successfully");
      }
    } catch (NoSuchAlgorithmException e) {
      throw new IOException("SSL not available: "+e.getMessage(), e);
    }
    server = SNMP4JSettings.getThreadFactory().createWorkerThread(
      "TLSTM_"+getAddress(), serverThread, true);
    if (connectionTimeout > 0) {
      // run as daemon
      socketCleaner = SNMP4JSettings.getTimerFactory().createTimer();
    }
    server.run();
  }

  /**
   * Sets the name of the listen thread for this UDP transport mapping.
   * This method has no effect, if called before {@link #listen()} has been
   * called for this transport mapping.
   *
   * @param name
   *    the new thread name.
   * @since 1.6
   */
  public void setThreadName(String name) {
    WorkerTask st = server;
    if (st instanceof Thread) {
      ((Thread)st).setName(name);
    }
  }

  /**
   * Returns the name of the listen thread.
   * @return
   *    the thread name if in listening mode, otherwise {@code null}.
   * @since 1.6
   */
  public String getThreadName() {
    WorkerTask st = server;
    if (st != null) {
      return ((Thread)st).getName();
    }
    else {
      return null;
    }
  }

  /**
   * Closes all open sockets and stops the internal server thread that
   * processes messages.
   */
  public void close() {
    for (SocketEntry entry : sockets.values()) {
      entry.closeSession();
    }
    WorkerTask st = server;
    if (st != null) {
      st.terminate();
      st.interrupt();
      try {
        st.join();
      }
      catch (InterruptedException ex) {
        logger.warn(ex);
      }
      server = null;
      for (SocketEntry entry : sockets.values()) {
        Socket s = entry.getSocket();
        if (s != null) {
          try {
            SocketChannel sc = s.getChannel();
            s.close();
            if (logger.isDebugEnabled()) {
              logger.debug("Socket to " + entry.getPeerAddress() + " closed");
            }
            if (sc != null) {
              sc.close();
              if (logger.isDebugEnabled()) {
                logger.debug("Socket channel to " +
                    entry.getPeerAddress() + " closed");
              }
            }
          }
          catch (IOException iox) {
            // ignore
            logger.debug(iox);
          }
        }
      }
      if (socketCleaner != null) {
        socketCleaner.cancel();
      }
      socketCleaner = null;
    }
  }

  /**
   * Closes a connection to the supplied remote address, if it is open. This
   * method is particularly useful when not using a timeout for remote
   * connections.
   *
   * @param remoteAddress
   *    the address of the peer socket.
   * @return
   *    {@code true} if the connection has been closed and
   *    {@code false} if there was nothing to close.
   * @throws java.io.IOException
   *    if the remote address cannot be closed due to an IO exception.
   * @since 1.7.1
   */
  public synchronized boolean close(TcpAddress remoteAddress) throws IOException {
    if (logger.isDebugEnabled()) {
      logger.debug("Closing socket for peer address "+remoteAddress);
    }
    SocketEntry entry = (SocketEntry)removeSocketEntry(remoteAddress);
    if (entry != null) {
      Socket s = entry.getSocket();
      if (s != null) {
        SocketChannel sc = entry.getSocket().getChannel();
        entry.getSocket().close();
        if (logger.isInfoEnabled()) {
          logger.info("Socket to " + entry.getPeerAddress() + " closed");
        }
        if (sc != null) {
          sc.close();
          if (logger.isDebugEnabled()) {
            logger.debug("Closed socket channel for peer address "+
                         remoteAddress);
          }
        }
      }
      return true;
    }
    return false;
  }

  protected Object removeSocketEntry(TcpAddress remoteAddress) {
    return sockets.remove(remoteAddress);
  }

  /**
   * Sends a SNMP message to the supplied address.
   * @param address
   *    an {@code TcpAddress}. A {@code ClassCastException} is thrown
   *    if {@code address} is not a {@code TcpAddress} instance.
   * @param message byte[]
   *    the message to sent.
   * @param tmStateReference
   *    the (optional) transport model state reference as defined by
   *    RFC 5590 section 6.1.
   * @throws java.io.IOException
   *    if an IO exception occurs while trying to send the message.
   */
  public void sendMessage(TcpAddress address, byte[] message,
                          TransportStateReference tmStateReference)
      throws IOException
  {
    if (server == null || serverThread == null) {
      if (isOpenSocketOnSending()) {
        listen();
      }
      else {
        handleDroppedMessageToSend(address, message, tmStateReference);
      }
    }
    if (serverThread != null) {
      if ((suspendedAddresses.size() > 0) && suspendedAddresses.contains(address)) {
        handleDroppedMessageToSend(address, message, tmStateReference);
      }
      else {
        serverThread.sendMessage(address, message, tmStateReference);
      }
    }
  }

  /**
   * Gets the connection timeout. This timeout specifies the time a connection
   * may be idle before it is closed.
   * @return long
   *    the idle timeout in milliseconds.
   */
  public long getConnectionTimeout() {
    return connectionTimeout;
  }

  /**
   * Sets the connection timeout. This timeout specifies the time a connection
   * may be idle before it is closed.
   * @param connectionTimeout
   *    the idle timeout in milliseconds. A zero or negative value will disable
   *    any timeout and connections opened by this transport mapping will stay
   *    opened until they are explicitly closed.
   */
  public void setConnectionTimeout(long connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  /**
   * Checks whether a server for incoming requests is enabled.
   * @return boolean
   */
  public boolean isServerEnabled() {
    return serverEnabled;
  }

  @Override
  public MessageLengthDecoder getMessageLengthDecoder() {
    return null;
  }

  /**
   * Sets whether a server for incoming requests should be created when
   * the transport is set into listen state. Setting this value has no effect
   * until the {@link #listen()} method is called (if the transport is already
   * listening, {@link #close()} has to be called before).
   * @param serverEnabled
   *    if {@code true} if the transport will listens for incoming
   *    requests after {@link #listen()} has been called.
   */
  public void setServerEnabled(boolean serverEnabled) {
    this.serverEnabled = serverEnabled;
  }

  @Override
  public void setMessageLengthDecoder(MessageLengthDecoder messageLengthDecoder) {
/*
    if (messageLengthDecoder == null) {
      throw new NullPointerException();
    }
    this.messageLengthDecoder = messageLengthDecoder;
    */
  }

  /**
   * Gets the inbound buffer size for incoming requests. When SNMP packets are
   * received that are longer than this maximum size, the messages will be
   * silently dropped and the connection will be closed.
   * @return
   *    the maximum inbound buffer size in bytes.
   */
  public int getMaxInboundMessageSize() {
    return super.getMaxInboundMessageSize();
  }

  /**
   * Sets the maximum buffer size for incoming requests. When SNMP packets are
   * received that are longer than this maximum size, the messages will be
   * silently dropped and the connection will be closed.
   * @param maxInboundMessageSize
   *    the length of the inbound buffer in bytes.
   */
  public void setMaxInboundMessageSize(int maxInboundMessageSize) {
    this.maxInboundMessageSize = maxInboundMessageSize;
  }


  private synchronized void timeoutSocket(SocketEntry entry) {
    if (connectionTimeout > 0) {
      SocketTimeout socketTimeout = new SocketTimeout(entry);
      entry.setSocketTimeout(socketTimeout);
      socketCleaner.schedule(socketTimeout, connectionTimeout);
    }
  }

  public boolean isListening() {
    return (server != null);
  }

  public static OctetString getFingerprint(X509Certificate cert) {
    OctetString certFingerprint = null;
    try {
      String algo = cert.getSigAlgName();
      if (algo.contains("with")) {
        algo = algo.substring(0, algo.indexOf("with"));
        if (algo.startsWith("SHA") && !algo.contains("-")) {
          algo = "SHA-"+algo.substring(3);
        }
      }
      MessageDigest md = MessageDigest.getInstance(algo);
      md.update(cert.getEncoded());
      certFingerprint = new OctetString(md.digest());
    } catch (NoSuchAlgorithmException e) {
      logger.error("No such digest algorithm exception while getting fingerprint from "+
                   cert+": "+e.getMessage(), e);
    } catch (CertificateEncodingException e) {
      logger.error("Certificate encoding exception while getting fingerprint from "+
                   cert+": "+e.getMessage(), e);
    }
    return certFingerprint;
  }

  public static Object getSubjAltName(Collection<List<?>> subjAltNames, int type) {
    if (subjAltNames != null) {
      for (List<?> entry : subjAltNames) {
        int t = (Integer)entry.get(0);
        if (t == type) {
          return entry.get(1);
        }
      }
    }
    return null;
  }

  @Override
  public TcpAddress getListenAddress() {
    int port = tcpAddress.getPort();
    ServerThread serverThreadCopy = serverThread;
    try {
      port = serverThreadCopy.ssc.socket().getLocalPort();
    }
    catch (NullPointerException npe) {
      // ignore
    }
    return new TcpAddress(tcpAddress.getInetAddress(), port);
  }

  /**
   * Sets optional server socket options. The default implementation does
   * nothing.
   * @param serverSocket
   *    the {@code ServerSocket} to apply additional non-default options.
   */
  protected void setSocketOptions(ServerSocket serverSocket) {
  }

  class SocketEntry {
    private Socket socket;
    private TcpAddress peerAddress;
    private long lastUse;
    private LinkedList<byte[]> message = new LinkedList<byte[]>();
    private ByteBuffer inNetBuffer;
    private ByteBuffer inAppBuffer;
    private ByteBuffer outAppBuffer;
    private ByteBuffer outNetBuffer;
    private volatile int registrations = 0;
    private SSLEngine sslEngine;
    private long sessionID;
    private TransportStateReference tmStateReference;
    private boolean handshakeFinished;

    private final Object outboundLock = new Object();
    private final Object inboundLock = new Object();

    private SocketTimeout socketTimeout;

    public SocketEntry(TcpAddress address, Socket socket,
                       boolean useClientMode,
                       TransportStateReference tmStateReference) throws NoSuchAlgorithmException {
      this.inAppBuffer = ByteBuffer.allocate(getMaxInboundMessageSize());
      this.inNetBuffer = ByteBuffer.allocate(getMaxInboundMessageSize());
      this.outNetBuffer = ByteBuffer.allocate(getMaxInboundMessageSize());
      this.peerAddress = address;
      this.tmStateReference = tmStateReference;
      this.socket = socket;
      this.lastUse = System.nanoTime();
      if (tmStateReference == null) {
        counterSupport.fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionAccepts));
      }
      SSLContext sslContext = sslEngineConfigurator.getSSLContext(useClientMode, tmStateReference);
      this.sslEngine = sslContext.createSSLEngine(address.getInetAddress().getHostName(), address.getPort());
      sslEngine.setUseClientMode(useClientMode);
//      sslEngineConfigurator.configure(SSLContext.getDefault(), useClientMode);
      sslEngineConfigurator.configure(sslEngine);
      synchronized (TLSTM.this) {
        sessionID = nextSessionID++;
      }
    }

    public synchronized void addRegistration(Selector selector, int opKey)
        throws ClosedChannelException
    {
      if ((this.registrations & opKey) == 0) {
        this.registrations |= opKey;
        if (logger.isDebugEnabled()) {
          logger.debug("Adding operation "+opKey+" for: " + toString());
        }
        socket.getChannel().register(selector, registrations, this);
      }
      else if (!socket.getChannel().isRegistered()) {
        this.registrations = opKey;
        if (logger.isDebugEnabled()) {
          logger.debug("Registering new operation "+opKey+" for: " + toString());
        }
        socket.getChannel().register(selector, opKey, this);
      }
    }

    public synchronized void removeRegistration(Selector selector, int opKey)
        throws ClosedChannelException {
      if ((this.registrations & opKey) == opKey) {
        this.registrations &= ~opKey;
        socket.getChannel().register(selector, this.registrations, this);
      }
    }

    public synchronized boolean isRegistered(int opKey) {
      return (this.registrations & opKey) == opKey;
    }

    public long getLastUse() {
      return lastUse;
    }

    public void used() {
      lastUse = System.nanoTime();
    }

    public Socket getSocket() {
      return socket;
    }

    public TcpAddress getPeerAddress() {
      return peerAddress;
    }

    public synchronized void addMessage(byte[] message) {
      this.message.add(message);
    }

    public synchronized void insertMessages(List<byte[]> messages) {
      this.message.addAll(0, messages);
    }

    public synchronized byte[] nextMessage() {
      if (this.message.size() > 0) {
        return this.message.removeFirst();
      }
      return null;
    }

    public synchronized boolean hasMessage() {
      return !this.message.isEmpty();
    }

    public void setInNetBuffer(ByteBuffer byteBuffer) {
      this.inNetBuffer = byteBuffer;
    }

    public ByteBuffer getInNetBuffer() {
      return inNetBuffer;
    }

    public ByteBuffer getOutNetBuffer() {
      return outNetBuffer;
    }

    public void setOutNetBuffer(ByteBuffer outNetBuffer) {
      this.outNetBuffer = outNetBuffer;
    }

    public String toString() {
      return "SocketEntry[peerAddress="+peerAddress+
          ",socket="+socket+",lastUse="+new Date(lastUse/SnmpConstants.MILLISECOND_TO_NANOSECOND)+
          ",inNetBuffer="+inNetBuffer+
          ",inAppBuffer="+inAppBuffer+
          ",outNetBuffer="+outNetBuffer+
          ",socketTimeout="+socketTimeout+"]";
    }

    public SocketTimeout getSocketTimeout() {
      return socketTimeout;
    }

    public void setSocketTimeout(SocketTimeout socketTimeout) {
      this.socketTimeout = socketTimeout;
    }

    public void checkTransportStateReference() {
      if (tmStateReference == null) {
        tmStateReference =
            new TransportStateReference(TLSTM.this, peerAddress, new OctetString(),
                SecurityLevel.authPriv, SecurityLevel.authPriv,
                true, sessionID);
        OctetString securityName = null;
        if (securityCallback != null) {
          try {
            securityName = securityCallback.getSecurityName(
                (X509Certificate[]) sslEngine.getSession().getPeerCertificates());
          } catch (SSLPeerUnverifiedException e) {
            logger.error("SSL peer '" + peerAddress + "' is not verified: " + e.getMessage(), e);
            sslEngine.setEnableSessionCreation(false);
          }
        }
        tmStateReference.setSecurityName(securityName);
      }
      else if (tmStateReference.getTransportSecurityLevel().equals(SecurityLevel.undefined)) {
        tmStateReference.setTransportSecurityLevel(SecurityLevel.authPriv);
      }


    }

    public void setInAppBuffer(ByteBuffer inAppBuffer) {
      this.inAppBuffer = inAppBuffer;
    }

    public ByteBuffer getInAppBuffer() {
      return inAppBuffer;
    }

    public boolean isHandshakeFinished() {
      return handshakeFinished;
    }

    public void setHandshakeFinished(boolean handshakeFinished) {
      this.handshakeFinished = handshakeFinished;
    }

    public boolean isAppOutPending() {
      synchronized (outboundLock) {
        return (outAppBuffer != null) && (outAppBuffer.limit() > 0);
      }
    }

    public long getSessionID() {
      return sessionID;
    }

    public void closeSession() {
      sslEngine.closeOutbound();
      counterSupport.fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionServerCloses));
      try {
        SSLEngineResult result;
        do {
          result = sendNetMessage(this);
        }
        while (result != null && (result.getStatus() != SSLEngineResult.Status.CLOSED) &&
               (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_WRAP));

      } catch (IOException e) {
        logger.error("IOException while closing outbound channel of " + this + ": " + e.getMessage(), e);
      }
    }
  }

  class SocketTimeout extends TimerTask {
    private SocketEntry entry;

    public SocketTimeout(SocketEntry entry) {
      this.entry = entry;
    }

    /**
     * run
     */
    public void run() {
      long now = System.nanoTime();
      if ((socketCleaner == null) ||
          ((now - entry.getLastUse())/SnmpConstants.MILLISECOND_TO_NANOSECOND >= connectionTimeout)) {
        if (logger.isDebugEnabled()) {
          logger.debug("Socket has not been used for "+
                       (now - entry.getLastUse())+
                       " milliseconds, closing it");
        }
        removeSocketEntry(entry.getPeerAddress());
        SocketEntry entryCopy = entry;
        try {
          synchronized (entryCopy) {
            entryCopy.getSocket().close();
          }
          logger.info("Socket to "+entryCopy.getPeerAddress()+
                      " closed due to timeout");
        }
        catch (IOException ex) {
          logger.error(ex);
        }
      }
      else {
        long nextRun = System.currentTimeMillis() +
            (now - entry.getLastUse())/SnmpConstants.MILLISECOND_TO_NANOSECOND + connectionTimeout;
        if (logger.isDebugEnabled()) {
          logger.debug("Scheduling " + nextRun);
        }
        SocketTimeout socketTimeout = new SocketTimeout(entry);
        entry.setSocketTimeout(socketTimeout);
        socketCleaner.schedule(socketTimeout, nextRun);
      }
    }

    public boolean cancel(){
        boolean result = super.cancel();
        // free objects early
        entry = null;
        return result;
    }
  }

  class ServerThread implements WorkerTask {

    private volatile boolean stop = false;
    private Throwable lastError = null;
    private ServerSocketChannel ssc;
    private Selector selector;

    private LinkedList<SocketEntry> pending = new LinkedList<SocketEntry>();
    private BlockingQueue<SocketEntry> outQueue = new LinkedBlockingQueue<SocketEntry>();
    private BlockingQueue<SocketEntry> inQueue = new LinkedBlockingQueue<SocketEntry>();

    public ServerThread() throws IOException, NoSuchAlgorithmException {
      // Selector for incoming requests
      selector = Selector.open();
      if (serverEnabled) {
        // Create a new server socket and set to non blocking mode
        ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        // Bind the server socket
        InetSocketAddress isa = new InetSocketAddress(tcpAddress.getInetAddress(),
            tcpAddress.getPort());
        setSocketOptions(ssc.socket());
        ssc.socket().bind(isa);
        // Register accepts on the server socket with the selector. This
        // step tells the selector that the socket wants to be put on the
        // ready list when accept operations occur, so allowing multiplexed
        // non-blocking I/O to take place.
        ssc.register(selector, SelectionKey.OP_ACCEPT);
      }
    }

    private synchronized void processQueues() {
      while (!outQueue.isEmpty() || !inQueue.isEmpty()) {
        while (!outQueue.isEmpty()) {
          SocketEntry entry = null;
          try {
            SSLEngineResult result;
            entry = outQueue.take();
            result = sendNetMessage(entry);
            if ((result != null) && runDelegatedTasks(result, entry)) {
              if (entry.isAppOutPending()) {
                writeMessage(entry, entry.getSocket().getChannel());
              }
            }
          } catch (IOException iox) {
            logger.error("IO exception caught while SSL processing: "+iox.getMessage(), iox);
            while (inQueue.remove(entry)) {
              // no body
            }
          } catch (InterruptedException e) {
            logger.error("SSL processing interrupted: "+e.getMessage(), e);
            return;
          }
        }
        while (!inQueue.isEmpty()) {
          SocketEntry entry = null;
          try {
            entry = inQueue.take();
            synchronized (entry.inboundLock) {
              // Is there any data to read?
              if (entry.getInNetBuffer().position() > 0) {
                entry.inNetBuffer.flip();
                logger.debug("TLS inNetBuffer = " + entry.inNetBuffer);
                SSLEngineResult nextResult =
                        entry.sslEngine.unwrap(entry.inNetBuffer, entry.inAppBuffer);
                adjustInNetBuffer(entry, nextResult);
                if (runDelegatedTasks(nextResult, entry)) {
                  switch (nextResult.getStatus()) {
                    case BUFFER_UNDERFLOW:
                      entry.inNetBuffer.position(entry.inNetBuffer.limit());
                      entry.inNetBuffer.limit(entry.inNetBuffer.capacity());
                      entry.addRegistration(selector, SelectionKey.OP_READ);
                      break;
                    case BUFFER_OVERFLOW:
                      // TODO
                      break;
                    case CLOSED:
                      continue;
                    case OK:
                      if (entry.isAppOutPending()) {
                        // we have a message to send
                        writeMessage(entry, entry.getSocket().getChannel());
                      }
                      entry.inAppBuffer.flip();
                      logger.debug("Dispatching inAppBuffer=" + entry.inAppBuffer);
                      if (entry.inAppBuffer.limit() > 0) {
                        dispatchMessage(entry.getPeerAddress(),
                                entry.inAppBuffer, entry.inAppBuffer.limit(),
                                entry.sessionID, entry.tmStateReference);
                      }
                      entry.inAppBuffer.clear();
                  }
                }
              }
              else {
                entry.addRegistration(selector, SelectionKey.OP_READ);
              }
            }
          } catch (IOException iox) {
            logger.error("IO exception caught while SSL processing: "+iox.getMessage(), iox);
            while (inQueue.remove(entry)) {
              // no body
            }
          } catch (InterruptedException e) {
            logger.error("SSL processing interrupted: "+e.getMessage(), e);
            return;
          }
        }
      }
    }
    private void processPending() {
      synchronized (pending) {
        for (int i=0; i<pending.size(); i++) {
          SocketEntry entry = pending.getFirst();
          try {
            // Register the channel with the selector, indicating
            // interest in connection completion and attaching the
            // target object so that we can get the target back
            // after the key is added to the selector's
            // selected-key set
            if (entry.getSocket().isConnected()) {
              if (entry.isHandshakeFinished()) {
                entry.addRegistration(selector, SelectionKey.OP_WRITE);
              }
            }
            else {
              entry.addRegistration(selector, SelectionKey.OP_CONNECT);
            }
          }
          catch (CancelledKeyException ckex) {
            logger.warn(ckex);
            pending.remove(entry);
            try {
              entry.getSocket().getChannel().close();
              TransportStateEvent e =
                  new TransportStateEvent(TLSTM.this,
                                          entry.getPeerAddress(),
                                          TransportStateEvent.STATE_CLOSED,
                                          null);
              fireConnectionStateChanged(e);
            }
            catch (IOException ex) {
              logger.error(ex);
            }
          }
          catch (IOException iox) {
            logger.error(iox);
            pending.remove(entry);
            // Something went wrong, so close the channel and
            // record the failure
            try {
              entry.getSocket().getChannel().close();
              TransportStateEvent e =
                  new TransportStateEvent(TLSTM.this,
                                          entry.getPeerAddress(),
                                          TransportStateEvent.STATE_CLOSED,
                                          iox);
              fireConnectionStateChanged(e);
            }
            catch (IOException ex) {
              logger.error(ex);
            }
            lastError = iox;
            if (SNMP4JSettings.isForwardRuntimeExceptions()) {
              throw new RuntimeException(iox);
            }
          }
        }
      }
    }

    /**
     * If the result indicates that we have outstanding tasks to do,
     * go ahead and run them in this thread.
     * @param result
     *    the SSLEngine wrap/unwrap result.
     * @param entry
     *    the session to use.
     * @return
     *    {@code true} if processing of delegated tasks has been
     *    finished, {@code false} otherwise.
     */
    public boolean runDelegatedTasks(SSLEngineResult result, SocketEntry entry) throws IOException {
      if (logger.isDebugEnabled()) {
        logger.debug("Running delegated task on "+entry+": "+result);
      }
      SSLEngineResult.HandshakeStatus status = result.getHandshakeStatus();
      if (status == SSLEngineResult.HandshakeStatus.NEED_TASK) {
        Runnable runnable;
        while ((runnable = entry.sslEngine.getDelegatedTask()) != null) {
          logger.debug("Running delegated task...");
          runnable.run();
        }
        status = entry.sslEngine.getHandshakeStatus();
        if (status == SSLEngineResult.HandshakeStatus.NEED_TASK) {
          throw new IOException("Inconsistent Handshake status");
        }
        logger.info("Handshake status = " + status);
      }
      switch (result.getStatus()) {
        case BUFFER_UNDERFLOW:
          entry.inNetBuffer.position(entry.inNetBuffer.limit());
          entry.inNetBuffer.limit(entry.inNetBuffer.capacity());
          entry.addRegistration(selector, SelectionKey.OP_READ);
          return false;
        case CLOSED:
          return false;
      }
      switch (status) {
        case NEED_WRAP:
          outQueue.add(entry);
//          entry.addRegistration(selector, SelectionKey.OP_WRITE);
          break;
        case NEED_UNWRAP:
          if (logger.isDebugEnabled()) {
            logger.debug("NEED_UNRWAP processing with inNetBuffer=" + entry.inNetBuffer);
          }
          inQueue.add(entry);
          entry.addRegistration(selector, SelectionKey.OP_READ);
          break;
        case FINISHED:
          logger.debug("TLS handshake finished");
          entry.setHandshakeFinished(true);/*
          if (result.bytesProduced() > 0) {
            writeNetBuffer(entry, entry.getSocket().getChannel());
          }
          /*
          if (entry.isAppOutPending()) {
            writeMessage(entry, entry.getSocket().getChannel());
          }
          */
          // fall through
        case NOT_HANDSHAKING:
          if (result.bytesProduced() > 0) {
            writeNetBuffer(entry, entry.getSocket().getChannel());
          }
          return true;
	    }
      return false;
    }

    protected void connectSocketToSendMessage(Address address, byte[] message,
                                              Socket s, SocketEntry entry, Map<Address,
                                              SocketEntry> sockets) {
      SocketEntry currentSocketEntry = sockets.putIfAbsent(address, entry);
      if (currentSocketEntry != null && currentSocketEntry.getSocket().isConnected()) {
        entry = currentSocketEntry;
        if (logger.isDebugEnabled()) {
          logger.debug("Concurrent connection attempt detected, canceling this one to " + address);
        }
        entry.addMessage(message);
        try {
          s.close();
        }
        catch (IOException iox) {
          logger.error("Failed to close recently opened socket for '"+ address +"', with "+
                  iox.getMessage(), iox);
        }
        if (currentSocketEntry.getSocket().isConnected()) {
          queueNewMessage(entry);
          return;
        }
      }
      else if (currentSocketEntry != null && !currentSocketEntry.getSocket().isConnected()) {
        entry.insertMessages(currentSocketEntry.message);
        sockets.put(address, entry);
        try {
          currentSocketEntry.getSocket().close();
        } catch (IOException iox) {
          logger.error("Failed to close socket for '"+ address +"', with "+
                  iox.getMessage(), iox);
        }
      }
      queueNewMessage(entry);
      logger.debug("Trying to connect to " + address);
    }

    private void queueNewMessage(SocketEntry entry) {
      synchronized (pending) {
        pending.add(entry);
      }
      selector.wakeup();
    }

    public Throwable getLastError() {
      return lastError;
    }

    public void sendMessage(Address address, byte[] message,
                            TransportStateReference tmStateReference)
        throws IOException
    {
      Socket s = null;
      SocketEntry entry = sockets.get(address);
      if (logger.isDebugEnabled()) {
        logger.debug("Looking up connection for destination '"+address+
                     "' returned: "+entry);
        logger.debug(sockets.toString());
      }
      if (entry != null) {
        if ((tmStateReference != null) && (tmStateReference.getSessionID() != null) &&
            (!tmStateReference.getSessionID().equals(entry.getSessionID()))) {
          // session IDs do not match -> drop message
          counterSupport.fireIncrementCounter(
              new CounterEvent(this, SnmpConstants.snmpTlstmSessionNoSessions));
          throw new IOException("Session "+tmStateReference.getSessionID()+" not available");
        }
        s = entry.getSocket();
      }
      if ((s == null) || (s.isClosed()) || (!s.isConnected())) {
        if (logger.isDebugEnabled()) {
          logger.debug("Socket for address '"+address+ "' is closed, opening it...");
        }
        synchronized (pending) {
          pending.remove(entry);
        }
        SocketChannel sc;
        try {
          InetSocketAddress targetAddress =
              new InetSocketAddress(((TcpAddress)address).getInetAddress(),
                                    ((TcpAddress)address).getPort());
          if ((s == null) || (s.isClosed())) {
            // Open the channel, set it to non-blocking, initiate connect
            sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.connect(targetAddress);
            counterSupport.fireIncrementCounter(
                new CounterEvent(this, SnmpConstants.snmpTlstmSessionOpens));
          }
          else {
            sc = s.getChannel();
            sc.configureBlocking(false);
            if (!sc.isConnectionPending()) {
              sc.connect(targetAddress);
              counterSupport.fireIncrementCounter(
                  new CounterEvent(this, SnmpConstants.snmpTlstmSessionOpens));
            }
            else {
              if (matchingStateReferences(tmStateReference, entry.tmStateReference)) {
                entry.addMessage(message);
                synchronized (pending) {
                  pending.add(entry);
                }
                selector.wakeup();
                return;
              }
              else {
                logger.error("TransportStateReferences refNew="+tmStateReference+
                             ",refOld="+entry.tmStateReference+" do not match, message dropped");
                throw new IOException("Transport state reference does not match existing reference"+
                    " for this session/target");
              }
            }
          }
          s = sc.socket();
          entry = new SocketEntry((TcpAddress)address, s, true, tmStateReference);
          entry.addMessage(message);
          sockets.put(address, entry);

          synchronized (pending) {
            pending.add(entry);
          }

          selector.wakeup();
          logger.debug("Trying to connect to "+address);
        }
        catch (IOException iox) {
          logger.error(iox);
          throw iox;
        } catch (NoSuchAlgorithmException e) {
          logger.error("NoSuchAlgorithmException while sending message to "+address+": "+e.getMessage(), e);
        }
      }
      else if (matchingStateReferences(tmStateReference, entry.tmStateReference)) {
        entry.addMessage(message);
        synchronized (pending) {
          pending.addFirst(entry);
        }
        logger.debug("Waking up selector for new message");
        selector.wakeup();
      }
      else {
        logger.error("TransportStateReferences refNew="+tmStateReference+
            ",refOld="+entry.tmStateReference+" do not match, message dropped");
        throw new IOException("Transport state reference does not match existing reference"+
            " for this session/target");

      }
    }


    public void run() {
      // Here's where everything happens. The select method will
      // return when any operations registered above have occurred, the
      // thread has been interrupted, etc.
      try {
        while (!stop) {
          try {
            processQueues();
            if (selector.select() > 0) {
              if (stop) {
                break;
              }
              // Someone is ready for I/O, get the ready keys
              Set<SelectionKey> readyKeys = selector.selectedKeys();
              Iterator<SelectionKey> it = readyKeys.iterator();

              // Walk through the ready keys collection and process date requests.
              while (it.hasNext()) {
                try {
                  SocketEntry entry = null;
                  SelectionKey sk = it.next();
                  it.remove();
                  SocketChannel readChannel = null;
                  TcpAddress incomingAddress = null;
                  if (sk.isAcceptable()) {
                    logger.debug("Key is acceptable");
                    // The key indexes into the selector so you
                    // can retrieve the socket that's ready for I/O
                    ServerSocketChannel nextReady =
                        (ServerSocketChannel) sk.channel();
                    Socket s = nextReady.accept().socket();
                    readChannel = s.getChannel();
                    readChannel.configureBlocking(false);

                    incomingAddress = new TcpAddress(s.getInetAddress(),
                                                     s.getPort());
                    entry = new SocketEntry(incomingAddress, s, false, null);
                    entry.addRegistration(selector, SelectionKey.OP_READ);
                    sockets.put(incomingAddress, entry);
                    timeoutSocket(entry);
                    TransportStateEvent e =
                        new TransportStateEvent(TLSTM.this,
                                                incomingAddress,
                                                TransportStateEvent.
                                                STATE_CONNECTED,
                                                null);
                    fireConnectionStateChanged(e);
                    if (e.isCancelled()) {
                      logger.warn("Incoming connection cancelled");
                      s.close();
                      removeSocketEntry(incomingAddress);
                      readChannel = null;
                    }
                  }
                  else if (sk.isWritable()) {
                    logger.debug("Key is writable");
                    incomingAddress = writeData(sk, incomingAddress);
                  }
                  else if (sk.isReadable()) {
                    logger.debug("Key is readable");
                    readChannel = (SocketChannel) sk.channel();
                    incomingAddress =
                        new TcpAddress(readChannel.socket().getInetAddress(),
                                       readChannel.socket().getPort());
                  }
                  else if (sk.isConnectable()) {
                    logger.debug("Key is connectable");
                    connectChannel(sk, incomingAddress);
                  }

                  if (readChannel != null) {
                    logger.debug("Key is reading");
                    try {
                      readMessage(sk, readChannel, incomingAddress, entry);
                    }
                    catch (IOException iox) {
                      // IO exception -> channel closed remotely
                      logger.warn(iox);
                      iox.printStackTrace();
                      sk.cancel();
                      readChannel.close();
                      TransportStateEvent e =
                          new TransportStateEvent(TLSTM.this,
                                                  incomingAddress,
                                                  TransportStateEvent.
                                                  STATE_DISCONNECTED_REMOTELY,
                                                  iox);
                      fireConnectionStateChanged(e);
                    }
                  }
                }
                catch (CancelledKeyException ckex) {
                  if (logger.isDebugEnabled()) {
                    logger.debug("Selection key cancelled, skipping it");
                  }
                } catch (NoSuchAlgorithmException e) {
                  logger.error("NoSuchAlgorithm while reading from server socket: "+e.getMessage(), e);
                }
              }
            }
          }
          catch (NullPointerException npex) {
            // There seems to happen a NullPointerException within the select()
            npex.printStackTrace();
            logger.warn("NullPointerException within select()?");
            stop = true;
          }
          processPending();
        }
        if (ssc != null) {
          ssc.close();
        }
        if (selector != null) {
          selector.close();
        }
      }
      catch (IOException iox) {
        logger.error(iox);
        lastError = iox;
      }
      if (!stop) {
        stop = true;
        synchronized (TLSTM.this) {
          server = null;
        }
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Worker task finished: " + getClass().getName());
      }
    }

    private void connectChannel(SelectionKey sk, TcpAddress incomingAddress) {
      SocketEntry entry = (SocketEntry) sk.attachment();
      try {
        SocketChannel sc = (SocketChannel) sk.channel();
        if (!sc.isConnected()) {
          if (sc.finishConnect()) {
            sc.configureBlocking(false);
            logger.debug("Connected to " + entry.getPeerAddress());
            // make sure connection is closed if not used for timeout
            // micro seconds
            timeoutSocket(entry);
            entry.removeRegistration(selector, SelectionKey.OP_CONNECT);
            entry.addRegistration(selector, SelectionKey.OP_WRITE);
          }
          else {
            entry = null;
          }
        }
        if (entry != null) {
          Address addr = (incomingAddress == null) ?
                                      entry.getPeerAddress() : incomingAddress;
          logger.debug("Fire connected event for "+addr);
          TransportStateEvent e =
              new TransportStateEvent(TLSTM.this,
                                      addr,
                                      TransportStateEvent.
                                      STATE_CONNECTED,
                                      null);
          fireConnectionStateChanged(e);
        }
      }
      catch (IOException iox) {
        logger.warn(iox);
        sk.cancel();
        closeChannel(sk.channel());
        if (entry != null) {
          pending.remove(entry);
        }
      }
    }

    private TcpAddress writeData(SelectionKey sk, TcpAddress incomingAddress) {
      SocketEntry entry = (SocketEntry) sk.attachment();
      try {
        SocketChannel sc = (SocketChannel) sk.channel();
        incomingAddress =
            new TcpAddress(sc.socket().getInetAddress(),
                           sc.socket().getPort());
        if ((entry != null) && (!entry.hasMessage())) {
          synchronized (pending) {
            pending.remove(entry);
            entry.removeRegistration(selector, SelectionKey.OP_WRITE);
          }
        }
        if (entry != null) {
          writeMessage(entry, sc);
        }
      }
      catch (IOException iox) {
        logger.warn(iox);
        TransportStateEvent e =
            new TransportStateEvent(TLSTM.this,
                                    incomingAddress,
                                    TransportStateEvent.
                                    STATE_DISCONNECTED_REMOTELY,
                                    iox);
        fireConnectionStateChanged(e);
        // make sure channel is closed properly:
        closeChannel(sk.channel());
      }
      return incomingAddress;
    }

    private void closeChannel(SelectableChannel channel) {
      try {
        channel.close();
      }
      catch (IOException channelCloseException) {
        logger.warn(channelCloseException);
      }
    }

    private void readMessage(SelectionKey sk, SocketChannel readChannel,
                             TcpAddress incomingAddress,
                             SocketEntry session) throws IOException {
      SocketEntry entry = (SocketEntry) sk.attachment();
      if (entry == null) {
        entry = session;
      }
      if (entry == null) {
        logger.error("SocketEntry null in readMessage");
      }
      assert (entry != null);
      // note that socket has been used
      entry.used();
      ByteBuffer inNetBuffer = entry.getInNetBuffer();
      ByteBuffer inAppBuffer = entry.getInAppBuffer();
      try {
        long bytesRead = readChannel.read(inNetBuffer);
        if (logger.isDebugEnabled()) {
          logger.debug("Read " + bytesRead + " bytes from " + incomingAddress);
          logger.debug("TLS inNetBuffer: "+inNetBuffer);
        }
        if (bytesRead < 0) {
          logger.debug("Socket closed remotely");
          sk.cancel();
          readChannel.close();
          TransportStateEvent e =
              new TransportStateEvent(TLSTM.this,
                                      incomingAddress,
                                      TransportStateEvent.
                                      STATE_DISCONNECTED_REMOTELY,
                                      null);
          fireConnectionStateChanged(e);
          return;
        }
        if (bytesRead > 0) {
          SSLEngineResult result;
          synchronized (entry.inboundLock) {
            do {
              inNetBuffer.flip();
              result = entry.sslEngine.unwrap(inNetBuffer, inAppBuffer);
              adjustInNetBuffer(entry, result);
              switch (result.getStatus()) {
                case BUFFER_OVERFLOW:
                  // TODO handle overflow
                  throw new IOException("BUFFER_OVERFLOW");
              }
              if (runDelegatedTasks(result, entry)) {
                if (result.bytesProduced() > 0) {
                  entry.inAppBuffer.flip();
                  if (logger.isDebugEnabled()) {
                    logger.debug("Reading inappBuffer=" + entry.inAppBuffer);
                  }
                  // SSL session is established
                  if (entry.inAppBuffer.remaining() % tlsMaxFragmentSize == 0) {
                    if (logger.isDebugEnabled()) {
                      logger.debug("Checking PDU header for fragmented message: " + entry);
                    }
                    try {
                      BER.decodeHeader(new BERInputStream(entry.inAppBuffer.asReadOnlyBuffer()),
                              new BER.MutableByte(), true);
                    } catch (IOException iox) {
                      entry.inAppBuffer.position(entry.inAppBuffer.limit());
                      entry.inAppBuffer.limit(entry.inAppBuffer.capacity());
                      // wait to get rest of the PDU first
                      if (logger.isDebugEnabled()) {
                        logger.debug("Waiting for rest of packet because: " + iox.getMessage() +
                                ", inAppBuffer=" + entry.inAppBuffer);
                      }
                      continue;
                    }
                  }
                  entry.checkTransportStateReference();
                  dispatchMessage(incomingAddress, inAppBuffer, inAppBuffer.limit(),
                          entry.sessionID,
                          entry.tmStateReference);
                  entry.getInAppBuffer().clear();
                  break;
                } else if (entry.isAppOutPending()) {
                  writeMessage(entry, entry.getSocket().getChannel());
                }
              }
              else {
                break;
              }
            } while (inNetBuffer.position() > 0 && inNetBuffer.remaining() > 0);
          }
        }
      }
      catch (ClosedChannelException ccex) {
        sk.cancel();
        if (logger.isDebugEnabled()) {
          logger.debug("Read channel not open, no bytes read from " +
                       incomingAddress);
        }
      }
    }

    private ByteBuffer createBufferCopy(ByteBuffer buffer) {
      byte[] conInNetData = new byte[buffer.limit()];
      int buflen = buffer.limit() - buffer.remaining();
      buffer.flip();
      buffer.get(conInNetData, 0, buflen);
      ByteBuffer bufferCopy = ByteBuffer.wrap(conInNetData);
      bufferCopy.position(buflen);
      return bufferCopy;
    }

    private void dispatchMessage(TcpAddress incomingAddress,
                                 ByteBuffer byteBuffer, long bytesRead,
                                 Object sessionID,
                                 TransportStateReference tmStateReference) {
      byteBuffer.flip();
      if (logger.isDebugEnabled()) {
        logger.debug("Received message from " + incomingAddress +
                     " with length " + bytesRead + ": " +
                     new OctetString(byteBuffer.array(), 0,
                                     (int)bytesRead).toHexString());
      }
      ByteBuffer bis;
      if (isAsyncMsgProcessingSupported()) {
        byte[] bytes = new byte[(int)bytesRead];
        System.arraycopy(byteBuffer.array(), 0, bytes, 0, (int)bytesRead);
        bis = ByteBuffer.wrap(bytes);
      }
      else {
        bis = ByteBuffer.wrap(byteBuffer.array(),
                              0, (int) bytesRead);
      }
      fireProcessMessage(incomingAddress, bis,tmStateReference);
    }

    private void writeMessage(SocketEntry entry, SocketChannel sc) throws
        IOException {
      synchronized (entry.outboundLock) {
        boolean sendNextFragment = false;
        do {
          sendNextFragment = false;
          int offset = 0;
          if (entry.outAppBuffer == null) {
            byte[] message = entry.nextMessage();
            if (message != null) {
              entry.outAppBuffer = ByteBuffer.wrap(message);
              if (logger.isDebugEnabled()) {
                logger.debug("Sending message with length " +
                        message.length + " to " +
                        entry.getPeerAddress() + ": " +
                        new OctetString(message).toHexString());
              }
            } else {
              entry.removeRegistration(selector, SelectionKey.OP_WRITE);
              // Make sure that we did not clear a selection key that was concurrently
              // added:
              if (entry.hasMessage() &&
                      !entry.isRegistered(SelectionKey.OP_WRITE)) {
                entry.addRegistration(selector, SelectionKey.OP_WRITE);
                logger.debug("Waking up selector");
                selector.wakeup();
              }
              entry.addRegistration(selector, SelectionKey.OP_READ);
              return;
            }
          }
          else {
            offset = entry.outAppBuffer.position();
          }
          SSLEngineResult result;
          result = entry.sslEngine.wrap(entry.outAppBuffer, entry.outNetBuffer);
          if (result.getStatus() == SSLEngineResult.Status.OK) {
            if (result.bytesProduced() > 0) {
              writeNetBuffer(entry, sc);
            }
          } else if (runDelegatedTasks(result, entry)) {
            logger.debug("SSL session OK");
  /*
            if (entry.isAppOutPending()) {
              writeMessage(entry, entry.getSocket().getChannel());
            }
            */
          }
          if (result.bytesConsumed() >= entry.outAppBuffer.limit() - offset) {
            logger.debug("Payload sent completely");
            entry.outAppBuffer = null;
          } else if (result.bytesConsumed() > 0) {
            logger.debug("Fragment of size " + result.bytesConsumed() + " sent: " + entry);
            sendNextFragment = true;
          }
        } while (sendNextFragment);
      }
      entry.addRegistration(selector, SelectionKey.OP_READ);
    }

    public void close() {
      stop = true;
      WorkerTask st = server;
      if (st != null) {
        st.terminate();
      }
    }

    public void terminate() {
      stop = true;
      if (logger.isDebugEnabled()) {
        logger.debug("Terminated worker task: " + getClass().getName());
      }
    }

    public void join() {
      if (logger.isDebugEnabled()) {
        logger.debug("Joining worker task: " + getClass().getName());
      }
    }

    public void interrupt() {
      stop = true;
      if (logger.isDebugEnabled()) {
        logger.debug("Interrupting worker task: " + getClass().getName());
      }
      selector.wakeup();
    }
  }

  void writeNetBuffer(SocketEntry entry, SocketChannel sc) throws IOException {
    entry.outNetBuffer.flip();
    // Send SSL/TLS encoded data to peer
    while (entry.outNetBuffer.hasRemaining()) {
      logger.debug("Writing TLS outNetBuffer(PAYLOAD): "+entry.outNetBuffer);
      int num = sc.write(entry.outNetBuffer);
      logger.debug("Wrote TLS "+num+" bytes from outNetBuffer(PAYLOAD)");
      if (num == -1) {
        throw new IOException("TLS connection closed");
      }
      else if (num == 0) {
        entry.outNetBuffer.compact();
        return;
      }
    }
    entry.outNetBuffer.clear();
  }

  private boolean matchingStateReferences(TransportStateReference tmStateReferenceNew,
                                          TransportStateReference tmStateReferenceExisting) {
    if ((tmStateReferenceExisting == null) || (tmStateReferenceNew == null)) {
      logger.error("Failed to compare TransportStateReferences refNew="+tmStateReferenceNew+
                   ",refOld="+tmStateReferenceExisting);
      return false;
    }
    if ((tmStateReferenceNew.getSecurityName() == null) ||
        (tmStateReferenceExisting.getSecurityName() == null)) {
      logger.error("Could not match TransportStateReferences refNew="+tmStateReferenceNew+
                   ",refOld="+tmStateReferenceExisting);
      return false;
    }
    else if (!tmStateReferenceNew.getSecurityName().equals(tmStateReferenceExisting.getSecurityName())) {
      return false;
    }
    return true;
  }

  private SSLEngineResult sendNetMessage(SocketEntry entry) throws IOException {
    SSLEngineResult result;
    synchronized (entry.outboundLock) {
      if (!entry.outNetBuffer.hasRemaining()) {
        return null;
      }
      result = entry.sslEngine.wrap(ByteBuffer.allocate(0), entry.outNetBuffer);
      entry.outNetBuffer.flip();
      logger.debug("TLS outNetBuffer = "+entry.outNetBuffer);
      entry.socket.getChannel().write(entry.outNetBuffer);
      entry.outNetBuffer.clear();
    }
    return result;
  }

  public String getTrustStore() {
    if (trustStore == null) {
      return System.getProperty("javax.net.ssl.trustStore");
    }
    return trustStore;
  }

  public void setTrustStore(String trustStore) {
    this.trustStore = trustStore;
  }

  public String getTrustStorePassword() {
    if (trustStorePassword == null) {
      return System.getProperty("javax.net.ssl.trustStorePassword");
    }
    return trustStorePassword;
  }

  public void setTrustStorePassword(String trustStorePassword) {
    this.trustStorePassword = trustStorePassword;
  }


  interface SSLEngineConfigurator {
    /**
     * Configure the supplied SSLEngine for TLS.
     * Configuration includes enabled protocol(s),
     * cipher codes, etc.
     *
     * @param sslEngine
     *    a {@link SSLEngine} to configure.
     */
    void configure(SSLEngine sslEngine);

    /**
     * Gets the SSLContext for this SSL connection.
     * @param useClientMode
     *    {@code true} if the connection is established in client mode.
     * @param transportStateReference
     *    the transportStateReference with additional
     *    security information for the SSL connection
     *    to establish.
     * @return
     *    the SSLContext.
     */
    SSLContext getSSLContext(boolean useClientMode, TransportStateReference transportStateReference);
  }

  protected class DefaultSSLEngineConfiguration implements SSLEngineConfigurator {

    private TrustManager[] trustManagers;

    @Override
    public void configure(SSLEngine sslEngine) {
      logger.debug("Configuring SSL engine, supported protocols are " +
          Arrays.asList(sslEngine.getSupportedProtocols()) + ", supported ciphers are " +
          Arrays.asList(sslEngine.getSupportedCipherSuites())+", https defaults are "+
                        System.getProperty("https.cipherSuites"));
      String[] supportedCipherSuites = sslEngine.getEnabledCipherSuites();
      List<String> enabledCipherSuites = new ArrayList<String>(supportedCipherSuites.length);
      for (String cs : supportedCipherSuites) {
        if (!cs.contains("_anon_") && (!cs.contains("_NULL_"))) {
          enabledCipherSuites.add(cs);
        }
      }
      //enabledCipherSuites.add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256");
      sslEngine.setEnabledCipherSuites(enabledCipherSuites.toArray(new String[enabledCipherSuites.size()]));
      sslEngine.setEnabledProtocols(getTlsProtocols());
      if (!sslEngine.getUseClientMode()) {
        sslEngine.setNeedClientAuth(true);
        sslEngine.setWantClientAuth(true);
        logger.info("Need client authentication set to true");
      }
      logger.info("Configured SSL engine, enabled protocols are "+
                  Arrays.asList(sslEngine.getEnabledProtocols())+", enabled ciphers are "+
                  Arrays.asList(sslEngine.getEnabledCipherSuites()));
    }

    @Override
    public SSLContext getSSLContext(boolean useClientMode, TransportStateReference transportStateReference) {
      try {
        String protocol = DEFAULT_TLSTM_PROTOCOLS;
        if ((getTlsProtocols() != null) && (getTlsProtocols().length > 0)) {
          protocol = getTlsProtocols()[0];

        }
        SSLContext sslContext = SSLContext.getInstance(protocol);
        TrustManagerFactory tmf =
            TrustManagerFactory.getInstance("SunPKIX");
        // use default keystore
        try {
          FileInputStream fisKeyStore = new FileInputStream(getKeyStore());
          FileInputStream fisTrustStore = new FileInputStream(getTrustStore());
          KeyStore ks = KeyStore.getInstance("JKS");
          ks.load(fisKeyStore, (getKeyStorePassword() != null) ? getKeyStorePassword().toCharArray() : null);
          if (logger.isInfoEnabled()) {
            logger.info("KeyStore '"+getKeyStore()+"' contains: "+Collections.list(ks.aliases()));
          }

          filterCertificates(ks, transportStateReference);

          // Set up key manager factory to use our key store
          KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
          kmf.init(ks, (getKeyStorePassword()  != null) ? getKeyStorePassword().toCharArray() : null);

          KeyStore ts = KeyStore.getInstance("JKS");
          ts.load(fisTrustStore, (getTrustStorePassword() != null) ? getTrustStorePassword().toCharArray() : null);
          if (logger.isInfoEnabled()) {
            logger.info("TrustStore '" + trustStore + "' contains: " + Collections.list(ts.aliases()));
          }
          tmf.init(ts);
          trustManagers = tmf.getTrustManagers();
          if (logger.isDebugEnabled()) {
            logger.debug("SSL context initializing with TrustManagers: " + Arrays.asList(trustManagers) +
                         " and factory "+trustManagerFactory.getClass().getName());
          }
          sslContext.init(kmf.getKeyManagers(),
              new TrustManager[]{ trustManagerFactory.create((X509TrustManager) trustManagers[0],
                                    useClientMode, transportStateReference)},
              null);
          return sslContext;
        } catch (KeyStoreException e) {
          logger.error("Failed to initialize SSLContext because of a KeyStoreException: " + e.getMessage(), e);
        } catch (KeyManagementException e) {
          logger.error("Failed to initialize SSLContext because of a KeyManagementException: " + e.getMessage(), e);
        } catch (UnrecoverableKeyException e) {
          logger.error("Failed to initialize SSLContext because of an UnrecoverableKeyException: " + e.getMessage(), e);
        } catch (CertificateException e) {
          logger.error("Failed to initialize SSLContext because of a CertificateException: " + e.getMessage(), e);
        } catch (FileNotFoundException e) {
          logger.error("Failed to initialize SSLContext because of a FileNotFoundException: " + e.getMessage(), e);
        } catch (IOException e) {
          logger.error("Failed to initialize SSLContext because of an IOException: " + e.getMessage(), e);
        }
      } catch (NoSuchAlgorithmException e) {
        logger.error("Failed to initialize SSLContext because of an NoSuchAlgorithmException: " + e.getMessage(), e);
      }
      return null;
    }

    private void filterCertificates(KeyStore ks, TransportStateReference transportStateReference) {
      String localCertAlias = localCertificateAlias;
      if ((securityCallback != null) && (transportStateReference != null)) {
        localCertAlias = securityCallback.getLocalCertificateAlias(transportStateReference.getAddress());
        if (localCertAlias == null) {
          localCertAlias = localCertificateAlias;
        }
      }
      if (localCertAlias != null) {
        try {
          java.security.cert.Certificate[] chain = ks.getCertificateChain(localCertAlias);
          if (chain == null) {
            logger.warn("Local certificate with alias '"+localCertAlias+"' not found. Known aliases are: "+
                Collections.list(ks.aliases()));
          }
          else {
            List<String> chainAliases = new ArrayList<String>(chain.length);
            for (java.security.cert.Certificate certificate : chain) {
              String alias = ks.getCertificateAlias(certificate);
              if (alias != null) {
                chainAliases.add(alias);
              }
            }
            // now delete all others from key store
            for (String alias : Collections.list(ks.aliases())) {
              if (!chainAliases.contains(alias)) {
                ks.deleteEntry(alias);
              }
            }
          }
        } catch (KeyStoreException e) {
          logger.error("Failed to get certificate chain for alias "+
              localCertAlias+": "+e.getMessage(),e);
        }
      }
    }

  }

  private void adjustInNetBuffer(SocketEntry entry, SSLEngineResult result) {
    if (result.getStatus() == SSLEngineResult.Status.OK) {
      if (result.bytesConsumed() == entry.inNetBuffer.limit()) {
        entry.inNetBuffer.clear();
      } else if (result.bytesConsumed() > 0) {
        entry.inNetBuffer.compact();
      }
    }
  }

  public interface TLSTMTrustManagerFactory {
    X509TrustManager create(X509TrustManager trustManager, boolean useClientMode,
                            TransportStateReference tmStateReference);
  }

  private class DefaultTLSTMTrustManagerFactory implements TLSTMTrustManagerFactory {
    public X509TrustManager create(X509TrustManager trustManager, boolean useClientMode,
                                 TransportStateReference tmStateReference) {
      return new TlsTrustManager(trustManager, useClientMode, tmStateReference, counterSupport, securityCallback);
    }
  }
}
