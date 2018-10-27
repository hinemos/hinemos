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
import com.clustercontrol.notify.util.ExecCommand;
import com.clustercontrol.plugin.api.AsyncTaskFactory;

public class NotifyCommandTaskFactory implements AsyncTaskFactory {

	public static final Log log = LogFactory.getLog(NotifyCommandTaskFactory.class);

	private static final ExecCommand _execCommand = new ExecCommand();

	@Override
	public Runnable createTask(Object param) {
		return new NotifyCommandTask(param);
	}

	public static class NotifyCommandTask implements Runnable {

		private final NotifyRequestMessage msg;

		public NotifyCommandTask(Object param) {
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

				_execCommand.notify(msg);

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
