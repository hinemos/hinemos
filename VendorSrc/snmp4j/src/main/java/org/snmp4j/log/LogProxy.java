/*_############################################################################
  _## 
  _##  SNMP4J - LogProxy.java  
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

import java.io.Serializable;
import java.util.*;

/**
 * A <code>LogProxy</code> can be used for late binding loggers. A proxy
 * does not log anything, if its logger member is <code>null</code>.
 * Otherwise it forwards all its messages to that logger. Even if the
 * proxy logger is not set, the <code>LogProxy</code> has a name.
 *
 * @author Frank Fock
 * @version 1.8
 */
public class LogProxy implements LogAdapter {

  private String name;
  private LogAdapter logger;

  public LogProxy(String name) {
    this.name = name;
  }

  public LogProxy(LogAdapter logger) {
    this.logger = logger;
  }

  public void debug(Serializable message) {
    if (logger != null) {
      logger.debug(message);
    }
  }

  public void error(Serializable message) {
    if (logger != null) {
      logger.error(message);
    }
  }

  public void error(CharSequence message, Throwable throwable) {
    if (logger != null) {
      logger.error(message, throwable);
    }
  }

  public void fatal(Object message) {
    if (logger != null) {
      logger.fatal(message);
    }
  }

  public void fatal(CharSequence message, Throwable throwable) {
    if (logger != null) {
      logger.fatal(message, throwable);
    }
  }

  public LogLevel getEffectiveLogLevel() {
    if (logger != null) {
      return logger.getEffectiveLogLevel();
    }
    return LogLevel.OFF;
  }

  public Iterator getLogHandler() {
    if (logger != null) {
      return logger.getLogHandler();
    }
    return Collections.EMPTY_LIST.iterator();
  }

  public LogLevel getLogLevel() {
    if (logger != null) {
      return logger.getLogLevel();
    }
    return LogLevel.OFF;
  }

  public String getName() {
    if (logger != null) {
      logger.getName();
    }
    return name;
  }

  public void info(CharSequence message) {
    if (logger != null) {
      logger.info(message);
    }
  }

  public boolean isDebugEnabled() {
    return (logger != null) && logger.isDebugEnabled();
  }

  public boolean isInfoEnabled() {
    return (logger != null) && logger.isInfoEnabled();
  }

  public boolean isWarnEnabled() {
    return (logger != null) && logger.isWarnEnabled();
  }

  public void setLogLevel(LogLevel level) {
    if (logger != null) {
      logger.setLogLevel(level);
    }
  }

  public void warn(Serializable message) {
    if (logger != null) {
      logger.warn(message);
    }
  }

  /**
   * Gets the proxied logger.
   * @return
   *    a LogAdapter the actually logs the messages.
   */
  public LogAdapter getLogger() {
    return logger;
  }

  /**
   * Sets the logger that logs the log messages logged with this proxy.
   * @param logger
   *    a LogAdapter.
   */
  public void setLogger(LogAdapter logger) {
    this.logger = logger;
  }
}
