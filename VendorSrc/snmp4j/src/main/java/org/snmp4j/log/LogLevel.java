/*_############################################################################
  _## 
  _##  SNMP4J - LogLevel.java  
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

public class LogLevel {

  public static final int LEVEL_NONE = 0;
  public static final int LEVEL_OFF = 1;
  public static final int LEVEL_ALL = 2;
  public static final int LEVEL_TRACE = 3;
  public static final int LEVEL_DEBUG = 4;
  public static final int LEVEL_INFO = 5;
  public static final int LEVEL_WARN = 6;
  public static final int LEVEL_ERROR = 7;
  public static final int LEVEL_FATAL = 8;

  private static final String[] LEVEL_STRINGS = {
    "NONE","OFF","ALL","TRACE","DEBUG","INFO","WARN","ERROR","FATAL"
  };

  public static final LogLevel NONE = new LogLevel(LEVEL_NONE);
  public static final LogLevel OFF = new LogLevel(LEVEL_OFF);
  public static final LogLevel ALL = new LogLevel(LEVEL_ALL);
  public static final LogLevel TRACE = new LogLevel(LEVEL_TRACE);
  public static final LogLevel DEBUG = new LogLevel(LEVEL_DEBUG);
  public static final LogLevel INFO = new LogLevel(LEVEL_INFO);
  public static final LogLevel WARN = new LogLevel(LEVEL_WARN);
  public static final LogLevel ERROR = new LogLevel(LEVEL_ERROR);
  public static final LogLevel FATAL = new LogLevel(LEVEL_FATAL);

  private int level;

  public LogLevel(int level) {
    if ((level < 0) || (level > 8)) {
      throw new IllegalArgumentException("Unknown log level "+level);
    }
    this.level = level;
  }

  public LogLevel(String levelString) {
    this(levelFromString(levelString));
  }

  /**
   * Convert a level string "OFF", "ALL", "DEBUG", etc. into a level number.
   * @param levelString
   *    one of the level strings "OFF", "ALL", "TRACE", "DEBUG", "INFO", "WARN",
   *    "INFO", "ERROR", and "FATAL".
   * @return
   *    a number greater than zero if the level string could be converted into a level,
   *    zero otherwise.
   */
  public static int levelFromString(String levelString) {
    int ind = -1;
    for (int i=0; i<LEVEL_STRINGS.length; i++) {
      if (LEVEL_STRINGS[i].equals(levelString)) {
        ind = i;
        break;
      }
    }
    return ind;
  }

  /**
   * Returns a {@code LogLevel} object for the specified level string.
   * @param levelString
   *    one of the level strings "OFF", "ALL", "TRACE", "DEBUG", "INFO", "WARN",
   *    "ERROR", and "FATAL".
   * @return
   *    one of the {@code LogLevel} constants defined by this class.
   * @since 1.7.2
   */
  public static LogLevel toLevel(String levelString) {
    return new LogLevel(levelFromString(levelString));
  }

  public int getLevel() {
    return level;
  }

  public String toString() {
    return LEVEL_STRINGS[level];
  }

}
