/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.factory;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.util.OutputStatus;
import com.clustercontrol.plugin.api.AsyncTaskFactory;

public class NotifyStatusTaskFactory implements AsyncTaskFactory {

	public static final Log log = LogFactory.getLog(NotifyStatusTaskFactory.class);

	private static final OutputStatus _outputStatus = new OutputStatus();

	@Override
	public Runnable createTask(Object param) {
		return new NotifyStatusTask(param);
	}

	public static class NotifyStatusTask implements Runnable {

		private final Object msg;

		public NotifyStatusTask(Object param) {
			msg = param;
		}

		@SuppressWarnings("unchecked")
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

				if (msg instanceof NotifyRequestMessage) {
					_outputStatus.notify((NotifyRequestMessage)msg);
				} else if (msg instanceof List) {
					_outputStatus.notify((List<NotifyRequestMessage>)msg);
				} else {
					log.warn("message type is not expected : " + msg.getClass().getName());
				}

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
			} else if (msg instanceof NotifyRequestMessage) {
				return this.getClass().getSimpleName() + "[NotifyRequestMessage = " + msg
						+ "[" + ((NotifyRequestMessage)msg).getOutputInfo() + "]]";
			} else {
				return this.getClass().getSimpleName() + "[List<NotifyRequestMessage> = " + msg
						+ "[" + msg.toString() + "]]";
			}
		}

	}

}
