/*_############################################################################
  _## 
  _##  SNMP4J - DecryptParams.java  
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

/**
 * Parameter class for encrypt and decrypt methods of {@link SecurityProtocol}.
 * @author Jochen Katz
 * @version 1.0
 */
public class DecryptParams
{
  /**
   * Initialize with the given value.
   * @param array
   *    the array as received on the wire
   * @param offset
   *    offset within the array
   * @param length
   *    length of the decrypt params
   */
  public DecryptParams(byte[] array, int offset, int length)
  {
    this.array = array;
    this.offset = offset;
    this.length = length;
  }

  /**
   * Inizialize with null values.
   */
  public DecryptParams()
  {
    this.array = null;
    this.offset = 0;
    this.length = 0;
  }

  /**
   * Initialize with the given value.
   * @param array
   *    the array as received on the wire
   * @param offset
   *    offset within the array
   * @param length
   *    length of the decrypt params
   */
  public void setValues(byte[] array, int offset, int length)
  {
    this.array = array;
    this.offset = offset;
    this.length = length;
  }

  public byte[] array;
  public int offset;
  public int length;
}
