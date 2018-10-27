/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.util.SendSyslog;
import com.clustercontrol.plugin.api.AsyncTaskFactory;

public class NotifyLogEscalationTaskFactory implements AsyncTaskFactory {

	public static final Log log = LogFactory.getLog(NotifyLogEscalationTaskFactory.class);

	private static final SendSyslog _sendSyslog = new SendSyslog();

	@Override
	public Runnable createTask(Object param) {
		return new NotifyLogEscalationTask(param);
	}

	public static class NotifyLogEscalationTask implements Runnable {

		private final NotifyRequestMessage msg;

		public NotifyLogEscalationTask(Object param) {
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

			JpaTransactionManager jtm = null;

			try {
				jtm = new JpaTransactionManager();
				jtm.begin();

				_sendSyslog.notify(msg);

				jtm.commit();
			} catch (Exception e) {
				if (jtm != null)
					jtm.rollback();
				log.warn("asynchronous task failure.", e);
			} finally {
				if (jtm != null)
					jtm.close();
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
