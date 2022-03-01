/*_############################################################################
  _## 
  _##  SNMP4J - CipherPool.java  
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

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

/**
 * The CipherPool class provides service to share and reuse Cipher instances, across
 * different threads. The maximum number of Ciphers in the pool might temporarily
 * exceed the {@link #maxPoolSize} to minimize waiting time.
 *
 * @author Frank Fock
 * @since 2.2.2
 */
public class CipherPool {

  private LinkedList<Cipher> availableCiphers;

  private int maxPoolSize;
  private int currentPoolSize;


  /**
   * Creates a new cipher pool with a pool size of {@link Runtime#availableProcessors()}.
   */
  public CipherPool() {
    this(Runtime.getRuntime().availableProcessors());
  }

  /**
   * Creates a new cipher pool with a given pool size.
   * @param maxPoolSize
   *   the maximum number of ciphers in the pool.
   */
  public CipherPool(int maxPoolSize) {
    this.currentPoolSize = 0;
    if (maxPoolSize < 0) {
      throw new IllegalArgumentException("Pool size must be >= 0");
    }
    this.maxPoolSize = maxPoolSize;
    this.availableCiphers = new LinkedList<Cipher>();
  }

  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  /**
   * Gets a Cipher from the pool. It must be returned to the pool by calling
   * {@link #offerCipher(Cipher)} when one of its {@link javax.crypto.Cipher#doFinal()}
   * methods have been called and it is not needed anymore.
   * @return
   *    a Cipher from the pool, or {@code null} if the pool currently does not contain any
   *    cipher.
   */
  public synchronized Cipher reuseCipher() {
    Cipher cipher = availableCiphers.poll();
    if (cipher == null) {
      currentPoolSize = 0;
    }
    else {
      currentPoolSize--;
    }
    return cipher;
  }

  /**
   * Offers a Cipher to the pool (thus returns it to the pool).
   * @param cipher
   *    a Cipher instance previously acquired by {@link #reuseCipher()} or created externally.
   */
  public synchronized void offerCipher(Cipher cipher) {
    if (currentPoolSize < maxPoolSize) {
      currentPoolSize++;
      availableCiphers.offer(cipher);
    }
  }
}
