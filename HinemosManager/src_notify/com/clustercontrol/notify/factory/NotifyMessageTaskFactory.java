/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.message.util.HinemosMessageNotifier;
import com.clustercontrol.plugin.api.AsyncTaskFactory;

public class NotifyMessageTaskFactory implements AsyncTaskFactory {

	public static final Log log = LogFactory.getLog(NotifyMessageTaskFactory.class);

	public static final HinemosMessageNotifier sendMessage = new HinemosMessageNotifier();

	@Override
	public Runnable createTask(Object param) {
		return new NotifyMessageTask(param);
	}

	public static class NotifyMessageTask implements Runnable {

		private final NotifyRequestMessage msg;

		public NotifyMessageTask(Object param) {
			if (param instanceof NotifyRequestMessage) {
				msg = (NotifyRequestMessage) param;
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
				sendMessage.notify(msg);
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
