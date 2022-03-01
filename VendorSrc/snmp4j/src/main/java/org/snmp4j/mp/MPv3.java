/*_############################################################################
  _## 
  _##  SNMP4J - MPv3.java  
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
package org.snmp4j.mp;

import java.io.*;
import java.lang.ref.WeakReference;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

import org.snmp4j.*;
import org.snmp4j.asn1.*;
import org.snmp4j.event.*;
import org.snmp4j.log.*;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.util.PDUFactory;

/**
 * The {@code MPv3} is the message processing model for SNMPv3.
 *
 * @author Frank Fock
 * @version 1.9.2
 */
public class MPv3 implements MessageProcessingModel, EngineIdCacheSize {

    public static final int ID = MessageProcessingModel.MPv3;
    public static final int MPv3_REPORTABLE_FLAG = 4;
    public static final int MAX_MESSAGE_ID = 2147483647;

    private static final int INT_LOW_16BIT_MASK = 0x0000FFFF;

    /**
     * Local engine ID constant for context engineID discovery as defined by RFC 5343.
     */
    public static final OctetString LOCAL_ENGINE_ID =
            OctetString.fromHexString("80:00:00:00:06");

    public static final int MAXLEN_ENGINE_ID = 32;
    public static final int MINLEN_ENGINE_ID = 5;

    private static final int MAX_HEADER_PAYLOAD_LENGTH =
            // length of msgFlags
            new OctetString("\0").getBERLength() +
                    // length of msgID, msgMaxSize, securityModel
                    3 * new Integer32(Integer.MAX_VALUE).getBERLength();

    private static final int MAX_HEADER_LENGTH =
            MAX_HEADER_PAYLOAD_LENGTH +
                    BER.getBERLengthOfLength(MAX_HEADER_PAYLOAD_LENGTH) + 1;

    private SecurityProtocols securityProtocols;

    private static final LogAdapter logger = LogFactory.getLogger(MPv3.class);
    private SecurityModels securityModels;

    private Cache cache;
    private Map<Address, OctetString> engineIDs;
    private int maxEngineIdCacheSize = SNMP4JSettings.getMaxEngineIdCacheSize();
    private byte[] localEngineID;

    private int currentMsgID = new Random().nextInt(MAX_MESSAGE_ID);

    private CounterSupport counterSupport;
    protected EngineIdCacheFactory engineIdCacheFactory = new LimitedCapacityEngineIdCacheFactory();

    transient List<SnmpEngineListener> snmpEngineListeners;

    protected PDUFactory incomingPDUFactory = new PDUFactory() {
        @Override
        public PDU createPDU(Target target) {
            return new ScopedPDU();
        }

        @Override
        public PDU createPDU(MessageProcessingModel messageProcessingModel) {
            return new ScopedPDU();
        }
    };

    /**
     * Creates a MPv3 with a default local engine ID.
     */
    public MPv3() {
        this(createLocalEngineID(), null);
    }

    /**
     * Creates a MPv3 with a supplied local engine ID.
     *
     * @param localEngineID
     *         the local engine ID. Its length must be greater or equal than 5 and less or equal than 32.
     */
    public MPv3(byte[] localEngineID) {
        this(localEngineID, null);
        setLocalEngineID(localEngineID);
    }

    /**
     * Creates a MPv3 with a supplied local engine ID and {@link PDUFactory} for incoming messages.
     *
     * @param localEngineID
     *         the local engine ID. Its length must be greater or equal than 5 and less or equal than 32.
     * @param incomingPDUFactory
     *         a {@link PDUFactory}. If {@code null} the default factory will be used which creates {@link ScopedPDU}
     *         instances.
     *
     * @since 1.9.1
     */
    public MPv3(byte[] localEngineID, PDUFactory incomingPDUFactory) {
        this(localEngineID, incomingPDUFactory, SecurityProtocols.getInstance(),
                SecurityModels.getInstance(), CounterSupport.getInstance());
    }

    /**
     * This is a convenience constructor which can be used to create a MPv3 which is bound to a specific USM instance. A
     * dedicated USM instance per MPv3 is necessary if multiple {@link Snmp} instances are used within a VM.
     *
     * @param usm
     *         an USM instance.
     *
     * @since 1.10
     */
    public MPv3(USM usm) {
        this(usm.getLocalEngineID().getValue(), null,
                SecurityProtocols.getInstance(),
                SecurityModels.getCollection(new SecurityModel[]{usm}),
                CounterSupport.getInstance());
    }

    /**
     * Creates a fully qualified MPv3 instance with custom security protocols and models as well as a custom counter
     * support. The current message ID is set using the USM engine boots counter (if available) according to the RFC3412
     * §6.2.
     *
     * @param localEngineID
     *         the local engine ID. Its length must be greater or equal than 5 and less or equal than 32.
     * @param incomingPDUFactory
     *         a {@link PDUFactory}. If {@code null} the default factory will be used which creates {@link ScopedPDU}
     *         instances.
     * @param secProtocols
     *         the SecurityProtocols instance to use when looking up a security protocol. To get a default instance, use
     *         {@link SecurityProtocols#getInstance()}.
     * @param secModels
     *         the SecurityModels instance to use when looking up a security model. If you use more than one USM
     *         instance, you need to create a SecurityProtocols instance (container) for each such USM instance (and
     *         MPv3 combination). To get a default instance, use {@link SecurityProtocols#getInstance()}.
     * @param counterSupport
     *         The CounterSupport instance to be used to count events created by this MPv3 instance. To get a default
     *         instance, use {@link CounterSupport#getInstance()}.
     *
     * @since 1.10
     */
    public MPv3(byte[] localEngineID, PDUFactory incomingPDUFactory,
                SecurityProtocols secProtocols,
                SecurityModels secModels,
                CounterSupport counterSupport) {
        if (incomingPDUFactory != null) {
            this.incomingPDUFactory = incomingPDUFactory;
        }
        engineIDs = engineIdCacheFactory.createEngineIdMap(this);
        cache = new Cache();
        if (secProtocols == null) {
            throw new NullPointerException();
        }
        securityProtocols = secProtocols;
        if (secModels == null) {
            throw new NullPointerException();
        }
        securityModels = secModels;
        if (counterSupport == null) {
            throw new NullPointerException();
        }
        this.counterSupport = counterSupport;
        setLocalEngineID(localEngineID);
        SecurityModel usm = secModels.getSecurityModel(new Integer32(USM.SECURITY_MODEL_USM));
        if (usm instanceof USM) {
            setCurrentMsgID(randomMsgID(((USM) usm).getEngineBoots()));
        }
    }

    /**
     * Returns the enging ID factory that was used to create the current engine ID cache.
     *
     * @return a {@link org.snmp4j.mp.MPv3.EngineIdCacheFactory} implementation.
     * @since 2.3.4
     */
    public EngineIdCacheFactory getEngineIdCacheFactory() {
        return engineIdCacheFactory;
    }

    /**
     * Sets the engine ID cache factory and resets (clears) the current cache. The maximum size of the cache is
     * determined using {@link #getMaxEngineIdCacheSize()} as this implements the {@link EngineIdCacheSize} interface.
     * By default the maximum cache size {@link SNMP4JSettings#getMaxEngineIdCacheSize()} is used.
     *
     * @param engineIdCacheFactory
     *         a {@link org.snmp4j.mp.MPv3.EngineIdCacheFactory} implementation that is used to create a new cache.
     *
     * @since 2.3.4
     */
    public void setEngineIdCacheFactory(EngineIdCacheFactory engineIdCacheFactory) {
        engineIDs = engineIdCacheFactory.createEngineIdMap(this);
        this.engineIdCacheFactory = engineIdCacheFactory;
    }

