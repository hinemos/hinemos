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

package com.clustercontrol.jobmanagement.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.jobmanagement.bean.JobSessionRequestMessage;
import com.clustercontrol.jobmanagement.session.JobRunManagementBean;
import com.clustercontrol.plugin.api.AsyncTaskFactory;

public class CreateJobSessionTaskFactory implements AsyncTaskFactory {

	public static final Log log = LogFactory.getLog(CreateJobSessionTaskFactory.class);

	@Override
	public Runnable createTask(Object param) {
		return new CreateJobSessionTask(param);
	}

	public static class CreateJobSessionTask implements Runnable {

		private final JobSessionRequestMessage msg;

		public CreateJobSessionTask(Object param) {
			if (param instanceof JobSessionRequestMessage) {
				msg = (JobSessionRequestMessage)param;
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

			JobRunManagementBean.makeSession(msg);
		}

		@Override
		public String toString() {
			if (msg == null) {
				return this.getClass().getSimpleName() + "[CreateJobSessionRequestMessage=null]";
			} else {
				return this.getClass().getSimpleName() + "[CreateJobSessionRequestMessage=" + msg
						+ "[" + msg + "]]";
			}
		}
	}

}
