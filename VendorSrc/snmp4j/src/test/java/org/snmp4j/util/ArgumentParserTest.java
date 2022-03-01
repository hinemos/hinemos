/*_############################################################################
  _## 
  _##  SNMP4J - ArgumentParserTest.java  
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

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by fock on 12.04.2016.
 */
public class ArgumentParserTest {

  private static final String OPTIONS =
      "+L[s{license},s{key}] +d[s{=off}<(?i)(off|error|warn|info|debug)>] +Dn +s +f[s{=3}<(1|2|3|4)>] ";
  private static final String CONSOLE_OPTIONS =
      "+w[i{=79}] +h[i{=0}] ";
  private static final String MIB_OPTIONS =
      "+M[s] +Msmi[s] +m[s] ";
  private static final String V3_OPTIONS =
      "+a[s<MD5|SHA>] +A[s] +bc[i{=0}] +e[x] +E[x] -rsl[s{=low}<(low|basic|secure)>] "+
          "+y[s<DES|3DES|AES128|AES192|AES256|AES192p|AES256p>] +Y[s] +u[s] +l[x] +n[s] ";
  private static final String TRAP_OPTIONS =
      "+Ta[s{=0.0.0.0}<(\\d){1,3}\\.(\\d){1,3}\\.(\\d){1,3}\\.(\\d){1,3}>] " +
          "-To[s{=1.3.6.1.6.3.1.1.5.1}<([a-zA-Z\\-0-9]*:)?[0-9a-zA-Z\\-\\.]*>] " +
          "+Te[s{=0.0}<([a-zA-Z\\-0-9]*:)?[0-9a-zA-Z\\-\\.]*>] " +
          "+Ts[i{=0}] +Tg[i{=0}] +Tu[l{=0}] ";
  private static final String TLS_OPTIONS =
      "+tls-trust-ca[s] +tls-peer-id[s] +tls-local-id[s] +tls-version[s{=TLSv1}<(TLSv1|TLSv1.1|TLSv1.2)>] "+
          "+Djavax.net.ssl.keyStore +Djavax.net.ssl.keyStorePassword "+
          "+Djavax.net.ssl.trustStore +Djavax.net.ssl.trustStorePassword ";
  private static final String SNMP_OPTIONS =
      "+c[s] +r[i{=1}] +t[i{=5000}] +v[s{=3}<1|2c|3>] +Ors[i{=65535}] +p ";
  private static final String SNMPV3_ONLY_OPTIONS =
      "+c[s] +r[i{=1}] +t[i{=5000}] +v[s{=3}<3>] +Ors[i{=65535}] ";
  private static final String BULK_OPTIONS =
      "-Cr[i{=10}] -Cn[i{=0}] ";
  private static final String TABLE_OPTIONS =
      "+Cil[s] +Ciu[s] +Ch +CH +Ci +Cl +Cw[i] +Cf[s] +Cc[i] +Cb[i{=10}] +CB "+
          "+Otd +OtCSV +OttCSV ";
  private static final String WALK_OPTIONS =
      "+ilo ";
  private static final String USM_CHANGE_OPTIONS =
      "+CE[x] +createAndWait ";

  private static final String ALL_OPTIONS = CONSOLE_OPTIONS + MIB_OPTIONS +
      OPTIONS + V3_OPTIONS + TLS_OPTIONS + TRAP_OPTIONS +
      SNMP_OPTIONS + BULK_OPTIONS + TABLE_OPTIONS + WALK_OPTIONS +
      USM_CHANGE_OPTIONS;


  private static final String ADDRESS_PARAMETER =
      "#address[s<((udp|tcp):)?.*[/[0-9]+]?>] ";

  private static final String OID_PARAMETER =
      "#OID[s<([A-Z]+[a-zA-Z\\-0-9]*:)?([a-z][a-zA-Z\\-0-9])?([0-9]+[\\.][0-9]+)?[^=]*(=(\\{[iusxdnotab]\\})?.*)?>] ";

  private static final String OPT_OID_PARAMETER =
      "+OID[s<([a-zA-Z\\-0-9]*:)?[0-9a-zA-Z\\-\\.#]*(=(\\{[iusxdnotab]\\})?.*)?>] ";

  private static final String OIDLIST_PARAMETER =
      OID_PARAMETER + ".. ";