    @Override
    public int getMaxEngineIdCacheSize() {
        return maxEngineIdCacheSize;
    }

    /**
     * Sets the upper limit for the engine ID cache. Modifying this value will not immediately take effect on the cache
     * size.
     *
     * @param maxEngineIdCacheSize
     *         the maximum number of engine IDs hold in the internal cache. If more than those engine IDs are used by
     *         the MPv3, the eldest engine ID is removed from the cache. Eldest means the eldest initial use. A
     *         different cache can be implemented by using a custom {@link EngineIdCacheFactory} and setting it after
     *         calling this constructor.
     */
    public void setMaxEngineIdCacheSize(int maxEngineIdCacheSize) {
        this.maxEngineIdCacheSize = maxEngineIdCacheSize;
    }

    /**
     * Creates a local engine ID based on the local IP address and additional four random bytes. WARNING: Do not use
     * this engine ID generator for a command responder (agent) if you DO NOT persistently save the one time generated
     * engine ID for subsequent use when the agent is restarted.
     *
     * @return a new local engine ID with a random part to avoid engine ID clashes for multiple command generators on
     * the same system.
     */
    public static byte[] createLocalEngineID() {
        int enterpriseID = SNMP4JSettings.getEnterpriseID();
        byte[] engineID = new byte[5];
        engineID[0] = (byte) (0x80 | ((enterpriseID >> 24) & 0xFF));
        engineID[1] = (byte) ((enterpriseID >> 16) & 0xFF);
        engineID[2] = (byte) ((enterpriseID >> 8) & 0xFF);
        engineID[3] = (byte) (enterpriseID & 0xFF);
        engineID[4] = 2;
        OctetString os = new OctetString();
        try {
            byte[] b = InetAddress.getLocalHost().getAddress();
            if (b.length == 4) {
                engineID[4] = 1;
            }
            os.setValue(b);
        } catch (UnknownHostException ex) {
            logger.debug("Local host cannot be determined for creation of local engine ID");
            engineID[4] = 4;
            os.setValue("SNMP4J".getBytes());
        }
        OctetString ownEngineID = new OctetString(engineID);
        Random random = new Random(System.nanoTime());
        byte[] fourBytes = new byte[4];
        random.nextBytes(fourBytes);
        ownEngineID.append(os);
        ownEngineID.append(fourBytes);
        return ownEngineID.getValue();
    }

    /**
     * Creates a local engine ID based on the ID string supplied
     *
     * @param id
     *         an ID string.
     *
     * @return a new local engine ID.
     */
    public static byte[] createLocalEngineID(OctetString id) {
        int enterpriseID = SNMP4JSettings.getEnterpriseID();
        byte[] engineID = new byte[5];
        engineID[0] = (byte) (0x80 | ((enterpriseID >> 24) & 0xFF));
        engineID[1] = (byte) ((enterpriseID >> 16) & 0xFF);
        engineID[2] = (byte) ((enterpriseID >> 8) & 0xFF);
        engineID[3] = (byte) (enterpriseID & 0xFF);
        engineID[4] = 4;
        OctetString ownEngineID = new OctetString(engineID);
        ownEngineID.append(id);
        return ownEngineID.getValue();
    }

    /**
     * Sets the local engine ID. This value must not be changed after message processing has been started. Note: When
     * setting the local engine ID, the engine boots counter should be known at the same time. Thus, please also call
     * <pre>
     *   setCurrentMsgID(randomMsgID(engineBoots));
     * </pre>
     * before starting the message processing.
     *
     * @param engineID
     *         the local engine ID. Its length must be greater or equal than 5 and less or equal than 32.
     */
    public void setLocalEngineID(byte[] engineID) {
        if ((engineID == null) ||
                (engineID.length < MINLEN_ENGINE_ID) ||
                (engineID.length > MAXLEN_ENGINE_ID)) {
            throw new IllegalArgumentException("Illegal (local) engine ID");
        }
        this.localEngineID = engineID;
    }

    /**
     * Creates a random message ID according to the method proposed by RFC3412: "Values for msgID SHOULD be generated in
     * a manner that avoids re-use of any outstanding values.  Doing so provides protection against some replay attacks.
     *  One possible implementation strategy would be to use the low-order bits of snmpEngineBoots [RFC3411] as the
     * high-order portion of the msgID value and a monotonically increasing integer for the low-order portion of
     * msgID."
     *
     * @param engineBoots
     *         the number of boot operations already occurred for this SNMP entity.
     *
     * @return the new random message ID.
     */
    public static int randomMsgID(int engineBoots) {
        return (new Random().nextInt(MAX_MESSAGE_ID) & INT_LOW_16BIT_MASK) | ((engineBoots & INT_LOW_16BIT_MASK) << 16);
    }

    /**
     * Gets a copy of the local engine ID.
     *
     * @return a byte array containing the local engine ID.
     */
    public byte[] getLocalEngineID() {
        byte[] retval = new byte[localEngineID.length];
        System.arraycopy(localEngineID, 0, retval, 0, localEngineID.length);
        return retval;
    }

    /**
     * Creates and initializes the default security protocols.
     *
     * @see SecurityProtocols#addDefaultProtocols()
     */
    public void initDefaults() {
        securityProtocols.addDefaultProtocols();
    }

    /**
     * Gets an authentication protocol for the supplied ID.
     *
     * @param id
     *         an authentication protocol OID.
     *
     * @return an {@link AuthenticationProtocol} instance if the supplied ID is supported, otherwise {@code null} is
     * returned.
     */
    public AuthenticationProtocol getAuthProtocol(OID id) {
        return securityProtocols.getAuthenticationProtocol(id);
    }

    /**
     * Gets an privacy protocol for the supplied ID.
     *
     * @param id
     *         an privacy protocol OID.
     *
     * @return an {@link PrivacyProtocol} instance if the supplied ID is supported, otherwise {@code null} is returned.
     */
    public PrivacyProtocol getPrivProtocol(OID id) {
        return securityProtocols.getPrivacyProtocol(id);
    }

    /**
     * Gets the security model for the supplied ID.
     *
     * @param id
     *         a security model ID.
     *
     * @return a {@link SecurityModel} instance if the supplied ID is supported, otherwise {@code null} is returned.
     */
    public SecurityModel getSecurityModel(int id) {
        return securityModels.getSecurityModel(new Integer32(id));
    }

    public int getID() {
        return ID;
    }

    public boolean isProtocolVersionSupported(int version) {
        return (version == SnmpConstants.version3);
    }

