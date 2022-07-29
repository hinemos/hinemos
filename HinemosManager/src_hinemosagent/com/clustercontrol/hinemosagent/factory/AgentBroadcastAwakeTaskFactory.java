/*
 * Copyright (c) 2020 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.factory;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.plugin.api.AsyncTaskFactory;

/**
 * HinemosエージェントへのBroadcastでのawake送信を行う非同期タスクです。
 * 
 * @since 6.2.0
 */
public class AgentBroadcastAwakeTaskFactory  implements AsyncTaskFactory {

	public static final Log log = LogFactory.getLog(AgentBroadcastAwakeTaskFactory.class);

	@Override
	public Runnable createTask(Object param) {
		// パラメータの型が特定のものでない(通常はプログラムエラー)場合は、nullをタスクに渡す。
		if (param instanceof Long) {
			return new AgentBroadcastAwakeTask((Long) param);
		} else {
			log.warn("createTask: Invalid type = " + (param == null ? "null" : param.getClass().getName()));
			return new AgentBroadcastAwakeTask(null);
		}
	}

	public static class AgentBroadcastAwakeTask implements Runnable {
		private Long param; //依頼時のHinemos時刻
		
		public AgentBroadcastAwakeTask( Long param) {
			this.param = param;
		}

		public Long getParameter() {
			return param;
		}
		
		@Override
		public void run() {
			if(log.isDebugEnabled()){
				log.debug("run: generate time=" + param.toString());
			}
			//awake送信対象リストが空になるまで送信
			AgentConnectUtil.execAwakeForFlowControl();

		}
	}

}