  private static final String OPT_OIDLIST_PARAMETER =
      OPT_OID_PARAMETER + ".. ";

  private static final String USER_PARAMETER =
      "#user[s] +cloneFromUser[s] +cloneFromEngineID[x]";

  private static final String[][] COMMANDS = {

      {
          "oid",
          OPTIONS + CONSOLE_OPTIONS + MIB_OPTIONS,
          "#command[s<oid>] #mode[s<find|find-by-smi>] +ONAME[s] "
      }, {
      "smi",
      OPTIONS + CONSOLE_OPTIONS + MIB_OPTIONS,
      "#command[s<smi>] "+OIDLIST_PARAMETER
  }, {
      "mib",
      OPTIONS + CONSOLE_OPTIONS + MIB_OPTIONS,
      "#command[s<mib>] #mode[s<add|del|list"+/*|verify*/">] +file[s]"
  }, {
      "set",
      OPTIONS + CONSOLE_OPTIONS + SNMP_OPTIONS + V3_OPTIONS + TLS_OPTIONS + MIB_OPTIONS,
      "#command[s<set>] " + ADDRESS_PARAMETER + OIDLIST_PARAMETER
  }, {
      "get",
      OPTIONS + CONSOLE_OPTIONS + SNMP_OPTIONS + V3_OPTIONS + TLS_OPTIONS + MIB_OPTIONS,
      "#command[s<get>] " + ADDRESS_PARAMETER + OIDLIST_PARAMETER
  }, {
      "getnext",
      OPTIONS + CONSOLE_OPTIONS + SNMP_OPTIONS + V3_OPTIONS + TLS_OPTIONS + MIB_OPTIONS,
      "#command[s<getnext>] " + ADDRESS_PARAMETER + OIDLIST_PARAMETER
  }, {
      "getbulk",
      OPTIONS + CONSOLE_OPTIONS + SNMP_OPTIONS + V3_OPTIONS + TLS_OPTIONS + BULK_OPTIONS
          + MIB_OPTIONS,
      "#command[s<getbulk>] " + ADDRESS_PARAMETER + OIDLIST_PARAMETER
  }, {
      "inform",
      OPTIONS + CONSOLE_OPTIONS + SNMP_OPTIONS + V3_OPTIONS + TLS_OPTIONS + TRAP_OPTIONS
          + MIB_OPTIONS,
      "#command[s<inform>] " + ADDRESS_PARAMETER + OPT_OIDLIST_PARAMETER
  }, {
      "trap",
      OPTIONS + CONSOLE_OPTIONS + SNMP_OPTIONS + V3_OPTIONS + TLS_OPTIONS + TRAP_OPTIONS
          + MIB_OPTIONS,
      "#command[s<trap>] " + ADDRESS_PARAMETER + OPT_OIDLIST_PARAMETER
  }, {
      "v1trap",
      OPTIONS + CONSOLE_OPTIONS + SNMP_OPTIONS + TRAP_OPTIONS + MIB_OPTIONS,
      "#command[s<v1trap>] " + ADDRESS_PARAMETER + OPT_OIDLIST_PARAMETER
  }, {
      "table",
      OPTIONS + CONSOLE_OPTIONS + SNMP_OPTIONS + V3_OPTIONS + TLS_OPTIONS + BULK_OPTIONS +
          TABLE_OPTIONS + MIB_OPTIONS,
      "#command[s<table>] " + ADDRESS_PARAMETER + OIDLIST_PARAMETER
  }, {
      "walk",
      OPTIONS + CONSOLE_OPTIONS + SNMP_OPTIONS + V3_OPTIONS + TLS_OPTIONS + BULK_OPTIONS +
          TABLE_OPTIONS + MIB_OPTIONS + WALK_OPTIONS,
      "#command[s<walk>] " + ADDRESS_PARAMETER + OID_PARAMETER
  }, {
      "dump-snapshot",
      OPTIONS + CONSOLE_OPTIONS,
      "#command[s<dump-snapshot>] #file[s]"
  }, {
      "create-snapshot",
      OPTIONS + CONSOLE_OPTIONS + SNMP_OPTIONS + V3_OPTIONS + TLS_OPTIONS + BULK_OPTIONS +
          TABLE_OPTIONS + MIB_OPTIONS + WALK_OPTIONS,
      "#command[s<create-snapshot>] #file[s] "+ADDRESS_PARAMETER + OID_PARAMETER
  }, {
      "listen",
      OPTIONS + CONSOLE_OPTIONS + SNMP_OPTIONS + V3_OPTIONS + TLS_OPTIONS + MIB_OPTIONS,
      "#command[s<listen>] " + ADDRESS_PARAMETER
  }, {
      "defaults",
      OPTIONS + CONSOLE_OPTIONS + V3_OPTIONS + TLS_OPTIONS + TRAP_OPTIONS + SNMP_OPTIONS +
          BULK_OPTIONS + TABLE_OPTIONS + MIB_OPTIONS,
      "#command[s<defaults>] #mode[s<list|save|reset>]"
  }, {
      "help",
      OPTIONS + CONSOLE_OPTIONS,
      "#command[s<help>] +subject[s<all|create-snapshot|defaults|dump-snapshot|"+
          "get|getbulk|getnext|inform|license|listen|oid|mib|"+
          "set|smi|table|trap|usmKey|usmUser|v1trap|version|walk>]"
  }, {
      "example",
      OPTIONS + CONSOLE_OPTIONS,
      "#command[s<example>] +subject[s<create-snapshot|defaults|dump-snapshot|"+
          "get|getbulk|getnext|inform|license|listen|oid|mib|"+
          "set|smi|table|trap|v1trap|version|walk>]"
  }, {
      "version",
      OPTIONS + CONSOLE_OPTIONS,
      "#command[s<version>]"
  }, {
      "license",
      OPTIONS + CONSOLE_OPTIONS,
      "#command[s<license>]"
  },
      {
          "usmUser",
          OPTIONS + CONSOLE_OPTIONS + V3_OPTIONS + SNMPV3_ONLY_OPTIONS +
              USM_CHANGE_OPTIONS,
          "#command[s<usmUser>] +subcommand[s<create|delete>] "+
              ADDRESS_PARAMETER + USER_PARAMETER
      },
      {
          "usmKey",
          OPTIONS + CONSOLE_OPTIONS + V3_OPTIONS + SNMPV3_ONLY_OPTIONS +
              USM_CHANGE_OPTIONS,
          "#command[s<usmKey>] +subcommand[s<auth|priv|authPriv>] "+
              ADDRESS_PARAMETER + "#old[s] #new[s] +user[s] "
      }
  };


