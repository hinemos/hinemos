/*_############################################################################
  _## 
  _##  SNMP4J - LogControl.java  
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
package org.snmp4j.tools.console;

import java.io.*;
import java.text.*;
import java.util.*;

import org.snmp4j.*;
import org.snmp4j.event.*;
import org.snmp4j.log.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.*;
import org.snmp4j.util.*;
import org.snmp4j.security.USM;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;

/**
 * The <code>LogControl</code> tool can be used to control the log levels
 * of agents implementing the SNMP4J-LOG-MIB.
 *
 * Run <code>java -cp SNMP4J.jar org.snmp4j.tools.console.LogControl help</code>
 * to get help and command usage.
 *
 * @author Frank Fock
 * @version 1.10
 * @since 1.10
 */
public class LogControl {

  public static final OID[] SNMP4J_LOGGER_OIDS = {
      new OID("1.3.6.1.4.1.4976.10.1.1.1.1.2.2.1.3"), // snmp4jLogLoggerLevel
      new OID("1.3.6.1.4.1.4976.10.1.1.1.1.2.2.1.4"), // snmp4jLogLoggerEffectiveLevel
      new OID("1.3.6.1.4.1.4976.10.1.1.1.1.2.2.1.6")  // snmp4jLogLoggerRowStatus
  };

  private static final String OPTIONS =
      "+a[s{=MD5}<(MD5|SHA)>] "+
      "+A[s] +b[i{=0}] "+
      "-c[s{=public}] -bc[i{=0}] +u[s{securityName}] -t[l{timeout=5000}] -r[i{retries=0}] "+
      "+l[o<\\n\\n[:\\n\\n]*>] "+
      "+e[o<\\n\\n[:\\n\\n]*>] "+
      "+E[o<\\n\\n[:\\n\\n]*>] "+
      "+n[s] "+
      "+Y[s] +y[s<(DES|3DES|AES|AES128|AES192|AES256)>] "+
      "-v[s{version=3}<(1|2c|3)>] ";

  private static final String COMMAND_PARAMETER =
      "#command[s<(set|list)>] +following[s] ..";

  private static final String[][] COMMANDS =
  {
     {
      "list",
      OPTIONS,
      "#command[s<list>] #address[s<(udp|tcp):.*[/[0-9]+]?>] +filter[s]"
     },

     {
     "set",
     OPTIONS,
     "#command[s<set>] #address[s<(udp|tcp):.*[/[0-9]+]?>] #logger[s] "+
     "#level[s<(NONE|OFF|ALL|TRACE|DEBUG|INFO|WARN|ERROR|FATAL)>]"
     },

     {
     "help",
     "",
     "#command[s<help>] +subject[s<list|set>]"
     }
  };

  private Map parameters;

  public LogControl(Map args) {
    this.parameters = args;
  }

