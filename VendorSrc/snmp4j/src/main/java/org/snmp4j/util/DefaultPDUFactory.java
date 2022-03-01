/*_############################################################################
  _## 
  _##  SNMP4J - DefaultPDUFactory.java  
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

package org.snmp4j.util;

import org.snmp4j.*;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OctetString;

/**
 * The <code>DefaultPDUFactory</code> is a default implementation of the
 * <code>PDUFactory</code> interface. It creates PDUs depending on the
 * target's message processing model. That is, a {@link PDUv1} instance is
 * created for a SNMPv1 target whereas a {@link ScopedPDU} is created
 * for a SNMPv3 target. In all other cases a {@link PDU} instance is created.
 *
 * @author Frank Fock
 * @version 2.5.1
 * @since 1.0.4
 */
public class DefaultPDUFactory implements PDUFactory {

  public static final int GETBULK_DEFAULT_MAX_REPETITIONS = 5;
  public static final int GETBULK_DEFAULT_NON_REPEATERS = 0;

  private int pduType = PDU.GET;
  private int maxRepetitions = GETBULK_DEFAULT_MAX_REPETITIONS;
  private int nonRepeaters = GETBULK_DEFAULT_NON_REPEATERS;

  private OctetString contextEngineID;
  private OctetString contextName;

  /**
   * Creates a PDU factory for the {@link PDU#GET} PDU type.
   */
  public DefaultPDUFactory() {
  }

  /**
   * Creates a PDU factory for the specified PDU type.
   * Context engine ID and name will be set to empty {@link OctetString}
   * instances.
   * @param pduType
   *    a PDU type as specified by {@link PDU}.
   */
  public DefaultPDUFactory(int pduType) {
    setPduType(pduType);
    this.contextEngineID = new OctetString();
    this.contextName = new OctetString();
  }

  /**
   * Creates a {@link PDUFactory} with type and context information.
   * @param pduType
   *     a PDU type as specified by {@link PDU}.
   * @param contextEngineID
   *     a context engine ID to be used when creating {@link ScopedPDU}s.
   * @param contextName
   *     a context name to be used when creating {@link ScopedPDU}s.
   * @since 2.2
   */
  public DefaultPDUFactory(int pduType, OctetString contextEngineID, OctetString contextName) {
    this(pduType);
    this.contextEngineID = contextEngineID;
    this.contextName = contextName;
  }

  public void setPduType(int pduType) {
    this.pduType = pduType;
  }

  public int getPduType() {
    return pduType;
  }

  /**
   * Create a <code>PDU</code> instance for the supplied target.
   *
   * @param target the <code>Target</code> where the PDU to be created will be
   *   sent.
   * @return PDU a PDU instance that is compatible with the supplied target.
   */
  public PDU createPDU(Target target) {
    PDU pdu = createPDU(target, pduType);
    applyContextInfoToScopedPDU(pdu);
    return pdu;
  }

  /**
   * Sets context engine ID and context name members on the given PDU if that PDU
   * is a {@link ScopedPDU}.
   *
   * @param pdu
   *    a {@link PDU} instance which is modified if it is a {@link ScopedPDU} instance.
   * @since 2.5.0
   */
  protected void applyContextInfoToScopedPDU(PDU pdu) {
    if (pdu instanceof ScopedPDU) {
      ScopedPDU scopedPDU = (ScopedPDU)pdu;
      scopedPDU.setContextEngineID(contextEngineID);
      scopedPDU.setContextName(contextName);
    }
  }

  /**
   * Create a <code>PDU</code> instance for the supplied target. For GETBULK
   * PDUs, the default max repetitions and non repeaters are used. See
   * {@link #GETBULK_DEFAULT_MAX_REPETITIONS} and
   * {@link #GETBULK_DEFAULT_NON_REPEATERS}.
   *
   * @param target the <code>Target</code> where the PDU to be created will be
   *    sent.
   * @param pduType
   *    a PDU type as specified by {@link PDU}.
   * @return PDU
   *    a PDU instance that is compatible with the supplied target.
   */
  public static PDU createPDU(Target target, int pduType) {
    return createPDU(target, pduType, GETBULK_DEFAULT_MAX_REPETITIONS, GETBULK_DEFAULT_NON_REPEATERS);
  }

  /**
   * Create a <code>PDU</code> instance for the supplied target.
   *
   * @param target the <code>Target</code> where the PDU to be created will be
   *    sent.
   * @param pduType
   *    a PDU type as specified by {@link PDU}.
   * @param maxRepetitions
   *    the maximum number of repetitions for GETBULK PDUs created
   *    by this factory.
   * @param nonRepeaters
   *    the number of non-repeater variable bindings
   *    (processed like GETNEXT) for GETBULK PDUs created
   *    by this factory.
   * @return PDU
   *    a PDU instance that is compatible with the supplied target.
   * @since 2.2
   */
  public static PDU createPDU(Target target, int pduType, int maxRepetitions, int nonRepeaters) {
    PDU request = createPDU(target.getVersion());
    request.setType(pduType);
    if (pduType == PDU.GETBULK) {
      request.setMaxRepetitions(maxRepetitions);
      request.setNonRepeaters(nonRepeaters);
    }
    return request;
  }

