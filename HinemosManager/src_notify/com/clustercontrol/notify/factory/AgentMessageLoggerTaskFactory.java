/*

Copyright (C) 2020 NTT DATA Corporation

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

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.plugin.api.AsyncTaskFactory;
import com.clustercontrol.util.apllog.AgentMessageLogger;

/**
*
* HinemosAgentからの内部メッセージ(sendMessageによる送信)のログ出力を行うクラス<BR>
*
* HinemosAgent内部から送られたメッセージをHinemos上の通知の一種として処理します。
* 
* FIXME 用途を考慮して、AgentMessageLoggerの内部クラスとして配置の予定だったが、AsyncWorkerPlugin側の実装の都合上 このパッケージに配置となっている。
*
*/

public class AgentMessageLoggerTaskFactory implements AsyncTaskFactory {

	public static final Log log = LogFactory.getLog(AgentMessageLoggerTaskFactory.class);

	@Override
	public Runnable createTask(Object param) {
		return new AgentMessageLoggerTask(param);
	}

	public static class AgentMessageLoggerTask implements Runnable {

		private final OutputBasicInfo msg;

		public  AgentMessageLoggerTask(Object param) {
			if ( param instanceof OutputBasicInfo) {
				msg = (OutputBasicInfo)param;
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
			if( log.isDebugEnabled() ){
				log.debug("run() message : " + msg);
			}

			JpaTransactionManager jtm = null;

			try {
				jtm = new JpaTransactionManager();
				jtm.begin();

				AgentMessageLogger.put(msg);

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
				return this.getClass().getSimpleName() + "[OutputBasicInfo = null]";
			} else {
				return this.getClass().getSimpleName() + "[OutputBasicInfo = " + msg + "]";
			}
		}
	}

}
