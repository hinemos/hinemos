/*_############################################################################
  _## 
  _##  SNMP4J - ThreadPoolTest.java  
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

import java.util.Random;

import static org.junit.Assert.assertTrue;

public class ThreadPoolTest {

    private static Random random = new Random();

    @Test
    public void testExecuteWorkerTaskBusyPool() throws Exception {
        ThreadPool threadPool = ThreadPool.create("BusyTestPool", 4);
        threadPool.setTaskManagersBusyTimeoutMillis(0);
        TestTask[] tasks = new TestTask[1000];
        for (int i=0; i<tasks.length; i++) {
            tasks[i] = new TestTask(1);
            threadPool.execute(tasks[i]);
            if ((i+1) % threadPool.taskManagers.size() == 0) {
              Thread.sleep(15);
            }
        }
    }

    private class TestTask implements WorkerTask {

        private int iterations;
        private boolean stop = false;
        private boolean running = false;
        private boolean finished = false;

        public TestTask(int iterations) {
            this.iterations = iterations;
        }

        /**
         * The {@code WorkerPool} might call this method to hint the active
         * {@code WorkTask} instance to complete execution as soon as possible.
         */
        @Override
        public void terminate() {
           // not implemented
        }

        /**
         * Waits until this task has been finished.
         *
         * @throws InterruptedException if the join has been interrupted by another thread.
         */
        @Override
        public void join() throws InterruptedException {
            while (!finished) {
                Thread.sleep(5);
            }
        }

        /**
         * Interrupts this task.
         *
         * @see Thread#interrupt()
         */
        @Override
        public void interrupt() {
            stop = true;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            running = true;
            for (; (!stop) && (iterations > 0); iterations--) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            finished = true;
        }

        public boolean isFinished() {
            return finished;
        }

        public boolean isRunning() {
            return running;
        }
    }
}