  /**
   * Creates a <code>PDU</code> instance for the specified SNMP version.
   * @param targetVersion
   *    a SNMP version as defined by {@link SnmpConstants}.
   * @return
   *    a PDU instance that is compatible with the supplied target SNMP version.
   * @since 1.7.3
   */
  public static PDU createPDU(int targetVersion) {
    PDU request;
    switch (targetVersion) {
      case SnmpConstants.version3: {
        request = new ScopedPDU();
        break;
      }
      case SnmpConstants.version1: {
        request = new PDUv1();
        break;
      }
      default:
        request = new PDU();
    }
    return request;
  }

  /**
   * Creates a <code>PDU</code> instance for the specified {@link MessageProcessingModel}.
   * @param messageProcessingModel
   *    a message processing model instance.
   * @return
   *    a PDU that is compatible with the specified message processing model.
   * @since 2.2
   */
  @Override
  public PDU createPDU(MessageProcessingModel messageProcessingModel) {
    PDU pdu = createPduByMP(messageProcessingModel);
    applyContextInfoToScopedPDU(pdu);
    return pdu;
  }

  /**
   * Creates a <code>PDU</code> instance for the specified {@link MessageProcessingModel} and PDU type.
   * @param messageProcessingModel
   *    a message processing model instance.
   * @param pduType
   *    the type for the new PDU.
   * @return
   *    a PDU that is compatible with the specified message processing model.
   * @since 2.2
   */
  public static PDU createPDU(MessageProcessingModel messageProcessingModel, int pduType) {
    PDU pdu = createPduByMP(messageProcessingModel);
    pdu.setType(pduType);
    return pdu;
  }

  private static PDU createPduByMP(MessageProcessingModel messageProcessingModel) {
    PDU pdu;
    switch (messageProcessingModel.getID()) {
      case MessageProcessingModel.MPv3: {
        pdu = new ScopedPDU();
        break;
      }
      case MessageProcessingModel.MPv1: {
        pdu = new PDUv1();
        break;
      }
      default:
        pdu = new PDU();
    }
    return pdu;
  }

  /**
   * Gets the maximum number of repetitions for the repetitions
   * variable bindings for {@link PDU#GETBULK} PDUs.
   * Default is {@link #GETBULK_DEFAULT_MAX_REPETITIONS}
   * @return
   *    the maximum number of repetitions for GETBULK PDUs created
   *    by this factory.
   * @since 2.2
   */
  public int getMaxRepetitions() {
    return maxRepetitions;
  }

  /**
   * Sets the max repetitions parameter value for GETBULK PDUs created by
   * this factory.
   * @param maxRepetitions
   *    the maximum number of repetitions for GETBULK PDUs created
   *    by this factory.
   * @since 2.2
   */
  public void setMaxRepetitions(int maxRepetitions) {
    this.maxRepetitions = maxRepetitions;
  }

  /**
   * Gets the number of non repeater variable bindings for
   * {@link PDU#GETBULK} PDUs.
   * Default is {@link #GETBULK_DEFAULT_NON_REPEATERS}.
   * @return
   *    the number of non-repeater variable bindings
   *    (processed like GETNEXT) for GETBULK PDUs created
   *    by this factory.
   * @since 2.2
   */
  public int getNonRepeaters() {
    return nonRepeaters;
  }

  /**
   * Sets the non repeaters parameter value for GETBULK PDUs created by
   * this factory.
   * @param nonRepeaters
   *    the number of non-repeater variable bindings
   *    (processed like GETNEXT) for GETBULK PDUs created
   *    by this factory.
   * @since 2.2
   */
  public void setNonRepeaters(int nonRepeaters) {
    this.nonRepeaters = nonRepeaters;
  }

  /**
   * Gets the context engine ID for {@link ScopedPDU} instances created
   * by this factory.
   * @return
   *    the context engine ID or <code>null</code> if context engine ID is
   *    the same as authoritative engine ID.
   * @since 2.2
   */
  public OctetString getContextEngineID() {
    return contextEngineID;
  }

  /**
   * Sets the context engine ID for {@link ScopedPDU} instances created
   * by this factory.
   *
   * @param contextEngineID
   *    the context engine ID or <code>null</code> if context engine ID is
   *    the same as authoritative engine ID.
   * @since 2.2
   */
  public void setContextEngineID(OctetString contextEngineID) {
    this.contextEngineID = contextEngineID;
  }

  /**
   * Gets the context name for {@link ScopedPDU} instances created
   * by this factory.
   *
   * @return
   *    the context name or <code>null</code> for the default context.
   * @since 2.2
   */
   public OctetString getContextName() {
    return contextName;
  }

  /**
   * Sets the context name for {@link ScopedPDU} instances created
   * by this factory.
   * @param contextName
   *    the context name or <code>null</code> for the default context.
   * @since 2.2
   */
  public void setContextName(OctetString contextName) {
    this.contextName = contextName;
  }
}
