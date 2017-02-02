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
//  File:       StatusCodes.java
//
//  Contents:   Contains status codes common to Intel Firmware
//
//  Notes:      This ResourceBundle can be localized (english is default)
//
//----------------------------------------------------------------------------

package intel.management.wsman.resources;

/**
 * A resource bundle for loading text descriptions of Intel(r) AMT firmware codes
 *
 *<P> Example of how to use:
 *
 * <P><code>
 *
 * java.util.ResourceBundle<b>
               res =<br>
   java.util.ResourceBundle.getBundle("intel.management.wsman.resources.StatusCodes");<br>
 * String result = res.getString("0");<br>
 *</code>
 *
 *
 */
public class StatusCodes extends java.util.ListResourceBundle {

 /**
 * Gets the error code mapping table
 *
 *
 *
 * @return
 * the error code mapping table
 *
 *
 *
 */

    public Object[][] getContents() {
        return contents;
    }

 /**
 * The error code mapping table
 *
 *
 */
    static final Object[][] contents = {

        // These are the status messages
        {"0","Request succeeded."},
        {"1","An internal error in the Intel(r) AMT device has occurred"},
        {"2","Intel(r) AMT device has not progressed far enough in its initialization to process the command."},
        {"3","Command is not permitted in current operating mode."},
        {"4","Length field of header is invalid."},
        {"5","The requested hardware asset inventory table checksum is not available."},
        {"6","The Integrity Check Value field of the request message sent by Intel(r) AMT enabled device is invalid."},
        {"7","The specified ISV version is not supported"},
        {"8","The specified queried application is not registered."},
        {"9","Either an invalid name or a not previously registered or Enterprise name was specified"},
        {"10","The application handle provided in the request message has never been allocated."},
        {"11","The requested number of bytes cannot be allocated in ISV storage."},
        {"12","The specified name is invalid."},
        {"13","The specified block does not exist."},
        {"14","The specified byte offset is invalid."},
        {"15","The specified byte count is invalid."},
        {"16","The requesting application is not permitted to request execution of the specified operation."},
        {"17","The requesting application is not the owner of the block as required for the requested operation."},
        {"18","The specified block is locked by another application."},
        {"19","The specified block is not locked."},
        {"20","The specified group permission bits are invalid."},
        {"21","The specified group does not exist."},
        {"22","The specified member count is invalid."},
        {"23","The request cannot be satisfied because a maximum limit associated with the request has been reached."},
        {"24","The specified key algorithm is invalid."},
        {"25","Authentication failed"},
        {"26","The specified DHCP mode is invalid."},
        {"27","The specified IP address is not a valid IP unicast address."},
        {"28","The specified domain name is not a valid domain name."},
        {"29","Not Used"},
        {"30","The requested operation cannot be performed because a prerequisite request message has not been received."},
        {"31","Not Used"},
        {"32","The specified provisioning mode code is undefined."},
        {"33","Not Used"},
        {"34","The specified time was not accepted by the Intel(r) AMT device"},
        {"35","StartingIndex is invalid."},
        {"36","A parameter is invalid."},
        {"37","An invalid netmask was supplied (a valid netmask is an IP address in which all ‘1’s are before the ‘0’ – e.g. FFFC0000h is valid, FF0C0000h is invalid)."},
        {"38","The operation failed because the Flash wear-out protection mechanism prevented a write to an NVRAM sector."},
        {"39","ME FW did not receive the entire image file."},
        {"40","ME FW received an image file with an invalid signature."},
        {"41","LME can not support the requested version."},
        {"42","The PID must be a 64 bit quantity made up of ASCII codes of some combination of 8 characters – capital alphabets (A–Z), and numbers (0–9)"},
        {"43","The PPS must be a 256 bit quantity made up of ASCII codes of some combination of 32 characters – capital alphabets (A–Z), and numbers (0–9)"},
        {"44","Full BIST test has been blocked"},
        {"45","A TCP/IP connection could not be opened on with the selected port."},
        {"46","Max number of connection reached. LME can not open the requested connection."},
        {"47","Random key generation is in progress."},
        {"48","A randomly generated key does not exist."},
        {"49","Self-generated AMT certificate does not exist."},
        {"1024","Operetion disabled by policy."},
        {"2048","This code establishes a dividing line between status codes which are common to host interface and network interface and status codes which are used by network interface only."},
        {"2049","The OEM number specified in the remote control command is not supported by the Intel(r) AMT device."},
        {"2050","The boot option specified in the remote control command is not supported by the Intel(r) AMT devic."},
        {"2051","The command specified in the remote control command is not supported by the Intel(r) AMT device."},
        {"2052","The special command specified in the remote control command is not supported by the Intel(r) AMT device."},
        {"2053","The handle specified in the command is invalid."},
        {"2054","The password specified in the User ACL is invalid."},
        {"2055","The realm specified in the User ACL is invalid."},
        {"2056","The FPACL or EACL entry is used by an active registration and cannot be removed or modified."},
        {"2057","Essential data is missing on CommitChanges command."},
        {"2058","The parameter specified is a duplicate of an existing value. Returned for a case where duplicate entries are added to FPACL (Factory Partner Allocation Control List) or EACL (Enterprise Access Control List) lists."},
        {"2059","Event Log operation failed due to the current freeze status of the log."},
        {"2060","The device is missing private key material."},
        {"2061","The device is currently generating a keypair. "},
        {"2062","An invalid Key was entered."},
        {"2063","An invalid X.509 certificate was entered."},
        {"2064","Certificate Chain and Private Key do not match."},
        {"2065","The request cannot be satisfied because the maximum number of allowed Kerberos domains has been reached. (The domain is determined by the first 24 Bytes of the SID.)"},
        {"2066","The requested configuration is unsupported."},
        {"2067","A profile with the requested priority already exists."},
        {"2068","Unable to find specified element"},
        {"2069","Invalid User credentials"},
        {"2070","Passphrase is invalid"},
        {"2072","A certificate handle must be chosen before the operation can be completed."},
        {"2075","The command is defined as Audit Log event and can not belogged."},
        {"2076","One of the ME components is not ready for unprovisioning."}
    };
}