  @Test
  public void selectCommand() throws Exception {
    String[] args = { "-l", "0x80:00:13:70:01:c0:a8:02:0e","-a","SHA", "-A", "SHADESAuthPassword", "-y", "DES", "-Y",
        "SHADESPrivPassword", "-u", "SHADES", "-CE", "0x80:00:13:70:02:c0:a8:02:0e", "usmUser", "create",
        "127.0.0.1/4700", "newuser", "SHADES", "0x80:00:13:70:01:c0:a8:02:0f" };
    String[] commandSet = ArgumentParser.selectCommand(args, ALL_OPTIONS, COMMANDS);
    //System.out.println(Arrays.asList(commandSet));
    assertEquals("usmUser", commandSet[0]);
    ArgumentParser parser =
        new ArgumentParser(commandSet[1], commandSet[2]);
    Map<String,List<?>> commandLineParameters = parser.parse(args);
    assertEquals("low", commandLineParameters.get("rsl").get(0));
    //System.out.println(commandLineParameters);
  }

  @Test
  public void parseFormat() throws Exception {
    String format = "#address[s<(udp|tcp):.*[/[0-9]+]?>{=udp:127.0.0.1/161}] ..";
    ArgumentParser argumentParser = new ArgumentParser("", format);
    assertEquals("{address=ArgumentFormat[option=address,parameter=true,vararg=true,mandatatory=true,"+
        "parameters=[ArgumentParameter[name=,type=2,patttern=(udp|tcp):.*[/[0-9]+]?,defaultValue=udp:127.0.0.1/161]]]}",
        argumentParser.getParameterFormat().toString());
    Map<String,List<?>> commandLineParameters = argumentParser.parse(new String[] { });
    assertTrue(commandLineParameters.containsKey("address"));
  }

}
