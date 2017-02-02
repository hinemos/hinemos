/*
Copyright (c) 2009 - 2010, Intel Corporation
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
      in the documentation and/or other materials provided with the distribution.
    * Neither the name of Intel Corporation nor the names of its contributors may be used to endorse or promote products derived from
      this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

//----------------------------------------------------------------------------
//
//  File:       WsmanItem.java
//
//  Contents: Contains the definition of the WsmanItem class
//
//  Notes:
//
//----------------------------------------------------------------------------
package intel.management.wsman;

/**
 * Represents an object and a reference returned from a WsMan Enumeration.
 *
 *
 * <P>
 * WsmanItems are returned when performing an enumeration that results in
 * both Objects and References. (e.g. when using <code>WsmanUtils.ENUMERATE_OBJECT_AND_EPR</code>
 * during an enumeration.)

 *
 * @see WsmanEnumeration
 *
 */
public class WsmanItem {

    private ManagedInstance _instance;
    private ManagedReference _ref;

    /**
     * Construct an item from an instance and reference.
     *
     * <P>
     * <code>intel.management.wsman.WsmanUtils.LoadDocument()</code> can be used to create a
     * document object from an XML string or stream.
     *
     * @param instance The instance item
     * @param ref The reference to the instance item
     *
     *
     *
     */
    protected WsmanItem(ManagedInstance instance, ManagedReference ref) {
        _instance = instance;
        _ref= ref;
    }


    /**
     *
     * Returns the instance part of the item
     *
     *
     * @return
     * The instance object
     *
     *
     *
     */
    public ManagedInstance getObject() {
        return _instance;
    }

    /**
     *
     * Returns the reference part of the item
     *
     *
     * @return
     * The reference object
     *
     *
     *
     */
    public ManagedReference getReference() {
        return _ref;
    }
}
