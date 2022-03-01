/*_############################################################################
  _## 
  _##  SNMP4J - DefaultTimerFactory.java  
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

import java.util.*;

/**
 * This <code>DefaultTimerFactory</code> creates a new <code>Timer</code>
 * which is configured to run as daemon.
 *
 * @author Frank Fock
 * @version 1.9
 * @since 1.9
 */
public class DefaultTimerFactory implements TimerFactory {

  public DefaultTimerFactory() {
  }

  public CommonTimer createTimer() {
    return new TimerAdapter();
  }

  class TimerAdapter implements CommonTimer {

    private Timer timer = new Timer(true);

    public void schedule(TimerTask task, long delay) {
      timer.schedule(task, delay);
    }

    public void cancel() {
      timer.cancel();
    }

    public void schedule(TimerTask task, Date firstTime, long period) {
      timer.schedule(task, firstTime, period);
    }

    public void schedule(TimerTask task, long delay, long period) {
      timer.schedule(task, delay, period);
    }
  }
}