  public void run() {
    String command = (String) ArgumentParser.getValue(parameters, "command", 0);
    if ("help".equals(command)) {
      String subject = (String) ArgumentParser.getValue(parameters, "subject", 0);
      if (subject == null) {
        printUsage();
      }
      else if ("list".equalsIgnoreCase(subject)) {
        printUsageHeader();
        printListUsage();
        printOptions();
      }
      else if ("set".equalsIgnoreCase(subject)) {
        printUsageHeader();
        printListUsage();
        printOptions();
      }
    }
    else {
      TransportMapping<UdpAddress> localTransport = null;
      try {
        localTransport =
            new DefaultUdpTransportMapping(new UdpAddress("0.0.0.0/0"));
        MessageDispatcher md = new MessageDispatcherImpl();
        Snmp snmp = new Snmp(md, localTransport);
        SecurityProtocols.getInstance().addDefaultProtocols();
        OctetString localEngineID = new OctetString(
            MPv3.createLocalEngineID(new OctetString("LogControl"+
            System.currentTimeMillis())));
        md.addMessageProcessingModel(new MPv1());
        md.addMessageProcessingModel(new MPv2c());
        USM usm = new USM(SecurityProtocols.getInstance(),
                          localEngineID,
                          0);
        md.addMessageProcessingModel(new MPv3(usm));

        SnmpConfigurator snmpConfig = new SnmpConfigurator();
        snmpConfig.configure(snmp, parameters);
        snmp.listen();
        Target t = snmpConfig.getTarget(parameters);
        PDUFactory pduFactory = snmpConfig.getPDUFactory(parameters);
        if ("list".equals(command)) {
          listLoggers(snmp, t, pduFactory);
        }
        else if ("set".equals(command)) {
          setLevel(snmp, t, pduFactory);
        }
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  private void setLevel(Snmp snmp, Target target, PDUFactory pduFactory)
      throws IOException
  {
    PDU pdu = pduFactory.createPDU(target);
    OID levelOID = new OID(SNMP4J_LOGGER_OIDS[0]);
    String logger = (String) ArgumentParser.getValue(parameters, "logger", 0);
    if (logger != null) {
      OID loggerIndex = new OctetString(logger).toSubIndex(true);
      String newLevel = (String) ArgumentParser.getValue(parameters, "level", 0);
      levelOID.append(loggerIndex);
      int level = LogLevel.toLevel(newLevel).getLevel();
      pdu.add(new VariableBinding(levelOID, new Integer32(level)));

      ResponseEvent response = snmp.set(pdu, target);
      if (response.getResponse() != null) {
        switch (response.getResponse().getErrorStatus()) {
          case PDU.noError: {
            verifyLoggerModification(snmp, target, pdu, levelOID, logger,
                                     loggerIndex, newLevel, response);
            break;
          }
          case PDU.inconsistentName:
          case PDU.noCreation:
            pdu.clear();
            OID rowStatusOID = new OID(SNMP4J_LOGGER_OIDS[2]);
            rowStatusOID.append(loggerIndex);
            pdu.add(new VariableBinding(levelOID, new Integer32(level)));
            pdu.add(new VariableBinding(rowStatusOID, new Integer32(4)));

            response = snmp.set(pdu, target);

            if ((response.getResponse() != null) &&
                (response.getResponse().getErrorStatus() == PDU.noError)) {
              System.out.println("Logger created successfully.");
            }

            verifyLoggerModification(snmp, target, pdu, levelOID, logger,
                                     loggerIndex, newLevel, response);
            break;
        }
      }
      else {
        System.out.println("SET request timed out.");
      }
    }
  }

  private void verifyLoggerModification(Snmp snmp, Target target, PDU pdu,
                                        OID levelOID, String logger,
                                        OID loggerIndex, String newLevel,
                                        ResponseEvent response) throws
      IOException {
    pdu.clear();
    OID effLevelOID = new OID(SNMP4J_LOGGER_OIDS[1]);
    effLevelOID.append(loggerIndex);
    pdu.add(new VariableBinding(levelOID));
    pdu.add(new VariableBinding(effLevelOID));
    response = snmp.get(pdu, target);
    PDU respPDU = response.getResponse();
    if ((respPDU != null) &&
        (respPDU.getErrorStatus() == PDU.noError) &&
        (!respPDU.get(0).isException()) &&
        (!respPDU.get(1).isException())) {
      PDU resp = response.getResponse();
      LogLevel setLevel = new LogLevel(resp.get(0).getVariable().toInt());
      LogLevel effectiveLevel = new LogLevel(resp.get(1).getVariable().toInt());
      System.out.println("Set logger '"+logger+"' level to "+newLevel+
                         ". Now levels are "+setLevel+" (configured) and "+
                         effectiveLevel+" (effective).");
    }
    else {
      System.out.println("SET request successfully sent, but verfication failed:");
      if (respPDU == null) {
        System.out.println("GET request timed out.");
      }
      else if (respPDU.getErrorStatus() != PDU.noError) {
        System.out.println(PDU.toErrorStatusText(respPDU.getErrorStatus()));
      }
      else {
        System.out.println(respPDU.toString());
      }
    }
  }

  private synchronized void listLoggers(Snmp snmp, Target target, PDUFactory pduFactory) {
    TableUtils tableUtils = new TableUtils(snmp, pduFactory);
    OID lowerBound = null;
    OID upperBound = null;
    String filter = (String) ArgumentParser.getValue(parameters, "filter", 0);
    if (filter != null) {
      OctetString filterString = new OctetString(filter);
      lowerBound = filterString.toSubIndex(true);
      upperBound = lowerBound.nextPeer();
    }
    LoggerListListener lll = new LoggerListListener();
    tableUtils.getTable(target, SNMP4J_LOGGER_OIDS, lll, this,
                        lowerBound, upperBound);
    while (!lll.isFinished()) {
      try {
        wait();
      }
      catch (InterruptedException ex) {
        // ignore
      }
    }
  }

  class LoggerListListener implements TableListener {

    private boolean finished;

    public boolean next(TableEvent event) {
      printLogger(event);
      return event.getStatus() == TableEvent.STATUS_OK;
    }

    public void finished(TableEvent event) {
      printLogger(event);
      finished = true;
      synchronized (event.getUserObject()) {
        event.getUserObject().notify();
      }
    }

    private void printLogger(TableEvent event) {
      if ((event.getStatus() == TableEvent.STATUS_OK) &&
          (event.getIndex() != null)) {
        int rowStatus = event.getColumns()[2].getVariable().toInt();
        if (rowStatus == 1) {
          OctetString name = new OctetString();
          name.fromSubIndex(event.getIndex(), true);
          LogLevel level = new LogLevel(event.getColumns()[0].getVariable().
                                        toInt());
          LogLevel effectiveLevel = new LogLevel(event.getColumns()[1].
                                                 getVariable().toInt());
          System.out.println(name.toString() + "=" + level + "(" +
                             effectiveLevel + ")");
        }
      }
      else if (event.getStatus() != TableEvent.STATUS_OK) {
        System.err.println("Logger list command failed with: "+
                           event.getErrorMessage());
      }
    }

    public boolean isFinished() {
      return finished;
    }
  }

  public static void main(String[] args) {
    try {
      String[] commandSet =
          ArgumentParser.selectCommand(args, OPTIONS, COMMANDS);
      if (commandSet == null) {
        printUsage();
        System.exit(2);
      }
      ArgumentParser parser =
          new ArgumentParser(commandSet[1], commandSet[2]);

      Map commandLineParameters = parser.parse(args);
      LogControl logcontrol = new LogControl(commandLineParameters);
      logcontrol.run();
    }
    catch (ParseException pex) {
      System.out.println(pex.getMessage());
      System.exit(1);
    }
  }

  private static void printUsage() {
    printUsageHeader();
    printHelpUsage();
    printListUsage();
    printSetUsage();
    printOptions();
  }

  private static void printUsageHeader() {
    System.out.println("LogControl <OPTIONS> <COMMAND> <PARAMETERS>");
    System.out.println("where <COMMAND> is one of: ");
  }

  private static void printOptions() {
    System.out.println("valid <OPTIONS> are:");
    System.out.println("  -a  authProtocol      Sets the authentication protocol used to");
    System.out.println("                        authenticate SNMPv3 messages. Valid values are");
    System.out.println("                        MD5 and SHA.");
    System.out.println("  -A  authPassphrase    Sets the authentication pass phrase for authenticated");
    System.out.println("                        SNMPv3 messages.");
    System.out.println("  -bc bootCounter       The boot counter to be used (default is 0)");
    System.out.println("  -c  community         The SNMPv1/v2c community to use (default is 'public')");
    System.out.println("  -e  engineID          Sets the authoritative engine ID of the command");
    System.out.println("                        responder used for SNMPv3 request messages. If not");
    System.out.println("                        supplied, the engine ID will be discovered.");
    System.out.println("  -E  contextEngineID   Sets the context engine ID used for the SNMPv3 scoped");
    System.out.println("                        PDU. The authoritative engine ID will be used for the");
    System.out.println("                        context engine ID, if the latter is not specified.");
    System.out.println("  -l  localEngineID     Sets the local engine ID. This option can be");
    System.out.println("                        used to avoid engine ID clashes through duplicate IDs");
    System.out.println("                        leading to usmStatsNotInTimeWindows reports.");
    System.out.println("  -n  contextName       Sets the target context name for SNMPv3 messages. ");
    System.out.println("                        Default is the empty string.");
    System.out.println("  -u  securityName      The SNMPv3 security name");
    System.out.println("  -t  timeout           SNMP timeout in milli-seconds (default is 5000)");
    System.out.println("  -r  retries           SNMP retries (default is 0) ");
    System.out.println("  -v  1|2c|3            The SNMP version (one of 1, 2c, or 3)");
    System.out.println("  -y  privacyProtocol   Sets the privacy protocol to be used to encrypt");
    System.out.println("                        SNMPv3 messages. Valid values are DES, AES (AES128),");
    System.out.println("                        AES192, AES256, and 3DES(DESEDE).");
    System.out.println("  -Y  privacyPassphrase Sets the privacy pass phrase for encrypted");
    System.out.println("                        SNMPv3 messages.");
  }

  private static void printSetUsage() {
    System.out.println(" set <ADDRESS> <LOGGER> <LEVEL>  Set a LOGGER to a new LEVEL at agent");
    System.out.println("                                 ADDRESS (e.g. 'udp:localhost/161').");
    System.out.println("                                 LOGGER is a fully qualified logger name and");
    System.out.println("                                 LEVEL is one of NONE, OFF, ALL, TRACE, DEBUG,");
    System.out.println("                                 INFO, WARN, ERROR, or FATAL.");
  }

  private static void printListUsage() {
    System.out.println(" list <ADDRESS> [FILTER]         List logger configuration for the agent at");
    System.out.println("                                 ADDRESS (e.g. 'udp:localhost/161') with");
    System.out.println("                                 for all logger names that contain start with");
    System.out.println("                                 the optional parameter string FILTER.");
  }

  private static void printHelpUsage() {
    System.out.println(" help [COMMAND]                  Print usage help for the specified command.");
  }
}
