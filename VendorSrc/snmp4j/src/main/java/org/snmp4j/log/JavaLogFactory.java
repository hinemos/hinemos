/*_############################################################################
  _## 
  _##  SNMP4J - JavaLogFactory.java  
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
package org.snmp4j.log;

import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.*;

/**
 * The <code>JavaLogFactory</code> implements a SNMP4J LogFactory for
 * Java logging. In order to use Java's <code>java.util.logging</code>
 * for logging SNMP4J log messages the static {@link LogFactory#setLogFactory}
 * method has to be used before any SNMP4J class is referenced or instantiated.
 *
 * @author Frank Fock
 * @version 1.7.2
 */
public class JavaLogFactory extends LogFactory {

  public JavaLogFactory() {
  }

  protected LogAdapter createLogger(Class c) {
    return new JavaLogAdapter(Logger.getLogger(c.getName()));
  }

  protected LogAdapter createLogger(String className) {
    return new JavaLogAdapter(Logger.getLogger(className));
  }

  public LogAdapter getRootLogger() {
    return new JavaLogAdapter(Logger.getLogger(""));
  }

  public Iterator loggers() {
    Enumeration<String> loggerNames = LogManager.getLogManager().getLoggerNames();
    return new JavaLogAdapterIterator(loggerNames);
  }

  public class JavaLogAdapterIterator implements Iterator {
    private Enumeration<String> loggerNames;

    protected JavaLogAdapterIterator(Enumeration<String> loggerNames) {
      this.loggerNames = loggerNames;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

    public final boolean hasNext() {
      return loggerNames.hasMoreElements();
    }

    public Object next() {
      String loggerName = loggerNames.nextElement();
      Logger logger = Logger.getLogger(loggerName);
      return new JavaLogAdapter(logger);
    }
  }
}
