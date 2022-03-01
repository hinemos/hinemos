/*_############################################################################
  _## 
  _##  SNMP4J - SecurityModels.java  
  _## 
  _##  Copyright (C) 2003-2020  Frank Fock and Jochen Katz (SNMP4J.org)
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
package org.snmp4j.security;

import java.util.*;
import org.snmp4j.smi.Integer32;

/**
 * The <code>SecurityModels</code> class is a collection of all
 * supported security models of a SNMP entity.
 *
 * @author Frank Fock
 * @author Jochen Katz
 * @version 2.6.4
 */
public class SecurityModels {

  private Map<Integer32, SecurityModel> securityModels = new Hashtable<Integer32, SecurityModel>(3);

  private static SecurityModels instance = null;

  public SecurityModels() {
  }

  /**
   * Gets the security singleton instance.
   * @return
   *    the <code>SecurityModels</code> instance.
   */
  public synchronized static SecurityModels getInstance() {
    if (instance == null) {
      instance = new SecurityModels();
    }
    return instance;
  }

  /**
   * Gets the SecurityModels collection instance that contains the supplied
   * {@link SecurityModel}s.
   * @param models
   *    an array of {@link SecurityModel} instances.
   * @return
   *    a new instance of SecurityModels that contains the supplied models.
   * @since 1.10
   */
  public static SecurityModels getCollection(SecurityModel[] models) {
    SecurityModels smc = new SecurityModels();
    for (SecurityModel model : models) {
      smc.addSecurityModel(model);
    }
    return smc;
  }

  /**
   * Adds a security model to the central repository of security models.
   * @param model
   *    a <code>SecurityModel</code>. If a security model with the same ID
   *    already
   * @return
   *    this resulting security models object.
   */
  public SecurityModels addSecurityModel(SecurityModel model) {
    securityModels.put(new Integer32(model.getID()), model);
    return this;
  }

  /**
   * Removes a security model from the central repository of security models.
   * @param id
   *    the <code>Integer32</code> ID of the security model to remove.
   * @return
   *    the removed <code>SecurityModel</code> or <code>null</code> if
   *    <code>id</code> is not registered.
   */
  public SecurityModel removeSecurityModel(Integer32 id) {
    return securityModels.remove(id);
  }

  /**
   * Returns a security model from the central repository of security models.
   * @param id
   *    the <code>Integer32</code> ID of the security model to return.
   * @return
   *    the with <code>id</code> associated <code>SecurityModel</code> or
   *    <code>null</code> if no such model is registered.
   */
  public SecurityModel getSecurityModel(Integer32 id) {
    return securityModels.get(id);
  }
}

