/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
