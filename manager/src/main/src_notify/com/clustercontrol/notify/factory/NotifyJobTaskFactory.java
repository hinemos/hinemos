/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.util.RunJob;
import com.clustercontrol.plugin.api.AsyncTaskFactory;

public class NotifyJobTaskFactory implements AsyncTaskFactory {

	public static final Log log = LogFactory.getLog(NotifyJobTaskFactory.class);

	private static final RunJob _runJob = new RunJob();

	@Override
	public Runnable createTask(Object param) {
		return new NotifyJobTask(param);
	}

	public static class NotifyJobTask implements Runnable {

		private final NotifyRequestMessage msg;

		public NotifyJobTask(Object param) {
			if (param instanceof NotifyRequestMessage) {
				msg = (NotifyRequestMessage)param;
			} else {
				msg = null;
			}
		}

		@Override
		public void run() {

			if (msg == null) {
				log.warn("message is not assigned.");
				return;
			}
			log.debug("run() message : " + msg);


			try {
				_runJob.notify(msg);

			} catch (Exception e) {
				log.warn("asynchronous task failure.", e);
			}

		}

		@Override
		public String toString() {
			if (msg == null) {
				return this.getClass().getSimpleName() + "[NotifyRequestMessage = null]";
			} else {
				return this.getClass().getSimpleName() + "[NotifyRequestMessage = " + msg
						+ "[" + msg.getOutputInfo() + "]]";
			}

		}

	}

}
