/*_############################################################################
  _## 
  _##  SNMP4J - Salt.java  
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

import java.util.Random;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import org.snmp4j.log.*;

/**
 * Class that holds a 64 bit salt value for crypto operations.
 *
 * This class tries to use the SecureRandom class to initialize
 * the salt value. If SecureRandom is not available the class Random
 * is used.
 *
 * @author Jochen Katz
 * @version 1.0
 */
class Salt {
  private long salt;

  private static Salt instance = null;
  private static final LogAdapter logger = LogFactory.getLogger(Salt.class);

  /**
   * Default constructor, initializes the salt to a random value.
   */
  protected Salt() {
    byte[] rnd = new byte[8];

    try {
      SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
      sr.nextBytes(rnd);
    }
    catch (NoSuchAlgorithmException nsae) {
      logger.warn("Could not use SecureRandom. Using Random instead.");
      Random r = new Random();
      r.nextBytes(rnd);
    }

    salt = rnd[0];

    for (int i = 0; i < 7; i++) {
      salt = (salt * 256) + ((int)rnd[i]) + 128;
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Initialized Salt to " + Long.toHexString(salt) + ".");
    }
  }

  /**
   * Get a initialized Salt object.
   *
   * @return the Salt object
   */
  public static Salt getInstance() {
    if (instance == null) {
      instance = new Salt();
    }
    return instance;
  }

  /**
   * Get the next value of the salt by adding one to its current value.
   * This might result in a predictable salt value if it is not combined with
   * other somehow unpredictable (random) data.
   *
   * @return
   *    previous value increased by one.
   */
  public synchronized long getNext() {
    return salt++;
  }
}