    /**
     * Adds an engine ID (other than the local engine ID) to the internal storage.
     *
     * @param address
     *         the {@link Address} of the remote SNMP engine.
     * @param engineID
     *         the engine ID of the remote SNMP engine.
     *
     * @return {@code true} if the engine ID has been added, <code>false</code> otherwise (if the supplied {@code
     * engineID} equals the local one).
     */
    public boolean addEngineID(Address address, OctetString engineID) {
        if (!Arrays.equals(this.localEngineID, engineID.getValue())) {
            try {
                OctetString previousEngineID = addEngineIdToCache(address, engineID);
                if ((snmpEngineListeners != null) && ((previousEngineID == null) || (!previousEngineID.equals(engineID)))) {
                    fireEngineChanged(new SnmpEngineEvent(this,
                            SnmpEngineEvent.ADDED_ENGINE_ID,
                            engineID, address));
                }
            } catch (IllegalArgumentException iaex) {
                fireEngineChanged(new SnmpEngineEvent(this,
                        SnmpEngineEvent.IGNORED_ENGINE_ID,
                        engineID, address));
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Put the engine ID for the given address into the internal cache. If the cache reached its limit,
     *
     * @param address
     *         the address of the engine ID
     * @param engineID
     *         the engine ID to cache.
     *
     * @return the previous engine ID or {@code null} if there was no engine ID cached for the given address.
     * @throws IllegalArgumentException
     *         when the local maximum cache size is exceeded.
     * @since 2.3.4
     */
    protected OctetString addEngineIdToCache(Address address, OctetString engineID) {
        if ((maxEngineIdCacheSize > 0) && (engineIDs.size() >= maxEngineIdCacheSize)) {
            if (engineIDs.containsKey(address)) {
                return engineIDs.put(address, engineID);
            }
            String msg = "MPv3: Failed to add engineID '" + engineID.toHexString() + "' for address '" + address +
                    "' to local cache because its size limit of " + maxEngineIdCacheSize + "has been reached";
            logger.warn(msg);
            throw new IllegalArgumentException(msg);
        } else {
            return engineIDs.put(address, engineID);
        }
    }

    /**
     * Gets the engine ID associated with the supplied address from the local storage and fires the corresponding {@link
     * SnmpEngineEvent}.
     *
     * @param address
     *         the {@link Address} of the remote SNMP engine.
     *
     * @return the engine ID of the remote SNMP engine or {@code null} if there is no entry for {@code address} in the
     * local storage.
     */
    public OctetString getEngineID(Address address) {
        return engineIDs.get(address);
    }

    /**
     * Removes an engine ID association from the local storage and fires the corresponding {@link SnmpEngineEvent}.
     *
     * @param address
     *         the {@link Address} of the remote SNMP engine for whose engine ID is to be removed.
     *
     * @return the removed engine ID of the remote SNMP engine or {@code null} if there is no entry for {@code address}
     * in the local storage.
     */
    public OctetString removeEngineID(Address address) {
        OctetString engineID = engineIDs.remove(address);
        if ((engineID != null) && (snmpEngineListeners != null)) {
            fireEngineChanged(new SnmpEngineEvent(this, SnmpEngineEvent.REMOVED_ENGINE_ID, engineID, address));
        }
        return engineID;
    }


    /**
     * The {@code CacheEntry} class holds state reference information for the MPv3 message processing model for a single
     * message.
     *
     * @author Frank Fock
     * @version 1.0
     */
    protected static class CacheEntry<A extends Address> extends StateReference<A> {
        private static final long serialVersionUID = 8698046643337640719L;
        int msgID;
        long transactionID;
        byte[] secEngineID;
        SecurityModel secModel;
        byte[] secName;
        int secLevel;
        byte[] contextEngineID;
        byte[] contextName;
        SecurityStateReference secStateReference;
        int errorCode;

        public CacheEntry(int msgID,
                          long reqID,
                          byte[] secEngineID,
                          SecurityModel secModel,
                          byte[] secName,
                          int secLevel,
                          byte[] contextEngineID,
                          byte[] contextName,
                          SecurityStateReference secStateReference,
                          int errorCode) {
            this.msgID = msgID;
            this.transactionID = reqID;
            this.secEngineID = secEngineID;
            this.secModel = secModel;
            this.secName = secName;
            this.secLevel = secLevel;
            this.contextEngineID = contextEngineID;
            this.contextName = contextName;
            this.secStateReference = secStateReference;
            this.errorCode = errorCode;
        }
    }

    /**
     * The {@code Cache} stores state reference information for the MPv3.
     *
     * @author Frank Fock
     * @version 1.0
     */
    protected static class Cache {

        private Map<PduHandle, StateReference<?>> entries = new WeakHashMap<>(25);
        private Map<MessageID, WeakReference<PduHandle>> msgIdToPduHandleMapping = new WeakHashMap<>(25);

        /**
         * Adds a <code>StateReference</code> to the cache. The <code>PduHandle</code> of the supplied entry will be set
         * to
         * <code>null</code> when the new entry is already part of the cache, because the
         * cache uses a <code>WeakHashMap</code> internally which uses the
         * <code>PduHandle</code> as key. If the new entry equals an existing entry
         * except of the message ID then the new message ID will be added to the existing entry.
         *
         * @param entry
         *         the state reference to add.
         *
         * @return {@link SnmpConstants#SNMP_MP_DOUBLED_MESSAGE} if the entry already exists and {@link
         * SnmpConstants#SNMP_MP_OK} on success.
         */
        public synchronized int addEntry(StateReference<?> entry) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding cache entry: " + entry);
            }
            StateReference<?> existing = entries.get(entry.getPduHandle());
            if (existing != null) {
                // reassign handle for comparison:
                existing.setPduHandle(entry.getPduHandle());
                if (existing.equals(entry)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Doubled message: " + entry);
                    }
                    // clear it again to remove strong self-reference
                    existing.setPduHandle(null);
                    return SnmpConstants.SNMP_MP_DOUBLED_MESSAGE;
                } else if (existing.equalsExceptMsgID(entry)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding previous message IDs " + existing.getMessageIDs() + " to new entry " + entry);
                    }
                    entry.addMessageIDs(existing.getMessageIDs());
                } else if (logger.isDebugEnabled()) {
                    logger.debug("New entry does not match existing, although request ID is the same " + entry + " != " + existing);
                }
                // clear it again to remove strong self-reference
                existing.setPduHandle(null);
            }
            // add it
            PduHandle key = entry.getPduHandle();
            // because we are using a weak hash map for the cache, we need to null out
            // our key from the entry.
            entry.setPduHandle(null);
            entries.put(key, entry);
            WeakReference<PduHandle> pduHandleReference = new WeakReference<>(key);
            msgIdToPduHandleMapping.put(entry.getMsgID(), pduHandleReference);
            if (entry.getMessageIDs() != null) {
                for (MessageID id: entry.getMessageIDs()) {
                    msgIdToPduHandleMapping.put(id, pduHandleReference);
                }
            }
            return SnmpConstants.SNMP_MP_OK;
        }

        /**
         * Delete the cache entry with the supplied {@link PduHandle}.
         *
         * @param pduHandle
         *         a pduHandle.
         *
         * @return {@code true} if an entry has been deleted, {@code false} otherwise.
         */
        public synchronized boolean deleteEntry(PduHandle pduHandle) {
            StateReference<?> e = entries.remove(pduHandle);
            if (e != null) {
                msgIdToPduHandleMapping.remove(e.getMsgID());
                if (e.getMessageIDs() != null) {
                    for (MessageID messageID : e.getMessageIDs()) {
                        msgIdToPduHandleMapping.remove(messageID);
                    }
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Removed cache entry: " + e);
                }
            }
            return (e != null);
        }

