/*_############################################################################
  _## 
  _##  SNMP4J - ConsoleLogAdapter.java  
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
import java.util.Iterator;
import java.util.Collections;

/**
 * The <code>ConsoleLogAdapter</code> provides simple logging to the console.
 *
 * @author Frank Fock
 * @version 1.6.1
 * @since 1.6
 */
public class ConsoleLogAdapter implements LogAdapter {

  private static boolean debugEnabled = false;
  private static boolean infoEnabled = true;
  private static boolean warnEnabled = true;

  public ConsoleLogAdapter() {
  }

  /**
   * Logs a debug message.
   *
   * @param message the message to log.
   */
  public void debug(Serializable message) {
    if (debugEnabled) {
      System.out.println(message.toString());
    }
  }

  /**
   * Logs an error message.
   *
   * @param message the message to log.
   */
  public void error(Serializable message) {
    System.err.println(message.toString());
  }

  /**
   * Logs an error message.
   *
   * @param message the message to log.
   * @param throwable the exception that caused to error.
   */
  public void error(CharSequence message, Throwable throwable) {
    System.err.println(message.toString());
  }

  /**
   * Logs a fatal message.
   *
   * @param message the message to log.
   */
  public void fatal(Object message) {
    System.err.println(message.toString());
  }

  /**
   * Logs a fatal message.
   *
   * @param message the message to log.
   * @param throwable the exception that caused to error.
   */
  public void fatal(CharSequence message, Throwable throwable) {
    System.err.println(message.toString());
  }

  /**
   * Logs an informational message.
   *
   * @param message the message to log.
   */
  public void info(CharSequence message) {
    if (infoEnabled) {
      System.out.println(message.toString());
    }
  }

  /**
   * Checks whether DEBUG level logging is activated for this log adapter.
   *
   * @return <code>true</code> if logging is enabled or <code>false</code>
   *   otherwise.
   */
  public boolean isDebugEnabled() {
    return debugEnabled;
  }

  /**
   * Checks whether INFO level logging is activated for this log adapter.
   *
   * @return <code>true</code> if logging is enabled or <code>false</code>
   *   otherwise.
   */
  public boolean isInfoEnabled() {
    return infoEnabled;
  }

  /**
   * Checks whether WARN level logging is activated for this log adapter.
   *
   * @return <code>true</code> if logging is enabled or <code>false</code>
   *   otherwise.
   */
  public boolean isWarnEnabled() {
    return warnEnabled;
  }

  /**
   * Logs an warning message.
   *
   * @param message the message to log.
   */
  public void warn(Serializable message) {
    if (warnEnabled) {
      System.out.println(message.toString());
    }
  }

  public static void setDebugEnabled(boolean isDebugEnabled) {
    debugEnabled = isDebugEnabled;
  }

  public static void setWarnEnabled(boolean isWarnEnabled) {
    warnEnabled = isWarnEnabled;
  }

  public static void setInfoEnabled(boolean isInfoEnabled) {
    infoEnabled = isInfoEnabled;
  }

  public void setLogLevel(LogLevel level) {
    debugEnabled = false;
    warnEnabled = false;
    infoEnabled = false;
    switch (level.getLevel()) {
      case LogLevel.LEVEL_TRACE:
      case LogLevel.LEVEL_DEBUG:
      case LogLevel.LEVEL_ALL:
        debugEnabled = true;
        warnEnabled = true;
        infoEnabled = true;
        break;
      case LogLevel.LEVEL_INFO:
        infoEnabled = true;
        warnEnabled = true;
        break;
      case LogLevel.LEVEL_WARN:
        warnEnabled = true;
        break;
      default:
    }
  }

  public String getName() {
    return "";
  }

  public LogLevel getLogLevel() {
    if (debugEnabled) {
      return LogLevel.DEBUG;
    }
    else if (infoEnabled) {
      return LogLevel.INFO;
    }
    else if (warnEnabled) {
      return LogLevel.WARN;
    }
    return LogLevel.OFF;
  }

  public LogLevel getEffectiveLogLevel() {
    return getLogLevel();
  }

  public Iterator getLogHandler() {
    return Collections.EMPTY_LIST.iterator();
  }
}
