/*_############################################################################
  _## 
  _##  SNMP4J - JavaLogAdapter.java  
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The <code>JavaLogAdapter</code> log adapter provides logging for SNMP4J
 * through the Java logging (<code>java.util.logging</code>).
 *
 * @author Frank Fock
 * @version 1.9.1
 * @since 1.7.2
 */
public class JavaLogAdapter implements LogAdapter {

  private final Logger logger;

  public JavaLogAdapter(Logger logger) {
    this.logger = logger;
  }

  // ---- Checking methods

  public boolean isDebugEnabled() {
    return isLoggable(LogLevel.DEBUG);
  }

  public boolean isInfoEnabled() {
    return isLoggable(LogLevel.INFO);
  }

  public boolean isWarnEnabled() {
    return isLoggable(LogLevel.WARN);
  }

  // ---- Logging methods

  public void debug(Serializable message) {
    log(LogLevel.DEBUG, message.toString(), null);
  }

  public void info(CharSequence message) {
    log(LogLevel.INFO, message.toString(), null);
  }

  public void warn(Serializable message) {
    log(LogLevel.WARN, message.toString(), null);
  }

  public void error(Serializable message) {
    log(LogLevel.ERROR, message.toString(), null);
  }

  public void error(CharSequence message, Throwable t) {
    log(LogLevel.ERROR, message.toString(), t);
  }

  public void fatal(Object message) {
    log(LogLevel.FATAL, message.toString(), null);
  }

  public void fatal(CharSequence message, Throwable t) {
    log(LogLevel.FATAL, message.toString(), t);
  }

  // ---- Public methods

  public LogLevel getEffectiveLogLevel() {
    return fromJavaToSnmp4jLevel(logger.getLevel());
  }

  public Iterator<Handler> getLogHandler() {
    return Arrays.asList(logger.getHandlers()).iterator();
  }

  public LogLevel getLogLevel() {
    return getEffectiveLogLevel();
  }

  public String getName() {
    return logger.getName();
  }

  public void setLogLevel(LogLevel logLevel) {
    logger.setLevel(fromSnmp4jToJdk(logLevel));
  }

  // ---- Private methods

  private boolean isLoggable(LogLevel logLevel) {
    return logger.isLoggable(fromSnmp4jToJdk(logLevel));
  }

  private void log(LogLevel logLevel, String msg, Throwable t) {
    logger.log(fromSnmp4jToJdk(logLevel), msg, t);
  }

  /**
   * Mapping from <code>org.snmp4j.log.LogLevel</code> to
   * <code>java.util.logging.Level</code>.
   *
   * @param logLevel
   *    The <code>LogLevel</code> to mapped
   * @return the <code>Level</code>
   *    mapped to or <code>null</code> if
   *    <code>null</code> was specified as the parameter.
   */
  private static Level fromSnmp4jToJdk(LogLevel logLevel) {
    if (logLevel == null) {
      return null;
    }
    switch (logLevel.getLevel()) {
      case LogLevel.LEVEL_ALL:
        return Level.ALL;
      case LogLevel.LEVEL_DEBUG:
        return Level.FINE;
      case LogLevel.LEVEL_TRACE:
        return Level.FINEST;
      case LogLevel.LEVEL_INFO:
        return Level.INFO;
      case LogLevel.LEVEL_WARN:
        return Level.WARNING;
      case LogLevel.LEVEL_ERROR:
        return Level.SEVERE;
      case LogLevel.LEVEL_FATAL:
        return Level.SEVERE;
      case LogLevel.LEVEL_OFF:
        return Level.OFF;
      case LogLevel.LEVEL_NONE:
        return Level.OFF;
      default:
        throw new IllegalArgumentException(
            "Mapping not defined from SNMP4J level " + logLevel
            + " to Java logging level");
    }
  }

  /**
   * Mapping from <code>java.util.logging.Level</code> to
   * <code>org.snmp4j.log.LogLevel</code>.
   *
   * @param level
   *    The <code>Level</code> to mapped
   * @return
   *    the <code>LogLevel</code> mapped to or {@link LogLevel#NONE} if
   *    <code>null</code> was specified as the parameter.
   */
  private static LogLevel fromJavaToSnmp4jLevel(Level level) {
    if (level == null) {
      return LogLevel.NONE;
    }
    else if (Level.ALL.equals(level)) {
      return LogLevel.ALL;
    }
    else if (Level.SEVERE.equals(level)) {
      return LogLevel.FATAL;
    }
    else if (Level.WARNING.equals(level)) {
      return LogLevel.WARN;
    }
    else if (Level.INFO.equals(level)) {
      return LogLevel.INFO;
    }
    else if (Level.CONFIG.equals(level)) {
      return LogLevel.DEBUG;
    }
    else if (Level.FINE.equals(level)) {
      return LogLevel.DEBUG;
    }
    else if (Level.FINER.equals(level)) {
      return LogLevel.TRACE;
    }
    else if (Level.FINEST.equals(level)) {
      return LogLevel.TRACE;
    }
    else if (Level.OFF.equals(level)) {
      return LogLevel.DEBUG;
    }
    else {
      throw new IllegalArgumentException("Mapping not defined from Java level "
                                         + level.getName() +
                                         " to SNMP4J logging level");
    }
  }
}