        /**
         * Pop the cache entry with the supplied ID from the cache.
         *
         * @param messageID
         *         a message ID.
         *
         * @return a {@link CacheEntry} instance with the given message ID or {@code null} if such an entry cannot be
         * found. If a cache entry is returned, the same is removed from the cache.
         */
        public synchronized StateReference<?> popEntry(MessageID messageID) {
            WeakReference<PduHandle> pduHandleReference = msgIdToPduHandleMapping.get(messageID);
            if (pduHandleReference != null) {
                PduHandle pduHandle = pduHandleReference.get();
                if (pduHandle != null) {
                    StateReference<?> e = entries.get(pduHandle);
                    if ((e != null) && (e.isMatchingMessageID(messageID))) {
                        deleteEntry(pduHandle);
                        entries.remove(pduHandle);
                        e.setPduHandle(pduHandle);
                        return e;
                    }
                }
            }
            return null;
        }
    }

    /**
     * The {@code HeaderData} represents the message header information of SNMPv3 message.
     *
     * @author Frank Fock
     * @version 1.0
     */
    public static class HeaderData
            implements BERSerializable {

        public static final byte FLAG_AUTH = 0x01;
        public static final byte FLAG_PRIV = 0x02;

        Integer32 msgID = new Integer32(0);
        Integer32 msgMaxSize = new Integer32(Integer.MAX_VALUE);
        OctetString msgFlags = new OctetString(new byte[1]);
        Integer32 securityModel = new Integer32(SecurityModel.SECURITY_MODEL_ANY);

        public void setMsgID(int msgID) {
            this.msgID.setValue(msgID);
        }

        public int getMsgID() {
            return msgID.getValue();
        }

        public void setMsgMaxSize(int msgMaxSize) {
            this.msgMaxSize.setValue(msgMaxSize);
        }

        public int getMsgMaxSize() {
            return msgMaxSize.getValue();
        }

        public void setMsgFlags(int flags) {
            this.msgFlags.getValue()[0] = (byte) flags;
        }

        public int getMsgFlags() {
            return msgFlags.getValue()[0] & 0xFF;
        }

        public void setSecurityModel(int model) {
            securityModel.setValue(model);
        }

        public int getSecurityModel() {
            return securityModel.getValue();
        }

        public int getBERPayloadLength() {
            int length = msgID.getBERLength();
            length += msgMaxSize.getBERLength();
            length += msgFlags.getBERLength();
            length += securityModel.getBERLength();
            return length;
        }

        public int getBERLength() {
            int length = getBERPayloadLength();
            length += BER.getBERLengthOfLength(length) + 1;
            return length;
        }

        public void decodeBER(BERInputStream message) throws IOException {
            BER.MutableByte type = new BER.MutableByte();
            int length = BER.decodeHeader(message, type);
            if (type.getValue() != BER.SEQUENCE) {
                throw new IOException("Unexpected sequence header type: " +
                        type.getValue());
            }
            long startPos = message.getPosition();
            msgID.decodeBER(message);
            msgMaxSize.decodeBER(message);
            if (msgMaxSize.getValue() < 484) {
                throw new IOException("Invalid msgMaxSize: " + msgMaxSize);
            }
            msgFlags.decodeBER(message);
            if (msgFlags.length() != 1) {
                throw new IOException("Message flags length != 1: " + msgFlags.length());
            }
            securityModel.decodeBER(message);
            if (logger.isDebugEnabled()) {
                logger.debug("SNMPv3 header decoded: msgId=" + msgID +
                        ", msgMaxSize=" + msgMaxSize +
                        ", msgFlags=" + msgFlags.toHexString() +
                        ", secModel=" + securityModel);
            }
            BER.checkSequenceLength(length, (int) (message.getPosition() - startPos),this);
        }

        public void encodeBER(OutputStream outputStream) throws IOException {
            BER.encodeHeader(outputStream, BER.SEQUENCE, getBERPayloadLength());
            msgID.encodeBER(outputStream);
            msgMaxSize.encodeBER(outputStream);
            msgFlags.encodeBER(outputStream);
            securityModel.encodeBER(outputStream);
        }
    }

    /**
     * Gets unique message ID.
     *
     * @return a message ID greater or equal to one and less or equal {@link #MAX_MESSAGE_ID}.
     */
    public synchronized int getNextMessageID() {
        if (currentMsgID >= MAX_MESSAGE_ID) {
            currentMsgID = 1;
        }
        return currentMsgID++;
    }

    /**
     * Gets the security protocols supported by this {@link MPv3}.
     *
     * @return return a {@link SecurityProtocols}.
     */
    public SecurityProtocols getSecurityProtocols() {
        return securityProtocols;
    }

    /**
     * Sets the security protocols for this {@link MPv3}.
     *
     * @param securityProtocols
     *         SecurityProtocols
     */
    public void setSecurityProtocols(SecurityProtocols securityProtocols) {
        this.securityProtocols = securityProtocols;
    }

    public void releaseStateReference(PduHandle pduHandle) {
        cache.deleteEntry(pduHandle);
    }

    public int prepareOutgoingMessage(Address transportAddress, int maxMessageSize,
                                      int messageProcessingModel, int securityModel,
                                      byte[] securityName, int securityLevel, PDU pdu,
                                      boolean expectResponse, PduHandle sendPduHandle,
                                      Address destTransportAddress,
                                      BEROutputStream outgoingMessage,
                                      TransportStateReference tmStateReference) throws IOException {
        if (!(pdu instanceof ScopedPDU)) {
            throw new IllegalArgumentException(
                    "MPv3 only accepts ScopedPDU instances as pdu parameter");
        }
        ScopedPDU scopedPDU = (ScopedPDU) pdu;
        SecurityModel secModel =
                securityModels.getSecurityModel(new Integer32(securityModel));
        if (secModel == null) {
            return SnmpConstants.SNMP_MP_UNSUPPORTED_SECURITY_MODEL;
        }
        // lookup engine ID
        byte[] secEngineID;
        if (secModel.hasAuthoritativeEngineID()) {
            OctetString securityEngineID = engineIDs.get(transportAddress);
            if (securityEngineID != null) {
                secEngineID = securityEngineID.getValue();
                if (scopedPDU.getContextEngineID().length() == 0) {
                    switch (pdu.getType()) {
                        case PDU.NOTIFICATION:
                        case PDU.INFORM: {
                            OctetString localEngineID = new OctetString(getLocalEngineID());
                            if (logger.isDebugEnabled()) {
                                logger.debug("Context engine ID of scoped PDU is empty! Setting it to local engine ID: " +
                                        localEngineID.toHexString());
                            }
                            scopedPDU.setContextEngineID(localEngineID);
                            break;
                        }
                        default:
                            if (logger.isDebugEnabled()) {
                                logger.debug("Context engine ID of scoped PDU is empty! Setting it to authoritative engine ID: " +
                                        securityEngineID.toHexString());
                            }
                            scopedPDU.setContextEngineID(new OctetString(secEngineID));
                    }
                }
            } else {
                secEngineID = new byte[0];
            }
        } else {
            secEngineID = new byte[0];
        }
        // determine request type
        if (pdu.isConfirmedPdu()) {
            if (secEngineID.length == 0) {
                if (secModel.supportsEngineIdDiscovery()) {
                    securityLevel = SecurityLevel.NOAUTH_NOPRIV;
                    // do not send any management information
                    scopedPDU = (ScopedPDU) scopedPDU.clone();
                    scopedPDU.clear();
                } else if ((scopedPDU.getContextEngineID() == null) ||
                        (scopedPDU.getContextEngineID().length() == 0)) {
                    logger.warn("ScopedPDU with empty context engine ID");
                } else if (!LOCAL_ENGINE_ID.equals(scopedPDU.getContextEngineID()) &&
                        getEngineID(transportAddress) == null) {
                    // Learn context engine ID
                    addEngineID(transportAddress, scopedPDU.getContextEngineID());
                }
            }
        } else {
            if (scopedPDU.getContextEngineID().length() == 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Context engine ID of unconfirmed scoped PDU is empty! " +
                            "Setting it to local engine ID");
                }
                scopedPDU.setContextEngineID(new OctetString(localEngineID));
            }
        }

        // get length of scoped PDU
        int scopedPDULength = scopedPDU.getBERLength();
        BEROutputStream scopedPdu =
                new BEROutputStream(ByteBuffer.allocate(scopedPDULength));

        scopedPDU.encodeBER(scopedPdu);

        HeaderData headerData = new HeaderData();
        int flags = 0;
        switch (securityLevel) {
            case SecurityLevel.NOAUTH_NOPRIV:
                flags = 0;
                break;
            case SecurityLevel.AUTH_NOPRIV:
                flags = 1;
                break;
            case SecurityLevel.AUTH_PRIV:
                flags = 3;
                break;
        }
        if (scopedPDU.isConfirmedPdu()) {
            flags |= MPv3_REPORTABLE_FLAG;
        } else {
            secEngineID = localEngineID;
        }

        int msgID = getNextMessageID();
        headerData.setMsgFlags(flags);
        headerData.setMsgID(msgID);
        headerData.setMsgMaxSize(maxMessageSize);
        headerData.setSecurityModel(securityModel);

        ByteBuffer globalDataBuffer =
                ByteBuffer.allocate(headerData.getBERLength());
        BEROutputStream globalDataOutputStream =
                new BEROutputStream(globalDataBuffer);
        headerData.encodeBER(globalDataOutputStream);

        BERInputStream scopedPDUInput = new BERInputStream(scopedPdu.rewind());

        // output data
        SecurityParameters securityParameters =
                secModel.newSecurityParametersInstance();

        int status =
                secModel.generateRequestMessage(messageProcessingModel,
                        globalDataBuffer.array(),
                        maxMessageSize,
                        securityModel,
                        secEngineID,
                        securityName,
                        securityLevel,
                        scopedPDUInput,
                        securityParameters,
                        outgoingMessage,
                        tmStateReference);
        if (status == SnmpConstants.SNMPv3_USM_OK) {
            if (expectResponse) {
                cache.addEntry(new StateReference(msgID,
                        flags,
                        maxMessageSize,
                        sendPduHandle,
                        transportAddress,
                        null,
                        secEngineID, secModel,
                        securityName, securityLevel,
                        scopedPDU.getContextEngineID().
                                getValue(),
                        scopedPDU.getContextName().getValue(),
                        null,
                        status));
            }
        }
        return status;
    }

    public int prepareResponseMessage(int messageProcessingModel,
                                      int maxMessageSize,
                                      int securityModel,
                                      byte[] securityName,
                                      int securityLevel,
                                      PDU pdu,
                                      int maxSizeResponseScopedPDU,
                                      StateReference stateReference,
                                      StatusInformation statusInformation,
                                      BEROutputStream outgoingMessage) throws
            IOException {
        /** Leave entry in cache or remove it? RFC3414 §3.1.a.1 says discard it*/
        StateReference<?> cacheEntry = cache.popEntry(stateReference.getMsgID());
        if (cacheEntry == null) {
            return SnmpConstants.SNMP_MP_UNKNOWN_MSGID;
        }

        // get length of scoped PDU
        // get length of scoped PDU
        int scopedPDULength = pdu.getBERLength();
        BEROutputStream scopedPDU;
        // check length
        if (scopedPDULength > maxSizeResponseScopedPDU) {
            PDU tooBigPDU = new ScopedPDU((ScopedPDU) pdu);
            tooBigPDU.clear();
            tooBigPDU.setRequestID(pdu.getRequestID());
            tooBigPDU.setErrorStatus(SnmpConstants.SNMP_ERROR_TOO_BIG);
            tooBigPDU.setErrorIndex(0);
            scopedPDULength = tooBigPDU.getBERLength();
            scopedPDU = new BEROutputStream(ByteBuffer.allocate(scopedPDULength));
            tooBigPDU.encodeBER(scopedPDU);
        } else {
            scopedPDU = new BEROutputStream(ByteBuffer.allocate(scopedPDULength));
            pdu.encodeBER(scopedPDU);
        }

        HeaderData headerData = new HeaderData();
        int flags = 0;
        switch (securityLevel) {
            case SecurityLevel.NOAUTH_NOPRIV:
                flags = 0;
                break;
            case SecurityLevel.AUTH_NOPRIV:
                flags = 1;
                break;
            case SecurityLevel.AUTH_PRIV:
                flags = 3;
                break;
        }
        // response message is not reportable
        headerData.setMsgFlags(flags);
        headerData.setMsgID(stateReference.getMsgID().getID());
        headerData.setMsgMaxSize(maxMessageSize);
        headerData.setSecurityModel(securityModel);

        ByteBuffer globalDataBuffer =
                ByteBuffer.allocate(headerData.getBERLength());
        BEROutputStream globalDataOutputStream =
                new BEROutputStream(globalDataBuffer);
        headerData.encodeBER(globalDataOutputStream);

        OctetString securityEngineID;
        switch (pdu.getType()) {
            case PDU.RESPONSE:
            case PDU.TRAP:
            case PDU.REPORT:
            case PDU.V1TRAP:
                securityEngineID = new OctetString(localEngineID);
                break;
            default:
                securityEngineID = new OctetString(cacheEntry.getSecurityEngineID());
        }

        BERInputStream scopedPDUInput = new BERInputStream(scopedPDU.rewind());

        SecurityModel secModel =
                securityModels.getSecurityModel(new Integer32(securityModel));
        // output data
        SecurityParameters securityParameters =
                secModel.newSecurityParametersInstance();

        return secModel.generateResponseMessage(getID(),
                globalDataBuffer.array(),
                maxMessageSize,
                securityModel,
                securityEngineID.getValue(),
                securityName,
                securityLevel,
                scopedPDUInput,
                cacheEntry.getSecurityStateReference(),
                securityParameters,
                outgoingMessage);
    }

    /**
     * Sends a report message.
     *
     * @param messageDispatcher
     *         Send the message on behalf the supplied MessageDispatcher instance.
     * @param pdu
     *         ScopedPDU If {@code null}, then contextEngineID, contextName, and requestID of the report generated will
     *         be zero length and zero respective. Otherwise these values are extracted from the PDU.
     * @param securityLevel
     *         The security level to use when sending this report.
     * @param securityModel
     *         The security model to use when sending this report.
     * @param securityName
     *         The security name to use when sending this report.
     * @param maxSizeResponseScopedPDU
     *         the maximum size of of the report message (will be most likely ignored because a report should always fit
     *         in 484  bytes).
     * @param stateReference
     *         the state reference associated with the original message.
     * @param payload
     *         the variable binding to include in the report message.
     *
     * @return an SNMP MPv3 error code or 0 if the report has been send successfully.
     */
    public int sendReport(MessageDispatcher messageDispatcher,
                          ScopedPDU pdu,
                          int securityLevel,
                          int securityModel,
                          OctetString securityName,
                          int maxSizeResponseScopedPDU,
                          StateReference<?> stateReference,
                          VariableBinding payload) {
        ScopedPDU reportPDU = new ScopedPDU();
        reportPDU.setType(PDU.REPORT);
        if (pdu != null) {
            reportPDU.setContextEngineID(pdu.getContextEngineID());
            reportPDU.setContextName(pdu.getContextName());
            reportPDU.setRequestID(pdu.getRequestID());
        } else {
            // RFC 3412 §7.1.3d)
            reportPDU.setContextEngineID(new OctetString(getLocalEngineID()));
        }
        reportPDU.add(payload);
        StatusInformation statusInformation = new StatusInformation();
        try {
            int status = messageDispatcher.returnResponsePdu(getID(),
                    securityModel,
                    securityName.getValue(),
                    securityLevel,
                    reportPDU,
                    maxSizeResponseScopedPDU,
                    stateReference,
                    statusInformation);
            if (status != SnmpConstants.SNMP_ERROR_SUCCESS) {
                logger.warn("Error while sending report: " + status);
                return SnmpConstants.SNMP_MP_ERROR;
            }
        } catch (MessageException mex) {
            logger.error("Error while sending report: " + mex.getMessage());
            return SnmpConstants.SNMP_MP_ERROR;
        }
        return SnmpConstants.SNMP_MP_OK;
    }

    @Override
    public int prepareDataElements(MessageDispatcher messageDispatcher, Address transportAddress,
                                                       BERInputStream wholeMsg,
                                                       TransportStateReference tmStateReference,
                                                       Integer32 messageProcessingModel, Integer32 securityModel,
                                                       OctetString securityName, Integer32 securityLevel,
                                                       MutablePDU pdu, PduHandle sendPduHandle,
                                                       Integer32 maxSizeResponseScopedPDU,
                                                       StatusInformation statusInformation,
                                                       MutableStateReference mutableStateReference) {
        try {
            StateReference stateReference = new StateReference();
            // check if there is transport mapping information
            if (mutableStateReference.getStateReference() != null) {
                stateReference.setTransportMapping(
                        mutableStateReference.getStateReference().getTransportMapping());
            }
            messageProcessingModel.setValue(MPv3);
            wholeMsg.mark(16);

            BER.MutableByte type = new BER.MutableByte();
            int length = BER.decodeHeader(wholeMsg, type);
            if (type.getValue() != BER.SEQUENCE) {
                return SnmpConstants.SNMP_MP_PARSE_ERROR;
            }
            long lengthOfLength = wholeMsg.getPosition();
            wholeMsg.reset();
            wholeMsg.mark(length);
            if (wholeMsg.skip(lengthOfLength) != lengthOfLength) {
                return SnmpConstants.SNMP_MP_PARSE_ERROR;
            }

            Integer32 snmpVersion = new Integer32();
            snmpVersion.decodeBER(wholeMsg);
            if (snmpVersion.getValue() != SnmpConstants.version3) {
                // internal error -> should not happen
                throw new RuntimeException(
                        "Internal error unexpected SNMP version read");
            }
            // decode SNMPv3 header
            HeaderData header = new HeaderData();
            header.decodeBER(wholeMsg);
            securityModel.setValue(header.getSecurityModel());

            stateReference.setMsgID(header.getMsgID());
            stateReference.setMsgFlags(header.getMsgFlags());
            stateReference.setAddress(transportAddress);

            mutableStateReference.setStateReference(stateReference);

            // the usm has to recalculate this value
            maxSizeResponseScopedPDU.setValue(header.msgMaxSize.getValue() -
                    MAX_HEADER_LENGTH);

            ScopedPDU scopedPdu = (ScopedPDU) incomingPDUFactory.createPDU(this);
            pdu.setPdu(scopedPdu);

            SecurityModel secModel = securityModels.getSecurityModel(securityModel);
            if (secModel == null) {
                logger.error("RFC3412 §7.2.4 - Unsupported security model: " +
                        securityModel);
                CounterEvent event =
                        new CounterEvent(this,
                                SnmpConstants.snmpUnknownSecurityModels);
                fireIncrementCounter(event);
                return SnmpConstants.SNMP_MP_UNSUPPORTED_SECURITY_MODEL;
            }

            // determine security level
            switch (header.getMsgFlags() & 0x03) {
                case 3: {
                    securityLevel.setValue(SecurityLevel.AUTH_PRIV);
                    break;
                }
                case 0: {
                    securityLevel.setValue(SecurityLevel.NOAUTH_NOPRIV);
                    break;
                }
                case 1: {
                    securityLevel.setValue(SecurityLevel.AUTH_NOPRIV);
                    break;
                }
                default: {
                    securityLevel.setValue(SecurityLevel.NOAUTH_NOPRIV);
                    logger.debug("RFC3412 §7.2.5 - Invalid message (illegal msgFlags)");
                    CounterEvent event = new CounterEvent(this,
                            SnmpConstants.snmpInvalidMsgs);
                    fireIncrementCounter(event);
                    // do not send back report
                    return SnmpConstants.SNMP_MP_INVALID_MESSAGE;
                }
            }
            statusInformation.setSecurityLevel(securityLevel);

            int secParametersPosition = (int) wholeMsg.getPosition();
            // get security parameters
            SecurityParameters secParameters =
                    secModel.newSecurityParametersInstance();
            secParameters.decodeBER(wholeMsg);
            secParameters.setSecurityParametersPosition(secParametersPosition);

            // reportable flag
            boolean reportableFlag = ((header.getMsgFlags() & 0x04) > 0);

            OctetString securityEngineID = new OctetString();
            // create a new security state reference
            SecurityStateReference secStateReference =
                    secModel.newSecurityStateReference();
            // create output stream for scoped PDU
            // may be optimized by an output stream that maps directly into the
            // original input
            wholeMsg.reset();

            BEROutputStream scopedPDU = new BEROutputStream();
            int status =
                    secModel.processIncomingMsg(snmpVersion.getValue(),
                            header.getMsgMaxSize() - MAX_HEADER_LENGTH,
                            secParameters,
                            secModel,
                            securityLevel.getValue(),
                            wholeMsg,
                            tmStateReference,
                            // output parameters
                            securityEngineID,
                            securityName,
                            scopedPDU,
                            maxSizeResponseScopedPDU,
                            secStateReference,
                            statusInformation);
            wholeMsg.close();
            if (status == SnmpConstants.SNMPv3_USM_OK) {
                try {
                    BERInputStream scopedPduStream =
                            new BERInputStream(scopedPDU.rewind());
                    scopedPdu.decodeBER(scopedPduStream);
                    sendPduHandle.setTransactionID(scopedPdu.getRequestID().getValue());

                    // add the engine ID to the local cache if it is a report or response, do not add traps.
                    if ((securityEngineID.length() > 0) && scopedPdu.isResponsePdu()) {
                        addEngineID(transportAddress, securityEngineID);
                    }
                } catch (IOException iox) {
                    logger.warn("ASN.1 parse error: " + iox.getMessage());
                    if (logger.isDebugEnabled()) {
                        iox.printStackTrace();
                    }
                    CounterEvent event = new CounterEvent(this,
                            SnmpConstants.
                                    snmpInASNParseErrs);
                    fireIncrementCounter(event);
                    return SnmpConstants.SNMP_MP_PARSE_ERROR;
                }
                if (((scopedPdu.getContextEngineID() == null) ||
                        (scopedPdu.getContextEngineID().length() == 0)) &&
                        ((scopedPdu.getType() != PDU.RESPONSE) &&
                                (scopedPdu.getType() != PDU.REPORT))) {
                    CounterEvent event = new CounterEvent(this,
                            SnmpConstants.
                                    snmpUnknownPDUHandlers);
                    fireIncrementCounter(event);
                    VariableBinding errorIndication =
                            new VariableBinding(event.getOid(), event.getCurrentValue());
                    statusInformation.setErrorIndication(errorIndication);
                    status = SnmpConstants.SNMP_MP_UNKNOWN_PDU_HANDLERS;
                }
            }

            stateReference.setSecurityName(securityName.getValue());
            stateReference.setSecurityEngineID(securityEngineID.getValue());
            stateReference.setSecurityLevel(securityLevel.getValue());
            stateReference.setSecurityModel(secModel);
            stateReference.setSecurityStateReference(secStateReference);
            stateReference.setPduHandle(sendPduHandle);

            if (status != SnmpConstants.SNMPv3_USM_OK) {
                if ((reportableFlag) &&
                        (statusInformation.getErrorIndication() != null)) {
                    // RFC3412 §7.2.6.a - generate a report
                    try {
                        if (scopedPDU.getBuffer() != null) {
                            BERInputStream scopedPduStream =
                                    new BERInputStream(scopedPDU.rewind());
                            scopedPdu.decodeBER(scopedPduStream);
                        } else { // incoming message could not be decoded
                            scopedPdu = null;
                        }
                    } catch (IOException iox) {
                        logger.warn(iox);
                        scopedPdu = null;
                    }

                    StateReference cacheEntry =
                            new StateReference(header.getMsgID(),
                                    header.getMsgFlags(),
                                    maxSizeResponseScopedPDU.getValue(),
                                    sendPduHandle,
                                    transportAddress,
                                    null,
                                    securityEngineID.getValue(),
                                    secModel, securityName.getValue(),
                                    securityLevel.getValue(),
                                    (scopedPdu == null) ? new byte[0] :
                                            scopedPdu.getContextEngineID().getValue(),
                                    (scopedPdu == null) ? new byte[0] :
                                            scopedPdu.getContextName().getValue(),
                                    secStateReference, status);
                    cache.addEntry(cacheEntry);

                    int reportStatus =
                            sendReport(messageDispatcher, scopedPdu,
                                    statusInformation.getSecurityLevel().getValue(),
                                    secModel.getID(), securityName,
                                    maxSizeResponseScopedPDU.getValue(),
                                    stateReference,
                                    statusInformation.getErrorIndication());
                    if (reportStatus != SnmpConstants.SNMP_MP_OK) {
                        logger.warn("Sending report failed with error code: " +
                                reportStatus);
                    }
                }
                return status;
            }

            stateReference.setContextEngineID(scopedPdu.getContextEngineID().getValue());
            stateReference.setContextName(scopedPdu.getContextName().getValue());
            stateReference.setMaxSizeResponseScopedPDU(maxSizeResponseScopedPDU.
                    getValue());
            if ((scopedPdu.getType() == PDU.RESPONSE) ||
                    (scopedPdu.getType() == PDU.REPORT)) {
                StateReference<?> cacheEntry = cache.popEntry(StateReference.createMessageID(header.getMsgID()));
                if (cacheEntry != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("RFC3412 §7.2.10 - Received PDU (msgID=" +
                                header.getMsgID() + ") is a response or " +
                                "an internal class message. PduHandle.transactionID = " +
                                cacheEntry.getPduHandle().getTransactionID());
                    }
                    sendPduHandle.copyFrom(cacheEntry.getPduHandle());

                    if (scopedPdu.getType() == PDU.REPORT) {

                        statusInformation.setContextEngineID(scopedPdu.getContextEngineID().getValue());
                        statusInformation.setContextName(scopedPdu.getContextName().getValue());
                        statusInformation.setSecurityLevel(securityLevel);

                        if (((cacheEntry.getSecurityEngineID().length != 0) &&
                                (!securityEngineID.equalsValue(cacheEntry.getSecurityEngineID()))) ||
                                (secModel.getID() != cacheEntry.getSecurityModel().getID()) ||
                                ((!securityName.equalsValue(cacheEntry.getSecurityName()) &&
                                        (securityName.length() != 0)))) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "RFC 3412 §7.2.11 - Received report message does not match sent message. Cache entry is: " +
                                                cacheEntry + ", received secName=" + securityName + ",secModel=" + secModel +
                                                ",secEngineID=" + securityEngineID);
                            }
                            //cache.deleteEntry(cacheEntry.getPduHandle());
                            mutableStateReference.setStateReference(null);
                            return SnmpConstants.SNMP_MP_MATCH_ERROR;
                        }
                        if (!addEngineID(cacheEntry.getAddress(), securityEngineID)) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("Engine ID '" + securityEngineID +
                                        "' could not be added to engine ID cache for " +
                                        "target address '" + cacheEntry.getAddress() +
                                        "' because engine ID matches local engine ID or cache size limit is reached");
                            }
                        }
                        //cache.deleteEntry(cacheEntry.getPduHandle());
                        mutableStateReference.setStateReference(null);
                        logger.debug("MPv3 finished");
                        return SnmpConstants.SNMP_MP_OK;
                    }
                    if (scopedPdu.getType() == PDU.RESPONSE) {
                        if (((!securityEngineID.equalsValue(cacheEntry.getSecurityEngineID())) &&
                                (cacheEntry.getSecurityEngineID().length != 0)) ||
                                (secModel.getID() != cacheEntry.getSecurityModel().getID()) ||
                                (!securityName.equalsValue(cacheEntry.getSecurityName())) ||
                                (securityLevel.getValue() != cacheEntry.getSecurityLevel()) ||
                                ((!scopedPdu.getContextEngineID().equalsValue(cacheEntry.getContextEngineID())) &&
                                        (cacheEntry.getContextEngineID().length != 0)) ||
                                ((!scopedPdu.getContextName().equalsValue(cacheEntry.getContextName()) &&
                                        (cacheEntry.getContextName().length != 0)))) {
                            logger.debug(
                                    "RFC 3412 §7.2.12.b - Received response message does not match sent message");
                            //cache.deleteEntry(cacheEntry.getPduHandle());
                            mutableStateReference.setStateReference(null);
                            return SnmpConstants.SNMP_MP_MATCH_ERROR;
                        }
                        //cache.deleteEntry(cacheEntry.getPduHandle());
                        mutableStateReference.setStateReference(null);
                        logger.debug("MPv3 finished");
                        return SnmpConstants.SNMP_MP_OK;
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("RFC3412 §7.2.10 - Received PDU (msgID=" +
                                header.getMsgID() + ") is a response or " +
                                "internal class message, but cached " +
                                "information for the msgID could not be found");
                    }
                    return SnmpConstants.SNMP_MP_UNKNOWN_MSGID;
                }
            } else {
                logger.debug("RFC3412 §7.2.10 - Received PDU is NOT a response or " +
                        "internal class message -> unchanged PduHandle = " +
                        sendPduHandle);
            }
            switch (scopedPdu.getType()) {
                case PDU.GET:
                case PDU.GETBULK:
                case PDU.GETNEXT:
                case PDU.INFORM:
                case PDU.SET: {
                    if (securityEngineID.length() == 0) {
                        logger.debug("Received confirmed message with 0 length security engine ID");
                    } else if (!securityEngineID.equalsValue(localEngineID)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("RFC3412 §7.2.13.a - Security engine ID " +
                                    securityEngineID.toHexString() +
                                    " does not match local engine ID " +
                                    new OctetString(localEngineID).toHexString());
                        }
                        mutableStateReference.setStateReference(null);
                        return SnmpConstants.SNMP_MP_INVALID_ENGINEID;
                    }
                    int cacheStatus = cache.addEntry(stateReference);
                    if (cacheStatus == SnmpConstants.SNMP_MP_DOUBLED_MESSAGE) {
                        mutableStateReference.setStateReference(null);
                    }
                    return SnmpConstants.SNMP_MP_OK;
                }
                case PDU.TRAP:
                case PDU.V1TRAP: {
                    mutableStateReference.setStateReference(null);
                    return SnmpConstants.SNMP_MP_OK;
                }
            }
            // this line should not be reached
            return SnmpConstants.SNMP_MP_ERROR;
        } catch (IOException iox) {
            logger.warn("MPv3 parse error: " + iox.getMessage());
            if (logger.isDebugEnabled()) {
                iox.printStackTrace();
            }
            return SnmpConstants.SNMP_MP_PARSE_ERROR;
        }
    }

    /**
     * Sets the security models supported by this MPv3.
     *
     * @param securityModels
     *         a {@link SecurityModels} instance.
     */
    public void setSecurityModels(SecurityModels securityModels) {
        this.securityModels = securityModels;
    }

    /**
     * Gets the security models supported by this MPv3.
     *
     * @return a {@link SecurityModels} instance.
     */
    public SecurityModels getSecurityModels() {
        return securityModels;
    }

    /**
     * Fire a counter incrementation event.
     *
     * @param e
     *         CounterEvent
     */
    protected void fireIncrementCounter(CounterEvent e) {
        if (counterSupport != null) {
            counterSupport.fireIncrementCounter(e);
        }
    }

    /**
     * Gets the counter support instance that can be used to register for counter incrementation events.
     *
     * @return a {@link CounterSupport} instance that is used to fire {@link CounterEvent}.
     */
    public CounterSupport getCounterSupport() {
        return counterSupport;
    }

    /**
     * Sets the counter support instance. By default, the singleton instance provided by the {@link CounterSupport}
     * instance is used.
     *
     * @param counterSupport
     *         a {@link CounterSupport} subclass instance.
     */
    public void setCounterSupport(CounterSupport counterSupport) {
        if (counterSupport == null) {
            throw new NullPointerException();
        }
        this.counterSupport = counterSupport;
    }

    /**
     * Adds a SNMP engine listener that needs to be informed about changes to the engine ID cache.
     *
     * @param l
     *         a {@link SnmpEngineListener} instance.
     *
     * @since 1.6
     */
    public synchronized void addSnmpEngineListener(SnmpEngineListener l) {
        List<SnmpEngineListener> listeners = snmpEngineListeners;
        if (listeners == null) {
            listeners = new ArrayList<SnmpEngineListener>();
        } else {
            listeners = new ArrayList<SnmpEngineListener>(snmpEngineListeners);
        }
        listeners.add(l);
        this.snmpEngineListeners = listeners;
    }

    /**
     * Removes a SNMP engine listener.
     *
     * @param l
     *         a {@link SnmpEngineListener} instance.
     *
     * @since 1.6
     */
    public synchronized void removeSnmpEngineListener(SnmpEngineListener l) {
        List<SnmpEngineListener> listeners = snmpEngineListeners;
        if (listeners != null) {
            listeners = new ArrayList<SnmpEngineListener>(listeners);
            listeners.remove(l);
            this.snmpEngineListeners = listeners;
        }
    }

    /**
     * Get the number of cached engine IDs.
     *
     * @return size of the internal engine ID cache.
     * @since 2.3.4
     */
    public int getEngineIdCacheSize() {
        return engineIDs.size();
    }

    /**
     * Creates a PDU class that is used to parse incoming SNMP messages.
     *
     * @param target
     *         the {@code target} parameter must be ignored.
     *
     * @return a {@link ScopedPDU} instance by default.
     * @since 1.9.1
     * @deprecated Use {@link org.snmp4j.util.DefaultPDUFactory#createPDU(MessageProcessingModel, int)} instead.
     */
    @Deprecated
    public PDU createPDU(Target target) {
        return incomingPDUFactory.createPDU(target);
    }

    /**
     * Gets the message ID that will be used for the next request to be sent by this message processing model.
     *
     * @return the next message ID used by the MPv3.
     * @since 2.4.3
     */
    public int getNextMsgID() {
        return currentMsgID;
    }

    /**
     * Sets the next message ID. According to RFC3412, the message ID should be unique across reboots: "Values for msgID
     * SHOULD be generated in a manner that avoids re-use of any outstanding values.  Doing so provides protection
     * against some replay attacks.  One possible implementation strategy would be to use the low-order bits of
     * snmpEngineBoots [RFC3411] as the high-order portion of the msgID value and a monotonically increasing integer for
     * the low-order portion of msgID."
     *
     * @param nextMsgID
     *         a message ID that has not been used by this SNMP entity yet (preferably also not used during previous
     *         runs).
     *
     * @since 2.4.3
     */
    public void setCurrentMsgID(int nextMsgID) {
        this.currentMsgID = nextMsgID;
    }

    /**
     * Fires a SNMP engine event the registered listeners.
     *
     * @param engineEvent
     *         the {@code SnmpEngineEvent} instance to fire.
     *
     * @since 1.6
     */
    protected void fireEngineChanged(SnmpEngineEvent engineEvent) {
        if (snmpEngineListeners != null) {
            List<SnmpEngineListener> listeners = snmpEngineListeners;
            for (SnmpEngineListener listener : listeners) {
                listener.engineChanged(engineEvent);
            }
        }
    }

    /**
     * The {@code EngineIdCacheFactory} creates an engine ID cache with upper limit.
     */
    public interface EngineIdCacheFactory {
        /**
         * Create a engine ID map with the given maximum capacity. If more than those engine IDs are added, the eldest
         * engine IDs will be removed from the map before the new one is added.
         *
         * @param maximumCapacity
         *         the upper limit of the number of engine ID mappings in this map.
         *
         * @return the created map.
         */
        Map<Address, OctetString> createEngineIdMap(org.snmp4j.mp.EngineIdCacheSize maximumCapacity);
    }

    private static class LimitedCapacityEngineIdCacheFactory implements EngineIdCacheFactory {
        @Override
        public Map<Address, OctetString> createEngineIdMap(final org.snmp4j.mp.EngineIdCacheSize cacheSize) {
            Map<Address, OctetString> map = new HashMap<Address, OctetString>();
            return Collections.synchronizedMap(map);
        }
    }
}
